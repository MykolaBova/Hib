/**
CREATE TABLE SEQUENCES (
SEQUENCE_ID VARCHAR(30) NOT NULL,
ID INTEGER NOT NULL,
CONSTRAINT PK_SEQUENCE PRIMARY KEY(SEQUENCE_ID)
);

INSERT INTO SEQUENCES VALUES('MY_SEQUENCE', 0); // INSERT ROW FOR EACH TABLE...

-HIGH/LOW ID pattern implementation-
 */
package org.julp.util.db;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.math.BigInteger;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.db.DBServices;
import org.julp.db.DBServicesUtils;

public class KeyGenerator {

    protected String table;    // required
    protected String idColumn; // required
    protected String sequenceNameColumn; // required for non-externalSequence
    protected int increment = 10; // any number greater than zero
    /** DO NOT USE THE SAME CONNECTION AS THE REST OF THE APPLICATION SINCE CONNECTION FOR KeyGenerator COMMITS ALWAYS!!! */
    protected DBServices dbServices;
    protected DBServicesUtils dbUtils;
    protected int retryMax = 3;   // any number greater than zero
    protected long timeout = 250; // any number greater than zero
    protected String sqlState = "23000"; // SQLSTATE - duplicate key
    protected int vendorErrorCode = Integer.MIN_VALUE;
    protected int isolationLevel = - 1;
    protected boolean isolationLevelSet = false;
    protected Map lowValues = Collections.synchronizedMap(new HashMap());
    protected Map hiValues = Collections.synchronizedMap(new HashMap());
    protected Class keyClass = Integer.class;   // any class which extends java.lang.Number
    protected boolean externalSequence = false; // used with "real" sequences: Oracle, PostgreSQL, etc...
    protected String select;
    protected String update;
    protected String insert; // if not exists automatically creates new sequence upon request
    protected boolean createSequenceOnDemand = true;
    protected Number initialValue = 0; // not used with "real" sequences
    protected Set sequences;
    protected boolean cacheStatements;
    private final transient Logger logger = Logger.getLogger(getClass().getName());
    
    public KeyGenerator() {
    }

    public KeyGenerator(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public KeyGenerator(String table, String sequenceNameColumn, String idColumn, int isolationLevel) {
        this.table = table;
        this.sequenceNameColumn = sequenceNameColumn;
        this.idColumn = idColumn;
        this.isolationLevel = isolationLevel;
    }

    public KeyGenerator(String table, String sequenceNameColumn, String idColumn) {
        this.table = table;
        this.sequenceNameColumn = sequenceNameColumn;
        this.idColumn = idColumn;
    }

    public KeyGenerator(String table, String sequenceNameColumn, String idColumn, int isolationLevel, boolean externalSequence) {
        this.table = table;
        this.sequenceNameColumn = sequenceNameColumn;
        this.idColumn = idColumn;
        this.isolationLevel = isolationLevel;
        this.externalSequence = externalSequence;
    }

    public KeyGenerator(String table, String sequenceNameColumn, String idColumn, boolean externalSequence) {
        this.table = table;
        this.sequenceNameColumn = sequenceNameColumn;
        this.idColumn = idColumn;
        this.externalSequence = externalSequence;
    }

    public void init() {
        if (!externalSequence && this.table == null) {
            throw new IllegalStateException("Table name is missing");
        }
        if (!externalSequence && this.sequenceNameColumn == null) {
            throw new IllegalStateException("Sequence column name is missing");
        }
        if (this.idColumn == null) {
            throw new IllegalStateException("ID column name is missing");
        }
        if (dbServices == null) {
            throw new IllegalStateException("DBServices not set");
        }
        if (!externalSequence) {
            // SELECT ID FROM SEQUENCES WHERE SEQUENCE_NAME = ? ${sequence}
            select = "SELECT " + idColumn + " FROM " + table + " WHERE " + sequenceNameColumn + " = ?";
            // UPDATE SEQUENCES SET ID = ID + 10 WHERE SEQUENCE_NAME = ? AND ID = ? ${current ID}
            update = "UPDATE " + table + " SET " + idColumn + " = " + idColumn + " + " + String.valueOf(increment) + " WHERE " + sequenceNameColumn + " = ? AND " + idColumn + " = ?";
            if (createSequenceOnDemand) {
                // INSERT INTO SEQUENCES (SEQUENCE_NAME, ID) VALUES (?, 0) ${sequence}
                insert = "INSERT INTO " + table + " (" + sequenceNameColumn + ", " + idColumn + ") VALUES (?, " + initialValue.toString() + ")";
            }
            String allSequences = "SELECT " + sequenceNameColumn + " FROM " + table;
            try {
                sequences = Collections.synchronizedSet(new HashSet(dbUtils.getSingleColumnResultAsList(allSequences)));
            } catch (SQLException sqle) {
                throw new RuntimeException(sqle);
            }
        }
    }

    public synchronized Number getNextValue(String sequence) throws SQLException {
        if (!externalSequence && this.select == null) {
            throw new IllegalStateException("KeyGenerator not initialized");
        }
        if (!externalSequence && !sequences.contains(sequence) && createSequenceOnDemand) { // new sequence
            Collection param = new ArrayList(1);
            param.add(sequence);
            dbServices.execute(insert, param);
            sequences.add(sequence);
        }
        Object value = lowValues.get(sequenceNameColumn);
        Number hiValue = null;
        if (value == null) {
            value = getSeed(sequence);
            lowValues.put(sequenceNameColumn, value);
            if (keyClass.getName().equals("java.lang.Integer")) {
                hiValue = ((Number) value).intValue() + increment;
            } else if (keyClass.getName().equals("java.lang.Long")) {
                hiValue = ((Number) value).longValue() + increment;
            } else if (keyClass.getName().equals("java.math.BigInteger")) {
                hiValue = new BigInteger(value.toString()).add(new BigInteger(String.valueOf(increment)));
            } else if (keyClass.getName().equals("java.math.BigDecimal")) {
                hiValue = new BigDecimal(value.toString()).add(new BigDecimal(String.valueOf(increment)));
            } else {
                try {
                    hiValue = new BigDecimal(value.toString()).add(new BigDecimal(String.valueOf(increment)));
                    Constructor c = keyClass.getConstructor(String.class);
                    hiValue = (Number) c.newInstance(hiValue.toString());
                } catch (InvocationTargetException ite) {
                    throw new SQLException(ite.getTargetException().toString());
                } catch (Exception e) {
                    throw new SQLException(e.toString());
                }
            }
            hiValues.put(sequenceNameColumn, hiValue);
        } else {
            if (keyClass.getName().equals("java.lang.Integer")) {
                if (((Number) value).intValue() < ((Number) hiValues.get(sequenceNameColumn)).intValue() - 1) {
                    value = Integer.valueOf(((Number) value).intValue() + 1);
                    lowValues.put(sequenceNameColumn, value);
                } else {
                    value = getSeed(sequence);
                    lowValues.put(sequenceNameColumn, value);
                    hiValues.put(sequenceNameColumn, ((Number) value).intValue() + increment);
                }
            } else if (keyClass.getName().equals("java.lang.Long")) {
                if (((Number) value).longValue() < ((Number) hiValues.get(sequenceNameColumn)).longValue() - 1) {
                    value = Long.valueOf(((Number) value).longValue() + 1);
                    lowValues.put(sequenceNameColumn, value);
                } else {
                    value = getSeed(sequence);
                    lowValues.put(sequenceNameColumn, value);
                    hiValues.put(sequenceNameColumn, ((Number) value).longValue() + increment);
                }
            } else if (keyClass.getName().equals("java.math.BigInteger")) {
                if (new BigInteger(value.toString()).compareTo(new BigInteger(hiValues.get(sequenceNameColumn).toString())) == -1) {
                    value = new BigInteger(value.toString()).add(BigInteger.ONE);
                    lowValues.put(sequenceNameColumn, value);
                } else {
                    value = getSeed(sequence);
                    lowValues.put(sequenceNameColumn, value);
                    hiValues.put(sequenceNameColumn, new BigInteger(value.toString()).add(new BigInteger(String.valueOf(increment))));
                }
            } else if (keyClass.getName().equals("java.math.BigDecimal")) {
                if (new BigDecimal(value.toString()).compareTo(new BigDecimal(hiValues.get(sequenceNameColumn).toString())) == -1) {
                    value = new BigDecimal(value.toString()).add(BigDecimal.ONE);
                    lowValues.put(sequenceNameColumn, value);
                } else {
                    value = getSeed(sequence);
                    lowValues.put(sequenceNameColumn, value);
                    hiValues.put(sequenceNameColumn, new BigDecimal(value.toString()).add(new BigDecimal(String.valueOf(increment))));
                }
            } else {
                if (new BigDecimal(value.toString()).compareTo(new BigDecimal(hiValues.get(sequenceNameColumn).toString())) == -1) {
                    value = new BigDecimal(value.toString()).add(BigDecimal.ONE);
                    try {
                        value = new BigDecimal(value.toString()).add(BigDecimal.ONE);
                        Constructor c = keyClass.getConstructor(String.class);
                        lowValues.put(sequenceNameColumn, c.newInstance(value.toString()));
                    } catch (InvocationTargetException ite) {
                        throw new SQLException(ite.getTargetException().toString());
                    } catch (Exception e) {
                        throw new SQLException(e.toString());
                    }
                } else {
                    value = getSeed(sequence);
                    lowValues.put(sequenceNameColumn, value);
                    try {
                        Constructor c = keyClass.getConstructor(String.class);
                        BigDecimal temp = new BigDecimal(value.toString()).add(new BigDecimal(String.valueOf(increment)));
                        hiValues.put(sequenceNameColumn, c.newInstance(temp.toString()));
                    } catch (InvocationTargetException ite) {
                        throw new SQLException(ite.getTargetException().toString());
                    } catch (Exception e) {
                        throw new SQLException(e.toString());
                    }
                }
            }
        }
        return (Number) value;
    }

    public synchronized Number getCurrentValue(String sequence) throws SQLException {
        if (!externalSequence && this.select == null) {
            throw new IllegalStateException("KeyGenerator not initialized");
        }
        if (!externalSequence && !sequences.contains(sequence) && createSequenceOnDemand) { // new sequence
            Collection param = new ArrayList(1);
            param.add(sequence);
            dbServices.execute(insert, param);
            sequences.add(sequence);
        }
        Object currentValue;
        try {
            if (externalSequence) {
                if (table == null || table.trim().length() == 0) {
                    currentValue = dbUtils.getSingleValue("SELECT " + idColumn);
                } else {
                    currentValue = dbUtils.getSingleValue("SELECT " + idColumn + " FROM " + table);
                }
            } else {
                Collection param = new ArrayList(1);
                param.add(sequence);
                currentValue = dbUtils.getSingleValue(select, param);
            }
        } catch (SQLException sqle) {
            throw sqle;
        } finally {
            dbServices.release(false);
        }
        return (Number) currentValue;
    }

    protected synchronized Number getSeed(String sequence) throws SQLException {
        if (!externalSequence && this.select == null) {
            throw new IllegalStateException("KeyGenerator not initialized");
        }
        Object id = null;
        boolean success = false;
        if (externalSequence) {
            try {
                id = dbUtils.getSingleValue("SELECT " + idColumn + " FROM " + table);
                success = true;
            } finally {
                dbServices.release(false);
            }
        } else {
            try {
                int retryCount = 0;
                if (isolationLevel != -1 && !isolationLevelSet) { // set it only once
                    dbServices.getConnection().setTransactionIsolation(isolationLevel);
                    isolationLevelSet = true;
                }
                dbServices.beginTran();
                while (!success && retryCount <= retryMax) {
                    try {
                        Collection param = new ArrayList(1);
                        param.add(sequence);
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::param: " + param);
                        }
                        id = dbUtils.getSingleValue(select, param);
                        param.add(id);
                        dbServices.execute(update, param);
                        success = true;
                        if (keyClass.getName().equals("java.lang.Integer")) {
                            id = Integer.valueOf(((Number) id).intValue() + increment);
                        } else if (keyClass.getName().equals("java.lang.Long")) {
                            id = Long.valueOf(((Number) id).intValue() + increment);
                        } else if (keyClass.getName().equals("java.math.BigInteger")) {
                            id = new BigInteger(id.toString()).add(new BigInteger(String.valueOf(increment)));
                        } else if (keyClass.getName().equals("java.math.BigDecimal")) {
                            id = new BigDecimal(id.toString()).add(new BigDecimal(String.valueOf(increment)));
                        } else {
                            Constructor c = keyClass.getConstructor(String.class);
                            id = c.newInstance(String.valueOf(increment));
                        }
                        break;
                    } catch (InstantiationException ie) {
                        throw new SQLException(ie.toString());
                    } catch (IllegalAccessException iae) {
                        throw new SQLException(iae.toString());
                    } catch (InvocationTargetException ite) {
                        throw new SQLException(ite.getTargetException().toString());
                    } catch (SQLException sqle) {
                        if ((sqle.getSQLState() != null && sqle.getSQLState().equals(sqlState)) || sqle.getErrorCode() == vendorErrorCode) {
                            // duplicate key: retry
                        } else {
                            throw sqle; // some other problem
                        }
                    }
                    retryCount++;
                    try {
                        wait(timeout);
                    } catch (InterruptedException ie) {
                        throw new SQLException("Cannot get unique ID");
                    }
                }
            } catch (NoSuchMethodException nsme) {
                throw new SQLException(nsme.getMessage()); // should not happend
            } catch (SQLException sqle) {
                throw sqle;
            } finally {
                dbServices.commitTran(); // commit no matter what
                dbServices.release(false);
            }
        }
        if (!success) {
            throw new SQLException("Cannot get unique ID");
        }
        return (Number) id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getIdColumn() {
        return idColumn;
    }

    public void setIdColumn(String idColumn) {
        this.idColumn = idColumn;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public DBServices getDBServices() {
        return dbServices;
    }

    public void setDBServices(DBServices dbServices) {
        this.dbServices = dbServices;
        this.dbServices.setCacheStatements(cacheStatements);
        this.dbUtils = new DBServicesUtils(dbServices);
    }

    public int getRetryMax() {
        return retryMax;
    }

    public void setRetryMax(int retryMax) {
        this.retryMax = retryMax;
    }

    public String getSqlState() {
        return sqlState;
    }

    public void setSqlState(String sqlState) {
        this.sqlState = sqlState;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("Timeout must be greater than zero");
        }
        this.timeout = timeout;
    }

    public int getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public String getSequenceNameColumn() {
        return sequenceNameColumn;
    }

    public void setSequenceNameColumn(String sequenceNameColumn) {
        this.sequenceNameColumn = sequenceNameColumn;
    }

    public Class getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(Class keyClass) {
        if (Number.class.isAssignableFrom(keyClass)) {
            this.keyClass = keyClass;
        } else {
            throw new IllegalArgumentException("KeyClass must extend java.lang.Number");
        }
        try {
            Constructor c = keyClass.getConstructor(String.class);
            Object obj = c.newInstance("0");
            String stringValue = obj.toString();
            if (stringValue == null && !(stringValue.equals("0") && !(stringValue.startsWith("0.")))) {
                throw new IllegalArgumentException(keyClass.getName() + ".toString() must return String value of Number");
            }
        } catch (InvocationTargetException ite) {
            throw new IllegalArgumentException(ite.getTargetException().toString());
        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
    }

    public boolean isExternalSequence() {
        return externalSequence;
    }

    public void setExternalSequence(boolean externalSequence) {
        this.externalSequence = externalSequence;
    }

    public Number getInitialValue() {
        return initialValue;
    }

    public void setInitialValue(Number initialValue) {
        this.initialValue = initialValue;
    }

    public boolean isCacheStatements() {
        return cacheStatements;
    }

    public void setCacheStatements(boolean cacheStatements) {
        this.cacheStatements = cacheStatements;
    }

    public boolean isCreateSequenceOnDemand() {
        return createSequenceOnDemand;
    }

    public void setCreateSequenceOnDemand(boolean createSequenceOnDemand) {
        this.createSequenceOnDemand = createSequenceOnDemand;
    }

    public void reset() throws SQLException {
        table = null;
        idColumn = null;
        sequenceNameColumn = null;
        increment = 10;
        retryMax = 3;
        timeout = 250;
        vendorErrorCode = Integer.MIN_VALUE;
        isolationLevel = - 1;
        isolationLevelSet = false;
        lowValues.clear();
        hiValues.clear();
        keyClass = Integer.class;
        externalSequence = false;
        select = null;
        update = null;
        insert = null;
        initialValue = 0;
        cacheStatements = false;
        sequences.clear();
        dbServices.release(true);
    }

}

package org.julp.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.AbstractDomainObjectFactory;
import org.julp.Converter;
import org.julp.DataAccessException;
import org.julp.DataHolder;
import org.julp.DataWriter;
import org.julp.DomainObject;
import org.julp.MetaData;
import org.julp.PersistentState;

@SuppressWarnings("rawtypes")
public class DBDataWriter<T> implements DataWriter<T> {

    private static final long serialVersionUID = -3375528510404563635L;
    protected int removedCount = 0;
    protected int createdCount = 0;
    protected int modifiedCount = 0;
    protected List<DomainObject<T>> removedObjects = null;
    protected List<DomainObject<T>> createdObjects = null;
    protected List<DomainObject<T>> modifiedObjects = null;
    protected List<StatementHolder> generatedSQL = null;
    protected AbstractDomainObjectFactory<T> objectFactory = null;
    protected String fullTableName = null;
    protected String modifiedCatalog = null;
    protected String modifiedSchema = null;
    protected String modifiedTable = null;
    protected static final Object[] EMPTY_ARG = new Object[0];
    protected Throwable persistenceError = null;
    protected boolean exceptionOnEmptyObjectList = false;
    protected boolean lowerCaseKeywords = true;
    protected static final String DOT = ".";
    /**
     * This is JDBC utility
     */
    protected DBServices dbServices = null;
    protected boolean batchEnabled = true;
    protected Map<Enum<?>, Object> options;
    protected Converter converter;
    /**
     * Set sequence or disable update/insert/delete
     */
    protected DataModificationSequence[] dataModificationSequence;
    /**
     * Disable DB modifications
     */
    protected boolean readOnly = false;
    /**
     * Current Optimistic lock setting. Default is KEY_COLUMNS (Primary Key only)
     */
    protected OptimisticLock optimisticLock = OptimisticLock.KEY_COLUMNS;
    /*
     Throw exception if PreparedStatement.executeUpdate() does not return 1
     for each DomainObject.
     It means that the row in DB Table was modified or deleted
     by another user/process.
     */
    protected boolean throwOptimisticLockDeleteException = true;
    protected boolean throwOptimisticLockUpdateException = true;
    protected boolean throwFailedInsertException = true;
    /**
     * END of Optimistic lock settings ********************************
     */
    /**
     * Target DB catalog
     */
    protected String catalog = null;
    /**
     * If overrideCatalogName == true and catalog from this.metaData is null than use catalog member variable
     */
    protected boolean overrideCatalogName = false;
    /**
     * Target DB schema
     */
    protected String schema = null;
    /**
     * Target DB Table
     */
    protected String table = null;
    /**
     * Do not execute INSERTS/UPDATES/DELETES - just generate SQL and parameters
     */
    protected boolean generateSQLOnly = false;
    /**
     * Some databases (like hsqldb) failed when UPDATE statement is using TABLE_NAME.COLUMN_NAME in SET clause. INSERT also failed
     */
    protected boolean noFullColumnName = false;
    /**
     * Throw Exception or ignore if DomainObject has less fields than mapping
     */
    protected boolean throwMissingFieldException = false;
    /**
     * Sometimes instead of updates it is necessary to do delete/insert
     */
    private boolean removeAndCreateInsteadOfStore = false;
    protected DBMetaData<T> metaData;
    protected Map<String, Collection<Collection>> removedBatch;
    protected Map<String, Collection<Collection>> createdBatch;
    protected Map<String, Collection<Collection>> modifiedBatch;
    /**
     *  In case of multiple tables get PrimaryKey only for updatable table
     */
    protected boolean updatableTablePKOnly = false;
    private static final transient Logger logger = Logger.getLogger(DBDataWriter.class.getName());
    public enum Options {
        exceptionOnEmptyObjectList,
        readOnly,
        dataModificationSequence,
        dbServices,
        optimisticLock,
        throwOptimisticLockDeleteException,
        throwOptimisticLockUpdateException,
        throwFailedInsertException,
        overrideCatalogName,
        catalog,
        schema,
        table,
        generateSQLOnly,
        noFullColumnName,
        throwMissingFieldException,
        exceptionHandler,
        removeAndCreateInsteadOfStore,
        batchEnabled,
        updatableTablePKOnly,
        lowerCaseKeywords
    };

    public DBDataWriter() {
    }

    public DBDataWriter(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    @SuppressWarnings("unchecked")
    protected void init() throws SQLException {
        removedCount = 0;
        createdCount = 0;
        modifiedCount = 0;
        if (metaData == null) {
            if (objectFactory.getMetaData() == null) {
                 objectFactory.populateMetaData();
            }
            setMetaData(objectFactory.getMetaData());
        }

        Collection tables = metaData.getTables().values();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::metaData.getTables(): " + metaData.getTables());
        }

        if (tables.size() == 1) {
            //there is only one table for this object, otherwise you must set catalog and/or schema and table to update/delete/insert each table
            if (metaData.getCatalogName(1) == null && !overrideCatalogName) {
                setCatalog(this.catalog);
            }
            setSchema(metaData.getSchemaName(1));
            setTable(metaData.getTableName(1));
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::catalog: " + this.catalog);
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::schema: " + getSchema());
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::table: " + getTable());
            }
        }
        if (!objectFactory.isReadOnly()) { // don't need PK if this is ReadOnly
            if (updatableTablePKOnly) {
                Set<String> pk = getPrimaryKey(this.getCatalog(), getSchema(), getTable());
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::pk: " + pk);
                }
            } else {
                /**
                 * Get all distinct table names for class (DomainObject)
                 */
                Iterator<String[]> iter = tables.iterator();
                while (iter.hasNext()) {
                    String[] tableId = iter.next();
                    if (tableId[0] == null && overrideCatalogName) {
                        tableId[0] = this.catalog;
                    }
                    Set<String> pk = getPrimaryKey(tableId[0], tableId[1], tableId[2]);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::pk: " + pk);
                    }
                }
            }
        }

        modifiedCatalog = this.getCatalog();
        modifiedSchema = this.getSchema();
        modifiedTable = this.getTable();

        persistenceError = null;
        if (removedObjects == null) {
            removedObjects = new ArrayList();
        }
        if (createdObjects == null) {
            createdObjects = new ArrayList();
        }
        if (modifiedObjects == null) {
            modifiedObjects = new ArrayList();
        }
        if (modifiedTable == null) {
            if (tables.size() > 0) {
                throw new SQLException(objectFactory.getDomainClass() + " is mapped to " + tables.size() + " tables. You must specify table name to modify. Use DBDataWriter.setTable(<TABLE_NAME>) or DBDataWriter.setOptions(Map{DBDataWriter.Options.table, <TABLE_NAME>}) or DomainObjectFactory.setOptions(Map{DBDataWriter.Options.table, <TABLE_NAME>})");
            } else {
                throw new SQLException("Table name to modify is missing"); // should not happened
            }
        }
        fullTableName = modifiedTable;
        if (modifiedSchema != null) {
            fullTableName = modifiedSchema + DOT + fullTableName;
            if (modifiedCatalog != null) {
                fullTableName = modifiedCatalog + DOT + fullTableName;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean writeData(AbstractDomainObjectFactory objectFactory) {
        if (objectFactory.isReadOnly()) {
            setPersistenceError(new DataAccessException("Read Only"));
            return false;
        }
        this.objectFactory = objectFactory;

        try {
            this.init();
        } catch (SQLException sqle) {
            setPersistenceError(sqle);
            return false;
        }
        boolean success = true;
        boolean empty = false;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::writeData()::objectFactory.getObjectList(): " + objectFactory.getObjectList());
        }
        if (objectFactory.getObjectList() == null || objectFactory.getObjectList().isEmpty()) {
            empty = true;
        }
        removedObjects = objectFactory.getRemovedObjects();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::writeData()::removedObjects: " + removedObjects);
        }
        if ((removedObjects == null || removedObjects.isEmpty()) && empty) {
            if (exceptionOnEmptyObjectList) {
                setPersistenceError(new SQLException("Nothing to write"));
                return false;
            }
            return true;
        }
        if (getDataModificationSequence() != null) {
            Iterator<DomainObject> it = objectFactory.getObjectList().iterator();
            while (it.hasNext()) {
                DomainObject<T> domainObject = (DomainObject<T>) it.next();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::writeData()::domainObject: " + domainObject);
                }
                if (domainObject.getPersistentState() == PersistentState.REMOVED && domainObject.getOriginalValues() != null && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    removedObjects.add(domainObject);
                } else if (domainObject.getPersistentState() == PersistentState.STORED && domainObject.getOriginalValues() != null && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    modifiedObjects.add(domainObject);
                } else if (domainObject.getPersistentState() == PersistentState.CREATED && domainObject.getOriginalValues() == null || domainObject.getOriginalValues().getFieldsCount() < 1) {
                    createdObjects.add(domainObject);
                }
            }

            for (int i = 0; i < getDataModificationSequence().length; i++) {
                if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_DELETE) {
                    removeObjects();
                } else if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_UPDATE) {
                    storeObjects();
                } else if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_INSERT) {
                    createObjects();
                }
            }
        } else { // no DML sequence
            objectFactory.getObjectList().addAll((List<DomainObject<T>>) removedObjects);
            Iterator<DomainObject<T>> it = objectFactory.getObjectList().iterator();
            while (it.hasNext()) {
                DomainObject<T> domainObject = (DomainObject<T>) it.next();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::writeData()::domainObject: " + domainObject);
                }
                if (domainObject.getPersistentState() == PersistentState.REMOVED && domainObject.getOriginalValues() != null && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    if (!removeObject(domainObject)) {
                        getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to remove: " + domainObject));
                        return false;
                    }
                } else if (domainObject.getPersistentState() == PersistentState.STORED && domainObject.getOriginalValues() != null && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    if (!storeObject(domainObject)) {
                        getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to store: " + domainObject));
                        return false;
                    }
                } else if (domainObject.getPersistentState() == PersistentState.CREATED && (domainObject.getOriginalValues() == null || domainObject.getOriginalValues().getFieldsCount() < 1)) {
                    if (!createObject(domainObject)) {
                        getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to create: " + domainObject));
                        return false;
                    }
                } else if (domainObject.getPersistentState() == PersistentState.ORIGINAL) {
                    // do nothing
                } else {
                    setPersistenceError(new SQLException("Invalid PersistentState"));
                    return false;
                }
            }
        }
        if (batchEnabled) {
            executeBatch();
        }
        return success;
    }

    protected void executeBatch() {
        try {
            if (getDataModificationSequence() != null) {
                for (int i = 0; i < getDataModificationSequence().length; i++) {
                    if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_DELETE) {
                        if (removedBatch != null) {
                            for (Map.Entry<String, Collection<Collection>> entry : removedBatch.entrySet()) {
                                int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                                List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                                int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                                setRemovedCount(removedCount + rowsAffected);
                                if (dhList.size() > 1) {
                                    getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                                    logger.finest("julp::" + this + "::removedObjectBatch: " + dhList);
                                }
                            }
                        }
                    } else if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_UPDATE) {
                        if (modifiedBatch != null) {
                            for (Map.Entry<String, Collection<Collection>> entry : modifiedBatch.entrySet()) {
                                int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                                List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                                int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                                setModifiedCount(modifiedCount + rowsAffected);
                                if (dhList.size() > 1) {
                                    getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                                    logger.finest("julp::" + this + "::storedObjectBatch: " + dhList);
                                }
                            }
                        }
                    } else if (getDataModificationSequence(i) == DataModificationSequence.DATA_MODIFICATION_SEQUENCE_INSERT) {
                        if (createdBatch != null) {
                            for (Map.Entry<String, Collection<Collection>> entry : createdBatch.entrySet()) {
                                int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                                List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                                int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                                setCreatedCount(createdCount + rowsAffected);
                                if (dhList.size() > 1) {
                                    getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                                    logger.finest("julp::" + this + "::createdObjectBatch: " + dhList);
                                }
                            }
                        }
                    }
                }
            } else {
                if (removedBatch != null) {
                    for (Map.Entry<String, Collection<Collection>> entry : removedBatch.entrySet()) {
                        int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                        List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                        int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                        setRemovedCount(removedCount + rowsAffected);
                        if (dhList.size() > 1) {
                            getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                            logger.finest("julp::" + this + "::removedObjectBatch: " + dhList);
                        }
                    }
                }
                if (modifiedBatch != null) {
                    for (Map.Entry<String, Collection<Collection>> entry : modifiedBatch.entrySet()) {
                        int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                        List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                        int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                        setModifiedCount(modifiedCount + rowsAffected);
                        if (dhList.size() > 1) {
                            getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                            logger.finest("julp::" + this + "::storedObjectBatch: " + dhList);
                        }
                    }
                }
                if (createdBatch != null) {
                    for (Map.Entry<String, Collection<Collection>> entry : createdBatch.entrySet()) {
                        int[] n = getDBServices().executeBatch(entry.getKey(), entry.getValue());
                        List<DataHolder> dhList = getDBServices().calcAffectedRows(n);
                        int rowsAffected = (Integer) dhList.get(dhList.size() - 1).getFieldValue("AFFECTED_ROWS");
                        setCreatedCount(createdCount + rowsAffected);
                        if (dhList.size() > 1) {
                            getDBServices().populateWarnings(new SQLWarning("executeBatch(): " + dhList));
                            logger.finest("julp::" + this + "::createdObjectBatch: " + dhList);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.throwing(getClass().getCanonicalName(), "executeBatch", e);
            setPersistenceError(e);
        }
    }

    protected boolean removeObjects() {
        if (removedObjects.isEmpty()) {
            return true;
        }
        Iterator<DomainObject<T>> iter = removedObjects.iterator();
        while (iter.hasNext()) {
            DomainObject<T> domainObject = (DomainObject<T>) iter.next();
            if (!removeObject(domainObject)) {
                getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to remove: " + domainObject));
                return false;
            }
        }
        return true;
    }

    protected boolean storeObjects() {
        Iterator<DomainObject<T>> iter = modifiedObjects.iterator();
        while (iter.hasNext()) {
            DomainObject<T> domainObject = (DomainObject<T>) iter.next();
            if (!storeObject(domainObject)) {
                getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to store: " + domainObject));
                return false;
            }
        }
        return true;
    }

    protected boolean createObjects() {
        Iterator<DomainObject<T>> iter = createdObjects.iterator();
        while (iter.hasNext()) {
            DomainObject<T> domainObject = (DomainObject<T>) iter.next();
            if (!createObject(domainObject)) {
                getDBServices().populateWarnings(new SQLWarning("writeData() - Failed to create: " + domainObject));
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    protected boolean removeObject(DomainObject<T> domainObject) {
        String fieldName = null;
        Collection params = new ArrayList();
        StringBuilder sql = new StringBuilder(lowerCaseKeywords ? "delete from "  : "DELETE FROM ");
        try {
            sql.append(fullTableName);
            sql.append(lowerCaseKeywords ? " where " : " WHERE ");
            /**
             * Always use PrimaryKeys columns in WHERE statement
             */
            Set<String> pk = getPrimaryKey(modifiedCatalog, modifiedSchema, modifiedTable);
            Iterator<String> iter = pk.iterator();
            while (iter.hasNext()) {
                String columnName = iter.next();   // this is full column name
//                if (columnName.indexOf(DOT) > -1) {
//                   fieldName = (String) objectFactory.getMapping().get(columnName);
//                } else {
//                   fieldName = (String) objectFactory.getMapping().get(columnName);
//                }
                int n = -1;
                if (columnName.indexOf(DOT) > -1) {
                    n = metaData.getColumnIndexByFullColumnName(columnName);
                } else {
                    n = metaData.getColumnIndexByColumnName(columnName);
                }
                fieldName = (String) metaData.getFieldName(n);

                Object origValue = domainObject.getOriginalValue(fieldName);
                if (origValue == null) {
                    sql.append(columnName).append(lowerCaseKeywords ? " is null and " : " IS NULL AND ");
                } else {
                    sql.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                    params.add(origValue);
                }
            }
            if (getOptimisticLock() == OptimisticLock.KEY_COLUMNS) { // built already
                /**
                 * For DELETE make KEY_AND_MODIFIED_COLUMNS and KEY_AND_UPDATEBLE_COLUMNS the same: include all table columns in WHERE statement
                 */
            } else if (getOptimisticLock() == OptimisticLock.KEY_AND_MODIFIED_COLUMNS || getOptimisticLock() == OptimisticLock.KEY_AND_UPDATEBLE_COLUMNS) {
                for (int i = 1; i <= metaData.getFieldCount(); i++) {
                    catalog = metaData.getCatalogName(i);
                    schema = metaData.getSchemaName(i);
                    table = metaData.getTableName(i);
                    if (modifiedCatalog != null) {
                        if (!modifiedCatalog.equals(catalog)) {
                            continue;
                        }
                    }
                    if (modifiedSchema != null) {
                        if (!modifiedSchema.equals(schema)) {
                            continue;
                        }
                    }
                    if (modifiedTable != null) {
                        if (!modifiedTable.equals(table)) {
                            continue;
                        }
                    }
                    String columnName = metaData.getFullColumnName(i);
                    if (sql.indexOf(columnName) > -1) {
                        continue; // this is PK column - processed already
                    }
                    if (!metaData.isWritable(i) || metaData.isReadOnly(i)) {
                        continue;
                    }
                    fieldName = metaData.getFieldName(i);
                    try {
                        Object origValue = domainObject.getOriginalValue(fieldName);
                        if (origValue == null) {
                            sql.append(columnName).append(lowerCaseKeywords ? " is null and " : " IS NULL AND ");
                        } else {
                            sql.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                            params.add(origValue);
                        }
                    } catch (Throwable t) {
                        throw new DataAccessException(t.getMessage());
                    }
                }
            } else {
                throw new DataAccessException("Invalid Optimistic Lock settings");
            }
            int len = sql.length();
            sql = sql.delete(len - 5, len - 1);
            if (isGenerateSQLOnly()) {
                if (generatedSQL == null) {
                    generatedSQL = new ArrayList<>();
                }
                StatementHolder entry = new StatementHolder(sql.toString(), params);
                generatedSQL.add(entry);
            } else if (batchEnabled) {
                if (removedBatch == null) {
                    removedBatch = new LinkedHashMap<>();
                }
                if (!removedBatch.containsKey(sql.toString())) {
                    Collection c = new HashSet<>();
                    c.add(params);
                    removedBatch.put(sql.toString(), c);
                } else {
                    removedBatch.get(sql.toString()).add(params);
                }
            } else {
                int rowsAffected = getDBServices().execute(sql.toString(), params);
                if (rowsAffected != 1) {
                    if (isThrowOptimisticLockDeleteException()) {
                        throw new DataAccessException("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected);
                    } else {
                        getDBServices().populateWarnings(new SQLWarning("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected));
                        return false;
                    }
                } else {
                    setRemovedCount(removedCount + rowsAffected);
                }
            }
        } catch (SQLException e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::removeObject::Exception in removeObject: " + e);
            }
            setPersistenceError(e);
            logger.throwing(getClass().getCanonicalName(), "removeObject", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected boolean storeObject(DomainObject domainObject) {
        if (removeAndCreateInsteadOfStore) {
            int oldRemovedCount = removedCount;
            int oldCreatedCount = createdCount;
            boolean success1 = removeObject(domainObject);
            boolean success2 = createObject(domainObject);
            removedCount = oldRemovedCount;
            createdCount = oldCreatedCount;
            if (!success1 || !success2) {
                return false;
            } else {
                modifiedCount++;
                return true;
            }
        }
        String fieldName = null;
        StringBuilder sql = new StringBuilder(lowerCaseKeywords ? "update " : "UPDATE ");
        List params = new ArrayList();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::storeObject");
        }
        try {
            sql.append(fullTableName).append(lowerCaseKeywords ? " set " : " SET ");
            for (int i = 1; i <= metaData.getFieldCount(); i++) {
                catalog = metaData.getCatalogName(i);
                schema = metaData.getSchemaName(i);
                table = metaData.getTableName(i);
                if (modifiedCatalog != null) {
                    if (!modifiedCatalog.equals(catalog)) {
                        continue;
                    }
                }
                if (modifiedSchema != null) {
                    if (!modifiedSchema.equals(schema)) {
                        continue;
                    }
                }
                if (modifiedTable != null) {
                    if (!modifiedTable.equals(table)) {
                        continue;
                    }
                }
                if (!metaData.isWritable(i) || metaData.isReadOnly(i)) {
                    continue;
                }
                String columnName = null;
                if (isNoFullColumnName()) {
                    columnName = metaData.getColumnName(i);
                } else {
                    columnName = metaData.getFullColumnName(i);
                }
                fieldName = metaData.getFieldName(i);
                Object value = readValue(domainObject, metaData.getReadMethod(i));
                Object origValue = domainObject.getOriginalValue(fieldName);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::storeObject::fieldName: " + fieldName + "::value: " + value + "::origValue: " + origValue + "::isWritable: " + metaData.isWritable(i) + "::isReadOnly: " + metaData.isReadOnly(i));
                }
                if (origValue == null) {
                    if (value == null) {
                        continue; // the same value - do not include in update
                    } else {
                        sql.append(columnName).append(" = ?, ");
                        params.add(value);
                    }
                } else {
                    if (value == null) {
                        sql.append(columnName).append(lowerCaseKeywords ? " = null, " : " = NULL, ");
                    } else {
                        if (!origValue.equals(value)) {
                            sql.append(columnName).append(" = ?, ");
                            params.add(value);
                        } else {
                            continue;
                        }
                    }
                }
            }
            int idx = sql.lastIndexOf(", ");
            if (idx > -1) {
                sql = sql.delete(idx, idx + 2);
            } else { // No modified column's data
                domainObject.setPersistentState(PersistentState.ORIGINAL);
                return true;
            }
            /**
             * Building WHERE
             */
            /**
             * Always use PrimaryKeys columns in WHERE statement
             */
            StringBuilder where = new StringBuilder(lowerCaseKeywords ? " where " : " WHERE ");
            Set<String> pk = getPrimaryKey(modifiedCatalog, modifiedSchema, modifiedTable);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::pk: " + pk + " modifiedSchema: " + modifiedSchema + " modifiedTable: " + modifiedTable);
            }
            Iterator<String> iter = pk.iterator();
            while (iter.hasNext()) {
                String columnName = iter.next();
                int n = -1;
                if (columnName.indexOf(DOT) > -1) {
                    n = metaData.getColumnIndexByFullColumnName(columnName);
                } else {
                    n = metaData.getColumnIndexByColumnName(columnName);
                }
                fieldName = metaData.getFieldName(n);
                //fieldName = (String) objectFactory.getMapping().get(columnName);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::storeObject::columnName: " + columnName + " fieldName: " + fieldName);
                }
                Object origValue = domainObject.getOriginalValue(fieldName);
                if (origValue == null) {
                    where.append(columnName).append(lowerCaseKeywords ? " is null and " : " IS NULL AND ");
                } else {
                    where.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                    params.add(origValue);
                }
            }
            if (getOptimisticLock() == OptimisticLock.KEY_COLUMNS) {
                idx = where.lastIndexOf(lowerCaseKeywords ? " and " : " AND ");
                where = where.delete(idx + 1, idx + 5);
            }
            if (getOptimisticLock() == OptimisticLock.KEY_COLUMNS) {
                // ... built already
            } else if (getOptimisticLock() == OptimisticLock.KEY_AND_MODIFIED_COLUMNS) {
                for (int i = 1; i <= metaData.getFieldCount(); i++) {
                    catalog = metaData.getCatalogName(i);
                    schema = metaData.getSchemaName(i);
                    table = metaData.getTableName(i);
                    if (modifiedCatalog != null) {
                        if (!modifiedCatalog.equals(catalog)) {
                            continue;
                        }
                    }
                    if (modifiedSchema != null) {
                        if (!modifiedSchema.equals(schema)) {
                            continue;
                        }
                    }
                    if (modifiedTable != null) {
                        if (!modifiedTable.equals(table)) {
                            continue;
                        }
                    }
                    String columnName = metaData.getFullColumnName(i);
                    if (where.indexOf(columnName) > -1) {
                        continue; // this is PK column - processed already
                    }
                    if (!metaData.isWritable(i) || metaData.isReadOnly(i)) {
                        continue;
                    }
                    fieldName = metaData.getFieldName(i);
                    Object value = readValue(domainObject, metaData.getReadMethod(i));
                    Object origValue = domainObject.getOriginalValue(fieldName);
                    if (origValue == null) {
                        if (value == null) {
                            continue;
                        } else {
                            where.append(columnName).append(lowerCaseKeywords ? " is null and " : " IS NULL AND ");
                        }
                    } else {
                        if (value == null) {
                            where.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                            params.add(origValue);
                        } else {
                            if (origValue.equals(value)) {
                                continue;
                            } else {
                                where.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                                params.add(origValue);
                            }
                        }
                    }
                }
            } else if (getOptimisticLock() == OptimisticLock.KEY_AND_UPDATEBLE_COLUMNS) {
                for (int i = 1; i <= metaData.getFieldCount(); i++) {
                    catalog = metaData.getCatalogName(i);
                    schema = metaData.getSchemaName(i);
                    table = metaData.getTableName(i);
                    if (modifiedCatalog != null) {
                        if (!modifiedCatalog.equals(catalog)) {
                            continue;
                        }
                    }
                    if (modifiedSchema != null) {
                        if (!modifiedSchema.equals(schema)) {
                            continue;
                        }
                    }
                    if (modifiedTable != null) {
                        if (!modifiedTable.equals(table)) {
                            continue;
                        }
                    }
                    String columnName = metaData.getFullColumnName(i);
                    if (where.indexOf(columnName) > -1) {
                        continue; // this is PK column - processed already
                    }
                    if (!metaData.isWritable(i) || metaData.isReadOnly(i)) {
                        continue;
                    }
                    fieldName = metaData.getFieldName(i);
                    //Object value = readValue(domainObject, metaData.getReadMethod(i));
                    Object origValue = domainObject.getOriginalValue(fieldName);
                    if (origValue == null) {
                        where.append(columnName).append(lowerCaseKeywords ? " is null and " : " IS NULL AND ");
                    } else {
                        where.append(columnName).append(lowerCaseKeywords ? " = ? and " : " = ? AND ");
                        params.add(origValue);
                    }
                }
            } else {
                throw new DataAccessException("Invalid Optimistic Lock settings");
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::where 1 : " + where);
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::KEY_COLUMNS 1 : " + OptimisticLock.KEY_COLUMNS);
            }
            if (getOptimisticLock() != OptimisticLock.KEY_COLUMNS) {
                idx = where.length();
                where = where.delete(idx - 5, idx - 1);
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::where 2 : " + where);
            }
            sql.append(where);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::storeObject: " + sql + "; params: " + params);
            }
            if (isGenerateSQLOnly()) {
                if (generatedSQL == null) {
                    generatedSQL = new ArrayList<>();
                }
                StatementHolder entry = new StatementHolder(sql.toString(), params);
                generatedSQL.add(entry);
            } else if (batchEnabled) {
                if (modifiedBatch == null) {
                    modifiedBatch = new LinkedHashMap<>();
                }
                if (!modifiedBatch.containsKey(sql.toString())) {
                    Collection c = new HashSet<>();
                    c.add(params);
                    modifiedBatch.put(sql.toString(), c);
                } else {
                    modifiedBatch.get(sql.toString()).add(params);
                }
            } else {
                int rowsAffected = getDBServices().execute(sql.toString(), params);
                if (rowsAffected != 1) {
                    if (isThrowOptimisticLockUpdateException()) {
                        throw new DataAccessException("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected);
                    } else {
                        getDBServices().populateWarnings(new SQLWarning("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected));
                        return false;
                    }
                } else {
                    setModifiedCount(modifiedCount + rowsAffected);
                }
            }
        } catch (SQLException e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::storeObject::Exception in storeObject: " + e);
            }
            setPersistenceError(e);
            logger.throwing(getClass().getCanonicalName(), "storeObject", e);
            return false;
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    protected boolean createObject(DomainObject domainObject) {
        StringBuilder sql = new StringBuilder(lowerCaseKeywords ? "insert into " : "INSERT INTO ");
        List params = new ArrayList();
        try {
            sql.append(fullTableName);
            sql.append(" (");
            for (int i = 1; i <= metaData.getFieldCount(); i++) {
                catalog = metaData.getCatalogName(i);
                schema = metaData.getSchemaName(i);
                table = metaData.getTableName(i);
                if (modifiedCatalog != null) {
                    if (!modifiedCatalog.equals(catalog)) {
                        continue;
                    }
                }
                if (modifiedSchema != null) {
                    if (!modifiedSchema.equals(schema)) {
                        continue;
                    }
                }
                if (modifiedTable != null) {
                    if (!modifiedTable.equals(table)) {
                        continue;
                    }
                }
                if (!metaData.isWritable(i) || metaData.isReadOnly(i)) {
                    continue;
                }
                String columnName = null;
                if (isNoFullColumnName()) {
                    columnName = metaData.getColumnName(i);
                } else {
                    columnName = metaData.getFullColumnName(i);
                }
                sql.append(columnName).append(", ");
                Object value = readValue(domainObject, metaData.getReadMethod(i));
                params.add(value);
            }
            int idx = sql.lastIndexOf(", ");
            sql = sql.delete(idx, idx + 2);
            sql.append(lowerCaseKeywords ? ") values (" : ") VALUES (");
            ListIterator li = params.listIterator();
            while (li.hasNext()) {
                Object value = li.next();
                if (value == null) {
                    sql.append(lowerCaseKeywords ? "null, " : "NULL, ");
                    li.remove();
                } else {
                    sql.append("?, ");
                }
            }
            idx = sql.lastIndexOf(",");
            sql = sql.delete(idx, idx + 2);
            sql.append(")");
            if (isGenerateSQLOnly()) {
                if (generatedSQL == null) {
                    generatedSQL = new ArrayList<>();
                }
                StatementHolder entry = new StatementHolder(sql.toString(), params);      
                generatedSQL.add(entry);
            } else if (batchEnabled) {
                if (createdBatch == null) {
                    createdBatch = new LinkedHashMap<>();
                }
                if (!createdBatch.containsKey(sql.toString())) {
                    Collection c = new HashSet<>();
                    c.add(params);
                    createdBatch.put(sql.toString(), c);
                } else {
                    createdBatch.get(sql.toString()).add(params);
                }
            } else {
                int rowsAffected = getDBServices().execute(sql.toString(), params);
                if (rowsAffected != 1) {
                    //getDBServices().populateWarnings(new SQLWarning("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected));
                    //return false;
                    throw new DataAccessException("Failed: " + sql + " " + params + ", rowsAffected: " + rowsAffected);
                } else {
                    setCreatedCount(createdCount + rowsAffected);
                }
            }
        } catch (SQLException e) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::createObject::Exception in createObject: " + e);
            }
            setPersistenceError(e);
            logger.throwing(getClass().getCanonicalName(), "createObject", e);
            return false;
        }
        return true;
    }

    /**
     * Getter for property removedCount.
     *
     * @return Value of property removedCount.
     *
     */
    @Override
    public int getRemovedCount() {
        return removedCount;
    }

    /**
     * Setter for property removedCount.
     *
     * @param removedCount New value of property removedCount.
     *
     */
    protected void setRemovedCount(int removedCount) {
        this.removedCount = removedCount;
    }

    /**
     * Getter for property createdCount.
     *
     * @return Value of property createdCount.
     *
     */
    @Override
    public int getCreatedCount() {
        return createdCount;
    }

    /**
     * Setter for property createdCount.
     *
     * @param createdCount New value of property createdCount.
     *
     */
    protected void setCreatedCount(int createdCount) {
        this.createdCount = createdCount;
    }

    /**
     * Getter for property modifiedCount.
     *
     * @return Value of property modifiedCount.
     *
     */
    @Override
    public int getModifiedCount() {
        return modifiedCount;
    }

    /**
     * Setter for property modifiedCount.
     *
     * @param modifiedCount New value of property modifiedCount.
     *
     */
    protected void setModifiedCount(int modifiedCount) {
        this.modifiedCount = modifiedCount;
    }

    @Override
    public void reset() {
        removedCount = 0;
        createdCount = 0;
        modifiedCount = 0;
        //objectList = null;
        generatedSQL = null;
        removedObjects = null;
        createdObjects = null;
        modifiedObjects = null;
        removedBatch = null;
        createdBatch = null;
        modifiedBatch = null;
        //metaData = null;
        fullTableName = null;
        modifiedTable = null;
        modifiedSchema = null;
        modifiedCatalog = null;
        persistenceError = null;
    }

    /**
     * Getter for property generatedSQL.
     *
     * @return Value of property generatedSQL. make sure you call this method BEFORE DomainObjectFactory.synchronizePersistentState()
     */
    public List<StatementHolder> getGeneratedSQL() {
        return generatedSQL;
    }

    /**
     * Getter for property persistenceError.
     *
     * @return Value of property persistenceError.
     *
     */
    @Override
    public Throwable getPersistenceError() {
        return persistenceError;
    }

    /**
     * Setter for property persistenceError.
     *
     * @param persistenceError New value of property persistenceError.
     *
     */
    public void setPersistenceError(java.lang.Throwable persistenceError) {
        this.persistenceError = persistenceError;
    }

    @Override
    public Converter getConverter() {
        return converter;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    /**
     * Getter for property modifiedCatalog.
     *
     * @return Value of property modifiedCatalog.
     *
     */
    public java.lang.String getModifiedCatalog() {
        return modifiedCatalog;
    }

    /**
     * Setter for property modifiedCatalog.
     *
     * @param modifiedCatalog New value of property modifiedCatalog.
     *
     */
    public void setModifiedCatalog(java.lang.String modifiedCatalog) {
        this.modifiedCatalog = modifiedCatalog;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * Getter for property generatedSQLasXML.
     *
     * @return Value of property generatedSQLasXML. make sure you call this method BEFORE DomainObjectFactory.synchronizePersistentState()
     */
    public java.lang.String getGeneratedSQLasXML() {
        if (this.generatedSQL == null || this.generatedSQL.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<generated-sql>\n");
        Iterator<StatementHolder> iter = this.generatedSQL.iterator();
        while (iter.hasNext()) {
            sb.append(" <sql>\n");
            StatementHolder holder = iter.next();
            sb.append("  <statement><![CDATA[").append(holder.getQuery()).append("]]></statement>\n");
            Collection params = holder.getParams();
            if (params != null && !params.isEmpty()) {
                sb.append("  <params>\n");
                Iterator paramIter = params.iterator();
                while (paramIter.hasNext()) {
                    Object param = paramIter.next();
                    sb.append("   <param>\n");
                    sb.append("    <value>").append("<![CDATA[").append(param.toString()).append("]]>").append("</value>\n");
                    sb.append("    <datatype>").append(param.getClass().getName()).append("</datatype>\n");
                    sb.append("   </param>\n");
                }
                sb.append("  </params>\n");
            }
            sb.append(" </sql>\n");
        }
        sb.append("</generated-sql>\n");
        return sb.toString();
    }

    @Override
    public boolean isExceptionOnEmptyObjectList() {
        return exceptionOnEmptyObjectList;
    }

    @Override
    public void setExceptionOnEmptyObjectList(boolean exceptionOnEmptyObjectList) {
        this.exceptionOnEmptyObjectList = exceptionOnEmptyObjectList;
    }

    protected Object readValue(DomainObject<T> domainObject, Method method) throws DataAccessException {
        Object value = null;
        try {
            value = method.invoke(domainObject, EMPTY_ARG);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw new DataAccessException(((InvocationTargetException) t).getTargetException());
            } else {
                throw new DataAccessException(t);
            }
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getPrimaryKey(String catalog, String schema, String table) throws SQLException {
        Object pk = PKCache.getInstance().getPrimaryKey(catalog, schema, table);
        if (pk == null) {
            pk = PKCache.getInstance().getPrimaryKey(getDBServices().getConnection(), catalog, schema, table);
        }
        return (Set<String>) pk;
    }

    public void setPrimaryKey(String catalog, String schema, String table, Set<String> pk) {
        PKCache.getInstance().setPrimaryKey(catalog, schema, table, pk);
    }

    /**
     * Getter for property dbServices.
     *
     * @return Value of property dbServices.
     *
     */
    public DBServices getDBServices() {
        return this.dbServices;
    }

    /**
     * Setter for property dbServices.
     *
     * @param dbServices New value of property dbServices.
     *
     */
    public void setDBServices(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    /**
     * Indexed getter for property dataModificationSequence.
     *
     * @param index Index of the property.
     * @return Value of the property at <CODE>index</CODE>.
     *
     */
    public DataModificationSequence getDataModificationSequence(int index) {
        return this.dataModificationSequence[index];
    }

    /**
     * Getter for property dataModificationSequence.
     *
     * @return Value of property dataModificationSequence.
     *
     */
    public DataModificationSequence[] getDataModificationSequence() {
        return this.dataModificationSequence;
    }

    /**
     * Setter for property dataModificationSequence.
     *
     * @param dataModificationSequence New value of property dataModificationSequence.
     *
     */
    public void setDataModificationSequence(DataModificationSequence[] dataModificationSequence) {
        this.dataModificationSequence = dataModificationSequence;
    }

    /**
     * Getter for property optimisticLock.
     *
     * @return Value of property optimisticLock.
     *
     */
    public OptimisticLock getOptimisticLock() {
        return optimisticLock;
    }

    /**
     * Setter for property optimisticLock.
     *
     * @param optimisticLock New value of property optimisticLock.
     *
     */
    public void setOptimisticLock(OptimisticLock optimisticLock) {
        this.optimisticLock = optimisticLock;
    }

    /**
     * Getter for property schema.
     *
     * @return Value of property schema.
     *
     */
    public java.lang.String getSchema() {
        return schema;
    }

    /**
     * Setter for property schema.
     *
     * @param schema New value of property schema.
     *
     */
    public void setSchema(java.lang.String schema) {
        this.schema = schema;
    }

    /**
     * Getter for property table.
     *
     * @return Value of property table.
     *
     */
    public java.lang.String getTable() {
        return table;
    }

    /**
     * Setter for property table.
     *
     * @param table New value of property table.
     *
     */
    public void setTable(java.lang.String table) {
        this.table = table;
    }

    /**
     * Getter for property throwOptimisticLockDeleteException.
     *
     * @return Value of property throwOptimisticLockDeleteException.
     *
     */
    public boolean isThrowOptimisticLockDeleteException() {
        return throwOptimisticLockDeleteException;
    }

    /**
     * Setter for property throwOptimisticLockDeleteException.
     *
     * @param throwOptimisticLockDeleteException New value of property throwOptimisticLockDeleteException.
     *
     */
    public void setThrowOptimisticLockDeleteException(boolean throwOptimisticLockDeleteException) {
        this.throwOptimisticLockDeleteException = throwOptimisticLockDeleteException;
    }

    /**
     * Getter for property throwOptimisticLockUpdateException.
     *
     * @return Value of property throwOptimisticLockUpdateException.
     *
     */
    public boolean isThrowOptimisticLockUpdateException() {
        return throwOptimisticLockUpdateException;
    }

    /**
     * Setter for property throwOptimisticLockUpdateException.
     *
     * @param throwOptimisticLockUpdateException New value of property throwOptimisticLockUpdateException.
     *
     */
    public void setThrowOptimisticLockUpdateException(boolean throwOptimisticLockUpdateException) {
        this.throwOptimisticLockUpdateException = throwOptimisticLockUpdateException;
    }

    public void setThrowFailedInsertException(boolean throwFailedInsertException) {
        this.throwFailedInsertException = throwFailedInsertException;
    }

    public boolean isThrowFailedInsertException() {
        return this.throwFailedInsertException;
    }

    /**
     * Getter for property generateSQLOnly.
     *
     * @return Value of property generateSQLOnly.
     *
     */
    public boolean isGenerateSQLOnly() {
        return generateSQLOnly;
    }

    /**
     * Setter for property generateSQLOnly.
     *
     * @param generateSQLOnly New value of property generateSQLOnly.
     *
     */
    public void setGenerateSQLOnly(boolean generateSQLOnly) {
        this.generateSQLOnly = generateSQLOnly;
    }

    /**
     * Getter for property catalog.
     *
     * @return Value of property catalog.
     *
     */
    public java.lang.String getCatalog() {
        return catalog;
    }

    /**
     * Setter for property catalog.
     *
     * @param catalog New value of property catalog.
     *
     */
    public void setCatalog(java.lang.String catalog) {
        this.catalog = catalog;
    }

    /**
     * Getter for property noFullColumnName.
     *
     * @return Value of property noFullColumnName.
     *
     */
    public boolean isNoFullColumnName() {
        return noFullColumnName;
    }

    /**
     * Setter for property noFullColumnName.
     *
     * @param noFullColumnName New value of property noFullColumnName.
     *
     */
    public void setNoFullColumnName(boolean noFullColumnName) {
        this.noFullColumnName = noFullColumnName;
    }

    public boolean isOverrideCatalogName() {
        return overrideCatalogName;
    }

    public void setOverrideCatalogName(boolean overrideCatalogName) {
        this.overrideCatalogName = overrideCatalogName;
    }

    public boolean isThrowMissingFieldException() {
        return throwMissingFieldException;
    }

    public void setThrowMissingFieldException(boolean throwMissingFieldException) {
        this.throwMissingFieldException = throwMissingFieldException;
    }

    public boolean isBatchEnabled() {
        return batchEnabled;
    }

    public void setBatchEnabled(boolean batchEnabled) {
        this.batchEnabled = batchEnabled;
    }

    @Override
    public MetaData<T> getMetaData() {
        return metaData;
    }

    @Override
    public void setMetaData(MetaData<T> metaData) {
        this.metaData = (DBMetaData<T>) metaData;
    }

    @Override
    public Map<Enum<?>, Object> getOptions() {
        return this.options;
    }

    public boolean isUpdatableTablePKOnly() {
        return updatableTablePKOnly;
    }

    public void setUpdatableTablePKOnly(boolean updatableTablePKOnly) {
        this.updatableTablePKOnly = updatableTablePKOnly;
    }

    @Override
    public void setOptions(Map<Enum<?>, Object> options) {
        if (options == null) {
            return;
        }
        this.options = options;
        Object exceptionOnEmptyObjectListOption = options.get(Options.exceptionOnEmptyObjectList);
        if (exceptionOnEmptyObjectListOption != null) {
            setExceptionOnEmptyObjectList((Boolean) exceptionOnEmptyObjectListOption);
        }

        Object readOnlyOption = options.get(Options.readOnly);
        if (readOnlyOption != null) {
            setReadOnly((Boolean) readOnlyOption);
        }

        Object dataModificationSequenceOption = options.get(Options.dataModificationSequence);
        if (dataModificationSequenceOption != null) {
            setDataModificationSequence((DataModificationSequence[]) dataModificationSequenceOption);
        }

        Object dbServicesOption = options.get(Options.dbServices);
        if (dbServicesOption != null) {
            setDBServices((DBServices) dbServicesOption);
        }

        Object optimisticLockOption = options.get(Options.optimisticLock);
        if (optimisticLockOption != null) {
            setOptimisticLock((OptimisticLock) optimisticLockOption);
        }

        Object throwOptimisticLockDeleteExceptionOption = options.get(Options.throwOptimisticLockDeleteException);
        if (throwOptimisticLockDeleteExceptionOption != null) {
            setThrowOptimisticLockDeleteException((Boolean) throwOptimisticLockDeleteExceptionOption);
        }

        Object throwOptimisticLockUpdateExceptionOption = options.get(Options.throwOptimisticLockUpdateException);
        if (throwOptimisticLockUpdateExceptionOption != null) {
            setThrowOptimisticLockUpdateException((Boolean) throwOptimisticLockUpdateExceptionOption);
        }

        Object throwFailedInsertExceptionOption = options.get(Options.throwFailedInsertException);
        if (throwFailedInsertExceptionOption != null) {
            setThrowFailedInsertException((Boolean) throwFailedInsertExceptionOption);
        }

        Object overrideCatalogNameOption = options.get(Options.overrideCatalogName);
        if (overrideCatalogNameOption != null) {
            setOverrideCatalogName((Boolean) overrideCatalogNameOption);
        }

        Object catalogOption = options.get(Options.catalog);
        if (catalogOption != null) {
            setCatalog((String) catalogOption);
        }

        Object schemaOption = options.get(Options.schema);
        if (schemaOption != null) {
            setSchema((String) schemaOption);
        }

        Object tableOption = options.get(Options.table);
        if (tableOption != null) {
            setTable((String) tableOption);
        }

        Object generateSQLOnlyOption = options.get(Options.generateSQLOnly);
        if (generateSQLOnlyOption != null) {
            setGenerateSQLOnly((Boolean) generateSQLOnlyOption);
        }

        Object noFullColumnNameOption = options.get(Options.noFullColumnName);
        if (noFullColumnNameOption != null) {
            setNoFullColumnName((Boolean) noFullColumnNameOption);
        }

        Object throwMissingFieldExceptionOption = options.get(Options.throwMissingFieldException);
        if (throwMissingFieldExceptionOption != null) {
            setThrowMissingFieldException((Boolean) throwMissingFieldExceptionOption);
        }

        Object removeAndCreateInsteadOfStoreOption = options.get(Options.removeAndCreateInsteadOfStore);
        if (removeAndCreateInsteadOfStoreOption != null) {
            removeAndCreateInsteadOfStore = (Boolean) removeAndCreateInsteadOfStoreOption;
        }

        Object batchEnabledOption = options.get(Options.batchEnabled);
        if (batchEnabledOption != null) {
            batchEnabled = (Boolean) batchEnabledOption;
        }

        Object updatableTablePKOnlyOption = options.get(Options.updatableTablePKOnly);
        if (updatableTablePKOnlyOption != null) {
            updatableTablePKOnly = (Boolean) updatableTablePKOnlyOption;
        }
        
        Object lowerCaseKeywordsOption = options.get(Options.lowerCaseKeywords);
        if (lowerCaseKeywordsOption != null) {
            lowerCaseKeywords = (Boolean) lowerCaseKeywordsOption;
        }
    }
}

package org.julp.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import org.julp.DataAccessException;
import org.julp.DataHolder;

public class DBServices implements java.io.Serializable {

    private static final long serialVersionUID = 3005730076670636096L;

    public enum TransactionOnCloseConnection {
        NONE, ROLLBACK, COMMIT
    };
    protected boolean connectionClosed = false;
    protected boolean setConnectionToNullOnRelease = false;
    protected transient DataSource dataSource;
    protected transient Connection connection;
    protected transient Statement statement;
    protected transient PreparedStatement preparedStatement;
    protected transient CallableStatement callStatement = null;
    protected transient ResultSet resultSet;
    protected Properties connectionProperties = null;
    protected String cursorName = null;
    protected int maxFieldSize = 0;
    protected int maxRows = 0;
    protected int queryTimeout = 0;
    protected int fetchDirection = 0;
    protected int fetchSize = 0;
    //TYPE_FORWARD_ONLY = 1003, TYPE_SCROLL_INSENSITIVE = 1004, TYPE_SCROLL_SENSITIVE = 1005
    protected int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    //CONCUR_READ_ONLY, CONCUR_UPDATABLE
    protected int concurrency = ResultSet.CONCUR_READ_ONLY;
    protected boolean readOnly = false;
    protected boolean closeStatementAfterExecute = true;
    protected boolean escapeProcessing = true;
    protected boolean tranInProcess = false;
    protected boolean resetDataSource = false;
    protected boolean processWarnings = false;
    protected boolean processStoredProc = false;
    protected boolean closeResultSet = true;
    protected TransactionOnCloseConnection transactionOnCloseConnection = TransactionOnCloseConnection.NONE;
    protected int maxRowsToProcessInTransaction = 0; // How many rows to update/insert/delete in transaction
    protected int rowsAffectedInTransaction = 0;
    protected String connectionId = "<none>";
    protected String placeHolder = null;
    protected int maxNumberOfParams = -1;
    protected transient List<ResultSet> resultSets = null;
    protected List<DataHolder> warnings = null;
    protected StoredProcedureResult processResult = StoredProcedureResult.IGNORE_RESULT;

    //indicators how to handle ResultSet(s) from Stored Procedures and Functions
    //IGNORE_RESULT - don't process ResultSet(s)
    //RESULT_AS_DATA_HOLDERS_LIST - List of DataHolder's
    //RESULT_AS_VECTOR - Vector of Vectors
    //RESULT_AS_MAP - Map of Lists
    //RESULT_AS_ROWSET - CachedRowSetImpl
    public enum StoredProcedureResult {
        IGNORE_RESULT, RESULT_AS_DATA_HOLDERS_LIST, RESULT_AS_VECTOR, RESULT_AS_MAP, RESULT_AS_ROWSET;
    };
    protected boolean cacheStatements = false;
    protected int maxStatementCacheSize = 20; // must be set before statementsCache init, default: 20
    protected String cachedRowSetClassName = "com.sun.rowset.CachedRowSetImpl"; // set another implementation...
    /** 
     If ejbContext is not null it is indication that this is EJB Contaner managed transaction and
     instead of connection.rollback() there will be ejbContext.setRollbackOnly
     and no connection.setAutoCommit(false), no connection.commit().
     */
    protected Object ejbContext = null;
    private static final transient Logger logger = Logger.getLogger(DBServices.class.getName());
    protected List<StatementHolder> auditTrail;
    protected boolean audit;
    protected BasicStatementCache cachedStatements;

    public DBServices() {

    }

    /**
     * Getter for property connection.
     *
     * @return Value of property connection.
     *
     */
    public java.sql.Connection getConnection() throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getConnection()::connection 1: " + connection + "::isTranInProcess(): " + tranInProcess + " \n");
        }
        if (isTranInProcess()) {
            return this.connection;
        }
        if (connection == null || connection.isClosed()) {
            //if (connection == null || this.isConnectionClosed()){ //Sybase bug workaround??
            dataSource = this.getDataSource();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getConnection()::dataSource 1: " + dataSource + " \n");
            }
            connection = dataSource.getConnection();
            //connection.rollback(); //Sybase bug workaround??
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getConnection()::connection 2: " + connection + " \n");
            }
        }
        if (isTranInProcess()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getConnection()::isTranInProcess(): " + isTranInProcess() + "::connectionId: " + connectionId + "\n");
            }
            if (!this.connectionId.equals(connection.toString())) {
                throw new SQLException("Connection was closed while in transaction");
            }
        }
        if (isProcessWarnings()) {
            populateWarnings(connection.getWarnings());
            connection.clearWarnings();
        }
        return connection;
    }

    /**
     * Setter for property connection.
     *
     * @param connection New value of property connection.
     *
     */
    public void setConnection(java.sql.Connection connection) {
        if (this.connection != null) {
            try {
                this.connection.close();
                clearStatementCache();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "julp::" + this + "::old connection:: " + connection + ": " + e + "\n", e);
            }
        }
        this.connection = connection;
    }

    /**
     * Getter for property dataSource.
     *
     * @return Value of property dataSource.
     *
     */
    public javax.sql.DataSource getDataSource() throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getDataSource()::dataSource: " + dataSource + " \n");
        }
        return dataSource;
    }

    /**
     * Setter for property dataSource.
     *
     * @param dataSource New value of property dataSource.
     *
     */
    public void setDataSource(javax.sql.DataSource dataSource) {
        if (this.connection != null) {
            try {
                this.connection.close();
                clearStatementCache();
            } catch (SQLException e) {
                logger.log(Level.SEVERE, "julp::" + this + "::old connection:: " + connection + ": " + e + "\n", e);
            }
        }
        this.dataSource = dataSource;
    }

    protected void prepareStatement(String sql) throws SQLException {
        this.preparedStatement = this.getConnection().prepareStatement(sql, getResultSetType(), getConcurrency());
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::prepareStatement()::sql: \n" + sql + " \n");
        }
        this.preparedStatement.setEscapeProcessing(this.getEscapeProcessing());
        this.preparedStatement.setFetchSize(this.getFetchSize());
        this.preparedStatement.setMaxRows(this.getMaxRows());
        this.preparedStatement.setMaxFieldSize(this.getMaxFieldSize());
        this.preparedStatement.setQueryTimeout(this.getQueryTimeout());
        if (this.getCursorName() != null) {
            this.preparedStatement.setCursorName(this.getCursorName());
        }
    }

    /**
     * Getter for property preparedStatement.
     *
     * @return Value of property preparedStatement.
     *
     */
    public PreparedStatement getPreparedStatement(String sql, Collection<?> params) throws SQLException {
        if (sql == null || sql.trim().length() == 0) {
            throw new SQLException("SQL String is empty or null");
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getPreparedStatement(): \n" + sql + " ::params: " + params + " \n");
        }
        if (cacheStatements) {  // PreparedStatement to be reused, otherwise must be closed and set to null
            Object obj = cachedStatements.get(sql);
            if (obj == null) {
                prepareStatement(sql);
                addCachedStatement(sql, preparedStatement);
            } else {
                preparedStatement = (PreparedStatement) obj;
            }
        } else {
            prepareStatement(sql);
        }
        Iterator<?> paramsIter = params.iterator();
        int parmIdx = 1;
        while (paramsIter.hasNext()) {
            Object param = paramsIter.next();
            if (param != null) {
                if (param.getClass().getName().equals("java.util.Date")) {
                    param = new java.sql.Timestamp(((java.util.Date) param).getTime());
                }
                preparedStatement.setObject(parmIdx, param);
            } else {
                preparedStatement.setNull(parmIdx, Types.NULL);
            }
            parmIdx++;
        }
        if (isProcessWarnings()) {
            populateWarnings(preparedStatement.getWarnings());
            preparedStatement.clearWarnings();
        }
        return preparedStatement;
    }

    /**
     * Setter for property preparedStatement.
     *
     * @param preparedStatement New value of property preparedStatement.
     *
     */
    public void setPreparedStatement(java.sql.PreparedStatement preparedStatement) {
        this.preparedStatement = preparedStatement;
    }

    private void audit(String sql, Collection<?> params) {
        if (isAudit()) {
            if (auditTrail == null) {
                auditTrail = new ArrayList<>();
            }
            auditTrail.add(new StatementHolder(new Timestamp(System.currentTimeMillis()), sql, params));
        }
    }

    public java.sql.ResultSet getCurrentResultSet() {
        return this.resultSet;
    }

    public List<java.sql.ResultSet> getCurrentResultSets() {
        return this.resultSets;
    }

    //***********************************************************************
    /**
     * Getter for property resultSet.
     *
     * @return Value of property resultSet.
     *
     */
    public java.sql.ResultSet getResultSet(String sql) throws SQLException {
        if (resultSet != null && isCloseResultSet()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getResultSet()::closing ResultSet: " + resultSet + " \n");
            }
            resultSet.close();
        }
        audit(sql, null);
        resultSet = this.getStatement(sql).executeQuery(sql);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getResultSet()::sql: \n" + sql + " \n");
        }
        if (isProcessWarnings()) {
            populateWarnings(resultSet.getWarnings());
            this.resultSet.clearWarnings();
        }
        return resultSet;
    }

    public java.sql.ResultSet getResultSet(String sql, Collection<?> params) throws SQLException {
        if (resultSet != null && isCloseResultSet()) {
            resultSet.close();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getResultSet()::closing ResultSet: " + resultSet + " \n");
            }
        }
        audit(sql, params);
        resultSet = this.getPreparedStatement(sql, params).executeQuery();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getResultSet(): " + resultSet + " \n");
        }
        if (isProcessWarnings()) {
            populateWarnings(resultSet.getWarnings());
        }
        return resultSet;
    }

    public java.sql.ResultSet[] getResultSets(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        this.maxNumberOfParams = maxNumberOfParams;
        this.placeHolder = placeHolder;
        return getResultSets(sql, params);
    }

    @SuppressWarnings({"unchecked", "static-access", "rawtypes"})
    public java.sql.ResultSet[] getResultSets(String sql, Collection<?> params) throws SQLException {
        /**
         * This convenient method to built "IN" sql string if number of
         * arguments for "IN" is greater than max number of parameters database
         * can handle. It will create several ResultSet's. The argument list
         * which is greater then max number of parameters database can handle
         * MUST be the last in the params. Also placeHolder and
         * maxNumberOfParams must be set.
         *
         * Example: String sql = "SELECT * FROM CUSTOMER WHERE LAST_NAME <> ?
         * AND CUSTOMER_ID IN (:#)"; Collection argsList = new ArrayList(); List
         * custId = new ArrayList(); custId.add(new Integer(0)); custId.add(new
         * Integer(1)); custId.add(new Integer(2)); custId.add(new Integer(3));
         * custId.add(new Integer(4)); custId.add(new Integer(5));
         * custId.add(new Integer(6)); custId.add(new Integer(7));
         *
         * argsList.add("John Doh"); argsList.add(custId);
         *
         * factory.getDBServices().setMaxNumberOfParams(3);
         * factory.getDBServices().setPlaceHolder(":#");
         * factory.load(factory.getDBServices().getResultSets(sql, argsList));
         * factory.getDBServices().release(true);
         */
        closeResults();
        resultSets = new ArrayList();
        Collection newArgsList = new ArrayList();
        String newSql = null;
        Collection<?> newArgsList1 = null;
        boolean loaded = false;
        boolean closeResultOrig = this.closeResultSet;
        this.closeResultSet = false;
        int compoundParameters = 0;
        int paramsSize = params.size();
        Iterator<?> testParamsIter = params.iterator();
        while (testParamsIter.hasNext()) {
            Object param = testParamsIter.next();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getResultSets()::param:" + param + "::instanceof java.util.Collection: " + (param instanceof java.util.Collection) + " \n");
            }
            if (param instanceof java.util.Collection) {  // this is compound parameter
                compoundParameters++;
            }
        }
        String[] placeholdersTest = sql.split(this.placeHolder);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getResultSets()::sql: \n" + sql + "::placeholdersTest: " + Arrays.asList(placeholdersTest) + " \n");
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getResultSets()::sql: \n" + sql + "::compoundParameters: " + compoundParameters + "::placeholdersTest.length: " + placeholdersTest.length + " \n");
        }
        if ((placeholdersTest.length - 1) != compoundParameters) {
            throw new SQLException("Number of placeholders in sql is not equal to number of compound parameters");
        }
        try {
            Iterator iter1 = params.iterator();
            int paramsCount = 0;
            while (iter1.hasNext()) {
                Object thisArg = iter1.next();
                if (thisArg instanceof java.util.Collection) {  // this is compound parameter
                    if (maxNumberOfParams < 0) {
                        throw new SQLException("Invalid maxNumberOfParams: " + maxNumberOfParams);
                    }
                    List<?> list = new ArrayList((Collection<?>) thisArg);
                    int thisParmSize = list.size();
                    if (thisParmSize > maxNumberOfParams) {
                        if (paramsCount < (paramsSize - 1)) {
                            throw new SQLException("Only the last entry can have more arguments then Max Number of Parameters");
                        }
                        newSql = sql;
                        StringBuilder sb = new StringBuilder();
                        int count = 0;
                        //int total = 0;
                        List temp = new ArrayList();
                        Object paramValue = null;
                        for (int i = 0; i < thisParmSize; i++) {
                            if (count < (maxNumberOfParams)) {
                                paramValue = list.get(i);
                                sb.append("?,");
                                temp.add(paramValue);
                                if (i == (thisParmSize - 1)) {
                                    int idx = sb.length() - 1;
                                    char c = sb.charAt(idx);
                                    if (c == ',') {
                                        sb.deleteCharAt(idx);
                                    }
                                    newSql = sql.replaceFirst(placeHolder, sb.toString());
                                    newArgsList1 = new ArrayList(newArgsList);
                                    newArgsList1.addAll(temp);
                                    if (logger.isLoggable(Level.FINEST)) {
                                        logger.finest("julp::" + this + "::getResultSets()::sql: " + newSql + "::args: " + newArgsList1 + " \n");
                                    }
                                    audit(sql, params);
                                    CachedRowSet crs = (CachedRowSet) getClass().forName(cachedRowSetClassName).newInstance();
                                    ResultSet rs = null;
                                    try {
                                        rs = this.getPreparedStatement(newSql, newArgsList1).executeQuery();
                                        crs.populate(rs);
                                        resultSets.add(crs);
                                    } catch (Exception e) {
                                        throw new DataAccessException(e);
                                    } finally {
                                        if (rs != null) {
                                            rs.close();
                                        }
                                    }
                                    //resultSets.add(this.getPreparedStatement(newSql, newArgsList1).executeQuery());// warnings??
                                    loaded = true;
                                    break;
                                }
                                count++;
                            } else if (count == (maxNumberOfParams)) {
                                int idx = sb.length() - 1;
                                char c = sb.charAt(idx);
                                if (c == ',') {
                                    sb.deleteCharAt(idx);
                                }
                                newSql = sql.replaceFirst(placeHolder, sb.toString());
                                newArgsList1 = new ArrayList(newArgsList);
                                newArgsList1.addAll(temp);
                                if (logger.isLoggable(Level.FINEST)) {
                                    logger.finest("julp::" + this + "::getResultSets()::sql: " + newSql + "::args: " + newArgsList1 + " \n");
                                }
                                audit(sql, params);
                                CachedRowSet crs = (CachedRowSet) getClass().forName(cachedRowSetClassName).newInstance();
                                ResultSet rs = null;
                                try {
                                    rs = this.getPreparedStatement(newSql, newArgsList1).executeQuery();
                                    crs.populate(rs);
                                    resultSets.add(crs);
                                } catch (Exception e) {
                                    throw new DataAccessException(e);
                                } finally {
                                    if (rs != null) {
                                        rs.close();
                                    }
                                }
                                //resultSets.add(this.getPreparedStatement(newSql, newArgsList1).executeQuery());// warnings??
                                loaded = true;
                                temp.clear();
                                sb.setLength(0);
                                newSql = sql;
                                count = 0;
                                i--;
                            }
                        }
                    } else {
                        StringBuilder sb = new StringBuilder();
                        Iterator iter2 = list.iterator();
                        while (iter2.hasNext()) {
                            sb.append("?,");
                            newArgsList.add(iter2.next());
                        }
                        if (sb.indexOf(",") != -1) {
                            sb.deleteCharAt(sb.lastIndexOf(","));
                        }
                        sql = sql.replaceFirst(placeHolder, sb.toString());
                    }
                } else { // this is regular parameter
                    newArgsList.add(thisArg);
                }
                paramsCount++;
            }
            if (!loaded) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::getResultSets()::sql: \n" + sql + "::args: " + newArgsList + " \n");
                }
                audit(sql, params);
                CachedRowSet crs = (CachedRowSet) getClass().forName(cachedRowSetClassName).newInstance();
                ResultSet rs = null;
                try {
                    rs = this.getPreparedStatement(sql, newArgsList).executeQuery();
                    crs.populate(rs);
                    resultSets.add(crs);
                } catch (Exception e) {
                    throw new DataAccessException(e);
                } finally {
                    if (rs != null) {
                        rs.close();
                    }
                }
                //resultSets.add(this.getPreparedStatement(sql, newArgsList).executeQuery()); // warnings??
            }
        } catch (Exception e) {
            throw new SQLException(e);
        }
        setCloseResultSet(closeResultOrig);
        return (java.sql.ResultSet[]) resultSets.toArray(new ResultSet[0]);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public List executeStoredProcedure(String sql, Parameters params, StoredProcedureResult processResult) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::executeStoredProcedure()::sql: \n" + sql + "\n");
        }
        List returnValue = new ArrayList();
        try {
            callStatement = getConnection().prepareCall(sql);
            for (int i = 1; i <= params.getParameterCount(); i++) {
                int parameterMode = params.getParameterMode(i);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::executeStoredProcedure()::parameterMode: " + parameterMode + "::paramIndex: " + i + "::paramType: " + params.getParameterType(i) + "::parameterName: " + params.getParameterName(i) + "::parameterValue: " + params.getParameter(i) + "\n");
                }
                if (parameterMode == Parameters.parameterModeIn) {
                    callStatement.setObject(i, params.getParameter(i));
                } else if (parameterMode == Parameters.parameterModeOut) {
                    callStatement.registerOutParameter(i, params.getParameterType(i));
                } else if (parameterMode == Parameters.parameterModeInOut) {
                    callStatement.registerOutParameter(i, params.getParameterType(i));
                    callStatement.setObject(i, params.getParameter(i));
                }
            }

            ResultSet result = null;
            int rowsAffected = 0;
            audit(sql, Arrays.asList(params.getParams()));
            boolean hasResult = callStatement.execute();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::executeStoredProcedure()::hasResult: " + hasResult + "\n");
            }

            // process ResultSet(s) and/or updateCount
            while (hasResult || rowsAffected != -1) {
                if (hasResult == true) {
                    result = callStatement.getResultSet();
                    try {
                        if (processResult == StoredProcedureResult.IGNORE_RESULT) {
                            // do nothing
                        } else if (processResult == StoredProcedureResult.RESULT_AS_DATA_HOLDERS_LIST) {
                            ResultSetMetaData rsmd = result.getMetaData();
                            int colCount = rsmd.getColumnCount();
                            List rows = new ArrayList();
                            String[] colNames = new String[colCount];
                            Map<String, Integer> dups = new HashMap<>();
                            // rename columns if there are duplicate names
                            for (int col = 1; col <= colCount; col++) {
                                String colName = rsmd.getColumnName(col);
                                if (!dups.containsKey(colName)) {
                                    dups.put(colName, 0);
                                } else {
                                    Integer num = dups.get(colName);
                                    num = num + 1;
                                    dups.put(colName, num);
                                    colName = colName + num;
                                }
                                colNames[col - 1] = colName;
                            }
                            while (result.next()) {
                                DataHolder dataHolder = new DataHolder(colCount);
                                for (int col = 1; col <= colCount; col++) {
                                    dataHolder.setFieldNameAndValue(col, colNames[col - 1], result.getObject(col));
                                }
                                rows.add(dataHolder);
                            }
                            if (isProcessWarnings()) {
                                populateWarnings(result.getWarnings());
                                result.clearWarnings();
                            }
                            returnValue.add(rows);
                        } else if (processResult == StoredProcedureResult.RESULT_AS_VECTOR) {
                            int colCount = result.getMetaData().getColumnCount();
                            List rows = new ArrayList();
                            while (result.next()) {
                                List row = new ArrayList(colCount);
                                for (int col = 1; col <= colCount; col++) {
                                    row.add(result.getObject(col));
                                }
                                rows.add(row);
                            }
                            if (isProcessWarnings()) {
                                populateWarnings(result.getWarnings());
                                result.clearWarnings();
                            }
                            returnValue.add(rows);
                        } else if (processResult == StoredProcedureResult.RESULT_AS_MAP) {
                            ResultSetMetaData rsmd = result.getMetaData();
                            int colCount = rsmd.getColumnCount();
                            Map columns = new LinkedHashMap(colCount);
                            List[] column = new ArrayList[colCount];
                            for (int i = 0; i < column.length; i++) {
                                column[i] = new ArrayList<>();
                            }
                            while (result.next()) {
                                for (int col = 1; col <= colCount; col++) {
                                    Object value = result.getObject(col);
                                    column[col - 1].add(value);
                                }
                            }
                            Map<String, Integer> dups = new HashMap<>();
                            for (int col = 1; col <= colCount; col++) {
                                String colName = rsmd.getColumnName(col);
                                if (!dups.containsKey(colName)) {
                                    dups.put(colName, 0);
                                } else {
                                    Integer num = dups.get(colName);
                                    num = num + 1;
                                    dups.put(colName, num);
                                    colName = colName + num;
                                }
                                columns.put(colName, column[col - 1]);
                            }
                            if (isProcessWarnings()) {
                                populateWarnings(result.getWarnings());
                                result.clearWarnings();
                            }
                            returnValue.add(columns);

                        } else if (processResult == StoredProcedureResult.RESULT_AS_ROWSET) {
                            CachedRowSet crs = (CachedRowSet) Class.forName(cachedRowSetClassName).newInstance();
                            crs.populate(result);
                            returnValue.add(crs);
                        }
                    } finally {
                        if (isProcessWarnings()) {
                            populateWarnings(result.getWarnings());
                            result.clearWarnings();
                        }
                        if (logger.isLoggable(Level.FINEST)) {
                            logger.finest("julp::" + this + "::executeStoredProcedure()::closing ResultSet: " + result + " \n");
                        }
                        result.close();
                        result = null;
                    }
                } else {
                    rowsAffected = callStatement.getUpdateCount();
                    if (rowsAffected != -1) {
                        returnValue.add(rowsAffected);
                    }
                }
                hasResult = callStatement.getMoreResults();
            }

            // process OUT and/or INOUT parameters
            for (int i = 1; i <= params.getParameterCount(); i++) {
                int parameterMode = params.getParameterMode(i);
                if (parameterMode == Parameters.parameterModeOut || parameterMode == Parameters.parameterModeInOut) {
                    Object value = callStatement.getObject(i);
                    params.setParameter(i, value);
                }
            }
            if (isProcessWarnings()) {
                populateWarnings(callStatement.getWarnings());
                this.callStatement.clearWarnings();
            }
            returnValue.add(params);
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            if (isCloseStatementAfterExecute()) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::executeStoredProcedure()::closing CallableStatement: " + callStatement + " \n");
                }
                if (callStatement != null) {
                    callStatement.close();
                    callStatement = null;
                }
            }
        }
        return returnValue;
    }

    @SuppressWarnings("rawtypes")
    public int[] executeBatch(String sql, Collection<Collection> batch) throws SQLException {
        int[] rowsAffected;
        try {
            this.prepareStatement(sql);
            Iterator<Collection> batchIter = batch.iterator();
            while (batchIter.hasNext()) {
                Collection params = batchIter.next();
                Iterator paramsIter = params.iterator();
                int parmIdx = 1;
                while (paramsIter.hasNext()) {
                    Object param = paramsIter.next();
                    if (param != null) {
                        if (param.getClass().getName().equals("java.util.Date")) {
                            param = new java.sql.Timestamp(((java.util.Date) param).getTime());
                        }
                        preparedStatement.setObject(parmIdx, param);
                    } else {
                        preparedStatement.setNull(parmIdx, Types.NULL);
                    }
                    parmIdx++;
                }
                preparedStatement.addBatch();
                audit(sql, params);
            }
            rowsAffected = this.preparedStatement.executeBatch();
            List<DataHolder> dhList = calcAffectedRows(rowsAffected);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::executeBatch(" + sql + ")::batch: " + batch + "::" + dhList + "\n");
            }
        } finally {
            if (isProcessWarnings()) {
                populateWarnings(preparedStatement.getWarnings());
                this.preparedStatement.clearWarnings();
            }
            if (isCloseStatementAfterExecute()) {
                if (preparedStatement != null) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::executeBatch()::closing PreparedStatement: " + preparedStatement + " \n");
                    }
                    preparedStatement.close();
                    preparedStatement = null;
                }
            }
        }
        return rowsAffected;
    }

    public int[] executeBatch(Collection<String> batch) throws SQLException {
        int[] rowsAffected;
        createStatement();
        try {
            Iterator<String> batchIter = batch.iterator();
            String sql = null;
            while (batchIter.hasNext()) {
                sql = batchIter.next();
                this.statement.addBatch(sql);
                audit(sql, null);
            }
            rowsAffected = this.statement.executeBatch();
            List<DataHolder> dhList = calcAffectedRows(rowsAffected);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::executeBatch(" + sql + ")::batch: " + batch + "::" + dhList + "\n");
            }
        } finally {
            if (isProcessWarnings()) {
                populateWarnings(statement.getWarnings());
                this.statement.clearWarnings();
            }
            if (isCloseStatementAfterExecute()) {
                if (statement != null) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::executeBatch()::closing Statement: " + statement + " \n");
                    }
                    statement.close();
                    statement = null;
                }
            }
        }
        return rowsAffected;
    }

    public List<DataHolder> calcAffectedRows(int[] batchUpdates) {
        int rows = 0;
        List<DataHolder> holders = null;
        for (int i = 0; i < batchUpdates.length; i++) {
            if (batchUpdates[i] >= 0) {
                rows = rows + batchUpdates[i];
            } else if (batchUpdates[i] == Statement.SUCCESS_NO_INFO) {
                if (holders == null) {
                    holders = new ArrayList<DataHolder>();
                }
                DataHolder holder = new DataHolder(1);
                holder.setFieldNameAndValue(1, "SUCCESS_NO_INFO", i);
                holders.add(holder);
            } else if (batchUpdates[i] == Statement.EXECUTE_FAILED) {
                if (holders == null) {
                    holders = new ArrayList<>();
                }
                DataHolder holder = new DataHolder(1);
                holder.setFieldNameAndValue(1, "EXECUTE_FAILED", i);
                holders.add(holder);
            }
        }
        if (holders == null) {
            holders = new ArrayList<>();
        }
        DataHolder holder = new DataHolder(1);
        holder.setFieldNameAndValue(1, "AFFECTED_ROWS", rows);
        holders.add(holder);

        return holders;
    }

    public int execute(String sql) throws SQLException {
        int rowsAffected = -1;
        try {
            audit(sql, null);
            rowsAffected = this.getStatement(sql).executeUpdate(sql);
        } finally {
            if (isProcessWarnings()) {
                populateWarnings(statement.getWarnings());
                this.statement.clearWarnings();
            }
            if (isCloseStatementAfterExecute()) {
                if (statement != null) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::execute()::closing Statement: " + statement + " \n");
                    }
                    statement.close();
                    statement = null;
                }
            }
        }
        this.setRowsAffectedInTransaction(rowsAffectedInTransaction + rowsAffected);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::execute()::sql: \n" + sql + "::rowsAffected: " + rowsAffected + "\n");
        }
        return rowsAffected;
    }

    @SuppressWarnings("rawtypes")
    public int execute(String sql, Collection params) throws SQLException {
        int rowsAffected = -1;
        try {
            audit(sql, params);
            rowsAffected = this.getPreparedStatement(sql, params).executeUpdate();
        } finally {
            if (isProcessWarnings()) {
                populateWarnings(statement.getWarnings());
                this.statement.clearWarnings();
            }
            if (isCloseStatementAfterExecute()) {
                if (preparedStatement != null) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::execute()::closing PreparedStatement: " + preparedStatement + " \n");
                    }
                    preparedStatement.close();
                    preparedStatement = null;
                }
            }
        }
        this.setRowsAffectedInTransaction(rowsAffectedInTransaction + rowsAffected);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::execute()::sql: \n" + sql + " params: " + params + "::rowsAffected: " + rowsAffected + "\n");
        }
        return rowsAffected;
    }

    /**
     * Setter for property resultSet.
     *
     * @param resultSet New value of property resultSet.
     *
     */
    public void setResultSet(java.sql.ResultSet resultSet) {
        this.resultSet = resultSet;
    }

    protected void createStatement() throws SQLException {
        statement = this.getConnection().createStatement(getResultSetType(), getConcurrency());
        statement.setEscapeProcessing(this.getEscapeProcessing());
        statement.setFetchSize(this.getFetchSize());
        statement.setMaxRows(this.getMaxRows());
        statement.setMaxFieldSize(this.getMaxFieldSize());
        statement.setQueryTimeout(this.getQueryTimeout());
        if (this.getCursorName() != null) {
            statement.setCursorName(this.getCursorName());
        }
    }

    /**
     * Getter for property statement.
     *
     * @return Value of property statement.
     *
     */
    public java.sql.Statement getStatement(String sql) throws SQLException {
        if (sql == null || sql.trim().length() == 0) {
            throw new SQLException("SQL String is empty or null");
        }
        if (cacheStatements) {  // Statement to be reused, otherwise must be closed and set to null
            Object obj = cachedStatements.get(sql);
            if (obj != null) {
                statement = (Statement) obj;
            } else {
                createStatement();
                addCachedStatement(sql, statement);
            }
        } else {
            createStatement();
        }
        if (isProcessWarnings()) {
            populateWarnings(statement.getWarnings());
            statement.clearWarnings();
        }
        return statement;
    }

    protected void closeResults() throws SQLException {
        if (resultSets != null) {
            for (int i = 0; i < resultSets.size(); i++) {
                Object obj = resultSets.get(i);
                if (obj != null) {
                    ((ResultSet) obj).close();
                    obj = null;
                }
            }
            resultSets = null;
        }
    }

    /**
     * Setter for property statement.
     *
     * @param statement New value of property statement.
     *
     */
    public void setStatement(java.sql.Statement statement) {
        this.statement = statement;
    }

    public void release(boolean closeConnection) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::release::closeConnection: " + closeConnection + " \n");
        }
        if (resultSet != null) {
            resultSet.close();
            resultSet = null;
        }
        closeResults();
        if (!cacheStatements) {
            if (statement != null) {
                statement.close();
                statement = null;
            }
            if (preparedStatement != null) {
                preparedStatement.close();
                preparedStatement = null;
            }
            if (callStatement != null) {
                callStatement.close();
                callStatement = null;
            }
        }
        if (warnings != null) {
            warnings.clear();
        }
        if (connection != null) {
            if (closeConnection == true) {
                clearStatementCache();
                if (transactionOnCloseConnection == TransactionOnCloseConnection.ROLLBACK) {
                    connection.rollback();
                } else if (transactionOnCloseConnection == TransactionOnCloseConnection.COMMIT) {
                    connection.commit();
                }
                connection.close();
                setConnectionClosed(true);
                if (setConnectionToNullOnRelease) {
                    connection = null;
                }
                this.setTranInProcess(false);
                this.setConnectionId("<none>");
            } else {
                //do nothing
            }
        }
    }

    public void beginTran() throws SQLException {
        if (ejbContext == null) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::BEGIN TRAN::previous connectionId: " + connectionId + " \n");
            }
            audit("beginTran", null);
            this.setRowsAffectedInTransaction(0);
            this.getConnection().setAutoCommit(false);
            this.setConnectionId(this.connection.toString());
            this.setTranInProcess(true);
            if (isProcessWarnings()) {
                populateWarnings(this.connection.getWarnings());
                this.connection.clearWarnings();
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::BEGIN TRAN::current connectionId: " + connectionId + " \n");
            }
        } else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::ejbContext: " + ejbContext + " \n");
            }
        }
    }

    public void commitTran() throws SQLException {
        if (ejbContext == null) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::COMMIT TRAN::current connectionId: " + connectionId + " \n");
            }
            audit("commit", null);
            this.getConnection().commit();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::COMMITTED\n");
            }
            this.getConnection().setAutoCommit(true);//?? Sybase bug
            this.setTranInProcess(false);
            this.setConnectionId("<none>");
            if (isProcessWarnings()) {
                populateWarnings(this.connection.getWarnings());
                this.connection.clearWarnings();
            }
        } else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::ejbContext: " + ejbContext + " \n");
            }
        }
    }

    public void rollbackTran() throws SQLException {
        if (ejbContext != null) {
            try {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::ejbContext: " + ejbContext + ".setRollbackOnlyconnectionId: " + connectionId + " \n");
                }
                Method m = ejbContext.getClass().getMethod("setRollbackOnly", new Class[]{});
                m.invoke(ejbContext, new Object[]{});
            } catch (InvocationTargetException ete) {
                throw new SQLException(ete.getTargetException());
            } catch (Exception e) {
                throw new SQLException(e);
            }
        } else {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::ROLLBACK TRAN::connectionId: " + connectionId + " \n");
            }
            audit("rollback", null);
            this.getConnection().rollback();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::AFTER ROLLBACK::connectionId: " + connectionId + " \n");
            }
            this.getConnection().setAutoCommit(true);
            this.setTranInProcess(false);
            this.setConnectionId("<none>");
            if (isProcessWarnings()) {
                populateWarnings(this.connection.getWarnings());
                this.connection.clearWarnings();
            }
        }
    }

    /**
     * Getter for property fetchSize.
     *
     * @return Value of property fetchSize.
     *
     */
    public int getFetchSize() {
        return fetchSize;
    }

    /**
     * Setter for property fetchSize.
     *
     * @param fetchSize New value of property fetchSize.
     *
     */
    public void setFetchSize(int fetchSize) {
        this.fetchSize = fetchSize;
    }

    /**
     * Getter for property fetchDirection.
     *
     * @return Value of property fetchDirection.
     *
     */
    public int getFetchDirection() {
        return fetchDirection;
    }

    /**
     * Setter for property fetchDirection.
     *
     * @param fetchDirection New value of property fetchDirection.
     *
     */
    public void setFetchDirection(int fetchDirection) {
        this.fetchDirection = fetchDirection;
    }

    /**
     * Getter for property queryTimeout.
     *
     * @return Value of property queryTimeout.
     *
     */
    public int getQueryTimeout() {
        return queryTimeout;
    }

    /**
     * Setter for property queryTimeout.
     *
     * @param queryTimeout New value of property queryTimeout.
     *
     */
    public void setQueryTimeout(int queryTimeout) {
        this.queryTimeout = queryTimeout;
    }

    /**
     * Getter for property maxRows.
     *
     * @return Value of property maxRows.
     *
     */
    public int getMaxRows() {
        return maxRows;
    }

    /**
     * Setter for property maxRows.
     *
     * @param maxRows New value of property maxRows.
     *
     */
    public void setMaxRows(int maxRows) {
        this.maxRows = maxRows;
    }

    /**
     * Getter for property maxFieldSize.
     *
     * @return Value of property maxFieldSize.
     *
     */
    public int getMaxFieldSize() {
        return maxFieldSize;
    }

    /**
     * Setter for property maxFieldSize.
     *
     * @param maxFieldSize New value of property maxFieldSize.
     *
     */
    public void setMaxFieldSize(int maxFieldSize) {
        this.maxFieldSize = maxFieldSize;
    }

    /**
     * Getter for property escapeProcessing.
     *
     * @return Value of property escapeProcessing.
     *
     */
    public boolean getEscapeProcessing() {
        return escapeProcessing;
    }

    /**
     * Setter for property escapeProcessing.
     *
     * @param escapeProcessing New value of property escapeProcessing.
     *
     */
    public void setEscapeProcessing(boolean escapeProcessing) {
        this.escapeProcessing = escapeProcessing;
    }

    /**
     * Getter for property resultSetType.
     *
     * @return Value of property resultSetType.
     *
     */
    public int getResultSetType() {
        return resultSetType;
    }

    /**
     * Setter for property resultSetType.
     *
     * @param resultSetType New value of property resultSetType.
     *
     */
    public void setResultSetType(int resultSetType) {
        this.resultSetType = resultSetType;
    }

    /**
     * Getter for property concurrency.
     *
     * @return Value of property concurrency.
     *
     */
    public int getConcurrency() {
        return concurrency;
    }

    /**
     * Setter for property concurrency.
     *
     * @param concurrency New value of property concurrency.
     *
     */
    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    /**
     * Getter for property connectionClosed.
     *
     * @return Value of property connectionClosed.
     *
     */
    public boolean isConnectionClosed() {
        return connectionClosed;
    }

    /**
     * Setter for property connectionClosed.
     *
     * @param connectionClosed New value of property connectionClosed.
     *
     */
    protected void setConnectionClosed(boolean connectionClosed) {
        this.connectionClosed = connectionClosed;
    }

    /**
     * Getter for property maxRowsToProcessInTransaction.
     *
     * @return Value of property maxRowsToProcessInTransaction.
     *
     */
    public int getMaxRowsToProcessInTransaction() {
        return maxRowsToProcessInTransaction;
    }

    /**
     * Setter for property maxRowsToProcessInTransaction.
     *
     * @param maxRowsToProcessInTransaction New value of property
     * maxRowsToProcessInTransaction.
     *
     */
    public void setMaxRowsToProcessInTransaction(int maxRowsToProcessInTransaction) throws SQLException {
        if (maxRowsToProcessInTransaction < 0) {
            throw new SQLException("Invalid maxRowsToProcessInTransaction value");
        }
        this.maxRowsToProcessInTransaction = maxRowsToProcessInTransaction;
    }

    /**
     * Getter for property rowsAffectedInTransaction.
     *
     * @return Value of property rowsAffectedInTransaction.
     *
     */
    public int getRowsAffectedInTransaction() {
        return rowsAffectedInTransaction;
    }

    /**
     * Setter for property rowsAffectedInTransaction.
     *
     * @param rowsAffectedInTransaction New value of property
     * rowsAffectedInTransaction.
     *
     */
    public void setRowsAffectedInTransaction(int rowsAffectedInTransaction) throws SQLException {
        this.rowsAffectedInTransaction = rowsAffectedInTransaction;
        if (this.maxRowsToProcessInTransaction != 0
                && this.rowsAffectedInTransaction >= this.maxRowsToProcessInTransaction) {
            try {
                this.commitTran();
                this.release(false);
                this.beginTran();
            } catch (SQLException e) {
                this.rollbackTran();
                throw e;
            }
        }
    }

    /**
     * Getter for property connectionProperties.
     *
     * @return Value of property connectionProperties.
     *
     */
    public java.util.Properties getConnectionProperties() {
        return connectionProperties;
    }

    /**
     * Setter for property connectionProperties.
     *
     * @param connectionProperties New value of property connectionProperties.
     *
     */
    public void setConnectionProperties(java.util.Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * Getter for property closeStatementAfterExecute.
     *
     * @return Value of property closeStatementAfterExecute.
     *
     */
    public boolean isCloseStatementAfterExecute() {
        return closeStatementAfterExecute;
    }

    /**
     * Setter for property closeStatementAfterExecute.
     *
     * @param closeStatementAfterExecute New value of property
     * closeStatementAfterExecute.
     *
     */
    public void setCloseStatementAfterExecute(boolean closeStatementAfterExecute) {
        this.closeStatementAfterExecute = closeStatementAfterExecute;
    }

    /**
     * Getter for property connectionId.
     *
     * @return Value of property connectionId.
     *
     */
    public java.lang.String getConnectionId() {
        return connectionId;
    }

    /**
     * Setter for property connectionId.
     *
     * @param connectionId New value of property connectionId.
     *
     */
    public void setConnectionId(java.lang.String connectionId) {
        this.connectionId = connectionId;
    }

    /**
     * Getter for property tranInProcess.
     *
     * @return Value of property tranInProcess.
     *
     */
    public boolean isTranInProcess() {
        return tranInProcess;
    }

    /**
     * Setter for property tranInProcess.
     *
     * @param tranInProcess New value of property tranInProcess.
     *
     */
    public void setTranInProcess(boolean tranInProcess) {
        this.tranInProcess = tranInProcess;
    }

    /**
     * Getter for property resetDataSource.
     *
     * @return Value of property resetDataSource.
     */
    public boolean isResetDataSource() {
        return resetDataSource;
    }

    /**
     * Setter for property resetDataSource.
     *
     * @param resetDataSource New value of property resetDataSource.
     */
    public void setResetDataSource(boolean resetDataSource) {
        this.resetDataSource = resetDataSource;
    }

    public java.lang.String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(java.lang.String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public int getMaxNumberOfParams() {
        return maxNumberOfParams;
    }

    public void setMaxNumberOfParams(int maxNumberOfParams) {
        this.maxNumberOfParams = maxNumberOfParams;
    }

    public List<DataHolder> getWarnings() {
        return (warnings == null) ? new ArrayList<DataHolder>() : warnings;
    }

    public void setProcessWarnings(boolean processWarnings) {
        if (processWarnings) {
            warnings = new ArrayList<>();
        } else {
            warnings.clear();
            warnings = null;
        }
        this.processWarnings = processWarnings;
    }

    public boolean isProcessWarnings() {
        return this.processWarnings;
    }

    /**
     * Make sure to read warnings before release()
     *
     * @param warn
     */
    public void populateWarnings(SQLWarning warn) {
        while (warn != null) {
            DataHolder holder = new DataHolder(3);
            holder.setFieldNameAndValue(1, "message", warn.getMessage());
            holder.setFieldNameAndValue(2, "SQLState", warn.getSQLState());
            holder.setFieldNameAndValue(3, "vendorErrorCode", Integer.valueOf(warn.getErrorCode()));
            if (warnings == null) {
                warnings = new ArrayList<>();
            }
            warnings.add(holder);
            warn = warn.getNextWarning();
        }
    }

    public boolean isCacheStatements() {
        return cacheStatements;
    }

    @SuppressWarnings("rawtypes")
    public void clearStatementCache() {
        if (cachedStatements != null) {
            try {
              cachedStatements.clear();
            } catch (SQLException sqle) {
                logger.throwing(getClass().getCanonicalName(), "clearStatementCache", sqle);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void setCacheStatements(boolean cacheStatements) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::setCacheStatements()::cacheStatements: " + cacheStatements + " \n");
        }
        if (cacheStatements) {
            if (cachedStatements == null) {
               cachedStatements = new BasicStatementCache(maxStatementCacheSize);
            }
            setCloseStatementAfterExecute(false);
        } else {
            clearStatementCache();
            cachedStatements = null;
        }
        this.cacheStatements = cacheStatements;
    }

    public int getMaxStatementCacheSize() {
        return this.maxStatementCacheSize;
    }

    public void setMaxStatementCacheSize(int maxStatementCacheSize) {
        this.maxStatementCacheSize = maxStatementCacheSize;
    }

    protected void addCachedStatement(String statementId, Statement statementToCache) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::addCachedStatement()::statementId: " + statementId + "::statementToCache: " + statementToCache + "\n");
        }
        cachedStatements.add(statementId, statementToCache);
    }

    public String getCachedRowSetClassName() {
        return cachedRowSetClassName;
    }

    public void setCachedRowSetClassName(String cachedRowSetClassName) {
        this.cachedRowSetClassName = cachedRowSetClassName;
    }

    public void setCloseResultSet(boolean closeResultSet) {
        this.closeResultSet = closeResultSet;
    }

    public boolean isCloseResultSet() {
        return this.closeResultSet;
    }

    public String getCursorName() {
        return cursorName;
    }

    public void setCursorName(String cursorName) {
        this.cursorName = cursorName;
    }

    public TransactionOnCloseConnection getProcessTransactionOnCloseConnection() {
        return transactionOnCloseConnection;
    }

    public void setTransactionOnCloseConnection(TransactionOnCloseConnection transactionOnCloseConnection) {
        this.transactionOnCloseConnection = transactionOnCloseConnection;
    }

    public boolean isSetConnectionToNullOnRelease() {
        return setConnectionToNullOnRelease;
    }

    public void setSetConnectionToNullOnRelease(boolean setConnectionToNullOnRelease) {
        this.setConnectionToNullOnRelease = setConnectionToNullOnRelease;
    }

    public boolean isAudit() {
        return audit;
    }

    public void setAudit(boolean audit) {
        this.audit = audit;
    }

    public List<StatementHolder> getAuditTrail() {
        return auditTrail == null ? Collections.<StatementHolder>emptyList() : auditTrail;
    }
}

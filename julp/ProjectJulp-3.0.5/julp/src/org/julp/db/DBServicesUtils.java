package org.julp.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.julp.DataHolder;
import org.julp.ValueObject;

@SuppressWarnings("unchecked")
public class DBServicesUtils implements java.io.Serializable {

    private static final long serialVersionUID = 6368514693141186457L;
    protected String dateFormat = "yyyy-MM-dd HH:mm:ss";
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    protected String timeFormat = "HH:mm:ss";
    protected String decimalFormat = null;
    protected final static String PARAM_PLACEHOLDER = "?";
    protected final static String SINGLE_QUOTE = "'";
    protected final static String DOT = ".";
    protected final static String COMMA = ",";
    protected DBServices dbServices;

    public DBServicesUtils() {
    }

    public DBServicesUtils(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    /**
     * Convert PreparedStatement's SQL and parameters for use in Statement or
     * logging by replacing "?" with parameters values
     */
    public String convert(String sql, Collection<?> params) {
        if (params == null || params.isEmpty()) {
            return sql;
        }
        StringBuilder sb = new StringBuilder(sql);
        Iterator<?> iter = params.iterator();
        while (iter.hasNext()) {
            String stringValue;
            Object param = iter.next();
            if (param instanceof Number) {
                stringValue = String.valueOf(param);
                if (stringValue.indexOf(DOT) > -1 && decimalFormat != null) {
                    stringValue = formatDecimal((Number) param);
                }
            } else if (param instanceof String) {
                stringValue = SINGLE_QUOTE.concat(String.valueOf(param)).concat(SINGLE_QUOTE);
            } else if (param instanceof Time) {
                stringValue = formatTime((Time) param);
            } else if (param instanceof Timestamp) {
                stringValue = formatTimestamp((Timestamp) param);
            } else if (param instanceof Date) {
                stringValue = formatDate((Date) param);
            } else {
                stringValue = String.valueOf(param);
            }
            int idx = sb.indexOf(PARAM_PLACEHOLDER);
            if (idx > -1) {
                sb.replace(idx, idx + 1, stringValue);
            }
        }
        return sb.toString();
    }

    protected String formatDate(Date d) {
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        return SINGLE_QUOTE.concat(sdf.format(d)).concat(SINGLE_QUOTE);
    }

    protected String formatTime(Time t) {
        SimpleDateFormat sdf = new SimpleDateFormat(timeFormat);
        return SINGLE_QUOTE.concat(sdf.format(t)).concat(SINGLE_QUOTE);
    }

    protected String formatTimestamp(Timestamp ts) {
        SimpleDateFormat sdf = new SimpleDateFormat(timestampFormat);
        return SINGLE_QUOTE.concat(sdf.format(ts)).concat(SINGLE_QUOTE);
    }

    protected String formatDecimal(Number n) {
        DecimalFormat df = new DecimalFormat(decimalFormat);
        return df.format(n);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getTimestampFormat() {
        return timestampFormat;
    }

    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getDecimalFormat() {
        return decimalFormat;
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    // **************************************************************************
    public DataHolder[] getResultAsDataHoldersArray(String sql, Collection<?> params, boolean populateColumnNames) throws SQLException {
        return (DataHolder[]) getResultAsDataHoldersList(sql, params, populateColumnNames).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultAsDataHoldersArray(String sql, boolean populateColumnNames) throws SQLException {
        return (DataHolder[]) getResultAsDataHoldersList(sql, populateColumnNames).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultAsDataHoldersArray(String sql) throws SQLException {
        return (DataHolder[]) getResultAsDataHoldersList(sql).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultAsDataHoldersArray(String sql, Collection<?> params) throws SQLException {
        return (DataHolder[]) getResultAsDataHoldersList(sql, params).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultsAsDataHoldersArray(String sql, Collection<?> params) throws SQLException {
        return (DataHolder[]) getResultsAsDataHoldersList(sql, params, true).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultsAsDataHoldersArray(String sql, Collection<?> params, boolean populateColumnNames) throws SQLException {
        return (DataHolder[]) getResultsAsDataHoldersList(sql, params, populateColumnNames).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultsAsDataHoldersArray(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        return (DataHolder[]) getResultsAsDataHoldersList(sql, params, maxNumberOfParams, placeHolder, true).toArray(new DataHolder[0]);
    }

    public DataHolder[] getResultsAsDataHoldersArray(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder, boolean populateColumnNames) throws SQLException {
        return (DataHolder[]) getResultsAsDataHoldersList(sql, params, maxNumberOfParams, placeHolder, populateColumnNames).toArray(new DataHolder[0]);
    }

    // **************************************************************************
    public <T> T getSingleValue(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getSingleValueInternal();
    }

    public <T> T getSingleValue(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getSingleValueInternal();
    }

    protected <T> T getSingleValueInternal() throws SQLException {
        Object value = null;
        if (dbServices.getCurrentResultSet().next()) {
            value = dbServices.getCurrentResultSet().getObject(1);
        } else {
            throw new SQLException("No data");
        }
        warnings();
        return (T) value;
    }

    // **************************************************************************
    public <T> List<T> getSingleColumnResultAsList(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getSingleColumnResultAsListInternal();
    }

    public <T> List<T> getSingleColumnResultAsList(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getSingleColumnResultAsListInternal();
    }

    protected <T> List<T> getSingleColumnResultAsListInternal() throws SQLException {
        List<T> rows = new ArrayList<>();
        while (dbServices.getCurrentResultSet().next()) {
            rows.add((T) dbServices.getCurrentResultSet().getObject(1));
        }
        warnings();
        return rows;
    }

    public <T> List<T> getSingleColumnResultsAsList(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getSingleColumnResultsAsListInternal();
    }

    public <T> List<T> getSingleColumnResultsAsList(String sql, List<?> params) throws SQLException {
        dbServices.getResultSets(sql, params);
        return getSingleColumnResultsAsListInternal();
    }

    protected <T> List<T> getSingleColumnResultsAsListInternal() throws SQLException {
        List<T> rows = new ArrayList<>();
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                rows.add((T) ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(1));
            }
            warnings(i);
        }
        return rows;
    }

    // **************************************************************************
    public <K, V> Map<K, V> getTwoColumnsResultAsLinkedMap(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getTwoColumnsResultAsLinkedMapInternal();
    }

    public <K, V> Map<K, V> getTwoColumnsResultAsLinkedMap(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getTwoColumnsResultAsLinkedMapInternal();
    }

    protected <K, V> Map<K, V> getTwoColumnsResultAsLinkedMapInternal() throws SQLException {
        Map<K, V> rows = new LinkedHashMap<>();
        while (dbServices.getCurrentResultSet().next()) {
            rows.put((K) dbServices.getCurrentResultSet().getObject(1), (V) dbServices.getCurrentResultSet().getObject(2));
        }
        return rows;
    }

    public <K, V> Map<K, V> getTwoColumnsResultsAsLinkedMap(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getTwoColumnsResultsAsMapInternal();
    }

    public <K, V> Map<K, V> getTwoColumnsResultsAsMap(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getTwoColumnsResultsAsMapInternal();
    }

    protected <K, V> Map<K, V> getTwoColumnsResultsAsMapInternal() throws SQLException {
        Map<K, V> rows = new HashMap<>();
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                rows.put((K) ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(1), (V) ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(2));
            }
            warnings(i);
        }
        return rows;
    }

    public <K, V> Map<K, V> getTwoColumnsResultAsMap(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getTwoColumnsResultAsMapInternal();
    }

    public <K, V> Map<K, V> getTwoColumnsResultAsMap(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getTwoColumnsResultAsMapInternal();
    }

    protected <K, V> Map<K, V> getTwoColumnsResultAsMapInternal() throws SQLException {
        Map<K, V> rows = new HashMap<>();
        while (dbServices.getCurrentResultSet().next()) {
            rows.put((K) dbServices.getCurrentResultSet().getObject(1), (V) dbServices.getCurrentResultSet().getObject(2));
        }
        return rows;
    }

//    public Map getTwoColumnsResultsAsMap(String sql, List params) throws SQLException {
//       dbServices.getResultSets(sql, params);
//        Map rows = new LinkedHashMap();
//        for (int i = 0; i < resultSets.size(); i++) {
//            while (((ResultSet) resultSets.get(i)).next()) {
//                rows.put(dbServices.getCurrentResultSet().getObject(1), dbServices.getCurrentResultSet().getObject(2));
//            }
//            if (dbServices.isProcessWarnings()) {
//               dbServices.populateWarnings(((ResultSet) resultSets.get(i)).getWarnings());
//                ((ResultSet) resultSets.get(i)).clearWarnings();
//            }
//        }
//        return rows;
//    }
    // **************************************************************************
    public <T> List<ValueObject<T>> getResultAsValueObjectList(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getResultAsValueObjectListInterna();
    }

    public <T> List<ValueObject<T>> getResultAsValueObjectList(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getResultAsValueObjectListInterna();
    }

    protected <T> List<ValueObject<T>> getResultAsValueObjectListInterna() throws SQLException {
        List<ValueObject<T>> rows = new ArrayList<>();
        while (dbServices.getCurrentResultSet().next()) {
            rows.add(new ValueObject<>((T) dbServices.getCurrentResultSet().getObject(1), dbServices.getCurrentResultSet().getString(2)));
        }
        return rows;
    }

    public <T> List<ValueObject<T>> getResultsAsValueObjectList(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getResultsAsValueObjectList();
    }

    public <T> List<ValueObject<T>> getResultsAsValueObjectList(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSets(sql, params);
        return getResultsAsValueObjectList();
    }

    protected <T> List<ValueObject<T>> getResultsAsValueObjectList() throws SQLException {
        List<ValueObject<T>> rows = new ArrayList<>();
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                rows.add(new ValueObject<>((T) dbServices.getCurrentResultSet().getObject(1), dbServices.getCurrentResultSet().getString(2)));
            }
            warnings(i);
        }
        return rows;
    }

    // **************************************************************************
    public List<DataHolder> getResultAsDataHoldersList(String sql, Collection<?> params, boolean populateColumnNames) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getResultAsDataHoldersListInternal(populateColumnNames);
    }

    public List<DataHolder> getResultAsDataHoldersList(String sql, boolean populateColumnNames) throws SQLException {
        dbServices.getResultSet(sql);
        return getResultAsDataHoldersListInternal(populateColumnNames);
    }

    protected List<DataHolder> getResultAsDataHoldersListInternal(boolean populateColumnNames) throws SQLException {
        ResultSetMetaData rsmd = dbServices.getCurrentResultSet().getMetaData();
        int colCount = rsmd.getColumnCount();
        List<DataHolder> rows = new ArrayList<>();
        String[] colNames = new String[colCount];
        Map<String, Integer> dups = new HashMap<>();
        if (populateColumnNames) {
            // rename columns if there are duplicate names
            for (int col = 1; col <= colCount; col++) {
                String colName = rsmd.getColumnName(col);
                if (!dups.containsKey(colName)) {
                    dups.put(colName, 0);
                } else {
                    int num = dups.get(colName);
                    num = num + 1;
                    dups.put(colName, num);
                    colName = colName + num;
                }
                colNames[col - 1] = colName;
            }
        }
        while (dbServices.getCurrentResultSet().next()) {
            DataHolder dataHolder = new DataHolder(colCount);
            for (int col = 1; col <= colCount; col++) {
                if (populateColumnNames) {
                    dataHolder.setFieldNameAndValue(col, colNames[col - 1], dbServices.getCurrentResultSet().getObject(col));
                } else {
                    dataHolder.setFieldValue(col, dbServices.getCurrentResultSet().getObject(col));
                }
            }
            rows.add(dataHolder);
        }
        warnings();
        return rows;
    }

    public List<DataHolder> getResultAsDataHoldersList(String sql) throws SQLException {
        return getResultAsDataHoldersList(sql, true);
    }

    public List<DataHolder> getResultAsDataHoldersList(String sql, Collection<?> params) throws SQLException {
        return getResultAsDataHoldersList(sql, params, true);
    }

    public List<DataHolder> getResultsAsDataHoldersList(String sql, Collection<?> params) throws SQLException {
        return getResultsAsDataHoldersList(sql, params, true);
    }

    public List<DataHolder> getResultsAsDataHoldersList(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        return getResultsAsDataHoldersList(sql, params, maxNumberOfParams, placeHolder, true);
    }

    public List<DataHolder> getResultsAsDataHoldersList(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder, boolean populateColumnNames) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getResultsAsDataHoldersListInternal(populateColumnNames);
    }

    public List<DataHolder> getResultsAsDataHoldersList(String sql, Collection<?> params, boolean populateColumnNames) throws SQLException {
        dbServices.getResultSets(sql, params, dbServices.getMaxNumberOfParams(), dbServices.getPlaceHolder());
        return getResultsAsDataHoldersListInternal(populateColumnNames);
    }

    protected List<DataHolder> getResultsAsDataHoldersListInternal(boolean populateColumnNames) throws SQLException {
        ResultSetMetaData rsmd = ((ResultSet) dbServices.getCurrentResultSets().get(0)).getMetaData();
        int colCount = rsmd.getColumnCount();
        List<DataHolder> rows = new ArrayList<>();
        String[] colNames = new String[colCount];
        Map<String, Integer> dups = new HashMap<>();
        // rename columns if there are duplicate names
        for (int col = 1; col <= colCount; col++) {
            String colName = rsmd.getColumnName(col);
            if (!dups.containsKey(colName)) {
                dups.put(colName, 0);
            } else {
                int num = dups.get(colName);
                num = num + 1;
                dups.put(colName, num);
                colName = colName + num;
            }
            colNames[col - 1] = colName;
        }
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                DataHolder dataHolder = new DataHolder(colCount);
                for (int col = 1; col <= colCount; col++) {
                    if (populateColumnNames) {
                        //Object value = ((ResultSet) resultSets.get(i)).getObject(col);
                        dataHolder.setFieldNameAndValue(col, colNames[col - 1], ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(col));
                    } else {
                        dataHolder.setFieldValue(col, ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(col));
                    }
                }
                rows.add(dataHolder);
            }
            warnings(i);
        }
        return rows;
    }

    // **************************************************************************
    /**
     * @return List of Lists
     */
    public List<List<?>> getResultsAsList(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getResultsAsListInternal();
    }

    /**
     * @return List of Lists
     */
    public List<List<?>> getResultsAsList(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSets(sql, params);
        return getResultsAsListInternal();
    }

    protected List<List<?>> getResultsAsListInternal() throws SQLException {
        int colCount = ((ResultSet) dbServices.getCurrentResultSets().get(0)).getMetaData().getColumnCount();
        List<List<?>> rows = new ArrayList<>();
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                List<Object> row = new ArrayList<>(colCount);
                for (int col = 1; col <= colCount; col++) {
                    Object value = ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(col);
                    row.add(value);
                }
                rows.add(row);
            }
            warnings(i);
        }
        return rows;
    }

    /**
     * @return List of Lists
     */
    public List<List<?>> getResultAsList(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getResultAsListInternal();
    }

    /**
     * @return List of Lists
     */
    public List<List<?>> getResultAsList(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getResultAsListInternal();
    }

    protected List<List<?>> getResultAsListInternal() throws SQLException {
        int colCount = dbServices.getCurrentResultSet().getMetaData().getColumnCount();
        List<List<?>> rows = new ArrayList<>();
        while (dbServices.getCurrentResultSet().next()) {
            List<Object> row = new ArrayList<>(colCount);
            for (int col = 1; col <= colCount; col++) {
                row.add(dbServices.getCurrentResultSet().getObject(col));
            }
            rows.add(row);
        }
        warnings();
        return rows;
    }

    // **************************************************************************
    /**
     * @return Map of Lists. Each key is column name and each value is List of
     * column values
     */
    public Map<String, List<?>> getResultsAsMap(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSets(sql, params);
        return getResultsAsMapInternal();
    }

    /**
     * @return Map of Lists. Each key is column name and each value is List of column values
     */
    public Map<String, List<?>> getResultsAsMap(String sql, Collection<?> params, int maxNumberOfParams, String placeHolder) throws SQLException {
        dbServices.getResultSets(sql, params, maxNumberOfParams, placeHolder);
        return getResultsAsMapInternal();
    }

    protected Map<String, List<?>> getResultsAsMapInternal() throws SQLException {
        ResultSetMetaData rsmd = ((ResultSet) dbServices.getCurrentResultSets().get(0)).getMetaData();
        int colCount = rsmd.getColumnCount();
        Map<String, List<?>> columns = new LinkedHashMap<>(colCount);
        List<Object>[] columnValues = new ArrayList[colCount];
        for (int i = 0; i < columnValues.length; i++) {
            columnValues[i] = new ArrayList<>();
        }
        for (int i = 0; i < dbServices.getCurrentResultSets().size(); i++) {
            while (((ResultSet) dbServices.getCurrentResultSets().get(i)).next()) {
                for (int col = 1; col <= colCount; col++) {
                    Object value = ((ResultSet) dbServices.getCurrentResultSets().get(i)).getObject(col);
                    columnValues[col - 1].add(value);
                }
            }
            warnings(i);
        }
        Map<String, Integer> dups = new HashMap<>();
        for (int col = 1; col <= colCount; col++) {
            String colName = rsmd.getColumnName(col);
            if (!dups.containsKey(colName)) {
                dups.put(colName, 0);
            } else {
                int num = dups.get(colName);
                num = num + 1;
                dups.put(colName, num);
                colName = colName + num;
            }
            columns.put(colName, columnValues[col - 1]);
        }
        return columns;
    }

    /**
     * @return Map of Lists. Each key is column name and each value is List of column values
     */
    public Map<String, List<?>> getResultAsMap(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getResultAsMapInternal();
    }

    /**
     * @return Map of Lists. Each key is column name and each value is List of column values
     */
    public Map<String, List<?>> getResultAsMap(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getResultAsMapInternal();
    }

    protected Map<String, List<?>> getResultAsMapInternal() throws SQLException {
        ResultSetMetaData rsmd = dbServices.getCurrentResultSet().getMetaData();
        int colCount = rsmd.getColumnCount();
        Map<String, List<?>> columns = new LinkedHashMap<>(colCount);
        List<Object>[] columnValues = new ArrayList[colCount];
        for (int i = 0; i < columnValues.length; i++) {
            columnValues[i] = new ArrayList<>();
        }
        while (dbServices.getCurrentResultSet().next()) {
            for (int col = 1; col <= colCount; col++) {
                Object value = dbServices.getCurrentResultSet().getObject(col);
                columnValues[col - 1].add(value);
            }
        }
        Map<String, Integer> dups = new HashMap<>();
        for (int col = 1; col <= colCount; col++) {
            String colName = rsmd.getColumnName(col);
            if (!dups.containsKey(colName)) {
                dups.put(colName, 0);
            } else {
                int num = dups.get(colName);
                num = num + 1;
                dups.put(colName, num);
                colName = colName + num;
            }
            columns.put(colName, columnValues[col - 1]);
        }
        warnings();
        return columns;
    }

    // **************************************************************************
    public List<Map<String, ?>> getResultAsListOfMaps(String sql) throws SQLException {
        dbServices.getResultSet(sql);
        return getResultAsListOfMapsInternal();
    }

    public List<Map<String, ?>> getResultAsListOfMaps(String sql, Collection<?> params) throws SQLException {
        dbServices.getResultSet(sql, params);
        return getResultAsListOfMapsInternal();
    }

    protected List<Map<String, ?>> getResultAsListOfMapsInternal() throws SQLException {
        List<Map<String, ?>> list = new ArrayList<>();
        ResultSetMetaData rsmd = dbServices.getCurrentResultSet().getMetaData();
        int colCount = rsmd.getColumnCount();
        List<String> columns = new ArrayList<>();

        Map<String, Integer> dups = new HashMap<>();
        for (int col = 1; col <= colCount; col++) {
            String colName = rsmd.getColumnName(col);
            if (!dups.containsKey(colName)) {
                dups.put(colName, 0);
            } else {
                int num = dups.get(colName);
                num = num + 1;
                dups.put(colName, num);
                colName = colName + num;
            }
            columns.add(colName);
        }
        while (dbServices.getCurrentResultSet().next()) {
            Map<String, Object> rowMap = new LinkedHashMap<>(colCount);
            for (int col = 1; col <= colCount; col++) {
                Object value = dbServices.getCurrentResultSet().getObject(col);
                rowMap.put(columns.get(col - 1), value);
            }
            list.add(rowMap);
        }
        warnings();
        return list;
    }

    // **************************************************************************
    public CharSequence buildInClause(Collection<?> params) {
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("params is empty");
        }
        StringBuilder sb = new StringBuilder();
        int last = params.size() - 1;
        for (int idx = 0; idx < params.size(); idx++) {
            sb.append('?');
            if (idx < last) {
                sb.append(", ");
            }
        }
        return sb;
    }

    /**
     * Build 'IN' clause. If number of params is greater than max than build it
     * using 'OR': (col_1 in ( ?, ?, ?, ?, ? ) or col_1 in ( ?, ?, ?, ?, ? ) or
     * col_1 in ( ?, ? )) Useful in cases when database cannot handle more than
     * specific number of parameters in 'IN' clause: i.e Oracle
     */
    public CharSequence buildInClause(String columnName, Collection<?> params, int max) {
        if (columnName == null || columnName.trim().length() == 0) {
            throw new IllegalArgumentException("columnName is empty");
        }
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("params is empty");
        }
        if (max < 1) {
            throw new IllegalArgumentException("max must be greater then 0");
        }

        final String IN = " in (";
        final String OR = ") or ";

        int j = 0;
        StringBuilder sb = new StringBuilder();

        sb.append('(');
        int ceil = (int) Math.ceil((double) params.size() / (double) max);
        outerLoop:
        for (int n = 0; n < ceil; n++) {
            if (j == params.size()) {
                break;
            }
            sb.append(columnName).append(IN);
            for (int count = 0; count < max; count++) {
                if (j == (params.size())) {
                    int idx = sb.lastIndexOf(COMMA);
                    if (idx > -1) {
                        sb.deleteCharAt(idx);
                    }
                    sb.append(')');
                    break outerLoop;
                }
                sb.append(PARAM_PLACEHOLDER).append(COMMA);
                j++;
            }
            int idx = sb.lastIndexOf(COMMA);
            if (idx > -1) {
                sb.deleteCharAt(idx);
            }
            if (j < (params.size())) {
                sb.append(OR);
            } else {
                sb.append(')');
            }
        }

        sb.append(')');
        //System.out.println(sb);
        return sb;
    }

    public Collection<?> flattenMultiColumnsParams(Collection<Collection<?>> params) {
        List<?> list = new ArrayList<>();
        for (Collection<?> param : params) {
            list.addAll((List) param);
        }
        return list;
    }

    public Collection<?> flattenParams(Collection<Collection<?>> params) {
        List<Object> list = new ArrayList<>();
        for (Object param : params) {
            if (param instanceof Collection) {
                list.addAll((Collection) param);
            } else {
                list.add(param);
            }
        }
        return list;
    }

    /**
     * Build 'IN' clause. If number of params is greater than max than build it
     * using 'OR': ((col_1,col_2) in ((?,?),(?,?),(?,?),(?,?),(?,?)) or
     * (col_1,col_2) in ((?,?),(?,?),(?,?),(?,?),(?,?))) Useful in cases when
     * database cannot handle more than specific number of parameters in 'IN'
     * clause: i.e Oracle
     */
    /**
     * Used together with public CharSequence buildInClause(List<String>
     * columnNames, Collection<Collection> params, int max) to build SQL IN
     * clause
     */
    public CharSequence buildInClause(Collection<String> columnNames, Collection<Collection<?>> params, int max) {
        int maxSize = max / columnNames.size();
        if (columnNames == null || columnNames.isEmpty()) {
            throw new IllegalArgumentException("columnName is empty");
        }
        if (params == null || params.isEmpty()) {
            throw new IllegalArgumentException("params is empty");
        }
        if (maxSize < 1) {
            throw new IllegalArgumentException("max must be equal or greater then number of columns (" + columnNames.size() + ")");
        }

        final String IN = " in (";
        final String OR = ") or ";

        int j = 0;
        StringBuilder sb = new StringBuilder();

        sb.append('(');
        int ceil = (int) Math.ceil((double) params.size() / (double) maxSize);
        outerLoop:
        for (int n = 0; n < ceil; n++) {
            if (j == params.size()) {
                break;
            }
            sb.append('(');
            for (String colName : columnNames) {
                sb.append(colName).append(COMMA);
            }
            int idx = sb.lastIndexOf(COMMA);
            if (idx > -1) {
                sb.deleteCharAt(idx);
            }
            sb.append(')');
            sb.append(IN);
            for (int count = 0; count < maxSize; count++) {
                if (j == (params.size())) {
                    idx = sb.lastIndexOf(COMMA);
                    if (idx > -1) {
                        sb.deleteCharAt(idx);
                    }
                    sb.append(')');
                    break outerLoop;
                }
                sb.append('(');
                for (int z = 0; z < columnNames.size(); z++) {
                    sb.append(PARAM_PLACEHOLDER).append(COMMA);
                }
                idx = sb.lastIndexOf(COMMA);
                if (idx > -1) {
                    sb.deleteCharAt(idx);
                }
                sb.append(')');
                sb.append(COMMA);
                j++;
            }
            idx = sb.lastIndexOf(COMMA);
            if (idx > -1) {
                sb.deleteCharAt(idx);
            }
            if (j < (params.size())) {
                sb.append(OR);
            } else {
                sb.append(')');
            }
        }

        sb.append(')');
        //System.out.println(sb);
        return sb;
    }

    public DBServices getDBServices() {
        return dbServices;
    }

    public void setDBServices(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    private void warnings() throws SQLException {
        if (dbServices.isProcessWarnings()) {
            dbServices.populateWarnings(dbServices.getCurrentResultSet().getWarnings());
            dbServices.getCurrentResultSet().clearWarnings();
        }
    }

    private void warnings(int i) throws SQLException {
        if (dbServices.isProcessWarnings()) {
            dbServices.populateWarnings(((ResultSet) dbServices.getCurrentResultSets().get(i)).getWarnings());
            ((ResultSet) dbServices.getCurrentResultSets().get(i)).clearWarnings();
        }
    }

//    public static void main(String[] args) {
//        String sql = "INSERT INTO PRODUCT (PRODUCT_ID, PRICE, NAME, COMMENTS, CREATED_ON) VALUES (?, ?, ?, ?, ?)";
//        List params = new ArrayList();
//        params.add(51);
//        params.add(299.98);
//        params.add("Zaurus SL-5600");
//        params.add("Good deal!");
//        params.add(new java.sql.Date(12222222222L));
//
//        DBServicesUtils utils = new DBServicesUtils();
//        //pc.setDecimalFormat("#,##0.0000");
//        String s = utils.convert(sql, params);
//        System.out.println(s);
//
//        sql = "UPDATE PRODUCT SET PRICE = ? WHERE PRODUCT.PRODUCT_ID = ?";
//        params.clear();
//        params.add(2.42);
//        params.add(49);
//        s = utils.convert(sql, params);
//        System.out.println(s);
//    }
//    public static void main(String[] args) {
//        List<String> columns = new ArrayList<String>();
//        columns.add("col_1");
//        columns.add("col_2");
//
//        List<List<?>> params = new ArrayList<List<?>>();
//
//        List p0 = new ArrayList();
//        p0.add("aaa0");
//        p0.add("bbb0");
//        params.add(p0);
//
//        List p1 = new ArrayList();
//        p1.add("aaa1");
//        p1.add("bbb1");
//        params.add(p1);
//
//        List p2 = new ArrayList();
//        p2.add("aaa2");
//        p2.add("bbb2");
//        params.add(p2);
//
//         List p3 = new ArrayList();
//        p3.add("aaa3");
//        p3.add("bbb3");
//        params.add(p3);
//
//        List p4 = new ArrayList();
//        p4.add("aaa4");
//        p4.add("bbb4");
//        params.add(p4);
//
//        List p5 = new ArrayList();
//        p5.add("aaa5");
//        p5.add("bbb5");
//        params.add(p5);
//
//        List p6 = new ArrayList();
//        p6.add("aaa6");
//        p6.add("bbb6");
//        params.add(p6);
//
//        List p7 = new ArrayList();
//        p7.add("aaa7");
//        p7.add("bbb7");
//        params.add(p7);
//
//        List p8 = new ArrayList();
//        p8.add("aaa8");
//        p8.add("bbb8");
//        params.add(p8);
//
//        List p9 = new ArrayList();
//        p9.add("aaa9");
//        p9.add("bbb9");
//        params.add(p9);
//
//        DBServicesUtils utils = new DBServicesUtils();
//        CharSequence in = utils.buildInClause(columns, params, 10);
//        List flatParams = utils.flattenMultiColumnsParams(params);
//        System.out.println(in);
//        System.out.println(flatParams);
//    }
}

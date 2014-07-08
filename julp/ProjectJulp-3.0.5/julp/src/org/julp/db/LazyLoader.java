package org.julp.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.DataAccessException;
import org.julp.DomainObject;
import org.julp.Wrapper;

public class LazyLoader<T> {

    private DomainObjectFactory<T> domainObjectFactory;
    private DomainObjectFactory<T> lazyLoadedDomainObjectFactory;
    private List<DomainObject<T>> objectList = new ArrayList<>();
    private String originalSql;
    /**
     * Hold primary keys from all tables for the SELECT
     */
    private Set<String> pkColumns = new HashSet<>();
    protected static final Object[] EMPTY_ARG = new Object[0];
    private static final String DOT = ".";
    private boolean lowerCaseKeywords = true;
    private static final transient Logger logger = Logger.getLogger(LazyLoader.class.getName());

    public LazyLoader() {
    }

    public LazyLoader(DomainObjectFactory<T> domainObjectFactory) {
        this.domainObjectFactory = domainObjectFactory;
        init();
    }

    public DomainObjectFactory<T> getDomainObjectFactory() {
        return domainObjectFactory;
    }

    private void init() {
        try {
            lazyLoadedDomainObjectFactory = new DomainObjectFactory<>();
            lazyLoadedDomainObjectFactory.setDomainClass(domainObjectFactory.getDomainClass());
            lazyLoadedDomainObjectFactory.setMetaData(domainObjectFactory.getMetaData());
            lazyLoadedDomainObjectFactory.getDataReader().setMetaData(domainObjectFactory.getMetaData());
            if (domainObjectFactory.getOptions() != null && domainObjectFactory.getOptions().containsKey(DBDataReader.Options.resultSetIndexToFieldMap)) {
                Map<Enum<?>, Object> options = domainObjectFactory.getOptions();
                options.remove(DBDataReader.Options.resultSetIndexToFieldMap);
                if (!options.isEmpty()) {
                    lazyLoadedDomainObjectFactory.setOptions(options);
                }
            } else {
                lazyLoadedDomainObjectFactory.setOptions(domainObjectFactory.getOptions());
            }
            lazyLoadedDomainObjectFactory.setDBServices(domainObjectFactory.getDBServices());
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
            throw new DataAccessException(e);
        }
    }

    public void setDomainObjectFactory(DomainObjectFactory<T> domainObjectFactory) {
        this.domainObjectFactory = domainObjectFactory;
        init();
    }

//    private void clear() {
//        if (this.objectList != null) {
//            this.objectList.clear();
//        }
//    }
    @SuppressWarnings("rawtypes")
    public List<T> getLazyLoadedObjects(String sql, Collection params, Set<String> optionalFields) {
        return loadLazyObjects(sql, params, optionalFields);
    }

    public List<T> getLazyLoadedObjects(String sql, Set<String> optionalFields) {
        return loadLazyObjects(sql, null, optionalFields);
    }

    /**
     * Return list of objects loaded only with primary keys and optional fields
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<T> loadLazyObjects(String sql, Collection params, Set<String> optionalFields) {
        ResultSet rs = null;
        this.originalSql = sql;
        try {
            Set<String> optionalColumns = null;
            if (optionalFields != null) {
                optionalColumns = new HashSet<>(optionalFields.size());
                for (String fieldName : optionalFields) {
                    int colIndex = ((DBMetaData<T>) domainObjectFactory.getMetaData()).getFieldIndexByFieldName(fieldName);
                    optionalColumns.add(((DBMetaData<T>) domainObjectFactory.getMetaData()).getFullColumnName(colIndex));
                }
            }
            rs = getKeyColumns(((DBMetaData<T>) domainObjectFactory.getMetaData()).getTables(), sql, params, optionalColumns);
            lazyLoadedDomainObjectFactory.getDataReader();
            lazyLoadedDomainObjectFactory.setLazyLoading(true);
            lazyLoadedDomainObjectFactory.load(new Wrapper(rs));
            //this.objectList.addAll((List<DomainObject<T>>) lazyLoadedDomainObjectFactory.getObjectList());
            this.objectList.addAll((List) lazyLoadedDomainObjectFactory.getObjectList());
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.log(Level.SEVERE, null, e);
                }
            }
        }
        return (List<T>) this.objectList;
    }

    @SuppressWarnings("rawtypes")
    public ResultSet getKeyColumns(Map<String, String[]> tables, String sql, Collection params, Set<String> optionalColumns) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getKeyColumns()::sql: " + sql + "::params: " + params + " + ::optionalColumns: " + optionalColumns + "\n");
        }
        Set<String> columns = new HashSet<>();
        StringBuilder sb = new StringBuilder(lowerCaseKeywords ? "select " : "SELECT ");
        for (Map.Entry<String, String[]> entry : tables.entrySet()) {
            Set<String> pk = PKCache.getInstance().getPrimaryKey(domainObjectFactory.getDBServices().getConnection(), entry.getValue()[0], entry.getValue()[1], entry.getValue()[2]);
            columns.addAll(pk);
            pkColumns.addAll(pk);
        }

        if (optionalColumns != null) {
            for (String columnsName : optionalColumns) {
                columns.add(columnsName);
            }
        }

        Map<String, String> mapping = new HashMap<>(pkColumns.size());
        for (String column : columns) {
            mapping.put(column, (String) domainObjectFactory.getMapping().get(column));
        }
        lazyLoadedDomainObjectFactory.setMapping(mapping);
        lazyLoadedDomainObjectFactory.populateMetaData();
        Map<Integer, String> resultSetIndexToFieldMap = new HashMap<>(columns.size());
        int idx = sql.toLowerCase().indexOf(" from ");
        if (idx > -1) {
            String generatedSql = sql.substring(idx);
            int count = 1;
            for (String colName : columns) {
                sb.append(colName).append(", ");
                int n = ((DBMetaData<T>) lazyLoadedDomainObjectFactory.getMetaData()).getColumnIndexByFullColumnName(colName);
                resultSetIndexToFieldMap.put(count, lazyLoadedDomainObjectFactory.getMetaData().getFieldName(n));
                count++;
            }
            Map<Enum<?>, Object> options = lazyLoadedDomainObjectFactory.getOptions();
            if (options == null) {
                options = new HashMap<>();
            }
            options.put(DBDataReader.Options.resultSetIndexToFieldMap, resultSetIndexToFieldMap);
            lazyLoadedDomainObjectFactory.setOptions(options);
            int n = sb.lastIndexOf(",");
            if (n > -1) {
                sb.deleteCharAt(n);
            }
            sb.append(generatedSql);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getKeyColumns()::generatedSql: " + generatedSql + " \n");
            }
        }
        if (params == null) {
            return lazyLoadedDomainObjectFactory.getDBServices().getResultSet(sb.toString());
        } else {
            return lazyLoadedDomainObjectFactory.getDBServices().getResultSet(sb.toString(), params);
        }
    }

    /**
     * Return list of fully loaded objects
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<T> getLoadedObjects(List<T> lazyLoadedObjects) throws Exception {
        Collection params = new ArrayList();
        int[] colIndexes = new int[pkColumns.size()];
        int i = 0;
        for (String columnName : pkColumns) {
            if (columnName.indexOf(DOT) > -1) {
                colIndexes[i] = ((DBMetaData<T>) domainObjectFactory.getMetaData()).getColumnIndexByFullColumnName(columnName);
            } else {
                colIndexes[i] = ((DBMetaData<T>) domainObjectFactory.getMetaData()).getColumnIndexByColumnName(columnName);
            }
            i++;
        }
        String generatedWhere = generateWhere();
        String tmp = originalSql.toLowerCase();
        String generatedSql = null;
        int joinIdx = tmp.indexOf(" join ");
        if (joinIdx > -1) {
            int whereIdx = tmp.indexOf(" where ", joinIdx);
            if (whereIdx > -1) {
                generatedSql = originalSql.substring(0, whereIdx) + generatedWhere;
            }
        } else {
            int groupByIdx = tmp.indexOf(" group by ");
            int orderByIdx = tmp.indexOf(" order by ");
            if (groupByIdx > -1) {
                generatedSql = originalSql.substring(0, groupByIdx);
                generatedSql = generatedSql + generatedWhere;
            } else {
                if (orderByIdx > -1) {
                    generatedSql = originalSql.substring(0, orderByIdx);
                    generatedSql = generatedSql + generatedWhere;
                }
            }
        }

        if (generatedSql == null) {
            generatedSql = originalSql + generatedWhere;
        }

        StringBuilder sb = new StringBuilder();
        for (T domainObject : lazyLoadedObjects) {
            sb.append(generatedSql).append(lowerCaseKeywords ? "\n union all \n" : "\n UNION ALL \n");
            try {
                for (int n = 0; n < pkColumns.size(); n++) {
                    params.add(readValue(colIndexes[n], (DomainObject) domainObject));
                }
            } catch (Throwable ex) {
                throw new DataAccessException(ex);
            }
        }

        int unionIndex = sb.lastIndexOf(lowerCaseKeywords ? " union all" : " UNION ALL");
        if (unionIndex > -1) {
            sb.delete(unionIndex, sb.length() - 1);
        }

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getLoadedObjects()::generatedSql: " + sb + " params: " + params + "\n");
        }

        // domainObjectFactory.setLazyLoading(false);
        //System.out.println(sb + " " + params);
        domainObjectFactory.load(new Wrapper(domainObjectFactory.getDBServices().getResultSet(sb.toString(), params)));
        return (List<T>) domainObjectFactory.getObjectList();
    }

    protected Object readValue(int ind, DomainObject<T> domainObject) throws Throwable {
        Object value = null;
        try {
            Method method = domainObjectFactory.getMetaData().getReadMethod(ind);
            value = method.invoke(domainObject, EMPTY_ARG);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw ((InvocationTargetException) t).getTargetException();
            } else {
                throw t;
            }
        }
        return value;
    }

    protected String generateWhere() {
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (String column : pkColumns) {
            if (count == 0) {
                if (originalSql.toLowerCase().indexOf(" where ") == -1) {
                    sb.append(lowerCaseKeywords ? " where " : " WHERE ").append(column).append(" = ?");
                } else {
                    sb.append(lowerCaseKeywords ? " and " : " AND ").append(column).append(" = ?");
                }
            } else {
                sb.append(lowerCaseKeywords ? " and " : " AND ").append(column).append(" = ?");
            }
            count++;
        }
        int idx = sb.lastIndexOf(",");
        if (idx > -1) {
            sb.deleteCharAt(idx);
        }
        return sb.toString();
    }

    public void setOriginalSelect(String sql) {
        this.originalSql = sql;
    }

    public void setPrimaryKey(Set<String> pkColumns) {
        this.pkColumns = pkColumns;
    }

    public boolean isLowerCaseKeywords() {
        return lowerCaseKeywords;
    }

    public void setLowerCaseKeywords(boolean lowerCaseKeywords) {
        this.lowerCaseKeywords = lowerCaseKeywords;
    }
}

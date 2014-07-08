package org.julp.db;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.CGLibInstantiator;
import org.julp.Converter;
import org.julp.DataAccessException;
import org.julp.DataHolder;
import org.julp.DataReader;
import org.julp.DomainObject;
import org.julp.DomainObjectInstantiator;
import org.julp.Instantiator;
import org.julp.MetaData;
import org.julp.Wrapper;

public class DBDataReader<T> implements DataReader<T> {

    private static final long serialVersionUID = 1L;
    protected DBMetaData<T> metaData;
    protected Converter converter;
    protected Class<T> domainClass;
    protected boolean lazyLoading = false;
    protected boolean readOnly = false;
    protected boolean useColumnName = false;
    protected Instantiator<T> instantiator = null;
    protected boolean ignoreMissingFields;
    protected Map<Enum<?>, Object> options;
    protected int offset = 0;
    protected int limit = Integer.MAX_VALUE;
    private static final transient Logger logger = Logger.getLogger(DBDataReader.class.getName());
    /**
     * This map must be used if JDBC driver does not support ResultSetMetaData.getTableName() and ResultSet has more than one column with the same name
     */
    private Map<Integer, String> resultSetIndexToFieldMap;

    public enum Options {
        lazyLoading, readOnly, useColumnName, instantiator, resultSetIndexToFieldMap, ignoreMissingFields;
    }

    public DBDataReader() {
        
    }

    @Override
    public List<DomainObject<T>> readData(Wrapper data, int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        return readData(data);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<DomainObject<T>> readData(Wrapper data) {
        if (resultSetIndexToFieldMap != null && !resultSetIndexToFieldMap.isEmpty()) {
            return readData1(data);
        }
        List<DomainObject<T>> objectList = new ArrayList<>();
        ResultSet rs;
        try {            
            rs = data.unwrap(ResultSet.class);
            if (offset > 0) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::readData()::offset: " + offset + "::limit: " + limit);
                    logger.finest("julp::" + this + "::readData()::ResultSet.getRow()::before absolute(): " + rs.getRow());
                }
                rs.absolute(offset);
                if (logger.isLoggable(Level.FINEST)) {                    
                    logger.finest("julp::" + this + "::readData()::ResultSet.getRow()::after absolute(): " + rs.getRow());
                }                
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        long ts1 = -1;
        long ts2 = -1;
        if (logger.isLoggable(Level.FINEST)) {
            ts1 = System.currentTimeMillis();
        }

        ResultSetMetaData rsmd;
        List<String> duplicateColumnsCheck = null;
        try {
            rsmd = rs.getMetaData();
            int rsColCount = rsmd.getColumnCount();
            Map<Integer, Integer> resultSetColumnIndexes = new HashMap<>(rsColCount);
            for (int resultSetColIndex = 1; resultSetColIndex <= rsColCount; resultSetColIndex++) {
                String column = rsmd.getColumnName(resultSetColIndex);
                if (duplicateColumnsCheck == null) {
                    duplicateColumnsCheck = new ArrayList<>();
                }
                if (duplicateColumnsCheck.contains(column)) {
                    duplicateColumnsCheck.add(column);
                    String msg = "Since your JDBC driver does not support ResultSetMetaData.getTableName() and your SELECT statement has duplicate column name (" + column + ") you must use DBReadData.setOptions(DBReadData.resultSetIndexToFieldMap, map). \nMap must have key=index of column in SELECT statement and value=fieldName e.g.: 1=customerId, 2=lastName. etc";
                    logger.severe(msg);
                    System.out.println(msg);
                    throw new DataAccessException("Duplicate column name in ResultSet: " + rsmd.getColumnName(resultSetColIndex));
                } else {
                    duplicateColumnsCheck.add(column);
                }
                int metaDataColumnIndex = metaData.getColumnIndexByColumnName(column);
                resultSetColumnIndexes.put(metaDataColumnIndex, resultSetColIndex);
            }

            int metaDataColumnsCount = this.metaData.getFieldCount();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::readData()::metaDataColumnsCount: " + metaDataColumnsCount);
            }
            if (converter == null) {
                converter = new DBConverter(data);
            } else {
                converter.setData(data);
            }
            domainClass = metaData.getDomainClass();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::readData()::domainClass class::" + domainClass.getName());
            }

            int row = 0;
            while (rs.next() && row < limit) {
                DataHolder originalValues = null;
                if (!readOnly && !lazyLoading) {
                    originalValues = new DataHolder(metaDataColumnsCount);
                }
                DomainObject<T> domainObject = (DomainObject<T>) getInstantiator().newInstance(domainClass);
                domainObject.setLoading(true);
                for (int metaDataColumnIndex = 1; metaDataColumnIndex <= metaDataColumnsCount; metaDataColumnIndex++) {
                    int resultSetColumnIndex = -1;
                    if (resultSetColumnIndexes.containsKey(metaDataColumnIndex)) {
                        resultSetColumnIndex = resultSetColumnIndexes.get(metaDataColumnIndex);
                    }
                    String fieldName = this.metaData.getFieldName(metaDataColumnIndex);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::readData()::fieldName: " + fieldName);
                    }
                    if (fieldName != null) {  // ResultSet may have more columns then this Object fields, so skip unneeded column
                        Object[] convertedValue = null;
                        String fieldClassName = this.metaData.getFieldClassName(metaDataColumnIndex);
                        if (useColumnName) {
                            convertedValue = converter.convert(fieldClassName, this.metaData.getColumnName(metaDataColumnIndex));
                        } else {
                            if (resultSetColumnIndex != -1) {
                                convertedValue = converter.convert(fieldClassName, resultSetColumnIndex);
                            } else {
                                this.metaData.setWritable(metaDataColumnIndex, false);
                            }
                        }
                        if (!readOnly && !lazyLoading) {
                            if (resultSetColumnIndex != -1) {
                                originalValues.setFieldNameAndValue(metaDataColumnIndex, fieldName, convertedValue[0]);
                            } else {
                                originalValues.setFieldNameAndValue(metaDataColumnIndex, fieldName, null);
                            }
                        }
                        if (this.metaData.getWriteMethod(metaDataColumnIndex) == null && !ignoreMissingFields) {
                            throw new IllegalArgumentException("Write method for field '" + fieldName + "' is null. Make sure field name has correct field in class " + domainClass.getName());
                        } else if (this.metaData.getWriteMethod(metaDataColumnIndex) == null && ignoreMissingFields) {
                            // ignore
                        } else {
                            if (resultSetColumnIndex != -1) {
                                this.metaData.getWriteMethod(metaDataColumnIndex).invoke(domainObject, convertedValue);
                            }
                        }
                    }
                    if (logger.isLoggable(Level.FINEST)) {
                        System.out.print("\n");
                    }
                }
                if (!readOnly && !lazyLoading) {
                    domainObject.setOriginalValues(originalValues);
                }
                domainObject.setLoading(false);
                if (!readOnly && !lazyLoading) {
                    domainObject.setLoaded(true);
                }
                objectList.add(domainObject);
                row++;
            }

            if (logger.isLoggable(Level.FINEST)) {
                ts2 = System.currentTimeMillis();
                logger.finest("julp::" + this + "::readData() total: " + (ts2 - ts1 + " ms"));
            }
            
            offset = 0;
            limit = Integer.MAX_VALUE;
        } catch (InvocationTargetException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e.getTargetException());
        } catch (IllegalAccessException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e);
        }
        return objectList;
    }

    @SuppressWarnings("unchecked")
    protected List<DomainObject<T>> readData1(Wrapper data) {
        List<DomainObject<T>> objectList = new ArrayList<>();
        int columnCount = resultSetIndexToFieldMap.size();
        ResultSet rs;
        try {            
            rs = data.unwrap(ResultSet.class);
            if (offset > 0) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::readData1()::offset: " + offset + "::limit: " + limit);
                    logger.finest("julp::" + this + "::readData1()::ResultSet.getRow()::before absolute(): " + rs.getRow());
                }
                rs.absolute(offset);
                if (logger.isLoggable(Level.FINEST)) {                    
                    logger.finest("julp::" + this + "::readData1()::ResultSet.getRow()::after absolute(): " + rs.getRow());
                }                
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
                
        long ts1 = -1;
        long ts2 = -1;

        if (logger.isLoggable(Level.FINEST)) {
            ts1 = System.currentTimeMillis();
        }

        try {
            if (converter == null) {
                converter = new DBConverter(data);
            } else {
                converter.setData(data);
            }
            domainClass = metaData.getDomainClass();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::readData1()::domainClass class::" + domainClass.getName());
            }
            while (rs.next()) {
                DataHolder originalValues = null;
                if (!readOnly && !lazyLoading) {
                    originalValues = new DataHolder(columnCount);
                }
                DomainObject<T> domainObject = (DomainObject<T>) getInstantiator().newInstance(domainClass);
                domainObject.setLoading(true);
                for (int metaDataColumnIndex = 1; metaDataColumnIndex <= columnCount; metaDataColumnIndex++) {
                    String fieldName = this.resultSetIndexToFieldMap.get(metaDataColumnIndex);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::readData1()::fieldName: " + fieldName);
                    }

                    Object[] convertedValue = null;
                    int idx = metaData.getFieldIndexByFieldName(fieldName);
                    String fieldClassName = this.metaData.getFieldClassName(idx);

                    convertedValue = converter.convert(fieldClassName, metaDataColumnIndex);

                    if (!readOnly && !lazyLoading) {
                        originalValues.setFieldNameAndValue(metaDataColumnIndex, fieldName, convertedValue[0]);
                    }
                    if (this.metaData.getWriteMethod(idx) == null) {
                        throw new IllegalArgumentException("Write method for field '" + fieldName + "' is null. Make sure field name has correct field in class " + domainClass.getName());
                    } else {
                        this.metaData.getWriteMethod(idx).invoke(domainObject, convertedValue);
                    }

                    if (logger.isLoggable(Level.FINEST)) {
                        System.out.print("\n");
                    }
                }
                if (!readOnly && !lazyLoading) {
                    domainObject.setOriginalValues(originalValues);
                }
                domainObject.setLoading(false);
                if (!readOnly && !lazyLoading) {
                    domainObject.setLoaded(true);
                }
                objectList.add(domainObject);
            }

            if (logger.isLoggable(Level.FINEST)) {
                ts2 = System.currentTimeMillis();
                logger.finest("julp::" + this + "::readData1() total: " + (ts2 - ts1 + " ms"));
            }
            
            offset = 0;
            limit = Integer.MAX_VALUE;
        } catch (InvocationTargetException e) {
            logger.throwing(getClass().getName(), "readData1", e);
            throw new DataAccessException(e.getTargetException());
        } catch (IllegalAccessException e) {
            logger.throwing(getClass().getName(), "readData1", e);
            throw new DataAccessException(e);
        } catch (SQLException e) {
            logger.throwing(getClass().getName(), "readData1", e);
            throw new DataAccessException(e);
        }
        return objectList;
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
    public Converter getConverter() {
        if (converter == null) {
            converter = new DBConverter();
        }
        return converter;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public Class<T> getDomainClass() {
        return domainClass;
    }

    @Override
    public void setInstantiator(Instantiator<T> instantiator) {
        this.instantiator = instantiator;
    }

    @Override
    public Instantiator<T> getInstantiator() {
        if (instantiator == null) {
            createInstantiator();
        }
        return this.instantiator;
    }

    protected void createInstantiator() {
        if (domainClass == null) {
            if (metaData == null) {
                throw new DataAccessException("MetaData is null");
            }
            domainClass = getMetaData().getDomainClass();
        }
        if (!DomainObject.class.isAssignableFrom(domainClass)) {
            if (instantiator == null) {
                instantiator = new CGLibInstantiator<>();
            } else {
                if (!CGLibInstantiator.class.isAssignableFrom(instantiator.getClass())) {
                    instantiator = new CGLibInstantiator<>();
                }
            }
        } else {
            if (instantiator == null) {
                instantiator = new DomainObjectInstantiator<>();
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::createInstantiator()::instantiator: " + instantiator);
        }
    }

    @Override
    public boolean isLazyLoading() {
        return lazyLoading;
    }

    @Override
    public void setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
    }
    
    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isUseColumnName() {
        return useColumnName;
    }

    public void setUseColumnName(boolean useColumnName) {
        this.useColumnName = useColumnName;
    }

    public boolean isIgnoreMissingFields() {
        return ignoreMissingFields;
    }

    public void setIgnoreMissingFields(boolean ignoreMissingFields) {
        this.ignoreMissingFields = ignoreMissingFields;
    }

    public long getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    public Map<Integer, String> getResultSetIndexToFieldMap() {
        return resultSetIndexToFieldMap;
    }

    public void setResultSetIndexToFieldMap(Map<Integer, String> resultSetIndexToFieldMap) {
        this.resultSetIndexToFieldMap = resultSetIndexToFieldMap;
    }

    @Override
    public Map<Enum<?>, Object> getOptions() {
        return this.options;
    }

    @Override
    public void setOptions(Map<Enum<?>, Object> options) {
        if (options == null) {
            return;
        }
        this.options = options;
        Object lazyLoadingOption = options.get(Options.lazyLoading);
        if (lazyLoadingOption != null) {
            lazyLoading = (Boolean) lazyLoadingOption;
        }
        Object readOnlyOption = options.get(Options.readOnly);
        if (readOnlyOption != null) {
            readOnly = (Boolean) readOnlyOption;
        }
        Object useColumnNameOption = options.get(Options.useColumnName);
        if (useColumnNameOption != null) {
            useColumnName = (Boolean) useColumnNameOption;
        }
        Object instantiatorOption = options.get(Options.instantiator);
        if (instantiatorOption != null) {
            instantiator = (Instantiator<T>) instantiatorOption;
        }
        Object resultSetIndexToFieldMapOption = options.get(Options.resultSetIndexToFieldMap);
        if (resultSetIndexToFieldMapOption != null) {
            resultSetIndexToFieldMap = (Map<Integer, String>) resultSetIndexToFieldMapOption;
        }
        Object ignoreMissingFieldsOption = options.get(Options.ignoreMissingFields);
        if (ignoreMissingFieldsOption != null) {
            ignoreMissingFields = (Boolean) ignoreMissingFieldsOption;
        }
    }
}

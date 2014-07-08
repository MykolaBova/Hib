package org.julp.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

public class CsvDataReader<T> implements DataReader<T> {
    
    private static final long serialVersionUID = 1L;

    protected CsvMetaData metaData;
    protected Converter converter;
    protected Class domainClass;
    protected boolean readOnly = false;
    protected boolean lazyLoading = false;
    protected Instantiator instantiator = null;
    protected boolean ignoreMissingFields;
    protected boolean header;
    protected String fieldSeparator = ",";
    protected Map<Enum<?>, Object> options;
    protected int offset = 0;
    protected int limit = Integer.MAX_VALUE;
    private final transient Logger logger = Logger.getLogger(getClass().getName());

    public enum Options {
        readOnly, ignoreMissingFields, header, fieldSeparator;
    }

    public CsvDataReader() {
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public Converter getConverter() {
        if (converter == null) {
            converter = new CsvConverter();
        }
        return converter;
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    @Override
    public List<DomainObject<T>> readData(Wrapper data, int offset, int limit) {
        this.offset = offset;
        this.limit = limit;
        return readData(data);
    }

    @Override
    public List<DomainObject<T>> readData(Wrapper data) {
        List<DomainObject<T>> objectList = new ArrayList<>();
        BufferedReader br = null;
        try {
            Reader r = data.unwrap(Reader.class);
            if (!(r instanceof BufferedReader)) {
                br = new BufferedReader(r);
            } else {
                br = (BufferedReader) r;
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
                converter = new CsvConverter();
            }
            domainClass = metaData.getDomainClass();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::readData()::domainClass class::" + domainClass.getName());
            }
            int rowToLoad = 0;
            int rowToSkip = 0;
            String line;
            while ((line = br.readLine()) != null && line.trim().length() > 0) {
                if (header && rowToLoad == 0) {
                    rowToLoad++;
                    continue;
                }
                if (offset > 0 && rowToSkip < offset) {
                    rowToSkip++;
                    continue;
                }
                if (header && (rowToLoad - 1) == limit) {
                    break;
                } else if (!header && (rowToLoad) == limit) {
                    break;
                }
                converter.setData(new Wrapper(line.split(fieldSeparator)));
                DataHolder originalValues = null;
                if (!readOnly && !lazyLoading) {
                    originalValues = new DataHolder(metaData.getFieldCount());
                }
                DomainObject domainObject = (DomainObject) getInstantiator().newInstance(domainClass);
                domainObject.setLoading(true);
                for (int metaDataColumnIndex = 1; metaDataColumnIndex <= metaData.getFieldCount(); metaDataColumnIndex++) {
                    String fieldName = (String) this.metaData.getFieldName(metaDataColumnIndex);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::readData()::fieldName: " + fieldName);
                    }

                    Object[] convertedValue = null;
                    int idx = metaData.getFieldIndexByFieldName(fieldName);
                    String fieldClassName = this.metaData.getFieldClassName(idx);

                    convertedValue = converter.convert(fieldClassName, getColumnIndexByFieldName(fieldName));

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
                rowToLoad++;
                rowToSkip++;
            }

            if (logger.isLoggable(Level.FINEST)) {
                ts2 = System.currentTimeMillis();
                logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::readData() total: " + (ts2 - ts1 + " ms"));
            }

            offset = 0;
            limit = Integer.MAX_VALUE;
        } catch (InvocationTargetException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e.getTargetException());
        } catch (IllegalAccessException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e);
        } catch (IOException e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e);
        }
        return objectList;
    }
    
    protected int getColumnIndexByFieldName(String fieldName) {
        int idx = -1;
        Set<Map.Entry<String, String>> set = getMetaData().getMapping().entrySet();
        for (Map.Entry<String, String> entry : set) {
            if (entry.getValue().equals(fieldName)) {
                idx = Integer.parseInt(entry.getKey());
                return idx;
            }
        }
        return idx;
    }

    @Override
    public CsvMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public void setMetaData(MetaData metaData) {
        this.metaData = (CsvMetaData) metaData;
    }

    @Override
    public Class getDomainClass() {
        return this.domainClass;
    }

    @Override
    public Map<Enum<?>, Object> getOptions() {
        return this.options;
    }

    public long getOffset() {
        return offset;
    }

    public int getLimit() {
        return limit;
    }

    @Override
    public void setOptions(Map options) {
        if (options == null) {
            return;
        }
        this.options = options;
        Object readOnlyOption = options.get(Options.readOnly);
        if (readOnlyOption != null) {
            readOnly = (Boolean) readOnlyOption;
        }
        Object ignoreMissingFieldsOption = options.get(Options.ignoreMissingFields);
        if (ignoreMissingFieldsOption != null) {
            ignoreMissingFields = (Boolean) ignoreMissingFieldsOption;
        }
        Object headerOption = options.get(Options.header);
        if (headerOption != null) {
            header = (Boolean) headerOption;
        }
        Object fieldSeparatorOption = options.get(Options.fieldSeparator);
        if (fieldSeparatorOption != null) {
            fieldSeparator = (String) fieldSeparatorOption;
        }
    }

    @Override
    public void setInstantiator(Instantiator instantiator) {
        this.instantiator = instantiator;
    }

    @Override
    public Instantiator getInstantiator() {
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
                instantiator = new CGLibInstantiator<T>();
            } else {
                if (!CGLibInstantiator.class.isAssignableFrom(instantiator.getClass())) {
                    instantiator = new CGLibInstantiator<T>();
                }
            }
        } else {
            if (instantiator == null) {
                instantiator = new DomainObjectInstantiator<T>();
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::createInstantiator()::instantiator: " + instantiator);
        }
    }

    @Override
    public boolean isLazyLoading() {
        return this.lazyLoading;
    }

    @Override
    public void setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
    }
}

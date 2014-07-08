package org.julp.xls;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Sheet;
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

public class XlsDataReader<T> implements DataReader<T> {

    protected XlsMetaData metaData;
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

    public XlsDataReader() {
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }

    @Override
    public Converter getConverter() {
        if (converter == null) {
            converter = new XlsConverter();
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
        Sheet sheet = null;
        try {
            sheet = data.unwrap(Sheet.class);
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
                converter = new XlsConverter();
            }
            domainClass = metaData.getDomainClass();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::readData()::domainClass class: " + domainClass.getName());
            }
            int rowToLoad = 0;            
            for (int i = 0; i < sheet.getRows(); i++) {
                if (header && rowToLoad == 0) {
                    rowToLoad++;                    
                    continue;               
                }
                if (offset > 0 && i < offset) {                  
                     i++;
                    continue;                       
                }     
                if (header && (rowToLoad - 1) == limit) {
                    break;
                } else if (!header && (rowToLoad) == limit) {
                    break;                    
                }                
                converter.setData(new Wrapper(sheet.getRow(i)));
                DataHolder originalValues = null;
                if (!readOnly && !lazyLoading) {
                    originalValues = new DataHolder(metaData.getFieldCount());
                }
                DomainObject<T> domainObject = (DomainObject<T>) getInstantiator().newInstance(domainClass);
                domainObject.setLoading(true);
                for (int metaDataColumnIndex = 1; metaDataColumnIndex <= metaData.getFieldCount(); metaDataColumnIndex++) {
                    String fieldName = (String) this.metaData.getFieldName(metaDataColumnIndex);
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::readData()::fieldName: " + fieldName);
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
                rowToLoad++;                
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
        } catch (Exception e) {
            logger.throwing(getClass().getName(), "readData", e);
            throw new DataAccessException(e);
        }
        return objectList;
    }

    @Override
    public XlsMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public void setMetaData(MetaData metaData) {
        this.metaData = (XlsMetaData) metaData;
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

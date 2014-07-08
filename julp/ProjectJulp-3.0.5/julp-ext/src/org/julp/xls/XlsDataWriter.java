package org.julp.xls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.write.Label;
import jxl.write.WritableSheet;
import org.julp.AbstractDomainObjectFactory;
import org.julp.AbstractMetaData;
import org.julp.Converter;
import org.julp.DataAccessException;
import org.julp.DataWriter;
import org.julp.DomainObject;
import org.julp.MetaData;
import org.julp.PersistentState;

public class XlsDataWriter<T> implements DataWriter<T> {

    protected int removedCount = 0;
    protected int createdCount = 0;
    protected int modifiedCount = 0;
    protected List<DomainObject> removedObjects = null;
    protected List<DomainObject> createdObjects = null;
    protected List<DomainObject> modifiedObjects = null;
    protected AbstractDomainObjectFactory objectFactory = null;
    protected static final Object[] EMPTY_READ_ARG = new Object[0];
    protected Throwable persistenceError = null;
    protected boolean exceptionOnEmptyObjectList = false;
    protected boolean header;
    protected boolean qoutedValues;
    protected Converter converter;
    //protected String fieldSeparator = ",";    
    protected static final String QUOTE = "\"";
    protected Map<Enum<?>, Object> options;
    /**
     * Disable modifications
     */
    protected boolean readOnly = false;
    /**
     * Throw Exception or ignore if DomainObject has less fields than mapping
     */
    protected boolean throwMissingFieldException = false;
    protected XlsMetaData metaData;
    private final transient Logger logger = Logger.getLogger(getClass().getName());

    public enum Options {        
        exceptionOnEmptyObjectList,
        readOnly,
        throwMissingFieldException,
        exceptionHandler,
        qoutedValues,
        header
    };

    public XlsDataWriter() {
    }

    protected void init() {
        removedCount = 0;
        createdCount = 0;
        modifiedCount = 0;
        if (metaData == null) {
            objectFactory.populateMetaData();
        }
        if (removedObjects == null) {
            removedObjects = new ArrayList();
        }
        if (createdObjects == null) {
            createdObjects = new ArrayList();
        }
        if (modifiedObjects == null) {
            modifiedObjects = new ArrayList();
        }
        converter = new XlsConverter();
    }

    @Override
    public boolean writeData(AbstractDomainObjectFactory objectFactory) {
        if (objectFactory.isReadOnly()) {
            setPersistenceError(new DataAccessException("Read Only"));
            return false;
        }
        this.objectFactory = objectFactory;
        WritableSheet sheet = ((XlsDomainObjectFactory) objectFactory).getSheet();
        try {
            this.init();
        } catch (Exception e) {
            setPersistenceError(e);
            return false;
        }
        boolean success = true;
        boolean empty = false;
        try {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::writeData()::objectFactory.getObjectList(): " + objectFactory.getObjectList());
            }
            if (objectFactory.getObjectList() == null || objectFactory.getObjectList().isEmpty()) {
                empty = true;
            }
            removedObjects = objectFactory.getRemovedObjects();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::writeData()::removedObjects: " + removedObjects);
            }
            if ((removedObjects == null || removedObjects.isEmpty()) && empty) {
                if (exceptionOnEmptyObjectList) {
                    setPersistenceError(new DataAccessException("Nothing to write"));
                    return false;
                }
                return true;
            }
            this.removedCount = objectFactory.getRemovedObjects().size();
            int n = 0;
            if (header) {
                for (int i = 1; i <= metaData.getFieldCount(); i++) {
                    String fieldLabel = metaData.getFieldLabel(i);
                    if (fieldLabel == null) {
                        fieldLabel = metaData.toLabel(metaData.getFieldName(i));
                    }
                    Label label = new Label((i - 1), 0, fieldLabel);  
                    sheet.addCell(label); 
                }
                n++;
            }

            for (Iterator<DomainObject> it = objectFactory.getObjectList().iterator(); it.hasNext();) {
                DomainObject d = it.next();
                if (d.getPersistentState() == PersistentState.CREATED) {
                    createdCount++;
                } else if (d.getPersistentState() == PersistentState.STORED) {
                    modifiedCount++;
                }
                
                for (int i = 1; i <= metaData.getFieldCount(); i++) {
                    Object value = readValue(d, metaData.getReadMethod(i));
                    if (Number.class.isAssignableFrom(metaData.getFieldClass(i))) {
                        jxl.write.Number num = new jxl.write.Number((i - 1),  n, converter.convert(value, Double.class));  
                        sheet.addCell(num); 
                    } else if (String.class.isAssignableFrom(metaData.getFieldClass(i))) {
                        jxl.write.Label str = new jxl.write.Label((i - 1), n, converter.convert(value, String.class));  
                        sheet.addCell(str); 
                    } else if (Date.class.isAssignableFrom(metaData.getFieldClass(i))) {
                        jxl.write.DateTime dt = new jxl.write.DateTime((i - 1), n, converter.convert(value, Date.class));  
                        sheet.addCell(dt); 
                    } else if (Boolean.class.isAssignableFrom(metaData.getFieldClass(i))) {
                        jxl.write.Boolean b = new jxl.write.Boolean((i - 1), n, converter.convert(value, Boolean.class));  
                        sheet.addCell(b); 
                    }
                }
                n++;
            }
        } catch (Exception e) {
            Logger.getLogger(XlsDataWriter.class.getName()).log(Level.SEVERE, null, e);
            setPersistenceError(e);
            success = false;
        }
        return success;
    }

    @Override
    public void reset() {
        removedCount = 0;
        createdCount = 0;
        modifiedCount = 0;
        removedObjects = null;
        createdObjects = null;
        modifiedObjects = null;
        persistenceError = null;
    }

    @Override
    public int getModifiedCount() {
        return this.modifiedCount;
    }

    @Override
    public int getCreatedCount() {
        return this.createdCount;
    }

    @Override
    public int getRemovedCount() {
        return this.removedCount;
    }

    @Override
    public AbstractMetaData getMetaData() {
        return this.metaData;
    }

    @Override
    public void setMetaData(MetaData metaData) {
        this.metaData = (XlsMetaData) metaData;
    }

    @Override
    public Throwable getPersistenceError() {
        return this.persistenceError;
    }

    @Override
    public boolean isExceptionOnEmptyObjectList() {
        return this.exceptionOnEmptyObjectList;
    }

    @Override
    public void setExceptionOnEmptyObjectList(boolean exceptionOnEmptyObjectList) {
        this.exceptionOnEmptyObjectList = exceptionOnEmptyObjectList;
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
        Object qoutedValuesOption = options.get(XlsDataWriter.Options.qoutedValues);
        if (qoutedValuesOption != null) {
            qoutedValues = (Boolean) qoutedValuesOption;
        }
        Object readOnlyOption = options.get(Options.readOnly);
        if (readOnlyOption != null) {
            readOnly = (Boolean) readOnlyOption;
        }
        Object headerOption = options.get(Options.header);
        if (headerOption != null) {
            header = (Boolean) headerOption;
        }
    }

    public boolean isHeader() {
        return header;
    }

    public void setHeader(boolean header) {
        this.header = header;
    }

    public void setPersistenceError(java.lang.Throwable persistenceError) {
        this.persistenceError = persistenceError;
    }

    protected Object readValue(DomainObject domainObject, Method method) throws DataAccessException {
        Object value = null;
        try {
            value = method.invoke(domainObject, EMPTY_READ_ARG);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw new DataAccessException(((InvocationTargetException) t).getTargetException());
            } else {
                throw new DataAccessException(t);
            }
        }
        return value;
    }
    
    @Override
    public Converter getConverter() {
        return converter;
    }

    @Override
    public void setConverter(Converter converter) {
        this.converter = converter;
    }
}

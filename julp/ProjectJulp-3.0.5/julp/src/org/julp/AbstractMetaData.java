package org.julp;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractMetaData<T> implements MetaData<T> {

    private static final long serialVersionUID = 1L;
    protected int[] precision = null;
    protected int[] scale = null;
    protected boolean[] caseSensitive = null;
    protected boolean[] currency = null;
    protected boolean[] definitelyWritable = null;
    protected boolean[] readOnly = null;
    protected boolean[] searchable = null;
    protected boolean[] signed = null;
    protected boolean[] writable = null;
    protected int[] nullable = null;
    // java.lang.reflect.Method is not serializeable, so after sending this object
    // to another JavaVM writeMethod and readMethod will become null    
    protected transient Method[] readMethod = null;
    protected transient Method[] writeMethod = null;
    protected String[] fieldClassName = null;
    protected Class<?>[] fieldClass = null;
    protected Class<T> domainClass = null; // DomainObject
    protected String[] fieldName = null;
    protected String[] fieldLabel = null;
    protected int fieldCount = 0;
    protected Map<String, String> mapping;
    // Throw Exception or ignore if DomainObject has less fields than mappings  
    protected boolean throwMissingFieldException = false;
    protected static final transient Logger logger = Logger.getLogger(AbstractMetaData.class.getName());

    public AbstractMetaData() {
    }

    @Override
    public int getScale(int fieldIndex) throws DataAccessException {
        return this.scale[fieldIndex - 1];
    }

    @Override
    public void setPrecision(int fieldIndex, int precision) throws DataAccessException {
        this.precision[fieldIndex - 1] = precision;
    }

    @Override
    public void setScale(int fieldIndex, int scale) throws DataAccessException {
        this.scale[fieldIndex - 1] = scale;
    }

    @Override
    public int getPrecision(int fieldIndex) throws DataAccessException {
        return this.precision[fieldIndex - 1];
    }

    @Override
    public boolean isCaseSensitive(int fieldIndex) throws DataAccessException {
        return this.caseSensitive[fieldIndex - 1];
    }

    @Override
    public boolean isCurrency(int fieldIndex) throws DataAccessException {
        return this.currency[fieldIndex - 1];
    }

    @Override
    public boolean isDefinitelyWritable(int fieldIndex) throws DataAccessException {
        return this.definitelyWritable[fieldIndex - 1];
    }

    @Override
    public int isNullable(int fieldIndex) throws DataAccessException {
        return this.nullable[fieldIndex - 1];
    }

    @Override
    public boolean isReadOnly(int fieldIndex) throws DataAccessException {
        return this.readOnly[fieldIndex - 1];
    }

    @Override
    public boolean isSearchable(int fieldIndex) throws DataAccessException {
        return this.searchable[fieldIndex - 1];
    }

    @Override
    public boolean isSigned(int fieldIndex) throws DataAccessException {
        return this.signed[fieldIndex - 1];
    }

    @Override
    public boolean isWritable(int fieldIndex) throws DataAccessException {
        return this.writable[fieldIndex - 1];
    }

    @Override
    public void setCaseSensitive(int fieldIndex, boolean property) throws DataAccessException {
        this.caseSensitive[fieldIndex - 1] = property;
    }

    @Override
    public void setCurrency(int fieldIndex, boolean property) throws DataAccessException {
        this.currency[fieldIndex - 1] = property;
    }

    @Override
    public void setNullable(int fieldIndex, int property) throws DataAccessException {
        this.nullable[fieldIndex - 1] = property;
    }

    @Override
    public void setSearchable(int fieldIndex, boolean property) throws DataAccessException {
        this.searchable[fieldIndex - 1] = property;
    }

    @Override
    public void setSigned(int fieldIndex, boolean property) throws DataAccessException {
        this.signed[fieldIndex - 1] = property;
    }

    @Override
    public void setWritable(int fieldIndex, boolean writable) {
        this.writable[fieldIndex - 1] = writable;
        this.readOnly[fieldIndex - 1] = !writable;
    }

    @Override
    public void setDefinitelyWritable(int fieldIndex, boolean definitelyWritable) {
        this.definitelyWritable[fieldIndex - 1] = definitelyWritable;
    }

    @Override
    public void setReadOnly(int fieldIndex, boolean readOnly) throws DataAccessException {
        this.readOnly[fieldIndex - 1] = readOnly;
        this.writable[fieldIndex - 1] = !readOnly;
    }

    @Override
    public String getFieldName(int fieldIndex) throws DataAccessException {
        return this.fieldName[fieldIndex - 1];
    }

    @Override
    public void setFieldName(int fieldIndex, String fieldName) throws DataAccessException {
        this.fieldName[fieldIndex - 1] = fieldName;
    }

    @Override
    public String getFieldLabel(int fieldIndex) throws DataAccessException {
        return this.fieldLabel[fieldIndex - 1];
    }

    @Override
    public void setFieldLabel(int fieldIndex, String fieldLabel) throws DataAccessException {
        this.fieldLabel[fieldIndex - 1] = fieldLabel;
    }

    @Override
    public Method getWriteMethod(int fieldIndex) throws DataAccessException {
        if (this.writeMethod == null || this.writeMethod[fieldIndex - 1] == null) {
            populateWriteMethod(fieldIndex, fieldName[fieldIndex - 1]);
        }
        return this.writeMethod[fieldIndex - 1];
    }

    @Override
    public void setWriteMethod(int fieldIndex, Method writeMethod) {
        if (this.writeMethod == null) {
            this.writeMethod = new Method[fieldCount];
        }
        this.writeMethod[fieldIndex - 1] = writeMethod;
    }

    @Override
    public Method getReadMethod(int fieldIndex) throws DataAccessException {
        if (this.readMethod == null || this.readMethod[fieldIndex - 1] == null) {
            populateReadMethod(fieldIndex, fieldName[fieldIndex - 1]);
        }
        return this.readMethod[fieldIndex - 1];
    }

    @Override
    public void setReadMethod(int fieldIndex, Method readMethod) {
        if (this.readMethod == null) {
            this.readMethod = new Method[fieldCount];
        }
        this.readMethod[fieldIndex - 1] = readMethod;
    }

    @Override
    public String getFieldClassName(int fieldIndex) throws DataAccessException {
        return this.fieldClassName[fieldIndex - 1];
    }

    @Override
    public void setFieldClassName(int fieldIndex, String fieldClassName) throws DataAccessException {
        this.fieldClassName[fieldIndex - 1] = fieldClassName;
    }

    @Override
    public java.lang.Class<?> getFieldClass(int fieldIndex) {
        return this.fieldClass[fieldIndex - 1];
    }

    @Override
    public void setFieldClass(int fieldIndex, Class<?> fieldClass) {
        this.fieldClass[fieldIndex - 1] = fieldClass;
    }

    protected void populateReadMethod(int fieldIndex, String fieldName) throws DataAccessException {
        try {
            Method mRead = (new PropertyDescriptor(fieldName, this.domainClass)).getReadMethod();
            this.setReadMethod(fieldIndex, mRead);
        } catch (IntrospectionException e) {
            if (throwMissingFieldException) {
                throw new DataAccessException(e);
            }
        }
    }

    protected void populateWriteMethod(int fieldIndex, String fieldName) throws DataAccessException {
        try {
            Method mWrite = (new PropertyDescriptor(fieldName, this.domainClass)).getWriteMethod();
            this.setWriteMethod(fieldIndex, mWrite);
            Class<?>[] paramTypes = mWrite.getParameterTypes();
            Class<?> paramClass = paramTypes[0];
            this.setFieldClass(fieldIndex, paramClass);
            this.setFieldClassName(fieldIndex, paramClass.getName());
        } catch (IntrospectionException e) {
            if (throwMissingFieldException) {
                throw new DataAccessException(e);
            }
        }
    }

    @Override
    public boolean isThrowMissingFieldException() {
        return throwMissingFieldException;
    }

    @Override
    public void setThrowMissingFieldException(boolean throwMissingFieldException) {
        this.throwMissingFieldException = throwMissingFieldException;
    }

    @Override
    public Class<T> getDomainClass() {
        return this.domainClass;
    }

    @Override
    public int getFieldCount() {
        return fieldCount;
    }

    @Override
    public void setFieldCount(int fieldCount) {
        this.fieldCount = fieldCount;
    }

    @Override
    public String toLabel(String fieldName) {
        String label = ((String) fieldName).toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean toUpper = false;
        for (int i = 0; i < label.length(); i++) {
            char c = label.charAt(i);
            if (c != '_') {
                if (i == 0) {
                    toUpper = true;
                }
                if (toUpper) {
                    c = Character.toUpperCase(c);
                    //} else {
                }
                sb.append(c);
                toUpper = false;
            } else {
                if (i > 0) {
                    sb.append(' ');
                }
                toUpper = true;
            }
        }
        return sb.toString();
    }

    @Override
    public int getFieldIndexByFieldName(String searchedFieldName) {
        if (searchedFieldName == null) {
            logger.warning("Parameter 'searchedFieldName' is null. Check mappings");
            return -1;
        }
        for (int i = 0; i < this.fieldName.length; i++) {
            if (fieldName[i] == null) {
                return -1;
            }
            if (searchedFieldName.equalsIgnoreCase(fieldName[i])) {
                return i + 1;
            }
        }
        logger.warning("Field index for " + searchedFieldName + " is not found. Check mappings");
        return -1;
    }

    @Override
    public Map<String, String> getMapping() {
        return mapping;
    }
}

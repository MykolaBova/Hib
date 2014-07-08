package org.julp;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Map;

public interface MetaData<T> extends Serializable {

    Class<T> getDomainClass();

    Class<?> getFieldClass(int fieldIndex);

    String getFieldClassName(int fieldIndex) throws DataAccessException;

    int getFieldCount();

    int getFieldIndexByFieldName(String searchedFieldName);

    String getFieldLabel(int fieldIndex) throws DataAccessException;

    String getFieldName(int fieldIndex) throws DataAccessException;

    Map<String, String> getMapping();

    /**
     * Get the designated field's number of decimal digits.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return precision
     * @exception DataAccessException if an error occurs
     *
     */
    int getPrecision(int fieldIndex) throws DataAccessException;

    Method getReadMethod(int fieldIndex) throws DataAccessException;

    /**
     * Gets the designated field's number of digits to right of the decimal point.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return scale
     * @exception DataAccessException if an error occurs
     *
     */
    int getScale(int fieldIndex) throws DataAccessException;

    Method getWriteMethod(int fieldIndex) throws DataAccessException;

    /** Indicates whether a field's case matters.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isCaseSensitive(int fieldIndex) throws DataAccessException;

    /** Indicates whether the designated field is a cash value.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isCurrency(int fieldIndex) throws DataAccessException;

    /** Indicates whether a write on the designated field will definitely succeed.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isDefinitelyWritable(int fieldIndex) throws DataAccessException;

    /** Indicates the nullability of values in the designated field.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return the nullability status of the given field; one of <code>fieldNoNulls</code>,
     *          <code>fieldNullable</code> or <code>fieldNullableUnknown</code>
     * @exception DataAccessException if an error occurs
     *
     */
    int isNullable(int fieldIndex) throws DataAccessException;

    /** Indicates whether the designated field is definitely not writable.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isReadOnly(int fieldIndex) throws DataAccessException;

    /** Indicates whether the designated field can be used in a where clause.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isSearchable(int fieldIndex) throws DataAccessException;

    /** Indicates whether values in the designated field are signed numbers.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isSigned(int fieldIndex) throws DataAccessException;

    boolean isThrowMissingFieldException();

    /** Indicates whether it is possible for a write on the designated field to succeed.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if an error occurs
     *
     */
    boolean isWritable(int fieldIndex) throws DataAccessException;

    void populate(Map<?, ?> mapping, Class<?> domainClass) throws DataAccessException;

    /** Sets whether the designated field is case sensitive.
     * The default is <code>false</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param property <code>true</code> if the field is case sensitive;
     *                 <code>false</code> if it is not
     *
     * @exception DataAccessException if an error occurs
     *
     */
    void setCaseSensitive(int fieldIndex, boolean property) throws DataAccessException;

    /** Sets whether the designated field is a cash value.
     * The default is <code>false</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param property <code>true</code> if the field is a cash value;
     *                 <code>false</code> if it is not
     *
     * @exception DataAccessException if an error occurs
     *
     */
    void setCurrency(int fieldIndex, boolean property) throws DataAccessException;

    void setDefinitelyWritable(int fieldIndex, boolean definitelyWritable);

    void setFieldClass(int fieldIndex, Class<?> fieldClass);

    void setFieldClassName(int fieldIndex, String fieldClassName) throws DataAccessException;

    void setFieldCount(int fieldCount);

    void setFieldLabel(int fieldIndex, String fieldLabel) throws DataAccessException;

    void setFieldName(int fieldIndex, String fieldName) throws DataAccessException;

    /** Sets whether the designated field's value can be set to
     * <code>NULL</code>.
     * The default is <code>ResultSetMetaData.fieldNullableUnknown</code>
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param property one of the following constants:
     *                 <code>ResultSetMetaData.fieldNoNulls</code>,
     *                 <code>ResultSetMetaData.fieldNullable</code>, or
     *                 <code>ResultSetMetaData.fieldNullableUnknown</code>
     *
     * @exception DataAccessException if an error occurs
     *
     */
    void setNullable(int fieldIndex, int property) throws DataAccessException;

    /**
     * Sets the designated field's number of decimal digits to the given
     * <code>int</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param precision the total number of decimal digits
     * @exception DataAccessException if an error occurs
     *
     */
    void setPrecision(int fieldIndex, int precision) throws DataAccessException;

    void setReadMethod(int fieldIndex, Method readMethod);

    void setReadOnly(int fieldIndex, boolean readOnly) throws DataAccessException;

    /**
     * Sets the designated field's number of digits to the right of the decimal point to the given
     * <code>int</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param scale the number of digits to right of decimal point
     * @exception DataAccessException if an error occurs
     *
     */
    void setScale(int fieldIndex, int scale) throws DataAccessException;

    /** Sets whether the designated field can be used in a where clause.
     * The default is <code>false</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param property <code>true</code> if the field can be used in a
     *                 <code>WHERE</code> clause; <code>false</code> if it cannot
     *
     * @exception DataAccessException if an error occurs
     *
     */
    void setSearchable(int fieldIndex, boolean property) throws DataAccessException;

    /** Sets whether the designated field is a signed number.
     * The default is <code>false</code>.
     *
     * @param fieldIndex the first field is 1, the second is 2, ...
     * @param property <code>true</code> if the field is a signed number;
     *                 <code>false</code> if it is not
     *
     * @exception DataAccessException if an error occurs
     *
     */
    void setSigned(int fieldIndex, boolean property) throws DataAccessException;

    void setThrowMissingFieldException(boolean throwMissingFieldException);

    void setWritable(int fieldIndex, boolean writable);

    void setWriteMethod(int fieldIndex, Method writeMethod);

    String toLabel(String fieldName);
    
}

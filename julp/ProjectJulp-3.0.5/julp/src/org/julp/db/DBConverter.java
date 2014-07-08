package org.julp.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.julp.AbstractConverter;
import org.julp.DataAccessException;
import org.julp.Wrapper;

public class DBConverter extends AbstractConverter {

    private static final long serialVersionUID = 7000388748015599628L;
    protected transient ResultSet resultSet;

    /**
     * Convert values from ResultSet to required datatypes
     */
    public DBConverter() {
    }

    public DBConverter(Wrapper data) {
        this.resultSet = (ResultSet) data.unwrap(ResultSet.class);
    }

    @Override
    public Object[] convert(String fieldClassName, String sourceName) throws DataAccessException {
        Object value = null;
        Object[] convertedValue = null;
        // So far there is support only for listed DataTypes. Feel free to add more... 
        try {
            if (fieldClassName == null) { //???
                convertedValue = new Object[1];
                value = resultSet.getObject(sourceName);
                convertedValue[0] = value;
            } else if (fieldClassName.equals("java.lang.String")) {
                convertedValue = new String[1];
                value = resultSet.getString(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (String) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Integer")) {
                convertedValue = new Integer[1];
                value = Integer.valueOf(resultSet.getInt(sourceName));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Integer) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Long")) {
                convertedValue = new Long[1];
                value = Long.valueOf(resultSet.getLong(sourceName));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Long) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Double")) {
                convertedValue = new Double[1];
                value = Double.valueOf(resultSet.getDouble(sourceName));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Double) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Float")) {
                convertedValue = new Float[1];
                value = Float.valueOf(resultSet.getFloat(sourceName));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Float) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Short")) {
                convertedValue = new Short[1];
                value = Short.valueOf(resultSet.getShort(sourceName));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Short) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Timestamp")) {
                convertedValue = new java.sql.Timestamp[1];
                value = (java.sql.Timestamp) resultSet.getTimestamp(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Timestamp) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Time")) {
                convertedValue = new java.sql.Time[1];
                value = (java.sql.Time) resultSet.getTime(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Time) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Date")) {
                convertedValue = new java.sql.Date[1];
                value = (java.sql.Date) resultSet.getDate(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Date) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.util.Date")) {
                convertedValue = new java.util.Date[1];
                value = (java.util.Date) resultSet.getTimestamp(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.util.Date) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.math.BigDecimal")) {
                convertedValue = new java.math.BigDecimal[1];
                value = resultSet.getBigDecimal(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.math.BigDecimal) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.math.BigInteger")) {
                convertedValue = new java.math.BigInteger[1];
                long longValue = 0;
                longValue = resultSet.getLong(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.math.BigInteger) null;
                } else {
                    value = java.math.BigInteger.valueOf(longValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("boolean")) {
                convertedValue = new Boolean[1];
                boolean booleanValue = false;
                booleanValue = resultSet.getBoolean(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Boolean.FALSE;
                } else {
                    value = Boolean.valueOf(booleanValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("byte")) {
                convertedValue = new Byte[1];
                byte byteValue = 0;
                byteValue = resultSet.getByte(sourceName);
                if (resultSet.wasNull()) {
                    byte emptyByte = 0;
                    convertedValue[0] = Byte.valueOf(emptyByte);
                } else {
                    value = Byte.valueOf(byteValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("char")) {
                convertedValue = new Character[1];
                String stringValue = null;
                stringValue = resultSet.getString(sourceName);
                if (resultSet.wasNull()) {
                    char emptyChar = '\u0000';
                    convertedValue[0] = Character.valueOf(emptyChar);
                } else {
                    value = Character.valueOf(stringValue.charAt(0));
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("double")) {
                convertedValue = new Double[1];
                double doubleValue = 0.0;
                doubleValue = resultSet.getDouble(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Double.valueOf(0.0);
                } else {
                    value = Double.valueOf(doubleValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("float")) {
                convertedValue = new Float[1];
                float floatValue = 0;
                floatValue = resultSet.getFloat(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Float.valueOf(0.0f);
                } else {
                    value = new Float(floatValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("int")) {
                convertedValue = new Integer[1];
                int intValue = 0;
                intValue = resultSet.getInt(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Integer.valueOf(0);
                } else {
                    value = Integer.valueOf(intValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("long")) {
                convertedValue = new Long[1];
                long longValue = 0;
                longValue = resultSet.getLong(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Long.valueOf(0);
                } else {
                    value = Long.valueOf(longValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("short")) {
                convertedValue = new Short[1];
                short shortValue = 0;
                shortValue = resultSet.getShort(sourceName);
                if (resultSet.wasNull()) {
                    short s = 0;
                    convertedValue[0] = Short.valueOf(s);
                } else {
                    value = Short.valueOf(shortValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Boolean")) {
                convertedValue = new Boolean[1];
                boolean booleanValue = false;
                booleanValue = resultSet.getBoolean(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Boolean) null;
                } else {
                    value = Boolean.valueOf(booleanValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Character")) {
                convertedValue = new Character[1];
                String stringValue = null;
                stringValue = resultSet.getString(sourceName);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Character) null;
                } else {
                    value = Character.valueOf(stringValue.charAt(0));
                    convertedValue[0] = value;
                }
            } else {
                convertedValue = new Object[1];
                value = resultSet.getObject(sourceName);
                convertedValue[0] = value;
            }
        } catch (SQLException sqle) {
            throw new DataAccessException(sqle);
        }
        return convertedValue;
    }

    @Override
    public Object[] convert(String fieldClassName, int sourceIndex) throws DataAccessException {
        Object value = null;
        Object[] convertedValue = null;
        // So far there is support only for listed DataTypes. Feel free to add more...
        try {
            if (fieldClassName == null) { //???
                convertedValue = new Object[1];
                value = resultSet.getObject(sourceIndex);
                convertedValue[0] = value;
            } else if (fieldClassName.equals("java.lang.String")) {
                convertedValue = new String[1];
                value = resultSet.getString(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (String) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Integer")) {
                convertedValue = new Integer[1];
                value = Integer.valueOf(resultSet.getInt(sourceIndex));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Integer) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Long")) {
                convertedValue = new Long[1];
                value = Long.valueOf(resultSet.getLong(sourceIndex));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Long) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Double")) {
                convertedValue = new Double[1];
                value = Double.valueOf(resultSet.getDouble(sourceIndex));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Double) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Float")) {
                convertedValue = new Float[1];
                value = Float.valueOf(resultSet.getFloat(sourceIndex));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Float) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Short")) {
                convertedValue = new Short[1];
                value = Short.valueOf(resultSet.getShort(sourceIndex));
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Short) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Timestamp")) {
                convertedValue = new java.sql.Timestamp[1];
                value = (java.sql.Timestamp) resultSet.getTimestamp(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Timestamp) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Time")) {
                convertedValue = new java.sql.Time[1];
                value = (java.sql.Time) resultSet.getTime(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Time) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.sql.Date")) {
                convertedValue = new java.sql.Date[1];
                value = (java.sql.Date) resultSet.getDate(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.sql.Date) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.util.Date")) {
                convertedValue = new java.util.Date[1];
                value = (java.util.Date) resultSet.getTimestamp(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.util.Date) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.math.BigDecimal")) {
                convertedValue = new java.math.BigDecimal[1];
                value = resultSet.getBigDecimal(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.math.BigDecimal) null;
                } else {
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.math.BigInteger")) {
                convertedValue = new java.math.BigInteger[1];
                long longValue = 0;
                longValue = resultSet.getLong(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (java.math.BigInteger) null;
                } else {
                    value = java.math.BigInteger.valueOf(longValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("boolean")) {
                convertedValue = new Boolean[1];
                boolean booleanValue = false;
                booleanValue = resultSet.getBoolean(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Boolean.FALSE;
                } else {
                    value = Boolean.valueOf(booleanValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("byte")) {
                convertedValue = new Byte[1];
                byte byteValue = 0;
                byteValue = resultSet.getByte(sourceIndex);
                if (resultSet.wasNull()) {
                    byte emptyByte = 0;
                    convertedValue[0] = Byte.valueOf(emptyByte);
                } else {
                    value = Byte.valueOf(byteValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("char")) {
                convertedValue = new Character[1];
                String stringValue = null;
                stringValue = resultSet.getString(sourceIndex);
                if (resultSet.wasNull()) {
                    char emptyChar = '\u0000';
                    convertedValue[0] = Character.valueOf(emptyChar);
                } else {
                    value = Character.valueOf(stringValue.charAt(0));
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("double")) {
                convertedValue = new Double[1];
                double doubleValue = 0.0;
                doubleValue = resultSet.getDouble(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Double.valueOf(0.0);
                } else {
                    value = Double.valueOf(doubleValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("float")) {
                convertedValue = new Float[1];
                float floatValue = 0;
                floatValue = resultSet.getFloat(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Float.valueOf(0.0f);
                } else {
                    value = new Float(floatValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("int")) {
                convertedValue = new Integer[1];
                int intValue = 0;
                intValue = resultSet.getInt(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Integer.valueOf(0);
                } else {
                    value = Integer.valueOf(intValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("long")) {
                convertedValue = new Long[1];
                long longValue = 0;
                longValue = resultSet.getLong(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = Long.valueOf(0);
                } else {
                    value = Long.valueOf(longValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("short")) {
                convertedValue = new Short[1];
                short shortValue = 0;
                shortValue = resultSet.getShort(sourceIndex);
                if (resultSet.wasNull()) {
                    short s = 0;
                    convertedValue[0] = Short.valueOf(s);
                } else {
                    value = Short.valueOf(shortValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Boolean")) {
                convertedValue = new Boolean[1];
                boolean booleanValue = false;
                booleanValue = resultSet.getBoolean(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Boolean) null;
                } else {
                    value = Boolean.valueOf(booleanValue);
                    convertedValue[0] = value;
                }
            } else if (fieldClassName.equals("java.lang.Character")) {
                convertedValue = new Character[1];
                String stringValue = null;
                stringValue = resultSet.getString(sourceIndex);
                if (resultSet.wasNull()) {
                    convertedValue[0] = (Character) null;
                } else {
                    value = Character.valueOf(stringValue.charAt(0));
                    convertedValue[0] = value;
                }
            } else {
                convertedValue = new Object[1];
                value = resultSet.getObject(sourceIndex);
                convertedValue[0] = value;
            }
        } catch (SQLException sqle) {
            throw new DataAccessException(sqle);
        }
        return convertedValue;
    }

    @Override
    public void setData(Wrapper data) {
        resultSet = (ResultSet) data.unwrap(ResultSet.class);
    }
}

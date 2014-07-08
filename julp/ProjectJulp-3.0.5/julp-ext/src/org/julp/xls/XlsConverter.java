package org.julp.xls;

import java.text.SimpleDateFormat;
import jxl.Cell;
import org.julp.AbstractConverter;
import org.julp.Converter;
import org.julp.DataAccessException;
import org.julp.Wrapper;

public class XlsConverter extends AbstractConverter implements Converter {

    protected Cell[] cells;

    public XlsConverter() {
    }

    @Override
    public Object[] convert(String fieldClassName, String sourceName) throws DataAccessException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Object[] convert(String fieldClassName, int sourceIndex) throws DataAccessException {
        int idx = sourceIndex - 1;
        String stringValue = cells[idx].getContents();
        stringValue = stringValue.trim();
        if (stringValue.startsWith("\"") && stringValue.endsWith("\"")) {
            stringValue = stringValue.substring(1, stringValue.length() - 1);
        }
        Object[] convertedValue = null;
        // So far there is support only for listed DataTypes. Feel free to add more...
        try {
            if (fieldClassName.equals("java.lang.String")) {
                convertedValue = new String[1];
                if (stringValue == null) {
                    convertedValue[0] = (String) null;
                } else {
                    convertedValue[0] = stringValue;
                }
            } else if (fieldClassName.equals("java.lang.Integer")) {
                convertedValue = new Integer[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Integer) null;
                } else {
                    convertedValue[0] = Integer.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("java.lang.Long")) {
                convertedValue = new Long[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Long) null;
                } else {
                    convertedValue[0] = Long.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("java.lang.Double")) {
                convertedValue = new Double[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Double) null;
                } else {
                    convertedValue[0] = new Double(stringValue);
                }
            } else if (fieldClassName.equals("java.lang.Float")) {
                convertedValue = new Float[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Float) null;
                } else {
                    convertedValue[0] = Float.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("java.lang.Short")) {
                convertedValue = new Short[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Short) null;
                } else {
                    convertedValue[0] = Short.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("java.sql.Timestamp")) {
                convertedValue = new java.sql.Timestamp[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.sql.Timestamp) null;
                } else {
                    convertedValue[0] = new java.sql.Timestamp(new SimpleDateFormat(timestampFormat).parse(stringValue).getTime());
                }
            } else if (fieldClassName.equals("java.sql.Time")) {
                convertedValue = new java.sql.Time[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.sql.Time) null;
                } else {
                    convertedValue[0] = new java.sql.Time(new SimpleDateFormat(timeFormat).parse(stringValue).getTime());
                }
            } else if (fieldClassName.equals("java.sql.Date")) {
                convertedValue = new java.sql.Date[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.sql.Date) null;
                } else {
                    convertedValue[0] = new java.sql.Date(new SimpleDateFormat(dateFormat).parse(stringValue).getTime());
                }
            } else if (fieldClassName.equals("java.util.Date")) {
                convertedValue = new java.util.Date[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.util.Date) null;
                } else {
                    convertedValue[0] = new SimpleDateFormat(dateFormat).parse(stringValue);
                }
            } else if (fieldClassName.equals("java.math.BigDecimal")) {
                convertedValue = new java.math.BigDecimal[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.math.BigDecimal) null;
                } else {
                    convertedValue[0] = new java.math.BigDecimal(stringValue);
                }
            } else if (fieldClassName.equals("java.math.BigInteger")) {
                convertedValue = new java.math.BigInteger[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (java.math.BigInteger) null;
                } else {
                    convertedValue[0] = new java.math.BigInteger(stringValue);
                }
            } else if (fieldClassName.equals("boolean")) {
                convertedValue = new Boolean[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Boolean.FALSE;
                } else {
                    convertedValue[0] = Boolean.valueOf(toBoolean(stringValue));
                }
            } else if (fieldClassName.equals("byte")) {
                convertedValue = new Byte[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    byte emptyByte = 0;
                    convertedValue[0] = Byte.valueOf(emptyByte);
                } else {
                    convertedValue[0] = Byte.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("char")) {
                convertedValue = new Character[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    char emptyChar = '\u0000';
                    convertedValue[0] = Character.valueOf(emptyChar);
                } else {
                    convertedValue[0] = Character.valueOf(stringValue.charAt(0));
                }
            } else if (fieldClassName.equals("double")) {
                convertedValue = new Double[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Double.valueOf(0.0);
                } else {
                    convertedValue[0] = Double.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("float")) {
                convertedValue = new Float[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Float.valueOf(0.0f);
                } else {
                    convertedValue[0] = Float.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("int")) {
                convertedValue = new Integer[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Integer.valueOf(0);
                } else {
                    convertedValue[0] = Integer.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("long")) {
                convertedValue = new Long[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Long.valueOf(0);
                } else {
                    convertedValue[0] = Long.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("short")) {
                convertedValue = new Short[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    short s = 0;
                    convertedValue[0] = Short.valueOf(s);
                } else {
                    convertedValue[0] = Short.valueOf(stringValue);
                }
            } else if (fieldClassName.equals("java.lang.Boolean")) {
                convertedValue = new Boolean[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = (Boolean) null;
                } else {
                    convertedValue[0] = Boolean.valueOf(toBoolean(stringValue));
                }
            } else if (fieldClassName.equals("java.lang.Character")) {
                convertedValue = new Character[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    char emptyChar = (Character) null;
                    convertedValue[0] = Character.valueOf(emptyChar);
                } else {
                    convertedValue[0] = Character.valueOf(stringValue.charAt(0));
                }
            } else {
                convertedValue = new Object[1];
                convertedValue[0] = stringValue;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }

        return convertedValue;
    }

    @Override
    public void setData(Wrapper data) throws DataAccessException {
        cells = data.unwrap(Cell[].class);
    }
}

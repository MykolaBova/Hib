package org.julp.util.common;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataTypeConverter {

    // return datatype's default if there is a problem to convert
    protected boolean defaultOnError;
    // return datatype's default if parameter is null
    protected boolean defaultOnNull;
    protected String dateFormat = "yyyy-MM-dd HH:mm:ss";
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    protected String timeFormat = "HH:mm:ss";

    public DataTypeConverter() {
    }

    public Timestamp getTimestamp(Object value) {
        Timestamp ts = null;
        try {
            if (value instanceof Number) {
                ts = new Timestamp(((Number) value).longValue());
            } else if (value instanceof String) {
                ts = new Timestamp(new SimpleDateFormat(timestampFormat).parse((String) value).getTime());
            } else {
                ts = new Timestamp(new SimpleDateFormat(timestampFormat).parse(value.toString()).getTime());
            }
        } catch (Throwable th) {
            throw new IllegalArgumentException("getTimestamp(Object value): cannot convert " + value);
        }
        return ts;
    }

    public Time getTime(Object value) {
        Time t = null;
        try {
            if (value instanceof Number) {
                t = new Time(((Number) value).longValue());
            } else if (value instanceof String) {
                t = new Time(new SimpleDateFormat(timeFormat).parse((String) value).getTime());
            } else {
                t = new Time(new SimpleDateFormat(timeFormat).parse(value.toString()).getTime());
            }
        } catch (Throwable th) {
            throw new IllegalArgumentException("getTime(Object value): cannot convert " + value);
        }
        return t;
    }

    public Date getDate(Object value) {
        Date d = null;
        try {
            if (value instanceof Number) {
                d = new Date(((Number) value).longValue());
            } else if (value instanceof String) {
                d = new Date(new SimpleDateFormat(timeFormat).parse((String) value).getTime());
            } else {
                d = new Date(new SimpleDateFormat(timeFormat).parse(value.toString()).getTime());
            }
        } catch (Throwable th) {
            throw new IllegalArgumentException("getDate(Object value): cannot convert " + value);
        }
        return d;
    }

    public boolean getBoolean(Object value) {
        boolean convertedValue = false;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getBoolean(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            if (((Number) value).intValue() == 1) {
                convertedValue = true;
            } else if (((Number) value).intValue() == 0) {
                convertedValue = false;
            } else {
                throw new IllegalArgumentException("getBoolean(Object value): value to convert is invalid: " + value);
            }
        } else if (value instanceof String) {
            String temp = ((String) value).trim().toLowerCase();
            if (temp.equals("yes") || temp.equals("y") || temp.equals("true") || temp.equals("on")) {
                convertedValue = true;
            } else if (temp.equals("no") || temp.equals("n") || temp.equals("false") || temp.equals("off")) {
                convertedValue = false;
            } else {
                throw new IllegalArgumentException("getBoolean(Object value): value to convert is invalid: " + value);
            }
        }
        return convertedValue;
    }

    public int getInt(Object value) {
        int convertedValue = 0;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getInt(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Integer.parseInt((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getInt(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getInt(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public double getDouble(Object value) {
        double convertedValue = 0.0d;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getDouble(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).doubleValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Double.parseDouble((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getDouble(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getDouble(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public long getLong(Object value) {
        long convertedValue = 0L;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getLong(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Long.parseLong((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getLong(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getLong(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public float getFloat(Object value) {
        float convertedValue = 0.0f;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getFloat(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).floatValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Float.parseFloat((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getFloat(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getFloat(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public short getShort(Object value) {
        short convertedValue = 0;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getShort(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).shortValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Short.parseShort((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getShort(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getShort(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public byte getByte(Object value) {
        byte convertedValue = 0;
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getByte(Object value): value to convert is null");
            }
        }

        if (value instanceof Number) {
            convertedValue = ((Number) value).byteValue();
        } else if (value instanceof String) {
            try {
                convertedValue = Byte.parseByte((String) value);
            } catch (NumberFormatException nfe) {
                if (defaultOnError) {
                    return convertedValue;
                } else {
                    throw new IllegalArgumentException("getByte(Object value): cannot convert " + value);
                }
            }
        } else {
            if (!defaultOnError) {
                throw new NullPointerException("getByte(Object value): value to convert is invalid");
            }
        }
        return convertedValue;
    }

    public char getChar(Object value) {
        char convertedValue = '\u0000';
        if (value == null) {
            if (defaultOnNull) {
                return convertedValue;
            } else {
                throw new NullPointerException("getChar(Object value): value to convert is null");
            }
        }

        String stringValue = value.toString();
        if (!stringValue.equals("")) {
            convertedValue = stringValue.charAt(0);
        }
        return convertedValue;
    }

    public boolean isDefaultOnError() {
        return defaultOnError;
    }

    public void setDefaultOnError(boolean defaultOnError) {
        this.defaultOnError = defaultOnError;
    }

    public boolean isDefaultOnNull() {
        return defaultOnNull;
    }

    public void setDefaultOnNull(boolean defaultOnNull) {
        this.defaultOnNull = defaultOnNull;
    }

    public Object[] convert(Object value) {
        if (value == null) {
            return null;
        }
        return convert(value.toString(), value.getClass());
    }

    public Object[] convert(String stringValue, Class clazz) {
        return convert(stringValue, clazz.getName());
    }

    // To use with java.lang.reflect.Method.invoke(Object obj, Object[] args)
    public Object[] convert(String stringValue, String className) {
        Object[] convertedValue = null;
        try {
            if (className.equals("java.lang.String")) {
                convertedValue = new String[1];
                if (stringValue == null) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = stringValue;
                }
            } else if (className.equals("java.lang.Integer")) {
                convertedValue = new Integer[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = Integer.valueOf(stringValue);
                }
            } else if (className.equals("java.lang.Long")) {
                convertedValue = new Long[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = Long.valueOf(stringValue);
                }
            } else if (className.equals("java.lang.Double")) {
                convertedValue = new Double[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new Double(stringValue);
                }
            } else if (className.equals("java.lang.Float")) {
                convertedValue = new Float[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = Float.valueOf(stringValue);
                }
            } else if (className.equals("java.lang.Short")) {
                convertedValue = new Short[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = Short.valueOf(stringValue);
                }
            } else if (className.equals("java.sql.Timestamp")) {
                convertedValue = new java.sql.Timestamp[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new java.sql.Timestamp(new SimpleDateFormat(timestampFormat).parse(stringValue).getTime());
                }
            } else if (className.equals("java.sql.Time")) {
                convertedValue = new java.sql.Time[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new java.sql.Time(new SimpleDateFormat(timeFormat).parse(stringValue).getTime());
                }
            } else if (className.equals("java.sql.Date")) {
                convertedValue = new java.sql.Date[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new java.sql.Date(new SimpleDateFormat(dateFormat).parse(stringValue).getTime());
                }
            } else if (className.equals("java.util.Date")) {
                convertedValue = new java.util.Date[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new SimpleDateFormat(dateFormat).parse(stringValue);
                }
            } else if (className.equals("java.math.BigDecimal")) {
                convertedValue = new java.math.BigDecimal[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new java.math.BigDecimal(stringValue);
                }
            } else if (className.equals("java.math.BigInteger")) {
                convertedValue = new java.math.BigInteger[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = null;
                } else {
                    convertedValue[0] = new java.math.BigInteger(stringValue);
                }
            } else if (className.equals("boolean")) {
                convertedValue = new Boolean[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Boolean.FALSE;
                } else {
                    convertedValue[0] = Boolean.valueOf(stringValue);
                }
            } else if (className.equals("byte")) {
                convertedValue = new Byte[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    byte emptyByte = 0;
                    convertedValue[0] = Byte.valueOf(emptyByte);
                } else {
                    convertedValue[0] = Byte.valueOf(stringValue);
                }
            } else if (className.equals("char")) {
                convertedValue = new Character[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    char emptyChar = '\u0000';
                    convertedValue[0] = Character.valueOf(emptyChar);
                } else {
                    convertedValue[0] = Character.valueOf(stringValue.charAt(0));
                }
            } else if (className.equals("double")) {
                convertedValue = new Double[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Double.valueOf(0.0);
                } else {
                    convertedValue[0] = Double.valueOf(stringValue);
                }
            } else if (className.equals("float")) {
                convertedValue = new Float[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Float.valueOf(0.0f);
                } else {
                    convertedValue[0] = Float.valueOf(stringValue);
                }
            } else if (className.equals("int")) {
                convertedValue = new Integer[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Integer.valueOf(0);
                } else {
                    convertedValue[0] = Integer.valueOf(stringValue);
                }
            } else if (className.equals("long")) {
                convertedValue = new Long[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Long.valueOf(0);
                } else {
                    convertedValue[0] = Long.valueOf(stringValue);
                }
            } else if (className.equals("short")) {
                convertedValue = new Short[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    short s = 0;
                    convertedValue[0] = Short.valueOf(s);
                } else {
                    convertedValue[0] = Short.valueOf(stringValue);
                }
            } else if (className.equals("java.lang.Boolean")) {
                convertedValue = new Boolean[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    convertedValue[0] = Boolean.FALSE;
                } else {
                    convertedValue[0] = Boolean.valueOf(stringValue);
                }
            } else if (className.equals("java.lang.Character")) {
                convertedValue = new Character[1];
                if (stringValue == null || stringValue.trim().length() == 0) {
                    char emptyChar = '\u0000';
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

   
}

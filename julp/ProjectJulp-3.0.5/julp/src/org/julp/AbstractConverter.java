package org.julp;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class AbstractConverter implements Converter {

    private static final long serialVersionUID = 1L;
    protected String dateFormat = "yyyy-MM-dd HH:mm:ss";
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    protected String timeFormat = "HH:mm:ss";
    protected SimpleDateFormat dateFormatter;
    protected SimpleDateFormat timestampFormatter;
    protected SimpleDateFormat timeFormatter;

    @Override
    public String getDateFormat() {
        return dateFormat;
    }

    @Override
    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat;
    }

    @Override
    public String getTimestampFormat() {
        return timestampFormat;
    }

    @Override
    public void setTimestampFormat(String timestampFormat) {
        this.timestampFormat = timestampFormat;
    }

    @Override
    public String getTimeFormat() {
        return timeFormat;
    }

    @Override
    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    @Override
    public <I, O> O convert(I input, Class<O> outputClass) throws DataAccessException {
        try {
            if (input == null || outputClass == null) {
                return null;
            }
            if (java.util.Date.class.isAssignableFrom(outputClass)) {
                return convertTemporal(input, outputClass);
            }
            if (outputClass.isPrimitive()) {
                return convertPrimitive(input, outputClass);
            }
            return outputClass.getConstructor(String.class).newInstance(input.toString());
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O convertPrimitive(I input, Class<O> outputClass) throws DataAccessException {
        Object returnValue = null;
        try {
            String className = outputClass.toString();
            String value = input.toString();
            if (className.equals("int")) {
                returnValue = Integer.parseInt(value);
            } else if (className.equals("long")) {
                returnValue = Long.parseLong(value);
            } else if (className.equals("short")) {
                returnValue = Short.parseShort(value);
            } else if (className.equals("byte")) {
                returnValue = Byte.parseByte(value);
            } else if (className.equals("double")) {
                returnValue = Double.parseDouble(value);
            } else if (className.equals("float")) {
                returnValue = Float.parseFloat(value);
            } else if (className.equals("char")) {
                returnValue = Character.valueOf(value.charAt(0));
            } else if (className.equals("boolean")) {
                returnValue = Boolean.parseBoolean(value);
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return (O) returnValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I, O> O convertTemporal(I input, Class<O> outputClass) {
        try {
            if (java.sql.Timestamp.class.isAssignableFrom(outputClass)) {
                if (String.class.isAssignableFrom(input.getClass())) {
                    if (timestampFormatter == null) {
                        timestampFormatter = new SimpleDateFormat(timestampFormat);
                    }
                    return ((O) new Timestamp(timestampFormatter.parse((String) input).getTime()));
                } else if (Number.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Timestamp(((Number) input).longValue()));
                } else if (Date.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Timestamp(((Date) input).getTime()));
                }
            } else if (java.sql.Time.class.isAssignableFrom(outputClass)) {
                if (String.class.isAssignableFrom(input.getClass())) {
                    if (timeFormatter == null) {
                        timeFormatter = new SimpleDateFormat(timeFormat);
                    }
                    return ((O) new Time(timeFormatter.parse((String) input).getTime()));
                } else if (Number.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Time(((Number) input).longValue()));
                } else if (Date.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Time(((Date) input).getTime()));
                }
            } else if (java.sql.Date.class.isAssignableFrom(outputClass)) {
                if (String.class.isAssignableFrom(input.getClass())) {
                    if (dateFormatter == null) {
                        dateFormatter = new SimpleDateFormat(dateFormat);
                    }
                    return ((O) new java.sql.Date(dateFormatter.parse((String) input).getTime()));
                } else if (Number.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Date(((Number) input).longValue()));
                } else if (Date.class.isAssignableFrom(input.getClass())) {
                    return ((O) input);
                }
            } else if (java.util.Date.class.isAssignableFrom(outputClass)) {
                if (String.class.isAssignableFrom(input.getClass())) {
                    if (dateFormatter == null) {
                        dateFormatter = new SimpleDateFormat(dateFormat);
                    }
                    return ((O) new Date(dateFormatter.parse((String) input).getTime()));
                } else if (Number.class.isAssignableFrom(input.getClass())) {
                    return ((O) new Date(((Number) input).longValue()));
                } else if (Date.class.isAssignableFrom(input.getClass())) {
                    return ((O) input);
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
        return null;
    }

    protected boolean toBoolean(String value) {
        if (value == null) {
            return false;
        }
        String temp = value.trim();
        if (temp.equals("0")) {
            return false;
        } else if (temp.equalsIgnoreCase("false")) {
            return false;
        } else if (temp.equalsIgnoreCase("n")) {
            return false;
        } else if (temp.equalsIgnoreCase("n")) {
            return false;
        } else if (temp.equalsIgnoreCase("no")) {
            return false;
        } else if (temp.equalsIgnoreCase("off")) {
            return false;
        } else if (temp.equalsIgnoreCase("1")) {
            return true;
        } else if (temp.equalsIgnoreCase("y")) {
            return true;
        } else if (temp.equalsIgnoreCase("yes")) {
            return true;
        } else if (temp.equalsIgnoreCase("on")) {
            return true;
        } else if (temp.equalsIgnoreCase("true")) {
            return true;
        }
        return false;
    }
}

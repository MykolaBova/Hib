package org.julp.util.common;

import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Converter {

    protected String dateFormat = "yyyy-MM-dd HH:mm:ss";
    protected String timestampFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    protected String timeFormat = "HH:mm:ss";

    public  Converter() {
        
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

    public <I, O> O convert(I input, Class<O> outputClass) throws Exception {
        if (input == null && outputClass == null) {
            return null;
        }
        if (java.util.Date.class.isAssignableFrom(outputClass)) {
            return convertTemporal(input, outputClass);
        }
        if (outputClass.isPrimitive()) {
            return convertPrimitive(input, outputClass);
        }
        return outputClass.getConstructor(String.class).newInstance(input.toString());
    }

    @SuppressWarnings("unchecked")
    public <I, O> O convertPrimitive(I input, Class<O> outputClass) throws Exception {
        Object returnValue = null;
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
        return (O) returnValue;
    }

    @SuppressWarnings("unchecked")
    public  <I, O> O convertTemporal(I input, Class<O> outputClass) throws Exception {
        if (java.sql.Timestamp.class.isAssignableFrom(outputClass)) {
            if (String.class.isAssignableFrom(input.getClass())) {
                return ((O) new Timestamp(new SimpleDateFormat(timestampFormat).parse((String) input).getTime()));
            } else if (Number.class.isAssignableFrom(input.getClass())) {
                return ((O) new Timestamp(((Number) input).longValue()));
            } else if (Date.class.isAssignableFrom(input.getClass())) {
                return ((O) new Timestamp(((Date) input).getTime()));
            }
        } else if (java.sql.Time.class.isAssignableFrom(outputClass)) {
            if (String.class.isAssignableFrom(input.getClass())) {
                return ((O) new Time(new SimpleDateFormat(timeFormat).parse((String) input).getTime()));
            } else if (Number.class.isAssignableFrom(input.getClass())) {
                return ((O) new Time(((Number) input).longValue()));
            } else if (Date.class.isAssignableFrom(input.getClass())) {
                return ((O) new Time(((Date) input).getTime()));
            }
        } else if (java.util.Date.class.isAssignableFrom(outputClass)) {
            if (String.class.isAssignableFrom(input.getClass())) {
                return ((O) new Date(new SimpleDateFormat(dateFormat).parse((String) input).getTime()));
            } else if (Number.class.isAssignableFrom(input.getClass())) {
                return ((O) new Date(((Number) input).longValue()));
            } else if (Date.class.isAssignableFrom(input.getClass())) {
                return ((O) input);
            }
        }
        return null;
    }
}

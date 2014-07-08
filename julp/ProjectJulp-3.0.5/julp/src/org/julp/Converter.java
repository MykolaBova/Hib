package org.julp;

import java.io.Serializable;

 public interface Converter extends Serializable {
     Object[] convert(String fieldClassName, String sourceName) throws DataAccessException;
     Object[] convert(String fieldClassName, int sourceIndex) throws DataAccessException;
     void setData(Wrapper data) throws DataAccessException;        
     <I, O> O convert(I input, Class<O> outputClass) throws DataAccessException;
     <I, O> O convertPrimitive(I input, Class<O> outputClass) throws DataAccessException;    
     <I, O> O convertTemporal(I input, Class<O> outputClass) throws DataAccessException;
     String getDateFormat();
     void setDateFormat(String dateFormat);
     String getTimestampFormat() ;
     void setTimestampFormat(String timestampFormat);
     String getTimeFormat();
     void setTimeFormat(String timeFormat);
}

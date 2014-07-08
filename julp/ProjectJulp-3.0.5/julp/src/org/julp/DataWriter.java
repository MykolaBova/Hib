package org.julp;

import java.util.Map;

 public interface DataWriter<T> extends java.io.Serializable {

    public enum DataModificationSequence {
        DATA_MODIFICATION_SEQUENCE_DELETE, DATA_MODIFICATION_SEQUENCE_INSERT, DATA_MODIFICATION_SEQUENCE_UPDATE
    };

     boolean writeData(AbstractDomainObjectFactory<?> objectFactory);
     void reset();
     int getModifiedCount();
     int getCreatedCount();
     int getRemovedCount();
     MetaData<T> getMetaData();
     void setMetaData(MetaData<T> metaData);
     java.lang.Throwable getPersistenceError();
     boolean isExceptionOnEmptyObjectList() ;
     void setExceptionOnEmptyObjectList(boolean exceptionOnEmptyObjectList) ;
     void setOptions(Map<Enum<?>, Object> options);
     Map<Enum<?>, Object> getOptions();
     void setConverter(Converter converter);
     Converter getConverter();
}

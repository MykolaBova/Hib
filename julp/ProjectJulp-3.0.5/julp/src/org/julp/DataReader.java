package org.julp;

import java.util.List;
import java.util.Map;

 public interface DataReader<T> extends java.io.Serializable {

     void setConverter(Converter converter);
     Converter getConverter();
     List<DomainObject<T>> readData(Wrapper data, int offset, int limit);
     List<DomainObject<T>> readData(Wrapper data);
     MetaData<T> getMetaData();
     void setMetaData(MetaData<T> metaData);
     Class<T> getDomainClass();
     void setOptions(Map<Enum<?>, Object> options);
     Map<Enum<?>, Object> getOptions();
     void setInstantiator(Instantiator<T> instantiator);
     Instantiator<T> getInstantiator();
     boolean isLazyLoading();
     void setLazyLoading(boolean lazyLoading);
}

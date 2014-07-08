package org.julp;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Map;

 public interface DomainObject<T> extends java.io.Serializable {
     void setOriginalValues(DataHolder originalValues);
     DataHolder getOriginalValues();
     void setOriginalValue(String fieldName, Object value);
     Object getOriginalValue(String fieldName);
     PersistentState getPersistentState();
     void setPersistentState(PersistentState persistentState);
     int getObjectId();
     void setObjectId(int objectId);
     boolean store();
     boolean remove();
     boolean create();
     boolean load();
     boolean isLoaded();
     void setLoaded(boolean loaded);
     boolean isLoading();
     void setLoading(boolean loading);
     boolean isModified();
     void setModified(boolean modified);
     void setDisplayValue(String field, String displayValue);
     Object getDisplayValue(String field);
     boolean syncOriginal();
     void setDomainFactoryId(long domainFactoryId);
     long getDomainFactoryId();    
     void addPropertyChangeListener(PropertyChangeListener listener);
     void removePropertyChangeListener(PropertyChangeListener listener);
     PropertyChangeSupport getPropertyChangeSupport();
     void reset();
     Map<String, String> getDisplayValues() ;
     void setDisplayValues(Map<String, String> displayValues);
     boolean isDisplayInternalMethods();
     void setDisplayInternalMethods(boolean displayInternalMethods);
     List<String> getInternalMethods();
     void setInternalMethods(List<String> internalMethods);    
     T detach();
     void reattach(T instance);
}

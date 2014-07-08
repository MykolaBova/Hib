package org.julp.examples;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.julp.DataAccessException;
import org.julp.DataHolder;
import org.julp.DomainObject;
import org.julp.PersistentState;

/**
 * Normally to make your object able to persist you need to extend this object
 * If you can't or don't want to extend, just make your class
 * implement org.julp.DomainObject interface and copy and paste members
 * and methods from org.julp.AbstractDomainObject into your class.
 * You don't need to copy toString() method.
 * If you can't/don't want to extend then your classes will be enhanced by CGLIB/ASM 
 */
public class CustomerNoExtending<T> implements DomainObject<T> {

    private static final long serialVersionUID = 1043203579243398131L;
    protected T instance;
    protected DataHolder originalValues = null;
    protected int objectId = -1;
    protected PersistentState persistentState = PersistentState.UNDEFINED;
    protected boolean loaded = false;
    protected boolean loading = false;
    protected boolean modified = false;
    protected Map<String, String> displayValues; // fieldName -> DisplayValue
    protected static final Object[] EMPTY_READ_ARG = new Object[0];
    protected long domainFactoryId = -1;
    protected Class<?> originalClass;
    protected PropertyChangeSupport pcs;
    protected boolean displayInternalMethods;
    /** Methods to skip while building toString() */
    protected List<String> internalMethods = Arrays.asList(new String[] {
        "getOriginalValues",
        "getOriginalValue",
        "getPersistentState",
        "getObjectId",
        "isLoaded",
        "isLoading",
        "isModified",
        "getDisplayValue",
        "getDomainFactoryId",        
        "getPropertyChangeSupport",
        "getDisplayValues",
        "isDisplayInternalMethods",
        "getInternalMethods"        
    });

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object value = null;
        Method[] methods = getClass().getMethods();                
        
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.equals("getClass")) {
                continue;
            }
            if (!displayInternalMethods && internalMethods.contains(methodName)) {
                    continue;
            }
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && methods[i].getParameterTypes().length == 0) {
                try {
                    value = readValue(methods[i]);
                } catch (Throwable t) {
                    continue;
                }
                String fieldFirstChar = "";
                if (methodName.startsWith("is")) {
                    fieldFirstChar = methodName.substring(2, 3).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(3));
                } else if (methodName.startsWith("get")) {
                    fieldFirstChar = methodName.substring(3, 4).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(4));
                }
                sb.append("=");
                sb.append((value == null) ? "" : value);
                sb.append("&");
            }            
        }        
        int idx = sb.lastIndexOf("&");
        if (idx > -1) {
            sb.deleteCharAt(idx);
        }        
        return sb.toString();
    }

    @Override
    public void setOriginalValues(DataHolder originalValues) {
        this.originalValues = originalValues;
    }

    @Override
    public DataHolder getOriginalValues() {
        return this.originalValues;
    }

    @Override
    public void setOriginalValue(String fieldName, Object value) {
        if (originalValues == null) {
            throw new IllegalArgumentException("setOriginalValue: DataHolder not set");
        }
        originalValues.setFieldValue(fieldName, value);
    }

    @Override
    public Object getOriginalValue(String fieldName) {
        Object value = (originalValues == null) ? null : originalValues.getFieldValue(fieldName);
        return value;
    }

    @Override
    public PersistentState getPersistentState() {
        return persistentState;
    }

    @Override
    public void setPersistentState(PersistentState persistentState) {
        this.persistentState = persistentState;
    }

    /** Getter for property objectId.
     * @return Value of property objectId.
     *
     */
    @Override
    public int getObjectId() {
        return objectId;
    }

    /**
     *  Every time  DomainObjectFactory is loaded this object is assigned objectId
     *  which is valid only for the same DomainObjectFactory (see domainFactoryId)
     *  if DomainObject's objectId used with other DomainObjectFactory it gets new objectId
     *  when used with DomainObjectFactory.[setObject(...), load(), create(), store(), remove()]
     */
    @Override
    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    /**       
    Just a mark for data modification, actual modification done in DomainObjectFactory -> DataWriter
    normally this methods called from DomainObjectFactory.[create(DomainObject), store(DomainObject), remove(DomainObject)]
     */
    @Override
    public boolean store() {
        if (originalValues != null && originalValues.getFieldsCount() > 0) {
            setPersistentState(PersistentState.STORED);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove() {
        if (originalValues != null && originalValues.getFieldsCount() > 0) {
            setPersistentState(PersistentState.REMOVED);
            return true;
        }
        return false;
    }

    @Override
    public boolean create() {
        if (originalValues == null || originalValues.getFieldsCount() < 1) {
            setPersistentState(PersistentState.CREATED);
            return true;
        }
        return false;
    }

    /** Populate originalValues with values from DomainObject */
    @Override
    public boolean syncOriginal() {
        boolean sync = false;
        if (originalValues != null && originalValues.getFieldsCount() > 0) {
            try {
                if (getPersistentState() == PersistentState.REMOVED) {
                    setPersistentState(PersistentState.UNDEFINED);
                } else {
                    setPersistentState(PersistentState.ORIGINAL);
                }
                for (int fieldIndex = 1; fieldIndex <= originalValues.getFieldsCount(); fieldIndex++) {
                    String fieldName = originalValues.getFieldName(fieldIndex);
                    Object value = null;                    
                    PropertyDescriptor pd = new PropertyDescriptor(fieldName, getClass());
                    value = readValue(pd.getReadMethod());
                    originalValues.setFieldValue(fieldIndex, value);
                }
                sync = true;
                setModified(false);
            } catch (Throwable t) {
                throw new DataAccessException(t);
            }
        }
        return sync;
    }

    /** Populate DomainObject with values from originalValues */
    @Override
    public boolean load() {
        setLoading(true);
        if (originalValues != null && originalValues.getFieldsCount() > 0) {
            try {
                Object[] param = new Object[1];
                for (int fieldIndex = 1; fieldIndex <= originalValues.getFieldsCount(); fieldIndex++) {
                    param[0] = null;
                    String fieldName = originalValues.getFieldName(fieldIndex);
                    Object value = originalValues.getFieldValue(fieldIndex);
                    PropertyDescriptor pd = new PropertyDescriptor(fieldName, getClass());
                    param[0] = value;
                    writeValue(pd.getWriteMethod(), param);
                }
            } catch (Throwable t) {
                throw new DataAccessException(t);
            }
            setLoading(false);
            setLoaded(true);
            setModified(false);
            return true;
        }
        setLoading(false);
        return false;
    }

    /** Getter for property loaded.
     * @return Value of property loaded.
     *
     */
    @Override
    public boolean isLoaded() {
        return loaded;
    }

    /** Setter for property loaded.
     * @param loaded New value of property loaded.
     *
     */
    @Override
    public void setLoaded(boolean loaded) {
        this.setPersistentState(PersistentState.ORIGINAL);
        this.loaded = loaded;
    }

    /**
     * Indicator that this object in process of loading with values from database
     */
    @Override
    public boolean isLoading() {
        return this.loading;
    }

    @Override
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    /**
     * Indicator that this object is modified. You have to call it yourself unless your objects enhanced by CGLIB/ASM
     */
    @Override
    public boolean isModified() {
        return this.modified;
    }

    @Override
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     *  Display value for given field. 
     *  Example: field value is "123" but display must be "XYZ"
     */
    @Override
    public void setDisplayValue(String field, String displayValue) {
        if (displayValues == null) {
            displayValues = new HashMap<String, String>();
        }
        displayValues.put(field, displayValue);
    }

    @Override
    public Object getDisplayValue(String fieldName) {
        Object display;
        try {
            if (displayValues == null) {
                PropertyDescriptor pd = new PropertyDescriptor(fieldName, getClass());
                display = readValue(pd.getReadMethod());
            } else {
                display = displayValues.get(fieldName);
            }
        } catch (Throwable t) {
            throw new DataAccessException(t);
        }
        return display;
    }

    @Override
    public void setDomainFactoryId(long domainFactoryId) {
        this.domainFactoryId = domainFactoryId;
    }

    @Override
    public long getDomainFactoryId() {
        return this.domainFactoryId;
    }

    protected void writeValue(Method method, Object[] value) throws Throwable {
        try {
            method.invoke(this, value);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw ((InvocationTargetException) t).getTargetException();
            } else {
                throw t;
            }
        }
    }

    protected Object readValue(Method method) throws Throwable {
        Object value = null;
        try {
                value = method.invoke(this, EMPTY_READ_ARG);          
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw ((InvocationTargetException) t).getTargetException();
            } else {
                throw t;
            }
        }
        return value;
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        if (pcs == null) {
            pcs = new PropertyChangeSupport(this);
        }
        pcs.addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    @Override
    public synchronized PropertyChangeSupport getPropertyChangeSupport() {
        return pcs;
    }

    /** Make an object "brand-new", however live values intact */
    @Override
    public void reset() {
        originalValues = null;
        objectId = -1;
        domainFactoryId = -1;
        persistentState = PersistentState.UNDEFINED;
        loaded = false;
        loading = false;
        modified = false;
        pcs = null;
    }
    
    @Override
    public Map<String, String> getDisplayValues() {
        return displayValues;
    }

    @Override
    public void setDisplayValues(Map<String, String> displayValues) {
        this.displayValues = displayValues;
    }
    
    @Override
    public boolean isDisplayInternalMethods() {
        return displayInternalMethods;
    }

    @Override
    public void setDisplayInternalMethods(boolean displayInternalMethods) {
        this.displayInternalMethods = displayInternalMethods;
    }

    @Override
    public List<String> getInternalMethods() {
        return internalMethods;
    }

    @Override
    public void setInternalMethods(List<String> internalMethods) {
        this.internalMethods = internalMethods;
    }

    @Override
    public T detach() {
        return instance;
    }       
    
// ==========================================
	
    protected java.lang.Integer customerId;
    protected java.lang.String firstName;
    protected java.lang.String lastName;
    protected java.lang.String street;
    protected java.lang.String city;   
    
    protected java.lang.String phone;
    protected Double total;
    protected Integer invoiceId;
    protected Integer item;
    protected Timestamp modifiedOn;
    
    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (customerId == null) {
                throw new IllegalArgumentException("Missing field: customerId");
            }
            if (!customerId.equals(this.customerId)) {
                this.modified = true;
            }
        }
        this.customerId = customerId;
    }

    public java.lang.String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(java.lang.String firstName) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (firstName == null) {
                throw new IllegalArgumentException("Missing field: firstName");
            }
            if (!firstName.equals(this.firstName)) {
                this.modified = true;
            }
        }
        this.firstName = firstName;
    }

    public java.lang.String getLastName() {
        return this.lastName;
    }

    public void setLastName(java.lang.String lastName) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (lastName == null) {
                throw new IllegalArgumentException("Missing field: lastName");
            }
            if (!lastName.equals(this.lastName)) {
                this.modified = true;
            }
        }
        this.lastName = lastName;
    }

    public java.lang.String getStreet() {
        return this.street;
    }

    public void setStreet(java.lang.String street) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (street == null) {
                throw new IllegalArgumentException("Missing field: street");
            }
            if (!street.equals(this.street)) {
                this.modified = true;
            }
        }
        this.street = street;
    }

    public java.lang.String getCity() {
        return this.city;
    }

    public void setCity(java.lang.String city) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (city == null) {
                throw new IllegalArgumentException("Missing field: city");
            }
            if (!city.equals(this.city)) {
                this.modified = true;
            }
        }
        this.city = city;
    }

    public java.lang.String getPhone() {
        return this.phone;
    }

    public void setPhone(java.lang.String phone) {
        if (!isLoading()) {
            /* DBColumn nullability: Nullable - modify as needed */
            if (phone == null && this.phone != null) {
                this.modified = true;
            } else if (phone != null && this.phone == null) {
                this.modified = true;
            } else if (!phone.equals(this.phone)) {
                this.modified = true;
            }
        }
        this.phone = phone;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
    
    
    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }
    
    public Timestamp getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Timestamp modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public void reattach(T t) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

package org.julp;

import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class AbstractDomainObjectFactory<T> implements Serializable {

    /*================================== members start =======================*/
    private static final long serialVersionUID = 1195039185383139622L;
    protected MetaData<T> metaData = null;
    /**
     * Mapping of DB Table columns to object fields
     */
    protected Map<String, String> mapping = null;
    /**
     * Object to persist
     */
    protected Class<T> domainClass = null;
    /**
     * See AbstractDomainObject setObjectId()
     */
    protected int objectId = -1;
    /**
     * Collection of Objects (data)
     */
    protected List<DomainObject<T>> objectList = new ArrayList<>();
    /**
     * Collection of Objects to delete from DB
     */
    protected List<DomainObject<T>> removedObjects = new ArrayList<>();
    /**
     * Useful to send only modified objects to another tier
     */
    protected boolean discardUnmodifiedObjects;
    /**
     * Disable data modifications
     */
    protected boolean readOnly = false;
    /**
     * This object generates data modification
     */
    protected DataWriter<T> dataWriter = null;
    /**
     * used as argument in method.invoke(obj, EMPTY_ARG)
     */
    protected final static Object[] EMPTY_ARG = new Object[0];
    /**
     * If lazyLoding == true than set only original values vs. original and
     * current
     */
    protected boolean lazyLoading = false;
    /**
     * control if load() append objects to objectList )
     */
    protected boolean append = false;
    /**
     * Used to determine if created/deleted/updated DomainObject belongs to this
     * factory
     */
    protected long domainFactoryId = System.currentTimeMillis(); // ?? 
    /**
     * If false suppress exception when call writeData on empty objectList
     */
    protected boolean exceptionOnEmptyObjectList = false;
    /**
     * Just a convenient member/method
     */
    protected boolean valid = true;
    /**
     * Create and populate MetaData automatically
     */
    protected boolean createDefaultMetaData = true;
    /**
     * Throw Exception or ignore if DomainObject has less fields than mapping
     */
    protected boolean throwMissingFieldException = false;
    /**
     * object to read data into objectList<DomainObject>
     */
    protected DataReader<T> dataReader = null;
    /**
     * options to set to DataReader and DataWriter implementations
     */
    protected Map<Enum<?>, Object> options;
    protected Instantiator<T> instantiator = null;
    protected PropertyChangeListener[] listeners = null;
    private static final transient Logger logger = Logger.getLogger(AbstractDomainObjectFactory.class.getName());
    private boolean lenient = true;
    /*================================== members end==========================*/

    /**
     * Returns filtered list of previously loaded objects
     */
    @SuppressWarnings("unchecked")
    public List<T> filter(Predicate<T> p) {
        List<T> filteredList = new ArrayList<>();
        for (T t : (List<T>) objectList) {
            if (p.evaluate(t)) {
                filteredList.add(t);
            }
        }
        return filteredList;
    }

    /**
     * Loaded factory with data from several sources
     */
    public int load(Wrapper[] data) throws Exception {
        boolean origAppend = this.isAppend();
        this.setAppend(true);
        int rowCount = 0;
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::data.length: " + data.length);
            logger.finest("julp::" + this + "::data: " + Arrays.asList(data));
        }

        for (int i = 0; i < data.length; i++) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::data[" + i + "]: " + data[i]);
            }
            rowCount = rowCount + load(data[i]);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::rowCount_" + i + ": " + rowCount);
            }
        }
        this.setAppend(origAppend);
        return rowCount;
    }

    public int load(Wrapper data, int offset, int limit) throws DataAccessException {
        if (!append) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::load()::objectId 1: " + objectId);
            }
            this.objectId = -1;
            if (this.objectList != null) {
                this.objectList.clear();
            }
        }
        getDataReader().setMetaData(getMetaData());
        if (options != null) {
            getDataReader().setOptions(options);
        }
        this.objectList.addAll(getDataReader().readData(data, offset, limit));

        for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
            domainObject.setObjectId(this.getNextObjectId());
            domainObject.setPersistentState(PersistentState.ORIGINAL);
            domainObject.setDomainFactoryId(domainFactoryId);

            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    domainObject.addPropertyChangeListener(l);
                }
            }
        }
        return this.objectList.size();
    }

    /**
     * Loaded factory with data
     */
    public int load(Wrapper data) throws DataAccessException {
        if (!append) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::load()::objectId 1: " + objectId);
            }
            this.objectId = -1;
            if (this.objectList != null) {
                this.objectList.clear();
            }
        }
        getDataReader().setMetaData(getMetaData());
        if (options != null) {
            getDataReader().setOptions(options);
        }
        this.objectList.addAll(getDataReader().readData(data));

        for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
            domainObject.setObjectId(this.getNextObjectId());
            domainObject.setPersistentState(PersistentState.ORIGINAL);
            domainObject.setDomainFactoryId(domainFactoryId);

            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    domainObject.addPropertyChangeListener(l);
                }
            }
        }
        return this.objectList.size();
    }

    /**
     * Getter for property mapping.
     *
     * @return Value of property mapping.
     */
    public Map<String, String> getMapping() {
        return mapping;
    }

    /**
     * Setter for property mapping.
     *
     * @param mapping New value of property mapping. Format:
     * [TABLE].COLUMN_1=fieldName1 [TABLE].COLUMN_2=fieldName2 etc...
     */
    public void setMapping(Map<String, String> mapping) {
        if (this.mapping == null) {
            this.mapping = new HashMap<>();
        } else {
            this.mapping.clear();
        }
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            this.mapping.put(entry.getKey().trim(), entry.getValue().trim());
        }
        init();
    }

    /**
     * @param mapping Set mapping using string format:
     * [TABLE].COLUMN=fieldName=, [TABLE].COLUMN=fieldName, etc...
     */
    public void setMapping(String mapping) {
        StringTokenizer tokenizer = new StringTokenizer(mapping, ",", false);
        if (this.mapping == null) {
            this.mapping = new HashMap<>();
        } else {
            this.mapping.clear();
        }
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int idx = token.indexOf("=");
            if (idx == -1) {
                throw new IllegalArgumentException("DomainObjectFactory::setMapping(String mapping): " + mapping + "(Argument format: <COLUMN_NAME>=<fieldName>)");
            }
            this.mapping.put(token.substring(0, idx).trim(), token.substring(idx + 1).trim());
        }
        init();
    }

    /**
     * Getter for property domainClass.
     *
     * @return Value of property domainClass.
     */
    public java.lang.Class<T> getDomainClass() {
        return domainClass;
    }

    /**
     * Setter for property domainClass.
     *
     * @param domainClass New value of property domainClass.
     */
    public void setDomainClass(java.lang.Class<T> domainClass) {
        this.domainClass = domainClass;
        if (metaData != null && metaData.getDomainClass() != null && !metaData.getDomainClass().equals(domainClass)) {
            metaData.populate(mapping, domainClass);
            setDataReader(null);
            getDataReader().setMetaData(metaData);
        }
    }

    /**
     * After loading and modifying, set domainObject back to factory
     */
    @SuppressWarnings("unchecked")
    public int setObject(T domainObject) {
        int idx = -1;
        try {
            int id = -1;
            if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                id = this.getNextObjectId();
                ((DomainObject<T>) domainObject).setObjectId(id);
                this.objectList.add((DomainObject<T>) domainObject);
                idx = this.objectList.size();
            } else {
                id = ((DomainObject<T>) domainObject).getObjectId();
                if (id == -1) {  // new object
                    id = this.getNextObjectId();
                    ((DomainObject<T>) domainObject).setObjectId(id);
                    this.objectList.add((DomainObject<T>) domainObject);
                    idx = this.objectList.size();
                } else {
                    idx = findIdxByObjectId(id);
                    if (idx >= 0) {
                        this.objectList.set(idx, (DomainObject<T>) domainObject);
                    } else {
                        this.objectList.add((DomainObject<T>) domainObject);
                    }
                }
            }
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return idx;
    }

    @SuppressWarnings("unchecked")
    public void setObject(int index, T domainObject) {
        try {
            if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                int id = this.getNextObjectId();
                ((DomainObject<T>) domainObject).setObjectId(id);
            } else {
                if (((DomainObject<T>) domainObject).getObjectId() == -1) {
                    int id = this.getNextObjectId();
                    ((DomainObject<T>) domainObject).setObjectId(id);
                }
            }
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        this.objectList.set(index, (DomainObject<T>) domainObject);
    }

    @SuppressWarnings("unchecked")
    public void addObject(int index, T domainObject) {
        try {
            int id = -1;
            if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                id = this.getNextObjectId();
                ((DomainObject<T>) domainObject).setObjectId(id);
            } else {
                if (((DomainObject<T>) domainObject).getObjectId() == -1) {
                    id = this.getNextObjectId();
                    ((DomainObject<T>) domainObject).setObjectId(id);
                }
            }
            this.objectList.add(index, (DomainObject<T>) domainObject);
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
    }

    @SuppressWarnings("unchecked")
    public int addObject(T domainObject) {
        try {
            int id = -1;
            if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                id = this.getNextObjectId();
                ((DomainObject<T>) domainObject).setObjectId(id);
            } else {
                if (((DomainObject<T>) domainObject).getObjectId() == -1) {
                    id = this.getNextObjectId();
                    ((DomainObject<T>) domainObject).setObjectId(id);
                }
            }
            this.objectList.add((DomainObject<T>) domainObject);
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return this.objectList.size() - 1;
    }

    @SuppressWarnings("unchecked")
    public T findObjectByObjectId(int objectId) {
        int idx = findIdxByObjectId(objectId);
        return (T) this.objectList.get(idx);
    }

    public int findIdxByObjectId(int objectId) {
        if (objectId == -1) {
            return objectId;
        }
        int id = -1;
        int idx = -1;
        boolean found = false;
        Iterator<DomainObject<T>> it = this.objectList.iterator();
        while (it.hasNext()) {
            DomainObject<T> domainObject = it.next();
            id = domainObject.getObjectId();
            if (objectId == id) {
                found = true;
                idx++;
                break;
            }
            idx++;
        }
        if (!found) {
            idx = -1;
        }
        return idx;
    }

    protected int getNextObjectId() {
        this.objectId++;
        return this.objectId;
    }

    /**
     * Getter for property objectList.
     *
     * @return Value of property objectList.
     */
    public List<DomainObject<T>> getObjectList() {
        return objectList;
    }

    /**
     * Setter for property objectList.
     *
     * @param objectList New value of property objectList.
     */
    public void setObjectList(List<DomainObject<T>> objectList) {
        this.objectList = objectList;
        if (objectList != null) {
            this.objectId = -1;
            for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
                domainObject.setDomainFactoryId(domainFactoryId);
                domainObject.setObjectId(getNextObjectId());
            }
            if (listeners != null) {
                for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
                    for (PropertyChangeListener l : listeners) {
                        domainObject.addPropertyChangeListener(l);
                    }
                }
            }
        }
    }

    /**
     * Will not delete from DB, just from objectList
     */
    @SuppressWarnings("unchecked")
    public boolean discard(T domainObject) {
        if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
            return false;
        }
        int idx = findIdxByObjectId(((DomainObject<T>) domainObject).getObjectId());
        if (idx >= 0) {
            int discardedObjectId = this.objectList.remove(idx).getObjectId();
            if (((DomainObject<T>) domainObject).getObjectId() != discardedObjectId) {
                throw new DataAccessException("discard()::expected objectId: " + ((DomainObject<T>) domainObject).getObjectId() + ", discardedObjectId: " + discardedObjectId);
            }
            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    ((DomainObject<T>) domainObject).removePropertyChangeListener(l);
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Will remove from db after writeData() and commit;
     */
    @SuppressWarnings("unchecked")
    public boolean remove(T domainObject) {
        boolean success = false;
        try {
            boolean createOriginalValues = lenient;
            int removedIdx = -1;
            if (((DomainObject<T>) domainObject).getDomainFactoryId() == domainFactoryId) {
                int idx = findIdxByObjectId(((DomainObject<T>) domainObject).getObjectId());
                if (!beforeRemove(domainObject)) {
                    return success;
                }
                if (idx >= 0) {
                    if (((DomainObject<T>) domainObject).getPersistentState() == PersistentState.CREATED) {
                        int removedObjectId = this.objectList.remove(idx).getObjectId(); // Will not delete from DB, just from objectList
                        if (((DomainObject<T>) domainObject).getObjectId() != removedObjectId) {
                            throw new DataAccessException("remove()::expected objectId: " + ((DomainObject<T>) domainObject).getObjectId() + ", removedObjectId: " + removedObjectId);
                        }
                        success = false;
                    } else {
                        if (createOriginalValues && ((DomainObject<T>) domainObject).getOriginalValues() == null) {
                            ((DomainObject<T>) domainObject).setOriginalValues(createOriginalValues());
                            syncOriginal(domainObject);
                        }
                        if (((DomainObject<T>) domainObject).remove()) {
                            this.removedObjects.add((DomainObject<T>) domainObject);
                            removedIdx = this.removedObjects.size();
                            int removedObjectId = this.objectList.remove(idx).getObjectId();
                            if (((DomainObject<T>) domainObject).getObjectId() != removedObjectId) {
                                throw new DataAccessException("remove()::expected objectId: " + ((DomainObject<T>) domainObject).getObjectId() + ", removedObjectId: " + removedObjectId);
                            }
                            success = true;
                        } else { // discard?                           
                            success = false;
                        }
                    }
                } else {
                    ((DomainObject<T>) domainObject).setObjectId(this.getNextObjectId());
                    if (((DomainObject<T>) domainObject).getPersistentState() == PersistentState.CREATED) {
                        // Will not delete from DB, just from objectList
                        success = false;
                    } else {
                        if (createOriginalValues && ((DomainObject<T>) domainObject).getOriginalValues() == null) {
                            ((DomainObject<T>) domainObject).setOriginalValues(createOriginalValues());
                            syncOriginal(domainObject);
                        }
                        if (((DomainObject<T>) domainObject).remove()) {
                            this.removedObjects.add((DomainObject<T>) domainObject);
                            success = true;
                        }
                    }
                }
                if (!afterRemove(removedIdx)) {
                    return false;
                }
            } else {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                if (!beforeRemove(domainObject)) {
                    return false;
                }
                ((DomainObject<T>) domainObject).setObjectId(this.getNextObjectId());
                if (((DomainObject<T>) domainObject).getPersistentState() == PersistentState.CREATED) {
                    this.removedObjects.add(((DomainObject<T>) domainObject)); // Will not delete from DB, just from objectList
                    success = false;
                } else {
                    if (createOriginalValues && ((DomainObject<T>) domainObject).getOriginalValues() == null) {
                        ((DomainObject<T>) domainObject).setOriginalValues(createOriginalValues());
                        syncOriginal(domainObject);
                    }
                    if (((DomainObject<T>) domainObject).remove()) {
                        this.removedObjects.add((DomainObject<T>) domainObject);
                        success = true;
                    }
                }
                if (!afterRemove(removedIdx)) {
                    return false;
                }
            }
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return success;
    }

    /**
     * Override as needed
     */
    public boolean beforeRemove(T domainObject) {
        return true;
    }

    /**
     * Override as needed
     */
    public boolean afterRemove(int idx) {
        return true;
    }

    /**
     * Will update db after writeData() and commit;
     */
    @SuppressWarnings("unchecked")
    public boolean store(T domainObject) {
        boolean success = false;
        try {
            int idx = -1;
            if (((DomainObject<T>) domainObject).getDomainFactoryId() == domainFactoryId) {  // domainObject could be from another DomainObjectFactory or created manually or else...
                idx = findIdxByObjectId(((DomainObject<T>) domainObject).getObjectId());
                if (!beforeStore(domainObject)) {
                    return false;
                }
                if (idx >= 0) {
                    if (((DomainObject<T>) domainObject).store()) {
                        this.objectList.set(idx, (DomainObject<T>) domainObject);
                        success = true;
                    }
                } else {
                    ((DomainObject<T>) domainObject).setObjectId(this.getNextObjectId());
                    if (((DomainObject<T>) domainObject).store()) {
                        this.objectList.add((DomainObject<T>) domainObject);
                        idx = this.objectList.size();
                        success = true;
                    } else {
                        success = false;
                    }
                }
                if (!afterStore(idx)) {
                    return false;
                }
            } else {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                ((DomainObject<T>) domainObject).setObjectId(this.getNextObjectId());
                if (!beforeStore(domainObject)) {
                    return false;
                }
                if (((DomainObject<T>) domainObject).store()) {
                    this.objectList.add((DomainObject<T>) domainObject);
                    idx = this.objectList.size();
                    success = true;
                } else {
                    success = false;
                }
                if (!afterStore(idx)) {
                    return false;
                }
            }
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return success;
    }

    /**
     * Override as needed
     */
    public boolean beforeStore(T domainObjectx) {
        return true;
    }

    /**
     * Override as needed
     */
    public boolean afterStore(int idx) {
        return true;
    }

    /**
     * Will insert into db after writeData() and commit;
     */
    @SuppressWarnings("unchecked")
    public boolean create(T domainObject) {
        boolean success = false;
        try {
            if (lenient) {
                ((DomainObject<T>) domainObject).setOriginalValues(null);
            }
            if (((DomainObject<T>) domainObject).getDomainFactoryId() != domainFactoryId) {
                ((DomainObject<T>) domainObject).setDomainFactoryId(domainFactoryId);
                ((DomainObject<T>) domainObject).setObjectId(-1);
            }
            int idx = -1;

            if (!beforeCreate(domainObject)) {
                return false;
            }
            if (((DomainObject<T>) domainObject).create()) {
                idx = this.setObject(domainObject);
                success = true;
            }
            if (!afterCreate(idx)) {
                return false;
            }
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return success;
    }

    /**
     * Override as needed
     */
    public boolean beforeCreate(T domainObject) {
        return true;
    }

    /**
     * Override as needed
     */
    public boolean afterCreate(int idx) {
        return true;
    }

    /**
     * Getter for property removedObjects.
     *
     * @return Value of property removedObjects. removedObjects marked with
     * PersistentState.REMOVED.
     */
    public List<DomainObject<T>> getRemovedObjects() {
        return removedObjects;
    }

    /**
     * Setter for property removedObjects.
     *
     * @param removedObjects New value of property removedObjects.
     */
    public void setRemovedObjects(List<DomainObject<T>> removedObjects) {
        this.removedObjects = removedObjects;
    }

    /**
     * Getter for property discardUnmodifiedObjects.
     *
     * @return Value of property discardUnmodifiedObjects.
     */
    public boolean isDiscardUnmodifiedObjects() {
        return discardUnmodifiedObjects;
    }

    /**
     * Setter for property discardUnmodifiedObjects.
     *
     * @param discardUnmodifiedObjects New value of property
     * discardUnmodifiedObjects. leave only modified objects in objectList
     */
    public void setDiscardUnmodifiedObjects(boolean discardUnmodifiedObjects) {
        ListIterator<DomainObject<T>> li = this.objectList.listIterator();
        while (li.hasNext()) {
            DomainObject<T> domainObject = (DomainObject<T>) li.next();
            if (domainObject.getPersistentState() == PersistentState.ORIGINAL) {
                if (listeners != null) {
                    for (PropertyChangeListener l : listeners) {
                        domainObject.removePropertyChangeListener(l);
                    }
                }
                li.remove();
            }
        }
        this.discardUnmodifiedObjects = discardUnmodifiedObjects;
    }

    /**
     * Getter for property readOnly.
     *
     * @return Value of property readOnly.
     *
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Setter for property readOnly.
     *
     * @param readOnly New value of property readOnly. makes this not updatable.
     */
    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    /**
     * All modified objects in objectList set to PersistentState.ORIGINAL and
     * object values set to original (as during lod() or syncOriginal or
     * setOriginal)
     */
    public void setOriginal() {
        if (isReadOnly()) {
            throw new IllegalStateException("Cannot setOriginal() for readOnly object");
        }
        objectList.addAll((List<DomainObject<T>>) removedObjects);
        ListIterator<DomainObject<T>> li = objectList.listIterator();
        while (li.hasNext()) {
            DomainObject<T> domainObject = li.next();
            if (domainObject.getPersistentState() == PersistentState.REMOVED
                    && domainObject.getOriginalValues() != null
                    && domainObject.getOriginalValues().getFieldsCount() > 0) {
                domainObject.setPersistentState(PersistentState.ORIGINAL);
            } else if (domainObject.getPersistentState() == PersistentState.CREATED
                    && domainObject.getOriginalValues() == null
                    || domainObject.getOriginalValues().getFieldsCount() < 1) {
                li.remove();
            } else if (domainObject.getPersistentState() == PersistentState.STORED
                    && domainObject.getOriginalValues() != null
                    && domainObject.getOriginalValues().getFieldsCount() > 0) {
                domainObject.setPersistentState(PersistentState.ORIGINAL);
            } else {
                throw new IllegalArgumentException("Invalid Persistent Status");
            }
        }
        removedObjects.clear();
    }

    /**
     * Override as needed
     */
    public boolean beforeWriteData() {
        return true;
    }

    /**
     * Implement and call this method to persist data
     */
    public abstract boolean writeData();

    /**
     * Override as needed
     */
    public boolean afterWriteData() {
        return true;
    }

    /**
     * Getter for property metaData.
     *
     * @return Value of property metaData.
     */
    public MetaData<T> getMetaData() {
        if (createDefaultMetaData && this.metaData == null) {
            populateMetaData();
        }
        return this.metaData;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void loadMappings(String resource) {
        Properties prop = loadProperties(resource);
        setMapping((Map) prop);
    }

    public Properties loadProperties(String resource) {
        java.io.InputStream inStream = null;
        Properties props = new Properties();
        try {
            inStream = this.getClass().getResourceAsStream(resource);
            if (inStream == null) {
                inStream = new FileInputStream(resource);
            }
            props.load(inStream);
        } catch (IOException e) {
            throw new DataAccessException(e);
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }
            } catch (java.io.IOException ioe) {
                throw new DataAccessException(ioe);
            }
        }
        return props;
    }

    public abstract void populateMetaData();

    protected void init() {
        if (dataReader == null) {
            getDataReader();
        }
        if (dataWriter == null) {
            getDataWriter();
        }
    }

    /**
     * Setter for property metaData.
     *
     * @param metaData New value of property metaData.
     */
    public void setMetaData(MetaData<T> metaData) {
        this.metaData = metaData;
        init();
    }

    /**
     * Populate DomainObject with values from originalValues
     */
    @SuppressWarnings("unchecked")
    public boolean load(T domainObject) {
        try {
            ((DomainObject<T>) domainObject).setLoading(true);
            if (((DomainObject<T>) domainObject).getOriginalValues() != null && ((DomainObject<T>) domainObject).getOriginalValues().getFieldsCount() > 0) {
                try {
                    Object[] param = new Object[1];
                    for (int i = 1; i <= metaData.getFieldCount(); i++) {
                        String fieldName = metaData.getFieldName(i);
                        Method writeMethod = metaData.getWriteMethod(i);
                        Object value = ((DomainObject<T>) domainObject).getOriginalValues().getFieldValue(fieldName);
                        param[0] = value;
                        try {
                            writeMethod.invoke(domainObject, param);
                        } catch (Exception e) {
                            throw new DataAccessException(e);
                        }
                    }
                } catch (Throwable t) {
                    throw new DataAccessException(t);
                }
                ((DomainObject<T>) domainObject).setLoading(false);
                ((DomainObject<T>) domainObject).setLoaded(true);
                ((DomainObject<T>) domainObject).setModified(false);
                return true;
            }
            ((DomainObject<T>) domainObject).setLoading(false);
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
        return false;
    }

    /**
     * Populate originalValues with values from DomainObject
     */
    @SuppressWarnings("unchecked")
    public void syncOriginal(T domainObject) {
        try {
            DataHolder originalValues = null;
            if (((DomainObject<T>) domainObject).getOriginalValues() == null) {
                originalValues = new DataHolder(metaData.getFieldCount());
            } else {
                originalValues = ((DomainObject<T>) domainObject).getOriginalValues();
            }
            for (int i = 1; i <= metaData.getFieldCount(); i++) {
                String fieldName = metaData.getFieldName(i);
                Method readMethod = metaData.getReadMethod(i);
                try {
                    Object value = readMethod.invoke(domainObject, EMPTY_ARG);
                    originalValues.setFieldNameAndValue(i, fieldName, value);
                } catch (Exception e) {
                    throw new DataAccessException(e);
                }
            }
            if (((DomainObject<T>) domainObject).getPersistentState() == PersistentState.REMOVED) {
                ((DomainObject<T>) domainObject).setPersistentState(PersistentState.UNDEFINED);
            } else {
                ((DomainObject<T>) domainObject).setPersistentState(PersistentState.ORIGINAL);
            }
            ((DomainObject<T>) domainObject).setOriginalValues(originalValues);
        } catch (ClassCastException e) {
            throw new DataAccessException(domainClass.getCanonicalName() + " must extend org.julp.AbstractDomainObject or instantiated by using " + getClass().getCanonicalName() + ".newInstance(); or enchanced  by using " + getClass().getCanonicalName() + ".attach(T instance);", e);
        }
    }

    /**
     * IMPORTANT! Call this method after successful COMMIT to sync original and
     * current values: copy current values to original and set persistentState
     * == ORIGINAL and discard Objects deleted from DB
     */
    public void synchronizePersistentState() {
        if (isReadOnly()) {
            throw new IllegalStateException("Cannot call synchronizePersistentState() for readOnly object");
        }
        try {
            removedObjects.clear();
            ListIterator<DomainObject<T>> li = objectList.listIterator();
            while (li.hasNext()) {
                boolean copyValues = true;
                boolean created = false;
                DomainObject<T> domainObject = li.next();
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::synchronizePersistentState()::DomainObject: " + domainObject + " \n");
                }
                if (domainObject.getPersistentState() == PersistentState.REMOVED
                        && domainObject.getOriginalValues() != null
                        && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    li.remove();
                    copyValues = false;
                } else if (domainObject.getPersistentState() == PersistentState.CREATED
                        && (domainObject.getOriginalValues() == null
                        || domainObject.getOriginalValues().getFieldsCount() < 1)) {
                    domainObject.setPersistentState(PersistentState.ORIGINAL);
                    created = true;
                } else if (domainObject.getPersistentState() == PersistentState.STORED
                        && domainObject.getOriginalValues() != null
                        && domainObject.getOriginalValues().getFieldsCount() > 0) {
                    domainObject.setPersistentState(PersistentState.ORIGINAL);
                } else if (domainObject.getPersistentState() == PersistentState.ORIGINAL) {
                    // do nothing
                    continue;
                } else {
                    throw new IllegalArgumentException("Invalid PersistentState");
                }
                if (copyValues) { // Sync values in Object and it's originalValues
                    DataHolder originalValues = null;
                    if (created) {
                        originalValues = new DataHolder(mapping.size());
                    } else {
                        originalValues = domainObject.getOriginalValues();
                    }
                    Iterator<String> iter = mapping.values().iterator();
                    int fieldIndex = 0;
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::synchronizePersistentState() \n");
                    }
                    while (iter.hasNext()) {
                        fieldIndex++;
                        String fieldName = iter.next();
                        if (created) {
                            Method readMethod = getMetaData().getReadMethod(getMetaData().getFieldIndexByFieldName(fieldName));
                            Object value = readMethod.invoke(domainObject, EMPTY_ARG);
                            originalValues.setFieldNameAndValue(fieldIndex, fieldName, value);
                        } else {
                            if (originalValues.findFieldIndex(fieldName) != -1) {
                                Method readMethod = getMetaData().getReadMethod(getMetaData().getFieldIndexByFieldName(fieldName));
                                Object value = readMethod.invoke(domainObject, EMPTY_ARG);
                                domainObject.setOriginalValue(fieldName, value);
                            }
                        }
                    }
                    if (created) {
                        domainObject.setOriginalValues(originalValues);
                        created = false;
                    }
                    domainObject.setModified(false);
                }
            }
            if (this.dataWriter != null) {
                this.dataWriter.reset();
            }
            valid = true;
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw new DataAccessException(((InvocationTargetException) t).getTargetException());
            } else {
                throw new DataAccessException(t);
            }
        }
    }

    /**
     * Getter for property removedCount.
     *
     * @return Value of property removedCount. get number of removed objects
     * after writeData()
     */
    public int getRemovedCount() {
        if (dataWriter == null) {
            throw new NullPointerException("DataWriter not initialized");
        }
        return dataWriter.getRemovedCount();
    }

    /**
     * Getter for property createdCount.
     *
     * @return Value of property createdCount. get number of created objects
     * after writeData()
     */
    public int getCreatedCount() {
        if (dataWriter == null) {
            throw new NullPointerException("DataWriter not initialized");
        }
        return dataWriter.getCreatedCount();
    }

    /**
     * Getter for property modifiedCount.
     *
     * @return Value of property modifiedCount. get number of modified objects
     * after writeData()
     */
    public int getModifiedCount() {
        if (dataWriter == null) {
            throw new NullPointerException("DataWriter not initialized");
        }
        return dataWriter.getModifiedCount();
    }

    /**
     * Getter for property dataReader.
     *
     * @return Value of property dataReader.
     */
    public DataReader<T> getDataReader() {
        return dataReader;
    }

    /**
     * Setter for property dataReader.
     *
     * @param dataReader New value of property dataReader.
     */
    public void setDataReader(DataReader<T> dataReader) {
        this.dataReader = dataReader;
        if (this.dataReader != null && options != null) {
            this.dataReader.setOptions(options);
        }
    }

    /**
     * Getter for property dataWriter.
     *
     * @return Value of property dataWriter.
     */
    public DataWriter<T> getDataWriter() {
        return dataWriter;
    }

    /**
     * Setter for property dataWriter.
     *
     * @param dataWriter New value of property dataWriter.
     */
    public void setDataWriter(DataWriter<T> dataWriter) {
        this.dataWriter = dataWriter;
        if (this.dataWriter != null && options != null) {
            this.dataWriter.setOptions(options);
        }
    }

    /**
     * Getter for property lazyLoading.
     *
     * @return Value of property lazyLoading.
     */
    public boolean isLazyLoading() {
        return lazyLoading;
    }

    /**
     * Setter for property lazyLoading.
     *
     * @param lazyLoading New value of property lazyLoading.
     */
    public void setLazyLoading(boolean lazyLoading) {
        this.lazyLoading = lazyLoading;
        if (dataReader != null) {
            dataReader.setLazyLoading(lazyLoading);
        } else {
            throw new NullPointerException("DataReader not initialized");
        }
    }

    /**
     * Getter for property append.
     *
     * @return Value of property append.
     */
    public boolean isAppend() {
        return append;
    }

    /**
     * Setter for property append.
     *
     * @param append New value of property append. append values to objectList
     * during load() instead of removing old values first
     */
    public void setAppend(boolean append) {
        this.append = append;
    }

    /**
     * Returns error during writeData()
     */
    public java.lang.Throwable getPersistenceError() {
        if (this.dataWriter == null) {
            return null;
        }
        return this.dataWriter.getPersistenceError();
    }

    /**
     * Clear: removedObjects, objectList
     */
    public void clearData() {
        if (this.objectList != null) {
            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    for (DomainObject domainObject : this.objectList) {
                        domainObject.removePropertyChangeListener(l);
                    }
                }
            }
            this.objectList.clear();
        }
        if (this.removedObjects != null) {
            if (listeners != null) {
                for (PropertyChangeListener l : listeners) {
                    for (DomainObject domainObject : this.removedObjects) {
                        domainObject.removePropertyChangeListener(l);
                    }
                }
            }
            this.removedObjects.clear();
        }
        objectId = -1;
        valid = true;
    }

    /**
     * Override as needed
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Create DataHolder from MetaData
     */
    public DataHolder createOriginalValues() {
        DataHolder dataHolder = new DataHolder(getMetaData().getFieldCount());
        for (int i = 1; i <= getMetaData().getFieldCount(); i++) {
            dataHolder.setFieldName(i, getMetaData().getFieldName(i));
        }
        return dataHolder;
    }

    public boolean isExceptionOnEmptyObjectList() {
        return exceptionOnEmptyObjectList;
    }

    /**
     * Throw error if there is no data on writeData()
     */
    public void setExceptionOnEmptyObjectList(boolean exceptionOnEmptyObjectList) {
        this.exceptionOnEmptyObjectList = exceptionOnEmptyObjectList;
        if (dataWriter != null) {
            dataWriter.setExceptionOnEmptyObjectList(exceptionOnEmptyObjectList);
        }
    }

    public boolean isCreateDefaultMetaData() {
        return createDefaultMetaData;
    }

    public void setCreateDefaultMetaData(boolean createDefaultMetaData) {
        this.createDefaultMetaData = createDefaultMetaData;
    }

    public boolean isThrowMissingFieldException() {
        return throwMissingFieldException;
    }

    public void setThrowMissingFieldException(boolean throwMissingFieldException) {
        this.throwMissingFieldException = throwMissingFieldException;
        if (metaData != null) {
            metaData.setThrowMissingFieldException(throwMissingFieldException);
        }
    }

    public void setOptions(Map<Enum<?>, Object> options) {
        this.options = options;
        if (dataReader != null) {
            dataReader.setOptions(options);
        }
        if (dataWriter != null) {
            dataWriter.setOptions(options);
        }
    }

    public Map<Enum<?>, Object> getOptions() {
        return this.options;
    }

    /**
     * Return casted objectList
     */
    @SuppressWarnings("unchecked")
    public List<T> getObjects() {
        return ((List<T>) this.objectList);
    }

    /**
     * Instantiate an object. If the object does not implement DomainObject then
     * it will be enhanced by CGLIB/ASM. You cannot create your POJO by using
     * 'MyPOJO pojo = new MyPOJO()'. Must use this factory method or attach(T
     * instance) MyPOJO pojo = new MyPOJO(); DomainObjectFactory factory...
     * MyPOJO attachedPOJO = (MyPOJO) factory.attach(pojo); However using
     * attach(...) method has a limitation. From within enhanced object you
     * cannot call methods of DomainObject:      <code>
     * public void setCustomerId(java.lang.Integer customerId) {
     * if (!((DomainObject<T>) this).isLoading()) {
     * ...
     * </code>
     *
     * this will throw ClassCastException. In case you need to call DomainObject
     * methods from within attached (enhanced) object instantiate object using
     * newInstance()
     */
    public T newInstance() {
        if (getDataReader().getDomainClass() == null) {
            getDataReader().setMetaData(getMetaData());
        }
        Object obj = getDataReader().getInstantiator().newInstance(domainClass);
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                ((DomainObject<T>) obj).addPropertyChangeListener(l);
            }
        }
        return setupEnhancedObject(obj, PersistentState.UNDEFINED);
    }

    /**
     * Returns unenhanced object and discarded it from objectList. If
     * domainObject is not enchanced the method returns the same object
     * otherwise it returns object defined in DomainObjectFactory (objectClass).
     */
    @SuppressWarnings("unchecked")
    public T detach(T instance) {
        if (instance == null) {
            throw new DataAccessException("Instance is null");
        }
        if (!(instance instanceof DomainObject)) {
            throw new IllegalArgumentException("Parameter 'instance' must implement DomainObject");
        }
        if (((DomainObject<T>) instance).getDomainFactoryId() != domainFactoryId) {
            throw new DataAccessException("DomainObjectFactory has no DomainObject: " + instance);
        }
        discard((T) instance);
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                ((DomainObject<T>) instance).removePropertyChangeListener(l);
            }
        }
        if (getDataReader().getInstantiator().isEnhanced(instance)) {
            return (T) ((DomainObject<T>) instance).detach();
        } else {
            return instance;
        }
    }

    /**
     * MyPOJO pojo = new MyPOJO(); DomainObjectFactory factory... MyPOJO
     * attachedPOJO = (MyPOJO) factory.attach(pojo); See newInstance() comments
     */
    public T attach(T instance) {
        if (instance == null) {
            throw new DataAccessException("Instance is null");
        }
        if (getDataReader().getDomainClass() == null) {
            getDataReader().setMetaData(getMetaData());
        }
        Object obj = getDataReader().getInstantiator().enhance(instance);
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                ((DomainObject<T>) obj).addPropertyChangeListener(l);
            }
        }
        return setupEnhancedObject(obj, PersistentState.ORIGINAL);
    }

    /**
     * MyPOJO pojo = new MyPOJO(); DomainObjectFactory factory... MyPOJO
     * attachedPOJO = (MyPOJO) factory.attach(pojo, PersistentState.CREATED);
     * This will enhance object which will generate INSERT See newInstance()
     * comments
     */
    public T attach(T instance, PersistentState state) {
        if (getDataReader().getDomainClass() == null) {
            getDataReader().setMetaData(getMetaData());
        }
        Object obj = getDataReader().getInstantiator().enhance(instance);
        if (listeners != null) {
            for (PropertyChangeListener l : listeners) {
                ((DomainObject<T>) obj).addPropertyChangeListener(l);
            }
        }
        return setupEnhancedObject(obj, state);
    }

    @SuppressWarnings("unchecked")
    protected T setupEnhancedObject(Object obj, PersistentState state) {
        if (state == PersistentState.STORED) {
            throw new IllegalArgumentException("PersistentState must not be STORED");
        }
        ((DomainObject<T>) obj).setLoading(true);
        ((DomainObject<T>) obj).setDomainFactoryId(domainFactoryId);
        ((DomainObject<T>) obj).setObjectId(getNextObjectId());
        if (state == PersistentState.ORIGINAL) {
            ((DomainObject<T>) obj).setOriginalValues(createOriginalValues());
            ((DomainObject<T>) obj).syncOriginal();
            addObject((T) obj);
        } else if (state == PersistentState.UNDEFINED) {
            ((DomainObject<T>) obj).setOriginalValues(createOriginalValues());
        } else if (state == PersistentState.CREATED) {
            create((T) obj);
        } else if (state == PersistentState.REMOVED) {
            remove((T) obj);
        }
        if (listeners != null) {
            for (PropertyChangeListener pcl : listeners) {
                ((DomainObject<T>) obj).addPropertyChangeListener(pcl);
            }
        }
        ((DomainObject<T>) obj).setLoading(false);
        return (T) obj;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        Object value;
        Method[] methods = getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.equals("getMetaData") || methodName.equals("getClass")) {
                continue;
            }
//            if (methodName.equals("getDataModificationSequence") && methods[i].getParameterTypes().length == 0) {
//                if (dataModificationSequence != null && dataModificationSequence.length > 0) {
//                    sb.append("dataModificationSequence=").append(new String(dataModificationSequence)).append("&");
//                } else {
//                    sb.append("dataModificationSequence=").append("&");
//                }
//            } else

            if ((methodName.startsWith("get") || methodName.startsWith("is")) && methods[i].getParameterTypes().length == 0) {
                try {
                    value = methods[i].invoke(this, EMPTY_ARG);
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
        return sb.toString();
    }

    public void addPropertyChangeListeners(PropertyChangeListener[] listeners) {
        if (listeners == null) {
            throw new IllegalArgumentException("addPropertyChangeListeners: listeners are null");
        }
        this.listeners = listeners;
        if (objectList != null && !objectList.isEmpty()) {
            for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
                for (PropertyChangeListener l : listeners) {
                    domainObject.addPropertyChangeListener(l);
                }
            }
        }
    }

    public void removePropertyChangeListeners(PropertyChangeListener[] listeners) {
        if (listeners != null && objectList != null) {
            for (DomainObject<T> domainObject : (List<DomainObject<T>>) this.objectList) {
                for (PropertyChangeListener l : listeners) {
                    domainObject.removePropertyChangeListener(l);
                }
            }
        }
    }

    protected Object readValue(Method method, Object obj) throws Throwable {
        Object value = null;
        try {
            value = method.invoke(obj, EMPTY_ARG);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw ((InvocationTargetException) t).getTargetException();
            } else {
                throw t;
            }
        }
        return value;
    }

    protected void writeValue(Object obj, Method method, Object[] value) throws Throwable {
        try {
            method.invoke(obj, value);
        } catch (Throwable t) {
            if (t instanceof InvocationTargetException) {
                throw ((InvocationTargetException) t).getTargetException();
            } else {
                throw t;
            }
        }
    }
}

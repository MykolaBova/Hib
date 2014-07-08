package simpleorm.dataset;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import simpleorm.utils.SException;
import simpleorm.utils.SLog;


/**
 * A DataSet contains records of various types together with their meta data.  
 * The records are indexed by their type and primary keys, and so can be efficiently retrieved.
 * A list of dirty records that need to be updated is also maintained.<p>
 *
 * A DataSet is normally associated with a SSession/Jdbc, and is accessed indirectly via the SSessionJdbc methods.
 * But a DataSet can also be accessed directly when it is detached from the SSession.<p> 
 * @author aberglas
 */
public class SDataSet implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 20083L;

	/**
	 * Of all records retrieved. Both key and body are SRecordInstances, which
	 * has redefined equals() and hashTable();
     * (Was known as the cache.)
	 */
	Map<SRecordInstance,SRecordInstance> records = new LinkedHashMap<SRecordInstance,SRecordInstance>();
    	
	/** Ordered list of SRecordInstances to flush and purge. 
     * NEEDED to maintain the Order of the flushing for foreign key updates.
     * @See SFieldReference.innerRawSetFieldValue
     */
	ArrayList<SRecordInstance> dirtyRecords = new ArrayList<SRecordInstance>(20);
	
//	/**
//	 * The record cache as saved by a savepoint 
//	 */
//	private Map<SRecordInstance,SRecordInstance> spRecords = null;
//	/**
//	 * The dirty list as saved by a savepoint 
//	 */
//	private ArrayList<SRecordInstance> spDirtyRecords = null;
//	/**
//     * The list of records that have been changed after the begin and must be restored
//     */
//	private ArrayList<SRecordInstance> spNewDirtyRecords = null;
    
    transient private SSessionI session;
    
    transient SLog logger;
    
	public SDataSet() {
		//
	}

        ////////////////// These should be the only Public methods /////////////////////

   public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, Object... keys) {
		RI keyInstance = rmeta.newRecordInstance();
		RI instance = finder(keyInstance, keys);
        if (instance == null) {
            instance = keyInstance;
            instance.setInitialValues();
		    records.put(instance, instance);
            // We leave it not new and not dirty.  That is set by the SSession.
        }
        return instance;
	}
    public <RI extends SRecordInstance> RI find(SRecordMeta<RI> rmeta, Object... keys) {
		RI keyInstance = rmeta.newRecordInstance();
		RI instance = finder(keyInstance, keys);
        return instance;
	}
    private <RI extends SRecordInstance> RI finder(RI keyInstance, Object[] keys) {
        keyInstance.setDataSet(this); // needs to be done now for logging.
        keyInstance.setPrimaryKeys(keys);
        RI instance = this.findUsingPrototype(keyInstance);
        return instance;
    }

	public <RI extends SRecordInstance> RI create(SRecordMeta<RI> rmeta, Object... keys) {
		RI keyInstance = rmeta.newRecordInstance();
		RI instance = finder(keyInstance, keys);
        if (instance != null)
            throw new SException.Data("Record already in dataset " + keyInstance);
        keyInstance.setInitialValues();
        keyInstance.setNewRow(true);
        keyInstance.setDirty(true);
		records.put(keyInstance, keyInstance);
        return keyInstance;
	}
        
    /** Used if no key is known, and one is to be provided later. */
    public <RI extends SRecordInstance> RI createWithNullKey(SRecordMeta<RI> rmeta) {
		RI instance = rmeta.newRecordInstance();
        instance.nullPrimaryKeys();
        instance.setInitialValues(); // won't change nulled primary keys
        instance.setDataSet(this); // needs to be done now for logging.
        instance.setNewRow(true);
        instance.setDirty(true);
		records.put(instance, instance);
        return instance;
	}

    /** Remove record from cache, does not flag it for deletion. */
    public void removeRecord(SRecordInstance rinst) {

        rinst.setDirty(false);
    	records.remove(rinst);
//        if ( ! hasSavepoint())
        	rinst.destroy(); 
//        else
//        	rinst.restoreSavepoint();
	}

    public Collection<SRecordInstance> queryAllRecords() {
		return records.values();
	}
    
    public <R extends SRecordInstance> List<R> findAllRecords(SRecordMeta<R> rmeta) {
    	ArrayList<R> res = new ArrayList<R>();
    	for (SRecordInstance inst : queryAllRecords()) {
    		if (inst.getMeta() == rmeta)
    			res.add((R) inst);
    	}
		return res;
	}
    
    /** Return all records in dataset where rec.ref == ref.  
     * Ie. the inverse of findRecrod.
     */
    public <I extends SRecordInstance, R extends SRecordInstance> List<R> queryReferencing(
        I refed, SFieldReference<I> ref) {
        if (ref.getReferencedRecordMeta() != refed.getMeta())
            throw new SException.Error("Reference " + ref + " does not reference a " + refed);
        ArrayList<R> res = new ArrayList<R>();
          for (SRecordInstance rec: queryAllRecords()) {
              if (rec.getMeta() == ref.getRecordMeta()) {
                  if (rec.findReference(ref) == refed)
                      res.add((R)rec);
              }                  
          }
        return res;
    }

	public boolean isAttached() {
		return session != null;
	}
	
    
	// Should be done on each record, one by one
	public void clearDirtyList() {
		getDirtyRecords().clear();
	}
    
   /** Ordered list of SRecordInstances to flush and purge. */
   public List<SRecordInstance> getDirtyRecords() {
        return dirtyRecords;
    }

   /** Remove all records from cache and update list. */
    public void purge() {
        for (SRecordInstance ri : records.values()) {
            ri.destroy();
        }
        records.clear();
        clearDirtyList();
    }
    
   	public void destroy() {
        unbindSession(); //false);
        purge();
        records = null;
        dirtyRecords = null;
	}
    
    /** Clone the dataset.
     * Useful befor attempting to attach and commit to recover from errors.
     * (See LongTransactionTest).<p>
     * 
     * Curent implementation uses slow serialization alg, should really use clone()/able.
     */
    @Override public SDataSet clone() {
        Object original = this;
        SDataSet clone = null;
        try {
            //Increased buffer size to speed up writing  
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(original);
            out.flush();
            out.close();

            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
            clone =(SDataSet)in.readObject();
            in.close();
            bos.close();

            return clone;
        } catch (Exception es) {
            throw new SException.Error("While cloning " + original, es);
        }
    }

    /**
     * Savepoint, used to recover from an error, rollback a transaction but nevertheless detach the
     * dataset to send it back to the user for modification.
     * Should be called at the same time as ses.begin(), as in ses.begin(ds.savepoint());
     * Beware, this will make a copy of the record cache and dirty list, so this might be costly.
     * Use this when working with a dataSet that is coming from a user interface and that you
     * would like to give back to the user in case of an error.
     * Use it with caution when working on big dataSets.  
     */
//    private SDataSet savepoint() {
//    	spRecords = new LinkedHashMap<SRecordInstance,SRecordInstance>();
//    	spRecords.putAll(records);
//    	spDirtyRecords = new ArrayList<SRecordInstance>(20);
//    	spDirtyRecords.addAll(dirtyRecords);
//    	spNewDirtyRecords = new ArrayList<SRecordInstance>();
//    	for (SRecordInstance rinst : dirtyRecords) { 
//    		rinst.savepoint();
//    	}
//    	return this;
//    }
    
//    private void restoreSavepoint() {
//    	if (hasSavepoint()) {
//    		for (SRecordInstance rinst : spDirtyRecords) {
//    			if ( rinst.hasSavepoint() ) // record has been flushed
//    				rinst.restoreSavepoint();
//    		}
//    		for (SRecordInstance rinst : spNewDirtyRecords) {
//    			if ( rinst.hasSavepoint() ) // should have
//    				rinst.restoreSavepoint();
//    		}
//    		records = spRecords;
//    		dirtyRecords = spDirtyRecords;
//    		clearSavepoint();
//    	}
//    	else {
//    		throw new SException.Error("No savepoint for this dataSet. Inconsistent call.");
//    	}
//    }
    
//    public boolean hasSavepoint() {
//    	return (spRecords != null);
//    }
//    
//    private void clearSavepoint() {
//    	spDirtyRecords = null;
//    	spRecords = null;
//    	spNewDirtyRecords = null;
//    }

   ////////////////////// These should ideally all be package local, not accessed even from simpleorm.database ////////////////////

    /**
     * Create a new instance of this record, used by SSession during the fiddly doFindOrCreate etc.<p>
     */
    @Deprecated public <RI extends SRecordInstance> RI newInstanceNotInDataSet(SRecordMeta<RI> rmeta) {
        return rmeta.newRecordInstance();
    }
        
	@Deprecated public <RI extends SRecordInstance> RI findUsingPrototype(RI rinst) {
//        System.err.println(">ds findfromcache " + rinst + rinst.hashCode());
//        dumpDataSet();
		RI result = (RI) records.get(rinst);
		if (result != null)
			result.setWasInCache(true);
//        System.err.println("<ds findfromcache " + result);
		return result;
	}
    	
	@Deprecated public void pokeIntoDataSet(SRecordInstance rinst) {
		SRecordInstance prev = records.put(rinst, rinst);
		rinst.setDataSet(this);
	}


	void putInDirtyList(SRecordInstance rinst) {
		if (rinst.getDataSet() != this) {
			throw new SException.Error("Inconsistant DataSet");
		}
        if (rinst.dirtyRecordsIndex != -1) 
            throw new SException.InternalError("Already in dirty list " + rinst);
		this.getDirtyRecords().add(rinst);
		rinst.dirtyRecordsIndex =  getDirtyRecords().size() -1;
//		if (hasSavepoint())
//			spNewDirtyRecords.add(rinst);
	}
	
	void removeFromDirtyList(int index, SRecordInstance rinst) {
		if (rinst.getDataSet() != this) {
			throw new SException.Error("Inconsistant DataSet");
		}
		if (getDirtyRecords().get(index) != rinst) {
			throw new SException.Error("Inconsistant updateList index");
		}
		this.getDirtyRecords().set(index, null);
	}
	
    public SLog getLogger() {
         if (logger == null) {
             logger = SLog.newSLog(); // eg. after unserialize.
             logger.setSession(getSession());
         }
        return logger;
    }
    
//    /**
//     * Create a new instance of this record, unattached to a dataset.
//     * Used for backward compatibility, records normally ONLY live within
//     * a DataSet.  
//     * Detached records are marked as new.
//     * @See SDataSet#attach
//     */
//    @Deprecated static public <RI extends SRecordInstance> RI createDetachedInstance(SRecordMeta<RI> rmeta) {
//        RI inst = rmeta.newRecordInstance();
//        inst.setNewRow(true);
//        return inst;
//    }
//    
//    /** Attach rinst into the dataset, checking that it is not already there. 
//     * References should be null.
//     * Only for compatibility with 2.*, normally records only normal created within data sets.
//     */
//   	@Deprecated public void attach(SRecordInstance rinst) {
//		SRecordInstance prev = records.put(rinst, rinst);
//        if (prev != null) 
//            throw new SException.Error("Record already in dataset " + prev);
//		rinst.setDataSet(this);
//        if (rinst.isDirty()) // normal
//            putInDirtyList(rinst);
//	}

    
    
   	/**
	 * Dumps out the entire cache of records for this connection. For debugging
	 * wierd bugs only.
	 */
	public void dumpDataSet() {
		getLogger().message("DumpDataSet " + queryAllRecords().size() + " records.");
		for (SRecordInstance ri : queryAllRecords()) {
			getLogger().message("    " + ri + ri.hashCode());
            if (ri.getDataSet() != this)
                throw new SException.InternalError("Wrong DataSet "+ ri + ri.getDataSet());
        }
        //getLogger().message("    Dirty " + dirtyRecords);
	}

    @Override public String toString() {
        return "[SDataSet " + getSession() + "]";
    }
    
   //////////////////////////// Empty get/set /////////////////////////////////
   
    public SSessionI getSession() {
        return session;
    }

//    public void setSession(SSessionI session) {
//        setSession(session, false);
//    }

    public void bindSession(SSessionI session){ //, boolean savepoint) {
    	if (session == null)
    		throw new SException.InternalError("Trying to set a null session on "+this);
        // make reuse of a destroyed SDataSet failfast (could also make it possible, but
        // it would encourage bad user code... To reuse a dataset, you should detach it.
        if (records == null || dirtyRecords == null)
        	throw new SException.Error("You cannot reuse a destroyed dataSet");
        this.session = session;
        // savepoint if asked
//        if (savepoint)
//        	this.savepoint();
    }
    
    public void unbindSession() { // boolean restore) {
//    	if (restore) {
//    		if ( ! hasSavepoint())
//    			throw new SException.InternalError("Attempt to restore a non existing savepoint");
//    		restoreSavepoint();
//    	}
    	this.session = null;
//    	clearSavepoint();
    }
    
}

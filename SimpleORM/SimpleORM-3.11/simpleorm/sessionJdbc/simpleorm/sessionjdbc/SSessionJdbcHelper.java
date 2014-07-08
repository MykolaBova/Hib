package simpleorm.sessionjdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SRecordInstance.BrokenOptimisticLockException;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;

/**
 * Internal class, One to One with SSession, this contains database access related routines.
 * @author aberglas
 */
class SSessionJdbcHelper {
    SSessionJdbc session;

    SSessionJdbcHelper(SSessionJdbc session) {
        this.session = session;
    }

    
        	// Looks in current dataSet for a record matching the given keys. Creates one if necessary and if create==true
	public <RI extends SRecordInstance> RI  doFindOrCreate(SRecordMeta<RI> rmeta, SFieldScalar[] selectList, SQueryMode queryMode, boolean mayCreate, Object[] keys) {
		session.checkBegunThread();

        boolean readOnly = queryMode.equals(SQueryMode.SREAD_ONLY);
		
		if (getLogger().enableFields())
			getLogger().fields("findOrCreate " + rmeta + " " + SUte.arrayToString(keys));	
        
        RI instance = session.dataSet.findOrCreate(rmeta, keys);
        
        if (needsRequery(instance, readOnly, selectList)) {
        
			instance = findInDatabase(instance, // != null ? instance : keyInstance, // If requery
					queryMode, selectList, readOnly);
			
			if (getLogger().enableQueries())
				getLogger().queries("findOrCreate: " + instance  + " (from database)"
						+ (instance.isNewRow() ? " New Row" : " Existing Row"));
			
			if (instance.isNewRow()) {
				if (mayCreate) {
					instance.validatePrimaryKeys(); // Could throw SValidationException
					instance.setDirty(true); // a new row is dirty, should be inserted on flush/commit
				}
				else {
					// If we return null, instance has nothing to do in the dataset record list.
					// So we can create instance with same key later
					session.dataSet.removeRecord(instance);
					return null;
				}
			} else 
                instance.doQueryRecord();                
		}

		if (instance.getDataSet() != session.dataSet)
			throw new SException.InternalError("Inconsistent DataSet "	+ instance + instance.getDataSet() + this);

		if (instance.getMeta() != rmeta)
			throw new SException.InternalError("Found " + instance	+ " instead of " + rmeta);
		
		return instance;
	}

	protected <RI extends SRecordInstance> RI findInDatabase(RI instance, SQueryMode queryMode, SFieldScalar[] selectList,
			boolean readOnly) {

		ResultSet rs = null;
		session.checkBegunThread();
		session.statistics.incrementNrFindInDatabase();

		boolean existing = false;
		if (queryMode.equals(SQueryMode.SASSUME_CREATE)) {
			existing = false;
		} else {
			// Determine the SQL Query
			String qry = session.getDriver().selectSQL(
					selectList, instance.getMeta(), instance.getMeta().getPrimaryKeys(), // for WHERE
					null, queryMode == SQueryMode.SFOR_UPDATE);

			try {
				// Define the primary key query parameters
				List<Object> qparams = new ArrayList<Object>(6);
				for (SFieldScalar key : instance.getMeta().getPrimaryKeys()) {
					Object value = instance.getRawArrayValue(key);
					qparams.add(key.writeFieldValue(value));
					instance.defineInitialValue(key); // needed in case key not in explicit select list.
				}

				if (getLogger().enableQueries())
					getLogger().queries("findOrCreate querying '" + substituteToString(qry, qparams) + "'...");

				// Execute the Query
				rs = session.executeQuery(0L, qry, qparams);

				// Retrieve the result.
				if (session.rsNext(rs)) {
					retrieveRecord(instance, selectList, 1, rs, readOnly, true);
					existing = true;
					if (session.rsNext(rs))
						throw new SException.Jdbc("Primary key not unique " + instance);
				}
			} finally {
				SSessionJdbc.closeResultSetAndStatement(rs);
			}

		}
		instance.setNewRow(!existing);
		instance.setReadOnly(readOnly); // we only got to findInDatabase because it was not in the cache
		return instance;
	}

    boolean needsRequery(SRecordInstance rinst, boolean readOnly, SFieldScalar[] selectList) {
        
    	if (rinst.isNewRow())
        	return false;
        
		boolean requery = !readOnly && rinst.isReadOnly();
		for (SFieldMeta selF : selectList) {
            //if (selF.getRecordMeta() == rinst.getMeta()) // Not needed anymore as we only pass in the right selectList for the table
			requery = requery || ! rinst.isValid(selF);
            if (requery)
            	break;
		}
        if (requery && rinst.isDirty())
            throw new SException.Error("Need to flush dirty before requery " + rinst);
        
		return requery;
	}

    /**
     * Flush just one instance to the database. Normally called by
     * <code>SSession.flush()</code> in reponse to <code>commit</code>
     * but can also be called expicitly if the update order needs to be
     * modified. This method does nothing unless the record is dirty.
     * <p> ## This should really utilize the new batching techniques if the JDBC
     * driver supports them. This would substantially minimize the nr of round
     * trips to the server.
     * <p>
     */
    void flush(SRecordInstance instance) {

    	if (!instance.isDirty()) return; // flushing twice OK
    	try {
    		session.statistics.incrementNrFlushRecord();
    		SRecordMeta<?> meta = instance.getMeta();
    		if (getLogger().enableUpdates())
    			getLogger().updates("Flushing '"+ instance+ "'"+ (!instance.isDirty()?" ?Not Dirty? ":"") +
    					(instance.isNewRow() ? "INSERT" : instance.isDeleted() ? "DELETE":"UPDATE"));

    		if ( ! session.hasBegun())
    			throw new SException.Error("Inconsistent Connections " + instance+ instance.getDataSet() + this);
    		if (instance.getDataSet() != session.getDataSet())
    			throw new SException.Error("Inconsistent Connections " + instance+ instance.getDataSet() + this);

    		// Generate keys if needed
    		SGenerator theGenerator = null; // maybe needed later for postUpdate.
    		if (instance.isNewRow() && !instance.isDeleted())
    			for (SFieldScalar pkey: meta.getPrimaryKeys()) {
    				Object pkval = instance.getObject(pkey);
    				if (pkval == null) {
    					SGenerator.setNewGenerator(pkey);
    					theGenerator =  pkey.getGenerator();
    					if (theGenerator != null)
    						theGenerator.preUpdateWithGeneratedKey(session, instance);
    					else
    						throw new SException.Error("No generator for null primary key " + pkey);
    				}
    			}

    		if ( ! instance.isDeleted()) // do not try to validate deleted instance (fields are not accessible)
    			instance.doValidateRecord();

    		// / Determine fields to be updated.
    		ArrayList<SFieldScalar> fieldList = new ArrayList<SFieldScalar>(meta.getFieldMetas().size());
    		if (!instance.isDeleted()) {
    			for (SFieldScalar sfm : meta.getAllScalarFields()) {
					boolean upd = (instance.isNewRow() && sfm.isPrimary());
					upd = upd || (instance.isDirty(sfm)) ;
					// Primarly keys are marked dirty if retrieved via findOrCreate.
					if (upd)
						fieldList.add(sfm);
    			}
    		}

    		// If the database needs updating...
    		if (!(instance.isNewRow() && instance.isDeleted())
    				&& (instance.isDeleted() || fieldList.size() > 0)) {

    			// / Determine the SQL Query
    			String qry = null;

    			// Allocate max size, Object[] more efficient than ArrayList (??)
    			Object[] keyMetaValues = new Object[meta.getFieldMetas().size()];
    			ArrayList<SFieldScalar> keyMetas  = keyFieldMetas(instance, keyMetaValues);

    			if (instance.isNewRow())
    				qry = session.getDriver().insertSQL(fieldList, meta);
    			else if (instance.isDeleted())
    				qry = session.getDriver().deleteSQL(meta, keyMetas, instance, keyMetaValues);
    			else
    				qry = session.getDriver().updateSQL(fieldList, meta, keyMetas,	instance, keyMetaValues);
    			// ## could cache the creation of these queries. But pretty
    			// fast anyway. Batching them is much more important.

    			// / Prepare the statement
    			Connection con = session.jdbcConnection;
    			PreparedStatement ps = null;
    			ps = flushPreparedStatement(con, qry, instance);

    			// / setObject(,)s the new body values for INSERT or UPDATE
    			// statement
    			int jx = 0; // JDBC setString
    			ArrayList<Object> parameters = new ArrayList<Object>(20); // Just used for tracing
    			if (!instance.isDeleted()) {
    				for (SFieldScalar fieldMeta : fieldList) {
    					try {
    						jx++;
    						parameters.add(instance.getRawArrayValue(fieldMeta));
    						Object value = instance.getRawArrayValue(fieldMeta);
    						if (value == null && fieldMeta.isPrimary() && theGenerator==null)
    							throw new SException.Error("Null primary key not set (after createWithNullKey) " + instance);                        
    						if (value == null)
        						// FieldMeta knows about its jvaSqlType. Otherwise, some jdbc drivers complain (eg. Postgresql)
    							ps.setNull(jx, fieldMeta.javaSqlType());
    						else
    							// writeFieldValue converts True to "Y" etc.
    							fieldMeta.writeFieldValue(ps, jx, instance.getRawArrayValue(fieldMeta));
    					} catch (Exception se) {
    						throw new SException.Jdbc("Setting Values " + instance + "'" + qry + "' Field " + fieldMeta, se).setInstance(instance);
    					}
    				}
    			}
    			// / setObject(,)s the key for UPDATE or DELETE statement.  Includes optimisitic fields.
    			if (!instance.isNewRow()) {
    				for (int kx = 0; kx < keyMetas.size(); kx++) {
    					SFieldScalar fieldMeta =  keyMetas.get(kx);

    					Object value;
    					try {
    						value = fieldMeta.writeFieldValue(instance.getInitialValue(fieldMeta));
    					} catch (Exception se) {
    						throw new SException.Jdbc("Converting optimistic field value '"
    								+ instance + " '" + qry + "' " + jx	+ keyMetas.get(kx) + " = "
    								+ instance.getRawArrayValue(fieldMeta), se).setInstance(instance);
    					}
    					if (value != null) { // ###  Is this correct: else generates IS NULL
    						jx++;
    						if (getLogger().enableFields())
    							getLogger().fields("Where " + instance + jx + keyMetas.get(kx) + " = " + value);
    						try {
    							parameters.add(value);
    							ps.setObject(jx, value);
    						} catch (Exception se) {
    							throw new SException.Jdbc("Setting  " + instance
    									+ " '" + qry + "' " + jx + keyMetas.get(kx)
    									+ " = " + value, se).setInstance(instance);
    						}
    					}
    				}
    			}

    			// Logging
    			if (getLogger().enableQueries())
    				getLogger().queries("FlushSQL " + substituteToString(qry, parameters));

    			// / Execute the Query
    			int result = 0;
    			result = flushExecuteUpdate(ps, qry, instance);
    			if (result == 0)
    				throw new BrokenOptimisticLockException(instance);
    			//else throw new SException.InternalError("Rows Updated " + result + " != 1 " + instance);
    			// MS SQL includes any records updated by db triggers in the count.
    			try {
    				ps.close();
    			} catch (Exception cl1) {
    				throw new SException.Jdbc("Closing " + instance, cl1).setInstance(instance);
    			}

    			if (theGenerator!=null) {
    				theGenerator.postUpdateWithGeneratedKey(session, instance);
    			}

    			for (SFieldScalar cfld: fieldList)
    				instance.defineInitialValue(cfld);                        

    		} // Database needs updating

    		// / Do not clear field dirties as complicates rollbackandDetachDataSet.
    		instance.setDirty(false);
    		instance.setNewRow(false); // Any subsequent flush should be an UPDATE.
    		// If was deleted, remove from cache. Pk can then be reused.
    		if (instance.isDeleted()) {
    			instance.getDataSet().removeRecord(instance);
    		}
    		// if newRow && deleted OK to leave in updateListIndex.

    		return;
    	} catch (SException sex) {throw sex;}
    	catch (Exception ex) {
    		throw new SException.Jdbc("While flushing " + instance, ex).setRecordInstance(instance);
    	}
    } // flush

    // Extract into separate method to make profiling easier.
    private int flushExecuteUpdate(PreparedStatement ps, String qry, SRecordInstance instance) {
        int result;
        try {
            result = ps.executeUpdate();
        } catch (Exception rsex) {
            throw new SException.Jdbc("Executing " + qry + " for " + instance, rsex).setInstance(instance);
    }
        return result;
    }
    private PreparedStatement flushPreparedStatement(Connection con, String qry, SRecordInstance instance) {
        PreparedStatement ps;
        try {
            ps = con.prepareStatement(qry); // Let the JDBC driver cache these.
        } catch (Exception psex) {
            throw new SException.Jdbc("Preparing " + instance + "'" + qry + "'", psex).setInstance(instance);
        }
        return ps;
    }

    /**
	 * Returns the list of fields that are used as part of the optimistic
	 * locking. Includes Primary key fields, any valid fields that are dirty,
	 * but not references.  Adds their values to keyMetaValues.
     * 
     * (Internal to SimpleORM only.)
	 */
	ArrayList<SFieldScalar> keyFieldMetas(SRecordInstance instance, Object[] keyMetaValues) {
		ArrayList<SFieldScalar> res = new ArrayList<SFieldScalar>();
		int addCounter = 0;
		for (SFieldMeta fld : instance.getMeta().getFieldMetas()) {
			if (fld instanceof SFieldReference) continue;
			SFieldScalar sclFld = (SFieldScalar) fld;

            if (sclFld.isNotOptimisticLocked() || !instance.isValid(fld)) continue;
            if ( (instance.isDirty(fld) && !instance.isNewRow()) || ((SFieldScalar)fld).isPrimary()  ) {
					res.add(sclFld);
					keyMetaValues[addCounter++] = instance.getInitialValue(fld);
			}
		}
		return res;
	}

    void retrieveRecord(SRecordInstance inst, SFieldScalar[] selectList, int beginIndex, ResultSet rs, boolean readOnly,  boolean checkPrimaryKey) {
    	
		Object qvalue = null;
		for (SFieldScalar fMeta : selectList) {
			qvalue = null;
			try {
				qvalue = fMeta.queryFieldValue(rs, beginIndex);
			} catch (Exception ge) {
				throw new SException.Jdbc("Getting Field " + beginIndex + " from " + this, ge);
			}
			if (checkPrimaryKey && fMeta.isPrimary()) {
    			// ## This is dubious. What if they are the same except for trailing spaces?
        		// This test will pass, but in general the records will not be
            	// considered the same as we do not generally trim spaces.
                // See SRecordInstance.getString.
				if (qvalue == null || !SUte.trimStringEquals(qvalue, inst.getRawArrayValue(fMeta)))
					throw new SException.InternalError("Bad PKey "
							+ qvalue.getClass() + " '" + qvalue	+ "' !equal() '"
							+ inst.getRawArrayValue(fMeta).getClass() + "' "	+ inst.getRawArrayValue(fMeta));
			}
			inst.setRawArrayValue(fMeta, qvalue);
            inst.defineInitialValue(fMeta);                        
			inst.setReadOnly(readOnly);
			beginIndex++;
		}

		// / Update instance flags
		inst.setReadOnly(readOnly);
	}

    SLog getLogger() {return session.getLogger();}
    
    SDriver getDriver() { return session.getDriver(); }
    
    /**
	 * Substitute '?' in the sql query with the values in the parameters array.
	 * Used to create meaningful SQL strings for logging purposes ONLY.
	 */
	public static String substituteToString(String qry, List<Object> parameters) {
		StringBuffer buffer = new StringBuffer();
		int fromIndex = 0;
		for (int ii = 0; ii < parameters.size(); ii++) {
			int index = qry.indexOf('?', fromIndex);
			buffer.append(qry.substring(fromIndex, index));
			buffer.append("?=" + parameters.get(ii) + "?");
			fromIndex = index + 1;
		}

		buffer.append(qry.substring(fromIndex));

		return buffer.toString();
	}

}

package simpleorm.sessionjdbc;

import static simpleorm.sessionjdbc.SDriver.OffsetStrategy.JDBC;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import javax.sql.DataSource;

import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SQuery;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SQueryResult;
import simpleorm.dataset.SQueryTransient;
import simpleorm.dataset.SRecordGeneric;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SRecordTransient;
import simpleorm.dataset.SSelectMode;
import simpleorm.dataset.SSessionI;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;
import simpleorm.utils.SUte;


/*
 * Copyright (c) 2002 Southern Cross Software Queensland (SCSQ).  All rights
 * reserved.  See COPYRIGHT.txt included in this distribution.
 */

/**
 * Performs all the interactions between a DataSet and a JDBC Connection.
 * <p>
 * 
 * SSessionJdbc.open creates a new session and associates it with the current thread.<p>
 * 
 * All transactions must start with a {@link #begin}, which normally creates a 
 * new DataSet to store records in.   They with either a
 * {@link #commit} or a {@link #rollback} which normally destroy the DataSet and
 * all records in it. <code>commit</code> also
 * {@link #flush}es any outsanding changes.<p>
 * 
 * Methods are then provided to retrieve recrods from the databse by primary key or general query.  <p>
 * 
 * There are also convenience method for performing raw SQL queries should that be required.<p>
 */

public class SSessionJdbc extends SSessionI {

	
	///////////// Factory + thread management //////////////////
	
	private static ThreadLocal<SSessionJdbc> threadSConnection = new ThreadLocal<SSessionJdbc>();
	private static long nrCons = 0; // ever, in JVM.
	private DataSource dataSource; // may be null.
	private SDriver sDriver = null;
    Thread thread = Thread.currentThread();
    final SQueryMode DEFAULTQM = SQueryMode.SBASIC;
    SStatistics statistics = new SStatistics(this);
    private boolean flushed = false;

    
	/**
	 * Creates a SimpleORM connection based on the JDBC connection, and attaches it to the current thread.
	 * <p>
	 * 
	 * connectionName is a simple name that can be used to identify this
	 * connection in log traces etc. Eg. the session ID plus the user name. But
	 * keep it short for logging.
	 * <p>
	 * 
	 * The driver is usually determined automatically (if null) based on the
	 * connection, but if it is overriden make sure to create a new driver instance
	 * for each connection.
	 * <p>
	 */
	public static SSessionJdbc open(Connection con, String connectionName, SDriver driver) {
		SSessionJdbc ses = new SSessionJdbc();
		ses.innerOpen(con, connectionName, driver);
		return ses;
	}
	/** Convenience method if a DataSource is used.   Just calls source.getConnection().  */
	public static SSessionJdbc open(DataSource source, String connectionName, SDriver driver) {
		Connection con = null;
			try {
				con = source.getConnection();
			} catch (Exception ex) {
				throw new SException.Jdbc("Opening " + source, ex);
			}
			SSessionJdbc ses = open(con, connectionName, driver);
			ses.dataSource = source;
			return ses;
	}
	
	public static SSessionJdbc open(DataSource source, String connectionName) {
		return open(source, connectionName,  null);
	}
	public static SSessionJdbc open(Connection con, String connectionName) {
		return open(con, connectionName,  null);
	}
	
    	   /**
	 * Allows SSession to be subclassed. connection should normally be null,
	 * in which case conf.obtainPrimaryJDBCConnection is used to open it.
	 * (rawConnection and sDriverName are deprecated.)<p>
        * 
        * We use DataSource rather than just Connection to allow a driver to create
        * a second connection to generate keys etc.  But that has never been used, and
        * some old code that potentially supported it has been removed.<p>
	 */
	protected void innerOpen(Connection con, String connectionName, SDriver driver) {
		name = connectionName;
        
        synchronized (SSessionJdbc.class) {
             conNr = ++nrCons;
        }

		SSessionJdbc badscon = getThreadLocalSession();
		if (badscon != null)
			throw new SException.Error("Thread's SSession already open "	+ badscon);

		try {
			if (con == null || con.isClosed())
				throw new SException.Error("Connection " + con	+ " is not open.");
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		}

		jdbcConnection = con;
		associateWithThread();

     	sDriver = driver == null ? SDriver.newSDriver(con) : driver;
        sDriver.session = this;
		try {
			if (jdbcConnection.getAutoCommit())
				jdbcConnection.setAutoCommit(false);
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		}
		if (getLogger().enableDebug()) {
			String jvsn = Package.getPackage("java.lang").getImplementationVersion();
			getLogger().connections("Attached Connection " + this + " SimpleORM "
				+ SUte.simpleormVersion() + " jdk " + jvsn);
		}
	}
	
	protected void associateWithThread() { // Potentially overriden in SConnectionEJB
		threadSConnection.set(this);
	}
	protected void dissassociateFromThread() { // Potentially overriden in SConnectionEJB
		threadSConnection.set(null);
	}

	/**
	 * Retrieve or create the SSession associated with this "Context".
	 * Normally this just means (indirectly) call getThreadedConnection which
	 * finds the connection associated with this thread. <p>
     * 
     * (In extras there is an unsupported 
	 * EJB.SConnectionEJB class which dispached to
	 * getTransactionConnection instead.)
	 * <p>
	 * 
	 * It may or may not be begun. May be used to set properties for the
	 * connection, which persist between transactions but not between attach
	 * ments.<p>
     * 
     * This is never used within SimpleOrm itself, it is just a convenience method for users.<p>
	 */
	static public SSessionJdbc getThreadLocalSession() {
		return threadSConnection.get();
	}
    	
	/**
     * Main method for closing a session.<p>
     * 
	 * Closes the JDBC connection and then calls
	 * <code>detachWithoutClosing</code> to detach the SimpleORM connection
	 * from the current thread. Should usually be put in a finally clause. No
	 * error if already detached or closed so safe in finally clauses.
	 * <p>
	 */
	public void close() {
            closeCon(jdbcConnection);
            closeSession();
    }
	private void closeCon(Connection con) {
        boolean isOpen = false;
		try {
			isOpen = con != null && !con.isClosed();
		} catch (Exception ex) {
			throw new SException.Jdbc("isClosed ", ex);
		}
		if (isOpen) {
			try {
                con.rollback(); // If transaction open and
												// exception, esp. for DB2.
            } catch (Exception ex) {
                throw new SException.Jdbc("Error rollback " + con, ex);
            }
            finally {
            	try {
            		con.close();
            	} catch (Exception ex) {
            		throw new SException.Jdbc("Error closing " + con, ex);
            	}
            }
        }
    }
	private void closeSession() {
		getLogger().connections("Detaching Connection " + this);
		if (hasBegun()) {
			// throw new SException.Error(...)
			// Does not behave well in finally clauses -- if the try fails
			// this then throws another "not committed" exception which masks
			// the first. No known work arround in Java!
			getLogger().error(
                "Transaction has unflushed updated records.  This is normally caused by an unrelated Exception throwing to the finally block in which case ignore this message.  But if no other exception then a commit() probably missing.\n");
		}
		destroyAll(); // For J2EE case where flush/detach used.
		jdbcConnection = null;
		dissassociateFromThread();
	}
    	
	/**
	 * Enables the SimpleORM connection to be disassociated with the current
	 * thread, and then possibly attached to another thread.   
     * It also enables multiple sessions to be used within the same thread
     * without getting mixed thread exceptions.
     * (But not multiple threads to use the same session concurrently.)<p>
	 * 
	 * Rarely used, normally better to just create a session in the appropriate thread.
     * Both Swing and EJBs can be used without doing this.
	 * <p>
	 * 
	 * Do not not do it unless you really have to.
	 */
	public SSessionJdbc detachFromThread() {
		SSessionJdbc session = threadSConnection.get();
		getLogger().connections("unsafeDetachFromThread " + session);
        if (session == null || session != this)
            throw new SException.Error("Session is not associated with thread " + session + this);
		threadSConnection.set(null);
        thread = null;
		return session;
	}
    
    /**
	 * Re-attacheds a detached connection to the current thread.
	 * 
	 * @see #detachFromThread
	 */
	public void attachToThread() {
		SSessionJdbc scon = threadSConnection.get();
		if (scon != null)
			throw new SException.Error("This thread already has connection " + scon);
		threadSConnection.set(this);
        thread = Thread.currentThread();
		getLogger().connections("unsafeAttachToThread " + this);
	}

	        
	///////////////// Transactions  ///////////////

    SSessionJdbcHelper sessionHelper = new SSessionJdbcHelper(this);
    
	Connection jdbcConnection = null;
	
	/**
	 * The getMetaData() url. Saved here so that toString can display it even on
	 * a closed connection. NOT USED anymore.
	 */
	//private String url = null;
	/** Only to identify connection for toString() */
	private long conNr;
	/**
	 * Provided by user to identify thread, eg. HTML session id. Keep it short.
	 */
	String name = "";
	/**
	 * The SDataSet to which this Session is providing a SQueryEngine and
	 * and database persistence service.
	 */
    SDataSet dataSet = null;

    /** No direct creation of Connections. */
	protected SSessionJdbc() {
	}

	/** Note that this will be null if the session was created by a jdbc Connection instead of a DataSource. */
	public DataSource getDataSource() {
		return dataSource;
	}
	
	/**
	 * Start a new transaction. A JDBC connection must already be attached to
	 * this thread, and must not be mid transaction.
	 */
	public void begin() {
		begin(new SDataSet());
	}
    
    /** Begin a new transaction based on a previously detached data set */
	public void begin(SDataSet ds) {
		
		// Savepoints have been removed, at least for now. */
		
//		begin(ds, false);
//	}
//	
//	/** Begin a new transaction based on a previously detached data set.
//	 * Enabling savepoint will allow to recover and detach the dataset on rollback. It will
//	 * make a savepoint copy of every dirty record, and of the dataSet internal structures.
//	 * Common usage would be to enable savepoint when the dataset comes from the UI
//	 * and should be send back to user on error (on a SValidation exception for example).
//	 * One would disable savepoint when dataSet comes from a batch job that generates
//	 * (possibly a lot of) data and will take care of error conditions (regenerating data for
//	 * example).
//	 * 
//     * @param ds
//     * @param savepoint true to allow rollbackAndDetachDataSet, at the price
//     *    of some overhead on begin and when making new records dirty.
//     *    false: don't create a dataSet savepoint on begin, at the price of not
//     *    being able to recover the dataSet on rollback.
//     */
//    public void begin(SDataSet ds, boolean savepoint) {
        checkThread();
		if (hasBegun())
			throw new SException.Error("Transaction already Begun.");
		if (dataSet != null)
			throw new SException.InternalError("Non empty existing session dataset/update list.");
        if (ds.getSession() != null)
            throw new SException.Error("DataSet already has a session " + ds + ds.getSession());
        ds.bindSession(this); //, savepoint);
        this.dataSet = ds;
        this.flushed = false;
		getLogger().connections("Begun Connection " + this);
    }
    /** Transaction is active between begin and commit. */
    
    public boolean hasBegun() {
		return dataSet != null;
	}
    
        /** Simple check to avoid threading bugs in user code.  
     * Might refine later.
     * Also checks thread has begun.
     */
	void checkBegunThread() {
		checkThread();
        if (!hasBegun())
            throw new SException.Error("Transaction already committed, need begin.");
	}
	void checkThread() {
		if (thread != null // Eg. if detached.
            && thread != Thread.currentThread()) 
            throw new SException.Error("Session created on thread " + thread + " but now used in thread " + Thread.currentThread());
    }

	/**
	 * Flush and purge the transaction cache to the database, and then commit
	 * the transaction. Note that this is the only way that a transaction should
	 * be commited -- do not use JDBC commit directly. Once commited,
	 * <code>begin()</code> must be used to start the next transaction.<P>
     * 
     * Also closes any open SResultSets and SPreparedStatements<p>
	 */
	public void commit() {
        flush();
        innerCommit();
		destroyAll(); // Destroy dataSet here <-----scon.dataSet.uncommittedFlushes = false;
		dataSet = null; // do it in detroyAll()
	}

    public SDataSet commitAndDetachDataSet() {
        flush();
        innerCommit();
        SDataSet ds = dataSet;
        ds.unbindSession(); //false);
        dataSet = null;
		return ds;
	}
    
    /**
     * This method is used to detach a dataset that has not been flushed
     * during the session. It will rollback the underlying jdbcConnection.
     * Use case include manual findOrCreate over multpiple sessions.
     * <em>Beware</em>, trying to use this method on a flushed dataset will throw an exception
     * without commiting or rolling back. So the dataset will not be detached, and the
     * session won't be commited or rolled back. You must catch the exception and do
     * whatever you is sensible for you (either commit or rollback).
     * @return
     */
    public SDataSet detachUnflushedDataSet() {
    	if (flushed)
    		throw new SException.Error("Trying to detach a flushed DataSet. **That has not been committed or rolled back**");
        SDataSet ds = dataSet;
        ds.unbindSession(); //false);
        dataSet = null;
        try {
			jdbcConnection.rollback();
		} catch (SQLException e) {
			throw new SException.Jdbc("While releasing the transaction ",e);
		}
		return ds;
	}
    
    private void innerCommit() {
        statistics.incrementNrTransactions();
        this.flushed = false;
        try {
			jdbcConnection.commit();
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		}        
		getLogger().connections("Committed Connection " + this);
    }
    
	/**
	 * Purges the cache and rolls back the transaction. Any uncommited updates
	 * to the database are also rolled back.<p>
     * 
     * Also closes any open SResultSets and SPreparedStatements<p>

	 */
	public void rollback() {
		innerRollback();
		destroyAll(); // Destroy dataSet here <-----scon.dataSet.uncommittedFlushes = false;
		dataSet = null; // do it in detroyAll()
	}
	
	public SDataSet rollBackAndDetachDataSet() {
		innerRollback();
        SDataSet ds = dataSet;
        ds.unbindSession(); //true);
        this.dataSet = null;
		return ds;
	}
	
	private void innerRollback() {
		checkBegunThread();
        statistics.incrementNrTransactions();
        this.flushed = false;
		try {
			jdbcConnection.rollback();
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		}
		getLogger().connections("Rolled Back Connection " + this);	
	}

    /**
     * Set the transaction isolation mode.
     * SimpleORM does not normally change the isolation mode, but consider setting it to 
     * Connection.TRANSACTION_SERIALIZABLE to achieve greater reliability in many databases.
     * @param mode as defined by java.sql.Connection.setTransactionIsolation
     */
    public void setTransactionIsolation(int mode) {
        try {
            jdbcConnection.setTransactionIsolation(mode);
        } catch (SQLException se) {throw new SException.Jdbc("While setting isolation mode " + mode, se);}
    }
	
	
	/** Flush all records of tables in this transaction to the database. */
    public void flush() {
		checkBegunThread();        
		for (int rx=0; rx<dataSet.getDirtyRecords().size(); rx++) {
            // Need explicit index to avoid concurrent update errors (flush(ri) nulls entry in the list).
            SRecordInstance ri = dataSet.getDirtyRecords().get(rx);
			if ( ri != null)  // Could have been manually flushed.
				flush(ri);
		}
		dataSet.clearDirtyList();
	}
	/**
	 * Flush this instance to the database. Normally called by
	 * <code>SSession.flush()</code> in reponse to <code>commit</code>
	 * but can also be called expicitly if the update order needs to be
	 * modified. This method does nothing unless the record is dirty.
	 * <p>
	 * 
	 * @see SSession#flush
	 */
	public void flush(SRecordInstance instance) {
		checkBegunThread();
		sessionHelper.flush(instance);
		this.flushed = true;
	}
	
	/**
	 * Flushes this record instance to the database, removes it from the
	 * transaction cache, and then destroys the record so that it can no longer
	 * be used. Any future <code>findOrCreate</code> will requery the data
	 * base and produce a new record.
	 * <p>
	 * 
	 * This is useful if one wants to do raw JDBC updates on the record, and be
	 * guaranteed not to have an inconsistent cache. (Where bulk updates can be
	 * used they are several times faster than updates made via the JVM -- see
	 * the benchmarks section in the white paper.)
	 * <p>
     * It can also be useful to force a requery of records if optmistic locks are being broken, 
     * although care must be taken not to rely on values returned by a outdated query.
	 * 
	 * @see SSession#flush
	 */
	public void flushAndPurge(SRecordInstance ri) {
		checkBegunThread();
		sessionHelper.flush(ri);
		dataSet.removeRecord(ri);
	}

	/**
	 * Flushes and Purges all record instances. Can be used before a raw JDBC
	 * update to ensure that the cache remains consistent after the query.
	 * 
	 * @see SRecordInstance#flushAndPurge
	 * @see SRecordMeta#flushAndPurge
	 */
	public void flushAndPurge() {
		checkBegunThread();
		flush();
		if (dataSet == null) return;
		dataSet.purge();
	}

	/**
	 * Destroy all records in all tables so that they cannot be used again and
	 * to encourage garbage collection.
	 */
	void destroyAll() {
		if (dataSet == null) return;
		dataSet.destroy();
		dataSet = null;
	}

	@Override public String toString() {
		return "[SS " + conNr + "." + name +"]";
	}

    ///////////////  Queries ////////////////////
    
    /**
     * Query, but without first flushing the cache, not normally used.
     * @see query
     */
    public <RI extends SRecordInstance> SQueryResult<RI> queryNoFlush(SQuery<RI> qry) {
        checkBegunThread();
 		SQueryExecute<RI> qXeq = getDriver().queryExecuteFactory(this, qry);
		return qXeq.executeQuery();
	}
	/**
	 * Execute the query and return a list of records.
     * First always flushes any dirty records to the database.
     * (Flush but not commit.)
	 */
    public <RI extends SRecordInstance> SQueryResult<RI> query(SQuery<RI> qry) {
        flush();
        return queryNoFlush(qry);
	}	
//    /** Same as query but throws an exception if there is more than one record returned. */
//	public <RI extends SRecordInstance> RI queryOnlyRecord(SQuery<RI> qry) {
//		List<RI> res = query(qry);
//		if (res.size() == 0)
//			return null;
//		if (res.size() == 1)
//			return res.get(0);
//		throw new SException.Error("More than one record in result set (" + res.size() + ") " + qry);
//	}

	/////////////////  AggregateQueries //////////////
	/**
     * AggQuery, but without first flushing the cache, not normally used.
     * @see query
     */
    public SQueryResult<SRecordTransient> queryTransientNoFlush(SQueryTransient qry) {
        checkBegunThread();
 		SQueryTransientExecute<?> qXeq = getDriver().queryExecuteFactory(this, qry);
		return qXeq.executeAggregateQuery();
	}
	/**
	 * Execute the query and return a list of maps.
     * First always flushes any dirty records to the database.
     * (Flush but not commit.)
	 */
    public SQueryResult<SRecordTransient> queryTransient(SQueryTransient qry) {
        flush();
        return queryTransientNoFlush(qry);
    }	

    ////////////////// findOrCreate //////////////////
    
    /** Find a record or create one if it does not already exist.  
     * Retrieve selectList fields, use queryMode.
     */
  	@Override public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, SFieldScalar[] selectList, SQueryMode queryMode, Object... keys) {
		//System.err.println("foc sl " + SUte.arrayToString(selectList) + " keys " + SUte.arrayToString(keys));
        return sessionHelper.doFindOrCreate(rmeta, selectList, queryMode, true, keys);
	}	
	public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, SSelectMode selectMode, SQueryMode queryMode, Object... keys) {
		return findOrCreate(rmeta, rmeta.fieldsForMode(selectMode), queryMode, keys);
	}
    public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, SSelectMode selectMode, Object... keys) {
		return findOrCreate(rmeta, selectMode, DEFAULTQM, keys);
	}	
	public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, SQueryMode queryMode, Object... keys) {
		return findOrCreate(rmeta, SSelectMode.SNORMAL, queryMode, keys);
	}
    public <RI extends SRecordInstance> RI findOrCreate(SRecordMeta<RI> rmeta, Object... keys) {
        //System.err.println("foc sl  keys " + SUte.arrayToString(keys));
        return findOrCreate(rmeta, SSelectMode.SNORMAL, keys);
	}		
	/**
	 * mustFind
	 * @param <RI>
	 * @param rmeta
	 * @param keys
	 * @return
	 */
	public <RI extends SRecordInstance> RI mustFind(SRecordMeta<RI> rmeta, SQueryMode queryMode, Object... keys) {
        RI found = find(rmeta, queryMode, keys);
		if (found == null)
			throw new SException.Data("Record not found " + SUte.arrayToString(keys));
		return found;
	}
	public <RI extends SRecordInstance> RI mustFind(SRecordMeta<RI> rmeta, Object... keys) {
        return mustFind(rmeta, DEFAULTQM, keys);
    }
	
    /** Same as findOrCreate but if not existing returns null rather than creating a new record.
     * (Note that it may still need to query the database to determine if it is not there.)
     */
	@Override public <RI extends SRecordInstance> RI find(SRecordMeta<RI> rmeta, SFieldScalar[] selectList, SQueryMode queryMode, Object... keys) {
		return sessionHelper.doFindOrCreate(rmeta, selectList, queryMode, false, keys);
	}
	public <RI extends SRecordInstance> RI find(SRecordMeta<RI> rmeta, SQueryMode queryMode, Object... keys) {
		return find(rmeta, rmeta.getQueriedScalarFields(), queryMode, keys);
	}
	public <RI extends SRecordInstance> RI find(SRecordMeta<RI> rmeta, Object... keys) {
        return find(rmeta, DEFAULTQM, keys);
	}
	
    /** 
     * Create a new record.<p>
     * 
     * Assumes the record is new, will do an insert at flush time which will cause a unique index violation if
     * it was not.  Use a findOrCreate followed by assertNewRow if it is unclear whether the row is really new.<p>
     */
	public <RI extends SRecordInstance> RI create(SRecordMeta<RI> rmeta, Object... keys) {
        RI res = sessionHelper.doFindOrCreate(rmeta, rmeta.fieldsForMode(SSelectMode.SNORMAL), SQueryMode.SASSUME_CREATE, true, keys);
        res.assertNewRow();
        return res;
	}
	        
    /**
	 * Creates a new object with a newly generated key. 
	 * 
	 * Always creates a new empty record.   
	 */
	public <RI extends SRecordInstance> RI createWithGeneratedKey(SRecordMeta<RI> rmeta) {
        checkBegunThread();

        RI newRec = null;
		for (SFieldScalar key : rmeta.getPrimaryKeys()) {
            SGenerator.setNewGenerator(key);
            SGenerator gen = key.getGenerator();
            if (gen != null)
                newRec = (RI) gen.createWithGeneratedKey(this, rmeta);
			break;
		}
		return newRec;		
	}

	/**
	 * Removes this record from the cache but without flushing it. Any changes
	 * to it will be lost. This might be useful if the record is being manually
	 * updated/deleted and you want to deliberately ignore any direct changes.
	 * <p>
	 * 
	 * Dangerous, use with care.
	 * <p>
	 * 
	 * @see #flushAndPurge
	 */
	public void dirtyPurge(SRecordInstance rinst) {
		dataSet.removeRecord(rinst);
	}
	
    /////////////////////////////  Raw Queries /////////////////////
    
    	/**
	 * Convenience routine for doing bulk updates using raw JDBC. Dangerous.
	 * Take care with caching and never use this to commit a transaction.
	 * Returns number of rows updated. Does nothing if sql == null.<p>
     * 
     * Flushes before performing update.  But does not requery any updated records.<p>
	 */
	public int rawUpdateDB(String sql, Object... params) {
        flush();
        return rawUpdateDBNoFlush(sql, params);
    }
	public int rawUpdateDBNoFlush(String sql, Object... params) {
		getLogger().updates("rawDB " + sql);        
		int res = -1;
		if (sql != null) {
			if ( ! hasBegun())
				throw new SException.Error("Transaction not begin()ed.");
            PreparedStatement ps = null;
			try {
				ps = jdbcConnection.prepareStatement(sql);
				for (int px = 0; px < params.length; px++) {
					ps.setObject(px + 1, params[px]);
				}
				res = ps.executeUpdate();
			} catch (Exception ex) {
				throw new SException.Jdbc("SQL: " + sql, ex);
			} finally {
                try {
                   if (ps != null) ps.close();
                } catch (Exception ex){throw new SException.Jdbc(ex);}
            }
		}
		return res;
	}

    /**
     * Executes sql with parameters, returns a list of Maps of values, one map per row.
     * The key to the maps is the meta data returned by JDBC. 
     * This is normally just the column name, but can be specified in most SQLs.
     * eg.<p>
     * <code>SELECT MAX(SALARY) AS MAX_SAL ... FROM .... <code>
     */
    public SQueryResult<SRecordGeneric> rawQuery(String sql, boolean flush, Object... params) {
        if (flush) flush();
        return rawQueryInner(sql, params);//, true, true);
    }

    /** Returns a single value of at most one row.
     * @See #rawQueryDBMaps
     */
    public Object rawQuerySingle(String sql, boolean flush, Object... params) {
        if (flush) flush();
		SQueryResult<SRecordGeneric> result = rawQueryInner(sql, params); //, false, false);
		SRecordGeneric record = result.oneOrNone();
		Set<Entry<String, Object>> columns = record.entrySet();
		if (columns.size() > 1)
			throw new SException.Error("Only one column can be returned " + sql);
		for (Entry<String, Object> col : columns) { // only one
			return col.getValue();
		}
		return null;
	}

    /** Executes sql with params returning either one value, a Map of values, or a List of Maps of values.  */
    private SQueryResult<SRecordGeneric> rawQueryInner(String sql, Object[] params) { //, boolean multVals, boolean multRows) {
        
        ResultSet rs = null;
		try {
			rs = executeQuery(0L, sql, Arrays.asList(params));
            final ResultSetMetaData metaData = rs.getMetaData();

			//Object res = multRows ? new SQueryResult(sql) : null;
            SQueryResult<SRecordGeneric> res = new SQueryResult<SRecordGeneric>(sql);
			while (rs.next()) {
//                if (res != null && ! multVals){
//					throw new SException.Error("Query returned multiple rows "
//							+ sql + SUte.arrayToString(params));                    
//                }
//				if (!multVals) {
//                    if (metaData.getColumnCount() != 1)
//                        throw new SException.Error("Only one column can be returned " + sql);
//					res = rs.getObject(1);
//                } else {
                	SRecordTransient ares = new SRecordTransient();
					for (int rx = 0; rx < metaData.getColumnCount(); rx++) {
						ares.put(metaData.getColumnName(rx + 1), rs.getObject(rx + 1));
                    }
//                    if (!multRows)
//                        res = ares;
//                    else
                        res.add(ares);
//				}
			}
			if (getLogger().enableQueries())
				getLogger().queries("rawJDBC " + sql + SUte.arrayToString(params));
			if (getLogger().enableFields())
                getLogger().fields( " ---> " + res);
			return res;
		} catch (Exception ex) {
			throw new SException.Jdbc(ex);
		} finally {
			SSessionJdbc.closeResultSetAndStatement(rs);
        }
	}
    
    /**
     * Creates a new PreparedStatement, sets the parameters and executes the query.
     * Notice that null parameters are not allowed (as we don't know which java.sql.Type to use...)
     * Returns a resultSet with offset correction applied as necessary if offset != 0
     * The calling object must take care of closing the resultSet and the underlying ps.
     * Typical it will be done in finally{} like this:
     * finally {
	 *		SSessionJdbc.closeResultSetAndStatement(rs);
     * }
     * Used in SQueryExecute, SQueryTransientExecute, etc...
     * @param offset
     * @param sqlQuery
     * @param params
     * @return
     */
    protected ResultSet executeQuery(long offset, String sqlQuery, List<Object> params) {
    	checkBegunThread();
    	PreparedStatement ps = prepareStatement(offset, sqlQuery, params);
    	ResultSet rs = null;
    	try {
			rs = ps.executeQuery();
			offsetResultSet(rs, offset);
		} catch (Exception rsex) {
			if (ps != null) {
				try {
					ps.close();
				} catch (SQLException e) {
					throw new SException.Jdbc("Executing " + sqlQuery, rsex); // we keep first exception
				}
			}
			throw new SException.Jdbc("Executing " + sqlQuery, rsex);
		}
    	return rs;
    }
    /** Create a PreparedStatement and sets its parameters*/
    private PreparedStatement prepareStatement(long offset, String sqlQuery, List<Object> params) {
    	if (getLogger().enableQueries())
			getLogger().queries("Selecting " + SSessionJdbcHelper.substituteToString(sqlQuery, params));

    	PreparedStatement jdbcPreparedStatement = null;
		try {
			// fro - see if we want a scrollable ResultSet. Beware these may be
			// inefficient depending on the jdbc driver and DB
			int RsType = ResultSet.TYPE_FORWARD_ONLY;
			if (offset != 0
					&& getDriver().getOffsetStrategy().equals(JDBC)) {
				RsType = ResultSet.TYPE_SCROLL_INSENSITIVE;
			}
			jdbcPreparedStatement = getJdbcConnection().prepareStatement(sqlQuery, RsType, ResultSet.CONCUR_READ_ONLY);
		} catch (Exception psex) {
			if (jdbcPreparedStatement != null) {
				try {
					jdbcPreparedStatement.close();
				} catch (SQLException e) {
					throw new SException.Jdbc("Preparing '" + sqlQuery + "'", psex); // we keep first exception
				}
			}
			throw new SException.Jdbc("Preparing '" + sqlQuery + "'", psex);
		}
		// Set the parameters
		for (int px = 0; px < params.size(); px++) {
			try {
				jdbcPreparedStatement.setObject(px + 1, params.get(px)); // ## may cause problems in some
				                                                         // JDBC drivers with Object
			} catch (Exception se) {
				if (jdbcPreparedStatement != null) {
					try {
						jdbcPreparedStatement.close();
					} catch (SQLException e) {
						throw new SException.Jdbc("Setting " + this + " '?' " + (px+1), se); // we keep first exception
					}
				}
				throw new SException.Jdbc("Setting " + this + " '?' "+ (px + 1), se);
			}
		}
		return jdbcPreparedStatement;
    }
    
    /**
	 * Called just after executeQuery to Skip over records until offset has been
	 * reached. Drivers may optimize this in various ways, eg. LIMIT keywords
	 * where supported by the database, or by using the JDBC
	 */
	private void offsetResultSet(ResultSet rs, long offset) {
		Statement ps = null;
		try {
			if (SDriver.OffsetStrategy.JDBC.equals(getDriver().getOffsetStrategy())	&& offset != 0) {
				rs.setFetchDirection(ResultSet.FETCH_UNKNOWN);
				rs.absolute((int) offset); // 1 is first row, 0 is before first row.
			} else if (SDriver.OffsetStrategy.SCAN.equals(getDriver().getOffsetStrategy())) {
				boolean next = true;
				for (int rx = 0; next && rx < offset; rx++) {
					next = rs.next();
				}
			}
		} catch (Exception ex) {
			if (rs != null) {
				try {
					ps = rs.getStatement();
				} catch (SQLException rsex) {
					throw new SException.Jdbc("Applying offset " + offset + " to resultSet", ex);
				}
				finally {
					if (ps != null) {
						try {
							ps.close();
						} catch (SQLException e) {
							throw new SException.Jdbc("Applying offset " + offset + " to resultSet", ex); // we keep first exception
						}
					}
				}
			}
			throw new SException.Jdbc("Applying offset " + offset + " to resultSet", ex);
		}
	}
	/** Moves the ResultSet cursor one row, and return true if there was
	 * one more row, or false if the cursor was already at the end of the resultset
	 */
	protected boolean rsNext(ResultSet rs) {
		Statement ps = null;
		try {
			return rs.next();
		} catch (SQLException ex) {
			if (rs != null) {
				try {
					ps = rs.getStatement();
				} catch (SQLException rsex) {
					throw new SException.Jdbc("Moving forward in rs "+rs, ex);
				}
				finally {
					if (ps != null) {
						try {
							ps.close();
						} catch (SQLException e) {
							throw new SException.Jdbc("Moving forward in rs "+rs, ex); // we keep first exception
						}
					}
				}
			}
			throw new SException.Jdbc("Moving forward in rs "+rs, ex);
		}
	}

    //////////////// MISC ////////////////
    
	    	
    public SLog getLogger() {
        if (getDataSet() != null)
           return getDataSet().getLogger();
        else
           return SLog.getSessionlessLogger();
    }

        ///////////////////////////// Empty get/set ers ////////////////
    
    public Connection getJdbcConnection() { return jdbcConnection;}

    public SDataSet getDataSet() {
		return this.dataSet;
	}
	
	/**
	 * Eg. if (getDriver() instanceOf SDriverPostgres) ... Also
	 * getDriver().setMyFavoritePerConnectionParameter.
	 */
	public SDriver getDriver() {
		return sDriver;
	}

    public SStatistics getStatistics() {
        return statistics;
    }
    /**
     * Will try to close the resultSet and its underlying Statement
     * To be used in finally{} when calling session.executeQuery()
     * @param rs
     */
    protected static void closeResultSetAndStatement(ResultSet rs) {
		Statement ps = null;
        try {
            if (rs != null) {
            	ps = rs.getStatement();
            	if (ps == null)
            		throw new SException.InternalError("Could not get the statement from the resultSet");
            	// don't try to close explicitely, as
            	//if ( ! rs.isClosed()) // isClosed is since Java6 only
            	//	rs.close(); // close on an already close rs throws an exception in some drivers
            	// closing ps will close the rs...
            }
        } catch (Exception e1){
        	throw new SException.Jdbc("Closing rs ", e1);
    	}
        finally {
        	try {
        		if (ps != null)
        			ps.close();
        	} catch (Exception e2){
        		throw new SException.Jdbc("Closing ps ", e2);
        	}
        }
    }
    
    /**
	 * for debugging purpose
     */
    public String queryToString(SQueryTransient qry) {
		String sql = getDriver().queryExecuteFactory(this, qry).buildSqlQuery();
		StringBuffer buffer = new StringBuffer();
		List<Object> parameters = qry.getUnderlyingQuery().getQueryParameters();
		int fromIndex = 0;
		for (int ii = 0; ii < parameters.size(); ii++) {
			int index = sql.indexOf('?', fromIndex);
			buffer.append(sql.substring(fromIndex, index));
			buffer.append("'" + parameters.get(ii) + "'");
			fromIndex = index + 1;
		}

		buffer.append(sql.substring(fromIndex));

		return buffer.toString();
    }
    
}
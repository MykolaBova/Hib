package simpleorm.examples;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.sql.Connection;
import java.util.List;
import simpleorm.dataset.SDataSet;
import simpleorm.dataset.SQueryMode;
import simpleorm.dataset.SRecordInstance;
import simpleorm.sessionjdbc.SSessionJdbc;
import simpleorm.utils.SException;
import simpleorm.utils.SLog;

/**
 * Demonstrates Cache/locking issues, in particular a long, user interaction
 * transaction (optimistic locking), thread test.
 * <p>
 * 
 * An Employee record is read, and then detatched. It is then serialized to a
 * file and read back to simulate its journey to another tier. It is then
 * modified, and reattached to a SimpleORM connection.
 * <p>
 */
public class LongTransactionTest {

	public static void main(String[] argv) throws Exception {
		SSessionJdbc ses = TestUte.initializeTest(LongTransactionTest.class); // Look at this code
            ses.begin();
            TestUte.dropAllTables(ses);
            ses.commit();
			TestUte.createDeptEmp(ses);   
			detachedTest();
            dirtyTest();
			basicOptimisticTest();
            simpleDetachTest();
			longTest();
			flushAndPurgeTest();
			threadTest();
			whereNullTest();
            rollbackDetachTest();
            savepointTest();
            detachUnflushedTest();
            ses.close();
	}

    public static void dirtyTest() {
    	   	
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		SLog.getSessionlessLogger().message("\n################ dirtyTest #################\n");
        Employee emp200 = ses.findOrCreate(Employee.EMPLOYEE, "200");
        Department d200 = emp200.findReference(emp200.DEPARTMENT);
        TestUte.assertEqual("Two00", emp200.getString(emp200.NAME));
        TestUte.assertEqual("D200", d200.getString(d200.NAME));
        TestUte.assertTrue(!emp200.isDirty());        
        TestUte.assertTrue(!d200.isDirty());        
        ses.commit();
    }
    
    public static void detachedTest() throws Exception {
	   			SLog.getSessionlessLogger().message("\n################ detachedTest #################\n");

    	// Do while detached, eg UI Layer
    	SDataSet myDs = new SDataSet();
    	Employee e1200 = myDs.create(Employee.EMPLOYEE, "1200");
    	e1200.setString(Employee.NAME, "Mr BEAN");
    	Employee e1200b = myDs.find(Employee.EMPLOYEE, "1200");
    	TestUte.assertEqual(e1200, e1200b);
    	TestUte.assertTrue(myDs.isAttached() == false);
    	TestUte.assertTrue(e1200.isNewRow());
    	TestUte.assertTrue(e1200.isDirty());
        
        e1200b.setString(e1200b.MANAGER_EMPEE_ID, "1200");
    	Employee e1200mgr = (Employee)e1200b.getObject(e1200b.MANAGER);
        TestUte.assertEqual(e1200b, e1200mgr);
        
//        // The following usage is DEPRECATED
//        Employee e1300 = SDataSet.createDetachedInstance(Employee.EMPLOYEE);
//        e1300.setString(e1300.EMPEE_ID, "1300");
//        e1300.setString(e1300.NAME, "Created Detached");
//        TestUte.assertEqual(e1300.getString(e1300.NAME), "Created Detached");
//        e1300.setString(e1300.MANAGER_EMPEE_ID, "1200");
//        TestUte.assertEqual(e1300.getString(e1300.MANAGER_EMPEE_ID), "1200");
//        myDs.attach(e1300);        
//        TestUte.assertEqual(e1300.findReference(e1300.MANAGER), e1200);
//        //myDs.dumpDataSet();
        
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
    	ses.begin(myDs);
    	// new rows are dirty, should be created and flushed
    	ses.commit();
    	
    	// Check the record was saved in database
    	ses = SSessionJdbc.getThreadLocalSession();
    	ses.begin();
    	Employee emp = ses.mustFind(Employee.EMPLOYEE, "1200");
    	TestUte.assertTrue("Mr BEAN".equals(emp.getString(Employee.NAME)));

//        Employee emp13 = ses.mustFind(Employee.EMPLOYEE, "1300");
//    	TestUte.assertTrue("Created Detached".equals(emp13.getString(Employee.NAME)));
        ses.commit();
    	
        // Check Attach/Detach
        try {
            TestUte.initializeTest(LongTransactionTest.class); 
            throw new SException.Test("Two Trans not trapped");
        } catch (SException.Error tt){
            ses.getLogger().connections("Init Exception " + tt);
        }
        
		final SSessionJdbc ses2 = ses.detachFromThread();
        final SSessionJdbc ses3 = TestUte.initializeTest(LongTransactionTest.class);
        TestUte.assertTrue(ses2 != ses3);
        ses2.begin(); // OK if unusual to use detached thread.
        
        final Department[] t2Dept = new Department[1];
        Thread t2 = new Thread() {
			@Override public void run() {
                try {
                    try {
                        ses3.begin();
                        throw new SException.Test("Mixed Theads not caught");
                    } catch (SException.Error mte){}
                ses2.attachToThread(); // not necessary, could just have used ses2
                SSessionJdbc sest = SSessionJdbc.getThreadLocalSession();
                t2Dept[0] = sest.findOrCreate(Department.DEPARTMENT, "100");
                sest.commit();
                sest.close();
                 } catch (Exception te) {
                    te.printStackTrace();
                    System.exit(1);  // else exception ignored
				}
            }
         };
         t2.start();
         t2.join();

        ses3.begin();
        Department dept100d = ses3.findOrCreate(Department.DEPARTMENT, "100");
        TestUte.assertTrue(t2Dept[0] != dept100d); // ie from different sessions
        ses3.commit();        
    }
    
	/** Basic single record optimistic tests. */
	public static void basicOptimisticTest() throws Exception {
		SLog.getSessionlessLogger().message("\n################ BasiciOptimisiticTest #################\n");

		// Now check breaking a lock with update.
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
		Department d400d = ses.findOrCreate(Department.DEPARTMENT, "400");

		// Break the lock. This could be happening in another transaction.
		ses.rawUpdateDB("UPDATE XX_DEPARTMENT SET BUDGET = 4444 WHERE DEPT_ID = '400'");

        d400d.setDouble(Department.BUDGET, 3333);
        try {
			ses.commit();
			throw new SException.Test("Broken Optimistic Lock Update not detected.");
		} catch (SRecordInstance.BrokenOptimisticLockException beu) {
             ses.getLogger().connections("Opt Exception " + beu);
		}

		ses.rollback();

		// / Now check breaking a lock with delete.
		ses.begin();
		Department d400e = ses.findOrCreate(Department.DEPARTMENT, "400");

		// Break the lock. This could be happening in another transaction.
		ses.rawUpdateDB("DELETE FROM XX_DEPARTMENT WHERE DEPT_ID = '400'");

        d400e.deleteRecord();
        try {
			ses.commit();
			throw new SException.Test("Broken Optimistic Lock Delete not detected.");
		} catch (SRecordInstance.BrokenOptimisticLockException bed) {
            ses.getLogger().connections("Opt Exception " + bed);
        }
		ses.rollback();
        
        ses.begin();
        ses.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        // todo write a phantom test
        ses.commit();
        ses.begin();
          Employee e100r = ses.mustFind(Employee.EMPLOYEE, "100");
          e100r.setString(e100r.RESUME, "This is my life");
          ses.rawUpdateDB("UPDATE XX_EMPLOYEE SET RESUME = 'NOT CHECKED' WHERE \"Empee*Id\"='100'");
          // Will not produce optimistic lock exception because RESUME is NOT_OPTIMISTIC_LOCKED.
        ses.commit();
	}

    static void simpleDetachTest() throws Exception {
        SLog.getSessionlessLogger().message("################ Simple Detach Test #################");
     	SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

        // Load a record into a dataset and detach it.
		Department dept400a = ses.mustFind(Department.DEPARTMENT, "400");
		Employee emp200e = ses.mustFind(Employee.EMPLOYEE, "200");

        Department dept500a = ses.mustFind(Department.DEPARTMENT, "500");

		SDataSet ds = ses.commitAndDetachDataSet();
        ds.dumpDataSet();
		ses.close();

		//////////  Data set is now detached from database, could be moved far away.
        
		Department d400d = ds.find(Department.DEPARTMENT, "400");
        ds.dumpDataSet();
		d400d.setDouble(d400d.BUDGET, 50001);

        Department d500d = ds.find(Department.DEPARTMENT, "500");
        d500d.deleteRecord();  // Deletion will actually happen upon reattachment.
        
        Department d700 = ds.createWithNullKey(Department.DEPARTMENT); // We'll set the key after attaching
        d700.setString(d700.NAME, "Added 700 no key");
        
        Department d600 = ds.create(Department.DEPARTMENT, "600");
        d600.setString(d600.NAME, "Added Detached");
        d600.setDouble(d400d.BUDGET, 90060);
        
		Department d200d = ds.find(Department.DEPARTMENT, "200");
        TestUte.assertEqual(null, d200d); // Had not been retrieved into Dataset

        Employee e200d=ds.find(Employee.EMPLOYEE, "200");
        String e200dept = e200d.getString(e200d.DEPT_ID); // OK
        
        try {
            Department d200d2 = e200d.findReference(e200d.DEPARTMENT); // Not in data Set
            throw new SException.Test("No error find ref " + d200d2);
        } catch (SException.Data de){};
        
//        ds.dumpDataSet();
        e200d.setReference(e200d.DEPARTMENT, d400d);
        
        List<Employee> d400emps = ds.queryReferencing(d400d, Employee.DEPARTMENT);
        TestUte.assertEqual(d400emps.get(0), e200d);
            
		///////// Reattach the dataset to the database. 
		ses = TestUte.initializeTest(LongTransactionTest.class);
		ses.begin(ds); ds=null;

		//ses.attachDataSet(ds);
        
        Department d400c  = ses.mustFind(Department.DEPARTMENT, "400");
		TestUte.assertTrue(d400c.isDirty());

        d700.setString(d700.DEPT_ID, "700"); // OK to set primary key as was created null.
        
		// / Check double flushing OK
		ses.flush();
		
		// Check we can reuse the pk of a deleted record that has been flushed
		ses.create(Department.DEPARTMENT, "500");
		
		ses.commit();
		
		ses.begin();
		Department d500 = ses.find(Department.DEPARTMENT, "500");
		d500.deleteRecord();
		ses.commit();

		// / Check that the update really happened.
		ses.begin();
        
		TestUte.assertEqual(50001.0, 
            ses.rawQuerySingle("SELECT BUDGET FROM XX_DEPARTMENT WHERE DEPT_ID = '400'",true));
        
        Department d600r = ses.mustFind(Department.DEPARTMENT, "600");
        TestUte.assertEqual("Added Detached", d600r.getString(d600.NAME));
        
        Department d700r = ses.mustFind(Department.DEPARTMENT, "700");
        TestUte.assertEqual("Added 700 no key", d700r.getString(d700.NAME));

        TestUte.assertEqual(0L, 
            ses.rawQuerySingle("SELECT count(*) FROM XX_DEPARTMENT WHERE DEPT_ID = '500'",true));
        
		ses.commit();

    }
	/**
	 * Detatach and seriealize before loading and reattaching, do multi record
	 * detach. More vigourous test.
	 */
	public static void longTest() throws Exception {
		
		
		// / Query Employee 200 and detatch it.
		SLog.getSessionlessLogger().message("\n################ LongTransTest #################");
		SSessionJdbc ses1 = SSessionJdbc.getThreadLocalSession();
		ses1.begin();

		// Ceck getReferenceNoQuery && IsDirty
		Employee emp200 = ses1.findOrCreate(Employee.EMPLOYEE, "200");
		Object d200NoQ1 = emp200.getReferenceNoQuery(emp200.DEPARTMENT);
		Object d200NoQ2 = emp200.getReferenceNoQuery(emp200.DEPARTMENT);
		TestUte.assertTrue(d200NoQ1 == Boolean.FALSE && d200NoQ2 == Boolean.FALSE);
		Department dept200 = emp200.findReference(emp200.DEPARTMENT);
		Object d200NoQ3 = emp200.getReferenceNoQuery(emp200.DEPARTMENT);
		TestUte.assertTrue(d200NoQ3 == dept200);
		// Strictly correct, but not important: TestUte.assertTrue(!emp200.isDirty(emp200.DEPARTMENT));
		emp200.setReference(emp200.DEPARTMENT, dept200);
		// TestUte.assertTrue(!emp200.isDirty(emp200.DEPARTMENT));

		Employee emp100 = emp200.findReference(emp200.MANAGER);
        ses1.getDataSet().dumpDataSet();
		SDataSet detachedDs = ses1.commitAndDetachDataSet();
		// Note that emp100 is not detatched.
		ses1.close();
        detachedDs.dumpDataSet();

		// / Serialize the Employee. This also serializes the
		// referenced and detatched department but not the non-retrieved
		// manager. This may change, see white paper.

		File file = File.createTempFile("serialized-",".tmp");

		SLog.getSessionlessLogger().message("####Serializing...");
		FileOutputStream out = new FileOutputStream(file);
		ObjectOutputStream outs = new ObjectOutputStream(out);
        outs.writeObject(detachedDs);
		outs.flush();
		out.close();

		// Send the the Employee far away...and bring it back.

		// / DeSerialize
		FileInputStream in = new FileInputStream(file);
		ObjectInputStream ins = new ObjectInputStream(in);
		SDataSet restoredDs = (SDataSet) ins.readObject();
		in.close();
    
		TestUte.assertTrue(detachedDs != restoredDs);
		Employee emp2 = restoredDs.find(Employee.EMPLOYEE, "200");
		TestUte.assertTrue(emp2 != emp200);
		// / Update the Employee, say from a user input.
		emp2.getDouble(emp2.SALARY);
		emp2.setDouble(emp2.SALARY, 2222);

		// / Create a new Employee while still detached
		Employee e800 = restoredDs.create(Employee.EMPLOYEE, "800");
		e800.setString(Employee.NAME, "New800");

		// / The Departement Record is available
		Department dept2 = emp2.findReference(emp2.DEPARTMENT); // OK
		TestUte.assertEqual("H400", dept2.getString(dept2.NAME));
		TestUte.assertTrue(!dept2.isAttached());

		// The Manager record can also become available now we detach whole dataSet
		emp2.findReference(emp2.MANAGER); // Should not fail Fail now that we detach whole dataSet

		Employee emp1 = restoredDs.find(Employee.EMPLOYEE, "100");

        restoredDs.dumpDataSet();
        try {
            Department d1 = emp1.findReference(emp1.DEPARTMENT);
            throw new RuntimeException("Should not be able to retrieve record from database while detached " + d1);
        } catch (SException.Data x){}
        
        
        //////////////////  Attach the Employee to a new session and flush. /////////////////////
        SSessionJdbc ses2 = TestUte.initializeTest(LongTransactionTest.class);
		ses2.begin(restoredDs);

		//emp2 = ses.attach(emp2); // recursively attaches dept200
		//ses.attachDataSet(restoredDs);
		TestUte.assertTrue(emp2.isDirty());
		TestUte.assertTrue(emp2.getDouble(emp2.SALARY) == 2222);
		SLog.getSessionlessLogger().debug(emp2.allFields());

		TestUte.assertTrue(dept2.isAttached()); // by emp2.attach.
		Employee mgr = emp2.findReference(emp2.MANAGER); // OK Now
		TestUte.assertTrue("One00".equals(mgr.getString(mgr.NAME)));

		// / Check double flushing OK
		ses2.flush();
		emp2.setDouble(emp2.SALARY, 3333);

		ses2.commit();

		// / Check that the update really happened.
		ses2.begin();
		TestUte.assertEqual(
            ses2.rawQuerySingle("SELECT SALARY FROM XX_EMPLOYEE WHERE \"Empee*Id\" = '200'",true),
			3333.0);
		TestUte.assertEqual(
			ses2.rawQuerySingle("SELECT ENAME FROM XX_EMPLOYEE WHERE\"Empee*Id\" = '800'",true), 
            "New800");
		ses2.commit();

		// When not finding a record, dataset should not keep a SRecordInstance in his record list.
		SDataSet dataset = new SDataSet();
		ses2.begin(dataset);
		Employee empX100 = ses2.find(Employee.EMPLOYEE, "X100");
		ses2.commitAndDetachDataSet();
		
		if (empX100==null){
			// This should not cause problem
			empX100 = dataset.create(Employee.EMPLOYEE, "X100");
		}
		
	}

	static void flushAndPurgeTest() throws Exception {
		SLog.getSessionlessLogger().message("############# FlushAndPurgeTest ##############");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		Department dept100a = ses.findOrCreate(Department.DEPARTMENT, "100");
		dept100a.setDouble(Department.BUDGET, 123);

		ses.flushAndPurge();

		ses.rawUpdateDB("UPDATE XX_DEPARTMENT SET BUDGET = BUDGET * 2 WHERE DEPT_ID = '100'");

		// Check that the changed value is reflected in the in memory
		// Department.
		Department dept100b = ses.findOrCreate(Department.DEPARTMENT, "100");
		TestUte.assertTrue(dept100b.getDouble(Department.BUDGET) == 246);
		TestUte.assertTrue(dept100a != dept100b);
		ses.commit();
	}

	/** Check locking works OK using two threads. */
	static void threadTest() throws Exception {
		SLog.getSessionlessLogger().message("################ threadTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();

		Employee emp200 = ses.findOrCreate(Employee.EMPLOYEE, SQueryMode.SREAD_ONLY, "200");

		Department d100 = ses.findOrCreate(Department.DEPARTMENT, SQueryMode.SFOR_UPDATE, "100");
		d100.setDouble(d100.BUDGET, 2143);
		ses.flush(d100); // Not actually necessary for Oracle at least, For Update produces exclusive lock.

		final Object waiter = new Object();

		// d100 should be locked, so this thread will wait.
		Thread t2 = new Thread() {
			@Override public void run() {
				try {
					SSessionJdbc ses =null;
					try {
						ses = TestUte.initializeTest(LongTransactionTest.class); 
					} catch (Exception ex) {
						throw new SException.Error(ex);
					}

					ses.begin();
					threadCheck1 = true;

					Employee emp200 = ses.findOrCreate(Employee.EMPLOYEE, SQueryMode.SREAD_ONLY, "200");

					threadCheck1a = true;

					Department d100 = ses.findOrCreate(Department.DEPARTMENT, SQueryMode.SFOR_UPDATE, "100");
                    // Unclear what happpens if not For_Update.  Probably waits anyway.

					threadCheck2 = true;
					d100.setDouble(d100.BUDGET, 666);
					ses.commit();

				} catch (Exception te) {
                    te.printStackTrace();
                    System.exit(1);  // else exception ignored
				} finally {
					SSessionJdbc.getThreadLocalSession().close();
                }
				synchronized (waiter) {
					waiter.notify();
				}
			}
		};
		t2.start();

		synchronized (waiter) {
			waiter.wait(2000); // Force a thread reschedule.  Will time out if locking.
		}
		TestUte.assertTrue(threadCheck1); // Really was a reschedule.
		//SLog.getSessionlessLogger().message("Read Only is non blocking " + threadCheck1a);
		/**
		 * For some wierd reason, PostgreSQL will occasionally and
		 * unreproducatbly hang about here, often the very first time the test
		 * is run after a reboot! Needs investigation.
		 */
		//if (ses.getDriver() instanceof SDriverPostgres)
			TestUte.assertTrue(threadCheck1a); // ReadOnly non blocking

		boolean locking = ses.getThreadLocalSession().getDriver().supportsLocking();
		SLog.getSessionlessLogger().message("Locking " + locking);
		if (locking) {
			TestUte.assertTrue(!threadCheck2); // There really was a lock, waiting.

			TestUte.assertEqual(
                ses.rawQuerySingle("SELECT BUDGET FROM XX_DEPARTMENT WHERE DEPT_ID = '100'",true),
				2143.0); // ie has not been updated.
			ses.commit();

			t2.join(); // Now the other thread updates the value

			ses.begin();
			TestUte.assertEqual(
                ses.rawQuerySingle("SELECT BUDGET FROM XX_DEPARTMENT WHERE DEPT_ID = '100'",true),
				666.0); // ie has not been updated.
			ses.commit();
		} else { // no locking
			t2.join(); // If this hangs there really is locking after all.

			d100.setDouble(d100.BUDGET, 13); 
            // Need this to break lock  (No update requred after flush)
            System.err.println("Post " + d100.allFields());
			try {
				ses.commit();
				throw new SException.Test("Broken Optimistic Lock not detected.");
			} catch (SRecordInstance.BrokenOptimisticLockException be) {
			}

			ses.rollback();
		}
	}

	static volatile boolean threadCheck1 = false, threadCheck1a = false,
			threadCheck2 = false;

	static void whereNullTest() throws Exception {
		SLog.getSessionlessLogger().message("################ whereNullTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		// Make a null and non-null field value for Department record
		ses.begin();
		ses.rawUpdateDB("UPDATE XX_DEPARTMENT SET DNAME = null, BUDGET=666 WHERE DEPT_ID = '100'");
		ses.commit();

		ses.begin();
		Department d100a = ses.findOrCreate(Department.DEPARTMENT, "100");
		d100a.setString(d100a.NAME, "One00");
		d100a.setDouble(d100a.BUDGET, 246); // also test update clause
											// formulation of non-null field

		ses.commit();

		// Check that the changed value is correct
		ses.begin();
		Department dept100b = ses.findOrCreate(Department.DEPARTMENT, "100");
		TestUte.assertTrue("One00".equals(dept100b.getString(dept100b.NAME)));
		TestUte.assertTrue(dept100b.getDouble(dept100b.BUDGET) == 246);
		ses.commit();

	}

    static void rollbackDetachTest() throws Exception {
        SLog.getSessionlessLogger().message("################ rollbackDetachTest #################");
		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();		
		ses.begin();
        ses.mustFind(Employee.EMPLOYEE, "100");
        SDataSet ds1 = ses.commitAndDetachDataSet();
        
        ds1.find(Employee.EMPLOYEE, "100").setString(Employee.NAME, "100++");
        ds1.create(Employee.EMPLOYEE, "200").setString(Employee.NAME, "200++"); // Already exists
                
        SDataSet ds2 = ds1.clone();
        
        ses.begin(ds1);
        
        try {
          ses.flush(); // duplicate.
          throw new SException.Test("Duplicate not found");
        } catch (SException.Jdbc jex){
             ses.getLogger().connections("Flush Exception " + jex);
            TestUte.assertEqual("200++", jex.getInstance().getString(Employee.NAME));
        }
        ses.rollback();

        ds2.dumpDataSet(); System.err.println(ds1.getDirtyRecords());
        ds2.removeRecord(ds2.find(Employee.EMPLOYEE, "200"));
        ds2.create(Employee.EMPLOYEE, "900bis").setString(Employee.NAME, "900++"); // Already exists
        
        ses.begin(ds2);        
		ses.commit();
        
        ses.begin();
        TestUte.assertEqual("100++", ses.mustFind(Employee.EMPLOYEE, "100").getString(Employee.NAME));
        TestUte.assertEqual("900++", ses.mustFind(Employee.EMPLOYEE, "900bis").getString(Employee.NAME)); // new
        ses.commit();
    }
        
    static void savepointTest() throws Exception {
//        SLog.getSessionlessLogger().message("################ savepointTest #################");
//		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();		
//		ses.begin();
//        ses.mustFind(Employee.EMPLOYEE, "100");
//        SDataSet ds1 = ses.commitAndDetachDataSet();
//        
//        ds1.find(Employee.EMPLOYEE, "100").setString(Employee.NAME, "100++");
//        ds1.create(Employee.EMPLOYEE, "200").setString(Employee.NAME, "200++"); // Already exists
//        
//        ses.begin(ds1, true);
//        
//        try {
//          ses.flush(); // 200 is a duplicate record
//          throw new SException.Test("Duplicate not found");
//        } catch (SException.Jdbc jex){
//             ses.getLogger().connections("Flush Exception " + jex);
//            TestUte.assertEqual("200++", jex.getInstance().getString(Employee.NAME));
//        }
//        ses.rollBackAndDetachDataSet();
//
//        ds1.dumpDataSet(); System.err.println(ds1.getDirtyRecords());
//        ds1.removeRecord(ds1.find(Employee.EMPLOYEE, "200"));
//        ds1.create(Employee.EMPLOYEE, "900").setString(Employee.NAME, "900++"); // does not exist
//        
//        ses.begin(ds1);
//		ses.commitAndDetachDataSet();
//        
//        ses.begin();
//        TestUte.assertEqual("100++", ses.mustFind(Employee.EMPLOYEE, "100").getString(Employee.NAME));
//        TestUte.assertEqual("900++", ses.mustFind(Employee.EMPLOYEE, "900").getString(Employee.NAME)); // new
//        ses.commit();
//        
//        // ######## test savepoint on a record modified before begin()
//        Employee e100 = ds1.find(Employee.EMPLOYEE, "100");
//        e100.setString(Employee.NAME, "NoName");
//        ses.begin(ds1, true);
//        TestUte.assertEqual("NoName", e100.getString(Employee.NAME));
//        e100.setString(Employee.NAME, "A weird name");
//        TestUte.assertEqual("A weird name", e100.getString(Employee.NAME));
//        ses.flush();
//        ses.rollBackAndDetachDataSet();
//        TestUte.assertEqual("NoName", e100.getString(Employee.NAME));
//
//        // ######### test savepoint on a record modified after begin()
//        // save name
//        ses.begin(ds1);
//        ses.commit();
//        // check name is in database
//        ses.begin();
//        e100 = ses.mustFind(Employee.EMPLOYEE, "100");
//        TestUte.assertEqual("NoName", e100.getString(Employee.NAME));
//        ses.commit();
//        // begin a new session on new dataset, with savepoint
//        ds1 = new SDataSet();
//        ses.begin(ds1, true);
//        e100 = ses.find(Employee.EMPLOYEE, "100");
//        e100.setString(Employee.NAME, "A weird name");
//        TestUte.assertEqual("A weird name", e100.getString(Employee.NAME));
//        ses.flush();
//        ses.rollBackAndDetachDataSet();
//        TestUte.assertEqual("NoName", e100.getString(Employee.NAME));
//
//        
//        // ########### test deleted record can be rolledback too
//        // get e900 in dataSet
//        ses.begin(ds1, true);
//        ses.find(Employee.EMPLOYEE, "900");
//        ses.commitAndDetachDataSet();
//        // delete during a transaction
//        ses.begin(ds1, true);
//        ses.find(Employee.EMPLOYEE, "900").deleteRecord();
//        ses.flush();
//        ses.rollBackAndDetachDataSet();
//        Employee e900 = ds1.find(Employee.EMPLOYEE, "900");
//        TestUte.assertEqual("900++", ds1.find(Employee.EMPLOYEE, "900").getString(Employee.NAME));
//        // delete before transaction
//        e900.deleteRecord();
//        ses.begin(ds1, true);
//        ses.flush();
//        ses.rollBackAndDetachDataSet();
//        TestUte.assertTrue(e900.isDeleted());
//        // now delete for real
//        ses.begin(ds1);
//        ses.commit();
//        // check is does not exists anymore
//        ds1 = new SDataSet();
//        ses.begin(ds1);
//        e900 = ses.find(Employee.EMPLOYEE, "900");
//        TestUte.assertTrue(e900 == null);
//        ses.commitAndDetachDataSet();
//        // also in dataSet
//        e900 = ds1.find(Employee.EMPLOYEE, "900");
//        TestUte.assertTrue(e900 == null);
//        
//        // ######### test savepoint on a record modified before and after begin()
//        // check name in database : NoName
//        ds1 = new SDataSet();
//        ses.begin(ds1,true);
//        e100 = ses.mustFind(Employee.EMPLOYEE, "100");
//        TestUte.assertEqual("NoName", e100.getString(Employee.NAME));
//        ses.commitAndDetachDataSet();
//        // check savepoint is cleared
//        TestUte.assertTrue( ! ds1.hasSavepoint());
//        // modify name : RealName
//        e100.setString(Employee.NAME, "RealName");
//        // begin a new session on new dataset, with savepoint, and modify again to FakeName
//        ses.begin(ds1, true);
//        TestUte.assertEqual("RealName", e100.getString(Employee.NAME));
//        e100.setString(Employee.NAME, "FakeName");
//        TestUte.assertEqual("FakeName", e100.getString(Employee.NAME));
//        ses.flush();
//        ses.rollBackAndDetachDataSet();
//        TestUte.assertEqual("RealName", e100.getString(Employee.NAME));
//        ses.begin(ds1);
//        ses.commitAndDetachDataSet();
//        TestUte.assertEqual("RealName", e100.getString(Employee.NAME));
    }
    
    static void detachUnflushedTest() throws Exception {
        SLog.getSessionlessLogger().message("################ detachUnflushedTest #################");
        SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
        
        SDataSet ds = new SDataSet();
        
        Employee newEmp = null;
        Employee newEmp2 = null;
		
        ses.begin(ds);
        newEmp = ses.find(Employee.EMPLOYEE, "myNewEmp");
        ses.find(Employee.EMPLOYEE, "100");
        ses.detachUnflushedDataSet();
        
        if (newEmp == null) {
        	newEmp = ds.create(Employee.EMPLOYEE, "myNewEmp");
        }
        
        ses.begin(ds);
        newEmp2 = ses.find(Employee.EMPLOYEE, "myNewEmp2");
        ses.detachUnflushedDataSet();
        
        TestUte.assertTrue(newEmp.isNewRow());
        TestUte.assertTrue( ! ds.find(Employee.EMPLOYEE, "100").isNewRow());
        
     }
}

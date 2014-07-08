package EJB;
import java.sql.Connection;
import java.util.*;

import simpleorm.core.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import com.borderfree.generic.domain.RawDBData;


/**
 * Specializes SConnection for use with JTA Transaction management.
 * (We do not want references to J2EE classes to appear directly in SimpleORM, 
 * which should be kept...Simple.)
 * (There is absolutely no need to use JTA and aberglas recommends against JTA.) <p>
 * 
 * The main problems is that EJBs do not guarantee that the same thread will be used
 * accross EJB calls.  SimpleORM normally associates connections with threads.
 * 
 * The basic idea is to associate connections with the JTA transaction object
 * intead of the current thread.  
 * DatabaseORM then implements a Synchronization object that tracks JTA commits and rollbacks.
 *
 * This work is not part of the core SimpleORM package and is provided very much "as is".  
 * However, the hooks it uses into SimpleORM are fully supported, and it provides
 * a pretty straightforward outline as to how to integrate SimpleORM with EJBs.
 * 
 * @author Dan Hristodorescu
 */
public class SConnectionEJB extends SConnection{

	/** Associates SConnection state to the current transaction. */
  private static HashMap connectionToTransactions = new HashMap();

  /** No direct creation of Connections. */
  protected SConnectionEJB(){}
  
  static class EJBConnectionGetter extends SConnection.ConnectionGetter {
  	/*
  	 * Return connection associated with current thread or transaction,
  	 * error if both.
  	 */
  	protected SConnection getAConnection() {
  		SConnection threadCon = super.getAConnection();
  		SConnection transCon = SConnectionEJB.getTransactionConnection();
  		if (threadCon == null) return transCon;
  		else if (transCon == null) return threadCon;
  		else throw new SException.Error(
  				"Currently have both a thread connection " + threadCon 
					+ " and a transaction connection " + transCon);
  	}
  }
  static {SConnection.connectionGetter = new EJBConnectionGetter();}
  
  static SConnection getTransactionConnection()
  {
  	/*
      Transaction tx = getTransaction();
      SConnection conn = null;
      
      synchronized(connectionToTransactions)
      {
          conn = (SConnection) connectionToTransactions.get(tx);
      }
      return conn;
      */
  	return null;
  }

	/** overrides SConnection ot create an SConnectionEJB instead. */
	public static void attach(SConfiguration conf, String connectionName) {
      attach(conf, name);
  }

	/** The JTA manager will do the commit. */
  public boolean mustCommitBeforeDetaching() { // Overrides
    return false;
  }
  
  protected void rawAttach() { // Overriden in SConnectionEJB
  	Transaction tx = getTransaction();
  	
  	synchronized(connectionToTransactions)
		{
  		connectionToTransactions.put(tx, this);
		}
  }

  protected void rawDetach() { // Overrides
    Transaction tx = getTransaction();
    synchronized(connectionToTransactions)
    {
        connectionToTransactions.remove(tx);
    } 
  }

  public static Transaction getTransaction()
  {      
  	InitialContext initCtx = null;
  	Transaction tx = null;      
  	
  	try
		{
  		initCtx = new InitialContext();
  		TransactionManager txm = (TransactionManager)
			initCtx.lookup("java:/TransactionManager");
  		tx = txm.getTransaction();          
		}
  	catch(Exception ex)
		{
  		throw new IllegalStateException("Cannot lookup transaction manager: " 
  				+ ex.getClass().getName() + " - " + ex.getMessage());
		}
  	finally
		{
  		try
			{
  			if (initCtx != null)
  			{
  				initCtx.close();
  			}
			}
  		catch (NamingException ne)
			{        
			}
		}
  	
  	if(tx == null)
  	{
  		throw new IllegalStateException("No transaction manager, not
  				in EJB container or NOT_SUPPORTED transaction descriptor: Please use
					JDBC");
  	}
  	
  	return tx;      
  }

}





 
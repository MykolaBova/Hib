//package com.borderfree.generic.database;


import java.sql.Connection;
import java.sql.SQLException;


import simpleorm.core.SConnection;
import simpleorm.core.SDriverOpta;


/**
 * This class registers itself tiwht the transaction manager
 * to receive Commit/Rollback callbacks.
 * 
 * @author dhristodorescu Borderfree
 */
public final class DatabaseORM implements Synchronization
{

    private DatabaseORM()
    {
    }

    public static DatabaseORM getDatabase(Connection connection)
throws SQLException, NamingException, SystemException,
            RollbackException
    {
        DatabaseORM database = new DatabaseORM();
	Transaction tx = SConnection.attachConnection(connection);            
        tx.registerSynchronization(this); // <<< This enables the call backs.
        return database;
    }

    public Object load(RawDBData clazz, Object key)
    {        
        return clazz.getMeta().findOrCreate(key);
    }

    public Object create(RawDBData clazz)
    {
        return clazz.getMeta().createWithGeneratedKey();
    }

    public void flush()
    {
        SConnection.flush();    
    }
    
    public void beforeCompletion()
    {
        Transaction tx = SConnection.getTransaction();
        if(tx == null)
        {
            throw new IllegalStateException("BeforeCompletion: No
transaction in process");
        }
        int status = Status.STATUS_UNKNOWN;
        try
        {
             status = tx.getStatus();
        }
        catch(SystemException ex)
        {
            throw new IllegalStateException("BeforeCompletion: Error
getting transaction status - " + ex.getClass().getName() + " "
+ex.getMessage());
        }
        
        if(status == Status.STATUS_ACTIVE || status ==
Status.STATUS_COMMITTING 
                || status == Status.STATUS_PREPARED ||  status ==
Status.STATUS_PREPARING)
        {
            SConnection.flush();    
        }        
        SConnection.detachWithoutClosing();
    }

    public void afterCompletion(int status)
    {
    }
        
}
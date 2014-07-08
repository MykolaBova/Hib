/**
 * @author dhristodorescu Borderfree
 * 
 * Not sure why this separate from DatabaseORM, or why it implements SessionBean. 
 */

public class BaseServiceBean implements SessionBean
{
 protected DatabaseORM getDatabaseORM() throws SQLException,
NamingException, SystemException, RollbackException
 {
      return DatabaseORM.getDatabase(getConnection());
 }

 protected Connection getConnection() throws SQLException, NamingException
  {            
      InitialContext initCtx = null;
      try
      {
        initCtx = new InitialContext();
        String connectionString =
        	com.borderfree.generic.database.Database.DEFAULT_JTS_CONNECTION;
        DataSource ds = (javax.sql.DataSource)initCtx.lookup(connectionString);
        Connection connection = ds.getConnection();
        return connection;
      }
      finally
      {
        try
        {
          if (initCtx != null)
            initCtx.close();
        }
      }    
  }
}
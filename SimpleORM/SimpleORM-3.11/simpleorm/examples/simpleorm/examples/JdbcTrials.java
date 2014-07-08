package simpleorm.examples;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

/**
 * Small tests with raw JDBC to test database.
 * @author aberglas
 */
public class JdbcTrials {
    
    public static void main(String[] argv) throws Exception {
        hsqlSeparateSequence();
    }
    
    static void hsqlInsertSequence() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection   conn = DriverManager.getConnection("jdbc:hsqldb:file:trivialDB;shutdown=true;","sa","");
        
           Statement xst = conn.createStatement();
            try {int xrdr = xst.executeUpdate("DROP TABLE sample_table");} catch(Exception ex){}
            int xrcr = xst.executeUpdate("CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)");
            //int xrD = xst.executeUpdate("DELETE FROM sample_table");
            int xr2 = xst.executeUpdate("INSERT INTO sample_table(str_col,num_col) VALUES('One', 100)");
            query(conn, 1, "CALL IDENTITY()");
            int xr4 = xst.executeUpdate("INSERT INTO sample_table(ID, str_col,num_col) VALUES(25, 'Two', 200)");
            query(conn, 1, "CALL IDENTITY()");
            int xr3 = xst.executeUpdate("INSERT INTO sample_table(str_col,num_col) VALUES('Two', 200)");
            query(conn, 1, "CALL IDENTITY()");

            // NEXT Identity goes 26...;  ie. Identity is max(last ident, next).  Starts at 0;  Drop table resets.  Delete does not.
            
            query(conn, 3, "SELECT * FROM sample_table WHERE num_col < 250");
            
            xst.close();
        
          
        conn.close(); 

            //CallableStatement ident =  conn.prepareCall("{ ? =  CALL IDENTITY() }"); Fails
            //ident.registerOutParameter(1, Types.BIGINT); // "Not Supported"
            //ident.execute();
            //int idx  = ident.getInt(1);
    }

    static void hsqlSeparateSequence() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
        Connection   conn = DriverManager.getConnection("jdbc:hsqldb:file:trivialDB;shutdown=true;","sa","");
        
           Statement xst = conn.createStatement();
            //int xrcrseq = xst.executeUpdate("CREATE SEQUENCE SEQTBL_SEQ");
            
            query(conn, 1, "CALL NEXT VALUE FOR SEQTBL_SEQ");
            
            xst.close();
        
          
        conn.close(); 

            //CallableStatement ident =  conn.prepareCall("{ ? =  CALL IDENTITY() }"); Fails
            //ident.registerOutParameter(1, Types.BIGINT); // "Not Supported"
            //ident.execute();
            //int idx  = ident.getInt(1);
    }
    
    /** Had trouble making HSQL persist between calls.  Need shutdown-true. */
    static void persistDB() throws Exception {
        Class.forName("org.hsqldb.jdbcDriver");
           Connection   conn = DriverManager.getConnection("jdbc:hsqldb:file:trivialDB;shutdown=true;","sa","");
        
           if (true) query(conn, 3, "SELECT * FROM sample_table WHERE num_col < 250");
           
           Statement xst = conn.createStatement();
            int xr1 = xst.executeUpdate("CREATE TABLE sample_table ( id INTEGER IDENTITY, str_col VARCHAR(256), num_col INTEGER)");
            int xr2 = xst.executeUpdate("INSERT INTO sample_table(str_col,num_col) VALUES('Ford', 100)");
            xst.close();

//           Statement sdst = conn.createStatement();   
//           sdst.execute("SHUTDOWN");
           conn.close(); 
    }
    
    static void query(Connection con, int nrcols, String query) throws Exception  {
        System.err.println("\n===== QUERY " + query);
        Statement qst = con.createStatement();
        ResultSet rs = qst.executeQuery(query);
        while (rs.next()) {
            System.err.print("== Row: ");
            for (int i = 0; i < nrcols; ++i) {
                Object o = rs.getObject(i + 1);    // Is SQL the first column is indexed
                System.err.print(o.toString() + "; ");
            }
            System.err.println();
        }
    }

}

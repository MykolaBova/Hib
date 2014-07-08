package org.julp.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PKCacheTest {

    private Connection conn;

    public PKCacheTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {

    }

    @Before
    public void setUp() {
        String url = "jdbc:h2:tcp://localhost/../data/sharefiledb;MODE=MSSQLServer;IFEXISTS=TRUE";
        String driver = "org.h2.Driver";
        String user = "sa";
        String pass = "";
        BasicDataSourceImpl ds = new BasicDataSourceImpl();
        ds.setUserName(user);
        ds.setPassword(pass);
        ds.setDriverName(driver);
        ds.setConnectionUrl(url);
        try {
            conn = ds.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(PKCacheTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(PKCacheTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

//    @Test
//    public void testGetInstance() {
//        System.out.println("getInstance");
//        PKCache expResult = null;
//        PKCache result = PKCache.getInstance();
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

//    @Test
//    public void testSetPrimaryKey() {
//        System.out.println("setPrimaryKey");
//        String catalog = "";
//        String schema = "";
//        String table = "";
//        Set<String> pk = null;
//        PKCache instance = null;
//        instance.setPrimaryKey(catalog, schema, table, pk);
//        fail("The test case is a prototype.");
//    }

//    @Test
//    public void testPopulatePrimaryKey_4args() {
//        System.out.println("populatePrimaryKey");
//        String catalog = "";
//        String schema = "";
//        String table = "";
//        Set<String> additionalColumns = null;
//        PKCache instance = null;
//        Set<String> expResult = null;
//        Set<String> result = instance.populatePrimaryKey(catalog, schema, table, additionalColumns);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

    @Test
    public void testPopulatePrimaryKey_5args() {
        System.out.println("populatePrimaryKey");
        //Connection connection = null;
        String catalog = null;
        String schema = null;
        String table = "UPLOADED_FILES";
        Set<String> additionalColumns = null;
        PKCache instance = PKCache.getInstance();
        Set<String> expResult = null;
        Set<String> result = instance.populatePrimaryKey(conn, catalog, schema, table, additionalColumns);
        assertEquals(expResult, result);
        fail("The test case is a prototype.");
    }

//    @Test
//    public void testGetPrimaryKey_3args() {
//        System.out.println("getPrimaryKey");
//        String catalog = "";
//        String schema = "";
//        String table = "";
//        PKCache instance = null;
//        Set<String> expResult = null;
//        Set<String> result = instance.getPrimaryKey(catalog, schema, table);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

//    @Test
//    public void testGetPrimaryKey_4args() {
//        System.out.println("getPrimaryKey");
//        Connection connection = null;
//        String catalog = "";
//        String schema = "";
//        String table = "";
//        PKCache instance = null;
//        Set<String> expResult = null;
//        Set<String> result = instance.getPrimaryKey(connection, catalog, schema, table);
//        assertEquals(expResult, result);
//        fail("The test case is a prototype.");
//    }

}

package org.julp.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.julp.Wrapper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DBServicesJUnitTest {

    private BasicDataSourceImpl ds = new BasicDataSourceImpl();

    public DBServicesJUnitTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
//        ds.setDriverName("org.hsqldb.jdbcDriver");
//        ds.setDbURL("jdbc:hsqldb:hsql://localhost/julp_examples");
//        ds.setUserName("sa");
//        ds.setPassword("");
        
        ds.setDriverName("oracle.jdbc.OracleDriver");
        ds.setConnectionProperties("jdbc:oracle:thin:@localhost:1521:XE");
        ds.setUserName("julp");
        ds.setPassword("julp");                   
    }

    @After
    public void tearDown() {
        try {
            ds.closeConnection();
        } catch (SQLException ex) {
            Logger.getLogger(DBServicesJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testGetResultSets() {
        DBServices dBServices = new DBServices();
        dBServices.setDataSource(ds);
        String sql = "SELECT * FROM CUSTOMER WHERE LAST_NAME <> ? AND CUSTOMER_ID IN (:#)";
        try {
            dBServices.setPlaceHolder(":#");
            dBServices.setMaxNumberOfParams(3);
            Collection params = new ArrayList();
            Collection in = new ArrayList();
            in.addAll(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
            params.add("ffff");
            params.add(in);

            ResultSet[] rs = dBServices.getResultSets(sql, params);
            System.out.println("ResultSet.lenght: " + rs.length);
            Assert.assertEquals(4, rs.length);
            
            DomainObjectFactory<Customer> f = new DomainObjectFactory<Customer>();
            f.setDBServices(dBServices);
            f.setDomainClass(Customer.class);
            
            f.loadMappings("Customer.properties");
            Wrapper[] wrappers = new Wrapper[rs.length];
            for (int i = 0 ; i < wrappers.length; i++) {
                wrappers[i] = new Wrapper(rs[i]);
            }                        
            f.load(wrappers);
            List<Customer> customers = f.getObjects();
            Assert.assertEquals(10, customers.size());
            System.out.println("customers.size(): " + 10);
            for (Customer c : customers) {
                System.out.println(c);
            }                                    
        } catch (Exception ex) {
            Logger.getLogger(DBServicesJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                dBServices.release(true);
            } catch (SQLException ex) {
                Logger.getLogger(DBServicesJUnitTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

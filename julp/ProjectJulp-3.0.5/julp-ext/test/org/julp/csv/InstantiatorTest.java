/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.julp.csv;

import org.julp.CGLibInstantiator;
import org.julp.CGLibNamingPolicy;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author lng
 */
public class InstantiatorTest {

    public InstantiatorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }
    
    @Test
   public void enchanceTest0() { 
        CsvCustomerFactory f = new CsvCustomerFactory();
//        //MetaData md = f.getMetaData();
       CGLibInstantiator<Customer> ins = new CGLibInstantiator<Customer>(new CGLibNamingPolicy());
       
        //ins.setNamingPolicy(new CGLibNamingPolicy());
        Customer c0 = new Customer();
        c0.setCustomerId(1);
        c0.setLastName("1");
        
        int hc0 = c0.hashCode();
        
        Customer c1 = ins.enhance(c0);
        System.out.println(c1.getClass());
        String cn = c1.getClass().getCanonicalName();
        int hc1 = c1.hashCode();
        int i = 1;
        
    }
    

//    @Test
//    public void enchanceTest() {
//        CsvCustomerFactory f = new CsvCustomerFactory();
//        //MetaData md = f.getMetaData();
//        CGLibInstantiator<Customer> ins = new CGLibInstantiator<Customer>();
//
//        Customer c0 = new Customer();
//        c0.setCustomerId(333);
//        c0.setFirstName("Greg");
//        c0.setLastName("Oswalds");
//        c0.setCity("Phila");
//        c0.setStreet("123 Main St.");
//
//        //Customer c = ins.enhance(c0);
////        c0 = ins.enhance(c0);
//        c0 = f.attach(c0, PersistentState.CREATED);
//        
//        c0.setCity("New York");
//        
//        //Customer c1 = (Customer) ((DomainObject) c0).detach();
//        //c1.setCity("Boston");
//        
//        f.create(c0);
//        
//        String s0 = c0.getCity();
//
//        Customer c2 = new Customer();
//        c2.setCustomerId(222);
//        c2.setFirstName("John");
//        c2.setLastName("Smith");
//        c2.setCity("Jersey City");
//        c2.setStreet("State Road.");
//        
//        Customer c3 = f.attach(c2);
//        
//        String s2 = c0.getCity();
//        s2 = s2;
//   }
    
//    @Test
//    public void enchanceTest1() {
//        CsvCustomerFactory f = new CsvCustomerFactory();
//        //MetaData md = f.getMetaData();
//        CGLibInstantiator<Customer> ins = new CGLibInstantiator<Customer>();
//        long n = 100000;
//            
//        long ts2 = System.currentTimeMillis();
//        for (int i = 0; i < n; i++) {
//            ins.newInstance(Customer.class);
//        }
//        long ts3 = System.currentTimeMillis();
//        System.out.println("newInstance 1: " + (ts3 - ts2) / 1000d);        
//
//        //Customer c0 = new Customer();
//       // ins.setInstantiationPolicy(InstantiationPolicy.DELEGATE);
//        long ts0 = System.currentTimeMillis();
//        for (int i = 0; i < n; i++) {
//            ins.enhance(new Customer());
//        }
//        long ts1 = System.currentTimeMillis();
//        System.out.println("enhance 2: " + (ts1 - ts0) / 1000d);        
//    }

//    @Test
//    public void enchanceTest2() {
//        CsvCustomerFactory f = new CsvCustomerFactory();
//        //MetaData md = f.getMetaData();
//        CGLibInstantiator<Customer> ins = new CGLibInstantiator<Customer>();
//
//        //Customer c0 = new Customer();
//        ins.setInstantiationPolicy(InstantiationPolicy.DELEGATE);
//        long ts0 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            ins.enhance(new Customer());
//        }
//        long ts1 = System.currentTimeMillis();
//        System.out.println("enhance 1:" + (ts1 - ts0) / 1000d);
//
//        ins.setInstantiationPolicy(InstantiationPolicy.EXTEND);
//        long ts2 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            ins.newInstance(Customer.class);
//        }
//        long ts3 = System.currentTimeMillis();
//        System.out.println("newInstance 2:" + (ts3 - ts2) / 1000d);
//    }
}

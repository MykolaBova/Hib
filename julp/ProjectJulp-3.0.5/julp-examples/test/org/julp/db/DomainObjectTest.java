package org.julp.db;

import org.julp.DataHolder;
import org.julp.DomainObject;
import org.julp.examples.Customer;
import org.julp.examples.POJOCustomer;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainObjectTest {

    public DomainObjectTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testSyncOriginal1() {
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(POJOCustomer.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");

        POJOCustomer c1 = (POJOCustomer) dof.newInstance();
        c1.setCustomerId(1);
        c1.setFirstName("John");
        c1.setLastName("Smith");
        c1.setStreet("123 Main st.");
        c1.setCity("Somecity");

        DataHolder dh = new DataHolder(6);
        dh.setFieldNameAndValue(1, "customerId", 1);
        dh.setFieldNameAndValue(2, "firstName", "Michael");
        dh.setFieldNameAndValue(3, "lastName", "Clancy");
        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
        dh.setFieldNameAndValue(5, "city", "San Francisco");
        dh.setFieldNameAndValue(6, "phone", "4444444444");
        ((DomainObject) c1).setOriginalValues(dh);

        ((DomainObject) c1).syncOriginal();
        DataHolder orig1 = ((DomainObject) c1).getOriginalValues();
        System.out.println(orig1);
    }

    @Test
    public void testSyncOriginal2() {
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(Customer.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");

        Customer c1 = (Customer) dof.newInstance();
        c1.setCustomerId(1);
        c1.setFirstName("John");
        c1.setLastName("Smith");
        c1.setStreet("123 Main st.");
        c1.setCity("Somecity");

        DataHolder dh = new DataHolder(5);
        dh.setFieldNameAndValue(1, "customerId", 1);
        dh.setFieldNameAndValue(2, "firstName", "Michael");
        dh.setFieldNameAndValue(3, "lastName", "Clancy");
        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
        dh.setFieldNameAndValue(5, "city", "San Francisco");

        ((DomainObject) c1).setOriginalValues(dh);

        ((DomainObject) c1).syncOriginal();
        DataHolder orig1 = ((DomainObject) c1).getOriginalValues();
        System.out.println(orig1);
    }

    @Test
    public void load1() {
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(POJOCustomer.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");

        POJOCustomer c1 = (POJOCustomer) dof.newInstance();
        DataHolder dh = new DataHolder(6);
        dh.setFieldNameAndValue(1, "customerId", 1);
        dh.setFieldNameAndValue(2, "firstName", "Michael");
        dh.setFieldNameAndValue(3, "lastName", "Clancy");
        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
        dh.setFieldNameAndValue(5, "city", "San Francisco");
        dh.setFieldNameAndValue(6, "phone", "4444444444");
        ((DomainObject) c1).setOriginalValues(dh);

        ((DomainObject) c1).load();
        
        System.out.println(c1);
    }
    
      @Test
    public void load2() {
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(Customer.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");

        Customer c1 = (Customer) dof.newInstance();

        DataHolder dh = new DataHolder(5);
        dh.setFieldNameAndValue(1, "customerId", 1);
        dh.setFieldNameAndValue(2, "firstName", "Michael");
        dh.setFieldNameAndValue(3, "lastName", "Clancy");
        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
        dh.setFieldNameAndValue(5, "city", "San Francisco");        
        ((DomainObject) c1).setOriginalValues(dh);

        ((DomainObject) c1).load();
        
        System.out.println(c1);
    }
      
      @Test
      public void testGetDisplayValue() {        
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(POJOCustomer.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName, CUSTOMER.CITY=city");

        POJOCustomer c1 = (POJOCustomer) dof.newInstance();

        DataHolder dh = new DataHolder(5);
        dh.setFieldNameAndValue(1, "customerId", 1);
        dh.setFieldNameAndValue(2, "firstName", "Michael");
        dh.setFieldNameAndValue(3, "lastName", "Clancy");
        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
        dh.setFieldNameAndValue(5, "city", "San Francisco");        
        ((DomainObject) c1).setOriginalValues(dh);
        ((DomainObject) c1).load();
        
        ((DomainObject) c1).setDisplayValue("customerId", "Abc");
        ((DomainObject) c1).setDisplayValue("city", "San Francisco, CA");

        System.out.println(((DomainObject) c1).getDisplayValue("customerId"));
        System.out.println(((DomainObject) c1).getDisplayValue("city"));        
        System.out.println(c1);               
      }
}


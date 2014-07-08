package org.julp.db;

import org.julp.DomainObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class DomainObjectFactoryTest {

    public DomainObjectFactoryTest() {
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

//    @Test
//    public void testSetObject() {
//        System.out.println("setObject(DomainObject domainObject)");
//
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        int idx = dof.setObject(c1);
//        assertEquals(0, idx);
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//        idx = dof.setObject(c2);
//        assertEquals(1, idx);
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//        idx = dof.setObject(c3);
//        assertEquals(2, idx);
//
//        CustomerExtendindAbstractDomainObject c4 = new CustomerExtendindAbstractDomainObject();
//        //c3.setCustomerId(3);        
//        c4.setLastName("4");
//        idx = dof.setObject(c4);
//        assertEquals(4, idx);
//    }
//
//    @Test
//    public void testSetObject1() {
//        System.out.println("setObject(int index, DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        dof.setObject(0, c1);
//        assertEquals(Integer.valueOf(1), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(0)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//        dof.setObject(1, c2);
//        assertEquals(Integer.valueOf(2), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(1)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//        dof.setObject(2, c3);
//        assertEquals(Integer.valueOf(3), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(2)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c4 = new CustomerExtendindAbstractDomainObject();
//        c4.setCustomerId(4);
//        c4.setLastName("4");
//        dof.setObject(0, c4);
//        assertEquals(Integer.valueOf(4), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(0)).getCustomerId());
//    }
//
//    @Test
//    public void testAddObject() {
//        System.out.println("addObject(int index, DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = new CustomerExtendindAbstractDomainObject();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        dof.addObject(0, c1);
//        assertEquals(Integer.valueOf(1), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(0)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c2 = new CustomerExtendindAbstractDomainObject();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//        dof.addObject(1, c2);
//        assertEquals(Integer.valueOf(2), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(1)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c3 = new CustomerExtendindAbstractDomainObject();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//        dof.addObject(2, c3);
//        assertEquals(Integer.valueOf(3), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(2)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c4 = new CustomerExtendindAbstractDomainObject();
//        c4.setCustomerId(4);
//        c4.setLastName("4");
//        dof.addObject(0, c4);
//        assertEquals(Integer.valueOf(4), ((CustomerExtendindAbstractDomainObject) dof.getObjects().get(0)).getCustomerId());
//    }
//
//    @Test
//    public void testAddObject1() {
//        System.out.println("addObject(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = new CustomerExtendindAbstractDomainObject();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        int idx = dof.addObject(c1);
//        assertEquals(0, idx);
//    }
//
//    @Test
//    public void testFindObjectByObjectId() {
//        System.out.println("findObjectByObjectId(int objectId)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        assertEquals(Integer.valueOf(1), ((CustomerExtendindAbstractDomainObject) dof.findObjectByObjectId(0)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//        assertEquals(Integer.valueOf(2), ((CustomerExtendindAbstractDomainObject) dof.findObjectByObjectId(1)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//        assertEquals(Integer.valueOf(3), ((CustomerExtendindAbstractDomainObject) dof.findObjectByObjectId(2)).getCustomerId());
//
//        CustomerExtendindAbstractDomainObject c4 = new CustomerExtendindAbstractDomainObject();
//        c4.setCustomerId(4);
//        c4.setLastName("4");
//        dof.addObject(c4);
//        assertEquals(Integer.valueOf(4), ((CustomerExtendindAbstractDomainObject) dof.findObjectByObjectId(3)).getCustomerId());
//    }
//
//    @Test
//    public void testFindIdxByObjectId() {
//        System.out.println("findIdxByObjectId(int objectId)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//        assertEquals(0, dof.findIdxByObjectId(c1.getObjectId()));
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//        assertEquals(1, dof.findIdxByObjectId(c2.getObjectId()));
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//        assertEquals(2, dof.findIdxByObjectId(c3.getObjectId()));
//
//        CustomerExtendindAbstractDomainObject c4 = new CustomerExtendindAbstractDomainObject();
//        c4.setCustomerId(4);
//        c4.setLastName("4");
//        dof.addObject(c4);
//        assertEquals(3, dof.findIdxByObjectId(c4.getObjectId()));
//    }
//
//    @Test
//    public void testDiscard() {
//        System.out.println("discard(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//
//        dof.discard(c2);
//        assertEquals(-1, dof.findIdxByObjectId(c2.getObjectId()));
//    }
//    
    @Test
    public void testDetach() {
        System.out.println("detach(DomainObject domainObject)");
        DomainObjectFactory dof = new DomainObjectFactory();
        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");

//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");

//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//
//        Object detached = dof.detach(c2);
//        assertEquals(-1, dof.findIdxByObjectId(c2.getObjectId()));

        dof.populateMetaData();
        dof.setDomainClass(Customer.class);
       // dof.setInstantiationPolicy(InstantiationPolicy.DELEGATE);

        Customer c4 = new Customer();
        c4.setCustomerId(4);
        c4.setLastName("four");
        Customer c5 = (Customer) dof.attach(c4);
        Integer id5 = c5.getCustomerId();
        c5.setCustomerId(5);
        c5.setLastName("five");

        Customer c6 = (Customer) dof.detach((DomainObject) c5);
        Integer id6 = c6.getCustomerId();
        c6.setCustomerId(6);
        c6.setLastName("six");

        Customer c7 = (Customer) dof.newInstance();
        c7.setCustomerId(7);
        c7.setLastName("seven");

        Customer c8 = (Customer) dof.detach((DomainObject) c7);
        Integer id8 = c8.getCustomerId();
        c8.setCustomerId(8);
        c8.setLastName("eight");

        Integer id = c5.getCustomerId();        
        ((DomainObject) c5).reattach(c8);
        id = c5.getCustomerId();
        Integer test = id;
        
//        
//        Customer c6 = (Customer) ((DomainObject) c5).detach();
//        




        //Customer c9 = (Customer) ((DomainObject) c8).detach();
        c7.getCustomerId();
    }
//
//    @Test
//    public void testRemove() {
//        System.out.println("remove(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//
//        dof.remove(c2);
//        assertEquals(-1, dof.findIdxByObjectId(c2.getObjectId()));
//        assertTrue(dof.getRemovedObjects().contains(c2));
//        assertTrue(((CustomerExtendindAbstractDomainObject) dof.getRemovedObjects().get(0)).getPersistentState() == PersistentState.REMOVED);
//    }
//
//    @Test
//    public void testStore() {
//        System.out.println("store(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(CustomerExtendindAbstractDomainObject.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        CustomerExtendindAbstractDomainObject c1 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//
//        CustomerExtendindAbstractDomainObject c2 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//
//        CustomerExtendindAbstractDomainObject c3 = (CustomerExtendindAbstractDomainObject) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//
//        c2.setOriginalValues(dof.createOriginalValues());
//        dof.store(c2);
//
//        CustomerExtendindAbstractDomainObject c = (CustomerExtendindAbstractDomainObject) dof.findObjectByObjectId(c2.getObjectId());
//        assertTrue(c.getPersistentState() == PersistentState.STORED);
//    }
//
//    @Test
//    public void testCreate() {
//        System.out.println("create(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(POJOCustomer.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        POJOCustomer c1 = (POJOCustomer) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setLastName("1");
//
//        POJOCustomer c2 = (POJOCustomer) dof.newInstance();
//        c2.setCustomerId(2);
//        c2.setLastName("2");
//
//        POJOCustomer c3 = (POJOCustomer) dof.newInstance();
//        c3.setCustomerId(3);
//        c3.setLastName("3");
//
//        dof.create(c2);
//
//        POJOCustomer c = (POJOCustomer) dof.findObjectByObjectId(((DomainObject) c2).getObjectId());
//        assertTrue(((DomainObject) c).getPersistentState() == PersistentState.CREATED);
//    }
//
//    @Test
//    public void testSyncOriginal1() {
//        System.out.println("syncOriginal(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(POJOCustomer.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        POJOCustomer c1 = (POJOCustomer) dof.newInstance();
//        c1.setCustomerId(1);
//        c1.setFirstName("John");
//        c1.setLastName("Smith");
//        c1.setStreet("123 Main st.");
//        c1.setCity("Somecity");
//
//        DataHolder dh = new DataHolder(6);
//        dh.setFieldNameAndValue(1, "customerId", 1);
//        dh.setFieldNameAndValue(2, "firstName", "Michael");
//        dh.setFieldNameAndValue(3, "lastName", "Clancy");
//        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
//        dh.setFieldNameAndValue(5, "city", "San Francisco");
//        dh.setFieldNameAndValue(6, "phone", "4444444444");
//        ((DomainObject) c1).setOriginalValues(dh);
//
//        dof.syncOriginal(c1); 
//        DataHolder orig1 = ((DomainObject) c1).getOriginalValues();        
//    }
//    
//    @Test
//    public void testLoad1() {
//        System.out.println("load(DomainObject domainObject)");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(POJOCustomer.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//
//        POJOCustomer c1 = (POJOCustomer) dof.newInstance();
//        DataHolder dh = new DataHolder(6);
//        dh.setFieldNameAndValue(1, "customerId", 1);
//        dh.setFieldNameAndValue(2, "firstName", "Michael");
//        dh.setFieldNameAndValue(3, "lastName", "Clancy");
//        dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
//        dh.setFieldNameAndValue(5, "city", "San Francisco");
//        dh.setFieldNameAndValue(6, "phone", "4444444444");
//        ((DomainObject) c1).setOriginalValues(dh);
//        
//        dof.load(c1);
//        System.out.println(c1);
//    }   
//    
//    @Test
//    public void illegalClass() {
//        System.out.println("illegalClass");
//        DomainObjectFactory dof = new DomainObjectFactory();
//        dof.setDomainClass(Customer.class);
//        dof.setMapping("CUSTOMER.CUSTOMER_ID=customerId, CUSTOMER.LAST_NAME=lastName");
//        try {
//            dof.store(new Customer());
//        } catch (Throwable t) {
//            System.out.println(t);
//            assertTrue(t.getCause() instanceof java.lang.ClassCastException);
//        }
//    }
}

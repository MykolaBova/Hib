package org.julp;

import java.util.ArrayList;
import java.util.List;
import org.julp.examples.POJOCustomer;
import org.junit.*;
import static org.junit.Assert.assertEquals;

public class GenericPredicateTest {

    public GenericPredicateTest() {
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
    public void testEvaluate() {
        System.out.println("evaluate");
        Object t = null;
        //GenericPredicate instance = new GenericPredicate();

        POJOCustomer c1 = new POJOCustomer();
        c1.setCustomerId(1);
        //c1.setCustomerId(null);
        c1.setFirstName("John");

        POJOCustomer c2 = null;
        //POJOCustomer c2 = new POJOCustomer();
        //c2.setCustomerId(2);
        //c2.setFirstName("Tom");
        POJOCustomer c3 = new POJOCustomer();
        c3.setCustomerId(3);
        c3.setFirstName("Gary");
        List<POJOCustomer> list = new ArrayList<>();
        list.add(c1);
        list.add(c2);
        list.add(c3);

        DataHolder filter = new DataHolder(1);
        filter.setFieldNameAndValue(1, "customerId", Integer.valueOf(1));
        Predicate<POJOCustomer> p = new GenericPredicate<>(filter);
        System.out.println(list);

        List<POJOCustomer> filteredList = new ArrayList<>();
        for (POJOCustomer c : list) {
            if (p.evaluate(c)) {
                filteredList.add(c);
            }
        }
        System.out.println(filteredList);
        int expResult = 1;
        int result = filteredList.get(0).getCustomerId();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    @Test
    public void testFindClass() {
        System.out.println("findClass");
        Object t = new POJOCustomer();
        GenericPredicate instance = new GenericPredicate();
        Class expResult = POJOCustomer.class;
        Class result = instance.findClass(t);
        System.out.println(result);
        assertEquals(expResult, result);

    }

}

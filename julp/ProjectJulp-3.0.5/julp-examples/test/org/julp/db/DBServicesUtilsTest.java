package org.julp.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;

public class DBServicesUtilsTest {

    public DBServicesUtilsTest() {
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
    public void buildInClause() {
        Collection p = new ArrayList();
        p.add("aaa");
        p.add("bbb");
        p.add("ccc");
        p.add("aaa1");
        p.add("bbb1");
        p.add("ccc1");
        p.add("aaa2");
        p.add("bbb2");
        p.add("ccc2");
        p.add("aaa3");
        p.add("bbb3");
        p.add("ccc13");

        DBServicesUtils utils = new DBServicesUtils();
        CharSequence in = utils.buildInClause("col_1", p, 5);
        System.out.println(in);

        assertEquals("(col_1 in (?,?,?,?,?) or col_1 in (?,?,?,?,?) or col_1 in (?,?))", in.toString());
    }

    @Test
    public void buildInClause1() {
        Collection<String> columns = new ArrayList<>();
        columns.add("col_1");
        columns.add("col_2");

        Collection<Collection<?>> params = new ArrayList<>();

        Collection p0 = new ArrayList();
        p0.add("aaa");
        p0.add("bbb");
        params.add(p0);

        Collection p1 = new ArrayList();
        p1.add("aaa");
        p1.add("bbb");
        params.add(p1);

        Collection p2 = new ArrayList();
        p2.add("aaa");
        p2.add("bbb");
        params.add(p2);

        Collection p3 = new ArrayList();
        p3.add("aaa");
        p3.add("bbb");
        params.add(p3);

        Collection p4 = new ArrayList();
        p4.add("aaa");
        p4.add("bbb");
        params.add(p4);

        Collection p5 = new ArrayList();
        p5.add("aaa");
        p5.add("bbb");
        params.add(p5);

        Collection p6 = new ArrayList();
        p6.add("aaa");
        p6.add("bbb");
        params.add(p6);

        Collection p7 = new ArrayList();
        p7.add("aaa");
        p7.add("bbb");
        params.add(p7);

        Collection p8 = new ArrayList();
        p8.add("aaa");
        p8.add("bbb");
        params.add(p8);

        Collection p9 = new ArrayList();
        p9.add("aaa");
        p9.add("bbb");
        params.add(p9);

        DBServicesUtils utils = new DBServicesUtils();
        CharSequence in = utils.buildInClause(columns, params, 4);
        System.out.println(in);
        
        assertEquals("((col_1,col_2) in ((?,?),(?,?)) or (col_1,col_2) in ((?,?),(?,?)) or (col_1,col_2) in ((?,?),(?,?)) or (col_1,col_2) in ((?,?),(?,?)) or (col_1,col_2) in ((?,?),(?,?)))", in.toString());
    }
    
     @Test
    public void buildInClause2() {
        Collection<String> columns = new ArrayList<String>();
        columns.add("col_1");
        columns.add("col_2");
        columns.add("col_3");

        Collection<Collection<?>> params = new ArrayList<Collection<?>>();

        Collection p0 = new ArrayList();
        p0.add("aaa");
        p0.add("bbb");
        p0.add("bbb");
        params.add(p0);

        Collection p1 = new ArrayList();
        p1.add("aaa");
        p1.add("bbb");
        p0.add("bbb");
        params.add(p1);

        Collection p2 = new ArrayList();
        p2.add("aaa");
        p2.add("bbb");
        p0.add("bbb");
        params.add(p2);

        Collection p3 = new ArrayList();
        p3.add("aaa");
        p3.add("bbb");
        p0.add("bbb");
        params.add(p3);

        Collection p4 = new ArrayList();
        p4.add("aaa");
        p4.add("bbb");
        p0.add("bbb");
        params.add(p4);

        Collection p5 = new ArrayList();
        p5.add("aaa");
        p5.add("bbb");
        p0.add("bbb");
        params.add(p5);

        Collection p6 = new ArrayList();
        p6.add("aaa");
        p6.add("bbb");
        p0.add("bbb");
        params.add(p6);

        Collection p7 = new ArrayList();
        p7.add("aaa");
        p7.add("bbb");
        p0.add("bbb");
        params.add(p7);

        Collection p8 = new ArrayList();
        p8.add("aaa");
        p8.add("bbb");
        p0.add("bbb");
        params.add(p8);

        Collection p9 = new ArrayList();
        p9.add("aaa");
        p0.add("bbb");
        p9.add("bbb");
        params.add(p9);

        DBServicesUtils utils = new DBServicesUtils();
        CharSequence in = utils.buildInClause(columns, params, 10);
        System.out.println(in);
        
        assertEquals("((col_1,col_2,col_3) in ((?,?,?),(?,?,?),(?,?,?)) or (col_1,col_2,col_3) in ((?,?,?),(?,?,?),(?,?,?)) or (col_1,col_2,col_3) in ((?,?,?),(?,?,?),(?,?,?)) or (col_1,col_2,col_3) in ((?,?,?)))", in.toString());
    }
}

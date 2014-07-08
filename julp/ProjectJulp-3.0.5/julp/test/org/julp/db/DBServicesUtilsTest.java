package org.julp.db;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.julp.DataHolder;
import org.julp.ValueObject;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class DBServicesUtilsTest {
    
    public DBServicesUtilsTest() {
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

    /**
     * Test of convert method, of class DBServicesUtils.
     */
    @Test
    public void testConvert() {
        System.out.println("convert");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.convert(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of formatDate method, of class DBServicesUtils.
     */
    @Test
    public void testConvertDate() {
        System.out.println("formatDate");
        Date d = null;
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.formatDate(d);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of formatTime method, of class DBServicesUtils.
     */
    @Test
    public void testConvertTime() {
        System.out.println("formatTime");
        Time t = null;
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.formatTime(t);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of formatTimestamp method, of class DBServicesUtils.
     */
    @Test
    public void testConvertTimestamp() {
        System.out.println("formatTimestamp");
        Timestamp ts = null;
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.formatTimestamp(ts);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of formatDecimal method, of class DBServicesUtils.
     */
    @Test
    public void testConvertDecimal() {
        System.out.println("formatDecimal");
        Number n = null;
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.formatDecimal(n);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDateFormat method, of class DBServicesUtils.
     */
    @Test
    public void testGetDateFormat() {
        System.out.println("getDateFormat");
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.getDateFormat();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setDateFormat method, of class DBServicesUtils.
     */
    @Test
    public void testSetDateFormat() {
        System.out.println("setDateFormat");
        String dateFormat = "";
        DBServicesUtils instance = new DBServicesUtils();
        instance.setDateFormat(dateFormat);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTimestampFormat method, of class DBServicesUtils.
     */
    @Test
    public void testGetTimestampFormat() {
        System.out.println("getTimestampFormat");
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.getTimestampFormat();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setTimestampFormat method, of class DBServicesUtils.
     */
    @Test
    public void testSetTimestampFormat() {
        System.out.println("setTimestampFormat");
        String timestampFormat = "";
        DBServicesUtils instance = new DBServicesUtils();
        instance.setTimestampFormat(timestampFormat);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTimeFormat method, of class DBServicesUtils.
     */
    @Test
    public void testGetTimeFormat() {
        System.out.println("getTimeFormat");
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.getTimeFormat();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setTimeFormat method, of class DBServicesUtils.
     */
    @Test
    public void testSetTimeFormat() {
        System.out.println("setTimeFormat");
        String timeFormat = "";
        DBServicesUtils instance = new DBServicesUtils();
        instance.setTimeFormat(timeFormat);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDecimalFormat method, of class DBServicesUtils.
     */
    @Test
    public void testGetDecimalFormat() {
        System.out.println("getDecimalFormat");
        DBServicesUtils instance = new DBServicesUtils();
        String expResult = "";
        String result = instance.getDecimalFormat();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setDecimalFormat method, of class DBServicesUtils.
     */
    @Test
    public void testSetDecimalFormat() {
        System.out.println("setDecimalFormat");
        String decimalFormat = "";
        DBServicesUtils instance = new DBServicesUtils();
        instance.setDecimalFormat(decimalFormat);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersArray_3args() throws Exception {
        System.out.println("getResultAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultAsDataHoldersArray(sql, params, populateColumnNames);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersArray_String_boolean() throws Exception {
        System.out.println("getResultAsDataHoldersArray");
        String sql = "";
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultAsDataHoldersArray(sql, populateColumnNames);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersArray_String() throws Exception {
        System.out.println("getResultAsDataHoldersArray");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultAsDataHoldersArray(sql);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersArray_String_Collection() throws Exception {
        System.out.println("getResultAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultAsDataHoldersArray(sql, params);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersArray_String_Collection() throws Exception {
        System.out.println("getResultsAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultsAsDataHoldersArray(sql, params);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersArray_3args() throws Exception {
        System.out.println("getResultsAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultsAsDataHoldersArray(sql, params, populateColumnNames);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersArray_4args() throws Exception {
        System.out.println("getResultsAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultsAsDataHoldersArray(sql, params, maxNumberOfParams, placeHolder);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersArray method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersArray_5args() throws Exception {
        System.out.println("getResultsAsDataHoldersArray");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        DataHolder[] expResult = null;
        DataHolder[] result = instance.getResultsAsDataHoldersArray(sql, params, maxNumberOfParams, placeHolder, populateColumnNames);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleValue method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleValue_String() throws Exception {
        System.out.println("getSingleValue");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        Object expResult = null;
        Object result = instance.getSingleValue(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleValue method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleValue_String_Collection() throws Exception {
        System.out.println("getSingleValue");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Object expResult = null;
        Object result = instance.getSingleValue(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleColumnResultAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleColumnResultAsList_String() throws Exception {
        System.out.println("getSingleColumnResultAsList");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        List expResult = null;
        List result = instance.getSingleColumnResultAsList(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleColumnResultAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleColumnResultAsList_String_Collection() throws Exception {
        System.out.println("getSingleColumnResultAsList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List expResult = null;
        List result = instance.getSingleColumnResultAsList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleColumnResultsAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleColumnResultsAsList_4args() throws Exception {
        System.out.println("getSingleColumnResultsAsList");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        List expResult = null;
        List result = instance.getSingleColumnResultsAsList(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getSingleColumnResultsAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetSingleColumnResultsAsList_String_List() throws Exception {
        System.out.println("getSingleColumnResultsAsList");
        String sql = "";
        List params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List expResult = null;
        List result = instance.getSingleColumnResultsAsList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultAsLinkedMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultAsLinkedMap_String() throws Exception {
        System.out.println("getTwoColumnsResultAsLinkedMap");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultAsLinkedMap(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultAsLinkedMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultAsLinkedMap_String_Collection() throws Exception {
        System.out.println("getTwoColumnsResultAsLinkedMap");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultAsLinkedMap(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultsAsLinkedMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultsAsLinkedMap() throws Exception {
        System.out.println("getTwoColumnsResultsAsLinkedMap");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultsAsLinkedMap(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultsAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultsAsMap() throws Exception {
        System.out.println("getTwoColumnsResultsAsMap");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultsAsMap(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultAsMap_String() throws Exception {
        System.out.println("getTwoColumnsResultAsMap");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultAsMap(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getTwoColumnsResultAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetTwoColumnsResultAsMap_String_Collection() throws Exception {
        System.out.println("getTwoColumnsResultAsMap");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Map expResult = null;
        Map result = instance.getTwoColumnsResultAsMap(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsValueObjectList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsValueObjectList_String() throws Exception {
        System.out.println("getResultAsValueObjectList");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<ValueObject<?>> expResult = null;
        List<ValueObject<Object>> result = instance.getResultAsValueObjectList(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsValueObjectList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsValueObjectList_String_Collection() throws Exception {
        System.out.println("getResultAsValueObjectList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<ValueObject<Object>> expResult = null;
        List<ValueObject<Object>> result = instance.getResultAsValueObjectList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsValueObjectList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsValueObjectList_4args() throws Exception {
        System.out.println("getResultsAsValueObjectList");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<ValueObject<Object>> expResult = null;
        List<ValueObject<Object>> result = instance.getResultsAsValueObjectList(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsValueObjectList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsValueObjectList_String_Collection() throws Exception {
        System.out.println("getResultsAsValueObjectList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<ValueObject<Object>> expResult = null;
        List<ValueObject<Object>> result = instance.getResultsAsValueObjectList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersList_3args() throws Exception {
        System.out.println("getResultAsDataHoldersList");
        String sql = "";
        Collection params = null;
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultAsDataHoldersList(sql, params, populateColumnNames);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersList_String_boolean() throws Exception {
        System.out.println("getResultAsDataHoldersList");
        String sql = "";
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultAsDataHoldersList(sql, populateColumnNames);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersList_String() throws Exception {
        System.out.println("getResultAsDataHoldersList");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultAsDataHoldersList(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsDataHoldersList_String_Collection() throws Exception {
        System.out.println("getResultAsDataHoldersList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultAsDataHoldersList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersList_String_Collection() throws Exception {
        System.out.println("getResultsAsDataHoldersList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultsAsDataHoldersList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersList_4args() throws Exception {
        System.out.println("getResultsAsDataHoldersList");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultsAsDataHoldersList(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersList_5args() throws Exception {
        System.out.println("getResultsAsDataHoldersList");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultsAsDataHoldersList(sql, params, maxNumberOfParams, placeHolder, populateColumnNames);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsDataHoldersList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsDataHoldersList_3args() throws Exception {
        System.out.println("getResultsAsDataHoldersList");
        String sql = "";
        Collection params = null;
        boolean populateColumnNames = false;
        DBServicesUtils instance = new DBServicesUtils();
        List<DataHolder> expResult = null;
        List<DataHolder> result = instance.getResultsAsDataHoldersList(sql, params, populateColumnNames);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsList_4args() throws Exception {
        System.out.println("getResultsAsList");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<List<?>> expResult = null;
        List<List<?>> result = instance.getResultsAsList(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsList_String_Collection() throws Exception {
        System.out.println("getResultsAsList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<List<?>> expResult = null;
        List<List<?>> result = instance.getResultsAsList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsList_String() throws Exception {
        System.out.println("getResultAsList");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<List<?>> expResult = null;
        List<List<?>> result = instance.getResultAsList(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsList method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsList_String_Collection() throws Exception {
        System.out.println("getResultAsList");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<List<?>> expResult = null;
        List<List<?>> result = instance.getResultAsList(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsMap_String_Collection() throws Exception {
        System.out.println("getResultsAsMap");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Map<String, List<?>> expResult = null;
        Map<String, List<?>> result = instance.getResultsAsMap(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultsAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultsAsMap_4args() throws Exception {
        System.out.println("getResultsAsMap");
        String sql = "";
        Collection params = null;
        int maxNumberOfParams = 0;
        String placeHolder = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map<String, List<?>> expResult = null;
        Map<String, List<?>> result = instance.getResultsAsMap(sql, params, maxNumberOfParams, placeHolder);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsMap_String_Collection() throws Exception {
        System.out.println("getResultAsMap");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Map<String, List<?>> expResult = null;
        Map<String, List<?>> result = instance.getResultAsMap(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsMap method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsMap_String() throws Exception {
        System.out.println("getResultAsMap");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        Map<String, List<?>> expResult = null;
        Map<String, List<?>> result = instance.getResultAsMap(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of buildInClause method, of class DBServicesUtils.
     */
    @Test
    public void testBuildInClause_3args_1() {
        System.out.println("buildInClause");
        String columnName = "";
        Collection params = null;
        int max = 0;
        DBServicesUtils instance = new DBServicesUtils();
        CharSequence expResult = null;
        CharSequence result = instance.buildInClause(columnName, params, max);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of flattenMultiColumnsParams method, of class DBServicesUtils.
     */
    @Test
    public void testFlattenMultiColumnsParams() {
        System.out.println("flattenMultiColumnsParams");
        Collection<Collection<?>> params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Collection expResult = null;
        Collection result = instance.flattenMultiColumnsParams(params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of buildInClause method, of class DBServicesUtils.
     */
    @Test
    public void testBuildInClause_3args_2() {
        System.out.println("buildInClause");
        Collection<String> columnNames = null;
        Collection<Collection<?>> params = null;
        int max = 0;
        DBServicesUtils instance = new DBServicesUtils();
        CharSequence expResult = null;
        CharSequence result = instance.buildInClause(columnNames, params, max);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getDBServices method, of class DBServicesUtils.
     */
    @Test
    public void testGetDBServices() {
        System.out.println("getDBServices");
        DBServicesUtils instance = new DBServicesUtils();
        DBServices expResult = null;
        DBServices result = instance.getDBServices();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of setDBServices method, of class DBServicesUtils.
     */
    @Test
    public void testSetDBServices() {
        System.out.println("setDBServices");
        DBServices dbServices = null;
        DBServicesUtils instance = new DBServicesUtils();
        instance.setDBServices(dbServices);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsListOfMaps method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsListOfMaps_String() throws Exception {
        System.out.println("getResultAsListOfMaps");
        String sql = "";
        DBServicesUtils instance = new DBServicesUtils();
        List<Map<String, ?>> expResult = null;
        List<Map<String, ?>> result = instance.getResultAsListOfMaps(sql);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of getResultAsListOfMaps method, of class DBServicesUtils.
     */
    @Test
    public void testGetResultAsListOfMaps_String_Collection() throws Exception {
        System.out.println("getResultAsListOfMaps");
        String sql = "";
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        List<Map<String, ?>> expResult = null;
        List<Map<String, ?>> result = instance.getResultAsListOfMaps(sql, params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of buildInClause method, of class DBServicesUtils.
     */
    @Test
    public void testBuildInClause_Collection() {
        System.out.println("buildInClause");
        Collection params = null;
        DBServicesUtils instance = new DBServicesUtils();
        CharSequence expResult = null;
        CharSequence result = instance.buildInClause(params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of flattenParams method, of class DBServicesUtils.
     */
    @Test
    public void testFlattenParams() {
        System.out.println("flattenParams");
        Collection<Collection<?>> params = null;
        DBServicesUtils instance = new DBServicesUtils();
        Collection expResult = null;
        Collection result = instance.flattenParams(params);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}

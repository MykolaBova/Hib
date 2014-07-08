package org.julp.examples;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.*;
import org.julp.db.DBDataWriter;
import org.julp.db.DBMetaData;
import org.julp.db.DBServicesUtils;
import org.julp.db.DomainObjectFactory;
import org.julp.db.OptimisticLock;
import org.julp.db.StatementHolder;
import org.julp.search.SearchCriteriaBuilder;
import org.julp.search.SearchCriteriaHolder;

public class CustomerFactory extends DomainObjectFactory {

    private static final long serialVersionUID = -4728204400024324456L;
    protected Properties queries = null;
    private Logger logger = Logger.getLogger(getClass().getName());
    private DBServicesUtils utils = new DBServicesUtils();

    public CustomerFactory() {
        /* IT IS NOT NECESSARY TO LOAD MAPPINGS THIS WAY, COULD BE ANYTHING: XML, JNDI,  ETC... */
        queries = loadProperties("Customer.sql");
        loadMappings("Customer.properties");
        setDomainClass(Customer.class);
    }

    public void selectAs() {
        logger.info("\n======================= this is an example of using some methods to populate collections ===========================\n");
        try {
            loadMappings("Customer.properties");
            String sql = queries.getProperty("findAllCustomersWithPhone");
            logger.info("============== as ValueObjects list (first column value and second column display) ==================");            
            utils.setDBServices(dbServices);
            List list = utils.getResultAsValueObjectList(sql);
            logger.info(list.toString());
            logger.info("============== as List of Lists ==================");            
            List list1 = utils.getResultAsList(sql);
            logger.info(list1.toString());
            logger.info("============== as Map of Lists (key=column name, value=list of values for the column) ==================");            
            Map map = utils.getResultAsMap(sql);
            logger.info(map.toString());      
            logger.info("============== as DataHolders array ==================");        
            DataHolder[] dataHolder = utils.getResultAsDataHoldersArray(sql, false);
            for (int i = 0; i < dataHolder.length; i++) {
                logger.info(dataHolder[i].toString());
            }
            String lastName = utils.getSingleValue("select LAST_NAME from CUSTOMER where CUSTOMER_ID = 1");
            logger.info("last name: " + lastName);
            logger.info("============== as List of Maps (key is a columns name and value is a column value) ==================");   
            List<Map<String, ?>> listOfMaps = utils.getResultAsListOfMaps(sql);
            for (Map<String, ?> rowMap : listOfMaps) {
                logger.info(rowMap.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                dbServices.release(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void inClause() {
        logger.info("\n======================= this is an example of using 'IN' CLAUSE \n======================= and using SELECT with number of parameters \n======================= more than DBMS can except (like more then 1000 in Oracle) to populate objectList ===========================\n");
        try {
            dbServices.setMaxNumberOfParams(3);
            dbServices.setPlaceHolder(":#"); // could be any String            
            dbServices.setCacheStatements(true);
            utils.setDBServices(dbServices);
            loadMappings("Customer.properties");

            List argsList = new ArrayList();
            String sql = queries.getProperty("inClause1");

            //List city = new ArrayList();
            //city.add("City1");
            //city.add("City2");
            //city.add("City3");
            //
            //List phone = new ArrayList();
            //phone.add("1111111111");
            //phone.add("2222222222");

            List custId = new ArrayList();
            custId.add(0);
            custId.add(1);
            custId.add(2);
            custId.add(3);
            custId.add(4);
            custId.add(5);
            custId.add(6);
            custId.add(7);

            argsList.add("John Doh");
            //argsList.add(city);
            //argsList.add(phone);
            argsList.add(custId);
            ResultSet[] results = dbServices.getResultSets(sql, argsList);
            Wrapper[] resultWrappers = new Wrapper[results.length];
            int i = 0;
            for (ResultSet rs : results) {
                resultWrappers[i] = new Wrapper(rs);
                i++;
            }
            load(resultWrappers);

            Iterator it1 = getObjectList().iterator();
            while (it1.hasNext()) {
                logger.info(it1.next().toString());
            }
            dbServices.release(false);
            logger.info("==================================");

            //List list = dbServices.getResultsAsDataHoldersList(sql, argsList, false);
            List list = utils.getResultsAsDataHoldersList(sql, argsList, true);

            Iterator it2 = list.iterator();
            while (it2.hasNext()) {
                logger.info(it2.next().toString());
            }
            
            logger.info("==================================");
            
            sql = queries.getProperty("inClause2");
            List<String> names = new ArrayList<String>();
            names.add("James");
            names.add("Andrew");
            names.add("Anne");
            names.add("Julia");
            names.add("George");
            names.add("Laura");
            CharSequence in1 = utils.buildInClause("first_name", names, 2); // see method API
            sql = sql.replace(":#", in1);
            List<DataHolder> result = (List<DataHolder>) utils.getResultsAsDataHoldersList(sql, names, true);
            
           for (DataHolder dh : result) {
               System.out.println(dh.toString());
           }
            
           logger.info("==================================");
           
            sql = queries.getProperty("inClause2");
            Collection<Collection<?>> nameList = new ArrayList<>();
            nameList.add(Arrays.asList(new String[] {"Laura", "Steel"}));
            nameList.add(Arrays.asList(new String[] {"Susanne", "King"}));
            nameList.add(Arrays.asList(new String[] {"Anne", "Miller"}));
            nameList.add(Arrays.asList(new String[] {"Michael", "Clancy"}));
            nameList.add(Arrays.asList(new String[] {"Sylvia", "Ringer"}));
            nameList.add(Arrays.asList(new String[] {"Laura", "Miller"}));
            nameList.add(Arrays.asList(new String[] {"Laura", "White"}));
            nameList.add(Arrays.asList(new String[] {"James", "Peterson"}));
            nameList.add(Arrays.asList(new String[] {"Andrew", "Miller"}));                                          
            Collection<String> cols = Arrays.asList(new String[] {"first_name", "last_name"});
            CharSequence in2 = utils.buildInClause(cols, nameList, 6); // see method API
            sql = sql.replace(":#", in2);            
            Collection list1 = utils.flattenMultiColumnsParams(nameList);                        
            List<DataHolder> result2 = (List<DataHolder>) utils.getResultsAsDataHoldersList(sql, list1, true);
            
           for (DataHolder dh : result2) {
               System.out.println(dh.toString());
           }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                dbServices.release(true);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public int findAllCustomers() {
        int records = 0;
        try {
            String sql = queries.getProperty("findAllCustomers");
            this.dbServices.setResultSetType(ResultSet.TYPE_SCROLL_INSENSITIVE);
            this.dbServices.setConcurrency(ResultSet.CONCUR_READ_ONLY);            
            records = this.load(new Wrapper(this.dbServices.getResultSet(sql)), 0, 10);
            printAllCustomers();
            System.out.println("==============");
            records = this.load(new Wrapper(this.dbServices.getResultSet(sql)), 10, 10);
            printAllCustomers();
            System.out.println("==============");
            records = this.load(new Wrapper(this.dbServices.getResultSet(sql)), 20, 10);
            printAllCustomers();
            records = this.load(new Wrapper(this.dbServices.getResultSet(sql)));
            printAllCustomers();
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        return records;
    }
    
//        public int findAllCustomers() {
//        int records = 0;
//        try {
//            String sql = queries.getProperty("findAllCustomers");
//            records = this.load(new Wrapper(this.dbServices.getResultSet(sql)));
//            printAllCustomers();
//        } catch (Exception sqle) {
//            throw new DataAccessException(sqle);
//        }
//        return records;
//    }

    public int findAllCustomersWithPhone() {
        logger.info("\n======================= this is an example of using inheritance ===========================\n");
        this.setDomainClass(CustomerWithPhone.class);
        loadMappings("CustomersWithPhone.properties");

        /*or:  comment line above: setMapping(loadMappings("CustomersWithPhone.properties"));
        and uncomment two lines below */

        //setMapping((Map) loadMappings("Customer.properties"));
        //this.mapping.put("CUSTOMER.PHONE", "phone");

        int records = 0;
        try {
            records = this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("findAllCustomersWithPhone"))));
            printAllCustomers();
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        return records;
    }

    public void createAndStoreCustomers() {                        
        logger.info("\n======================= this is an example of creating and storing objects (INSERT & UPDATE) ===========================\n");
        this.setDomainClass(CustomerWithPhone.class);
        //options.put(DBDataWriter.Options.dataModificationSequence, new char[] {DATA_MODIFICATION_SEQUENCE_INSERT});
        options.put(DBDataWriter.Options.optimisticLock, OptimisticLock.KEY_AND_UPDATEBLE_COLUMNS);
        //setOptions(options);
        ((DBDataWriter) getDataWriter()).setBatchEnabled(true);
        loadMappings("CustomersWithPhone.properties");
        int records = findAllCustomers();
        System.out.print(" ======== findAllCustomers::records: " + records);

        //remove(getObjectList().get(5));
        // remove(getObjectList().get(6));
        //remove(getObjectList().get(7));

        ListIterator li = this.objectList.listIterator();
        while (li.hasNext()) {
            CustomerWithPhone customer = (CustomerWithPhone) li.next();
            String lastName = customer.getLastName();
//            if (lastName.equals("Miller")) {
            customer.setPhone("222222222222");
            ((DomainObject) customer).store();
//            }

            if (lastName.equals("Peterson")) {
                customer.setPhone("1212121212");
                ((DomainObject) customer).store();
            }
        }

        CustomerWithPhone customerWithPhone1 = (CustomerWithPhone) newInstance();
        // this is NOT proper way to genarate id
        customerWithPhone1.setCustomerId(new Integer(records + 1));
        customerWithPhone1.setFirstName("Joe");
        customerWithPhone1.setLastName("Doe");
        customerWithPhone1.setStreet("Main st.");
        customerWithPhone1.setCity("SomeTown");
        customerWithPhone1.setPhone("7777777777");

        this.create(customerWithPhone1);
        logger.info("\ncreated customer: " + customerWithPhone1 + "\n");

        CustomerWithPhone customerWithPhone2 = (CustomerWithPhone) newInstance();
        // this is NOT proper way to genarate id
        //customerWithPhone2.setCustomerId(5);
        customerWithPhone2.setCustomerId(new Integer(records + 2));
        customerWithPhone2.setFirstName("Mfs");
        customerWithPhone2.setLastName("Doesdsdsdsd");
        customerWithPhone2.setStreet("Main st.");
        customerWithPhone2.setCity("SomeTown");
        customerWithPhone2.setPhone("2342342344");

        this.create(customerWithPhone2);
        logger.info("\ncreated customer: " + customerWithPhone2 + "\n");

        CustomerWithPhone customerWithPhone3 = (CustomerWithPhone) newInstance();
        // this is NOT proper way to genarate id
        customerWithPhone3.setCustomerId(new Integer(records + 3));
        customerWithPhone3.setFirstName("Rwppw");
        customerWithPhone3.setLastName("Rfffff");
        customerWithPhone3.setStreet("Main st.");
        customerWithPhone3.setCity("SomeTown");
        customerWithPhone3.setPhone("444444444");

        this.create(customerWithPhone3);
        logger.info("\ncreated customer: " + customerWithPhone3 + "\n");

        /*       another way:
        customerWithPhone.create();
        customerWithPhone.setObjectId(this.getNextObjectId());
        this.getObjectList().add(product);
        or:
        customerWithPhone.create();
        this.setObject(customerWithPhone);
         */

        logger.info("\n======================= this is after data modifications ===========================\n");
        //printAllCustomers();

        try {            
            this.dbServices.beginTran();
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (success) {
                this.dbServices.commitTran();
            } else {
                throw new Exception("Data modification: failed");
            }
            this.synchronizePersistentState();            
        } catch (Throwable t) {
            try {
                this.dbServices.rollbackTran();
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
            t.printStackTrace();
        } finally {
            try {
                this.dbServices.release(true);                
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }

        logger.info("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
        //printAllCustomers();
    }

    public void createPOJOCustomerNoExtending() {                
        logger.info("\n======================= this is an example of creating objects (INSERT & UPDATE)  ===========================\n");
        logger.info("\n======================= this is an example of using domain object without extending AbstractDomainObject by using CGLib/ASM libs =================\n");
        this.setDomainClass(POJOCustomer.class);
        loadMappings("CustomersWithPhone.properties");
        //((DBDataWriter) getDataWriter()).setBatchEnabled(false);
        options = new HashMap<Enum, Object>();
        options.put(DBDataWriter.Options.optimisticLock, OptimisticLock.KEY_AND_MODIFIED_COLUMNS);
        options.put(DBDataWriter.Options.noFullColumnName, true);
        //setOptions(opt);
        getDBServices().setAudit(true);
        getDBServices().setCacheStatements(true);
        try {
            load(new Wrapper(dbServices.getResultSet(queries.getProperty("findAllCustomers"))));
        } catch (SQLException ex) {
            Logger.getLogger(CustomerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }

        logger.info("\n======================= filter ===========================\n");
        DataHolder filter = new DataHolder(1);
        filter.setFieldNameAndValue(1, "city", "Dallas");
        Predicate p = new GenericPredicate(filter);

        Collection<POJOCustomer> customers = filter(p);
        for (POJOCustomer customer : customers) {     
            ((DomainObject) customer).addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                   System.out.println(evt.getPropertyName() + " - old value: " + evt.getOldValue() + ", new value: " + evt.getNewValue());
                }
            });
                if (customer.getPhone() == null) {
                    customer.setPhone("1234567890");                    
                    store(customer);
                }
               System.out.println("Customer from Dallas: " + customer.toString());
        }

        POJOCustomer testCustomer = (POJOCustomer) this.newInstance();    
        // this is NOT the proper way to generate id 
        testCustomer.setCustomerId(Integer.MAX_VALUE);
        testCustomer.setFirstName("Joe");
        testCustomer.setLastName("Doe");
        testCustomer.setStreet("Main st.");
        testCustomer.setCity("SomeTown");
        testCustomer.setPhone("7777777777");

        this.create(testCustomer);
        logger.info("\ncreated customer: " + testCustomer + "\n");

        logger.info("\n======================= this is after data modifications ===========================\n");

        try {
            this.getDBServices().beginTran();
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (success) {
                this.getDBServices().commitTran();
            } else {
                throw new Exception("Data modification: failed");
            }
            this.synchronizePersistentState();
        } catch (Throwable t) {
            try {
                this.getDBServices().rollbackTran();
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
            t.printStackTrace();
        } finally {
            try {
                this.getDBServices().release(true);
                logger.info("\n======================= this is an example of using Audit ===========================\n");
                this.dbServices.setAudit(false);
                List<StatementHolder> auditTrail = this.dbServices.getAuditTrail();
                for (StatementHolder audit : auditTrail) {
                    System.out.println(audit);
                }
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }

        logger.info("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
        printAllCustomers();
    }

    public void createAndStoreCustomersNoExtending() {
        logger.info("\n======================= this is an example of creating and storing objects (INSERT & UPDATE)  ===========================\n");
        logger.info("\n======================= this is an example of using domain object without extending abstract DomainObject =================\n");
        this.setDomainClass(CustomerNoExtending.class);
        loadMappings("CustomersWithPhone.properties");
        ((DBDataWriter) getDataWriter()).setBatchEnabled(false);
        options = new HashMap<Enum, Object>();
        options.put(DBDataWriter.Options.optimisticLock, OptimisticLock.KEY_AND_MODIFIED_COLUMNS);
        options.put(DBDataWriter.Options.noFullColumnName, true);
        //setOptions(opt);

        getDBServices().setCacheStatements(true);
        int records = findAllCustomers();
        CustomerNoExtending testCustomer = (CustomerNoExtending) newInstance();

        //boolean b = testCustomer instanceof CustomerNoExtending;
        boolean b1 = testCustomer.getClass() == CustomerNoExtending.class;

        // this is NOT proper way to genarate id
        testCustomer.setCustomerId(records + 1);
        testCustomer.setFirstName("Joe");
        testCustomer.setLastName("Doe");
        testCustomer.setStreet("Main st.");
        testCustomer.setCity("SomeTown");
        testCustomer.setPhone("7777777777");

        this.create((DomainObject) testCustomer);
        logger.info("\ncreated customer: " + testCustomer + "\n");

        ListIterator li = this.objectList.listIterator();
        while (li.hasNext()) {
            CustomerNoExtending customer = (CustomerNoExtending) li.next();
            String lastName = customer.getLastName();
            if (lastName.equals("Miller")) {
                customer.setPhone("8888888888");
                ((DomainObject) customer).store();
            }
        }

        logger.info("\n======================= this is after data modifications ===========================\n");
        printAllCustomers();

        try {
            this.getDBServices().beginTran();
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (success) {
                this.getDBServices().commitTran();
            } else {
                throw new Exception("Data modification: failed");
            }
            this.synchronizePersistentState();
        } catch (Throwable t) {
            try {
                this.getDBServices().rollbackTran();
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
            t.printStackTrace();
        } finally {
            try {
                this.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }

        logger.info("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
        //printAllCustomers();
    }

    public void findCustomer() {
        logger.info("\n======================= this is an example of using Criteria  ===========================\n");
        loadMappings("Customer.properties");
        this.populateMetaData();
        List holders = new ArrayList(2);
        SearchCriteriaHolder searchCriteriaHolder1 = new SearchCriteriaHolder();
        searchCriteriaHolder1.setFieldName("lastName");
        searchCriteriaHolder1.setOperator(SearchCriteriaHolder.LIKE);
        searchCriteriaHolder1.setFunctions("UPPER(${})"); //UPPER(col_name)
        searchCriteriaHolder1.setSearchValue("MI%");
        searchCriteriaHolder1.setBooleanCondition(SearchCriteriaHolder.OR);
        holders.add(searchCriteriaHolder1);
        SearchCriteriaHolder searchCriteriaHolder2 = new SearchCriteriaHolder();
        searchCriteriaHolder2.setFieldName("lastName");
        searchCriteriaHolder2.setOperator(SearchCriteriaHolder.LIKE);
        searchCriteriaHolder2.setFunctions("UPPER(${})");
        searchCriteriaHolder2.setSearchValue("S%");
        holders.add(searchCriteriaHolder2);
        SearchCriteriaBuilder criteria = new SearchCriteriaBuilder();
        criteria.setMetaData((DBMetaData) this.metaData);
        criteria.setSearchCriteriaHolders(holders);
        criteria.buildCriteria();
        String select = this.queries.getProperty("findAllCustomers");
        criteria.setSelect(select);
        String sql = criteria.getQuery();
        Collection params = criteria.getArguments();
        try {
            this.load(new Wrapper(this.dbServices.getResultSet(sql, params)));
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        printAllCustomers();
    }

    protected void printAllCustomers() {
        List customers = this.getObjectList();
        Iterator iter = customers.iterator();
        while (iter.hasNext()) {
            Object customer = iter.next();
            System.out.println(customer.toString());
        }
        System.out.println();
    }

    protected void customerWithInvoices() {
        Logger.getLogger(getClass().getName()).info("\n======================= This is an example of using filter ===========================\n");
        this.setDomainClass(Customer.class);
        InvoiceFactory invoiceFactory = new InvoiceFactory();
        invoiceFactory.setDBServices(dbServices);
        invoiceFactory.loadAllInvoices();

        loadMappings("Customer.properties");

        try {
            this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("findTheBiggestSpender"))));
            Customer c = (Customer) getObjects().get(0);
            DataHolder filter = new DataHolder(1);
            filter.setFieldNameAndValue(1, "customerId", c.getCustomerId());

//           ==================  another way:            
//           DataHolder filter = new DataHolder();
//           filter.setFieldsCount(1);
//           filter.setFieldName(1, "customerId");
//           filter.setObject("customerId", c.getCustomerId());

            Predicate<Customer> p = new GenericPredicate<Customer>(filter);
            Set<Invoice> customerInvoices = new HashSet<Invoice>();
            customerInvoices.addAll(invoiceFactory.filter(p));
            c.setInvoices(customerInvoices);
            printAllCustomers();
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
    }

    public void persistDisconnectedData() {
        Logger.getLogger(getClass().getName()).info("\n======================= This is an example of INSERT/DELETE/UPDATE without retrieving objects from database first  ===========================\n");
        long t1 = System.currentTimeMillis();
        PropertyChangeListener pcl = new CustomerChangeListener();
        this.addPropertyChangeListeners(new PropertyChangeListener[] {pcl});
        this.setDomainClass(POJOCustomer.class);
        loadMappings("CustomersWithPhone.properties");
        populateMetaData();
        getDataReader().setMetaData(getMetaData());
        options = new HashMap<Enum, Object>();
        options.put(DBDataWriter.Options.noFullColumnName, true);
        //setOptions(opt);
        try {
            //POJOCustomer c1 = (POJOCustomer) this.newInstance();
            POJOCustomer c1 = new POJOCustomer();
            c1.setCustomerId(777);
            c1.setFirstName("John");
            c1.setLastName("Smith");
            c1.setStreet("123 Main st.");
            c1.setCity("Somecity");
            POJOCustomer enhancedC1 = (POJOCustomer) this.attach(c1);            
            ((DomainObject) enhancedC1).reset();
            ((DomainObject) enhancedC1).addPropertyChangeListener(pcl);
            this.create(enhancedC1);
            logger.info("\ncreated customer: " + enhancedC1 + "\n");

            POJOCustomer c2 = (POJOCustomer) this.newInstance();
            load(c2);    // or  ((DomainObject) c2).load();         
            c2.setCustomerId(2);
            syncOriginal(c2); // or  ((DomainObject) c2).syncOriginal();
            this.remove(c2);
            logger.info("\nremoved customer: " + c2 + "\n");

            POJOCustomer c3 = (POJOCustomer) this.newInstance();
            DataHolder dh = new DataHolder(6);
            dh.setFieldNameAndValue(1, "customerId", 3);
            dh.setFieldNameAndValue(2, "firstName", "Michael");
            dh.setFieldNameAndValue(3, "lastName", "Clancy");
            dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
            dh.setFieldNameAndValue(5, "city", "San Francisco");
            dh.setFieldNameAndValue(6, "phone", "4444444444");
            ((DomainObject) c3).setOriginalValues(dh);
            c3.setCustomerId(3);
            c3.setFirstName("Michael");
            c3.setLastName("Clancy");
            c3.setStreet("542 Upland Pl.");
            c3.setCity("San Francisco");
            c3.setPhone("9876543210");
            this.store(c3);
            logger.info("\nstored customer: " + c3 + "\n");

            POJOCustomer c4 = (POJOCustomer) this.newInstance();
            c4.setCustomerId(4);
            c4.setFirstName("Sylvia");
            c4.setLastName("Ringer");
            c4.setStreet("365 College Av.");
            c4.setCity("Dallas");
            c4.setPhone("5555555555");
            syncOriginal(c4);
            c4.setPhone("9988776655");
            this.store(c4);
            logger.info("\nstored customer: " + c4 + "\n");

            POJOCustomer c5 = (POJOCustomer) newInstance();
            DataHolder dh2 = new DataHolder(6);
            dh2.setFieldNameAndValue(1, "customerId", 5);
            dh2.setFieldNameAndValue(2, "firstName", "Laura");
            dh2.setFieldNameAndValue(3, "lastName", "Miller");
            dh2.setFieldNameAndValue(4, "street", "294 Seventh Av.");
            dh2.setFieldNameAndValue(5, "city", "Paris");
            dh2.setFieldNameAndValue(6, "phone", null);
            ((DomainObject) c5).setOriginalValues(dh2);
            //((DomainObject) c5).load();
            load(c5);
            c5.setPhone("1122334455");
            this.store(c5);
            logger.info("\nstored customer: " + c5 + "\n");

            getDBServices().beginTran();
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (success) {
                getDBServices().commitTran();
            } else {
                throw new Exception("Data modification: failed");
            }
            this.synchronizePersistentState();

        } catch (Throwable sqle) {
            throw new DataAccessException(sqle);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("cglib ----------------------------------------------------- Sec: " + (t2 - t1) / 1000d);
    }

    public void persistDisconnectedData1() {
       PropertyChangeListener pcl = new CustomerChangeListener();
       this.addPropertyChangeListeners(new PropertyChangeListener[] {pcl});
        Logger.getLogger(getClass().getName()).info("\n======================= This is an example of INSERT/DELETE/UPDATE without retrieving objects from database first  ===========================\n");
        long t1 = System.currentTimeMillis();        
        this.setDomainClass(CustomerWithPhone.class);
        loadMappings("CustomersWithPhone.properties");
        populateMetaData();
        getDataReader().setMetaData(getMetaData());
        options = new HashMap<Enum, Object>();
        options.put(DBDataWriter.Options.noFullColumnName, true);
        //setOptions(opt);
        try {
            CustomerWithPhone c1 = (CustomerWithPhone) this.newInstance();
            c1.setCustomerId(Integer.MAX_VALUE);
            c1.setFirstName("John");
            c1.setLastName("Smith");
            c1.setStreet("123 Main st.");
            c1.setCity("Somecity");
            this.create((DomainObject) c1);
            logger.info("\ncreated customer: " + c1 + "\n");

            CustomerWithPhone c2 = (CustomerWithPhone) this.newInstance();
            load(c2);    // or  ((DomainObject) c2).load();         
            c2.setCustomerId(2);
            syncOriginal(c2); // or  ((DomainObject) c2).syncOriginal();
            this.remove(c2);
            logger.info("\nremoved customer: " + c2 + "\n");

            CustomerWithPhone c3 = (CustomerWithPhone) this.newInstance();
            DataHolder dh = new DataHolder(6);
            dh.setFieldNameAndValue(1, "customerId", 3);
            dh.setFieldNameAndValue(2, "firstName", "Michael");
            dh.setFieldNameAndValue(3, "lastName", "Clancy");
            dh.setFieldNameAndValue(4, "street", "542 Upland Pl.");
            dh.setFieldNameAndValue(5, "city", "San Francisco");
            dh.setFieldNameAndValue(6, "phone", "4444444444");
            ((DomainObject) c3).setOriginalValues(dh);
            c3.setCustomerId(3);
            c3.setFirstName("Michael");
            c3.setLastName("Clancy");
            c3.setStreet("542 Upland Pl.");
            c3.setCity("San Francisco");
            c3.setPhone("9876543210");
            this.store(c3);
            logger.info("\nstored customer: " + c3 + "\n");

            CustomerWithPhone c4 = (CustomerWithPhone) this.newInstance();
            c4.setCustomerId(4);
            c4.setFirstName("Sylvia");
            c4.setLastName("Ringer");
            c4.setStreet("365 College Av.");
            c4.setCity("Dallas");
            c4.setPhone("5555555555");
            syncOriginal(c4);
            c4.setPhone("9988776655");
            this.store(c4);
            logger.info("\nstored customer: " + c4 + "\n");

            CustomerWithPhone c5 = (CustomerWithPhone) newInstance();
            DataHolder dh2 = new DataHolder(6);
            dh2.setFieldNameAndValue(1, "customerId", 5);
            dh2.setFieldNameAndValue(2, "firstName", "Laura");
            dh2.setFieldNameAndValue(3, "lastName", "Miller");
            dh2.setFieldNameAndValue(4, "street", "294 Seventh Av.");
            dh2.setFieldNameAndValue(5, "city", "Paris");
            dh2.setFieldNameAndValue(6, "phone", null);
            ((DomainObject) c5).setOriginalValues(dh2);
            //((DomainObject) c5).load();
            load(c5);
            c5.setPhone("1122334455");
            this.store(c5);
            logger.info("\nstored customer: " + c5 + "\n");

            getDBServices().beginTran();
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (success) {
                getDBServices().commitTran();
            } else {
                throw new Exception("Data modification: failed");
            }
            this.synchronizePersistentState();

        } catch (Throwable sqle) {
            throw new DataAccessException(sqle);
        }
        long t2 = System.currentTimeMillis();
        System.out.println("DomainObject ----------------------------------------------------- Sec: " + (t2 - t1) / 1000d);
    }
    
    private class CustomerChangeListener  implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            System.out.println("------------------- propertyChange: " + evt.getPropertyName() + ": " + evt.getOldValue() + ", " +  evt.getNewValue());
        }        
    }
    
}

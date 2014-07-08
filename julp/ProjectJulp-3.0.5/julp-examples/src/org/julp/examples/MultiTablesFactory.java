package org.julp.examples;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.DataAccessException;
import org.julp.DomainObject;
import org.julp.Wrapper;
import org.julp.db.DBDataReader;
import org.julp.db.DBDataWriter;
import org.julp.db.DomainObjectFactory;
import org.julp.db.LazyLoader;
import org.julp.db.OptimisticLock;

public class MultiTablesFactory extends DomainObjectFactory {

    private static final long serialVersionUID = -580517328628103624L;
    protected Map resultSetOptionMap;
    protected Properties queries = null;

    public MultiTablesFactory() {
        setDomainClass(MultiTables.class);
        /* IT IS NOT NECESSARY TO LOAD MAPPINGS THIS WAY, COULD BE ANYTHING: XML, JNDI, DATABASE, ETC... */
        loadMappings("MultiTables.properties");
        queries = loadProperties("MultiTables.sql");

        // use this if your JDBC driver does not support ResultSetMetaData.getTableName() and your SELECT statement has duplicate column names (" + column + ") you must use DBReadData.setOptions(DBReadData.resultSetIndexToFieldMap, map). 
        // Map must have key=index of column in SELECT statement and value=fieldName e.g.: 1=customerId, 2=lastName. etc");
        Properties p = loadProperties("MultiTablesResultSetToField.properties");
        resultSetOptionMap = new HashMap<Integer, String>(p.size());
        for (Object entry : p.entrySet()) {
            resultSetOptionMap.put(Integer.parseInt(((Map.Entry) entry).getKey().toString()), ((Map.Entry) entry).getValue().toString());
        }
        this.options = new HashMap<Enum, Object>();        
        getDataReader().setMetaData(getMetaData());    
    }

    public int findAllCustomerInvoices() {
        int records = 0;
        try {
            Collection arg = new ArrayList(1);
            options.put(DBDataReader.Options.resultSetIndexToFieldMap, resultSetOptionMap);
            //setOptions(options);
            records = this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("findAll"))));
            printData();
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        return records;
    }

    public void lazyLoading() {
        System.out.println("\n======================= this is an example of using lazy loading ===========================\n");
        options.put(DBDataReader.Options.resultSetIndexToFieldMap, resultSetOptionMap);
        //setOptions(options);
        LazyLoader lazyLoader = new LazyLoader(this);
        getDBServices().setCacheStatements(false);
        Set<String> optionalFields = new HashSet<String>();
        optionalFields.add("firstName");
        optionalFields.add("lastName");
        String sql = queries.getProperty("findNewYorkCustomers");
        List<MultiTables> lazyObjects = lazyLoader.getLazyLoadedObjects(sql, optionalFields);
        System.out.println("\n======================= Objects below are lazy loaded: only Primery Keys and optional columns. 'originalValues<DataHolder>' are not populated ===========================\n");
        for (MultiTables domainObject : lazyObjects) {
            System.out.println(domainObject);
        }

        options.put(DBDataReader.Options.resultSetIndexToFieldMap, resultSetOptionMap);
        //setOptions(options);
        getDataReader();
        List<MultiTables> loadedObjects = null;
        try {
            System.out.println("\n======================= Objects below are fully loaded. Note: there are all columns of 4 joined tables  ===========================\n");
            int start = 0;
            int step = 10;
            int end = start + step;
            int sublist = 1;
            while (end < lazyObjects.size()) {
                List<MultiTables> subList = lazyObjects.subList(start, end);
                loadedObjects = lazyLoader.getLoadedObjects(subList);
                System.out.println("\n======================= sublist: " + sublist);
                for (MultiTables domainObject : loadedObjects) {
                    System.out.println(domainObject);
                }
                start = end;
                end = end + step;
                sublist++;
            }

            System.out.println("\n======================= sublist: " + sublist);
            if (start < lazyObjects.size()) {
                for (int i = start; i < lazyObjects.size(); i++) {
                    System.out.println(lazyObjects.get(i));
                }
            }

//            Pager pager = new Pager(objectList, 10);
//            int pagesTotal = pager.getPagesTotal();
//
//            for (int pageNum = 1; pageNum <=pagesTotal; pageNum++) {
//                PageHolder subList = pager.getPage(pageNum);
//                System.out.println("\n======================= page: " + pageNum);
//                for (MultiTables domainObject :  (List<MultiTables>) subList.getPage()) {
//                    System.out.println(domainObject);
//                }
//            }

//            List<DomainObject> loadedObjects = lazyLoader.getLoadedObjects(lazyObjects);
//            for (DomainObject domainObject : loadedObjects) {
//                System.out.println(domainObject);
//            }
        } catch (Exception ex) {
            Logger.getLogger(MultiTablesFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void modifyInvoices() {
        if (options == null) {
            options = new HashMap<Enum, Object>();
        }
        // if another user/process UPDATEd or DELETEd rows in database then throw exception
        options.put(DBDataWriter.Options.optimisticLock, OptimisticLock.KEY_AND_UPDATEBLE_COLUMNS);
        options.put(DBDataWriter.Options.throwOptimisticLockDeleteException, true);
        options.put(DBDataWriter.Options.throwOptimisticLockUpdateException, true);
        //options.put(DBDataWriter.Options.removeAndCreateInsteadOfStore, true);
        options.put(DBDataWriter.Options.batchEnabled, true);
        options.put(DBDataWriter.Options.noFullColumnName, true);
        options.put(DBDataReader.Options.resultSetIndexToFieldMap, resultSetOptionMap);

        getDBServices().setCacheStatements(false);
        //setOptions(options);
        Map invoiceTotalMap = new HashMap();
        Collection arg = new ArrayList(1);
        arg.add(new Integer(1));
        try {
            this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("findAllCustomerInvoices"), arg)));
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }

        // remove some records
        ListIterator li1 = this.objectList.listIterator();
        while (li1.hasNext()) {
            MultiTables record = (MultiTables) li1.next();
            Integer invoiceId = record.getInvoiceId();
            BigDecimal cost = record.getCost();
            if (cost.doubleValue() > 30.0) {
                System.out.println("\n======================= removing: \n" + record);
                ((DomainObject) record).remove();
            } else {
                BigDecimal invoiceTotal = (BigDecimal) invoiceTotalMap.get(invoiceId);
                if (invoiceTotal == null) {
                    invoiceTotal = new BigDecimal(0.0);
                }
                invoiceTotal = invoiceTotal.add(cost);
                invoiceTotalMap.put(invoiceId, invoiceTotal);
            }
        }

        System.out.println("\n======================= invoiceTotalMap: \n" + invoiceTotalMap);

        System.out.println("\n======================= this is after data modifications ===========================\n");        

        try {
            // since MultiTables can modify more then one table
            // we need to specify table name
            options.put(DBDataWriter.Options.table, "ITEM");
            //setOptions(options);
            //((DBDataWriter) dataWriter).setTable("ITEM");
            this.dbServices.beginTran();
            boolean success1 = this.writeData();
            Throwable t1 = this.getPersistenceError();
            if (t1 != null) {
                throw t1;
            }

            // update INVOICE table            
            //update invoices total
            ListIterator li2 = this.objectList.listIterator();
            while (li2.hasNext()) {
                MultiTables record = (MultiTables) li2.next();
                Integer invoiceId = record.getInvoiceId();
                Object total = invoiceTotalMap.get(invoiceId);
                BigDecimal invoiceTotal = null;
                if (total == null) {
                    invoiceTotal = new BigDecimal(0.0);
                } else {
                    invoiceTotal = (BigDecimal) total;
                }
                record.setTotal(invoiceTotal);
                record.setModifiedOn(new Timestamp(System.currentTimeMillis()));
                ((DomainObject) record).store();
            }
            //options.put(DBDataWriter.Options.table, "INVOICE");
            //setOptions(options);
            ((DBDataWriter) dataWriter).setTable("INVOICE");
//            // do not DELETE invoice
//            DataWriter.DataModificationSequence[] updateOnly = new DataWriter.DataModificationSequence[1];
//            updateOnly[0] = DataWriter.DataModificationSequence.DATA_MODIFICATION_SEQUENCE_UPDATE;
//            ((DBDataWriter) dataWriter).setDataModificationSequence(updateOnly);

            boolean success2 = this.writeData();
            Throwable t2 = this.getPersistenceError();
            if (t2 != null) {
                throw t2;
            }
            if (success1 == true && success2 == true) {
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

        System.out.println("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
        printData();
    }

    protected void printData() {
        List rows = this.getObjectList();
        Iterator iter = rows.iterator();
        while (iter.hasNext()) {
            MultiTables row = (MultiTables) iter.next();
            System.out.println(row);
        }
    }
}

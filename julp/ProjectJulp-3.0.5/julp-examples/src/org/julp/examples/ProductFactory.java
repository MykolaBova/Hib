package org.julp.examples;

import java.util.*;
import org.julp.*;
import org.julp.db.DBDataWriter;
import org.julp.db.DBServicesUtils;
import org.julp.db.DomainObjectFactory;
import org.julp.db.StatementHolder;

public class ProductFactory extends DomainObjectFactory {

    private static final long serialVersionUID = 7136223093991428508L;
    protected Properties queries = null;

    public ProductFactory() {
        this.setDomainClass(Product.class);
        /* IT IS NOT NESSESARY TO LOAD MAPPINGS THIS WAY, COULD BE ANYTHING: XML, JNDI, DATABASE, ETC... */
        loadMappings("Product.properties");
        queries = loadProperties("Product.sql");
    }

    public int findAllProducts() {
        int records = 0;
        try {
            records = this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("findAllProducts"))));
            printAllProducts();
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        return records;
    }

    public void createAndStoreProductsLater() {
        ((DBDataWriter) getDataWriter()).setGenerateSQLOnly(true);
        int records = findAllProducts();
        Product product = new Product();
        product.setProductId(new Integer(records + 1)); // this is NOT proper way to genarate id !!!
        product.setName("Zaurus SL-5600");
        product.setPrice(299.98);
        product.setComments("Good deal!");
        product = (Product) attach(product);
        this.create(product);
        System.out.println("\n=============== Created product: " + product + "\n");

        /*       
        another way:
        product.create();
        product.setObjectId(this.getNextObjectId());
        this.getObjectList().add(product);
        or:
        product.create();
        this.setObject(product);
         */

        /*
        Product productToRemove = (Product) this.objectList.get(0);        
        this.remove(productToRemove);
        System.out.println("\nRemoved objects: " + this.getRemovedObjects() + "\n");                
        // another way:        
        //((Product) this.objectList.get(0)).remove();   
         */
        java.text.NumberFormat nf = java.text.NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
        ListIterator li = this.objectList.listIterator();
        while (li.hasNext()) {
            Product productToUpdate = (Product) li.next();
            double currentPrice = productToUpdate.getPrice();
            if (currentPrice < 10) {
                double newPrice = currentPrice * 1.1;
                productToUpdate.setPrice(Double.parseDouble(nf.format(newPrice)));
                ((DomainObject) productToUpdate).store();
            }
        }

        //System.out.println("\n======================= this is after data modifications ===========================\n");
        //printAllProducts(); 
 /*       
        try{
        this.dbServices.beginTran();
        boolean success = this.writeData();
        Throwable t = this.getError();
        if (t != null){
        throw t;
        }
        if (success){
        this.dbServices.commitTran();
        }else{
        throw new Exception("Data modification: failed");
        }
        this.synchronizePersistentState()
        }catch (Throwable t){
        try{
        this.dbServices.rollbackTran();
        }catch (Exception sqle){
        sqle.printStackTrace();
        throw new DataAccessException(sqle);
        }
        t.printStackTrace();
        }finally{
        try{
        this.dbServices.release(true);
        }catch (Exception sqle){
        sqle.printStackTrace();
        throw new DataAccessException(sqle);
        }
        }
         */

        String generatedSQLasXML = null;
        List<StatementHolder> generatedSQL = null;
        try {
            boolean success = this.writeData();
            Throwable t = this.getPersistenceError();
            if (t != null) {
                throw t;
            }
            if (!success) {
                throw new Exception("Data modification: failed");
            }
            // make sure you get this values BEFORE synchronizePersistentState()
            generatedSQLasXML = ((DBDataWriter) dataWriter).getGeneratedSQLasXML();
            generatedSQL = ((DBDataWriter) dataWriter).getGeneratedSQL();
            this.synchronizePersistentState();
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            try {
                this.dbServices.release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }

        System.out.println("\n======================= this generated XML which can be sent to another Application/ApplicationServer to update database ===========================\n");
        System.out.println(generatedSQLasXML);
        System.out.println("\n======================= this generated stetements and parameters which can be sent to another Application/ApplicationServer to update database ===========================\n");
        Iterator<StatementHolder> it = generatedSQL.iterator();
        while (it.hasNext()) {
            StatementHolder holder = it.next();
            Collection params = holder.getParams();
            System.out.println("sql: " + holder.getQuery() + " params: " + params);
        }
        
        System.out.println("\n======================= Convert PreparedStatement's SQL and parameters for use in Statement by replacing \"?\" with parameters values ===========================\n");
        DBServicesUtils utils = new DBServicesUtils();
        for (StatementHolder holder : generatedSQL) {
            String sql = utils.convert(holder.getQuery(), holder.getParams());
            System.out.println("sql: " + sql);
        }
        
    }
    
     public void getProductPages() {        
        System.out.println("\n======================= this is all products ===========================\n");
        findAllProducts();
        Pager pager = new Pager(getObjectList());
        pager.setPageSize(8);
        PageHolder page = pager.getPage(1);
        System.out.println("\n=============== Total records: " + page.getObjectsTotal() + ", Page " + page.getPageNumber() + " of " + page.getPagesTotal() + " ===============\n");
        Iterator iter1 = page.getPage().iterator();
        while (iter1.hasNext()) {
            Product product = (Product) iter1.next();
            System.out.println(product);
        }

        PageHolder thirdPage = pager.getPage(3);
        System.out.println("\n=============== Total records: " + thirdPage.getObjectsTotal() + ", Page " + thirdPage.getPageNumber() + " of " + thirdPage.getPagesTotal() + " ===============\n");
        Iterator iter3 = thirdPage.getPage().iterator();
        while (iter3.hasNext()) {
            Product product = (Product) iter3.next();
            System.out.println(product);
        }

        PageHolder secondPage = pager.getPage(7);
        System.out.println("\n=============== Total records: " + secondPage.getObjectsTotal() + ", Page " + secondPage.getPageNumber() + " of " + secondPage.getPagesTotal() + " ===============\n");

        Iterator iter2 = secondPage.getPage().iterator();
        while (iter2.hasNext()) {
            Product product = (Product) iter2.next();
            System.out.println(product);
        }
    }

    public void deleteAndInsertInsteadOfUpdate() {
        findAllProducts();
        if (options == null) {
            options = new HashMap<Enum, Object>();
        }
        options.put(DBDataWriter.Options.removeAndCreateInsteadOfStore, true);
        options.put(DBDataWriter.Options.noFullColumnName, true); //hsqldb bug workaround
        //setOptions(options);
        Pager pager = new Pager(getObjectList());
        pager.setPageSize(10);
        PageHolder page = pager.getPage(1);
        System.out.println("\n=============== Total records: " + page.getObjectsTotal() + ", Page " + page.getPageNumber() + " of " + page.getPagesTotal() + " ===============\n");
        Iterator<Product> iter1 = (Iterator<Product>) page.getPage().iterator();
        while (iter1.hasNext()) {
            Product product = iter1.next();
            product.setPrice(product.getPrice() * 1.1);
            store(product);
            //System.out.println(product);
        }

        try {
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
        } catch (Throwable t) {
            try {
                getDBServices().rollbackTran();
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
            t.printStackTrace();
        } finally {
            try {
                getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
        System.out.println("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
    }

    protected void printAllProducts() {
        List<Product> products = this.getObjects();        
        for (Product product : products) {           
            System.out.println(product);
        }
    }
}

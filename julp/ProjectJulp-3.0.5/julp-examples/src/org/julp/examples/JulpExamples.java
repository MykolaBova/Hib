package org.julp.examples;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.julp.AbstractDomainObjectFactory;
import org.julp.DataAccessException;
import org.julp.db.BasicDataSourceImpl;
import org.julp.db.BasicStatementCache;
import org.julp.db.DBDataReader;
import org.julp.db.DBDataWriter;
import org.julp.db.DBServices;
import org.julp.examples.csv.CsvCustomerFactory;
import org.julp.examples.gui.AdhocTest;
import org.julp.examples.gui.JulpTableModelExample;
import org.julp.examples.xls.XlsCustomerFactory;
import org.julp.util.db.SQLScriptExecutor;

public class JulpExamples {

    protected String driver = null;
    protected String dbURL = null;
    protected String user = null;
    protected String password = "";
    protected String connectionOptions = null;
    protected String filePath = null;
    protected String example = null;
    protected SQLScriptExecutor setup = new SQLScriptExecutor();

    public JulpExamples() {
        setupLogging();        
    }

    private void setupLogging() {
        InputStream is = null;
        try {
            is = getClass().getResourceAsStream("julp-examples-logging.properties");
            LogManager.getLogManager().readConfiguration(is);
            Logger.getLogger(DBDataReader.class.getName()).setLevel(Level.OFF);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
        }
    }

    private void reloadDatabase() {
        Logger.getLogger(DBServices.class.getName()).setLevel(Level.OFF);
        Logger.getLogger(AbstractDomainObjectFactory.class.getName()).setLevel(Level.OFF);
        System.out.println("\n======================= Re-creating database =======================\n");
        try {
            if (driver.contains("firebird")) {
                setup.setAutoCommit(true);
                setup.getDbServices().setCloseStatementAfterExecute(false);
                setup.execute(driver, dbURL, user, password, connectionOptions, filePath, "|", false);
            } else {
                setup.execute(driver, dbURL, user, password, connectionOptions, filePath, "|", true);
            }
        } catch (SQLException ex) {
            Logger.getLogger(JulpExamples.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\n======================= Running example ============================\n");
        Logger.getLogger(DBServices.class.getName()).setLevel(Level.ALL);
         Logger.getLogger(BasicStatementCache.class.getName()).setLevel(Level.ALL);
    }

    public void findAllProducts() {
        ProductFactory productFactory = new ProductFactory();
        ((DBDataWriter) productFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            productFactory.setDBServices(getDBServices());
            reloadDatabase();
            productFactory.findAllProducts();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                productFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void getProductPages() {
        ProductFactory productFactory = new ProductFactory();
        ((DBDataWriter) productFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            productFactory.setDBServices(getDBServices());
            reloadDatabase();
            productFactory.getProductPages();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                productFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void findAllCustomers() {
        //options.put(DBDataWriter.Options.noFullColumnName, true); //hsqldb bug workaround
        //options.put(DBDataWriter.Options.dbServices, dbServices);

        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        customerFactory.getDBServices().setCacheStatements(true);

        try {
            reloadDatabase();
            customerFactory.findAllCustomers();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void findCustomer() {
        CustomerFactory customerFactory = new CustomerFactory();
        /// customerFactory.setObjectType(Customer.class);
        customerFactory.loadMappings("Customer.properties");
        ((DBDataWriter) customerFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            customerFactory.setDBServices(getDBServices());
            reloadDatabase();
            customerFactory.findCustomer();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void findAllCustomersWithPhone() {
        CustomerFactory customerFactory = new CustomerFactory();
        ((DBDataWriter) customerFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            customerFactory.setDBServices(getDBServices());
            reloadDatabase();
            customerFactory.findAllCustomersWithPhone();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void createAndStoreProductsLater() {
        ProductFactory productFactory = new ProductFactory();
        try {
            productFactory.setDBServices(getDBServices());
            ((DBDataWriter) productFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
            reloadDatabase();
            productFactory.createAndStoreProductsLater();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                productFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void createAndStoreCustomers() {
        Map<Enum, Object> options = new HashMap<Enum, Object>();
        options.put(DBDataWriter.Options.noFullColumnName, true); //hsqldb bug workaround        

        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        customerFactory.setOptions(options);
        customerFactory.getDBServices().setCacheStatements(true);
        try {
            reloadDatabase();
            customerFactory.createAndStoreCustomers();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void createAndStorePOJOCustomersNoExtending() {
        Map<Enum, Object> options = new HashMap<>();
        options.put(DBDataWriter.Options.noFullColumnName, true); //hsqldb bug workaround
        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        customerFactory.setOptions(options);
        customerFactory.getDBServices().setCacheStatements(true);
        try {
            reloadDatabase();
            customerFactory.createPOJOCustomerNoExtending();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void createAndStoreCustomersNoExtending() {
        Map<Enum, Object> options = new HashMap<>();        
        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        customerFactory.setOptions(options);
        customerFactory.getDBServices().setCacheStatements(true);
        try {
            reloadDatabase();
            customerFactory.createAndStoreCustomersNoExtending();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void lazyLoading() {
        MultiTablesFactory multiTablesFactory = new MultiTablesFactory();
        multiTablesFactory.setDBServices(getDBServices());
        multiTablesFactory.getDBServices().setCacheStatements(true);
        ((DBDataWriter) multiTablesFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            reloadDatabase();
            multiTablesFactory.lazyLoading();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                multiTablesFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void modifyInvoices() {       
        DBServices dbServices = getDBServices();
        MultiTablesFactory multiTablesFactory = new MultiTablesFactory();
        multiTablesFactory.setDBServices(dbServices);
        try {
            reloadDatabase();
            multiTablesFactory.setDBServices(dbServices);
            multiTablesFactory.modifyInvoices();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                dbServices.release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void findAllCustomerInvoices() {
        MultiTablesFactory multiTablesFactory = new MultiTablesFactory();
        multiTablesFactory.setDBServices(getDBServices());
        multiTablesFactory.getDBServices().setCacheStatements(true);
        ((DBDataWriter) multiTablesFactory.getDataWriter()).setNoFullColumnName(true); //hsqldb bug workaround
        try {
            reloadDatabase();
            multiTablesFactory.findAllCustomerInvoices();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                multiTablesFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void inClause() {
        CustomerFactory factory = new CustomerFactory();
        factory.setDBServices(getDBServices());
        factory.setReadOnly(true);
        factory.getDBServices().setMaxStatementCacheSize(3);
        factory.getDBServices().setCacheStatements(true);
        try {
            reloadDatabase();
            factory.inClause();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                factory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void selectAs() {
        CustomerFactory factory = new CustomerFactory();
        factory.setDBServices(getDBServices());
        factory.setReadOnly(true);
        try {
            reloadDatabase();
            factory.selectAs();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                factory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void tableModel() {
        CustomerFactory factory = new CustomerFactory();
        factory.loadMappings("Customer.properties");
        factory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            JulpTableModelExample modelExample = new JulpTableModelExample();
            modelExample.setFactory(factory);
            modelExample.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                factory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void adhoc() {
        CustomerFactory factory = new CustomerFactory();
        factory.loadMappings("CustomersWithPhone.properties");
        factory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            final AdhocTest adhoc = new AdhocTest();
            adhoc.setFactory(factory);
            adhoc.setVisible(true);
            adhoc.openDialog(true);
            //adhoc.addCriteria();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                factory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void deleteAndInsertInsteadOfUpdate() {
        ItemFactory itemFactory = new ItemFactory();
        itemFactory.setDBServices(getDBServices());
        //itemFactory.getDBServices().setMaxStatementCacheSize(5);
        itemFactory.getDBServices().setCacheStatements(true);
        try {
            reloadDatabase();
            itemFactory.deleteAndInsertInsteadOfUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                itemFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }

    public void customerWithInvoices() {
        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            customerFactory.customerWithInvoices();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }
    
   public void persistDisconnectedData() {
        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            customerFactory.persistDisconnectedData();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }
   
      public void persistDisconnectedData1() {
        CustomerFactory customerFactory = new CustomerFactory();
        customerFactory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            customerFactory.persistDisconnectedData1();
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                customerFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }
      
    public void loadCustomerInvoicesWithItems() {
        InvoiceFactory invoiceFactory = new InvoiceFactory();
        invoiceFactory.setDBServices(getDBServices());
        try {
            reloadDatabase();
            int customerId = 4;
            invoiceFactory.loadCustomerInvoicesWithItems(customerId);
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            try {
                invoiceFactory.getDBServices().release(true);
            } catch (Exception sqle) {
                sqle.printStackTrace();
                throw new DataAccessException(sqle);
            }
        }
    }
    
    public void loadFromCsvFile() {
        CsvCustomerFactory factory = new CsvCustomerFactory();
        factory.loadFromCsvFile();
    }
    
    public void writeToCsvFile() {
        CsvCustomerFactory factory = new CsvCustomerFactory();
        factory.writeToCsvFile();
    }
    
    public void loadFromXlsFile() {
        XlsCustomerFactory factory = new XlsCustomerFactory();
        factory.loadFromXlsFile();
    }
    
    public void writeToXlsFile() {
        XlsCustomerFactory factory = new XlsCustomerFactory();
        factory.writeToXlsFile();
    }
    
    protected DataSource getDataSource() {
        BasicDataSourceImpl dataSource = new BasicDataSourceImpl();
        dataSource.setUserName(user);
        dataSource.setPassword(password);
        dataSource.setDriverName(driver);
        dataSource.setConnectionUrl(dbURL);
        Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        if (connectionOptions != null && connectionOptions.trim().length() > 0) {
            StringTokenizer st = new StringTokenizer(connectionOptions, ",", false);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int idx = token.indexOf("=");
                if (idx == -1) {
                    throw new IllegalArgumentException("Invalid optional connection properties format. \nIt must have format: name1=value1, name2=value2, ...");
                }
                String name = token.substring(0, idx);
                String value = token.substring(idx + 1);
                prop.setProperty(name, value);
            }
        }

        dataSource.setConnectionProperties(prop);
        return dataSource;
    }

    protected DBServices getDBServices() {
        DBServices dbServices = new DBServices();
        try {
            dbServices.setDataSource(getDataSource());
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        }
        return dbServices;
    }

    /** Getter for property connectionOptions.
     * @return Value of property connectionOptions.
     *
     */
    public java.lang.String getConnectionOptions() {
        return connectionOptions;
    }

    /** Setter for property connectionOptions.
     * @param connectionOptions New value of property connectionOptions.
     *
     */
    public void setConnectionOptions(java.lang.String connectionOptions) {
        this.connectionOptions = connectionOptions;
    }

    /** Getter for property dbURL.
     * @return Value of property dbURL.
     *
     */
    public java.lang.String getDbURL() {
        return dbURL;
    }

    /** Setter for property dbURL.
     * @param dbURL New value of property dbURL.
     *
     */
    public void setDbURL(java.lang.String dbURL) {
        this.dbURL = dbURL;
    }

    /** Getter for property driver.
     * @return Value of property driver.
     *
     */
    public java.lang.String getDriver() {
        return driver;
    }

    /** Setter for property driver.
     * @param driver New value of property driver.
     *
     */
    public void setDriver(java.lang.String driver) {
        this.driver = driver;
    }

    /** Getter for property example.
     * @return Value of property example.
     *
     */
    public java.lang.String getExample() {
        return example;
    }

    /** Setter for property example.
     * @param example New value of property example.
     *
     */
    public void setExample(java.lang.String example) {
        this.example = example;
    }

    /** Getter for property filePath.
     * @return Value of property filePath.
     *
     */
    public java.lang.String getFilePath() {
        return filePath;
    }

    /** Setter for property filePath.
     * @param filePath New value of property filePath.
     *
     */
    public void setFilePath(java.lang.String filePath) {
        this.filePath = filePath;
    }

    /** Getter for property password.
     * @return Value of property password.
     *
     */
    public java.lang.String getPassword() {
        return password;
    }

    /** Setter for property password.
     * @param password New value of property password.
     *
     */
    public void setPassword(java.lang.String password) {
        this.password = password;
    }

    /** Getter for property user.
     * @return Value of property user.
     *
     */
    public java.lang.String getUser() {
        return user;
    }

    /** Setter for property user.
     * @param user New value of property user.
     *
     */
    public void setUser(java.lang.String user) {
        this.user = user;
    }
}

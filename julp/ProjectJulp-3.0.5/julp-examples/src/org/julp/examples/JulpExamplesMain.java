package org.julp.examples;



public class JulpExamplesMain {

    public JulpExamplesMain() {
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
        System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
        
        String driver = null;
        String dbURL = null;
        String user = null;
        String password = "";
        String connectionOptions = null;
        String filePath = null;
        String example = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-d")) {
                driver = args[i].substring(2).trim();
                System.out.println("driver: " + driver);
            } else if (args[i].startsWith("-r")) {
                dbURL = args[i].substring(2).trim();
                System.out.println("dbURL: " + dbURL);
            } else if (args[i].startsWith("-u")) {
                user = args[i].substring(2).trim();
                System.out.println("user: " + user);
            } else if (args[i].startsWith("-p")) {
                password = args[i].substring(2).trim();
                System.out.println("password: " + password);
            } else if (args[i].startsWith("-o")) {
                connectionOptions = args[i].substring(2).trim();
                System.out.println("connectionOptions: " + connectionOptions);
            } else if (args[i].startsWith("-f")) {
                filePath = args[i].substring(2).trim();
                System.out.println("filePath: " + filePath);
            } else if (args[i].startsWith("-e")) {
                example = args[i].substring(2).trim();
                //System.out.println("example: " + example);
            }
        }

        //driver = "org.hsqldb.jdbcDriver";
        //dbURL = "jdbc:hsqldb:hsq://localhost";
        //user = "sa";
        //password = "";
        //connectionOptions = "";
        //filePath = "C:\\Temp\\ProjectJulp\\julp-examples\\lib\\setup.sql";
        //example = "findAllProducts";

        System.out.println("======================= Example: " + example + " ===========================\n");
        if (driver == null || dbURL == null || user == null || filePath == null || example == null) {
            System.out.println("Usage: -d{JDBCDriver} -r{dbURL} -u{user} [-p password] [-o connection_options (name1=value1,name2=value2,...)] -f{filePath} -e{Example name}");
            return;
        }

        JulpExamples examples = new JulpExamples();
        examples.setDriver(driver);
        examples.setDbURL(dbURL);
        examples.setUser(user);
        examples.setPassword(password);
        examples.setConnectionOptions(connectionOptions);
        examples.setFilePath(filePath);
        examples.setExample(example);

        if (example.equals("findAllProducts")) {
            examples.findAllProducts();
        } else if (example.equals("createAndStoreProductsLater")) {
            examples.createAndStoreProductsLater();
        } else if (example.equals("findAllCustomers")) {
            examples.findAllCustomers();
        } else if (example.equals("findAllCustomersWithPhone")) {
            examples.findAllCustomersWithPhone();
        } else if (example.equals("createAndStoreCustomers")) {
            examples.createAndStoreCustomers();
        } else if (example.equals("getProductPages")) {
            examples.getProductPages();
        } else if (example.equals("findCustomer")) {
            examples.findCustomer();
        } else if (example.equals("modifyInvoices")) {
            examples.modifyInvoices();
        } else if (example.equals("INClause")) {
            examples.inClause();
        } else if (example.equals("selectAs")) {
            examples.selectAs();
        } else if (example.equals("tableModel")) {
            examples.tableModel();
        } else if (example.equals("adhoc")) {
            examples.adhoc();
        } else if (example.equals("createAndStoreCustomersNoExtending")) {
            examples.createAndStoreCustomersNoExtending();
        } else if (example.equals("createPOJOCustomerNoExtending")) {
            examples.createAndStorePOJOCustomersNoExtending();
        } else if (example.equals("lazyLoading")) {
            examples.lazyLoading();
        } else if (example.equals("findAllCustomerInvoices")) {
            examples.findAllCustomerInvoices();
        } else if (example.equals("deleteAndInsertInsteadOfUpdate")) {
            examples.deleteAndInsertInsteadOfUpdate();
        } else if (example.equals("customerWithInvoices")) {
            examples.customerWithInvoices();
        } else if (example.equals("persistDisconnectedData")) {
//            examples.persistDisconnectedData1();
            examples.persistDisconnectedData();
//            examples.persistDisconnectedData1();
//            examples.persistDisconnectedData();
//            examples.persistDisconnectedData1();
//            examples.persistDisconnectedData();
        } else if (example.equals( "loadCustomerInvoicesWithItems")) {
            examples.loadCustomerInvoicesWithItems();
        } else if (example.equals( "loadFromCsvFile")) {
            examples.loadFromCsvFile();
        } else if (example.equals( "writeToCsvFile")) {
            examples.writeToCsvFile();
        } else if (example.equals( "loadFromXlsFile")) {
            examples.loadFromXlsFile();
        } else if (example.equals( "writeToXlsFile")) {
            examples.writeToXlsFile();
        }
    }
}

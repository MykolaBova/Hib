package org.julp.csv;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.DataAccessException;
import org.julp.Wrapper;

public class CsvCustomerFactory extends CsvDomainObjectFactory<Customer> {

    private static final Logger logger = Logger.getLogger(CsvCustomerFactory.class.getName());

    public CsvCustomerFactory() {
        setDomainClass(Customer.class);
        loadMappings("CsvCustomer.properties");
    }

    public void loadFromCsvFile() {
        logger.info("\n======================= this is an example of loading from Csv file ===========================\n");
        options = new HashMap<Enum<?>, Object>();      
        options.put(CsvDataReader.Options.header, true);
        Reader r = null;
        try {
            String path = "./CustomerOrig.csv";
            File f = new File(path); 
            if (!f.exists()) {
                path = "../CustomerOrig.csv";
                f = new File(path); 
            }
            logger.info("\npath: " + f.getCanonicalPath() + "\n");
            r = new FileReader(f);
            Wrapper data = new Wrapper(r);
            load(data);
            List<Customer> customers = getObjects();
            for (Customer c : customers) {
                System.out.println(c);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataAccessException(e);
        } finally {
            if (r != null) {
                try {
                    r.close();
                } catch (IOException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void writeToCsvFile() {
        logger.info("\n======================= this is an example of writing to Csv file ===========================\n");
        options = new HashMap<Enum<?>, Object>();
        //options.put(CsvDataWriter.Options.qoutedValues, true);
        options.put(CsvDataWriter.Options.header, true);        
        CsvFileServices fileServices = new CsvFileServices();
        try {
            String path = "./Customer.csv";
            File f = new File(path); 
            if (!f.exists()) {
                path = "../Customer.csv";
                f = new File(path); 
            }
            logger.info("\npath: " + f.getCanonicalPath() + "\n");
            Customer c = (Customer) newInstance();
            c.setCustomerId(111);
            c.setFirstName("John");
            c.setLastName("Smith");
            c.setCity("Toronto");
            c.setStreet("44 Main St.");
            create(c);
            logger.info("\ncreated customer: " + c + "\n");
            Customer c1 = (Customer) newInstance();            
            c1.setCustomerId(222);
            c1.setFirstName("Pete");
            c1.setLastName("Sam");
            c1.setCity("New York");
            c1.setStreet("123 5th Ave");            
            create(c1);
            logger.info("\ncreated customer: " + c1 + "\n");
            fileServices.setFile(f);
            fileServices.setRemoveBackupFile(false);                      
            fileServices.begin();            
            setWriter(fileServices.getWriter());
            boolean success = writeData();
            //if (1 == 1) {
            //    throw new Exception("test");
            //}
            if (success && getPersistenceError() == null) {
                fileServices.commit();
                synchronizePersistentState();
            } else {
                fileServices.rollback();
                throw new DataAccessException("Rollbak");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fileServices.rollback();
            } catch (IOException ex1) {
                logger.log(Level.SEVERE, null, ex1);
            }
            throw new DataAccessException(e);
        } finally {
            fileServices.release();
        }
        logger.info("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
    }
}

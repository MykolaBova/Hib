package org.julp.examples.xls;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import org.julp.DataAccessException;
import org.julp.Wrapper;
import org.julp.examples.Customer;
import org.julp.xls.XlsDataReader;
import org.julp.xls.XlsDataWriter;
import org.julp.xls.XlsDomainObjectFactory;
import org.julp.xls.XlsFileServices;

public class XlsCustomerFactory extends XlsDomainObjectFactory<Customer> {

    private Logger logger = Logger.getLogger(getClass().getName());

    public XlsCustomerFactory() {
        setDomainClass(Customer.class);
        loadMappings("XlsCustomer.properties");
    }

    public void loadFromXlsFile() {
        logger.info("\n======================= this is an example of loading from excel file ===========================\n");
        Workbook originalWorkbook = null;
        try {
            String path = "./CustomerOrig.xls";
            File file = new File(path);
            if (!file.exists()) {
                path = "../CustomerOrig.xls";
                file = new File(path);
            }

            // InputStream is = ...
            //originalWorkbook = Workbook.getWorkbook(is);

            originalWorkbook = Workbook.getWorkbook(file);
            options = new HashMap<Enum<?>, Object>();
            options.put(XlsDataReader.Options.header, true);
            Wrapper w = new Wrapper(originalWorkbook.getSheet(0));            
            load(w, 0, 10);
            List<Customer> customers = getObjects();
            for (Customer c : customers) {
                System.out.println(c);
            }
            System.out.println("====================");
            load(w, 10, 10);
            customers = getObjects();
            for (Customer c : customers) {
                System.out.println(c);
            }
            System.out.println("====================");
            load(w, 45, 10);
            customers = getObjects();
            for (Customer c : customers) {
                System.out.println(c);
            }
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            originalWorkbook.close();
        }
    }

    public void writeToXlsFile() {
        logger.info("\n======================= this is an example of writing to Excel file ===========================\n");
        options = new HashMap<Enum<?>, Object>();
        //options.put(XlsDataWriter.Options.qoutedValues, true);
        options.put(XlsDataWriter.Options.header, true);
        XlsFileServices services = new XlsFileServices();
        try {
            String path = "../Customer.xls";
            File file = new File(path);
            if (!file.exists()) {
                path = "../Customer.xls";
                file = new File(path);
            }
            logger.info("\npath: " + file.getCanonicalPath() + "\n");

            services.setFile(file);
            services.setRemoveBackupFile(false);
            services.begin();
            setSheet(services.getWorkbook().getSheet(0));

            //OutputStream os = ...
//            WritableWorkbook wwb = Workbook.createWorkbook(os);
//            setSheet(wwb.getSheet(0));

            boolean success = writeData();
            //if (1 == 1) {
            //    throw new Exception("test");
            //}
            if (success && getPersistenceError() == null) {
                services.commit();
                synchronizePersistentState();
            } else {
                services.rollback();
                throw new DataAccessException("Rollbak");
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                services.rollback();
            } catch (Exception ex1) {
                logger.log(Level.SEVERE, null, ex1);
            }
            throw new DataAccessException(e);
        } finally {
            services.release();
        }
        logger.info("\n======================= this is after COMMIT & synchronizePersistentState() or after ROLLBACK ===========================\n");
    }
}

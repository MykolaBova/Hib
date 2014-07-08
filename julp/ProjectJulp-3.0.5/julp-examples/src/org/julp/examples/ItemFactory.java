package org.julp.examples;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import org.julp.DataAccessException;
import org.julp.DomainObject;
import org.julp.PageHolder;
import org.julp.Pager;
import org.julp.Wrapper;
import org.julp.db.DBDataWriter;
import org.julp.db.DomainObjectFactory;

public class ItemFactory extends DomainObjectFactory implements java.io.Serializable {
    private static final long serialVersionUID = 7032692014153115011L;
     protected Properties queries = null;

    public ItemFactory() {
        setDomainClass(Item.class);
        loadMappings("Item.properties");
        queries = loadProperties("Item.sql");
    }

    public List<DomainObject> findItemsByProductId(int productId) {
         int records = 0;
        try {
            Collection param = new ArrayList();
            param.add(productId);
            records = this.load(new Wrapper(dbServices.getResultSet(queries.getProperty("itemsByProductId"), param)));
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
        return getObjectList();
    }

    public void deleteAndInsertInsteadOfUpdate() {
        List items = findItemsByProductId(0);
        if (options == null) {
            options = new HashMap<Enum, Object>();
        }
        options.put(DBDataWriter.Options.removeAndCreateInsteadOfStore, true);
        options.put(DBDataWriter.Options.batchEnabled, false);
        options.put(DBDataWriter.Options.noFullColumnName, true); //hsqldb bug workaround
        //setOptions(options);
        Pager pager = new Pager(items);
        pager.setPageSize(10);
        PageHolder page = pager.getPage(1);
        System.out.println("\n=============== Total records: " + page.getObjectsTotal() + ", Page " + page.getPageNumber() + " of " + page.getPagesTotal() + " ===============\n");
        Iterator<Item> iter = (Iterator<Item>) page.getPage().iterator();
        while (iter.hasNext()) {
            Item item = iter.next();
            item.setQuantity(item.getQuantity() + 10);
            store(item);
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
}

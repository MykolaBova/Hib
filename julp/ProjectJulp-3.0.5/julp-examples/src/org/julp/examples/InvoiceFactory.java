package org.julp.examples;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import org.julp.DataAccessException;
import org.julp.DataHolder;
import org.julp.DomainObject;
import org.julp.GenericPredicate;
import org.julp.Predicate;
import org.julp.Wrapper;
import org.julp.db.DomainObjectFactory;

public class InvoiceFactory extends DomainObjectFactory {

    private static final long serialVersionUID = 1633790094631860446L;
    protected Properties queries = null;

    public InvoiceFactory() {
        this.setDomainClass(Invoice.class);
        loadMappings("Invoice.properties");
        queries = loadProperties("Invoice.sql");
    }

    public void loadAllInvoices() {
        try {
            this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("allInvoices"))));
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
    }

    public void loadCustomerInvoicesWithItems(int customerId) {
        ItemFactory itemFactory = new ItemFactory();
        itemFactory.setDBServices(getDBServices());
        Collection params = new ArrayList<>(1);
        params.add(customerId);
        try {
            Properties prop = loadProperties("Item.sql");
            itemFactory.load(new Wrapper(itemFactory.getDBServices().getResultSet(prop.getProperty("itemsByCustomerId"), params)));
            for (Iterator<DomainObject> it = itemFactory.getObjectList().iterator(); it.hasNext();) {
                DomainObject o = it.next();                
            }
        } catch (SQLException sqle) {
            throw new DataAccessException(sqle);
        }

        try {
            this.load(new Wrapper(this.dbServices.getResultSet(queries.getProperty("customerInvoices"), params)));
            Collection<Invoice> invoices = getObjects();
            for (Invoice invoice : invoices) {
                DataHolder filter = new DataHolder(1);
                filter.setFieldNameAndValue(1, "invoiceId", invoice.getInvoiceId());
                Predicate<Invoice> p = new GenericPredicate<>(filter);
                invoice.setItems(itemFactory.filter(p));          
                System.out.println(invoice);
            }
        } catch (Exception sqle) {
            throw new DataAccessException(sqle);
        }
    }
}
package org.julp.examples;

import java.sql.Timestamp;
import java.util.Collection;

public class Invoice /*extends org.julp.AbstractDomainObject*/ {

    private static final long serialVersionUID = 7754509747238030234L;
    protected java.lang.Integer invoiceId;
    protected java.lang.Integer customerId;
    protected java.math.BigDecimal total;
    protected Timestamp modifiedOn;
    protected Collection items;
    
    public Invoice() {
    }

    public java.lang.Integer getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(java.lang.Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
        this.customerId = customerId;
    }

    public java.math.BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(java.math.BigDecimal total) {
        this.total = total;
    }
    
    public Timestamp getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Timestamp modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
    
    public Collection getItems() {
        return items;
    }

    public void setItems(Collection items) {
        this.items = items;
    }
}

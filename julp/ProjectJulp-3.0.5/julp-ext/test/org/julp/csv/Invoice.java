package org.julp.csv;

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
//        if (!isLoading()) {
//            /* DBColumn nullability: NoNulls - modify as needed */
//            if (invoiceId == null) {
//                throw new IllegalArgumentException("Missing value: Invoice Id");
//            }
//            if (!invoiceId.equals(this.invoiceId)) {
//                this.modified = true;
//            }
//        }
        this.invoiceId = invoiceId;
    }

    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
//        if (!isLoading()) {
//            /* DBColumn nullability: NoNulls - modify as needed */
//            if (customerId == null) {
//                throw new IllegalArgumentException("Missing value: Customer Id");
//            }
//            if (!customerId.equals(this.customerId)) {
//                this.modified = true;
//            }
//        }
        this.customerId = customerId;
    }

    public java.math.BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(java.math.BigDecimal total) {
//        if (!isLoading()) {
//            /* DBColumn nullability: NoNulls - modify as needed */
//            if (total == null) {
//                throw new IllegalArgumentException("Missing value: Total");
//            }
//            if (!total.equals(this.total)) {
//                this.modified = true;
//            }
//        }
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

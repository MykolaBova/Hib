package org.julp.examples;

import java.sql.Timestamp;
import org.julp.DomainObject;

public class MultiTables /*extends org.julp.AbstractDomainObject*/ {

    private static final long serialVersionUID = 120363597971216889L;
    protected java.lang.Integer customerId;
    protected java.lang.String firstName;
    protected java.lang.String lastName;
    protected java.lang.String street;
    protected java.lang.String city;
    protected java.lang.String phone;
    protected java.lang.Integer invoiceId;
    protected java.lang.Integer invoiceCustomerId;
    protected java.math.BigDecimal total;
    protected Timestamp modifiedOn;
    protected java.lang.Integer itemInvoiceId;
    protected java.lang.Integer item;
    protected java.lang.Integer itemProductId;
    protected java.lang.Integer quantity;
    protected java.math.BigDecimal cost;
    protected java.lang.Integer productId;
    protected java.lang.String name;
    protected java.lang.Double price;
    protected java.lang.String comments;

    public MultiTables() {
    }

    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("Missing value: Customer Id");
        }
        this.customerId = customerId;
    }

    public java.lang.String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(java.lang.String firstName) {
        if (firstName == null || firstName.trim().length() == 0) {
            throw new IllegalArgumentException("Missing value: First Name");
        }
        this.firstName = firstName;
    }

    public java.lang.String getLastName() {
        return this.lastName;
    }

    public void setLastName(java.lang.String lastName) {
        if (lastName == null || lastName.trim().length() == 0) {
            throw new IllegalArgumentException("Missing value: Last Name");
        }
        this.lastName = lastName;
    }

    public java.lang.String getStreet() {
        return this.street;
    }

    public void setStreet(java.lang.String street) {
        if (street == null || street.trim().length() == 0) {
            throw new IllegalArgumentException("Missing value: Street");
        }
        this.street = street;
    }

    public java.lang.String getCity() {
        return this.city;
    }

    public void setCity(java.lang.String city) {
        if (city == null || city.trim().length() == 0) {
            throw new IllegalArgumentException("Missing value: City");
        }
        this.city = city;
    }

    public java.lang.String getPhone() {
        return this.phone;
    }

    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }

    public java.lang.Integer getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(java.lang.Integer invoiceId) {
        if (invoiceId == null) {
            throw new IllegalArgumentException("Missing value: Invoice Id");
        }
        this.invoiceId = invoiceId;
    }

    public java.lang.Integer getInvoiceCustomerId() {
        return this.invoiceCustomerId;
    }

    public void setInvoiceCustomerId(java.lang.Integer invoiceCustomerId) {
        if (invoiceCustomerId == null) {
            throw new IllegalArgumentException("Missing value: Customer Id");
        }
        this.invoiceCustomerId = invoiceCustomerId;
    }

    public java.math.BigDecimal getTotal() {
        return this.total;
    }

    public void setTotal(java.math.BigDecimal total) {
        if (total == null) {
            throw new IllegalArgumentException("Missing value: Total");
        }
        this.total = total;
    }

    public java.lang.Integer getItemInvoiceId() {
        return this.itemInvoiceId;
    }

    public void setItemInvoiceId(java.lang.Integer itemInvoiceId) {
        if (itemInvoiceId == null) {
            throw new IllegalArgumentException("Missing value: Invoice Id");
        }
        this.itemInvoiceId = itemInvoiceId;
    }

    public java.lang.Integer getItem() {
        return this.item;
    }

    public void setItem(java.lang.Integer item) {
        if (item == null) {
            throw new IllegalArgumentException("Missing value: Item");
        }
        this.item = item;
    }

    public java.lang.Integer getItemProductId() {
        return this.itemProductId;
    }

    public void setItemProductId(java.lang.Integer itemProductId) {
        if (itemProductId == null) {
            throw new IllegalArgumentException("Missing value: Product Id");
        }
        this.itemProductId = itemProductId;
    }

    public java.lang.Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(java.lang.Integer quantity) {
        if (quantity == null) {
            throw new IllegalArgumentException("Missing value: Quantity");
        }
        this.quantity = quantity;
    }

    public java.math.BigDecimal getCost() {
        return this.cost;
    }

    public void setCost(java.math.BigDecimal cost) {
        if (cost == null) {
            throw new IllegalArgumentException("Missing value: Cost");
        }
        this.cost = cost;
    }

    public java.lang.Integer getProductId() {
        return this.productId;
    }

    public void setProductId(java.lang.Integer productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Missing value: Product Id");
        }
        this.productId = productId;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Missing value: Name");
        }
        this.name = name;
    }

    public java.lang.Double getPrice() {
        return this.price;
    }

    public void setPrice(java.lang.Double price) {
        if (price == null) {
            throw new IllegalArgumentException("Missing value: Price");
        }
        this.price = price;
    }

    public java.lang.String getComments() {
        return this.comments;
    }

    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }

    public Timestamp getModifiedOn() {
        return this.modifiedOn;
    }

    public void setModifiedOn(Timestamp modifiedOn) {
        this.modifiedOn = modifiedOn;
    }
}

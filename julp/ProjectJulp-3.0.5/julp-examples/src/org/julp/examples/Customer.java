package org.julp.examples;

import java.util.Set;

public class Customer /*extends org.julp.AbstractDomainObject */ { 

    private static final long serialVersionUID = 3848111596275851680L;
    protected java.lang.Integer customerId;
    protected java.lang.String firstName;
    protected java.lang.String lastName;
    protected java.lang.String street;
    protected java.lang.String city;    
    protected Set<Invoice> invoices;

    public Customer() {
    }

    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
        this.customerId = customerId;
    }

    public java.lang.String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(java.lang.String firstName) {
        this.firstName = firstName;
    }

    public java.lang.String getLastName() {
        return this.lastName;
    }

    public void setLastName(java.lang.String lastName) {
        this.lastName = lastName;
    }

    public java.lang.String getStreet() {
        return this.street;
    }

    public void setStreet(java.lang.String street) {
        this.street = street;
    }

    public java.lang.String getCity() {
        return this.city;
    }

    public void setCity(java.lang.String city) {
        this.city = city;
    }
    
    public Set<Invoice> getInvoices() {
        return invoices;
    }

    public void setInvoices(Set<Invoice> invoices) {
        this.invoices = invoices;
    }
}

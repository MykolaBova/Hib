package org.julp.db;

import org.julp.AbstractDomainObject;

public class CustomerExtendindAbstractDomainObject extends AbstractDomainObject {

    protected java.lang.Integer customerId;
    protected java.lang.String firstName;
    protected java.lang.String lastName;
    protected java.lang.String street;
    protected java.lang.String city;

    public java.lang.Integer getCustomerId() {
        return this.customerId;
    }

    public void setCustomerId(java.lang.Integer customerId) {
        if (!isLoading()) {
            /*
             * DBColumn nullability: NoNulls - modify as needed
             */
            if (customerId == null) {
                throw new IllegalArgumentException("Missing field: customerId");
            }
            if (!customerId.equals(this.customerId)) {
                this.modified = true;
            }
        }
        this.customerId = customerId;
    }

    public java.lang.String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(java.lang.String firstName) {
        if (!isLoading()) {
            /*
             * DBColumn nullability: NoNulls - modify as needed
             */
            if (firstName == null) {
                throw new IllegalArgumentException("Missing field: firstName");
            }

        }
        if (!firstName.equals(this.firstName)) {
            this.modified = true;
        }
        this.firstName = firstName;
    }

    public java.lang.String getLastName() {
        return this.lastName;
    }

    public void setLastName(java.lang.String lastName) {
        if (!isLoading()) {
            /*
             * DBColumn nullability: NoNulls - modify as needed
             */
            if (lastName == null) {
                throw new IllegalArgumentException("Missing field: lastName");
            }
            if (!lastName.equals(this.lastName)) {
                this.modified = true;
            }
        }
        this.lastName = lastName;
    }

    public java.lang.String getStreet() {
        return this.street;
    }

    public void setStreet(java.lang.String street) {
        if (!isLoading()) {
            /*
             * DBColumn nullability: NoNulls - modify as needed
             */
            if (street == null) {
                throw new IllegalArgumentException("Missing field: street");
            }
            if (!street.equals(this.street)) {
                this.modified = true;
            }
        }
        this.street = street;
    }

    public java.lang.String getCity() {
        return this.city;
    }

    public void setCity(java.lang.String city) {
        if (!isLoading()) {
            /*
             * DBColumn nullability: NoNulls - modify as needed
             */
            if (city == null) {
                throw new IllegalArgumentException("Missing field: city");
            }
            if (!city.equals(this.city)) {
                this.modified = true;
            }
        }
        this.city = city;
    }
}

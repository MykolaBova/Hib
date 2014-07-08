package org.julp.examples;

public class CustomerWithPhone extends Customer {

    private static final long serialVersionUID = -8172540514352482061L;
    protected java.lang.String phone;

    public CustomerWithPhone() {
    }

    public java.lang.String getPhone() {
        return this.phone;
    }

    public void setPhone(java.lang.String phone) {
        this.phone = phone;
    }
}

package org.julp.examples;

public class Product {

    private static final long serialVersionUID = -4166510180518275833L;
    protected java.lang.Integer productId;
    protected java.lang.String name;
    protected double price;
    protected java.lang.String comments;

    public Product() {
    }

    public java.lang.Integer getProductId() {
        return this.productId;
    }

    public void setProductId(java.lang.Integer productId) {
        /* DBColumn nullability: NoNulls - modify as needed */
        if (productId == null) {
            throw new IllegalArgumentException("Missing field: productId");
        }
        this.productId = productId;
    }

    public java.lang.String getName() {
        return this.name;
    }

    public void setName(java.lang.String name) {
        /* DBColumn nullability: NoNulls - modify as needed */
        if (name == null) {
            throw new IllegalArgumentException("Missing field: name");
        }
        this.name = name;
    }

    public double getPrice() {
        return this.price;
    }

    public void setPrice(double price) {
        /* DBColumn nullability: NoNulls - modify as needed */
        if (price <= 0.0) {
            throw new IllegalArgumentException("Invalid Price: " + price);
        }
        this.price = price;
    }

    public java.lang.String getComments() {
        return this.comments;
    }

    public void setComments(java.lang.String comments) {
        this.comments = comments;
    }
}

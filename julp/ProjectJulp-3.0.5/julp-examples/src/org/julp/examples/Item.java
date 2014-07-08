package org.julp.examples;

public class Item extends org.julp.AbstractDomainObject {

    private static final long serialVersionUID = 8272241061874128668L;
    protected java.lang.Integer invoiceId;
    protected java.lang.Integer item;
    protected java.lang.Integer productId;
    protected java.lang.Integer quantity;
    protected java.math.BigDecimal cost;

    public Item() {
    }

    public java.lang.Integer getInvoiceId() {
        return this.invoiceId;
    }

    public void setInvoiceId(java.lang.Integer invoiceId) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (invoiceId == null) {
                throw new IllegalArgumentException("Missing field: invoiceId");
            }
            if (!invoiceId.equals(this.invoiceId)) {
                this.modified = true;
            }
        }
        this.invoiceId = invoiceId;
    }

    public java.lang.Integer getItem() {
        return this.item;
    }

    public void setItem(java.lang.Integer item) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (item == null) {
                throw new IllegalArgumentException("Missing field: item");
            }
            if (!item.equals(this.item)) {
                this.modified = true;
            }
        }
        this.item = item;
    }

    public java.lang.Integer getProductId() {
        return this.productId;
    }

    public void setProductId(java.lang.Integer productId) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (productId == null) {
                throw new IllegalArgumentException("Missing field: productId");
            }
            if (!productId.equals(this.productId)) {
                this.modified = true;
            }
        }
        this.productId = productId;
    }

    public java.lang.Integer getQuantity() {
        return this.quantity;
    }

    public void setQuantity(java.lang.Integer quantity) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (quantity == null) {
                throw new IllegalArgumentException("Missing field: quantity");
            }
            if (!quantity.equals(this.quantity)) {
                this.modified = true;
            }
        }
        this.quantity = quantity;
    }

    public java.math.BigDecimal getCost() {
        return this.cost;
    }

    public void setCost(java.math.BigDecimal cost) {
        if (!isLoading()) {
            /* DBColumn nullability: NoNulls - modify as needed */
            if (cost == null) {
                throw new IllegalArgumentException("Missing field: cost");
            }
            if (!cost.equals(this.cost)) {
                this.modified = true;
            }
        }
        this.cost = cost;
    }
}

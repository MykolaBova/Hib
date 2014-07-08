package org.julp;

public class ValueObject<T> implements java.io.Serializable, Comparable<ValueObject<T>> {

    private static final long serialVersionUID = 109103127983201986L;
    protected T value;
    protected String valueLabel;
    protected boolean compareByValue = false;
    protected boolean errorOnNullCompare;

    public ValueObject() {
    }

    public ValueObject(T value, String valueLabel) {
        this.value = value;
        this.valueLabel = valueLabel;
    }

    public ValueObject(T value, String valueLabel, boolean compareByValue) {
        this.value = value;
        this.valueLabel = valueLabel;
        this.compareByValue = compareByValue;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public java.lang.String getValueLabel() {
        if (valueLabel == null) {
            if (value != null) {
                valueLabel = value.toString();
            } else {
                valueLabel = "";
            }
        }
        return valueLabel;
    }

    public void setValueLabel(java.lang.String valueLabel) {
        this.valueLabel = valueLabel;
    }

    public boolean isCompareByValue() {
        return compareByValue;
    }

    public void setCompareByValue(boolean compareByValue) {
        this.compareByValue = compareByValue;
    }

    @Override
    public String toString() {
        return isCompareByValue() ? (value == null ? "" : value.toString()) : getValueLabel();
    }

    @Override
    @SuppressWarnings("unchecked")
    public int compareTo(ValueObject<T> otherObj) {
        if (otherObj == null || otherObj.getValue() == null || this.getValue() == null) {
            if (errorOnNullCompare) {
                throw new NullPointerException("ValueObject is null or ValueObject.getValue() is null");
            } else {
                return -1;
            }
        }
        if (this.equals(otherObj)) {
            return 0;
        }
        if (compareByValue) {
            return ((Comparable<Comparable<?>>) this.value).compareTo((Comparable<Comparable<?>>) otherObj.value);
        } else {
            return this.getValueLabel().compareTo(otherObj.getValueLabel());
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        if (compareByValue) {
            hash = 97 * hash + (this.value != null ? this.value.hashCode() : 0);
        } else {
            hash = 97 * hash + (this.valueLabel != null ? this.valueLabel.hashCode() : 0);
        }                
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ValueObject<T> other = (ValueObject<T>) obj;
        if (compareByValue) {
            if (this.value != other.value && (this.value == null || !this.value.equals(other.value))) {
                return false;
            }
        } else {
            if ((this.valueLabel == null) ? (other.valueLabel != null) : !this.valueLabel.equals(other.valueLabel)) {
                return false;
            }
        }
        return true;
    }
 
    public boolean isErrorOnNullCompare() {
        return errorOnNullCompare;
    }

    public void setErrorOnNullCompare(boolean errorOnNullCompare) {
        this.errorOnNullCompare = errorOnNullCompare;
    }
}

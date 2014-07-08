package org.julp;

/**
 * This object is holding original values from database.
 * The values are using to build UPDATE and WHERE clauses
 * fields are "1 - based" like java.sql.ResultSet.
 */
public class DataHolder implements java.io.Serializable {

    private static final long serialVersionUID = -3308019020586724852L;
    protected int fieldsCount = 0;
    protected Object[] data = null;
    protected String[] fieldNames = null;
    protected boolean ignoreUndefinedFieldName = false;

    public DataHolder() {
    }

    public DataHolder(int fieldsCount) {
        this.fieldsCount = fieldsCount;
        fieldNames = new String[fieldsCount];
        data = new Object[fieldsCount];
    }

    public int getFieldsCount() {
        return fieldsCount;
    }

    public void setFieldsCount(int fieldsCount) {
        this.fieldsCount = fieldsCount;
        fieldNames = new String[fieldsCount];
        data = new Object[fieldsCount];
    }

    public void setFieldValue(int fieldIndex, Object value) {
        this.data[fieldIndex - 1] = value;
    }

    public Object getFieldValue(int fieldIndex) {
        return this.data[fieldIndex - 1];
    }

    public void setFieldNameAndValue(int fieldIndex, String fieldName, Object value) {
        this.data[fieldIndex - 1] = value;
        if (this.fieldNames == null) {
            this.fieldNames = new String[this.fieldsCount];
        }
        this.fieldNames[fieldIndex - 1] = fieldName;
    }
    
    public void addFieldNameAndValue(String fieldName, Object value) {
        int fieldIndex = findEmptyIndex(true);
        if (fieldIndex != -1) {
            setFieldNameAndValue(fieldIndex + 1, fieldName, value);
        } else {
            throw new IndexOutOfBoundsException("addFieldNameAndValue: no empty indexes. (fieldsCount: " + fieldsCount + ")");
        }
    }
    
    /** returns empty index searching from the beginning or the end */
    public int findEmptyIndex(boolean upperBound) {
        int idx = 0;
        int upper = fieldsCount - 1;
        boolean found = false;
        if (!upperBound) {
            for (; idx < fieldsCount; idx++) {
                if (fieldNames[idx] == null) {
                    found = true;
                    break;
                }
            }   
            if  (found) {
                return idx + 1;
            } else {
                return -1;
            }
        } else {                                       
            for (; upper >= idx; upper--) {
                if (fieldsCount == 1 && fieldNames[upper] == null) {
                    found = true;
                } else if (fieldNames[upper] != null) {
                    if (upper == fieldsCount - 1) {
                        found = false;
                    } else {
                        found = true;
                    }                    
                    break;
                }
            }   
            if  (found) {
                return upper + 1;
            } else {
                return -1;
            }
        }
    }
        
    public void setFieldValue(String fieldName, Object value) {
        int fieldIndex = findFieldIndex(fieldName);
        if (fieldIndex < 0) {
            if (this.fieldNames == null) {
                if (!ignoreUndefinedFieldName) {
                    throw new IllegalArgumentException("Field Names are not set");
                }
                System.out.println("Field Names are not set");
            } else {
                if (!ignoreUndefinedFieldName) {
                    throw new IllegalArgumentException("No such field: " + fieldName);
                }
            }
        }
        this.data[fieldIndex - 1] = value;
    }

    public Object getFieldValue(String fieldName) {
        if (fieldName == null) {
            return null;
        }
        int fieldIndex = findFieldIndex(fieldName);
        if (fieldIndex < 0) {
            if (this.fieldNames == null) {
                if (!ignoreUndefinedFieldName) {
                    throw new IllegalArgumentException("Field Names are not set");
                }
            } else {
                if (!ignoreUndefinedFieldName) {
                    throw new IllegalArgumentException("No such field: " + fieldName);
                }
            }
            return null;
        }
        return this.data[fieldIndex - 1];
    }

    public void setFieldName(int fieldIndex, String fieldName) {
        if (this.fieldNames == null) {
            this.fieldNames = new String[this.fieldsCount];
        }
        this.fieldNames[fieldIndex - 1] = fieldName;
    }

    public String getFieldName(int fieldIndex) {
        if (this.fieldNames == null || this.fieldNames.length == 0) {
            return null;
        }
        return this.fieldNames[fieldIndex - 1];
    }

    public int findFieldIndex(String fieldName) {
        int fieldIndex = -1;
        if (this.fieldNames == null || fieldName == null) {
            return fieldIndex;
        }
        for (int i = 0; i < this.fieldsCount; i++) {
            if (fieldNames[i] == null) {
                continue;
            }
            if (fieldNames[i].equals(fieldName)) {
                fieldIndex = i + 1;
                break;
            }
        }
        return fieldIndex;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        if (this.fieldNames != null && this.fieldNames.length == fieldsCount) {
            for (int i = 0; i < fieldsCount; i++) {
                sb.append(this.fieldNames[i]).append("=");
                if (this.data[i] == null) {
                } else {
                    sb.append(this.data[i]);
                }
                sb.append("&");
            }
            sb.deleteCharAt(sb.length() - 1);
        } else {
            for (int i = 0; i < fieldsCount; i++) {
                if (this.data[i] == null) {
                    sb.append("null");
                } else {
                    sb.append(this.data[i]);
                }
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }

    public java.lang.Object[] getData() {
        return this.data;
    }

    public void setData(java.lang.Object[] data) {
        this.data = data;
    }

    public boolean isIgnoreUndefinedFieldName() {
        return ignoreUndefinedFieldName;
    }

    public void setIgnoreUndefinedFieldName(boolean ignoreUndefinedFieldName) {
        this.ignoreUndefinedFieldName = ignoreUndefinedFieldName;
    }
}

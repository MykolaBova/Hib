package org.julp.search;

import java.util.*;

public class SearchCriteriaHolder implements java.io.Serializable {

    private static final long serialVersionUID = -8241419034422841941L;
    protected String fieldLabel = null;
    protected String fieldName = null;
    protected String operator = "=";
    protected String functions = null;
    protected Object searchValue = null;
    protected String booleanCondition = AND;
    public static final String LESS = "<";
    public static final String LESS_OR_EQUAL = "<=";
    public static final String EQUAL = "=";
    public static final String LIKE = "LIKE";
    public static final String NOT_LIKE = "NOT LIKE";
    public static final String IN = "IN";
    public static final String NOT_IN = "NOT IN";
    public static final String NOT_EQUAL = "<>";
    public static final String NOT_EQUAL_ = "!=";
    public static final String NOT_EXISTS = "NOT EXISTS";
    public static final String EXISTS = "EXISTS";
    public static final String BETWEEN = "BETWEEN";
    public static final String NOT_BETWEEN = "NOT BETWEEN";
    public static final String IS = "IS";
    public static final String IS_NOT = "IS NOT";
    public static final String GREATER_OR_EQUAL = ">=";
    public static final String GREATER = ">";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String AND_NESTED_LOGIC_START = "AND (";
    public static final String OR_NESTED_LOGIC_START = "OR (";
    public static final String AND_NESTED_LOGIC_END = ") AND";
    public static final String OR_NESTED_LOGIC_END = ") OR";
    public static final String AND_NESTED_LOGIC = ") AND (";
    public static final String OR_NESTED_LOGIC = ") OR (";
    public static final String NESTED_LOGIC_START = "(";
    public static final String NESTED_LOGIC_END = ")";
    // hints for search using LIKE operator
    public enum LIKE_HINTS {STARTS_WITH, CONTAINS, ENDS_WITH};
    // default hint
    protected LIKE_HINTS likeHint = LIKE_HINTS.STARTS_WITH;
    protected String operators = "<=NOT LIKE!=NOT IN<>NOT EXISTSNOT BETWEEN IS NOT >=";
    protected Map<?, ?> searchValuesMap = null;
    protected List<?> searchValuesList = null;
    protected boolean literalParameter = false;
    protected String overrideColumnName;
    protected boolean overrideOperator;
    protected boolean overrideBooleanCondition;

    public SearchCriteriaHolder() {
    }

    /** Getter for property fieldName.
     * @return Value of property fieldName.
     *
     */
    public java.lang.String getFieldName() {
        return fieldName;
    }

    /** Setter for property fieldName.
     * @param fieldName New value of property fieldName.
     *
     */
    public void setFieldName(java.lang.String fieldName) {
        this.fieldName = fieldName;
    }

    /** Getter for property operator.
     * @return Value of property operator.
     *
     */
    public java.lang.String getOperator() {
        return operator;
    }

    /** Setter for property operator.
     * @param operator New value of property operator.
     *
     */
    public void setOperator(java.lang.String operator) {
        if (operators.indexOf(operator.toUpperCase()) == -1 && !overrideOperator) {
            throw new IllegalArgumentException("Invalid Operator: " + operator);
        }
        this.operator = operator;
    }

    /** Getter for property searchValue.
     * @return Value of property searchValue.
     *
     */
    public java.lang.Object getSearchValue() {
        return searchValue;
    }

    /** Setter for property searchValue.
     * @param searchValue New value of property searchValue.
     *
     */
    public void setSearchValue(java.lang.Object searchValue) {
        this.searchValue = searchValue;
    }

    /** Getter for property booleanCondition.
     * @return Value of property booleanCondition.
     *
     */
    public java.lang.String getBooleanCondition() {
        return booleanCondition;
    }

    /** Setter for property booleanCondition.
     * @param booleanCondition New value of property booleanCondition.
     *
     */
    public void setBooleanCondition(java.lang.String booleanCondition) {
        if (booleanCondition == null) {
            throw new IllegalArgumentException("Missing Boolean Condition. (AND/OR)");
        }
        String tmp = booleanCondition.toUpperCase();
        if (booleanCondition.equalsIgnoreCase(AND)
                || tmp.equals(OR)
                || tmp.equals(AND_NESTED_LOGIC_START)
                || tmp.equals(AND_NESTED_LOGIC_END)
                || tmp.equals(OR_NESTED_LOGIC_START)
                || tmp.equals(NESTED_LOGIC_START)
                || tmp.equals(NESTED_LOGIC_END)
                || tmp.equals(OR_NESTED_LOGIC_END)
                || tmp.equals(OR_NESTED_LOGIC)
                || tmp.equals(AND_NESTED_LOGIC)) {
        } else {
            if (!overrideBooleanCondition) {
                throw new IllegalArgumentException("Invalid Boolean Condition: \n\n" + booleanCondition + " \n\nOnly AND/OR must be used");
            }
        }
        this.booleanCondition = booleanCondition;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String space = " ";
        if (functions != null) {
            sb.append(functions.replace("${}", fieldName));
        } else {
            sb.append(fieldName);
        }
        sb.append(space).append(operator).append(space);
        if (searchValuesList != null) {
            sb.append(searchValuesList);
        } else {
            sb.append(searchValue);
        }
        sb.append(space).append(booleanCondition).append(space);

        return sb.toString();
    }

    /** Getter for property searchValuesMap.
     * @return Value of property searchValuesMap.
     *
     */
    public java.util.Map<?, ?> getSearchValuesMap() {
        return searchValuesMap;
    }

    /** Setter for property searchValuesMap.
     * @param searchValuesMap New value of property searchValuesMap.
     *
     */
    public void setSearchValuesMap(java.util.Map<?, ?> searchValuesMap) {
        this.searchValuesMap = searchValuesMap;
    }

    /** Getter for property fieldLabel.
     * @return Value of property fieldLabel.
     *
     */
    public java.lang.String getFieldLabel() {
        return fieldLabel;
    }

    /** Setter for property fieldLabel.
     * @param fieldLabel New value of property fieldLabel.
     *
     */
    public void setFieldLabel(java.lang.String fieldLabel) {
        this.fieldLabel = fieldLabel;
    }

    /**
     * Getter for property searchValuesList.
     * @return Value of property searchValuesList.
     */
    public java.util.List<?> getSearchValuesList() {
        return searchValuesList;
    }

    /**
     * Setter for property searchValuesList.
     * @param searchValuesList New value of property searchValuesList.
     */
    public void setSearchValuesList(java.util.List<?> searchValuesList) {
        this.searchValuesList = searchValuesList;
    }

    /**
     * Getter for property likeHint.
     * @return Value of property likeHint.
     */
    public LIKE_HINTS getLikeHint() {
        return likeHint;
    }

    /**
     * Setter for property likeHint.
     * @param likeHint New value of property likeHint.
     */
    public void setLikeHint(LIKE_HINTS likeHint) {
        this.likeHint = likeHint;
    }

    /**
     * Getter for property functions.
     * @return Value of property functions.
     */
    public java.lang.String getFunctions() {
        return this.functions;
    }

    /**
     * Setter for property functions.
     * @param functions New value of property functions.
     * Example:
     * Assume the value of the field is 'This is a test'
     *
     * SearchCriteriaHolder holder = new SearchCriteriaHolder();
     * ...
     * holder.setFunctions("CONCAT(UPPER(SUBSTR(${}, 10)), 'XYZ')" );
     * ...
     * result would be: TESTXYZ
     */
    public void setFunctions(java.lang.String functions) {
        this.functions = functions;
    }


    public boolean isLiteralParameter()
   {
      return literalParameter;
   }

   public void setLiteralParameter(boolean literalParameter)
   {
      this.literalParameter = literalParameter;
   }

    public String getOverrideColumnName() {
        return overrideColumnName;
    }

    public void setOverrideColumnName(String overrideColumnName) {
        this.overrideColumnName = overrideColumnName;
    }

    public boolean isOverrideOperator() {
        return overrideOperator;
    }

    public void setOverrideOperator(boolean overrideOperator) {
        this.overrideOperator = overrideOperator;
    }

    public boolean isOverrideBooleanCondition() {
        return overrideBooleanCondition;
    }

    public void setOverrideBooleanCondition(boolean overrideBooleanCondition) {
        this.overrideBooleanCondition = overrideBooleanCondition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SearchCriteriaHolder other = (SearchCriteriaHolder) obj;
        if ((this.fieldName == null) ? (other.fieldName != null) : !this.fieldName.equals(other.fieldName)) {
            return false;
        }
        if ((this.operator == null) ? (other.operator != null) : !this.operator.equals(other.operator)) {
            return false;
        }
        if ((this.functions == null) ? (other.functions != null) : !this.functions.equals(other.functions)) {
            return false;
        }
        if (this.searchValue != other.searchValue && (this.searchValue == null || !this.searchValue.equals(other.searchValue))) {
            return false;
        }
        if ((this.booleanCondition == null) ? (other.booleanCondition != null) : !this.booleanCondition.equals(other.booleanCondition)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 53 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
        hash = 53 * hash + (this.operator != null ? this.operator.hashCode() : 0);
        hash = 53 * hash + (this.functions != null ? this.functions.hashCode() : 0);
        hash = 53 * hash + (this.searchValue != null ? this.searchValue.hashCode() : 0);
        hash = 53 * hash + (this.booleanCondition != null ? this.booleanCondition.hashCode() : 0);
        return hash;
    }
}

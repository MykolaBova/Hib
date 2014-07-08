package org.julp.search;

import java.lang.reflect.Method;
import java.util.*;
import org.julp.*;
import org.julp.db.DBMetaData;

/**
 *  This object is not intended to be a *real* Query engine.
 *  It is intended to be used with GUI to allow end users append WHERE clause to SELECT statement.
 *  How it works?
 *  <ul>
 *    <li>User would add Criteria</li>
 *    <li>Criteria would have
 *      <ol>
 *        <li>List of fields to search. User must select one of the fields</li>
 *        <li>List of operators for each field (fieldOperators). You can specify which operators can be used with selected field. For example you can remove "LIKE" operator for numeric field</li>
 *        <li>Some fields can have list of values to select from (fieldValues). Example: field "State" can have a drop-down control (combobox) with list of states</li>
 *        <li>Boolean condition AND/OR. User must select one of them if there will be another Criteria</li>
 *      </ol>
 *     </li>
 *     <li>Use <code>beforeBuildCriteria()</code> in descendants to modify/validate user input</li>
 *     <li>After user done <code>buildCriteria()</code> would generate WHERE clause, which would be added to pre-defined SELECT statement</li>
 *     <li>To make it user-friendly fields, operators, etc. should have "display values" and "real values"
 *       Example:
 *       <code>
 *       fields.add(new SomeObject("firstName", "First Name"));
 *       fields.add(new SomeObject("lastName", "Last Name"));
 *       fields.add(new SomeObject("state", "State"));<br>
 *       ...
 *       </code>
 *     </li>
 *   </ul>
 */

public abstract class AbstractSearchCriteriaBuilder {

    protected List fields;
    protected DBMetaData<?> metaData;
    protected List<?> operatorsList;
    protected Map<Object, List<?>> fieldOperators;
    protected Map<Object, List<?>> fieldValues;
    protected List<SearchCriteriaHolder> searchCriteriaHolders = new ArrayList<>();
    protected List<Object> arguments = new ArrayList<>();
    protected String select = "";
    protected String from = "";
    protected String joins = "";
    protected String where = "";
    protected String groupBy = "";
    protected String having = "";
    protected String orderBy = "";
    protected String query = "";
    protected String dynamicWhere = "";
    protected String executable;
    protected Map<String, SearchCriteriaHolder.LIKE_HINTS> likeHints = new HashMap<>(3);
    protected Set<String> adhocColumns = new HashSet<>();
    protected boolean lowerCaseKeywords = true;
    protected static final String SPACE = " ";
    public static final String PARAM_PLACEHOLDER = "?";
    public static final String COMMA = ",";


    public AbstractSearchCriteriaBuilder() {
    }

    /** Override this method to add/modify arguments, etc */
    public void beforeBuildCriteria() {
    }

    /** Override this method to add/modify arguments, etc */
    public void afterBuildCriteria() {
    }

    public void reset() {
        //if (fields != null) fields.clear();
        //metaData = null;
        //if (operatorsList != null) operatorsList.clear();
        //if (fieldOperators != null) fieldOperators.clear(); 
        //if (fieldValues != null) fieldValues.clear();
        if (searchCriteriaHolders != null) {
            searchCriteriaHolders.clear();
        }
        if (arguments != null) {
            arguments.clear();
        }
        if (adhocColumns != null) {
            adhocColumns.clear();
        }
        //if (likeHint != null) likeHint.clear();
        select = "";
        from = "";
        joins = "";
        where = "";
        groupBy = "";
        having = "";
        orderBy = "";
        query = "";
        dynamicWhere = "";
    }

    public String buildCriteria() {
        beforeBuildCriteria();
        if (arguments != null) {
            arguments.clear();
        }
        StringBuilder sb = new StringBuilder();
        int size = searchCriteriaHolders.size();
        Iterator<SearchCriteriaHolder> it = searchCriteriaHolders.iterator();
        int count = 0;
        while (it.hasNext()) {
            count++;
            SearchCriteriaHolder holder = it.next();
            String fieldName = holder.getFieldName();
            String functions = holder.getFunctions();
            if (fieldName == null || fieldName.trim().length() == 0) {
                throw new IllegalArgumentException("Search: missing field name");
            }
            String operator = holder.getOperator();
            Object searchValue = holder.getSearchValue();
            String booleanCondition = holder.getBooleanCondition();
            String columnName;
            try {
                if (holder.getOverrideColumnName() != null) {
                    columnName = holder.getOverrideColumnName();
                } else {
                    columnName = metaData.getFullColumnName(metaData.getFieldIndexByFieldName(fieldName));
                }
                holder.setFieldName(columnName);
                adhocColumns.add(columnName);
            } catch (Exception e) {
                throw new DataAccessException(e);
            }
            if (functions != null) {
                sb.append(replace(functions, columnName));
            } else {
                sb.append(columnName);
            }
            if (holder.getSearchValuesList() != null && !holder.getSearchValuesList().isEmpty()) {
                if (!operator.equals(SearchCriteriaHolder.IN) && !operator.equals(SearchCriteriaHolder.NOT_IN)) {
                    throw new IllegalArgumentException("Must use \"IN\"/\"NOT IN\" operator");
                }
                sb.append(SPACE).append(operator).append(" (");
                for (Object arg : holder.getSearchValuesList()) {
                    sb.append(PARAM_PLACEHOLDER).append(COMMA);
                    this.arguments.add(arg);
                }
                sb.deleteCharAt(sb.lastIndexOf(COMMA));
                sb.append(") ");
            } else {
                if (searchValue == null) {
                    if (operator.equals(SearchCriteriaHolder.EQUAL)) {
                        sb.append(SPACE).append(lowerCaseKeywords ? "is null" : "IS NULL").append(SPACE);
                    } else if (operator.equals(SearchCriteriaHolder.NOT_EQUAL) || operator.equals(SearchCriteriaHolder.NOT_EQUAL_)) {
                        sb.append(SPACE).append(lowerCaseKeywords ? "is not null" : "IS NOT NULL").append(SPACE);
                    } else {
                        throw new IllegalArgumentException("Invalid operator for empty search value");
                    }
                } else {
                    sb.append(SPACE).append(operator).append(SPACE);
                    if (holder.isLiteralParameter()) {
                       sb.append(holder.getSearchValue());
                    } else {
                       sb.append(PARAM_PLACEHOLDER);
                       this.arguments.add(searchValue);
                    }
                }
            }
            if (count < size) {
                sb.append(SPACE).append(booleanCondition).append("\n").append(SPACE);
            } else {
                if (booleanCondition.equals(SearchCriteriaHolder.OR_NESTED_LOGIC)
                        || booleanCondition.equals(SearchCriteriaHolder.AND_NESTED_LOGIC)
                        || booleanCondition.equals(SearchCriteriaHolder.AND_NESTED_LOGIC_END)
                        || booleanCondition.equals(SearchCriteriaHolder.OR_NESTED_LOGIC_END)
                        || booleanCondition.equals(SearchCriteriaHolder.NESTED_LOGIC_END)) {
                    sb.append(SPACE).append(") ");
                } else {
                    //sb.append(SPACE).append(booleanCondition);                                
                }
            }
        }
        if (searchCriteriaHolders.isEmpty()) {
            throw new DataAccessException("No search criteria");
        }
        setDynamicWhere(sb.toString());
        afterBuildCriteria();
        return dynamicWhere;
    }

    public String setSort(List<String[]> sort) {
        return setSort(sort, true, true);
    }

    public String setSort(List<String[]> sort, boolean findMapping) {
        return setSort(sort, findMapping, true);
    }

    /** Change original "ORDER BY" clause.
    @findMapping == true means convert field name into column name
    @replace == true replace "OREDER BY" vs. append.
     */
    public String setSort(List<String[]> sort, boolean findMapping, boolean replace) {
        if (sort.isEmpty()) {
            return orderBy;
        }
        StringBuilder sb = new StringBuilder(lowerCaseKeywords ? " order by " : " ORDER BY ");
        if (!replace) {
            if (orderBy != null && !orderBy.trim().equals("")) {
                sb.append(orderBy).append(COMMA);
            }
        }
        Iterator<String[]> iter = sort.iterator();
        while (iter.hasNext()) {
            String[] fieldNameAndSortDir = iter.next();
            String columnName;
            if (findMapping) {
                try {
                    columnName = metaData.getFullColumnName(metaData.getFieldIndexByFieldName(fieldNameAndSortDir[0]));
                } catch (Exception sqle) {
                    throw new DataAccessException(sqle);
                }
            } else {
                columnName = fieldNameAndSortDir[0];
            }
            sb.append(columnName);
            if (fieldNameAndSortDir[1] != null) {
                sb.append(SPACE).append(fieldNameAndSortDir[1]);
            }
            sb.append(COMMA);
        }
        int idx = sb.lastIndexOf(",");
        if (idx > -1) {
            sb.deleteCharAt(idx);
        }
        orderBy = sb.toString();
        return orderBy;
    }

    public void addSearch() {
        this.searchCriteriaHolders.add(new SearchCriteriaHolder());
    }

    public void removeSearch(int idx) {
        try {
            searchCriteriaHolders.remove(idx);
        } catch (IndexOutOfBoundsException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    public int size() {
        return searchCriteriaHolders.size();
    }

    /** Getter for property fields.
     * @return Value of property fields.
     *
     */
    public java.util.List getFields() {
        return fields;
    }

    /** Setter for property fields.
     * @param fields New value of property fields.
     *
     */
    public void setFields(java.util.List fields) {
        this.fields = fields;
    }

    /** Getter for property operatorsList.
     * @return Value of property operatorsList.
     *
     */
    public java.util.List<?> getOperatorsList() {
        return operatorsList;
    }

    /** Setter for property operatorsList.
     * @param operatorsList New value of property operatorsList.
     *
     */
    public void setOperatorsList(java.util.List<String> operatorsList) {
        this.operatorsList = operatorsList;
    }

    /** Getter for property fieldOperators.
     * @return Value of property fieldOperators.
     *
     */
    public java.util.Map<Object, List<?>> getFieldOperators() {
        return fieldOperators;
    }

    /** Setter for property fieldOperators.
     * @param fieldOperators New value of property fieldOperators.
     *
     */
    public void setFieldOperators(java.util.Map<Object, List<?>> fieldOperators) {
        this.fieldOperators = fieldOperators;
    }

    /** Getter for property fieldValues.
     * @return Value of property fieldValues.
     *
     */
    public java.util.Map<Object, List<?>> getFieldValues() {
        return fieldValues;
    }

    /** Setter for property fieldValues.
     * @param fieldValues New value of property fieldValues.
     *
     */
    public void setFieldValues(java.util.Map<Object, List<?>> fieldValues) {
        this.fieldValues = fieldValues;
    }

    /** Getter for property searchCriteriaHolders.
     * @return Value of property searchCriteriaHolders.
     *
     */
    public java.util.List<SearchCriteriaHolder> getSearchCriteriaHolders() {
        return searchCriteriaHolders;
    }

    /** Setter for property searchCriteriaHolders.
     * @param searchCriteriaHolders New value of property searchCriteriaHolders.
     *
     */
    public void setSearchCriteriaHolders(java.util.List<SearchCriteriaHolder> searchCriteriaHolders) {
        this.searchCriteriaHolders = searchCriteriaHolders;
    }

    /** Getter for property arguments.
     * @return Value of property arguments.
     *
     */
    public java.util.List<?> getArguments() {
        return arguments;
    }

    /** Setter for property arguments.
     * @param arguments New value of property arguments.
     *
     */
    public void setArguments(java.util.List<Object> arguments) {
        this.arguments = arguments;
    }

    /** Getter for property metaData.
     * @return Value of property metaData.
     *
     */
    public DBMetaData<?> getMetaData() {
        return metaData;
    }

    /** Setter for property metaData.
     * @param metaData New value of property metaData.
     *
     */
    public void setMetaData(DBMetaData<?> metaData) {
        this.metaData = metaData;
    }

    /**
     * Getter for property select.
     * @return Value of property select.
     */
    public java.lang.String getSelect() {
        return select;
    }

    /**
     * Setter for property select.
     * @param select New value of property select.
     */
    public void setSelect(java.lang.String select) {
        if (select != null) {
            this.select = select.trim();
        }
    }

    /**
     * Getter for property from.
     * @return Value of property from.
     */
    public java.lang.String getFrom() {
        return from;
    }

    /**
     * Setter for property from.
     * @param from New value of property from.
     */
    public void setFrom(java.lang.String from) {
        if (from != null) {
            this.from = from.trim();
        }
    }

    /**
     * Getter for property joins.
     * @return Value of property joins.
     */
    public java.lang.String getJoins() {
        return joins;
    }

    /**
     * Setter for property joins.
     * @param joins New value of property joins.
     */
    public void setJoins(java.lang.String joins) {
        if (joins != null) {
            this.joins = joins.trim();
        }
    }

    /**
     * Getter for property where.
     * @return Value of property where.
     */
    public java.lang.String getWhere() {
        return where;
    }

    /**
     * Setter for property where.
     * @param where New value of property where.
     */
    public void setWhere(java.lang.String where) {
        if (where != null) {
            this.where = where.trim();
        }
    }

    /**
     * Getter for property groupBy.
     * @return Value of property groupBy.
     */
    public java.lang.String getGroupBy() {
        return groupBy;
    }

    /**
     * Setter for property groupBy.
     * @param groupBy New value of property groupBy.
     */
    public void setGroupBy(java.lang.String groupBy) {
        if (groupBy != null) {
            this.groupBy = groupBy.trim();
        }
    }

    /**
     * Getter for property having.
     * @return Value of property having.
     */
    public java.lang.String getHaving() {
        return having;
    }

    /**
     * Setter for property having.
     * @param having New value of property having.
     */
    public void setHaving(java.lang.String having) {
        if (having != null) {
            this.having = having.trim();
        }
    }

    /**
     * Getter for property orderBy.
     * @return Value of property orderBy.
     */
    public java.lang.String getOrderBy() {
        return orderBy;
    }

    /**
     * Setter for property orderBy.
     * @param orderBy New value of property orderBy.
     */
    public void setOrderBy(java.lang.String orderBy) {
        if (orderBy != null) {
            this.orderBy = orderBy.trim();
        }
    }

    /**
     * Getter for property query.
     * @return Value of property query.
     */
    public java.lang.String getQuery() {
        StringBuilder sb = new StringBuilder();
        if (select.trim().toUpperCase().startsWith("SELECT")) {
            sb.append(select);
        } else {
            sb.append(lowerCaseKeywords ? "select " : "SELECT ").append(select);
        }
        if (!from.trim().equals("")) {
            if (!from.toUpperCase().startsWith("FROM")) {
                sb.append(lowerCaseKeywords ? " \nfrom " : " \nFROM ").append(from.trim());
            } else {
                sb.append(SPACE).append(from.trim());
            }
        }
        if (!joins.trim().equals("")) {
            sb.append(SPACE).append(joins.trim());
        }
        if (!where.equals("")) {
            if (!where.trim().toUpperCase().startsWith("WHERE")) {
                sb.append(lowerCaseKeywords ? " \nwhere " : " \nWHERE ").append(where.trim());
            } else {
                sb.append(SPACE).append(where.trim());
            }
        }

        if (!dynamicWhere.trim().equals("")) {
            if (where != null && !where.trim().equals("")) {
                sb.append(lowerCaseKeywords ? " and\n (" : " AND\n (").append(dynamicWhere.trim());
            } else {
                sb.append(lowerCaseKeywords ? " \nwhere (" :  " \nWHERE (").append(dynamicWhere.trim());
            }
            sb.append(")");
        }

        if (!groupBy.equals("")) {
            if (!groupBy.trim().toUpperCase().startsWith("GROUP ")) {
                sb.append(lowerCaseKeywords ? " \ngroup by " : " \nGROUP BY ").append(groupBy.trim());
            } else {
                sb.append(SPACE).append(groupBy.trim());
            }
        }
        if (!having.equals("")) {
            if (!having.trim().toUpperCase().startsWith("HAVING")) {
                sb.append(lowerCaseKeywords ? " \nhaving " : " \nHAVING ").append(having.trim());
            } else {
                sb.append(SPACE).append(having.trim());
            }
        }
        if (!orderBy.equals("")) {
            if (!orderBy.trim().toUpperCase().startsWith("ORDER ")) {
                sb.append(lowerCaseKeywords ? " \norder by " : " \nORDER BY ").append(orderBy.trim());
            } else {
                sb.append(SPACE).append(orderBy.trim());
            }
        }
        query = sb.toString();
        //reset();
        return query;
    }

    /**
     * Setter for property query.
     * @param query New value of property query.
     */
    public void setQuery(java.lang.String query) {
        this.query = query;
    }

    /**
     * Getter for property likeHints.
     * @return Value of property likeHint.
     */
    public java.util.Map<String, SearchCriteriaHolder.LIKE_HINTS> getLikeHints() {
        return likeHints;
    }

    public SearchCriteriaHolder.LIKE_HINTS getLikeHint(String operatorName) {
        return likeHints.get(operatorName);
    }

    /**
     * Setter for property likeHint.
     * @param likeHint New value of property likeHint.
     */
    public void setLikeHint(String operatorName, SearchCriteriaHolder.LIKE_HINTS likeHint) {
        this.likeHints.put(operatorName, likeHint);
    }

    public void setLikeHints(java.util.Map<String, SearchCriteriaHolder.LIKE_HINTS> likeHints) {
        this.likeHints = likeHints;
    }

    /**
     * Getter for property dynamicWhere.
     * @return Value of property dynamicWhere.
     */
    public java.lang.String getDynamicWhere() {
        return dynamicWhere;
    }

    /**
     * Setter for property dynamicWhere.
     * @param dynamicWhere New value of property dynamicWhere.
     */
    public void setDynamicWhere(java.lang.String dynamicWhere) {
        if (where != null) {
            this.dynamicWhere = dynamicWhere.trim();
        }
    }

    protected String replace(String functions, String columnName) {
        int start = functions.indexOf("${");
        int end = functions.indexOf("}", start);
        String result = functions.substring(0, start);
        result = result + columnName + functions.substring(end + 1);
        return result;
    }

    public String getExecutable() {
        return executable;
    }

    public void setExecutable(String executable) {
        this.executable = executable;
    }
    
    public boolean isLowerCaseKeywords() {
        return lowerCaseKeywords;
    }

    public void setLowerCaseKeywords(boolean lowerCaseKeywords) {
        this.lowerCaseKeywords = lowerCaseKeywords;
    }

    @Override
    public String toString() {
        Object[] EMPTY_READ_ARG = new Object[0];
        StringBuilder sb = new StringBuilder();
        Object value;
        Method[] methods = getClass().getMethods();
        for (int i = 0; i < methods.length; i++) {
            String methodName = methods[i].getName();
            if (methodName.equals("") || methodName.equals("getClass")) {
                continue;
            }
            if ((methodName.startsWith("get") || methodName.startsWith("is")) && methods[i].getParameterTypes().length == 0) {
                try {
                    value = methods[i].invoke(this, EMPTY_READ_ARG);
                } catch (Throwable t) {
                    continue;
                }
                String fieldFirstChar = "";
                if (methodName.startsWith("is")) {
                    fieldFirstChar = methodName.substring(2, 3).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(3));
                } else if (methodName.startsWith("get")) {
                    fieldFirstChar = methodName.substring(3, 4).toLowerCase();
                    sb.append(fieldFirstChar);
                    sb.append(methodName.substring(4));
                }
                sb.append("=");
                sb.append((value == null) ? "" : value);
                sb.append("&");
            }
        }
        return sb.toString();
    }

}

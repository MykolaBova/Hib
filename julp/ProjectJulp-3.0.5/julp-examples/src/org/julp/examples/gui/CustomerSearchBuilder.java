package org.julp.examples.gui;

import java.util.*;
import org.julp.DataAccessException;
import org.julp.ValueObject;
import org.julp.db.DBMetaData;
import org.julp.examples.Customer;
import org.julp.search.SearchCriteriaHolder;
import org.julp.search.XPathSearchCriteriaBuilder;

public class CustomerSearchBuilder extends XPathSearchCriteriaBuilder {

    public CustomerSearchBuilder() {
        if (this.fields == null) {
            fields = new ArrayList();
        }
        Properties props = loadMappings("/org/julp/examples/CustomerInvoice.properties");
        this.metaData = new DBMetaData();
        try {
            this.metaData.populate(props, Customer.class);
            Iterator iter = props.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                String fieldName = entry.getValue().toString();
                int idx = metaData.getFieldIndexByFieldName(fieldName);
                String label = metaData.getColumnLabel(idx);
                ValueObject value = new ValueObject(fieldName, label);
                fields.add(value);
            }
            Collections.<ValueObject>sort(fields);
            //fields.add(new ValueObject("item", "Item"));
        } catch (Exception sqle) {
            sqle.printStackTrace();
            throw new DataAccessException(sqle);
        }

        this.setLikeHint("Starts with", SearchCriteriaHolder.LIKE_HINTS.STARTS_WITH);
        this.setLikeHint("Contains", SearchCriteriaHolder.LIKE_HINTS.CONTAINS);

        List<ValueObject> operatorsList1 = new ArrayList<ValueObject>(3);        
        operatorsList1.add(new ValueObject(SearchCriteriaHolder.EQUAL, "Equal"));
        operatorsList1.add(new ValueObject(SearchCriteriaHolder.NOT_EQUAL, "Not Equal"));
        operatorsList1.add(new ValueObject(SearchCriteriaHolder.LIKE, "Contains"));
        operatorsList1.add(new ValueObject(SearchCriteriaHolder.LIKE, "Starts with"));

        List<ValueObject> operatorsList2 = new ArrayList<ValueObject>(4);
        operatorsList2.add(new ValueObject(SearchCriteriaHolder.EQUAL, "Equal"));
        operatorsList2.add(new ValueObject(SearchCriteriaHolder.NOT_EQUAL, "Not Equal"));
        operatorsList2.add(new ValueObject(SearchCriteriaHolder.GREATER_OR_EQUAL, "Greater then or Equal"));
        operatorsList2.add(new ValueObject(SearchCriteriaHolder.LESS_OR_EQUAL, "Less or Equal"));

        List<ValueObject> operatorsList3 = new ArrayList<ValueObject>(2);
        operatorsList3.add(new ValueObject(SearchCriteriaHolder.EQUAL, "Equal"));
        operatorsList3.add(new ValueObject(SearchCriteriaHolder.NOT_EQUAL, "Not Equal"));

        if (this.fieldOperators == null) {
            fieldOperators = new HashMap<Object, List<?>>();
        }
        Collections.sort(fields);
        Iterator<ValueObject> fieldsIterator = fields.iterator();
        while (fieldsIterator.hasNext()) {
            ValueObject field = fieldsIterator.next();
            String value = (String) field.getValue();
            if (value.equals("customerId")) {
                fieldOperators.put(field, operatorsList3);
            } else if (value.equals("city")) {
                fieldOperators.put(field, operatorsList3);
            } else if (value.equals("item")) {
                fieldOperators.put(field, operatorsList2);
            } else {
                fieldOperators.put(field, operatorsList1);
            }
        }
        List cities = getCities();

        if (this.fieldValues == null) {
            fieldValues = new HashMap();
        }

        fieldValues.put(new ValueObject("city", "City"), cities);
    }

    protected Properties loadMappings(String path) {
        java.io.InputStream inStream = null;
        Properties props = new Properties();
        try {
            inStream = this.getClass().getResourceAsStream(path);
            props.load(inStream);
        } catch (java.io.IOException ioe) {
            throw new DataAccessException(ioe);
        } finally {
            try {
                inStream.close();
            } catch (java.io.IOException ioe) {
                throw new DataAccessException(ioe);
            }
        }
        return props;
    }

    protected List getCities() {
        List cities = new ArrayList();
        cities.add("Albany");
        cities.add("Annapolis");
        cities.add("Atlanta");
        cities.add("Augusta");
        cities.add("Austin");
        cities.add("Baton Rouge");
        cities.add("Berne");
        cities.add("Bismarck");
        cities.add("Boise");
        cities.add("Boston");
        cities.add("Carson City");
        cities.add("Charleston");
        cities.add("Charlottetown");
        cities.add("Cheyenne");
        cities.add("Chicago");
        cities.add("Columbia");
        cities.add("Columbus");
        cities.add("Concord");
        cities.add("Dallas");
        cities.add("Denver");
        cities.add("Des Moines");
        cities.add("Dover");
        cities.add("Edmonton");
        cities.add("Frankfort");
        cities.add("Fredericton");
        cities.add("Halifax");
        cities.add("Harrisburg");
        cities.add("Hartford");
        cities.add("Helena");
        cities.add("Honolulu");
        cities.add("Indianapolis");
        cities.add("Jackson");
        cities.add("Jefferson City");
        cities.add("Juneau");
        cities.add("Lansing");
        cities.add("Lincoln");
        cities.add("Little Rock");
        cities.add("Lyon");
        cities.add("Madison");
        cities.add("Montgomery");
        cities.add("Montpelier");
        cities.add("Nashville");
        cities.add("New York");
        cities.add("Oklahoma City");
        cities.add("Olten");
        cities.add("Olympia");
        cities.add("Oslo");
        cities.add("Palo Alto");
        cities.add("Paris");
        cities.add("Phoenix");
        cities.add("Pierre");
        cities.add("Providence");
        cities.add("Quebec");
        cities.add("Raleigh");
        cities.add("Regina");
        cities.add("Richmond");
        cities.add("Sacramento");
        cities.add("Salem");
        cities.add("Salt Lake City");
        cities.add("San Francisco");
        cities.add("Santa Fe");
        cities.add("Seattle");
        cities.add("Springfield");
        cities.add("St. John's");
        cities.add("St. Paul");
        cities.add("Tallahassee");
        cities.add("Topeka");
        cities.add("Toronto");
        cities.add("Trenton");
        cities.add("Victoria");
        cities.add("Washington");
        cities.add("Whitehorse");
        cities.add("Winnipeg");
        cities.add("Yellowknife");
        return cities;
    }

    @Override
    public void beforeBuildCriteria() {

        ListIterator lit = searchCriteriaHolders.listIterator();
        boolean itemSearch = false;
        while (lit.hasNext()) {
            SearchCriteriaHolder holder = (SearchCriteriaHolder) lit.next();
            String fieldName = holder.getFieldName();
            if (fieldName == null || fieldName.trim().length() == 0) {
                throw new IllegalArgumentException("Missing Field Name");
            }
            if (fieldName.equals("item")) {
                itemSearch = true;
            }
            String columnName = null;
            try {
                columnName = this.metaData.getFullColumnName(this.metaData.getFieldIndexByFieldName(fieldName));
                adhocColumns.add(columnName);
            } catch (Exception sqle) {
                throw new DataAccessException(sqle);
            }

            String operator = holder.getOperator();
            String searchValue = "";
            Object value = holder.getSearchValue();

            if (value != null) {
                searchValue = value.toString();
                if (fieldName.equals("birthdate")) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy");
                        java.sql.Date d = new java.sql.Date(sdf.parse(searchValue).getTime());
                        holder.setSearchValue(d);
                    } catch (java.text.ParseException e) {
                        throw new IllegalArgumentException("Invalid Date");
                    }
                }

                if (fieldName.equals("createdTs") || fieldName.equals("modifiedTs")) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
                        java.sql.Timestamp ts = new java.sql.Timestamp(sdf.parse(searchValue + ".0").getTime());
                        holder.setSearchValue(ts);
                    } catch (java.text.ParseException e) {
                        throw new IllegalArgumentException("Invalid DateTime");
                    }
                }
            }

            if (operator.equalsIgnoreCase("LIKE")) {
                if (searchValue == null || searchValue.trim().equals("")) {
                    throw new IllegalArgumentException("Invalid/missing search value");
                }
                holder.setFunctions("UPPER(${})");
                if (holder.getLikeHint() == SearchCriteriaHolder.LIKE_HINTS.STARTS_WITH) {
                    searchValue = searchValue.toUpperCase() + "%";
                } else if (holder.getLikeHint() == SearchCriteriaHolder.LIKE_HINTS.CONTAINS) {
                    searchValue = "%" + searchValue.toUpperCase() + "%";
                } else if (holder.getLikeHint() == SearchCriteriaHolder.LIKE_HINTS.ENDS_WITH) {
                    searchValue = "%" + searchValue.toUpperCase();
                }
                holder.setSearchValue(searchValue);
            }

            /*
            if (operator.indexOf("LIKE") > -1){
            if (searchValue.trim().equals("")){
            throw new IllegalArgumentException("Invalid search value");
            }
            if (functionName != null && functionName.equalsIgnoreCase("lower")){
            searchValue = "%" + searchValue.toLowerCase() + "%";
            }else if (functionName != null && functionName.equalsIgnoreCase("upper")){
            searchValue = "%" + searchValue.toUpperCase() + "%";
            }
            holder.setSearchValue(searchValue);
            }
             */
        }

        // does not make a lot of sense, just an example
        if (itemSearch) {
            SearchCriteriaHolder sch = new SearchCriteriaHolder();
            sch.setFieldName("dummy");
            sch.setOverrideColumnName("");
            sch.setOverrideOperator(true);
            sch.setOperator("");
            sch.setLiteralParameter(true);           
            sch.setSearchValue(" exists (select null from PRODUCT where PRICE > 20)");
            searchCriteriaHolders.add(sch);
        }
    }
}

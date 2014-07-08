package org.julp.search;

import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.*;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.julp.DataAccessException;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

public class XPathSQLQueryReader implements Serializable {

    private static final long serialVersionUID = 372128384021907505L;
    protected String select;
    protected String from;
    protected String where;
    protected String joins;
    protected String groupBy;
    protected String having;
    protected String orderBy;
    protected String query;
    protected String queryId = "";
    protected String queryFilePath;
    protected String executable;
    protected String queryURI;
    protected Reader reader;
    protected Document doc;
    protected boolean reloadDocument = false;
    protected boolean distinct = false;
    protected boolean ansiJoin = false;
    private boolean lowerCaseKeywords = true;
    // if user selected column for adhoc than use this to add tables join(s)
    protected Map<String, String> columnConditionMappings;
    // if user selected column for adhoc than use this to add tables join(s) (non-ansi joins)
    protected Map<String, String> columnTablesMapping;
    // list of column for adhoc
    protected Set<String> adhocColumns;
    protected Element ansiJoinElement;
    protected Element selectElement;
    protected Element fromElement;
    protected Element joinsElement;
    protected Element whereElement;
    protected Element groupByElement;
    protected Element havingElement;
    protected Element orderByElement;
    protected Element executableElement;    
    private static final transient Logger logger = Logger.getLogger(XPathSQLQueryReader.class.getName());

    public XPathSQLQueryReader() {
    }

    public void reset() {
        if (ansiJoinElement != null) {
            ansiJoin = true;
        } else {
            ansiJoin = false;
        }
        String attrib = selectElement.getAttribute("distinct");
        if (attrib != null && attrib.equals("true")) {
            distinct = true;
        } else {
            distinct = false;
        }

        select = selectElement.getNodeValue();
        from = fromElement.getNodeValue();
        joins = joinsElement.getNodeValue();
        where = whereElement.getNodeValue();
        groupBy = groupByElement.getNodeValue();
        having = havingElement.getNodeValue();
        orderBy = orderByElement.getNodeValue();

        if (columnTablesMapping != null) {
            columnTablesMapping.clear();
        }
        if (columnConditionMappings != null) {
            columnConditionMappings.clear();
        }
        adhocColumns = null;
    }

    public void resetAll() {
        from = null;
        where = null;
        joins = null;
        groupBy = null;
        having = null;
        orderBy = null;
        queryId = "";
        distinct = false;
        ansiJoin = false;
        if (columnConditionMappings != null) {
            columnConditionMappings.clear();
        }
        if (columnTablesMapping != null) {
            columnTablesMapping.clear();
        }
        adhocColumns = null;
        executable = null;
    }

    public void loadExecutable(String queryId) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::loadExecutable()::queryId: " + queryId);
        }
        if (!this.queryId.equals(queryId)) {
            //reloadDocument = true;
            resetAll();
        }
        if (reloadDocument) {
            resetAll();
        }
        if (this.queryId.equals(queryId)) {
            return;
        }

        this.queryId = queryId;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);

            // Create the builder and parse the file
            if (doc == null || reloadDocument) {
                if (queryFilePath != null) {
                    doc = factory.newDocumentBuilder().parse(new File(queryFilePath));
                } else if (queryURI != null) {
                    doc = factory.newDocumentBuilder().parse(queryURI);
                } else if (reader != null) {
                    doc = factory.newDocumentBuilder().parse(new InputSource(reader));
                } else {
                    throw new IOException("Queries source is missing");
                }
            }
            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            executableElement = (Element) xpath.evaluate("//executable[@id='" + queryId + "']", doc, XPathConstants.NODE);
            executable = executableElement.getTextContent();
        } catch (IOException ioe) {
            throw new DataAccessException(ioe);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    public void loadExecutable(String queryId, boolean reloadDocument) {
        this.reloadDocument = reloadDocument;
        this.loadExecutable(queryId);
    }

    public void loadQuery(String queryId, boolean reloadDocument) {
        this.reloadDocument = reloadDocument;
        this.loadQuery(queryId);
    }

    public void loadQuery(String queryId) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::loadQuery()::queryId: " + queryId);
        }
        if (!this.queryId.equals(queryId)) {
            //reloadDocument = true;
            resetAll();
        }
        if (reloadDocument) {
            resetAll();
        }
        if (this.queryId.equals(queryId)) {
            //reset();
            return;
        }

        this.queryId = queryId;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(true);

            // Create the builder and parse the file          
            if (doc == null || reloadDocument) {
                if (queryFilePath != null) {
                    doc = factory.newDocumentBuilder().parse(new File(queryFilePath));
                } else if (queryURI != null) {
                    doc = factory.newDocumentBuilder().parse(queryURI);
                } else if (reader != null) {
                    doc = factory.newDocumentBuilder().parse(new InputSource(reader));
                } else {
                    throw new IOException("Queries source is missing");
                }
            }

            XPathFactory xpathFactory = XPathFactory.newInstance();
            XPath xpath = xpathFactory.newXPath();

            Element queryElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']", doc, XPathConstants.NODE);
            if (queryElement == null) {
                throw new IllegalArgumentException("Query Id: \"" + queryId + "\" does not exist");
            }

            ansiJoinElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/ansi_join", doc, XPathConstants.NODE);
            if (ansiJoinElement != null) {
                ansiJoin = true;
            } else {
                ansiJoin = false;
            }
            selectElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/select", doc, XPathConstants.NODE);
            String attrib = selectElement.getAttribute("distinct");
            if (attrib != null && attrib.equals("true")) {
                distinct = true;
            } else {
                distinct = false;
            }
            select = selectElement.getTextContent();

            fromElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/from", doc, XPathConstants.NODE);
            if (fromElement != null) {
                from = fromElement.getTextContent();
            }

            if (isAnsiJoin()) {
                joinsElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/joins", doc, XPathConstants.NODE);
                joins = joinsElement.getTextContent();
            }

            whereElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/where", doc, XPathConstants.NODE);
            if (whereElement != null) {
                where = whereElement.getTextContent();
            }

            groupByElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/group_by", doc, XPathConstants.NODE);
            if (groupByElement != null) {
                groupBy = groupByElement.getTextContent();
            }

            havingElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/having", doc, XPathConstants.NODE);
            if (havingElement != null) {
                having = havingElement.getTextContent();
            }

            orderByElement = (Element) xpath.evaluate("//query[@id='" + queryId + "']/order_by", doc, XPathConstants.NODE);
            if (orderByElement != null) {
                orderBy = orderByElement.getTextContent();
            }

            NodeList adhocMappings = (NodeList) xpath.evaluate("//query[@id='" + queryId + "']/adhoc_mappings/*", doc, XPathConstants.NODESET);

            //for(int i = 0; i < adhocMappings.getLength(); i++)
            //{
            //   System.out.println("adhocMappings: " + adhocMappings.item(i).getTextContent());
            //}

            if (adhocMappings != null && adhocMappings.getLength() > 0) {
                if (columnConditionMappings == null) {
                    columnConditionMappings = new HashMap<>();
                }
                if (columnTablesMapping == null) {
                    columnTablesMapping = new HashMap<>();
                }
                for (int i = 0; i < adhocMappings.getLength(); i++) {
                    Element adhocElement = (Element) adhocMappings.item(i);
                    if (adhocElement != null) {
                        //System.out.println("adhocElement: " + adhocElement.getTextContent());
                        NodeList conditionNodes = adhocElement.getElementsByTagName("conditions");
                        if (conditionNodes != null) {
                            Element conditionsElement = (Element) conditionNodes.item(0);
                            //System.out.println("conditionsElement: " + conditionsElement.getTextContent());
                            String conditions = conditionsElement.getTextContent();
                            NodeList columnsNodes = adhocElement.getElementsByTagName("columns");
                            if (columnsNodes != null) {
                                Element columnsElement = (Element) columnsNodes.item(0);
                                //System.out.println("columnsElement: " + conditionsElement.getTextContent());
                                if (columnsElement != null) {
                                    NodeList columns = columnsElement.getElementsByTagName("column");
                                    //System.out.println("columns: " + columns);
                                    for (int j = 0; j < columns.getLength(); j++) {
                                        //System.out.println("columns.item(i): " + columns.item(j));
                                        String column = columns.item(j).getTextContent();
                                        //System.out.println("column: " + column);
                                        if (column.trim().length() == 0) {
                                            continue;
                                        }
                                        columnConditionMappings.put(column, conditions);
                                        if (!isAnsiJoin()) {
                                            Element adhocTablesElement = (Element) adhocElement.getElementsByTagName("tables").item(0);
                                            if (adhocTablesElement != null) {
                                                String tables = adhocTablesElement.getTextContent();
                                                columnTablesMapping.put(column, tables);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                //System.out.println("columnConditionMappings: " + columnConditionMappings);                
            }
        } catch (Exception e) {            
            throw new DataAccessException(e);
        }
    }

    public String getQueryURI() {
        return queryURI;
    }

    public void setQueryURI(String queryURI) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::setQueryURI()::queryURI: " + queryURI);
        }
        if (this.queryURI == null || !this.queryURI.equals(queryURI.toString())) {
            setReloadDocument(true);
        }
        this.queryFilePath = null;
        this.reader = null;
        this.queryURI = queryURI;
    }

    public java.lang.String getQueryFilePath() {
        return queryFilePath;
    }

    public void setQueryFilePath(java.lang.String queryFilePath) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::setQueryFilePath()::queryFilePath: " + queryFilePath);
        }
        if (this.queryFilePath == null || !this.queryFilePath.equals(queryFilePath)) {
            setReloadDocument(true);
        }
        this.queryURI = null;
        this.reader = null;
        this.queryFilePath = queryFilePath;
    }

    public Reader getReader() {
        return reader;
    }

    public void setReader(Reader reader) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::setReader()::reader: " + reader);
        }
        if (this.reader == null || !this.reader.equals(reader)) {
            setReloadDocument(true);
        }
        this.queryURI = null;
        this.queryFilePath = null;
        this.reader = reader;
    }

    public String getGroupBy() {
        return (groupBy == null ? "" : groupBy);
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }

    public String getOrderBy() {
        return (orderBy == null ? "" : orderBy);
    }

    public void setOrderBy(String orderBy) {
        this.orderBy = orderBy;
    }

    public String getWhere() {
        if (adhocColumns != null) {
            if (!isAnsiJoin()) {
                // add "dynamic" WHERE condition(s) to "static", remove dups
                Set<String> columnConditions = new HashSet<>();
                Iterator<String> adhocColumnsIter = adhocColumns.iterator();
                while (adhocColumnsIter.hasNext()) {
                    String column = (String) adhocColumnsIter.next();
                    String conditions = getColumnConditionMappings(column);
                    if (conditions != null && !conditions.trim().equals("")) {
                        columnConditions.add(conditions.trim());
                    }
                }
                StringBuilder sb3 = new StringBuilder();
                Iterator<String> columnConditionsIter = columnConditions.iterator();
                while (columnConditionsIter.hasNext()) {
                    String value = (String) columnConditionsIter.next();
                    if (value != null && !value.trim().equals("")) {
                        sb3.append(value).append(lowerCaseKeywords ? " and\n " : " AND\n ");
                    }
                }
                if (sb3.length() > 0) {
                    sb3.setLength(sb3.length() - 5);
                }
                if (where == null || where.trim().equals("")) {
                    if (sb3.length() > 0) {
                        where = sb3.toString().trim();
                    }
                } else {
                    if (sb3.length() > 0) {
                        where = where.trim() + (lowerCaseKeywords ? " and\n " : " AND\n ") + sb3.toString().trim();
                    } else {
                        //where.trim();
                    }
                }
            }
        }
        return (where == null ? "" : where.trim());
    }

    public void setWhere(String where) {
        this.where = where;
    }

    public String getHaving() {
        return (having == null ? "" : having);
    }

    public void setHaving(String having) {
        this.having = having;
    }

    public String getFrom() {
        if (adhocColumns != null) {
            // add "dynamic" FROM condition(s) to "static", remove dups
            StringBuilder sb1 = new StringBuilder();
            if (from != null) {
                sb1.append(from);
            }
            Iterator<String> adhocColumnsIter = adhocColumns.iterator();
            while (adhocColumnsIter.hasNext()) {
                Object adhocTables = getColumnTablesMapping(adhocColumnsIter.next());
                if (adhocTables != null) {
                    sb1.append(", ").append(adhocTables);
                }
            }
            StringTokenizer st = new StringTokenizer(sb1.toString(), ",", false);
            Set<String> tableNames = new HashSet<>();
            while (st.hasMoreTokens()) {
                String tableName = st.nextToken();
                tableNames.add(tableName.trim());
            }
            StringBuilder sb2 = new StringBuilder();
            Iterator<String> tablesNameIter = tableNames.iterator();
            while (tablesNameIter.hasNext()) {
                sb2.append(tablesNameIter.next()).append(", ");
            }
            int idx = sb2.lastIndexOf(",");
            if (idx > -1) {
                sb2.deleteCharAt(idx);
                from = sb2.toString();
            }
        }
        return (from == null ? "" : from);
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public boolean isReloadDocument() {
        return reloadDocument;
    }

    public void setReloadDocument(boolean reloadDocument) {
        this.reloadDocument = reloadDocument;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    public String getJoins() {
        if (adhocColumns != null) {
            if (isAnsiJoin()) {
                Set<String> columnConditions = new HashSet<>();
                Iterator<String> adhocColumnsIter = adhocColumns.iterator();
                while (adhocColumnsIter.hasNext()) {
                    String column = adhocColumnsIter.next();
                    String conditions = getColumnConditionMappings(column);
                    if (conditions != null && !conditions.trim().equals("")) {
                        columnConditions.add(conditions.trim());
                    }
                }
                StringBuilder sb3 = new StringBuilder();
                Iterator<String> columnConditionsIter = columnConditions.iterator();
                while (columnConditionsIter.hasNext()) {
                    String value = columnConditionsIter.next();
                    if (value != null && !value.trim().equals("")) {
                        sb3.append(value.trim()).append(lowerCaseKeywords ? " and\n " : " AND\n ");
                    }
                }
                if (sb3.length() > 0) {
                    sb3.setLength(sb3.length() - 5);
                }
                if (joins == null || joins.trim().equals("")) {
                    joins = sb3.toString();
                } else {
                    joins = joins.trim() + " " + sb3.toString();
                }
            }
        }
        return (joins == null ? "" : joins);
    }

    public void setJoins(String joins) {
        this.joins = joins;
    }

    public void setQuery(java.lang.String query) {
        this.query = query;
    }

    public String getQuery() {
        return this.query;
    }

    /**
     * Getter for property select.
     * @return Value of property select.
     */
    public java.lang.String getSelect() {
        if (isDistinct()) {
            return (lowerCaseKeywords ? " distinct " : " DISTINCT ") + select;
        }
        return select;
    }

    /**
     * Setter for property select.
     * @param select New value of property select.
     */
    public void setSelect(java.lang.String select) {
        this.select = select;
    }

    /**
     * Getter for property ansiJoin.
     * @return Value of property ansiJoin.
     */
    public boolean isAnsiJoin() {
        return ansiJoin;
    }

    /**
     * Setter for property ansiJoin.
     * @param ansiJoin New value of property ansiJoin.
     */
    public void setAnsiJoin(boolean ansiJoin) {
        this.ansiJoin = ansiJoin;
    }

    /**
     * Getter for property columnConditionMappings.
     * @return Value of property columnConditionMappings.
     */
    public java.util.Map<String, String> getColumnConditionMappings() {
        return columnConditionMappings;
    }

    public String getColumnConditionMappings(String columnName) {
        if (columnConditionMappings == null) {
            return null;
        }
        return (columnConditionMappings.containsKey(columnName) ? (String) columnConditionMappings.get(columnName) : "");
    }

    /**
     * Setter for property columnConditionMappings.
     * @param columnConditionMappings New value of property columnConditionMappings.
     */
    public void setColumnConditionMappings(java.util.Map<String, String> columnConditionMappings) {
        this.columnConditionMappings = columnConditionMappings;
    }

    public void setColumnConditionMappings(String columnName, String conditions) {
        this.columnConditionMappings.put(columnName, conditions);
    }

    /**
     * Getter for property columnTablesMapping.
     * @return Value of property columnTablesMapping.
     */
    public java.util.Map<String, String> getColumnTablesMapping() {
        return columnTablesMapping;
    }

    public String getColumnTablesMapping(String columnName) {
        if (columnTablesMapping == null) {
            return null;
        }
        return (String) columnTablesMapping.get(columnName);
    }

    /**
     * Setter for property columnTablesMapping.
     * @param columnTablesMapping New value of property columnTablesMapping.
     */
    public void setColumnTablesMapping(java.util.Map<String, String> columnTablesMapping) {
        this.columnTablesMapping = columnTablesMapping;
    }

    public void setColumnTablesMapping(String columnName, String tables) {
        this.columnTablesMapping.put(columnName, tables);
    }

    /**
     * Getter for property adhocColumns.
     * @return Value of property adhocColumns.
     */
    public java.util.Set<String> getAdhocColumns() {
        return adhocColumns;
    }

    /**
     * Setter for property adhocColumns.
     * @param adhocColumns New value of property adhocColumns.
     */
    public void setAdhocColumns(java.util.Set<String> adhocColumns) {
        this.adhocColumns = adhocColumns;
    }

    public Element getExecutableElement() {
        return executableElement;
    }

    public void setExecutableElement(Element executableElement) {
        this.executableElement = executableElement;
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
        final Object[] EMPTY_ARG = new Object[0];
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
                    value = methods[i].invoke(this, EMPTY_ARG);
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

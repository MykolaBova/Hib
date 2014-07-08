package org.julp.db;

import java.lang.reflect.Method;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import org.julp.AbstractMetaData;
import org.julp.DataAccessException;

/**
 * Populates and caches columns, tables, fields, read/write methods, etc... Not all properties are used/populated. Add support for properties as needed.
 */
public class DBMetaData<T> extends AbstractMetaData<T> {

    private static final long serialVersionUID = -4786749470828742028L;
    private static final String DOT = ".";
    protected int[] columnDisplaySize = null;
    protected int[] columnType = null;
    protected String[] catalogName = null;
    protected String[] schemaName = null;
    protected String[] tableName = null;
    protected String[] columnClassName = null;
    protected String[] columnLabel = null;
    protected String[] columnName = null;
    protected String[] columnTypeName = null;
    protected String[] fullColumnName = null;
    protected boolean[] autoIncrement = null;
    protected Map<String, String[]> tables = new HashMap<>();

    /**
     * Gets the designated column's table's catalog name.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the name of the catalog for the table in which the given column appears or "" if not applicable
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getCatalogName(int columnIndex) throws DataAccessException {
        return this.catalogName[columnIndex - 1];
    }

    /**
     * <p>Returns the fully-qualified name of the Java class whose instances are manufactured if the method
     * <code>ResultSet.getObject</code> is called to retrieve a value from the column.
     * <code>ResultSet.getObject</code> may return a subclass of the class returned by this method.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the fully-qualified name of the class in the Java programming language that would be used by the method <code>ResultSet.getObject</code> to retrieve the value in the specified column.
     * This is the class name used for custom mapping.
     * @exception DataAccessException if a database access error occurs
     * @since 1.2
     *
     */
    public String getColumnClassName(int columnIndex) throws DataAccessException {
        return this.columnClassName[columnIndex - 1];
    }

    /**
     * Indicates the designated column's normal maximum width in characters.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the normal maximum number of characters allowed as the width of the designated column
     * @exception DataAccessException if a database access error occurs
     *
     */
    public int getColumnDisplaySize(int columnIndex) throws DataAccessException {
        return this.columnDisplaySize[columnIndex - 1];
    }

    /**
     * Gets the designated column's suggested title for use in printouts and displays.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return the suggested column title
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getColumnLabel(int columnIndex) throws DataAccessException {
        return this.columnLabel[columnIndex - 1];
    }

    /**
     * Get the designated column's name.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return column name
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getColumnName(int columnIndex) throws DataAccessException {
        return this.columnName[columnIndex - 1];
    }

    /**
     * Retrieves the designated column's SQL type.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return SQL type from Types
     * @exception DataAccessException if a database access error occurs
     * @see Types
     *
     */
    public int getColumnType(int columnIndex) throws DataAccessException {
        return this.columnType[columnIndex - 1];
    }

    /**
     * Retrieves the designated column's database-specific type name.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return type name used by the database. If the column type is a user-defined type, then a fully-qualified type name is returned.
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getColumnTypeName(int columnIndex) throws DataAccessException {
        return this.columnTypeName[columnIndex - 1];
    }

    /**
     * Get the designated column's table's schema.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return schema name or "" if not applicable
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getSchemaName(int columnIndex) throws DataAccessException {
        return this.schemaName[columnIndex - 1];
    }

    /**
     * Gets the designated column's table name.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return table name or "" if not applicable
     * @exception DataAccessException if a database access error occurs
     *
     */
    public String getTableName(int columnIndex) throws DataAccessException {
        return this.tableName[columnIndex - 1];
    }

    /**
     * Indicates whether the designated column is automatically numbered, thus read-only.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @return <code>true</code> if so; <code>false</code> otherwise
     * @exception DataAccessException if a database access error occurs
     *
     */
    public boolean isAutoIncrement(int columnIndex) throws DataAccessException {
        return this.autoIncrement[columnIndex - 1];
    }

    /**
     * Sets whether the designated column is automatically numbered, and thus read-only. The default is for a
     * <code>RowSet</code> object's columns not to be automatically numbered.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param property <code>true</code> if the column is automatically numbered; <code>false</code> if it is not
     *
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setAutoIncrement(int columnIndex, boolean property) throws DataAccessException {
        this.autoIncrement[columnIndex - 1] = property;
    }

    /**
     * Sets the designated column's table's catalog name, if any, to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param catalogName the column's catalog name
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setCatalogName(int columnIndex, String catalogName) throws DataAccessException {
        this.catalogName[columnIndex - 1] = catalogName;
    }

    /**
     * Sets the designated column's normal maximum width in chars to the given
     * <code>int</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param size the normal maximum number of characters for the designated column
     *
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setColumnDisplaySize(int columnIndex, int size) throws DataAccessException {
        this.columnDisplaySize[columnIndex - 1] = size;
    }

    /**
     * Sets the suggested column title for use in printouts and displays, if any, to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param label the column title
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setColumnLabel(int columnIndex, String label) throws DataAccessException {
        this.columnLabel[columnIndex - 1] = label;
    }

    /**
     * Sets the name of the designated column to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param columnName the designated column's name
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setColumnName(int columnIndex, String columnName) throws DataAccessException {
        this.columnName[columnIndex - 1] = columnName;
    }

    /**
     * Sets the designated column's SQL type to the one given.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param SQLType the column's SQL type
     * @exception DataAccessException if a database access error occurs
     * @see Types
     *
     */
    public void setColumnType(int columnIndex, int SQLType) throws DataAccessException {
        this.columnType[columnIndex - 1] = SQLType;
    }

    /**
     * Sets the designated column's type name that is specific to the data source, if any, to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param typeName data source specific type name.
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setColumnTypeName(int columnIndex, String typeName) throws DataAccessException {
        this.columnTypeName[columnIndex - 1] = typeName;
    }

    /**
     * Sets the name of the designated column's table's schema, if any, to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param schemaName the schema name
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setSchemaName(int columnIndex, String schemaName) throws DataAccessException {
        this.schemaName[columnIndex - 1] = schemaName;
    }

    /**
     * Sets the designated column's table name, if any, to the given
     * <code>String</code>.
     *
     * @param columnIndex the first column is 1, the second is 2, ...
     * @param tableName the column's table name
     * @exception DataAccessException if a database access error occurs
     *
     */
    public void setTableName(int columnIndex, String tableName) throws DataAccessException {
        this.tableName[columnIndex - 1] = tableName;
    }

    public void setColumnClassName(int columnIndex, java.lang.String columnClassName) {
        this.columnClassName[columnIndex - 1] = columnClassName;
    }

    public java.util.Map<String, String[]> getTables() {
        return tables;
    }

    public void setTables(java.util.Map<String, String[]> tables) {
        this.tables = tables;
    }

    public String getFullColumnName(int columnIndex) throws DataAccessException {
        if (columnIndex < 1) {
            throw new DataAccessException("DBMetaData: missing/invalid mapping");
        }
        return this.fullColumnName[columnIndex - 1];
    }

    public void setFullColumnName(int columnIndex, String columnName) throws DataAccessException {
        this.fullColumnName[columnIndex - 1] = columnName;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void populate(Map mapping, Class domainClass) throws DataAccessException {
        if (mapping == null || mapping.isEmpty()) {
            throw new DataAccessException("DBMetaData: missing/invalid mapping");
        }
        this.domainClass = domainClass;
        this.fieldCount = mapping.size();
        this.readOnly = new boolean[fieldCount];
        this.fieldName = new String[fieldCount];
        this.fieldLabel = new String[fieldCount];
        this.readMethod = new Method[fieldCount];
        this.writeMethod = new Method[fieldCount];
        this.fieldClass = new Class[fieldCount];
        this.fieldClassName = new String[fieldCount];

        this.catalogName = new String[fieldCount];
        this.schemaName = new String[fieldCount];
        this.columnClassName =  new String[fieldCount]; 
        this.tableName = new String[fieldCount];
        this.columnName = new String[fieldCount];
        this.fullColumnName = new String[fieldCount];
        this.autoIncrement = new boolean[fieldCount];
        
        this.columnLabel = new String[fieldCount];
        this.columnDisplaySize = new int[fieldCount];
        this.columnTypeName =  new String[fieldCount]; 
        this.columnType = new int[fieldCount];
        this.writable = new boolean[fieldCount];

        Iterator<Map.Entry<String, String>> iter = mapping.entrySet().iterator();
        int columnIndex = 1;
        while (iter.hasNext()) {
            String catalog = null;
            String schema = null;
            String table = null;
            String tableId = null;
            String column = null;
            Map.Entry<String, String> entry = iter.next();
            String fullColName = entry.getKey();
            StringTokenizer st = new StringTokenizer(fullColName, DOT, false);
            int tokens = st.countTokens();
            if (tokens == 2) { // only table and column names, no schema and no catalog
                int idx1 = fullColName.indexOf(DOT);
                tableId = fullColName.substring(0, idx1).trim();
                table = tableId;
            } else if (tokens == 3) { // no catalog is supplied
                int idx1 = fullColName.indexOf(DOT);
                int idx2 = fullColName.lastIndexOf(DOT);
                tableId = fullColName.substring(0, idx2).trim();
                table = fullColName.substring(idx1 + 1, idx2).trim();
                schema = fullColName.substring(0, idx1).trim();
            } else if (tokens == 4) { // catalog, schema, table and column
                tableId = fullColName.substring(0, fullColName.lastIndexOf(DOT)).trim();
                int i = 0;
                while (st.hasMoreTokens() && i < 3) {
                    String token = st.nextToken().trim();
                    if (i == 0) {
                        catalog = token;
                    } else if (i == 1) {
                        schema = token;
                    } else if (i == 2) {
                        table = token;
                    }
                    i++;
                }
            } else {
                if (tokens == 1) {
                    throw new IllegalArgumentException("Invalid DB Mappings: missing table name or column: " + fullColName);
                }
                throw new IllegalArgumentException("Invalid DB Mappings");
            }
            column = fullColName.substring(fullColName.lastIndexOf(DOT) + 1);
            this.setCatalogName(columnIndex, catalog);
            this.setSchemaName(columnIndex, schema);
            this.setTableName(columnIndex, table);
            this.setColumnName(columnIndex, column);
            this.setFullColumnName(columnIndex, fullColName);
            String field = entry.getValue();
            //String field = (String) mapping.get(fullColName);
            this.setFieldName(columnIndex, field);
            String[] distinctTables = new String[3];
            distinctTables[0] = catalog;
            distinctTables[1] = schema;
            distinctTables[2] = table;
            this.tables.put(tableId, distinctTables);
            this.setReadOnly(columnIndex, false);
            //this.setWritable(columnIndex, true); //it's done already: see setReadOnly() method
            this.populateReadMethod(columnIndex, field);
            this.populateWriteMethod(columnIndex, field);
            this.setColumnLabel(columnIndex, toLabel(getColumnName(columnIndex)));
            columnIndex++;
        }
    }

    public int getColumnIndexByColumnName(String columnName) {
        if (columnName == null) {
            logger.warning("Parameter 'columnName' is null. Check mappings");
            return -1;
        }
        for (int i = 0; i < this.columnName.length; i++) {
            if (this.columnName[i] == null) {
                return -1;
            }
            if (columnName.equalsIgnoreCase(this.columnName[i])) {
                return i + 1;
            }
        }
        logger.warning("Column index for " + columnName + " is not found. Check mappings");
        return -1;
    }

    public int getColumnIndexByFullColumnName(String fullColumnNameParam) {
        if (fullColumnNameParam == null) {
            logger.warning("Parameter 'fullColumnNameParam' is null. Check mappings");
            return -1;
        }
        for (int i = 0; i < this.fullColumnName.length; i++) {
            if (fullColumnName[i] == null) {
                logger.warning("Column index for " + fullColumnNameParam + " is not found. Check mappings");
                return -1;
            }
            if (fullColumnNameParam.equalsIgnoreCase(fullColumnName[i])) {
                return i + 1;
            }
        }
        logger.warning("Column index for " + fullColumnNameParam + " is not found. Check mappings");
        return -1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("julp::org.julp.db.DBMetaData: ");
        sb.append("\nfieldCount: ").append(fieldCount);
        if (catalogName != null) {
            sb.append("\ncatalogName: ").append(Arrays.asList(catalogName));
        }
        sb.append("\nschemaName: ").append(Arrays.asList(schemaName));
        sb.append("\ntableName: ").append(Arrays.asList(tableName));
        //sb.append("\ncolumnClassName: " + Arrays.asList(columnClassName));
        sb.append("\ncolumnLabel: ").append(Arrays.asList(columnLabel));
        sb.append("\ncolumnName: ").append(Arrays.asList(columnName));
        sb.append("\nfullColumnName: ").append(Arrays.asList(fullColumnName));
        sb.append("\nfieldName: ").append(Arrays.asList(fieldName));
        sb.append("\nreadMethod: ").append(Arrays.asList(readMethod));
        sb.append("\nwriteMethod: ").append(Arrays.asList(writeMethod));
        //sb.append("\nwritable: " + Arrays.asList(writable));
        sb.append("\nfieldClassName: ").append(Arrays.asList(fieldClassName));
        sb.append("\ndomainClass: ").append(domainClass);
        return sb.toString();
    }
}

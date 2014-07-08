package org.julp.gui.generator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;

@SuppressWarnings("UseOfObsoleteCollectionType")
public class ResultSet2JavaObjectsGenerator {

    protected Vector info = new Vector();
    protected Map<String, Integer> dupsMap = new HashMap<>();
    protected Map nullable;
    protected String driverPath;
    
    protected static final String DOT = ".";
    protected Driver driver;
    protected String driverClassName = null;

    public ResultSet2JavaObjectsGenerator() {
    }

    public Vector getInfo() {
        return info;
    }

    public boolean testConnection(Properties props) throws ClassNotFoundException {
        boolean success = true;
        Connection con = null;
        try {
            if (driver == null || (driverClassName == null || !driverClassName.trim().equals(props.getProperty("driver")))) {
                driver = (Driver) Class.forName(props.getProperty("driver")).newInstance();
                DriverManager.registerDriver(new DriverHolder(driver));                
            }
            con = DriverManager.getConnection(props.getProperty("url"), props);
            driverClassName = props.getProperty("driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            success = false;
            throw new RuntimeException(e);
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return success;
    }

    protected boolean loadDriver(String driverJarPath, Properties props) {
        java.io.File file = new File(driverJarPath);
        if (file != null && file.exists()) {
            Connection con = null;
            try {
                URL url = file.toURI().toURL();
                //URL url = new URL("jar:file:/path/to/driver.jar!/");
                //URL url = new URL("jar:file:/" + file.getAbsolutePath() + "!");
                driverClassName = props.getProperty("driver");
                URLClassLoader ucl = new URLClassLoader(new URL[] {url});
                driver = (Driver) Class.forName(driverClassName, true, ucl).newInstance();
                DriverManager.registerDriver(new DriverHolder(driver));
                con = DriverManager.getConnection(props.getProperty("url"), props);
            } catch (Exception ex) {
                driverClassName = null;
                ex.printStackTrace();
                throw new RuntimeException(ex);
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                    con = null;
                } catch (java.sql.SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }
    
    protected String getColumnClassName(String driverClassName, ResultSetMetaData rsmd, int colIndex) {
        String colClassName = null;
        try {
            colClassName = rsmd.getColumnClassName(colIndex);            
            if (colClassName.equals("java.math.BigDecimal") && driverClassName.toLowerCase().contains("oracle")) {                
                int s = rsmd.getScale(colIndex);
                if (s == 0) {
                    int p = rsmd.getPrecision(colIndex);
                    if (p > 9) {
                        colClassName = "java.lang.Long";
                    } else if (p > 4 && p < 10) {
                        colClassName = "java.lang.Integer";
                    } else if (p > 1 && p < 5) {
                        colClassName = "java.lang.Short";   
                    } else if (p == 1) {
                        colClassName = "java.lang.Boolean";        
                    }                    
                }                                
            }
        } catch (Throwable t) { // some JDBC drivres don't support this
            int type = -1;
            try {
                type = rsmd.getColumnType(colIndex);
            } catch (SQLException e) {
                e.printStackTrace();
                 throw new RuntimeException(e);
            }
            if (type == Types.BIGINT) {
                colClassName = "java.lang.Long";
            } else if (type == Types.BOOLEAN) {
                colClassName = "java.lang.Boolean";
            } else if (type == Types.CHAR || type == Types.VARCHAR) {
                colClassName = "java.lang.String";
            } else if (type == Types.DATE) {
                colClassName = "java.sql.Date";
            } else if (type == Types.TIMESTAMP) {
                colClassName = "java.sql.Timestamp";
            } else if (type == Types.TIME) {
                colClassName = "java.sql.Time";
            } else if (type == Types.DECIMAL || type == Types.NUMERIC) {
                colClassName = "java.math.BigDecimal";
            } else if (type == Types.DOUBLE) {
                colClassName = "java.lang.Double";
            } else if (type == Types.FLOAT || type == Types.REAL) {
                colClassName = "java.lang.Float";
            } else if (type == Types.INTEGER) {
                colClassName = "java.lang.Integer";
            } else if (type == Types.SMALLINT) {
                colClassName = "java.lang.Short";
            } else if (type == Types.TINYINT) {
                colClassName = "java.lang.Byte";
            } else {
                colClassName = "java.lang.Object";
            }
        }
        return colClassName;
    }

    public void createInfo(String driverClassName, String dbUrl, String user, String password, String optionalConnProps, String sqlSelect) throws ClassNotFoundException {
        if (driverClassName == null || driverClassName.trim().equals("")) {
            throw new IllegalArgumentException("Driver name is missing");
        } else if (dbUrl == null || dbUrl.trim().equals("")) {
            throw new IllegalArgumentException("Database URL is missing");
        } else if (sqlSelect == null || sqlSelect.trim().equals("")) {
            throw new IllegalArgumentException("SQL SELECT is missing");
        }
        Connection con = null;
        Statement stmt = null;
        ResultSet rs = null;
        info.clear();
        dupsMap.clear();
        try {
            Properties prop = new Properties();
            prop.setProperty("user", user);
            prop.setProperty("password", password);
            if (optionalConnProps != null && optionalConnProps.trim().length() > 0) {
                StringTokenizer st = new StringTokenizer(optionalConnProps, ",", false);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int idx = token.indexOf("=");
                    if (idx == -1) {
                        throw new IllegalArgumentException("Invalid optional connection properties format. \nIt must have format: name1=value1, name2=value2, ...");
                    }
                    String name = token.substring(0, idx);
                    String value = token.substring(idx + 1);
                    prop.setProperty(name, value);
                }
            }
            try {
                if (driver == null || !driverClassName.equals(this.driverClassName)) {
                    driver = (Driver) Class.forName(driverClassName).newInstance();
                }
            } catch (ClassNotFoundException cne) {
                throw cne;
            }
            con = DriverManager.getConnection(dbUrl, prop);
            stmt = con.createStatement();
            rs = stmt.executeQuery(sqlSelect);
            nullable = new LinkedHashMap();
            ResultSetMetaData rsmd = rs.getMetaData();
            int colCount = rsmd.getColumnCount();
            for (int i = 1; i <= colCount; i++) {
                String schemaName = rsmd.getSchemaName(i);
                String tableName = rsmd.getTableName(i);
                String fullColName = rsmd.getColumnName(i);
                if (tableName != null && tableName.trim().length() > 0) {
                    fullColName = tableName + DOT + fullColName;
                } 
                if (schemaName != null && schemaName.trim().length() > 0) {
                    fullColName = schemaName + DOT + fullColName;
                }
                String colName = fullColName.substring(fullColName.lastIndexOf(DOT) + 1);
                String field = null;
                //if (fullColName.contains(DOT)) {
                    field = toCamelCase(colName);
                //}
                String colClassName = getColumnClassName(driverClassName, rsmd, i);
                Vector colInfo = new Vector(8);
                colInfo.add(fullColName);
                colInfo.add(colClassName);
                colInfo.add(field);
                int nullableInfo = rsmd.isNullable(i);
                String nullableInfoString = null;
                if (nullableInfo == ResultSetMetaData.columnNullable) {
                    nullableInfoString = "Nullable";
                } else if (nullableInfo == ResultSetMetaData.columnNullableUnknown) {
                    nullableInfoString = "Unknown";
                } else {
                    nullableInfoString = "NoNulls";
                }
                colInfo.add(toLabel(colName));
                colInfo.add(""); // Swing
                colInfo.add(""); // html
                colInfo.add(nullableInfoString);
                colInfo.add(true);
                info.add(colInfo);
                nullable.put(field, nullableInfoString);
            }
        } catch (ClassNotFoundException e) {
            throw e;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (con != null) {
                    con.close();
                }
                rs = null;
                stmt = null;
                con = null;
            } catch (java.sql.SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /* JavaBean (DomainObject) */
    public void generate(Vector data, String packageName, String className, String outputDir, boolean overwrite, boolean checkNull) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";");
        sb.append("\n\n");
        sb.append("public class ").append(className).append(" implements java.io.Serializable {\n");
        //sb.append("public class ").append(className).append(" /* extends org.julp.AbstractDomainObject */ implements java.io.Serializable {\n");
        
        Iterator it1 = data.iterator();
        /* member variables */
        while (it1.hasNext()) {
            Vector entry = (Vector) it1.next();
            String colName = (String) entry.get(0);
            String dataType = (String) entry.get(1);
            String field = (String) entry.get(2);
            String label = (String) entry.get(3);
            String nullableField = (String) entry.get(6);
            if (colName == null || colName.trim().equals("")) {
                throw new IllegalArgumentException("Column name is missing");
            }
            if (dataType == null || dataType.trim().equals("")) {
                throw new IllegalArgumentException("DataType is missing");
            }
            if (field == null || field.trim().equals("")) {
                throw new IllegalArgumentException("Field name is missing");
            }
            if (label == null || label.trim().equals("")) {
                throw new IllegalArgumentException("Label is missing");
            }
            if (nullable == null || nullableField.trim().equals("")) {
                throw new IllegalArgumentException("Nullable is missing");
            }
            dataType = dataType.replaceFirst("class ", "");
            sb.append("\n\t").append("private ").append(dataType).append(" ").append(field).append(";");
        }
        sb.append("\n\n\t");
        sb.append("public ").append(className).append("() {}\n");
        
        Iterator it2 = data.iterator();
        while (it2.hasNext()) {
            Vector entry = (Vector) it2.next();
            String field = (String) entry.get(2);
            String value = (String) entry.get(1);
            value = value.replaceFirst("class ", "");
            /* getters */
            if (value.toLowerCase().contains("bool")) {
                sb.append("\n\tpublic ").append(value).append(" is").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("() {");
            } else {
                sb.append("\n\tpublic ").append(value).append(" get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("() {");
            }

            sb.append("\n\t\t").append("return this.").append(field).append(";");
            sb.append("\n\t}");
            sb.append("\n");
            /* setters */
            sb.append("\n\tpublic void").append(" set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(").append(value).append(" ").append(field).append(") {");
            /* optional check for null in setters start */
            if (checkNull) {
                sb.append("\n\t\t").append("/* if (!isLoading()) {");
                String colNullability = (String) entry.get(6);
                sb.append("\n\t\t\t//DBColumn nullability: ").append(colNullability).append(" - modify as needed ");
                if (value.equals("int") || value.equals("long") || value.equals("short") || value.equals("char") || value.equals("float") || value.equals("double") || value.equals("boolean")) {
                    sb.append("\n\t\t\t").append("if (").append(field).append(" != this.").append(field).append(") {");
                    sb.append("\n\t\t\t\t").append("this.modified = true;");
                    sb.append("\n\t\t\t").append("}");
                    sb.append("\n\t\t} */");
                } else {
                    if ("NoNulls".equals(colNullability) || "Unknown".equals(colNullability)) {
                        String label = (String) entry.get(3);
                        sb.append("\n\t\t\t").append("if (").append(field).append(" == null");
                        if (value.equals("java.lang.String")) {
                            sb.append(" || ").append(field).append(".trim().length() == 0");
                        }
                        sb.append(") {");
                        sb.append("\n\t\t\t\t").append("throw new IllegalArgumentException(\"Missing value: ").append(label).append("\");");
                        sb.append("\n\t\t\t").append("}");
                        sb.append("\n\t\t\t").append("if (!").append(field).append(".equals(this.").append(field).append(")) {");
                        sb.append("\n\t\t\t\t").append("this.modified = true;");
                        sb.append("\n\t\t\t").append("}");
                    } else {
                        sb.append("\n\t\t\t").append("if (").append(field).append(" == null && this.").append(field).append(" != null) {");
                        sb.append("\n\t\t\t\t").append("this.modified = true;");
                        sb.append("\n\t\t\t").append("} else if (").append(field).append(" != null && this.").append(field).append(" == null) {");
                        sb.append("\n\t\t\t\t").append("this.modified = true;");
                        sb.append("\n\t\t\t").append("} else if (").append(field).append(" == null && this.").append(field).append(" == null) {");
                        sb.append("\n\t\t\t\t").append("this.modified = true;");
                        sb.append("\n\t\t\t").append("} else if (!").append(field).append(".equals(this.").append(field).append(")) {");
                        sb.append("\n\t\t\t\t").append("this.modified = true;");
                        sb.append("\n\t\t\t").append("}");
                    }
                    sb.append("\n\t\t} */");
                    /* optional check for null in setters end */
                }
            }
            sb.append("\n\t\t").append("this.").append(field).append(" = ").append(field).append(";");
            sb.append("\n\t}");
            sb.append("\n");
        }
        sb.append("\n").append("}\n\n");
        String source = sb.toString();
        write(packageName, className, outputDir, overwrite, source, "java");
        generateFactory(packageName, className, outputDir, overwrite);
        generateMappings(data, packageName, className, outputDir, overwrite);
    }
    
    
    /* HTML form */
    public void generate(Vector data, String outputDir, String fileName, boolean overwrite) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n<head>\n</head>\n <body>");
        sb.append("\n  <form action='' name='' method=''>");
        sb.append("\n  <table>");
        Iterator it1 = data.iterator();
        while (it1.hasNext()) {
            Vector entry = (Vector) it1.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String field = (String) entry.get(2);
            String inputType = (String) entry.get(5);
            if (inputType == null || inputType.trim().equals("")) {
                throw new IllegalArgumentException("HTML Input Type name is missing");
            }
            sb.append("\n   <tr>");
            if (inputType.toLowerCase().equals("textarea")) {
                sb.append("\n    <td valign='top'>");
                sb.append("\n    <td class=''  valign='top'>");
            } else {
                sb.append("\n    <td class=''>");
            }
            if (!inputType.toLowerCase().equals("hidden")) {
                String label = (String) entry.get(3);
                sb.append(label);
                sb.append(":</td>");
            }
            sb.append("\n    <td>");
            if (inputType.toLowerCase().equals("textarea")) {
                sb.append("<textarea name='").append(field).append("' cols='40' rows='5' class=''>").append("</textarea>");
            } else if (inputType.toLowerCase().equals("select")) {
                sb.append("<select name='").append(field).append("' size='1' class=''>").append("</select>");
            } else {
                sb.append("<input type='").append(inputType).append("' name='").append(field).append("' class='' value=''>");
            }
            sb.append("</td>\n   </tr>");
        }
        sb.append("\n  </table>\n  </form>\n </body>\n</html>");
        write(fileName, outputDir, overwrite, sb.toString());
    }

    protected StringBuilder handleDataType(String dataType) {
        StringBuilder sb = new StringBuilder();
        if (dataType.equals("double")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0.0\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Double(currentValue).doubleValue();");
        } else if (dataType.equals("int")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Integer(currentValue).intValue();");
        } else if (dataType.equals("long")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Long(currentValue).longValue();");
        } else if (dataType.equals("short")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Short(currentValue).shortValue();");
        } else if (dataType.equals("float")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0.0\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Float(currentValue).floatValue();");
        } else if (dataType.equals("byte")) {
            sb.append("\n\t\t\t").append(dataType).append(" value = new Byte(currentValue).byteValue();");
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"0\";");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("boolean")) {
            sb.append("\n\t\t\tif (currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\tcurrentValue = \"false\";");
            sb.append("\n\t\t\t}");
            sb.append("\n\t\t\t").append(dataType).append(" value = new Boolean(currentValue).booleanValue();");
        } else if (dataType.equals("char")) {
            sb.append("\n\t\t\tchar value = '\\u0000';");
            sb.append("\n\t\t\tif (!currentValue.equals(\"\")) {");
            sb.append("\n\t\t\t\t").append("value = new Character(currentValue).charValue();");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("java.sql.Date")) {
            sb.append("\n\t\t\tjava.sql.Date value = null;");
            sb.append("\n\t\t\ttry {");
            sb.append("\n\t\t\t\tvalue = new java.sql.Date(dateFormat.parse(currentValue).getTime());");
            sb.append("\n\t\t\t} catch (java.text.ParseException pe) {");
            sb.append("\n\t\t\t\tthrow new RuntimeException(pe);");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("java.util.Date")) {
            sb.append("\n\t\t\tjava.util.Date value = null;");
            sb.append("\n\t\t\ttry {");
            sb.append("\n\t\t\t\tvalue = dateFormat.parse(currentValue));");
            sb.append("\n\t\t\t} catch (java.text.ParseException pe) {");
            sb.append("\n\t\t\t\tthrow new RuntimeException(pe);");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("java.sql.Timestamp")) {
            sb.append("\n\t\t\tjava.sql.Timestamp value = null;");
            sb.append("\n\t\t\ttry {");
            sb.append("\n\t\t\t\tvalue = new java.sql.Timestamp(timestampFormat.parse(currentValue).getTime());");
            sb.append("\n\t\t\t} catch (java.text.ParseException pe) {");
            sb.append("\n\t\t\t\tthrow new RuntimeException(pe);");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("java.sql.Time")) {
            sb.append("\n\t\t\tjava.sql.Time value = null;");
            sb.append("\n\t\t\ttry {");
            sb.append("\n\t\t\t\tvalue = new java.sql.Time(timeFormat.parse(currentValue).getTime());");
            sb.append("\n\t\t\t} catch (java.text.ParseException pe) {");
            sb.append("\n\t\t\t\tthrow new RuntimeException(pe);");
            sb.append("\n\t\t\t}");
        } else if (dataType.equals("java.lang.Object")) {
            sb.append("\n\t\t//<TODO: convert java.lang.Object into correct datatype??>");
        } else {
            sb.append("\n\t\t\t").append(dataType).append(" value = new ").append(dataType).append("(currentValue);");
        }
        return sb;
    }

    protected StringBuilder generateHandlers(Vector data, String domainObject) {
        StringBuilder sb = new StringBuilder();
        /*  Editors getters */
        sb.append("\n\t /*  Editors getters */");
        Iterator it = data.iterator();
        while (it.hasNext()) {
            Vector entry = (Vector) it.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String field = (String) entry.get(2);
            String editor = (String) entry.get(4);
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            sb.append("\n\n\tpublic ").append(editor).append(" get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append(editorShortName).append("() {");
            sb.append("\n\t\t").append("return this.").append(field).append(editorShortName).append(";");
            sb.append("\n\t}");
        }
        sb.append("\n\t");
        Iterator it4 = data.iterator();
        while (it4.hasNext()) {
            Vector entry = (Vector) it4.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String dataType = (String) entry.get(1);
            String field = (String) entry.get(2);
            String editor = (String) entry.get(4);
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            /*
            events handlers:
            get form's editor value on focusGained event and
            set value from form's editor to domainObject on focusLost event if the value has changed
             */
            if (editor.equals("javax.swing.JTextField")
                    || editor.equals("javax.swing.JLabel")
                    || editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusGained(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\torigValue = ").append(field).append(editorShortName).append(".getText();");
                sb.append("\n\t}");
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusLost(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\tcurrentValue = ").append(field).append(editorShortName).append(".getText();");
                sb.append("\n\t\tif (!currentValue.equals(origValue)) {");
                sb.append(handleDataType(dataType));
                sb.append("\n\t\t\ttry {");
                sb.append("\n\t\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(value);");
                sb.append("\n\t\t\t} catch(Exception e) {");
                sb.append("\n\t\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JFormattedTextField")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusGained(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\t").append(field).append(editorShortName).append(".commitEdit();");
                sb.append("\n\t\t} catch (java.text.ParseException pe) {");
                sb.append("\n\t\t\t// ignore? throw new RuntimeException(pe);");
                sb.append("\n\t\t}");
                sb.append("\n\t\tObject fieldValue = ").append(field).append(editorShortName).append(".getValue();");
                sb.append("\n\t\tif (fieldValue == null) {");
                sb.append("\n\t\t\torigValue = \"\";");
                sb.append("\n\t\t} else{");
                sb.append("\n\t\t\torigValue = fieldValue.toString();");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusLost(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\t").append(field).append(editorShortName).append(".commitEdit();");
                sb.append("\n\t\t} catch (java.text.ParseException pe) {");
                sb.append("\n\t\t\tthrow new RuntimeException(pe);");
                sb.append("\n\t\t}");
                sb.append("\n\t\tObject fieldValue = ").append(field).append(editorShortName).append(".getValue();");
                sb.append("\n\t\tif (fieldValue == null) {");
                sb.append("\n\t\t\tcurrentValue = \"\";");
                sb.append("\n\t\t} else{");
                sb.append("\n\t\t\tcurrentValue = fieldValue.toString();");
                sb.append("\n\t\t}");
                sb.append("\n\t\tif (!currentValue.equals(origValue)) {");
                sb.append("\n\t\t\ttry {");
                sb.append("\n\t\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(\"<TODO: convert datatype>\"fieldValue);");
                sb.append("\n\t\t\t} catch(Exception e) {");
                sb.append("\n\t\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JCheckBox") || editor.equals("javax.swing.JRadioButton")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("ItemStateChanged(java.awt.event.ItemEvent evt) {");
                sb.append("\n\t\tif (evt.getStateChange() == ItemEvent.SELECTED) {");
                sb.append("\n\t\t\t//<TODO: convert editor's state into field's value>");
                sb.append("\n\t\t\t//domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(<TODO>);");
                sb.append("\n\t\t} else if (evt.getStateChange() == ItemEvent.DESELECTED) {");
                sb.append("\n\t\t\t//<TODO: convert editor's state into field's value>");
                sb.append("\n\t\t\t//domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(<TODO>);");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JComboBox")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("ItemStateChanged(java.awt.event.ItemEvent evt) {");
                sb.append("\n\t\tif (evt.getStateChange() != java.awt.event.ItemEvent.SELECTED) return;");
                sb.append("\n\t\tObject fieldValue = ").append(field).append(editorShortName).append(".getSelectedItem();");
                sb.append("\n\t\t//<TODO: make sure to use \"DATA VALUE\" not a \"DISPLAY VALUE\"!>");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(fieldValue);");
                sb.append("\n\t\t} catch(Exception e) {");
                sb.append("\n\t\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JList")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("ValueChanged(java.awt.event.ListSelectionEvent evt) {");
                sb.append("\n\t\tObject fieldValue = ").append(field).append(editorShortName).append(".getSelectedValue();");
                sb.append("\n\t\t\t//<TODO: make sure to use \"DATA VALUE\" not a \"DISPLAY VALUE\"!>");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(fieldValue);");
                sb.append("\n\t\t} catch(Exception e) {");
                sb.append("\n\t\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JSpinner")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("StateChanged(javax.swing.event.ChangeEvent evt) {");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\t").append(field).append(editorShortName).append(".commitEdit();");
                sb.append("\n\t\t} catch (java.text.ParseException pe) {");
                sb.append("\n\t\t\t// ignore? throw new RuntimeException(pe);");
                sb.append("\n\t\t}");
                sb.append("\n\t\tObject fieldValue = ").append(field).append(editorShortName).append(".getValue();");
                sb.append("\n\t\ttry {");
                sb.append("\n\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(\"<TODO: convert datatype>\"fieldValue);");
                sb.append("\n\t\t} catch(Exception e) {");
                sb.append("\n\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            } else if (editor.equals("javax.swing.JPasswordField")) {
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusGained(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\torigValue = ").append("new String(").append(field).append(editorShortName).append(".getPassword());");
                sb.append("\n\t}");
                sb.append("\n\n\tprotected void ").append(field).append(editorShortName).append("FocusLost(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\tif (evt.isTemporary()) {");
                sb.append("\n\t\t\treturn;");
                sb.append("\n\t\t}");
                sb.append("\n\t\torigValue = ").append("new String(").append(field).append(editorShortName).append(".getPassword());");
                sb.append("\n\t\tif (!currentValue.equals(origValue)) {");
                sb.append(handleDataType(dataType));
                sb.append("\n\t\t\ttry {");
                sb.append("\n\t\t\t\tthis.domainObject.set").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("(value);");
                sb.append("\n\t\t\t} catch(Exception e) {");
                sb.append("\n\t\t\t\tJOptionPane.showMessageDialog(this, e.getMessage(), \"Error\", JOptionPane.ERROR_MESSAGE);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t}");
                sb.append("\n\t}");
            }
        }
        return sb;
    }

    protected StringBuilder generateMembers(Vector data, String domainObject) {
        StringBuilder sb = new StringBuilder();
        boolean textArea = false;
        Iterator it2 = data.iterator();
        while (it2.hasNext()) {
            Vector entry = (Vector) it2.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            Object field = entry.get(2);
            String editor = ((String) entry.get(4));
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            if (editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")) {
                textArea = true;
            }
        }
        /* member variables */
        sb.append("\n\t/* member variables */");
        sb.append("\n\t//protected Dimension LABEL_MAX = new Dimension(120, 30);");
        sb.append("\n\t//protected Dimension LABEL_MIN = new Dimension(90, 16);");
        sb.append("\n\tprotected Dimension LABEL_PREF = new Dimension(100, 20);");
        sb.append("\n\t//protected Dimension TEXT_MAX = new Dimension(120, 30);");
        sb.append("\n\t//protected Dimension TEXT_MIN = new Dimension(90, 16);");
        sb.append("\n\t//protected Dimension BUTTON_PREF = new Dimension(100, 20);");
        if (textArea) {
            sb.append("\n\tprotected Dimension TEXTAREA_PREF = new Dimension(300, 100);");
        }
        sb.append("\n\tprotected Dimension TEXT_PREF = new Dimension(300, 20);");
        sb.append("\n\n\t//origValue: original String value of currently editing component");
        sb.append("\n\tprotected String origValue = \"\";");
        sb.append("\n\n\t//currentValue: current String value of currently editing component");
        sb.append("\n\tprotected String currentValue = \"\";");
        sb.append("\n\n\t//domainObject: data object");
        sb.append("\n\tprotected ").append(domainObject).append(" domainObject;");
        Iterator it3 = data.iterator();
        boolean timeDone = false;
        boolean timestampDone = false;
        boolean dateDone = false;
        while (it3.hasNext()) {
            Vector entry = (Vector) it3.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String dataType = (String) entry.get(1);
            if (dataType.equals("java.util.Date") || dataType.equals("java.sql.Date")) {
                if (!dateDone) {
                    sb.append("\n\n\tprotected SimpleDateFormat dateFormat = new SimpleDateFormat(\"MM/dd/yyyy\");");
                    dateDone = true;
                }
            }
            if (dataType.equals("java.sql.Time")) {
                if (!timeDone) {
                    sb.append("\n\n\tprotected SimpleDateFormat timeFormat = new SimpleDateFormat(\"HH:mm:ss\");");
                    timeDone = true;
                }
            }
            if (dataType.equals("java.sql.Timestamp")) {
                if (!timestampDone) {
                    sb.append("\n\n\tprotected SimpleDateFormat timestampFormat = new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\");\n");
                    timestampDone = true;
                }
            }
            Object field = entry.get(2);
            String editor = ((String) entry.get(4));
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            // editor's label
            sb.append("\n\t").append("protected ").append("javax.swing.JLabel").append(" ").append(field).append("JLabel = new javax.swing.JLabel(\"").append(entry.get(3)).append(":\");\n");
            // editor
            sb.append("\n\t").append("protected ").append(editor).append(" ").append(field).append(editorShortName).append(" = new ").append(editor).append("();\n");
        }
        /* end member variables */
        sb.append("\n\n\tpublic ").append(domainObject).append(" getDomainObject() {");
        sb.append("\n\t\treturn this.domainObject;");
        sb.append("\n\t}");
        sb.append("\n\n\tpublic void setDomainObject(").append(domainObject).append(" domainObject) {\n");
        sb.append("\t\tthis.domainObject = domainObject;");
        sb.append("\n\t}\n");
        sb.append(generateHandlers(data, domainObject));
        return sb;
    }

    protected StringBuilder generateInit(Vector data, String domainObject) {
        StringBuilder sb = new StringBuilder();
        sb.append("\tprotected void init() {\n");
        sb.append("\n\t\t").append("//BORDER - border");
        sb.append("\n\t\t").append("//FILL - fill");
        sb.append("\n\t\t").append("//PREFERRED - preferred");
        sb.append("\n\t\t").append("//V_SPACE - vertical space between labels and text fields");
        sb.append("\n\t\t").append("//V_GAP - vertical gap between form elements");
        sb.append("\n\t\t").append("//H_GAP -  horizontal gap between form elements\n");
        sb.append("\n\t\t").append("double BORDER = 10;");
        sb.append("\n\t\t").append("double FILL = TableLayout.FILL;");
        sb.append("\n\t\t").append("double PREFERRED = TableLayout.PREFERRED;");
        sb.append("\n\t\t").append("double V_SPACE = 5;");
        sb.append("\n\t\t").append("double V_GAP = 10;");
        sb.append("\n\t\t").append("double H_GAP = 5;");
        sb.append("\n\t\t").append("double size[][] = {");
        sb.append("\n\t\t\t\t").append("{BORDER, PREFERRED, H_GAP, PREFERRED, BORDER}, //columns");
        sb.append("\n\t\t\t\t").append("{BORDER, ");
        Iterator it0 = data.iterator();
        while (it0.hasNext()) {
            Vector entry = (Vector) it0.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            sb.append("\n\t\t\t\t").append("PREFERRED, \n\t\t\t\tV_SPACE, ");
        }
        sb.append("\n\t\t\t\t").append("BORDER} //rows\n\t\t};");
        sb.append("\n\n\t\tthis.setLayout(new TableLayout(size));\n");
        Iterator it1 = data.iterator();
        int row = 1;
        while (it1.hasNext()) {
            Vector entry = (Vector) it1.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String dataType = (String) entry.get(1);
            Object field = entry.get(2);
            String editor = ((String) entry.get(4));
            if (editor == null || editor.trim().length() == 0) {
                throw new IllegalArgumentException("Swing Editor is missing");
            }
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            sb.append("\n\t\t").append(field).append("JLabel.setPreferredSize(LABEL_PREF);");
            sb.append("\n\t\t//").append(field).append("JLabel.setDisplayedMnemonic('<TODO>');");
            sb.append("\n\t\t").append(field).append("JLabel.setLabelFor(").append(field).append(editorShortName).append(");");
            sb.append("\n\t\tthis.add(").append(field).append("JLabel, ").append("\"1, ").append(String.valueOf(row)).append("\"); //column, row");
            if (editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")
                    || editor.equals("javax.swing.JList")) {
                sb.append("\n\n\t\t").append(field).append(editorShortName).append(".setPreferredSize(TEXTAREA_PREF); //<TODO>");
            } else {
                sb.append("\n\n\t\t").append(field).append(editorShortName).append(".setPreferredSize(TEXT_PREF); //<TODO>");
            }
            if (dataType.equals("double")
                    || dataType.equals("float")
                    || dataType.equals("int")
                    || dataType.equals("short")
                    || dataType.equals("long")
                    || dataType.equals("java.lang.Float")
                    || dataType.equals("java.math.BigDecimal")
                    || dataType.equals("java.lang.Double")
                    || dataType.equals("java.lang.Long")
                    || dataType.equals("java.math.BigInteger")) {
                if (editor.equals("javax.swing.JFormattedTextField")) {
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setHorizontalAlignment(javax.swing.JTextField.RIGHT);");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(NumberFormat.getNumberInstance())));");
                } else if (editor.equals("javax.swing.JTextField")) {
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setHorizontalAlignment(javax.swing.JTextField.RIGHT);");
                }
            } else if (dataType.equals("java.util.Date") || dataType.equals("java.sql.Date")) {
                if (editor.equals("javax.swing.JFormattedTextField")) {
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setHorizontalAlignment(javax.swing.JTextField.RIGHT);");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat(\"MM/dd/yyyy\"))));");
                }
            } else if (dataType.equals("java.sql.Time")) {
                if (editor.equals("javax.swing.JFormattedTextField")) {
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setHorizontalAlignment(javax.swing.JTextField.RIGHT);");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat(\"HH:mm:ss\"))));");
                }
            } else if (dataType.equals("java.sql.Timestamp")) {
                if (editor.equals("javax.swing.JFormattedTextField")) {
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setHorizontalAlignment(javax.swing.JTextField.RIGHT);");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(new SimpleDateFormat(\"MM/dd/yyyy HH:mm:ss\"))));");
                }
            }
            if (editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")
                    || editor.equals("javax.swing.JList")) {
                sb.append("\n\t\t").append("javax.swing.JScrollPane ").append(field).append("ScrollPane = new javax.swing.JScrollPane();");
                sb.append("\n\t\t").append(field).append("ScrollPane.setViewportView(").append(field).append(editorShortName).append(");");
                sb.append("\n\t\tthis.add(").append(field).append("ScrollPane").append(", \"3, ").append(String.valueOf(row)).append("\"); //column, row\n");
            } else {
                sb.append("\n\t\tthis.add(").append(field).append(editorShortName).append(", \"3, ").append(String.valueOf(row)).append("\"); //column, row\n");
            }
            row = row + 2;
        }
        /*  Events handlers: get editor's value on focusGained event and set value to domainObject on focusLost event if the value has changed */
        sb.append("\n\n\t\t/*  Events handlers: get editor's value on focusGained event \n\t\t\t and set value to domainObject on focusLost event if the value has changed */ \n");
        Iterator it2 = data.iterator();
        boolean textArea = false;
        while (it2.hasNext()) {
            Vector entry = (Vector) it2.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            Object field = entry.get(2);
            String editor = ((String) entry.get(4));
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            if (editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")) {
                textArea = true;
            }
            if (editor.equals("javax.swing.JTextField")
                    || editor.equals("javax.swing.JLabel")
                    || editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JTextPane")
                    || editor.equals("javax.swing.JPasswordField")
                    || editor.equals("javax.swing.JFormattedTextField")
                    || editor.equals("javax.swing.JPasswordField")) {
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addFocusListener(new java.awt.event.FocusAdapter() {");
                sb.append("\n\t\t\tpublic void focusGained(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("FocusGained(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t\tpublic void focusLost(java.awt.event.FocusEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("FocusLost(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else if (editor.equals("javax.swing.JCheckBox")) {
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addItemListener(new java.awt.event.ItemListener() {");
                sb.append("\n\t\t\tpublic void itemStateChanged(java.awt.event.ItemEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("ItemStateChanged(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else if (editor.equals("javax.swing.JRadioButton")) {
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addItemListener(new java.awt.event.ItemListener() {");
                sb.append("\n\t\t\tpublic void itemStateChanged(java.awt.event.ItemEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("ItemStateChanged(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else if (editor.equals("javax.swing.JComboBox")) {
                sb.append("\n\t\t//<TODO: make sure to use \"DATA VALUE\" not a \"DISPLAY VALUE\"!>");
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addItemListener(new java.awt.event.ItemListener() {");
                sb.append("\n\t\t\tpublic void itemStateChanged(java.awt.event.ItemEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("ItemStateChanged(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else if (editor.equals("javax.swing.JList")) {
                sb.append("\n\t\t//<TODO: make sure to use \"DATA VALUE\" not a \"DISPLAY VALUE\"!>");
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addListSelectionListener(new java.awt.event.ListSelectionListener() {");
                sb.append("\n\t\t\tpublic void valueChanged(java.awt.event.ListSelectionEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("ValueChanged(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else if (editor.equals("javax.swing.JSpinner")) {
                sb.append("\n\t\t").append(field).append(editorShortName).append(".addChangeListener(new javax.swing.event.ChangeListener() {");
                sb.append("\n\t\t\tpublic void stateChanged(javax.swing.event.ChangeEvent evt) {");
                sb.append("\n\t\t\t\t").append(field).append(editorShortName).append("StateChanged(evt);");
                sb.append("\n\t\t\t}");
                sb.append("\n\t\t});\n");
            } else {
                sb.append("\n\t\t//").append(field).append(editorShortName).append("<TODO!!??>\n");
            }
        }
        sb.append("\n\t}\n");   // end init()
        return sb;
    }

    protected StringBuilder generateLoad(Vector data, String domainObject) {
        StringBuilder sb = new StringBuilder();
        /* load(): populate components with data from domainObject */
        sb.append("\n\n\tpublic void load() {");
        sb.append("\n\t\t//domainObject.setLoading(true);");
        Iterator it5 = data.iterator();
        while (it5.hasNext()) {
            Vector entry = (Vector) it5.next();
            Boolean visible = (Boolean) entry.get(7);
            if (!visible.booleanValue()) {
                continue;
            }
            String dataType = (String) entry.get(1);
            String field = (String) entry.get(2);
            String editor = (String) entry.get(4);
            String editorShortName = editor.substring(editor.lastIndexOf(DOT) + 1);
            if (editor.equals("javax.swing.JTextField")
                    || editor.equals("javax.swing.JLabel")
                    || editor.equals("javax.swing.JTextArea")
                    || editor.equals("javax.swing.JEditorPane")
                    || editor.equals("javax.swing.JPasswordField")
                    || editor.equals("javax.swing.JTextPane")) {
                if (dataType.equals("double")) {
                    sb.append("\n\t\tdouble ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Double(").append(field).append(").toString());");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\tint ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Integer(").append(field).append(").toString());");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\tlong ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Long(").append(field).append(").toString();");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\tshort ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Short(").append(field).append(").toString());");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\tfloat ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Float(").append(field).append(").toString());");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\tboolean ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Boolean(").append(field).append(").toString());");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\tbyte ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Byte(").append(field).append(").toString());");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\tchar ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setText(new Character(").append(field).append(").toString());");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\tString ").append(field).append("String = dateFormat.format(").append(field).append(");");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append("String);");
                    sb.append("\n\t\t}");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\tString ").append(field).append("String = dateFormat.format(").append(field).append(");");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append("String);");
                    sb.append("\n\t\t}");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\tString ").append(field).append("String = timestampFormat.format(").append(field).append(");");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append("String);");
                    sb.append("\n\t\t}");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\tString ").append(field).append("String = timeFormat.format(").append(field).append(");");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append("String);");
                    sb.append("\n\t\t}");
                } else if (dataType.equals("java.lang.String")) {
                    sb.append("\n\t\tjava.lang.String ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append(");");
                    sb.append("\n\t\t}");
                } else {
                    sb.append("\n\t\tjava.lang.Object ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\tif (").append(field).append(" == null) {");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(\"\");");
                    sb.append("\n\t\t} else{");
                    sb.append("\n\t\t\t").append(field).append(editorShortName).append(".setText(").append(field).append(".toString());");
                    sb.append("\n\t\t}");
                }
            } else if (editor.equals("javax.swing.JCheckBox") || editor.equals("javax.swing.JRadioButton")) {
                sb.append("\n\t\t//").append(field).append(editorShortName).append("<TODO: set editor's state depending on domainObject field's value>");
                if (dataType.equals("double")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tDouble ").append(field).append(" = new Double(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tInteger ").append(field).append(" = new Integer(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tLong ").append(field).append(" = new Long(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tShort ").append(field).append(" = new Short(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = new Float(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tBoolean ").append(field).append(" = new Boolean(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tByte ").append(field).append(" = new Byte(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\tCharacter").append(field).append(" = new Character(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                } else {
                    sb.append("\n\t\tObject ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelected(\"<TODO: set>\" false/true);");
                }
            } else if (editor.equals("javax.swing.JFormattedTextField")) {
                if (dataType.equals("double")) {
                    sb.append("\n\t\tDouble ").append(field).append(" = new Double(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\tInteger ").append(field).append(" = new Integer(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\tLong ").append(field).append(" = new Long(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\tShort ").append(field).append(" = new Short(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = new Float(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\tBoolean ").append(field).append(" = new Boolean(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\tByte ").append(field).append(" = new Byte(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\tCharacter").append(field).append(" = new Character(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" =  this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" =  this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Double")) {
                    sb.append("\n\t\tDouble ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Integer")) {
                    sb.append("\n\t\tInteger ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Long")) {
                    sb.append("\n\t\tLong ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Short")) {
                    sb.append("\n\t\tShort ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Boolean")) {
                    sb.append("\n\t\tBoolean ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Byte")) {
                    sb.append("\n\t\tByte ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Character")) {
                    sb.append("\n\t\tCharacter ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else {
                    sb.append("\n\t\tObject ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                }
            } else if (editor.equals("javax.swing.JSpinner")) {
                if (dataType.equals("double")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tDouble ").append(field).append(" = new Double(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tInteger ").append(field).append(" = new Integer(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tLong ").append(field).append(" = new Long(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tShort ").append(field).append(" = new Short(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tFloat ").append(field).append(" = new Float(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tBoolean ").append(field).append(" = new Boolean(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tByte ").append(field).append(" = new Byte(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\t").append("// if field's datatype of domainObject is primitive than make sure to convert value into Object!");
                    sb.append("\n\t\tCharacter").append(field).append(" = new Character(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" =  this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" =  this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Double")) {
                    sb.append("\n\t\tDouble ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Integer")) {
                    sb.append("\n\t\tInteger ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Long")) {
                    sb.append("\n\t\tLong ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Short")) {
                    sb.append("\n\t\tShort ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Boolean")) {
                    sb.append("\n\t\tBoolean ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Byte")) {
                    sb.append("\n\t\tByte ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else if (dataType.equals("java.lang.Character")) {
                    sb.append("\n\t\tCharacter ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                } else {
                    sb.append("\n\t\tObject ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setValue(").append(field).append(");");
                }
            } else if (editor.equals("javax.swing.JComboBox")) {
                if (dataType.equals("double")) {
                    sb.append("\n\t\tDouble ").append(field).append(" = new Double(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\tInteger ").append(field).append(" = new Integer(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\tLong ").append(field).append(" = new Long(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\tShort ").append(field).append(" = new Short(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = new Float(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\tBoolean ").append(field).append(" = new Boolean(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\tByte ").append(field).append(" = new Byte(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\tCharacter").append(field).append(" = new Character(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                } else {
                    sb.append("\n\t\tObject ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedItem(").append(field).append(");");
                }
            } else if (editor.equals("javax.swing.JList")) {
                if (dataType.equals("double")) {
                    sb.append("\n\t\tDouble ").append(field).append(" = new Double(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("int")) {
                    sb.append("\n\t\tInteger ").append(field).append(" = new Integer(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("long")) {
                    sb.append("\n\t\tLong ").append(field).append(" = new Long(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("short")) {
                    sb.append("\n\t\tShort ").append(field).append(" = new Short(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("float")) {
                    sb.append("\n\t\tFloat ").append(field).append(" = new Float(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("boolean")) {
                    sb.append("\n\t\tBoolean ").append(field).append(" = new Boolean(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("byte")) {
                    sb.append("\n\t\tByte ").append(field).append(" = new Byte(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("char")) {
                    sb.append("\n\t\tCharacter").append(field).append(" = new Character(").append("this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("java.sql.Date")) {
                    sb.append("\n\t\tjava.sql.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("java.util.Date")) {
                    sb.append("\n\t\tjava.util.Date ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("java.sql.Time")) {
                    sb.append("\n\t\tjava.sql.Time ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else if (dataType.equals("java.sql.Timestamp")) {
                    sb.append("\n\t\tjava.sql.Timestamp ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                } else {
                    sb.append("\n\t\tObject ").append(field).append(" = this.domainObject.get").append(field.substring(0, 1).toUpperCase()).append(field.substring(1)).append("();");
                    sb.append("\n\t\t").append(field).append(editorShortName).append(".setSelectedValue(").append(field).append(", true);");
                }
            }
        }
        sb.append("\n\t\t//domainObject.setLoading(false);");
        sb.append("\n\t}\n"); //end of load()
        return sb;
    }
    
    /* Swing Form */
    public void generateSwingForm(Vector data, String packageName, String className, String outputDir, String domainObject, boolean overwrite) {
        StringBuilder sb = new StringBuilder();
        sb.append("package ").append(packageName).append(";");
        sb.append("\n\nimport info.clearthought.layout.TableLayout;");
        sb.append("\nimport java.awt.Dimension;\n");
        sb.append("\nimport javax.swing.*;");
        sb.append("\nimport java.text.*;");
        sb.append("\nimport java.util.*;");
        sb.append("\n\n");
        sb.append("/* =============== Modify as needed =============== */");
        sb.append("\n\n");
        sb.append("public class ").append(className).append(" extends javax.swing.JPanel {");
        sb.append("\n\n\t");
        sb.append("public ").append(className).append("() {");
        sb.append("\n\t\tinit();\n");
        sb.append("\t}\n\n");
        sb.append(generateInit(data, domainObject));
        sb.append(generateMembers(data, domainObject));
        sb.append(generateLoad(data, domainObject));
        sb.append("\n").append("}\n\n"); //end of class
        String source = sb.toString();
        write(packageName, className, outputDir, overwrite, source, "java");
    }

    protected String toCamelCase(Object columnName) {
        String column = (String) columnName;
        String field = column.toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean toUpper = false;
        for (int i = 0; i < field.length(); i++) {
            char c = field.charAt(i);
            if (c != '_') {
                if (toUpper) {
                    c = Character.toUpperCase(c);
                } else {
                }
                sb.append(c);
                toUpper = false;
            } else {
                toUpper = true;
            }
        }
        // Handle case when two (or more) tables in SELECT statement
        // have the same column names
        int dupsNum = 0;
        if (dupsMap.containsKey(column)) {
            dupsNum = dupsMap.get(column);
            dupsNum = dupsNum + 1;
            dupsMap.put(column, dupsNum);
            sb.append(dupsNum);
        } else {
            dupsMap.put(column, 0);
        }
        return sb.toString();
    }

    protected void generateFactory(String pakageName, String domainClassName, String outputDir, boolean override) {
        StringBuilder sb = new StringBuilder();
        String className = domainClassName + "Factory";
        sb.append("package ").append(pakageName).append(";");
        sb.append("\n\n");
        //sb.append("import java.util.*;");
        //sb.append("\n\n");
        sb.append("public class ").append(className).append(" extends org.julp.db.DomainObjectFactory<").append(domainClassName).append("> implements java.io.Serializable {\n");
        sb.append("\n\t");
        sb.append("public ").append(className).append("() {");
        sb.append("\n\t\t");
        sb.append("this.setDomainClass(").append(domainClassName).append(".class);");
        sb.append("\n\t}");
        sb.append("\n}");
        String source = sb.toString();
        write(pakageName, className, outputDir, override, source, "java");
    }

    protected void generateMappings(Vector data, String pakageName, String domainClassName, String outputDir, boolean override) {
        StringBuilder sb = new StringBuilder("# !!! MAKE SURE TO ADD/REMOVE CATALOG(s), SCHEMA(s) AND TABLE(s) NAMES. \n#TABLES, COLUMNS AND FIELDS ARE MANDATORY !!! \n");
        Iterator it3 = data.iterator();
        while (it3.hasNext()) {            
            Vector entry = (Vector) it3.next();
            String field = (String) entry.get(2);
            String columnName = (String) entry.get(0);
            if (columnName.indexOf(DOT) > -1) {
                int len = columnName.split("\\.").length;
                if (len == 3) {
                } else if (len == 2) {
                    columnName = "<SCHEMA_NAME>." + columnName;
                } else if (len == 1) {
                    columnName = "<SCHEMA_NAME>.<TABLE_NAME>." + columnName;
                }
            } else {
                columnName = "<SCHEMA_NAME>.<TABLE_NAME>." + columnName;
            }
            sb.append(columnName).append("=").append(field).append("\n");
        }
        String source = sb.toString();
        write(pakageName, domainClassName, outputDir, override, source, "properties");
    }

    protected void write(String pakageName, String className, String outputDir, boolean override, String source, String suffix) {
        char c = File.separatorChar;
        pakageName = pakageName.replace('.', c);
        String javaFile = outputDir + File.separator + pakageName + File.separator + className + DOT + suffix;
        File output = new File(outputDir);
        if (!output.exists()) {
            throw new IllegalArgumentException("Directory " + outputDir + " does not exist");
        }
        if (!output.isDirectory()) {
            throw new IllegalArgumentException("Directory " + outputDir + " is not Directory");
        }
        if (!override) {
            File test = new File(javaFile);
            if (test.exists()) {
                throw new IllegalArgumentException("File " + javaFile + " exists");
            }
        }
        File dir = new File(outputDir + File.separator + pakageName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(javaFile);
            fw.write(source);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        } finally {
            try {
                fw.close();
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }

    protected String toLabel(Object columnName) {
        String label = ((String) columnName).toLowerCase();
        StringBuilder sb = new StringBuilder();
        boolean toUpper = false;
        for (int i = 0; i
                < label.length(); i++) {
            char c = label.charAt(i);
            if (c != '_') {
                if (i == 0) {
                    toUpper = true;
                }
                if (toUpper) {
                    c = Character.toUpperCase(c);
                } else {
                }
                sb.append(c);
                toUpper = false;
            } else {
                sb.append(' ');
                toUpper = true;
            }
        }
        return sb.toString();
    }

    public void setDriverPath(String driverPath) {
        this.driverPath = driverPath;
    }

    protected void write(String fileName, String outputDir, boolean override, String source) {
        String page = outputDir + File.separator + fileName;
        File output = new File(outputDir);
        if (!output.exists()) {
            throw new IllegalArgumentException("Directory " + outputDir + " does not exist");
        }
        if (!output.isDirectory()) {
            throw new IllegalArgumentException("Directory " + outputDir + " is not a Directory");
        }
        if (!override) {
            File test = new File(page);
            if (test.exists()) {
                throw new IllegalArgumentException("File " + page + " exists");
            }
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(page);
            fw.write(source);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            }
        }
    }
}

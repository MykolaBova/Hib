package simpleorm.quickstart;

import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Table;

import simpleorm.quickstart.AParam;


/**
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 *
 */
final class ColumnGenerate {
    Table table;
    Column column;
    INiceNameFormatter niceName;
    String sFieldName;
    String niceColumnName;


    ColumnGenerate(Table table, Column column, INiceNameFormatter niceName) {
        this.table = table;
        this.column = column;
        this.niceName = niceName;

        sFieldName = jdbcTypeToSField(column);
        niceColumnName = niceName.niceNameForColumn(table.getName(),
                column.getName());
    }

	public boolean isPrimary(){
		
		return column.isPrimaryKey();
	}
    public void outputStaticFields(Writer out) throws IOException {

        out.write("   public static final " + sFieldName + " " +
            niceColumnName + " =\n");

        out.write("      new " + sFieldName + "(meta, \"" + column.getName() +
            "\"");

        if (sFieldName.equalsIgnoreCase("SFieldBigDecimal")) {
            out.write(", " + column.getSize() + ", " + column.getScale());
        } else if (sFieldName.equalsIgnoreCase("SFieldString")) {
            out.write(", " + column.getSize());
        }

        // required and/or primary key?
        String parameters = "";
        String comma = "";

        if (column.isPrimaryKey()) {
            parameters += (comma + "SFieldFlags.SPRIMARY_KEY");
            comma = ", ";
        }

        if (column.isRequired()) {
            parameters += (comma + "SFieldFlags.SMANDATORY");
            comma = ", ";            
        }
        if (column.isAutoIncrement()) {
            parameters += (comma + "SFD_GENERATED_KEY");
            comma = ", ";            
        }

        if (parameters.length() != 0) {
            out.write(",\n         new SFieldFlags[] { " + parameters +
                " }");
        }

        out.write(");\n\n");
    }

    /**
     * Generate something like<br>
            public long getfldIdBirdEvent(){
                    return this.getLong( fldIdBirdEvent);
            }<br>
            public void setfldIdBirdEvent( long value){
                    this.setLong( fldIdBirdEvent, value);
            }
    */
    public void outputGettersAndSetters(Writer out) throws IOException {
        String type = sFieldToPrimative(jdbcTypeToSField(column));
        String typeUpper = type.substring(0, 1).toUpperCase() +
            type.substring(1);

        out.write("   public " + type + " get_" + niceColumnName +
            "(){ return get" + typeUpper + "(" + niceColumnName + ");}\n");

        out.write("   public void set_" + niceColumnName + "( " + type +
            " value){" + "set" + typeUpper + "( " + niceColumnName +
            ",value);}\n\n");
    }

    private static String sFieldToPrimative(String sField) {
        if (sField.equalsIgnoreCase("SFieldDouble")) {
            return "BigDecimal";
        } else if (sField.equalsIgnoreCase("SFieldString")) {
            return "String";
        } else if (sField.equalsIgnoreCase("SFieldBigDecimal")) {
            return "BigDecimal";
        } else if (sField.equalsIgnoreCase("SFieldDate")) {
            return "Date";
        } else if (sField.equalsIgnoreCase("SFieldTimestamp")) {
            return "Date";
        } else if (sField.equalsIgnoreCase("SFieldInteger")) {
            return "int";
        } else if (sField.equalsIgnoreCase("SFieldLong")) {
            return "long";
        } else {
            return " {Code not implemented " + sField + "}";
        }
    }

    private static String sFieldToPrimativeObject(String sField) {
		if (sField.equalsIgnoreCase("SFieldInteger")) {
            return "Integer";
        } else if (sField.equalsIgnoreCase("SFieldLong")) {
            return "Long";
        } else {
            return null;
        }
    }

    /**
     * Mappings from Sybase to SimpleORM types.
     */
    private static String jdbcTypeToSField(Column _column) {
        String datatype = _column.getType();

        if (datatype.equalsIgnoreCase("DOUBLE PRECIS") ||
                datatype.equalsIgnoreCase("DOUBLE")) {
            return "SFieldDouble";
        }

        if (datatype.equalsIgnoreCase("VARCHAR") ||
                datatype.equalsIgnoreCase("LONGVARCHAR") ||
                datatype.equalsIgnoreCase("LONGVARBINARY")) {
            return "SFieldString";
        }

        if (datatype.equalsIgnoreCase("NUMERIC") ||
                datatype.equalsIgnoreCase("DECIMAL")) {
            if (_column.getScale() == 0) {
                if (_column.getPrecisionRadix() <= 9) {
                    return "SFieldInteger";
                } else if (_column.getPrecisionRadix() <= 20) {
                    return "SFieldLong";
                }
            }

            return "SFieldBigDecimal";
        }

        if (datatype.equalsIgnoreCase("IMAGE")) {
            return "SFieldString";
        }

        if (datatype.equalsIgnoreCase("DATETIME")) {
            return "SFieldDate";
        }

        if (datatype.equalsIgnoreCase("CHAR")) {
            return "SFieldString";
        }

        if (datatype.equalsIgnoreCase("SMALLINT")) {
            return "SFieldInteger";
        }

        if (datatype.equalsIgnoreCase("BIT")) {
            return "SFieldInteger"; //should be something else but ...
        }

        if (datatype.equalsIgnoreCase("INT") ||
                datatype.equalsIgnoreCase("INTEGER")) {
            if (_column.getSize() > 9) {
                return "SFieldLong";
            } else {
                return "SFieldInteger";
            }
        }

        if (datatype.equalsIgnoreCase("TEXT")) {
            return "SFieldString";
        }

        if (datatype.equalsIgnoreCase("TINYINT")) {
            return "SFieldInteger";
        }

        if (datatype.equalsIgnoreCase("SMALLDATETIME")) {
            return "SFieldDate";
        }

        if (datatype.equalsIgnoreCase("TIMESTAMP")) {
            return "SFieldTimestamp";
        }

        //VARBINARY to Timestamp - are you sure          
        if (datatype.equalsIgnoreCase("VARBINARY")) {
            return "SFieldTimestamp";
        }

        //map a Float to double.          
        if (datatype.equalsIgnoreCase("FLOAT")|| datatype.equalsIgnoreCase("REAL")) {
            return "SFieldDouble";
        }

        if (datatype.equalsIgnoreCase("DATE")) {
            return "SFieldDate";
        } else {
            return datatype.substring(0, 1).toUpperCase() +
            datatype.substring(1).toLowerCase();
        }
    }

    /**example: "int fldEmpleeID"*/
    public AParam getParameters() {
    	
            //not a foreign key
            String name = "_" + niceColumnName;
            String type = sFieldToPrimative(jdbcTypeToSField(column));
            
            String methodParam = type + " " + name;
            String objectParam = name;
            if( sFieldToPrimativeObject(jdbcTypeToSField(column)) != null){
            	objectParam = "new " +
                sFieldToPrimativeObject(jdbcTypeToSField(column)) + "( " +
                name + ")";
            };                
            return new AParam(name,type, methodParam, objectParam);    
		
    }

}

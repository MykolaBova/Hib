package simpleorm.quickstart;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.Database;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 *
 */
final class TableGenerate {
    Table table;
    INiceNameFormatter niceName;
    Database db;
    File root;

    /**Name of the classes and packages generated*/
    String classNameBase;
    String classNameBR;
    String packageName;


    /**map of ColumnGenerate objects Keyed by ColumnName*/
    Map hmColumns = new HashMap();

    /**Store the ColumnGenerate objects*/
    Vector vtrColumns = new Vector();

    /**Store the primary ColumnGenerate  here*/
    Vector vtrPrimaryColumns = new Vector();

    /**Store the ForeignKeygenerate objects here*/
    Vector vtrForeignKeys = new Vector();


    public static final String[] BASECLASS_IMPORTS =
    {	
    	"simpleorm.dataset.*",
    	"simpleorm.utils.*", 
    	"simpleorm.sessionjdbc.SSessionJdbc", 
    	"java.math.BigDecimal", 
    	"java.util.Date"
    	,};
    
    public static final String[] BRCLASS_IMPORTS =
    {	
    };
    
    public TableGenerate(Database db, Table table, INiceNameFormatter niceName,
        String packageName, File root) {
        this.table = table;
        this.niceName = niceName;
        this.db = db;
        this.root = root;
        classNameBase = niceName.niceNameForTable(table.getName()) + "_gen";
        classNameBR = niceName.niceNameForTable(table.getName());
        this.packageName = packageName;

        loadColumns();
        loadForeignKeys();
    }

    /** put all the columns into the hash map*/
    public void loadColumns() {
        //Load up the fields
        Iterator columnsEnum = table.getColumns().iterator();

        while (columnsEnum.hasNext()) {
            ColumnGenerate column = new ColumnGenerate(table,
                    (Column) columnsEnum.next(), niceName);
            hmColumns.put(column.column.getName(), column);
            vtrColumns.add(column);

            if (column.isPrimary()) {
                vtrPrimaryColumns.add(column);
            }
        }
    }

    /**Add the foreign keys to the hash map*/
    public void loadForeignKeys() {
        //Now process the foreign Keys
        Iterator keysEnum = table.getForeignKeys().iterator();

        while (keysEnum.hasNext()) {
            ForeignKey key = (ForeignKey) keysEnum.next();
            ForeignKeyGenerate keyGen = new ForeignKeyGenerate(table, key,
                    niceName, hmColumns);

            //Now add the referenceKey
            vtrForeignKeys.add(keyGen);
        }
    }

    /**
     * Generates the base class file.
     * File will always be generated
     */
    public void generateBaseClass() throws IOException {
        FileWriter fw = new FileWriter( new File( root, classNameBase + ".java"));
        PrintWriter out =new PrintWriter(fw);
        //Output all the header stuff
        out.println("package " + packageName + ";");
        
        // Imports-sections
        for (int i = 0; i < BASECLASS_IMPORTS.length; i++) {
			out.println("import "+BASECLASS_IMPORTS[i]+";");
		}
        out.println();
        
        //Class comment
        out.println("/**	Base class of table " + table.getName() +".<br>");
        out.println("*Do not edit as will be regenerated by running SimpleORMGenerator");
        out.println("*Generated on " + new Date().toString());
        out.println("***/");
        
        out.println("abstract class " + classNameBase +
            " extends SRecordInstance implements java.io.Serializable {");
        out.println();
//        public static final SRecordMeta <Attachedfile> meta = new SRecordMeta<Attachedfile>(Attachedfile.class, "AttachedFile");

        out.println("   public static final SRecordMeta <"+classNameBR +"> meta = new SRecordMeta<"+classNameBR +">(" +classNameBR + ".class, \"" + table.getName() + "\");");
        out.println();

        //Now generate the fields
        out.println("//Columns in table");

        for (int i = 0; i < vtrColumns.size(); i++) {
            ColumnGenerate field = (ColumnGenerate) vtrColumns.get(i);
            field.outputStaticFields(out);
        }

        //And the getters and setters
        out.println("//Column getters and setters");

        for (int i = 0; i < vtrColumns.size(); i++) {
            ColumnGenerate field = (ColumnGenerate) vtrColumns.get(i);
            field.outputGettersAndSetters(out);
        }

        //The foreign keys, only getters and setters
		if( vtrForeignKeys.size() > 0){        
	        out.println("//Foreign key getters and setters");        
		}
        for (int i = 0; i < vtrForeignKeys.size(); i++) {
            ForeignKeyGenerate field = (ForeignKeyGenerate) vtrForeignKeys.get(i);
            field.outputGettersAndSetters(out);
        }
        
        //the find or create methods
        out.println("//Find and create");        
        genFindOrCreate(out);
		getRefFindOrCreate( out);
		
        //now the end stuff

		out.println("   public SRecordMeta <"+classNameBR +"> getMeta() {");
        out.println("       return meta;");
        out.println("   }");
        out.println("}");
        out.close();
    }

    /** Generate the Business Rules class.
     * File will only be generated if file does not exist
     */
    public void generateBRClass() throws IOException {

        File brFile = new File(root, classNameBR +  ".java");

        if (brFile.exists() == false) {
        	PrintWriter out = new PrintWriter(new FileWriter(brFile));

            //Output all the header stuff
            out.println("package " + packageName + ";");
            for (int i = 0; i < BRCLASS_IMPORTS.length; i++) {
				out.println("import "+BRCLASS_IMPORTS[i]+";");
			}
            out.println();
            out.println("/**Business rules class for table " + table.getName() +".<br>");
            out.println("* Will not be regenerated by SimpleORMGenerator, add any business rules to this class");
            out.println("**/");
            out.println();
            
            out.println("public class " + classNameBR + " extends " +
                classNameBase+" implements java.io.Serializable {");
            out.println();
            out.println("}");
            out.close();
        }
    }

    /**
     * Nice type casted FindOrCreate() for column parameters
    
    public static Matter findOrCreate( SSessionJdbc ses, String _fldClientno, String _fldMatterNo ){
      return (Matter) ses.findOrCreate( meta, new Object[] {new String( _fldClientno), new String( _fldMatterNo)});
    }    
    
    *
    *
    */
    private void genFindOrCreate(Writer out) throws IOException {
        Vector vtrParameters = new Vector();

        Iterator pKeys = table.getPrimaryKeyColumns().iterator();

        //find the column matching each primary keys and add its paramters to the list.
        while (pKeys.hasNext()) {
            Column key = (Column) pKeys.next();
            ColumnGenerate column = (ColumnGenerate) hmColumns.get(key.getName());
            vtrParameters.add(column.getParameters());
        }

        //Build up a string of the paramter type and variables
        //eg "int _EmployeeID, SFieldReference _Department"
        String params = "";

        //and a string of object params
        //eg "new Integer( EmployeeID), _Department
        String paramsVarables = "";

        for (int i = 0; i < vtrParameters.size(); i++) {
            AParam param = (AParam) vtrParameters.get(i);
            params += param.methodParam;
            paramsVarables += param.objectParam;

            if (i < (vtrParameters.size() - 1)) {
                params += ", ";
                paramsVarables += ", ";
            }
        }

        //Now we can create the final string
        String str = "   public static " + classNameBR + " findOrCreate( SSessionJdbc ses " +(params.isEmpty() ? "" : ",") +
            params + " ){\n" + "      return ses.findOrCreate(meta, new Object[] {" + paramsVarables +
            "});\n   }\n";

        out.write(str);
    }

    /**
     * Nice type casted FindOrCreate() using reference fields
     *
     *
     *
     */
    private void getRefFindOrCreate(Writer out)throws IOException {
        //Look for those foreign keys that only use primary columns 
        for (int i = 0; i < vtrForeignKeys.size(); i++) {
            ForeignKeyGenerate key = (ForeignKeyGenerate) vtrForeignKeys.elementAt(i);
            if (key.isPrimary()) {
                key.outputFindOrCreate(out);
            }
        }
    }
}

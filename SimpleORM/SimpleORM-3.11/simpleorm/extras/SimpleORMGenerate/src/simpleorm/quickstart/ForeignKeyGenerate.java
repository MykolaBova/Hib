package simpleorm.quickstart;

import org.apache.commons.sql.model.Column;
import org.apache.commons.sql.model.ForeignKey;
import org.apache.commons.sql.model.Reference;
import org.apache.commons.sql.model.Table;

import java.io.IOException;
import java.io.Writer;

import java.util.*;


/**
 * @author <a href="mailto:richard.schmidt@inform6.com">Richard Schmidt</a>
 *
 */
final class ForeignKeyGenerate {
    public ForeignKey key;
    public Table table;
    INiceNameFormatter niceName;
    Map hmColumns ; //key = column name, object = ColumnGenerate;

    public ForeignKeyGenerate(Table table, ForeignKey key,
        INiceNameFormatter niceName, Map columns) {
        this.key = key;
        this.table = table;
        this.niceName = niceName;
        this.hmColumns = columns;
    }

    /**Never used*/
    public void outputStaticFields(Writer out) throws IOException {
    }

    /**
    Need to generate something like this:
    public Task get_refTask(){
            try {
                return Task.findOrCreate(get_fldClientno(), get_fldMatterno(),
                    get_fldTaskno());
            } catch (SException e) {
                if (e.getMessage().indexOf("Null Primary key") > 0) {
                    return null;
                }

                throw e;
            }}
            NEW:
       public Task get_refTask(SSessionJdbc ses){
            try {
            	return ses.findOrCreate(Task.meta, get_fldMatterno(),get_fldTaskno());
                //return Task.findOrCreate(get_fldClientno(), get_fldMatterno(),get_fldTaskno());
            } catch (SException e) {
                if (e.getMessage().indexOf("Null Primary key") > 0) {
                    return null;
                }

                throw e;
            }}
               
          
            
            
            
    public void set_refTask( Task value){
              set_fldClientno( value.get_fldClientno());
               set_fldMatterno( value.get_fldMatterno());
               set_fldTaskno( value.get_fldTaskno());
    }
             */
    public void outputGettersAndSetters(Writer out) throws IOException {
        String foreignTableName = niceName.niceNameForTable(key.getForeignTable());
        String keyName = niceName.niceNameForForeignKey(table.getName(),
                key.getForeignTable());

        //Do the getter
        //public Task get_refTask(SSessionJdbc ses){ 		
        out.write("   public " + foreignTableName + " get_" + keyName +
            "(SSessionJdbc ses){\n");

        //      try {
        out.write("     try{\n");

        //return Task.findOrCreate( get_fldClientno(), get_fldMatterno(), get_fldTaskno());        
        //Assumes that references will be in primary key order!
        out.write("/** Old code: \n");
        out.write("        return " + foreignTableName + ".findOrCreate(");
        Iterator itt = key.getReferences().iterator();
        while (itt.hasNext()) {
            Reference ref = (Reference) itt.next();
            out.write("get_" +
                niceName.niceNameForColumn(table.getName(), ref.getLocal()) +
                "()");

            if (itt.hasNext()) {
                out.write(",");
            }
        }
        out.write(");\n");
        out.write("New code below :**/\n");
        out.write("        return ses.findOrCreate("+foreignTableName+".meta");

        List references = key.getReferences();
        String parameterArray = "";
        for (Object o : references) {
        	parameterArray += "        	get_" +niceName.niceNameForColumn(table.getName(),((Reference) o).getLocal()) +"(),\n";
        }
        out.write(",new Object[]{ \n"+parameterArray + " });\n");
        //        } catch (SException e) {
        //            if (e.getMessage().indexOf("Null Primary key") > 0) {
        //                return null;
        //            }
        //            throw e;
        //    	}		
        out.write("     } catch (SException e) {\n");
        out.write("        if (e.getMessage().indexOf(\"Null Primary key\") > 0) {\n");
        out.write("          return null;\n");
        out.write("        }\n");
        out.write("        throw e;\n");
        out.write("     }\n");
        out.write("   }\n");

        //Now do the setter		
        //public void set_refTask( Task value){ 
        out.write("   public void set_" + keyName + "( " + foreignTableName +
            " value){\n");
        itt = key.getReferences().iterator();

        while (itt.hasNext()) {
            Reference ref = (Reference) itt.next();

            //set_fldClientno( value.get_fldClientno());
            out.write("      set_" +
                niceName.niceNameForColumn(table.getName(), ref.getLocal()) +
                "( value.get_" +
                niceName.niceNameForColumn(key.getForeignTable(),
                    ref.getForeign()) + "());\n");
        }

        ;
        out.write("   }\n\n");
    }

//    /**example: Department fldRefDepartment"*/
//    public AParam getParameters() {
//        String methodParam = niceName.niceNameForTable(key.getForeignTable()) +
//            " _ref" +
//            niceName.niceNameForForeignKey(table.getName(),
//                key.getForeignTable());
//
//        String objectParam = " _ref" +
//            niceName.niceNameForForeignKey(table.getName(),
//                key.getForeignTable());
//
//        return new AParam(methodParam, objectParam);
//    }

    public boolean isPrimary() {
        List list = key.getReferences();
        Iterator itt = list.iterator();

        while (itt.hasNext()) {
            Reference ref = (Reference) itt.next();
            String column = ref.getLocal();

            if (table.findColumn(column).isPrimaryKey() == false) {
                return false;
            }
        }

        return true;
    }

    private List primaryColumnsNotInRefernce() {
        List list = table.getPrimaryKeyColumns();

        Iterator itt = key.getReferences().iterator();

        while (itt.hasNext()) {
            Reference ref = (Reference) itt.next();
            Column column = table.findColumn(ref.getLocal());
            list.remove(column);
        }

        return list;
    }
    
	private String foreignTableColumnName( Column column){
        Iterator itt = key.getReferences().iterator();

        while (itt.hasNext()) {
            Reference ref = (Reference) itt.next();
            if( ref.getLocal().equals( column.getName())){
            	return ref.getForeign();
            }
        }
        return "error";		
	}    
    
/**   
   public static Matter findOrCreate( SSessionJdbc ses,Client _ref, String _fldMatterNo ){
      return findOrCreate(ses, _ref.get_fldClientno(),_fldMatterNo);   
   }
*/   
    
    public void outputFindOrCreate(Writer out)throws IOException{
    	
    	List columnsNotIn = primaryColumnsNotInRefernce();
    	
//public static Matter findOrCreate( Client _ref, String _fldMatterNo ){     	
		out.write("   public static " + niceName.niceNameForTable( table.getName()) 
			+ " findOrCreate( SSessionJdbc ses," + niceName.niceNameForTable( key.getForeignTable()) 
			+ " _ref, ");
		//gerenate the method paramters 
		Iterator itt = columnsNotIn.iterator();
		while( itt.hasNext()){
			Column column = (Column) itt.next();
			ColumnGenerate cg = (ColumnGenerate) hmColumns.get( column.getName());
			out.write( cg.getParameters().methodParam);			
			if( itt.hasNext()){
				out.write( " ,");
			}
		}	
		out.write( "){\n");		 			    
		
//return findOrCreate( ses, _ref.get_fldClientno(),_fldMatterNo);   		
		out.write( "      return findOrCreate( ses, ");
		
		//iterate through the primary columns again
		Iterator primaryColumnsItt = table.getPrimaryKeyColumns().iterator();
		while( primaryColumnsItt.hasNext()){
			
			Column column = (Column) primaryColumnsItt.next();
			if( columnsNotIn.contains( column) == false){
				
				//This is one of the fields in the refernece 
				out.write( "_ref.get_" + niceName.niceNameForColumn( 
						key.getForeignTable(), 
						foreignTableColumnName( column)) 
						+ "()");
			}else{
				
				//This is one of the fields passed as a parameter;
				ColumnGenerate cg = (ColumnGenerate) hmColumns.get( column.getName());
				out.write( cg.getParameters().paramName);							
			}
			if( primaryColumnsItt.hasNext()){
				out.write( ", ");
			}
		}
		out.write( ");\n");					    						
		
		out.write( "   }\n\n");					    		
    }
}

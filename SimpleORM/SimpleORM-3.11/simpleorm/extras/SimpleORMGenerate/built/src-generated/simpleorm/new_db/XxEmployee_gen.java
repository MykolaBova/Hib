package simpleorm.new_db;
import simpleorm.dataset.*;
import simpleorm.utils.*;
import simpleorm.sessionjdbc.SSessionJdbc;
import java.math.BigDecimal;
import java.util.Date;

/**	Base class of table XX_EMPLOYEE.<br>
*Do not edit as will be regenerated by running SimpleORMGenerator
*Generated on Wed Dec 10 14:47:40 EST 2008
***/
abstract class XxEmployee_gen extends SRecordInstance implements java.io.Serializable {

   public static final SRecordMeta <XxEmployee> meta = new SRecordMeta<XxEmployee>(XxEmployee.class, "XX_EMPLOYEE");

//Columns in table
   public static final SFieldString fldEmpeeid =
      new SFieldString(meta, "Empee*Id", 20,
         new SFieldFlags[] { SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

   public static final SFieldString fldEname =
      new SFieldString(meta, "ENAME", 40,
         new SFieldFlags[] { SFieldFlags.SMANDATORY });

   public static final SFieldString fldPhoneNr =
      new SFieldString(meta, "PHONE_NR", 20);

   public static final SFieldDouble fldSalary =
      new SFieldDouble(meta, "SALARY");

   public static final SFieldInteger fldNrDependents =
      new SFieldInteger(meta, "NR_DEPENDENTS");

   public static final SFieldString fldDeptId =
      new SFieldString(meta, "DEPT_ID", 10);

   public static final SFieldString fldManagerEmpeeId =
      new SFieldString(meta, "MANAGER_EMPEE_ID", 20);

   public static final SFieldString fldEtype =
      new SFieldString(meta, "ETYPE", 9);

   public static final SFieldString fldResume =
      new SFieldString(meta, "RESUME", 200);

//Column getters and setters
   public String get_fldEmpeeid(){ return getString(fldEmpeeid);}
   public void set_fldEmpeeid( String value){setString( fldEmpeeid,value);}

   public String get_fldEname(){ return getString(fldEname);}
   public void set_fldEname( String value){setString( fldEname,value);}

   public String get_fldPhoneNr(){ return getString(fldPhoneNr);}
   public void set_fldPhoneNr( String value){setString( fldPhoneNr,value);}

   public BigDecimal get_fldSalary(){ return getBigDecimal(fldSalary);}
   public void set_fldSalary( BigDecimal value){setBigDecimal( fldSalary,value);}

   public int get_fldNrDependents(){ return getInt(fldNrDependents);}
   public void set_fldNrDependents( int value){setInt( fldNrDependents,value);}

   public String get_fldDeptId(){ return getString(fldDeptId);}
   public void set_fldDeptId( String value){setString( fldDeptId,value);}

   public String get_fldManagerEmpeeId(){ return getString(fldManagerEmpeeId);}
   public void set_fldManagerEmpeeId( String value){setString( fldManagerEmpeeId,value);}

   public String get_fldEtype(){ return getString(fldEtype);}
   public void set_fldEtype( String value){setString( fldEtype,value);}

   public String get_fldResume(){ return getString(fldResume);}
   public void set_fldResume( String value){setString( fldResume,value);}

//Foreign key getters and setters
   public XxDepartment get_refXxDepartment(SSessionJdbc ses){
     try{
/** Old code: 
        return XxDepartment.findOrCreate(get_fldDeptId());
New code below :**/
        return ses.findOrCreate(XxDepartment.meta,new Object[]{ 
        	get_fldDeptId(),
 });
     } catch (SException e) {
        if (e.getMessage().indexOf("Null Primary key") > 0) {
          return null;
        }
        throw e;
     }
   }
   public void set_refXxDepartment( XxDepartment value){
      set_fldDeptId( value.get_fldDeptId());
   }

   public XxEmployee get_refXxEmployee(SSessionJdbc ses){
     try{
/** Old code: 
        return XxEmployee.findOrCreate(get_fldManagerEmpeeId());
New code below :**/
        return ses.findOrCreate(XxEmployee.meta,new Object[]{ 
        	get_fldManagerEmpeeId(),
 });
     } catch (SException e) {
        if (e.getMessage().indexOf("Null Primary key") > 0) {
          return null;
        }
        throw e;
     }
   }
   public void set_refXxEmployee( XxEmployee value){
      set_fldManagerEmpeeId( value.get_fldEmpeeid());
   }

//Find and create
   public static XxEmployee findOrCreate( SSessionJdbc ses ,String _fldEmpeeid ){
      return ses.findOrCreate(meta, new Object[] {_fldEmpeeid});
   }
//specializes abstract method
   public SRecordMeta getMeta() {
       return meta;
   }
}

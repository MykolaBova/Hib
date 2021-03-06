package simpleorm.new_db;
import simpleorm.dataset.*;
import simpleorm.utils.*;
import simpleorm.sessionjdbc.SSessionJdbc;
import java.math.BigDecimal;
import java.util.Date;

/**	Base class of table XX_DEPARTMENT.<br>
*Do not edit as will be regenerated by running SimpleORMGenerator
*Generated on Wed Dec 10 14:47:40 EST 2008
***/
abstract class XxDepartment_gen extends SRecordInstance implements java.io.Serializable {

   public static final SRecordMeta <XxDepartment> meta = new SRecordMeta<XxDepartment>(XxDepartment.class, "XX_DEPARTMENT");

//Columns in table
   public static final SFieldString fldDeptId =
      new SFieldString(meta, "DEPT_ID", 10,
         new SFieldFlags[] { SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

   public static final SFieldString fldDname =
      new SFieldString(meta, "DNAME", 40);

   public static final SFieldString fldMission =
      new SFieldString(meta, "MISSION", 40);

   public static final SFieldDouble fldBudget =
      new SFieldDouble(meta, "BUDGET");

   public static final SFieldDouble fldMaxSalary =
      new SFieldDouble(meta, "MAX_SALARY");

//Column getters and setters
   public String get_fldDeptId(){ return getString(fldDeptId);}
   public void set_fldDeptId( String value){setString( fldDeptId,value);}

   public String get_fldDname(){ return getString(fldDname);}
   public void set_fldDname( String value){setString( fldDname,value);}

   public String get_fldMission(){ return getString(fldMission);}
   public void set_fldMission( String value){setString( fldMission,value);}

   public BigDecimal get_fldBudget(){ return getBigDecimal(fldBudget);}
   public void set_fldBudget( BigDecimal value){setBigDecimal( fldBudget,value);}

   public BigDecimal get_fldMaxSalary(){ return getBigDecimal(fldMaxSalary);}
   public void set_fldMaxSalary( BigDecimal value){setBigDecimal( fldMaxSalary,value);}

//Find and create
   public static XxDepartment findOrCreate( SSessionJdbc ses ,String _fldDeptId ){
      return ses.findOrCreate(meta, new Object[] {_fldDeptId});
   }
//specializes abstract method
   public SRecordMeta getMeta() {
       return meta;
   }
}

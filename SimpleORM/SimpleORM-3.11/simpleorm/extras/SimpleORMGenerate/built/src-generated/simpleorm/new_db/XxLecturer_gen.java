package simpleorm.new_db;
import simpleorm.dataset.*;
import simpleorm.utils.*;
import simpleorm.sessionjdbc.SSessionJdbc;
import java.math.BigDecimal;
import java.util.Date;

/**	Base class of table XX_LECTURER.<br>
*Do not edit as will be regenerated by running SimpleORMGenerator
*Generated on Wed Dec 10 14:47:40 EST 2008
***/
abstract class XxLecturer_gen extends SRecordInstance implements java.io.Serializable {

   public static final SRecordMeta <XxLecturer> meta = new SRecordMeta<XxLecturer>(XxLecturer.class, "XX_LECTURER");

//Columns in table
   public static final SFieldString fldPersonId =
      new SFieldString(meta, "PERSON_ID", 20,
         new SFieldFlags[] { SFieldFlags.SPRIMARY_KEY, SFieldFlags.SMANDATORY });

   public static final SFieldString fldPname =
      new SFieldString(meta, "PNAME", 40);

   public static final SFieldString fldPhoneNr =
      new SFieldString(meta, "PHONE_NR", 20);

   public static final SFieldDouble fldXxSalary =
      new SFieldDouble(meta, "XX_SALARY");

//Column getters and setters
   public String get_fldPersonId(){ return getString(fldPersonId);}
   public void set_fldPersonId( String value){setString( fldPersonId,value);}

   public String get_fldPname(){ return getString(fldPname);}
   public void set_fldPname( String value){setString( fldPname,value);}

   public String get_fldPhoneNr(){ return getString(fldPhoneNr);}
   public void set_fldPhoneNr( String value){setString( fldPhoneNr,value);}

   public BigDecimal get_fldXxSalary(){ return getBigDecimal(fldXxSalary);}
   public void set_fldXxSalary( BigDecimal value){setBigDecimal( fldXxSalary,value);}

//Find and create
   public static XxLecturer findOrCreate( SSessionJdbc ses ,String _fldPersonId ){
      return ses.findOrCreate(meta, new Object[] {_fldPersonId});
   }
//specializes abstract method
   public SRecordMeta getMeta() {
       return meta;
   }
}
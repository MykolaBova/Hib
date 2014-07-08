package simpleorm.examples;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldReference;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import simpleorm.examples.SubTypeTest.Student;
import simpleorm.sessionjdbc.SSessionJdbc;

/**
 * Demonstrates Simple subtypes.  Lecturers and Students are People.
 * (Actually more like delegation, multiple "supertypes" are supported.)<p>
 * @author aberglas
 */
public class SubTypeTest {

   	static public class PersonMeta {
		public SFieldString PERSON_ID;         
		public SFieldString NAME;
		public SFieldString PHONE_NR;
        
        PersonMeta(SRecordMeta meta) {
            PERSON_ID = new SFieldString(meta,	"PERSON_ID", 20, SFieldFlags.SPRIMARY_KEY);
            NAME = new SFieldString(meta, "PNAME", 40, SFieldFlags.SDESCRIPTIVE);
            PHONE_NR = new SFieldString(meta,	"PHONE_NR", 20);
        }            
    }
    
   	static public class Lecturer extends SRecordInstance  {
		public static final SRecordMeta<Lecturer>  meta = new SRecordMeta(Lecturer.class, "XX_LECTURER");
        public static final PersonMeta PERSON = new PersonMeta(meta);
		public static final SFieldDouble SALARY = new SFieldDouble(meta, "XX_SALARY");
     	@Override public SRecordMeta getMeta() {
                return meta;
		};               
    }

    static public class Student extends SRecordInstance  {
		public static final SRecordMeta<Student>  meta = new SRecordMeta(Student.class, "XX_STUDENT");
        public static final PersonMeta PERSON = new PersonMeta(meta);
        public static final SFieldString LECTURER_ID  = new SFieldString(meta, "LECTURER_ID", 20);
    	static final SFieldReference<Lecturer> LECTURER
            = new SFieldReference(meta, Lecturer.meta, "LECTURER", LECTURER_ID);
		public static final SFieldString GRADE = new SFieldString(meta,	"SGRADE", 20);
     	@Override public SRecordMeta getMeta() {
                return meta;
		};               
    }

    static void subtypeTest() {
  		SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		ses.begin();
        
        Lecturer prof = ses.create(Lecturer.meta, "L123");
        prof.setString(prof.PERSON.NAME, "The Prof");
        prof.setDouble(prof.SALARY, 123.45);
        
        Student fred = ses.create(Student.meta, "S345");
        fred.setString(fred.PERSON.NAME, "Fred");
        fred.setReference(fred.LECTURER, prof);
        
        ses.commit();
        ses.begin();
        
        Student fred2 = ses.find(Student.meta, "S345");
        Lecturer prof2 = fred2.findReference(fred2.LECTURER);
        TestUte.assertEqual("The Prof", prof2.getString(prof2.PERSON.NAME));
        ses.commit();
        
    }
    	public static void main(String[] argv) throws Exception {
		TestUte.initializeTest(ValidationTest.class); // Look at this code.
		try {
           SSessionJdbc ses = SSessionJdbc.getThreadLocalSession();
		   ses.begin();

    	  // / Delete any old data from a previous run.
          TestUte.dropAllTables(ses);

		  ses.rawUpdateDB(ses.getDriver().createTableSQL(Lecturer.meta));
		  ses.rawUpdateDB(ses.getDriver().createTableSQL(Student.meta));

		  ses.commit();

		  subtypeTest();
		} finally {
			SSessionJdbc.getThreadLocalSession().close();
		}
	}

}

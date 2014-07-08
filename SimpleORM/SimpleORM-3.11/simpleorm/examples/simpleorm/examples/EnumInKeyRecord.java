package simpleorm.examples;

import simpleorm.dataset.SFieldDouble;
import simpleorm.dataset.SFieldEnum;
import simpleorm.dataset.SFieldString;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.SRecordMeta;
import simpleorm.dataset.SFieldFlags;
import static simpleorm.dataset.SFieldFlags.*;


/** This test class defines a table with a SFieldEnum in key */
public class EnumInKeyRecord extends SRecordInstance {

	private static final long serialVersionUID = 1L;
	
	public enum TypeTestEnum {TYPE_A, TYPE_B, TYPE_C};

	public static final SRecordMeta<EnumInKeyRecord> META 
        = new SRecordMeta(EnumInKeyRecord.class, "XX_ENUM_IN_KEY");

	// ie. SRecord objects describe SRecordInstances

	public static final SFieldString REC_ID 
    = new SFieldString(META, "REC_ID", 10, SPRIMARY_KEY);

	public static final SFieldEnum REC_TYP 
		= new SFieldEnum<TypeTestEnum>(META, "REC_TYP", TypeTestEnum.class, SPRIMARY_KEY);

	public static final SFieldString REC_NAME 
        = new SFieldString(META, "REC_NAME", 40, SDESCRIPTIVE);


	public @Override SRecordMeta<EnumInKeyRecord> getMeta() {
		return META;
	};
}

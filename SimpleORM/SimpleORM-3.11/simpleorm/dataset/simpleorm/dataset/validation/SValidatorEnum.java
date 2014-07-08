package simpleorm.dataset.validation;

import simpleorm.dataset.SFieldEnum;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;
import simpleorm.utils.SUte;

public class SValidatorEnum<T extends Enum<T>> extends SValidatorI {

	private static final long serialVersionUID = 20083L;
	
	private Class<T> enumClass = null;
	private SFieldEnum<T> fieldEnum = null;
	
	public SValidatorEnum(Class<T> enumCl) {
		enumClass = enumCl;
	}
	
	public SValidatorEnum(SFieldEnum<T> field) {
		fieldEnum = field;
	}
	
	@Override public void onValidate(SFieldMeta field, SRecordInstance instance)  { 
		// always validate if null (except not null validator of course)
		if (instance.isNull(field)) return;
		
		boolean valid = false;
		String val = instance.getString(field);
		if (enumClass == null) {
			if (fieldEnum == null) {
				throw new SException.Error("At least enumClass or fieldEnum must be not null " + field);
			}
			enumClass = fieldEnum.getEnumClass();
		}
		T[] consts = enumClass.getEnumConstants();
		for (T con : consts) {
			if (con.name().equals(val)) {
				valid = true;
			}
		}
		if ( ! valid) 
            throw new SException.Validation(
                "Field "+ field + " value " + val + " not in " + SUte.arrayToString(consts), (Object)consts);
	}
	
	public Class<T> getEnumClass() {
		return enumClass;
	}
}

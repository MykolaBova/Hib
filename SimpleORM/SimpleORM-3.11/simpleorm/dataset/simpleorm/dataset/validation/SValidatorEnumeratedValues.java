package simpleorm.dataset.validation;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;
import simpleorm.utils.SUte;

public class SValidatorEnumeratedValues extends SValidatorI {

	private static final long serialVersionUID = 20083L;
	
	private Object[] validValues = null;
	
	public SValidatorEnumeratedValues( Object... values ) {
		validValues = values;
	}
    
	@Override public void onValidate(SFieldMeta field, SRecordInstance instance)  {
		// always validate if null (except not null validator of course)
		if (instance.isNull(field)) return;
		
		String val = instance.getString(field);

		for (Object vv : validValues) {
			if (vv.equals(val)) return;
		}
		throw new SException.Validation(
            "Field "+field + " value " + val + " not in " + SUte.arrayToString(validValues), 
            (Object)validValues); // (Object) cast needed for ... args.
	}
	    
    public Object[] getValidValues() {
        return validValues;
    }

    public void setValidValues(Object... validValues) {
        this.validValues = validValues;
    }

}

package simpleorm.dataset.validation;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SFieldScalar;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;

public class SValidatorMaxLength extends SValidatorI {

	/**
	 * Confirms that the field size is max lengh, which defaults to the declared field size. 
	 */
	private static final long serialVersionUID = 20083L;
	private int maxLength = -1;
	
    public int getMaxLength() {
        return maxLength;
    }
    public SValidatorMaxLength setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        return this;
    }
    
	@Override public void onValidate(SFieldMeta field, SRecordInstance instance)  {
		// always validate if null (except not null validator of course)
		if (instance.isNull(field)) return;
        int len = maxLength;
        if (len < 0) len = ((SFieldScalar)field).getMaxSize();
		String val = instance.getString(field);
	    if (len < (val == null ? 0 : val.length()))
            throw new SException.Validation(
                "Param too long (>" + len + ") for field "+field+" and value "+val);
	}
}

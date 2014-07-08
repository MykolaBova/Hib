package simpleorm.dataset.validation;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;

public class SValidatorNotNull extends SValidatorI {

	private static final long serialVersionUID = 20083L;

	@Override
	public void onValidate(SFieldMeta field, SRecordInstance instance)  {
		
		if (instance.isNull(field)) 
            throw new SException.Validation("Field "+ field +" must be NOT NULL");
	}
}

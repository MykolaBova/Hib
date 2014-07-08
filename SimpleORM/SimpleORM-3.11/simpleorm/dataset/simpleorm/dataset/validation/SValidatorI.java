package simpleorm.dataset.validation;

import java.io.Serializable;
import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;

public abstract class SValidatorI implements Serializable {

	private static final long serialVersionUID = 20083L;
     
    /** Called when individual field changed, or when record updated.
      * (Default behaviour is to call all registered validators.)
      * @see SRecordInstance#onValidateField
      */
	public abstract void onValidate(SFieldMeta field, SRecordInstance instance) throws SException.Validation;
    
}

package simpleorm.dataset.validation;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.utils.SException;
import simpleorm.utils.SUte;

/**
 * Validates that a numeric value is greater than or equal to a given value, normally 0.
 * @author aberglas
 */
public class SValidatorGreaterEqual  extends SValidatorI {
  double minVal;

    public SValidatorGreaterEqual(double minVal) {
        this.minVal = minVal;
    }
    
    @Override public void onValidate(SFieldMeta field, SRecordInstance instance)  {
		// always validate if null (except not null validator of course)
		if (instance.isNull(field)) return;
	        
        double dval = instance.getDouble(field);

        if (dval < minVal)
    		throw new SException.Validation(
              "Field "+field + " value " + dval + " < " + minVal, minVal);
	}


    public double getMinVal() {
        return minVal;
    }

    public SValidatorGreaterEqual setMinVal(double minVal) {
        this.minVal = minVal;
        return this;
    }
  
    
}

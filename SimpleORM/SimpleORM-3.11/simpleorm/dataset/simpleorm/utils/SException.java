package simpleorm.utils;

import simpleorm.dataset.SFieldMeta;
import simpleorm.dataset.SRecordInstance;
import simpleorm.dataset.validation.SValidatorI;


/**
 * SimpleORM takes care to trap most error conditions and throw meaningful
 * exception messages. Unlike most other Java code, they always include
 * information about which objects were being processed at the time, eg. the
 * primary key of the current record. We strongly encourage this practice as it
 * greatly facilitates maintenance and debugging.
 * <p>
 * 
 * Subclasses are actually thrown, namely <code>SException.Error</code> to
 * indicate probable bugs in the user's program,
 * <code>SException.InternalError</code> to indicate bugs in SimpleOrm,
 * <code>SException.Jdbc</code> for chained JDBC errors,
 * <code>SException.Data</code> for errors that probably the result of
 * run-time data problems. <code>SException.Test</code> is used to indicate
 * failed test cases.
 * <p>
 * 
 * Other well defined exceptions that a user may want to trap are given explicit
 * subclasses (eg. <code>SRecordInstance.BrokenOptimisticLockException</code>).
 * <p>
 * 
 * SimpleORM only throws SExceptions which extend RuntimeException and so avoid
 * the need to clutter the code with unnecessary try/catch blocks. The only
 * reason to use try/catch when using SimpleORM is because you <i>really</i>
 * want to catch an error.
 * <p>
 * 
 * (The author stongly disagrees with the way that Java forces one to declare
 * and catch exceptions because it encourages mediocre programmers to hide
 * exceptions which is a very nasty practice. (Even Sun programmers do this
 * quite regularly in the Java libraries, eg. <code>System.out.println()</code>!)
 * It also clutters the code for no good reason. IMHO Java's "checked"
 * exceptions add no value whatsoever in practice.)
 * <P>
 */

public abstract class SException extends RuntimeException {
    Class<SValidatorI> validator;
    SRecordInstance instance;
    SFieldMeta fieldMeta;
    Object fieldValue;
    Object[] params;
    

	public SException(String message, Throwable nestedException) {
        super(message, nestedException);
	}

	public SException(String message) {
        super(message);
        // Java does not allow this(message, this) !
	}

	public SException(Throwable nestedException) {
        super(nestedException);
	}

	/** Avoid using this one, include message. */
	public SException() {
		super();
	}

////////// empty get/set ////////////
    public SRecordInstance getRecordInstance() {
        return instance;
    }
    public SException setRecordInstance(SRecordInstance instance) {
        this.instance = instance;
        return this;
    }
    public SFieldMeta getFieldMeta() {
        return fieldMeta;
    }
    public SException setFieldMeta(SFieldMeta fieldMeta) {
        this.fieldMeta = fieldMeta;
        return this;
    }
    public SRecordInstance getInstance() {
        return instance;
    }
    public SException setInstance(SRecordInstance instance) {
        this.instance = instance;
        return this;
    }
    public Object getFieldValue() {
        return fieldValue;
    }
    public SException setFieldValue(Object fieldValue) {
        this.fieldValue = fieldValue;
        return this;
    }
    public Object[] getParams() {
        return params;
    }
    public SException setParams(Object... params) {
        this.params = params;
        return this;
    }
    public Class<SValidatorI> getValidator() {
        return validator;
    }
    public SException setValidator(Class<SValidatorI> validator) {
        this.validator = validator;
        return this;
    }

  
    	
	/** Probable bug in user's program. */
	public static class Error extends SException {
		static final long serialVersionUID = 20083;

		public Error(String message, Throwable nestedException) {
			super(message, nestedException);
		}

		public Error(String message) {
			super(message);
		}

		public Error(Throwable nestedException) {
			super(nestedException);
		}

		public Error() {
			super();
		}
	}

	/** Probable bug in SimpleORM. */
	public static class InternalError extends SException {
		static final long serialVersionUID = 20083;

		public InternalError(String message, Throwable nestedException) {
			super(message, nestedException);
		}

		public InternalError(String message) {
			super(message);
		}

		public InternalError(Throwable nestedException) {
			super(nestedException);
		}

		public InternalError() {
			super();
		}
	}

	/**
	 * Chained JDBC Exception, could be anything as JDBC does not separate
	 * exceptions out and provides minimal information about their underlying
	 * causes.
	 */
	public static class Jdbc extends SException {
		static final long serialVersionUID = 20083;

		public Jdbc(String message, Throwable nestedException) {
			super(message, nestedException);
		}

		public Jdbc(String message) {
			super(message);
		}

		public Jdbc(Throwable nestedException) {
			super(nestedException);
		}

		public Jdbc() {
			super();
		}
	}

	/**
	 * Errors in data or environment at runtime, not necessarily a programming
	 * bug.
	 */
	public static class Data extends SException {
		static final long serialVersionUID = 20083;

		public Data(String message, Throwable nestedException) {
			super(message, nestedException);
		}

		public Data(String message) {
			super(message);
		}

		public Data(Throwable nestedException) {
			super(nestedException);
		}

		public Data() {
			super();
		}
	}

/**
 * This Exception thrown by user written code to indicate user data entry
 * errors, especially in the business rule methods. The idea is that the calling
 * method can trap them and present neat messages to the user. Do not use this
 * exception for mysterious internal errors, only for well defined user errors
 * that do not require a stack trace.
 * <p>
 * 
 */
    static public class Validation extends SException {

        static final long serialVersionUID = 20083;

        /** message is formatted with MessageFormat cons(inst, params). 
              Details such as instance, field, validator class are automatically appended
              by the caller of the method
         */
        public Validation(String message, Object... params) {
            super(message);
        }
    }

	/** Exception thrown due to failed unit test cases. */
	public static class Test extends SException {
		static final long serialVersionUID = 20083;

		public Test(String message, Throwable nestedException) {
			super(message, nestedException);
		}

		public Test(String message) {
			super(message);
		}

		public Test(Throwable nestedException) {
			super(nestedException);
		}

		public Test() {
			super();
		}
	}
}

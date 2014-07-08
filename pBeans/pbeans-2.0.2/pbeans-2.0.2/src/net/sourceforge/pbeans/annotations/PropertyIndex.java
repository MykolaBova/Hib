package net.sourceforge.pbeans.annotations;

import java.lang.annotation.*;

/**
 * This annotation can be part of an array with which
 * {@link PersistentClass#indexes()} is set. 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyIndex {
	boolean unique();
	String[] propertyNames();
	
	/**
	 * Number of characters used in index for string fields.
	 * This value is optional and whether it is actually used
	 * depends on the underlying database implementation. 
	 * Sometimes it's necessary to 
	 * set it if indexes can't be created due to key length.
	 */
	int keyLength() default 0;
}


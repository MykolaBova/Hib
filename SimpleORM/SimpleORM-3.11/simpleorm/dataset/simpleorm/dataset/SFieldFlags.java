package simpleorm.dataset;

/**
 * Defines aspects of fields.
 * PRIMARY_KEY: part of primary key
 * MANDATORY: Not Null
 * NOT_OPTIMISTIC_LOCKED  Not added to WHERE clause for optimistic locking.  
      To stop types like Oracle LONG VARCHAR ending up in where clause.  (was named UNCHECKED)
 * ASCHAR,  Create string as CHAR, not VARCHAR
 * DESCRIPTIVE: Include in selects where SSelectMode.DESCRIPTIVE
 * UNQUERIED: Exclude from selects unless SSelectMode.ALL
 */
public enum SFieldFlags {
	SPRIMARY_KEY,
	SMANDATORY,
	SNOT_OPTIMISTIC_LOCKED, // was named UNCHECKED.  To stop types like Oracle LONG VARCHAR ending up in where clause.
	SDESCRIPTIVE,
	SUNQUERIED































































}

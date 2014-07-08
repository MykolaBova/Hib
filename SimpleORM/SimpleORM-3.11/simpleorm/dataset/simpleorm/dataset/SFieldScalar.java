package simpleorm.dataset;

import static simpleorm.dataset.SFieldFlags.SPRIMARY_KEY;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import simpleorm.dataset.validation.SValidatorI;
import simpleorm.dataset.validation.SValidatorNotNull;
import simpleorm.utils.SException;
/*
 * Copyright (c) 2002 Southern Cross Software Queensland (SCSQ).  All rights
 * reserved.  See COPYRIGHT.txt included in this distribution.
 */


/** Scalar fields such as String, Int, but not References to other tables.
 * Only scalars are actually stored in tables.  
 * It is scalars that are marked as primary keys etc.
 */
public abstract class SFieldScalar extends SFieldMeta {

    /**
     * A list of all the SFieldReferences that refer to this field, which must be a Foreign Key field.
     * (The same foreign key can be used for multiple, overlapping references, but only once in a given reference.)
     * Used to null references if the fkey field is updated.
     */
    // TODO should it be transient ?
    private List<SFieldReference> references = new ArrayList<SFieldReference>();
    
    private int maxSize = -1; // -1 mean no maxSize
    
    public String sqlDataTypeOverride;
    
    private SGeneratorMode generatorMode = null;
    private Object[] generatorParameters;
    public Object theGenerator;
    
    private Object initialValue = null;


    public SFieldScalar(SRecordMeta sRecord, String fieldName,  SFieldFlags... pvals) {
        super(sRecord, fieldName, pvals);
        if ( this.isMandatory() ) { 
            addValidator(new SValidatorNotNull());
        }
  		if (this.isPrimary() && this.isUnqueried()) {
			throw new SException.Error("Cannot be primary and unquiried "+this);
		}

    }

    /** If set, this overrides the entire column data type string generated in the CREATE TABLE string */
    public <T extends SFieldScalar> T overrideSqlDataType(String dataType) {
        sqlDataTypeOverride = dataType;
        return (T)this;
    }
    /** The default data type if not overriden by overrideSqlDataType.
    This is only called when the data type is actually needed by CREATE TABLE,
    ie if no sqlDataTypeOverride is specified. 
     */
    public abstract String defaultSqlDataType();
    
    /**
     * @return int constant from java.sql.Types, used for setting sql datatype with null values
     */
    public abstract int javaSqlType();

    public boolean isMandatory() {
        return getFlags().contains(SFieldFlags.SMANDATORY);
    }

    public boolean isGenerated() {
        //return getFlags().contains(SScalarFlags.GENERATED);
        return this.generatorMode != null;
    }
    
    public boolean isPrimary() {
        return getFlags().contains(SPRIMARY_KEY);
    }
    
    public boolean isNotOptimisticLocked() {
        return getFlags().contains(SFieldFlags.SNOT_OPTIMISTIC_LOCKED);
    }

    /** The column name, which for now is the same as the field name but that may change. */
    public String getColumnName() {
        return getFieldName();
    }
    
    public List<SFieldReference> getReferences() {
        return Collections.unmodifiableList(references);
    }

    public @Override  String toLongerString() {
        return "[" + this + (isPrimary() ? " PKey" : "") + (isForeignKey() ? " FKey" : "") + "]";
    }

    void addReference( SFieldReference ref) {
        //referencesAndKeys.put(ref, pkey);
        references.add(ref);
    }

    /**
     * Is this field part of at least one forign key ?
     */
    public boolean isForeignKey() {
        return !references.isEmpty();
    }

    void setRawFieldValue(SRecordInstance instance, Object value) {

        // Set refed fields to null when fkey is overlapping
        // This is not called if value not changed, so spurious clearing should not happen.
        // (It matters if the records are detached, and so cannot be re-got later.)
        for (SFieldReference ref : this.getReferences()) {
            ref.clearOverlappedForeignKey(instance);
        }
        instance.setRawArrayValue(this, value);
    }

    Object getRawFieldValue(SRecordInstance finst, SQueryMode queryMode, SSelectMode selectMode) {
        if (!finst.isValid(this))
            throw new SException.Error("Cannot get unretrieved/unset field " + finst + "." + this);
        return finst.getRawArrayValue(this);
    }

    /**
     * Check if two fields can be used on both sides of a foreign key
     * relation (same type, length, ...)
     */
    abstract boolean isFKeyCompatible(SFieldScalar field);
    
        /**
     * Sets the generator associated to this field. This will make the field
     * generated, reference the generator for future use and **remove any NotNullValidaotr***
     * (so if you want to add one, do it after you call setGenerator)
     * Also clears the Generator Object.
     * @param <T>
     * @param gen
     * @return
     */
    public <T extends SFieldScalar> T setGeneratorMode(SGeneratorMode gen, Object... gParams) {
        this.generatorMode = gen;
        setGenerator(null);
        generatorParameters = gParams;
        List<SValidatorI> validators = getValidators();
        Iterator<SValidatorI> it = validators.iterator();
        while (it.hasNext()) {
        	SValidatorI val = it.next();
        	if (val instanceof SValidatorNotNull)
        		it.remove();
        }
        return (T) this;
    }

    public SGeneratorMode getGeneratorMode() {
        return generatorMode;
    }

    public Object[] getGeneratorParameter() {
        return generatorParameters;
    }

    /** Just gets the current generator.  
     * SGenerator.setNewGenerator should have been called first to set it.
     * (The DataSet layer does not know about gerators as such, hence the separate step.)
     * @See getGeneratorMode, which just gets the mode as set by the user.
     */
    public <Generator extends Object> Generator getGenerator() {
        return (Generator)theGenerator;
    }

    public void setGenerator(Object theGenerator) {
        this.theGenerator = theGenerator;
    }

        
    protected void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
    public int getMaxSize() {
        return maxSize;
    }
    
    /** Set the initial value for this field. This value
     * will be used as the initial value of the field when a
     * new record is initially created. After that, the value
     * won't be used anymore (we won't try to replace null values or
     * things like that...).
     * Setting initialValue on a field does not make it dirty.
     * Also notice that the initial value is not reflected in DDL generation (for now).
     * @param ival the initial value. Type must match with the internal type
     * used for this field
     * @return this
     */
    public <T extends SFieldScalar> T  setInitialValue(Object ival) {
    	this.initialValue = ival;
    	return (T) this;
    }
    /** Return the initial value of this field, or null
     * @see setInitialValue
     */
    public Object getInitialValue() {
    	return this.initialValue;
    }
    
    /**
     * Compare the value of this field between the two instances passed as parameters.
     * Used to compare instances.
     * @param inst
     * @param other
     * @return  a negative int if inst.getObject(this) < other.getObject(this),
     * 0 if inst.getObject(this).equals(other.getObject(this))
     * and a positive int if inst.getObject(this) > other.getObject(this).
     * Null is greater than any other value.
     * Two null values are declared equal (return 0)

     */
    public abstract int compareField(SRecordInstance inst, SRecordInstance other); 

}

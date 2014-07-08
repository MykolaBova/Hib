package simpleorm.dataset;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Map;

import simpleorm.utils.SException;

public abstract class SRecordGeneric implements Map<String, Object> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Use isNull to check if a field is null. Do not rely on get() returning null, nor on getLong returnng 0, etc...
	 * @param field
	 * @return true is the field is set and has a null value.
	 */
	abstract public boolean isNull(String field);
	
	abstract public void setNull(String key);
	
	/**
	 * Convinience method to get a field from a SFieldMeta.
	 * Like getObject() in SRecordInstance, but will only use the field name, so will work for SRecordTransient.
	 * @param fmeta
	 * @return
	 */
	public Object get(SFieldMeta fmeta) {
		return get(fmeta.getFieldName());
	}
	
	/**Get the value as a double
	 * Returns 0 for null, following jdbc, use isNull() to check the real value is necessary
	 */
	public double getDouble(String field) {
		try {
			return convertToDouble(this.get(field));
		} catch (Exception ex) {
			throw new SException.Data("Could not convert " + field + " to double " + this.getDouble(field), ex);
		}
	}
	
	protected double convertToDouble(Object val) throws NumberFormatException {
		double result = 0;
   		if (val == null)
			return 0;
		if (val instanceof Number)
			result = ((Number) val).doubleValue();
		else {
			result = Double.parseDouble(val.toString());
		}
		return result;
	}
	/**Get the value as a Integer if possible 
	 * Returns 0 for null, following jdbc, use isNull() to check the real value is necessary
	 */
	public Integer getInt(String field) {
		Object val = this.get(field);
		try {
			return convertToInt(val);
		} catch (Exception ex) {
			throw new SException.Data("Could not convert " + field + " to int " + val, ex);
		}
	}
	
	protected int convertToInt(Object val) throws NumberFormatException {
		int result = 0;
		if (val == null)
			return 0;
		if (val instanceof Number)
			result = ((Number) val).intValue();
		else {
			result = Integer.parseInt(val.toString());
		}
		return result;
	}
	/**Get the value as a Long if possible 
	 * Returns 0 for null, following jdbc, use isNull() to check the real value is necessary
	 */
	public long getLong(String field) {
		Object val = this.get(field);
		try {
			return convertToLong(val);
		} catch (Exception ex) {
			throw new SException.Data("Could not convert " + field + " to long " + val, ex);
				//.setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}
	
	protected long convertToLong(Object val) throws NumberFormatException {
		long result = 0;
		if (val == null)
			return 0;
		if (val instanceof Number)
			return ((Number) val).longValue();
		else {
			result = Long.parseLong(val.toString());
		}
		return result;
	}
	
	/**Get the value as a float if possible 
	 * Returns 0 for null, following jdbc, use isNull() to check the real value is necessary
	 * @throws SException.Data if impossible
	 */
	public float getFloat(String field) {
		Object val = this.get(field);
		try {
			return convertToFloat(val);
		}
		catch (NumberFormatException e) {
			throw new SException.Data("Could not convert " + field + " to long " + val, e);
			//.setFieldMeta(field).setRecordInstance(this).setParams(val);
		}
	}
	
	protected float convertToFloat(Object val) throws NumberFormatException {
		float result = 0;
		if (val == null)
			return 0;
		if (val instanceof Number)
			result = ((Number) val).floatValue();
		else {
			result = Float.valueOf(val.toString());
		}
		return result;
	}
	
	/**Get the value as a BigDecimal if possible 
	 * @throws SException.Data if impossible
	 */
	public BigDecimal getBigDecimal(String field) {
		Object value = this.get(field);
		try {
			return convertToBigDecimal(value);
		}
		catch (NumberFormatException e) {
			throw new SException.Data(value + " cannot be converted to BigDecimal.");
		}
	}
	
	protected BigDecimal convertToBigDecimal(Object value) throws NumberFormatException {
		if (value instanceof BigDecimal)
			return (BigDecimal) value;
		else if (value instanceof Number)
			return new BigDecimal(((Number) value).doubleValue());
		else if (value instanceof String)
			return new BigDecimal((String) value);
		else
			throw new NumberFormatException();
	}
	
	/**Get the value as a String.
	 */
	public String getString(String field) {
		return convertToString(get(field));
	}
	
	protected String convertToString(Object val) {
		if (val == null)
			return null;
		String str = val.toString();
		int end = str.length() - 1;
		int sx = end;
		for (; sx > -1 && str.charAt(sx) == ' '; sx--)
			;
		if (sx != end)
			return str.substring(0, sx + 1);
		else
			return str;
	}
	
	/**
	 * Returns false if null.
	 */
	public boolean getBoolean(String field) {
		try {
			Object val = get(field);
			return val == Boolean.TRUE;
		} catch (SException.Error er) {
			throw er;
		}
	}
	
	public java.sql.Timestamp getTimestamp(String field) {
		Object val = get(field);
		try {
			return convertToTimestamp(val);
		}
		catch (ClassCastException e) {
			throw new SException.Data(val + " cannot be converted to TimeStamp.");
		}
	}
	
	protected Timestamp convertToTimestamp(Object val) throws ClassCastException {
		if (val == null) return null;
		if (val instanceof java.sql.Timestamp) {
			return (java.sql.Timestamp) val;
		}
		else { // might be oracle.sql.Date or oracle.sql.Timestamp...
	 		       // We don't won't to depend on oracle driver, so use reflection
			try {
				Method meth = val.getClass().getMethod("timestampValue");
				return (java.sql.Timestamp) meth.invoke(val);
			}
			catch (Exception e) {
				throw new ClassCastException(val + " cannot be converted to TimeStamp.");
			}
		}
	}
	
	public java.sql.Date getDate(String field) {
		Object raw = get(field);
		try {
			return convertToDate(raw);
		}
		catch (ClassCastException e) {
			throw new SException.Data(raw + " cannot be converted to Date.");
		}
	}
	
	protected java.sql.Date convertToDate(Object val) throws ClassCastException {
		if (val == null) return null;
		if (val instanceof java.sql.Date) {
			return (java.sql.Date) val;
		}
		else { // might be oracle.sql.Date or oracle.sql.Timestamp...
			       // We don't won't to depend on oracle driver, so use reflection
			try {
				Method meth = val.getClass().getMethod("dateValue");
				return (java.sql.Date) meth.invoke(val);
			}
			catch (Exception e) {
				throw new ClassCastException(val + " cannot be converted to Date.");
			}
		}
	}
	
	public java.sql.Time getTime(String field) {
		Object raw = get(field);
		try {
			return convertToTime(raw);
		}
		catch (ClassCastException e) {
				throw new SException.Data(raw + " cannot be converted to Time.", e);
		}
	}
	
	protected java.sql.Time convertToTime(Object raw) throws ClassCastException {
		if (raw == null) return null;
		if (raw instanceof java.sql.Time) {
			return (java.sql.Time) raw;
		}
		else {  // might be oracle.sql.Date or oracle.sql.Timestamp...
					// We don't won't to depend on oracle driver, so use reflection
			try {
				Method meth = raw.getClass().getMethod("timeValue");
				return (java.sql.Time) meth.invoke(raw);
			}
			catch (Exception e) {
				throw new ClassCastException(raw + " cannot be converted to Time.");
			}
		}
	}
	
	/** Casts get() to byte[]. */
	public byte[] getBytes(String field) {
		try {
			Object val = get(field);
			return (byte[]) val;
		} catch (SException.Error er) {
			throw er;
		}
	}
}

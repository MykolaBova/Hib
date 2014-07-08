package simpleorm.dataset;

/**
 * Booleans are represented as Boolean.TRUE/FALSE internally. However, SQL89
 * does not have a concept of boolean, only strings and numbers. So these are
 * normally mapped to strings like "Y", "N". Which representation is determined
 * in subclasses. (The JDBC java docs are useless as always "getBoolean gets
 * booleans...").
 * <p>
 * 
 * (The issue is with JDBC, not SimpleORM. And of course SFieldObject can always
 * be used if really needed.)
 */

public abstract class SFieldBoolean extends SFieldScalar {
	public SFieldBoolean(SRecordMeta meta, String columnName,
			SFieldFlags... pvals) {
		super(meta, columnName, pvals);
	}

// Removed -- too many I18N issues.  This needs to be done at an outer level.
//	/**
//	 * Converts from the users external representation to the internal
//	 * representation stored in SRecordInstance. This is deliberately quite
//	 * permissive, and should work even if the external world is a bit
//	 * inconsistent.
//	 */
//	protected Boolean convertExternalToBoolean(Object raw) {
//		if (raw == null)
//			return null;
//		else if (raw instanceof Boolean)
//			return (Boolean) raw;
//		else if (raw instanceof String) {
//			String s = (String) raw;
//			if ("t".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s)
//					|| "y".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s))
//				return Boolean.TRUE;
//			else if ("f".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s)
//					|| "n".equalsIgnoreCase(s) || "no".equalsIgnoreCase(s))
//				return Boolean.FALSE;
//			else
//				throw new SException.Error("String \"" + s
//						+ "\" could not be converted to a java.lang.Boolean");
//		} else if (raw instanceof Number) {
//			int i = ((Number) raw).intValue();
//			return i != 0 ? Boolean.TRUE : Boolean.FALSE;
//		} else
//			throw new SException.Error("Cannot Convert '" + raw
//					+ "' to Boolean.");
//	}
}

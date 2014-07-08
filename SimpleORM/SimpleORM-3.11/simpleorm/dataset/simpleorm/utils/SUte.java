package simpleorm.utils;

import simpleorm.dataset.SRecordMeta;

/**
 * Miscellaneous static utility methods that have nowhere else to live.
 */
public class SUte {

    /**
     * Depricated! ` True if <code>element</code> is in <code>bitSet</code>.
     * Throws exception if lower short does not match the <code>setType</code>
     * indicating mismatched flags.
     */
    public static boolean inBitSet(long bitSet, long element, long setType) {
        if ((bitSet != 0 && (bitSet & 0xFFFF) != (setType & 0xFFFF)) || ((element & 0xFFFF) != (setType & 0xFFFF))) {
            throw new SException.Error("Mismatched BitSet " + bitSet + ", " + element + " vs " + setType);
        }
        return (bitSet & element & 0xFFFFFFFFFFFF0000L) != 0;
    }

    /** Pretty class names without package prefixes etc. */
    public static String cleanClass(Class<?> cls) {
        if (cls == null) {
            return "NullClass";
        }
        String className = cls.getName();
        int x1 = className.lastIndexOf(".") + 1;
        int x2 = className.lastIndexOf("$") + 1;
        return className.substring(x1 > x2 ? x1 : x2);
    }

    /**
     * Makes a string out of elements of an array for tracing. If array is not
     * an object just toString it.
     */
    public static String arrayToString(Object array) {
		if (array == null)
            return "{NULL}";
		if (!(array instanceof Object[]))
            return array.toString();
        Object[] realArray = (Object[]) array;
        StringBuffer sb = new StringBuffer("{");
        for (int ax = 0; ax < realArray.length; ax++) {
			if (ax > 0)
                sb.append(", ");
            sb.append(realArray[ax] + "");
        }
        return sb.toString() + "}";
    }

    /**
     * The SimpleORM Version. Major.Minor. Guaranteed upward compatibility
     * within all major versions (except 00.*!). Guaranteed that these will sort
     * properly, hence leading zeros.
     * 
     * Is not a constant because Java will copy the constant into any dependent
     * modules !!!
     */
    public static String simpleormVersion() {
        return "03.11";
    }

    public static boolean trimStringEquals(Object firstObj, Object secondObj) {
		if (!(firstObj instanceof String))
            return firstObj.equals(secondObj);
        String first = (String) firstObj, second = (String) secondObj;
		if (second == null)
            return false;
        int fx = first.length() - 1, sx = second.length() - 1;
        boolean trimed = false;
		for (; fx > -1 && first.charAt(fx) == ' '; fx--)
            trimed = true;
		for (; sx > -1 && second.charAt(sx) == ' '; sx--)
            trimed = true;
		if (fx != sx)
            return false;
		if (!trimed)
            return first.equals(second); // Optimization
		for (; fx > -1; fx--)
			if (first.charAt(fx) != second.charAt(fx))
                return false;
        return true;
    }
    
    /** @deprecated See SFieldReference.toLongerString */
    @Deprecated public static String allFieldsString(SRecordMeta<?> meta) {
        return meta.toLongerString();
    }
}

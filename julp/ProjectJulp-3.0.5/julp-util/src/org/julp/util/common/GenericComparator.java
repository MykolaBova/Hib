package org.julp.util.common;

import java.lang.reflect.Method;
import java.io.Serializable;
import java.util.Comparator;
import java.lang.reflect.InvocationTargetException;

public class GenericComparator implements Comparator, Serializable {

    private static final long serialVersionUID = -3218074439846996936L;
    protected String methodName = null;
    protected Class[] parmClass = null;
    protected Object[] parms = null;
    protected boolean reverse = false;
    /** If compared Objects are not instanceof String this is ignored */
    protected boolean caseInsensitiveOrder;

    public GenericComparator() {
    }

    public GenericComparator(String methodName) {
        this.methodName = methodName;
        this.parmClass = new Class[0];
    }

    public GenericComparator(String methodName, boolean reverse) {
        this.methodName = methodName;
        this.parmClass = new Class[0];
        this.reverse = reverse;
    }

    public GenericComparator(String methodName, Object[] parms) {
        this.methodName = methodName;
        this.parmClass = new Class[parms.length];
        this.parms = parms;
        for (int i = 0; i < parms.length; i++) {
            this.parmClass[i] = parms.getClass();
        }
    }

    public GenericComparator(String methodName, Object[] parms, boolean reverse) {
        this.methodName = methodName;
        this.parmClass = new Class[parms.length];
        this.reverse = reverse;
        this.parms = parms;
        for (int i = 0; i < parms.length; i++) {
            this.parmClass[i] = parms.getClass();
        }
    }

    @Override
    public int compare(Object o1, Object o2) {
        Method objMethod = null;
        Object object1 = null;
        Object object2 = null;
        if (!(o1.getClass().isInstance(o2))) {
            throw new ClassCastException("Classes do not match "
                    + o1.getClass().getName() + " : " + o2.getClass().getName());
        }
        try {
            objMethod = o1.getClass().getMethod(methodName, parmClass);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Method name: " + this.methodName
                    + " not found in class " + o1.getClass().getName());
        }
        try {
            object1 = objMethod.invoke(o1, parms);
            object2 = objMethod.invoke(o2, parms);
        } catch (IllegalAccessException e1) {
            throw new RuntimeException("Access denied to method "
                    + this.methodName + " in class " + o1.getClass().getName());
        } catch (InvocationTargetException e2) {
            throw new RuntimeException(this.methodName + " in class "
                    + o1.getClass().getName() + " threw an exception: "
                    + e2.getTargetException().getMessage(), e2.getTargetException());
        }

        if (reverse) {
            if (object1 == null && object2 != null) {
                return -1;
            } else if (object1 != null && object2 == null) {
                return -1;
            } else if (object1 == null && object2 == null) {
                return -1;
            } else {
                if (object1 instanceof String && isCaseInsensitiveOrder()) {
                    return -((String) object1).compareToIgnoreCase((String) object2);
                } else {
                    return -((Comparable) object1).compareTo(object2);
                }
            }
        } else {
            if (object1 == null && object2 != null) {
                return 1;
            } else if (object1 != null && object2 == null) {
                return -1;
            } else if (object1 == null && object2 == null) {
                return -1;
            } else {
                if (object1 instanceof String && isCaseInsensitiveOrder()) {
                    return ((String) object1).compareToIgnoreCase((String) object2);
                } else {
                    return ((Comparable) object1).compareTo(object2);
                }
            }
        }
    }

    /** Getter for property methodName.
     * @return Value of property methodName.
     *
     */
    public java.lang.String getMethodName() {
        return methodName;
    }

    /** Setter for property methodName.
     * @param methodName New value of property methodName.
     *
     */
    public void setMethodName(java.lang.String methodName) {
        this.methodName = methodName;
    }

    /** Getter for property parmClass.
     * @return Value of property parmClass.
     *
     */
    public java.lang.Class[] getParmClass() {
        return this.parmClass;
    }

    /** Setter for property parmClass.
     * @param parmClass New value of property parmClass.
     *
     */
    public void setParmClass(java.lang.Class[] parmClass) {
        this.parmClass = parmClass;
    }

    /** Getter for property parms.
     * @return Value of property parms.
     *
     */
    public java.lang.Object[] getParms() {
        return this.parms;
    }

    /** Setter for property parms.
     * @param parms New value of property parms.
     *
     */
    public void setParms(java.lang.Object[] parms) {
        this.parms = parms;
    }

    /** Getter for property reverse.
     * @return Value of property reverse.
     *
     */
    public boolean isReverse() {
        return reverse;
    }

    /** Setter for property reverse.
     * @param reverse New value of property reverse.
     *
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Getter for property caseInsensitiveOrder.
     * @return Value of property caseInsensitiveOrder.
     */
    public boolean isCaseInsensitiveOrder() {
        return caseInsensitiveOrder;
    }

    /**
     * Setter for property caseInsensitiveOrder.
     * @param caseInsensitiveOrder New value of property caseInsensitiveOrder.
     */
    public void setCaseInsensitiveOrder(boolean caseInsensitiveOrder) {
        this.caseInsensitiveOrder = caseInsensitiveOrder;
    }
}

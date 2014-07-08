 package org.julp;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import net.sf.cglib.proxy.Enhancer;

 /** This predicate is using equals to evaluate */
public class GenericPredicate<T> implements Predicate<T> {

    private DataHolder filter;
    protected static final Object[] EMPTY_ARG = new Object[0];
    protected Method readMethod;
    protected Map<String, Method> methods;
    protected T t;
    protected boolean continueOnNull = true;

    public GenericPredicate() {
    }

    public GenericPredicate(DataHolder filter) {
        this.filter = filter;
    }

    @Override
    public boolean evaluate(T t) {
        this.t = t;
        populateReadMethods(t);

        for (int idx = 1; idx <= filter.getFieldsCount(); idx++) {
            try {
                String fieldName = filter.getFieldName(idx);
                Object filterValue = filter.getFieldValue(idx);
                Object value = readValue(methods.get(fieldName));
                if (filterValue == null && value != null) {
                    return false;
                } else if (filterValue != null && value == null) {
                    return false;
                } else if (filterValue == null && value == null) {
                    continue;
                } else if (!filterValue.equals(value)) {
                    return false;
                }
            } catch (Throwable e) {
                if (e instanceof NullPointerException && continueOnNull) {
                    return false;
                }
                throw new DataAccessException(e);
            }
        }
        return true;
    }

    public void setFilter(DataHolder filter) {
        this.filter = filter;
    }

    protected Object readValue(Method method) throws Throwable {
        Object value = null;
        try {
            value = method.invoke(t, EMPTY_ARG);
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                throw ((InvocationTargetException) e).getTargetException();
            } else {
                throw e;
            }
        }
        return value;
    }

    protected void populateReadMethods(T t) throws DataAccessException {
        if (t == null) {
            return;
        }
        try {
            if (methods == null) {
                methods = new HashMap<>();
            }

            if (findClass(this.t).equals(findClass(t)) && !methods.isEmpty()) {
                return;
            } else {
                methods.clear();
            }

            for (int idx = 1; idx <= filter.getFieldsCount(); idx++) {
                String fieldName = filter.getFieldName(idx);
                readMethod = (new PropertyDescriptor(fieldName, findClass(t))).getReadMethod();
                methods.put(fieldName, readMethod);
            }
        } catch (IntrospectionException e) {
            throw new DataAccessException(e);
        }
    }      
    
    public Class<?> findClass(T t) {
        if (t == null) {
            return null;
        }
        if (t instanceof DomainObject) {
            if (isEnhanced(t)) {               
                return  t.getClass().getSuperclass();       
            } else {
                return t.getClass();
            }           
        } else {
            return t.getClass();
        }
    }

    @Override
    public void setContinueOnNull(boolean continueOnNull) {
        this.continueOnNull = continueOnNull;
    }
    
    protected boolean isEnhanced(T t) {
        boolean enhanced = Enhancer.isEnhanced(t.getClass());
        return enhanced;
    }                        
}

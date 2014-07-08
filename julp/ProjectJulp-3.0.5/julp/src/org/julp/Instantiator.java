package org.julp;

import java.io.Serializable;
import java.util.Set;

public interface Instantiator<T> extends Serializable {
    static final Object[] EMPTY_ARG = new Object[0];
    T newInstance(Class<T> domainClass);
    T enhance(T instance);
    Set<String> getAbstractDomainObjectMethodsToSkip();
    void setAbstractDomainObjectMethodsToSkip(Set<String> methodsToSkip);
    boolean isEnhanced(T t);
}

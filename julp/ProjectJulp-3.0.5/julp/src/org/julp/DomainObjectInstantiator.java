package org.julp;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Used if Class domainClass extends AbstractDomainObject originally
 */
public class DomainObjectInstantiator<T> implements Instantiator<T> {

    private static final long serialVersionUID = -7865189974761626736L;
    private static transient final Logger logger = Logger.getLogger(DomainObjectInstantiator.class.getName());

    public DomainObjectInstantiator() {
    }

    @Override
    public T newInstance(Class<T> domainClass) {
        try {
            return (T) domainClass.newInstance();
        } catch (InstantiationException e) {
            logger.throwing(getClass().getName(), "newInstance", e);
            throw new DataAccessException(e);
        } catch (IllegalAccessException e) {
            logger.throwing(getClass().getName(), "newInstance", e);
            throw new DataAccessException(e);
        }
    }

    @Override
    public T enhance(T instance) {
        throw new UnsupportedOperationException("Method is not supported for " + getClass());
    }

    @Override
    public Set<String> getAbstractDomainObjectMethodsToSkip() {
        throw new UnsupportedOperationException("Method is not supported for " + getClass());
    }

    @Override
    public void setAbstractDomainObjectMethodsToSkip(Set<String> methodsToSkip) {
        throw new UnsupportedOperationException("Method is not supported for " + getClass());
    }

    public T detach(T instance) {
        return instance;
    }

    @Override
    public boolean isEnhanced(T instance) {
        return false;
    }
}

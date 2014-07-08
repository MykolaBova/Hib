package org.julp;

public class Wrapper {

    private Object object;

    public Wrapper(Object object) {
        this.object = object;
    }

    @SuppressWarnings("unchecked")
    public <T> T unwrap(Class<T> type) throws DataAccessException {
        if (isWrapperFor(type)) {
            return (T) object;
        }
        throw new DataAccessException("Type: " + type.getCanonicalName() + ", must be assignable from " + object.getClass());
    }

    public boolean isWrapperFor(Class<?> type) throws DataAccessException {
        return (type != null && type.isAssignableFrom(object.getClass()));
    }
}

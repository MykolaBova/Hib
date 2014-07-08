package org.julp;

public interface Predicate<T> {
    boolean evaluate(T t);
    void setContinueOnNull(boolean continueOnNull);
}

package org.julp.security;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;

public abstract class SubjectListener implements PropertyChangeListener, Serializable {

    private static final long serialVersionUID = 2212035254417321621L;
    protected Subject subject;
    private final transient Logger logger = Logger.getLogger(getClass().getName());
    
    public SubjectListener() {
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent propertyChangeEvent) {
        subject = (Subject) propertyChangeEvent.getNewValue();
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("\t[" + getClass().getName() + "] " + subject);
        }
        postPropertyChange(propertyChangeEvent);
    }

    public abstract void postPropertyChange(java.beans.PropertyChangeEvent propertyChangeEvent);
}

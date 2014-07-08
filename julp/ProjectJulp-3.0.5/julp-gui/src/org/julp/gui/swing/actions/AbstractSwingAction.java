package org.julp.gui.swing.actions;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public abstract class AbstractSwingAction extends AbstractAction {

    private static final long serialVersionUID = 5014997109404875866L;
    protected String smallIconPath;
    protected String largeIconPath;
    protected boolean throwExceptionOnError;

    public java.lang.String getLargeIconPath() {
        return largeIconPath;
    }

    protected void init() {

    }

    public void setLargeIconPath(java.lang.String largeIconPath) {
        this.largeIconPath = largeIconPath;
        try {
            Icon icon = new ImageIcon(getClass().getResource(largeIconPath));
            putValue(Action.LARGE_ICON_KEY, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public java.lang.String getSmallIconPath() {
        return smallIconPath;
    }

    public void setSmallIconPath(java.lang.String smallIconPath) {
        this.smallIconPath = smallIconPath;
        try {
            Icon icon = new ImageIcon(getClass().getResource(smallIconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public boolean isThrowExceptionOnError() {
        return throwExceptionOnError;
    }

    public void setThrowExceptionOnError(boolean throwExceptionOnError) {
        this.throwExceptionOnError = throwExceptionOnError;
    }
}

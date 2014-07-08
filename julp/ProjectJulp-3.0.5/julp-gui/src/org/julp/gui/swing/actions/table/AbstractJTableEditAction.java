package org.julp.gui.swing.actions.table;

import javax.swing.Action;

public abstract class AbstractJTableEditAction extends AbstractJTableAction {

    private static final long serialVersionUID = 6347143838988071851L;
    protected String data;
    protected String cellDelimiter = "\t";
    protected String lineSeparator = System.getProperty("line.separator");

    public AbstractJTableEditAction() {
    }

    public AbstractJTableEditAction(String name) {
        putValue(Action.NAME, name);
    }

    protected void validateData() {
    }

    public java.lang.String getCellDelimiter() {
        return cellDelimiter;
    }

    public void setCellDelimiter(java.lang.String cellDelimiter) {
        this.cellDelimiter = cellDelimiter;
    }

    public java.lang.String getLineSeparator() {
        return lineSeparator;
    }

    public void setLineSeparator(java.lang.String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }
}

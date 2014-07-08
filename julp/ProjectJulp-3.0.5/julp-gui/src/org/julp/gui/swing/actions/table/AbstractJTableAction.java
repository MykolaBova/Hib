package org.julp.gui.swing.actions.table;

import javax.swing.JTable;
import java.awt.Component;
import javax.swing.Action;
import org.julp.gui.swing.actions.AbstractSwingAction;

public abstract class AbstractJTableAction extends AbstractSwingAction {

    private static final long serialVersionUID = 3261136537969043676L;
    protected JTable table;
    protected Component parentComponent;

    public AbstractJTableAction() {            
        init();
    }

    public AbstractJTableAction(String name) {
       putValue(Action.NAME, name);
    }

    public javax.swing.JTable getTable() {
        return table;
    }

    public void setTable(javax.swing.JTable table) {
        this.table = table;
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }
}

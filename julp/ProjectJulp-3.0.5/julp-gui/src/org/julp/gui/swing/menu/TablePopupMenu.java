package org.julp.gui.swing.menu;

import javax.swing.*;
import org.julp.gui.swing.actions.table.*;
import java.awt.event.ActionListener;
import java.awt.Component;

public class TablePopupMenu extends JPopupMenu {

    private static final long serialVersionUID = -6690000966805572679L;
    protected JTable table;
    protected ActionListener saveAsActionListener;
    protected Component parentComponent;
    protected SaveAsAction saveAsAction = new SaveAsAction();

    public TablePopupMenu() {
        super();
    }

    public TablePopupMenu(String label) {
        super(label);
    }

    public TablePopupMenu(String label, JTable table, ActionListener saveAsActionListener, Component parentComponent) {
        super(label);
        this.table = table;
        this.saveAsActionListener = saveAsActionListener;
        this.parentComponent = parentComponent;
        setup();
    }

    public void setup() {
        try {
            javax.swing.JMenuItem selectAll = new javax.swing.JMenuItem();
            SelectAllCellsAction selectAllCells = new SelectAllCellsAction();
            selectAllCells.setTable(table);
            selectAll.setAction(selectAllCells);
            javax.swing.JMenuItem copy = new javax.swing.JMenuItem();
            CellCopyAction copyAction = new CellCopyAction();
            copyAction.setTable(table);
            copy.setAction(copyAction);
            javax.swing.JMenuItem paste = new javax.swing.JMenuItem();
            CellPasteAction pasteAction = new CellPasteAction();
            pasteAction.setTable(table);
            paste.setAction(pasteAction);
            javax.swing.JMenuItem cut = new javax.swing.JMenuItem();
            CellCutAction cutAction = new CellCutAction();
            cutAction.setTable(table);
            cut.setAction(cutAction);
            javax.swing.JMenuItem delete = new javax.swing.JMenuItem();
            CellDeleteAction deleteAction = new CellDeleteAction();
            deleteAction.setTable(table);
            delete.setAction(deleteAction);
            this.add(selectAll);
            this.add(copy);
            this.add(cut);
            this.add(paste);
            this.add(delete);
            this.addSeparator();
            javax.swing.JMenuItem saveAs = new javax.swing.JMenuItem("Save As ...");
            saveAsAction.setTable(table);
            saveAsAction.setParentComponent(getParentComponent());
            saveAs.setAction(saveAsAction);
            saveAs.addActionListener(saveAsActionListener);
            this.add(saveAs);
        } catch (Throwable t) {
            JOptionPane.showMessageDialog(parentComponent, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public javax.swing.JTable getTable() {
        return table;
    }

    public void setTable(javax.swing.JTable table) {
        this.table = table;
        saveAsAction.setTable(table);
    }

    public java.awt.event.ActionListener getSaveAsActionListener() {
        return saveAsActionListener;
    }

    public void setSaveAsActionListener(java.awt.event.ActionListener saveAsActionListener) {
        this.saveAsActionListener = saveAsActionListener;
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
        saveAsAction.setParentComponent(parentComponent);
    }
}

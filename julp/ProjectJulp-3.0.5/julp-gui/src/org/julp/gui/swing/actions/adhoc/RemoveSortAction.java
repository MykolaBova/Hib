package org.julp.gui.swing.actions.adhoc;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.julp.gui.swing.actions.AbstractSwingAction;

public class RemoveSortAction extends AbstractSwingAction  {

    private static final long serialVersionUID = -1861129910008733061L;
    protected JTable sortTable;

    public RemoveSortAction() {        
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.NAME, "Remove Sort");
        putValue(Action.SHORT_DESCRIPTION, "Remove Sort");
    }

    public RemoveSortAction(String name) {
           putValue(Action.NAME, name);
    }

    @Override
    public void init() {        
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.NAME, "Remove Sort");
        putValue(Action.SHORT_DESCRIPTION, "Remove Sort");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (sortTable.getCellEditor() != null) {
            sortTable.getCellEditor().stopCellEditing();
        }
        int sortRow = sortTable.getSelectedRow();
        if (sortRow >= 0) {
            ((DefaultTableModel) sortTable.getModel()).removeRow(sortRow);
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }
        int rowCount = ((DefaultTableModel) sortTable.getModel()).getRowCount();
        if (rowCount == 0) {
            //return;
        } else if (sortRow == rowCount) {
            sortRow = sortRow - 1;
            sortTable.setRowSelectionInterval(sortRow, sortRow);
        } else {
            sortTable.setRowSelectionInterval(sortRow, sortRow);
        }
    }

    public javax.swing.JTable getSortTable() {
        return sortTable;
    }

    public void setSortTable(javax.swing.JTable sortTable) {
        this.sortTable = sortTable;
    }
}

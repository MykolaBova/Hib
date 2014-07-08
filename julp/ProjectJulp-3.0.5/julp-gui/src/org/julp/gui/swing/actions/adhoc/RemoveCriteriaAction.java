package org.julp.gui.swing.actions.adhoc;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.julp.ValueObject;
import org.julp.gui.swing.actions.AbstractSwingAction;
import org.julp.gui.swing.combo.SteppedComboBox;

public class RemoveCriteriaAction extends AbstractSwingAction {

    private static final long serialVersionUID = -1018962852220332282L;
    protected JTable criteriaTable;
    protected JTable sortTable;

    public RemoveCriteriaAction() {        
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.NAME, "Remove Criteria");
        putValue(Action.SHORT_DESCRIPTION, "Remove Criteria");
    }

    public RemoveCriteriaAction(String name) {
        putValue(Action.NAME, name);
    }

    @Override
    public void init() {
        super.init();
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.NAME, "Remove Criteria");
        putValue(Action.SHORT_DESCRIPTION, "Remove Criteria");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (criteriaTable.getCellEditor() != null) {
            criteriaTable.getCellEditor().stopCellEditing();
        }
        int row = criteriaTable.getSelectedRow();
        if (row >= 0) {
            ValueObject field = (ValueObject) criteriaTable.getValueAt(row, 0);
            int sortRowCount = sortTable.getModel().getRowCount();
            for (int i = 0; i < sortRowCount; i++) {
                if (field.equals((ValueObject) sortTable.getValueAt(i, 0))) {
                    ((DefaultTableModel) sortTable.getModel()).removeRow(i);
                    i--;
                    sortRowCount--;
                }
            }
            ((DefaultTableModel) criteriaTable.getModel()).removeRow(row);

            int rowCount = criteriaTable.getModel().getRowCount();
            java.util.Set selectedFields = new java.util.TreeSet();
            for (int i = 0; i < rowCount; i++) {
                selectedFields.add(criteriaTable.getValueAt(i, 0));
            }

            sortRowCount = sortTable.getModel().getRowCount();
            for (int i = 0; i < sortRowCount; i++) {
                ValueObject sortField = (ValueObject) sortTable.getValueAt(i, 0);
                if (!selectedFields.contains(sortField)) {
                    ((DefaultTableModel) sortTable.getModel()).removeRow(i);
                    i--;
                    sortRowCount--;
                }
            }

            SteppedComboBox sortFields = new SteppedComboBox(selectedFields.toArray());
            sortFields.setPopupWidth(sortFields.getPreferredSize().width);
            sortRowCount = sortTable.getModel().getRowCount();
            for (int i = 0; i < sortRowCount; i++) {
                sortTable.getColumn("Field").setCellEditor(new DefaultCellEditor(sortFields));
            }

        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }
        int rowCount = ((DefaultTableModel) criteriaTable.getModel()).getRowCount();
        if (rowCount == 0) {
            return;
        } else if (row == rowCount) {
            row = row - 1;
            criteriaTable.setRowSelectionInterval(row, row);
        } else {
            criteriaTable.setRowSelectionInterval(row, row);
        }
    }

    public javax.swing.JTable getCriteriaTable() {
        return criteriaTable;
    }

    public void setCriteriaTable(javax.swing.JTable criteriaTable) {
        this.criteriaTable = criteriaTable;
    }

    public javax.swing.JTable getSortTable() {
        return sortTable;
    }

    public void setSortTable(javax.swing.JTable sortTable) {
        this.sortTable = sortTable;
    }
}

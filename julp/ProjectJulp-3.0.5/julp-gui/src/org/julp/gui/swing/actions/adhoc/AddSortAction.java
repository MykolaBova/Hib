package org.julp.gui.swing.actions.adhoc;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.julp.*;
import java.util.*;
import org.julp.gui.swing.actions.AbstractSwingAction;
import org.julp.gui.swing.combo.SteppedComboBox;
import org.julp.search.SearchCriteriaBuilder;

public class AddSortAction extends AbstractSwingAction {

    private static final long serialVersionUID = -3600719942267166210L;
    protected JTable sortTable;
    protected JTable criteriaTable;
    protected SearchCriteriaBuilder searchCriteriaBuilder;
    protected java.util.List fields;

    public AddSortAction() {       
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.NAME, "Add Sort");
        putValue(Action.SHORT_DESCRIPTION, "Add Sort");
    }

    public AddSortAction(String name) {
        putValue(Action.NAME, name);
    }

    @Override
    protected void init() {
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.NAME, "Add Sort");
        putValue(Action.SHORT_DESCRIPTION, "Add Sort");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int rowCount = criteriaTable.getModel().getRowCount();
        if (rowCount == 0) {
            java.awt.Toolkit.getDefaultToolkit().beep();
            return;
        }

        DefaultTableModel defaultTableModelSort = (DefaultTableModel) sortTable.getModel();
        Object[] rowData = new Object[2];
        defaultTableModelSort.addRow(rowData);
        int sortRow = defaultTableModelSort.getRowCount() - 1;
        sortTable.setRowSelectionInterval(sortRow, sortRow);

        fields = searchCriteriaBuilder.getFields();
        Collections.sort(fields);
        Object[] fieldsData = fields.toArray();
        ValueObject firstField = null;
        if (fieldsData.length > 0) {
            firstField = (ValueObject) fieldsData[0];
        }
        sortTable.setValueAt(firstField, sortRow, 0);
        sortTable.setValueAt(Boolean.TRUE, sortRow, 1);

        SteppedComboBox fieldsComboBox = new SteppedComboBox(fieldsData);
        fieldsComboBox.setPopupWidth(fieldsComboBox.getPreferredSize().width);
        sortTable.getColumn("Field").setCellEditor(new javax.swing.DefaultCellEditor(fieldsComboBox));
    }

    public javax.swing.JTable getSortTable() {
        return sortTable;
    }

    public void setSortTable(javax.swing.JTable sortTable) {
        this.sortTable = sortTable;
    }

    public SearchCriteriaBuilder getSearchCriteriaBuilder() {
        return searchCriteriaBuilder;
    }

    public void setSearchCriteriaBuilder(SearchCriteriaBuilder searchCriteriaBuilder) {
        this.searchCriteriaBuilder = searchCriteriaBuilder;
    }

    public javax.swing.JTable getCriteriaTable() {
        return criteriaTable;
    }

    public void setCriteriaTable(javax.swing.JTable criteriaTable) {
        this.criteriaTable = criteriaTable;
    }

    public java.util.List getFields() {
        return fields;
    }

    public void setFields(java.util.List fields) {
        this.fields = fields;
    }
}

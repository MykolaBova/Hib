package org.julp.gui.swing.actions.adhoc;

import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import org.julp.*;
import java.util.*;
import org.julp.gui.swing.actions.AbstractSwingAction;
import org.julp.gui.swing.combo.SteppedComboBox;
import org.julp.search.SearchCriteriaBuilder;
import org.julp.search.SearchCriteriaHolder;

public class AddCriteriaAction extends AbstractSwingAction {

    private static final long serialVersionUID = -4574027318898381627L;
    protected JTable criteriaTable;
    protected SearchCriteriaBuilder searchCriteriaBuilder;

    public AddCriteriaAction() {
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.NAME, "Add Criteria");
        putValue(Action.SHORT_DESCRIPTION, "Add Criteria");
    }

    public AddCriteriaAction(String name) {
        putValue(Action.NAME, name);
    }

    @Override
    protected void init() {
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.NAME, "Add Criteria");
        putValue(Action.SHORT_DESCRIPTION, "Add Criteria");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DefaultTableModel defaultTableModel = (DefaultTableModel) criteriaTable.getModel();
        Object[] rowData = new Object[4];
        defaultTableModel.addRow(rowData);
        criteriaTable.clearSelection();
        int row = defaultTableModel.getRowCount() - 1;
        criteriaTable.setRowSelectionInterval(row, row);
        ValueObject firstField = (ValueObject) searchCriteriaBuilder.getFields().get(0);
        criteriaTable.setValueAt(firstField, row, 0);

        /* operators (conditions) */

        Map fieldOperators = searchCriteriaBuilder.getFieldOperators();
        Object obj = fieldOperators.get(firstField);
        if (obj != null) {
            java.util.List operList = (java.util.List) obj;
            ValueObject firstOperator = (ValueObject) operList.get(0);
            criteriaTable.setValueAt(firstOperator, row, 1);
        }

        /* fields */
        java.util.List fieldsData = searchCriteriaBuilder.getFields();
        Collections.sort(fieldsData);
        SteppedComboBox fields = new SteppedComboBox(fieldsData.toArray());
        fields.setPopupWidth(fields.getPreferredSize().width);
        criteriaTable.getColumn("Field").setCellEditor(new javax.swing.DefaultCellEditor(fields));       
        int width = (int) fields.getPreferredSize().getWidth();
        criteriaTable.getColumnModel().getColumn(0).setPreferredWidth(width);
        criteriaTable.getColumnModel().getColumn(0).setMaxWidth(width);

        /* AND/OR */
        ValueObject booleanConditionData[] = new ValueObject[9];
        booleanConditionData[0] = new ValueObject(SearchCriteriaHolder.AND, SearchCriteriaHolder.AND);
        booleanConditionData[1] = new ValueObject(SearchCriteriaHolder.OR, SearchCriteriaHolder.OR);
        booleanConditionData[2] = new ValueObject(SearchCriteriaHolder.AND_NESTED_LOGIC_START, SearchCriteriaHolder.AND_NESTED_LOGIC_START);
        booleanConditionData[3] = new ValueObject(SearchCriteriaHolder.OR_NESTED_LOGIC_START, SearchCriteriaHolder.OR_NESTED_LOGIC_START);
        booleanConditionData[4] = new ValueObject(SearchCriteriaHolder.NESTED_LOGIC_END, SearchCriteriaHolder.NESTED_LOGIC_END);
        booleanConditionData[5] = new ValueObject(SearchCriteriaHolder.AND_NESTED_LOGIC_END, SearchCriteriaHolder.AND_NESTED_LOGIC_END);
        booleanConditionData[6] = new ValueObject(SearchCriteriaHolder.OR_NESTED_LOGIC_END, SearchCriteriaHolder.OR_NESTED_LOGIC_END);
        booleanConditionData[7] = new ValueObject(SearchCriteriaHolder.AND_NESTED_LOGIC, SearchCriteriaHolder.AND_NESTED_LOGIC);
        booleanConditionData[8] = new ValueObject(SearchCriteriaHolder.OR_NESTED_LOGIC, SearchCriteriaHolder.OR_NESTED_LOGIC);

        SteppedComboBox booleanConditions = new SteppedComboBox(booleanConditionData);
        booleanConditions.setPopupWidth(booleanConditions.getPreferredSize().width);
        criteriaTable.getColumn("AND/OR").setCellEditor(new javax.swing.DefaultCellEditor(booleanConditions));

        width = (int) booleanConditions.getPreferredSize().getWidth();
        criteriaTable.getColumnModel().getColumn(3).setPreferredWidth(width);
        criteriaTable.getColumnModel().getColumn(3).setMaxWidth(width);
    }

    public javax.swing.JTable getCriteriaTable() {
        return criteriaTable;
    }

    public void setCriteriaTable(javax.swing.JTable criteriaTable) {
        this.criteriaTable = criteriaTable;
    }

    public SearchCriteriaBuilder getSearchCriteriaBuilder() {
        return searchCriteriaBuilder;
    }

    public void setSearchCriteriaBuilder(SearchCriteriaBuilder searchCriteriaBuilder) {
        this.searchCriteriaBuilder = searchCriteriaBuilder;
    }
}

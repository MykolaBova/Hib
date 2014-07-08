package org.julp.gui.swing.combo;

import javax.swing.*;
import javax.swing.plaf.basic.*;

//public class JComboTableUI extends BasicComboBoxUI {
public class JComboTableUI extends javax.swing.plaf.metal.MetalComboBoxUI {

    protected int dataColumn = -1;
    protected int popupHeight = 150;
    protected JTable popupTable;
    protected JTable parentTable;

    public JComboTableUI(JTable popupTable, JTable parentTable) {
        super();
        this.popupTable = popupTable;
        this.parentTable = parentTable;
    }

    public JComboTableUI(JTable popupTable) {
        super();
        this.popupTable = popupTable;
    }

    protected ComboPopup createPopup() {
        JComboTablePopup comboPopup = new JComboTablePopup(comboBox, this, popupTable, parentTable);
        comboPopup.setPopupHeight(popupHeight);
        comboPopup.setDataColumn(dataColumn);
        return comboPopup;
    }

    public int getDataColumn() {
        return dataColumn;
    }

    public void setDataColumn(int dataColumn) {
        this.dataColumn = dataColumn;
    }

    public int getPopupHeight() {
        return popupHeight;
    }

    public void setPopupHeight(int popupHeight) {
        this.popupHeight = popupHeight;
    }
}

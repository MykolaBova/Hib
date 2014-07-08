package org.julp.gui.swing.combo;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public class JComboTablePopup extends BasicComboPopup implements ListSelectionListener, ItemListener {

    protected JComboBox comboBox;
    protected JComboTableUI ui;
    protected JTable popupTable;
    protected JTable parentTable;
    protected JScrollPane pane;
    protected int width = 0;
    protected int dataColumn = -1;
    protected int popupHeight = 150;

    public JComboTablePopup(JComboBox comboBox, JComboTableUI ui, JTable popupTable) {
        super(comboBox);
        init(comboBox, ui, popupTable, null);
    }

    public JComboTablePopup(JComboBox comboBox, JComboTableUI ui, JTable popupTable, JTable parentTable) {
        super(comboBox);
        init(comboBox, ui, popupTable, parentTable);
    }
    
    private void init(JComboBox comboBox, JComboTableUI ui, JTable popupTable, JTable parentTable) {
        setup(comboBox, ui, popupTable, parentTable);
    }

    protected void setup(JComboBox comboBox, JComboTableUI ui, JTable popupTable, JTable parentTable) {
        this.comboBox = comboBox;
        this.comboBox.setEditable(false);
        this.ui = ui;
        this.setPopupTable(popupTable);
        this.parentTable = parentTable;

        TableColumnModel columnModel = popupTable.getColumnModel();
        int colCount = columnModel.getColumnCount();
        for (int col = 0; col < colCount; col++) {
            width = width + columnModel.getColumn(col).getPreferredWidth();
        }

        popupTable.getTableHeader().setReorderingAllowed(false);
        popupTable.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tableMouseClicked(evt);
            }
        });

        pane = new JScrollPane(popupTable);
        popupTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        popupTable.getSelectionModel().addListSelectionListener(this);
        comboBox.addItemListener(this);

        comboBox.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {

            @Override
            public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
            }

            @Override
            public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
                comboBoxPopupMenuWillBecomeInvisible(evt);
            }

            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
            }
        });

        comboBox.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                comboBoxKeyPressed(evt);
            }
        });
    }

    private void comboBoxPopupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
        int row = getPopupTable().getSelectedRow();
        if (row < 0) {
            return;
        }
        Object value = getPopupTable().getValueAt(row, this.dataColumn);
        comboBox.removeAllItems();
        comboBox.addItem(value);
        comboBox.setSelectedIndex(0);

        if (parentTable != null) {
            int parentTableRow = parentTable.getSelectedRow();
            int parentTableCol = parentTable.getSelectedColumn();
            //System.out.println("parentTableRow: " + parentTableRow);
            //System.out.println("parentTableCol: " + parentTableCol);
            if (row < 0) {
                return;
            }
            parentTable.setValueAt(value, parentTableRow, parentTableCol);
        }
        getPopupTable().removeRowSelectionInterval(row, row);
        //System.out.println("Object value: " + value);
        //System.out.println("comboBox.getItemCount(): " + comboBox.getItemCount());
        //System.out.println("comboBox.getSelectedIndex(): " + comboBox.getSelectedIndex());
        //System.out.println("comboBox.getSelectedItem(): " + comboBox.getSelectedItem());
        //System.out.println("Selected value: " + value);
    }

    @Override
    public void setVisible(boolean visible) {
        if (!visible) {
            super.setVisible(visible);
            return;
        }
        removeAll();
        int comboBoxWidth = comboBox.getWidth();
        if (comboBoxWidth > width) {
            width = comboBoxWidth;
        }
        pane.setPreferredSize(new Dimension(width, popupHeight));
        add(pane);
        super.setVisible(visible);
    }

    private void comboBoxKeyPressed(java.awt.event.KeyEvent evt) {
        //if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_KP_UP){
        //    System.out.println("up");
        //}else if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_KP_DOWN){
        //    System.out.println("down");
        //}
    }

    public void valueChanged(ListSelectionEvent evt) {
        //System.out.println(evt);
    }

    public void itemStateChanged(ItemEvent evt) {
        //System.out.println(evt);
    }

    private void tableMouseClicked(java.awt.event.MouseEvent evt) {
        comboBox.hidePopup();
    }

    public int getPopupHeight() {
        return popupHeight;
    }

    public void setPopupHeight(int popupHeight) {
        this.popupHeight = popupHeight;
    }

    public JTable getParentTable() {
        return parentTable;
    }

    public void setParentTable(JTable parentTable) {
        this.parentTable = parentTable;
    }

    public JTable getPopupTable() {
        return popupTable;
    }

    public void setPopupTable(JTable popupTable) {
        this.popupTable = popupTable;
    }

    public int getDataColumn() {
        return dataColumn;
    }

    public void setDataColumn(int dataColumn) {
        this.dataColumn = dataColumn;
    }
}

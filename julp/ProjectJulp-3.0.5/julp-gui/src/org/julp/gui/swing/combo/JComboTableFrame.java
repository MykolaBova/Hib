package org.julp.gui.swing.combo;

import java.awt.Dimension;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

public class JComboTableFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = 4182563306325728449L;
    
    public JComboTableFrame() {
        initComponents();
        Dimension d1 = comboBox3.getPreferredSize();
        comboBox3.setPreferredSize(new Dimension(50, 20));
        comboBox3.setPopupWidth(d1.width);
        topPanel.add(comboBox3);
        
        JComboTableUI ui1 = new JComboTableUI(getTable1());
        ui1.setDataColumn(2);
        ui1.setPopupHeight(100);
        comboBox1.setUI(ui1);
        comboBox1.setPreferredSize(new Dimension(150, 20));
        topPanel.add(comboBox1);
        
        JComboTableUI ui = new JComboTableUI(getTable2(), parentTable);
        ui.setDataColumn(0);
        ui.setPopupHeight(100);
        comboBox4.setUI(ui);
        
        parentTable.getColumn("Title 1").setCellEditor(new javax.swing.DefaultCellEditor(comboBox4));
        Dimension d2 = comboBox2.getPreferredSize();
        comboBox2.setPreferredSize(new Dimension(50, d2.height));
        comboBox2.setPopupWidth(d2.width);
        parentTable.getColumn("Title 2").setCellEditor(new javax.swing.DefaultCellEditor(comboBox2));
        
    }
    
    protected JTable getTable1(){
        Vector rows = new Vector();
        Vector row1 = new Vector(3);
        row1.add("1");
        row1.add("aaaaaaaa");
        row1.add(new Double(777.77));
        
        Vector row2 = new Vector(3);
        row2.add("2");
        row2.add("bbbbbbb");
        row2.add(new Double(888.88));
        
        Vector row3 = new Vector(3);
        row3.add("3");
        row3.add("aaazsdffffffffff");
        row3.add(new Double(999.99));
        
        Vector row4 = new Vector(3);
        row4.add("4");
        row4.add("fhbddjjdd");
        row4.add(new Double(1000.99));
        
        Vector row5 = new Vector(3);
        row5.add("5");
        row5.add("zzsdsdsd");
        row5.add(new Double(1110.99));
        
        Vector row6 = new Vector(3);
        row6.add("6");
        row6.add("kkkkkkkkk");
        row6.add(new Double(1222.99));
        
        Vector row7 = new Vector(3);
        row7.add("7");
        row7.add("ssdfsdfsdf");
        row7.add(new Double(1333.99));
        
        Vector columns = new Vector(3);
        columns.add("Id");
        columns.add("Desc");
        columns.add("Amount");
        
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        rows.add(row5);
        rows.add(row6);
        rows.add(row7);
        
        model = new JComboTableModel();
        model.setDataVector(rows, columns);
        
        popupTable1 = new JTable(model);
        popupTable1.setShowGrid(false);
        popupTable1.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return popupTable1;
    }
    
    protected JTable getTable2(){
        Vector rows = new Vector();
        Vector row1 = new Vector(3);
        row1.add("1");
        row1.add("aaaaaaaa");
        row1.add(new Double(777.77));
        
        Vector row2 = new Vector(3);
        row2.add("2");
        row2.add("bbbbbbb");
        row2.add(new Double(888.88));
        
        Vector row3 = new Vector(3);
        row3.add("3");
        row3.add("aaazsdffffffffff");
        row3.add(new Double(999.99));
        
        Vector row4 = new Vector(3);
        row4.add("4");
        row4.add("fhbddjjdd");
        row4.add(new Double(1000.99));
        
        Vector row5 = new Vector(3);
        row5.add("5");
        row5.add("zzsdsdsd");
        row5.add(new Double(1110.99));
        
        Vector row6 = new Vector(3);
        row6.add("6");
        row6.add("kkkkkkkkk");
        row6.add(new Double(1222.99));
        
        Vector row7 = new Vector(3);
        row7.add("7");
        row7.add("ssdfsdfsdf");
        row7.add(new Double(1333.99));
        
        Vector columns = new Vector(3);
        columns.add("Id");
        columns.add("Desc");
        columns.add("Amount");
        
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);
        rows.add(row4);
        rows.add(row5);
        rows.add(row6);
        rows.add(row7);
        
        model = new JComboTableModel();
        model.setDataVector(rows, columns);
        popupTable2 = new JTable(model);
        popupTable2.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        return popupTable2;
    }
    
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        topPanel = new javax.swing.JPanel();
        centerPanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        parentTable = new javax.swing.JTable();
        bottomPanel = new javax.swing.JPanel();
        button = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.BorderLayout(5, 5));

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        topPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        topPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 10));
        getContentPane().add(topPanel, java.awt.BorderLayout.NORTH);

        centerPanel.setLayout(new java.awt.BorderLayout());

        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 1, 10));
        parentTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        parentTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        tableScrollPane.setViewportView(parentTable);

        centerPanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        bottomPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 10, 5, 5));
        button.setText("Print Current Cell Value");
        button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonActionPerformed(evt);
            }
        });

        bottomPanel.add(button);

        getContentPane().add(bottomPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void buttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonActionPerformed
        int row = parentTable.getSelectedRow();
        int col = parentTable.getSelectedColumn();
        System.out.println("row: " + row + " col: " + col);
        if (row < 0) return;
        Object value = parentTable.getValueAt(row, col);
        System.out.println("Object value: " + value);
    }//GEN-LAST:event_buttonActionPerformed
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JButton button;
    private javax.swing.JPanel centerPanel;
    private javax.swing.JTable parentTable;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
    private JComboTableModel model;
    private JTable popupTable1;
    private JTable popupTable2;
    private String[] str1 = {
        "A",
        "abcdefghijklmnopqrstuvwxyz",
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ",
        "0123456789"
    };
    
    private String[] str2 = {
        "Z",
        "kjkjkjkjkjjjk",
        "UUUUUUUUUUUUUUUUUUXCDG",
        "998776545"
    };
    
    private SteppedComboBox comboBox1 = new SteppedComboBox();
    private SteppedComboBox comboBox2 = new SteppedComboBox(str2);
    private SteppedComboBox comboBox3 = new SteppedComboBox(str1);
    private SteppedComboBox comboBox4 = new SteppedComboBox(str2);
    
    protected class JComboTableModel extends DefaultTableModel {
        private static final long serialVersionUID = 3502052103555830495L;
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
    
    public static void main(String args[]) {
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                java.util.Enumeration defaultUI = UIManager.getDefaults().keys();
                String ui;
                javax.swing.plaf.FontUIResource sansPlain11 = new javax.swing.plaf.FontUIResource("sans", java.awt.Font.PLAIN, 11);
                while (defaultUI.hasMoreElements()){
                    ui = defaultUI.nextElement().toString();
                    if (ui.endsWith(".font")){
                        UIManager.getDefaults().put(ui, sansPlain11);
                    }
                }
                new JComboTableFrame().setVisible(true);
            }
        });
    }
    
}

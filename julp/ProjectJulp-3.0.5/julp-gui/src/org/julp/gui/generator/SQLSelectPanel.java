package org.julp.gui.generator;

import info.clearthought.layout.TableLayout;
import javax.swing.*;

public class SQLSelectPanel extends javax.swing.JPanel {

    /**
     * Creates new form SQLSelectPanel
     */
    public SQLSelectPanel() {
        // b - border
        // f - FILL
        // p - PREFERRED
        // vs - vertical space between labels and text fields
        // vg - vertical gap between form elements
        // hg - horizontal gap between form elements

        double b = 5;
        double f = TableLayout.FILL;
        double p = TableLayout.PREFERRED;
        double vs = 5;
        double vg = 10;
        double hg = 5;

        double size[][] = {
            {b, p, hg, f, hg, b},
            {b, f, vg, p, b}
        };
        this.setLayout(new TableLayout(size));

        sqlSelectLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        sqlSelectLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        sqlSelectLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        sqlSelectLabel.setLabelFor(sqlSelectJTextArea);
        this.add(sqlSelectLabel, "1, 1");

//        sqlSelectJTextArea.setFont(new java.awt.Font("Default", 0, 12));
//        sqlSelectJTextArea.setMaximumSize(new java.awt.Dimension(400, 250));
//        sqlSelectJTextArea.setMinimumSize(new java.awt.Dimension(100, 50));
//        sqlSelectJTextArea.setPreferredSize(new java.awt.Dimension(300, 150));
        sqlSelectJTextArea.setText("Note: \nSince we do not need ResultSet, just ResultSetMetaData, \nyou should specify condition in WHERE clause to return no rows: \nSELECT ... WHERE 1 = 2;\n ");
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setViewportView(sqlSelectJTextArea);
        this.add(scrollPane, "3, 1, 4, 1");

        clearButton.setFont(new java.awt.Font("Default", 0, 12));
        clearButton.setMaximumSize(new java.awt.Dimension(100, 40));
        clearButton.setMinimumSize(new java.awt.Dimension(30, 16));
        clearButton.setPreferredSize(new java.awt.Dimension(90, 25));
        clearButton.setMnemonic('r');
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        JPanel buttonsPanel = new JPanel();
        double[][] buttonsPanelSize = {{p}, {p}};
        buttonsPanel.setLayout(new TableLayout(buttonsPanelSize));
        buttonsPanel.add(clearButton, "0, 0, r, t");
        this.add(buttonsPanel, "3, 3, 4, 3, r, t");
    }

    protected void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String sql = "SELECT <COLUMN_LIST> \n\nFROM <TABLE_LIST>\n\nWHERE 1 = 2";
        sqlSelectJTextArea.setText(sql);
        sqlSelectJTextArea.grabFocus();
        sqlSelectJTextArea.setCaretPosition(20);
        sqlSelectJTextArea.moveCaretPosition(7);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents

    public javax.swing.JTextArea getSqlSelectJTextArea() {
        return sqlSelectJTextArea;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected javax.swing.JTextArea sqlSelectJTextArea = new javax.swing.JTextArea();
    protected javax.swing.JLabel sqlSelectLabel = new javax.swing.JLabel("");
    protected javax.swing.JButton clearButton = new javax.swing.JButton("Clear");
    protected javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane();
}
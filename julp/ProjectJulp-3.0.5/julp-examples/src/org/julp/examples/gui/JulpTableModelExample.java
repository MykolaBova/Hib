package org.julp.examples.gui;

import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import org.julp.AbstractDomainObjectFactory;
import org.julp.DomainObject;
import org.julp.ValueObject;
import org.julp.db.DomainObjectFactory;
import org.julp.examples.CustomerFactory;
import org.julp.gui.swing.combo.SteppedComboBox;
import org.julp.gui.swing.table.DisplayValueTableRenderer;
import org.julp.gui.swing.table.JulpTableModel;

public class JulpTableModelExample extends JFrame implements TableModelListener {

    public JulpTableModelExample() {
        java.util.Enumeration defaultUI = UIManager.getDefaults().keys();
        String ui;
        javax.swing.plaf.FontUIResource sansPlain11 = new javax.swing.plaf.FontUIResource("sans", java.awt.Font.PLAIN, 11);
        while (defaultUI.hasMoreElements()) {
            ui = defaultUI.nextElement().toString();
            if (ui.endsWith(".font")) {
                UIManager.getDefaults().put(ui, sansPlain11);
            }
        }
        initComponents();
        table.setSurrendersFocusOnKeystroke(true);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        table.putClientProperty("terminateEditOnFocus", Boolean.TRUE);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.setFillsViewportHeight(true);
        table.setUpdateSelectionOnSort(true);

        customerIdDisplay.put(new Integer(0), "AXAA - 0");
        customerIdDisplay.put(new Integer(1), "A1FA - 1");
        customerIdDisplay.put(new Integer(2), "BDBB - 2");
        customerIdDisplay.put(new Integer(3), "CCTC - 3");
        customerIdDisplay.put(new Integer(4), "AAAD - 4");
        customerIdDisplay.put(new Integer(5), "AADA - 5");
        customerIdDisplay.put(new Integer(6), "BBSB - 6");
        customerIdDisplay.put(new Integer(7), "CCUC - 7");
        customerIdDisplay.put(new Integer(8), "AADA - 8");
        customerIdDisplay.put(new Integer(9), "A3AA - 9");
        customerIdDisplay.put(new Integer(10), "DBBB - 10");
        customerIdDisplay.put(new Integer(11), "AHAA - 11");
        customerIdDisplay.put(new Integer(12), "AAJA - 12");
        customerIdDisplay.put(new Integer(13), "BBCVB - 13");
        customerIdDisplay.put(new Integer(14), "CCCD - 14");
        customerIdDisplay.put(new Integer(15), "AADA - 15");
        customerIdDisplay.put(new Integer(16), "BB7B - 16");
        customerIdDisplay.put(new Integer(17), "CCDC - 17");
        customerIdDisplay.put(new Integer(18), "AAAL - 18");
        customerIdDisplay.put(new Integer(19), "AAAG - 19");
        customerIdDisplay.put(new Integer(20), "BBFB - 20");
        customerIdDisplay.put(new Integer(21), "AAA4 - 21");
        customerIdDisplay.put(new Integer(22), "BBB5 - 22");
        customerIdDisplay.put(new Integer(23), "CC6C - 23");
        customerIdDisplay.put(new Integer(24), "AA4A - 24");
        customerIdDisplay.put(new Integer(25), "AA2A - 25");
        customerIdDisplay.put(new Integer(26), "B2BB - 26");
        customerIdDisplay.put(new Integer(27), "C2CC - 27");
        customerIdDisplay.put(new Integer(28), "A4AA - 28");
        customerIdDisplay.put(new Integer(29), "A5AA - 29");
        customerIdDisplay.put(new Integer(30), "LBBB - 30");
        customerIdDisplay.put(new Integer(31), "A7AA - 31");
        customerIdDisplay.put(new Integer(32), "BB8B - 32");
        customerIdDisplay.put(new Integer(33), "CCCC - 33");
        customerIdDisplay.put(new Integer(34), "AAGA - 34");
        customerIdDisplay.put(new Integer(35), "AAA6 - 35");
        customerIdDisplay.put(new Integer(36), "BB7B - 36");
        customerIdDisplay.put(new Integer(37), "CC3C - 37");
        customerIdDisplay.put(new Integer(38), "AA3A - 38");
        customerIdDisplay.put(new Integer(39), "AA6A - 39");
        customerIdDisplay.put(new Integer(40), "B7BB - 40");
        customerIdDisplay.put(new Integer(41), "A5AA - 41");
        customerIdDisplay.put(new Integer(42), "B8BB - 42");
        customerIdDisplay.put(new Integer(43), "CC8C - 43");
        customerIdDisplay.put(new Integer(44), "AAA8 - 44");
        customerIdDisplay.put(new Integer(45), "AAA4 - 45");
        customerIdDisplay.put(new Integer(46), "AAAA - 46");
        customerIdDisplay.put(new Integer(47), "BBBB - 47");
        customerIdDisplay.put(new Integer(48), "CCCC - 48");
        customerIdDisplay.put(new Integer(49), "DDDD - 49");
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        centerPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        buttonPanel = new javax.swing.JPanel();
        openButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        printRowButton = new javax.swing.JButton();
        printAllButton = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 5, 1, 5));
        centerPanel.setLayout(new java.awt.BorderLayout());

        jLabel1.setText("Click on header to sort");
        centerPanel.add(jLabel1, java.awt.BorderLayout.NORTH);

        table.setAutoCreateRowSorter(true);
        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        table.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setFillsViewportHeight(true);
        table.setUpdateSelectionOnSort(false);
        jScrollPane1.setViewportView(table);

        centerPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        openButton.setMnemonic('p');
        openButton.setText("Populate");
        openButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(openButton);

        addButton.setMnemonic('a');
        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(addButton);

        removeButton.setMnemonic('r');
        removeButton.setText("Remove");
        removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(removeButton);

        printRowButton.setMnemonic('t');
        printRowButton.setText("Print row");
        printRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printRowButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(printRowButton);

        printAllButton.setText("Print All");
        printAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printAllButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(printAllButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        fileMenu.setText("File");

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText("Help");

        contentsMenuItem.setText("Contents");
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void printAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printAllButtonActionPerformed
        if (table.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Populate table first", "Print All", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Iterator iter = ((JulpTableModel) table.getModel()).getFactory().getObjectList().iterator();
        while (iter.hasNext()) {
            System.out.println(iter.next());
        }
    }//GEN-LAST:event_printAllButtonActionPerformed

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        try {
            int[] rows = table.getSelectedRows();
            if (rows.length < 1) {
                JOptionPane.showMessageDialog(this, "Select row(s) first", "Remove", JOptionPane.ERROR_MESSAGE);
                return;
            }
            for (int rowIndex = rows.length - 1; rowIndex >= 0; rowIndex--) {
                int removeRow = rows[rowIndex];
                int modelRow = table.convertRowIndexToModel(removeRow);
                ((JulpTableModel) table.getModel()).removeRow(modelRow);
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_removeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        try {
            if (table.getRowCount() == 0) {
                JulpTableModel model = getModel(false);
                prepare(model);
            }
            ((JulpTableModel) table.getModel()).addRow();
            int row = table.getRowCount();
            if (row > 0) {
                row = row - 1;
            }
            java.awt.Rectangle aRect = table.getCellRect(row, 0, true);
            table.scrollRectToVisible(aRect);
            table.setRowSelectionInterval(row, row);
            table.editCellAt(row, 0);
            Component c = table.getEditorComponent();
            if (c != null) {
                Point p = c.getLocationOnScreen();
                final Robot r = new Robot();
                r.mouseMove(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
                r.mousePress(InputEvent.BUTTON1_MASK);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        r.mouseRelease(InputEvent.BUTTON1_MASK);
                    }
                });
            }
        } catch (final Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
//            SwingUtilities.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    ErrorDisplayPanel errorPanel = new ErrorDisplayPanel(null, e);
//                    Frame f = JOptionPane.getFrameForComponent(centerPanel);
//                    JOptionPane.showMessageDialog(f, errorPanel, "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            });
            
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void printRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printRowButtonActionPerformed
        if (table.getModel() instanceof JulpTableModel) {
            JulpTableModel theModel = (JulpTableModel) table.getModel();
            int row = table.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select row first", "Print Row", JOptionPane.ERROR_MESSAGE);
                return;
            }
            System.out.println(theModel.getFactory().getObjectList().get(row));
        } else {
            JOptionPane.showMessageDialog(this, "Populate table first", "Print row", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_printRowButtonActionPerformed

    private void prepare(JulpTableModel model) {
        
        ValueObject[] cityList = new ValueObject[12];
        cityList[0] = new ValueObject("Berne", "1. Berne");
        cityList[1] = new ValueObject("Boston", "2. Boston");
        cityList[2] = new ValueObject("Chicago", "3. Chicago");
        cityList[3] = new ValueObject("Dallas", "4. Dallas");
        cityList[4] = new ValueObject("Lyon", "5. Lyon");
        cityList[5] = new ValueObject("New York", "6. New York");
        cityList[6] = new ValueObject("Olten", "7. Olten");
        cityList[7] = new ValueObject("Seattle", "8. Seattle");
        cityList[8] = new ValueObject("Oslo", "9. Oslo");
        cityList[9] = new ValueObject("Palo Alto", "10. Palo Alto");
        cityList[10] = new ValueObject("Paris", "11. Paris");
        cityList[11] = new ValueObject("San Francisco", "12. San Francisco");

        ValueObject[] idList = new ValueObject[50];
        idList[0] = new ValueObject(new Integer(0), "AXAA - 0");
        idList[1] = new ValueObject(new Integer(1), "A1FA - 1");
        idList[2] = new ValueObject(new Integer(2), "BDBB - 2");
        idList[3] = new ValueObject(new Integer(3), "CCTC - 3");
        idList[4] = new ValueObject(new Integer(4), "AAAD - 4");
        idList[5] = new ValueObject(new Integer(5), "AADA - 5");
        idList[6] = new ValueObject(new Integer(6), "BBSB - 6");
        idList[7] = new ValueObject(new Integer(7), "CCUC - 7");
        idList[8] = new ValueObject(new Integer(8), "AADA - 8");
        idList[9] = new ValueObject(new Integer(9), "A3AA - 9");
        idList[10] = new ValueObject(new Integer(10), "DBBB - 10");
        idList[11] = new ValueObject(new Integer(11), "AHAA - 11");
        idList[12] = new ValueObject(new Integer(12), "AAJA - 12");
        idList[13] = new ValueObject(new Integer(13), "BBCVB - 13");
        idList[14] = new ValueObject(new Integer(14), "CCCD - 14");
        idList[15] = new ValueObject(new Integer(15), "AADA - 15");
        idList[16] = new ValueObject(new Integer(16), "BB7B - 16");
        idList[17] = new ValueObject(new Integer(17), "CCDC - 17");
        idList[18] = new ValueObject(new Integer(18), "AAAL - 18");
        idList[19] = new ValueObject(new Integer(19), "AAAG - 19");
        idList[20] = new ValueObject(new Integer(20), "BBFB - 20");
        idList[21] = new ValueObject(new Integer(21), "AAA4 - 21");
        idList[22] = new ValueObject(new Integer(22), "BBB5 - 22");
        idList[23] = new ValueObject(new Integer(23), "CC6C - 23");
        idList[24] = new ValueObject(new Integer(24), "AA4A - 24");
        idList[25] = new ValueObject(new Integer(25), "AA2A - 25");
        idList[26] = new ValueObject(new Integer(26), "B2BB - 26");
        idList[27] = new ValueObject(new Integer(27), "C2CC - 27");
        idList[28] = new ValueObject(new Integer(28), "A4AA - 28");
        idList[29] = new ValueObject(new Integer(29), "A5AA - 29");
        idList[30] = new ValueObject(new Integer(30), "LBBB - 30");
        idList[31] = new ValueObject(new Integer(31), "A7AA - 31");
        idList[32] = new ValueObject(new Integer(32), "BB8B - 32");
        idList[33] = new ValueObject(new Integer(33), "CCCC - 33");
        idList[34] = new ValueObject(new Integer(34), "AAGA - 34");
        idList[35] = new ValueObject(new Integer(35), "AAA6 - 35");
        idList[36] = new ValueObject(new Integer(36), "BB7B - 36");
        idList[37] = new ValueObject(new Integer(37), "CC3C - 37");
        idList[38] = new ValueObject(new Integer(38), "AA3A - 38");
        idList[39] = new ValueObject(new Integer(39), "AA6A - 39");
        idList[40] = new ValueObject(new Integer(40), "B7BB - 40");
        idList[41] = new ValueObject(new Integer(41), "A5AA - 41");
        idList[42] = new ValueObject(new Integer(42), "B8BB - 42");
        idList[43] = new ValueObject(new Integer(43), "CC8C - 43");
        idList[44] = new ValueObject(new Integer(44), "AAA8 - 44");
        idList[45] = new ValueObject(new Integer(45), "AAA4 - 45");
        idList[46] = new ValueObject(new Integer(46), "AAAA - 46");
        idList[47] = new ValueObject(new Integer(47), "BBBB - 47");
        idList[48] = new ValueObject(new Integer(48), "CCCC - 48");
        idList[49] = new ValueObject(new Integer(49), "DDDD - 49");

        JComboBox cityCombo = new SteppedComboBox(cityList);
        JComboBox idCombo = new SteppedComboBox(idList);

        DisplayValueTableRenderer cityRenderer = new DisplayValueTableRenderer();
        Map cityMapping = new HashMap();

        cityMapping.put("Berne", "Berne");
        cityMapping.put("Boston", "Boston");
        cityMapping.put("Chicago", "Chicago");
        cityMapping.put("Dallas", "Dallas");
        cityMapping.put("Lyon", "Lyon");
        cityMapping.put("New York", "New York");
        cityMapping.put("Olten", "Olten");
        cityMapping.put("Seattle", "Seattle");
        cityMapping.put("Oslo", "Oslo");
        cityMapping.put("Palo Alto", "Palo Alto");
        cityMapping.put("Paris", "Paris");
        cityMapping.put("San Francisco", "San Francisco");

        cityRenderer.setMapping(cityMapping);

        DisplayValueTableRenderer idRenderer = new DisplayValueTableRenderer();
        idRenderer.setMapping(customerIdDisplay);

        model.setFieldDisplayValues("customerId", customerIdDisplay);
        model.setFieldDisplayValues("city", cityMapping);

        System.out.println("jTable1: " + table == null);
        System.out.println("model: " + model == null);
        table.setModel(model);
        //jTable1.getTableHeader().addMouseListener(model);
        table.getColumn("City").setCellRenderer(cityRenderer);
        table.getColumn("City").setCellEditor(new DefaultCellEditor(cityCombo));
        table.getColumn("Customer Id").setCellRenderer(idRenderer);
        table.getColumn("Customer Id").setCellEditor(new DefaultCellEditor(idCombo));
    }
    
    private void openButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openButtonActionPerformed
        try {
            // factory = new CustomerFactory();
            JulpTableModel model = getModel(true);
            prepare(model);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            //ErrorDisplayPanel errorPanel = new ErrorDisplayPanel(null, e);
            //JOptionPane.showMessageDialog(this, errorPanel, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_openButtonActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    protected DomainObjectFactory getFactory() {
        return factory;
    }

    protected JulpTableModel getModel(boolean load) {
        JulpTableModel model = new JulpTableModel();
        try {
            //factory.getMetaData().setWritable(1, false);
            model.setDisplayableFields(Arrays.asList(new String[] {"customerId", "firstName", "lastName", "street", "city"}));
            model.setLabels(Arrays.asList(new String[] {"Customer Id", "First Name", "Last Name", "Street", "City"}));
            if (load) {
                factory.findAllCustomers();
                ListIterator li = factory.getObjectList().listIterator();
                while (li.hasNext()) {
                    org.julp.examples.Customer customer = (org.julp.examples.Customer) li.next();
                    Object customerId = customer.getCustomerId();
                    Object customerIdDisplayValue = customerIdDisplay.get(customerId);
                    ((DomainObject) customer).setDisplayValue("customerId", customerIdDisplayValue.toString());
                }
            }
            model.setFactory(factory);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return model;
    }

    public void setFactory(CustomerFactory factory) {
        this.factory = factory;
    }

    public boolean handleException(DomainObject domainObject, Throwable t) {
        return false;
    }

    public boolean handleException(DomainObject domainObject, String sql, Throwable t) {
        return false;
    }

    public boolean handleException(DomainObject domainObject, String field, Object value, Throwable t) {
        JOptionPane.showMessageDialog(this, t.getCause().getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        return false;
    }

    public boolean handleException(DomainObject domainObject, String sql, Collection params, Throwable t) {
        return false;
    }

    public void setDomainObjectFactory(AbstractDomainObjectFactory factory) {
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        try {
            //
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);

        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JMenuItem aboutMenuItem;
    protected javax.swing.JButton addButton;
    protected javax.swing.JPanel buttonPanel;
    protected javax.swing.JPanel centerPanel;
    protected javax.swing.JMenuItem contentsMenuItem;
    protected javax.swing.JMenuItem exitMenuItem;
    protected javax.swing.JMenu fileMenu;
    protected javax.swing.JMenu helpMenu;
    protected javax.swing.JLabel jLabel1;
    protected javax.swing.JScrollPane jScrollPane1;
    protected javax.swing.JMenuBar menuBar;
    protected javax.swing.JButton openButton;
    protected javax.swing.JButton printAllButton;
    protected javax.swing.JButton printRowButton;
    protected javax.swing.JButton removeButton;
    protected javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables
    private CustomerFactory factory;
    private Map customerIdDisplay = new HashMap();
}

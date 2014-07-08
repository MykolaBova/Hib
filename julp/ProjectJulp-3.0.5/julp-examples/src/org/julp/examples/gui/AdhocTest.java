package org.julp.examples.gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import org.julp.Wrapper;
import org.julp.examples.CustomerFactory;
import org.julp.examples.CustomerNoExtending;
import org.julp.gui.swing.adhoc.AdhocDialog;
import org.julp.gui.swing.table.JulpTableModel;
import org.julp.search.SearchCriteriaBuilder;
import org.julp.search.XPathSearchCriteriaBuilder;

public class AdhocTest extends javax.swing.JFrame {

    private static final long serialVersionUID = -663875674942368355L;

    /** Creates new form AdhocTest */
    public AdhocTest() {
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
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        centerPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        southPanel = new javax.swing.JPanel();
        findButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        centerPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        centerPanel.setLayout(new java.awt.BorderLayout());

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        jTable1.setRowHeight(20);
        jScrollPane1.setViewportView(jTable1);

        centerPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(centerPanel, java.awt.BorderLayout.CENTER);

        southPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 5));
        southPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        findButton.setMnemonic('f');
        findButton.setText("Find");
        findButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findButtonActionPerformed(evt);
            }
        });
        southPanel.add(findButton);

        getContentPane().add(southPanel, java.awt.BorderLayout.SOUTH);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-600)/2, (screenSize.height-400)/2, 600, 400);
    }// </editor-fold>//GEN-END:initComponents

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
        openDialog(false);
    }//GEN-LAST:event_findButtonActionPerformed

    public void addCriteria() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                adhocDialog.addCriteria();
            }
        });
    }

    public void openDialog(boolean addCriteria) {
        String queryId = "customer2";

        if (this.adhocDialog != null) {
            ((XPathSearchCriteriaBuilder) adhocDialog.getSearchCriteriaBuilder()).loadQuery(queryId);
            adhocDialog.setVisible(true);
            return;
        }
        adhocDialog = new AdhocDialog(this, this);
        //adhocDialog = new AdhocDialog(this);

        CustomerSearchBuilder searchCriteria = new CustomerSearchBuilder();        
        URL url = null;
        try {            
            url = getClass().getResource("/org/julp/examples/queries.xml");         
            searchCriteria.setQueryURI(url.toURI());
        } catch (Exception e) {
            e.printStackTrace();
        }                   
        
        searchCriteria.loadQuery(queryId);
        adhocDialog.setSearchCriteriaBuilder(searchCriteria);

        if (addCriteria) {
            addCriteria();
        }
        adhocDialog.setVisible(true);
    }

    protected void initFactory() {
        try {
            factory.setDomainClass(CustomerNoExtending.class);
            factory.loadMappings("CustomerInvoice.properties");
            factory.populateMetaData();
            factory.getMetaData().setWritable(1, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void find() {
        try {
            SearchCriteriaBuilder searchCriteriaBuilder = adhocDialog.getSearchCriteriaBuilder();
            searchCriteriaBuilder.buildCriteria();
            String sql = searchCriteriaBuilder.getQuery();
            Collection args = searchCriteriaBuilder.getArguments();
            System.out.println(sql + ": " + args);
            try {
                initFactory();
                factory.load(new Wrapper(factory.getDBServices().getResultSet(sql, args)));
                searchCriteriaBuilder.reset();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            JulpTableModel model = getModel();            
            List fieldsList = new ArrayList();
            fieldsList.add("customerId");
            fieldsList.add("lastName");
            fieldsList.add("firstName");
            fieldsList.add("street");
            fieldsList.add("city");
            fieldsList.add("invoiceId");
            //fieldsList.add("item");                       
            fieldsList.add("total");            
            model.setDisplayableFields(fieldsList);
            jTable1.setModel(model);
            //jTable1.getTableHeader().addMouseListener(model);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected JulpTableModel getModel() {
        JulpTableModel model = new JulpTableModel();
        try {
            model.setFactory(factory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //model.setCaseInsensitiveOrder(true);
        return model;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JButton findButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JPanel southPanel;
    // End of variables declaration//GEN-END:variables
    private AdhocDialog adhocDialog;
    private CustomerFactory factory;

    public CustomerFactory getFactory() {
        return factory;
    }

    public void setFactory(CustomerFactory factory) {
        this.factory = factory;
    }
}

package org.julp.gui.swing.frame;

public class CommonMaintenanceInternalFrame extends CommonInternalFrame {

    private static final long serialVersionUID = -4990066125934105658L;

    public CommonMaintenanceInternalFrame() {
        initComponents();
    }

    public javax.swing.JButton getAddButton() {
        return addButton;
    }

    public void setAddButton(javax.swing.JButton addButton) {
        this.addButton = addButton;
    }

    public javax.swing.JButton getFindButton() {
        return findButton;
    }

    public void setFindButton(javax.swing.JButton findButton) {
        this.findButton = findButton;
    }

    public javax.swing.JToolBar getMaintToolBar() {
        return maintToolBar;
    }

    public void setMaintToolBar(javax.swing.JToolBar maintToolBar) {
        this.maintToolBar = maintToolBar;
    }

    public javax.swing.JButton getRemoveButton() {
        return removeButton;
    }

    public void setRemoveButton(javax.swing.JButton removeButton) {
        this.removeButton = removeButton;
    }

    public javax.swing.JButton getStoreButton() {
        return storeButton;
    }

    public void setStoreButton(javax.swing.JButton storeButton) {
        this.storeButton = storeButton;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        maintToolBar = new javax.swing.JToolBar();
        findButton = new javax.swing.JButton();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        storeButton = new javax.swing.JButton();

        FormListener formListener = new FormListener();

        maintToolBar.setRollover(true);
        findButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Find16.gif")));
        findButton.setMnemonic('f');
        findButton.setToolTipText("Find");
        findButton.setMaximumSize(new java.awt.Dimension(20, 20));
        findButton.setMinimumSize(new java.awt.Dimension(16, 16));
        findButton.setPreferredSize(new java.awt.Dimension(20, 20));
        findButton.addActionListener(formListener);

        maintToolBar.add(findButton);

        addButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Add16.gif")));
        addButton.setMnemonic('a');
        addButton.setToolTipText("Add");
        addButton.setMaximumSize(new java.awt.Dimension(20, 20));
        addButton.setMinimumSize(new java.awt.Dimension(16, 16));
        addButton.setPreferredSize(new java.awt.Dimension(20, 20));
        addButton.addActionListener(formListener);

        maintToolBar.add(addButton);

        removeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete16.gif")));
        removeButton.setMnemonic('d');
        removeButton.setToolTipText("Delete");
        removeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        removeButton.setMinimumSize(new java.awt.Dimension(16, 16));
        removeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        removeButton.addActionListener(formListener);

        maintToolBar.add(removeButton);

        storeButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save16.gif")));
        storeButton.setMnemonic('s');
        storeButton.setToolTipText("Save");
        storeButton.setMaximumSize(new java.awt.Dimension(20, 20));
        storeButton.setMinimumSize(new java.awt.Dimension(16, 16));
        storeButton.setPreferredSize(new java.awt.Dimension(20, 20));
        storeButton.addActionListener(formListener);

        maintToolBar.add(storeButton);

        getContentPane().add(maintToolBar, java.awt.BorderLayout.NORTH);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == findButton) {
                CommonMaintenanceInternalFrame.this.findButtonActionPerformed(evt);
            }
            else if (evt.getSource() == addButton) {
                CommonMaintenanceInternalFrame.this.addButtonActionPerformed(evt);
            }
            else if (evt.getSource() == removeButton) {
                CommonMaintenanceInternalFrame.this.removeButtonActionPerformed(evt);
            }
            else if (evt.getSource() == storeButton) {
                CommonMaintenanceInternalFrame.this.storeButtonActionPerformed(evt);
            }
        }
    }
    // </editor-fold>//GEN-END:initComponents

    private void removeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeButtonActionPerformed
        remove();
    }//GEN-LAST:event_removeButtonActionPerformed

    private void storeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_storeButtonActionPerformed
        store();
    }//GEN-LAST:event_storeButtonActionPerformed

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        add();
    }//GEN-LAST:event_addButtonActionPerformed

    private void findButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findButtonActionPerformed
    }//GEN-LAST:event_findButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JButton addButton;
    protected javax.swing.JButton findButton;
    protected javax.swing.JToolBar maintToolBar;
    protected javax.swing.JButton removeButton;
    protected javax.swing.JButton storeButton;
    // End of variables declaration//GEN-END:variables
}

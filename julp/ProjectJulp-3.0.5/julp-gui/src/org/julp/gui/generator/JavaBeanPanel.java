package org.julp.gui.generator;

import info.clearthought.layout.TableLayout;
import javax.swing.*;

public class JavaBeanPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = 298772810077712455L;

    public JavaBeanPanel() {
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
            {b, p, hg, f, hg, p, b},
            {b, p, vs, p, vs, p, vs, p, b}
        };

        this.setLayout(new TableLayout(size));
        javaBeanOutputDirLabel.setDisplayedMnemonic('O');
        javaBeanOutputDirLabel.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanOutputDirLabel.setText("Output Dir:");
        javaBeanOutputDirLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        javaBeanOutputDirLabel.setMinimumSize(new java.awt.Dimension(50, 20));
        javaBeanOutputDirLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        javaBeanOutputDirLabel.setLabelFor(javaBeanOutputDirTextField);
        this.add(javaBeanOutputDirLabel, "1, 1");

        javaBeanOutputDirTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        javaBeanOutputDirTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        javaBeanOutputDirTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(javaBeanOutputDirTextField, "3, 1");

        javaBeanOutputDirSearchButton.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanOutputDirSearchButton.setText("...");
        javaBeanOutputDirSearchButton.setMaximumSize(new java.awt.Dimension(46, 30));
        javaBeanOutputDirSearchButton.setMinimumSize(new java.awt.Dimension(20, 20));
        javaBeanOutputDirSearchButton.setPreferredSize(new java.awt.Dimension(30, 20));
        javaBeanOutputDirSearchButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                javaBeanOutputDirSearchButtonActionPerformed(evt);
            }
        });

        this.add(javaBeanOutputDirSearchButton, "5, 1");

        javaBeanPackageLabel.setDisplayedMnemonic('P');
        javaBeanPackageLabel.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanPackageLabel.setText("Package:");
        javaBeanPackageLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        javaBeanPackageLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        javaBeanPackageLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        javaBeanPackageLabel.setLabelFor(javaBeanPackageTextField);
        this.add(javaBeanPackageLabel, "1, 3");

        javaBeanPackageTextField.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanPackageTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        javaBeanPackageTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        javaBeanPackageTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(javaBeanPackageTextField, "3, 3");

        javaBeanClassNameLabel.setDisplayedMnemonic('C');
        javaBeanClassNameLabel.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanClassNameLabel.setText("Class Name:");
        javaBeanClassNameLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        javaBeanClassNameLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        javaBeanClassNameLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        javaBeanClassNameLabel.setLabelFor(javaBeanClassNameTextField);
        this.add(javaBeanClassNameLabel, "1, 5");

        javaBeanClassNameTextField.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanClassNameTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        javaBeanClassNameTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        javaBeanClassNameTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(javaBeanClassNameTextField, "3, 5");

        javaBeanOverwriteLabel.setDisplayedMnemonic('R');
        javaBeanOverwriteLabel.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanOverwriteLabel.setText("Overwrite File:");
        javaBeanOverwriteLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        javaBeanOverwriteLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        javaBeanOverwriteLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        javaBeanOverwriteLabel.setLabelFor(javaBeanOverwriteCheckBox);
        this.add(javaBeanOverwriteLabel, "1, 7");

        javaBeanOverwriteCheckBox.setFont(new java.awt.Font("Default", 0, 12));
        javaBeanOverwriteCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        javaBeanOverwriteCheckBox.setMaximumSize(new java.awt.Dimension(30, 30));
        javaBeanOverwriteCheckBox.setMinimumSize(new java.awt.Dimension(20, 20));
        javaBeanOverwriteCheckBox.setPreferredSize(new java.awt.Dimension(20, 20));
        javaBeanOverwriteCheckBox.addItemListener(new java.awt.event.ItemListener() {

            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                overwriteFileCheckBoxItemStateChanged(evt);
            }
        });
        this.add(javaBeanOverwriteCheckBox, "3, 7");

    }

    protected void overwriteFileCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            overrideFile = false;
        } else {
            overrideFile = true;
        }
    }

    protected void javaBeanOutputDirSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(SwingUtilities.windowForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            javaBeanOutputDirTextField.setText(path);
            fc = null;
            javaBeanOutputDirTextField.grabFocus();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents

    public javax.swing.JTextField getJavaBeanClassNameTextField() {
        return javaBeanClassNameTextField;
    }

    public javax.swing.JTextField getJavaBeanOutputDirTextField() {
        return javaBeanOutputDirTextField;
    }

    public javax.swing.JCheckBox getJavaBeanOverwriteCheckBox() {
        return javaBeanOverwriteCheckBox;
    }

    public javax.swing.JTextField getJavaBeanPackageTextField() {
        return javaBeanPackageTextField;
    }

    public boolean isOverrideFile() {
        return overrideFile;
    }

    public void setOverrideFile(boolean overrideFile) {
        this.overrideFile = overrideFile;
    }

    protected void setDir(String dir) {
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected javax.swing.JLabel javaBeanOutputDirLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField javaBeanOutputDirTextField = new javax.swing.JTextField();
    protected javax.swing.JButton javaBeanOutputDirSearchButton = new javax.swing.JButton();
    protected javax.swing.JLabel javaBeanPackageLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField javaBeanPackageTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel javaBeanClassNameLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField javaBeanClassNameTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel javaBeanOverwriteLabel = new javax.swing.JLabel();
    protected javax.swing.JCheckBox javaBeanOverwriteCheckBox = new javax.swing.JCheckBox();
    protected boolean overrideFile = false;
}

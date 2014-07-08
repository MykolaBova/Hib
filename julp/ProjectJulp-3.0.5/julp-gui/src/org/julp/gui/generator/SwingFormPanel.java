package org.julp.gui.generator;

import info.clearthought.layout.TableLayout;
import javax.swing.*;

public class SwingFormPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -6194482781676064022L;

    public SwingFormPanel() {
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

        swingFormOutputDirLabel.setDisplayedMnemonic('u');
        swingFormOutputDirLabel.setFont(new java.awt.Font("Default", 0, 12));
        swingFormOutputDirLabel.setText("Output Dir:");
        swingFormOutputDirLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        swingFormOutputDirLabel.setMinimumSize(new java.awt.Dimension(50, 20));
        swingFormOutputDirLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        swingFormOutputDirLabel.setLabelFor(swingFormOutputDirTextField);
        this.add(swingFormOutputDirLabel, "1, 1");

        swingFormOutputDirTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        swingFormOutputDirTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        swingFormOutputDirTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(swingFormOutputDirTextField, "3, 1");

        swingFormOutputDirSearchButton.setFont(new java.awt.Font("Default", 0, 12));
        swingFormOutputDirSearchButton.setText("...");
        swingFormOutputDirSearchButton.setMaximumSize(new java.awt.Dimension(46, 30));
        swingFormOutputDirSearchButton.setMinimumSize(new java.awt.Dimension(20, 20));
        swingFormOutputDirSearchButton.setPreferredSize(new java.awt.Dimension(30, 20));
        swingFormOutputDirSearchButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swingFormOutputDirSearchButtonActionPerformed(evt);
            }
        });

        this.add(swingFormOutputDirSearchButton, "5, 1");

        swingFormPackageLabel.setDisplayedMnemonic('a');
        swingFormPackageLabel.setFont(new java.awt.Font("Default", 0, 12));
        swingFormPackageLabel.setText("Package:");
        swingFormPackageLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        swingFormPackageLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        swingFormPackageLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        swingFormPackageLabel.setLabelFor(swingFormPackageTextField);
        this.add(swingFormPackageLabel, "1, 3");

        swingFormPackageTextField.setFont(new java.awt.Font("Default", 0, 12));
        swingFormPackageTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        swingFormPackageTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        swingFormPackageTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(swingFormPackageTextField, "3, 3");

        swingFormClassNameLabel.setDisplayedMnemonic('l');
        swingFormClassNameLabel.setFont(new java.awt.Font("Default", 0, 12));
        swingFormClassNameLabel.setText("Class Name:");
        swingFormClassNameLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        swingFormClassNameLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        swingFormClassNameLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        swingFormClassNameLabel.setLabelFor(swingFormClassNameTextField);
        this.add(swingFormClassNameLabel, "1, 5");

        swingFormClassNameTextField.setFont(new java.awt.Font("Default", 0, 12));
        swingFormClassNameTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        swingFormClassNameTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        swingFormClassNameTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(swingFormClassNameTextField, "3, 5");

        swingFormOverwriteLabel.setDisplayedMnemonic('v');
        swingFormOverwriteLabel.setFont(new java.awt.Font("Default", 0, 12));
        swingFormOverwriteLabel.setText("Overwrite File:");
        swingFormOverwriteLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        swingFormOverwriteLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        swingFormOverwriteLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        swingFormOverwriteLabel.setLabelFor(swingFormOverwriteCheckBox);
        this.add(swingFormOverwriteLabel, "1, 7");

        swingFormOverwriteCheckBox.setFont(new java.awt.Font("Default", 0, 12));
        swingFormOverwriteCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        swingFormOverwriteCheckBox.setMaximumSize(new java.awt.Dimension(30, 30));
        swingFormOverwriteCheckBox.setMinimumSize(new java.awt.Dimension(20, 20));
        swingFormOverwriteCheckBox.setPreferredSize(new java.awt.Dimension(20, 20));
        swingFormOverwriteCheckBox.addItemListener(new java.awt.event.ItemListener() {

            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                overwriteFileCheckBoxItemStateChanged(evt);
            }
        });
        this.add(swingFormOverwriteCheckBox, "3, 7");
    }

    protected void overwriteFileCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            overwiteFile = false;
        } else {
            overwiteFile = true;
        }
    }

    public void enableComponents(boolean enable) {
        if (enable) {
            swingFormOutputDirTextField.setEnabled(enable);
            swingFormOutputDirSearchButton.setEnabled(enable);
            swingFormPackageTextField.setEnabled(enable);
            swingFormClassNameTextField.setEnabled(enable);
            //swingFormOverwriteCheckBox.setSelected(enable);
            swingFormOverwriteCheckBox.setEnabled(enable);
        } else {
            swingFormOutputDirTextField.setText("");
            swingFormOutputDirTextField.setEnabled(enable);
            swingFormOutputDirSearchButton.setEnabled(enable);
            swingFormPackageTextField.setText("");
            swingFormPackageTextField.setEnabled(enable);
            swingFormClassNameTextField.setText("");
            swingFormClassNameTextField.setEnabled(enable);
            swingFormOverwriteCheckBox.setEnabled(enable);
            swingFormOverwriteCheckBox.setSelected(enable);
        }
    }

    protected void swingFormOutputDirSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(SwingUtilities.windowForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            swingFormOutputDirTextField.setText(path);
            fc = null;
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents

    public javax.swing.JTextField getSwingFormClassNameTextField() {
        return swingFormClassNameTextField;
    }

    public javax.swing.JTextField getSwingFormOutputDirTextField() {
        return swingFormOutputDirTextField;
    }

    public javax.swing.JCheckBox getSwingFormOverwriteCheckBox() {
        return swingFormOverwriteCheckBox;
    }

    public javax.swing.JTextField getSwingFormPackageTextField() {
        return swingFormPackageTextField;
    }

    public boolean isOverwriteFile() {
        return overwiteFile;
    }

    public void setOverwriteFile(boolean overwiteFile) {
        this.overwiteFile = overwiteFile;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected javax.swing.JLabel swingFormOutputDirLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField swingFormOutputDirTextField = new javax.swing.JTextField();
    protected javax.swing.JButton swingFormOutputDirSearchButton = new javax.swing.JButton();
    protected javax.swing.JLabel swingFormPackageLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField swingFormPackageTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel swingFormClassNameLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField swingFormClassNameTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel swingFormOverwriteLabel = new javax.swing.JLabel();
    protected javax.swing.JCheckBox swingFormOverwriteCheckBox = new javax.swing.JCheckBox();
    protected boolean overwiteFile = false;
}

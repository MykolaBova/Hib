package org.julp.gui.generator;

import info.clearthought.layout.TableLayout;
import javax.swing.*;

public class HTMLFormPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -7023656913974620865L;

    public HTMLFormPanel() {
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
        htmlFormOutputDirLabel.setDisplayedMnemonic('t');
        htmlFormOutputDirLabel.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormOutputDirLabel.setText("Output Dir:");
        htmlFormOutputDirLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        htmlFormOutputDirLabel.setMinimumSize(new java.awt.Dimension(50, 20));
        htmlFormOutputDirLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        htmlFormOutputDirLabel.setLabelFor(htmlFormOutputDirTextField);
        this.add(htmlFormOutputDirLabel, "1, 1");

        htmlFormOutputDirTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        htmlFormOutputDirTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        htmlFormOutputDirTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(htmlFormOutputDirTextField, "3, 1");

        htmlFormOutputDirSearchButton.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormOutputDirSearchButton.setText("...");
        htmlFormOutputDirSearchButton.setMaximumSize(new java.awt.Dimension(46, 30));
        htmlFormOutputDirSearchButton.setMinimumSize(new java.awt.Dimension(20, 20));
        htmlFormOutputDirSearchButton.setPreferredSize(new java.awt.Dimension(30, 20));
        htmlFormOutputDirSearchButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                htmlFormOutputDirSearchButtonActionPerformed(evt);
            }
        });

        this.add(htmlFormOutputDirSearchButton, "5, 1");

        htmlFormFileNameLabel.setDisplayedMnemonic('f');
        htmlFormFileNameLabel.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormFileNameLabel.setText("File Name:");
        htmlFormFileNameLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        htmlFormFileNameLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        htmlFormFileNameLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        htmlFormFileNameLabel.setLabelFor(htmlFormFileNameTextField);
        this.add(htmlFormFileNameLabel, "1, 3");

        htmlFormFileNameTextField.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormFileNameTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        htmlFormFileNameTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        htmlFormFileNameTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(htmlFormFileNameTextField, "3, 3");

        htmlFormOverwriteLabel.setDisplayedMnemonic('w');
        htmlFormOverwriteLabel.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormOverwriteLabel.setText("Overwrite File:");
        htmlFormOverwriteLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        htmlFormOverwriteLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        htmlFormOverwriteLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        htmlFormOverwriteLabel.setLabelFor(htmlFormOverwriteCheckBox);
        this.add(htmlFormOverwriteLabel, "1, 5");

        htmlFormOverwriteCheckBox.setFont(new java.awt.Font("Default", 0, 12));
        htmlFormOverwriteCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        htmlFormOverwriteCheckBox.setMaximumSize(new java.awt.Dimension(30, 30));
        htmlFormOverwriteCheckBox.setMinimumSize(new java.awt.Dimension(20, 20));
        htmlFormOverwriteCheckBox.setPreferredSize(new java.awt.Dimension(20, 20));
        htmlFormOverwriteCheckBox.addItemListener(new java.awt.event.ItemListener() {

            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                overwriteFileCheckBoxItemStateChanged(evt);
            }
        });
        this.add(htmlFormOverwriteCheckBox, "3, 5");
    }

    protected void overwriteFileCheckBoxItemStateChanged(java.awt.event.ItemEvent evt) {
        if (evt.getStateChange() == java.awt.event.ItemEvent.DESELECTED) {
            overrideFile = false;
        } else {
            overrideFile = true;
        }
    }

    protected void htmlFormOutputDirSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = fc.showOpenDialog(SwingUtilities.windowForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            String path = file.getAbsolutePath();
            htmlFormOutputDirTextField.setText(path);
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

    public void enableComponents(boolean enable) {
        if (enable) {
            htmlFormOutputDirTextField.setEnabled(enable);
            htmlFormOutputDirSearchButton.setEnabled(enable);
            htmlFormFileNameTextField.setEnabled(enable);
            htmlFormOverwriteCheckBox.setEnabled(enable);
        } else {
            htmlFormOutputDirTextField.setText("");
            htmlFormOutputDirTextField.setEnabled(enable);
            htmlFormOutputDirSearchButton.setEnabled(enable);
            htmlFormFileNameTextField.setText("");
            htmlFormFileNameTextField.setEnabled(enable);
            htmlFormOverwriteCheckBox.setEnabled(enable);
            htmlFormOverwriteCheckBox.setSelected(enable);
        }
    }

    public javax.swing.JTextField getHtmlFormFileNameTextField() {
        return htmlFormFileNameTextField;
    }

    public javax.swing.JTextField getHtmlFormOutputDirTextField() {
        return htmlFormOutputDirTextField;
    }

    public javax.swing.JCheckBox getHtmlFormOverwriteCheckBox() {
        return htmlFormOverwriteCheckBox;
    }

    public boolean isOverrideFile() {
        return overrideFile;
    }

    public void setOverrideFile(boolean overrideFile) {
        this.overrideFile = overrideFile;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected javax.swing.JLabel htmlFormOutputDirLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField htmlFormOutputDirTextField = new javax.swing.JTextField();
    protected javax.swing.JButton htmlFormOutputDirSearchButton = new javax.swing.JButton();
    protected javax.swing.JLabel htmlFormFileNameLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField htmlFormFileNameTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel htmlFormOverwriteLabel = new javax.swing.JLabel();
    protected javax.swing.JCheckBox htmlFormOverwriteCheckBox = new javax.swing.JCheckBox();
    protected boolean overrideFile = false;
}

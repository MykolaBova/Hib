package org.julp.gui.generator;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;
import org.julp.gui.swing.combo.*;
import javax.swing.border.*;

@SuppressWarnings("UseOfObsoleteCollectionType")
public class GeneratorFrame extends javax.swing.JFrame {

    private static final long serialVersionUID = -7225952453754051018L;

    public GeneratorFrame() {
        initComponents();

        CompoundBorder panelBorder = BorderFactory.createCompoundBorder(new EmptyBorder(5, 20, 10, 20), rowHeightSpinner.getBorder());

        connectionInfoPanel.setBorder(new TitledBorder(panelBorder, "Connection Info"));
        sqlSelectPanel.setBorder(new TitledBorder(panelBorder, "SQL Select"));
        generationOptionsPanel.setBorder(new TitledBorder(panelBorder, "Generate"));
        javaBeanPanel.setBorder(new TitledBorder(panelBorder, "JavaBean"));
        swingFormPanel.setBorder(new TitledBorder(panelBorder, "Swing Form"));
        htmlFormPanel.setBorder(new TitledBorder(panelBorder, "HTML Form"));

        jScrollPane1.setBorder(panelBorder);
        optionsPanel.setBorder(new EmptyBorder(0, 20, 0, 15));
        buttonsPanel.setBorder(new EmptyBorder(0, 20, 0, 15));

        stepOnePanel.add(connectionInfoPanel);
        stepOnePanel.add(sqlSelectPanel);
        stepOnePanel.add(generationOptionsPanel);
        stepTwoPanel.add(javaBeanPanel);
        stepTwoPanel.add(swingFormPanel);
        stepTwoPanel.add(htmlFormPanel);

        connectionInfoPanel.setGenerator(generator);

        javaBeanPanel.getJavaBeanOutputDirTextField().addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected()) {
                    String s1 = swingFormPanel.getSwingFormOutputDirTextField().getText();
                    if (s1 == null || s1.trim().equals("")) {
                        String s2 = javaBeanPanel.getJavaBeanOutputDirTextField().getText();
                        swingFormPanel.getSwingFormOutputDirTextField().setText(s2);
                    }
                }

                if (generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected()) {
                    String s3 = htmlFormPanel.getHtmlFormOutputDirTextField().getText();
                    if (s3 == null || s3.trim().equals("")) {
                        String s4 = javaBeanPanel.getJavaBeanOutputDirTextField().getText();
                        htmlFormPanel.getHtmlFormOutputDirTextField().setText(s4);
                    }
                }
            }
        });

        javaBeanPanel.getJavaBeanPackageTextField().addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                String s1 = javaBeanPanel.getJavaBeanPackageTextField().getText();
                if (s1 != null) {
                    s1 = s1.replaceAll("/", ".");
                    s1 = s1.replaceAll("\\\\", ".");
                    javaBeanPanel.getJavaBeanPackageTextField().setText(s1);
                }
                if (generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected()) {
                    String s2 = swingFormPanel.getSwingFormPackageTextField().getText();
                    if (s2 == null || s2.trim().equals("")) {
                        if (s1 != null && !(s1.trim().equals(""))) {
                            swingFormPanel.getSwingFormPackageTextField().setText(s1 + ".gui");
                        }
                    }
                }
            }
        });

        javaBeanPanel.getJavaBeanClassNameTextField().addFocusListener(new java.awt.event.FocusAdapter() {

            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected()) {
                    String s1 = swingFormPanel.getSwingFormClassNameTextField().getText();
                    if (s1 == null || s1.trim().equals("")) {
                        String s2 = javaBeanPanel.getJavaBeanClassNameTextField().getText();
                        if (s2 != null && !(s2.trim().equals(""))) {
                            swingFormPanel.getSwingFormClassNameTextField().setText(s2 + "Form");
                        }
                    }
                }

                if (generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected()) {
                    String s3 = htmlFormPanel.getHtmlFormFileNameTextField().getText();
                    if (s3 == null || s3.trim().equals("")) {
                        String s4 = javaBeanPanel.getJavaBeanClassNameTextField().getText();
                        if (s4 != null && !(s4.trim().equals(""))) {
                            htmlFormPanel.getHtmlFormFileNameTextField().setText(s4 + "Form.html");
                        }
                    }
                }
            }
        });

        jTabbedPane1.setMnemonicAt(0, KeyEvent.VK_1);
        jTabbedPane1.setMnemonicAt(1, KeyEvent.VK_2);
        jTabbedPane1.setMnemonicAt(2, KeyEvent.VK_3);
        try {
            load();
        } catch (Exception e) {
            // ignore
        }
        setupTable();
        rowHeightSpinner.setValue(20);
        generationDataTable.setRowHeight(20);
    }

    protected void setupTable() {
        generationDataTable.setSurrendersFocusOnKeystroke(true);
        generationDataTable.getSelectionModel().setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);

        String[] dataTypesData = {
            "java.lang.String",
            "java.lang.Boolean",
            "java.lang.Byte",
            "java.lang.Character",
            "java.lang.Double",
            "java.lang.Float",
            "java.lang.Integer",
            "java.lang.Long",
            "java.lang.Short",
            "java.math.BigDecimal",
            "java.math.BigInteger",
            "java.sql.Date",
            "java.sql.Timestamp",
            "java.sql.Time",
            "java.util.Date",
            "java.lang.Object",
            "boolean",
            "byte",
            "char",
            "double",
            "float",
            "int",
            "long",
            "short"};

        SteppedComboBox dataTypes = new SteppedComboBox(dataTypesData);
        Dimension d = dataTypes.getPreferredSize();
        dataTypes.setPopupWidth(d.width);
        dataTypes.setFont(new java.awt.Font("Default", 0, 12));

        String[] swingEditorsData = {
            "javax.swing.JCheckBox",
            "javax.swing.JComboBox",
            "javax.swing.JFormattedTextField",
            "javax.swing.JLabel",
            "javax.swing.JPasswordField",
            "javax.swing.JRadioButton",
            "javax.swing.JTextArea",
            "javax.swing.JTextField",
            "javax.swing.JList",
            "javax.swing.JEditorPane",
            "javax.swing.JTextPane",
            "javax.swing.JSpinner"};

        SteppedComboBox swingEditors = new SteppedComboBox(swingEditorsData);
        d = swingEditors.getPreferredSize();
        swingEditors.setPopupWidth(d.width);
        swingEditors.setFont(new java.awt.Font("Default", 0, 12));

        String[] htmlInputsData = {
            "text",
            "checkbox",
            "hidden",
            "password",
            "radio",
            "textarea",
            "select"};

        JComboBox htmlInputs = new JComboBox(htmlInputsData);
        htmlInputs.setFont(new java.awt.Font("Default", 0, 12));

        String[] nullableData = {"NoNulls", "Nullable", "Unknown"};
        JComboBox nullable = new JComboBox(nullableData);
        nullable.setFont(new java.awt.Font("Default", 0, 12));

        dataTypes.setEditable(true);
        swingEditors.setEditable(true);
        htmlInputs.setEditable(true);
        nullable.setEditable(false);

        generationDataTable.getColumn("Data Type").setCellEditor(new DefaultCellEditor(dataTypes));
        generationDataTable.getColumn("Swing Editor").setCellEditor(new DefaultCellEditor(swingEditors));
        generationDataTable.getColumn("HTML Input Type").setCellEditor(new DefaultCellEditor(htmlInputs));
        generationDataTable.getColumn("Nullable").setCellEditor(new DefaultCellEditor(nullable));
    }

    protected void load() {
        Properties props = new Properties();
        InputStream is = null;
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            PropertiesHolder.getInstance().setUserHome(userHome);
        } else {
            userHome = PropertiesHolder.getInstance().getUserHome();
        }
        String path = userHome + File.separator + "ConnectionInfo.properties";
        try {
            File f = new File(path);
            boolean exists = f.exists();
            if (!exists || f.length() == 0) {
                f = new File(".", "ConnectionInfo.properties");
                exists = f.exists();
                if (!exists || f.length() == 0) {
                    path = null;
                } else {
                    path = f.getCanonicalPath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (path == null) {
            return;
        }
        
        try {
            is = new FileInputStream(path);
            props.load(is);
            String driver = props.getProperty("driver", "");
            String url = props.getProperty("url", "");
            String user = props.getProperty("user", "");
            String password = props.getProperty("password", "");
            String optionalConnProps = props.getProperty("optional_conn_props", "");
            connectionInfoPanel.getDriverTextField().setText(driver);
            connectionInfoPanel.getDbUrlTextField().setText(url);
            connectionInfoPanel.getUserTextField().setText(user);
            connectionInfoPanel.getPasswordField().setText(password);
            connectionInfoPanel.getConnectionPropertiesTextField().setText(optionalConnProps);
            System.setProperty("driver", driver);
            System.setProperty("url", url);
            System.setProperty("user", user);
            System.setProperty("password", password);
            System.setProperty("optional_conn_props", optionalConnProps);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(this, ioe.getMessage(), "Warning", JOptionPane.WARNING_MESSAGE);
        } finally {
            try {
                is.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(this, ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        stepOnePanel = new javax.swing.JPanel();
        stepTwoPanel = new javax.swing.JPanel();
        stepThreePanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        checkNullInSettersCheckBox = new javax.swing.JCheckBox();
        rowHeightLabel = new javax.swing.JLabel();
        rowHeightSpinner = new javax.swing.JSpinner();
        jScrollPane1 = new javax.swing.JScrollPane();
        generationDataTable = new javax.swing.JTable();
        buttonsPanel = new javax.swing.JPanel();
        populateButton = new javax.swing.JButton();
        addRowButton = new javax.swing.JButton();
        inserRowButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        generateButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Java Objects Generator");
        setName(""); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout(20, 10));

        jTabbedPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 20, 20, 20));
        jTabbedPane1.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        jTabbedPane1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jTabbedPane1StateChanged(evt);
            }
        });

        stepOnePanel.setLayout(new java.awt.GridLayout(3, 1, 10, 0));
        jTabbedPane1.addTab("Step #1", stepOnePanel);

        stepTwoPanel.setLayout(new java.awt.GridLayout(3, 1, 10, 0));
        jTabbedPane1.addTab("Step #2", stepTwoPanel);

        stepThreePanel.setLayout(new java.awt.BorderLayout());

        optionsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        checkNullInSettersCheckBox.setMnemonic('u');
		checkNullInSettersCheckBox.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        checkNullInSettersCheckBox.setText("Check null in Setters:");
        checkNullInSettersCheckBox.setHorizontalTextPosition(javax.swing.SwingConstants.LEADING);
        optionsPanel.add(checkNullInSettersCheckBox);

        rowHeightLabel.setDisplayedMnemonic('H');
        rowHeightLabel.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        rowHeightLabel.setLabelFor(rowHeightSpinner);
        rowHeightLabel.setText("Row Height:");
        optionsPanel.add(rowHeightLabel);

        rowHeightSpinner.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        rowHeightSpinner.setMaximumSize(new java.awt.Dimension(40, 40));
        rowHeightSpinner.setMinimumSize(new java.awt.Dimension(30, 16));
        rowHeightSpinner.setPreferredSize(new java.awt.Dimension(40, 20));
        rowHeightSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rowHeightSpinnerStateChanged(evt);
            }
        });
        optionsPanel.add(rowHeightSpinner);

        stepThreePanel.add(optionsPanel, java.awt.BorderLayout.NORTH);

        generationDataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "DBColumn", "Data Type", "Field Name", "Label", "Swing Editor", "HTML Input Type", "Nullable", "Visible"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Boolean.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(generationDataTable);

        stepThreePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        populateButton.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        populateButton.setMnemonic('p');
        populateButton.setText("Populate");
        populateButton.setMaximumSize(new java.awt.Dimension(100, 40));
        populateButton.setMinimumSize(new java.awt.Dimension(30, 16));
        populateButton.setPreferredSize(new java.awt.Dimension(90, 25));
        populateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                populateButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(populateButton);

        addRowButton.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        addRowButton.setMnemonic('a');
        addRowButton.setText("Add");
        addRowButton.setMaximumSize(new java.awt.Dimension(100, 40));
        addRowButton.setMinimumSize(new java.awt.Dimension(30, 16));
        addRowButton.setPreferredSize(new java.awt.Dimension(90, 25));
        addRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addRowButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(addRowButton);

        inserRowButton.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        inserRowButton.setMnemonic('i');
        inserRowButton.setText("Insert");
        inserRowButton.setMaximumSize(new java.awt.Dimension(100, 40));
        inserRowButton.setMinimumSize(new java.awt.Dimension(30, 16));
        inserRowButton.setPreferredSize(new java.awt.Dimension(90, 25));
        inserRowButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                inserRowButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(inserRowButton);

        deleteButton.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        deleteButton.setMnemonic('d');
        deleteButton.setText("Delete");
        deleteButton.setMaximumSize(new java.awt.Dimension(100, 40));
        deleteButton.setMinimumSize(new java.awt.Dimension(30, 16));
        deleteButton.setPreferredSize(new java.awt.Dimension(90, 25));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(deleteButton);

        generateButton.setFont(new java.awt.Font("Default", 0, 12)); // NOI18N
        generateButton.setMnemonic('g');
        generateButton.setText("Generate");
        generateButton.setMaximumSize(new java.awt.Dimension(100, 40));
        generateButton.setMinimumSize(new java.awt.Dimension(50, 16));
        generateButton.setPreferredSize(new java.awt.Dimension(90, 25));
        generateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateButtonActionPerformed(evt);
            }
        });
        buttonsPanel.add(generateButton);

        stepThreePanel.add(buttonsPanel, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Step #3", stepThreePanel);

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
        fileMenu.setText("File");

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        helpMenu.setMnemonic('h');
        helpMenu.setText("Help");

        jMenuItem1.setMnemonic('a');
        jMenuItem1.setText("About");
        helpMenu.add(jMenuItem1);

        jMenuBar1.add(helpMenu);

        setJMenuBar(jMenuBar1);

        setSize(new java.awt.Dimension(610, 740));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void rowHeightSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rowHeightSpinnerStateChanged
        JSpinner spinner = (JSpinner) (evt.getSource());
        SpinnerNumberModel model = (SpinnerNumberModel) spinner.getModel();
        Number n = model.getNumber();
        generationDataTable.setRowHeight(n.intValue());
    }//GEN-LAST:event_rowHeightSpinnerStateChanged

    private void populateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_populateButtonActionPerformed
        try {
            String driver = connectionInfoPanel.getDriverTextField().getText();
            String dbUrl = connectionInfoPanel.getDbUrlTextField().getText();
            String user = connectionInfoPanel.getUserTextField().getText();
            String password = new String(connectionInfoPanel.getPasswordField().getPassword());
            String optionalConnProps = connectionInfoPanel.getConnectionPropertiesTextField().getText();
            String sqlSelect = sqlSelectPanel.getSqlSelectJTextArea().getText();
            try {
                generator.createInfo(driver, dbUrl, user, password, optionalConnProps, sqlSelect);
            } catch (ClassNotFoundException e) {
                connectionInfoPanel.loadDriver(false);
                generator.createInfo(driver, dbUrl, user, password, optionalConnProps, sqlSelect);
            }
            boolean enableSwing = generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected();
            boolean enableHtml = generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected();
            Vector info = generator.getInfo();
            Iterator iter = info.iterator();
            while (iter.hasNext()) {
                Vector colInfo = (Vector) iter.next();
                if (enableSwing) {
                    colInfo.set(4, "javax.swing.JTextField"); // Swing editor
                }
                if (enableHtml) {
                    colInfo.set(5, "text"); // html input
                }
            }
            Vector colNames = new Vector(8);
            colNames.add("DBColumn");
            colNames.add("Data Type");
            colNames.add("Field Name");
            colNames.add("Label");
            colNames.add("Swing Editor");
            colNames.add("HTML Input Type");
            colNames.add("Nullable");
            colNames.add("Visible");
            ((DefaultTableModel) generationDataTable.getModel()).setDataVector(info, colNames);
            setupTable();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Populate Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_populateButtonActionPerformed

    private void addRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addRowButtonActionPerformed
        Object[] rowData = new Object[8];
        rowData[7] = true;
        ((DefaultTableModel) generationDataTable.getModel()).addRow(rowData);
    }//GEN-LAST:event_addRowButtonActionPerformed

    private void jTabbedPane1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jTabbedPane1StateChanged
        int idx = ((JTabbedPane) evt.getSource()).getSelectedIndex();
        if (idx == 1) { //step #2
            boolean enable = generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected();
            swingFormPanel.enableComponents(enable);
            enable = generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected();
            htmlFormPanel.enableComponents(enable);
        } else if (idx == 2) { //step #3
            int rowCount = generationDataTable.getRowCount();
            boolean enable = generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected();
            if (!enable) {
                for (int row = 0; row < rowCount; row++) {
                    generationDataTable.setValueAt("", row, 4);
                }
            }
            enable = generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected();
            if (!enable) {
                for (int row = 0; row < rowCount; row++) {
                    generationDataTable.setValueAt("", row, 5);
                }
            }
        }
    }//GEN-LAST:event_jTabbedPane1StateChanged

    private void generateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_generateButtonActionPerformed
        if (generator == null) {
            generator = new ResultSet2JavaObjectsGenerator();
        }
        try {
            if (generationDataTable.getCellEditor() != null) {
                generationDataTable.getCellEditor().stopCellEditing();
            }

            Vector data = ((DefaultTableModel) generationDataTable.getModel()).getDataVector();
            if (data.isEmpty()) {
                throw new Exception("No Data");
            }
            String packageName = javaBeanPanel.getJavaBeanPackageTextField().getText();
            if (packageName == null || packageName.trim().equals("")) {
                throw new Exception("Package name is missing");
            }
            String className = javaBeanPanel.getJavaBeanClassNameTextField().getText();
            if (className == null || className.trim().equals("")) {
                throw new Exception("ClassName is missing");
            }
            String outputDir = javaBeanPanel.getJavaBeanOutputDirTextField().getText();
            if (outputDir == null || outputDir.trim().equals("")) {
                throw new Exception("Output Directory is missing");
            }
            boolean overwrite = javaBeanPanel.isOverrideFile();
            boolean checkNullInSetters = checkNullInSettersCheckBox.isSelected();
            generator.generate(data, packageName, className, outputDir, overwrite, checkNullInSetters);

            if (generationOptionsPanel.getGenerateHtmlFormCheckBox().isSelected()) {
                String htmlOutputDir = htmlFormPanel.getHtmlFormOutputDirTextField().getText();
                if (htmlOutputDir == null || htmlOutputDir.trim().equals("")) {
                    throw new Exception("HTML Form Output Directory is missing");
                }
                String fileName = htmlFormPanel.getHtmlFormFileNameTextField().getText();
                if (fileName == null || fileName.trim().equals("")) {
                    throw new Exception("HTML File Name is missing");
                }
                boolean overwriteHtmlFile = htmlFormPanel.isOverrideFile();
                generator.generate(data, htmlOutputDir, fileName, overwriteHtmlFile);
            }

            if (generationOptionsPanel.getGenerateSwingFormCheckBox().isSelected()) {
                String swingFormOutputDir = swingFormPanel.getSwingFormOutputDirTextField().getText();
                if (swingFormOutputDir == null || swingFormOutputDir.trim().equals("")) {
                    throw new Exception("Swing Form Output Directory is missing");
                }
                String swingFormPackageName = swingFormPanel.getSwingFormPackageTextField().getText();
                if (swingFormPackageName == null || swingFormPackageName.trim().equals("")) {
                    throw new Exception("Swing Form Package name is missing");
                }
                String swingFormClassName = swingFormPanel.getSwingFormClassNameTextField().getText();
                if (swingFormClassName == null || swingFormClassName.trim().equals("")) {
                    throw new Exception("Swing Form ClassName is missing");
                }
                boolean overwriteSwingForm = swingFormPanel.isOverwriteFile();
                String domainObject = javaBeanPanel.getJavaBeanPackageTextField().getText() + "." + javaBeanPanel.getJavaBeanClassNameTextField().getText();
                generator.generateSwingForm(data, swingFormPackageName, swingFormClassName, swingFormOutputDir, domainObject, overwriteSwingForm);
            }
            JOptionPane.showMessageDialog(this, "Success", "Generation", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            if (e instanceof java.lang.ClassNotFoundException) {
            }
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, e.getMessage(), "Generation Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_generateButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int rowCount = generationDataTable.getRowCount();
        if (rowCount > 0) {
            int row = generationDataTable.getSelectedRow();
            if (row >= 0) {
                ((DefaultTableModel) generationDataTable.getModel()).removeRow(row);
            } else {
                JOptionPane.showMessageDialog(this, "Select row first", "Delete Row", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void inserRowButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_inserRowButtonActionPerformed
        int row = generationDataTable.getSelectedRow();
        if (row >= 0) {
            Object[] rowData = new Object[8];
            rowData[7] = true;
            ((DefaultTableModel) generationDataTable.getModel()).insertRow(row, rowData);
        } else {
            JOptionPane.showMessageDialog(this, "Select row first", "Insert Row", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_inserRowButtonActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new GeneratorFrame().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addRowButton;
    private javax.swing.JPanel buttonsPanel;
    private javax.swing.JCheckBox checkNullInSettersCheckBox;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton generateButton;
    private javax.swing.JTable generationDataTable;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JButton inserRowButton;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JButton populateButton;
    private javax.swing.JLabel rowHeightLabel;
    private javax.swing.JSpinner rowHeightSpinner;
    private javax.swing.JPanel stepOnePanel;
    private javax.swing.JPanel stepThreePanel;
    private javax.swing.JPanel stepTwoPanel;
    // End of variables declaration//GEN-END:variables
    protected ResultSet2JavaObjectsGenerator generator = new ResultSet2JavaObjectsGenerator();
    protected ConnectionInfoPanel connectionInfoPanel = new ConnectionInfoPanel();
    protected SQLSelectPanel sqlSelectPanel = new SQLSelectPanel();
    protected GenerationOptionsPanel generationOptionsPanel = new GenerationOptionsPanel();
    protected JavaBeanPanel javaBeanPanel = new JavaBeanPanel();
    protected SwingFormPanel swingFormPanel = new SwingFormPanel();
    protected HTMLFormPanel htmlFormPanel = new HTMLFormPanel();
}

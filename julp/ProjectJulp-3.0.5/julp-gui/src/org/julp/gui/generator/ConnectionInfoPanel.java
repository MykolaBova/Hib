package org.julp.gui.generator;

import info.clearthought.layout.TableLayout;
import javax.swing.*;
import java.io.*;
import java.util.*;
import org.julp.gui.swing.filechooser.GenericFileFilter;

public class ConnectionInfoPanel extends javax.swing.JPanel {

    private static final long serialVersionUID = -4890943737172960679L;
    private ResultSet2JavaObjectsGenerator generator;

    public ConnectionInfoPanel() {
        init();
    }

    private void init() {

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
            {b, p, vs, p, vs, p, vs, p, vs, p, vg, p, b}
        };
        this.setLayout(new TableLayout(size));

        driverLabel.setDisplayedMnemonic('d');
        driverLabel.setFont(new java.awt.Font("Default", 0, 12));
        driverLabel.setText("Driver:");
        driverLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        driverLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        driverLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        driverLabel.setLabelFor(driverTextField);
        this.add(driverLabel, "1, 1");

        driverTextField.setFont(new java.awt.Font("Default", 0, 12));
        driverTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        driverTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        driverTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(driverTextField, "3, 1, 4, 1");

        dbUrlLabel.setDisplayedMnemonic('b');
        dbUrlLabel.setFont(new java.awt.Font("Default", 0, 12));
        dbUrlLabel.setText("DB URL:");
        dbUrlLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        dbUrlLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        dbUrlLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        dbUrlLabel.setLabelFor(dbUrlTextField);
        this.add(dbUrlLabel, "1, 3");

        dbUrlTextField.setFont(new java.awt.Font("Default", 0, 12));
        dbUrlTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        dbUrlTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        dbUrlTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(dbUrlTextField, "3, 3, 4, 3");

        userLabel.setDisplayedMnemonic('u');
        userLabel.setFont(new java.awt.Font("Default", 0, 12));
        userLabel.setText("User:");
        userLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        userLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        userLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        userLabel.setLabelFor(userTextField);
        this.add(userLabel, "1, 5");

        userTextField.setFont(new java.awt.Font("Default", 0, 12));
        userTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        userTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        userTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(userTextField, "3, 5, 4, 5");

        passwordLabel.setDisplayedMnemonic('p');
        passwordLabel.setFont(new java.awt.Font("Default", 0, 12));
        passwordLabel.setText("Password:");
        passwordLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        passwordLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        passwordLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        passwordLabel.setLabelFor(passwordField);
        this.add(passwordLabel, "1, 7");

        passwordField.setFont(new java.awt.Font("Default", 0, 12));
        passwordField.setMaximumSize(new java.awt.Dimension(400, 30));
        passwordField.setMinimumSize(new java.awt.Dimension(100, 16));
        passwordField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(passwordField, "3, 7, 4, 7");

        connectionPropertiesLabel.setDisplayedMnemonic('c');
        connectionPropertiesLabel.setFont(new java.awt.Font("Default", 0, 12));
        connectionPropertiesLabel.setText("Conn Props:");
        connectionPropertiesLabel.setMaximumSize(new java.awt.Dimension(120, 30));
        connectionPropertiesLabel.setMinimumSize(new java.awt.Dimension(90, 16));
        connectionPropertiesLabel.setPreferredSize(new java.awt.Dimension(100, 20));
        connectionPropertiesLabel.setLabelFor(connectionPropertiesTextField);
        this.add(connectionPropertiesLabel, "1, 9");

        connectionPropertiesTextField.setFont(new java.awt.Font("Default", 0, 12));
        connectionPropertiesTextField.setMaximumSize(new java.awt.Dimension(400, 30));
        connectionPropertiesTextField.setMinimumSize(new java.awt.Dimension(100, 16));
        connectionPropertiesTextField.setPreferredSize(new java.awt.Dimension(300, 20));
        this.add(connectionPropertiesTextField, "3, 9, 4, 9");

        testConnectionButton.setFont(new java.awt.Font("Default", 0, 12));
        testConnectionButton.setMaximumSize(new java.awt.Dimension(100, 40));
        testConnectionButton.setMinimumSize(new java.awt.Dimension(30, 16));
        testConnectionButton.setPreferredSize(new java.awt.Dimension(90, 25));
        testConnectionButton.setMnemonic('t');
        testConnectionButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                testConnectionButtonActionPerformed(evt);
            }
        });

        loadConnectionInfoButton.setFont(new java.awt.Font("Default", 0, 12));
        loadConnectionInfoButton.setMaximumSize(new java.awt.Dimension(100, 40));
        loadConnectionInfoButton.setMinimumSize(new java.awt.Dimension(30, 16));
        loadConnectionInfoButton.setPreferredSize(new java.awt.Dimension(90, 25));
        loadConnectionInfoButton.setMnemonic('l');
        loadConnectionInfoButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadConnectionInfoButtonActionPerformed(evt);
            }
        });

        saveConnectionInfoButton.setFont(new java.awt.Font("Default", 0, 12));
        saveConnectionInfoButton.setMaximumSize(new java.awt.Dimension(100, 40));
        saveConnectionInfoButton.setMinimumSize(new java.awt.Dimension(30, 16));
        saveConnectionInfoButton.setPreferredSize(new java.awt.Dimension(90, 25));
        saveConnectionInfoButton.setMnemonic('s');
        saveConnectionInfoButton.addActionListener(new java.awt.event.ActionListener() {

            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConnectionInfoButtonActionPerformed(evt);
            }
        });

        JPanel buttonsPanel = new JPanel();
        double[][] buttonsPanelSize = {{p, hg, p, hg, p}, {p}};
        buttonsPanel.setLayout(new TableLayout(buttonsPanelSize));
        buttonsPanel.add(loadConnectionInfoButton, "0, 0, r, t");
        buttonsPanel.add(testConnectionButton, "2, 0, r, t");
        buttonsPanel.add(saveConnectionInfoButton, "4, 0, r, t");
        this.add(buttonsPanel, "3, 11, 4, 11 r, t");
    }

    protected void testConnectionButtonActionPerformed(java.awt.event.ActionEvent evt) {
        Properties prop = new Properties();
        try {
            prop.setProperty("driver", driverTextField.getText());
            prop.setProperty("user", userTextField.getText());
            prop.setProperty("password", new String(passwordField.getPassword()));
            String optionalConnProps = connectionPropertiesTextField.getText();
            if (optionalConnProps != null && optionalConnProps.trim().length() > 0) {
                StringTokenizer st = new StringTokenizer(optionalConnProps, ",", false);
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    int idx = token.indexOf("=");
                    if (idx == -1) {
                        throw new IllegalArgumentException("Invalid optional connection properties format. \nIt must have format: name1=value1, name2=value2, ...");
                    }
                    String name = token.substring(0, idx);
                    String value = token.substring(idx + 1);
                    prop.setProperty(name, value);
                }
            }
            prop.setProperty("url", dbUrlTextField.getText());
            boolean success = generator.testConnection(prop);
            if (success) {
                JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), "Success", "Connect", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (ClassNotFoundException cnfe) {
            loadDriver(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadDriver(boolean showStatus) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Please select " + driverTextField.getText() + " jar");
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(new GenericFileFilter("jar", "Jar File"));
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(SwingUtilities.windowForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            if (file != null) {                
                Properties prop = new Properties();
                prop.setProperty("user", userTextField.getText());
                prop.setProperty("password", new String(passwordField.getPassword()));
                String optionalConnProps = connectionPropertiesTextField.getText();
                if (optionalConnProps != null && optionalConnProps.trim().length() > 0) {
                    StringTokenizer st = new StringTokenizer(optionalConnProps, ",", false);
                    while (st.hasMoreTokens()) {
                        String token = st.nextToken();
                        int idx = token.indexOf("=");
                        if (idx == -1) {
                            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), "Invalid optional connection properties format. \nIt must have format: name1=value1, name2=value2, ...", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        String name = token.substring(0, idx);
                        String value = token.substring(idx + 1);
                        prop.setProperty(name, value);
                    }
                }
                try {
                    String driverJarPath = null;
                    if (file != null && file.exists()) {
                        driverJarPath = file.getAbsolutePath();
                        prop.setProperty("driver", driverTextField.getText());
                        prop.setProperty("url", dbUrlTextField.getText());
                        generator.loadDriver(driverJarPath, prop);
                    } else {
                        throw new Exception("File " + driverJarPath + " is invalid");
                    }
                    if (showStatus) {
                        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), "Success", "Connect", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }        
    }

    protected void loadConnectionInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {
        loadConnectionProperties();
    }

    protected void saveConnectionInfoButtonActionPerformed(java.awt.event.ActionEvent evt) {
        storeConnectionProperties();
    }

    protected void loadConnectionProperties() {
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setFileFilter(new GenericFileFilter("properties", "Properties File"));
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(SwingUtilities.windowForComponent(this));
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fc.getSelectedFile();
            if (file != null) {
                java.io.InputStream inStream = null;
                Properties loadProps = new Properties();
                try {
                    inStream = file.toURI().toURL().openStream();
                    loadProps.load(inStream);
                    String loadDriver = loadProps.getProperty("driver");
                    String url = loadProps.getProperty("url");
                    String user = loadProps.getProperty("user");
                    String password = loadProps.getProperty("password");
                    String optionalConnProps = loadProps.getProperty("optional_conn_props");

                    driverTextField.setText(loadDriver);
                    userTextField.setText(user);
                    passwordField.setText(password);
                    dbUrlTextField.setText(url);
                    connectionPropertiesTextField.setText(optionalConnProps);
                } catch (java.io.IOException ioe) {
                    ioe.printStackTrace();
                    JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    try {
                        if (inStream != null) {
                            inStream.close();
                        }
                    } catch (java.io.IOException ioe) {
                        ioe.printStackTrace();
                        JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            fc = null;
        }
    }

    protected void storeConnectionProperties() {
        FileWriter fw = null;
        try {
            JFileChooser fc = new JFileChooser();
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fc.setFileFilter(new GenericFileFilter("properties", "Properties File"));
            fc.setAcceptAllFileFilterUsed(false);
            int returnVal = fc.showSaveDialog(SwingUtilities.windowForComponent(this));
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fc.getSelectedFile();
                path = file.getAbsolutePath();
                if (!path.toLowerCase().endsWith(".properties")) {
                    path = path + ".properties";
                }
                if (file.exists()) {
                    file.delete();
                }
                if (file != null) {
                    fw = new FileWriter(path, false);
                    driver = getDriverTextField().getText();
                    String url = getDbUrlTextField().getText();
                    String user = getUserTextField().getText();
                    String password = new String(getPasswordField().getPassword());
                    String optionalConnProps = getConnectionPropertiesTextField().getText();
                    props.setProperty("driver", driver);
                    props.setProperty("url", url);
                    props.setProperty("user", user);
                    props.setProperty("password", password);
                    System.setProperty("driver", driver);
                    System.setProperty("url", url);
                    System.setProperty("user", user);
                    System.setProperty("password", password);

                    String endline = "\n";
                    fw.write("driver=" + driver);
                    fw.write(endline);
                    fw.write("url=" + url);
                    fw.write(endline);
                    fw.write("user=" + user);
                    fw.write(endline);
                    fw.write("password=" + password);
                    fw.write(endline);
                    fw.write("optional_conn_props=" + optionalConnProps);
                    fw.write(endline);
                }
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), fnfe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
                JOptionPane.showMessageDialog(SwingUtilities.windowForComponent(this), ioe.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void setGenerator(ResultSet2JavaObjectsGenerator generator) {
        this.generator = generator;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(null);

    }//GEN-END:initComponents

    public javax.swing.JTextField getConnectionPropertiesTextField() {
        return connectionPropertiesTextField;
    }

    public javax.swing.JTextField getDbUrlTextField() {
        return dbUrlTextField;
    }

    public javax.swing.JTextField getDriverTextField() {
        return driverTextField;
    }

    public javax.swing.JCheckBox getGenerateHtmlFormCheckBox() {
        return generateHtmlFormCheckBox;
    }

    public javax.swing.JCheckBox getGenerateJavaBeanCheckBox() {
        return generateJavaBeanCheckBox;
    }

    public javax.swing.JCheckBox getGenerateSwingFormCheckBox() {
        return generateSwingFormCheckBox;
    }

    public javax.swing.JPasswordField getPasswordField() {
        return passwordField;
    }

    public javax.swing.JButton getSaveConnectionInfoButton() {
        return saveConnectionInfoButton;
    }

    public javax.swing.JButton getTestConnectionButton() {
        return testConnectionButton;
    }

    public javax.swing.JTextField getUserTextField() {
        return userTextField;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected javax.swing.JLabel driverLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField driverTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel dbUrlLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField dbUrlTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel userLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField userTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel passwordLabel = new javax.swing.JLabel();
    protected javax.swing.JPasswordField passwordField = new javax.swing.JPasswordField();
    protected javax.swing.JLabel connectionPropertiesLabel = new javax.swing.JLabel();
    protected javax.swing.JTextField connectionPropertiesTextField = new javax.swing.JTextField();
    protected javax.swing.JLabel sqlSelectLabel = new javax.swing.JLabel();
    protected javax.swing.JCheckBox generateJavaBeanCheckBox = new javax.swing.JCheckBox();
    protected javax.swing.JCheckBox generateSwingFormCheckBox = new javax.swing.JCheckBox();
    protected javax.swing.JCheckBox generateHtmlFormCheckBox = new javax.swing.JCheckBox();
    protected javax.swing.JButton testConnectionButton = new javax.swing.JButton("Test");
    protected javax.swing.JButton saveConnectionInfoButton = new javax.swing.JButton("Save");
    protected javax.swing.JButton loadConnectionInfoButton = new javax.swing.JButton("Load");
    protected Properties props = new Properties();
    protected String path = null;
    protected String driver = null;
}

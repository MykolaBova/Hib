package org.julp.gui.swing.login;

import info.clearthought.layout.TableLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.AccountException;
import javax.security.auth.login.CredentialException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import org.julp.security.UsernamePasswordCallbackHandler;

public class LoginlDialog extends javax.swing.JDialog {

    private static final long serialVersionUID = 313361872378803690L;
    public static final String SUBJECT = "subject";

    public LoginlDialog(Frame frame, String iconURL, LoginContext loginContext, UsernamePasswordCallbackHandler callbackHandler) {
        super(frame);
        initComponents();
        setIcon(iconURL);

        logonPanel.add(imageLabel, "1, 1, 6, 1");
        logonPanel.add(userNameLabel, "1, 3");
        logonPanel.add(userNameTextField, "3, 3, 6, 3");
        logonPanel.add(passwordLabel, "1, 5");
        logonPanel.add(passwordField, "3, 5, 6, 5");
        logonPanel.add(okButton, "4, 7");
        logonPanel.add(cancelButton, "6, 7");
        logonPanel.add(msgLabel, "0, 9, 7, 9");

        getRootPane().setDefaultButton(okButton);
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", escapeAction);

        try {
            this.loginContext = loginContext;
            this.callbackHandler = callbackHandler;

//            loginContext.login();
//
//            callbackHandler.handle(callbacks);
//
//            for (int i = 0; i < callbacks.length; i++) {
//                if (callbacks[i] instanceof NameCallback) {
//                    System.out.println("username prompt: " + ((NameCallback) callbacks[i]).getPrompt());
//                    //((NameCallback) callbacks[i]).setName(username);
//                } else if (callbacks[i] instanceof PasswordCallback) {
//                    System.out.println("password prompt: " + ((PasswordCallback) callbacks[i]).getPrompt());
//                    //((PasswordCallback) callbacks[i]).setPassword(password);
//                }
//            }

        } catch (Throwable t) {
            setMessage(t.getMessage());
            t.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private boolean login() {
        // Now authenticate. If we don't get an exception, we succeeded
        try {
            numberOfAttemps++;
            if (numberOfAttemps > maxNumberOfAttemps) {
                throw new LoginException("Too many login attemps");
            }
            if (callbackHandler == null) {
                throw new NullPointerException("CallbackHandler not initialized");
            }
            callbackHandler.setUsername(userNameTextField.getText());
            callbackHandler.setPassword(passwordField.getPassword());
            loginContext.login();
            //callbackHandler.clearPassword();
            return true;
        } catch (Throwable t) {
            if (t instanceof CredentialException) {
                passwordField.requestFocus();
                passwordField.setCaretPosition(passwordField.getPassword().length);
                passwordField.moveCaretPosition(0);
            } else if (t instanceof AccountException) {
                userNameTextField.requestFocus();
                userNameTextField.setCaretPosition(userNameTextField.getText().length());
                userNameTextField.moveCaretPosition(0);
            } else if (t instanceof LoginException) {
                if (t.getMessage().equals("Too many login attemps")) {
                    JOptionPane.showMessageDialog(this, t.getMessage(), "Login Error", JOptionPane.ERROR_MESSAGE);
                    doClose(-3);
                }
            }
            setMessage(t.getMessage());
            t.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
            callbackHandler.clearPassword();
            return false;
        }
    }

    private void setIcon(String iconURL) {
        try {
            Icon icon = new ImageIcon(new java.net.URL(iconURL));
            imageLabel.setIcon(icon);
            int h = icon.getIconHeight();
            imageLabel.setSize(imageLabel.getWidth(), h);
            this.setSize(getWidth(), getHeight() + h);
            logonPanel.setSize(this.getSize());
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, getWidth(), this.getHeight());
        } catch (java.net.MalformedURLException e) {
            imageLabel.setVisible(false);
            imageLabel.setSize(0, 0);
            logonPanel.setSize(this.getSize());
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            setBounds((screenSize.width - this.getWidth()) / 2, (screenSize.height - this.getHeight()) / 2, getWidth(), this.getHeight());
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        imageLabel = new javax.swing.JLabel();
        userNameLabel = new javax.swing.JLabel();
        userNameTextField = new javax.swing.JTextField();
        passwordLabel = new javax.swing.JLabel();
        passwordField = new javax.swing.JPasswordField();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        msgLabel = new javax.swing.JLabel();
        logonPanel = new javax.swing.JPanel();

        imageLabel.setBackground(new java.awt.Color(255, 255, 255));
        imageLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        imageLabel.setInheritsPopupMenu(false);
        imageLabel.setRequestFocusEnabled(false);
        imageLabel.setVerifyInputWhenFocusTarget(false);
        userNameLabel.setDisplayedMnemonic('n');
        userNameLabel.setLabelFor(userNameTextField);
        userNameLabel.setText("User Name:");
        userNameTextField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                userNameTextFieldFocusGained(evt);
            }
        });

        passwordLabel.setDisplayedMnemonic('p');
        passwordLabel.setLabelFor(passwordField);
        passwordLabel.setText("Password:");
        passwordField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                passwordFieldFocusGained(evt);
            }
        });

        okButton.setText("OK");
        okButton.setMaximumSize(new java.awt.Dimension(75, 25));
        okButton.setMinimumSize(new java.awt.Dimension(75, 25));
        okButton.setPreferredSize(new java.awt.Dimension(75, 25));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        msgLabel.setText("Please login...");
        msgLabel.setToolTipText("");
        msgLabel.setVerifyInputWhenFocusTarget(false);
        msgLabel.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        getContentPane().setLayout(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Login");
        setAlwaysOnTop(true);
        setName("LogonDialog");
        setUndecorated(true);
        info.clearthought.layout.TableLayout tableLayout2 = new info.clearthought.layout.TableLayout();
        tableLayout2.setColumn(new double[]{10.0, TableLayout.PREFERRED, 5.0, TableLayout.FILL, TableLayout.PREFERRED, 5.0, TableLayout.PREFERRED, 10.0});
        tableLayout2.setRow(new double[] {10.0, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 5.0, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 15.0, TableLayout.PREFERRED, 10.0});
        logonPanel.setLayout(tableLayout2);

        logonPanel.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.EtchedBorder(), new javax.swing.border.EmptyBorder(new java.awt.Insets(10, 10, 0, 10))));
        logonPanel.setMaximumSize(new java.awt.Dimension(0, 0));
        logonPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        logonPanel.setPreferredSize(new java.awt.Dimension(0, 0));
        getContentPane().add(logonPanel);
        logonPanel.setBounds(0, 0, 0, 0);

        java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        setBounds((screenSize.width-400)/2, (screenSize.height-200)/2, 400, 200);
    }
    // </editor-fold>//GEN-END:initComponents

    private void passwordFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_passwordFieldFocusGained
        passwordField.setCaretPosition(passwordField.getPassword().length);
        passwordField.moveCaretPosition(0);
    }//GEN-LAST:event_passwordFieldFocusGained

    private void userNameTextFieldFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_userNameTextFieldFocusGained
        userNameTextField.setCaretPosition(userNameTextField.getText().length());
        userNameTextField.moveCaretPosition(0);
    }//GEN-LAST:event_userNameTextFieldFocusGained

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (login()) {
            try {
                setSubject(loginContext.getSubject());
            } catch (Exception e) {
                e.printStackTrace();
                setMessage(e.getMessage());
                return;
            }
            setVisible(false);
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(-1);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void doClose(int retStatus) {
        setVisible(false);
        dispose();
        System.exit(retStatus);
    }

    public void setMessage(java.lang.String msg) {
        msgLabel.setToolTipText(msg == null ? "Login Error" : msg);
        msgLabel.setText(msg == null ? "Login Error" : msg);
    }

    public String getIconURL() {
        return iconURL;
    }

    public void setIconURL(String iconURL) {
        this.iconURL = iconURL;
    }

    public void setMaxNumberOfAttemps(int maxNumberOfAttemps) {
        this.maxNumberOfAttemps = maxNumberOfAttemps;
    }

    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        changes.addPropertyChangeListener(l);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        changes.removePropertyChangeListener(l);
    }

    private void setSubject(Subject subject) {
        changes.firePropertyChange(SUBJECT, this.subject, subject);
        this.subject = loginContext.getSubject();
    }

    public void setLoginContext(LoginContext loginContext) {
        this.loginContext = loginContext;
    }
    KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
    Action escapeAction = new AbstractAction() {
        private static final long serialVersionUID = -5644390861803492172L;

        @Override
        public void actionPerformed(ActionEvent e) {
            doClose(-1);
        }
    };
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel imageLabel;
    private javax.swing.JPanel logonPanel;
    private javax.swing.JLabel msgLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPasswordField passwordField;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel userNameLabel;
    private javax.swing.JTextField userNameTextField;
    // End of variables declaration//GEN-END:variables
    private String iconURL;
    private LoginContext loginContext = null;
    private UsernamePasswordCallbackHandler callbackHandler;
    private Callback[] callbacks;
    private int numberOfAttemps = 0;
    private int maxNumberOfAttemps = 3;
    private PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private Subject subject;

}

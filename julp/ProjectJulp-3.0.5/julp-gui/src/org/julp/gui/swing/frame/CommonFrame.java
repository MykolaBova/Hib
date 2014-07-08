package org.julp.gui.swing.frame;

import java.util.*;
import javax.swing.*;

public abstract class CommonFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = -5720528950847184414L;

    public CommonFrame() {
    }

    public CommonFrame(String title) {
        super(title);
    }

    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setFont(null);
        setName("common_frame");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm

    public java.util.Map getUISettings() {
        return IUSettings;
    }

    public void setUISettings(java.util.Map uiSettings) {
        this.IUSettings = uiSettings;
        java.util.Enumeration defaultUI = UIManager.getDefaults().keys();
        String ui;
        //javax.swing.plaf.FontUIResource sansPlain12 = new javax.swing.plaf.FontUIResource("sans", java.awt.Font.PLAIN, 12);
        while (defaultUI.hasMoreElements()) {
            ui = defaultUI.nextElement().toString();
            Object obj = uiSettings.get(ui);
            if (obj != null) {
                UIManager.getDefaults().put(ui, obj);
            }

            //if (ui.endsWith(".font")){
            //UIManager.getDefaults().put(ui, sansPlain12);
            //}
            //SwingUtilities.updateComponentTreeUI(fr_Jf);
        }

    }
//    public void setStatus(java.lang.String status) {
//        this.status.setText(status);
//    }    
//    
//    public String getStatus() {
//        return this.status.getText();
//    }
    /** Getter for property desktopPane.
     * @return Value of property desktopPane.
     *
     */
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    protected Map IUSettings;
    //private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(getClass());
    //private javax.swing.JDesktopPane desktopPane;
    //private javax.swing.JLabel status;    
    //status = new javax.swing.JLabel();
    //desktopPane = new javax.swing.JDesktopPane();    
}

/*
CheckBox.font
Tree.font
Viewport.font
ProgressBar.font
RadioButtonMenuItem.font
FormattedTextField.font
ToolBar.font
ColorChooser.font
ToggleButton.font
Panel.font
TextArea.font
Menu.font
Spinner.font
TableHeader.font
TextField.font
OptionPane.font
MenuBar.font
Button.font
Label.font
PasswordField.font
ScrollPane.font
MenuItem.font
DesktopIcon.font
ToolTip.font
List.font
EditorPane.font
Table.font
TabbedPane.font
RadioButton.font
CheckBoxMenuItem.font
TextPane.font
PopupMenu.font
TitledBorder.font
ComboBox.font
 */

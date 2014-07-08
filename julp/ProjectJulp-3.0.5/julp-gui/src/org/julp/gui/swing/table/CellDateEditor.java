package org.julp.gui.swing.table;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EventObject;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

public class CellDateEditor extends DefaultCellEditor{
    
    protected JFormattedTextField ftf;
    protected SimpleDateFormat dateFormat;
    protected int defaultAlignment = JLabel.RIGHT;
    
    //protected Date defaultValue = new Date(-62135751600000L);
    protected Date defaultValue = null;
    protected boolean throwExceptionOnInvalidDate;
    protected boolean throwExceptionOnNullDate;
    protected String nullValueMessage = "Missing Date value";
    protected String invalidValueMessage = "Invalid Date";
    
    public CellDateEditor(String format, String mask) {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        ftf.selectAll();
        ftf.setBorder(null);
        ftf.setHorizontalAlignment(defaultAlignment);
        //ftf.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        
        //ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ftf.setFocusLostBehavior(JFormattedTextField.COMMIT);
        
        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(
                KeyEvent.VK_ENTER, 0),
                "check");
        ftf.getActionMap().put("check", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) { //The text is invalid.
                    if (userSaysRevert()) { //reverted
                        ftf.postActionEvent(); //inform the editor
                    }
                } else try {              //The text is valid,
                    ftf.commitEdit();     //so use it.
                    ftf.postActionEvent(); //stop editing
                } catch (java.text.ParseException exc) { }
            }
        });
        
        dateFormat = new SimpleDateFormat(format);
        dateFormat.setLenient(false);
        
        DefaultFormatterFactory factory = new DefaultFormatterFactory();
        factory.setDisplayFormatter(createDisplayFormatter(mask));
        factory.setDefaultFormatter(new DateFormatter(dateFormat));
        factory.setEditFormatter(createDisplayFormatter(mask));
        ftf.setFormatterFactory(factory);
    }
    
    
//     public boolean stopCellEditing() {
//
//        if (ftf.isEditValid()) {
//            try {
//                ftf.commitEdit();
//            } catch (java.text.ParseException exc) {
//                exc.printStackTrace();
//            }
//
//        } else { //text is invalid
//            System.out.print("************");
//        }
//        return super.stopCellEditing();
//    }
//
//    ////Override to ensure that the value remains an Date.
//    public Object getCellEditorValue() {
//        Object obj = ftf.getValue();
//        if (obj == null){
//            return null;
//        }
//        if (obj instanceof Date) {
//            return obj;
//        }else if (obj instanceof String){
//            try {
//                return dateFormat.parse((String) obj);
//            } catch (ParseException exc) {
//                System.err.println("getCellEditorValue: can't parse o: " + obj);
//                return null;
//            }
//        }
//        return obj;
//
////        else {
////            if (DEBUG) {
////                System.out.println("getCellEditorValue: o isn't a Date");
////            }
////            try {
////                return dateFormat.parseObject(o.toString());
////            } catch (ParseException exc) {
////                System.err.println("getCellEditorValue: can't parse o: " + o);
////                return null;
////            }
////        }
//    }
    
    
   
    public boolean stopCellEditing() {
        if (ftf.isEditValid()) {
            try {
                ftf.commitEdit();
            } catch (java.text.ParseException exc) {
                //ftf.setValue(defaultValue);
                //super.cancelCellEditing();
            }
        } else { //text is invalid
            //ftf.setValue(defaultValue);
            //super.cancelCellEditing();
        }
        return super.stopCellEditing();
        
    }
    
    
//    //Override to ensure that the value remains a Date.
//    public Object getCellEditorValue() {
//        JFormattedTextField ftf = (JFormattedTextField) getComponent();
//        Object o = ftf.getValue();
//        if (o instanceof Date) {
//            return o;
//        } else if (o instanceof String) {
//            //System.out.println("getCellEditorValue: o isn't a Date");
//            try {
//                return dateFormat.parse((String) o);
//                
//            } catch (ParseException exc) {
//                System.err.println("getCellEditorValue: can't parse: " + o);
//                return null;
//            }
//        }
//        return o;
//    }
    
    /* Override to ensure that the value remains an Date. */
    public Object getCellEditorValue() {
        Object obj = ftf.getValue();
        System.out.println("1: " + obj);
        if (obj == null){
            obj = defaultValue;
        }
        if (obj instanceof Date) {
            //return obj;
        }else if (obj instanceof String){
            try {
                obj = dateFormat.parse((String) obj);
            } catch (ParseException exc) {
                obj = defaultValue;
            }
        }
        System.out.println("2: " + obj);
        return obj;
    }
    
    protected MaskFormatter createDisplayFormatter(String mask) {
        MaskFormatter formatter = null;
        try {
            formatter = new MaskFormatter(mask);
        } catch (java.text.ParseException exc) {
            throw new IllegalArgumentException("Invalid format: " + mask);
        }
        return formatter;
    }
    
//    public void setClickCountToStart(int count) {
//        super.setClickCountToStart(1);
//    }
//
//    public int getClickCountToStart(){
//        return 1;
//    }
    
    public int getDefaultAlignment() {
        return defaultAlignment;
    }
    
    public void setDefaultAlignment(int defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }
    
    public Date getDefaultValue() {
        return defaultValue;
    }
    
    public void setDefaultValue(Date defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    public boolean isThrowExceptionOnInvalidDate() {
        return throwExceptionOnInvalidDate;
    }
    
    public void setThrowExceptionOnInvalidDate(boolean throwExceptionOnInvalidDate) {
        this.throwExceptionOnInvalidDate = throwExceptionOnInvalidDate;
    }
    
    public boolean isThrowExceptionOnNullDate() {
        return throwExceptionOnNullDate;
    }
    
    public void setThrowExceptionOnNullDate(boolean throwExceptionOnNullDate) {
        this.throwExceptionOnNullDate = throwExceptionOnNullDate;
    }
    
    public String getNullValueMessage() {
        return nullValueMessage;
    }
    
    public void setNullValueMessage(String nullValueMessage) {
        this.nullValueMessage = nullValueMessage;
    }
    
    public String getInvalidValueMessage() {
        return invalidValueMessage;
    }
    
    public void setInvalidValueMessage(String invalidValueMessage) {
        this.invalidValueMessage = invalidValueMessage;
    }
    
    public boolean isCellEditable(EventObject evt) {
        if (evt instanceof MouseEvent) {
            int clickCount;
            
            // For single-click activation
            clickCount = 1;
            
//            // For double-click activation
//            clickCount = 2;
//
//            // For triple-click activation
//            clickCount = 3;
            
            return ((MouseEvent)evt).getClickCount() >= clickCount;
        }
        return true;
    }
    
    /**
     * Lets the user know that the text they entered is
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false,
     * indicating that the user wants to continue editing.
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
                "Revert"};
                int answer = JOptionPane.showOptionDialog(
                        SwingUtilities.getWindowAncestor(ftf),
                        "The value must be a Date.\n"
                        + "You can either continue editing "
                        + "or revert to the last valid value.",
                        "Invalid Text Entered",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE,
                        null,
                        options,
                        options[1]);
                
                if (answer == 1) { //Revert!
                    ftf.setValue(ftf.getValue());
                    return true;
                }
                return false;
    }
    
}

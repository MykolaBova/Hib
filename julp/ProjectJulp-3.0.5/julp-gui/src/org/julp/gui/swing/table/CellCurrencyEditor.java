package org.julp.gui.swing.table;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.EventObject;
import javax.swing.DefaultCellEditor;
//import javax.swing.DefaultCellEditor.EditorDelegate;
import javax.swing.JFormattedTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.NumberFormatter;

public class CellCurrencyEditor extends DefaultCellEditor {
    
    private JFormattedTextField ftf;
    private NumberFormat amountDisplayFormat;
    private NumberFormat amountEditFormat;
    private NumberFormat currencyFormat;
    // private NumberFormat percentDisplayFormat;
    //private NumberFormat percentEditFormat;
    //private NumberFormat paymentFormat;
    
    public CellCurrencyEditor(JFormattedTextField tf) {
	super(tf);
	ftf = (JFormattedTextField) getComponent();
	//ftf.selectAll();
//        setUpFormats();
//        DefaultFormatterFactory dff = new DefaultFormatterFactory(
//                new NumberFormatter(amountDisplayFormat),
//                new NumberFormatter(amountEditFormat),
//                new NumberFormatter());
//        DefaultFormatterFactory dff = new DefaultFormatterFactory();
//        dff.setDefaultFormatter((new NumberFormatter(amountEditFormat)));
//        dff.setEditFormatter((new NumberFormatter(amountEditFormat)));
//        dff.setDisplayFormatter((new NumberFormatter(amountEditFormat)));
	
	//ftf.setFormatterFactory(dff);
	//ftf.selectAll();
	NumberFormat amountFormat = NumberFormat.getCurrencyInstance();
	amountFormat.setMinimumFractionDigits(2);
	NumberFormatter currencyFormatter = new NumberFormatter(amountFormat);
	currencyFormatter.install(ftf);
	ftf.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
	ftf.setHorizontalAlignment(SwingConstants.RIGHT);
	ftf.setBorder(null);
	
/*
	//React when the user presses Enter while the editor is
	//active.  (Tab is handled as specified by
	//JFormattedTextField's focusLostBehavior property.)
	ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
	ftf.getActionMap().put("check", new AbstractAction() {
	    public void actionPerformed(ActionEvent e) {
		if (!ftf.isEditValid()) { //The text is invalid.
		    //if (userSaysRevert()) { //reverted
			ftf.postActionEvent(); //inform the editor
		    //}
		} else try {               //The text is valid,
		    ftf.commitEdit();      //so use it.
		    ftf.postActionEvent(); //stop editing
		} catch (java.text.ParseException exc) { }
	    }
	});
 */
	
 /*
  
	delegate = new EditorDelegate() {
  
	    public void setValue(Object param) {
		Number value = null;
		Number defaultNumber = null;
		try{
		    defaultNumber = currencyFormat.parse("0.0");
		    if (param == null) {
			//Number value = (Number) param;
			tf.setValue(currencyFormat.format(0.0));
		    } else {
			//double d = value.doubleValue();
			//String format = currencyFormat.format(d);
			String format = currencyFormat.format(param);
			tf.setValue(format);
		    }
		}catch (Exception e){
		    tf.setValue(defaultNumber);
		}
	    }
  
	    public Object getCellEditorValue() {
		Number defaultNumber = new Double("0.0");
		try {
		    //defaultNumber = currencyFormat.parse("0.0");
		    String text = ftf.getText();
		    Number number = currencyFormat.parse(text);
		    //double parsed = number.doubleValue();
		    //Double d = new Double(parsed);
		    //return d;
		    return number;
		} catch (ParseException e) {
		    e.printStackTrace();
		    //return new Double(0.0);
		    return defaultNumber;
		}
	    }
	};
  */
    }
    
//    private void setUpFormats() {
//        amountDisplayFormat = NumberFormat.getCurrencyInstance();
//        amountDisplayFormat.setMinimumFractionDigits(2);
//        amountEditFormat = NumberFormat.getCurrencyInstance();
//        amountEditFormat.setMinimumFractionDigits(2);
//        currencyFormat = NumberFormat.getCurrencyInstance();
//    }
    
    
    //Override to invoke setValue on the formatted text field.
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
	final JFormattedTextField ftf =
	(JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
	ftf.setValue(value == null?BigDecimal.ZERO:value);
	//ftf.selectAll();
	if (isSelected) {
//            formatLabel.setForeground(table.getSelectionBackground());
//            formatLabel.setBackground(table.getSelectionForeground());
	    ftf.setForeground(table.getSelectionForeground());
	    //ftf.setBackground(table.getSelectionBackground());
	    ftf.setOpaque(true);	    
	    String text = ftf.getText();
	    if (text != null && text.length() > 0){
		//System.out.println("selectText() text: " + text);
		//ftf.setCaretPosition(text.length());
		//ftf.moveCaretPosition(0);
		ftf.setBorder(javax.swing.border.LineBorder.createBlackLineBorder());	
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			ftf.selectAll();
			ftf.setCaretPosition(ftf.getText().length());
		        ftf.moveCaretPosition(0);
		    }
		});
	    }
	}
	return ftf;
    }
    
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
    
    
    //Override to ensure that the value remains a NUMBER.
    public Object getCellEditorValue() {
	//JFormattedTextField tf = (JFormattedTextField)getComponent();
	//ftf.selectAll();
	Object o = ftf.getValue();
	if (o instanceof Number) {
	    return ((Number) o);
	} else {
	    //if (DEBUG) {
	    System.out.println("getCellEditorValue: " + o + " isn't a Number");
	    //}
//            try {
	    //return integerFormat.parseObject(o.toString());
	    return ((Number) BigDecimal.ZERO);
//            } catch (ParseException exc) {
//                System.err.println("getCellEditorValue: can't parse o: " + o);
//                return null;
//            }
	}
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
    
//    /**
//     * Lets the user know that the text they entered is
//     * bad. Returns true if the user elects to revert to
//     * the last good value.  Otherwise, returns false,
//     * indicating that the user wants to continue editing.
//     */
//    protected boolean userSaysRevert() {
//        Toolkit.getDefaultToolkit().beep();
//        ftf.selectAll();
//        Object[] options = {"Edit",
//                "Revert"};
//                int answer = JOptionPane.showOptionDialog(
//                        SwingUtilities.getWindowAncestor(ftf),
//                        "The value must be a Number.\n"
//                        + "You can either continue editing "
//                        + "or revert to the last valid value.",
//                        "Invalid Text Entered",
//                        JOptionPane.YES_NO_OPTION,
//                        JOptionPane.ERROR_MESSAGE,
//                        null,
//                        options,
//                        options[1]);
//
//                if (answer == 1) { //Revert!
//                    ftf.setValue(ftf.getValue());
//                    return true;
//                }
//                return false;
//    }
}

//new JFormattedTextField(
//                            new DefaultFormatterFactory(
//                                new NumberFormatter(amountDisplayFormat),
//                                new NumberFormatter(amountDisplayFormat),
//                                new NumberFormatte
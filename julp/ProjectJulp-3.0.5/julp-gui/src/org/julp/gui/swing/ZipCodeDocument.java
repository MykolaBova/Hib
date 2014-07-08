package org.julp.gui.swing;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

public class ZipCodeDocument extends PlainDocument {

    private static final long serialVersionUID = -1820497981833615558L;
    StringBuffer currentValue = new StringBuffer();

    @Override
    public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
        if (str == null) {
            return;
        }
        if (!str.equals("")) {
            try {
                Integer.parseInt(str);
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                throw new BadLocationException(str, offset);
            }
        }
        //Makes up for the dash '-' that is in the super Content but not in currentValue
        if (offset > 6) {
            offset--;
        }
        currentValue.insert(offset, str);
        if (currentValue.length() > 9) {
            Toolkit.getDefaultToolkit().beep();
            throw new BadLocationException(str, offset);
        }
        String displayValue;
        if (currentValue.length() > 5) {
            displayValue = currentValue.substring(0, 5) + "-" + currentValue.substring(5);
        } else {
            displayValue = currentValue.toString();
        }
        super.remove(0, getLength());
        super.insertString(0, displayValue, a);
    }

    @Override
    public void remove(int offs, int len) throws BadLocationException {
        String text = getText(0, getLength());
        text = text.substring(0, offs) + text.substring(offs + len, getLength());
        text = text.replaceAll("-", "");
        currentValue.setLength(0);
        insertString(0, text, null);
    }
}

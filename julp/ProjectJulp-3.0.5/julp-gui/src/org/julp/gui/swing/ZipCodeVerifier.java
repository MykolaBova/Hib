package org.julp.gui.swing;

import java.awt.Toolkit;
import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class ZipCodeVerifier extends InputVerifier {

    @Override
    public boolean verify(JComponent input) {
        boolean returnValue;
        JTextField field = (JTextField) input;
        String text = field.getText().replaceAll("-", "");
        if (text.length() == 0) {
            returnValue = true;
        } else if (text.length() == 5 || text.length() == 9) {
            try {
                Integer.parseInt(text);
                returnValue = true;
            } catch (NumberFormatException e) {
                Toolkit.getDefaultToolkit().beep();
                returnValue = false;
            }
        } else {
            java.awt.Toolkit.getDefaultToolkit().beep();
            returnValue = false;
        }
        return returnValue;
    }
}

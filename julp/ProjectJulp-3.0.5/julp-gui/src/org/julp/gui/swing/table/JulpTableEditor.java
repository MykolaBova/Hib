package org.julp.gui.swing.table;

import javax.swing.*;

public class JulpTableEditor extends DefaultCellEditor{
    private static final long serialVersionUID = 2272387536941383677L;
    
    public JulpTableEditor(JTextField textField) {
        super(textField);
        setClickCountToStart(1);
    }
    
    public JulpTableEditor(JCheckBox checkBox) {
        super(checkBox);
        setClickCountToStart(1);
    }
    
    public JulpTableEditor(JComboBox comboBox) {
        super(comboBox);
        setClickCountToStart(1);
    }    
}

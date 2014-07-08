package org.julp.gui.swing.table;

import java.awt.Component;
import java.math.BigDecimal;
import java.text.NumberFormat;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

//NumberFormat format = NumberFormat.getCurrencyInstance();   
//table.getColumnModel().getColumn(1).setCellRenderer(new CellCurrencyRenderer(format));
public class CellCurrencyRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = 5363445974900268092L;
    protected String defaultNullFormat = "";
    protected NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    public CellCurrencyRenderer() {
    }

    public CellCurrencyRenderer(NumberFormat currencyFormat, String defaultNullFormat) {
        this.currencyFormat = currencyFormat;
        this.defaultNullFormat = defaultNullFormat;
    }

    public CellCurrencyRenderer(NumberFormat currencyFormat) {
        this.currencyFormat = currencyFormat;
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int col) {
        String mask;
        Number numericValue = new BigDecimal("0.00");
        if (value == null) {
            mask = defaultNullFormat;
        } else {
            if (value instanceof String) {
                System.out.println(value + " " + value.getClass());
                try {
                    numericValue = new BigDecimal((String) value);
                } catch (NumberFormatException nfe) {
                    numericValue = new BigDecimal("0.00");
                }

            } else if (value instanceof Number) {
                numericValue = (Number) value;
            }
            mask = currencyFormat.format(numericValue);
        }
        JLabel formatLabel = new JLabel(mask, SwingConstants.RIGHT);
        if (isSelected) {
            formatLabel.setBackground(table.getSelectionBackground());
            formatLabel.setOpaque(true);
            formatLabel.setForeground(table.getSelectionForeground());
        }
        if (hasFocus) {
//            formatLabel.setForeground(table.getSelectionBackground());
//            formatLabel.setBackground(table.getSelectionForeground());
            formatLabel.setForeground(table.getSelectionForeground());
            formatLabel.setBackground(table.getSelectionBackground());
            formatLabel.setOpaque(true);
        }
        return formatLabel;
    }

    public NumberFormat getCurrencyFormat() {
        return currencyFormat;
    }

    public void setCurrencyFormat(NumberFormat currencyFormat) {
        this.currencyFormat = currencyFormat;
    }
}

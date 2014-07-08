package org.julp.gui.swing.actions.table;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JTable;

public class CellCutAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = 1164574127122058553L;

    public CellCutAction() {
        super("cut-cell-action");
        init();
    }

    public CellCutAction(String iconPath) {
        super("cut-cell-action");
        putValue(Action.NAME, "Cut Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Cut16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellCutAction(String iconPath, JTable table) {
        super("cut-cell-action");
        this.table = table;
        putValue(Action.NAME, "Cut Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Cut16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellCutAction(JTable table) {
        super("cut-cell-action");
        this.table = table;
        putValue(Action.NAME, "Cut Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Cut Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object[] values;
        int[] x = table.getSelectedRows();
        int[] y = table.getSelectedColumns();
        int rowIndexStart = table.getSelectedRow();
        int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
        int colIndexStart = table.getSelectedColumn();
        int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();
        int i = 0;
        values = new Object[(x.length) * (y.length)];
        StringBuilder sb = new StringBuilder();

        for (int r = rowIndexStart; r <= rowIndexEnd; r++) {
            for (int c = colIndexStart; c <= colIndexEnd; c++) {
                if (table.isCellSelected(r, c)) {
                    Object cellValue = table.getValueAt(r, c);
                    if (table.isCellEditable(r, c)) {
                        table.setValueAt(null, r, c);
                    } else {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    values[i] = cellValue;
                    if (values[i] == null) {
                        values[i] = "";
                    }
                    sb.append(values[i]);
                    if (c < (colIndexEnd)) {
                        sb.append(cellDelimiter);
                    }
                    i++;
                }
            }
            sb.append(lineSeparator);
        }
        StringSelection stsel = new StringSelection(sb.toString());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stsel, stsel);
    }
}

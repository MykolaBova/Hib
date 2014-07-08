package org.julp.gui.swing.actions.table;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import javax.swing.JTable;
import java.awt.datatransfer.*;
import java.awt.event.InputEvent;
import javax.swing.Icon;

public class CellCopyAction extends AbstractJTableEditAction {
    private static final long serialVersionUID = -8464703738189257475L;

    public CellCopyAction() {
        super("copy-cell-action");
        putValue(Action.NAME, "Copy Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Y));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
    }

    public CellCopyAction(String iconPath) {
        super("copy-cell-action");
        putValue(Action.NAME, "Copy Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Y));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Copy16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellCopyAction(String iconPath, JTable table) {
        super("copy-cell-action");
        this.table = table;
        putValue(Action.NAME, "Copy Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Y));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Copy16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellCopyAction(JTable table) {
        super("copy-cell-action");
        this.table = table;
        putValue(Action.NAME, "Copy Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Y));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Copy Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_Y));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_MASK));
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
                    values[i] = cellValue;
                    if (values[i] == null) {
                        //values[i] = new Character('\u0000');
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

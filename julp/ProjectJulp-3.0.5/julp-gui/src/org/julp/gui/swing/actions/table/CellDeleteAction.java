package org.julp.gui.swing.actions.table;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JTable;

public class CellDeleteAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = -513461139478660295L;

    public CellDeleteAction() {
        super("delete-cell-action");
        putValue(Action.NAME, "Delete Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    public CellDeleteAction(String iconPath, JTable table) {
        super("delete-cell-action");
        this.table = table;
        putValue(Action.NAME, "Delete Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Delete16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellDeleteAction(JTable table) {
        super("delete-cell-action");
        this.table = table;
        putValue(Action.NAME, "Delete Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    public CellDeleteAction(String iconPath) {
        super("delete-cell-action");
        putValue(Action.NAME, "Delete Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Delete16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Delete Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {        
        int[] x = table.getSelectedRows();
        int[] y = table.getSelectedColumns();
        int rowIndexStart = table.getSelectedRow();
        int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
        int colIndexStart = table.getSelectedColumn();
        int colIndexEnd = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();
        int i = 0;
        for (int row = rowIndexStart; row <= rowIndexEnd; row++) {
            for (int column = colIndexStart; column <= colIndexEnd; column++) {
                if (table.isCellSelected(row, column)) {
                    if (table.isCellEditable(row, column)) {
                        table.setValueAt(null, row, column);
                    } else {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    i++;
                }
            }
        }
    }
}

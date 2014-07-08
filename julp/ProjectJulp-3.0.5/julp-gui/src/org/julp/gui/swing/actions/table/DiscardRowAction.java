package org.julp.gui.swing.actions.table;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import org.julp.gui.swing.table.JulpTableModel;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class DiscardRowAction extends AbstractJTableAction {

    private static final long serialVersionUID = -6903568395333706634L;

    public DiscardRowAction() {
        super("discard-row-action");
        putValue(Action.NAME, "Discard Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    public DiscardRowAction(String iconPath, JTable table) {
        super("discard-row-action");
        this.table = table;
        putValue(Action.NAME, "Discard Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Remove16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public DiscardRowAction(JTable table) {
        super("discard-row-action");
        this.table = table;
        putValue(Action.NAME, "Discard Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    public DiscardRowAction(String iconPath) {
        super("discard-row-action");
        init();
        try {
            //iconPath = "/toolbarButtonGraphics/general/Remove16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Discard Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_D));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        if (table.getModel() instanceof JulpTableModel) {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                return;
            }
            for (int rowIndex = rows.length - 1; rowIndex >= 0; rowIndex--) {
                int removeRow = rows[rowIndex];
                int modelRow = table.convertRowIndexToModel(removeRow);
                ((JulpTableModel) table.getModel()).discardRow(modelRow);
            }
        } else if (table.getModel() instanceof DefaultTableModel) {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {
                return;
            }
            for (int rowIndex = rows.length - 1; rowIndex >= 0; rowIndex--) {
                int removeRow = rows[rowIndex];
                int modelRow = table.convertRowIndexToModel(removeRow);
                ((DefaultTableModel) table.getModel()).removeRow(modelRow);
            }
        }
    }
}

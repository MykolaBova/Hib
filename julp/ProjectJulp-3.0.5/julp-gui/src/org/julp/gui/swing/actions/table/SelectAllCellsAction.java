package org.julp.gui.swing.actions.table;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.JTable;

public class SelectAllCellsAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = -6295412502977693076L;

    public SelectAllCellsAction() {
        super("select-all-cells-action");
        putValue(Action.NAME, "Select All Cells");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
    }

    public SelectAllCellsAction(String iconPath, JTable table) {
        super("select-all-cells-action");
        this.table = table;
        putValue(Action.NAME, "Select All Cells");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertBefore16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public SelectAllCellsAction(JTable table) {
        super("select-all-cells-action");
        this.table = table;
        putValue(Action.NAME, "Select All Cells");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
    }

    public SelectAllCellsAction(String iconPath) {
        super("select-all-cells-action");
        init();
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertBefore16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    protected void init() {
        putValue(Action.NAME, "Select All Cells");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_E));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.table.selectAll();
    }
}

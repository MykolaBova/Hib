package org.julp.gui.swing.actions.table;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import org.julp.gui.swing.table.JulpTableModel;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class RemoveRowAction extends AbstractJTableAction {

    private static final long serialVersionUID = 5144795180580608127L;

    public RemoveRowAction() {
        super("remove-row-action");
        putValue(Action.NAME, "Remove Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Remove Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_R));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_MASK));
    }

    public RemoveRowAction(String iconPath, JTable table) {
        super("remove-row-action");
        this.table = table;
        init();
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowDelete16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public RemoveRowAction(JTable table) {
        super("remove-row-action");
        this.table = table;
        init();
    }

    public RemoveRowAction(String iconPath) {
        super("remove-row-action");
        init();
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowDelete16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        TableModel model = table.getModel();
        if (model instanceof JulpTableModel) {
            int[] rows = table.getSelectedRows();
            if (rows.length == 0) {                
                return;
            }
            for (int rowIndex = rows.length - 1; rowIndex >= 0; rowIndex--) {                
                int removeRow = rows[rowIndex];
                int modelRow = table.convertRowIndexToModel(removeRow);
                ((JulpTableModel) table.getModel()).removeRow(modelRow);                
            }   
        } else if (model instanceof DefaultTableModel) {
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

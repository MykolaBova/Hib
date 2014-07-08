package org.julp.gui.swing.actions.table;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.julp.gui.swing.table.JulpTableModel;
import java.awt.event.InputEvent;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class InsertRowAction extends AbstractJTableAction {

    private static final long serialVersionUID = 3337084040667523405L;

    public InsertRowAction() {
        super("insert-row-action");
        putValue(Action.NAME, "Insert Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
    }

    public InsertRowAction(String iconPath, JTable table) {
        super("insert-row-action");
        this.table = table;
        putValue(Action.NAME, "Insert Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertBefore16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public InsertRowAction(JTable table) {
        super("insert-row-action");
        this.table = table;
        putValue(Action.NAME, "Insert Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
    }

    public InsertRowAction(String iconPath) {
        super("insert-row-action");
        putValue(Action.NAME, "Insert Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertBefore16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Insert Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_I));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int viewRow = -1;
        if (table.getModel() instanceof JulpTableModel) {
            int selectedRow = table.getSelectedRow();
            int modelRow = table.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0) {
                ((JulpTableModel) table.getModel()).insertRow(modelRow);
                viewRow = table.convertRowIndexToView(modelRow);
                table.setRowSelectionInterval(viewRow, viewRow);
            }
        } else if (table.getModel() instanceof DefaultTableModel) {
            int selectedRow = table.getSelectedRow();
            int modelRow = table.convertRowIndexToModel(selectedRow);
            if (modelRow >= 0) {
                int columns = table.getColumnCount();
                Object[] rowData = new Object[columns];
                 viewRow = table.convertRowIndexToView(modelRow);
                ((DefaultTableModel) table.getModel()).insertRow(viewRow, rowData);
                table.setRowSelectionInterval(viewRow, viewRow);
            }
        }
        table.editCellAt(viewRow, 0);
        Component c = table.getEditorComponent();
        if (c != null) {
            Point p = c.getLocationOnScreen();
            final Robot r;
            try {
                r = new Robot();
            } catch (AWTException ex) {
                throw new RuntimeException(ex);
            }
            r.mouseMove(p.x + c.getWidth() / 2, p.y + c.getHeight() / 2);
            r.mousePress(InputEvent.BUTTON1_MASK);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    r.mouseRelease(InputEvent.BUTTON1_MASK);
                }
            });
        }
    }
}

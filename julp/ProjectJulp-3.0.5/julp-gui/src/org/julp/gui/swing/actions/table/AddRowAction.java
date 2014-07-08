package org.julp.gui.swing.actions.table;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.julp.gui.swing.table.JulpTableModel;
import javax.swing.JTable;
import java.awt.event.InputEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

public class AddRowAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = 1845035507041966799L;

    public AddRowAction() {
        super("add-row-action");
        putValue(Action.NAME, "Add Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Add Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    }

    public AddRowAction(String iconPath, JTable table) {
        super("add-row-action");
        this.table = table;
        putValue(Action.NAME, "Add Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertAfter16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public AddRowAction(JTable table) {
        super("add-row-action");
        this.table = table;
        putValue(Action.NAME, "Add Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
    }

    public AddRowAction(String iconPath) {
        super("add-row-action");
        putValue(Action.NAME, "Add Row");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/table/RowInsertAfter16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int row = -1;
        if (table.getModel() instanceof JulpTableModel) {
            ((JulpTableModel) table.getModel()).addRow();
            row = table.getRowCount() - 1;
            if (row >= 0) {
                Rectangle r = table.getCellRect(0, 0, false);
                table.scrollRectToVisible(r);
                table.setRowSelectionInterval(row, row);
            }
        } else if (table.getModel() instanceof DefaultTableModel) {          
            int columns = table.getColumnCount();
            Object[] rowData = new Object[columns];
            ((DefaultTableModel) table.getModel()).addRow(rowData);
            row = table.getRowCount() - 1;
            if (row >= 0) {
                Rectangle r = table.getCellRect(0, 0, false);
                table.scrollRectToVisible(r);
                table.setRowSelectionInterval(row, row);
            }
        }
        table.editCellAt(row, 0);
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

package org.julp.gui.swing.table;

import java.awt.Component;
import java.util.EventObject;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import javax.swing.text.JTextComponent;

public class JulpTable extends JTable {

    private static final long serialVersionUID = 3172302692389535491L;

    public JulpTable() {
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        setRowSelectionAllowed(true);
        setColumnSelectionAllowed(true);
        setSurrendersFocusOnKeystroke(true);
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        putClientProperty("terminateEditOnFocus", Boolean.TRUE);
        //setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    }

    @Override
    public void changeSelection(int row, int column, boolean toggle, boolean extend) {
        super.changeSelection(row, column, toggle, extend);
        if (editCellAt(row, column)) {
            getEditorComponent().requestFocusInWindow();
        }
    }

    @Override
    public boolean editCellAt(int row, int column, EventObject e) {
        boolean result = super.editCellAt(row, column, e);
        final Component editor = getEditorComponent();
        //System.out.println(((JTextComponent) editor).getBorder());
        if (editor != null && editor instanceof JTextComponent) {
            if (e == null) {
                ((JTextComponent) editor).selectAll();
            } else {
//		SwingUtilities.invokeLater(new Runnable() {
//		    public void run() {
//			((JTextComponent) editor).selectAll();
//		    }
//		});
            }
        }
        return result;
    }

    @Override
    public Component prepareEditor(TableCellEditor editor, int row, int column) {
        final Component comp = super.prepareEditor(editor, row, column);
        if (comp instanceof JTextComponent) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((JTextComponent) comp).selectAll();
                }
            });
        }
        return comp;
    }
}

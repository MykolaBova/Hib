package org.julp.gui.swing.actions.table;

import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.datatransfer.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Icon;
import javax.swing.JTable;

public class CellPasteAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = -598077455543419956L;

    public CellPasteAction() {
        super("paste-cell-action");
        putValue(Action.NAME, "Paste Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
    }

    public CellPasteAction(String iconPath) {
        super("paste-cell-action");
        putValue(Action.NAME, "Paste Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Paste16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellPasteAction(String iconPath, JTable table) {
        super("paste-cell-action");
        this.table = table;
        putValue(Action.NAME, "Paste Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Paste16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CellPasteAction(JTable table) {
        super("paste-cell-action");
        this.table = table;
        putValue(Action.NAME, "Paste Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Paste Cell");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        int columnNumber = table.getColumnCount();
        int rowIndex = table.getSelectionModel().getMaxSelectionIndex();
        if (rowIndex == -1) {
            rowIndex = 0;
        }
        int colIndex = table.getColumnModel().getSelectionModel().getMaxSelectionIndex();
        if (colIndex == -1) {
            colIndex = 0;
        }
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        try {
            data = (String) (clipboard.getContents(this).getTransferData(DataFlavor.stringFlavor));
        } catch (UnsupportedFlavorException ufe) {
            throw new IllegalArgumentException("Invalid Paste operation");
        } catch (java.io.IOException ioe) {
            throw new IllegalArgumentException("Invalid Paste operation");
        }
        String[] lines = splitRows(data, lineSeparator);
        int dataRows = lines.length;
        int rowCount = table.getRowCount();
        if ((rowCount - rowIndex) < dataRows) {
            Toolkit.getDefaultToolkit().beep();
            if (isThrowExceptionOnError()) {
                throw new IllegalArgumentException("Invalid Paste operation: too many rows");
            } else {
                return;
            }
        }
        for (int lineNum = 0; lineNum < dataRows; lineNum++) {
            String line = lines[lineNum];
            String[] cells = splitColumns(line, cellDelimiter);
            int dataColumns = cells.length;
            if ((columnNumber - colIndex) < dataColumns) {
                Toolkit.getDefaultToolkit().beep();
                if (isThrowExceptionOnError()) {
                    throw new IllegalArgumentException("Invalid Paste operation: too many columns");
                } else {
                    return;
                }
            }
        }
        validateData();
        int row = rowIndex;
        for (int lineNum = 0; lineNum < dataRows; lineNum++) {
            String line = lines[lineNum];
            String[] cells = splitColumns(line, cellDelimiter);
            int dataColumns = cells.length;
            int column = colIndex;
            if (dataColumns == 0) {
                if (table.isCellEditable(row, column)) {
                    table.setValueAt("", row, column);
                } else {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
            for (int colNum = 0; colNum < dataColumns; colNum++) {
                Object value = cells[colNum];
                if (table.isCellEditable(row, column)) {
                    table.setValueAt(value, row, column);
                } else {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return;
                }
                column++;
            }
            row++;
        }
    }

    // unlike StringTokenizer and String.split() handles empty tokens
    protected String[] splitColumns(String data, String delimiter) {
        List tokens = new ArrayList();
        int end = 0;
        int start = 0;
        int len = data.length();
        while (end > -1) {
            String token = null;
            end = data.indexOf(delimiter, start);
            if (end == -1) {
                if (start < len) {
                    token = data.substring(start);
                    tokens.add(token);
                }
                if (data.endsWith(delimiter)) {
                    tokens.add("");
                }
                break;
            }
            token = data.substring(start, end);
            tokens.add(token);
            start = end + 1;
        }
        String[] returnValue = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            returnValue[i] = (String) tokens.get(i);
        }
        return returnValue;
    }

    protected String[] splitRows(String data, String delimiter) {
        List tokens = new ArrayList();
        int end = 0;
        int start = 0;
        int len = data.length();
        while (end > -1) {
            String token = null;
            end = data.indexOf(delimiter, start);
            if (end == -1) {
                if (start < len) {
                    token = data.substring(start);
                    tokens.add(token);
                }
                break;
            }
            token = data.substring(start, end);
            tokens.add(token);
            start = end + 1;
        }
        String[] returnValue = new String[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            returnValue[i] = (String) tokens.get(i);
        }
        return returnValue;
    }
}

package org.julp.gui.swing.actions.table;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.table.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import org.julp.gui.swing.filechooser.GenericFileFilter;
import org.julp.gui.swing.table.JulpTableModel;
import org.julp.*;
import org.julp.db.DBMetaData;

public class ImportFileAction extends AbstractJTableEditAction {

    private static final long serialVersionUID = 1650138726733334161L;
    // protected Component parentComponent;
    protected String selectedExtention;
    protected Map fileFiltersAttributes = new HashMap();
    protected String nullValueDisplay = "";
    protected String delimiter = "";
    protected boolean append;
    protected boolean headers;
    protected Locale locale = new Locale("en", "EN");

    public ImportFileAction() {
        super("import-file-action");
        putValue(Action.NAME, "Import File");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        //fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        //fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        //fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    public ImportFileAction(String iconPath) {
        super("import-file-action");
        putValue(Action.NAME, "Import File");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        //fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        //fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        //fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
        try {
            //iconPath = "/toolbarButtonGraphics/general/Import16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public ImportFileAction(String iconPath, JTable table) {
        super("import-file-action");
        this.table = table;
        putValue(Action.NAME, "Import File");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        //fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        //fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        //fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
        try {
            //iconPath = "/toolbarButtonGraphics/general/Import16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public ImportFileAction(JTable table) {
        super("import-file-action");
        this.table = table;
        putValue(Action.NAME, "Import File");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        //fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        //fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        //fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Import File");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK));
        //fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        //fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        //fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    protected String openFileLocation() {
        String path = null;
        JFileChooser fc = new JFileChooser();
        Iterator fileFilterIter = fileFiltersAttributes.entrySet().iterator();
        while (fileFilterIter.hasNext()) {
            Map.Entry entry = (Map.Entry) fileFilterIter.next();
            GenericFileFilter filter = new GenericFileFilter((String) entry.getKey(), (String) entry.getValue());
            fc.addChoosableFileFilter(filter);
        }
        fc.setAcceptAllFileFilterUsed(true);
        int returnVal = fc.showSaveDialog(null);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            path = file.getAbsolutePath();
            javax.swing.filechooser.FileFilter currentFilter = fc.getFileFilter();
            if (currentFilter instanceof GenericFileFilter) {
                selectedExtention = ((GenericFileFilter) currentFilter).getExtention();
            } else {
                selectedExtention = "";
            }
        } else {
            path = null;
        }
        return path;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        try {
            String path = openFileLocation();
            //System.out.println("path: " + path);
            if (path == null) {
                return;
            }
            int idx = path.lastIndexOf(".");
            String ext;
            if (idx <= 0) {
                ext = "";
            } else {
                ext = path.substring(idx).toLowerCase();
            }
            if (selectedExtention == null) {
                throw new IllegalArgumentException("Please provide valid File Name and Extention");
            }
            if (selectedExtention.equals("xls")) {
                if (!ext.equals(".xls")) {
                    path = path + ".xls";
                }

            } else if (selectedExtention.equals("txt")) {
                if (!ext.equals(".txt")) {
                    path = path + ".txt";
                }
                importTextFile(path);
            } else if (selectedExtention.equals("csv")) {
                if (!ext.equals(".csv")) {
                    path = path + ".csv";
                }
            } else {
                throw new IllegalArgumentException("Please provide valid file extention");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent, t.getMessage(), "Import File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void importTextFile(String path) {
        BufferedReader in = null;
        Object[] columNames = null;
        if (!append) {
            if (table.getModel() instanceof DefaultTableModel) {
                TableColumnModel tcm = table.getColumnModel();
                int colCount = tcm.getColumnCount();
                columNames = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    columNames[i] = tcm.getColumn(i).getHeaderValue();
                }
                Object[][] data = new Object[0][colCount];
                ((DefaultTableModel) table.getModel()).setDataVector(data, columNames);
            } else if (table.getModel() instanceof JulpTableModel) {
                ((JulpTableModel) table.getModel()).getFactory().clearData();
            } else {
                throw new UnsupportedOperationException(table.getModel().toString() + " is not supported");
            }
        }

        try {
            in = new BufferedReader(new FileReader(path));
            String line;
            int rowCount = this.table.getRowCount();

            if (table.getModel() instanceof JulpTableModel) {
                DBMetaData md = (DBMetaData) ((JulpTableModel) table.getModel()).getFactory().getMetaData();
            }
            Map mapping = ((JulpTableModel) table.getModel()).getFactory().getMapping();
            int row = 0;
            while ((line = in.readLine()) != null) {
                if (headers) {
                    if (row == 0) {
                        continue;
                    }
                }
                String[] items = line.split(delimiter);
                int len = items.length;
                if (table.getModel() instanceof DefaultTableModel) {
                    ((DefaultTableModel) table.getModel()).addRow(items);
                } else if (table.getModel() instanceof JulpTableModel) {
                    ((JulpTableModel) table.getModel()).addRow();
                    for (int column = 0; column < len; column++) {
                        table.setValueAt(items[column], row, column);
                    }
                } else {
                    throw new UnsupportedOperationException(table.getModel().toString() + " is not supported");
                }
                row++;
            }
        } catch (IOException ioe) {
            throw new DataAccessException(ioe);
        } finally {
            try {
                in.close();
            } catch (IOException ioe) {
                throw new DataAccessException(ioe);
            }
        }

        //throw new IllegalArgumentException("File must have the same number of columns as table");
    }

    protected void importExcelFile(String path) {
    }

    protected void importCsvFile(String path) {
    }

    public boolean isAppend() {
        return this.append;
    }

    public void setAppend(boolean append) {
        this.append = append;
    }

    public java.lang.String getNullValueDisplay() {
        return nullValueDisplay;
    }

    public void setNullValueDisplay(java.lang.String nullValueDisplay) {
        this.nullValueDisplay = nullValueDisplay;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isHeaders() {
        return this.headers;
    }

    public void setHeaders(boolean headers) {
        this.headers = headers;
    }
}

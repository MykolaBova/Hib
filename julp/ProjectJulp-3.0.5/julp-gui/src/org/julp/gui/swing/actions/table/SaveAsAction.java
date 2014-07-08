package org.julp.gui.swing.actions.table;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.table.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import org.julp.gui.swing.filechooser.GenericFileFilter;
import org.julp.ValueObject;
import org.julp.gui.swing.table.JulpTableModel;

public class SaveAsAction extends AbstractJTableAction {

    private static final long serialVersionUID = 5832002374068713656L;
    protected String selectedExtention;
    protected Map fileFiltersAttributes = new HashMap();
    protected String nullValueDisplay = "";
    protected Locale locale = new Locale("en", "EN");

    public SaveAsAction() {
        super("save-as-action");
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("html", "HTML Documents (*.html)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        fileFiltersAttributes.put("pdf", "PDF Documents (*.pdf)");
        fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    public SaveAsAction(String iconPath, JTable table) {
        super("save-as-action");
        this.table = table;
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("html", "HTML Documents (*.html)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        fileFiltersAttributes.put("pdf", "PDF Documents (*.pdf)");
        fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
        try {
            //iconPath = "/toolbarButtonGraphics/general/SaveAs16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public SaveAsAction(JTable table) {
        super("save-as-action");
        this.table = table;
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("html", "HTML Documents (*.html)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        fileFiltersAttributes.put("pdf", "PDF Documents (*.pdf)");
        fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    public SaveAsAction(String iconPath) {
        super("save-as-action");
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("html", "HTML Documents (*.html)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        fileFiltersAttributes.put("pdf", "PDF Documents (*.pdf)");
        fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
        try {
            //iconPath = "/toolbarButtonGraphics/general/SaveAs16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    @Override
    protected void init() {
        putValue(Action.NAME, "Save As...");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
        fileFiltersAttributes.put("xls", "Excel Documents (*.xls)");
        fileFiltersAttributes.put("html", "HTML Documents (*.html)");
        fileFiltersAttributes.put("txt", "Text Documents (*.txt)");
        fileFiltersAttributes.put("xml", "XML Documents (*.xml)");
        fileFiltersAttributes.put("pdf", "PDF Documents (*.pdf)");
        fileFiltersAttributes.put("csv", "Comma Separated Values (*.csv)");
    }

    protected String openFileSaveLocation() {
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
            String path = openFileSaveLocation();
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
                saveAsExcel(path);
            } else if (selectedExtention.equals("txt")) {
                if (!ext.equals(".txt")) {
                    path = path + ".txt";
                }
                saveAsText(path);
            } else if (selectedExtention.equals("pdf")) {
                if (!ext.equals(".pdf")) {
                    path = path + ".pdf";
                }
            } else if (selectedExtention.equals("csv")) {
                if (!ext.equals(".csv")) {
                    path = path + ".csv";
                }
                saveAsCsv(path);
            } else if (selectedExtention.equals("html")) {
                if (!ext.equals(".html")) {
                    path = path + ".html";
                }
                saveAsHtml(path);
            } else if (selectedExtention.equals("xml")) {
                if (!ext.equals(".xml")) {
                    path = path + ".xml";
                }
                saveAsXML(path);
            } else {
                throw new IllegalArgumentException("Please provide valid file extention");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            JOptionPane.showMessageDialog(parentComponent, t.getMessage(), "Save as Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    protected void saveAsText(String path) {
        StringBuilder sb = new StringBuilder();
        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        for (int col = 0; col < colCount; col++) {
            sb.append(table.getColumnName(col)).append("\t");
        }
        sb.append("\n");
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                Object value = table.getValueAt(row, col);
                sb.append((value == null) ? nullValueDisplay : value).append("\t");
            }
            sb.append("\n");
        }
        //System.out.println(sb);
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(path));
            bw.write(sb.toString());
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            try {
                bw.close();
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
        }
    }

    protected void saveAsHtml(String path) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html>\n");
        sb.append(" <head>\n");
        sb.append("  <title>\n");
        sb.append("  </title>\n");
        sb.append(" </head>\n");
        sb.append(" <body>\n");
        sb.append("  <table border='1'>\n");
        sb.append("   <tr>\n");
        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        for (int col = 0; col < colCount; col++) {
            sb.append("    <th>").append(table.getColumnName(col)).append("</th>\n");
        }
        sb.append("   </tr>\n");
        for (int row = 0; row < rowCount; row++) {
            sb.append("   <tr>\n");
            for (int col = 0; col < colCount; col++) {
                Object value = table.getValueAt(row, col);
                sb.append("    <td>").append((value == null) ? nullValueDisplay : value).append("</td>\n");
            }
            sb.append("   </tr>\n");
        }
        sb.append("  </table>\n");
        sb.append(" </body>\n");
        sb.append("</html>");
        //System.out.println(sb);
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(path));
            bw.write(sb.toString());
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            try {
                bw.close();
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
        }
    }

    protected void saveAsPdf(String path) {
        throw new RuntimeException("Not implemented yet - use \".html\"");
    }

    protected void saveAsXML(String path) {
        StringBuilder sb = new StringBuilder("<?xml version = '1.0' encoding = 'UTF-8'?>\n\n");
        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        TableModel model = table.getModel();
        List columns = new ArrayList(colCount);
        for (int col = 0; col < colCount; col++) {
            columns.add(model.getColumnName(col).replaceAll(" ", "_"));
        }
        sb.append("<rows>\n");
        for (int row = 0; row < rowCount; row++) {
            sb.append(" <row>\n");
            for (int col = 0; col < colCount; col++) {
                Object column = columns.get(col);
                sb.append("  <").append(column).append(">");
                Object value = table.getValueAt(row, col);
                sb.append((value == null) ? nullValueDisplay : value);
                sb.append("</").append(column).append(">\n");
            }
            sb.append(" </row>\n");
        }
        sb.append("</rows>\n");
        //System.out.println(sb);
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(path));
            bw.write(sb.toString());

        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            try {
                bw.close();
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
        }
    }

    protected void saveAsXML1(String path) {
        boolean fieldNameAsTag = true;
        boolean visibleColumnsOnly = true;
        StringBuilder sb = new StringBuilder("<?xml version = '1.0' encoding = 'UTF-8'?>\n\n");
        try {
            int colCount = -1;
            int rowCount = table.getRowCount();

            TableModel model = table.getModel();
            List columns = null;
            JulpTableModel julpTableModel = null;

            if (model instanceof JulpTableModel) {
                julpTableModel = (JulpTableModel) model;
                if (visibleColumnsOnly) {
                    colCount = table.getColumnCount();
                    if (fieldNameAsTag) {
                        columns = julpTableModel.getDisplayableFields();
                    } else {
                        columns = new ArrayList(julpTableModel.getDisplayableFields().size());
                        for (int col = 0; col < colCount; col++) {
                            columns.add(model.getColumnName(col).replaceAll(" ", "_"));
                        }
                    }
                } else {
                    colCount = julpTableModel.getFactory().getMetaData().getFieldCount();
                    columns = new ArrayList(colCount);
                }
            } else {
                colCount = table.getColumnCount();
                columns = new ArrayList(colCount);
            }

            if (model instanceof JulpTableModel) {
                if (visibleColumnsOnly) {
                    if (fieldNameAsTag) {
                        columns = julpTableModel.getDisplayableFields();
                    } else {
                        for (int col = 0; col < colCount; col++) {
                            columns.add(model.getColumnName(col).replaceAll(" ", "_"));
                        }
                    }
                } else {
                }
            } else {
                for (int col = 0; col < colCount; col++) {
                    columns.add(model.getColumnName(col).replaceAll(" ", "_"));
                }
            }

            sb.append("<rows>\n");
            for (int row = 0; row < rowCount; row++) {
                sb.append(" <row>\n");
                for (int col = 0; col < colCount; col++) {
                    Object column = columns.get(col);
                    sb.append("  <").append(column).append(">");
                    Object value = table.getValueAt(row, col);
                    sb.append((value == null) ? nullValueDisplay : value);
                    sb.append("</").append(column).append(">\n");
                }
                sb.append(" </row>\n");
            }
            sb.append("</rows>\n");

        } catch (Exception sqle) {
            throw new RuntimeException(sqle.getMessage());
        }
        //System.out.println(sb);
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(path));
            bw.write(sb.toString());

        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            try {
                bw.close();
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
        }
    }

    protected void saveAsExcel(String path) {
        jxl.write.WritableWorkbook newWorkbook = null;
        jxl.WorkbookSettings ws = new jxl.WorkbookSettings();
        ws.setLocale(this.locale);
        try {
            newWorkbook = jxl.Workbook.createWorkbook(new File(path), ws);
            jxl.write.WritableSheet newSheet = newWorkbook.createSheet("Sheet1", 0);
            jxl.write.WritableFont times10font = new jxl.write.WritableFont(jxl.write.WritableFont.ARIAL, 10, jxl.write.WritableFont.BOLD, false);
            jxl.write.WritableCellFormat times10format = new jxl.write.WritableCellFormat(times10font);

            int colCount = table.getColumnCount();
            int rowCount = table.getRowCount();
            int newSheetRowNum = 0;
            newSheet.insertRow(newSheetRowNum);
            for (int col = 0; col < colCount; col++) {
                jxl.write.Label label = new jxl.write.Label(col, newSheetRowNum, table.getColumnName(col), times10format);
                newSheet.addCell(label);
            }
            newSheetRowNum++;
            for (int i = 0; i < rowCount; i++) {
                newSheet.insertRow(newSheetRowNum);
                for (int col = 0; col < colCount; col++) {
                    Object value = table.getValueAt(i, col);
                    if (value instanceof ValueObject) {
                        if (((ValueObject) value).isCompareByValue()) {
                            value = ((ValueObject) value).getValue();
                        } else {
                            value = ((ValueObject) value).getValueLabel();
                        }
                    }
                    if (value == null) {
                        jxl.write.Blank blank = new jxl.write.Blank(col, newSheetRowNum);
                        newSheet.addCell(blank);
                    } else if (value instanceof java.lang.Number) {
                        jxl.write.Number n = new jxl.write.Number(col, newSheetRowNum, ((java.lang.Number) value).doubleValue());
                        newSheet.addCell(n);
                    } else if (value instanceof java.lang.String) {
                        jxl.write.Label label = new jxl.write.Label(col, newSheetRowNum, (String) value);
                        newSheet.addCell(label);
                    } else if (value instanceof java.util.Date) {
                        jxl.write.DateTime dt = new jxl.write.DateTime(col, newSheetRowNum, (java.util.Date) value);
                        newSheet.addCell(dt);
                    } else {
                        jxl.write.Label label = new jxl.write.Label(col, newSheetRowNum, value.toString());
                        newSheet.addCell(label);
                    }
                }
                newSheetRowNum++;
            }
            newWorkbook.write();
        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } catch (jxl.write.WriteException we) {
            throw new RuntimeException(we.getMessage());
        } finally {
            try {
                newWorkbook.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void saveAsCsv(String path) {
        StringBuilder sb = new StringBuilder();
        int colCount = table.getColumnCount();
        int rowCount = table.getRowCount();
        for (int col = 0; col < colCount; col++) {
            sb.append("\"").append(table.getColumnName(col)).append("\"");
            if (col < (colCount - 1)) {
                sb.append(",");
            }
        }
        sb.append("\n");
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < colCount; col++) {
                Object value = table.getValueAt(row, col);
                sb.append("\"").append((value == null) ? nullValueDisplay : value).append("\"");
                if (col < (colCount - 1)) {
                    sb.append(",");
                }
            }
            sb.append("\n");
        }
        java.io.BufferedWriter bw = null;
        try {
            bw = new java.io.BufferedWriter(new java.io.FileWriter(path));
            bw.write(sb.toString());

        } catch (java.io.IOException ioe) {
            throw new RuntimeException(ioe.getMessage());
        } finally {
            try {
                bw.close();
            } catch (java.io.IOException ioe) {
                throw new RuntimeException(ioe.getMessage());
            }
        }
    }

    public java.util.Map getFileFiltersAttributes() {
        return fileFiltersAttributes;
    }

    public void setFileFiltersAttributes(java.util.Map fileFiltersAttributes) {
        this.fileFiltersAttributes = fileFiltersAttributes;
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
}

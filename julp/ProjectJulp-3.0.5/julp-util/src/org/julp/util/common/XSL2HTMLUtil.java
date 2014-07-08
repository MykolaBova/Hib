package org.julp.util.common;

import java.io.*;
import jxl.*;

public class XSL2HTMLUtil {

    /** Creates a new instance of XSL2HTMLUtil */
    public XSL2HTMLUtil() {
    }

    public String convert(String fileName) {
        Workbook workbook = null;
        StringBuilder html = new StringBuilder("<html>\n");
        try {
            workbook = Workbook.getWorkbook(new File(fileName));
            Sheet sheet = workbook.getSheet(0);
            html.append(" <head><title>");
            html.append(sheet.getName());
            html.append(" </title></head>\n");
            html.append(" <body>\n");
            html.append(" <table border='1'>\n");
            //int colCount = sheet.getColumns();
            int rowCount = sheet.getRows();
            int count = 0;
            for (int r = 0; r < rowCount; r++) {
                Cell[] cell = sheet.getRow(r);
                html.append("  <tr>\n");
                html.append("   <td>");
                html.append(count);
                html.append("</td>\n");
                for (int c = 0; c < cell.length; c++) {
                    html.append("   <td>");
                    String value = cell[c].getContents();
                    if (value == null) {
                        value = "&nbsp;";
                    } else {
                        value = value.replaceAll("\"", "");
                        if (value.trim().equals("")) {
                            value = "&nbsp;";
                        }
                    }
                    html.append(value);
                    html.append("</td>\n");
                }
                html.append("  </tr>\n");
            }
            html.append(" </table>\n");
            html.append(" </body>\n</html>");
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (jxl.read.biff.BiffException be) {
            be.printStackTrace();
        } finally {
            workbook.close();
        }
//        System.out.println(html);
        return html.toString();
    }
}

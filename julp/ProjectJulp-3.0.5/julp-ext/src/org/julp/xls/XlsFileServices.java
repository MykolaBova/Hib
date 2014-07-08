package org.julp.xls;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableWorkbook;

public class XlsFileServices {

    protected boolean removeBackupFile = true;
    protected WritableWorkbook workbook = null;
    protected File file;
    protected String origFileName;
    protected File backup;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
    protected String backupExtention = "xls";
    protected static final char DOT = '.';
    private final transient Logger logger = Logger.getLogger(getClass().getName());

    public XlsFileServices() {
    }

    public SimpleDateFormat getDateFormat() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::getDateFormat(): " + dateFormat + " \n");
        }
        return dateFormat;
    }

    public boolean isRemoveBackupFile() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::isRemoveBackupFile(): " + removeBackupFile + " \n");
        }
        return removeBackupFile;
    }

    public void setRemoveBackupFile(boolean removeBackupFile) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setRemoveBackupFile(): " + removeBackupFile + " \n");
        }
        this.removeBackupFile = removeBackupFile;
    }

    public String getBackupExtention() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::getBackupExtention(): " + backupExtention + " \n");
        }
        return backupExtention;
    }

    public void setBackupExtention(String backupExtention) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setBackupExtention(): " + backupExtention + " \n");
        }
        this.backupExtention = backupExtention;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setDateFormat(): " + dateFormat + " \n");
        }
        this.dateFormat = dateFormat;
    }

    public File getFile() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::getFile(): " + " \n");
        }
        return file;
    }

    public void setFile(File file) throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setFile(): " + file + " \n");
        }        
        if (!file.exists()) {
            throw new IOException("File " + file.getCanonicalPath() + " does not exist");
        }
        if (!file.canWrite()) {
            throw new IOException("File " + file.getCanonicalPath() + " is not writable");
        }
        this.file = file;
    }

    /**
     * Transaction emulation
     */
    public void begin() throws Exception {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::begin(): " + " \n");
        }
        String ts = dateFormat.format(new Date());
        if (!file.exists()) {
            throw new IOException("File " + file.getCanonicalPath() + " does not exist");
        }
        origFileName = file.getCanonicalPath();
        String backupfileName = null;
        int idx = file.getName().lastIndexOf(DOT);
        if (idx > -1) {
            backupfileName = file.getName().substring(0, idx)  + ts + DOT + backupExtention;
        }

        WritableWorkbook backupWorkbook = null;
        try {
            backup = new File(file.getParentFile(), backupfileName);
            backup.createNewFile();           
            Workbook originalWorkbook = Workbook.getWorkbook(new FileInputStream(file));
            backupWorkbook = Workbook.createWorkbook(backup, originalWorkbook);
            backupWorkbook.write();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            backupWorkbook.close();
        }
    }

    public void commit() throws Exception {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::commit(): " + " \n");
        }
        try {
            if (workbook != null) {
                workbook.write();
            }
        } finally {
            workbook.close();
            if (removeBackupFile && (backup != null && backup.exists())) {
                if (!backup.delete()) {
                    throw new IOException("commit(): Cannot delete backup file " + file.getCanonicalPath());
                }
            }
        }
    }

    public void rollback() throws Exception {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::rollback(): " + " \n");
        }
        workbook.close();

        if (!file.delete()) {
            throw new IOException("rollback(): Cannot delete original " + file.getCanonicalPath() + " file");
        }

        if (!file.createNewFile()) {
            throw new IOException("rollback(): Cannot re-create original " + file.getCanonicalPath() + " file");
        }

        if (!backup.renameTo(file)) {
            WritableWorkbook origWorkbook = null;
            try {                
                Workbook backupWorkbook = Workbook.getWorkbook(backup);
                origWorkbook = Workbook.createWorkbook(file, backupWorkbook);
                origWorkbook.write();
            } finally {
                origWorkbook.close();
                if (removeBackupFile && (backup != null && backup.exists())) {
                    if (!backup.delete()) {
                        throw new IOException("rollback(): Cannot delete backup file " + backup.getCanonicalPath());
                    }
                }
            }
        }
    }

    public void release() {
//        if (logger.isLoggable(Level.FINEST)) {
//            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::release(): " + " \n");
//        }
//        this.writer = null;
//        this.file = null;
//        this.origFileName = null;
//        this.backup = null;
    }

    public WritableWorkbook getWorkbook() throws Exception {
        if (workbook == null) {
            boolean b = file.exists();
            workbook = Workbook.createWorkbook(file, Workbook.getWorkbook(file));
        }
        return workbook;
    }
}

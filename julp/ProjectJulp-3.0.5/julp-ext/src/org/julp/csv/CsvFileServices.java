package org.julp.csv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvFileServices {

    protected boolean removeBackupFile = true;
    protected BufferedWriter writer;
    protected File file;
    protected String origFileName;
    protected File backup;
    protected boolean append;
    protected SimpleDateFormat dateFormat = new SimpleDateFormat("_yyyy-MM-dd_HH-mm-ss");
    protected String backupExtention = "csv";
    protected static final char DOT = '.';
    private final transient Logger logger = Logger.getLogger(getClass().getName());

    public CsvFileServices() {
    }

    public SimpleDateFormat getDateFormat() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::getDateFormat(): " + dateFormat + " \n");
        }
        return dateFormat;
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setDateFormat(): "  + dateFormat + " \n");
        }
        this.dateFormat = dateFormat;
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

    public boolean isAppend() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::isAppend(): " + append + " \n");
        }
        return append;
    }

    public void setAppend(boolean append) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::setAppend(): " + append + " \n");
        }
        this.append = append;
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
        this.file = file;
        if (!file.exists()) {
            throw new IOException("File " + file.getCanonicalPath() + " does not exist");
        }
        if (!file.canWrite()) {
            throw new IOException("File " + file.getCanonicalPath() + " is not writable");
        }
    }

    /**
     * Transaction emulation
     */
    public void begin() throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::begin()" + " \n");
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
        backup = new File(file.getParentFile(), backupfileName);
        backup.createNewFile();
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            String str;
            br = new BufferedReader(new FileReader(file));
            bw = new BufferedWriter(new FileWriter(backup));
            while ((str = br.readLine()) != null) {
                bw.write(str);
                bw.newLine();
            }
        } finally {
            bw.flush();
            br.close();
            bw.close();
        }
    }

    public void commit() throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::commit(): " + " \n");
        }
        try {
            if (writer != null) {
                writer.flush();
            }
        } finally {
            writer.close();
            if (removeBackupFile && (backup != null && backup.exists())) {
                if (!backup.delete()) {
                    throw new IOException("commit(): Cannot delete backup file " + file.getCanonicalPath());
                }
            }
        }
    }

    public void rollback() throws IOException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::rollback(): " + " \n");
        }
        writer.close();

        if (!file.delete()) {
            throw new IOException("rollback(): Cannot delete original " + file.getCanonicalPath() + " file");
        }

        if (!file.createNewFile()) {
            throw new IOException("rollback(): Cannot re-create original " + file.getCanonicalPath() + " file");
        }

        if (!backup.renameTo(file)) {
            BufferedReader br = null;
            BufferedWriter bw = null;
            try {
                String str;
                br = new BufferedReader(new FileReader(backup.getCanonicalPath()));
                bw = new BufferedWriter(new FileWriter(file.getCanonicalPath()));

                while ((str = br.readLine()) != null) {
                    bw.write(str);
                    bw.newLine();
                }
            } finally {
                br.close();
                bw.close();
                if (removeBackupFile && (backup != null && backup.exists())) {
                    if (!backup.delete()) {
                        throw new IOException("rollback(): Cannot delete backup file " + backup.getCanonicalPath());
                    }
                }
            }
        }
    }

    public void release() {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::release(): " + " \n");
        }
        this.writer = null;
        this.file = null;
        this.origFileName = null;
        this.backup = null;
    }
        
    public BufferedWriter getWriter() throws IOException {
        writer = new BufferedWriter(new FileWriter(file, append));
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + new java.util.Date() + "::" + this.getClass() + "::" + this + "::getWriter(): " + writer + " \n");
        }
        return writer;
    }

}

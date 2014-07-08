package org.julp.gui.swing.filechooser;

import java.io.File;
import javax.swing.filechooser.*;

/*
* Generic FileFilter for JFileChooser
*/
public class GenericFileFilter extends FileFilter {

    protected String extention;
    protected String description;

    public GenericFileFilter() {
    }

    public GenericFileFilter(String extention, String description) {
        init(extention, description);
    }
    
    private void init(String extention, String description) {
        setExtention(extention);
        setDescription(description);
    }

    @Override
    public boolean accept(File f) {
        if (f.isDirectory() || f.getName().toLowerCase().endsWith("." + extention)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(java.lang.String description) {
        if (description == null || description.trim().length() == 0) {
            throw new IllegalArgumentException("Missing description");
        }
        this.description = description;
    }

    public java.lang.String getExtention() {
        return extention;
    }

    public void setExtention(java.lang.String extention) {
        if (extention == null || extention.trim().length() == 0) {
            throw new IllegalArgumentException("Missing extention");
        }
        this.extention = extention;
    }
}

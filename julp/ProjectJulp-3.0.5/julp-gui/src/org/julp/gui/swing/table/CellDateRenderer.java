package org.julp.gui.swing.table;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JLabel;
import javax.swing.table.DefaultTableCellRenderer;

public class CellDateRenderer extends DefaultTableCellRenderer {
    private static final long serialVersionUID = 8068412914204307246L;

    protected SimpleDateFormat formatter;
    protected String format;
    protected int defaultAlignment = JLabel.RIGHT;
    protected String defaultValue = "00/00/0000";
    protected boolean throwExceptionOnInvalidDate;
    protected boolean throwExceptionOnNullDate;
    protected String nullValueMessage = "Missing Date value";
    protected String invalidValueMessage = "Invalid Date";

    public CellDateRenderer(String format) {
        super();
        this.format = format;
        formatter = new SimpleDateFormat(format);
        formatter.setLenient(false);
        setHorizontalAlignment(defaultAlignment);
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            if (throwExceptionOnNullDate) {
                throw new IllegalArgumentException(nullValueMessage);
            }
            value = defaultValue;
        } else if (value instanceof Date) {
            value = formatter.format(value);
            super.setValue(value);
        } else if (value instanceof String) {
            try {
                value = formatter.parse((String) value);
            } catch (ParseException e) {
                if (throwExceptionOnInvalidDate) {
                    throw new IllegalArgumentException(invalidValueMessage + ": " + value);
                }
                value = defaultValue;
            }
        }
        super.setValue(value);
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public int getDefaultAlignment() {
        return defaultAlignment;
    }

    public void setDefaultAlignment(int defaultAlignment) {
        this.defaultAlignment = defaultAlignment;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isThrowExceptionOnInvalidDate() {
        return throwExceptionOnInvalidDate;
    }

    public void setThrowExceptionOnInvalidDate(boolean throwExceptionOnInvalidDate) {
        this.throwExceptionOnInvalidDate = throwExceptionOnInvalidDate;
    }

    public boolean isThrowExceptionOnNullDate() {
        return throwExceptionOnNullDate;
    }

    public void setThrowExceptionOnNullDate(boolean throwExceptionOnNullDate) {
        this.throwExceptionOnNullDate = throwExceptionOnNullDate;
    }

    public String getNullValueMessage() {
        return nullValueMessage;
    }

    public void setNullValueMessage(String nullValueMessage) {
        this.nullValueMessage = nullValueMessage;
    }

    public String getInvalidValueMessage() {
        return invalidValueMessage;
    }

    public void setInvalidValueMessage(String invalidValueMessage) {
        this.invalidValueMessage = invalidValueMessage;
    }
}

package org.julp.gui.swing.table;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.julp.AbstractDomainObjectFactory;
import org.julp.DataAccessException;
import org.julp.DomainObject;
import org.julp.PersistentState;
import org.julp.ValueObject;
import org.julp.db.DBMetaData;

/**
 * Don't forget: row/column number in DomainObjectFactory is 1-based and row/column number in JTable is 0-based
 */
public class JulpTableModel extends javax.swing.table.AbstractTableModel {

    private static final long serialVersionUID = -4983492470543528588L;
    protected AbstractDomainObjectFactory factory;
    protected List<String> displayableFields; // displayableFields names - could be all or less than fields in DomainObject
    protected List<String> labels; // column headers
    protected static final Object[] EMPTY_READ_ARG = new Object[0];
    public static final String GET_DISPLAY_VALUE = "getDisplayValue";
    protected Map displayValues;
    /* if factory is NOT readonly, but cells editing should not be allowed */
    protected boolean editable = true;

    public JulpTableModel() {
    }

    @Override
    public int getColumnCount() {
        return getDisplayableFields().size();
    }

    @Override
    public int getRowCount() {
        return factory.getObjectList().size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        String field = getDisplayableFields().get(columnIndex);
        int fieldIndex = factory.getMetaData().getFieldIndexByFieldName(field);
        DomainObject domainObject = null;
        try {
            Method readMethod = factory.getMetaData().getReadMethod(fieldIndex);
            domainObject = (DomainObject) factory.getObjectList().get(rowIndex);
            value = readMethod.invoke(domainObject, EMPTY_READ_ARG);
            if (this.displayValues != null && this.displayValues.containsKey(field)) {
                Map fieldDisplayValues = this.getFieldDisplayValues(field);
                String displayValue = fieldDisplayValues.get(value) == null ? "" : fieldDisplayValues.get(value).toString();
                domainObject.setDisplayValue(field, displayValue);
            }
        } catch (Exception e) {
            if (!handleException(domainObject, field, value, e)) {
                throw new DataAccessException(e);
            }
        }
        return value;
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        String field = getDisplayableFields().get(columnIndex);
        int fieldIndex = factory.getMetaData().getFieldIndexByFieldName(field);
        DomainObject domainObject = (DomainObject) factory.getObjectList().get(rowIndex);
        try {
            Method writeMethod = factory.getMetaData().getWriteMethod(fieldIndex);
            Object[] writeArg = new Object[1];
            if (value instanceof ValueObject) {
                writeArg[0] = ((ValueObject) value).getValue();
                domainObject.setDisplayValue(field, ((ValueObject) value).getValueLabel());
            } else {
                writeArg[0] = value;
            }
            writeMethod.invoke(domainObject, writeArg);
        } catch (final Exception e) {
            if (!handleException(domainObject, field, value, e)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        throw new DataAccessException(e);
                    }
                });
            }
        }
        fireTableCellUpdated(rowIndex, columnIndex);
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        Class columnClass;
        String field = getDisplayableFields().get(columnIndex);
        int fieldIndex = factory.getMetaData().getFieldIndexByFieldName(field);
        columnClass = factory.getMetaData().getFieldClass(fieldIndex);
        return columnClass;
    }

    @Override
    public String getColumnName(int columnIndex) {
        String columnName = null;
        if (labels != null) {
            columnName = labels.get(columnIndex);
        } else {
            String field = getDisplayableFields().get(columnIndex);
            DBMetaData md = (DBMetaData) factory.getMetaData();            
            try {
                columnName = md.toLabel(field);
            } catch (Exception e) {
                // ignore?
            }
        }
        return (columnName == null ? "" : columnName);
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (factory.isReadOnly() || !editable) {
            return false;
        }
        // Note that the data/cell address is constant, no matter where the cell appears
        String field = getDisplayableFields().get(columnIndex);
        int fieldIndex = factory.getMetaData().getFieldIndexByFieldName(field);
        boolean writable;
        boolean readOnly;
        try {
            writable = factory.getMetaData().isWritable(fieldIndex);
            readOnly = factory.getMetaData().isReadOnly(fieldIndex);
            if (writable && !readOnly) {
                return true;
            } else {
                return false;
            }
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    throw new DataAccessException(e);
                }
            });
        }
        return true;
    }

    public AbstractDomainObjectFactory getFactory() {
        return factory;
    }

    public void setFactory(AbstractDomainObjectFactory factory) {
        this.factory = factory;
        fireTableStructureChanged();
    }

    public List<String> getDisplayableFields() {
        if (displayableFields == null || displayableFields.isEmpty()) {
            displayableFields = new ArrayList(this.factory.getMapping().values());
        }
        return displayableFields;
    }

    public void setDisplayableFields(java.util.List displayableFields) {
        this.displayableFields = displayableFields;
    }

    public void removeRow(int row) {
        DomainObject domainObject = (DomainObject) factory.getObjectList().get(row);
        if (domainObject.getPersistentState() == PersistentState.CREATED) {
            factory.discard(domainObject);
            fireTableRowsDeleted(row, row);
        } else {
            if (factory.remove(domainObject)) {
                fireTableRowsDeleted(row, row);
            }
        }
    }

    public void discardRow(int row) {
        DomainObject domainObject = (DomainObject) factory.getObjectList().get(row);
        factory.discard(domainObject);
        fireTableRowsDeleted(row, row);
    }

    public void addRow() {
        DomainObject domainObject = (DomainObject) factory.newInstance();
        addRow(domainObject);
    }

    public void addRow(DomainObject domainObject) {
        try {
            factory.create(domainObject);
            int row = factory.getObjectList().size() - 1;
            fireTableRowsInserted(row, row);            
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    throw new DataAccessException(e);
                }
            });
        }
    }

    public void insertRow(int row, DomainObject domainObject) {
        try {
            domainObject.create();
            //int insertedRow = row + 1;
            if (!domainObject.getClass().getName().contains("EnhancerByCGLIB")) {         
                factory.getObjectList().add(row, domainObject);
            } else { // already in objectList                
                factory.getObjectList().add(row, domainObject);
                factory.getObjectList().remove(factory.getObjectList().size() - 1);                
            }
            //fireTableRowsInserted(row, row);
            fireTableDataChanged();
        } catch (final Exception e) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    throw new DataAccessException(e);
                }
            });
        }
    }

    public void insertRow(int row) {
        insertRow(row, (DomainObject) factory.newInstance());
    }

    public java.util.Map getDisplayValues() {
        return displayValues;
    }

    public void setDisplayValues(java.util.Map displayValues) {
        this.displayValues = displayValues;
    }

    public void setFieldDisplayValues(String field, Map values) {
        if (this.displayValues == null) {
            this.displayValues = new HashMap();
        }
        this.displayValues.put(field, values);
    }

    public Map getFieldDisplayValues(String field) {
        if (this.displayValues == null) {
            return null;
        }
        return (Map) this.displayValues.get(field);
    }

    /**
     * Override as needed
     */
    protected boolean handleException(DomainObject domainObject, String field, Object value, Throwable t) {
        boolean ignore = false;
        return ignore;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
    
    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}

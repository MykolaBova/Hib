package org.julp.gui.swing.table;

import javax.swing.table.*;
import java.util.Map;

public class DisplayValueTableRenderer extends DefaultTableCellRenderer {

    private static final long serialVersionUID = -1483000724926413301L;

    /*
     *  Renders display value (label) of the <real> value using provided map of
     *  values/display values (labels)     
     */
    public DisplayValueTableRenderer() {
        super();
    }
    Map mapping;

    @Override
    public void setValue(Object value) {
        Object valueLabel = mapping.get(value);
        if (valueLabel == null) {
            setText((value == null) ? "" : value.toString());
        } else {
            setText((String) valueLabel);
        }
    }

    public java.util.Map getMapping() {
        return mapping;
    }

    public void setMapping(java.util.Map mapping) {
        this.mapping = mapping;
    }
}

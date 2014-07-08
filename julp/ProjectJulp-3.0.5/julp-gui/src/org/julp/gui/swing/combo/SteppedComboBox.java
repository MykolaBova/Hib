package org.julp.gui.swing.combo;

import java.awt.*;
import java.util.*;

import javax.swing.*;
import javax.swing.plaf.metal.*;
import javax.swing.plaf.basic.*;

/**
 * @author Nobuo Tamemasa
 * @version 1.0 12/12/98
 */
public class SteppedComboBox extends JComboBox {

    private static final long serialVersionUID = -7809062571406169845L;
    protected int popupWidth;
    protected JTable parentTable;
    protected int dataColumn = -1;

    public SteppedComboBox(ComboBoxModel aModel) {
        super(aModel);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    public SteppedComboBox() {
        super();
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    public SteppedComboBox(Object[] items) {
        super(items);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    public SteppedComboBox(Vector items) {
        super(items);
        setUI(new SteppedComboBoxUI());
        popupWidth = 0;
    }

    public void setPopupWidth(int width) {
        popupWidth = width;
    }

    public Dimension getPopupSize() {
        Dimension size = getSize();
        if (popupWidth < 1) {
            popupWidth = size.width;
        }
        return new Dimension(popupWidth, size.height);
    }

    public JTable getParentTable() {
        return parentTable;
    }

    public void setParentTable(JTable parentTable) {
        this.parentTable = parentTable;
    }

    public int detDataColumn() {
        return dataColumn;
    }

    public void setDataColumn(int dataColumn) {
        this.dataColumn = dataColumn;
    }
}

/**
 * @author Nobuo Tamemasa
 * @version 1.0 12/12/98
 */
class SteppedComboBoxUI extends MetalComboBoxUI {

    @Override
    protected ComboPopup createPopup() {

        BasicComboPopup popup = new BasicComboPopup(comboBox) {

            private static final long serialVersionUID = 7887944716090080539L;

            @Override
            public void setVisible(boolean visible) {
                if (!visible) {
                    super.setVisible(visible);
                    return;
                }
                Dimension popupSize = ((SteppedComboBox) comboBox).getPopupSize();
                int comboBoxWidth = comboBox.getWidth();
                if (comboBoxWidth > popupSize.width) {
                    //popupSize = new Dimension(comboBoxWidth, popupSize.height);
                    popupSize.width = comboBoxWidth;
                }
                popupSize.setSize(popupSize.width, getPopupHeightForRowCount(comboBox.getMaximumRowCount()));
                Rectangle popupBounds = computePopupBounds(0, comboBox.getBounds().height, popupSize.width, popupSize.height);
                scroller.setMaximumSize(popupBounds.getSize());
                scroller.setPreferredSize(popupBounds.getSize());
                scroller.setMinimumSize(popupBounds.getSize());
                list.invalidate();
                int selectedIndex = comboBox.getSelectedIndex();
                if (selectedIndex == -1) {
                    list.clearSelection();
                } else {
                    list.setSelectedIndex(selectedIndex);
                }
                list.ensureIndexIsVisible(list.getSelectedIndex());
                setLightWeightPopupEnabled(comboBox.isLightWeightPopupEnabled());
                super.setVisible(visible);
                // show(comboBox, popupBounds.x, popupBounds.y);
            }
        };

        popup.getAccessibleContext().setAccessibleParent(comboBox);
        return popup;
    }
}

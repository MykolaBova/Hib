package org.julp.gui.swing.menu;

import javax.swing.*;
import org.julp.gui.swing.actions.text.*;
import java.awt.Component;

public class TextComponentPopupMenu extends JPopupMenu {

    private static final long serialVersionUID = -3284134559165482467L;
    protected Component parentComponent;

    public TextComponentPopupMenu() {
    }

    public TextComponentPopupMenu(String label) {
        super(label);
    }

    public void setup() {
        try {
            javax.swing.JMenuItem selectText = new javax.swing.JMenuItem();
            selectText.setAction(new SelectAllAction());
            javax.swing.JMenuItem copy = new javax.swing.JMenuItem();
            copy.setAction(new CopyAction());
            javax.swing.JMenuItem paste = new javax.swing.JMenuItem();
            paste.setAction(new PasteAction());
            javax.swing.JMenuItem cut = new javax.swing.JMenuItem();
            cut.setAction(new CutAction());
            javax.swing.JMenuItem delete = new javax.swing.JMenuItem();
            delete.setAction(new DeleteAction());
            this.add(selectText);
            this.add(copy);
            this.add(cut);
            this.add(paste);
            this.add(delete);
            this.addSeparator();

        } catch (Throwable t) {
            JOptionPane.showMessageDialog(parentComponent, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public Component getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(Component parentComponent) {
        this.parentComponent = parentComponent;
    }
}

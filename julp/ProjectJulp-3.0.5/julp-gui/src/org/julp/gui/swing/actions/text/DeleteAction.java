package org.julp.gui.swing.actions.text;

import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.text.TextAction;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import javax.swing.Icon;

public class DeleteAction extends TextAction {

    public DeleteAction(String iconPath) {
        super("delete-text-action");
        putValue(Action.NAME, "Delete");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_DELETE));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Delete16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public DeleteAction() {
        super("delete-text-action");
        putValue(Action.NAME, "Delete");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_DELETE));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    protected void init() {
        putValue(Action.NAME, "Delete");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_DELETE));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            target.replaceSelection("");
        }
    }
}

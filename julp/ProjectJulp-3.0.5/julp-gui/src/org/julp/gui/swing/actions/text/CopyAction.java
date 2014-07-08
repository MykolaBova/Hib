package org.julp.gui.swing.actions.text;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.text.DefaultEditorKit;

public class CopyAction extends DefaultEditorKit.CopyAction {

    public CopyAction(String iconPath) {
        putValue(Action.NAME, "Copy");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/Copy16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    public CopyAction() {
        putValue(Action.NAME, "Copy");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
    }

    protected void init() {
        putValue(Action.NAME, "Copy");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_C));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
    }
}

package org.julp.gui.swing.actions.text;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.ImageIcon;
import javax.swing.text.JTextComponent;
import java.awt.event.ActionEvent;
import javax.swing.Icon;
import javax.swing.text.TextAction;

public class SelectAllAction extends TextAction {

    private static final long serialVersionUID = -2195190425794504801L;

    public SelectAllAction(String iconPath) {
        super("select-text-action");
        putValue(Action.NAME, "Select All");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
        try {
            //iconPath = "/toolbarButtonGraphics/general/AlignJustifyHorizontal16.gif";
            Icon icon = new ImageIcon(getClass().getResource(iconPath));
            putValue(Action.SMALL_ICON, icon);
        } catch (Exception e) {
            // ignore
        }
    }

    protected void init() {
        putValue(Action.NAME, "Select All");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
    }

    public SelectAllAction() {
        super("select-text-action");
        putValue(Action.NAME, "Select All");
        putValue(Action.SHORT_DESCRIPTION, getValue(Action.NAME));
        putValue(Action.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_L));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_MASK));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
            target.selectAll();
        }
    }
}

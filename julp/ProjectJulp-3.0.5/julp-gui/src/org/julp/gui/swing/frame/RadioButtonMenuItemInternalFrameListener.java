package org.julp.gui.swing.frame;

public class RadioButtonMenuItemInternalFrameListener extends javax.swing.JRadioButtonMenuItem implements javax.swing.event.InternalFrameListener {
    private static final long serialVersionUID = -8048500520675288338L;

    /** Creates a new instance of RadioButtonMenuItemInternalFrameListener */
    public RadioButtonMenuItemInternalFrameListener() {
    }

    @Override
    public void internalFrameActivated(javax.swing.event.InternalFrameEvent e) {
        this.setSelected(true);
    }

    @Override
    public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
        //System.out.println("RadioButtonMenuItemInternalFrameListener::internalFrameClosed: " + e.getSource());
        java.awt.Container cont = null;
        try {
            cont = this.getParent();
            cont.remove(this);
        } catch (NullPointerException npe) {
            System.out.println("cont: " + cont);
        }
    }

    @Override
    public void internalFrameClosing(javax.swing.event.InternalFrameEvent e) {
        //System.out.println("RadioButtonMenuItemInternalFrameListener::internalFrameClosing: " + e.getSource());
    }

    @Override
    public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent e) {
    }

    @Override
    public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent e) {
    }

    @Override
    public void internalFrameIconified(javax.swing.event.InternalFrameEvent e) {
    }

    @Override
    public void internalFrameOpened(javax.swing.event.InternalFrameEvent e) {
    }
}

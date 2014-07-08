package org.julp.examples;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.julp.gui.swing.ErrorDisplayPanel;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    private Window window;

    public ExceptionHandler() {

//        SwingUtilities.invokeLater(new Runnable() {
//            @Override
//            public void run() {
        long eventMask = AWTEvent.WINDOW_EVENT_MASK;
        Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
            @Override
            public void eventDispatched(AWTEvent e) {
                if (e instanceof WindowEvent) {
                    if (e.getID() == WindowEvent.WINDOW_OPENED) {
                    } else if (e.getID() == WindowEvent.WINDOW_CLOSING) {
                    } else if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
                        window = (Window) e.getSource();
                        System.err.println(window);
                    }
                }
            }
        }, eventMask);
        //   }
        //});
    }

    public void handle(Throwable t) {
        // EDT exceptions
        handleException(Thread.currentThread().getName(), t, true);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable t) {
        // other uncaught exceptions
        handleException(thread.getName(), t, false);
    }

    protected void handleException(String tname, final Throwable t, boolean edtException) {
        t.printStackTrace();
        if (edtException) {
            //Window w = SwingUtilities.windowForComponent(comp);
            //Frame f = JOptionPane.getFrameForComponent(comp);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ErrorDisplayPanel errorPanel = new ErrorDisplayPanel(null, t);
                    final Component comp = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();
                    //Window w = SwingUtilities.getWindowAncestor(comp);
                    //Window w = SwingUtilities.windowForComponent(comp);
                    //Frame f = JOptionPane.getFrameForComponent(comp);
                    //System.err.println("getFocusOwner(): " + comp);
                    JOptionPane.showMessageDialog(window, errorPanel, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        }
    }
}

package org.julp.gui.generator;

public class PropertiesHolder implements java.io.Serializable {
    
    protected static String userHome = null;   
    private static final long serialVersionUID = -6940723085615259772L;

    private PropertiesHolder() {
    }
    
    public static PropertiesHolder getInstance() {
       return Holder.instance;
    }

     private static class Holder {
        static PropertiesHolder instance = new PropertiesHolder();
    }
    
    public java.lang.String getUserHome() {
        return userHome;
    }    
    
    public void setUserHome(java.lang.String userHome) {
        this.userHome = userHome;
    }    
    
}

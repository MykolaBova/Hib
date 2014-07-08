package org.julp.application;

import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.util.Iterator;
import java.util.Map;
import javax.security.auth.login.LoginContext;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 * <code>
 * public static void main(String[] args) {
 *      new ApplicationDescendant().getInstance().begin(args);
 * }
 * </code>
 */
public abstract class Application {

    protected LoginContext loginContext;
    protected String[] args;

    public Application() {
    }

    public void begin(String[] args) {
        this.args = args;
        try {
            configure();
            login();
            afterBegin();
        } catch (Throwable t) {
            t.printStackTrace();
            if (!GraphicsEnvironment.isHeadless()) {
                javax.swing.JOptionPane.showMessageDialog(null, t, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
            throw new RuntimeException(t);
        }
    }

    protected void afterBegin() {
    }

    /**     
     * // Create and populate Application Config. Implement in descendent object.
     * // example:
     * <code>
     * try{
     *    if (args.length != 0) {
     *        for (int i = 0;i < args.length;i++) {
     *            if (args[i].trim().toLowerCase().startsWith("-u")) {
     *                configURL = args[i].trim().substring(2);
     *            } else if (args[i].trim().toLowerCase().startsWith("-f")) {
     *                configFile = args[i].trim().substring(6);
     *            } else if (args[i].trim().toLowerCase().startsWith("-c")) {
     *                configClassName = args[i].trim().substring(6);
     *            }
     *        }
     *
     *        if (configClassName != null) {
     *            try{
     *                config = (Config) Class.forName(configClassName).newInstance();
     *            } catch (ClassNotFoundException e) {
     *                throw new Exception("Config ClassName (" + configClassName +") not found", e);
     *            } catch (InstantiationException e) {
     *                throw new Exception("Config (" + configClassName +") cannot be instantiated", e);
     *            } catch (IllegalAccessException e) {
     *                throw new Exception("Illegal Access for Config (" + configClassName +")", e);
     *            }
     *        } else {
     *            throw new NullPointerException("Missing argument: Config ClassName (-c)");
     *        }
     *
     *        if (configFile != null) {
     *            config.load(configFile);
     *        } else if (configURL != null) {
     *            URL url = new URL(configURL);
     *            config.load(url);
     *        }
     *
     *        if (configFile == null && configURL == null) {
     *            throw new NullPointerException("Please provide Config file path (-f) or URL (-u)");
     *        }
     *
     *        //String loginModuleConfigLocation = Config.getConfigValue("//login/java.security.auth.login.config");
     *     } else {
     *        throw new NullPointerException("Missing Parameters: Config ClassName & Configuration file or URL or XML");
     *    }
     * } catch (Exception e) {
     *    e.printStackTrace();
     *    throw new RuntimeException("Failure to configure: " + e.toString(), e);
     * }
     *
     * </code>
     *
     * // another example:
     *
     * <code>
     * try{
     *    if (args.length == 2) {
     *        String source = args[0];
     *        int idx = source.indexOf("-s");
     *        source = source.substring(idx + 2).trim();
     *        String sourceType = args[1];
     *        idx = sourceType.indexOf("-t");
     *        sourceType = sourceType.substring(idx + 2).trim();
     *        if (sourceType.equalsIgnoreCase("file")) {
     *            MyConfig.getInstance().load(source);
     *        } else if (sourceType.equalsIgnoreCase("url")) {
     *            URL url = new URL(source);
     *            MyConfig.getInstance().load(url);
     *        } else if (sourceType.equalsIgnoreCase("reader")) {
     *            MyConfig.getInstance().setXML(source);
     *            MyConfig.getInstance().load();
     *        } else {
     *            throw new IllegalArgumentException("Invalid Source type: " + sourceType + ". \nValid Source type(s): file, url, reader");
     *        }
     *    } else {
     *        throw new NullPointerException("Missing Parameter(s): Configuration Source and/or Source Type [File] [URL] [Reader] example: \n -s c:\\bes\\conf\\config.xml -t file \n -s http://bes/conf/config.xml -t url  \n -s <?xml version=\"1.0\" encoding=\"UTF-8\"?>\n  <configuration> ... \n   </configuration> -t reader");
     *   }
     * } catch (Exception e) {
     *   throw new RuntimeException(e);
     * }
     * boolean unitGUI = Boolean.parseBoolean(MyConfig.getInstance().getConfigValue(MyConstants.INIT_UI));
     * if (!unitGUI) {
     *    return;
     * }
     * Map uid = new HashMap();
     * String defaultlaf = MyConfig.getInstance().getConfigValue(MyConstants.PLAF);
     * if (defaultlaf != null) {
     *    if (defaultlaf.equals("CrossPlatformLookAndFeel")) {
     *        defaultlaf = UIManager.getCrossPlatformLookAndFeelClassName();
     *    } else if (defaultlaf.equals("SystemLookAndFeel")) {
     *        defaultlaf = UIManager.getSystemLookAndFeelClassName();
     *    }
     * } else {
     *    defaultlaf = UIManager.getCrossPlatformLookAndFeelClassName();
     * }
     *
     * int defaultFontSize = Integer.parseInt(MyConfig.getInstance().getConfigValue(MyConstants.DEFAULT_FONT_SIZE));
     * javax.swing.plaf.FontUIResource font = new javax.swing.plaf.FontUIResource(MyConfig.getInstance().getConfigValue(MyConstants.DEFAULT_FONT_NAME), java.awt.Font.PLAIN, defaultFontSize);
     * java.util.Enumeration keys = UIManager.getDefaults().keys();
     * while (keys.hasMoreElements()) {
     *    Object key = keys.nextElement();
     *    Object value = UIManager.get(key);
     *    if (value instanceof javax.swing.plaf.FontUIResource) {
     *        uid.put(key, font);
     *    }
     * }
     *initUI(defaultlaf, uid);
     *
     *</code>
     *
     */
    protected abstract void configure();

    /**
     * Implement in descendent object. Example:
     * <code>
     * protected void configure() {
     *      Map uid = new HashMap();
     *      String defaultlaf = Config.getConfigValue("//defaults//look-and-feel");
     *      if (defaultlaf != null) {
     *          if (defaultlaf.equals("CrossPlatformLookAndFeel")) {
     *              defaultlaf = UIManager.getCrossPlatformLookAndFeelClassName();
     *          } else if (defaultlaf.equals("SystemLookAndFeel")) {
     *              defaultlaf = UIManager.getSystemLookAndFeelClassName();
     *          }
     *      } else {
     *          defaultlaf = UIManager.getCrossPlatformLookAndFeelClassName();
     *      }
     *
     *      int defaultFontSize = Integer.parseInt(Config.getConfigValue("//defaults//font//size"));
     *      javax.swing.plaf.FontUIResource font = new javax.swing.plaf.FontUIResource(Config.getConfigValue("//defaults//font//name"), java.awt.Font.PLAIN, defaultFontSize);
     *      java.util.Enumeration keys = UIManager.getDefaults().keys();
     *      while (keys.hasMoreElements()) {
     *          Object key = keys.nextElement();
     *          Object value = UIManager.get(key);
     *          if (value instanceof javax.swing.plaf.FontUIResource) {
     *              uid.put(key, font);
     *          }
     *      }
     *
     *      initUI(defaultlaf, uid);
     *
     * }
     * </code>
     */
    protected static void initUI(String defaultlaf, Map uid) {
        if (!GraphicsEnvironment.isHeadless()) {
            Iterator iter = uid.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                UIManager.put(entry.getKey(), entry.getValue());
            }
            try {
                if (isLookAndFeelAvailable(defaultlaf)) {
                    UIManager.setLookAndFeel(defaultlaf);
                } else {
                    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
                }
            } catch (UnsupportedLookAndFeelException exc) {
                throw new RuntimeException("UnsupportedLookAndFeelException Error", exc);
            } catch (IllegalAccessException exc) {
                throw new RuntimeException("IllegalAccessException Error", exc);
            } catch (ClassNotFoundException exc) {
                throw new RuntimeException("ClassNotFoundException Error", exc);
            } catch (InstantiationException exc) {
                throw new RuntimeException("InstantiateException Error", exc);
            }
        } else {
            throw new HeadlessException();
        }
    }

    /**
     * Implement in descendent object. Example:
     * <code>
     * protected abstract void login() {
     *      String realm = Config.getConfigValue("//login/realm");
     *      String iconURL = Config.getConfigValue("//login/icon-url");
     *      UsernamePasswordCallbackHandler callbackHandler = new UsernamePasswordCallbackHandler();
     *      try{
     *          loginContext = new LoginContext(realm, subject, callbackHandler);
     *          LoginlDialog loginlDialog = new LoginlDialog(null, iconURL, loginContext, callbackHandler);
     *          subject = loginContext.getSubject();
     *      } catch (Exception e) {
     *          if (!GraphicsEnvironment.isHeadless()) {
     *              javax.swing.JOptionPane.showMessageDialog(null, e, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
     *          }
     *          throw new RuntimeException(e);
     *      }
     * }
     * </code>
     */
    protected abstract void login();

    /**
     * Implement in descendant object to release resources, etc...
     */
    public abstract void end();

    protected static boolean isLookAndFeelAvailable(String laf) {
        try {
            Class lnfClass = Class.forName(laf);
            LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
            return newLAF.isSupportedLookAndFeel();
        } catch (Exception e) {
            return false;
        }
    }
}

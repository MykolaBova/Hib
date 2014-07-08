package simpleorm.utils;

import java.io.PrintStream;

import simpleorm.dataset.SSessionI;

/**
 * Very simple logging system.
 * <p>
 * 
 * Logging calls are all made <code>SLog.log.</code>method so overriding this
 * class with a subclass of SLog provides full control of the logging if really
 * necessary.<p>
 * 
 * Most logging happens in the context of a session, which should be used to identify
 * and separate multiple threads in a log file.  However, some logging has no session
 * or dataset,  so we cannot simply associate the session with the slog object, and the
 */

public class SLog {
	static int level = 100;

    static Class<? extends SLog> slogClass = SLog.class; // or set to SLogSlf4J.class

	static SLog sessionlessLogger = newSLog();
    
    SSessionI session = null;

    public static SLog newSLog() {
        try {return slogClass.newInstance();}
        catch (Exception ex) {throw new SException.Error(ex);}
    }
    
	public void error(String msg) { // Rarely used, normally throw instead.
		log("#### -ERROR " + sessionToString() + "_" + msg);
	}

	public void warn(String msg) {
		log("## -WARN " + sessionToString() + "_" + msg);
	}

	// Open, begin, commit etc.
	public void connections(String msg) {
		if (level >= 10)
			log("  -" + msg);
	}

	// During flushing
	public boolean enableUpdates() {
		return level >= 20;
	}

	public void updates(String msg) {
		if (enableUpdates())
			log("    -" + msg);
	}

	// select() or findOrCreate()
	public boolean enableQueries() {
		return level >= 30;
	}

	public void queries(String msg) {
		if (enableQueries())
			log("      -" + msg);
	}

	/**
	 * set/get per field. enableFields enables the trace line to be surounded by
	 * an if test which is important as fields are an inner loop trace and the
	 * StringBuffer concatenation can be very expensive!
	 */
	public boolean enableFields() {
		return level >= 40;
	}

	public void fields(String msg) {
		if (enableFields())
			log("        -" + msg);
	}

	/** For detailed temporary traces during development only. */
	public boolean enableDebug() {
		return level >= 30;
	}

	public void debug(String msg) {
		if (enableDebug())
			log("          -" + "(" + msg + ")");
	}

	/**
	 * For messages that are the ouput, eg. of unit test programs. Never
	 * disabled.
	 */
	public void message(String msg) {
		log(" ---" + msg);
	}

	protected void log(String msg) {
		// Specialize this method.
    		getStream().println(sessionToString() + msg);
	}

	public PrintStream getStream() {
		return System.err;
	}

	public static String arrayToString(Object[] array) {
		if (array == null)
			return "NULL";
		StringBuffer res = new StringBuffer("[Array ");
		for (int ax = 0; ax < array.length; ax++) {
			if (ax > 0)
				res.append("|");
			res.append(array[ax]);
		}
		res.append("]");
		return res.toString();
	}

    protected String sessionToString() {
        return getSession() != null ? getSession().toString() : "[no Session] ";
    }

    //////////////// empty get/set ////////////////

    
    public static SLog getSessionlessLogger() { return sessionlessLogger; }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public static Class<? extends SLog> getSlogClass() {
        return slogClass;
    }

    public static void setSlogClass(Class<? extends SLog> slogClass) {
        SLog.slogClass = slogClass;
        SLog.sessionlessLogger = newSLog();
    }
    
    public SSessionI getSession() {
        return session;
    }

    public void setSession(SSessionI session) {
        this.session = session;
    }
    
}

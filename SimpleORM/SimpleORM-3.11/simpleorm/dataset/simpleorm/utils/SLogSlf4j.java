package simpleorm.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;


/**
 * An implementation of SLog to allow Simpleorm to log via Slf4j.
 * Log level is then controlled by slf4j, not by SLog.slog.level.
 * 
 * Mapping between SLog and slf4j log levels is:
 * 
 * SLog				Slf4j    - Marker
 * ---------------- --------------------
 * debug	   (30)	debug
 * warn				warn
 * message			warn
 * error			error
 * connections (10)	info
 * updates	   (20)	debug	   SORM_UPDATE
 * queries	   (30)	debug	   SORM_QUERY
 * fields	   (40)	trace	   SORM_FIELD
 * 
 * To use this logging API, you must:
 * 1) SLog.setSlogClass(SLogSlf4j.class); somewhere early in you app (in you servlet init sequence for example)
 * 2) make sure you have a slf4j implementation in your classpath (logback, simplelogger, etc.)
 * 
 * Configure slf4j, for examples, @see http://www.slf4j.org/
 * @author franck.routier@axege.com
 *
 */
public class SLogSlf4j extends SLog {
	
	Logger log = LoggerFactory.getLogger(SLogSlf4j.class);
	static Marker UPDATE_MARKER = MarkerFactory.getMarker("SORM_UPDATE");
	static Marker QUERY_MARKER = MarkerFactory.getMarker("SORM_QUERY");
	static Marker FIELD_MARKER = MarkerFactory.getMarker("SORM_FIELD");
	
	public int level = 10;

	public void error(String msg) { // Rarely used, normally throw instead.
		log.error("#### -ERROR {}_{}",sessionToString(), msg);
	}

	public void warn(String msg) {
		log.warn("## -WARN {}_{}",sessionToString(), msg);
	}

	// Open, begin, commit etc. 
	public void connections(String msg) {
		log.info("  -{}", sessionToString()+msg);
	}

	// During flushing
	public boolean enableUpdates() {
		return log.isDebugEnabled(UPDATE_MARKER);
	}

	public void updates(String msg) {
		if (enableUpdates())
			log.debug(UPDATE_MARKER, "    -{}", sessionToString()+msg);
	}

	// select() or findOrCreate()
	public boolean enableQueries() {
		return log.isDebugEnabled(QUERY_MARKER);
	}

	public void queries(String msg) {
		if (enableQueries())
			log.debug(QUERY_MARKER, "      -{}", sessionToString()+msg);
	}

	/**
	 * set/get per field. enableFields enables the trace line to be surounded by
	 * an if test which is important as fields are an inner loop trace and the
	 * StringBuffer concatenation can be very expensive!
	 */
	public boolean enableFields() {
		return log.isDebugEnabled(FIELD_MARKER);
	}

	public void fields(String msg) {
		if (enableFields())
			log.trace(FIELD_MARKER, "        -{}", sessionToString()+msg);
	}

	/** For detailed temporary traces during development only. */
	public boolean enableDebug() {
		return log.isDebugEnabled();
	}

	public void debug(String msg) { 
		log.debug("          -({})", sessionToString()+msg);
	}

	/**
	 * For messages that are the ouput, eg. of unit test programs. Never
	 * disabled.
	 */
	public void message(String msg) {
		log.warn(" ---{}", sessionToString()+msg);
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
    
}

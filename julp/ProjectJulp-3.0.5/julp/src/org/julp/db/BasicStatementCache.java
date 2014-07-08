package org.julp.db;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * In case JDBC driver or a Connection pool does not support
 * <code>Statement</code> pooling (caching).
 */
public class BasicStatementCache {

    protected int maxStatementCacheSize = 20; // must be set before statementsCache init, default: 20
    protected List<String> statementsId; // cached statements id (sql String)
    protected List<Statement> statementsCache;  // cached statements
    protected List<Integer> hits; // cached statements usage hits
    private static final transient Logger logger = Logger.getLogger(BasicStatementCache.class.getName());

    public BasicStatementCache() {

    }

    public BasicStatementCache(int maxStatementCacheSize) {
        this.maxStatementCacheSize = maxStatementCacheSize;
    }

    private void init() {
        statementsCache = new ArrayList<>(maxStatementCacheSize);
        statementsId = new ArrayList<>(maxStatementCacheSize);
        hits = new ArrayList<>(maxStatementCacheSize);
    }

    public Statement get(String sql) throws SQLException {
        if (statementsId == null) {
            return null;
        }
        Object obj = null;
        int idx = statementsId.indexOf(sql);
        if (idx >= 0) {
            obj = statementsCache.get(idx);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::get()::found cached statement at index: " + idx + "::" + (obj == null ? "<null>" : obj.toString() + "::" + obj.getClass()) + " \n");
                logger.finest("julp::" + this + "::statementsId: " + statementsId);
                logger.finest("julp::" + this + "::statementsCache: " + statementsCache);
                logger.finest("julp::" + this + "::hits: " + hits);
            }
            if (obj == null) { // for some reson like garbage collected? than add again
                statementsId.remove(idx);
                statementsCache.remove(idx);
                hits.remove(idx);
                return null;
            } else {
                if (((Statement) obj).getConnection() == null || ((Statement) obj).getConnection().isClosed() || ((Statement) obj).isClosed()) {
                    obj = null;
                    statementsId.remove(idx);
                    statementsCache.remove(idx);
                    hits.remove(idx);
                    return null;
                }
                if (obj instanceof PreparedStatement) {
                    try {
                        ((PreparedStatement) obj).clearParameters();
                    } catch (SQLException e) {
                        logger.severe(e.toString());
                    }
                }
                // How many times this Statement was called?
                Integer count = hits.get(idx);
                hits.set(idx, count == null ? 1 : count + 1);
            }
        }
        return obj == null ? null : (obj instanceof PreparedStatement ? (PreparedStatement) obj : (Statement) obj);
    }

    public void add(String statementId, Statement statement) throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::add()::statementId: " + statementId + "::statement: " + statement + "::isPoolable(): " + statement.isPoolable() + "\n");
        }
        if (!statement.isPoolable()) {
            return;
        }
        if (statementsId == null) {
            init();
        }
        if (statementsId.isEmpty()) {
            statementsId.add(statementId);
            statementsCache.add(statement);
            hits.add(0);
            return;
        }
        int idx = statementsId.indexOf(statementId);
        if (idx < 0) {
            if (statementsId.size() + 1 == maxStatementCacheSize) {
                // find and remove the list frequently required Statement
                Object listFrequentlyUsed = Collections.min(hits);
                int listFrequentlyUsedIndex = hits.indexOf(listFrequentlyUsed);
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::add()::listFrequentlyUsedIndex: " + listFrequentlyUsedIndex + "\n");
                }
                statementsId.remove(listFrequentlyUsedIndex);
                Object obj = statementsCache.get(listFrequentlyUsedIndex);
                if (obj != null) {
                    if (logger.isLoggable(Level.FINEST)) {
                        logger.finest("julp::" + this + "::add()::closing Statement: " + obj + " \n");
                    }
                    ((Statement) obj).close();
                    obj = null;
                }
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::add()::remove Statement: " + statementsCache.get(listFrequentlyUsedIndex) + " \n");
                }
                statementsCache.remove(listFrequentlyUsedIndex);
                hits.remove(listFrequentlyUsedIndex);
            }
            statementsId.add(statementId);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::add()::statementToCache: " + statement + "::" + statement.getClass() + "\n");
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::add()::statementToCache instanceof PreparedStatement: " + (statement instanceof PreparedStatement) + "\n");
            }
            statementsCache.add(statement);
            hits.add(0);
        } else {
            // statement is  in cache already
            Statement stmt = statementsCache.get(idx);
            if (stmt == null || stmt.getConnection() == null || stmt.getConnection().isClosed() || stmt.isClosed()) {
                stmt = null;
                statementsCache.set(idx, statement);
            }
        }
    }

    public void remove(String statementId) {
        if (statementsCache != null) {
            int idx = statementsId.indexOf(statementId);
            if (idx > -1) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::remove(" + statementId + "): " + statementId + "\n");
                }
                statementsId.remove(idx);
                hits.remove(idx);
                statementsCache.remove(idx);
            }
        }
    }

    public void clear() throws SQLException {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::clear()" + "\n");
        }
        if (statementsCache != null) {
            Iterator<?> iter = statementsCache.iterator();
            while (iter.hasNext()) {
                Object obj = iter.next();
                if (obj != null) {
                    ((Statement) obj).close();
                }
            }
            statementsCache.clear();
            hits.clear();
            statementsId.clear();
        }
    }

}

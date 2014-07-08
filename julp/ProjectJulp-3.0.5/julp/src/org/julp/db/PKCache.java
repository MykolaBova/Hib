package org.julp.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.julp.DataAccessException;

/**
 * Retrieves and caches PrimaryKeys from DatabaseMetaData or user input
 */
public class PKCache implements java.io.Serializable {

    private static final long serialVersionUID = -2506547717723319217L;
    private static final transient Logger logger = Logger.getLogger(LazyLoader.class.getName());
    /**
     * Key for each table
     */
    protected Map<String, Set<String>> pkCache = new HashMap<>();
    private static final String DOT = ".";

    private PKCache() {
    }

    private static class PKCacheHolder {

        static PKCache instance = new PKCache();
    }

    public static PKCache getInstance() {
        return PKCacheHolder.instance;
    }

    /*
     * This method allows to set columns for a table PrimaryKey without Database connection, etc.
     *
     * Make sure to use format:  catalog + DOT + schema + DOT + table + DOT + column
     * or schema + DOT + table + DOT + column
     */
    public synchronized void setPrimaryKey(String catalog, String schema, String table, Set<String> pk) {
        String tableId = buildTableId(catalog, schema, table);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::setPrimaryKey()::tableId: " + tableId + " pk: " + pk);
        }
        if (pk == null || pk.isEmpty()) {
            throw new IllegalArgumentException("setPrimaryKey(): PK list is empty");
        }
        pkCache.put(tableId, pk);
    }

    /**
     * Retrieves PK from cache, add additional columns to it and put it in cache. If PK is not in cache returns null
     */
    public synchronized Set<String> populatePrimaryKey(String catalog, String schema, String table, Set<String> additionalColumns) {
        Set<String> pk = getPrimaryKey(catalog, schema, table);
        if (pk == null) {
            return null;
        }
        if (additionalColumns != null) {
            pk.addAll(additionalColumns);
            String tableId = buildTableId(catalog, schema, table);
            this.pkCache.put(tableId, pk);
        }
        return pk;
    }

    /**
     * Retrieves PK from DatabaseMetaData, add additional columns to it and put it in cache
     */
    public synchronized Set<String> populatePrimaryKey(Connection connection, String catalog, String schema, String table, Set<String> additionalColumns) {
        Set<String> pk = getPrimaryKey(connection, catalog, schema, table);
        if (additionalColumns != null) {
            pk.addAll(additionalColumns);
            String tableId = buildTableId(catalog, schema, table);
            this.pkCache.put(tableId, pk);
        }
        return pk;
    }

    /**
     * Retrieves PK from cache. If PK is not in cache returns null
     */
    public synchronized Set<String> getPrimaryKey(String catalog, String schema, String table) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getPrimaryKey()::catalog: " + catalog + "::schema: " + schema + "::table: " + table);
        }
        String tableId = buildTableId(catalog, schema, table);
        if (this.pkCache.containsKey(tableId)) {
            return this.pkCache.get(tableId); //it's already cached
        } else {
            return null;
        }
    }

    /**
     * Retrieves PK from DatabaseMetaData and put it in cache. If PK is in cache returns PK from cache
     */
    public synchronized Set<String> getPrimaryKey(Connection connection, String catalog, String schema, String table) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getPrimaryKey()::catalog: " + catalog + "::schema: " + schema + "::table: " + table);
        }
        String tableId = buildTableId(catalog, schema, table);
        Set<String> pkFields = this.pkCache.get(tableId);
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getPrimaryKey()::tableId: " + tableId + " pkFields: " + pkFields);
        }
        if (pkFields != null) {
            return pkFields; //it's already cached
        } else {
            pkFields = new LinkedHashSet<>();
        }

        java.sql.ResultSet pkInfo = null;
        try {
            java.sql.DatabaseMetaData dbmd = connection.getMetaData();
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getPrimaryKey()::DatabaseMetaData: " + dbmd.getDatabaseProductName() + "::catalog: " + catalog + "::schema: " + schema + "::table: " + table);
            }
            pkInfo = dbmd.getPrimaryKeys(catalog, schema, table);
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getPrimaryKey()::pkInfo: " + pkInfo);
            }
            while (pkInfo.next()) {
                if (logger.isLoggable(Level.FINEST)) {
                    logger.finest("julp::" + this + "::getPrimaryKey()::pkInfo.next()");
                }
                String columnName = pkInfo.getString(4);
                pkFields.add(tableId + DOT + columnName);
            }
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getPrimaryKey()::pkFields: " + pkFields);
            }
            if (pkFields.isEmpty()) {
                throw new java.sql.SQLException("Cannot retrieve PrimaryKey info from DatabaseMetaData for " + tableId);
            }
        } catch (java.sql.SQLException sqle) {
            throw new DataAccessException(sqle);
        } catch (Exception e) {
            throw new DataAccessException(e);
        } finally {
            try {
                if (pkInfo != null) {
                    pkInfo.close();
                }
            } catch (java.sql.SQLException sqle) {
                throw new DataAccessException(sqle);
            }
        }
        pkCache.put(tableId, pkFields);
        return pkFields;
    }

    private String buildTableId(String catalog, String schema, String table) {
        String tableId = null;
        if (catalog != null && catalog.trim().length() != 0) {
            tableId = catalog.trim() + DOT + schema.trim() + DOT + table.trim();
        } else if (schema != null && schema.trim().length() != 0) {
            tableId = schema.trim() + DOT + table.trim();
        } else if (table != null && table.trim().length() != 0) {
            tableId = table.trim();
        } else {
            throw new IllegalArgumentException("getPrimaryKey(): All table identifiers are missing");
        }
        return tableId;
    }
}

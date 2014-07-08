package org.julp.util.db;

import java.util.*;
import java.io.*;
import java.sql.*;
import javax.sql.*;
import org.julp.DataAccessException;
import org.julp.db.BasicDataSourceImpl;
import org.julp.db.DBServices;

public class SQLScriptExecutor {

    protected String delimiter = ";";
    protected boolean useBatch;
    protected DBServices dbServices = new DBServices();
    protected boolean autoCommit;

    /**
     * Executes SQL script(s) from file
     */
    public SQLScriptExecutor() {
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(boolean autoCommit) {
        this.autoCommit = autoCommit;
    }

    public DBServices getDbServices() {
        return dbServices;
    }

    public void setDbServices(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    public void execute(String driver, String connectionUrl, String user, String password, String optionalConnProps, String filePath, String delimiter, boolean useBatch) throws SQLException {
        this.useBatch = useBatch;
        execute(driver, connectionUrl, user, password, optionalConnProps, filePath, delimiter);
    }

    public void execute(String driver, String connectionUrl, Properties connectionProperties, String filePath, String delimiter, boolean useBatch) throws SQLException {
        BasicDataSourceImpl dataSource = new BasicDataSourceImpl();
        dataSource.setConnectionUrl(connectionUrl);
        dataSource.setDriverName(driver);
        dataSource.setConnectionProperties(connectionProperties);
        execute(dataSource, filePath, delimiter);
    }

    public void execute(DataSource dataSource, String filePath) throws SQLException {
        execute(dataSource.getConnection(), filePath, this.delimiter);
    }

    public void execute(DataSource dataSource, String filePath, String delimiter) throws SQLException {
        execute(dataSource.getConnection(), filePath, delimiter);
    }

    public void execute(Connection conn, String filePath) throws SQLException {
        execute(conn, filePath, this.delimiter);
    }

    public void execute(Connection conn, String filePath, String delimiter) throws SQLException {
        if (delimiter != null && delimiter.trim().length() > 0) {
            this.delimiter = delimiter;
        }

        File sqlScript = new File(filePath);
        if (!sqlScript.exists()) {
            throw new IllegalArgumentException("File " + filePath + " does not exist");
        }

        dbServices.setConnection(conn);

        BufferedReader in = null;
        StringBuilder sb = new StringBuilder();
        try {
            in = new BufferedReader(new FileReader(filePath));
            String str;
            while ((str = in.readLine()) != null) {
                sb.append(str);
            }
        } catch (IOException e) {
            throw new DataAccessException(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                throw new DataAccessException(e);
            }
        }

        String sql = null;
        try {
            StringTokenizer st = new StringTokenizer(sb.toString(), this.delimiter, false);
            if (conn.getMetaData().supportsBatchUpdates() && useBatch) {
                Collection batch = new ArrayList();
                while (st.hasMoreTokens()) {
                    sql = st.nextToken();
                    batch.add(sql);
                }
                if (!autoCommit) {
                    dbServices.beginTran();
                }
                dbServices.executeBatch(batch);
            } else {
                dbServices.setCacheStatements(true);
                if (!autoCommit) {
                    dbServices.beginTran();
                }
                while (st.hasMoreTokens()) {
                    sql = st.nextToken();
                    dbServices.execute(sql);
                }
            }
            if (!autoCommit) {
                dbServices.commitTran();
            }
        } catch (Exception e) {
            System.err.println("Error for SQL: " + sql);
            e.printStackTrace();
            try {
                if (!autoCommit) {
                    dbServices.rollbackTran();
                }
            } catch (SQLException sqle) {
                throw new DataAccessException(sqle);
            }
            throw new DataAccessException(e);
        } finally {
            try {
                dbServices.release(true);
            } catch (Exception e) {
                e.printStackTrace();
                throw new DataAccessException(e);
            }
        }
    }

    public void execute(String driver, String connectionUrl, Properties connectionProperties, String filePath, String delimiter) throws SQLException {
        BasicDataSourceImpl dataSource = new BasicDataSourceImpl();
        dataSource.setConnectionUrl(connectionUrl);
        dataSource.setDriverName(driver);
        dataSource.setConnectionProperties(connectionProperties);
        execute(dataSource, filePath, delimiter);
    }

    public void execute(String driver, String connectionUrl, String user, String password, String optionalConnProps, String filePath) throws SQLException {
        execute(driver, connectionUrl, user, password, optionalConnProps, filePath, this.delimiter);
    }

    public void execute(String driver, String connectionUrl, String user, String password, String optionalConnProps, String filePath, String delimiter) throws SQLException {
        if (driver == null || driver.trim().equals("")) {
            throw new IllegalArgumentException("Driver name is missing");
        }
        if (user == null || user.trim().equals("")) {
            throw new IllegalArgumentException("User name is missing");
        }
        if (connectionUrl == null || connectionUrl.trim().equals("")) {
            throw new IllegalArgumentException("DbURL is missing");
        }
        Properties prop = new Properties();
        prop.setProperty("user", user);
        prop.setProperty("password", password);
        if (optionalConnProps != null && optionalConnProps.trim().length() > 0) {
            StringTokenizer st = new StringTokenizer(optionalConnProps, ",", false);
            while (st.hasMoreTokens()) {
                String token = st.nextToken();
                int idx = token.indexOf("=");
                if (idx == -1) {
                    throw new IllegalArgumentException("Invalid optional connection properties format. \nIt must have format: name1=value1, name2=value2, ...");
                }
                String name = token.substring(0, idx);
                String value = token.substring(idx + 1);
                prop.setProperty(name, value);
            }
        }
        execute(driver, connectionUrl, prop, filePath, delimiter);
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public boolean isUseBatch() {
        return useBatch;
    }

    public void setUseBatch(boolean useBatch) {
        this.useBatch = useBatch;
    }
}

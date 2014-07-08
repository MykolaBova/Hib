package org.julp.db;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.julp.DataAccessException;

public class BasicDataSourceImpl implements DataSource {

    protected transient Connection connection;
    protected Driver driver;
    protected Properties connectionProperties = null;
    private String driverName;
    private String connectionUrl;
    private String userName;
    private String password;
    protected String connectionId = "";
    protected String connectionPropertiesDelimiter = "&";
    private static final transient Logger logger = Logger.getLogger(BasicDataSourceImpl.class.getName());

    /**
     * BasicDataSourceImpl has no pooling capability and should be used only for testing or stand-along client
     */
    public BasicDataSourceImpl() {
    }

    @Override
    public java.sql.Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            if (logger.isLoggable(Level.FINEST)) {
                logger.finest("julp::" + this + "::getConnection()::connection 1: " + connection + " \n");
            }
            try {
                driver = (Driver) Class.forName(getDriverName()).newInstance();
                if (this.getConnectionProperties() == null || this.getConnectionProperties().isEmpty()) {
                    connection = DriverManager.getConnection(getConnectionUrl(), getUserName(), getPassword());
                } else {
                    connection = DriverManager.getConnection(getConnectionUrl(), this.getConnectionProperties());
                }
            } catch (ClassNotFoundException ex) {
                throw new DataAccessException(ex);
            } catch (SQLException sqle) {
                throw new DataAccessException(sqle);
            } catch (Exception e) {
                throw new DataAccessException(e);
            }
        }
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("julp::" + this + "::getConnection()::connection 2: " + connection + " \n");
        }
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        setUserName(userName);
        setPassword(password);
        return getConnection();
    }

    @Override
    public java.io.PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public void setLogWriter(java.io.PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    public java.lang.String getConnectionId() {
        return connectionId;
    }

    public String getDriverName() {
        return this.driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public java.lang.String getUserName() {
        return userName;
    }

    public void setUserName(java.lang.String userName) {
        this.userName = userName;
    }

    public java.lang.String getPassword() {
        return password;
    }

    public void setPassword(java.lang.String password) {
        this.password = password;
    }

    public void closeConnection() throws SQLException {
        if (connection != null) {
            //connection.rollback();//?? Sybase bug
            connection.close();
            connection = null;
        }
    }

    public void setConnectionProperties(String connectionProperties) {
        StringTokenizer tokenizer = new StringTokenizer(connectionProperties, connectionPropertiesDelimiter, false);
        if (this.connectionProperties == null) {
            this.connectionProperties = new Properties();
        } else {
            this.connectionProperties.clear();
        }
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int idx = token.indexOf("=");
            if (idx == -1) {
                throw new IllegalArgumentException("BasicDataSourceImpl::setConnectionProperties(String connectionProperties): " + connectionProperties + "(Argument format: <PROPERTY_NAME>=<PROPERTY_VALUE>[DELIMITER]...)");
            }
            this.connectionProperties.put(token.substring(0, idx).trim(), token.substring(idx + 1).trim());
        }
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getConnectionPropertiesDelimiter() {
        return connectionPropertiesDelimiter;
    }

    public void setConnectionPropertiesDelimiter(String connectionPropertiesDelimiter) {
        this.connectionPropertiesDelimiter = connectionPropertiesDelimiter;
    }

    public String getConnectionUrl() {
        return connectionUrl;
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return (iface != null && iface.isAssignableFrom(org.julp.db.BasicDataSourceImpl.class));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T unwrap(java.lang.Class<T> iface) {
        if (isWrapperFor(iface)) {
            return (T) this;
        }
        throw new IllegalArgumentException("Type: " + iface.getCanonicalName() + ", must be assignable from " + org.julp.db.BasicDataSourceImpl.class);
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Method is not supported for " + getClass());
    }

}

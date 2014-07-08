package net.sourceforge.pbeans.data;

import java.util.*;
import java.sql.*;
import javax.sql.*;

import java.util.logging.*;

class ConnectionPool {
	private static final Logger logger = Logger.getLogger(ConnectionPool.class.getName());
	
	/**
	 * Maximum number of times a connection is used.
	 * Apparently they accumulate data.
	 */
	private static final int MAX_USE_COUNT = 100;
	
	// Needs to be a set to ensure there aren't duplicates
	private final TreeSet<ConnectionWrapper> availableConnections = new TreeSet<ConnectionWrapper>();	
	private final Collection connections = new HashSet();
	private final Object monitor = this;
	private final DataSource dataSource;

	private int maxConnections;
	private int timeout;

	public ConnectionPool (DataSource ds, int maxConnections, int timeout) {
		this.dataSource = ds;
		this.maxConnections = maxConnections;
		this.timeout = timeout;
	}

	public void setMaxConnections (int maxConnections) {
		this.maxConnections = maxConnections;
	}

	public void setConnectionTimeout (int timeout) {
		this.timeout = timeout;
	}

	public ConnectionWrapper getConnectionWrapper() throws SQLException {
		TreeSet availConn = this.availableConnections;		
		for (;;) {
			try {
				ConnectionWrapper cw;
				int useCount = 0;
				synchronized (monitor) {
					cw = (ConnectionWrapper) availConn.first();
					if(cw != null) {
						availConn.remove(cw);
						useCount = cw.prepareForUse(Thread.currentThread());
					}
				}
				if(cw == null) {
					this.ensureConnectionsExist();
					continue;
				}
				if (useCount > MAX_USE_COUNT || cw.isClosed() || cw.hasTimedOut()) {
					try {
						// This removes it from connections list.
						cw.destroy();
					} catch (SQLException se) {
						// ignore
					}
					continue;
				}
				cw.touch();
				return cw;
			} catch (NoSuchElementException nse) {
				if(logger.isLoggable(Level.INFO)) {
					logger.info("getConnectionWrapper(): Creating connections.");
				}
				ensureConnectionsExist();
			}
		}
	}

	private void ensureConnectionsExist() throws SQLException {
		for (;;) {
			synchronized (monitor) {
				if (this.connections.size() < this.maxConnections) {
					Connection c = this.dataSource.getConnection();
					ConnectionWrapper cw = new ConnectionWrapper (c);
					this.connections.add (cw);
					this.availableConnections.add (cw);
					break;
				}
				else {
					ConnectionWrapper[] cws = (ConnectionWrapper[]) this.connections.toArray(new ConnectionWrapper[0]);
					boolean closedSomething = false;
					for (int i = 0; i < cws.length; i++) {
						if (cws[i].isClosed()) {
							this.connections.remove (cws[i]);
							closedSomething = true;
							break;
						}
						else if (cws[i].hasTimedOut()) {
							try {
								// This removes it from connections list
								cws[i].destroy();
							} catch (SQLException se) {
								// ignore
							}
							closedSomething = true;
							break;
						}
					}
					if (!closedSomething) {
						throw new SQLException ("Connection pool has been maxed out. Increase maxConnections.");
					}
				}
			}
		}
	}

	class ConnectionWrapper implements Comparable {
		private final Connection connection;
		private volatile long lastTouched;
		private Thread owner;
		private int useCount = 0;
		
		public ConnectionWrapper (Connection c) {
			this.connection = c;
			touch();
		}

		private int prepareForUse(Thread t) {
			// Assumed called within synchronized block.
			if(this.owner != null) {
				throw new IllegalStateException("ConnectionWrapper is already owned by " + this.owner);
			}
			this.owner = t;
			return this.useCount++;
		}
		
		public void touch() {
			this.lastTouched = System.currentTimeMillis();
		}

		public boolean hasTimedOut() {
			return System.currentTimeMillis() - this.lastTouched >= timeout;
		}

		public void release() {
			// This may only be called in the owner thread.
			synchronized (monitor) {
				// Null owner allowed given that release() may be
				// called multiple times.
				if(this.owner == null) {
					// Already released, right?
					return;
				}
				if(Thread.currentThread() != this.owner) {
					throw new IllegalStateException("Connection released in thread " + Thread.currentThread() + "; expecting " + this.owner);
				}
				this.owner = null;
				availableConnections.add (this);
			}
		}

		/**
		 * Releases connection wrapper on behalf of another thread.
		 * This should only be used by a finalizer that needs to
		 * release the connection.
		 * @param ownerThread Thread that owns the connection.
		 */
		public void release(Thread ownerThread) {
			synchronized (monitor) {
				if(this.owner == null) {
					// Already released, right?
					return;
				}
				if(ownerThread != this.owner) {
					throw new IllegalStateException("Connection released on behalf of " + ownerThread + "; expecting " + this.owner);
				}
				if(logger.isLoggable(Level.INFO)) {
					logger.info("release(): Releasing thread pool connection outside expected thread: " + ownerThread);
				}
				this.owner = null;
				availableConnections.add (this);
			}			
		}

		public boolean isClosed() throws SQLException {
			return connection.isClosed();
		}

		public void destroy() throws SQLException {
			try {
				this.connection.close();
			} finally {
				synchronized (monitor) {
					connections.remove(this);
				}				
			}
		}

		public PreparedStatement prepareStatement (String sql) throws SQLException {
			touch();
			return this.connection.prepareStatement (sql);
		}
		
		public PreparedStatement prepareStatement(String sql, int resultSetType,
                int resultSetConcurrency) throws SQLException {
			touch();
			return this.connection.prepareStatement (sql, resultSetType, resultSetConcurrency);
		}				
		
		public Connection getConnection() {
			return this.connection;
		}

		public int compareTo(Object o) {
			//TODO: Change so that the newest connection created
			//is preferred?			
			if(this == o) {
				return 0;
			}
			int ihc1 = System.identityHashCode(this);
			int ihc2 = System.identityHashCode(o);
			int diff = ihc1 - ihc2;
			if(diff == 0) {
				diff = System.identityHashCode(this.connection) - System.identityHashCode(((ConnectionWrapper) o).connection);
				if(diff == 0) {
					// very unlikely
					diff = 1;
				}
			}
			return diff;
		}
	}
}

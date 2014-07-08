package org.julp.db;

import org.julp.AbstractDomainObjectFactory;
import org.julp.DataAccessException;
import org.julp.DataReader;
import org.julp.DataWriter;

public class DomainObjectFactory<T> extends AbstractDomainObjectFactory<T> {

    private static final long serialVersionUID = 6118351907258601974L;
    /**
     * Throw exception if PreparedStatement.executeUpdate() does not return 1 for each DomainObject. It means that the row in DB Table most likely was modified or deleted by another user/process. Does
     * not work with Oracle JDBC Driver batch since driver does not return correct values
     */
    protected boolean throwOptimisticLockDeleteException = true;
    protected boolean throwOptimisticLockUpdateException = true;
    protected boolean throwFailedInsertException = true;
    /**
     * END of Optimistic lock settings ********************************
     */
    /**
     * Target DB catalog
     */
    protected String catalog = null;
    /**
     * If overrideCatalogName == true and catalog from this.metaData is null than use catalog member variable
     */
    protected boolean overrideCatalogName = false;
    /**
     * Target DB schema
     */
    protected String schema = null;
    /**
     * Target DB Table
     */
    protected String table = null;
    /**
     * This is JDBC utility
     */
    protected DBServices dbServices = null;
    /**
     * Do not execute INSERTS/UPDATES/DELETES - just generate SQL and parameters
     */
    protected boolean generateSQLOnly = false;
    /**
     * Some databases (like hsqldb) fail when UPDATE/INSERT statements are using TABLE_NAME.COLUMN_NAME in SET clause.
     */
    protected boolean noFullColumnName = false;

    public DomainObjectFactory() {
    }

    public DBServices getDBServices() {
        return dbServices;
    }

    public void setDBServices(DBServices dbServices) {
        this.dbServices = dbServices;
    }

    @Override
    public DataWriter<T> getDataWriter() {
        if (dataWriter == null) {
            dataWriter = new DBDataWriter<>();
        }
        return dataWriter;
    }

    @Override
    public DataReader<T> getDataReader() {
        if (dataReader == null) {
            dataReader = new DBDataReader<>();
        }
        return dataReader;
    }

    @Override
    public boolean writeData() {
        if (!isValid()) {
            return false;
        }
        if (!beforeWriteData()) {
            return false;
        }
        if (this.dataWriter == null) {
            this.dataWriter = getDataWriter();
        }
        if (dataWriter != null && dataWriter.getMetaData() == null) {
            this.dataWriter.setMetaData(this.metaData);
        }
        if (dataWriter != null && ((DBDataWriter<T>) dataWriter).getDBServices() == null) {
            ((DBDataWriter<T>) dataWriter).setDBServices(this.dbServices);          
        }
        if (dataWriter != null) {
            this.dataWriter.setOptions(options);
        }
        boolean success = this.dataWriter.writeData(this);
        if (!afterWriteData()) {
            return false;
        }
        return success;
    }

    @Override
    public void populateMetaData() {
        try {
            this.metaData = new DBMetaData<>();
            this.metaData.setThrowMissingFieldException(throwMissingFieldException);
            this.metaData.populate(getMapping(), this.domainClass);
            init();
            dataReader.setMetaData(metaData);
            dataWriter.setMetaData(metaData);
        } catch (DataAccessException e) {
            throw new DataAccessException(e);
        }
    }
}

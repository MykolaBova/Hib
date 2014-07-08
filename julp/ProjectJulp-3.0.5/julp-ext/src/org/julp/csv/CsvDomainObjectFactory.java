package org.julp.csv;

import java.io.BufferedWriter;
import org.julp.AbstractDomainObjectFactory;
import org.julp.DataAccessException;
import org.julp.DataReader;
import org.julp.DataWriter;

public class CsvDomainObjectFactory<T> extends AbstractDomainObjectFactory<T> {

    protected BufferedWriter writer;

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
            this.metaData = new CsvMetaData();
            this.metaData.setThrowMissingFieldException(throwMissingFieldException);
            this.metaData.populate(getMapping(), this.domainClass);
            init();
            dataReader.setMetaData(metaData);
            dataWriter.setMetaData(metaData);
        } catch (Exception e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public DataWriter getDataWriter() {
        if (dataWriter == null) {
            dataWriter = new CsvDataWriter();
        }
        return dataWriter;
    }

    @Override
    public DataReader getDataReader() {
        if (dataReader == null) {
            dataReader = new CsvDataReader();
        }
        return dataReader;
    }

    public BufferedWriter getWriter() {
        return writer;
    }

    public void setWriter(BufferedWriter writer) {
        this.writer = writer;
    }
}

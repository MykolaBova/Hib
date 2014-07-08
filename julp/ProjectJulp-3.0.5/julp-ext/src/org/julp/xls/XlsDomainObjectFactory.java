package org.julp.xls;

import jxl.write.WritableSheet;
import org.julp.AbstractDomainObjectFactory;
import org.julp.DataAccessException;
import org.julp.DataReader;
import org.julp.DataWriter;

public class XlsDomainObjectFactory<T> extends AbstractDomainObjectFactory<T> {

    protected WritableSheet sheet;

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
            this.metaData = new XlsMetaData();
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
            dataWriter = new XlsDataWriter();
        }
        return dataWriter;
    }

    @Override
    public DataReader getDataReader() {
        if (dataReader == null) {
            dataReader = new XlsDataReader();
        }
        return dataReader;
    }

    public WritableSheet getSheet() {
        return sheet;
    }

    public void setSheet(WritableSheet sheet) {
        this.sheet = sheet;
    }
}

package org.julp.csv;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.julp.AbstractMetaData;
import org.julp.DataAccessException;

public class CsvMetaData extends AbstractMetaData {

    @Override
    public void populate(Map mapping, Class domainClass) throws DataAccessException {
        if (mapping == null || mapping.isEmpty()) {
            throw new DataAccessException("CSVMetaData: missing/invalid mapping");
        }
        this.mapping = mapping;
        this.domainClass = domainClass;
        this.fieldCount = mapping.size();
        this.readOnly = new boolean[fieldCount];
        this.fieldName = new String[fieldCount];
        this.fieldLabel = new String[fieldCount];
        this.readMethod = new Method[fieldCount];
        this.writeMethod = new Method[fieldCount];
        this.fieldClass = new Class[fieldCount];
        this.fieldClassName = new String[fieldCount];
        this.writable = new boolean[fieldCount];

        Map<Integer, String> sortedMap = new TreeMap<>();
        Iterator<Map.Entry<String, String>> iter0 = mapping.entrySet().iterator();
        while (iter0.hasNext()) {
            Map.Entry<String, String> entry = iter0.next();
            sortedMap.put(Integer.valueOf(entry.getKey()), entry.getValue());
        }

        Iterator<Map.Entry<Integer, String>> iter1 = sortedMap.entrySet().iterator();
        int idx = 1;
        while (iter1.hasNext()) {
            Map.Entry<Integer, String> entry = iter1.next();
            String field = entry.getValue();
            this.setFieldName(idx, field);
            this.setReadOnly(idx, false);
            //this.setWritable(idx, true); //it's done already: see setReadOnly() method
            this.populateReadMethod(idx, field);
            this.populateWriteMethod(idx, field);
            idx++;
        }
    }
}

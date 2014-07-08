package com.tinyorm;

import java.util.ArrayList;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import com.tinyorm.Result;
import com.tinyorm.annotation.DBTable;
import com.tinyorm.annotation.DBField;

public class ResultMeta {

    private Class<? extends Result> resultClass;
    private String tableName;
    private Constructor rowMaker;
    private ArrayList<Field> resultFields;

    public ResultMeta(Class<? extends Result> cl) throws NoSuchMethodException {

        resultClass = cl;
        tableName = ((DBTable)cl.getAnnotation(DBTable.class)).value();

        Constructor c = cl.getDeclaredConstructor();
        if (! c.isAccessible()) c.setAccessible(true);
        rowMaker = c;

        resultFields = new ArrayList<Field>();
        for (Field f: cl.getDeclaredFields()) {
            if (f.isAnnotationPresent(DBField.class)) {
                if (! f.isAccessible()) f.setAccessible(true);
                resultFields.add(f);
            }
        };
    }

    public Class<? extends Result> getResultClass() {
        return this.resultClass;
    }
    public String getTableName() {
        return this.tableName;
    }
    public Constructor getRowMaker() {
        return this.rowMaker;
    }
    public ArrayList<Field> getResultFields() {
        return this.resultFields;
    }

}


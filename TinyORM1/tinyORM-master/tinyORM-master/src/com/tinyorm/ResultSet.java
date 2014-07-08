package com.tinyorm;

import java.util.HashMap;

import com.tinyorm.DBSQlite;
import com.tinyorm.Result;
import com.tinyorm.ResultMeta;

public abstract class ResultSet {
    
    private static final java.sql.Connection dbh = DBSQlite.c;

    private ResultMeta resultMeta;

    protected ResultSet(Class<? extends Result> cl) 
        throws NoSuchMethodException {
        
        resultMeta = Result.getResultMeta(cl);
    }

    public Object getById (int id) throws Exception {
        return Result.resultInstance(this.resultMeta.getResultClass(), new HashMap<String, Object>());
    }
}

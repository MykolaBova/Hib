package com.tinyorm;

import java.util.HashMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import com.tinyorm.ResultMeta;
import com.tinyorm.util.RuntimeCache;

public abstract class Result {
    
    public static Result resultInstance(
        Class<? extends Result> cl, 
        HashMap<String, Object> fields
    ) throws InstantiationException,
        NoSuchMethodException,
        IllegalAccessException, 
        InvocationTargetException {

        ResultMeta rMeta = getResultMeta(cl);

        Result inst = (Result)rMeta.getRowMaker().newInstance();

        for (Field f: rMeta.getResultFields()) {
            f.set(inst, fields.get(f.getName()));
        }

        return inst;
    }

    public static ResultMeta getResultMeta(Class<? extends Result> cl) 
        throws NoSuchMethodException {
        
        ResultMeta initMeta = null;
        ResultMeta meta = (ResultMeta)RuntimeCache.get(cl);
        if (meta == null) {
            initMeta = new ResultMeta(cl);
            RuntimeCache.put(cl, initMeta);
            return initMeta;
        } else {
            return meta;
        }
    }

}


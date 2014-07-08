package com.tinyorm.util;

import java.util.HashMap;

public final class RuntimeCache {

    private static HashMap<Object, Object> cache;
    static { cache = new HashMap<Object, Object>(); }

    public static void put(Object key, Object value) {
        cache.put(key, value);
    }

    public static Object get(Object key) {
        return cache.get(key);
    }

}

package org.julp.util.common;

import java.util.Set;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.io.Serializable;

public class MapValueSorter implements Serializable {

    private static final long serialVersionUID = -1829651326365965097L;
    private MapValueComparator comp = new MapValueComparator();

    public MapValueSorter() {
    }

    public Map sort(Map map) {
        Set set = map.entrySet();
        int len = set.size();
        Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[len]);
        Arrays.sort(entries, comp);
        LinkedHashMap lhm = new LinkedHashMap(len);
        for (int i = 0; i < len; i++) {
            lhm.put(entries[i].getKey(), entries[i].getValue());
        }
        set = null;
        map = null;
        entries = null;

        return lhm;
    }
}

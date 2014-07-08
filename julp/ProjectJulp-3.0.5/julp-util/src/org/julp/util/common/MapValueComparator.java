package org.julp.util.common;

import java.util.Comparator;
import java.util.Map;

// This class is used to sort Map by value (vs key).
public class MapValueComparator implements Comparator {

    private static final long serialVersionUID = 3986526037915738872L;
    protected Map.Entry mapEntry = null;

    public MapValueComparator() {
    }

    public MapValueComparator(Map.Entry mapEntry) {
        this.mapEntry = mapEntry;
    }

    @Override
    public int compare(Object o1, Object o2) {
        Object v1 = ((Map.Entry) o1).getValue();
        Object v2 = ((Map.Entry) o2).getValue();
        return ((Comparable) v1).compareTo(v2);
    }

}

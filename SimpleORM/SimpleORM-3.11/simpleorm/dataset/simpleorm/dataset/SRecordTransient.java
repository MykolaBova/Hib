package simpleorm.dataset;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;


public class SRecordTransient extends SRecordGeneric {

	private static final long serialVersionUID = 1L;
	
	private LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();

	
	@Override
	public boolean isNull(String field) {
		return (containsKey(field) && get(field) == null);
	}
	
	@Override
	public void setNull(String field) {
		put(field, null);
	}

	
	//
	// Implement Map by delegation to private values map
	//
	@Override
	public void clear() {
		values.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return values.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return values.containsValue(value);
	}

	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return values.entrySet();
	}

	@Override
	public Object get(Object key) {
		return values.get(key);
	}

	@Override
	public boolean isEmpty() {
		return values.isEmpty();
	}

	@Override
	public Set<String> keySet() {
		return values.keySet();
	}

	@Override
	public Object put(String key, Object value) {
		return values.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> map) {
		values.putAll(map);
	}

	@Override
	public Object remove(Object key) {
		return values.remove(key);
	}

	@Override
	public int size() {
		return values.size();
	}

	@Override
	public Collection<Object> values() {
		return values.values();
	}
	
}

package workflow.controller;

import java.util.Map;
import java.util.Set;

import ariba.util.core.MapUtil;

public abstract class AbstractContext implements Context {

	private Map<String, Object> values = MapUtil.map();

	
	@Override
	public Object put(String key, Object value) {
		if(value == null)
			return remove(key);
		return values.put(key, value);
	}
	@Override
	public Object get(String key) {
		return this.values().get(key);
	}
	
	@Override
	public Object remove(String key) {
		if(values.containsKey(key))
			return values.remove(key);
		return null;
	}
	
	@Override
	public Map<String, Object> values() {
		return values;
	}

	@Override
	public Set<String> keySet() {
		return values.keySet();
	}	


}

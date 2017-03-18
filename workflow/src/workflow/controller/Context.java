package workflow.controller;

import java.util.Map;
import java.util.Set;

public interface Context {

	public Object put(String key, Object value);
	public Object get(String key);
	public Object remove(String key);
	public Map<String, Object> values();
	public Set<String> keySet();
	public void setModel(Object model);
	public Object getModel();
}

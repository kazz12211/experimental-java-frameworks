package workflow.controller.rule;

import java.util.Map;

import core.util.MapUtils;

public class UserInfoDef {
	
	Map<String, Object> dictionary = MapUtils.map();
	
	public void setDictionary(Map<String, Object> dictionary) {
		this.dictionary = dictionary;
	}
	public Map<String, Object> getDictionary() {
		return dictionary;
	}
	public void add(String key, Object value) {
		this.dictionary.put(key, value);
	}
	public void remove(String key) {
		this.dictionary.remove(key);
	}
	public Object get(String key) {
		return this.dictionary.get(key);
	}
}

package workflow.util;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DictionaryEditor {
	private DictionaryXML original;
	private DictionaryXML edited;
	private boolean isEdited;
	private String filename;
	
	public DictionaryEditor(String filename) {
		this.filename = filename;
		this.load();
		isEdited = false;
	}

	private void load() {
		DictionaryXML xml = new DictionaryXML();
		try {
			xml.initWithPath(filename);
			original = xml;
		} catch (Exception e) {
			e.printStackTrace();
			original = null;
		}
		
		edited = original.copy();
	}
	
	public boolean isEdited() {
		return isEdited;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public void revert() {
		original.copyTo(edited);
		isEdited = false;
	}
		
	public void add(String key, Object value) {
		edited.add(key, value);
		isEdited = true;
	}
	
	public void remove(String key) {
		edited.remove(key);
		isEdited = true;
	}
	
	public int indexOf(String key) {
		return edited.indexOf(key);
	}
	
	public void insertAt(int index, String key, Object value) {
		edited.insertAt(index, key, value);
		isEdited = true;
	}
	
	public String getType(String key) {
		Object value = this.getValue(key);
		if(value instanceof Boolean)
			return DictionaryXML.BOOLEAN_TYPE;
		else if(value instanceof Number)
			return DictionaryXML.NUMBER_TYPE;
		else if(value instanceof Date)
			return DictionaryXML.DATE_TYPE;
		else if(value instanceof String) {
			if(isMultipleLineText(value))
				return "text";
			else
				return DictionaryXML.STRING_TYPE;
		} else if(value instanceof List)
			return DictionaryXML.ARRAY_TYPE;
		else if(value instanceof Map)
			return DictionaryXML.DICT_TYPE;
		else
			return null;
	}
	
	public boolean isMultipleLineText(Object value) {
		if(value instanceof String) {
			String lines[] = ((String)value).split("\n");
			if(lines.length > 1)
				return true;
		}
		return false;
	}
	
	public void save() throws Exception {
		edited.saveToPath(filename);
		this.load();
	}

	public List<String> getKeys() {
		return edited.keys();
	}

	public void setValue(String key, Object value) {
		edited.set(key, value);
	}

	public Object getValue(String key) {
		return edited.get(key);
	}

	public int keyCount() {
		return getKeys().size();
	}

	public void moveUp(String key) {
		edited.moveUp(key);
	}

	public void moveDown(String key) {
		edited.moveDown(key);
	}
	
	
}

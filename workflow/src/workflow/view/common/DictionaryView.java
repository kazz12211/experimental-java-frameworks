package workflow.view.common;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.util.core.ListUtil;

public class DictionaryView extends AWComponent {

	public String currentKey;
	public Object currentItem;
	
	public Map<String, Object> dictionary() {
		return (Map<String, Object>) this.valueForBinding("dictionary");
	}
	
	public List<String> sortedKeys() {
		Map<String, Object> dict = this.dictionary();
		List<String> keys = ListUtil.list(); 
		if(dict != null) {
			keys.addAll(dict.keySet());
			Collections.sort(keys);
		}
		return keys;
	}
	
	private boolean showTitle() {
		return this.booleanValueForBinding("showTitle");
	}
	
	public String title() {
		String title = (String) this.valueForBinding("title");
		if(title == null)
			title = AWLocal.localizedJavaString(1, "Dictionary", DictionaryView.class, requestContext());
		return (this.showTitle() ? title : null);
	}
	
	public Object value() {
		return this.dictionary().get(currentKey);
	}
	
	public String type() {
		Object value = this.value();
		if(value instanceof Map)
			return "dict";
		else if(value instanceof List)
			return "array";
		else if(value instanceof String)
			return "string";
		else if(value instanceof Number)
			return "number";
		else if(value instanceof Date)
			return "date";
		else if(value instanceof Boolean)
			return "boolean";
		else
			return "unknown";
	}
	
	@Override
	public boolean isStateless() { return false; }
}

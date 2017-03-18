package workflow.view.common;

import java.util.Date;
import java.util.List;
import java.util.Map;

import ariba.ui.aribaweb.core.AWComponent;

public class ArrayView extends AWComponent {

	public Object currentItem;
	
	public List<Object> array() {
		return (List<Object>) this.valueForBinding("array");
	}
	
	public String type() {
		if(currentItem instanceof Map)
			return "dict";
		else if(currentItem instanceof List)
			return "array";
		else if(currentItem instanceof String)
			return "string";
		else if(currentItem instanceof Number)
			return "number";
		else if(currentItem instanceof Date)
			return "date";
		else if(currentItem instanceof Boolean)
			return "boolean";
		else
			return "unknown";
	}

	
	@Override
	public boolean isStateless() { return false; }
	
}

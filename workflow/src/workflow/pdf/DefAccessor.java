package workflow.pdf;

import org.w3c.dom.Element;

import ariba.ui.widgets.XMLUtil;

public class DefAccessor {
	Element root;
	public DefAccessor(Element element) {
		this.root = element;
	}
	
	public Element getFirstElement(Element parent, String tagName) {
		Element elements[] = XMLUtil.getAllChildren(parent, tagName);
		if(elements != null && elements.length > 0)
			return elements[0];
		return null;
	}
	public String getStringValue(Element parent, String tagName, String defaultValue) {
		Element element = getFirstElement(parent, tagName);
		if(element == null)
			return defaultValue;
		return XMLUtil.getText(element, defaultValue);
	}

	public String getString(String tagName, String defaultValue) {
		return this.getStringValue(root, tagName, defaultValue);
	}
	
	public String[] getStrings(String tagName, String defaultValue) {
		return getStrings(tagName, defaultValue, ",");
	}
	
	public String[] getStrings(String tagName, String defaultValue, String separator) {
		String value = getStringValue(root, tagName, defaultValue);
		return value.split(separator);
	}
	
	public Element[] elementsNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		return elements;
	}
	
	public Element elementNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		if(elements != null && elements.length >= 1)
			return elements[0];
		return null;
	}



}

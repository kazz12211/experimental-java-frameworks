package workflow.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import core.util.ListUtils;
import core.util.MapUtils;
import core.util.NumberUtils;
import ariba.ui.widgets.XMLUtil;

public class PropertyList {

	private Document document = null;
	private Element docRoot = null;
	private List<String> keys;
	private Map<String, Object> keyValues;
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:SS");

	public PropertyList() {
		keys = ListUtils.list();
		keyValues = MapUtils.map();
	}

	public void initWithPath(String path) throws Exception {
		File file = new File(path);
		this.initWithFile(file);
	}
	public void initWithFile(File file) throws Exception {
		if(file.exists() == false) {
			file.createNewFile();
			String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<dict>\n</dict>\n";
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.close();
		}
		
		FileInputStream fis = new FileInputStream(file);
		this.initWithInputStream(fis);
		fis.close();
	}
	
	public void initWithInputStream(InputStream inputStream) throws Exception {
		InputSource input = new InputSource(inputStream);
		document = XMLUtil.document(input, false, true, null);
		Element docElement = document.getDocumentElement();
		if(docElement.getNodeName().equals("dict")) {
			docRoot = docElement;
			this.parse();
		}
	}
	
	private void parse() throws Exception {
		keyValues = this.parseDictionary(docRoot);
	}

	private Map<String, Object> parseDictionary(Element parent) throws Exception {
		Element elements[] = XMLUtil.getAllChildren(parent, null);
		Map<String, Object> map = MapUtils.map();
		int size = elements.length;
		boolean isRoot = parent == docRoot;
		for(int i = 0; i < size; ) {
			Element keyElement = elements[i];
			Element valueElement = elements[i+1];
			if(keyElement.getNodeName().equals("key") && isValidValueElement(valueElement)) {
				String key = XMLUtil.getText(keyElement, null);
				Object value = objectValue(valueElement);
				if(key != null && value != null) {
					if(isRoot)
						keys.add(key);
					map.put(key, value);
				}
			}
			i += 2;
		}
		return map;
	}
	
	public static final String DICT_TYPE = "dict";
	public static final String ARRAY_TYPE = "array";
	public static final String STRING_TYPE = "string";
	public static final String NUMBER_TYPE = "number";
	public static final String DATE_TYPE = "date";
	public static final String BOOLEAN_TYPE = "boolean";

	private Object objectValue(Element element) throws Exception {
		String type = element.getNodeName();
		if(DICT_TYPE.equals(type)) {
			return parseDictionary(element);
		} else if(ARRAY_TYPE.equals(type)) {
			return parseArray(element);
		} else if(STRING_TYPE.equals(type)) {
			return XMLUtil.getText(element, null);
		} else if(NUMBER_TYPE.equals(type)) {
			return parseNumber(element);
		} else if(DATE_TYPE.equals(type)) {
			return parseDate(element);
		} else if(BOOLEAN_TYPE.equals(type)) {
			return parseBoolean(element);
		}
		return null;
	}

	private List<Object> parseArray(Element element) throws Exception {
		Element elements[] = XMLUtil.getAllChildren(element, null);
		List<Object> list = ListUtils.list();
		int size = elements.length;
		for(int i = 0; i < size; i++) {
			Object value = this.objectValue(elements[i]);
			if(value != null) {
				list.add(value);
			}
		}
		return list;
	}
	
	private Number parseNumber(Element element) {
		String value = XMLUtil.getText(element, null);
		if(value == null)
			return null;
		return NumberUtils.toDouble(value);
	}
	
	private Date parseDate(Element element) throws ParseException {
		String value = XMLUtil.getText(element, null);
		if(value == null) {
			return null;
		}
		Date date = DATE_FORMAT.parse(value);
		return date;
	}
	
	private Boolean parseBoolean(Element element) {
		String value = XMLUtil.getText(element, null);
		if(value == null) {
			return null;
		}
		if("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value))
			return new Boolean(true);
		else if("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value))
			return new Boolean(false);
		return null;
	}

	private boolean isValidValueElement(Element element) {
		String type = element.getNodeName();
		if(DICT_TYPE.equals(type) || 
				ARRAY_TYPE.equals(type) || 
				STRING_TYPE.equals(type) || 
				NUMBER_TYPE.equals(type) || 
				DATE_TYPE.equals(type) || 
				BOOLEAN_TYPE.equals(type))
			return true;
		return false;
	}

	public Element documentRoot() {
		return docRoot;
	}
	
	public List<String> keys() {
		return keys;
	}
	
	public Map<String, Object> keyValues() {
		return keyValues;
	}
	
	public Object get(String key) {
		return keyValues.get(key);
	}

	public void set(String key, Object value) {
		keyValues.put(key, value);
	}

	public void copyTo(PropertyList edited) {
		edited.removeAll();
		for(String key : this.keys) {
			edited.add(key, get(key));
		}
	}
	
	private void removeAll() {
		keys.clear();
		keyValues.clear();
	}

	public PropertyList copy() {
		PropertyList props = new PropertyList();
		this.copyTo(props);
		return props;
	}
	
	public void add(String key, Object object) {
		if(!keys.contains(key))
			keys.add(key);
		keyValues.put(key, object);
	}
	
	public void remove(String key) {
		int index = keys.indexOf(key);
		if(index >= 0 && index < keys.size()) {
			keys.remove(index);
			keyValues.remove(key);
		}
	}
	public int indexOf(String key) {
		return keys.indexOf(key);
	}

	public void insertAt(int index, String key, Object value) {
		if(index >= 0 && index < keys.size()) {
			keys.add(index, key);
			keyValues.put(key, value);
		}
	}
	
	public void saveToPath(String path) throws Exception {
		File file = new File(path);
		this.saveToFile(file);
	}
	
	public void saveToFile(File file) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement("dict");
		doc.appendChild(root);
		
		this.createDictionaryElement(doc, root, keys, keyValues);
		
		FileOutputStream fos = new FileOutputStream(file);
		
		XMLUtil.serializeDocument(doc, fos);
		
		this.document = doc;
		this.docRoot = root;
		
	}

	private Element createDictionaryElement(Document doc, Element parent, List<String> keys, Map<?, ?> dict) {
		for(String key : keys) {
			Element keyElement = doc.createElement("key");
			Element valueElement = createValueElement(doc, dict.get(key));
			if(valueElement != null) {
				keyElement.setTextContent(key);
				parent.appendChild(keyElement);
				parent.appendChild(valueElement);
			}
		}
		return parent;
	}
	private Element createValueElement(Document doc, Object value) {
		if(value instanceof Boolean) {
			Element element = doc.createElement(BOOLEAN_TYPE);
			String str = ((Boolean)value).booleanValue() == true ? "true" : "false";
			element.setTextContent(str);
			return element;
		} else if(value instanceof Number) {
			Element element = doc.createElement(NUMBER_TYPE);
			Double val = new Double(((Number)value).doubleValue());
			String str = val.toString();
			element.setTextContent(str);
			return element;
		} else if(value instanceof Date) {
			Element element = doc.createElement(DATE_TYPE);
			String str = DATE_FORMAT.format((Date)value);
			element.setTextContent(str);
			return element;
		} else if(value instanceof String) {
			Element element = doc.createElement(STRING_TYPE);
			element.setTextContent((String)value);
			return element;
		} else if(value instanceof List) {
			Element element = doc.createElement(ARRAY_TYPE);
			for(Iterator<?> iter = ((List<?>)value).iterator(); iter.hasNext(); ) {
				Element child = createValueElement(doc, iter.next());
				element.appendChild(child);
			}
			return element;
		} else if(value instanceof Map) {
			Element element = doc.createElement(DICT_TYPE);
			List<String> keyList = ListUtils.list();
			Map<?,?> map = (Map<?,?>)value;
			for(Object key : map.keySet()) {
				keyList.add((String)key);
			}
			this.createDictionaryElement(doc, element, keyList, map);
			return element;
		}
		return null;
	}

	public void moveDown(String key) {
		int index = keys.indexOf(key);
		if(index == keys.size() - 1)
			return;
		keys.remove(key);
		keys.add(index+1, key);
	}
	
	public void moveUp(String key) {
		int index = keys.indexOf(key);
		if(index == 0)
			return;
		keys.remove(key);
		keys.add(index-1, key);
	}

}

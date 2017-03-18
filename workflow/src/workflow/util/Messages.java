package workflow.util;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.util.core.MapUtil;

public class Messages {

	private static Messages _sharedInstance = null;
	private Map<String, PropertyList> propertiesMap;
	
	private Messages() {}
	
	public static Messages sharedInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new Messages();
			_sharedInstance.init();
		}
		return _sharedInstance;
	}

	private void init() {
		propertiesMap = MapUtil.map();
		PropertyList props;
		try {
			props = Messages.loadProperties("Messages.plist");
			propertiesMap.put("en", props);
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			props = Messages.loadProperties("Messages_ja.plist");
			propertiesMap.put("ja", props);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static PropertyList loadProperties(String name) throws Exception {
		AWResource resource = AWServletApplication.sharedInstance().resourceManager().resourceNamed(name);
		URL path = new URL(resource.fullUrl());
		InputStream is = path.openStream();
		PropertyList props = new PropertyList();
		props.initWithInputStream(is);
		is.close();
		return props;
	}
	
	public static String getMessage(String key, String defaultStr, Locale locale) {
		String lang = locale.getLanguage();
		return getMessage(key, defaultStr, lang);
	}
	
	public static String getMessage(String key, String defaultStr, String lang) {
		PropertyList props = null;
		if("ja".equals(lang)) {
			props = sharedInstance().propertiesMap.get("ja");
		} else {
			props = sharedInstance().propertiesMap.get("en");
		}
		String s = (String) props.get(key);
		if(s == null) {
			System.err.println("The value for key '" + key + "' is missing.");
			s = defaultStr;
		}
		return s;
	}
}

package workflow.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Map;
import java.util.Set;

import ariba.util.core.MapUtil;
import ariba.util.core.SystemUtil;
import ariba.util.log.Log;

public class AppConfigManager {

	public static String RESOURCE_PATH;
	public static String CONFIG_PATH;
	private static String CONFIG_NAME;
	private static String PREFIX;
	private static String SUFFIX = ".config";
	private Map<String, Object> appPlist;
	private static AppConfigManager _sharedInstance = null;
	
	public static final String EXCLUSIVEMODE				= "exclusivemode";
	public static final String EXCLUSIVEUSERNAMES			= "exclusiveusernames";
	public static final String TESTMODE						= "testmode";
	public static final String RULE_TRACE					= "rule.trace";
	public static final String WORKFLOW_NOTIFICATION 		= "workflow.notification";
	
	static {
		if(SystemUtil.isWin32()) {
			RESOURCE_PATH="C:\\workflow\\opt\\local\\";
			CONFIG_PATH = RESOURCE_PATH + "workflow" + "\\";
		} else {
			RESOURCE_PATH = "/opt/local/";
			CONFIG_PATH = RESOURCE_PATH + "workflow" + "/";
		}
		CONFIG_NAME = CONFIG_PATH + "app.config"; 
		PREFIX = CONFIG_PATH + "user_";
	};

	public static AppConfigManager getInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new AppConfigManager();
			_sharedInstance.loadAppConfig();
		}
		return _sharedInstance;
	}
	protected AppConfigManager() {
	}
	
	private String loadFile(String filename) throws Exception {
		StringBuffer buffer = new StringBuffer();
		BufferedReader br = null;
		try {
			File f = new File(filename);
			if(f.exists() == false) {
				f.createNewFile();
				FileWriter wr = new FileWriter(f);
				wr.write("{}");
				wr.close();
				return new String("{}");
			}
			FileReader reader = new FileReader(f);
			br = new BufferedReader(reader);
			String line = null;
			while((line = br.readLine()) != null) {
				buffer.append(line);
			}
		} finally {
			if(br != null)
				br.close();
		}
		
		return buffer.toString();
	}
	
	private Map<String, Object> loadPlist(String filename) throws Exception {
		Map<String, Object> plist = MapUtil.map();
		String content = this.loadFile(filename);
		MapUtil.fromSerializedString(plist, content);
		return plist;
	}
	
	private void savePlist(Map<String, Object> plist, String filename) throws Exception {
		if(plist != null) {
			FileWriter writer = null;
			try {
				String content = MapUtil.toSerializedString(plist);
				writer = new FileWriter(filename);
				writer.write(content);
				writer.flush();
			} finally {
				if(writer != null)
					writer.close();
			}
		}
	}
	
	private void loadAppConfig() {
		try {
			appPlist = this.loadPlist(CONFIG_NAME);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to load plist from '" + CONFIG_NAME + "'", e);
		}
	}
	
	private Map<String, Object> loadUserConfig(String uid) {
		String filename = PREFIX + uid + SUFFIX;
		Map<String, Object> plist = MapUtil.map();
		try {
			plist = this.loadPlist(filename);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to load plist from '" + filename + "'", e);
		}
		return plist;
	}
	
	public Object get(String key) {
		return appPlist.get(key);
	}
	public void set(String key, Object value) {
		appPlist.put(key, value);
		try {
			this.savePlist(appPlist, CONFIG_NAME);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to save plist to '" + CONFIG_NAME + "'", e);
		}
	}
	
	public boolean getBoolean(String key, boolean defaultWhenNull) {
		Object value = this.get(key);
		if(value == null)
			return defaultWhenNull;
		if(value instanceof String) {
			return "true".equalsIgnoreCase((String)value);
		} else {
			return Boolean.TRUE.equals(value);
		}
	}
	
	public Set<String> allKeys() {
		return appPlist.keySet();
	}
	
	public Object get(String uid, String key) {
		Map<String, Object> plist = this.loadUserConfig(uid);
		return plist.get(key);
	}
	public void set(String uid, String key, Object value) {
		Map<String, Object> plist = this.loadUserConfig(uid);
		plist.put(key, value);
		try {
			this.savePlist(plist, PREFIX + uid + SUFFIX);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to save plist to '" + PREFIX + uid + SUFFIX + "'", e);
		}
	}
	
	public Set<String> allKeys(String uid) {
		Map<String, Object> plist = this.loadUserConfig(uid);
		return plist.keySet();
	}
	
	public void clear() {
		appPlist.clear();
		try {
			this.savePlist(appPlist, CONFIG_NAME);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to save cleared plist to '" + CONFIG_NAME + "'", e);
		}
	}
	
	public void clear(String uid) {
		Map<String, Object> plist = this.loadUserConfig(uid);
		plist.clear();
		try {
			this.savePlist(plist, PREFIX + uid + SUFFIX);
		} catch (Exception e) {
			Log.customer.error("AppConfigManager: failed to save cleared plist to '" + PREFIX + uid + SUFFIX + "'", e);
		}
	}
}

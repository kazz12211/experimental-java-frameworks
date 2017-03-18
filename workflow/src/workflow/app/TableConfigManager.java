package workflow.app;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import ariba.util.core.MapUtil;
import ariba.util.log.Log;

public class TableConfigManager {
	
	private static final String PREFIX = AppConfigManager.CONFIG_PATH + "tableconfig" + "/";
	private static final String SUFFIX = ".tableconfig";
	private Map<String, Object> plist;
	private String uid;
	
	public TableConfigManager(String uid) {
		this.uid = uid;
	}
	
	private String filename() {
		return PREFIX + uid + SUFFIX;
	}
	
	private String loadFile() throws IOException {
		StringBuffer buffer = new StringBuffer();
		BufferedReader br = null;
		try {
			File f = new File(filename());
			if(f.exists() == false) {
				f.createNewFile();
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
	private void loadPlist() throws Exception {
		if(plist == null) {
			plist = MapUtil.map();
			String content = loadFile();
			MapUtil.fromSerializedString(plist, content);
		}
	}
	
	private void savePlist() throws IOException {
		if(plist != null) {
			FileWriter writer = null;
			try {
				String content = MapUtil.toSerializedString(plist);
				writer = new FileWriter(filename());
				writer.write(content);
				writer.flush();
			} finally {
				if(writer != null)
					writer.close();
			}
			writer.close();
		}
	}
	
	public boolean loaded() {
		return plist != null;
	}
	
	public void load() {
		try {
			this.loadPlist();
		} catch (Exception e) {
			Log.customer.error("TableConfigManager: failed to load plist from '" + filename() + "'", e);
		}
	}
	
	public Map<String, Object> pop(String tableName) {
		if(plist != null) {
			Map<String, Object> tableConfig = (Map<String, Object>) plist.get(tableName);
			if(tableConfig == null) {
				tableConfig = MapUtil.map();
				plist.put(tableName, tableConfig);
			}
			return tableConfig;
		}
		return MapUtil.map();
	}
	
	public void push(Map<String, Object> tableConfig, String tableName) {
		plist.put(tableName, tableConfig);
	}
	
	public void save() {
		if(plist != null) {
			try {
				this.savePlist();
			} catch (Exception e) {
				Log.customer.error("TableConfigManager: failed to save plist to '" + filename() + "'", e);
			}
		}
	}
}

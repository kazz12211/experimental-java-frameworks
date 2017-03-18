package workflow.dashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;

import core.util.ListUtils;
import core.util.MapUtils;
import core.util.StringUtils;
import core.util.XMLConfig;
import ariba.ui.widgets.XMLUtil;
import ariba.util.core.MapUtil;
import ariba.util.log.Log;

public class DashboardManager {

	private static final String PREFIX = "/opt/local/workflow/dashboard/";
	private static final String SUFFIX = ".dashboard";
	private Map<String, Object> plist;
	private String uid;
	private DashboardXML dashboard;

	public DashboardManager(String uid) {
		this.uid = uid;
		this.dashboard = new DashboardXML();
		try {
			this.dashboard.init("dashboard.xml");
		} catch (Exception e) {
			Log.customer.error("DashboardManager : no valid dashboard.xml", e);
		}
	}
	
	public List<DashboardItem> getAllDashboardItems() {
		return dashboard.dashboardItems();
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
			plist = MapUtils.map();
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
			Log.customer.error("DashboardManager: failed to load plist from '" + filename() + "'", e);
		}
	}

	public List<DashboardItem> getDashboardItems() {
		if(plist != null) {
			List<DashboardItem> dashboardItems = ListUtils.list();
			List<Map<String, Object>> items = (List<Map<String, Object>>) plist.get("dashboardItems");
			if(items != null) {
				for(Map<String, Object> item : items) {
					DashboardItem dashboardItem = new DashboardItem();
					dashboardItem.initWithMap(item);
					dashboardItems.add(dashboardItem);
				}
			}
			return dashboardItems;
		}
		return ListUtils.list();
	}

	public void setDashboardItems(List<DashboardItem> dashboardItems) {
		List<Map<String, Object>> items = ListUtils.list();
		for(DashboardItem dashboardItem : dashboardItems) {
			items.add(dashboardItem.map());
		}
		plist.put("dashboardItems", items);
	}
	
	public void save() {
		if(plist != null) {
			try {
				this.savePlist();
			} catch (Exception e) {
				Log.customer.error("DashboardManager: failed to save plist to '" + filename() + "'", e);
			}
		}
	}
	
	class DashboardXML extends XMLConfig {
		
		List<DashboardItem> dashboardItems;
		
		public List<DashboardItem> dashboardItems() {
			return dashboardItems;
		}
		
		public void init(String filename) throws Exception {
			String path = super.getResourcePath(filename);
			URL url = new URL(path);
			Element docElem = XMLUtil.document(url, false, false, null).getDocumentElement();
			if(docElem.getNodeName().equals("dashboard")) {
				dashboardItems = ListUtils.list();
				Element[] elements = this.elementsNamed(docElem, "dashboardItem");
				for(Element element : elements) {
					parseDashboardItem(element);
				}
				Collections.sort(dashboardItems, new Comparator<DashboardItem>() {
					@Override
					public int compare(DashboardItem arg0, DashboardItem arg1) {
						return arg0.name.compareToIgnoreCase(arg1.name);
					}});
			}
		}

		private void parseDashboardItem(Element element) {
			String componentName = null;
			String name = null;
			String description = null;
			
			Element e = this.elementNamed(element, "componentName");
			if(e != null)
				componentName = XMLUtil.getText(e, null);
			e = this.elementNamed(element, "name");
			if(e != null)
				name = XMLUtil.getText(e, null);
			e = this.elementNamed(element, "description");
			if(e != null)
				description = XMLUtil.getText(e, null);
			
			if(StringUtils.nullOrEmpty(componentName) || StringUtils.nullOrEmpty(name))
				return;
			
			DashboardItem item = new DashboardItem();
			item.setComponentName(componentName);
			item.setName(name);
			item.setDescription(description);
			
			Element rolesElem = this.elementNamed(element, "roles");
			if(rolesElem != null) {
				Element[] roleElems = this.elementsNamed(rolesElem, "role");
				for(Element roleElem : roleElems) {
					String type = roleElem.getAttribute("type");
					String value = roleElem.getAttribute("value");
					if(StringUtils.nullOrEmpty(type) == false && StringUtils.nullOrEmpty(value) == false) {
						item.addRole(type, value);
					}
				}
			}
			
			Element categoryElem = this.elementNamed(element, "category");
			if(categoryElem != null) {
				Element nameElem = this.elementNamed(categoryElem, "name");
				Element descElem = this.elementNamed(categoryElem, "description");
				if(nameElem != null && XMLUtil.getText(nameElem, null) != null) {
					item.setCategory(XMLUtil.getText(nameElem, null), XMLUtil.getText(descElem, null));
				}
			}
			
			dashboardItems.add(item);
		}
	}

	public boolean selected(DashboardItem item) {
		return this.indexOfItem(item) >= 0;
	}

	public void remove(DashboardItem item) {
		if(plist != null) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) plist.get("dashboardItems");
			if(items != null) {
				int index = indexOfItem(item);
				if(index >= 0) {
					items.remove(index);
					this.save();
				}
			}
		}
	}

	public int indexOfItem(DashboardItem item) {
		if(plist != null) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) plist.get("dashboardItems");
			if(items == null)
				return -1;
			for(int i = 0; i < items.size(); i++) {
				if(items.get(i).get("componentName").equals(item.getComponentName()))
					return i;
			}
		}
		return -1;
	}

	public void add(DashboardItem item) {
		if(plist != null) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) plist.get("dashboardItems");
			if(items == null) {
				items = ListUtils.list();
				plist.put("dashboardItems", items);
			}
			items.add(item.map());
			this.save();
		}
	}

	public void remove(int index) {
		if(plist != null) {
			List<Map<String, Object>> items = (List<Map<String, Object>>) plist.get("dashboardItems");
			items.remove(index);
		}
	}

	public void moveFirst(DashboardItem item) {
		int index = indexOfItem(item);
		if(index > 0) {
			List<DashboardItem> items = this.getDashboardItems();
			items.remove(index);
			items.add(0, item);
			this.setDashboardItems(items);
		}
	}

	public void moveLeft(DashboardItem item) {
		int index = indexOfItem(item);
		if(index > 0) {
			List<DashboardItem> items = this.getDashboardItems();
			items.remove(index);
			items.add(index-1, item);
			this.setDashboardItems(items);
		}
	}

	public void moveRight(DashboardItem item) {
		int index = indexOfItem(item);
		List<DashboardItem> items = this.getDashboardItems();
		if(index < items.size() - 1) {
			items.remove(index);
			items.add(index+1, item);
			this.setDashboardItems(items);
		}
	}

	public void moveLast(DashboardItem item) {
		int index = indexOfItem(item);
		List<DashboardItem> items = this.getDashboardItems();
		if(index < items.size() - 1) {
			int size = items.size();
			items.remove(index);
			items.add(size-1, item);
			this.setDashboardItems(items);
		}
	}

}

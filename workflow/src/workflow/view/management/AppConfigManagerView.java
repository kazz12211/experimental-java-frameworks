package workflow.view.management;

import java.util.List;
import java.util.Set;

import core.util.ListUtils;
import workflow.app.AppConfigManager;
import workflow.model.User;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTDisplayGroup;

public class AppConfigManagerView extends AWComponent {
	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<UserSelectionData> users;
	public UserSelectionData selectedUser;
	public List<ConfigKeyValue> configKeyValues = ListUtils.list();
	public ConfigKeyValue currentItem;
	
	@Override
	public void init() {
		super.init();
		users = ListUtils.list();
		users.add(new UserSelectionData("global", "Global"));
		List<User> list = User.allEmployees();
		for(User user : list) {
			users.add(new UserSelectionData(user.getUniqueName(), user.getName()));
		}
	}
	
	public AWResponseGenerating selectUser() {
		configKeyValues.clear();
		
		if(selectedUser == null)
			return null;
		
		return this.reload();
	}
	
	public AWResponseGenerating reload() {
		if(selectedUser.userId.equals("global")) {
			Set<String> keys = AppConfigManager.getInstance().allKeys();
			for(String key : keys) {
				configKeyValues.add(new ConfigKeyValue(key, AppConfigManager.getInstance().get(key).toString()));
			}
		} else {
			Set<String> keys = AppConfigManager.getInstance().allKeys(selectedUser.userId);
			for(String key : keys) {
				configKeyValues.add(new ConfigKeyValue(key, AppConfigManager.getInstance().get(selectedUser.userId, key).toString()));
			}
		}
		return null;
	}
	
	public AWResponseGenerating save() {
		if(selectedUser.userId.equals("global")) {
			AppConfigManager.getInstance().clear();
			for(ConfigKeyValue kv : configKeyValues) {
				if(kv.key != null) {
					AppConfigManager.getInstance().set(kv.key, kv.value);
				}
			}
		} else {
			AppConfigManager.getInstance().clear(selectedUser.userId);
			for(ConfigKeyValue kv : configKeyValues) {
				if(kv.key != null) {
					AppConfigManager.getInstance().set(selectedUser.userId, kv.key, kv.value);
				}
			}
		}
		return null;
	}
	
	public AWResponseGenerating add() {
		configKeyValues.add(new ConfigKeyValue(null, null));
		return null;
	}
	
	public AWResponseGenerating remove() {
		configKeyValues.remove(displayGroup.selectedObject());
		return null;
	}
	
	@Override
	public boolean isStateless() { return false; }

	public class UserSelectionData {
		public String userId;
		public String userName;
		public UserSelectionData(String id, String name) {
			this.userId = id; this.userName = name;
		}
	}
	
	public class ConfigKeyValue {
		public String key;
		public String value;
		public ConfigKeyValue(String key, String value) {
			this.key = key;
			this.value = value;
		}
	}
}

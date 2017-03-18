package workflow.dashboard;

import java.util.List;
import java.util.Map;

import workflow.model.User;
import ariba.util.core.ListUtil;
import ariba.util.core.MapUtil;
import ariba.util.fieldvalue.FieldValue;

public class DashboardItem {
	String componentName;
	String name;
	String description;
	List<DashboardItemRole> roles;
	DashboardCategory category;
		
	public void initWithMap(Map<String, Object> map) {
		this.componentName = (String) map.get("componentName");
		this.name = (String) map.get("name");
		this.description = (String) map.get("description");
		roles = ListUtil.list();
		List<Map> list = (List<Map>) map.get("roles");
		for(Map m : list) {
			DashboardItemRole r = new DashboardItemRole();
			r.initWithMap(m);
			roles.add(r);
		}
		Map<String, Object> categoryMap = (Map<String, Object>) map.get("category");
		if(categoryMap != null) {
			category = new DashboardCategory();
			category.initWithMap(categoryMap);
		}
	}
	
	public Map<String, Object> map() {
		Map<String, Object> map = MapUtil.map();
		map.put("componentName", componentName);
		map.put("name", name);
		map.put("description", description);
		List<Map<String, Object>> list = ListUtil.list();
		for(DashboardItemRole role : roles) {
			list.add(role.map());
		}
		map.put("roles", list);
		if(category != null)
			map.put("category", category.map());
		return map;
	}
	
	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<DashboardItemRole> getRoles() {
		return roles;
	}

	public void setRoles(List<DashboardItemRole> roles) {
		this.roles = roles;
	}
	
	public void addRole(String type, String value) {
		DashboardItemRole role = new DashboardItemRole(type, value);
		if(this.roles == null)
			this.roles = ListUtil.list();
		this.roles.add(role);
	}
	
	public void setCategory(String name, String descr) {
		DashboardCategory cat = new DashboardCategory();
		cat.setName(name);
		cat.setDescription(descr);
		this.category = cat;
	}

	public DashboardCategory getCategory() {
		return category;
	}
	
	public boolean validate(User user) {
		if(ListUtil.nullOrEmptyList(roles))
			return false;
		for(DashboardItemRole role : roles) {
			if(role.validate(user))
				return true;
		}
		return false;
	}
	
	public String toString() {
		return this.map().toString();
	}

	public class DashboardItemRole {
		String type;		// role, user, property
		String value;
		
		public DashboardItemRole(String type, String value) {
			this.type = type;
			this.value = value;
		}
		
		public DashboardItemRole() {
		}

		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public boolean validate(User user) {
			Validator validator = null;
			if("user".equalsIgnoreCase(type))
				validator = UserValidator.instance();
			else if("role".equalsIgnoreCase(type))
				validator = RoleValidator.instance();
			else if("property".equalsIgnoreCase(type))
				validator = PropertyValidator.instance();
			if(validator != null)
				return validator.validate(user, value);
			return false;
		}
		
		public void initWithMap(Map<String, Object> map) {
			type = (String) map.get("type");
			value = (String) map.get("value");
		}
		public Map<String, Object> map() {
			Map<String, Object> map = MapUtil.map();
			map.put("type", type);
			map.put("value", value);
			return map;
		}
		public String toString() {
			return this.map().toString();
		}
	}
	
	interface Validator {
		boolean validate(User user, String value);
	}
	
	static class RoleValidator implements Validator {

		static RoleValidator instance = null;
		
		public static Validator instance() {
			if(instance == null)
				instance = new RoleValidator();
			return instance;
		}

		@Override
		public boolean validate(User user, String value) {
			//System.out.println("RoleValidator validating '" + user.getUniqueName() + "' and '" + value +"'");
			return user.hasRole(value);
		}
		
	}
	
	static class UserValidator implements Validator {

		static UserValidator instance = null;
		
		public static Validator instance() {
			if(instance == null)
				instance = new UserValidator();
			return instance;
		}

		@Override
		public boolean validate(User user, String value) {
			//System.out.println("UserValidator validating '" + user.getUniqueName() + "' and '" + value +"'");
			return value.equals(user.getUniqueName());
		}
		
	}
	
	static class PropertyValidator implements Validator {
		
		static PropertyValidator instance = null;
		
		public static Validator instance() {
			if(instance == null)
				instance = new PropertyValidator();
			return instance;
		}
		
		@Override
		public boolean validate(User user, String value) {
			//System.out.println("PropertyValidator validating '" + user.getUniqueName() + "' and '" + value +"'");
			Object obj = FieldValue.getFieldValue(user, value);
			if(obj == null)
				return false;
			if(obj instanceof Boolean)
				return ((Boolean)obj).booleanValue();
			if(obj instanceof Number)
				return ((Number)obj).intValue() != 0;
			return false;
		}
	}
	
	public class DashboardCategory {
		String name;
		String description;
		
		public DashboardCategory() {
			
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public void initWithMap(Map<String, Object> map) {
			this.name = (String) map.get("name");
			this.description = (String) map.get("description");
		}
		public Map<String, Object> map() {
			Map<String, Object> map = MapUtil.map();
			map.put("name", name);
			map.put("description", description);
			return map;
		}
	}

}

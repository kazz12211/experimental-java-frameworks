package workflow.controller.rule;

import workflow.model.User;
import ariba.util.fieldvalue.FieldValue;

public class CreatorRoleDef extends RoleDef {
	// type are; role, user, property
	CreatorValidation validator;
	
	public CreatorRoleDef(String name, String type) {
		super(name, type);
		if("role".equals(type))
			validator = new RoleValidator(name);
		else if("user".equals(type))
			validator = new UserValidator(name);
		else if("property".equals(type))
			validator = new PropertyValidator(name);
	}
	
	public CreatorValidation getValidator() {
		return validator;
	}
	
	public boolean isCreator(User user) {
		return this.validator.validate(user);
	}
	
	public interface CreatorValidation {
		
		public abstract boolean validate(User user);
	}
	public class RoleValidator implements CreatorValidation {
		String roleName;
		
		public RoleValidator(String roleName) {
			this.roleName = roleName;
		}

		@Override
		public boolean validate(User user) {
			return user.hasRole(roleName);
		}
		
	}
	public class UserValidator implements CreatorValidation {
		String userName;
		
		public UserValidator(String userName) {
			this.userName = userName;
		}

		@Override
		public boolean validate(User user) {
			return userName.equals(user.getUniqueName());
		}
		
	}
	public class PropertyValidator implements CreatorValidation {

		String key;
		
		public PropertyValidator(String key) {
			this.key = key;
		}

		@Override
		public boolean validate(User user) {
			Object value = FieldValue.getFieldValue(user, key);
			if(value == null)
				return false;
			if(value instanceof Boolean)
				return ((Boolean)value).booleanValue();
			if(value instanceof Number)
				return ((Number)value).intValue() != 0;
			return false;
		}
		
	}

}

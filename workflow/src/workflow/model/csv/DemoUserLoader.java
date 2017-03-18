package workflow.model.csv;

import java.util.List;
import java.util.Map;

import core.util.MapUtils;
import workflow.model.Role;
import workflow.model.User;
import ariba.ui.meta.persistence.ObjectContext;

public class DemoUserLoader extends Loader {

	public DemoUserLoader(ObjectContext oc) {
		super(oc);
	}

	@Override
	public void consumeLineOfTokens(String path, int rowIndex,
			List<String> record) throws Exception {
		if(record.size() < 4)
			return;
		String name = record.get(0);
		String uniqueName = record.get(1);
		String email = record.get(2);
		String password = record.get(3);
		String roleName = "Guest";
		if(record.size() >= 5)
			roleName = record.get(4);
		
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put("ldapUID", uniqueName);
		User user = oc.findOne(User.class, fieldValues);
		if(user == null) {
			user = oc.create(User.class);
			user.setLdapUID(uniqueName);
		}
		user.setName(name);
		user.setEmail(email);
		user.setPassword(password);
		user.setIsEmployee(new Boolean(true));
		
		Role r = Role.getRole(roleName);
		if(r != null) {
			r.addUser(user);
		}
		
	}

	@Override
	protected String getResourceName() {
		return "DemoUser.csv";
	}

}

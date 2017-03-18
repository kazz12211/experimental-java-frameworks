package workflow.model.csv;

import java.util.List;
import java.util.Map;

import workflow.model.Role;
import core.util.MapUtils;
import ariba.ui.meta.persistence.ObjectContext;

public class RoleLoader extends Loader {

	public RoleLoader(ObjectContext oc) {
		super(oc);
	}

	@Override
	public void consumeLineOfTokens(String path, int rowIndex, List<String> record)
			throws Exception {
		if(record.size() < 3)
			return;
		
		String uid = record.get(0);
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put("uid", uid);
		Role role = oc.findOne(Role.class, fieldValues);
		if(role == null) {
			role = oc.create(Role.class);
			role.setUid(uid);
		}
		role.setName(record.get(1));
		
		String boolStr = record.get(2);
		boolean isDefault = false;
		if(boolStr != null && !boolStr.isEmpty() && boolStr.equalsIgnoreCase("true"))
			isDefault = true;
		role.setIsDefault(new Boolean(isDefault));
		if(record.size() >= 4 && record.get(3) != null)
			role.setDescription(record.get(3));
	}

	@Override
	protected String getResourceName() {
		return "Role.csv";
	}

}

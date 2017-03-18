package workflow.roleassign.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.ListUtils;
import workflow.controller.AbstractActivityContent;
import workflow.model.Role;
import workflow.roleassign.model.RoleAssignmentAction;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTDisplayGroup;
import ariba.ui.wizard.core.Wizard;
import ariba.util.fieldvalue.FieldValue;

public class RoleAssignmentActionContent extends AbstractActivityContent {

	public AWTDisplayGroup sourceDisplayGroup = new AWTDisplayGroup();
	public List<Role> roles;
	public AWTDisplayGroup destDisplayGroup = new AWTDisplayGroup();
	
	@Override
	public void init() {
		super.init();
		roles = Role.listAll();
	}
	
	public List<Role> getFilteredRoles() {
		Collections.sort(roles, new Comparator<Role>() {
			@Override
			public int compare(Role o1, Role o2) {
				return o1.getUniqueName().compareToIgnoreCase(o2.getUniqueName());
			}});
		List<Role> filteredRoles = ListUtils.list();
		RoleAssignmentAction activity = (RoleAssignmentAction) this.getActivity();
		if(activity != null) {
			for(Role role : roles) {
				if(activity.getAssignedRoles().contains(role))
					continue;
				filteredRoles.add(role);
			}
		}
		
		return filteredRoles;
	}
	
	public AWResponseGenerating addRoles() {
		List<Role> roles = sourceDisplayGroup.selectedObjects();
		if(!ListUtils.nullOrEmpty(roles))
			((RoleAssignmentAction) this.getActivity()).addRoles(roles);
		return null;
	}
	
	public AWResponseGenerating removeRoles() {
		List<Role> roles = destDisplayGroup.selectedObjects();
		if(!ListUtils.nullOrEmpty(roles))
			((RoleAssignmentAction) this.getActivity()).removeRoles(roles);
		return null;
	}


}

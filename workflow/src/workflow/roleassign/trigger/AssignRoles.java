package workflow.roleassign.trigger;

import java.util.List;

import workflow.controller.trigger.Trigger;
import workflow.model.Role;
import workflow.model.User;
import workflow.roleassign.model.RoleAssignmentAction;
import workflow.roleassign.model.RoleAssignmentRequest;
import ariba.ui.meta.persistence.ObjectContext;

public class AssignRoles implements Trigger {

	@Override
	public void fire(Object model) {
		RoleAssignmentRequest workflow = (RoleAssignmentRequest) model;
		RoleAssignmentAction action = (RoleAssignmentAction) workflow.getActivityOfType(RoleAssignmentAction.class.getName());
		User requester = workflow.getRequester();
		if(requester == null)
			requester = workflow.getCreator();
		if(action != null && requester != null) {
			List<Role> roles = requester.getRoles();
			for(Role r : roles) {
				r.removeUser(requester);
			}
			for(Role r : action.getAssignedRoles()) {
				r.addUser(requester);
			}
		}
		ObjectContext.get().save();
	}

}

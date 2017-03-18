package workflow.roleassign.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import core.util.ListUtils;
import workflow.model.Role;
import workflow.model.Workflow;

@Entity
public class RoleAssignmentRequest extends Workflow {

	@OneToMany
	List<Role> requestedRoles;
	
	public List<Role> getRequestedRoles() {
		if(requestedRoles == null)
			requestedRoles = ListUtils.list();
		return requestedRoles;
	}
	
	public void setRequestedRoles(List<Role> requestedRoles) {
		this.requestedRoles = requestedRoles;
	}

	public void addRoles(List<Role> roles) {
		for(Role role : roles) {
			if(this.getRequestedRoles().contains(role) == false)
				this.getRequestedRoles().add(role);
		}
	}
	
	public void removeRoles(List<Role> roles) {
		if(ListUtils.nullOrEmpty(requestedRoles))
			return;
		requestedRoles.removeAll(roles);
	}

}

package workflow.roleassign.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.OneToMany;

import core.util.ListUtils;
import workflow.model.Activity;
import workflow.model.Role;

@Entity
public class RoleAssignmentAction extends Activity {

	@OneToMany
	List<Role> assignedRoles;
	
	public List<Role> getAssignedRoles() {
		if(assignedRoles ==  null)
			assignedRoles = ListUtils.list();
		return assignedRoles;
	}
	
	public void setAssignedRoles(List<Role> assignedRoles) {
		this.assignedRoles = assignedRoles;
	}

	public void addRoles(List<Role> roles) {
		this.getAssignedRoles().addAll(roles);
	}

	public void removeRoles(List<Role> roles) {
		if(ListUtils.nullOrEmpty(assignedRoles))
			return;
		assignedRoles.removeAll(roles);
	}

}

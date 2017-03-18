package workflow.roleassign.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.ListUtils;
import workflow.controller.ActivityFrameContent;
import workflow.controller.ActivityWizard;
import workflow.controller.WorkflowWizard;
import workflow.model.Role;
import workflow.roleassign.model.RoleAssignmentRequest;
import ariba.ui.table.AWTDisplayGroup;
import ariba.ui.wizard.core.Wizard;

public class RoleAssignmentMainView extends ActivityFrameContent {

	public AWTDisplayGroup sourceDisplayGroup = new AWTDisplayGroup();
	public List<Role> roles;
	public AWTDisplayGroup destDisplayGroup = new AWTDisplayGroup();

	@Override
	public void init() {
		super.init();
		roles = Role.listAll();
	}
	
	public RoleAssignmentRequest getWorkflow() {
		Wizard wizard = this.getWizard();
		RoleAssignmentRequest workflow = null;
		if(this.isInActivityWizard()) {
			workflow = (RoleAssignmentRequest) ((ActivityWizard) wizard).getActivityContext().getWorkflow();
		} else if(this.isInWorkflowWizard()) {
			workflow = (RoleAssignmentRequest) ((WorkflowWizard) wizard).getWorkflowContext().getWorkflow();
		}
		return workflow;
	}
	
	public List<Role> getFilteredRoles() {
		Collections.sort(roles, new Comparator<Role>() {
			@Override
			public int compare(Role o1, Role o2) {
				return o1.getUniqueName().compareToIgnoreCase(o2.getUniqueName());
			}});
		List<Role> filteredRoles = ListUtils.list();
		RoleAssignmentRequest workflow = this.getWorkflow();
		if(workflow != null) {
			for(Role role : roles) {
				if(workflow.getRequestedRoles().contains(role))
					continue;
				filteredRoles.add(role);
			}
		}
		
		return filteredRoles;
	}


}

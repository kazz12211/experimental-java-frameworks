package workflow.roleassign.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.ListUtils;
import workflow.controller.WorkflowFrameContent;
import workflow.controller.ActivityWizard;
import workflow.controller.WorkflowWizard;
import workflow.model.Role;
import workflow.model.User;
import workflow.roleassign.model.RoleAssignmentRequest;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTDisplayGroup;
import ariba.ui.wizard.core.Wizard;

public class RoleAssignmentMainEntry extends WorkflowFrameContent {

	public AWTDisplayGroup sourceDisplayGroup = new AWTDisplayGroup();
	public List<Role> roles;
	public AWTDisplayGroup destDisplayGroup = new AWTDisplayGroup();
	
	@Override
	public void init() {
		super.init();
		this.setValidator(new RoleAssignmentRequestUIValidator());
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
	
	public AWResponseGenerating addRoles() {
		List<Role> roles = sourceDisplayGroup.selectedObjects();
		if(!ListUtils.nullOrEmpty(roles))
			this.getWorkflow().addRoles(roles);
		return null;
	}
	
	public AWResponseGenerating removeRoles() {
		List<Role> roles = destDisplayGroup.selectedObjects();
		if(!ListUtils.nullOrEmpty(roles))
			this.getWorkflow().removeRoles(roles);
		return null;
	}
	
	public void requesterChanged() {
		RoleAssignmentRequest workflow = this.getWorkflow();
		User requester = workflow.getRequester();
		if(requester != null) {
			workflow.getRequestedRoles().clear();
			workflow.addRoles(requester.getRoles());
		}
	}
}

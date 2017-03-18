package workflow.roleassign.controller;

import workflow.controller.WorkflowFrameContent;
import workflow.model.ApprovalAction;
import workflow.roleassign.model.RoleAssignmentAction;
import ariba.ui.aribaweb.core.AWLocal;

public class RoleAssignmentActivities extends WorkflowFrameContent {

	@Override
	public void init() {
		super.init();
		this.setValidator(new RoleAssignmentRequestUIValidator());
	}
	
	public String[] labels() {
		String[] labels = new String[] {
				AWLocal.localizedJavaString(1000, "RoleAssignmentAction", RoleAssignmentActivities.class, requestContext()),
				AWLocal.localizedJavaString(1001, "Approval", RoleAssignmentActivities.class, requestContext())
			};
		return labels;
	}
	
	public String[] models() {
		String[] models = new String[] {
				RoleAssignmentAction.class.getName(),
				ApprovalAction.class.getName()
		};
		return models;
	}
	
	public String[] components() {
		String[] components = new String[] {
				"RoleAssignmentActionContent",
				"ApprovalContent"
		};
		return components;
	}
}

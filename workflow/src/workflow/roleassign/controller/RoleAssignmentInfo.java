package workflow.roleassign.controller;

import workflow.controller.WorkflowFrameContent;


public class RoleAssignmentInfo extends WorkflowFrameContent {

	@Override
	public void init() {
		super.init();
		this.setValidator(new RoleAssignmentRequestUIValidator());
	}
}

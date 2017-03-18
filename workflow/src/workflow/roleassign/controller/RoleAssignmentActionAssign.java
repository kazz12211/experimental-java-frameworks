package workflow.roleassign.controller;

import workflow.controller.ActivityFrameContent;


public class RoleAssignmentActionAssign extends ActivityFrameContent {

	@Override
	public void init() {
		super.init();
		this.setValidator(new RoleAssignmentActionUIValidator());
	}

	
}

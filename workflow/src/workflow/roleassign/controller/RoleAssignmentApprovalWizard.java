package workflow.roleassign.controller;

import workflow.WorkflowException;
import workflow.controller.ActivityWizard;
import workflow.model.Request;
import workflow.model.User;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.wizard.core.WizardStep;

public class RoleAssignmentApprovalWizard extends ActivityWizard {

	public RoleAssignmentApprovalWizard(User actor, String mode,
			Request request, AWRequestContext requestContext,
			AWResourceManager resourceManager) {
		super("workflow/roleassign/controller/RoleAssignmentApprovalWizard", actor, mode, request, requestContext, resourceManager);
	}

	@Override
	protected void localize(AWRequestContext requestContext) {
		this.setLabel(AWLocal.localizedJavaString(1001, "Role Approval", RoleAssignmentApprovalWizard.class, requestContext));
		WizardStep step = this.getStepWithName("view");
		step.setLabel(AWLocal.localizedJavaString(1002, "View", RoleAssignmentApprovalWizard.class, requestContext));
		step = this.getStepWithName("activities");
		step.setLabel(AWLocal.localizedJavaString(1003, "Activities", RoleAssignmentApprovalWizard.class, requestContext));
		step = this.getStepWithName("approve");
		step.setLabel(AWLocal.localizedJavaString(1004, "Approve", RoleAssignmentApprovalWizard.class, requestContext));
	}
	
	@Override
	protected void prepareForReject() throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void prepareForReturn() throws WorkflowException {
		// TODO Auto-generated method stub

	}


}

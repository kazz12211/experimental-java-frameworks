package workflow.roleassign.controller;

import java.util.List;

import workflow.WorkflowException;
import workflow.controller.ActivityWizard;
import workflow.model.Request;
import workflow.model.Role;
import workflow.model.User;
import workflow.roleassign.model.RoleAssignmentAction;
import workflow.roleassign.model.RoleAssignmentRequest;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.wizard.core.WizardStep;

public class RoleAssignmentActionWizard extends ActivityWizard {

	public RoleAssignmentActionWizard(User actor, String mode,
			Request request, AWRequestContext requestContext,
			AWResourceManager resourceManager) {
		super("workflow/roleassign/controller/RoleAssignmentActionWizard", actor, mode, request, requestContext, resourceManager);
	}

	@Override
	protected void localize(AWRequestContext requestContext) {
		this.setLabel(AWLocal.localizedJavaString(1001, "Assign Role", RoleAssignmentActionWizard.class, requestContext));
		WizardStep step = this.getStepWithName("view");
		step.setLabel(AWLocal.localizedJavaString(1002, "View", RoleAssignmentActionWizard.class, requestContext));
		step = this.getStepWithName("activities");
		step.setLabel(AWLocal.localizedJavaString(1003, "Activities", RoleAssignmentActionWizard.class, requestContext));
		step = this.getStepWithName("assign");
		step.setLabel(AWLocal.localizedJavaString(1004, "Assign", RoleAssignmentActionWizard.class, requestContext));
	}

	@Override
	protected void prepareForReject() throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void prepareForReturn() throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initForNew(AWRequestContext requestContext) {
		super.initForNew(requestContext);
		RoleAssignmentRequest workflow = (RoleAssignmentRequest) this.getActivityContext().getWorkflow();
		List<Role> roles = workflow.getRequestedRoles();
		RoleAssignmentAction action = (RoleAssignmentAction) this.getActivityContext().getActivity();
		action.addRoles(roles);
	}


}

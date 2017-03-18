package workflow.roleassign.controller;

import workflow.WorkflowException;
import workflow.controller.WorkflowWizard;
import workflow.controller.rule.WorkflowDef;
import workflow.model.User;
import workflow.model.Workflow;
import workflow.roleassign.model.RoleAssignmentRequest;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWRequestContext;
import ariba.ui.aribaweb.util.AWResourceManager;
import ariba.ui.wizard.core.WizardStep;

public class RoleAssignmentWizard extends WorkflowWizard {

	public RoleAssignmentWizard(String mode, Workflow model, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super("workflow/roleassign/controller/RoleAssignmentWizard", mode, model, requestContext, resourceManager);
	}
	
	public RoleAssignmentWizard(User creator, User requester, String mode, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super("workflow/roleassign/controller/RoleAssignmentWizard", creator, requester, mode, requestContext, resourceManager);
	}
	
	public RoleAssignmentWizard(User creator, User requester, Workflow workflow, String title, String mode, AWRequestContext requestContext, AWResourceManager resourceManager) {
		super("workflow/roleassign/controller/RoleAssignmentWizard", creator, requester, workflow, title, mode, requestContext, resourceManager);
	}
	
	@Override
	protected String getWorkflowName() {
		WorkflowDef def = workflowManager.rules.workflowDefForModel(this.workflowClass());
		if(def != null) {
			return def.getName();
		}
		return "RoleAssignmentRequest";
	}

	@Override
	protected void prepareForSave() throws WorkflowException {
		// TODO Auto-generated method stub

	}

	@Override
	protected Class<? extends Workflow> workflowClass() {
		return RoleAssignmentRequest.class;
	}

	@Override
	protected void localize(AWRequestContext requestContext) {
		this.setLabel(AWLocal.localizedJavaString(1001, "Role Assignment Request", RoleAssignmentWizard.class, requestContext));
		WizardStep step;
		step = this.getStepWithName("main");
		step.setLabel(AWLocal.localizedJavaString(1002, "Main", RoleAssignmentWizard.class, requestContext));
		step = this.getStepWithName("activities");
		step.setLabel(AWLocal.localizedJavaString(1003, "Activities", RoleAssignmentWizard.class, requestContext));
		step = this.getStepWithName("info");
		step.setLabel(AWLocal.localizedJavaString(1004, "Info", RoleAssignmentWizard.class, requestContext));
	}

	@Override
	protected void initUIForNew(AWRequestContext requestContext) {
		super.initUIForNew(requestContext);
		
		this.removeStep(this.getStepWithName("activities"));
		this.removeStep(this.getStepWithName("info"));
	}

	@Override
	protected void initForNew(AWRequestContext requestContext) {
		RoleAssignmentRequest workflow = (RoleAssignmentRequest) this.getWorkflow();
		if(workflow != null && workflow.getCreator() != null) {
			workflow.addRoles(workflow.getCreator().getRoles());
		}
	}


	@Override
	protected void initForEdit(AWRequestContext requestContext) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void initForInspect(AWRequestContext requestContext) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void prepareForSubmit() throws WorkflowException {
		// TODO Auto-generated method stub

	}

}

package workflow.view;

import java.util.List;

import workflow.app.DataTableComponent;
import workflow.aribaweb.component.ConfirmationPanel;
import workflow.controller.WorkflowManager;
import workflow.model.User;
import workflow.model.Workflow;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.ui.table.AWTDisplayGroup;
import ariba.util.core.Fmt;
import ariba.util.fieldvalue.FieldValue;

public class WorkflowListTrashView extends DataTableComponent {

	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public WorkflowManager workflowManager;

	@Override
	public void init() {
		super.init();
		workflowManager = (WorkflowManager) FieldValue.getFieldValue(session(), "workflowManager");
	}
	
	public List<Workflow> filteredObjects() {
		User user = (User) session().getFieldValue("user");
		return user.deletedWorkflows();
	}

	public AWResponseGenerating unremoveSelection() {
		String format = AWLocal.localizedJavaString(1, "Do you really want to unremove these %s workflows?", WorkflowListTrashView.class, requestContext());
		String message = Fmt.S(format, displayGroup.selectedObjects().size());
		return ConfirmationPanel.run(this, message, "doUnremoveSelection", null);
	}
	
	public void doUnremoveSelection() {
		User user = (User) FieldValue.getFieldValue(session(), "user");
		for(Object o : displayGroup.selectedObjects()) {
			this.workflowManager.unremove(user, (Workflow) o);
		}
		ObjectContext.get().save();
		for(Object o : displayGroup.selectedObjects()) {
			this.workflowManager.undeleted((Workflow) o);
		}
	}

	@Override
	public boolean isStateless() { return false; }

}

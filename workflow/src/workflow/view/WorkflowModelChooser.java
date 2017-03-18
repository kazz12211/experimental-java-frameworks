package workflow.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import workflow.controller.WorkflowManager;
import workflow.controller.rule.WorkflowDef;
import ariba.ui.aribaweb.core.AWComponent;

public class WorkflowModelChooser extends AWComponent {

	private List<WorkflowDef> models = null;
	
	public List<WorkflowDef> getModels() {
		if(models == null) {
			models = this.getWorkflowManager().rules.allWorkflowModels();
			Collections.sort(models, new Comparator<WorkflowDef>() {

				@Override
				public int compare(WorkflowDef arg0, WorkflowDef arg1) {
					return arg0.getName().compareTo(arg1.getName());
				}});
		}
		return models;
	}
	private WorkflowManager getWorkflowManager() {
		return (WorkflowManager) session().getFieldValue("workflowManager");
	}
	
	public boolean isStateless() { return false; }
}

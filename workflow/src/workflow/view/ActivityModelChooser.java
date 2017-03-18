package workflow.view;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import workflow.controller.WorkflowManager;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.WorkflowDef;
import core.util.ListUtils;
import ariba.ui.aribaweb.core.AWComponent;

public class ActivityModelChooser extends AWComponent {

	private List<WorkflowAndActivityDef> models = null;
	
	public List<WorkflowAndActivityDef> getModels() {
		if(models == null) {
			models = ListUtils.list();
			List<WorkflowDef> workflowDefs = this.getWorkflowManager().rules.allWorkflowModels();
			for(WorkflowDef workflowDef : workflowDefs) {
				List<ActivityDef> activityDefs = this.getWorkflowManager().rules.availableActivityModels(workflowDef);
				for(ActivityDef activityDef : activityDefs) {
					models.add(new WorkflowAndActivityDef(workflowDef, activityDef));
				}
			}
			Collections.sort(models, new Comparator<WorkflowAndActivityDef>() {

				@Override
				public int compare(WorkflowAndActivityDef o1,
						WorkflowAndActivityDef o2) {
					int comp = o1.workflowDef.getName().compareTo(o2.workflowDef.getName());
					if(comp != 0)
						return comp;
					return o1.activityDef.getName().compareTo(o2.activityDef.getName());
				}});
		}
		return models;
	}
	
	private WorkflowManager getWorkflowManager() {
		return (WorkflowManager) session().getFieldValue("workflowManager");
	}

	@Override
	public boolean isStateless() { return false; }
	
	public class WorkflowAndActivityDef {
		public WorkflowDef workflowDef;
		public ActivityDef activityDef;
		public WorkflowAndActivityDef(WorkflowDef workflowDef, ActivityDef activityDef) {
			this.workflowDef = workflowDef;
			this.activityDef = activityDef;
		}
		public String getName() {
			return workflowDef.getName() + " " + activityDef.getName();
		}
	}
}

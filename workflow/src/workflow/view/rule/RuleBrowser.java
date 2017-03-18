package workflow.view.rule;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;
import workflow.controller.WorkflowManager;
import workflow.controller.rule.ActivityDef;
import workflow.controller.rule.ActorRoleDef;
import workflow.controller.rule.CreatorRoleDef;
import workflow.controller.rule.WorkflowDef;
import workflow.controller.rule.flow.ActivityRef;
import workflow.controller.rule.flow.PathDef;
import workflow.controller.rule.flow.TransitionDef;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWLocal;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTSortOrdering;

public class RuleBrowser extends AWComponent {
	private WorkflowManager manager;
	
	public List<WorkflowDef> workflowDefs;
	public WorkflowDef workflowDef;
	public ActivityDef activityDef;
	public ActivityRef activityRef;
	public TransitionDef transitionDef;
	public PathDef pathDef;
	public boolean showDetails;
	public boolean layoutChangeLatch;

	public void init() {
		super.init();
		manager = (WorkflowManager) session().getFieldValue("workflowManager");
		workflowDefs = manager.rules.allWorkflowModels();
	}
	
	
	public AWResponseGenerating reload() {
		this.manager.reloadRule();
		return null;
	}
	
	public AWResponseGenerating layoutChanged() {
		layoutChangeLatch = true;
		return null;
	}

	public Map<?,?> getWorkflowDefTableConfig() {
		Map<String, Object> tableConfig = (Map<String, Object>) session().dict().get("RuleBrowserWorkflowDefTableConfig");
		if(tableConfig == null) {
			tableConfig = MapUtils.map();
			session().dict().put("RuleBrowserWorkflowDefTableConfig", tableConfig);
		}
		tableConfig.put("sortOrderings", ListUtils.list(AWTSortOrdering.sortOrderingWithKey("name", AWTSortOrdering.CompareAscending).serialize()));
		if(this.showDetails)
			tableConfig.put("hiddenColumns", ListUtils.list());
		else
			tableConfig.put("hiddenColumns", ListUtils.list("modelName", "controllerName"));
		return tableConfig;
	}
	
	public void setWorkflowDefTableConfig(Map<?,?> tableConf) {
		session().dict().put("RuleBrowserWorkflowDefTableConfig", tableConf);
	}
	
	public Map<?,?> getActivityDefTableConfig() {
		Map<String, Object> tableConfig = (Map<String, Object>) session().dict().get("RuleBrowserActivityDefTableConfig");
		if(tableConfig == null) {
			tableConfig = MapUtils.map();
			session().dict().put("RuleBrowserWorkflowDefTableConfig", tableConfig);
		}
		tableConfig.put("sortOrderings", ListUtils.list(AWTSortOrdering.sortOrderingWithKey("id", AWTSortOrdering.CompareAscending).serialize()));
		if(this.showDetails)
			tableConfig.put("hiddenColumns", ListUtils.list());
		else
			tableConfig.put("hiddenColumns", ListUtils.list("modelName", "controllerName"));
		return tableConfig;
	}
	
	public void setActivityDefTableConfig(Map<?,?> tableConf) {
		session().dict().put("RuleBrowserActivityDefTableConfig", tableConf);
	}

	public String getSourceActivity() {
		String sourceId = transitionDef.getSourceId();
		ActivityDef ad = manager.rules.lookupActivityDef(workflowDef, sourceId);
		return ad.getName();
	}
	
	public String getDestinationActivity() {
		String destId = pathDef.getDestinationId();
		if(destId != null) {
			ActivityDef ad = manager.rules.lookupActivityDef(workflowDef, destId);
			return ad.getName();
		} else {
			return null;
		}
	}

	public String getActivityName() {
		ActivityDef def = manager.rules.lookupActivityDef(workflowDef, activityRef.getId());
		return def.getName();
	}
	
	public String userInfoTitle() {
		return AWLocal.localizedJavaString(1, "User Info", RuleBrowser.class, requestContext());
	}
	
	public String getCreatorRoles() {
		List<CreatorRoleDef> defs = workflowDef.getCreatorRoles();
		StringBuffer string = new StringBuffer();
		for(CreatorRoleDef def : defs) {
			if(string.length() > 0)
				string.append(", ");
			string.append(def.getNameAndType());
		}
		return string.toString();
	}
	
	public String getActorRoles() {
		List<ActorRoleDef> defs = activityDef.getActorRoles();
		StringBuffer string = new StringBuffer();
		for(ActorRoleDef def : defs) {
			if(string.length() > 0)
				string.append(", ");
			string.append(def.getNameAndType());
		}
		return string.toString();
	}
	@Override
	public boolean isStateless() { return false; }
	
}

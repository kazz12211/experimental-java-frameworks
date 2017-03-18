package workflow.controller.rule;

import java.util.List;

import workflow.controller.rule.flow.RuleDef;
import workflow.model.User;
import core.util.ListUtils;

public class WorkflowDef extends ModelDef {
	List<CreatorRoleDef> creatorRoles;
	List<ActivityDef> activities = ListUtils.list();
	RuleDef rule;
	
	public WorkflowDef(String name, String modelName, String controllerName) {
		super(name, modelName, controllerName);
	}
	
	public void setCreatorRoles(List<CreatorRoleDef> creatorRoles) {
		this.creatorRoles = creatorRoles;
	}
	public List<CreatorRoleDef> getCreatorRoles() {
		return creatorRoles;
	}
	public void addCreatorRole(CreatorRoleDef creatorRole) {
		if(creatorRoles == null)
			creatorRoles = ListUtils.list();
		creatorRoles.add(creatorRole);
	}
	
	public List<ActivityDef> getActivities() {
		return activities;
	}
	public void addActivityDef(ActivityDef def) {
		this.activities.add(def);
	}

	public RuleDef getRule() {
		return rule;
	}
	public void setRule(RuleDef rule) {
		this.rule = rule;
	}

	public ActivityDef lookupActivity(String activityId) {
		for(ActivityDef ad : this.getActivities()) {
			if(ad.getId().equals(activityId))
				return ad;
		}
		return null;
	}

	public ActivityDef getActivityForModel(String modelName) {
		for(ActivityDef def : this.getActivities()) {
			if(def.getModelName().equals(modelName))
				return def;
		}
		return null;
	}

	public ActivityDef lookupActivityForActorRole(String roleName) {
		for(ActivityDef ad : this.getActivities()) {
			if(ad.hasActorRole(roleName))
				return ad;
		}
		return null;
	}

	public ActivityDef lookupActivityOfClass(String activityClassName) {
		for(ActivityDef ad : this.getActivities()) {
			if(ad.getModelName().equals(activityClassName))
				return ad;
		}
		return null;
	}


	public boolean isValid() {
		return (ListUtils.nullOrEmpty(creatorRoles) == false);
	}

	public boolean isCreator(User user) {
		if(!isValid())
			return false;
		for(CreatorRoleDef role : creatorRoles) {
			if(role.isCreator(user))
				return true;
		}
		return false;
	}


}

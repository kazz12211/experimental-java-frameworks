package workflow.controller.rule;

import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;
import workflow.controller.rule.trigger.TriggerDef;

public abstract class ModelDef {
	String name;
	String modelName;
	String controllerName;
	Map<String, List<TriggerDef>> triggers = MapUtils.map();
	UserInfoDef userInfo;
	
	public ModelDef(String name, String modelName, String controllerName) {
		this.name = name;
		this.modelName = modelName;
		this.controllerName = controllerName;
	}
	
	public String getName() {
		return name;
	}
	public String getModelName() {
		return modelName;
	}
	public String getControllerName() {
		return controllerName;
	}

	public Map<String, List<TriggerDef>> getTriggers() {
		return triggers;
	}
	
	public void addTrigger(TriggerDef trigger) {
		List<TriggerDef> list = triggers.get(trigger.getStage());
		if(list == null) {
			list = ListUtils.list();
			triggers.put(trigger.getStage(), list);
		}
		list.add(trigger);
	}
	
	public List<TriggerDef> getTriggersForStage(String stage) {
		return triggers.get(stage);
	}

	public List<TriggerDef> allTriggerDefs() {
		List<TriggerDef> defs = ListUtils.list();
		for(List<TriggerDef> list :  triggers.values()) {
			defs.addAll(list);
		}
		return defs;
	}
	
	public boolean hasTrigger() {
		return (this.triggers.size() > 0);
	}
	
	public Map<String, Object> userInfoDictionary() {
		if(userInfo != null)
			return userInfo.getDictionary();
		return null;
	}
	
	public void setUserInfo(UserInfoDef userInfo) {
		this.userInfo = userInfo;
	}
}

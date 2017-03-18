package workflow.controller.rule.trigger;

public class TriggerDef {

	String stage;
	String triggerClass;
	String id;
	
	public TriggerDef(String stage, String triggerClass, String id) {
		this.stage = stage;
		this.triggerClass = triggerClass;
		this.id = id;
	}
	
	public String getStage() {
		return stage;
	}
	public String getTriggerClass() {
		return triggerClass;
	}
	public String getId() {
		return id;
	}
	
	public String toString() {
		return "Trigger {id=" + id + ", stage=" + stage + ", " + triggerClass + "}";
	}
}

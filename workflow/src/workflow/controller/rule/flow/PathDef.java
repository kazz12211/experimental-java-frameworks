package workflow.controller.rule.flow;

import workflow.controller.rule.cond.Condition;


public class PathDef {

	Condition condition;
	String destinationId;
	String exitStatus;
	
	public Condition getCondition() {
		return condition;
	}
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	public String getDestinationId() {
		return destinationId;
	}
	public void setDestinationId(String destinationId) {
		this.destinationId = destinationId;
	}
	public String getExitStatus() {
		return exitStatus;
	}
	public void setExitStatus(String exitStatus) {
		this.exitStatus = exitStatus;
	}
	public boolean evaluate(Object model) {
		if(condition == null)
			return true;
		return condition.evaluate(model);
	}
	
	public boolean isExit() {
		return exitStatus != null;
	}
	
	public String toString() {
		StringBuffer string = new StringBuffer();
		string.append("PathDef{");
		string.append("condition=" + (condition != null ? condition.toString() : "null") + ";");
		string.append("destinationId=" + destinationId + ";");
		string.append("exitStatus=" + exitStatus);
		string.append("}");
		return string.toString();
	}
}

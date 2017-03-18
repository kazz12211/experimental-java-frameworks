package workflow.controller.rule.flow;

import workflow.controller.rule.cond.Condition;

public class ActivityRef {
	String id;
	Condition condition;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public Condition getCondition() {
		return condition;
	}
	public void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public boolean matches(Object model) {
		if(condition == null)
			return true;
		return condition.evaluate(model);
	}
	
	public String toString() {
		return "ActivityRef {id:" + id + ", condition:" + condition + "}";
	}
}

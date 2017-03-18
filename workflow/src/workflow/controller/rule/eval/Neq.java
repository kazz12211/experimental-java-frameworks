package workflow.controller.rule.eval;

import workflow.controller.Trace;
import workflow.controller.rule.WorkflowRule;

public class Neq extends CompareValues {

	@Override
	public boolean evaluate(Object value, String param) {
		Object obj = this.convertParamForTarget(value, param);
		if(WorkflowRule.debugMode)
			Trace.writeLog("Neq: evaluating value " + value + " (converted value " + obj + ")");
		boolean result = !(value.equals(obj));
		if(WorkflowRule.debugMode)
			Trace.writeLog("lt: evaluation result = " + result);
		return result;
	}

}

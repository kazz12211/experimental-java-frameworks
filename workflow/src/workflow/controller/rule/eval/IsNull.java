package workflow.controller.rule.eval;

import workflow.controller.Trace;
import workflow.controller.rule.WorkflowRule;

public class IsNull implements Eval {

	@Override
	public boolean evaluate(Object value, String param) {
		if(WorkflowRule.debugMode)
			Trace.writeLog("IsNull: evaluating value " + value);
		boolean result =  value == null;
		if(WorkflowRule.debugMode)
			Trace.writeLog("IsNull: evaluation result = " + result);
		return result;
	}

}

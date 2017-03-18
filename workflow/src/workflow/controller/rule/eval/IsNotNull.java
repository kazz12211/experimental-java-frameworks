package workflow.controller.rule.eval;

import workflow.controller.Trace;
import workflow.controller.rule.WorkflowRule;

public class IsNotNull implements Eval {

	@Override
	public boolean evaluate(Object value, String param) {
		if(WorkflowRule.debugMode)
			Trace.writeLog("IsNotNull: evaluating value " + value);
		boolean result = value != null;
		if(WorkflowRule.debugMode)
			Trace.writeLog("IsNotNull: evaluation result = " + result);
		return result;
	}

}

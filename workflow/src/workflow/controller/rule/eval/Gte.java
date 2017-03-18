package workflow.controller.rule.eval;

import workflow.controller.Trace;
import workflow.controller.rule.WorkflowRule;

public class Gte extends CompareValues {

	@Override
	public boolean evaluate(Object value, String param) {
		if(value instanceof Comparable) {
			Object obj = this.convertParamForTarget(value, param);
			if(WorkflowRule.debugMode)
				Trace.writeLog("Gte: evaluating value " + value + " (converted value " + obj + ")");
			boolean result = ((Comparable)value).compareTo(obj) >= 0;
			if(WorkflowRule.debugMode)
				Trace.writeLog("Gte: evaluation result = " + result);
			return result;
		}
		return false;
	}

}

package workflow.controller.rule.cond;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import core.util.MapUtils;
import workflow.controller.Trace;
import workflow.controller.rule.eval.Eq;
import workflow.controller.rule.eval.Eval;
import workflow.controller.rule.eval.Gt;
import workflow.controller.rule.eval.Gte;
import workflow.controller.rule.eval.IsNotNull;
import workflow.controller.rule.eval.IsNull;
import workflow.controller.rule.eval.Lt;
import workflow.controller.rule.eval.Lte;
import workflow.controller.rule.eval.Neq;
import ariba.util.fieldvalue.FieldValue;
import ariba.util.log.Log;

public interface Condition {

	public boolean evaluate(Object model);
	
	public class KeyValue implements Condition {
		static Map<String, Eval> evaluators;
		static {
			evaluators = MapUtils.map();
			evaluators.put("isnull", new IsNull());
			evaluators.put("isnotnull", new IsNotNull());
			evaluators.put("eq", new Eq());
			evaluators.put("neq", new Neq());
			evaluators.put("lt", new Lt());
			evaluators.put("lte", new Lte());
			evaluators.put("gt", new Gt());
			evaluators.put("gte", new Gte());
		}

		String key;
		String value;
		String op;
		
		public KeyValue(String key, String value, String op) {
			this.key = key;
			this.value = value;
			this.op = (op == null ? "eq" : op);
		}

		@Override
		public boolean evaluate(Object model) {
			Object val = FieldValue.getFieldValue(model, key);
			Eval eval = evaluators.get(op.toLowerCase());
			if(eval == null) {
				Trace.writeLog(this.toString() + " Invalid operator in key-value condition (No suitable evaluator)");
				return false;
			}
			Trace.writeLog("Evaluating " + model.getClass().getName() + " using " + this.toString());
			return eval.evaluate(val, value);
		}

		public String toString() {
			return "KeyValue {" + "key:" + key + "; value:" + value + "; op:" + op + "}";
		}
	}

	public class Not implements Condition {

		private Condition condition;
		
		public Not(Condition condition) {
			this.condition = condition;
		}
		@Override
		public boolean evaluate(Object model) {
			Trace.writeLog("Evaluating " + model.getClass().getName() + " using " + this.toString());
			return !(this.condition.evaluate(model));
		}
		
		public String toString() {
			return "Not (" + condition.toString() + ")"; 
		}
	}

	public abstract class Junction implements Condition {
		List<Condition> conditions;
		
		protected Junction(List<Condition> conditions) {
			this.conditions = conditions;
		}
		protected Junction(Condition[] conditions) {
			this.conditions = Arrays.asList(conditions);
		}
		public List<Condition> getConditions() {
			return conditions;
		}
	}

	public class Or extends Junction {

		public Or(List<Condition> conditions) {
			super(conditions);
		}
		public Or(Condition[] conditions) {
			super(conditions);
		}
		
		@Override
		public boolean evaluate(Object model) {
			Trace.writeLog("Evaluating " + model.getClass().getName() + " using " + this.toString());
			for(Condition cond : getConditions()) {
				if(cond.evaluate(model))
					return true;
			}
			return false;
		}
		
		public String toString() {
			return "Or (" + conditions.toString() + ")";
		}
	}
	
	public class And extends Junction {

		public And(List<Condition> conditions) {
			super(conditions);
		}
		public And(Condition[] conditions) {
			super(conditions);
		}
		
		@Override
		public boolean evaluate(Object model) {
			Trace.writeLog("Evaluating " + model.getClass().getName() + " using " + this.toString());
			for(Condition cond : getConditions()) {
				if(!cond.evaluate(model))
					return false;
			}
			return true;
		}
		
		public String toString() {
			return "And (" + conditions.toString() + ")";
		}
	}
}

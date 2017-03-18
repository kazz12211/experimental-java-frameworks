package workflow.controller.rule.eval;


public abstract class CompareValues implements Eval {
	
	protected Object convertParamForTarget(Object target, String param) {
		Class<?> targetClass = target.getClass();
		Object value = ValueConverter.convertForTarget(targetClass, param);
		return value;
	}
}

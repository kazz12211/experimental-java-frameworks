package core.util;

import java.lang.reflect.Method;

public class Selector {

	private String _methodName;
	private Class<?>[] _argumentClasses;

	public Selector(String methodName, Class<?> argClass) {
		this(methodName, new Class<?>[] {argClass});
	}

	public Selector(String methodName, Class<?> argClasses[]) {
		_methodName = methodName;
		_argumentClasses = argClasses;
	}
	
	public Object invoke(Object object, Object[] args) throws Exception {
		if(args.length != _argumentClasses.length) {
			throw new IllegalArgumentException("Expect " + _argumentClasses.length + " argument(s) but received " + args.length + " argument(s)");
		}
		Class<?> targetClass = object.getClass();
		Method method = null;
		try {
			method = ReflectionUtil.methodOnClass(targetClass, _methodName, _argumentClasses);
			return method.invoke(object, args);
		} catch (Exception e) {
			throw e;
		}
	}
	
	public Object safeInvoke(Object object, Object[] args) {
		try {
			return this.invoke(object, args);
		} catch (Exception e) {
			Log.coreUtilSelector.warn("Selector(" + _methodName + ") safeInvoke() throws", e);
		}
		return null;
	}
	
	public static Object safeInvokeSelector(Selector aSelector, Class<?> targetClass, Object object) {
		try {
			Method method = ReflectionUtil.methodOnClass(targetClass, aSelector._methodName, aSelector._argumentClasses);
			return method.invoke(targetClass, new Object[]{object});
		} catch (Exception e) {
			Log.coreUtilSelector.warn("Selector.safeInvokeSelector(" + targetClass.getName() + ", " + aSelector._methodName + ") failed", e);
		}
		
		return null;
	}

	public static boolean objectRespondsTo(Object object, Selector selector) {
		Class<?> targetClass = object.getClass();
		Method method = null;
		try {
			method =ReflectionUtil.methodOnClass(targetClass, selector._methodName, selector._argumentClasses);
		} catch (Exception e) {
			Log.coreUtilSelector.debug("Selector.objectRespondsTo(" + targetClass.getName() + ", " + selector._methodName + ") returns false");
		}
		return method != null;
	}
}

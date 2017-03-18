package core.util;

import java.lang.reflect.Method;

public class ReflectionUtil {

	public static Method methodOnObject(Object object, String methodName) throws Exception {
		return methodOnClass(object.getClass(), methodName);
	}

	public static Method methodOnClass(Class<?> aClass, String methodName) throws Exception {
		Method method = null;
		Method interfaces[] = methodsOnClass(aClass);
		int count = interfaces != null ? interfaces.length: 0;
		for(int i = 0; i < count; i++) {
			if(!interfaces[i].getName().equals(methodName))
				continue;
			method = aClass.getMethod(methodName,  interfaces[i].getParameterTypes());
			if(method == null)
				continue;
			break;
		}
		return method;
	}
	
	public static Method methodOnClass(Class<?> targetClass,
			String methodName, Class<?>[] argumentClasses) throws Exception {
		Method method = targetClass.getMethod(methodName, argumentClasses);
		return method;
	}

	public static Method[] methodsOnClass(Class<?> aClass) {
        Method methods[] = aClass.getMethods();
        if(methods == null)
            return new Method[0];
        return methods;
    }

	public static Object perform(String methodName, Object target, Object[] args) throws Exception {
        Method method = null;
        Object obj = null;
        
        if(methodName == null || target == null)
            throw new IllegalArgumentException("methodName or object is null.");
        method = methodOnObject(target, methodName);
        if(method == null) {
            throw new IllegalArgumentException(obj.getClass() + " does not implement method " + methodName + ".");
        }
        else {
            try {
                return method.invoke(target, args);
            } catch (Exception e) {
                throw e;
            }
        }
	}



}

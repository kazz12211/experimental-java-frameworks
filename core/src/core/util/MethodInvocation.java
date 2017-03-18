package core.util;

import java.lang.reflect.Method;

public class MethodInvocation {

    public static boolean respondsTo(Object object, String methodName, Class paramTypes[]) {
    	Method method = methodOnObject(object, methodName, paramTypes);
    	return (method != null);
    }
    
    public static boolean respondsTo(Object object, String methodName) {
    	return respondsTo(object, methodName, new Class[0]);
    }
    
    public static Object perform(Object object, String methodName, Object args[]) throws Exception {
        Method method = null;
        Object obj = null;
        
        if(methodName == null)
            throw new IllegalArgumentException("methodName is null.");
        method = methodOnObject(object, methodName);
        if(method == null) {
            throw new IllegalArgumentException(object.getClass() + " does not implement method " + methodName + ".");
        }
        else {
            try {
                return method.invoke(object, args);
            } catch (Exception e) {
                throw e;
            }
        }
    }
    
    public static Object perform(Object object, String methodName) throws Exception {
    	return perform(object, methodName, new Object[0]);
    }

	static private Method methodOnObject(Object object, String methodName, Class[] paramTypes) {
		Method method = null;
		Method interfaces[] = methodsOnClass(object.getClass());
		int count = interfaces != null ? interfaces.length : 0;
		for(int i = 0; i < count; i++) {
			if(!interfaces[i].getName().equals(methodName))
				continue;
			method = method(methodName, interfaces[i], object.getClass());
			Class params[] = method.getParameterTypes();
			for(int j = 0; j < params.length; j++) {
				if(params[j].equals(paramTypes[j]) == false) {
					method = null;
					break;
				}
			}
		}
		return method;
	}
	
	static private Method method(String methodName, Method method, Class aClass) {
        Method meth = null;
        try {
            meth = aClass.getMethod(methodName, method.getParameterTypes());
        } catch (Exception e) {}
		return meth;
	}
	
	static private Method methodOnObject(Object object, String methodName) {
		return methodOnClass(object.getClass(), methodName);
	}

	static private Method methodOnClass(Class aClass, String methodName) {
        Method method = null;
        Method interfaces[] = methodsOnClass(aClass);
        int count = interfaces != null ? interfaces.length : 0;
        for(int i = 0; i < count; i++) {
            if(!interfaces[i].getName().equals(methodName))
                continue;
            method = method(methodName, interfaces[i], aClass);
            if(method == null)
                continue;
            break;
        }
        
        return method;
	}

	static private Method[] methodsOnClass(Class aClass) {
        Method methods[] = null;
        try {
        	methods = aClass.getMethods();
        } catch (Exception ignore) {}
        if(methods == null)
        	methods = new Method[0];
		return methods;
	}

}

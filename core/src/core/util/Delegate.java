package core.util;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

public class Delegate {
	private Object _delegateObject;
	private volatile Class<?> _delegateClass;
	private Map<String, Method> _methods;
	private boolean _delegateConformance;
	
	public Delegate(Class<?> delegateClass) {
		_delegateClass = delegateClass;
		_delegateConformance = false;
	}
	
	public synchronized void setDelegate(Object object) {
		_delegateObject = object;
		if(_delegateObject != null) {
			Class aClass = object.getClass();
			if(_delegateClass.isAssignableFrom(aClass))
				_delegateConformance = true;
			_methods = MapUtils.map();
		}
	}
	
	public Object delegate() {
		return _delegateObject;
	}
	
	private Method method(String methodName) {
		Method meth = null;
		try {
			meth = ReflectionUtil.methodOnObject(_delegateObject, methodName);
		} catch (Exception e) {
			Log.coreUtilDelegate.error("method(String methodName) failed", e);
		}
		return meth;
	}
	
	private Method delegateMethod(String methodName, Method method, Class<?> aClass) {
		Method meth = null;
		try {
			meth = aClass.getMethod(methodName, method.getParameterTypes());
		} catch (Exception e) {
			Log.coreUtilDelegate.error("delegateMethod(String methodName, Method method, Class<?> aClass) failed", e);
		}
		return meth;
	}
	
	public boolean respondsTo(String methodName) {
		if(_methods == null)
			return false;
		if(_delegateConformance)
			return true;
		
		boolean responds = false;
		synchronized(this) {
			if(method(methodName) != null)
				responds = true;
		}
		return responds;
	}
	
	public boolean booleanPerform(String methodName) {
		return booleanPerform(methodName, new Object[0]);
	}

	public boolean booleanPerform(String methodName, Object arg) {
		return booleanPerform(methodName, new Object[] {arg});
	}
	
	public boolean booleanPerform(String methodName, Object arg1, Object arg2) {
		return booleanPerform(methodName, new Object[] {arg1, arg2});
	}

	public boolean booleanPerform(String methodName, Object arg1, Object arg2, Object arg3) {
		return booleanPerform(methodName, new Object[] {arg1, arg2, arg3});
	}
	
	public boolean booleanPerform(String methodName, List<?> args) {
		return booleanPerform(methodName, args.toArray());
	}
	
	public boolean booleanPerform(String methodName, Object args[]) {
		Object result = null;
		try {
			result = perform(methodName, args);
		} catch (Exception e) {
			Log.coreUtilDelegate.error("booleanPerform(String methodName, Object args[]) failed", e);
		}
		if(result == null)
			return false;
		if(result instanceof Boolean)
			return ((Boolean) result).booleanValue();
		return false;
	}
	
	public Object perform(String methodName) throws Exception {
        return perform(methodName, new Object[0]);
	}
	
	public Object perform(String methodName, Object arg) throws Exception {
        return perform(methodName, new Object[] { arg } );
    }

	public Object perform(String methodName, Object arg1, Object arg2) throws Exception {
        return perform(methodName, new Object[] { arg1, arg2 });
    }

	public Object perform(String methodName, Object arg1, Object arg2, Object arg3) throws Exception {
        return perform(methodName, new Object[] { arg1, arg2, arg3 });
    }

	public Object perform(String methodName, List<?> args) throws Exception {
        return perform(methodName, args.toArray());
    }

	public Object perform(String methodName, Object args[]) throws Exception {
        if(_delegateObject == null)
            throw new IllegalArgumentException("Attempt to invoke method on null object.");
            
        try {
            return ReflectionUtil.perform(methodName, _delegateObject, args);
        } catch (Exception e) {
            throw e;
        }
    }
    
	public int integerPerform(String methodName) {
        return integerPerform(methodName, new Object[0]);
    }
	
	public int integerPerform(String methodName, Object arg) {
        return integerPerform(methodName, new Object[]{arg});
    }
	
	public int integerPerform(String methodName, Object arg1, Object arg2) {
        return integerPerform(methodName, new Object[]{arg1, arg2});
    }
	
	public int integerPerform(String methodName, Object arg1, Object arg2, Object arg3) {
        return integerPerform(methodName, new Object[]{arg1, arg2, arg3});
    }
	
	public int integerPerform(String methodName, List<?> args) {
        return integerPerform(methodName, args.toArray());
    }
	
	public int integerPerform(String methodName, Object args[]) {
        Object obj = null;
        
        try {
            obj = perform(methodName, args);
        } catch (Exception e) {}
        
        if(obj == null)
            return 0;
        if(obj instanceof Number)
            return ((Number)obj).intValue();
        return 0;
    }

}

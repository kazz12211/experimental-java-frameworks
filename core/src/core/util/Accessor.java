package core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public abstract class Accessor {

	protected Class<?> _forClass;
	
	public abstract Object valueInObject(Object object);
	public abstract void setValueInObject(Object object, Object value);
	public abstract Accessor copy();
	public abstract Class<?> getParameterType();
	public abstract Class<?> getReturnType();

	protected Accessor() {}
	
	public static Accessor newGetAccessor(Class<?> receiverClass, String key) {
		Accessor getter = MethodAccessor.newAccessor(receiverClass, key, false);
		if(getter == null) {
			getter = FieldAccessor.newAccessor(receiverClass, key);
		}
		return getter;
	}
    
	public static Accessor newSetAccessor(Class<?> receiverClass, String key) {
		Accessor setter = MethodAccessor.newAccessor(receiverClass, key, true);
		if(setter == null) {
			setter = FieldAccessor.newAccessor(receiverClass, key);
		}
		return setter;
	}
    
	protected void setForClass(Class<?> forClass) {
		_forClass = forClass;
	}
    
	protected Class<?> forClass() {
		return _forClass;
	}
	
	public static class MethodAccessor extends Accessor {
		private Method method;
		private boolean isSetter;
		private boolean isScalar;
		private String key;

		public MethodAccessor(Method method, String key) {
			this.method = method;
			this.key = key;
			Class<?> c[] = this.method.getParameterTypes();
			this.isScalar = c.length == 1 && c[0].isPrimitive();
		}
		
		public Accessor copy() {
			return new MethodAccessor(method, key);
		}
		
		private void setIsSetter(boolean value) {
			isSetter = value;
		}
		
		private static Method[] getDeclaredMethods(Class<?> receiverClass) throws Exception {
			Method methodArray[] = null;
			try {
				methodArray = receiverClass.getDeclaredMethods();
			} catch (Exception exception) {
				throw new IllegalArgumentException(
					"Unable to getDeclaredMethods for receiverClass: "
						+ receiverClass
						+ "\n"
						+ exception);
			}
			return methodArray;
		}
		
		private static boolean compareKeyAndMethodNameWithPrefix(
			String key,
			String methodName,
			String prefixString) {
			boolean match = false;
			int keyLength = key.length();
			if (methodName.length() == prefixString.length() + keyLength
				&& methodName.startsWith(prefixString)
				&& methodName.regionMatches(4, key, 1, keyLength - 1)
				&& methodName.charAt(3) == Character.toUpperCase(key.charAt(0)))
				match = true;
			return match;
		}
		
		private static boolean standardSetterMethodCompare(String key, String methodName) {
			return compareKeyAndMethodNameWithPrefix(key, methodName, "set");
		}
		
		private static boolean standardGetterMethodCompare(String key, String methodName) {
			return (key.equals(methodName) || compareKeyAndMethodNameWithPrefix(key, methodName, "get"));
		}
		
		private static boolean compareKeyAndMethodName(
			String key,
			String methodName,
			boolean isSetter) {
			boolean match = false;
			if (isSetter)
				match = standardSetterMethodCompare(key, methodName);
			else
				match = standardGetterMethodCompare(key, methodName);
			return match;
		}
		
		public static MethodAccessor newAccessor(
			Class<?> receiverClass,
			String key,
			boolean isSetter) {
			MethodAccessor accessorMethod = null;
			Method methodArray[] = null;
			try {
				methodArray = getDeclaredMethods(receiverClass);
			} catch (Exception exception) {
				return null;
			}
			int methodCount = methodArray.length;
			for (int i = 0; i < methodCount; i++) {
				Method currentMethod = methodArray[i];
				if (!isSetter && currentMethod.getParameterTypes().length > 0)
					continue;
				String methodName = currentMethod.getName();
				if (!compareKeyAndMethodName(key, methodName, isSetter))
					continue;
				accessorMethod = new MethodAccessor(currentMethod, key);
				break;
			}
			if (accessorMethod == null) {
				Class<?> receiverSuperclass = receiverClass.getSuperclass();
				if (receiverSuperclass != null)
					accessorMethod = newAccessor(receiverSuperclass, key, isSetter);
			}
			if (accessorMethod != null)
				accessorMethod.setIsSetter(isSetter);
			return accessorMethod;
		}
		
		public Object valueInObject(Object receiver) {
			Object value = null;
			try {
				value = method.invoke(receiver, new Object[0]);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": valueInObject(Object receiver) exception _method: "
						+ method
						+ " receiver: "
						+ receiver
						+ "\n"
						+ e.getTargetException());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": valueInObject(Object receiver) exception _method: "
						+ method
						+ " receiver: "
						+ receiver
						+ "\n"
						+ e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": valueInObject(Object receiver) exception _method: "
						+ method
						+ " receiver: "
						+ receiver
						+ "\n"
						+ e);
			} catch (Exception e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": valueInObject(Object receiver) exception _method: "
						+ method
						+ " receiver: "
						+ receiver
						+ "\n"
						+ e);
			}
			return value;
		}
		
		public void setValueInObject(Object receiver, Object value) {
			Object args[] = { value };
			try {
				method.invoke(receiver, args);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": setValueInObject(Object receiver, Object value) _method:"
						+ method
						+ " receiver:"
						+ receiver
						+ " value:"
						+ value
						+ "\n"
						+ e.getTargetException());
			} catch (IllegalAccessException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": setValueInObject(Object receiver, Object value) _method:"
						+ method
						+ " receiver:"
						+ receiver
						+ " value:"
						+ value
						+ "\n"
						+ e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": setValueInObject(Object receiver, Object value) _method:"
						+ method
						+ " receiver:"
						+ receiver
						+ " value:"
						+ value
						+ "\n"
						+ e);
			} catch (Exception e) {
				throw new RuntimeException(
					getClass().getName()
						+ ": setValueInObject (Object receiver, Object value) _method:"
						+ method
						+ " receiver:"
						+ receiver
						+ " value:"
						+ value
						+ "\n"
						+ e);
			}
		}
		
		public Class<?> getParameterType() {
			if (isSetter) {
				Class<?> types[] = method.getParameterTypes();
				if (types.length == 1)
					return types[0];
				return null;
			} else {
				return method.getReturnType();
			}
		}

		@Override
		public Class<?> getReturnType() {
			if(this.isSetter)
				return Void.TYPE;
			return method.getReturnType();
		}
	}
	
	public static class FieldAccessor extends Accessor {
		
		private Field field;
		private static final FieldAccessor _dummy = new FieldAccessor(null, null);
		private boolean isScalar;
		private String key;
		
		public FieldAccessor(Field field, String key) {
			this.field = field;
			this.key = key;
			if (this.field != null)
				this.isScalar = this.field.getType().isPrimitive();
		}
		
		public Accessor copy() {
			return new FieldAccessor(field, key);
		}
		
		private static Field[] getDeclaredFields(Class<?> receiverClass)
			throws Exception {
			Field fieldArray[] = null;
			try {
				fieldArray = receiverClass.getDeclaredFields();
			} catch (Exception exception) {
				throw new IllegalArgumentException(
					"Unable to getDeclareFields for receiverClass: "
						+ receiverClass
						+ "\n"
						+ exception);
			}
			return fieldArray;
		}
		
		private static boolean compareKeyAndFieldName(
			String key,
			String fieldName) {
			boolean match = false;
			if (key.equals(fieldName))
				match = true;
			else if (
				fieldName.charAt(0) == '_'
					&& fieldName.length() == key.length() + 1
					&& fieldName.endsWith(key))
				match = true;
			return match;
		}
		
		private static FieldAccessor _newAccessorField(
			Class<?> receiverClass,
			String key) {
			FieldAccessor accessorField = null;
			Field fieldArray[] = null;
			try {
				fieldArray = getDeclaredFields(receiverClass);
			} catch (Exception exception) {
				return null;
			}
			if(fieldArray != null) {
				int fieldCount = fieldArray.length;
				for (int i = 0; i < fieldCount; i++) {
					Field field = fieldArray[i];
					String fieldName = field.getName();
					if (!compareKeyAndFieldName(key, fieldName))
						continue;
					accessorField = new FieldAccessor(field, key);
				}
			}
			if (accessorField == null) {
				Class<?> receiverSuperclass = receiverClass.getSuperclass();
				if (receiverSuperclass != null) {
					accessorField = newAccessor(receiverSuperclass, key);
				}
			}
			return accessorField;
		}
		
		public static FieldAccessor newAccessor(Class<?> receiverClass, String key) {
			FieldAccessor accessor = _newAccessorField(receiverClass, key);
			if (accessor == _dummy)
				accessor = null;
			return accessor;
		}
		
		public Object valueInObject(Object receiver) {
			Object value = null;
			try {
				value = field.get(receiver);
			} catch (Exception exception) {
				throw new RuntimeException(
					getClass().getName()
						+ ": valueInObject() exception: _field: "
						+ field
						+ " receiver: "
						+ receiver
						+ "\n"
						+ exception);
			}
			return value;
		}
		
		public void setValueInObject(Object receiver, Object value) {
			try {
				field.set(receiver, value);
			} catch (Exception exception) {
				throw new RuntimeException(
					getClass().getName()
						+ ": setValueInObject() exception: _field: "
						+ field
						+ " receiver: "
						+ receiver
						+ " value: "
						+ value
						+ "\n"
						+ exception);
			}
		}
		
		public Class<?> getParameterType() {
			return field.getType();
		}

		@Override
		public Class<?> getReturnType() {
			return field.getType();
		}

	}
}

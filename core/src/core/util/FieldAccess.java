package core.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 * オブジェクトのプロパティに対するアクセス方法を統一するインターフェース。
 * このインターフェースを実装したクラスのインスタンスは、内部のフィールドの値をキー（フィールド名）を指定して取得および設定が行える。
 * 
 * @author ktsubaki
 *
 */
public interface FieldAccess {

	/**
	 * プロパティーをキーを指定して取り出す。
	 * @param key
	 * @return
	 */
	public abstract Object getValueForKey(String key);
	/**
	 * プロパティーをキーを指定して設定する。
	 * @param value
	 * @param key
	 */
	public abstract void setValueForKey(Object value, String key);
	/**
	 * プロパティーをキーパス（ドットで区切られたキーの組み合わせ）を指定して取り出す。
	 * @param keyPath
	 * @return
	 */
	public abstract Object getValueForKeyPath(String keyPath);
	/**
	 * プロパティーをキーパス（ドットで区切られたキーの組み合わせ）を指定して設定する。
	 * @param obj
	 * @param keyPath
	 */
	public abstract void setValueForKeyPath(Object obj, String keyPath);
	
	public static class Util {
		public static Object getValueForKey(Object target, String key) {
			if(target instanceof FieldAccess) {
				return ((FieldAccess) target).getValueForKey(key);
			} else {
				return DefaultImplementation.getValueForKey(target, key);
			}
		}
		public static void setValueForKey(Object target, Object value, String key) {
			if(target instanceof FieldAccess) {
				((FieldAccess) target).setValueForKey(value, key);
			} else {
				DefaultImplementation.setValueForKey(target, value, key);
			}
		}
	}
	
	public static class DefaultImplementation {
		
		public static Object getValueForKey(Object object, String key) {
			if(key == null)
				return null;
			if(object instanceof Map) {
				return ((Map) object).get(key);
			} else {
				Accessor getter = Accessor.newGetterAccessor(object.getClass(), key);
				return getter.valueInObject(object);
			}
		}
		
		public static void setValueForKey(Object object, Object value, String key) {
			if(key == null)
				throw new IllegalArgumentException("Key cannot be null");
			if(object instanceof Map) {
				((Map) object).put(key, value);
			} else {
				Accessor setter = Accessor.newSetterAccessor(object.getClass(), key);
				setter.setValueInObject(object, value);
			}
		}
		
		public static Object getValueForKeyPath(Object object, String keyPath) {
			if(keyPath == null)
				return null;
			int index = keyPath.indexOf('.');
			if(index < 0)
				return getValueForKey(object, keyPath);
			else {
				String key = keyPath.substring(0, index);
				Object value = getValueForKey(object, key);
				return value != null ? getValueForKeyPath(value, keyPath.substring(index+1)) : null;
			}
		}
		
		public static void setValueForKeyPath(Object object, Object value, String keyPath) {
			if(keyPath == null)
				throw new IllegalArgumentException("Key path cannot be null");
			int index = keyPath.indexOf('.');
			if(index < 0)
				setValueForKey(object, value, keyPath);
			else {
				String key = keyPath.substring(0, index);
				Object target = getValueForKey(object, key);
				if(target != null)
					setValueForKeyPath(target, value, keyPath.substring(index+1));
			}
		}
	}
	
	public static class KeyValueCodingAdaptor {
		
		private static Map _setters = new HashMap();
		private static Map _getters = new HashMap();
		
		private static Accessor findGetter(Class<?> targetClass, String key) {
			Accessor accessor = (Accessor)MapUtils.doubleHashGet(_getters, targetClass, key);
			if(accessor == null) {
				synchronized(_getters) {
					accessor = (Accessor)MapUtils.doubleHashGet(_getters, targetClass, key);
					if(accessor == null) {
						accessor = Accessor.newGetterAccessor(targetClass, key);
						accessor = accessor.copy();
						accessor._forClass = targetClass;
						_getters = MapUtils.doubleHashPutInNewMap(_getters, targetClass, key, accessor);
					}
				}
			}
			return accessor;
		}
		private static Accessor findSetter(Class<?> targetClass, String key) {
			Accessor accessor = (Accessor)MapUtils.doubleHashGet(_setters, targetClass, key);
			if(accessor == null) {
				synchronized(_setters) {
					accessor = (Accessor)MapUtils.doubleHashGet(_setters, targetClass, key);
					if(accessor == null) {
						accessor = Accessor.newGetterAccessor(targetClass, key);
						accessor = accessor.copy();
						accessor._forClass = targetClass;
						_setters = MapUtils.doubleHashPutInNewMap(_setters, targetClass, key, accessor);
					}
				}
			}
			return accessor;
		}
	}
	
	public class KeyPath {
	    private static Map _keyPathHashtable = new HashMap();
	    private String _key;
	    private KeyPath _nextKey;
	    private String _stringValue;
	    protected Accessor _previousGetter;
	    protected Accessor _previousSetter;

	    public KeyPath() {
	    }

	    private String firstPartOfKeyPath(String keyPath) {
	      int indexOfDot = keyPath.indexOf(46);
	      String s = null;
	      if(indexOfDot == -1)
	        s = keyPath;
	      else
	        s = keyPath.substring(0, indexOfDot);
	      return s;
	    }

	    private String remainingPartsOfFirstKeyPathPart(String keyPath) {
	      String s = null;
	      int indexOfDot = keyPath.indexOf(46);
	      if(indexOfDot != -1)
	        s = keyPath.substring(indexOfDot+1);
	      return s;
	    }

	    public void init(String keyPath) {
	      _key = firstPartOfKeyPath(keyPath);
	      String remains = remainingPartsOfFirstKeyPathPart(keyPath);
	      if(remains != null) {
	        _nextKey = new KeyPath();
	        _nextKey.init(remains);
	      }
	    }

	    public void init(String key, KeyPath nextKey) {
	      _key = key;
	      _nextKey = nextKey;
	    }

	    public String key() {
	      return _key;
	    }

	    public KeyPath nextKey() {
	      return _nextKey;
	    }

	    public String toString() {
	      if(_stringValue == null)
	        if(_nextKey == null)
	          _stringValue = _key;
	        else
	          _stringValue = _key+"."+_nextKey.toString();
	      return _stringValue;
	    }

	    public static KeyPath keyPathForString(String keyPath) {
	      KeyPath kp = new KeyPath();
	      kp.init(keyPath);
	      return kp;
	    }

	    public static KeyPath sharedKeyPathForString(String keyPath) {
	      KeyPath kp = (KeyPath)_keyPathHashtable.get(keyPath);
	      if(kp == null) {
	        kp = keyPathForString(keyPath);
	        _keyPathHashtable = MapUtils.putInNewMap(_keyPathHashtable, keyPath, kp);
	      }
	      return kp;
	    }

	    public String lastKeyPathComponent() {
	      if(_nextKey == null)
	        return _key;
	      else
	        return _nextKey.lastKeyPathComponent();
	    }

	    public KeyPath keyPathByDeletingLastComponent() {
	      if(_nextKey == null)
	        return null;
	      else {
	        KeyPath keyPath = new KeyPath();
	        KeyPath rem = _nextKey.keyPathByDeletingLastComponent();
	        keyPath.init(_key, rem);
	        return keyPath;
	      }
	    }
		
	}
	
	abstract public class Accessor {
	    protected Class<?> _forClass;

	    public abstract Object valueInObject(Object obj);
	    public abstract void setValueInObject(Object obj, Object value);
	    public abstract Accessor copy();

	    public Accessor() {
	    }

	    public static Accessor newGetterAccessor(Class receiverClass, String key) {
	      Accessor getter = MethodAccessor.newAccessorMethod(receiverClass, key, false);
	      if(getter == null) {
	        getter = FieldAccessor.newAccessorField(receiverClass, key);
	        if(getter == null) {
	          throw new FieldAccessException("KeyValueCodingAdaptor: unable to locate getter method or field for key \""+key+"\" on receiver class: "+receiverClass);
	        }
	      }
	      return getter;
	    }

	    public static Accessor newSetterAccessor(Class receiverClass, String key) {
	      Accessor setter = MethodAccessor.newAccessorMethod(receiverClass, key, true);
	      if(setter == null) {
	        setter = FieldAccessor.newGetterAccessor(receiverClass, key);
	        if(setter == null) {
	          throw new FieldAccessException("KeyValueCodingAdaptor: unable to locate setter method or field for key \""+key+"\" on receiver class: "+receiverClass);
	        }
	      }
	      return setter;
	    }

	    protected void setForClass(Class<?> forClass) {
	      _forClass = forClass;
	    }
		
	}
	
	public class FieldAccessor extends Accessor {

	    private Field _field;
	    private static Map _fields = new HashMap();
	    private static final FieldAccessor _dummy = new FieldAccessor(null);

	    public FieldAccessor(Field field) {
	      _field = field;
	    }

	    public Accessor copy() {
	      return new FieldAccessor(_field);
	    }

	    private static Field[] getDeclaredFields(Class receiverClass) {
	      Field fieldArray[] = null;
	      try {
	        fieldArray = receiverClass.getDeclaredFields();
	      }
	      catch(Exception e) {
	        throw new FieldAccessException("Error: unable to getDeclaredFields for receiverClass: "+receiverClass+"\n"+e);
	      }
	      return fieldArray;
	    }

	    protected static boolean compareKeyAndFieldName(String key, String fieldName) {
	      boolean match = false;
	      if(key.equals(fieldName))
	        match = true;
	      else
	        if(fieldName.charAt(0) == '_' && fieldName.length() == key.length()+1 && fieldName.endsWith(key))
	          match = true;
	      return match;
	    }

	    private static FieldAccessor _newAccessorField(Class receiverClass, String key) {
	      FieldAccessor accessorField = null;
	      Field fieldArray[] = getDeclaredFields(receiverClass);
	      int fieldCount = fieldArray.length;
	      for(int i = 0; i < fieldCount; i++) {
	        Field field = fieldArray[i];
	        String fieldName = field.getName();
	        if(!compareKeyAndFieldName(key, fieldName))
	          continue;
	        accessorField = new FieldAccessor(field);
	      }
	      if(accessorField == null) {
	        Class receiverSuperclass = receiverClass.getSuperclass();
	        if(receiverSuperclass != null) {
	          accessorField = newAccessorField(receiverSuperclass, key);
	        }
	      }
	      return accessorField;
	    }

	    protected static FieldAccessor newAccessorField(Class receiverClass, String key) {
	      FieldAccessor accessor = (FieldAccessor)MapUtils.doubleHashGet(_fields, receiverClass, key);
	      if(accessor == null) {
	        synchronized(_fields) {
	          accessor = (FieldAccessor)MapUtils.doubleHashGet(_fields, receiverClass, key);
	          if(accessor == null) {
	            accessor = _newAccessorField(receiverClass, key);
	            if(accessor == null)
	              accessor = _dummy;
	            MapUtils.doubleHashPutInNewMap(_fields, receiverClass, key, accessor);
	          }
	        }
	      }
	      if(accessor == _dummy)
	        accessor = null;
	      return accessor;
	    }

	    public Object valueInObject(Object receiver) {
	      Object value = null;
	      try {
	        value = _field.get(receiver);
	      }
	      catch(Exception e) {
	        throw new FieldAccessException(getClass().getName()+ ": valueInObject(Object receiver) exception: _field: "+_field+" receiver: "+receiver+"\n"+e);
	      }
	      return value;
	    }

	    public void setValueInObject(Object receiver, Object value) {
	      try {
	        _field.set(receiver, value);
	      }
	      catch(Exception e) {
	        throw new FieldAccessException(getClass().getName() + ": valueInObject(Object receiver, Object value) _field: \"" + _field.getName() + "\" " + _field + " receiver: " + receiver + " value: (class: "+value.getClass().getName() + ") " + value + "\n"+e);
	      }
	    }
		
	}
	
	public class MethodAccessor extends Accessor {
	    private Method _method;

	    public MethodAccessor(Method getter) {
	      _method = getter;
	    }

	    public Accessor copy() {
	      return new MethodAccessor(_method);
	    }

	    private static Method[] getDeclaredMethods(Class receiverClass) {
	      Method methodArray[] = null;
	      try {
	        methodArray = receiverClass.getDeclaredMethods();
	      }
	      catch(Exception e) {
	        throw new FieldAccessException("Error: unable to getDeclaredMethods for receiverClass: " + receiverClass + "\n"+e);
	      }
	      return methodArray;
	    }

	    protected static boolean compareKeyAndMethodNameWithPrefix(String key, String methodName, String prefixString) {
	      boolean match = false;
	      int keyLength = key.length();
	      if(methodName.length() == prefixString.length()+keyLength && methodName.startsWith(prefixString) && methodName.regionMatches(4, key, 1, keyLength-1) && methodName.charAt(3) == Character.toUpperCase(key.charAt(0)))
	        match = true;
	      return match;
	    }

	    protected static boolean standardSetterMethodCompare(String key, String methodName) {
	      return compareKeyAndMethodNameWithPrefix(key, methodName, "set");
	    }

	    protected static boolean standardGetterMethodCompare(String key, String methodName) {
	      return key.equals(methodName) || compareKeyAndMethodNameWithPrefix(key, methodName, "get");
	    }

	    protected static boolean compareKeyAndMethodName(String key, String methodName, boolean isSetter) {
	      boolean match = false;
	      if(isSetter)
	        match = standardSetterMethodCompare(key, methodName);
	      else
	        match = standardGetterMethodCompare(key, methodName);
	      return match;
	    }

	    protected static MethodAccessor newAccessorMethod(Class receiverClass, String key, boolean isSetter) {
	      MethodAccessor accessorMethod = null;
	      Method methodArray[] = getDeclaredMethods(receiverClass);
	      int methodCount = methodArray.length;
	      for(int i = 0; i < methodCount; i++) {
	        Method currentMethod = methodArray[i];
	        if(!isSetter && currentMethod.getParameterTypes().length > 0)
	          continue;
	        String methodName = currentMethod.getName();
	        if(!compareKeyAndMethodName(key, methodName, isSetter))
	          continue;
	        accessorMethod = new MethodAccessor(currentMethod);
	        break;
	      }

	      if(accessorMethod == null) {
	        Class<?> receiverSuperclass = receiverClass.getSuperclass();
	        if(receiverSuperclass != null)
	          accessorMethod = newAccessorMethod(receiverSuperclass, key, isSetter);
	      }
	      return accessorMethod;
	    }

	    public Object valueInObject(Object receiver) {
	      Object value = null;
	      try {
	        value = _method.invoke(receiver, MapUtils.EmptyClassArray);
	      }
	      catch(InvocationTargetException e) {
	        throw new FieldAccessException(getClass().getName() + ": valueInObject(Object receiver) exception _method: " + _method + " receiver: " + receiver + "\n"+e.getTargetException());
	      }
	      catch (IllegalAccessException e) {
	        throw new FieldAccessException(getClass().getName() + ": valueInObject(Object receiver) exception _method: " + _method + " receiver: " + receiver + "\n"+e);
	      }
	      catch (IllegalArgumentException e) {
	        throw new FieldAccessException(getClass().getName() + ": valueInObject(Object receiver) exception _method: " + _method + " receiver: " + receiver + "\n"+e);
	      }
	      catch(Exception e) {
	        throw new FieldAccessException(getClass().getName() + ": valueInObject(Object receiver) exception _method: " + _method + " receiver: " + receiver + "\n"+e);
	      }
	      return value;
	    }

	    public void setValueInObject(Object receiver, Object value) {
	      Object args[] = {value};
	      try {
	        _method.invoke(receiver, args);
	      }
	      catch (InvocationTargetException e) {
	        throw new FieldAccessException(getClass().getName() + ": setValueInObject(Object receiver, Object value) _method:" + _method + " receiver:" + receiver + " value:" + value + "\n"+e.getTargetException());
	      }
	      catch (IllegalAccessException e) {
	        throw new FieldAccessException(getClass().getName() + ": setValueInObject(Object receiver, Object value) _method:" + _method + " receiver:" + receiver + " value:" + value + "\n"+e);
	      }
	      catch (IllegalArgumentException e) {
	        throw new FieldAccessException(getClass().getName() + ": setValueInObject(Object receiver, Object value) _method:" + _method + " receiver:" + receiver + " value:" + value + "\n"+e);
	      }
	      catch (Exception e) {
	        throw new FieldAccessException(getClass().getName() + ": setValueInObject (Object receiver, Object value) _method:" + _method + " receiver:" + receiver + " value:" + value + "\n"+e);
	      }
	    }
		
	}
	
	public class MapUtils {
		
		public static final Class<?> EmptyClassArray[] = new Class<?>[0];
		public static final Class<?> ClassClass = core.util.ClassUtils.classForName("java.lang.Class");
		
		public static Object doubleHashGet(Map<?, Map<?, ?>> map, Object firstKey, Object secondKey) {
			Map<?, ?> table = map.get(firstKey);
			if(table != null)
				return table.get(secondKey);
			return null;
		}
		
		public static Map doubleHashPutInNewMap(Map<?, Map<?, ?>> map, Object firstKey, Object secondKey, Object value) {
			Map<?, ?> table = map.get(firstKey);
			table = putInNewMap(table, secondKey, value);
			return putInNewMap(map, firstKey, table);
		}
		
		public static Map putInNewMap(Map<?, ?> map, Object key, Object value) {
			Map newMap = null;
			if(map == null) {
				map = new HashMap(1);
				newMap = map;
			} else {
				newMap = new HashMap(map.size() + 1);
			}
			addElements(newMap, map);
			newMap.put(key, value);
			return newMap;
		}
		
		public static void addElements(Map destination, Map source) {
			if(source != null && !source.isEmpty()) {
				Object currentSourceKey;
				Object currentSourceValue;
				for(Object key : source.keySet()) {
					currentSourceKey = key;
					currentSourceValue = source.get(currentSourceKey);
					destination.put(currentSourceKey, currentSourceValue);
				}
			}
		}

	}
}

package universe;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import asjava.uniclientlibs.UniString;
import core.util.Accessor;
import core.util.CharacterSet;
import core.util.ListUtils;
import universe.util.UniLogger;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniField {

	private static byte[] _NoBytes = new byte[0];
	
	public static Class<?> AffordableValueClasses[] = {
		java.lang.String.class,
		java.lang.Integer.class,
		java.lang.Long.class,
		java.lang.Short.class,
		java.lang.Float.class,
		java.lang.Double.class,
		java.lang.Boolean.class,
		java.lang.Byte.class,
		java.lang.Character.class,
		java.util.Date.class,
		java.math.BigDecimal.class,
		java.math.BigInteger.class,
		java.util.List.class,
		_NoBytes.getClass()
	};
	
	public static String AffordableValueClassNames[] = {
		"String",
		"Integer",
		"Long",
		"Short",
		"Float",
		"Double",
		"Boolean",
		"Byte",
		"Character",
		"Date",
		"BigDecimal",
		"BigInteger",
		"List",
		"Bytes"
	};
	
	protected static Class<?> DateClasses[] = {
		java.util.Date.class
	};
	protected static Class<?> NumberClasses[] = {
		Integer.class,
		java.lang.Long.class,
		java.lang.Short.class,
		java.lang.Float.class,
		java.lang.Double.class,
		java.lang.Boolean.class,
		java.math.BigDecimal.class,
		java.math.BigInteger.class
	};
	protected static Class<?> StringClasses[] = {
		java.lang.String.class,
	};

	String _columnName;
	String _key;
	boolean _isPrimaryKey;
	Class<?> _valueClass;
	UniEntity _entity;
	String _dateFormat;
	boolean _isMultiValue;
	private DateFormat __dateFormat;
	String _associationName;
	boolean _isReadOnly;
	boolean _lockKey;
	int _location = -1;
	
	public UniField(UniEntity entity, String columnName, String key) {
		this._entity = entity;
		this._columnName = columnName;
		this._key = key;
		this._isPrimaryKey = false;
	}
	public UniField(UniEntity entity, String columnName, String key, Class<?> valueClass, boolean isPK) {
		this._entity = entity;
		this._columnName = columnName;
		this._key = key;
		this._valueClass = valueClass;
		this._isPrimaryKey = isPK;
	}
	public UniField(UniEntity entity, String columnName, String key, String dateFormat) {
		this._entity = entity;
		this._columnName = columnName;
		this._key = key;
		this._dateFormat = dateFormat;
		this.__dateFormat = new SimpleDateFormat(dateFormat);
	}
	
	public String columnName() {
		return _columnName;
	}
	public String key() {
		return _key;
	}
	public boolean isPrimaryKey() {
		return _isPrimaryKey;
	}
	public Class<?> valueClass() {
		return _valueClass;
	}
	
	public DateFormat dateFormat() {
		if(_dateFormat == null)
			return null;
		if(__dateFormat == null)
			__dateFormat = new SimpleDateFormat(_dateFormat);
		return __dateFormat;
	}
	public boolean isMultiValue() {
		return _isMultiValue;
	}
	
	public String associationName() {
		return _associationName;
	}
	
	public boolean isReadOnly() {
		return (_isReadOnly || location() == -1);
	}
	
	public int location() {
		if(_location == -1) {
			UniFieldDefinition def = _entity.fieldDefinitionForField(this);
			if(def != null)
				_location = def.location();
		}
		return _location;
	}
	
	private Number coerceToNumber(Object value) {
		if(value == null)
			return null;
	
		String str = value.toString();
		
		if(str == null || str.length() == 0)
			return null;
		
		Number result = null;
		CharacterSet cset = new CharacterSet("0123456789.");
		str = cset.stringContainingCharacters(str);
		try {
			if(_valueClass.equals(java.lang.Integer.class))
				result = new java.lang.Integer(str);
			if(_valueClass.equals(java.lang.Short.class))
				result = new java.lang.Short(str);
			if(_valueClass.equals(java.lang.Long.class))
				result = new java.lang.Long(str);
			if(_valueClass.equals(java.lang.Float.class))
				result = new java.lang.Float(str);
			if(_valueClass.equals(java.lang.Double.class))
				result = new java.lang.Double(str);
			if(_valueClass.equals(java.math.BigDecimal.class))
				result = new java.math.BigDecimal(str);
			if(_valueClass.equals(java.math.BigInteger.class))
				result = new java.math.BigInteger(str);
		} catch (Exception e) {
			UniLogger.universe.warn("coerceToNumber(): The value " + value + " seems not to be a number");
		}
		return result;
	}
	
	public Object coerceValue(Object value) {
		if(value == null)
			return null;
		if(_valueClass == null)
			return value.toString();
		
		if(value instanceof Collection) {
			List<Object> values = ListUtils.list();
			for(Object obj : ((Collection)value)) {
				values.add(this.coerceValue(obj));
			}
			return values;
		}
		
		if(_valueClass.isAssignableFrom(value.getClass()))
			return value;
		
		if(_valueClass.equals(java.lang.String.class))
			return value.toString();
		
		if(_valueClass.equals(java.lang.Integer.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.lang.Integer(((java.lang.Number) value).intValue());
		}
		if(_valueClass.equals(java.lang.Short.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.lang.Short(((java.lang.Number) value).shortValue());
		}
		if(_valueClass.equals(java.lang.Long.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.lang.Long(((java.lang.Number) value).longValue());
		}
		if(_valueClass.equals(java.lang.Float.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.lang.Float(((java.lang.Number) value).floatValue());
		}
		if(_valueClass.equals(java.lang.Double.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.lang.Double(((java.lang.Number) value).doubleValue());
				
		}
		if(_valueClass.equals(java.math.BigInteger.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.math.BigInteger(value.toString());
		}
		if(_valueClass.equals(java.math.BigDecimal.class)) {
			if(!(value instanceof java.lang.Number))
				return coerceToNumber(value);
			else
				return new java.math.BigDecimal(value.toString());
		}
		if(_valueClass.equals(java.util.Date.class)) {
			if(value == null || value.toString().length() == 0)
				return null;
			try {
				return this.dateFormat().parseObject(value.toString());
			} catch (ParseException e) {
				UniLogger.universe.warn("UniField coerceValue() could not parse date string with format '" + _dateFormat + "'", e);
				return null;
			}
		}
		if(_valueClass.equals(java.lang.Boolean.class)) {
			if(!(value instanceof java.lang.Boolean))
				return new java.lang.Boolean(((Boolean) value).booleanValue());
			else {
				String s = value.toString();
				if("true".equalsIgnoreCase(s) || "yes".equalsIgnoreCase(s) || "1".equals(s))
					return new java.lang.Boolean(true);
				else
					return new java.lang.Boolean(false);
			}
		}
		if(_valueClass.equals(java.lang.Byte.class)) {
		}
		if(_valueClass.equals(java.lang.Character.class)) {
		}
		if(_valueClass.equals(_NoBytes.getClass())) {
		}
		return value;

	}
	
	public UniEntity entity() {
		return _entity;
	}
	
	protected Class<?> valueClassFromEntity() {
		Class<?> entitClass = _entity.entityClass();
		Accessor getter = Accessor.newGetAccessor(entitClass, _key);
		return getter.getReturnType();
	}
	
	@Override
	public String toString() {
		return "field {columnName=" + _columnName + "; key=" + _key + "; isPrimaryKey=" + _isPrimaryKey + "; valueClass=" + _valueClass.getName() + "; dateFormat=" + _dateFormat + "; isMultiValue=" + _isMultiValue + "; location=" + location() + "; lock=" + _lockKey + "; isReadOnly:" +_isReadOnly + "}";
	}
		
	public boolean isAssociated() {
		return (_associationName != null && _associationName.length() > 0);
	}
	public boolean isDate() {
		for(int i = 0; i < DateClasses.length; i++)
			if(_valueClass.equals(DateClasses[i]))
				return true;
		return false;
	}
	
	public boolean isNumber() {
		for(int i = 0; i < NumberClasses.length; i++) {
			if(_valueClass.equals(NumberClasses[i]))
				return true;
		}
		return false;
	}
	public String convertToString(Object value) {
		if(value == null)
			return "";
		if(value instanceof Date && dateFormat() != null)
			return dateFormat().format(value);
		return value.toString();
	}
	
	public UniString convertToUniString(Object value) {
		if(value == null)
			return new UniString("");
		if(_valueClass.equals(_NoBytes.getClass()))
			return new UniString((byte[]) value);
		return new UniString(convertToString(value));
	}
	
	public boolean isLockKey() {
		return _lockKey;
	}
	
	public boolean isBlob() {
		return _valueClass.equals(_NoBytes.getClass());
	}
}

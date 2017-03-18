package universe;

import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;

import core.util.Accessor;
import core.util.ClassUtils;
import core.util.FieldAccess;
import core.util.ListUtils;
import universe.object.UniFaultingList;
import universe.util.UniLogger;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniEntity {

	public static final String OptimisticLock = "optimistic";
	public static final String PessimisticLock = "pessimistic";
	public static final String FileLock = "file";
	public static final String NoCache ="none";
	public static final String NormalCache = "normal";
	public static final String StatisticalCache = "statistical";
	public static final String DistantFutureCache = "distantfuture";
	
	public enum CacheStrategy {
		None, Normal, Statistical, DistantFuture
	}
	
	public enum LockingStrategy {
		Optimistic, Pessimistic, File
	}

	UniModel _model;
	String _filename;
	String _entityName;
	Class<?> _entityClass;
	UniField[] _fields;
	UniAssociation[] _associations;
	UniJoin[] _joins;
	UniPrimaryKeyGenerator _pkGenerator;
	LockingStrategy _lockingStrategy = LockingStrategy.Optimistic;
	List<UniFieldDefinition> _definitions;
	CacheStrategy _cacheStrategy = CacheStrategy.Normal;
	List<UniField> _dataFields;
	List<UniField> _virtualFields;
	List<String> _lockColumnNames;
	UniInheritance _inheritance;
	
	public UniEntity(UniModel model, Class<?> entityClass, String entityName, String filename, UniField[] fields) {
		this._entityClass = entityClass;
		this._entityName = entityName;
		this._filename = filename;
		this._model = model;
		this._fields = fields;
	}
	
	public UniEntity(UniModel model, Class<?> entityClass, String entityName, String filename) {
		this._entityClass = entityClass;
		this._entityName = entityName;
		this._filename = filename;
		this._model = model;
	}

	public void setModel(UniModel model) {
		this._model = model;
	}
	public UniModel model() {
		return _model;
	}
	public Object primaryKeyForObject(Object object) {
		UniField pkField = primaryKeyField();
		if(pkField != null) {
			return FieldAccess.Util.getValueForKey(object, pkField.key());
		}
		return null;
	}
	public void setPrimaryKeyForObject(Object pk, Object object) {
		UniField pkField = primaryKeyField();
		if(pkField != null) {
			 FieldAccess.Util.setValueForKey(object, pk, pkField.key());
		}
	}
	
	public String entityName() {
		return _entityName;
	}
	public Class<?> entityClass() {
		return _entityClass;
	}
	
	public UniField[] fields() {
		return _fields;
	}
		
	public String filename() {
		return _filename;
	}
	
	public UniAssociation[] associations() {
		return _associations;
	}
	
	public UniJoin[] joins() {
		return _joins;
	}
	
	public LockingStrategy lockingStrategy() {
		return _lockingStrategy;
	}
	
	public CacheStrategy cacheStrategy() {
		return _cacheStrategy;
	}
	
	public List<UniFieldDefinition> dictionaryDefinitions() {
		return _definitions;
	}
	
	public void setDictionaryDefinitions(List<UniFieldDefinition> definitions) {
		this._definitions = definitions;
	}

	public UniField primaryKeyField() {
		for(UniField field : fields()) {
			if(field._isPrimaryKey)
				return field;
		}
		return null;
	}
	
	public UniField fieldNamed(String fieldName) {
		for(UniField field : fields()) {
			if(field.key().equals(fieldName))
				return field;
		}
		return null;
	}
	
	public UniField fieldWithColumnName(String columnName) {
		for(UniField field : fields()) {
			if(field.columnName().equals(columnName))
				return field;
		}
		return null;
	}

	public UniAssociation associationNamed(String assocName) {
		for(UniAssociation assoc : associations()) {
			if(assoc._name.equals(assocName))
				return assoc;
		}
		return null;
	}
	
	public UniJoin joinNamed(String key) {
		for(UniJoin join : joins()) {
			if(join._key.equals(key))
				return join;
		}
		return null;
	}

	public UniInheritance inheritance() {
		return _inheritance;
	}
	
	public UniEntity parentEntity() {
		if(_inheritance != null)
			return _inheritance.parentEntity();
		return null;
	}
	
	public List<UniEntity> childEntities() {
		List<UniEntity> subs = ListUtils.list();
		for(UniEntity entity : _model.entities()) {
			if(entity == this) continue;
			if(entity.parentEntity() == this && !subs.contains(entity)) {
				subs.add(entity);
				subs.addAll(entity.childEntities());
			}
		}
		return subs;
	}
	
	public void initObject(Object object, Map<String, Object> row, UniContext context) throws Exception {
		for(UniField field : fields()) {
			if(field.isAssociated())
				continue;
			String columnName = field.columnName();
			String key = field.key();
			Object value = row.get(columnName);
			if(Accessor.newSetAccessor(object.getClass(), key) == null) {
				UniLogger.universe.warn("Instance of entity '" + field.entity().entityName() + "' does not have property '" + key + "'");
				continue;
			}
			try {
				FieldAccess.Util.setValueForKey(object, field.coerceValue(value), key);
			} catch (Exception e) {
				UniLogger.universe.error("UniEntity.initObject(): could not set value '" + value + "' to field '" + key + "'.\n value is " + 
			(value != null ? value.getClass().getName() : "null") + " expected value is " + field.valueClass().getName(), e);
				throw e;
			}
		}
		
		for(UniAssociation assoc : associations()) {
			_initAssociation(object, row, assoc);
		}
		
		context.didFetch(object);
	}
	
	private void _initAssociation(Object object, Map<String, Object> record, UniAssociation assoc) throws Exception {
		if(Accessor.newSetAccessor(object.getClass(), assoc.key()) == null) {
			UniLogger.universe.warn("Entity '" + assoc.entity().entityName() + "' does not have property '" + assoc.key() + "'");
			return;
		}
		if(!assoc.shouldPrefetch()) {
			UniFaultingList faultingList = new UniFaultingList(object, assoc);
			faultingList.setFault(true);
			FieldAccess.Util.setValueForKey(object, faultingList, assoc.key());
			return;
		}
		try {
			UniEntity entity = assoc.entity();
			Class<?> assocClass = assoc.associationClass();
			List<Map<String, String>> rows = (List<Map<String, String>>) record.get(assoc.key());
			List subRows = ListUtils.list();
			for(Map<String, String> row : rows) {
				Object obj = ClassUtils.newInstance(assocClass);
				for(String key : row.keySet()) {
					UniField f = entity.fieldNamed(key);
					FieldAccess.Util.setValueForKey(obj, f.coerceValue(row.get(key)), key);
				}
				subRows.add(obj);
			}
			FieldAccess.Util.setValueForKey(object, subRows, assoc.key());
		} catch (Exception e) {
			UniLogger.universe.error("_initAssociation(): could not set value '" + record.get(assoc.key()) + "' to field '" + assoc.key() + "'", e);
			throw e;
		}
	}
	
	public void initAssocation(Object object, List<Map<String, String>> rows,
			UniAssociation assoc) throws Exception {
		if(Accessor.newSetAccessor(object.getClass(), assoc.key()) == null) {
			UniLogger.universe.warn("initAssociation(): Entity '" + assoc.entity().entityName() + "' does not have property '" + assoc.key() + "'");
			return;
		}
		try {
			UniEntity entity = assoc.entity();
			Class<?> assocClass = assoc.associationClass();
			List subRows = ListUtils.list();
			for(Map<String, String> row : rows) {
				Object obj = ClassUtils.newInstance(assocClass);
				for(String key : row.keySet()) {
					UniField f = entity.fieldNamed(key);
					FieldAccess.Util.setValueForKey(obj, f.coerceValue(row.get(key)), key);
				}
				subRows.add(obj);
			}
			FieldAccess.Util.setValueForKey(object, subRows, assoc.key());
		} catch (Exception e) {
			UniLogger.universe.error("initAssociation(): could not set value '" + rows + "' to field '" + assoc.key() + "'", e);
			throw e;
		}
	}

	public List resolveAssocation(Object object, List<Map<String, String>> rows, UniAssociation assoc) throws Exception {
		if(Accessor.newSetAccessor(object.getClass(), assoc.key()) == null) {
			UniLogger.universe.warn("resolveAssocation(): Entity '" + assoc.entity().entityName() + "' does not have property '" + assoc.key() + "'");
			return null;
		}
		try {
			UniEntity entity = assoc.entity();
			Class<?> assocClass = assoc.associationClass();
			List subRows = ListUtils.list();
			for(Map<String, String> row : rows) {
				Object obj = ClassUtils.newInstance(assocClass);
				for(String key : row.keySet()) {
					UniField f = entity.fieldNamed(key);
					FieldAccess.Util.setValueForKey(obj, f.coerceValue(row.get(key)), key);
				}
				subRows.add(obj);
			}
			return subRows;
		} catch (Exception e) {
			UniLogger.universe.error("resolveAssocation(): could not set value '" + rows + "' to field '" + assoc.key() + "'", e);
			throw e;
		}
	}

	@Override
	public String toString() {
		return "{entityName=" + _entityName + "; filename=" + _filename + "; entityClass=" + _entityClass.getName() + "; fields=" + fieldsDescription() + "; inheritance=" + _inheritance + "}";
	}

	private String fieldsDescription() {
		List<String> descs = ListUtils.list();
		for(UniField field : fields()) {
			descs.add(field.toString());
		}
		return "(" + ListUtils.listToString(descs, ", ") + ")";
	}
	
	public UniPrimaryKeyGenerator pkGenerator() {
		return _pkGenerator;
	}
	public void setPkGenerator(UniPrimaryKeyGenerator pkGenerator) {
		this._pkGenerator = pkGenerator;
	}
	
	public int fieldNameToLocation(String fieldName) {
		UniFieldDefinition def = this.fieldDefinitionWithFieldName(fieldName);
		if(def != null)
			return def.location();
		UniField field = this.fieldNamed(fieldName);
		if(field != null)
			return field.location();
		return -1;
	}

	public UniFieldDefinition fieldDefinitionForField(UniField field) {
		return fieldDefinitionWithFieldName(field.columnName());
	}
	
	public UniFieldDefinition fieldDefinitionWithFieldName(String fieldName) {
		if(_definitions != null) {
			for(UniFieldDefinition def : _definitions) {
				if(fieldName.equals(def.fieldName())) {
					return def;
				}
						
			}
		}
		return null;
	}

	public List<UniField> dataFields() {
		if(_dataFields == null) {
			_dataFields = ListUtils.list();
			for(UniField field : this.fields()) {
				if(isDataField(field))
					_dataFields.add(field);
			}
		}
		return _dataFields;
	}
	
	public List<UniField> virtualFields() {
		if(_virtualFields == null) {
			_virtualFields = ListUtils.list();
			for(UniField field : this.fields()) {
				if(isVirtualField(field))
					_virtualFields.add(field);
			}
		}
		return _virtualFields;
	}

	private boolean isDataField(UniField field) {
		UniFieldDefinition def = this.fieldDefinitionForField(field);
		if(def == null)
			return false;
		return def.isDataField();
	}

	private boolean isVirtualField(UniField field) {
		UniFieldDefinition def = this.fieldDefinitionForField(field);
		if(def == null)
			return false;
		return def.isVirtualField();
	}
	
	public List<String> lockColumnNames() {
		if(_lockColumnNames == null) {
			_lockColumnNames = ListUtils.list();
			for(UniField field : dataFields()) {
				if(field.isLockKey())
					_lockColumnNames.add(field.columnName());
			}
		}
		return _lockColumnNames;
	}

	public boolean isAbstractClass() {
		return (_entityClass.getModifiers() & Modifier.ABSTRACT) != 0;
	}

	public List<? extends UniEntity> concreteDescendantEntities() {
		List<UniEntity> descendants = ListUtils.list();
		if(this.childEntities().size() > 0) {
			for(UniEntity child : this.childEntities()) {
				if(!child.isAbstractClass())
					descendants.add(child);
				descendants.addAll(child.concreteDescendantEntities());
			}
		}
		return descendants;
	}

	public UniPredicate additionalPredicateForInheritance() {
		return _inheritance.predicate();
	}
}

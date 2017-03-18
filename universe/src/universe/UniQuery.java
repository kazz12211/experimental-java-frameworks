package universe;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import core.util.FieldAccess;
import core.util.ListUtils;
import core.util.MapUtils;
import core.util.Perf;
import universe.command.AggregateFunction;
import universe.command.AggregateFunctions;
import universe.command.Count;
import universe.command.Select;
import universe.object.UniFaultingList;
import universe.util.UniDynArrayUtil;
import universe.util.UniLogger;
import universe.util.UniStringUtil;
import asjava.uniclientlibs.UniDataSet;
import asjava.uniclientlibs.UniDynArray;
import asjava.uniclientlibs.UniString;
import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniSelectList;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniQuery extends UniDatabaseAccess {

	
	public UniQuery(UniObjectsSession session) {
		super(session);
	}
	
	public void resolveAssociation(Object object, UniAssociation assoc) {
		UniEntity entity = session().model().entityForClass(object.getClass());
		try {
			List<Map<String, String>> rows = this._loadAssociation(object, entity, assoc);
			entity.initAssocation(object, rows, assoc);
		} catch (Exception e) {
			UniLogger.universe.error("UniQuery failed to resolve association '" + assoc.name() + "'", e);
		}
		
	}
		
	public List loadObjectsInAssociation(Object owner, UniAssociation assoc) {
		List objects = ListUtils.list();
		UniEntity entity = assoc.entity();
		try {
			List<Map<String, String>> rows = this._loadAssociation(owner, entity, assoc);
			List values = entity.resolveAssocation(owner, rows, assoc);
			if(values != null)
				objects.addAll(values);
		} catch (Exception e) {
			UniLogger.universe.error("UniQuery failed to load objects in association '" + assoc.key() + "'", e);
		}	
		return objects;
	}
	
	public List loadObjectsInJoin(Object owner, UniJoin join) {
		Perf p = Perf.newPerf("Objects in to many join " + join.key() + " loaded");
		List objects = ListUtils.list();
		UniEntity destinationEntity = join.destinationEntity();
		UniEntity sourceEntity = join.source();
		UniField sourceField = sourceEntity.fieldNamed(join.sourceKey());
		try {
			if(sourceField.isMultiValue()) {
				List values = (List) FieldAccess.Util.getValueForKey(owner, join.sourceKey());
				for(Object value : values) {
					Map<String, Object> fieldValues = MapUtils.map();
					fieldValues.put(join.destinationKey(), value);
					Object obj = UniContext.get().findOne(destinationEntity.entityClass(), fieldValues);
					if(obj != null)
						objects.add(obj);
				}
			} else {
				Object sourceKeyValue = FieldAccess.Util.getValueForKey(owner, join.sourceKey());
				Map<String, Object> fieldValues = MapUtils.map();
				fieldValues.put(join.destinationKey(), sourceKeyValue);
				List values = UniContext.get().executeQuery(destinationEntity.entityClass(), fieldValues);
				if(values != null)
					objects.addAll(values);
			}
		} catch (Exception e) {
			UniLogger.universe.error("UniQuery failed to load objects in join '" + join.key() + "'", e);
		}
		p.stop();
		return objects;
	}

	public Object loadObjectInJoin(Object owner, UniJoin join) {
		Perf p = Perf.newPerf("Object in to one join " + join.key() + " loaded");
		Object sourceKeyValue = FieldAccess.Util.getValueForKey(owner, join.sourceKey());
		UniEntity destinationEntity = join.destinationEntity();
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put(join.destinationKey(), sourceKeyValue);
		Object value = null;
		if(join.cacheDestination())
			value = session().cachedObject(destinationEntity, join.destinationKey(), sourceKeyValue);
		if(value == null)
			value = UniContext.get().findOne(destinationEntity.entityClass(), fieldValues);
		if(join.cacheDestination())
			session().cache(value, join.destinationKey(), sourceKeyValue);
		p.stop();
		return value;
	}
	
	public Object storedValueForToOneRelationship(Object owner, String key, UniContext context) {
		UniEntity entity = context.entityForObject(owner);
		if(entity == null)
			return null;
		UniJoin join = entity.joinNamed(key);
		if(join == null) {
			UniLogger.universe.debug(key + " is not defined in entity " + entity.entityName());
			return null;
		}
		if(join.isToMany()) {
			UniLogger.universe.debug("Join " + key + " in entity " + entity.entityName() +  " is not to-one");
			return null;
		}
		return loadObjectInJoin(owner, join);
	}



	public <T> List<T> executeQuery(UniQuerySpecification spec, UniContext uniContext) {
		UniEntity entity = spec.entity();
		List<UniEntity> entities = ListUtils.list();
		if(!entity.isAbstractClass())
			entities.add(entity);
		entities.addAll(entity.concreteDescendantEntities());


		List objects = ListUtils.list();
		for(UniEntity ent : entities) {
			UniQuerySpecification qs;
			UniPredicate newPredicate = null;
			if(ent.inheritance() != null) {
				UniPredicate additionalPredicate = ent.additionalPredicateForInheritance();
				UniPredicate pred = spec.predicate();
				if(pred == null && additionalPredicate != null) {
					newPredicate = additionalPredicate;
				} else if(pred != null && additionalPredicate != null) {
					List<UniPredicate> ps = ListUtils.list();
					ps.add(additionalPredicate);
					if(pred instanceof UniPredicate.And) {
						ps.addAll(((UniPredicate.And) pred).predicates());
					} else {
						ps.add(pred);
					}
					newPredicate = new UniPredicate.And(ps);
				}
			}
			qs = new UniQuerySpecification(ent, newPredicate != null ? newPredicate : spec.predicate());
			qs.setFetchHint(spec.fetchHint());
			qs.setForceRefetch(spec.forceRefetch());
			qs.setSortOrderings(spec.sortOrderings());
			qs.setStoredQueryNumber(spec.storedQueryNumber());
			qs.setListNumber(spec.listNumber());
			objects.addAll(_executeQuery(qs, uniContext));	
		}
		return objects;
	}

	private Map<String, Object> _readDataFields(UniFile file, UniString recordId, UniEntityID entityId) throws Exception {
		Map<String, Object> row = MapUtils.map();
		List<UniField> dataFields = entityId.entity().dataFields();
		Perf p = Perf.newPerf("Read data fields of file '" + file.getFileName() + "'");
		UniString record = file.read(recordId);
		p.stop();
		UniDynArray array = new UniDynArray(record);
		for(UniField dataField : dataFields) {
			if(dataField.isAssociated())	continue;
			
			int loc = dataField.location();
			UniString str = array.extract(loc);
			if(dataField.isMultiValue()) {
				UniDynArray uda = new UniDynArray(str);
				List<String> values = UniDynArrayUtil.toStringList(uda);
				row.put(dataField.columnName(), values);
			} else if(dataField.isBlob()) {
				row.put(dataField.columnName(), str.getBytes());
				
			} else {
				row.put(dataField.columnName(), UniStringUtil.coerceToString(str));
			}
		}
		return row;
	}
	
	private Map<String, Object> _readVirtualFields(UniFile file, UniString recordId, UniEntityID entityId) throws Exception {
		Map<String, Object> row = MapUtils.map();
		List<UniField> virtualFields = entityId.entity().virtualFields();
		Perf p = Perf.newPerf("Read virtual fields of file '" + file.getFileName() + "'");
		for(UniField virtualField : virtualFields) {
			UniString value = file.readNamedField(recordId, virtualField.columnName());
			row.put(virtualField.columnName(), UniStringUtil.coerceToString(value));
		}
		p.stop();
		return row;
	}
		
	private List<Map<String, Object>> _executeQuery(Select select, UniEntity entity) throws Exception {
		List<Map<String, Object>> results = ListUtils.list();
		Map<UniEntityID, Map<String, Object>> snapshots = MapUtils.map();
		
		UniFile file = null;
		UniCommand command = null;
		
		try {
			session().establishConnection();
			boolean forceRefetch = select.querySpecification().forceRefetch();
			Map<String, Integer> hint = select.querySpecification().fetchHint();
			int fetchLimitSize = (hint != null && hint.get(UniQuerySpecification.FetchLimitSize) != null) ? hint.get(UniQuerySpecification.FetchLimitSize).intValue() : -1;
			int fetchLimitStart = (hint != null && hint.get(UniQuerySpecification.FetchLimitStart) != null)	? hint.get(UniQuerySpecification.FetchLimitStart).intValue() : 0;
			
			file = session().getFile(select.filename());
			command = select.uniCommand(_session.commandGenerator());
			Perf p = Perf.newPerf("Got the result of command(" + command.getCommand() + ")");
			session().executeUniCommand(command);
			UniSelectList list = session().selectList(select.listNumber());
			int rowNo = 0;
			int count = 0;
			UniDynArray array = list.readList();
			//UniDynArray subArray = UniDynArrayUtil.subArray(array, fetchLimitStart+1, fetchLimitSize);
			int dcount = array.dcount();
			for(int i = 1; i <= dcount; i++) {
			//for(UniString recordId = list.next(); !list.isLastRecordRead(); recordId = list.next()) {
				if(rowNo++ < fetchLimitStart)
					continue;
				
				if(fetchLimitSize != -1 && ++count > fetchLimitSize)
					break;
				
				UniString recordId = array.extract(i);
				file.setRecordID(recordId);
				
				UniEntityID entityId = session().obtainEntityID(entity, UniStringUtil.coerceToString(recordId));
				Object cache = session().cachedObject(entityId);
				Map<String, Object> snapshot = session().snapshotForEntityID(entityId);
				
				Map<String, Object> row = MapUtils.map();
				if(cache != null && snapshot != null && !forceRefetch) {
					row.putAll(snapshot);
				} else {
					row.putAll(_readDataFields(file, recordId, entityId));
					row.putAll(_readVirtualFields(file, recordId, entityId));
				}
				row.put("@ID", UniStringUtil.coerceToString(recordId));
				
				for(UniAssociation assoc : entity.associations()) {
					if(assoc.shouldPrefetch()) {
						List<Map<String, String>> subRows = this._loadAssociations(file, recordId, entity, assoc);
						String assocKey = assoc.key();
						row.put(assocKey, subRows);
					}
				}
				results.add(row);
				if(entityId != null)
					snapshots.put(entityId, row);
			}
			p.stop();
			array = null;
		} finally {
			if(command != null)
				command.cancel();
		}
		
		session().recordSnapshots(snapshots);
		
		return results;
	}

	private <T> List<T> _executeQuery(UniQuerySpecification spec, UniContext uniContext) {
		Select select = new Select(spec);
		List<Map<String, Object>> rows = null;
		try {
			rows = this._executeQuery(select, spec.entity());
		} catch (Exception e) {
			UniLogger.universe.error("UniQuery failed to fetch", e);
		}
		List list = ListUtils.list();
		if(rows != null) {
			UniEntity entity = spec.entity();
			for(Map<String, Object> row : rows) {
				Object object = null;
				try {
					UniEntityID entityId = session().obtainEntityID(entity, row.get("@ID"));
					Object cache = session().cachedObject(entityId);
					if(cache != null && !spec.forceRefetch())
						object = cache;
					else
						object = entity.entityClass().newInstance();
					entity.initObject(object, row, uniContext);
					session().cache(object);
					for(UniJoin join : entity.joins()) {
						String key = join.key();
						Object value = null;
						if(join.isToMany()) {
							if(join.shouldPrefetch()) {
								value = this.loadObjectsInJoin(object, join);
							} else {
								value = new UniFaultingList(object, join);
								((UniFaultingList) value).setFault(true);
							}
						} else {
							if(join.shouldPrefetch()) {
								value = this.loadObjectInJoin(object, join);
							} else {
								value = null;
							}
						}
						FieldAccess.Util.setValueForKey(object, value, key);
					}
					list.add(object);
				} catch (Exception e) {
					UniLogger.universe.error("UniQuery: error while initializing object of '" + entity.entityClass().getName() + "' from database row of table '" + entity.entityName() + "'", e);
				}
			}
		}
		
		return list;
		
	}
	

	private List<Map<String, String>> _loadAssociations(UniFile file, UniString recordId, UniEntity entity, UniAssociation assoc) throws Exception {
		Perf p = Perf.newPerf("Load association " + assoc.name() + " of entity " + entity.entityName());
		Map<String, List<String>> valuesByField = MapUtils.map();
		List<UniField> fields = assoc.fields();
		int numValues = 0;
		for(UniField field : fields) {
			List<String> values = UniDynArrayUtil.readDynArray(file, recordId, field.columnName());
			if(numValues < values.size())
				numValues = values.size();
			valuesByField.put(field.key(), values);
		}
		List<Map<String, String>> subRows = ListUtils.list();
		for(int i = 0; i < numValues; i++) {
			Map<String, String> subRow = MapUtils.map();
			for(String key : valuesByField.keySet()) {
				List<String> values = valuesByField.get(key);
				if(values.size() <= i)
					subRow.put(key, null);
				else
					subRow.put(key, values.get(i));
			}
			subRows.add(subRow);
		}
		p.stop();
		return subRows;
	}


	private List<Map<String, String>> _loadAssociation(Object object, UniEntity entity, UniAssociation assoc) throws Exception {
		Object pk = entity.primaryKeyForObject(object);
		List<UniField> fields = assoc.fields();
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(pk.toString());
		Map<String, List<String>> valuesByField = MapUtils.map();
		int numValues = 0;
		List<Map<String, String>> subRows = ListUtils.list();

		UniFile file = null;
		Perf p = Perf.newPerf("Association '" + assoc.name() + "' loaded");
		
		try {
			file = session().getFile(entity.filename());
			
			for(UniField field : fields) {
				List<String> values = UniDynArrayUtil.readDynArray(file, rowSet, field.columnName());
				if(numValues < values.size())
					numValues = values.size();
				valuesByField.put(field.key(), values);
			}
	
			for(int i = 0; i < numValues; i++) {
				Map<String, String> subRow = MapUtils.map();
				for(String key : valuesByField.keySet()) {
					List<String> values = valuesByField.get(key);
					if(values.size() > i)
						subRow.put(key, values.get(i));
					else
						subRow.put(key, "");
				}
				subRows.add(subRow);
			}
		} finally {
			if(file != null) {
				session().closeFile(file);
			}
		}
		
		p.stop();
		
		return subRows;
	}
	
	
	// sum, min, max, avg
	public Map<String, Number> executeAggregateFunctions(String key, UniQuerySpecification spec, UniContext uniContext) throws Exception {
		AggregateFunctions functions = new AggregateFunctions(key, spec);
		UniCommand command = null;
		Map<String, Number> values = MapUtils.map();
		try {
			session().establishConnection();
			command = functions.uniCommand(session().commandGenerator());
			String response = session().executeUniCommand(command);
			String lines[] = response.split("\n");
			int numberLine = ResponseHandling.numberLine(lines);
			if(numberLine != -1) {
				String valueLine = lines[numberLine].trim();
				String columns[] = ResponseHandling.values(valueLine, 4);
				values.put("SUM", new Double(columns[0]));
				values.put("MAX", new Double(columns[1]));
				values.put("MIN", new Double(columns[2]));
				values.put("AVG", new Double(columns[3]));
			}
		} finally {
			if(command != null)
				command.cancel();
		}
		return values;
	}
	
	public Number executeAggregateFunction(String key, String funcName, UniQuerySpecification spec, UniContext uniContext) throws Exception {
		AggregateFunction function = new AggregateFunction(key, funcName, spec);
		UniCommand command = null;
		Number value = null;
		try {
			session().establishConnection();
			command = function.uniCommand(session().commandGenerator());
			String response = session().executeUniCommand(command);
			UniLogger.universe_dev.debug(response);
			String lines[] = response.split("\n");
			int numberLine = ResponseHandling.numberLine(lines);
			UniEntity entity = spec.entity();
			UniField field = entity.fieldNamed(key);
			try {
				Class<?> valueClass = field.valueClass();
				if(Number.class.isAssignableFrom(valueClass)) {
					Constructor<Number> constructor = (Constructor<Number>) valueClass.getConstructor(String.class);
					value = constructor.newInstance(lines[numberLine].trim());
				}
			} catch (Exception e) { 
				UniLogger.universe_command.error("Error instantiating number object", e); }
		} finally {
			if(command != null)
				command.cancel();
		}
		return value;
	}
	
	// count
	public Long executeCountFunction(UniQuerySpecification spec, UniContext uniContext) throws Exception {
		Count function = new Count(spec);
		UniCommand command = null;
		long count = -1;
		try {
			session().establishConnection();
			command = function.uniCommand(session().commandGenerator());
			String response = session().executeUniCommand(command);
			String lines[] = response.split("\n");
			int len = lines.length;
			String columns[] = lines[len - 1].trim().split(" ");
			try {
				Long value = new Long(columns[0]);
				count = value.longValue();
			} catch (Exception ignore) {}
		} finally {
			if(command != null)
				command.cancel();
		}
		return new Long(count);
	}
	

	public <T> T  findOne(UniQuerySpecification spec, UniContext uniContext) {
		List<T> objects = this.executeQuery(spec, uniContext);
		if(objects.size() > 0)
			return objects.get(0);
		return null;
	}
	
	public <T> T  findOne(Class<T> entityClass, Map<String, Object> fieldValues, UniContext uniContext) {
		UniEntity entity = session().model().entityForClass(entityClass);
		UniQuerySpecification spec = new UniQuerySpecification(entity, UniPredicate.Util.createPredicateFromFieldValues(fieldValues));
		return findOne(spec, uniContext);
	}
	
	public <T> T  find(Class<T> entityClass, Object primaryKey, UniContext uniContext) {
		UniEntity entity = session().model().entityForClass(entityClass);
		UniField pkField = entity.primaryKeyField();
		if(pkField == null) {
			UniLogger.universe.error("UniQuery: no primary key field in entity '" + entity.entityName() + "'");
			return null;
		}
		Map<String, Object> fieldValues = MapUtils.map();
		fieldValues.put(pkField.key(), primaryKey);
		return findOne(entityClass, fieldValues, uniContext);
	}
	
	static class ResponseHandling {
		private static boolean isDigit(char ch) {
			return (ch >= '0' && ch <= '9') || ch == '-' || ch == '.';
		}
		private static boolean isComma(char ch) {
			return ch == ',';
		}
		
		private static String[] values(String line, int numValues) {
			int index = 0;
			String[] values = new String[numValues];
			StringBuffer buffer = new StringBuffer();
			boolean begin = true;
			for(int i = 0; i < line.length(); i++) {
				char ch = line.charAt(i);
				if(isDigit(ch)) {
					if(!begin)
						begin = true;
					if(begin)
						buffer.append(ch);
				} else if(isComma(ch)) {
					continue;
				} else {
					if(begin) {
						begin = false;
						values[index++] = buffer.toString();
						buffer.setLength(0);
						if(index == numValues)
							break;
					}
				}
				if(i == line.length() - 1) {
					if(isDigit(ch) && begin) {
						values[index++] = buffer.toString();
						buffer.setLength(0);
						if(index == numValues)
							break;
					}
				}
			}
			return values;
		}
	
		private static int numberLine(String[] lines) {
			for(int i = lines.length - 1; i >= 0; i--) {
				if(lines[i].startsWith("=="))
					return i+1;
			}
			return -1;
		}
	}

}

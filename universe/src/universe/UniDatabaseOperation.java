package universe;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.FieldAccess;
import core.util.ListUtils;
import core.util.MapUtils;
import core.util.Perf;
import universe.util.UniLogger;
import asjava.uniclientlibs.UniDynArray;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;

/**
 * 
 * @author ktsubaki
 *
 */
public abstract class UniDatabaseOperation {

	Object _object;
	UniEntity _entity;
	long _millis;

	protected UniDatabaseOperation(Object object, UniEntity entity) {
		this._object = object;
		this._entity = entity;
		this._millis = System.currentTimeMillis();
	}
	
	public long timestamp() {
		return _millis;
	}
	public Object object() {
		return _object;
	}
	public UniEntity entity() {
		return _entity;
	}
	
	public String toString() {
		return this.getClass().getSimpleName() + " {object=" + _object + "; entity=" + _entity.entityName() + "; timestamp=" + new Date(_millis) + "}";
	}
	
	public abstract void executeInContext(UniContext uniContext) throws Exception;
	
	public UniUpdater getProcessorInContext(UniContext uniContext) {
		UniUpdater updater = uniContext.updateProcessorForEntity(_entity);
		return updater;
	}
	
	public static List<UniDatabaseOperation> sortedOperations(List<UniDatabaseOperation> operations) {
		int size = operations.size();
		List<UniDatabaseOperation> copy = ListUtils.list(size);
		for(int i = 0; i < size; i++) {
			copy.add(operations.get(i));
		}
		Collections.sort(copy, new Comparator<UniDatabaseOperation>() {
			@Override
			public int compare(UniDatabaseOperation arg0,
					UniDatabaseOperation arg1) {
				return arg0._millis - arg1._millis > 0 ? 1 : arg0._millis - arg1._millis < 0 ? -1 : 0;
			}});
		return copy;
	}
	
	protected Map<String, Object> writeObject(Object object, Object pk, UniContext uniContext, boolean update) throws UniFileException {
		UniDynArray record = new UniDynArray();
		Map<String, Object> row = MapUtils.map();
		for(UniField field : _entity.fields()) {
			if(field.isAssociated() || field.isReadOnly() || field.location() <= 0)	continue;
			if(field.isMultiValue()) {
				Object list = FieldAccess.Util.getValueForKey(object, field.key());
				int location = 1;
				if(list instanceof Collection) {
					for(Object item : (Collection<?>)list) {
						_insertValue(record, field.location(), location++, field.convertToUniString(item));
					}
				} else {
					_insertValue(record, field.location(), location, field.convertToUniString(list));
				}
			} else {
				Object value = FieldAccess.Util.getValueForKey(object, field.key());
				_insertValue(record, field.location(), field.convertToUniString(value));
				row.put(field.key(), (value));
			}
		}
		
		for(UniAssociation assoc : _entity.associations()) {
			Object objects = FieldAccess.Util.getValueForKey(object, assoc.key());
			for(UniField field : assoc.fields()) {
				if(field.isMultiValue() || objects instanceof Collection) {
					int location = 1;
					for(Object item : (Collection<?>)objects) {
						Object value = FieldAccess.Util.getValueForKey(item, field.key());
						_insertValue(record, field.location(), location++, field.convertToUniString(value));
					}
				} else {
					Object value = FieldAccess.Util.getValueForKey(objects, field.key());
					_insertValue(record, field.location(), field.convertToUniString(value));
				}
			}
		}
		
		UniUpdater updater = this.getProcessorInContext(uniContext);			
		UniFile file = updater.session().getFile(_entity.filename());
		if(file == null)
			throw new IllegalStateException("UniDatabaseOperation: could not get UniFile named '" + _entity.filename() + "'");
		//updater.session().commandHistory().record((update ? "Updating " : "Inserting ") + _entity.entityName() + "(recordId=" + pk + ") row: " + row);
		file.write(pk, record);
		//updater.session().commandHistory().record((update ? "Update " : "Insert ") + _entity.entityName() + "(recordId=" + pk + ") values: " + record.toString());
		return row;
	}

	private void _insertValue(UniDynArray record, int location, Object value) {
		UniLogger.universe_test.info("*** _insertValue(" + record + ", " + location + ", " + value);
		record.replace(location, value);
	}
	private void _insertValue(UniDynArray record, int location, int valueLocation, Object value) {
		record.replace(location, valueLocation, value);
	}
	
	public interface DatabaseOperationCallback {
		public void didInsert(Object object);
		public void didFetch(Object object);
		public void didUpdate(Object object, UniEntity entity);
		public void willUpdate(Object object, UniEntity entity);
		public void didDelete(Object object, UniEntity entity);
		public void willDelete(Object object, UniEntity entity);
		public boolean shouldDelete(Object object, UniEntity entity);
	}
	
	public static class Update extends UniDatabaseOperation {

		public Update(Object object, UniEntity entity) {
			super(object, entity);
		}

		@Override
		public void executeInContext(UniContext uniContext) throws Exception {
			Object pk = _entity.primaryKeyForObject(_object);
						
			UniUpdater updater = this.getProcessorInContext(uniContext);			

			uniContext.willUpdate(_object, _entity);
			
			Perf p = Perf.newPerf(_entity.entityName() + "(" + pk + ") updated");
			this.obtainLock(pk, updater);
			Map<String, Object> row = this.writeObject(_object, pk, uniContext, true);
			this.releaseLock();
			p.stop();
			
			uniContext.didUpdate(_object, _entity);

			
			UniEntityID entityId = updater.session().obtainEntityID(_entity, _entity.primaryKeyForObject(_object));
			if(entityId != null && updater != null) {
				updater.session().updateSnapshot(entityId, row);
			}
		}

		private void obtainLock(Object pk, UniUpdater updater) {
			if(_entity.lockingStrategy() == UniEntity.LockingStrategy.Optimistic) {
			} else if(_entity.lockingStrategy() == UniEntity.LockingStrategy.Pessimistic) {
			} else if(_entity.lockingStrategy() == UniEntity.LockingStrategy.File) {
			}
		}
		
		private void releaseLock() {
			if(_entity.lockingStrategy() == UniEntity.LockingStrategy.Optimistic) {
			} else if(_entity.lockingStrategy() == UniEntity.LockingStrategy.Pessimistic) {
			} else if(_entity.lockingStrategy() == UniEntity.LockingStrategy.File) {
			}
		}

	}

	public static class Delete extends UniDatabaseOperation {

		public Delete(Object object, UniEntity entity) {
			super(object, entity);
		}
		
		private void deleteDestination(Object object, UniEntity entity, UniUpdater updater, UniContext uniContext) throws UniFileException {
			UniFile file = updater.session().getFile(entity.filename());
			Object pk = entity.primaryKeyForObject(object);
			uniContext.willDelete(object, entity);
			file.deleteRecord(pk);
			uniContext.didDelete(object, entity);
		}

		private void deleteDestinations(Collection<?> objects, UniEntity entity, UniUpdater updater, UniContext uniContext) throws UniFileException {
			for(Object object : objects) {
				deleteDestination(object, entity, updater, uniContext);
			}
		}
		private void nullifyDestination(Object object, UniEntity entity, String sourceKey, UniUpdater updater, UniContext uniContext) throws UniFileException {
			Object pk = entity.primaryKeyForObject(object);
			uniContext.willUpdate(object, entity);
			FieldAccess.Util.setValueForKey(object, null, sourceKey);
			writeObject(object, pk, uniContext, true);
			uniContext.didUpdate(object, entity);
		}
		private void nullifyDestinations(Collection<?> objects, UniEntity entity, String sourceKey, UniUpdater updater, UniContext uniContext) throws UniFileException {
			for(Object object : objects) {
				nullifyDestination(object, entity, sourceKey, updater, uniContext);
			}
		}
		
		@Override
		public void executeInContext(UniContext uniContext) throws Exception {
			if(!uniContext.shouldDelete(_object, _entity))
				return;
			
			uniContext.willDelete(_object, _entity);
			

			UniUpdater updater = this.getProcessorInContext(uniContext);			
			Object pk = _entity.primaryKeyForObject(_object);
			Perf p = Perf.newPerf(_entity.entityName() + "(" + pk + ") deleted");
			UniFile file = updater.session().getFile(_entity.filename());
			file.deleteRecord(pk);
			
			UniJoin joins[] = _entity.joins();
			if(joins != null && joins.length > 0) {
				for(UniJoin join : joins) {
					Object value = FieldAccess.Util.getValueForKey(_object, join.key());					

					if(value == null)
						continue;
					if(join.ownsDestination()) {
						if(join.isToMany() && value instanceof Collection) {
							deleteDestinations((Collection<?>)value, join.destinationEntity(), updater, uniContext);
						} else {
							deleteDestination(value, join.destinationEntity(), updater, uniContext);
						}
					} else {
						if(join.isToMany() && value instanceof Collection) {
							nullifyDestinations((Collection<?>)value, join.destinationEntity(), join.sourceKey(), updater, uniContext);
						} else {
							nullifyDestination(value, join.destinationEntity(), join.sourceKey(), updater, uniContext);
						}
					}
				}
			}

			p.stop();
			//updater.session().commandHistory().record("Deleted " + _entity.entityName() + "(" + pk + ")");
			
			uniContext.didDelete(_object, _entity);
			
			UniEntityID entityId = updater.session().obtainEntityID(_entity, _entity.primaryKeyForObject(_object));
			if(entityId != null && updater != null)
				updater.session().forgetSnapshot(entityId);
		}

	}

	public static class Insert extends UniDatabaseOperation {

		public Insert(Object object, UniEntity entity) {
			super(object, entity);
		}

		@Override
		public void executeInContext(UniContext uniContext) throws Exception {
			Object pk = _entity.primaryKeyForObject(_object);
			if(pk == null) {
				Perf p = Perf.newPerf("Got primary key for object " + _object);
				pk = _entity.pkGenerator().newPrimaryKeyForObject(_object, uniContext);
				p.stop();
				_entity.setPrimaryKeyForObject(pk, _object);
			}
			
			if(pk == null) {
				UniLogger.universe.error("No primary key for object of entity '" + _entity.entityName() + "'");
				return;
			}
			
			Perf p = Perf.newPerf("Inserted " + _entity.entityName()  + "(" + pk + ")");
			if(_entity.inheritance() != null && _entity.parentEntity() != null && _entity.inheritance().isSingleTableInheritance()) {
				UniField discField = _entity.inheritance().discriminateField();
				String discValue = _entity.inheritance().discriminateValue();
				FieldAccess.Util.setValueForKey(_object, discValue, discField.key());
			}
						
			Map<String, Object> row = this.writeObject(_object, pk, uniContext, false);
			
			
			p.stop();
			
			uniContext.didInsert(_object);

			UniUpdater updater = this.getProcessorInContext(uniContext);			
			UniEntityID entityId = updater.session().obtainEntityID(_entity, _entity.primaryKeyForObject(_object));
			if(entityId != null && updater != null)
				updater.session().recordSnapshot(entityId, row);
		}
		

	}

	public static boolean isInsertOperation(UniDatabaseOperation operation) {
		return (operation instanceof Insert);
	}

	public static boolean isUpdateOperation(UniDatabaseOperation operation) {
		return (operation instanceof Update);
	}

	public static boolean isDeleteOperation(UniDatabaseOperation operation) {
		return (operation instanceof Delete);
	}

}

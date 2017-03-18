package universe;

import java.util.List;

import core.util.ListUtils;
import core.util.Perf;
import core.util.Selector;
import asjava.uniobjects.UniTransaction;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniUpdater extends UniDatabaseAccess {

	private boolean _isActive = false;
	private List<UniDatabaseOperation> _inserts = ListUtils.list();
	private List<UniDatabaseOperation> _updates = ListUtils.list();
	private List<UniDatabaseOperation> _deletes = ListUtils.list();
	private boolean _hasChanges = false;
	private UniTransaction _tx;
	private static Selector validateForSaveSelector = new Selector("validateForSave", new Class[]{UniContext.class});
	private static Selector validateForDeleteSelector = new Selector("validateForDelete", new Class[]{UniContext.class});
	
	public UniUpdater(UniObjectsSession session) {
		super(session);
	}
		
	public void begin() throws Exception {
		session().establishConnection();
		if(!_isActive) {
			/*
			tx = session().uniSession().transaction();
			if(!tx.isActive())
				tx.begin();
			isActive = tx.isActive();
			*/
			_isActive = true;
		}
	}

	public void commit() throws Exception {
		if(_isActive) {
			/*
			if(isActive && tx != null) {
				tx.commit();
			}
			tx = null;
			*/
			_isActive = false;
		}
		clear();
	}

	public void rollback() {
		if(_isActive) {
			/*
			try {
				if(isActive && tx != null)
					tx.rollback();
			} catch (UniException e) {
				UniLogger.universe.warn("UniUpdater failed to rollback", e);
			} finally {
				isActive = false;
				tx = null;
			}
			*/
			_isActive = false;
		}
		clear();
	}

	protected void clear() {
		_inserts.clear();
		_updates.clear();
		_deletes.clear();
		_hasChanges  = false;
	}
	
	public boolean hasChanges() {
		return _hasChanges;
	}
	
	private UniDatabaseOperation findOperation(UniDatabaseOperation opr, List<UniDatabaseOperation> operations) {
		for(UniDatabaseOperation operation : operations) {
			if(operation.object().equals(opr.object()) && operation.entity().equals(opr.entity()))
				return operation;
		}
		return null;
	}
	
	public void insert(Object object, UniContext uniContext) {
		UniEntity entity = uniContext.entityForObject(object);
		UniDatabaseOperation opr = new UniDatabaseOperation.Insert(object, entity);
		UniDatabaseOperation found = findOperation(opr, _inserts);
		if(found != null) {
			found._millis = System.currentTimeMillis();
			found._object = object;
		} else {
			_inserts.add(new UniDatabaseOperation.Insert(object, entity));
		}
		_hasChanges = true;
	}

	// remove insert operation of the object
	public void remove(Object object, UniContext uniContext) {
		UniEntity entity = uniContext.entityForObject(object);
		for(UniDatabaseOperation opr : _inserts) {
			if(opr.object().equals(object) && opr.entity().equals(entity)) {
				_inserts.remove(opr);
				break;
			}
		}
	}


	public void delete(Object object, UniContext uniContext) {
		UniEntity entity = uniContext.entityForObject(object);
		UniDatabaseOperation opr = new UniDatabaseOperation.Delete(object, entity);
		UniDatabaseOperation found = findOperation(opr, _deletes);
		if(found != null) {
			found._millis = System.currentTimeMillis();
			found._object = object;
		} else {
			_deletes.add(new UniDatabaseOperation.Delete(object, entity));
		}
		_hasChanges = true;
	}

	public void update(Object object, UniContext uniContext) {
		UniEntity entity = uniContext.entityForObject(object);
		UniDatabaseOperation opr = new UniDatabaseOperation.Update(object, entity);
		UniDatabaseOperation found = findOperation(opr, _updates);
		if(found != null) {
			found._millis = System.currentTimeMillis();
			found._object = object;
		} else {
			_updates.add(new UniDatabaseOperation.Update(object, entity));
		}
		_hasChanges = true;
	}

	private List<UniDatabaseOperation> orderedOperations() {
		List<UniDatabaseOperation> operations = ListUtils.list();
		operations.addAll(UniDatabaseOperation.sortedOperations(_inserts));
		operations.addAll(UniDatabaseOperation.sortedOperations(_updates));
		operations.addAll(UniDatabaseOperation.sortedOperations(_deletes));
		return operations;
	}
	
	public void executeDatabaseOperations(UniContext uniContext) {
		List<UniDatabaseOperation> orderedOperations = this.orderedOperations();
		if(uniContext.shouldFilterOperations())
			orderedOperations = uniContext.filterOperations(orderedOperations);
		if(uniContext.shouldOrderOperations())
			orderedOperations = uniContext.orderOperations(orderedOperations);
		int numInsert = 0;
		int numUpdate = 0;
		int numDelete = 0;
		int numOps = orderedOperations.size();
		Perf p = Perf.newPerf("Finished database operations");
		UniDatabaseOperation lastOperation = null;
		try {
			for(UniDatabaseOperation operation : orderedOperations) {
				lastOperation = operation;
				if(UniDatabaseOperation.isInsertOperation(operation)) {
					if(Selector.objectRespondsTo(operation.object(), validateForSaveSelector)) {
						validateForSaveSelector.invoke(operation.object(), new Object[]{uniContext});
					}
					operation.executeInContext(uniContext);
					numInsert++;
				} else if(UniDatabaseOperation.isUpdateOperation(operation)) {
					if(Selector.objectRespondsTo(operation.object(), validateForSaveSelector)) {
						validateForSaveSelector.invoke(operation.object(), new Object[]{uniContext});
					}
					operation.executeInContext(uniContext);
					numUpdate++;
				} else if(UniDatabaseOperation.isDeleteOperation(operation)) {
					if(Selector.objectRespondsTo(operation.object(), validateForDeleteSelector)) {
						validateForDeleteSelector.invoke(operation.object(), new Object[]{uniContext});
					}
					operation.executeInContext(uniContext);
					numDelete++;
				}
			}
		} catch (UniValidationException invalid) {
			invalid.setDatabaseOperation(lastOperation);
			throw invalid;
		} catch (Exception e) {
			throw new IllegalStateException(lastOperation.toString(), e);
		} finally {
			p.stop("Total " + numOps + " operations (inserts=" + numInsert + ", updates=" + numUpdate + ", deletes=" + numDelete);
		}
	}


}

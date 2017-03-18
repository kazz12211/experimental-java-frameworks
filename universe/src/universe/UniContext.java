package universe;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import core.util.Assert;
import core.util.ClassUtils;
import core.util.ConservertiveThreadLocal;
import core.util.Delegate;
import core.util.ListUtils;
import core.util.MapUtils;
import core.util.Selector;
import universe.UniDatabaseOperation.DatabaseOperationCallback;
import universe.util.UniLogger;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniContext implements DatabaseOperationCallback {

	static ConservertiveThreadLocal<UniContext> _threadLocal = new ConservertiveThreadLocal<UniContext>() {
		@Override
		protected UniContext create() {
			return new UniContext();
		}};
		
	Map<UniModel, UniObjectsSession> _uniObjectsSessions = MapUtils.map();
	Map<UniModel, UniQuery> _queryProcessors = MapUtils.map();
	Map<UniModel, UniUpdater> _updateProcessors = MapUtils.map();
    UniContextGroup _contextGroup;
	
	protected UniContext() {
		Map<String, UniModel> models = UniModelGroup.models();
		for(UniModel model : models.values()) {
			UniObjectsSession session = new UniObjectsSession(model);
			_uniObjectsSessions.put(model, session);
			_queryProcessors.put(model, new UniQuery(session));
			_updateProcessors.put(model,  new UniUpdater(session));
		}
	}
	
	public static void bind(UniContext context) {
		_threadLocal.set(context);
	}
	
	public static void unbind() {
		_threadLocal.set(null);
	}
	
	public static UniContext get() {
		UniContext context = _threadLocal.get(true);
		Assert.that(context != null, "get() on thread local with no UniContext bound");
		return context;
	}
	
	public static UniContext peek() {
		UniContext context = _threadLocal.get();
		return context;
	}
	
	public static UniContext createContext() {
		return new UniContext();
	}
	
	public static void bindNewContext() {
		UniContext ctx = peek();
		if(ctx == null)
			bindContext(createContext());
	}
	
	public static void bindContext(UniContext context) {
		bind(context);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T create(String className) {
		return (T)create(ClassUtils.classForName(className));
	}

	@SuppressWarnings("unchecked")
	public <T> T create(Class<T> aClass) {
		T o = (T)ClassUtils.newInstance(aClass);
		Assert.that(o != null, "Unable to create instance of class: " + aClass.getName());
		this.recordForInsert(o);
		return o;
	}
	
	public void recordForInsert(Object object) {
		UniEntity entity = entityForObject(object);
		UniUpdater tx = updateProcessorForEntity(entity);
		tx.insert(object, this);
	}
	
	public void deleteObject(Object object) {
		UniEntity entity = entityForObject(object);
		UniUpdater tx = updateProcessorForEntity(entity);
		tx.delete(object, this);
	}
	
	public void updateObject(Object object) {
		UniEntity entity = entityForObject(object);
		UniUpdater tx = updateProcessorForEntity(entity);
		tx.update(object, this);
	}
	
	public UniQuery queryProcessorForEntity(UniEntity entity) {
		return queryProcessorForModel(entity.model());
	}

	public UniQuery queryProcessorForModel(UniModel model) {
		UniQuery query = _queryProcessors.get(model);
		if(query == null) {
			UniObjectsSession session = sessionForModel(model);
			query = new UniQuery(session);
			_queryProcessors.put(model, query);
		}
		return query;
	}
	
	public UniObjectsSession sessionForObject(Object object) {
		UniEntity entity = entityForObject(object);
		if(entity != null)
			return sessionForEntity(entity);
		return null;
	}
	
	public UniObjectsSession sessionForEntity(UniEntity entity) {
		UniModel model = entity.model();
		return sessionForModel(model);
	}

	public UniObjectsSession sessionForModel(UniModel model) {
		UniObjectsSession session = _uniObjectsSessions.get(model);
		return session;
	}
	

	public UniUpdater updateProcessorForEntity(UniEntity entity) {
		return updateProcessorForModel(entity.model());
	}
	
	public UniUpdater updateProcessorForModel(UniModel model) {
		UniUpdater tx = _updateProcessors.get(model);
		if(tx == null) {
			UniObjectsSession session = sessionForModel(model);
			tx = new UniUpdater(session);
			_updateProcessors.put(model,  tx);
		}
		return tx;
	}
	
	public List<?> executeQuery(UniQuerySpecification spec) {
		UniEntity entity = spec.entity();
		if(entity == null)
			entity = entityForClass(spec.entityClass());
		UniQuery query = queryProcessorForEntity(entity);
		return query.executeQuery(spec, this);
	}
	
	public List<?> executeQuery(Class<?> entityClass, Map<String, Object> fieldValues) {
		UniPredicate predicate = UniPredicate.Util.createPredicateFromFieldValues(fieldValues);
		UniEntity entity = entityForClass(entityClass);
		UniQuerySpecification spec = new UniQuerySpecification(entity, predicate);
		return executeQuery(spec);
	}
	
	public Object findOne(UniQuerySpecification spec) {
		List<?> results = this.executeQuery(spec);
		if(results.size() > 0)
			return results.get(0);
		return null;
	}
	
	public Object findOne(Class<?> entityClass, Map<String, Object> fieldValues) {
		List<?> results = this.executeQuery(entityClass, fieldValues);
		if(!ListUtils.nullOrEmpty(results))
			return results.get(0);
		return null;
	}
	
	public Object find(Class<?> entityClass, Object primaryKey) {
		UniEntity entity = entityForClass(entityClass);
		UniQuery query = queryProcessorForEntity(entity);
		return query.find(entityClass, primaryKey, this);
	}
	
	public Object storedValueForToOneRelationship(Object owner, String key) {
		UniEntity entity = entityForObject(owner);
		UniQuery query = queryProcessorForEntity(entity);
		return query.storedValueForToOneRelationship(owner, key, this);
	}

	public UniEntity entityForClass(Class<?> entityClass) {
		return UniModelGroup.defaultGroup().entityForClass(entityClass);
	}
	
	public UniEntity entityForObject(Object object) {
		Class<?> entityClass = object.getClass();
		return entityForClass(entityClass);
	}
		
	public void saveChanges() throws Exception {
		for(UniUpdater tx : _updateProcessors.values()) {
			if(!tx.hasChanges())	continue;
			tx.begin();
			try {
				tx.executeDatabaseOperations(this);
				tx.commit();
			} catch (UniValidationException invalid) {
				tx.rollback();
				throw invalid;
			} catch (IllegalStateException e) {
				tx.rollback();
				throw e;
			} 
		}
	}
	
	public void saveUpates(Object...objects) throws Exception {
		for(Object object : objects) {
			updateObject(object);
		}
		saveChanges();
	}
	
	public UniEntityID entityIDForObject(Object object) {
		UniEntity entity = entityForObject(object);
		if(entity != null) {
			UniObjectsSession session = sessionForEntity(entity);
			return session.obtainEntityID(entity, entity.primaryKeyForObject(object));
		}
		return null;
	}
		
	public Object cachedObject(Class<?> entityClass, Object primaryKey) {
		UniEntity entity = entityForClass(entityClass);
		Object object = sessionForEntity(entity).cachedObject(entity, primaryKey);
		if(object == null) {
			object = find(entityClass, primaryKey);
		}
		return object;
	}
	
	public void cache(Object object) {
		UniEntity entity = entityForObject(object);
		sessionForEntity(entity).cache(object);
	}
	
	public void cacheObjects(Collection<?> objects) {
		for(Object object : objects) {
			UniEntity entity = entityForObject(object);
			sessionForEntity(entity).cache(object);
		}
	}
	
	public Object reload(Object object) {
		UniEntityID entityId = entityIDForObject(object);
		if(sessionForEntity(entityId.entity()).snapshotForEntityID(entityId) != null)
			sessionForEntity(entityId.entity()).forgetSnapshot(entityId);
		if(objectFromCache(entityId) != null)
			sessionForEntity(entityId.entity()).removeCache(entityId);
		return find(object.getClass(), entityId.entity().primaryKeyForObject(object));
	}
		

	public Object objectFromCache(UniEntityID entityId) {
		UniEntity entity = entityId.entity();
		UniObjectsSession session = sessionForEntity(entity);
		return session.cachedObject(entityId);
	}
	
	public Object objectFromSnapshot(UniEntityID entityId) throws Exception {
		UniEntity entity = entityId.entity();
		Map<String, Object> snapshot = sessionForEntity(entity).snapshotForEntityID(entityId);
		Object object = ClassUtils.newInstance(entity.entityClass());
		entity.initObject(object, snapshot, this);
		return object;
	}

	@Override
	protected void finalize() throws Throwable {
		closeSessions();
		super.finalize();
	}

	private void closeSessions() {
		for(UniObjectsSession session : _uniObjectsSessions.values()) {
			session.disconnect();
		}
	}
	
	public void cacheEntityOnServer(UniEntity entity) {
		UniObjectsSession session = sessionForEntity(entity);
		session.cacheEntityOnServer(entity);
	}

	/*
	 * UniDatabaseOperationCallback
	 * 
	 */
	
	private static Selector awakeFromInsertSelector = new Selector("awakeFromInsert", new Class[]{UniContext.class});
	private static Selector awakeFromFetchSelector = new Selector("awakeFromFetch", new Class[]{UniContext.class});

	@Override
	public void didInsert(Object object) {
		if(Selector.objectRespondsTo(object, awakeFromInsertSelector)) {
			awakeFromInsertSelector.safeInvoke(object, new Object[]{this});
		}
		if(_delegate.respondsTo("awakeFromInsert")) {
			try {
				_delegate.perform("awakeFromInsert", new Object[]{object, this});
			} catch (Exception e) {
				UniLogger.universe.error("awakeFromInsert delegate method failed", e);
			}
		}
	}

	@Override
	public void didFetch(Object object) {
		if(Selector.objectRespondsTo(object, awakeFromFetchSelector)) {
			awakeFromFetchSelector.safeInvoke(object, new Object[]{this});
		}
		if(_delegate.respondsTo("awakeFromFetch")) {
			try {
				_delegate.perform("awakeFromFetch", new Object[]{object, this});
			} catch (Exception e) {
				UniLogger.universe.error("awakeFromFetch delegate method failed", e);
			}
		}
	}
	
	@Override
	public void didUpdate(Object object, UniEntity entity) {
		if(_delegate.respondsTo("didUpdate")) {
			try {
				_delegate.perform("didUpdate", new Object[]{object, entity, this});
			} catch (Exception e) {
				UniLogger.universe.error("didUpdate delegate method failed", e);
			}
		}
	}

	@Override
	public void willUpdate(Object object, UniEntity entity) {
		if(_delegate.respondsTo("willUpdate")) {
			try {
				_delegate.perform("willUpdate", new Object[]{object, entity, this});
			} catch (Exception e) {
				UniLogger.universe.error("willUpdate delegate method failed", e);
			}
		}
		
	}

	@Override
	public void didDelete(Object object, UniEntity entity) {
		if(_delegate.respondsTo("didDelete")) {
			try {
				_delegate.perform("didDelete", new Object[]{object, entity, this});
			} catch (Exception e) {
				UniLogger.universe.error("didDelete delegate method failed", e);
			}
		}
	}

	@Override
	public void willDelete(Object object, UniEntity entity) {
		if(_delegate.respondsTo("willDelete")) {
			try {
				_delegate.perform("willDelete", new Object[]{object, entity, this});
			} catch (Exception e) {
				UniLogger.universe.error("willDelete delegate method failed", e);
			}
		}
	}

	@Override
	public boolean shouldDelete(Object object, UniEntity entity) {
		if(_delegate.respondsTo("shouldDelete")) {
			try {
				return _delegate.booleanPerform("shouldDelete", object, entity, this);
			} catch (Exception e) {
				UniLogger.universe.error("willDelete delegate method failed", e);
			}
		}
		return true;
	}
	
	public boolean shouldOrderOperations() {
		if(_delegate.respondsTo("shouldOrderOperations")) {
			try {
				return _delegate.booleanPerform("shouldOrderOperations", this);
			} catch (Exception e) {
				UniLogger.universe.error("shouldOrderOperation delegate method failed", e);
			}
		}
		return false;
	}
	public boolean shouldFilterOperations() {
		if(_delegate.respondsTo("shouldFilterOperations")) {
			try {
				return _delegate.booleanPerform("shouldFilterOperations", this);
			} catch (Exception e) {
				UniLogger.universe.error("shouldFilterOperations delegate method failed", e);
			}
		}
		return false;
	}
	
	public List<UniDatabaseOperation> orderOperations(List<UniDatabaseOperation> operations) {
		if(_delegate.respondsTo("orderOperations")) {
			try {
				return (List<UniDatabaseOperation>) _delegate.perform("orderOperations", operations, this);
			} catch (Exception e) {
				UniLogger.universe.error("orderOperations delegate method failed", e);
			}
		}
		return operations;
	}
	
	public List<UniDatabaseOperation> filterOperations(List<UniDatabaseOperation> operations) {
		if(_delegate.respondsTo("orderOperations")) {
			try {
				return (List<UniDatabaseOperation>) _delegate.perform("filterOperations", operations, this);
			} catch (Exception e) {
				UniLogger.universe.error("filterOperations delegate method failed", e);
			}
		}
		return operations;
	}
	
	/*
	 * Delegates
	 */
	
	public interface UniContextDelegate {
		public void awakeFromInsert(Object object, UniContext uniContext);
		public void awakeFromFetch(Object object, UniContext uniContext);
		public void willUpdate(Object object, UniEntity entity, UniContext uniContext);
		public void didUpdate(Object object, UniEntity entity, UniContext uniContext);
		public void willDelete(Object object, UniEntity entity, UniContext uniContext);
		public void didDelete(Object object, UniEntity entity, UniContext uniContext);
		public boolean shouldDelete(Object object, UniEntity entity, UniContext uniContext);
		public boolean shouldOrderOperations(UniContext uniContext);
		public List<UniDatabaseOperation> orderOperations(List<UniDatabaseOperation> operations, UniContext uniContext);
		public boolean shouldFilterOperations(UniContext uniContext);
		public List<UniDatabaseOperation> filterOperations(List<UniDatabaseOperation> opearations, UniContext uniContext);
	}
	
	private Delegate _delegate = new Delegate(UniContextDelegate.class);
	
	public void setDelegate(Object delegateObject) {
		_delegate.setDelegate(delegateObject);
	}

	public static void bindNewContext(String groupName) {
		bindContext(createContext(), groupName);
	}

	static void bindContext(UniContext ctx, String groupName) {
		ctx._contextGroup = associateContextGroup(ctx, groupName);
		bind(ctx);
	}
	
	static Map<String, UniContextGroup> _ContextGroups = MapUtils.map();
	
	static UniContextGroup associateContextGroup(UniContext ctx, String name) {
		gcEmptyContextGroups();
		
		if(name == null)
			return new UniContextGroup(null);
		
		synchronized(_ContextGroups) {
			UniContextGroup group = _ContextGroups.get(name);
			if(group == null) {
				group = new UniContextGroup(name);
				_ContextGroups.put(name, group);
			}
			group.registerMember(ctx);
			return group;
		}
	}
	
	static void gcEmptyContextGroups() {
		synchronized(_ContextGroups) {
			Iterator <Map.Entry<String, UniContextGroup>> iter = _ContextGroups.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry<String, UniContextGroup> e = iter.next();
				if(e.getValue()._members.size() == 0) iter.remove();
			}
		}
	}
	
	static class UniContextGroup {
        String _name;
        int _saveCount;
        WeakHashMap <UniContext, Object> _members = new WeakHashMap();
		
        UniContextGroup(String name) {
        	_name = name;
        }
        
        void registerMember(UniContext member) {
        	_members.put(member, true);
        }
        Iterator <UniContext> memberIterator() {
        	return _members.keySet().iterator();
        }
	}

	// removed pre-inserted object
	public void remove(Object object) {
		UniEntity entity = entityForObject(object);
		UniUpdater tx = updateProcessorForEntity(entity);
		tx.remove(object, this);
	}

}

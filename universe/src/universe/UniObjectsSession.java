package universe;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import universe.command.Select;
import universe.util.UniDictionaryUtil;
import universe.util.UniLogger;
import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniDictionary;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniJava;
import asjava.uniobjects.UniSelectList;
import asjava.uniobjects.UniSession;
import asjava.uniobjects.UniSessionException;
import core.util.ListUtils;
import core.util.MapUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniObjectsSession {

	protected UniObjectsConnection _connection;
	protected UniJava _uniJava;
	protected UniSession _uniSession;
	protected UniModel _model;
	protected UniCommandGeneration _commandGenerator;
	//protected CommandHistory _commandHistory = new CommandHistory();
	private Map<UniEntityID, UniSnapshot> _snapshots;
	private List<UniEntityID> _entityIds;
	protected UniEntityCache _entityCache;
	
	public UniObjectsSession(UniModel model) {
		_model = model;
//		_connection = new UniObjectsConnection(model);
		_connection = UniObjectsConnection.get(model);
		_commandGenerator = new UniCommandGeneration(this);
	}

	public UniModel model() {
		return _model;
	}

	public boolean isConnected() {
		return _connection != null && _connection.isConnected();
	}
	
	public UniObjectsConnection connection() {
		return _connection;
	}
	
	public UniSession establishConnection() throws Exception {
		int status = _connection.establish();
		if(status == 0) {
			_snapshots = MapUtils.map();
			_entityIds = ListUtils.list();
			_entityCache = new UniEntityCache();
			_loadEntityDefinitions();
		}
		
		// Emergency
		if(_snapshots == null)
			_snapshots = MapUtils.map();
		if(_entityIds == null)
			_entityIds = ListUtils.list();
		if(_entityCache == null)
			_entityCache = new UniEntityCache();
		_loadEntityDefinitions();

		return _connection.uniSession();
	}
	
	public void disconnect() {
		if(this.isConnected()) {
			_connection.disconnect();
			_snapshots.clear();
			_snapshots = null;
			_entityIds.clear();
			_entityIds = null;
			_entityCache.clear();
			// _commandGenerator = null;
		}
	}
	
	public UniSession uniSession() {
		return _connection.uniSession();
	}

	public void executeUniCommand(String commandString, String...replies) throws Exception {
		_connection.executeUniCommand(commandString, replies);
		//_commandHistory.record(commandString);
	}		

	public String executeUniCommand(String commandString) throws Exception {
		String response = _connection.executeUniCommand(commandString);
		//_commandHistory.record(commandString);
		return response;
	}
	
	public String executeUniCommand(UniCommand command) throws Exception {
		String response = _connection.executeUniCommand(command);
		//_commandHistory.record(command.getCommand());
		return response;
	}
	
	public String executeUniCommandUntilComplete(String commandString) throws Exception {
		String response = _connection.executeUniCommandUntilComplete(commandString);
		//_commandHistory.record(commandString);
		return response;
	}
	
	public void storeBasic(UniObjectsBasic basic) throws Exception {
		_connection.storeBasic(basic);
	}
	
	public boolean isBasicStored(UniObjectsBasic basic) {
		return _connection.isStored(basic);
	}
	
	public boolean isBasicCompiled(UniObjectsBasic basic) {
		return _connection.isCompiled(basic);
	}
	
	public void compileBasic(UniObjectsBasic basic) throws Exception {
		_connection.compileBasic(basic);
	}
	
	public String executeBasic(UniObjectsBasic basic, boolean recompile, String...args) throws Exception {
		String response = _connection.executeBasic(basic, recompile, args);
		return response;
	}
	public String executeBasic(UniObjectsBasic basic) throws Exception {
		return executeBasic(basic, false, (String[])null);
	}
	public String executeBasic(String storageName, String programName) throws Exception {
		UniObjectsBasic basic = new UniObjectsBasic();
		basic.setProgramName(programName);
		basic.setStorageName(storageName);
		return executeBasic(basic);
	}
	
	// Experimental
	public String executeBasic(UniObjectsBasic basic, UniQuerySpecification spec, boolean recompile, String...args) throws Exception {
		this.establishConnection();
		Select select = new Select(spec);
		UniCommand command = select.uniCommand(_commandGenerator);
		this.executeUniCommand(command);
		String response = this.executeBasic(basic, recompile, args);
		return response;
	}
	// Experimental
	public String executeBasic(UniObjectsBasic basic, UniQuerySpecification spec) throws Exception {
		return executeBasic(basic, spec, false, (String[])null);
	}
	// Experimental
	public String executeBasic(UniObjectsBasic basic, String selectListName, boolean recompile, String...args) throws Exception {
		if(selectListName != null)
			this.executeUniCommand("GET.LIST " + selectListName);
		String response = this.executeBasic(basic, recompile, args);
		return response;
	}
	// Experimental
	public String executeBasic(UniObjectsBasic basic, String selectListName) throws Exception {
		return executeBasic(basic, selectListName, false, (String[])null);
	}
	
	// Experimental
	public int getSavedQuery(String selectListName, int listNumber) throws Exception {
		return UniStoredQuery.executeSavedQuery(this, selectListName, listNumber);
	}
	// Experimental
	public int intersectQuery(String selectListName1, String selectListName2, int listNumber) throws Exception {
		int num1 = getSavedQuery(selectListName1, 1);
		int num2 = getSavedQuery(selectListName2, 2);
		this.executeUniCommand("MERGE.LIST " + num1 + " INTERSECT " + num2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int intersectQuery(int listNumber1, int listNumber2, int listNumber) throws Exception {
		this.executeUniCommand("MERGE.LIST " + listNumber1 + " INTERSECT " + listNumber2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int unionQuery(String selectListName1, String selectListName2, int listNumber) throws Exception {
		int num1 = getSavedQuery(selectListName1, 1);
		int num2 = getSavedQuery(selectListName2, 2);
		this.executeUniCommand("MERGE.LIST " + num1 + " UNION " + num2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int unionQuery(int listNumber1, int listNumber2, int listNumber) throws Exception {
		this.executeUniCommand("MERGE.LIST " + listNumber1 + " UNION " + listNumber2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int diffQuery(String selectListName1, String selectListName2, int listNumber) throws Exception {
		int num1 = getSavedQuery(selectListName1, 1);
		int num2 = getSavedQuery(selectListName2, 2);
		this.executeUniCommand("MERGE.LIST " + num1 + " DIFF " + num2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int diffQuery(int listNumber1, int listNumber2, int listNumber) throws Exception {
		this.executeUniCommand("MERGE.LIST " + listNumber1 + " DIFF " + listNumber2 + " TO " + listNumber);
		return listNumber;
	}
	// Experimental
	public int intersectQueries(String...listNames) throws Exception {
		return UniStoredQuery.intersect.execute(this, listNames);
	}
	// Experimental
	public int unionQueries(String...listNames) throws Exception {
		return UniStoredQuery.union.execute(this, listNames);
	}
	// Experimental
	public int diffQueries(String...listNames) throws Exception {
		return UniStoredQuery.diff.execute(this, listNames);
	}
	// Experimental
	public int intersectQueriesAfterStoredQuery(int lastQuery, String...listNames) throws Exception {
		return UniStoredQuery.intersect.execute(this, lastQuery, listNames);
	}
	// Experimental
	public int unionQueriesAfterStoredQuery(int lastQuery, String...listNames) throws Exception {
		return UniStoredQuery.union.execute(this, lastQuery, listNames);
	}
	// Experimental
	public int diffQueriesAfterStoredQuery(int lastQuery, String...listNames) throws Exception {
		return UniStoredQuery.diff.execute(this, lastQuery, listNames);
	}
	// Experimental
	public int intersectQueries(List<String> listNames) throws Exception {
		return UniStoredQuery.intersect.execute(this, listNames);
	}
	// Experimental
	public int unionQueries(List<String> listNames) throws Exception {
		return UniStoredQuery.union.execute(this, listNames);
	}
	// Experimental
	public int diffQueries(List<String> listNames) throws Exception {
		return UniStoredQuery.diff.execute(this, listNames);
	}
	// Experimental
	public int intersectQueriesAfterStoredQuery(int lastQuery, List<String> listNames) throws Exception {
		return UniStoredQuery.intersect.execute(this, lastQuery, listNames);
	}
	// Experimental
	public int unionQueriesAfterStoredQuery(int lastQuery, List<String> listNames) throws Exception {
		return UniStoredQuery.union.execute(this, lastQuery, listNames);
	}
	// Experimental
	public int diffQueriesAfterStoredQuery(int lastQuery, List<String> listNames) throws Exception {
		return UniStoredQuery.diff.execute(this, lastQuery, listNames);
	}
	
	public int storeQuery(UniQuerySpecification spec) throws Exception {
		Select select = new Select(spec);
		UniCommand command = select.uniCommand(_commandGenerator);
		this.executeUniCommand(command);
		return spec.listNumber();
	}
	
	
	public UniFile getFile(String filename) {
		return _connection.getFile(filename);
	}
	public UniDictionary getDict(String filename) {
		return _connection.getDict(filename);
	}
	public void closeFile(UniFile file) {
		_connection.closeFile(file);
	}
	public void closeDict(UniDictionary dict) {
		_connection.closeDict(dict);
	}
	public UniSelectList selectList(int num) throws UniSessionException {
		return _connection.selectList(num);
	}

	/*
	public CommandHistory commandHistory() {
		return _commandHistory;
	}
	*/

	public void recordSnapshot(UniEntityID entityId, Map<String, Object> row) {
		UniLogger.universe_snapshot.debug("[SNAPSHOT] Recording snapshot: entityId=" + entityId.toString() + " snapshot=" + row);
		UniSnapshot snapshot = new UniSnapshot(row, entityId);
		_snapshots.put(entityId, snapshot);
	}
	
	public void updateSnapshot(UniEntityID entityId, Map<String, Object> row) {
		UniLogger.universe_snapshot.debug("[SNAPSHOT] Updating snapshot: entityId=" + entityId.toString() + " snapshot=" + row);
		UniSnapshot snapshot = _snapshots.get(entityId);
		if(snapshot != null) {
			snapshot.setSnapshot(row);
			snapshot.setTimestamp(System.currentTimeMillis());
		}
	}

	public void forgetSnapshot(UniEntityID entityId) {
		UniLogger.universe_snapshot.debug("[SNAPSHOT] Forgetting snapshot: entityId=" + entityId.toString());
		UniSnapshot snapshot = _snapshots.get(entityId);
		if(snapshot != null) {
			_snapshots.remove(entityId);
			snapshot = null;
		}
	}
	
	public Map<String, Object> snapshotForEntityID(UniEntityID entityId) {
		UniSnapshot snapshot = _snapshots.get(entityId);
		if(snapshot == null)
			return null;
		UniLogger.universe_snapshot.debug("[SNAPSHOT] Load snapshot: entityId=" + entityId.toString() + " snapshot=" + snapshot.snapshot());
		return snapshot.snapshot();
	}
	
	public UniSnapshot snapshotForPredicate(UniEntity entity, UniPredicate predicate) {
		for(UniSnapshot r : _snapshots.values()) {
			if(r._entityId.entity().equals(entity)) {
				if(predicate.matchesToRow(r._snapshot, entity))
					return r;
			}
		}
		return null;
	}
	
	public UniSnapshot snapshotForFieldValues(UniEntity entity, Map<String, Object> fieldValues) {
		UniPredicate p = UniPredicate.Util.createPredicateFromFieldValues(fieldValues);
		return snapshotForPredicate(entity, p);
	}
	
	public void recordSnapshots(Map<UniEntityID, Map<String, Object>> snapshots) {
		for(UniEntityID entityId : snapshots.keySet()) {
			recordSnapshot(entityId, snapshots.get(entityId));
		}
	}
	
	public UniEntityID obtainEntityID(Object object) {
		if(object == null)
			return null;
		UniEntity entity = UniModelGroup.defaultGroup().entityForClass(object.getClass());
		Object primaryKey = entity.primaryKeyForObject(object);
		return obtainEntityID(entity, primaryKey);
	}
	
	private List<UniEntityID> entityIds() {
		if(_entityIds == null)
			_entityIds = ListUtils.list();
		return _entityIds;
	}
	
	public UniEntityID obtainEntityID(UniEntity entity, Object primaryKey) {
		for(UniEntityID entityId : entityIds()) {
			if(entityId instanceof UniEntityID.PK && entityId.entity().equals(entity) && ((UniEntityID.PK) entityId).primaryKey().equals(primaryKey))
				return entityId;
		}
		UniEntityID newEntityId = new UniEntityID.PK(entity, primaryKey);
		entityIds().add(newEntityId);
		return newEntityId;
	}
	
	public UniEntityID obtainEntityID(UniEntity entity, String key, Object value) {
		for(UniEntityID entityId : entityIds()) {
			if(entityId instanceof UniEntityID.KeyValue && 
					entityId.entity().equals(entity) && 
					((UniEntityID.KeyValue) entityId).key().equals(key) &&
					((UniEntityID.KeyValue) entityId).value().equals(value))
				return entityId;
		}
		UniEntityID newEntityId = new UniEntityID.KeyValue(entity, key, value);
		entityIds().add(newEntityId);
		return newEntityId;
	}
	
	public UniEntityID lookupEntityIDInCache(UniEntity entity, Object primaryKey) {
		UniEntityID entityId = this.obtainEntityID(entity, primaryKey);
		return _entityCache.contains(entityId) ? entityId : null;
	}
	
	public Object cachedObject(UniEntity entity, Object primaryKey) {
		UniEntityID entityId = this.obtainEntityID(entity, primaryKey);
		return cachedObject(entityId);
	}
	
	public Object cachedObject(UniEntityID entityId) {
		if(_entityCache != null && entityId != null) {
			Object object = _entityCache.get(entityId);
			return object;
		}
		return null;
	}
	public Object cachedObject(UniEntity entity, String key, Object value) {
		UniEntityID entityId = this.obtainEntityID(entity, key, value);
		return cachedObject(entityId);
	}
	
	public void cache(Object object) {
		if(object != null) {
			UniEntityID entityId = this.obtainEntityID(object);
			_entityCache.add(entityId, object);
		}
	}
	
	public void cache(Object object, String key, Object value) {
		if(object != null) {
			UniEntity entity = UniModelGroup.defaultGroup().entityForClass(object.getClass());
			UniEntityID entityId = this.obtainEntityID(entity, key, value);
			_entityCache.add(entityId, object);
		}
	}


	public void cacheObjects(Collection<?> objects) {
		for(Object object : objects)
			this.cache(object);
	}
	
	public void clearCache() {
		_entityCache.clear();
	}
	
	public void removeCache(UniEntityID entityId) {
		_entityCache.remove(entityId);
	}

	private void _loadEntityDefinitions() {
		for(UniEntity entity : _model.entities()) {
			if(entity.dictionaryDefinitions() == null) {
				entity.setDictionaryDefinitions(UniDictionaryUtil.uniFieldDefinitions(entity.filename(), this));
			}
		}
	}

	public void cacheEntityOnServer(UniEntity entity) {
		try {
			this.executeUniCommand("M.ONCACHE " + entity.filename());
		} catch (Exception e) {
			UniLogger.universe_command.error("M.ONCACHE " + entity.filename() + " failed", e);
		}
	}


			
	
	/*
	private static final int MAX_HISTORY = 1000;
	private static final DateFormat HISTORY_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss SSS");
	
	public static class CommandHistory {
		long timestamps[];
		List<String> commands;
		int size;
		
		public CommandHistory() {
			timestamps = new long[MAX_HISTORY];
			commands = ListUtils.list(MAX_HISTORY);
			size = 0;
		}
		
		public synchronized void record(String command) {
			if(size == MAX_HISTORY) {
				commands.remove(0);
				System.arraycopy(timestamps, 1, timestamps, 0, timestamps.length-1);
				timestamps[MAX_HISTORY-1] = 0;
			} else {
				size++;
			}
			commands.add(command);
			timestamps[size-1] = System.currentTimeMillis();
			UniLogger.universe_command.debug("[COMMAND] " + command);
		}
		
		public String lastCommandWithTimestamp() {
			Date date = new Date(timestamps[size-1]);
			return HISTORY_TIMESTAMP_FORMAT.format(date) + " " + commands.get(size-1);
		}
		
		public int size() {
			return size;
		}
		
		public List<String> listCommands() {
			List<String> list = ListUtils.list();
			for(int i = size - 1; i >= 0; i--) {
				Date date = new Date(timestamps[i]);
				list.add(HISTORY_TIMESTAMP_FORMAT.format(date) + " " + commands.get(i));
			}
			return list;
		}
		
		public void clear() {
			Arrays.fill(timestamps, 0);
			commands.clear();
			size = 0;
		}
	}
	*/
	
	public UniCommandGeneration commandGenerator() {
		return _commandGenerator;
	}
		
	public static void setConnectionPoolingOn(boolean poolingOn) {
		UniJava.setUOPooling(poolingOn);
	}

	public static void setPoolingDebug(boolean poolingDebug) {
		UniJava.setPoolingDebug(poolingDebug);		
	}

	public static void setMinimumPoolSize(int minSize) {
		UniJava.setMinPoolSize(minSize);
	}

	public static void setMaximumPoolSize(int maxSize) {
		UniJava.setMaxPoolSize(maxSize);
	}

	public static void setIdelRemoveThreshold(int idleRemoveThreshold) {
		UniJava.setIdleRemoveThreshold(idleRemoveThreshold);
	}

	public static void setIdelRemoveExecInterval(int idelRemoveExecInterval) {
		UniJava.setIdleRemoveExecInterval(idelRemoveExecInterval);
	}

	public static void setOpenSessionTimeOut(int openSessionTimeOut) {
		UniJava.setOpenSessionTimeOut(openSessionTimeOut);
	}

		
	
}

package universe;

import java.util.List;
import java.util.Map;
import java.util.Random;

import universe.util.UniLogger;
import core.util.ListUtils;
import core.util.MapUtils;

public class UniConnectionPool {
	private Map<UniConnectionInfo, List<UniObjectsConnection>> _connections;
	private int _poolSize;
	private static UniConnectionPool _sharedPool = null;
	
	public static UniConnectionPool sharedPool() {
		if(_sharedPool == null) {
			_sharedPool = new UniConnectionPool(5);
		}
		return _sharedPool;
	}
	
	private UniConnectionPool(int size) {
		_poolSize = size;
		_connections = MapUtils.map();
	}
	
	private UniObjectsConnection createNewConnection(UniModel model) {
		UniLogger.universe_connection.debug("Creating connection for connection info " + model.connectionInfo());
		List<UniObjectsConnection> conns = _connections.get(model.connectionInfo());
		UniObjectsConnection conn = new UniObjectsConnection(model);
		conns.add(conn);
		return conn;
	}
	
	protected UniObjectsConnection findConnection(UniModel model) {
		UniLogger.universe_connection.debug("Finding connection for connection info " + model.connectionInfo());
		List<UniObjectsConnection> conns = _connections.get(model.connectionInfo());
		if(conns == null) {
			conns = ListUtils.list(_poolSize);
			_connections.put(model.connectionInfo(), conns);
			return createNewConnection(model);
		}
		if(conns.size() < _poolSize) {
			return createNewConnection(model);
		}
		
		int size = conns.size();
		int index = new Random().nextInt(size);
		UniObjectsConnection conn = conns.get(index);
		return conn;
	}

	public synchronized UniObjectsConnection get(UniModel model) {
		return findConnection(model);
	}
}

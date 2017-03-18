package universe;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import core.util.MapUtils;
import universe.util.UniLogger;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniEntityCache {

	Map<UniEntityID, CachedRecord> _cache = MapUtils.map();
	private static final long SHORT_LIFE = 30000L;
	private static final long LONG_LIFE = 180000L;
	private static final long DistantFuture = 1000L * 60L * 60L * 24L;
	private Stat _stat = new Stat();
	
	protected class CachedRecord {
		Object _object;
		long _timestamp;
		
		@Override
		public String toString() {
			return "{object=" + _object.toString() + "; timestamp=" + new Date(_timestamp) + "}";
		}

	}
	
	public Object add(UniEntityID entityId, Object object) {
		_stat.add();
		if(entityId.entity().cacheStrategy() == UniEntity.CacheStrategy.None)
			return object;
		
		Object obj = null;
		
		synchronized(_cache) {
			if(this.contains(entityId)) {
				obj = replace(entityId, object);
				UniLogger.universe_cache.debug("[CACHE] " + object.toString() + " with entityId(" + entityId + ") replaced");
			} else {
				CachedRecord record = new CachedRecord();
				record._object = object;
				record._timestamp = this._cacheExpiration(entityId);
				_cache.put(entityId, record);
				UniLogger.universe_cache.debug("[CACHE] " + object.toString() + " with entityId(" + entityId + ") cached");
				obj =  object;
			}
		}
		
		return obj;
	}
	
	private Object replace(UniEntityID entityId, Object object) {
		CachedRecord record = _cache.get(entityId);
		record._object = object;
		record._timestamp = this._cacheExpiration(entityId);
		_cache.put(entityId, record);
		return object;
	}
		
	public Object get(UniEntityID entityId) {
		if(entityId.entity().cacheStrategy() == UniEntity.CacheStrategy.None)
			return null;
		
		Object object = null;
		
		synchronized(_cache) {
			CachedRecord record = _cache.get(entityId);
			if(record != null && record._timestamp >= System.currentTimeMillis()) {
				UniLogger.universe_cache.debug("[CACHE] Returns cached object " + record._object +  " with entityId(" + entityId + ")");
				object = record._object;
			} else if(record != null) {
				_cache.remove(entityId);
			}
		}
		
		return object;
	}
			
	public boolean contains(UniEntityID entityId) {
		boolean found = false;
		synchronized(_cache) {
			found = _cache.containsKey(entityId);
		}
		return found;
	}
	
	public void remove(UniEntityID entityId) {
		synchronized(_cache) {
			if(contains(entityId)) {
				_cache.remove(entityId);
				UniLogger.universe_cache.debug("[CACHE] Removed from cache " + entityId);
			}
		}
	}
	
	public void clear() {
		synchronized(_cache) {
			UniLogger.universe_cache.debug("[CACHE] Cleared cache");
			_cache.clear();
		}
	}
	
	public void clearExpiredRecords() {
		synchronized(_cache) {
			long millis = System.currentTimeMillis();
			Set<Map.Entry<UniEntityID, CachedRecord>> entries = _cache.entrySet();
			for(Map.Entry<UniEntityID, CachedRecord> entry : entries) {
				if(entry.getValue()._timestamp < millis)
					_cache.remove(entry.getKey());
			}
		}
	}
	
	private long _cacheExpiration(UniEntityID entityId) {
		UniEntity.CacheStrategy strategy = entityId.entity().cacheStrategy();
		if(UniEntity.CacheStrategy.None == strategy)
			return System.currentTimeMillis();
		if(UniEntity.CacheStrategy.Normal == strategy)
			return System.currentTimeMillis() + SHORT_LIFE;
		if(UniEntity.CacheStrategy.Statistical == strategy)
			return System.currentTimeMillis() + _stat.goodValue();
		if(UniEntity.CacheStrategy.DistantFuture == strategy)
			return System.currentTimeMillis() + DistantFuture;
		return System.currentTimeMillis() + SHORT_LIFE;
	}
	

	class Stat {
		long _lastTime;
		long _previousTime;
		long _minInterval = SHORT_LIFE;
		long _maxInterval = LONG_LIFE;
		
		public void add() {
			if(_lastTime == 0)
				_previousTime = System.currentTimeMillis() - LONG_LIFE;
			else
				_previousTime = _lastTime;
			_lastTime = System.currentTimeMillis();
			if(_minInterval > (_lastTime - _previousTime))
				_minInterval = _lastTime - _previousTime;
			if(_maxInterval < (_lastTime - _previousTime))
				_maxInterval = _lastTime - _previousTime;
		}
		
		public long goodValue() {
			return (_minInterval + _maxInterval) / 2 + SHORT_LIFE;
		}
	}
}

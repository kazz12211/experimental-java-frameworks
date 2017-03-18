package universe;

import java.util.Map;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniSnapshot {
	long _timestamp;
	Map<String, Object> _snapshot;
	UniEntityID _entityId;
	
	public UniSnapshot(Map<String, Object> row, UniEntityID entityId) {
		this(row, entityId, System.currentTimeMillis());
	}
	public UniSnapshot(Map<String, Object> row, UniEntityID entityId, long timestamp) {
		_timestamp = timestamp;
		_snapshot = row;
		_entityId = entityId;
	}
	public Map<String, Object> snapshot() {
		return _snapshot;
	}
	public void setSnapshot(Map<String, Object> row) {
		_snapshot = row;
	}
	public void setTimestamp(long timestamp) {
		_timestamp = timestamp;
	}
	public UniEntityID entityId() {
		return _entityId;
	}
}

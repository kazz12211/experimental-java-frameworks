package universe;

import java.util.Collection;

import core.util.FieldAccess;
import universe.util.UniLogger;

/**
 * 
 * @author ktsubaki
 *
 */
public abstract class UniRelationship {

	UniEntity _entity;
	String _key;
	boolean _isToMany;
	boolean _shouldPrefetch = false;

	protected UniRelationship(UniEntity entity, String key, boolean isToMany) {
		this._entity = entity;
		this._key = key;
		this._isToMany = isToMany;
	}
	
	public String key() {
		return _key;
	}
	public void setKey(String key) {
		this._key = key;
	}
	public boolean isToMany() {
		return _isToMany;
	}
	public void setToMany(boolean isToMany) {
		this._isToMany = isToMany;
	}

	public UniEntity entity() {
		return _entity;
	}
	public void setEntity(UniEntity entity) {
		this._entity = entity;
	}
	public boolean shouldPrefetch() {
		return _shouldPrefetch;
	}
	public void setShoudPrefetch(boolean flag) {
		this._shouldPrefetch = flag;
	}

	public static void addObjectToBothSidesOfRelationshipWithKey(
			Object object, Object value, String key) {
		UniEntity sourceEntity = UniContext.get().entityForObject(object);
		UniEntity destEntity = UniContext.get().entityForObject(value);
		if(sourceEntity == null || destEntity == null)
			return;
		UniJoin rel = sourceEntity.joinNamed(key);
		if(rel == null)
			return;
		UniJoin reverseJoin = null;
		for(UniJoin join : destEntity.joins()) {
			if(join.destinationEntity() == sourceEntity) {
				reverseJoin = join; break;
			}
		}
		if(rel.isToMany()) {
			Collection col = (Collection) FieldAccess.Util.getValueForKey(object, rel.key());
			col.add(value);
			String sourceKey = rel.sourceKey();
			String destKey = rel.destinationKey();
			Object sourceValue = FieldAccess.Util.getValueForKey(object, sourceKey);
			FieldAccess.Util.setValueForKey(value, sourceValue, destKey);
			UniContext.get().updateObject(object);
			if(reverseJoin != null) {
				FieldAccess.Util.setValueForKey(value, object, reverseJoin.key());
			}
		} else {
			FieldAccess.Util.setValueForKey(object, value, rel.key());
			String sourceKey = rel.sourceKey();
			String destKey = rel.destinationKey();
			Object sourceValue = FieldAccess.Util.getValueForKey(object, sourceKey);
			FieldAccess.Util.setValueForKey(value, sourceValue, destKey);
			if(reverseJoin != null) {
				FieldAccess.Util.setValueForKey(value, object, reverseJoin.key());
			}
		}
		UniContext.get().updateObject(value);
	}

	public static void removeObjectFromBothSidesOfRelationshipWithKey(
			Object object, Object value, String key) {
		UniEntity sourceEntity = UniContext.get().entityForObject(object);
		UniEntity destEntity = UniContext.get().entityForObject(value);
		if(sourceEntity == null || destEntity == null)
			return;
		UniJoin rel = sourceEntity.joinNamed(key);
		if(rel == null)
			return;
		if(rel.isToMany()) {
			Collection col = (Collection) FieldAccess.Util.getValueForKey(object, rel.key());
			UniLogger.universe_test.info("**** Before delete " + col);
			UniLogger.universe_test.info("**** Removing " + value);
			UniEntityID eid = UniContext.get().entityIDForObject(value);
			for(Object obj : col) {
				UniEntityID eid2 = UniContext.get().entityIDForObject(obj);
				if(eid.equals(eid2)) {
					col.remove(obj);
					break;
				}
			}
			UniLogger.universe_test.info("**** After delete " + col);
			String destKey = rel.destinationKey();
			FieldAccess.Util.setValueForKey(value, null, destKey);
		} else {
			FieldAccess.Util.setValueForKey(object, null, rel.key());
			String sourceKey = rel.sourceKey();
			String destKey = rel.destinationKey();
			FieldAccess.Util.setValueForKey(value, null, destKey);
			FieldAccess.Util.setValueForKey(object, null, sourceKey);
			UniContext.get().updateObject(object);
		}
		
		if(rel.ownsDestination()) {
			UniContext.get().deleteObject(value);
		}
	}


}

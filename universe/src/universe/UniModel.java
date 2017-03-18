package universe;

import java.util.List;
import java.util.Map;

import core.util.ListUtils;
import core.util.MapUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniModel {
	String _name;
	Map<Class<?>, UniEntity> _entityMap = MapUtils.map();
	List<UniEntity> _entities = ListUtils.list();
	UniConnectionInfo _connectionInfo;
	
	public static UniModel modelNamed(String modelName) {
		return UniModelGroup.modelNamed(modelName);
	}
	
	public List<UniEntity> entities() {
		return _entities;
	}
		
	public void addEntity(Class<?> objectClass, UniEntity entity) {
		_entityMap.put(objectClass, entity);
		_entities.add(entity);
	}
	
	public void removeEntity(UniEntity entity) {
		Class<?> entityClass = entity.entityClass();
		_entityMap.remove(entityClass);
		_entities.remove(entity);
	}
	
	public UniEntity entityForClass(Class<?> objectClass) {
		return _entityMap.get(objectClass);
	}
	
	public UniEntity entityNamed(String entityName) {
		for(UniEntity entity : _entityMap.values()) {
			if(entity.entityName().equals(entityName))
				return entity;
		}
		return null;
	}
	
	public UniConnectionInfo connectionInfo() {
		return _connectionInfo;
	}
	
	public String name() {
		return _name;
	}
	public void setName(String name) {
		this._name = name;
	}
		
	@Override
	public String toString() {
		return "{name="+_name+"; entities=" + _entities.toString() + "; connectionInfo=" + _connectionInfo.toString() + "}";
	}
}

package universe;


/**
 * 
 * @author ktsubaki
 *
 */
public class UniJoin extends UniRelationship {

	String _destinationEntityName;
	String _sourceKey;
	String _destinationKey;
	boolean _ownsDestination;
	boolean _cacheDestination = true;
	
	public UniJoin(UniEntity sourceEntity, String destinationEntity, String key, String sourceKey, String destinationKey, boolean isToMany, boolean ownsDestination) {
		super(sourceEntity, key, isToMany);
		this._destinationEntityName = destinationEntity;
		this._sourceKey = sourceKey;
		this._destinationKey = destinationKey;
		this._ownsDestination = ownsDestination;
	}

	public UniEntity source() {
		return _entity;
	}
	public void setSource(UniEntity source) {
		_entity = source;
	}
	public String destinationEntityName() {
		return _destinationEntityName;
	}
	public void setDestinationEntityName(String destination) {
		this._destinationEntityName = destination;
	}
	
	public String sourceKey() {
		return _sourceKey;
	}
	public String destinationKey() {
		return _destinationKey;
	}
	public boolean ownsDestination() {
		return _ownsDestination;
	}
	public UniEntity destinationEntity() {
		UniModel model = _entity.model();
		return model != null ? model.entityNamed(_destinationEntityName) : null;
	}
	
	public String toString() {
		return "join {key=" + _key + "; targetEntity=" + _destinationEntityName + "}";
	}

	public boolean cacheDestination() {
		return _cacheDestination;
	}
	
}

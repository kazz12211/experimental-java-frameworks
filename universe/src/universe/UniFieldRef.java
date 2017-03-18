package universe;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniFieldRef {
	String _fieldName;
	UniAssociation _association;
	UniEntity _entity;
	
	public UniFieldRef(UniEntity entity, UniAssociation association, String fieldName) {
		this._entity = entity;
		this._association = association;
		this._fieldName = fieldName;
	}
	
	public UniEntity entity() {
		return _entity;
	}
	
	public UniAssociation association() {
		return _association;
	}
	
	public String fieldName() {
		return _fieldName;
	}
	
}

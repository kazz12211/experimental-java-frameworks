package universe;

import java.util.List;

import core.util.ListUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniAssociation extends UniRelationship {
	String _name;
	Class<?> _associationClass;
	UniFieldRef[] _fieldRefs;
	
	public UniAssociation(UniEntity entity, String name, String key, boolean isToMany, Class<?> associationClass) {
		super(entity, key, isToMany);
		this._name = name;
		this._associationClass = associationClass;
	}
	
	public String name() {
		return _name;
	}
	public void setName(String name) {
		this._name = name;
	}

	public Class<?> associationClass() {
		return _associationClass;
	}
	public void setAssociationClass(Class<?> associationClass) {
		this._associationClass = associationClass;
	}
	public UniFieldRef[] fieldRefs() {
		return _fieldRefs;
	}
	public void setFieldRefs(UniFieldRef[] fields) {
		this._fieldRefs = fields;
	}

	public List<UniField> fields() {
		return fieldsInEntity(_entity);
	}
	
	private List<UniField> fieldsInEntity(UniEntity entity) {
		List<UniField> fieldList = ListUtils.list();
		for(UniFieldRef ref : _fieldRefs) {
			UniField field = entity.fieldWithColumnName(ref.fieldName());
			fieldList.add(field);
		}
		return fieldList;
	}
	
	public String toString() {
		List<String> s = ListUtils.list();
		List<UniField> list = this.fields();
		for(UniField field : list) {
			s.add(field.toString());
		}
		String ss = ListUtils.listToString(s, ", ");
		
		return "association {name=" + _name + "; key=" + _key + "; associationClass=" + _associationClass + "(" + ss + ")}";
	}
	
}

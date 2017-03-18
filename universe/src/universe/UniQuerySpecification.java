package universe;

import java.util.List;
import java.util.Map;

import core.util.ListUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniQuerySpecification {
	UniEntity _entity;
	Class<?> _entityClass;
	UniPredicate _predicate;
	List<UniSortOrdering> _sortOrderings;
	Map<String, Integer> _fetchHint;
	Integer _storedListNumber = null;
	int _listNumber;
	
	public static final String FetchLimitSize = "fetchLimitSize";
	public static final String FetchLimitStart = "fetchLimitStart";
	public static final String FetchSample = "sampling";
	
	boolean _forceRefetch = false;
		
	public UniQuerySpecification(UniEntity entity, UniPredicate predicate) {
		this._entity = entity;
		this._entityClass = entity.entityClass();
		this._predicate = predicate;
		this._listNumber = 0;
	}
	public UniQuerySpecification(UniEntity entity, UniPredicate predicate, int listNumber) {
		this._entity = entity;
		this._entityClass = entity.entityClass();
		this._predicate = predicate;
		this._listNumber = listNumber;
	}
	
	public Class<?> entityClass() {
		return _entityClass;
	}

	public UniEntity entity() {
		if(_entity == null && _entityClass != null) {
			return null;
		}
		return _entity;
	}
	
	public UniPredicate predicate() {
		return _predicate;
	}
	
	public void setPredicate(UniPredicate predicate) {
		_predicate = predicate;
	}
	
	public void setSortOrderings(List<UniSortOrdering> sortOrderings) {
		this._sortOrderings = sortOrderings;
	}
	
	public List<UniSortOrdering> sortOrderings() {
		if(_sortOrderings == null) {
			_sortOrderings = ListUtils.list();
		}
		return _sortOrderings;
	}
	
	public void setFetchHint(Map<String, Integer> hint) {
		_fetchHint = hint;
	}
	public Map<String, Integer> fetchHint() {
		return _fetchHint;
	}
	public boolean forceRefetch() {
		return _forceRefetch;
	}
	public void setForceRefetch(boolean flag) {
		_forceRefetch = flag;
	}
	
	public void setStoredQueryNumber(Integer listNumber) {
		_storedListNumber = listNumber;
	}

	public Integer storedQueryNumber() {
		return _storedListNumber;
	}
	
	public int listNumber() {
		return _listNumber;
	}
	public void setListNumber(int listNumber) {
		_listNumber = listNumber;
	}
}

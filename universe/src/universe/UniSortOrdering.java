package universe;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import core.util.FieldAccess;
import core.util.ListUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniSortOrdering {

	public enum Direction {Ascending, Descending, CaseInsensitiveAsending, CaseInsensitiveDescending};
	
	String _key;
	Direction _direction;
	
	public UniSortOrdering(String key, Direction direction) {
		this._key = key;
		this._direction = direction;
	}
	
	public String key() {
		return _key;
	}
	
	public Direction direction() {
		return _direction;
	}

	public boolean isAscending() {
		return (_direction == Direction.Ascending || _direction == Direction.CaseInsensitiveAsending);
	}
	
	public boolean isDescending() {
		return (_direction == Direction.Descending || _direction == Direction.CaseInsensitiveDescending);
	}
	
	public boolean isCaseInsensitive() {
		return (_direction == Direction.CaseInsensitiveAsending || _direction == Direction.CaseInsensitiveDescending);
	}
	
	public String generateString(UniEntity entity) {
		UniField field = entity.fieldNamed(key());
		if(isDescending())
			return "BY.DSND " + field.columnName();
		return "BY " + field.columnName();
	}
	
	public List<?> sortedListUsingSortOrderings(List<?> list, List<UniSortOrdering> orderings) {
		List newList = ListUtils.copyList(list);
		sortUsingSortOrderings(newList, orderings);
		return newList;
	}
	public static void sortUsingSortOrderings(List<?> list, List<UniSortOrdering> orderings) {
		SOComparator comp = new SOComparator(orderings);
		Collections.sort(list, comp);
	}
	
	static class SOComparator implements Comparator {

		private List<UniSortOrdering> _orderings;

		SOComparator(List<UniSortOrdering> orderings) {
			_orderings = orderings;
		}
		
		private int compareUsingSortOrdering(Object o1, Object o2, UniSortOrdering ordering) {
			if(o1 == null && o2 == null)
				return 0;
			if(o1 != null && o2 == null)
				return ordering.isAscending() ? -1 : 1;
			if(o1 == null && o2 != null)
				return ordering.isAscending() ? 1 : -1;
			
			Object value1 = FieldAccess.Util.getValueForKey(o1, ordering.key());
			Object value2 = FieldAccess.Util.getValueForKey(o2, ordering.key());
			return ((Comparable) value1).compareTo((Comparable) value2);
		}
		
		@Override
		public int compare(Object o1, Object o2) {
			for(UniSortOrdering ordering : _orderings) {
				int result = compareUsingSortOrdering(o1, o2, ordering);
				if(result != 0)
					return result;
			}
			return 0;
		}
		
	}
	
}

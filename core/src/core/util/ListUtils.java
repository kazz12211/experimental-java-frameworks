package core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ListUtils {

	public static final List EmptyList = new ArrayList();
	
	public static String listToString(List l, String separator) {
		return StringUtils.join(l, separator);
	}

	public static boolean nullOrEmpty(List<?> list) {
		return list == null || list.isEmpty();
	}

	public static <T> List<T> list() {
		return new ArrayList<T>(1);
	}

	public static <T> List<T> list(int size) {
		return new ArrayList<T>(size);
	}
	public static <T> List<T> copyList(List list) {
		return new ArrayList<T>(list);
	}
	
	public static <T> List<T> filteredList(List<T> list, ListFilter<T> listFilter) {
		List<T> filteredList = list();
		for(T object : list) {
			if(listFilter.filter(object))
				filteredList.add(object);
		}
		return filteredList;
	}

    public static <T> void addElementsIfAbsent (List<T> destList,
            Collection<? extends T> aColl) {
    	if (aColl == null) {
    		return;
    	}

    	Iterator<? extends T> i = aColl.iterator();
    	while (i.hasNext()) {
    		addElementIfAbsent(destList, i.next());
    	}
    }

    public static <T> void addElementIfAbsent (List<T> l, T element) {
        if (!l.contains(element)) {
            l.add(element);
        }
    }
    
    public static <T> List<T> list (T object) {
        List<T> l = list(1);
        l.add(object);
        return l;
    }

    public static <T> List<T> list (T a, T b) {
        List<T> l = list(2);
        l.add(a);
        l.add(b);
        return l;
    }

	public static boolean listEquals(List v1, List v2) {
        if (v1 == v2) {
            return true;
        }
        if (v1 == null || v2 == null || v1.size() != v2.size()) {
            return false;
        }
        Iterator i1 = v1.iterator();
        Iterator i2 = v2.iterator();
        while (i1.hasNext() && i2.hasNext()) {
            if (!SystemUtils.objectEquals(i1.next(), i2.next())) {
                return false;
            }
        }
        if (i1.hasNext() || i2.hasNext()) {
            Log.coreUtil.warning(7801);
                //This should never happen
                //since we already checked that they have the same size
            return false;
        }
        return true;
	}

}

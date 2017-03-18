package core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ariba.util.core.SystemUtil;

public class MapUtils {

	public static final Map EmptyMap = new HashMap();
	
    public static <K,V> Map<K,V> map ()
    {
        return new HashMap<K,V>(2); 
    }

	public static boolean mapEquals(Map h1, Map h2) {
        if (h1 == h2) {
            return true;
        }
        if (h1 == null || h2 == null || h1.size() != h2.size()) {
            return false;
        }
        for (Iterator i = h1.keySet().iterator(); i.hasNext();) {
            Object key = i.next();
            Object val2 = h2.get(key);
            Object val1 = h1.get(key);
            if (! SystemUtils.objectEquals(val1, val2)) {
                return false;
            }
        }
        return true;
	}

}

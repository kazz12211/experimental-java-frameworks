package core.util;

public class ArrayUtils {

	public static boolean arrayEquals(Object[] a1, Object[] a2) {
        if (a1 == null && a2 == null) {
            return true;
        }
        if (a1 == null || a2 == null) {
            return false;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int e = 0, l = a1.length; e < l; e++) {
            if (!SystemUtils.objectEquals(a1[e], a2[e])) {
                return false;
            }
        }
        return true;
	}

}

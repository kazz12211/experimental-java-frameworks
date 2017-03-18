package core.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

public class SystemUtils {

	public static String stackTrace(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.close();
		try {
			writer.close();
		} catch (IOException e) {
			Assert.that(false, "IOException in SystemUtils.stackTrace");
		}
		return writer.toString();
	}

	public static String stackTrace() {
		return stackTrace(new Exception("Stack trace"));
	}

	public static boolean objectEquals(Object one, Object two) {
        if (one == null && two == null) {
            return true;
        }
        if (one == null || two == null) {
            return false;
        }
        if ((one instanceof Map) && (two instanceof Map)) {
            return MapUtils.mapEquals((Map)one,(Map)two);
        }
        else if ((one instanceof List) && (two instanceof List)) {
            return ListUtils.listEquals((List)one, (List)two);
        }
        else if ((one instanceof Object[]) && (two instanceof Object[])) {
            return ArrayUtils.arrayEquals((Object[])one, (Object[])two);
        }
        else {
            return one.equals(two);
        }
	}

}

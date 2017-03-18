package core.util;

public final class NumberUtils {

	private static String digitString(String string) {
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if(ch == '.')
				break;
			if(CharacterSet.digitCharacterSet.contains(ch)) {
				b.append(ch);
			}
		}
		return b.toString();
	}
	
	private static String numberString(String string) {
		StringBuffer b = new StringBuffer();
		for(int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if(CharacterSet.numberCharacterSet.contains(ch))
				b.append(ch);
		}
		return b.toString();
	}
	
	public static Integer toInteger(String string, int defaultInt) {
		if(string == null)
			return new Integer(defaultInt);
		String s = string.trim();
		boolean minus = false;
		if(s.charAt(0) == '-') {
			minus = true;
			s = s.substring(1);
		}
		String digitStr = digitString(s);
		if(digitStr.length() > 0) {
			int val = Integer.parseInt(digitStr);
			if(minus)
				return new Integer(-val);
			else
				return new Integer(val);
		}
		return new Integer(defaultInt);
	}
	
	public static Integer toInteger(String string) {
		return toInteger(string, 0);
	}
	
	public static Long toLong(String string, long defaultLong) {
		if(string == null)
			return new Long(defaultLong);
		String s = string.trim();
		boolean minus = false;
		if(s.charAt(0) == '-') {
			minus = true;
			s = s.substring(1);
		}
		String digitStr = digitString(s);
		if(digitStr.length() > 0) {
			long val = Long.parseLong(digitStr);
			if(minus)
				return new Long(-val);
			else
				return new Long(val);
		}
		return new Long(defaultLong);
	}
	
	public static Long toLong(String string) {
		return toLong(string, 0);
	}
	
	public static Double toDouble(String string, double defaultValue) {
		if(string == null)
			return new Double(defaultValue);
		String s = string.trim();
		boolean minus = false;
		if(s.charAt(0) == '-') {
			minus = true;
			s = s.substring(1);
		}
		String numberStr = numberString(s);
		if(numberStr.length() > 0) {
			double val = Double.parseDouble(numberStr);
			if(minus)
				return new Double(-val);
			else
				return new Double(val);
		}
		return new Double(defaultValue);
	}
	
	public static Double toDouble(String string) {
		return toDouble(string, 0.0);
	}
}

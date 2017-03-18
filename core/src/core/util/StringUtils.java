package core.util;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static String join(List l, String separator) {
		StringBuffer buffer = new StringBuffer();
		for(int i = 0, s = l.size(); i < s; i++) {
			if(i != 0)
				buffer.append(separator);
			buffer.append(l.get(i));
		}
		return buffer.toString();
	}
	
	public static boolean nullOrEmpty(String string) {
		return string == null || string.length() == 0;
	}
	
	public static boolean nullOrEmptyOrBlank(String string) {
		return nullOrEmpty(string) || string.trim().length() == 0;
	}
	
	public static String convert(String value, String sourceEncoding, String destEncoding) throws UnsupportedEncodingException {
		Map<String, String> conversion = createConversionMap(sourceEncoding, destEncoding);
		char oldChar;
		char newChar;
		String key;
		for (Iterator<String> itr = conversion.keySet().iterator(); itr
				.hasNext();) {
			key = itr.next();
			oldChar = toChar(key);
			newChar = toChar(conversion.get(key));
			value = value.replace(oldChar, newChar);
		}
		return value;
	}

	 private static Map<String, String> createConversionMap(String src, String dest) throws UnsupportedEncodingException {
		Map<String, String> conversion = new HashMap<String, String>();
		if ((src.equals("UTF8")) && (dest.equals("SJIS"))) {
			conversion.put("U+FF0D", "U+2212");
			conversion.put("U+FF5E", "U+301C");
			conversion.put("U+FFE0", "U+00A2");
			conversion.put("U+FFE1", "U+00A3");
			conversion.put("U+FFE2", "U+00AC");
			conversion.put("U+2015", "U+2014");
			conversion.put("U+2225", "U+2016");

		} else if ((src.equals("SJIS")) && (dest.equals("UTF8"))) {
			conversion.put("U+2212", "U+FF0D");
			conversion.put("U+301C", "U+FF5E");
			conversion.put("U+00A2", "U+FFE0");
			conversion.put("U+00A3", "U+FFE1");
			conversion.put("U+00AC", "U+FFE2");
			conversion.put("U+2014", "U+2015");
			conversion.put("U+2016", "U+2225");

		}
		/*else {
			throw new UnsupportedEncodingException("Could not create conversion map src="
					+ src + ",dest=" + dest);
		}*/
		return conversion;
	}

	private static char toChar(String value) {
		return (char) Integer.parseInt(value.trim().substring("U+".length()), 16);
	}
	
	public static boolean validateEmailAddress(String mailAddress) {
		Pattern pattern = Pattern.compile("/^[^0-9][a-zA-Z0-9_]+([.][a-zA-Z0-9_]+)*@\\[?([\\d\\w\\.-]+)]?$/");
		Matcher matcher = pattern.matcher(mailAddress);
		return matcher.matches();
	}

}

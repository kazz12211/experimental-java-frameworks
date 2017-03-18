package universe.util;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;

import asjava.uniclientlibs.UniConnection;
import asjava.uniclientlibs.UniString;
import core.util.EncodingDetector;
import core.util.StringUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniStringUtil {

	public static UniString getString(UniConnection connection, Object object) {
		return new UniString(connection, object);
	}
	
	public static String coerceToString(UniString uniString) {		
		return uniString.toString();
	}
	
	
	public static Number coerceToNumber(UniString uniString) {
		String str = coerceToString(uniString);
		if(str.contains("."))
			return new BigDecimal(str);
		else
			return new BigInteger(str);
	}
		
	public static String coerceToString(UniString uniString, String encoding) {
		if(encoding.equals("UTF8"))
			return uniString.toString();
		try {
			byte[] src = uniString.getBytes();
			byte[] dest = (new String(src, encoding)).getBytes("UTF8");
			String value = new String(dest, "UTF8");
			value = StringUtils.convert(value, encoding, "UTF8");
			return value;
		} catch (UnsupportedEncodingException e) {
			UniLogger.universe.info("coerceToString() encountered error", e);
		}
		return uniString.toString();
	}

	public static String guessEncodingAndCoerceToString(UniString string, String expectedEncoding) {
		byte bytes[] = string.getBytes();
		String guessedEncoding = EncodingDetector.detectEncodingWithDefaultEncoding(bytes, expectedEncoding);
		if(guessedEncoding.equals("UTF8"))
			return string.toString();
		try {
			byte[] dest = (new String(bytes, guessedEncoding)).getBytes("UTF8");
			String value = new String(dest, "UTF8");
			value = StringUtils.convert(value, guessedEncoding, "UTF8");
			return value;
		} catch (UnsupportedEncodingException e) {
			UniLogger.universe.info("coerceToString() encountered error", e);
		}
		return string.toString();
	}
}

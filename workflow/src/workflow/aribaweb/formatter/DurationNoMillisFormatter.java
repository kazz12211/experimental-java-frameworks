package workflow.aribaweb.formatter;

import java.text.ParseException;

import ariba.ui.aribaweb.util.AWFormatter;
import ariba.util.core.StringUtil;

public class DurationNoMillisFormatter extends AWFormatter {

	@Override
	public Object parseObject(String stringToParse) throws ParseException {
		if(StringUtil.nullOrEmptyOrBlankString(stringToParse))
			return null;
		String comps[] = stringToParse.split(":");
		long mul = 1;
		long dur = 0;
		for(int i = comps.length - 1; i >= 0; i--) {
			long v = Long.parseLong(comps[i]);
			dur += (v * mul);
			mul *= 60;
		}
		return new Long(dur);
	}

	private String zeroFilledNumberString(long number, int length) {
		String str = Long.toString(number);
		String subStr;
		
		int strLen = str.length();
		if(str.charAt(0) == '-') {
			strLen--;
			subStr = str.substring(1);
		} else {
			subStr = str;
		}
		
		if(strLen >= length)
			return str;
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < length - strLen; i++) {
			buffer.append("0");
		}
		buffer.append(subStr);
		if(str.charAt(0) == '-') {
			return "-" + buffer.toString();
		} else {
			return buffer.toString();
		}
	}
	
	@Override
	public String format(Object objectToFormat) {
		if(objectToFormat instanceof Number) {
			long dur = ((Number)objectToFormat).longValue();
			long day = dur / (60 * 60 * 24);
			dur = dur % (60 * 60 * 24);
			long hour = dur / (60 * 60);
			dur = dur % (60 * 60);
			long min = dur / (60);
			long sec = dur % 60;
			StringBuffer string = new StringBuffer();
			if(day > 0)
				string.append(Long.toString(day) + "d");
			string.append(this.zeroFilledNumberString(hour, 2) + ":");
			string.append(this.zeroFilledNumberString(min, 2) + ":");
			string.append(this.zeroFilledNumberString(sec, 2) );
			return string.toString();
		}
		return null;
	}

}

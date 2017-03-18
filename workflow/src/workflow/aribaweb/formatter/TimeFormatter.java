package workflow.aribaweb.formatter;

import java.text.ParseException;
import java.util.Date;

import core.util.DateUtils;
import core.util.StringUtils;
import ariba.ui.aribaweb.util.AWFormatter;

public class TimeFormatter extends AWFormatter {

	@Override
	public Object parseObject(String stringToParse) throws ParseException {
		if(StringUtils.nullOrEmptyOrBlank(stringToParse))
			return null;
		int datecomps[] = DateUtils.dateComponents();
		datecomps[3] = 0;
		datecomps[4] = 0;
		datecomps[5] = 0;
		datecomps[6] = 0;
		String comps[] = stringToParse.split(":");
		if(comps.length >= 3) {
			datecomps[3] = Integer.parseInt(comps[0]);
			datecomps[2] = Integer.parseInt(comps[1]);
			datecomps[1] = Integer.parseInt(comps[2]);
		}
		if(comps.length >= 2) {
			datecomps[2] = Integer.parseInt(comps[0]);
			datecomps[1] = Integer.parseInt(comps[1]);
		}
		if(comps.length >= 1) {
			datecomps[1] = Integer.parseInt(comps[0]);
		}
		return DateUtils.dateWithComponents(datecomps);
	}

	private String zeroFilledNumberString(int number, int length) {
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
		if(objectToFormat instanceof Date) {
			Date date = (Date) objectToFormat;
			int comps[] = DateUtils.dateComponents(date);
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.zeroFilledNumberString(comps[3], 2));
			buffer.append(":");
			buffer.append(this.zeroFilledNumberString(comps[4], 2));
			buffer.append(":");
			buffer.append(this.zeroFilledNumberString(comps[5], 2));
			return buffer.toString();
		}
		return null;
	}

}

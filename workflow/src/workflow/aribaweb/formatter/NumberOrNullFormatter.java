package workflow.aribaweb.formatter;

import java.text.NumberFormat;
import java.text.ParseException;

import ariba.ui.aribaweb.util.AWFormatter;
import ariba.util.core.StringUtil;

public class NumberOrNullFormatter extends AWFormatter {

	NumberFormat fmt;
	
	public NumberOrNullFormatter() {
		fmt = NumberFormat.getNumberInstance();
		fmt.setGroupingUsed(false);
	}
	
	@Override
	public Object parseObject(String stringToParse) throws ParseException {
		if(StringUtil.nullOrEmptyOrBlankString(stringToParse))
			return null;
		Number number = null;
		try {
			number = fmt.parse(stringToParse);
		} catch(ParseException e) {
			e.printStackTrace();
		}
		if(number == null) {
			return null;
		}
		return new Long(number.longValue());
	}

	@Override
	public String format(Object objectToFormat) {
		if(objectToFormat == null)
			return "";
		return fmt.format(objectToFormat);
	}

}

package workflow.aribaweb.formatter;

import java.text.DecimalFormat;
import java.text.ParseException;

import ariba.ui.aribaweb.util.AWFormatter;
import ariba.util.core.StringUtil;

public class DecimalIntFormatter extends AWFormatter {

	DecimalFormat fmt;
	
	public DecimalIntFormatter() {
		fmt = new DecimalFormat();
		fmt.setGroupingUsed(true);
		fmt.setGroupingSize(3);
		fmt.setMaximumFractionDigits(0);
		fmt.setMinimumFractionDigits(0);
	}
	
	@Override
	public Object parseObject(String stringToParse) throws ParseException {
		if(StringUtil.nullOrEmptyOrBlankString(stringToParse))
			return null;
		Number number = null;
		try {
			number = fmt.parse(stringToParse);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		if(number == null)
			return null;
		return new Integer(number.intValue());
	}

	@Override
	public String format(Object objectToFormat) {
		if(objectToFormat == null)
			return "";
		return fmt.format(objectToFormat);
	}

}

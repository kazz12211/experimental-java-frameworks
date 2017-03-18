package workflow.aribaweb.formatter;

import java.text.DecimalFormat;
import java.text.ParseException;

import ariba.ui.aribaweb.util.AWFormatter;
import ariba.util.core.StringUtil;

public class PercentageFormatter extends AWFormatter {

	DecimalFormat fmt;
	
	public PercentageFormatter() {
		fmt = new DecimalFormat();
		fmt.setGroupingUsed(true);
		fmt.setGroupingSize(3);
		fmt.setMaximumFractionDigits(2);
		fmt.setMinimumFractionDigits(2);
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
		return new Double(number.doubleValue());
	}

	@Override
	public String format(Object objectToFormat) {
		if(objectToFormat == null)
			return "";
		return fmt.format(objectToFormat);
	}

}

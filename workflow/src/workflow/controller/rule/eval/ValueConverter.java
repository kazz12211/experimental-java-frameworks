package workflow.controller.rule.eval;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import core.util.MapUtils;
import workflow.controller.Trace;
import workflow.controller.rule.WorkflowRule;

public abstract class ValueConverter {

	private static Map<Class<?>, ValueConverter> converters;
	static {
		converters = MapUtils.map();
		converters.put(Integer.TYPE, new IntegerConverter());
		converters.put(Integer.class, new IntegerConverter());
		converters.put(Short.TYPE, new IntegerConverter());
		converters.put(Short.class, new IntegerConverter());
		converters.put(Boolean.TYPE, new BooleanConverter());
		converters.put(Boolean.class, new BooleanConverter());
		converters.put(String.class, new StringConverter());
		converters.put(Float.TYPE, new FloatConverter());
		converters.put(Float.class, new FloatConverter());
		converters.put(Double.TYPE, new DoubleConverter());
		converters.put(Double.class, new DoubleConverter());
		converters.put(Date.class, new DateConverter());
	}
	
	public abstract Object convert(String string);

	public static Object convertForTarget(Class<?> targetClass, String string) {
		ValueConverter converter = converters.get(targetClass);
		Object converted = null;
		if(converter != null)
			converted = converter.convert(string);
		if(WorkflowRule.debugMode)
			Trace.writeLog("ValueConverter: convert value " + string + " using " + converter + ", result is " + converted);
		return converted;
	}

	static class IntegerConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			return new Integer(string);
		}
		
	}
	static class BooleanConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			if(string.equals("0") || string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no"))
				return new Boolean(false);
			return new Boolean(true);
		}
		
	}
	static class StringConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			return string;
		}
		
	}
	static class FloatConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			return new Float(string);
		}
		
	}
	static class DoubleConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			return new Double(string);
		}
		
	}
	static class DateConverter extends ValueConverter {

		@Override
		public Object convert(String string) {
			DateFormat shortFormat = new SimpleDateFormat("yyyy/MM/dd");
			DateFormat longFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = null;
			try {
				date = longFormat.parse(string);
			} catch (ParseException e) {
				try {
					date = shortFormat.parse(string);
				} catch (ParseException e1) {
					Trace.writeLog("DateConverter error: date format should be 'yyyy/MM/dd' or 'yyyy/MM/dd HH:mm:ss'");
				}
			}
			return date;
		}
		
	}
}

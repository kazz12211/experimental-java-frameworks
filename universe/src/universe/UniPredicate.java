package universe;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import core.util.FieldAccess;
import core.util.ListUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public interface UniPredicate {

	public enum Operator {
		Eq, Neq, Gt, Gte, Lt, Lte, Like, IsNull, IsNotNull, StartsWith, EndsWith, Contains
	};
	public static String[] UniOps = {
		"EQ",
		"NE",
		"GT",
		"GE",
		"LT",
		"LE",
		"LIKE",
		"IS.NULL",
		"IS.NOT.NULL",
		"LIKE",
		"LIKE",
		"LIKE"
	};
	
	public abstract String generateString(UniEntity entity);
	public abstract boolean matches(Object object, UniEntity entity);
	public abstract boolean matchesToRow(Map<String, Object> row, UniEntity entity);
	
	public class KeyValue implements UniPredicate {
		String _key;
		Object _value;
		Operator _operator;
		
		
		public KeyValue(String key, Object value) {
			this(key, value, Operator.Eq);
		}
		
		public KeyValue(String key, Object value, Operator eq) {
			_key = key;
			_value = value;
			_operator = eq;
		}

		public String key() {
			return _key;
		}
		public Object value() {
			return _value;
		}
		public Operator operator() {
			return _operator;
		}
		
		@Override
		public String generateString(UniEntity entity) {
			UniField field = entity.fieldNamed(_key);
			String opString = operatorString(field);
			if(_operator == Operator.IsNotNull || _operator == Operator.IsNull)
				return field._columnName + " " + opString;
			String opcString = valueString(entity, field);
			return field.columnName() + " " + opString + " " + opcString;
		}

		private String operatorString(UniField field) {
			return UniOps[_operator.ordinal()];
		}
		
		private String valueString(UniEntity entity, UniField field) {
			if(_value == null)
				return "NULL";
			if(_value instanceof String) {
				if(_operator == Operator.Contains)
					return "\"..." + _value.toString() + "...\"";
				if(_operator == Operator.StartsWith)
					return "\"" + _value.toString() + "...\"";
				if(_operator == Operator.EndsWith)
					return "\"..." + _value.toString() + "\"";
				return "\"" + _value.toString() + "\"";
			}	
			if(_value instanceof Number)
				return _value.toString();
			if(_value instanceof Boolean)
				return ((Boolean) _value).booleanValue() ? "TRUE" : "FALSE";
			if(_value instanceof Date) {
				DateFormat formatter = entity.fieldNamed(_key).dateFormat();
				if(formatter == null)
					return _value.toString();
				return "\"" + formatter.format((Date) _value) + "\"";
			}
			return _value.toString();
		}

		@Override
		public boolean matches(Object object, UniEntity entity) {
			return Util.matches(FieldAccess.DefaultImplementation.getValueForKeyPath(object, _key), _value, _operator);
		}

		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			UniField field = entity.fieldNamed(_key);
			return Util.matches(Util.getRowValue(row, field.columnName()), _value, _operator);
		}
		
		
	}
	
	public class Eval implements UniPredicate {
		String _evalString;
		
		public Eval(String evalString) {
			_evalString = evalString;
		}

		@Override
		public String generateString(UniEntity entity) {
			return _evalString;
		}

		@Override
		public boolean matches(Object object, UniEntity entity) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			// TODO Auto-generated method stub
			return false;
		}
		
	}
	
	public class KeyKey implements UniPredicate {
		String _leftKey;
		String _rightKey;
		Operator _operator;

		public KeyKey(String leftKey, String rightKey) {
			this(leftKey, rightKey, Operator.Eq);
		}
		
		public KeyKey(String leftKey, String rightKey, Operator operator) {
			_leftKey = leftKey;
			_rightKey = rightKey;
			_operator = operator;
		}
		
		public String leftKey() {
			return _leftKey;
		}
		
		public String rightKey() {
			return _rightKey;
		}
		
		public Operator operator() {
			return _operator;
		}

		private String operatorString() {
			return UniOps[_operator.ordinal()];
		}

		@Override
		public String generateString(UniEntity entity) {
			UniField leftField = entity.fieldNamed(_leftKey);
			UniField rightField = entity.fieldNamed(_rightKey);
			String opString = operatorString();
			return leftField.columnName() + " " + opString + " " + rightField.columnName();
		}

		@Override
		public boolean matches(Object object, UniEntity entity) {
			return Util.matches(FieldAccess.DefaultImplementation.getValueForKeyPath(object, _leftKey), FieldAccess.DefaultImplementation.getValueForKeyPath(object, _rightKey), _operator);
		}

		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			UniField leftField = entity.fieldNamed(_leftKey);
			UniField rightField = entity.fieldNamed(_rightKey);
			return Util.matches(Util.getRowValue(row, leftField.columnName()), Util.getRowValue(row, rightField.columnName()), _operator);
		}

	}
	
	public abstract class Junction implements UniPredicate {
		List<UniPredicate> _predicates;
		
		public Junction(List<UniPredicate> predicates) {
			this._predicates = predicates;
		}
		
		protected Junction(UniPredicate...predicates) {
			this._predicates = Arrays.asList(predicates);
		}
		
		protected List<String> listGeneratedStrings(UniEntity entity) {
			List<String> list = ListUtils.list();
			for(UniPredicate predicate : _predicates) {
				list.add(predicate.generateString(entity));
			}
			return list;
		}
		
		public List<UniPredicate> predicates() {
			return _predicates;
		}
	}
	
	public class And extends Junction {

		public And(List<UniPredicate> predicates) {
			super(predicates);
		}
		public And(UniPredicate...predicates) {
			super(predicates);
		}
		
		@Override
		public String generateString(UniEntity entity) {
			List<String> strings = listGeneratedStrings(entity);
			if(strings.size() == 0)
				return null;
			if(strings.size() == 1)
				return strings.get(0);
			String junction = ListUtils.listToString(strings, " AND ");
			return "(" + junction + ")";
		}
		
		@Override
		public boolean matches(Object object, UniEntity entity) {
			for(UniPredicate p : _predicates) {
				if(!p.matches(object, entity))
					return false;
			}
			return true;
		}
		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			for(UniPredicate p : _predicates) {
				if(!p.matchesToRow(row, entity))
					return false;
			}
			return true;
		}
		
	}
	
	public class Not implements UniPredicate {
		UniPredicate _predicate;
		
		public Not(UniPredicate predicate) {
			_predicate = predicate;
		}
		
		public UniPredicate predicate() {
			return _predicate;
		}

		@Override
		public String generateString(UniEntity entity) {
			return "NOT " + _predicate.generateString(entity);
		}

		@Override
		public boolean matches(Object object, UniEntity entity) {
			return !_predicate.matches(object, entity);
		}

		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			return !_predicate.matchesToRow(row, entity);
		}
	}

	public class Or extends Junction {

		public Or(List<UniPredicate> predicates) {
			super(predicates);
		}
		public Or(UniPredicate[] predicates) {
			super(predicates);
		}
		
		@Override
		public String generateString(UniEntity entity) {
			List<String> strings = listGeneratedStrings(entity);
			if(strings.size() == 0)
				return null;
			if(strings.size() == 1)
				return strings.get(0);
			String junction = ListUtils.listToString(strings, " OR ");
			return "(" + junction + ")";
		}
		@Override
		public boolean matches(Object object, UniEntity entity) {
			for(UniPredicate p : _predicates) {
				if(p.matches(object, entity))
					return true;
			}
			return false;
		}
		@Override
		public boolean matchesToRow(Map<String, Object> row, UniEntity entity) {
			for(UniPredicate p : _predicates) {
				if(p.matchesToRow(row, entity))
					return true;
			}
			return false;
		}
	}
		
	public static class Util {

		public static UniPredicate createPredicateFromFieldValues( Map<String, Object> fieldValues) {
			if(fieldValues.size() == 0)
				return null;
			if(fieldValues.size() == 1) {
				Map.Entry<String, Object> entry = fieldValues.entrySet().iterator().next();
				return new UniPredicate.KeyValue(entry.getKey(), entry.getValue());
			} else {
				List<UniPredicate> predicates = ListUtils.list();
				for(Map.Entry<String, Object> entry : fieldValues.entrySet()) {
					predicates.add(new UniPredicate.KeyValue(entry.getKey(), entry.getValue()));
				}
				return new UniPredicate.And(predicates);
			}
		}
		
		public static List filteredObjects(List objects, UniEntity entity, UniPredicate predicate) {
			List filteredObjects = ListUtils.list();
			for(Object object : objects) {
				if(predicate.matches(object, entity))
					filteredObjects.add(object);
			}
			return filteredObjects;
		}
				
		public static Object getRowValue(Map<String, Object>row,  String key) {
			return row.get(key);
		}
		
		public static boolean matches(Object left, Object right, Operator op) {
			if(left == null && right == null) {
				if(op == Operator.Eq)
					return true;
				if(op == Operator.Neq)
					return false;
			}
			if(left != null && right != null) {
				if(op == Operator.Eq)
					return left.equals(right);
				if(op == Operator.Neq)
					return !left.equals(right);
				if(op == Operator.Gt && left instanceof Comparable && right instanceof Comparable)
					return ((Comparable)left).compareTo((Comparable)right) > 0;
				if(op == Operator.Lt && left instanceof Comparable && right instanceof Comparable)
					return ((Comparable)left).compareTo((Comparable)right) < 0;
				if(op == Operator.Gte && left instanceof Comparable && right instanceof Comparable)
					return ((Comparable)left).compareTo((Comparable)right) >= 0;
				if(op == Operator.Lte && left instanceof Comparable && right instanceof Comparable)
					return ((Comparable)left).compareTo((Comparable)right) <= 0;
				if(op == Operator.EndsWith && left instanceof String && right instanceof String)
					return ((String)left).endsWith((String)right);
				if(op == Operator.StartsWith && left instanceof String && right instanceof String)
					return ((String)left).startsWith((String)right);
				if(op == Operator.Contains && left instanceof String && right instanceof String)
					return ((String)left).indexOf((String)right) >= 0;
				if(op == Operator.Like && left instanceof String && right instanceof String) {
					int index = ((String) right).indexOf("...");
					String rightStr = (String) right;
					if(index == 0)
						rightStr = rightStr.substring(3);
					else if(index > 0)
						rightStr = rightStr.substring(0, index);
					return ((String) left).indexOf(rightStr) >= 0;
				}
			}
				
			return false;
		}

	}

}

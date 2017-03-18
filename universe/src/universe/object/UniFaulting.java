package universe.object;

import universe.UniContext;
import core.util.FieldAccess;

public interface UniFaulting {

	public Object getStoredValueForRelationshipWithKey(String key);
	
	public static class DefaultImplementation {
		
		public Object getStoredValueForRelationshipWithKey(Object object, String key) {
			Object value = FieldAccess.DefaultImplementation.getValueForKey(object, key);
			if(value == null) {
				value = UniContext.get().storedValueForToOneRelationship(object, key);
				FieldAccess.DefaultImplementation.setValueForKey(object, value, key);
			}
			return value;
		}
	}
}

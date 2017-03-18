package universe;

import java.util.Map;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	Map<String, Object> _userInfo;
	UniDatabaseOperation _operation;
	
	public UniValidationException(String messsage, Throwable throwable, Map<String, Object> userInfo) {
		super(messsage, throwable);
		_userInfo = userInfo;
	}
	
	public UniValidationException(String messsage, Map<String, Object> userInfo) {
		super(messsage);
		_userInfo = userInfo;
	}
	
	public UniValidationException(Throwable throwable, Map<String, Object> userInfo) {
		super(throwable);
		_userInfo = userInfo;
	}
	
	public Map<String, Object> userInfo() {
		return _userInfo;
	}

	public void setDatabaseOperation(UniDatabaseOperation operation) {
		_operation = operation;
	}
	public UniDatabaseOperation databaseOperation() {
		return _operation;
	}
	

}

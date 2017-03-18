package workflow;

public class WorkflowException extends Exception {

	int errorCode;
	
	public WorkflowException(int errorCode, String errorDescription, Throwable exception) {
		super(errorDescription, exception);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
	public String getErrorDescription() {
		return this.getMessage();
	}
	public String getException() {
		if(this.getCause() != null)
			return this.getCause().getMessage() == null ? this.getCause().toString() : this.getCause().getMessage();
		return null;
	}

	private static final long serialVersionUID = 1L;

}

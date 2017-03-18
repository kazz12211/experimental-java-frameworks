package workflow.controller.trigger;

public interface Trigger {

	public static final String STAGE_CREATE = "create";
	public static final String STAGE_SAVE = "save";
	public static final String STAGE_SUBMIT = "submit";
	public static final String STAGE_COMPLETE = "complete";
	public static final String STAGE_REJECT = "reject";
	public static final String STAGE_EXPIRE = "expire";
	public static final String STAGE_REQUEST = "request";
	public static final String STAGE_WITHDRAW = "withdraw";
	public static final String STAGE_DELETE = "delete";
	public static final String STAGE_UNDELETE = "undelete";
	public static final String STAGE_CHANGE = "change";
	public static final String STAGE_ERROR = "error";
	
	public void fire(Object model);
		
}

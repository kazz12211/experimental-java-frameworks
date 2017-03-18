package workflow;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import workflow.csv.CSVConsumer;
import workflow.csv.CSVReader;
import ariba.ui.aribaweb.core.AWServerApplication;
import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.util.core.MapUtil;
import ariba.util.log.Log;

public class WorkflowError implements CSVConsumer {

	public static final int UNCAUGHT_EXCEPTION = 30000;

	public static final int NO_WORKFLOW_DEF = 1000;
	public static final int NO_RULE_DEF = 1001;
	public static final int INSTANTIATE_WORKFLOW_FAILED = 1002;
	public static final int INSTANTIATE_REQUEST_FAILED = 1003;
	public static final int INSTANTIATE_ACTIVITY_FAILED = 1004;
	public static final int COMPLETE_WORKFLOW_FAILED = 1005;
	public static final int REJECT_WORKFLOW_FAILED = 1006;
	public static final int SAVE_WORKFLOW_FAILED = 1007;
	public static final int SUBMIT_WORKFLOW_FAILED = 1008;
	public static final int SUBMIT_REQUEST_FAILED = 1009;
	public static final int SUBMIT_SUBSEQUENT_REQUEST_FAILED = 1010;
	public static final int REJECT_REQUEST_FAILED = 1011;
	public static final int SUBMIT_A_REQUEST_FAILED = 1012;
	public static final int INSTANTIATE_TRIGGER_ERROR = 1013;
	public static final int FIRE_TRIGGER_ERROR = 1014;
	public static final int WORKFLOW_TRIGGER_ERROR = 1015;
	public static final int REQUEST_TRIGGER_ERROR = 1016;
	public static final int ACTIVITY_TRIGGER_ERROR = 1017;
	public static final int EXPIRE_REQUEST_FAILED = 1018;
	public static final int EXPIRE_WORKFLOW_FAILED = 1019;

	public static final int SAVE_REQUEST_FAILED = 1020;

	public static final int INVALID_ROLE_ASSIGNED = 1021;


	private static WorkflowError _sharedInstance = new WorkflowError();
	private Map<String, String> data;
	
	protected WorkflowError() {
		data = MapUtil.map();
		load();
	}
	private void load() {
		InputStream is;
		try {
			is = this.getInputStream();
			CSVReader reader = new CSVReader(this);
			reader.read(is, "UTF-8");
		} catch (IOException e) {
			Log.customer.error("WorkflowError: could not load error messages.", e);
			data.put("30000", "System error");
		}
	}
	private InputStream getInputStream() throws IOException {
		AWServerApplication app = AWServletApplication.sharedInstance();
		AWResource resource = app.resourceManager().resourceNamed("errors.table");
		String full = resource.fullUrl();
		URL url = new URL(full);
		return url.openStream();
	}
	public static String getDescription(int errorCode) {
		return _sharedInstance._getDescription(errorCode);
	}

	private String _getDescription(int errorCode) {
		String code = Integer.toString(errorCode);
		String descr = data.get(code);
		if(descr == null) {
			descr = _getDescription(UNCAUGHT_EXCEPTION);
		}
		return descr;
	}
	@Override
	public void consumeLineOfTokens(String file, int row, List<String> line)
			throws Exception {
		if(line.size() >=2) {
			String code = line.get(0);
			String mess = line.get(1);
			data.put(code, mess);
		} else {
			Log.customer.error("WorkflowError: CSV loading error at line " + (row+1));
		}
	}

}

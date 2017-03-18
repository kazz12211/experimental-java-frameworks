package workflow.controller;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import workflow.app.AppConfigManager;

public class Trace {

	private static final String TRACE_FILE = AppConfigManager.RESOURCE_PATH + "logs" + "/" + "WorkflowDemo.trace";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Trace _sharedInstance = new Trace();
	private boolean debug;
	
	private Trace() {
		Object flag = (String) AppConfigManager.getInstance().get(AppConfigManager.RULE_TRACE);
		debug = (flag != null && ("true".equalsIgnoreCase(flag.toString()) || Boolean.TRUE.equals(flag)));
	}
	
	public static void writeLog(String message) {
		if(_sharedInstance.debug) {
			_sharedInstance.log(message);
		}
	}
	
	public synchronized void log(String message) {
		File file = new File(TRACE_FILE);
		if(!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(file.exists() && file.canWrite()) {
			FileWriter writer = null;
			try {
				writer = new FileWriter(file, true);
				writer.write(DATE_FORMAT.format(new Date()));
				writer.write(" ");
				writer.write(message);
				writer.write("\n");
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(writer != null) {
					try {
						writer.close();
					} catch (IOException ignore) {}
				}
			}
		}
	}
}

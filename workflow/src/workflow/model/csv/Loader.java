package workflow.model.csv;

import java.io.IOException;
import java.io.InputStream;

import workflow.csv.CSVConsumer;
import workflow.csv.CSVReader;
import ariba.ui.meta.persistence.ObjectContext;

public abstract class Loader implements CSVConsumer {

	protected ObjectContext oc;

	protected Loader(ObjectContext oc) {
		this.oc = oc;
	}
	
	protected abstract String getResourceName();
	
	protected InputStream getInputStream(String resourceName) {
		return this.getClass().getResourceAsStream(resourceName);
	}
	
	protected InputStream getInputStream() {
		return this.getInputStream(this.getResourceName());
	}
	
	public void load() throws IOException {
		InputStream is = this.getInputStream();
		CSVReader reader = new CSVReader(this);
		reader.read(is, "UTF-8");
	}
}

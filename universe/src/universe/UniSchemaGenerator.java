package universe;

import universe.util.UniFileUtil;
import asjava.uniobjects.UniFile;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniSchemaGenerator implements UniSchemaGeneration {

	private UniObjectsSession _session;

	public UniSchemaGenerator(UniObjectsSession session) {
		_session = session;
	}
	
	public UniObjectsSession session() {
		return _session;
	}
	
	private UniFile createFile(String filename) throws Exception {
		UniFile file = null;
		if(!UniFileUtil.exists(filename, session().uniSession())) {
			session().executeUniCommand("CREATE.FILE " + filename + " 2 1 1");
		}
		file = session().getFile(filename);
		return file;
	}
	
	@Override
	public void generateSchema(UniEntity entity) throws Exception {
		String filename = entity.filename();
		UniField fields[] = entity.fields();
		int location = 1;
		for(UniField field : fields) {
			if(field.isPrimaryKey())
				continue;
			createField(filename, field, location++);
		}
	}

	private void createField(String filename, UniField field, int location) throws Exception {
		String replies[] = new String[11];
		replies[0] = field.columnName();
		replies[1] = "D";
		replies[2] = Integer.toString(location);
		replies[3] = "";
		replies[4] = field.columnName();
		replies[5] = "";
		replies[6] = field.isMultiValue() ? "M" : "S";
		replies[7] = "";
		replies[8] = "";
		replies[9] = "";
		replies[10] = "";
		session().executeUniCommand("REVISE.DICT " + filename, replies);
	}

}

package universe;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import universe.util.UniLogger;
import core.util.StringUtils;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniObjectsBasic {
	String _program;
	File _programFile;
	String _programName;
	String _storageName;
	
	public String program() { return _program; }
	public void setProgram(String program) { _program = program; }
	public File programFile() { return _programFile; }
	public void setProgramFile(File programFile) { _programFile = programFile; }
	public String programName() { return _programName; }
	public void setProgramName(String programName) { _programName = programName; }
	public String storageName() { return _storageName; }
	public void setStorageName(String storageName) { _storageName = storageName; }
	
	public String sourceCode() {
		if(StringUtils.nullOrEmpty(_program)) {
			try {
				return sourceCodeFromFile();
			} catch (IOException e) {
				UniLogger.universe.error("Could not load Basic program source from file '" + _programFile.getName() + "'", e);
				return "";
			}
		}
		return _program;
	}
	
	private String sourceCodeFromFile() throws IOException {
		FileReader reader = new FileReader(_programFile);
		BufferedReader br = new BufferedReader(reader);
		StringBuffer buffer = new StringBuffer();
		String line = null;
		while((line = br.readLine()) != null) {
			buffer.append(line);
			buffer.append("\n");
		}
		br.close();
		reader.close();
		return buffer.toString();
	}
	
}

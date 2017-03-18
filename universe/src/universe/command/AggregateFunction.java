package universe.command;

import asjava.uniobjects.UniCommand;
import universe.UniCommandGeneration;
import universe.UniObjectsCommand;
import universe.UniQuerySpecification;

/**
 * 
 * @author ktsubaki
 *
 */
public class AggregateFunction extends UniObjectsCommand {
	String key;
	String funcName;
	UniQuerySpecification spec;

	public AggregateFunction(String key, String funcName, UniQuerySpecification spec) {
		this.key = key;
		this.funcName = funcName;
		this.spec = spec;
	}
	
	public UniQuerySpecification querySpecification() {
		return spec;
	}
	public String key() {
		return key;
	}
	public String functionName() {
		return funcName;
	}
	
	@Override
	public UniCommand uniCommand(UniCommandGeneration generator)  throws Exception {
		return generator.generate(this);
	}
}

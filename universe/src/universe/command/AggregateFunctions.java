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
public class AggregateFunctions extends UniObjectsCommand {
	String key;
	UniQuerySpecification spec;

	public AggregateFunctions(String key, UniQuerySpecification spec) {
		this.key = key;
		this.spec = spec;
	}
	
	public UniQuerySpecification querySpecification() {
		return spec;
	}
	public String key() {
		return key;
	}
	
	@Override
	public UniCommand uniCommand(UniCommandGeneration generator)  throws Exception {
		return generator.generate(this);
	}
}

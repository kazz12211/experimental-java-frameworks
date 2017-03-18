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
public class Count extends UniObjectsCommand {

	UniQuerySpecification spec;

	public Count(UniQuerySpecification spec) {
		this.spec = spec;
	}

	public UniQuerySpecification querySpecification() {
		return spec;
	}
	
	@Override
	public UniCommand uniCommand(UniCommandGeneration generator)  throws Exception {
		return generator.generate(this);
	}

}

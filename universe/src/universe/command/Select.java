package universe.command;

import asjava.uniobjects.UniCommand;
import universe.UniCommandGeneration;
import universe.UniEntity;
import universe.UniObjectsCommand;
import universe.UniQuerySpecification;

/**
 * 
 * @author ktsubaki
 *
 */
public class Select extends UniObjectsCommand {
	int listNumber = 0;
	UniQuerySpecification spec;
	
	public Select(String filename,String condition, int listNumber) {
		this.listNumber = listNumber;
	}
	
	public Select(UniQuerySpecification spec) {
		this.spec = spec;
	}

	public int listNumber() {
		if(spec != null)
			return spec.listNumber();
		return listNumber;
	}
	
	public UniQuerySpecification querySpecification() {
		return spec;
	}
	
	@Override
	public UniCommand uniCommand(UniCommandGeneration generator)  throws Exception {
		return generator.generate(this);
	}

	public String filename() {
		UniEntity entity = spec.entity();
		return entity.filename();
	}

}

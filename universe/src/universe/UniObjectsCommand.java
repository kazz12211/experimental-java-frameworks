package universe;

import asjava.uniobjects.UniCommand;

/**
 * 
 * @author ktsubaki
 *
 */
public abstract class UniObjectsCommand {
	public abstract UniCommand uniCommand(UniCommandGeneration generator) throws Exception;
}

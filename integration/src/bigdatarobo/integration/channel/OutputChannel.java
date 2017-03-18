package bigdatarobo.integration.channel;

import bigdatarobo.integration.message.BusMessage;


public abstract class OutputChannel extends Channel {

	public abstract void send(BusMessage message) throws Exception;
	
}

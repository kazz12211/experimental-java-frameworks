package bigdatarobo.integration.bus;

import bigdatarobo.integration.IntegrationBus;
import bigdatarobo.integration.channel.Channel;
import bigdatarobo.integration.message.BusMessage;

public abstract class OutboundListener extends BusListener {

	@Override
	public boolean isOutbound() { return true; }
	
	public abstract void complete(IntegrationBus bus, Channel channel, BusMessage message);
	public abstract void willSend(IntegrationBus bus, Channel channel, BusMessage message);

}

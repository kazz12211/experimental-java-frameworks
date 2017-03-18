package bigdatarobo.integration.bus;

import bigdatarobo.integration.IntegrationBus;
import bigdatarobo.integration.channel.Channel;
import bigdatarobo.integration.message.BusMessage;


public abstract class InboundListener extends BusListener {

	@Override
	public boolean isInbound() { return true; }
	
	public abstract void onMessage(IntegrationBus bus, Channel channel, BusMessage message);
}

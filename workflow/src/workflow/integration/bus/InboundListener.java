package workflow.integration.bus;

import workflow.integration.IntegrationBus;
import workflow.integration.channel.Channel;
import workflow.integration.message.BusMessage;


public abstract class InboundListener extends BusListener {

	@Override
	public boolean isInbound() { return true; }
	
	public abstract void onMessage(IntegrationBus bus, Channel channel, BusMessage message);
}

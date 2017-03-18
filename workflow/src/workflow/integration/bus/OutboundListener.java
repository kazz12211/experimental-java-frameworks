package workflow.integration.bus;

import workflow.integration.IntegrationBus;
import workflow.integration.channel.Channel;
import workflow.integration.message.BusMessage;

public abstract class OutboundListener extends BusListener {

	@Override
	public boolean isOutbound() { return true; }
	
	public abstract void complete(IntegrationBus bus, Channel channel, BusMessage message);
	public abstract void willSend(IntegrationBus bus, Channel channel, BusMessage message);

}

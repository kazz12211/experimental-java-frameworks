package workflow.integration.bus;

import workflow.integration.IntegrationBus;
import workflow.integration.channel.Channel;
import workflow.integration.message.BusMessage;
import workflow.integration.message.MessageType;

public abstract class BusListener {
	
	private MessageType messageType;

	public MessageType getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	
	public boolean isInbound() { return false; }
	public boolean isOutbound() { return false; }
	
	public abstract void failed(IntegrationBus bus, Channel channel, BusMessage message, Throwable error);

}

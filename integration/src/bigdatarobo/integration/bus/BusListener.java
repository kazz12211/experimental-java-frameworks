package bigdatarobo.integration.bus;

import bigdatarobo.integration.IntegrationBus;
import bigdatarobo.integration.channel.Channel;
import bigdatarobo.integration.message.BusMessage;
import bigdatarobo.integration.message.MessageType;


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

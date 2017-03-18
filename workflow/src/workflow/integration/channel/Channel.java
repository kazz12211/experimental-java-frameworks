package workflow.integration.channel;

import workflow.integration.message.MessageType;

public abstract class Channel {

	private MessageType messageType;
	
	public void setMessageType(MessageType messageType) {
		this.messageType = messageType;
	}
	public MessageType getMessageType() {
		return messageType;
	}
	public boolean isAsync() { return false; }
}

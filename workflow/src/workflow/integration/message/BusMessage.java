package workflow.integration.message;

import java.io.Serializable;

public abstract class BusMessage implements Serializable {

	private static final long serialVersionUID = 1L;

	private MessageType type;
	
	public void setType(MessageType type) {
		this.type = type;
	}
	public MessageType getType() {
		return type;
	}
	
	public boolean isOutgoing() { return false; }
	public boolean isIncoming() { return false; }
}

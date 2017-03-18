package bigdatarobo.integration.message;

public class MessageType {

	private String type;
	private String subType;
	
	public static final String ANONYMOUS_TYPE = "*";
	
	public MessageType(String type, String subType) {
		this.type = type;
		this.subType = subType;
		if(subType == null) {
			this.subType = ANONYMOUS_TYPE;
		}
	}
	
	public String getType() {
		return type;
	}
	public String getSubType() {
		return subType;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj instanceof MessageType) {
			MessageType mt = (MessageType)obj;
			return (mt.getType().equals(this.type) && mt.getSubType().equals(this.subType));
		}
		return false;
	}

}

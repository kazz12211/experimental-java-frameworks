package workflow.controller.rule;

public abstract class RoleDef {
	String name;
	String type;
	
	public RoleDef(String name, String type) {
		this.name = name;
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	public String getType() {
		return type;
	}

	public String getNameAndType() {
		return name + ":" + type;
	}
}

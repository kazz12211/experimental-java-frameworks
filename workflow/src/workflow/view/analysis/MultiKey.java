package workflow.view.analysis;

public class MultiKey {
	public String modelName;
	public String userName;
	public Integer year;
	public Integer month;
	public String status;

	public MultiKey(String modelName, String userName, Integer year, Integer month, String status) {
		this.modelName = modelName;
		this.userName = userName;
		this.year = year;
		this.month = month;
		this.status = status;
	}
	
	

	public boolean matches(MultiKey key) {
		return modelName.compareTo(key.modelName) == 0 &&
				userName.compareTo(key.userName) == 0 &&
				year.compareTo(key.year) == 0 &&
				month.compareTo(key.month) == 0 &&
				status.compareTo(key.status) == 0;
	}



	public String toString() {
		return "<" + modelName + ", " + userName + ", " + year + ", " + month + ", " + status + ">";
	}

}

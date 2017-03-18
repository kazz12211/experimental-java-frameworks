package workflow.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import core.BaseObject;

@Entity
public class Transition extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	
	@ManyToOne
	Workflow workflow;
	
	@ManyToOne
	Request source;
	@ManyToOne
	Request destination;
		
	public Workflow getWorkflow() {
		return workflow;
	}
	public void setWorkflow(Workflow workflow) {
		this.workflow = workflow;
	}
	public Request getSource() {
		return source;
	}
	public void setSource(Request source) {
		this.source = source;
	}
	public Request getDestination() {
		return destination;
	}
	public void setDestination(Request destination) {
		this.destination = destination;
	}
		
	public Long getId() {
		return id;
	}
	
	
}

package workflow.model;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import core.BaseObject;
import core.util.ListUtils;

@Entity
public class Folder extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;

	@ManyToOne
	User owner;
	String name;
	@OneToMany
	List<Workflow> workflows;
	
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Workflow> getWorkflows() {
		if(workflows == null)
			workflows = ListUtils.list();
		return workflows;
	}
	public void setWorkflows(List<Workflow> workflows) {
		this.workflows = workflows;
	}
	public Long getId() {
		return id;
	}
	
	public List<Workflow> filteredWorkflows() {
		List<Workflow> list = ListUtils.list();
		for(Workflow workflow : this.getWorkflows()) {
			if(!workflow.isArchived() && !workflow.isDeleted() && !workflow.isHidden())
				list.add(workflow);
		}
		return list;
	}
	
	public void addWorkflow(Workflow workflow) {
		this.getWorkflows().add(workflow);
		workflow.setFolder(this);
	}
	
	public void removeWorkflow(Workflow workflow) {
		this.getWorkflows().remove(workflow);
		workflow.setFolder(null);
	}
	
}

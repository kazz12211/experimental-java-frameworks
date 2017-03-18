package workflow.model;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Query;

import core.BaseObject;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.log.Log;

@Entity
@NamedQueries({
    @NamedQuery(name="deleteAllActivitySummary",
    		query="DELETE FROM AnalysisSummaryActivity wf")
})
public class AnalysisSummaryActivity extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	String workflowClass;
	String modelClass;
	String modelName;
	Integer year;
	Integer month;
	Integer day;
	String actorId;
	String statusCode;
	Long processTimeTotal;
	Long minProcessTime;
	Long maxProcessTime;
	Long numberOfActivity;
	
	public String getWorkflowClass() {
		return workflowClass;
	}
	public void setWorkflowClass(String workflowClass) {
		this.workflowClass = workflowClass;
	}
	public String getModelClass() {
		return modelClass;
	}
	public void setModelClass(String modelClass) {
		this.modelClass = modelClass;
	}
	public String getModelName() {
		return modelName;
	}
	public void setModelName(String modelName) {
		this.modelName = modelName;
	}
	public Integer getYear() {
		return year;
	}
	public void setYear(Integer year) {
		this.year = year;
	}
	public Integer getMonth() {
		return month;
	}
	public void setMonth(Integer month) {
		this.month = month;
	}
	public Integer getDay() {
		return day;
	}
	public void setDay(Integer day) {
		this.day = day;
	}
	public String getActorId() {
		return actorId;
	}
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}
	public Long getProcessTimeTotal() {
		return processTimeTotal;
	}
	public void setProcessTimeTotal(Long processTimeTotal) {
		this.processTimeTotal = processTimeTotal;
	}
	public Long getMinProcessTime() {
		return minProcessTime;
	}
	public void setMinProcessTime(Long minProcessTime) {
		this.minProcessTime = minProcessTime;
	}
	public Long getMaxProcessTime() {
		return maxProcessTime;
	}
	public void setMaxProcessTime(Long maxProcessTime) {
		this.maxProcessTime = maxProcessTime;
	}
	public Long getNumberOfActivity() {
		return numberOfActivity;
	}
	public void setNumberOfActivity(Long numberOfActivity) {
		this.numberOfActivity = numberOfActivity;
	}
	public Long getId() {
		return id;
	}
	
	public static boolean deleteAll() {
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			Query query = em.createNamedQuery("deleteAllActivitySummary");
			query.executeUpdate();
			et.commit();
			return true;
		} catch (Exception ex) {
			et.rollback();
			Log.customer.error("AnalysisSummaryActivity.deleteAll: could not delete data.", ex);
			return false;
		} 
	}

	public Actor getActor() {
		Actor actor = User.userWithUUID(actorId);
		if(actor == null)
			actor = Role.getRole(actorId);
		return actor;
	}
	public Status getStatus() {
		return Status.get(statusCode);
	}
}

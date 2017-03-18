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
    @NamedQuery(name="deleteAllWorkflowSummary",
    		query="DELETE FROM AnalysisSummaryWorkflow wf")
})
public class AnalysisSummaryWorkflow extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;
	String modelClass;
	String modelName;
	Integer year;
	Integer month;
	Integer day;
	String creatorId;
	String statusCode;
	
	Long numberOfRequests;
	Long processTimeTotal;
	Long minProcessTime;
	Long maxProcessTime;
	Long numberOfProcess;
	
	
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


	public String getCreatorId() {
		return creatorId;
	}


	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}


	public Long getNumberOfRequests() {
		return numberOfRequests;
	}


	public void setNumberOfRequests(Long numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
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


	public Long getNumberOfProcess() {
		return numberOfProcess;
	}


	public void setNumberOfProcess(Long numberOfProcess) {
		this.numberOfProcess = numberOfProcess;
	}
	
	public String getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public Long getId() {
		return id;
	}
	
	public static boolean deleteAll() {
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			Query query = em.createNamedQuery("deleteAllWorkflowSummary");
			query.executeUpdate();
			et.commit();
			return true;
		} catch (Exception ex) {
			et.rollback();
			Log.customer.error("AnalysisSummaryWorkflow.deleteAll: could not delete data.", ex);
			return false;
		} 
	}
	
	public Long getAverageProcessTime() {
		long total = processTimeTotal.longValue();
		long count = numberOfProcess;
		return new Long(total / count);
	}
	public User getCreator() {
		return User.userWithUUID(creatorId);
	}
	public Status getStatus() {
		return Status.get(statusCode);
	}
}

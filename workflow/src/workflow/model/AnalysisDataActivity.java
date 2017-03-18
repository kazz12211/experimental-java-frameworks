package workflow.model;

import java.util.Date;
import java.util.List;

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
import core.util.DateUtils;
import core.util.ListUtils;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.log.Log;

@Entity
@NamedQueries({
    @NamedQuery(name="deleteActivityData",
    		query="DELETE FROM AnalysisDataActivity wf " + 
    			"WHERE wf.requestedDate>= ?1 AND wf.requestedDate< ?2"),
    @NamedQuery(name="sumActivityData",
    		query = "SELECT year, month, day, workflowClass, modelClass, modelName, actorId, processedStatusCode, sum(processTime/1000) as totalProcessTime, max(processTime)/1000 as maxProcessTime, min(processTime)/1000 as minProcessTime, count(*) as numActivities FROM AnalysisDataActivity GROUP BY year, month, day, workflowCLass, modelClass, modelName, actorId, processedStatusCode"
    		)
})

public class AnalysisDataActivity extends BaseObject {

	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE)
	Long id;

	String workflowClass;
	String modelClass;
	String modelName;
	String actorId;
	Date requestedDate;
	Date submittedDate;
	Long processTime;
	String processedStatusCode;
	Integer year;
	Integer month;
	Integer day;
	
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
	public String getActorId() {
		return actorId;
	}
	public void setActorId(String actorId) {
		this.actorId = actorId;
	}
	public Date getRequestedDate() {
		return requestedDate;
	}
	public void setRequestedDate(Date requestedDate) {
		this.requestedDate = requestedDate;
	}
	public Date getSubmittedDate() {
		return submittedDate;
	}
	public void setSubmittedDate(Date submittedDate) {
		this.submittedDate = submittedDate;
	}
	public Long getProcessTime() {
		return processTime;
	}
	public void setProcessTime(Long processTime) {
		this.processTime = processTime;
	}
	public String getProcessedStatusCode() {
		return processedStatusCode;
	}
	public void setProcessedStatusCode(String processedStatusCode) {
		this.processedStatusCode = processedStatusCode;
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
	public Long getId() {
		return id;
	}
	
	public static boolean deleteRange(Date start, Date end) {
		Date s = DateUtils.startTimeOfTheDay(start);
		Date e = DateUtils.endTimeOfTheDay(end);
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		EntityTransaction et = em.getTransaction();
		try {
			et.begin();
			Query query = em.createNamedQuery("deleteActivityData");
			query.setParameter(1, s);
			query.setParameter(2, e);
			query.executeUpdate();
			et.commit();
			return true;
		} catch (Exception ex) {
			et.rollback();
			Log.customer.error("AnalysisDataActivity.deleteRange: could not delete data.", ex);
			return false;
		} 
	}
	
	public static List<?> sums() {
		List<?> list = ListUtils.list();
		EntityManager em = (EntityManager) ObjectContext.get().getDelegate();
		try {
			Query query = em.createNamedQuery("sumActivityData");
			list.addAll(query.getResultList());
		} catch (Exception ex) {
			Log.customer.error("AnalysisDataActivity.sums: could not summarize data.", ex);
		}
		return list;
	}

}

package workflow.tools;

import java.util.List;
import java.util.Map;

import rdbms.DbContext;
import rdbms.DbEntity;
import rdbms.DbModel;
import workflow.model.Activity;
import workflow.model.User;
import ariba.ui.meta.persistence.ObjectContext;
import ariba.util.core.ListUtil;
import ariba.util.core.MapUtil;
import ariba.util.log.Log;

public class MissingActivityLink {
	ObjectContext oc;
	DbContext jc;
	
	public MissingActivityLink(ObjectContext oc, DbContext jc) {
		this.oc = oc;
		this.jc = jc;
	}
	
	public List<ActorActivity> search(User user) {
		List<ActorActivity> ids = ListUtil.list();
		Long userId = user.getId();
		Map<String, Object> fieldValues = MapUtil.map();
		fieldValues.put("actorId", userId);
		List<ActorActivity> list = (List<ActorActivity>) jc.executeQuery(ActorActivity.class, fieldValues);
		for(ActorActivity aa : list) {
			Long actId = aa.getActivityId();
			Activity act = oc.find(Activity.class, actId);
			if(act == null) {
				ids.add(aa);
			}
		}
		return ids;
	}
	
	public void solve(User user) {
		List<ActorActivity> ids = this.search(user);
		for(ActorActivity aa : ids) {
			DbModel model = DbModel.modelNamed("workflow");
			DbEntity entity = model.entityForClass(ActorActivity.class);
			Map<String, Object> fieldValues = MapUtil.map();
			fieldValues.put("actorId", aa.getActorId());
			fieldValues.put("activityId", aa.getActivityId());
			try {
				ActorActivity act = (ActorActivity) jc.findOne(ActorActivity.class, fieldValues);
				jc.deleteObject(act);
				jc.saveChanges();
			} catch (Exception e) {
				Log.customer.error("MissingActivityList throws:", e);
			}
		} 
	}
}

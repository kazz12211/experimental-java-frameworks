package workflow.aribaweb.hibernate;

import java.io.Serializable;

import org.hibernate.ejb.HibernateEntityManager;

import ariba.ui.meta.persistence.ObjectContext;

public class HibernateUtil {
	
	public static void clearCache(Object object, ObjectContext oc) throws Exception {
		HibernateEntityManager em = (HibernateEntityManager) oc.getDelegate();
		//em.getSession().evict(object);
		//em.getSession().flush();
		//em.getSession().refresh(object);
		Object pk = oc.getPrimaryKey(object);
		if(pk != null && pk instanceof Serializable) {
			em.getSession().getSessionFactory().evict(object.getClass(), (Serializable) pk);
		}
	}

	
	public static Object reloadObject(Object object, ObjectContext oc) throws Exception{
		clearCache(object, oc);
		Object pk = oc.getPrimaryKey(object);
		return oc.find(object.getClass(), pk);
	}
	
	public static Object reloadObject(Class<?> objectClass, Object primaryKey, ObjectContext oc) throws Exception {
		Object obj = oc.find(objectClass, primaryKey);
		return reloadObject(obj, oc);
	}
}

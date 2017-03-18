/*
 * Created on 2003/11/27
 *
 */
package core.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

/**
 * @author ktsubaki
 *
 * 
 */
public class Notification implements Externalizable, Comparable {
	
	private String name = null;
	private Object object = null;
	private Map userInfo = null;
	
	/**
	 * 
	 */
	protected Notification() {
		super();
	}
	
	/**
	 * @param aName
	 * @param anObject
	 * @param anUserInfo
	 */
	public Notification(String aName, Object anObject, Map anUserInfo) {
		this();
		if(aName != null) {
			name = aName;
			object = anObject;
			userInfo = anUserInfo;
		} else {
			throw new IllegalArgumentException("Notification: name is null.");
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object o) {
		if(o != null) {
			if(o instanceof Notification) {
				return getName().compareTo(((Notification)o).getName());
			}
			throw new IllegalArgumentException("Notification compareTo: '" + o.getClass().getName() + "' is not an instance of '" + Notification.class + "'.");
		}
		throw new IllegalArgumentException("Notification.compareTo: null object.");
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(getName());
		out.writeObject(getObject());
		out.writeObject(getUserInfo());		
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		setName((String)in.readObject());
		setObject(in.readObject());
		setUserInfo((Map)in.readObject());		
	}
	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return
	 */
	public Object getObject() {
		return object;
	}

	/**
	 * @return
	 */
	public Map getUserInfo() {
		return userInfo;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param object
	 */
	public void setObject(Object object) {
		this.object = object;
	}

	/**
	 * @param map
	 */
	public void setUserInfo(Map map) {
		userInfo = map;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getName().hashCode();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object anObject) {
		if(this == anObject)
			return true;
		
		if(anObject instanceof Notification) {
			Notification notif = (Notification)anObject;
			
			if(getName().equals(notif.getName())) {
				Object obj = getObject();
				Object notifObj = notif.getObject();
				Map info = getUserInfo();
				Map notifInfo = notif.getUserInfo();
				
				if(obj != null && obj.equals(notifObj) == false) {
					return false;
				}
				
				if(info != null && info.equals(notifInfo) == false) {
					return false;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}
}

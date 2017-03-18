/*
 * Created on 2003/11/27
 *
 */
package core.util;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * @author ktsubaki
 *
 * 
 */
public class NotificationCenter {
	
	private static final NotificationCenter defaultCenter = new NotificationCenter();
	
	private final Map map = new WeakHashMap();
	
	/**
	 * 
	 */
	public NotificationCenter() {
		super();
	}
	
	/**
	 * @return
	 */
	public static NotificationCenter defaultCenter() {
		return defaultCenter;
	}

	/**
	 * @param anObserver
	 * @param aKey
	 * @param aName
	 * @param anObject
	 */
	private synchronized void registerObserverWithKey(NotificationObserver anObserver, Object aKey, String aName, Object anObject) {
		if ( anObserver != null )
		{
			if ( aKey != null )
			{
				Collection	aCollection = (Collection) map.get( aKey );
					
				if ( aCollection == null )
				{
					aCollection = new HashSet();
						
					map.put( aKey, aCollection );
				}
					
				aCollection.add(new Entry( anObserver, aName, anObject ) );
					
				return;
			}
		}

		throw new IllegalArgumentException( "Notification.Center.registerObserverForNotificationName: null observer." );
	}
		
	/**
	 * @param anObserver
	 * @param aName
	 * @param anObject
	 */
	public void addObserver(NotificationObserver anObserver, String aName, Object anObject)
	{
		if ( anObserver != null )
		{
			if ( aName != null )
			{
				registerObserverWithKey( anObserver, aName, aName, anObject );
			}
				
			if ( anObject != null )
			{
				registerObserverWithKey( anObserver, anObject, aName, anObject );
			}
				
			if ( ( aName == null ) && ( anObject == null ) )
			{
				registerObserverWithKey( anObserver, Null.nullValue(), aName, anObject );
			}
				
			return;
		}
			
		throw new IllegalArgumentException( "Notification.Center.addObserver: null observer." );
	}
		
	/**
	 * @param anObserver
	 * @param aKey
	 * @param aName
	 * @param anObject
	 */
	private synchronized void unregisterObserverWithKey(NotificationObserver anObserver, Object aKey, String aName, Object anObject)
	{
		if ( anObserver != null )
		{
			if ( aKey != null )
			{
				Collection	aCollection = (Collection) map.get( aKey );
					
				if ( ( aCollection != null ) && ( aCollection.isEmpty() == false ) )
				{
					Collection	someEntries = new ArrayList();
					
					for ( Iterator anIterator = aCollection.iterator(); anIterator.hasNext(); )
					{
						Entry	anEntry = (Entry) anIterator.next();
							
						if ( ( anEntry.isValid() == false ) || ( anEntry.accepts( aName, anObject ) != null ) )
						{
							someEntries.add( anEntry );
						}
					}

					if ( someEntries.isEmpty() == false )
					{
						aCollection.removeAll( someEntries );
					}
				}
					
				return;
			}
		}

		throw new IllegalArgumentException( "Notification.Center.registerObserverForNotificationName: null observer." );
	}
		
	/**
	 * @param anObserver
	 * @param aName
	 * @param anObject
	 */
	public void removeObserver(NotificationObserver anObserver, String aName, Object anObject)
	{
		if ( anObserver != null )
		{
			if ( aName != null )
			{
				unregisterObserverWithKey( anObserver, aName, aName, anObject );
			}
				
			if ( anObject != null )
			{
				unregisterObserverWithKey( anObserver, anObject, aName, anObject );
			}
				
			if ( ( aName == null ) && ( anObject == null ) )
			{
				unregisterObserverWithKey( anObserver, Null.nullValue(), aName, anObject );
			}
				
			return;
		}
			
		throw new IllegalArgumentException( "Notification.Center.removeObserver: null observer." );
	}
		
	/**
	 * @param aKey
	 * @param aName
	 * @param anObject
	 * @return
	 */
	private synchronized Collection observersForKey(Object aKey, String aName, Object anObject)
	{
		if ( aKey != null )
		{
			Collection	aCollection = (Collection) map.get( aKey );
				
			if ( aCollection != null )
			{
				Collection	someEntries = new ArrayList();
				Collection	someObservers = new ArrayList();

				for ( Iterator anIterator = aCollection.iterator(); anIterator.hasNext(); )
				{
					Entry	anEntry = (Entry) anIterator.next();
					
					if ( anEntry.isValid() == true )
					{
						NotificationObserver anObserver = anEntry.accepts( aName, anObject );
							
						if ( anObserver != null )
						{
							someObservers.add( anObserver );
						}
					}
					else
					{
						someEntries.add( anEntry );
					}
				}

				if ( someEntries.isEmpty() == false )
				{
					aCollection.removeAll( someEntries );
				}
					
				if ( someObservers.isEmpty() == false )
				{
					return someObservers;
				}
			}
				
			return null;
		}
			
		throw new IllegalArgumentException( "Notification.Center.observersForKey: null observer." );
	}
	
	/**
	 * @param name
	 */
	public void postNotification(String name) {
		postNotification(new Notification(name, null, null));
	}
	
	/**
	 * @param name
	 * @param object
	 */
	public void postNotification(String name, Object object) {
		postNotification(new Notification(name, object, null));
	}
	
	/**
	 * @param name
	 * @param object
	 * @param userInfo
	 */
	public void postNotification(String name, Object object, Map userInfo) {
		postNotification(new Notification(name, object, userInfo));
	}
	
	public void postNotification(String name, Object object, String userInfoKey, Object userInfoObject) {
		HashMap userInfo = new HashMap();
		if(userInfoObject != null)
			userInfo.put(userInfoKey, userInfoObject);
		postNotification(name, object, userInfo);
	}
	/**
	 * @param aNotification
	 */
	public void postNotification(Notification aNotification)
	{
		if ( aNotification != null )
		{
			String aName = aNotification.getName();
			Object anObject = aNotification.getObject();
			Collection aCollection = new HashSet();
			
			if ( aName != null )
			{
				Collection	someObservers = observersForKey( aName, aName, anObject );
					
				if ( someObservers != null )
				{
					aCollection.addAll( someObservers );
				}
			}
				
			if ( anObject != null )
			{
				Collection	someObservers = observersForKey( anObject, aName, anObject );
					
				if ( someObservers != null )
				{
					aCollection.addAll( someObservers );
				}
			}
				
			{
				Collection	someObservers = observersForKey( Null.nullValue(), aName, anObject );
					
				if ( someObservers != null )
				{
					aCollection.addAll( someObservers );
				}
			}
				
			if ( aCollection.isEmpty() == false )
			{
				Task	aTask = new Task( aCollection, aNotification );
				Thread	aThread = new Thread(Thread.currentThread().getThreadGroup(), aTask, aTask.getClass().getName() );
					
				aThread.setDaemon( true );
				aThread.setPriority( Thread.MIN_PRIORITY );
				aThread.start();
			}
				
			return;
		}
			
		throw new IllegalArgumentException( "Notification.Center.postNotification: null notification." );
	}
		
	/**
	 * @author ktsubaki
	 *
	 * 
	 */
	private static final class Entry extends Object
	{
		
		private Reference	observer = null;
		private String		name = null;
		private Reference	object = null;
	
		/**
		 * @param anObserver
		 * @param aName
		 * @param anObject
		 */
		private Entry(NotificationObserver anObserver, String aName, Object anObject)
		{
			super();
			
			setObserver( anObserver );
			setName( aName );
			setObject( anObject );
		}
		
		/**
		 * @return
		 */
		private NotificationObserver getObserver()
		{
			if ( observer != null )
			{
				return (NotificationObserver)observer.get();
			}
			
			return null;
		}
		
		/**
		 * @param aValue
		 */
		private void setObserver(NotificationObserver aValue)
		{
			if ( aValue != null )
			{
				observer = new WeakReference( aValue );
			}
		}
		
		/**
		 * @return
		 */
		private String getName()
		{
			return name;
		}
		
		/**
		 * @param aValue
		 */
		private void setName(String aValue)
		{
			name = aValue;
		}
		
		/**
		 * @return
		 */
		private Object getObject()
		{
			if ( object != null )
			{
				return object.get();
			}
			
			return null;
		}
		
		/**
		 * @param aValue
		 */
		private void setObject(Object aValue)
		{
			if ( aValue != null )
			{
				object = new WeakReference( aValue );
			}
		}
		
		/**
		 * @return
		 */
		private boolean isValid()
		{
			NotificationObserver anObserver = getObserver();
			
			if ( anObserver != null )
			{
				return true;
			}
			
			return false;
		}
		
		/**
		 * @param aName
		 * @param anObject
		 * @return
		 */
		private NotificationObserver accepts(String aName, Object anObject)
		{
			NotificationObserver anObserver = getObserver();
			
			if ( anObserver != null )
			{
				String	anEntryName = getName();
				Object	anEntryObject = getObject();
			
				if ( ( anEntryName == null ) && ( anEntryObject == null ) )
				{
					return anObserver;
				}
				else
				if ( ( anEntryName != null ) && ( anEntryObject != null ) )
				{
					if ( ( anEntryName.equals( aName ) == true ) &&
						( anEntryObject.equals( anObject ) == true ) )
					{
						return anObserver;
					}
				}
				else
				if ( anEntryName != null )
				{
					if ( anEntryName.equals( aName ) == true )
					{
						return anObserver;
					}
				}
				else
				if ( anEntryObject != null )
				{
					if ( anEntryObject.equals( anObject ) == true )
					{
						return anObserver;
					}
				}
			}
			
			return null;
		}
	}
	
/**
 * @author ktsubaki
 *
 * 
 */
//	===========================================================================
//	Task method(s)
//	---------------------------------------------------------------------------

	private static final class Task extends Object implements Runnable
	{
	
		private Collection	_observers = null;
		private Notification	notification = null;
	
		/**
		 * @param someObservers
		 * @param aNotification
		 */
		private Task(Collection someObservers, Notification aNotification)
		{
			super();
			
			setObservers( someObservers );
			setNotification( aNotification );
		}
	
		/**
		 * @return
		 */
		private Collection getObservers()
		{
			return _observers;
		}
		
		/**
		 * @param aValue
		 */
		private void setObservers(Collection aValue)
		{
			_observers = aValue;
		}
		
		/**
		 * @return
		 */
		private Notification getNotification()
		{
			return notification;
		}
		
		/**
		 * @param aValue
		 */
		private void setNotification(Notification aValue)
		{
			notification = aValue;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run()
		{
			Notification	aNotification = getNotification();
		
			for ( Iterator anIterator = getObservers().iterator(); anIterator.hasNext(); )
			{
				NotificationObserver	anObserver = (NotificationObserver) anIterator.next();
				
				anObserver.notify( aNotification );
			}
		}
		
	}
}

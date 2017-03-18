/*
 * Created on 2003/11/27
 *
 */
package core.util;

public class Null implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	public static final Null NULL = new Null();
	
	protected Null() {
		super();
	}
	
	public static Null nullValue() {
		return NULL;
	}
	
	public static boolean isNull(Object object) {
		return (object instanceof Null);
	}
	
	public boolean equals(Object obj) {
		return Null.isNull(obj);
	}
}

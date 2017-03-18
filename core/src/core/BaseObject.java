package core;

import core.util.FieldAccess;

/**
 * FieldAccessプロトコルを持つクラスです。アプリケーションデータクラスやコンポーネントクラスはこれを継承することで、プロパティーにアクセスするインターフェースが統一されます。
 * 
 * @author ktsubaki
 *
 */
public abstract class BaseObject implements FieldAccess {

	public void init ()
    {
    }
	
	private boolean classInheritsFromClass(Class<?> classInQuestion, Class<?> targetSuperclass) {
        boolean doesInherit = false;
        if (classInQuestion == targetSuperclass) {
            doesInherit = true;
        }
        else {
            Class<?> superclassInQuestion = classInQuestion.getSuperclass();
            if (superclassInQuestion != null) {
                doesInherit = classInheritsFromClass(superclassInQuestion, targetSuperclass);
            }
        }
        return doesInherit;
	}

    public boolean isKindOfClass (Class<?> targetClass)
    {
        return this.classInheritsFromClass(getClass(), targetClass);
    }

	@Override
	public Object getValueForKey(String key) {
		return FieldAccess.DefaultImplementation.getValueForKey(this, key);
	}

	@Override
	public void setValueForKey(Object value, String key) {
		FieldAccess.DefaultImplementation.setValueForKey(this, value, key);
	}

	@Override
	public Object getValueForKeyPath(String keyPath) {
		return FieldAccess.DefaultImplementation.getValueForKeyPath(this, keyPath);
	}

	@Override
	public void setValueForKeyPath(Object value, String keyPath) {
		FieldAccess.DefaultImplementation.setValueForKeyPath(this, value, keyPath);
	}
 
    
}

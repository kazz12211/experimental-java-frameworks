package core.util;

public abstract class ConservertiveThreadLocal<T> extends ThreadLocal<T> {

    @Override
    public final T get()
    {
        T t = super.get();
        if (t == null){
            remove();
        }
        return t;
    }

    @Override
    protected final T initialValue()
    {
        return null;
    }

    public T get(boolean create)
    {
        T t = get();
        if (t == null && create){
            t = create();
            set(t);
        }
        return t;
    }

    @Override
    public void set(T value)
    {
        if (value == null){
            remove();
        }else{
            super.set(value);
        }
    }

    protected abstract T create();

}

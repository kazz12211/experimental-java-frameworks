package core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class FaultingList<E> extends ArrayList<E> {

	protected static final long serialVersionUID = 1L;
	protected boolean _isFault = false;
	protected Object _owner;

	public boolean isFault() {
		return _isFault;
	}
	public void setFault(boolean isFault) {
		_isFault = isFault;
	}

	abstract protected void loadStoredValues();
	
	protected FaultingList(Object owner) {
		super();
		this._owner = owner;
		this._isFault = false;
	}
	
	public Object owner() {
		return _owner;
	}
	public void setOwner(Object owner) {
		this._owner = owner;
	}
	
	@Override
	public boolean contains(Object arg0) {
		if(_isFault)
			loadStoredValues();
		return super.contains(arg0);
	}

	@Override
	public int indexOf(Object arg0) {
		if(_isFault)
			loadStoredValues();
		return super.indexOf(arg0);
	}

	@Override
	public int lastIndexOf(Object arg0) {
		if(_isFault)
			loadStoredValues();
		return super.lastIndexOf(arg0);
	}

	@Override
	public int size() {
		if(_isFault)
			loadStoredValues();
		return super.size();
	}

	@Override
	public Object[] toArray() {
		if(_isFault)
			loadStoredValues();
		return super.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		if(_isFault)
			loadStoredValues();
		return super.toArray(arg0);
	}

	@Override
	public Iterator<E> iterator() {
		if(_isFault)
			loadStoredValues();
		return super.iterator();
	}

	@Override
	public ListIterator<E> listIterator() {
		if(_isFault)
			loadStoredValues();
		return super.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		if(_isFault)
			loadStoredValues();
		return super.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		if(_isFault)
			loadStoredValues();
		return super.subList(fromIndex, toIndex);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		if(_isFault)
			loadStoredValues();
		return super.containsAll(arg0);
	}

	public void turnIntoFault() {
		super.clear();
		_isFault = true;
	}



}

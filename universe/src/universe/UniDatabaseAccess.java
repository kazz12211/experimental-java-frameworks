package universe;

/**
 * 
 * @author ktsubaki
 *
 */
public abstract class UniDatabaseAccess {

	UniObjectsSession _session;
	UniModel _model;

	protected UniDatabaseAccess(UniObjectsSession session) {
		_session = session;
		_model = _session.model();
	}
		
	protected boolean isConnected() {
		return _session.isConnected();
	}
	
	public UniObjectsSession session() {
		if(_session == null)
			_session = new UniObjectsSession(_model);
		return _session;
	}
	
	public UniModel model() {
		return _model;
	}
	
}

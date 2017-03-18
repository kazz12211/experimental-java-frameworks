package universe;

import java.util.Map;

import core.util.MapUtils;
import core.util.Perf;
import universe.util.UniFileUtil;
import universe.util.UniLogger;
import asjava.uniclientlibs.UniDataSet;
import asjava.uniclientlibs.UniDynArray;
import asjava.uniclientlibs.UniException;
import asjava.uniobjects.UniCommand;
import asjava.uniobjects.UniDictionary;
import asjava.uniobjects.UniFile;
import asjava.uniobjects.UniFileException;
import asjava.uniobjects.UniJava;
import asjava.uniobjects.UniObjectsTokens;
import asjava.uniobjects.UniSelectList;
import asjava.uniobjects.UniSession;
import asjava.uniobjects.UniSessionException;
import asjava.unirpc.UniRPCConnection;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniObjectsConnection {
	
	UniJava _uniJava;
	UniSession _uniSession;
	Map<String, UniFile> _openedFiles = null;
	Map<String, UniDictionary> _openedDictionaries = null;
	UniModel _model;
	int _retainCount = 0;
	
	public UniObjectsConnection(UniModel model) {
		_model = model;
	}
	
	public UniModel model() {
		return _model;
	}
	
	public void retain() {
		_retainCount++;
	}
	public void release() {
		_retainCount--;
	}
	public int retainCount() {
		return _retainCount;
	}
	public static UniObjectsConnection get(UniModel model) {
		UniObjectsConnection conn = UniConnectionPool.sharedPool().get(model);
		conn.retain();
		return conn;
	}
	
	public UniRPCConnection rpcConnection() {
		return _uniSession.connection;
	}
	
	public boolean isConnected() {
		if(_uniSession == null)
			return false;
		return (_uniSession.isActive() && rpcConnection().isConnected() && rpcConnection().isActive());
	}
	
	public int establish() throws UniException {
		int reconnect = 1;
		UniConnectionInfo connInfo = _model.connectionInfo();
		if(_uniSession == null) {
			Perf p = Perf.newPerf("UniObjectsConnection opened");
			UniLogger.universe.debug("Connecting database");
			UniLogger.universe.debug("\thost     : " + _model.connectionInfo().host());
			UniLogger.universe.debug("\tusername : " + _model.connectionInfo().username());
			UniLogger.universe.debug("\tpassword : " + "**********");
			UniLogger.universe.debug("\tport     : " + _model.connectionInfo().port());
			UniLogger.universe.debug("\tpath     : " + _model.connectionInfo().accountPath());
			UniLogger.universe.debug("\tencoding : " + _model.connectionInfo().clientEncoding());
			if(_model.connectionInfo().sessionTimeout() != 0)
				UniLogger.universe.debug("\ttimeout  : " + _model.connectionInfo().sessionTimeout());
			_uniJava = new UniJava();
			_uniSession = _uniJava.openSession();
			String clientEncoding = _model.connectionInfo().clientEncoding();
			_uniSession.setSessionEncoding(clientEncoding);
			if(_model.connectionInfo().sessionTimeout() != 0)
			_uniSession.setTimeout(_model.connectionInfo().sessionTimeout());
			rpcConnection().setTimeoutSeconds(_model.connectionInfo().sessionTimeout());
			UniLogger.universe.debug("Connected");
			p.stop();
		}
		/*
		if(!_uniSession.isActive()) {
			Perf p = Perf.newPerf("Connected to UniVerse");
			_uniSession.connect(connInfo.host(), connInfo.port(), connInfo.username(), connInfo.password(), connInfo.accountPath());
			_uniSession.nlsMap().setName("NONE");
			p.stop();
			reconnect = 0;
		} else if(!rpcConnection().isConnected() || !rpcConnection().isActive()) {
			Perf p = Perf.newPerf("Connected to UniVerse");
			_uniSession.connect(connInfo.host(), connInfo.port(), connInfo.username(), connInfo.password(), connInfo.accountPath());
			_uniSession.nlsMap().setName("NONE");
			p.stop();
			reconnect = 0;
		}
		*/
		
		rpcConnection().setDebugLevel(_model.connectionInfo().packetDebugLevel());
		
		if(!isConnected()) {
			Perf p = Perf.newPerf("Connected to UniVerse");
			_uniSession.connect(connInfo.host(), connInfo.port(), connInfo.username(), connInfo.password(), connInfo.accountPath());
			_uniSession.nlsMap().setName("NONE");
			rpcConnection().setTimeoutSeconds(_model.connectionInfo().sessionTimeout());
			p.stop();
			reconnect = 0;
		}
		if(_openedFiles == null)
			_openedFiles = MapUtils.map();
		if(_openedDictionaries == null)
			_openedDictionaries = MapUtils.map();

		return reconnect;
	}
	
	public void disconnect() {
		if(this.isConnected()) {
			try {
				Perf p = Perf.newPerf("UniObjectsConnection closed");
				_uniSession.disconnect();
				p.stop();
				this.closeFiles();
				this.closeDictionaries();
			} catch (UniSessionException e) {
				UniLogger.universe.error(e);
			} finally {
				release();
				_uniSession = null;
				_uniJava = null;
			}
		}
	}
	
	public void closeFile(UniFile file) {
		for(String filename : _openedFiles.keySet()) {
			UniFile aFile = _openedFiles.get(filename);
			if(file == aFile && file.isOpen()) {
				try {
					file.close();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.closeFile(" + filename + "): file close error", e);
				}
				_openedFiles.remove(filename);
				break;
			}
		}
	}
	
	public void closeDict(UniDictionary dict) {
		for(String filename : _openedDictionaries.keySet()) {
			UniDictionary aDict = _openedDictionaries.get(filename);
			if(dict == aDict && dict.isOpen()) {
				try {
					dict.close();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.closeDictionary(" + filename + "): file close error", e);
				}
				_openedFiles.remove(filename);
				break;
			}
		}
	}

	public UniFile openFile(String filename) throws UniSessionException {
		this._checkConnection();
		Perf p = Perf.newPerf("File '" + filename + "' opened");
		UniFile file = _uniSession.openFile(filename);
		p.stop();
		return file;
	}
	
	public UniDictionary openDict(Object filename) throws UniSessionException {
		this._checkConnection();
		Perf p = Perf.newPerf("Dictionary '" + filename + "' opened");
		UniDictionary dict = _uniSession.openDict(filename);
		p.stop();
		return dict;
	}
	
	protected void closeFiles() {
		for(UniFile file : _openedFiles.values()) {
			if(file.isOpen())
				try {
					file.close();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.closeFiles(): file close error", e);
				}
		}
		_openedFiles.clear();
	}
	
	protected void closeDictionaries() {
		for(UniDictionary dict : _openedDictionaries.values()) {
			if(dict.isOpen())
				try {
					dict.close();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.closeDictionaries(): file close error", e);
				}
		}
		_openedDictionaries.clear();
	}

	public UniFile getFile(String filename) {
		UniFile file = null;
		if(_openedFiles.containsKey(filename)) {
			file = _openedFiles.get(filename);
			if(!file.isOpen())
				try {
					file.open();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.getFile(" + filename + "): file open error", e);
					file = null;
				}
		} else {
			try {
				file = openFile(filename);
				_openedFiles.put(filename, file);
			} catch (UniSessionException e) {
				UniLogger.universe.warn("UniObjectsConnection.getFile(" + filename + "): file open error", e);
			}
		}
		return file;
	}

	public UniDictionary getDict(String filename) {
		UniDictionary dict = null;
		if(_openedDictionaries.containsKey(filename)) {
			dict = _openedDictionaries.get(filename);
			if(!dict.isOpen())
				try {
					dict.open();
				} catch (UniFileException e) {
					UniLogger.universe.warn("UniObjectsConnection.getDict(" + filename + "): file open error", e);
					dict = null;
				}
			
		} else {
			try {
				dict = openDict(filename);
				_openedDictionaries.put(filename, dict);
			} catch (UniSessionException e) {
				UniLogger.universe.warn("UniObjectsConnection.getDict(" + filename + "): file open error", e);
			}
		}
		return dict;
	}


	public String executeUniCommandUntilComplete(String commandString) throws Exception {
		this._checkConnection();
		UniLogger.universe_command.debug("Executing: " + commandString);
		Perf p = Perf.newPerf("command(" + commandString + ") responded");
		StringBuffer response = new StringBuffer();
		UniCommand uCommand = _uniSession.command();
		uCommand.setCommand(commandString);
		uCommand.exec();
		do { 
			response.append(uCommand.response());
			if(uCommand.status() != UniObjectsTokens.UVS_COMPLETE)
				uCommand.reply("");
		} while (uCommand.status() != UniObjectsTokens.UVS_COMPLETE);
		p.stop();
		return response.toString();

	}
	
	public void executeUniCommand(String commandString, String...replies) throws Exception {
		this._checkConnection();
		UniLogger.universe_command.debug("Executing: " + commandString);
		Perf p = Perf.newPerf("command(" + commandString + ") responded");
		UniCommand uCommand = _uniSession.command();
		uCommand.setCommand(commandString);
		uCommand.exec();
		for(String reply : replies) {
			if(uCommand.status() != UniObjectsTokens.UVS_REPLY) 
				break;
			UniLogger.universe_command.debug("Response: " + uCommand.response());
			UniLogger.universe_command.debug("Reply: " + reply);
			uCommand.reply(reply);
		}
		p.stop();
	}
	
	public String executeUniCommand(String commandString) throws Exception {
		this._checkConnection();
		UniLogger.universe_command.debug("Executing: " + commandString);
		Perf p = Perf.newPerf("command(" + commandString + ") responded");
		UniCommand command = _uniSession.command();
		command.setCommand(commandString);
		command.exec();
		p.stop();
		String response = command.response();
		UniLogger.universe_command.debug("Response: " + response);
		return response;
	}

	public String executeUniCommand(UniCommand command) throws Exception {
		this._checkConnection();
		UniLogger.universe_command.debug("Executing: " + command.getCommand());
		Perf p = Perf.newPerf("command(" + command.getCommand() + ") responded");
		command.exec();
		p.stop();
		String response = command.response();
		UniLogger.universe_command.debug("Response: " + response);
		return response;
	}

	
	public UniSelectList selectList(int num) throws UniSessionException {
		this._checkConnection();
		Perf p = Perf.newPerf("Got select list (" + num + ")");
		UniSelectList selList = _uniSession.selectList(num);
		p.stop();
		return selList;
	}

	protected void _checkConnection() {
		if(!isConnected()) {
			UniLogger.universe.debug("UniObjectsConnection is automatically connecting to database");
			try {
				establish();
			} catch (Exception e) {
				UniLogger.universe.warn("UniObjectsConnection failed to connect to database");
			}
		}
	}


	public UniSession uniSession() {
		return _uniSession;
	}
	
	
	public void storeBasic(UniObjectsBasic basic) throws Exception {
		_checkConnection();
		
		if(!UniFileUtil.exists(basic.storageName(), _uniSession))
			createBasicStorage(basic);
		
		UniFile file = this.getFile(basic.storageName());
		String sourceCode = basic.sourceCode();
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(basic.programName());
		try {
			file.lockRecord(rowSet, UniObjectsTokens.UVT_WRITE_RELEASE);
			file.deleteRecord(rowSet);
			UniDynArray array = new UniDynArray();
			array.insert(1, basic.sourceCode());
			file.write(basic.programName(), array);
		} finally {
			if(file.isRecordLocked(basic.programName()))
				file.unlockRecord(rowSet);
		}
		
		compileBasic(basic);
		
	}
	
	public boolean isStored(UniObjectsBasic basic) {
		_checkConnection();

		if(!UniFileUtil.exists(basic.storageName(), _uniSession)) {
			try {
				createBasicStorage(basic);
			} catch (Exception e) {
				UniLogger.universe.error("Could not create Basic program storage '" + basic.storageName() + "'", e);
				return false;
			}
		}
		UniFile file = this.getFile(basic.storageName());
		if(file == null)
			return false;
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(basic.programName());
		UniDataSet dataSet = null;
		try {
			dataSet = file.readField(rowSet, 1);
		} catch (UniFileException e) {
			UniLogger.universe.error("Could not read Basic program '" + basic.programName()+ "' from storage '" + basic.storageName() + "'", e);
		}
		return dataSet != null;
	}
	
	private void createBasicStorage(UniObjectsBasic basic) throws Exception {
		this.executeUniCommand("CREATE.FILE " + basic.storageName() + " 19");
	}
	
	public void compileBasic(UniObjectsBasic basic) throws Exception {
		_checkConnection();
		this.executeUniCommand("PHANTOM BASIC " + basic.storageName() + " " + basic.programName());
	}
	
	public boolean isCompiled(UniObjectsBasic basic) {
		_checkConnection();
		UniFile file = this.getFile(basic.storageName() + ".O");
		if(file == null)
			return false;
		UniDataSet rowSet = new UniDataSet();
		rowSet.append(basic.programName());
		UniDataSet dataSet = null;
		try {
			dataSet = file.readField(rowSet, 1);
		} catch (UniFileException e) {
			UniLogger.universe.error("Could not read compiled Basic program '" + basic.programName()+ "' from storage '" + basic.storageName() + ".O'", e);
		}
		return dataSet != null;
	}
	
	public String executeBasic(UniObjectsBasic basic, boolean recompile, String...args) throws Exception {
		_checkConnection();
		if(recompile) {
			storeBasic(basic);
		}
		StringBuffer command = new StringBuffer();
		command.append("RUN " + basic.storageName() + " " + basic.programName());
		if(args != null && args.length > 0) {
			for(String arg : args) {
				command.append(" " + arg);
			}
		}
		return this.executeUniCommandUntilComplete(command.toString());
	}

}

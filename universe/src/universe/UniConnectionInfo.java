package universe;

import java.util.Properties;

/**
 * 
 * @author ktsubaki
 *
 */
public class UniConnectionInfo {
	
	public static final String DefaultSessionEncoding = "UTF8";
	
	String _host;
	int _port;
	String _username;
	String _password;
	String _accountPath;
	String _clientEncoding = DefaultSessionEncoding;
	int _sessionTimeout = 30;
	int _packetDebugLevel= 0;
	Properties _properties;
			
	public String username() {
		return _username;
	}
	public void setUsername(String username) {
		this._username = username;
	}
	public String password() {
		return _password;
	}
	public void setPassword(String password) {
		this._password = password;
	}
	public String host() {
		return _host;
	}
	public void setHost(String host) {
		this._host = host;
	}
	public int port() {
		if(_port == 0)
			return 31438;
		return _port;
	}
	public void setPort(int port) {
		this._port = port;
	}
	public String accountPath() {
		return _accountPath;
	}
	public void setAccountPath(String accountPath) {
		this._accountPath = accountPath;
	}
	public String clientEncoding() {
		return _clientEncoding;
	}
	public int sessionTimeout() {
		return _sessionTimeout;
	}
	public void setSessionTimeout(int timeoutMillis) {
		_sessionTimeout = timeoutMillis;
	}
	public Properties properties() {
		return _properties;
	}
	public void setProperties(Properties properties) {
		_properties = properties;
	}
	
	@Override
	public String toString() {
		return "{host=" + _host + "; username=" + _username + "; password=******" + "; accountPath=" + _accountPath + "}"; 
	}
	
	@Override
	public boolean equals(Object object) {
		if(object instanceof UniConnectionInfo) {
			UniConnectionInfo info = (UniConnectionInfo) object;
			return (this.accountPath().equals(info.accountPath()) &&
					this.host().equals(info.host()) &&
					this.port() == info.port() &&
					this.username().equals(info.username()));
		}
		return false;
	}
	public int packetDebugLevel() {
		return _packetDebugLevel;
	}
	public void setPacketDebugLevel(int debugLevel) {
		_packetDebugLevel = debugLevel;
	}
}

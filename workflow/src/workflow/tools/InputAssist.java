package workflow.tools;

import java.util.List;

import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.util.log.Log;

public class InputAssist extends AWComponent {

	protected UserDictionary dictionary;
	public String word;
	
	protected String getCategory() {
		return (String) valueForBinding("category");
	}
	
	protected String getUserId() {
		return (String) valueForBinding("userId");
	}
	
	protected AWComponent getCaller() {
		return (AWComponent) valueForBinding("caller");
	}
	
	protected String getCallbackMethod() {
		return (String) valueForBinding("callback");
	}
	
	public List<String> words() {
		return this.dictionary().words(this.getCategory());
	}
	
	public String getMenuId() {
		if(this.hasBinding("menuId"))
			return (String) valueForBinding("menuId");
		return "InputAssistMenuId";
	}
	
	public UserDictionary dictionary() {
		if(dictionary == null) {
			dictionary = new UserDictionary(this.getUserId());
			try {
				dictionary.load();
			} catch (Exception e) {
				Log.customer.error("InputAssist: could not load user dictionary", e);
			}
		}
		return dictionary;
	}
	
	public void selectWord() {
		this.getCaller().setFieldValue(this.getCallbackMethod(), word);
	}
	
	public AWResponseGenerating editDictionary() {
		return InputAssistEditor.open(this.getCaller(), this.dictionary(), this.getCategory());
	}
	
	@Override
	public boolean isStateless() { return false; }
	
}

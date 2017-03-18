package workflow.tools;

import java.util.List;

import ariba.ui.aribaweb.core.AWActionCallback;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.widgets.ModalPageWrapper;
import ariba.util.core.StringUtil;
import ariba.util.log.Log;

public class InputAssistEditor extends AWComponent {

	Callback _callback;
	UserDictionary dictionary;
	String category;
	public int index;
	public String newWord;
	public String word;
	
	public static AWResponseGenerating open(AWComponent caller, UserDictionary dictionary, String category) {
		InputAssistEditor editor = (InputAssistEditor) caller.pageWithName("InputAssistEditor");
		editor.setCaller(caller);
		editor.setUserDictionary(dictionary);
		editor.setCategory(category);
		return editor;
	}
	
	public List<String> words() {
		return dictionary.words(category);
	}
	public AWResponseGenerating add() {
		if(StringUtil.nullOrEmptyOrBlankString(newWord) == false) {
			dictionary.add(newWord, category);
			try {
				dictionary.save();
			} catch (Exception e) {
				Log.customer.error("InputAssistEditor: could not add new word.", e);
			}
		}
		return null;
	}
	
	public AWResponseGenerating remove() {
		dictionary.remove(word, category);
		try {
			dictionary.save();
		} catch (Exception e) {
			Log.customer.error("InputAssistEditor: could not remove a word.", e);
		}
		return null;
	}
	
	private void setUserDictionary(UserDictionary dictionary) {
		this.dictionary = dictionary;
	}

	private void setCaller(AWComponent caller) {
		_callback = new Callback(caller);
	}
	
	private void setCategory(String category) {
		this.category = category;
	}
	
	public AWResponseGenerating close() {
		ModalPageWrapper.prepareToExit(this);
		_callback.prepare(requestContext());
		return _callback.doneAction(this);
	}

	@Override
	public boolean isStateless() { return false; }

	@Override
	public boolean isClientPanel() { return true; }
	
	public class Callback extends AWActionCallback {
		
		AWComponent caller;
		public Callback(AWComponent caller) {
			super(caller);
			this.caller = caller;
		}
		@Override
		public AWResponseGenerating doneAction(AWComponent sender) {
			return caller.pageComponent();
		}
		
		public AWComponent getCaller() {
			return caller;
		}

	}

}

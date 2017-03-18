package workflow.aribaweb.component;

import ariba.ui.aribaweb.core.AWBinding;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWComponentActionRequestHandler;
import ariba.ui.aribaweb.core.AWGenericActionTag;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.aribaweb.util.AWEncodedString;
import ariba.ui.widgets.BindingNames;


public class Embedded extends AWComponent {
	private static final String[] SupportedBindingNames = {
		BindingNames.src, BindingNames.value, BindingNames.pageName, BindingNames.name, BindingNames.action };
	
	private static final char CHARSET[] = {
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
		};
	public AWEncodedString _elementId;
	private AWEncodedString _frameName;
	
	public String[] supportedBindingNames() {
		return SupportedBindingNames;
	}
	
	public AWEncodedString frameName() {
		if(_frameName == null) {
			_frameName = encodedStringValueForBinding(BindingNames.name);
			if(_frameName == null) {
				_frameName = _elementId;
			}
		}
		return _frameName;
	}
	
	public String srcUrl() {
		String srcUrl = null;
		AWBinding binding = bindingForName(BindingNames.src, false);
		if(binding != null) {
			srcUrl = (String)valueForBinding(binding);
		} else {
			String s = core.util.RandomString.generate(CHARSET, 8);
			srcUrl = AWComponentActionRequestHandler.SharedInstance.urlWithSenderId(requestContext(), _elementId);
			srcUrl += "&r=" + s;
		}
		return srcUrl;
	}
	
	public AWResponseGenerating getContent() {
		AWResponseGenerating actionResults = null;
		AWBinding actionBinding = null;
		AWBinding pageNameBinding = null;
		if((pageNameBinding = bindingForName(BindingNames.pageName)) == null &&
				(actionBinding = bindingForName(BindingNames.action)) == null) {
			actionResults = (AWResponseGenerating)valueForBinding(BindingNames.value);
		} else {
			actionResults = AWGenericActionTag.evaluateActionBindings(this, pageNameBinding, actionBinding);
		}
		requestContext().setFrameName(frameName());
		return actionResults;
	}
	
	public boolean isStateless() { return false; }
}

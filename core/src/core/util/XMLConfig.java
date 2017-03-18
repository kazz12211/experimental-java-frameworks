package core.util;

import org.w3c.dom.Element;

import ariba.ui.aribaweb.core.AWServerApplication;
import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.ui.widgets.XMLUtil;

public class XMLConfig {

	protected String getResourcePath(String resourceName) {
		AWServerApplication app = AWServletApplication.sharedInstance();
		AWResource resource = app.resourceManager().resourceNamed(resourceName);
		return resource.fullUrl();
	}
	
	protected Element elementNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		if(elements != null && elements.length >= 1)
			return elements[0];
		return null;
	}

	protected Element[] elementsNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		return elements;
	}


}

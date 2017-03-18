package workflow.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.w3c.dom.Element;

import ariba.ui.aribaweb.core.AWServerApplication;
import ariba.ui.aribaweb.util.AWResource;
import ariba.ui.servletadaptor.AWServletApplication;
import ariba.ui.widgets.XMLUtil;

public class ZenHan {
	private static ZenHan _sharedInstance = null;
	private String[] zenkakuKana;
	private String zenkakuKigou;
	private String zenkakuDigit;
	private String zenkakuNumber;
	private String zenkakuAlphabet;
	private String[] hankakuKana;
	
	protected ZenHan() {
		
	}
	
	public static ZenHan sharedInstance() {
		if(_sharedInstance == null) {
			_sharedInstance = new ZenHan();
			_sharedInstance.init();
		}
		return _sharedInstance;
	}

	private String getResourcePath(String resourceName) {
		AWServerApplication app = AWServletApplication.sharedInstance();
		AWResource resource = app.resourceManager().resourceNamed(resourceName);
		return resource.fullUrl();
	}
	
	public Element load(String resourceName) throws MalformedURLException {
		URL url = new URL(this.getResourcePath(resourceName));
		Element docElement = XMLUtil.document(url, false, false, null).getDocumentElement();
		if(docElement.getNodeName().equals("ZenHan"))
			return docElement;
		return null;
	}

	private void init() {
		try {
			Element docRoot = this.load("ZenHan.xml");
			Element element = this.elementNamed(docRoot, "ZenkakuKana");
			if(element != null) {
				String str = XMLUtil.getText(element, "ZenkakuKana");
				zenkakuKana = str.split(",");
			}
			element = this.elementNamed(docRoot, "ZenkakuKigou");
			if(element != null)
				zenkakuKigou = XMLUtil.getText(element, null);
			element = this.elementNamed(docRoot, "ZenkakuDigit");
			if(element != null)
				zenkakuDigit = XMLUtil.getText(element, null);
			element = this.elementNamed(docRoot, "ZenkakuNumber");
			if(element != null)
				zenkakuNumber = XMLUtil.getText(element, null);
			element = this.elementNamed(docRoot, "ZenkakuAlphabet");
			if(element != null)
				zenkakuAlphabet = XMLUtil.getText(element, null);
			element = this.elementNamed(docRoot, "HankakuKana");
			if(element != null) {
				String str = XMLUtil.getText(element, null);
				hankakuKana = str.split(",");
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	private Element elementNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		if(elements != null && elements.length >= 1)
			return elements[0];
		return null;
	}

	public String[] getZenkakuKana() {
		return zenkakuKana;
	}

	public String getZenkakuKigou() {
		return zenkakuKigou;
	}

	public String getZenkakuDigit() {
		return zenkakuDigit;
	}

	public String getZenkakuNumber() {
		return zenkakuNumber;
	}

	public String getZenkakuAlphabet() {
		return zenkakuAlphabet;
	}

	public String[] getHankakuKana() {
		return hankakuKana;
	}
	
	

}

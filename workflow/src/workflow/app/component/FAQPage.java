package workflow.app.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import workflow.model.User;
import core.util.ListUtils;
import ariba.ui.aribaweb.core.AWComponent;
import ariba.ui.aribaweb.core.AWResponseGenerating;
import ariba.ui.table.AWTDisplayGroup;
import ariba.ui.widgets.XMLUtil;
import ariba.util.log.Log;

public class FAQPage extends AWComponent {
	public AWTDisplayGroup displayGroup = new AWTDisplayGroup();
	public List<FAQItem> faqs;
	public FAQItem currentFaq;
	public boolean editMode;
	public List<Integer> frequencies;
		
	@Override
	public void init() {
		super.init();
		try {
			this.load();
		} catch (Exception e) {
			Log.customer.error("Could not load faq from file " + this.getPath(), e);
		}
		frequencies = ListUtils.list();
		frequencies.add(new Integer(1));
		frequencies.add(new Integer(2));
		frequencies.add(new Integer(3));
		frequencies.add(new Integer(4));
		frequencies.add(new Integer(5));
	}
	public String getApplicationName() {
		return (String) this.valueForBinding("applicationName");
	}
	
	private String getPath() {
		String path = "/opt/local/faq/" + this.getApplicationName();
		return path;
	}
	
	@Override
	public boolean isStateless() { return false; }
	
	public AWResponseGenerating saveFaqs() {
		try {
			this.save();
			this.sortByFrequency();
		} catch (Exception e) {
			Log.customer.error("Could not save faq to file " + this.getPath(), e);
		}
		return null;
	}
	
	public AWResponseGenerating addFaq() {
		faqs.add(new FAQItem());
		return null;
	}
	
	public AWResponseGenerating removeFaq() {
		faqs.remove(displayGroup.selectedObject());
		return null;
	}
	
	public boolean editable() {
		User user = (User) session().getFieldValue("user");
		return (user.hasRole("SystemAdministrator"));
	}
	
	public AWResponseGenerating toggleMode() {
		editMode = !editMode;
		return null;
	}
	
	private void load() throws Exception {
		File f = new File(getPath());
		if(f.exists() == false) {
			f.createNewFile();
			String content = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<faqs>\n</faqs>\n";
			FileWriter writer = new FileWriter(f);
			writer.write(content);
			writer.close();
		}
		FileInputStream fis = new FileInputStream(f);
		InputSource content = new InputSource(fis);
		Element docElem = XMLUtil.document(content, false, false, null).getDocumentElement();
		if(docElem.getNodeName().equals("faqs")) {
			faqs = ListUtils.list();
			Element elements[] = XMLUtil.getAllChildren(docElem, "faq");
			for(Element element : elements) {
				parseFaq(element);
			}
		}
	}
	
	private void save() throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.newDocument();
		Element root = doc.createElement("faqs");
		doc.appendChild(root);
		for(FAQItem faq : faqs) {
			Element e = doc.createElement("faq");
			root.appendChild(e);
			Element sub = doc.createElement("subject"); sub.setTextContent(faq.subject);
			e.appendChild(sub);
			Element ans = doc.createElement("answer"); ans.setTextContent(faq.answer);
			e.appendChild(ans);
			Element fre = doc.createElement("frequency"); fre.setTextContent(Integer.toString(faq.frequency));
			e.appendChild(fre);
			Element que = doc.createElement("question"); que.setTextContent(faq.question);
			e.appendChild(que);
		}
		
		FileOutputStream fos = new FileOutputStream(this.getPath());
		XMLUtil.serializeDocument(doc, fos);
	}
	
	private void parseFaq(Element element) {
		FAQItem item = new FAQItem();
		item.subject = this.stringValue(element, "subject", null);
		item.answer = this.stringValue(element, "answer", null);
		item.frequency = this.intValue(element, "frequency", 1);
		item.question = this.stringValue(element, "question", null);
		faqs.add(item);
		this.sortByFrequency();
	}

	private Element elementNamed(Element parent, String elementName) {
		Element elements[] = XMLUtil.getAllChildren(parent, elementName);
		if(elements != null && elements.length >= 1)
			return elements[0];
		return null;
	}
	private String stringValue(Element parent, String elementName, String theDefault) {
		Element elem = this.elementNamed(parent, elementName);
		if(elem != null)
			return XMLUtil.getText(elem, theDefault);
		return theDefault;
	}
	private int intValue(Element parent, String elementName, int theDefault) {
		String s = this.stringValue(parent, elementName, "0");
		return Integer.parseInt(s);
	}
	
	private void sortByFrequency() {
		Collections.sort(faqs, new Comparator<FAQItem>() {
			@Override
			public int compare(FAQItem i1, FAQItem i2) {
				return i1.frequency - i2.frequency;
			}});
	}
	
	public class FAQItem {
		
		public String subject;
		public String question;
		public String answer;
		public int frequency;
	}
}

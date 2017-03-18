package workflow.controller.rule.cond;

import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ariba.ui.widgets.XMLUtil;
import ariba.util.core.ListUtil;
import ariba.util.log.Log;

public abstract class ConditionParser {

	public abstract Condition parse(Element element);
	
	public static ConditionParser KeyValue = new KeyValue();
	public static ConditionParser Or = new Or();
	public static ConditionParser And = new And();
	public static ConditionParser Not = new Not();
	
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
		
	public static class KeyValue extends ConditionParser {

		String[] validOperators = {"isnull", "isnotnull", "eq", "neq", "lt", "lte", "gt", "gte"};
		
		@Override
		public Condition parse(Element element) {
			String key = element.getAttribute("key");
			String value = element.getAttribute("value");
			String op = element.getAttribute("operator");
			
			if(!validOperator(op)) {
				Log.customer.warn("ConditionParser: Invalid operator '" + op + "' for key-value condition");
			}
			
			Condition cond = new Condition.KeyValue(key, value, op);
			return cond;
		}

		private boolean validOperator(String op) {
			for(String val : validOperators) {
				if(val.equals(op))
					return true;
			}
			return false;
		}
		
	}

	public static class Not extends ConditionParser {

		@Override
		public Condition parse(Element element) {
			Element inside = null;
			inside = this.elementNamed(element, "key-value");
			if(inside == null)
				inside = this.elementNamed(element, "not");
			if(inside == null)
				inside = this.elementNamed(element, "or");
			if(inside == null)
				inside = this.elementNamed(element, "and");
			if(inside == null) {
				Log.customer.warn("ConditionParser: No condition inside not condition");
				return null;
			}
			if(inside.getNodeName().equals("key-value"))
				return new Condition.Not(ConditionParser.KeyValue.parse(inside));
			else if(inside.getNodeName().equals("not"))
				return new Condition.Not(ConditionParser.Not.parse(inside));
			else if(inside.getNodeName().equals("or"))
				return new Condition.Not(ConditionParser.Or.parse(inside));
			else if(inside.getNodeName().equals("and"))
				return new Condition.Not(ConditionParser.And.parse(inside));
			else
				Log.customer.warn("ConditionParser: Invalid condition type '" + inside.getNodeName() +"' inside not condition");
			return null;
		}
		
	}
	
	public static class Or extends ConditionParser {

		@Override
		public Condition parse(Element element) {
			NodeList children = element.getChildNodes();
			List<Condition> conditions = ListUtil.list();
			for(int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element)node;
					Condition cond = null;
					if(elem.getNodeName().equals("key-value"))
						cond =  ConditionParser.KeyValue.parse(elem);
					else if(elem.getNodeName().equals("not"))
						cond = ConditionParser.Not.parse(elem);
					else if(elem.getNodeName().equals("or"))
						cond = ConditionParser.Or.parse(elem);
					else if(elem.getNodeName().equals("and"))
						cond = ConditionParser.And.parse(elem);
					if(cond != null)
						conditions.add(cond);
				}
			}
			if(conditions.size() > 0) {
				return new Condition.Or(conditions);
			}
			return null;
		}
	
	}
	
	public static class And extends ConditionParser {

		@Override
		public Condition parse(Element element) {
			NodeList children = element.getChildNodes();
			List<Condition> conditions = ListUtil.list();
			for(int i = 0; i < children.getLength(); i++) {
				Node node = children.item(i);
				if(node.getNodeType() == Node.ELEMENT_NODE) {
					Element elem = (Element)node;
					Condition cond = null;
					if(elem.getNodeName().equals("key-value"))
						cond =  ConditionParser.KeyValue.parse(elem);
					else if(elem.getNodeName().equals("not"))
						cond = ConditionParser.Not.parse(elem);
					else if(elem.getNodeName().equals("or"))
						cond = ConditionParser.Or.parse(elem);
					else if(elem.getNodeName().equals("and"))
						cond = ConditionParser.And.parse(elem);
					if(cond != null)
						conditions.add(cond);
				}
			}
			if(conditions.size() > 0) {
				return new Condition.And(conditions);
			}
			return null;
		}
		
	}
}

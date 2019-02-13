package main.conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class SetsLists {

	private Document doc;

	public SetsLists(Document doc) {
		this.doc = doc;
		convertSimple("list","(",")");
		convertSimple("set","{","}");
	}

	private void convertSimple(String content, String open, String close) {
		NodeList nl = doc.getElementsByTagName(content);
		for(int i = 0; i < nl.getLength(); i++) {
			Node apply = nl.item(i);
			doc.renameNode(apply, null, "mrow");
			if(apply.getFirstChild().getNodeName().equals("bvar")) {
				convertSuchThat(apply,content,open,close);
			}else	{
				Node sep = doc.createElement("mo");sep.setTextContent(",");
				Node next = apply.getFirstChild().getNextSibling();
				while(next!=null) {
					apply.insertBefore(sep.cloneNode(true), next);
					next= next.getNextSibling();
				}				
				apply.insertBefore(doc.createElement("mo"),apply.getFirstChild());
				apply.getFirstChild().setTextContent(open);
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent(close);
			}
			i--;
		}		
	}

	private void convertSuchThat(Node apply, String content, String open, String close) {
		Element att = (Element)apply;
		att.removeAttribute("order");
		Node bvar = apply.getFirstChild().getFirstChild();
		Node condition = apply.getFirstChild().getNextSibling().getFirstChild();

		apply.replaceChild(bvar, apply.getFirstChild());
		Node seperator = doc.createElement("mo");
		seperator.setTextContent("|");
		apply.insertBefore(seperator, bvar.getNextSibling());
		apply.replaceChild(condition, seperator.getNextSibling());
		
		if(apply.getLastChild()!=condition)apply.replaceChild(apply.getLastChild(),bvar);
		
		apply.insertBefore(doc.createElement("mo"),apply.getFirstChild());
		apply.getFirstChild().setTextContent(open);
		apply.appendChild(doc.createElement("mo"));
		apply.getLastChild().setTextContent(close);
	}


}

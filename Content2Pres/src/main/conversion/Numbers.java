package main.conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Numbers {

	private Document doc;

	public Numbers(Document doc) {
		this.doc = doc;
		specialnums();
		setnames();
		convert();
		ci();
		cs();
		plus();
	}

	private void setnames() {
		String[][] snams = new String[][] {{"integers","Z"},{"reals","R"},
			{"rationals","Q"},{"naturalnumbers","N"},
			{"complexes","C"},{"primes","P"}};
		for(String[] pair : snams) {
			String cont = pair[0];
			String pres = pair[1];
			NodeList nl = doc.getElementsByTagName(cont);
			for(int i = 0; i < nl.getLength(); i ++) {
				Element n = (Element)nl.item(i);
				doc.renameNode(n, null, "mi");
				n.setTextContent(pres);
				n.setAttribute("mathvariant", "double-struck");
				i--;
			}
		}		
	}

	private void specialnums() {
		String[][] snums = new String[][] {{"exponentiale","e"},{"imaginaryi","i"},
											{"eulergamma","&#x3b3;"},{"infinity","&#x221e;"},
											{"notanumber","NaN"},{"pi","&#x3c0;"},
											{"true","true"},{"false","false"},
											{"emptyset","&#x2205;"},{"sin","sin"},
											{"cos","cos"},{"tan","tan"},{"ident","id"},
											{"mean","mean"}};
		for(String[] pair : snums) {
			String cont = pair[0];
			String pres = pair[1];
			NodeList nl = doc.getElementsByTagName(cont);
			for(int i = 0; i < nl.getLength(); i ++) {
				Node n = nl.item(i);
				doc.renameNode(n, null, "mi");
				n.setTextContent(pres);
				i--;
			}
		}		
	}

	private void ci() {
		NodeList nl = doc.getElementsByTagName("ci");
		for(int i = 0; i < nl.getLength(); i ++) {
			Node n = nl.item(i);
			doc.renameNode(n, null, "mi");
			n.setTextContent(n.getTextContent().trim());
			Element node = (Element) n;
			while (node != null && node.getAttributes().getLength() > 0) {
			    Node att = node.getAttributes().item(0);
			    node.getAttributes().removeNamedItem(att.getNodeName());
			}
			i--;
		}
		
	}
	
	private void cs() {
		NodeList nl = doc.getElementsByTagName("cs");
		for(int i = 0; i < nl.getLength(); i ++) {
			Node n = nl.item(i);
			doc.renameNode(n, null, "ms");
			if(n.getTextContent().equals("")) {
				n.setTextContent("&#xa0;&#xa0;");
			}
			Element node = (Element) n;
			while (node != null && node.getAttributes().getLength() > 0) {
			    Node att = node.getAttributes().item(0);
			    node.getAttributes().removeNamedItem(att.getNodeName());
			}
			i--;
			nl = doc.getElementsByTagName("cs");
		}
		
	}

	private void convert() {
		NodeList nl = doc.getElementsByTagName("cn");
		for(int i = 0; i < nl.getLength(); i ++) {
			Element num = (Element) nl.item(i);
			if(!num.getAttribute("base").equals("")) {
				base(num);
				i--;
			}
			else {
				String type = num.getAttribute("type");
				switch(type) {
				case "rational":
					rational(num);
					i--;
					break;
				case "complex-cartesian":
					cc(num);
					i--;
					break;
				case "complex-polar":
					cp(num);
					i--;
					break;
				case "hexdouble":
					doc.renameNode(num, null, "mi");
					num.setTextContent("0x"+num.getTextContent());
					i--;
					break;
				default:
					i--;
					break;
				}
			}
			num.removeAttribute("type");
			//num.setTextContent(num.getTextContent().trim());
			doc.renameNode(num, null, "mn");
			nl = doc.getElementsByTagName("cn");
		}
	}

	private void cp(Element num) {
		Node mrow = doc.createElement("mrow");
		num.removeAttribute("type");
		Node r = doc.createElement("mn");
		r.setTextContent(num.getFirstChild().getTextContent().trim());
		Node i = doc.createElement("mn");
		if(!num.getLastChild().getNodeName().equals("#text")) {
			i = num.getLastChild();
		}else {
			i.setTextContent(num.getLastChild().getTextContent().trim());
		}
		Node parent = num.getParentNode();
		parent.replaceChild(mrow, num);
		if(!r.getTextContent().equals("1")) {
			mrow.appendChild(r);
			mrow.appendChild(doc.createElement("mo"));mrow.getLastChild().setTextContent("&#x2062;");
		}
		mrow.appendChild(i);
		Node msup = doc.createElement("msup");mrow.appendChild(msup);
		msup.appendChild(doc.createElement("mi"));msup.getLastChild().setTextContent("e");
		Node suprow = doc.createElement("mrow");msup.appendChild(suprow);
		suprow.appendChild(i);
		if(i.hasChildNodes() && i.getNodeName().equals("mrow")) {
			suprow.appendChild(doc.createElement("mfenced"));
			suprow.getLastChild().appendChild(i);
		}
		suprow.appendChild(doc.createElement("mo"));suprow.getLastChild().setTextContent("&#x2062;");
		suprow.appendChild(doc.createElement("mi"));suprow.getLastChild().setTextContent("i");
		
	}

	private void cc(Element num) {
		Node mrow = doc.createElement("mrow");
		num.removeAttribute("type");
		Node r = doc.createElement("mn");
		r.setTextContent(num.getFirstChild().getTextContent().trim());
		Node i = doc.createElement("mn");
		i.setTextContent(num.getLastChild().getTextContent().trim());
		Node parent = num.getParentNode();
		parent.replaceChild(mrow, num);
		mrow.appendChild(r);
		mrow.appendChild(doc.createElement("mo"));mrow.getLastChild().setTextContent("+");
		mrow.appendChild(i);
		mrow.appendChild(doc.createElement("mo"));mrow.getLastChild().setTextContent("&#x2062;");
		mrow.appendChild(doc.createElement("mi"));mrow.getLastChild().setTextContent("i");
		
	}

	private void rational(Element num) {
		Node mrow = doc.createElement("mfrac");
		num.removeAttribute("type");
		Node n = doc.createElement("mn");
		n.setTextContent(num.getFirstChild().getTextContent());
		Node d = doc.createElement("mn");
		d.setTextContent(num.getLastChild().getTextContent());
		Node parent = num.getParentNode();
		parent.replaceChild(mrow, num);
		mrow.appendChild(n);
		mrow.appendChild(d);
	}

	private void base(Element num) {
		Node msup = doc.createElement("msub");
		String baseValue = num.getAttribute("base");
		Node base = doc.createElement("mn");
		base.setTextContent(baseValue);
		num.removeAttribute("base");
		Node parent = num.getParentNode();
		parent.insertBefore(msup, num);
		msup.appendChild(num);
		msup.appendChild(base);
	}
	
	
	private void plus() {
		NodeList nl = doc.getElementsByTagName("plus");
		for(int i = 0; i < nl.getLength(); i++) {
			doc.renameNode(nl.item(i),null,"mi");
			nl.item(i).setTextContent("plus");
			nl = doc.getElementsByTagName("plus");
			i--;
		}
	}

}

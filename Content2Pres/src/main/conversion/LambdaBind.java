package main.conversion;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class LambdaBind {

	private Document doc;

	public LambdaBind(Document doc) {
		this.doc = doc;
		convertBind();
		convertLambda();
	}

	private void convertBind() {
		NodeList nl = doc.getElementsByTagName("bind");
		for(int i = 0; i < nl.getLength(); i++) {
			Node apply = nl.item(i);
			String symbol = apply.getFirstChild().getNodeName().equals("exists")? "&#x2203;" : "";
			if(symbol.equals(""))
				symbol = apply.getFirstChild().getNodeName().equals("forall")? "&#x2200;" : "";
			if(symbol.equals(""))
				symbol = apply.getFirstChild().getNodeName().equals("lambda")? "&#x3BB;" : "";
			apply.removeChild(apply.getFirstChild());
			
			Node next = apply.getFirstChild();
			List<Node> bvars = new ArrayList<Node>();
			while(next.getNodeName().equals("bvar")) {
				bvars.add(next.getFirstChild());
				next = next.getNextSibling();
			}
			
			Node row = doc.createElement("mrow");
			row.appendChild(doc.createElement("mo"));
			row.getFirstChild().setTextContent(symbol);
			
			row.appendChild(bvars.get(0));
			for(int j = 1; j < bvars.size(); j++) {
				Node comma = doc.createElement("mo");
				comma.setTextContent(",");
				row.appendChild(comma);
				row.appendChild(bvars.get(j));
			}
			
			row.appendChild(doc.createElement("mo"));
			row.getLastChild().setTextContent(".");
			Node fenced = doc.createElement("mfenced");
			row.appendChild(fenced);
			fenced.appendChild(apply.getLastChild());
			
			Node parent = apply.getParentNode();
			parent.replaceChild(row, apply);
		
			i--;
		}		
	}
	
	private void convertLambda() {
		NodeList nl = doc.getElementsByTagName("lambda");
		for(int i = 0; i < nl.getLength(); i++) {
			Node apply = nl.item(i);
			Node row = doc.createElement("mrow");
			row.appendChild(doc.createElement("mi"));
			row.getFirstChild().setTextContent("&#x3bb;");
			
			Node next = apply.getFirstChild();
			List<Node> bvars = new ArrayList<Node>();
			while(next.getNodeName().equals("bvar")) {
				bvars.add(next.getFirstChild());
				next = next.getNextSibling();
			}
//			row.appendChild(apply.getFirstChild().getFirstChild());
			row.appendChild(bvars.get(0));
			for(int j = 1; j < bvars.size(); j++) {
				Node comma = doc.createElement("mo");
				comma.setTextContent(",");
				row.appendChild(comma);
				row.appendChild(bvars.get(j));
			}
			
			row.appendChild(doc.createElement("mo"));
			row.getLastChild().setTextContent(".");
			Node fenced = doc.createElement("mfenced");
			row.appendChild(fenced);
			fenced.appendChild(apply.getLastChild());
			
			Node parent = apply.getParentNode();
			parent.replaceChild(row, apply);
			i--;
		}		
	}

}

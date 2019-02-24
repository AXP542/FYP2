package ambiguities;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class FindInDoc {

	private Document doc;
	private Document patternDoc;
	private Document replacementDoc;
	private String repString;

	public FindInDoc(String pattern, Document doc) {
		this.doc = doc;
		String presString = pattern.split("%")[0];
		String contString = pattern.split("%")[1];
		this.repString = contString;
		this.patternDoc = makeDoc(presString);
		this.replacementDoc = makeDoc(contString);
	}
	
	public void convertAllMatches() {
		NodeList topRowChildNodes = patternDoc.getFirstChild().getChildNodes();
		List<Node> rowList = new ArrayList<Node>();
		for(int i = 0; i < topRowChildNodes.getLength(); i ++) {
			rowList.add(topRowChildNodes.item(i));
		}
		
		List<Node> allDocNodes = getAllNodes(doc.getFirstChild());
		while(allDocNodes.size() > 0) {
			Node startNode = allDocNodes.get(0);
			MatchRow mr = new MatchRow(rowList, startNode);
			if(mr.matches()) {

				NodeList oldVars = replacementDoc.getElementsByTagName("var");
				int v = 0;
				for(int i = 0; i < oldVars.getLength(); i++) {
					Node oldVar = oldVars.item(0);
					Node clone;
					if(Integer.parseInt(oldVar.getTextContent()) > 0) {
						clone = mr.vars.get(Integer.parseInt(oldVar.getTextContent())-1).cloneNode(true);
					}else {
						clone = mr.vars.get(v).cloneNode(true);
						v++;
					}
					// Type checking
					Element atts = (Element) oldVar;
					if(atts.hasAttribute("type")) {
						if(!((Element)clone).hasAttribute("type")){
							((Element)clone).setAttribute("type", atts.getAttribute("type"));
						}else if(!((Element)clone).getAttribute("type").equals(atts.getAttribute("type"))) {
							String errorText = ((Element)doc.getFirstChild()).getAttribute("error");
							errorText += "Expecting type: " + atts.getAttribute("type") + 
									" Var type: " + ((Element)clone).getAttribute("type") + "; ";
							((Element)doc.getFirstChild()).setAttribute("error", errorText);
						}
					}
					
					replacementDoc.adoptNode(clone);
					oldVar.getParentNode().replaceChild(clone, oldVar);
					i--;
				}
				
				Node replacement = replacementDoc.getFirstChild().getFirstChild();
				doc.adoptNode(replacement);
				Node parent = startNode.getParentNode();
				parent.insertBefore(replacement, startNode);
				for(Node del : mr.nodesToRemove) {
					parent.removeChild(del);
				}
				removeMRows(doc);
				
				allDocNodes = getAllNodes(doc.getFirstChild());
				this.replacementDoc = makeDoc(this.repString);
			}else {
				allDocNodes.remove(startNode);
			}
		}
	}
	
	private void removeMRows(Document mrowDoc) {	
		NodeList mrows = mrowDoc.getElementsByTagName("mrow");
		for(int i = 0; i < mrows.getLength(); i ++) {
			Node mrow = mrows.item(i);
			if(mrow.getChildNodes().getLength() < 2) {
				Node parent = mrow.getParentNode();
				parent.replaceChild(mrow.getFirstChild(), mrow);
				i--;
			}
		}		
	}

	private List<Node> getAllNodes(Node root) {
		List<Node> allNodes = new ArrayList<Node>();
		NodeList rootChildren = root.getChildNodes();
		for(int i = 0; i < rootChildren.getLength(); i++) {
			Node newRoot = rootChildren.item(i);
			if(!newRoot.getNodeName().substring(0, 1).equals("#")) {
				allNodes.add(newRoot);
			}if(newRoot.hasChildNodes()) {
				allNodes.addAll(getAllNodes(newRoot));
			}
		}		
		return allNodes;
	}
	
	
	private Document makeDoc(String xml) {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document r = db.parse(new InputSource(new StringReader(xml)));
			List<Node> nodes = getAllNodes(r.getFirstChild());
			for(Node n : nodes) {
				if(n.getNodeName().equals("attributes")) {
					Element parent = (Element) n.getParentNode();
					String[] attributes = n.getTextContent().split(","); 
					for(String att : attributes) {
						parent.setAttribute(att.split(":")[0], att.split(":")[1]);
						parent.removeChild(n);
					}
				}
			}
			return r;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}

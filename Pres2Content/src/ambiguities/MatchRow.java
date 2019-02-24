package ambiguities;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MatchRow {

	public Node startNode;
	private List<Node> rowList;
	public List<Node> nodesToRemove;
	public List<Node> vars;

	public MatchRow(List<Node> rowList, Node startNode) {
		this.startNode = startNode;
		this.rowList = rowList;
		this.nodesToRemove = new ArrayList<Node>();
		this.vars = new ArrayList<Node>();
	}

	// return true if doc contains a matching row
	public boolean matches() {
		Node nodeToCheck = startNode;
		for(Node expectedNode : rowList) {
			if(nodesMatch(nodeToCheck, expectedNode) && matchAttr(nodeToCheck, expectedNode)) {
				nodesToRemove.add(nodeToCheck);
				nodeToCheck = nodeToCheck.getNextSibling();
			}else {
				//remove all repl nodes from rowList doc
				return false;
			}
		}		
		return true;
	}


	private boolean nodesMatch(Node nodeToCheck, Node expectedNode) {
		if(nodeToCheck == null) {
			return false;
		}else if(expectedNode.getNodeName().equals("var")){
			vars.add(nodeToCheck);
			return true;
		}else if(expectedNode.getNodeName().equals(nodeToCheck.getNodeName())){
			if(!expectedNode.hasChildNodes()) {
				return !nodeToCheck.hasChildNodes();
			}else if(expectedNode.getFirstChild().getNodeName().equals("#text")) {
				return expectedNode.getTextContent().equals(nodeToCheck.getTextContent());
			}else if(expectedNode.hasChildNodes() && nodeToCheck.hasChildNodes()) {
				if(!nodeToCheck.getFirstChild().getNodeName().equals("#text")) {
					NodeList expectedChildNodes = expectedNode.getChildNodes();
					List<Node> nextRow = new ArrayList<Node>();
					for(int i = 0; i < expectedChildNodes.getLength(); i ++) {
						nextRow.add(expectedChildNodes.item(i));
					}
					MatchRow mr = new MatchRow(nextRow, nodeToCheck.getFirstChild());
					boolean r = mr.matches();
					vars.addAll(mr.vars);
					return r;
				}
			}			
		}
		
		return false;
	}
	
	
	
	private boolean matchAttr(Node nodeToCheck, Node expectedNode) {
		Element eNode = (Element)expectedNode;
		Element cNode = (Element)nodeToCheck;
				
		NamedNodeMap expected = eNode.getAttributes();
		for(int i = 0; i < expected.getLength(); i++) {
			Node att = expected.item(i);
			if(!cNode.getAttribute(att.getNodeName()).equals(att.getTextContent())) {
				return false;
			}
		}
		
		return true;
	}

}

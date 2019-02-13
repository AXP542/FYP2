package ambiguities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class MathInterpretor {

	private List<String> possibleInterpretations;
	private List<Document> convertedInts;

	public MathInterpretor(Document original) {
		this.possibleInterpretations = getPossibleInterpretations();
		this.convertedInts = new ArrayList<Document>();
		generateInts(cloneDocument(original));
		if(this.convertedInts.size() < 1) {
			this.convertedInts.add(original);
		}
		
	}
	public List<Document> getInterpretations() {
		return this.convertedInts;
	}

	private void generateInts(Document input) {
		Document doc = cloneDocument(input);
		List<Node> allNodes = getAllNodes(doc.getFirstChild());
		int nn = allNodes.size();
		for(int i = 0; i < nn; i ++) {
			List<MatchRow> matches = new ArrayList<MatchRow>();
			List<String> contentStrings = new ArrayList<String>();
			List<Document> matchedDocs = new ArrayList<Document>();
			for(String pair : this.possibleInterpretations) {
				String presString = pair.split("%")[0];
				String contString = pair.split("%")[1];
				Document expected = makeDoc(presString);
				NodeList topRowChildNodes = expected.getFirstChild().getChildNodes();
				List<Node> rowList = new ArrayList<Node>();
				for(int j = 0; j < topRowChildNodes.getLength(); j ++) {
					rowList.add(topRowChildNodes.item(j));
				}
				MatchRow mr = new MatchRow(rowList, allNodes.get(i));
				if(mr.matches()) {
					matches.add(mr);
					contentStrings.add(contString);
					matchedDocs.add(doc);
					doc = cloneDocument(input);
					allNodes = getAllNodes(doc.getFirstChild());
				}
			}
			if(matches.size() > 1) {
				int x = 0;
				for(MatchRow match : matches) {
					Document interp = convert(match, contentStrings.get(x), matchedDocs.get(x));
					MathInterpretor mi = new MathInterpretor(interp);
					this.convertedInts.addAll(mi.getInterpretations());
					x++;
				}
			}
		}
	}
	
	
	private Document convert(MatchRow match, String contString, Document doc) {
		Document replacementDoc = makeDoc(contString);
		NodeList oldVars = replacementDoc.getElementsByTagName("var");
		int v = 0;
		for(int i = 0; i < oldVars.getLength(); i++) {
			Node oldVar = oldVars.item(0);
			Node clone;
			if(Integer.parseInt(oldVar.getTextContent()) > 0) {
				clone = match.vars.get(Integer.parseInt(oldVar.getTextContent())-1).cloneNode(true);
			}else {
				clone = match.vars.get(v).cloneNode(true);
				v++;
			}
			replacementDoc.adoptNode(clone);
			oldVar.getParentNode().replaceChild(clone, oldVar);
			i--;
		}
		
		Node replacement = replacementDoc.getFirstChild().getFirstChild();
		doc.adoptNode(replacement);
		Node parent = match.startNode.getParentNode();
		parent.insertBefore(replacement, match.startNode);
		for(Node del : match.nodesToRemove) {
			parent.removeChild(del);
		}
		return doc;		
	}

	//Helper methods:
	
	
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
	

	private List<String> getPossibleInterpretations() {
		try {	
			List<String> pi = new ArrayList<String>();
			@SuppressWarnings("resource")
			Scanner s = new Scanner(new File("Ambiguities"));
			while (s.hasNext()){
			    pi.add(s.next());
			}	
			return pi;
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Document makeDoc(String xml) {
		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			return db.parse(new InputSource(new StringReader(xml)));
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	private Document cloneDocument(Document doc) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	        DocumentBuilder builder = dbf.newDocumentBuilder();
	        Document clone = builder.newDocument();
	        Node docNode = doc.getDocumentElement().cloneNode(true);
	        clone.adoptNode(docNode);
	        clone.appendChild(docNode);
			return clone;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
	}

}

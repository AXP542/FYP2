package ambiguities;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Pres2Cont {

	private Document original;
	private List<Document> interpretations;

	public Pres2Cont(Document original) {
		this.original = original;
		this.interpretations = new ArrayList<Document>();
		removeWhiteSpace(original);
		generateInterpretations();
		for(Document interp : this.interpretations) {
			convert(interp);
		}
		for(int index = 0; index < this.interpretations.size(); index ++) {
			Document interp = this.interpretations.get(index);
			if(isDuplicate(interp, this.interpretations)) {
				this.interpretations.remove(index);
				index--;
			}
		}
	}

	private Document convert(Document doc) {
		try {	
			@SuppressWarnings("resource")
			Scanner s = new Scanner(new File("Conversions"));
			while (s.hasNext()){
			    FindInDoc fid = new FindInDoc(s.next(), doc);
			    fid.convertAllMatches();
			}	
			trim(doc);
			return doc;
		}catch(FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private void generateInterpretations() {
		Document doc = cloneDocument(original);
		MathInterpretor mt = new MathInterpretor(doc);
		this.interpretations.addAll(mt.getInterpretations());
	}
	
	private boolean isDuplicate(Document doc, List<Document>list) {
		for(Document docb : list) {
			if(doc != docb) {
				List<Node>nodesA = getAllNodes(doc.getFirstChild());
				List<Node>nodesB = getAllNodes(docb.getFirstChild());
				if(nodesA.size() == nodesB.size()) {
					int i = 0;
					int matched = 0;
					for(Node a : nodesA) {
						if(a.getNodeName().equals(nodesB.get(i).getNodeName()) &&
							a.getTextContent().equals(nodesB.get(i).getTextContent())) {
								matched ++;
						}
						i++;
					}
					if(matched == i)
						return true;
				}
			}
		}
		return false;
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

	public List<Document> getInterpretations(){
		return this.interpretations;
	}
	
	/*
	 * Helper method to print the xml file to console
	 */
	public String print(Document doc) {
		try {			
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			String result = out.toString().replaceAll("&amp;", "&");
			result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
			
			String tabbed = "";
			int lines = result.split("\n").length;
			int tabNumber = 0;
			for(int i = 1; i < lines; i++) {
				String line = result.split("\n")[i].trim();
				if(!line.contains("/")) {
					tabbed += space(tabNumber) +  line + "\n";
					tabNumber ++;
				}else if(!line.startsWith("</") && line.contains("/")) {
					tabbed += space(tabNumber) +  line + "\n";					
				}else {
					tabNumber --;
					tabbed += space(tabNumber) +  line + "\n";
				}
			}
			return tabbed;
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;		
	}
	
	private String space(int tabNumber) {
		String space = "";
		for(int i = 0; i < tabNumber; i++) {
			space += "    ";
		}
		return space;
	}

	/*
	 * Remove all whitespace in the document
	 */
	private void removeWhiteSpace(Document doc) {
		try {
			XPathFactory xpathFactory = XPathFactory.newInstance();
			// XPath to find empty text nodes.
			XPathExpression xpathExp;
			xpathExp = xpathFactory.newXPath().compile(
			        "//text()[normalize-space(.) = '']");
			NodeList emptyTextNodes = (NodeList) 
			        xpathExp.evaluate(doc, XPathConstants.NODESET);
			// Remove each empty text node from document.
			for (int i = 0; i < emptyTextNodes.getLength(); i++) {
			    Node emptyTextNode = emptyTextNodes.item(i);
			    emptyTextNode.getParentNode().removeChild(emptyTextNode);
			}
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}  		
	}

	public Document getOriginal() {
		return original;
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
	
	
	private void trim(Document doc) {
		NodeList opens = doc.getElementsByTagName("mo");
		for(int i = 0; i < opens.getLength(); i ++) {
			Node open = opens.item(i);
			if(open.getNodeName().equals("mo") && open.getTextContent().equals("(")) {
				Node close = open.getNextSibling().getNextSibling();
				if(close.getNodeName().equals("mo") && close.getTextContent().equals(")")) {
					Node parent = open.getParentNode();
					parent.removeChild(close);
					doc.renameNode(open, null, "mrow");
					open.setTextContent(null);
					open.appendChild(open.getNextSibling());
					i --;
				}
			}
		}
		
		NodeList fences = doc.getElementsByTagName("mfenced");
		for(int i = 0; i < fences.getLength(); i ++) {
			Node fence = fences.item(i);
			if(fence.getChildNodes().getLength() == 1) {
				fence.getParentNode().replaceChild(fence.getFirstChild(), fence);
				i--;
			}
		}
		
		NodeList rows = doc.getElementsByTagName("mrow");
		for(int i = 0; i < rows.getLength(); i ++) {
			Node row = rows.item(i);
			if(row.getChildNodes().getLength() == 1) {
				row.getParentNode().replaceChild(row.getFirstChild(), row);
				i--;
			}
		}
		
	}

}

package main;

import java.io.StringWriter;
import java.io.Writer;

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

import main.conversion.ApplyTags;
import main.conversion.Invterval;
import main.conversion.LambdaBind;
import main.conversion.Numbers;
import main.conversion.SetsLists;
import main.conversion.VectorMatrices;

public class Content2Presentation {

	private Document doc;

	public Content2Presentation(Document doc) {
		this.doc = doc;
		removeWhiteSpace();
		convert();
	}


	private void convert() {
		// TODO Direct conversions (cn tags, setnames etc.)
		// TODO Apply tags- create a table with headers:
		// ContentTag, PresText, min elements, max elements, bound variable, bvar seperator,
		SetsLists sl = new SetsLists(doc);
		VectorMatrices vm = new VectorMatrices(doc);
		ApplyTags at = new ApplyTags(doc);
		LambdaBind lb = new LambdaBind(doc);
		Invterval in = new Invterval(doc);
		Numbers n = new Numbers(doc);
	}


	/*
	 * Remove all whitespace in the document
	 */
	private void removeWhiteSpace() {
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
	
	
	/*
	 * Helper method to print the xml file to console
	 */
	public String print() {
		try {			
			Transformer tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			Writer out = new StringWriter();
			tf.transform(new DOMSource(doc), new StreamResult(out));
			String result = out.toString().replaceAll("&amp;", "&");
			result = result.replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "");
			return result;
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

}

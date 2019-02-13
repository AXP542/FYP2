import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class ConvertContent {

	public static void main(String[] args) {
		File inputfolder = new File("content");
		for(File dir : inputfolder.listFiles()) {
			for(File f : dir.listFiles()) {
				try {
					byte[] encoded = Files.readAllBytes(f.toPath());
					String input = new String(encoded, Charset.defaultCharset());
					input = input.replaceAll("&", "&amp;");
					
					DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(input));
					Document doc = db.parse(is);
					removeWhiteSpace(doc);
					Element e = (Element) doc.getElementsByTagName("math").item(0);
					e.setAttribute("display", "inline");
					NodeList nl = doc.getElementsByTagName("csymbol");
					for(int i = 0; i < nl.getLength(); i ++) {
						Element csymbol = (Element) nl.item(i);
						doc.renameNode(csymbol, null, csymbol.getTextContent());
						csymbol.setTextContent(null);
						convert(csymbol, doc);
						csymbol.removeAttribute("cd");
						
						nl = doc.getElementsByTagName("csymbol");
						i--;
					}
					
					//System.out.println(print(doc));
					String newDoc = print(doc);
					newDoc = newDoc.replaceAll("vector_selector", "selector"); 
					newDoc = newDoc.replaceAll("gamma", "eulergamma");  
					newDoc = newDoc.replaceAll("cartesian_product", "cartesianproduct"); 
					newDoc = newDoc.replaceAll("unary_minus", "minus"); 
					newDoc = newDoc.replaceAll("remainder", "rem"); 
					newDoc = newDoc.replaceAll("matrix_selector", "selector");  
					String fn = f.getPath();
					new File( f.getParent().replace("content", "content3")).mkdirs();
					File f2 = new File(fn.replaceAll("content", "content3"));
					PrintWriter writer = new PrintWriter(f2.getPath(), "UTF-8");
					writer.println(newDoc);
					writer.close();
		
				} catch (SAXException | IOException | ParserConfigurationException | TransformerFactoryConfigurationError e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	public static void convert (Element csymbol, Document doc) {
		numbers(csymbol, doc);
	}

	public static void numbers (Element csymbol, Document doc) {
		if(csymbol.getNodeName().equalsIgnoreCase("z")) {
			
			doc.renameNode(csymbol, null, "integers");
		
		}else if(csymbol.getNodeName().equals("zero")) {
			
			doc.renameNode(csymbol, null, "cn");
			csymbol.setTextContent("0");
		
		}else if(csymbol.getNodeName().equals("one")) {
			
			doc.renameNode(csymbol, null, "cn");
			csymbol.setTextContent("1");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("r")) {
			
			doc.renameNode(csymbol, null, "reals");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("c")) {
			
			doc.renameNode(csymbol, null, "complexes");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("q")) {
			
			doc.renameNode(csymbol, null, "rationals");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("z")) {
			
			doc.renameNode(csymbol, null, "integers");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("n")) {
			
			doc.renameNode(csymbol, null, "naturalnumbers");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("p")) {
			
			doc.renameNode(csymbol, null, "primes");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("e")) {
			
			doc.renameNode(csymbol, null, "exponentiale");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("i")) {
			
			doc.renameNode(csymbol, null, "imaginaryi");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("nan")) {
			
			doc.renameNode(csymbol, null, "notanumber");
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("left_compose")) {
			
			doc.renameNode(csymbol, null, "compose");
		
		}else{
			log(csymbol,doc);
		}
	}
	
	public static void log (Element csymbol, Document doc) {
		if(csymbol.getNodeName().equalsIgnoreCase("log")) {
			
			Node apply = csymbol.getParentNode();
			Node logbase = doc.createElement("logbase");
			Node lb = apply.getFirstChild().getNextSibling();
			apply.insertBefore(logbase, lb);
			logbase.appendChild(lb);
		
		}else {
			matrix(csymbol,doc);
		}
	}
	
	public static void matrix (Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("matrix") || csymbol.getNodeName().equals("vector") ||
				csymbol.getNodeName().equals("piecewise") || csymbol.getNodeName().equals("piece")) {
			
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, csymbol.getNodeName());
			apply.removeChild(csymbol);
			
		}else if(csymbol.getNodeName().equals("matrixrow")){
			
			Node apply = csymbol.getParentNode();
			Node parent = apply.getParentNode();
			doc.renameNode(apply, null, csymbol.getNodeName());
			apply.removeChild(csymbol);
			if(!parent.getNodeName().equals("matrix")) {
				Node matrix = doc.createElement("matrix");
				parent.insertBefore(matrix, apply);
				matrix.appendChild(apply);
			}
			
		}else if(csymbol.getNodeName().equalsIgnoreCase("otherwise")) {
			
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, "piece");
			doc.renameNode(csymbol, null, "ci");
			csymbol.setTextContent("otherwise");
			apply.appendChild(csymbol);
		
		}else {
			cn(csymbol,doc);
		}			
	}
	
	public static void cn (Element csymbol, Document doc) {
		if(csymbol.getNodeName().equalsIgnoreCase("complex_cartesian")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "cn");
			apply.setAttribute("type", "complex-cartesian");
			apply.removeChild(csymbol);
			String a = apply.getFirstChild().getTextContent();
			apply.removeChild(apply.getFirstChild());
			String b = apply.getFirstChild().getTextContent();
			apply.removeChild(apply.getFirstChild());
			apply.appendChild(doc.createTextNode(a));
			apply.appendChild(doc.createElement("sep"));
			apply.appendChild(doc.createTextNode(b));
			
		}else if(csymbol.getNodeName().equalsIgnoreCase("based_integer") || csymbol.getNodeName().equalsIgnoreCase("based_float")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "cn");
			apply.removeChild(csymbol);
			apply.setAttribute("base", apply.getFirstChild().getTextContent());
			apply.removeChild(apply.getFirstChild());
			String val = apply.getLastChild().getTextContent();
			apply.removeChild(apply.getLastChild());
			apply.setTextContent(val);
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("rational")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "cn");
			apply.setAttribute("type", "rational");
			apply.removeChild(csymbol);
			Node nodea = apply.getFirstChild();
			Node nodeb = nodea.getNextSibling();
			if(nodea.getNodeName().equals("ci")||nodea.getNodeName().equals("cn")) {
				Node r = nodea;
				nodea = doc.createTextNode(nodea.getTextContent());
				apply.removeChild(r);
			}if(nodeb.getNodeName().equals("ci")||nodeb.getNodeName().equals("cn")) {
				Node r = nodeb;
				nodeb = doc.createTextNode(nodeb.getTextContent());
				apply.removeChild(r);
			}
			apply.appendChild(nodea);
			apply.appendChild(doc.createElement("sep"));
			apply.appendChild(nodeb);
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("complex_polar")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "cn");
			apply.setAttribute("type", "complex-polar");
			apply.removeChild(csymbol);
			Node nodea = apply.getFirstChild();
			Node nodeb = nodea.getNextSibling();
			if(nodea.getNodeName().equals("ci")||nodea.getNodeName().equals("cn")) {
				Node r = nodea;
				nodea = doc.createTextNode(nodea.getTextContent());
				apply.removeChild(r);
			}if(nodeb.getNodeName().equals("ci")||nodeb.getNodeName().equals("cn")) {
				Node r = nodeb;
				nodeb = doc.createTextNode(nodeb.getTextContent());
				apply.removeChild(r);
			}
			apply.appendChild(nodea);
			apply.appendChild(doc.createElement("sep"));
			apply.appendChild(nodeb);
		
		}else {
			interval(csymbol,doc);
		}
	}
	
	public static void interval (Element csymbol, Document doc) {
		if(csymbol.getNodeName().equalsIgnoreCase("interval_oo") ||
				csymbol.getNodeName().equalsIgnoreCase("interval")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "interval");
			apply.setAttribute("closure", "open");
			apply.removeChild(apply.getFirstChild());
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("interval_oc")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "interval");
			apply.setAttribute("closure", "open-closed");
			apply.removeChild(apply.getFirstChild());
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("interval_co")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "interval");
			apply.setAttribute("closure", "closed-open");
			apply.removeChild(apply.getFirstChild());
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("interval_cc")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "interval");
			apply.setAttribute("closure", "closed");
			apply.removeChild(apply.getFirstChild());
		
		}else if(csymbol.getNodeName().equalsIgnoreCase("integer_interval")) {
			
			Element apply = (Element) csymbol.getParentNode();
			doc.renameNode(apply, null, "interval");
			apply.setAttribute("closure", "closed");
			apply.removeChild(apply.getFirstChild());
		
		}else {
			root(csymbol,doc);
		}
	}
	
	
	private static void root(Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("root")) {
			if(!csymbol.getNextSibling().getNodeName().equals("degree")) {
				Node degree = doc.createElement("degree");
				Node apply = csymbol.getParentNode();
				degree.appendChild(apply.getLastChild());
				apply.insertBefore(degree, csymbol.getNextSibling());
			}
		}else {
			lambda(csymbol,doc);
		}
	}
	
	private static void lambda(Element csymbol, Document doc) {
		Node bind = csymbol.getParentNode();
		Node apply = bind.getParentNode();
		if(csymbol.getNodeName().equals("lambda") && apply.getFirstChild().equals(bind)) {
			bind.removeChild(csymbol);
			doc.renameNode(bind, null, "lambda");
		}else {
			limit(csymbol,doc);
		}
	}
	
	private static void limit(Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("limit")) {
			Node up = csymbol.getNextSibling();
			Node type = up.getNextSibling();
			Node bind = type.getNextSibling();
			bind.removeChild(bind.getFirstChild());
			Node bvar = bind.getFirstChild();
			Node expr = bvar.getNextSibling();
			Node parent = csymbol.getParentNode();
			Node condition = doc.createElement("condition");
			Node apply = doc.createElement("apply");
			Element tendsto = doc.createElement("tendsto");
			//set attr for tendsto
			String t = type.getTextContent();
			if(t.equals("above")||t.equals("below"))
				tendsto.setAttribute("type", t);
			
			parent.appendChild(bvar);
			parent.appendChild(condition);
			condition.appendChild(apply);
			apply.appendChild(tendsto);
			apply.appendChild(bvar.getFirstChild().cloneNode(true));
			apply.appendChild(up);
			parent.appendChild(expr);
			
			parent.removeChild(bind);
			parent.removeChild(type);
			
		}else {
			integral(csymbol,doc);
		}
	}	
	
	private static void integral(Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("int")) {
			doc.renameNode(csymbol, null, "int");
			Node bind = csymbol.getNextSibling();
			Node bvar = bind.getFirstChild().getNextSibling();
			Node expr = bind.getLastChild();
			Node apply = csymbol.getParentNode();
			apply.appendChild(bvar);
			apply.appendChild(expr);
			apply.removeChild(bind);			
		}else if(csymbol.getNodeName().equals("defint")) {
			doc.renameNode(csymbol, null, "int");
			Node interval = csymbol.getNextSibling();
			if(interval.getFirstChild().getTextContent().equals("ordered_interval") ||
					interval.getFirstChild().getTextContent().equals("interval")) {
				doc.renameNode(interval, null, "interval");
				interval.removeChild(interval.getFirstChild());
				Node lambda = interval.getNextSibling();
				lambda.removeChild(lambda.getFirstChild());
				Node apply = csymbol.getParentNode();
				Node lowlimit = doc.createElement("lowlimit");
				lowlimit.appendChild(interval.getFirstChild());
				Node uplimit = doc.createElement("uplimit");
				uplimit.appendChild(interval.getFirstChild());
				apply.appendChild(lambda.getFirstChild());
				apply.appendChild(lowlimit);
				apply.appendChild(uplimit);
				apply.appendChild(lambda.getFirstChild());
				apply.removeChild(lambda);
				apply.removeChild(interval);
			}else if (interval.getNodeName().equals("ci")) {
				Node domain = doc.createElement("domainofapplication");
				Node apply = csymbol.getParentNode();
				apply.insertBefore(domain, interval);
				domain.appendChild(interval);
				Node bind = domain.getNextSibling();
				bind.removeChild(bind.getFirstChild());
				apply.appendChild(bind.getFirstChild());
				apply.appendChild(bind.getFirstChild());
				apply.removeChild(bind);
			}
		}else {
			diff(csymbol,doc);
		}		
	}
	
	
	
	private static void diff(Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("diff")) {
			if(csymbol.getNextSibling().getNodeName().equals("bind")) {
				Node bind = csymbol.getNextSibling();
				Node bvar = bind.getFirstChild().getNextSibling();
				Node expr = bind.getLastChild();
				Node apply = csymbol.getParentNode();
				apply.appendChild(bvar);
				apply.appendChild(expr);
				apply.removeChild(bind);
			}
		}else if(csymbol.getNodeName().equals("partialdiff")) {
			Node list = csymbol.getNextSibling();
			Node bind = list.getNextSibling();
			bind.removeChild(bind.getFirstChild());
			doc.renameNode(bind, null, "lambda");
		}else if(csymbol.getNodeName().equals("partialdiffdegree")) {
			doc.renameNode(csymbol, null, "partialdiff");
			Node list = csymbol.getNextSibling();
			list.removeChild(list.getFirstChild());
			doc.renameNode(list, null, "list");
			Node d = list.getNextSibling();
			Node bind = d.getNextSibling();
			Node apply = csymbol.getParentNode();
			apply.removeChild(d);
			bind.removeChild(bind.getFirstChild());
			int[] degrees = new int[list.getChildNodes().getLength()];
			List<Node> difs = new ArrayList<Node>();
			int x = 1;
			for(int degree : degrees) {
				String s = list.getFirstChild().getTextContent();
				degree = Integer.parseInt(s);
				for(int i = 0; i < degree; i++) {
					Node n = doc.createElement("ci");
					n.setTextContent(x+"");
					difs.add(n);
				}
				list.removeChild(list.getFirstChild());
				x++;
			}
			for(Node n : difs) {
				list.appendChild(n);
			}
			doc.renameNode(bind, null, "lambda");			
		}else if(csymbol.getNodeName().equals("nthdiff")) {
			doc.renameNode(csymbol, null, "partialdiff");
			Node nth = doc.createElement("nthdiff");
			csymbol.getParentNode().insertBefore(nth, csymbol.getNextSibling());
			nth.appendChild(nth.getNextSibling());
		}else {
			selector(csymbol,doc);
		}		
	}

	private static void selector(Element csymbol, Document doc) {
		if(csymbol.getNodeName().equals("matrix_selector")) {
			doc.renameNode(csymbol, null, "selector");
			Node list  = doc.createElement("list");
			Node apply = csymbol.getParentNode();
			list.appendChild(csymbol.getNextSibling());
			list.appendChild(csymbol.getNextSibling());
			apply.appendChild(list);
		}else if(csymbol.getNodeName().equals("vector_selector")) {
			doc.renameNode(csymbol, null, "selector");
			Node apply = csymbol.getParentNode();
			apply.appendChild(csymbol.getNextSibling());
		}else {
			list(csymbol,doc);
		}		
	}
	
	private static void list(Element csymbol, Document doc) {
		String setlist = csymbol.getAttribute("cd");
		setlist = setlist.substring(0, setlist.length()-1);
		if(csymbol.getNodeName().equals("list")) {
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, "list");
			apply.removeChild(csymbol);
		}else if(csymbol.getNodeName().equals("set")) {
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, "set");
			apply.removeChild(csymbol);
		}else if(csymbol.getNodeName().equals("map")) {
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, setlist);
			apply.removeChild(csymbol);
			Node bind = apply.getFirstChild();
			bind.removeChild(bind.getFirstChild());
			Node bvar = bind.getFirstChild();
			Node val = bind.getLastChild();
			Node interval = bind.getNextSibling();
			Node in = doc.createElement("apply");
			in.appendChild(doc.createElement("in"));
			in.appendChild(bvar.getFirstChild().cloneNode(true));
			in.appendChild(interval);
			Node cond = doc.createElement("condition");
			cond.appendChild(in);
			apply.appendChild(bvar);
			apply.appendChild(cond);
			apply.appendChild(val);
			apply.removeChild(bind);
		}else if(csymbol.getNodeName().equals("suchthat")) {
			Node apply = csymbol.getParentNode();
			doc.renameNode(apply, null, setlist);
			apply.removeChild(csymbol);
			Node set = apply.getFirstChild();
			Node bind = set.getNextSibling();
			bind.removeChild(bind.getFirstChild());
			Node bvar = bind.getFirstChild();
			Node c = bind.getLastChild();
			Node in = doc.createElement("apply");
			in.appendChild(doc.createElement("in"));
			in.appendChild(bvar.getFirstChild().cloneNode(true));
			in.appendChild(set);
			Node cond = doc.createElement("condition");
			cond.appendChild(c);
			apply.appendChild(bvar);
			apply.appendChild(cond);
			apply.appendChild(in);
			apply.removeChild(bind);
		}else{
			
		}		
	}

	public static String print(Document doc) {
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
	
	private static void removeWhiteSpace(Document doc) {
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

}

package ambiguities;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class TestAmbiguities {

	public static void main(String[] args) {
		runAllFiles();
		//getConversions();
	}

	private static void runAllFiles() {
		String html = "<!DOCTYPE html>\n"  
					+ "<html>\n"
					+ "<head>\n"
					+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n"
					+ "<style>\n"
					+ "td, th {\n" 
					+ "  border: 1px solid #dddddd;whitespace:pre;}"
					+ "</style>"
					+ "</head>"
					+ "<body>"
					+ "<table>\n";
		File allinput = new File("input");
		for(File inputfolder : allinput.listFiles()) {
		inputfolder = new File(inputfolder.getPath()+"/Presentation");
		//File inputfolder = new File("inputOM/Presentation");
		for(File dir : inputfolder.listFiles()) {
			//File dir = new File("input/w3c/Presentation/arith1");
				for(File f : dir.listFiles()) {
					try {

						if(f.getParentFile().getParentFile().getParentFile().getPath().equals("input\\w3c")) {
							System.out.println(f.getPath());
							byte[] encoded = Files.readAllBytes(f.toPath());
							String input = new String(encoded, Charset.defaultCharset());
							input = input.replaceAll("&", "&amp;");
							
							DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							InputSource is = new InputSource();
							is.setCharacterStream(new StringReader(input));
							Document doc = db.parse(is);
							
							
							Pres2Cont p2c = new Pres2Cont(doc);
							List<Document> interpretations = p2c.getInterpretations();
							String vars = "";
							
							boolean adding2conversion = false;
							for(Document interp : interpretations) {
								if(adding2conversion) {
									String newConversion = getNewConversion(interp,p2c);
									System.out.println(newConversion);
									if(newConversion != null) {
										Writer output;
										output = new BufferedWriter(new FileWriter("Conversions", true));
										output.append(newConversion);
										output.close();
									}
								}
								
								String v = p2c.print(interp).replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;");
								vars += "<pre>" + v + "</pre>";
							}
							html += "<tr><td>"+p2c.print(p2c.getOriginal())+"</td><td><code>"+ vars +"</code></td></tr>";
							
							
						}
						
					}catch(Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		html += "</table>\n"
				+ "</body>\n"
				+ "</html>";
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter("Demo.html"));
		    	writer.write(html);
			    writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	
	private static String getNewConversion(Document interp, Pres2Cont p2c) {
		Node parent = interp.getFirstChild();
		if(parent.getFirstChild().getNodeName().equals("mrow")&&parent.getChildNodes().getLength() == 1) {
			Node mrow = parent.getFirstChild();
			while(mrow.getFirstChild() != null) {
				parent.appendChild(mrow.getFirstChild());
			}
			parent.removeChild(mrow);
		}
		int varcount = 0;
		List<Node> allNodes = getAllNodes(parent);
		@SuppressWarnings("resource")
		Scanner sc = new Scanner(System.in);
		for(int i = 0; i < allNodes.size(); i ++) {
			Node n = allNodes.get(i);
			System.out.println(n.getNodeName() + " : " + n.getTextContent() + " var?");
			String in = sc.nextLine();
			if(in.equals("y")) {
				varcount++;
				Node child = n.getFirstChild();
				while(child != null) {
					n.removeChild(child);
					child = n.getFirstChild();
				}
				interp.renameNode(n, null, "var");
				n.setTextContent(varcount + "");
				allNodes = getAllNodes(parent);
			}else if (in.equals("x")) {
				return null;
			}
		}
		interp.renameNode(interp.getFirstChild(), null, "pres");
		String pres = p2c.print(interp);
		pres = pres.replaceAll("\\s{2,}", "");
		pres = pres.replaceAll("\n", "");
		pres = pres.replaceAll(" xmlns=\"http://www.w3.org/1998/Math/MathML\" display=\"inline\"", "");
		pres = pres.replaceAll("&", "&amp;");
		pres = presRemoveComments(pres);
		System.out.println(pres + "\nreplacement: ");
		String cont = sc.nextLine().trim();
		if(cont.equals("x")) {
			return null;
		}
		return pres + "%<cont>" + cont + "</cont>%\n";
	}
	
	private static String presRemoveComments(String pres) {
		String r = "";
		int start = pres.indexOf("<!--");
		int end = pres.indexOf(">",start);
		if(pres.indexOf("<!--") < 0) {
			return pres;
		}else {
			r += pres.substring(0, start);
			r += pres.substring(end + 1);
			return presRemoveComments(r);
		}
	}

	private static List<Node> getAllNodes(Node root) {
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

//	private static void getConversions() {
//		List<String> successfulFiles = new ArrayList<String>();
//		List<String> failedFiles = new ArrayList<String>();
//		int uncounted = 0;
//		
//		File allinput = new File("input");
//		for(File inputfolder : allinput.listFiles()) {
//		inputfolder = new File(inputfolder.getPath()+"/Presentation");
//		//File inputfolder = new File("inputOM/Presentation");
//		for(File dir : inputfolder.listFiles()) {
//		//File dir = new File("inputOM/Presentation/calculus1");
//			int folderSuccess = 0;
//			for(File f : dir.listFiles()) {
//				try {
//					//System.out.println(f.getPath());
//					byte[] encoded = Files.readAllBytes(f.toPath());
//					String input = new String(encoded, Charset.defaultCharset());
//					input = input.replaceAll("&", "&amp;");
//					
//					DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
//					InputSource is = new InputSource();
//					is.setCharacterStream(new StringReader(input));
//					Document doc = db.parse(is);
//
//					String presInput = input;
//					
//					
//					String contentPath = f.getPath().replaceAll("Presentation", "Content");
//					File c = new File(contentPath);
//					if(c.exists()) {
//						// Organise the content file text 
//						byte[] b = Files.readAllBytes(c.toPath());
//						String content = new String(b, Charset.defaultCharset());
//						content = content.replaceAll("\\s{2,}", "").replaceAll("\n", "").replaceAll("</math>", "");
//						content = content.substring(content.indexOf(">")+1);
//						int next = content.indexOf(">", 1);
//						int end = content.indexOf("<", next);
//						while(end > 0) {
//							String sub = content.substring(next + 1, end);
//							content = content.substring(0, next + 1) + sub.trim() + content.substring(end);
//							next = content.indexOf(">", next + 1);
//							end = content.indexOf("<", next);
//						}
//						//Organise the converted string
//						presInput = presInput.replaceAll(" xmlns:om=\"http://www.openmath.org/OpenMath\"", "");
//						presInput = presInput.replaceAll("\n", "").replaceAll("</math>", "");
//						presInput = presInput.replaceAll("\\s", "");
//						presInput = presInput.substring(presInput.indexOf(">")+1);
//						
//						next = presInput.indexOf("<!--");
//						end = presInput.indexOf(">", next);
//						while(next > 0) {
//							String sub = presInput.substring(next + 4, end);
//							presInput = presInput.substring(0, next) + presInput.substring(end+1);
//							next = presInput.indexOf("<!--");
//							end = presInput.indexOf(">", next);
//						}
//						if(presInput.startsWith("<mrow>") && presInput.endsWith("</mrow>")) {
//							presInput = presInput.substring(6, presInput.length() - 7);
//						}
//						
//						System.out.println("<pres>" + presInput + "</pres>%<cont>" + content + "</cont>%" + f.getPath());
//					}else {
//					}
//				} catch (SAXException | IOException | ParserConfigurationException e) {
//					e.printStackTrace();
//				}
//			}System.out.println("");
//		}System.out.println("\n\n");
//		}				
//	}

}

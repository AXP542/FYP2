package main;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class TestPresentation {
	private static String htmlOut;

	public static void main(String[] args) {
		htmlOut = "<!DOCTYPE html>\n"  
				+ "<html>\n"
				+ "<head>\n"
				+ "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/mathjax/2.7.1/MathJax.js?config=TeX-AMS-MML_HTMLorMML\"></script>\n"
				+ "<style>\n"
				+ "td, th {\n" 
				+ "  border: 1px solid #dddddd;}"
				+ "</style>"
				+ "</head>"
				+ "<body>"
				+ "<table>\n"
				+ "<tr>"
				+ "<th>Conversion using program</th>\n"
				+ "<th>taken from w3c(expected)</th>\n"
				+ "</tr>";
		
		
		allFiles();
		//singleFile("inputOM/Content/alg1/expression0");
		
		
		htmlOut += "</table>\n"
				+ "</body>\\n"
				+ "</html>";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("Output.html"));
	    	writer.write(htmlOut);
		    writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void singleFile(String filePath) {				
		try {
			File f = new File(filePath);
			byte[] encoded = Files.readAllBytes(f.toPath());
			String input = new String(encoded, Charset.defaultCharset());
			input = input.replaceAll("&", "&amp;");
			
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(input));
			Document doc = db.parse(is);
	
			Content2Presentation mathDoc = new Content2Presentation(doc);
			String converted =  mathDoc.print();
			
			
			String presPath = f.getPath().replaceAll("Content", "Presentation");
			File p = new File(presPath);
			if(p.exists()) {	
				// Organise the content file text 
				byte[] b = Files.readAllBytes(p.toPath());
				String pres = new String(b, Charset.defaultCharset());
				pres = pres.replaceAll("\\s{2,}", "").replaceAll("\n", "");
				pres = pres.replace("display=\"block\"", "display=\"inline\"");
				pres = pres.replace(" xmlns:om=\"http://www.openmath.org/OpenMath\"", "");
				int next = pres.indexOf(">", 1);
				int end = pres.indexOf("<", next);
				while(end > 0) {
					String sub = pres.substring(next + 1, end);
					pres = pres.substring(0, next + 1) + sub.trim() + pres.substring(end);
					next = pres.indexOf(">", next + 1);
					end = pres.indexOf("<", next);
				}
				
				int comment = pres.indexOf("<!--");
				while(comment > 0) {
					next = comment;
					end = pres.indexOf("-->") + 3;
					pres = pres.substring(0, next) + pres.substring(end);
					comment = pres.indexOf("<!--");					
				}
				
				converted = converted.replaceAll(System.getProperty("line.separator"), "");
				System.out.println(converted);
				System.out.println(pres);
				if(!converted.trim().equals(pres.trim())) {
					System.out.println("Conversion does not match");
				}
			}else {
				System.out.println(presPath + " does not exist.");
				System.out.println(converted);
			}
		} catch (SAXException | IOException | ParserConfigurationException | TransformerFactoryConfigurationError e) {
			e.printStackTrace();
		}
		
	}

	private static void allFiles() {
		List<String> successfulFiles = new ArrayList<String>();
		List<String> failedFiles = new ArrayList<String>();
		int uncounted = 0;
		File allinput = new File("input");
		for(File inputfolder : allinput.listFiles()) {
		inputfolder = new File(inputfolder.getPath()+"/Content");
		//File inputfolder = new File("inputOM/Content");
		for(File dir : inputfolder.listFiles()) {
		//File dir = new File("inputOM/Presentation/calculus1");
			int folderSuccess = 0;
			for(File f : dir.listFiles()) {
				System.out.println("Trying " + f.getPath());
				try {
					byte[] encoded = Files.readAllBytes(f.toPath());
					String input = new String(encoded, Charset.defaultCharset());
					input = input.replaceAll("&", "&amp;");
					
					DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					InputSource is = new InputSource();
					is.setCharacterStream(new StringReader(input));
					Document doc = db.parse(is);
					
					String converted;
					try {
					Content2Presentation mathDoc = new Content2Presentation(doc);
					converted =  mathDoc.print();
					}catch(Exception e) {
						System.out.println("fail");
						converted = "error";
					}
					
					
					
					String presPath = f.getPath().replaceAll("Content", "Presentation");
					File p = new File(presPath);
					if(p.exists()) {
						// Organise the content file text 
						byte[] b = Files.readAllBytes(p.toPath());
						String pres = new String(b, Charset.defaultCharset());
						pres = pres.replaceAll("\\s{2,}", "").replaceAll("\n", "");
						pres = pres.replace("display=\"block\"", "display=\"inline\"");
						pres = pres.replace(" xmlns:om=\"http://www.openmath.org/OpenMath\"", "");
						int next = pres.indexOf(">", 1);
						int end = pres.indexOf("<", next);
						while(end > 0) {
							String sub = pres.substring(next + 1, end);
							pres = pres.substring(0, next + 1) + sub.trim() + pres.substring(end);
							next = pres.indexOf(">", next + 1);
							end = pres.indexOf("<", next);
						}
						//Remove comments
						int comment = pres.indexOf("<!--");
						while(comment > 0) {
							next = comment;
							end = pres.indexOf("-->") + 3;
							pres = pres.substring(0, next) + pres.substring(end);
							comment = pres.indexOf("<!--");					
						}
						//Organise the converted string
						converted = converted.replaceAll(System.getProperty("line.separator"), "");
						if(!converted.trim().equalsIgnoreCase(pres.trim())) {
							failedFiles.add(f.getPath());
							folderSuccess ++;								
						}else {
							successfulFiles.add(f.getPath());
						}
						converted = "<p>" + f.getPath() + "</p>" + converted;
						htmlOut += "<tr><td><div>" + converted + "</div></td><td><div>" + pres + "</div></td></tr>";						
					}else {
						System.out.println(presPath + " does not exist.");
						System.out.println(converted);
						uncounted++;
					}
				} catch (SAXException | IOException | ParserConfigurationException e) {
					e.printStackTrace();
				}
			}
			if(folderSuccess == 0)System.out.println("Complete folder: " + dir.getPath());
		}
		}
		
		System.out.println("\n\nEOF!");
		System.out.println("\n STATS:");
		System.out.println("Total: " + (successfulFiles.size()+failedFiles.size()));
		System.out.println("Successful: " + successfulFiles.size());
		System.out.println("Failed: " + failedFiles.size());
		System.out.println("Unmatched files: " + uncounted);
		
	}

}

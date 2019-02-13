package main.conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class VectorMatrices {

	private Document doc;

	public VectorMatrices(Document doc) {
		this.doc = doc;
		convertMatrix();
		convertVector();
		convertPiece();
	}

	private void convertPiece() {
		NodeList nl = doc.getElementsByTagName("piecewise");
		for(int i = 0; i < nl.getLength(); i++) {
			Node pw = nl.item(0);
			doc.renameNode(pw, null, "mrow");
			Node table = doc.createElement("mtable");
			Node piece = pw.getFirstChild();
			Element iff = doc.createElement("mtd");
			iff.setAttribute("columnalign", "left");
			iff.appendChild(doc.createElement("mtext"));
			iff.getFirstChild().setTextContent("&#xa0;if &#xa0;");
			while(piece != null) {
				doc.renameNode(piece, null, "mtr");
				piece.appendChild(doc.createElement("mtd"));
				piece.getLastChild().appendChild(piece.getFirstChild());
				piece.appendChild(doc.createElement("mtd"));
				piece.getLastChild().appendChild(piece.getFirstChild());
				piece.insertBefore(iff.cloneNode(true), piece.getLastChild());
				table.appendChild(piece);
				piece = pw.getFirstChild();
			}
			pw.appendChild(doc.createElement("mo"));
			pw.getLastChild().setTextContent("{");
			pw.appendChild(table);
		}
		
	}

	private void convertVector() {
		NodeList nl = doc.getElementsByTagName("vector");
		for(int i = 0; i < nl.getLength(); i++) {
			Node vector = nl.item(i);
			doc.renameNode(vector, null, "mrow");
			Node table = doc.createElement("mtable");
			NodeList vectorRows = vector.getChildNodes();
			for(int r = 0; r < vectorRows.getLength();r ++) {
				Node trow = doc.createElement("mtr");
				Node td = doc.createElement("mtd");
				td.appendChild(vectorRows.item(r));
				trow.appendChild(td);
				table.appendChild(trow);
				r--;
			}
			Node mfenced = doc.createElement("mfenced");
			vector.appendChild(mfenced);
			mfenced.appendChild(table);
			i--;
		}
		
	}

	private void convertMatrix() {
		NodeList nl = doc.getElementsByTagName("matrix");
		for(int i = 0; i < nl.getLength(); i++) {
			Node matrix = nl.item(i);
			if(matrix.getFirstChild().getNodeName().equals("matrixrow")) {
				doc.renameNode(matrix, null, "mrow");
				Node table = doc.createElement("mtable");
				NodeList matrixRows = matrix.getChildNodes();
				for(int r = 0; r < matrixRows.getLength();r ++) {
					Node trow = doc.createElement("mtr");
					Node d = matrixRows.item(r).getFirstChild();
					while(d!=null) {
						Node td = doc.createElement("mtd");
						td.appendChild(d);
						trow.appendChild(td);
						d = matrixRows.item(r).getFirstChild();
					}
					table.appendChild(trow);
					matrix.removeChild(matrixRows.item(r));
					r--;
				}
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent("(");
				matrix.appendChild(table);
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent(")");
				i--;
			}else {
				Node ivar = matrix.getFirstChild();
				Node jvar = ivar.getNextSibling();
				Node condition = jvar.getNextSibling();
				Node value = condition.getNextSibling();
				ivar = ivar.getFirstChild();
				jvar = jvar.getFirstChild();
				doc.renameNode(matrix, null, "mrow");
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent("[");
				matrix.appendChild(doc.createElement("msub"));
				matrix.getLastChild().appendChild(doc.createElement("mi"));
				matrix.getLastChild().getLastChild().setTextContent("m");
				matrix.getLastChild().appendChild(doc.createElement("mrow"));
				matrix.getLastChild().getLastChild().appendChild(ivar);
				matrix.getLastChild().getLastChild().appendChild(doc.createElement("mo"));
				matrix.getLastChild().getLastChild().getLastChild().setTextContent(",");
				matrix.getLastChild().getLastChild().appendChild(jvar);
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent("|");
				matrix.appendChild(doc.createElement("mrow"));
				matrix.getLastChild().appendChild(doc.createElement("msub"));
				matrix.getLastChild().getLastChild().appendChild(doc.createElement("mi"));
				matrix.getLastChild().getLastChild().getLastChild().setTextContent("m");
				matrix.getLastChild().getLastChild().appendChild(doc.createElement("mrow"));
				matrix.getLastChild().getLastChild().getLastChild().appendChild(ivar.cloneNode(true));
				matrix.getLastChild().getLastChild().getLastChild().appendChild(doc.createElement("mo"));
				matrix.getLastChild().getLastChild().getLastChild().getLastChild().setTextContent(",");
				matrix.getLastChild().getLastChild().getLastChild().appendChild(jvar.cloneNode(true));
				matrix.getLastChild().appendChild(doc.createElement("mo"));
				matrix.getLastChild().getLastChild().setTextContent("=");
				matrix.getLastChild().appendChild(value);
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent(";");
				matrix.appendChild(condition.getFirstChild());
				matrix.appendChild(doc.createElement("mo"));
				matrix.getLastChild().setTextContent("]");
				matrix.removeChild(matrix.getFirstChild());
				matrix.removeChild(matrix.getFirstChild());
				matrix.removeChild(matrix.getFirstChild());
			}
		}
		
	}

}

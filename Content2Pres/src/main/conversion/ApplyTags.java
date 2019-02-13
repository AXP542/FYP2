package main.conversion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ApplyTags {

	private Document doc;

	public ApplyTags(Document doc) {
		this.doc = doc;
		emptyApply();
		timesBrackets();
		while(getNext()!=null) {
			Node apply = getNext();
			convertInfix(apply);
		}
		lambda();
	}

	private void emptyApply() {
		NodeList nl = doc.getElementsByTagName("apply");
		for(int i = 0; i < nl.getLength(); i ++) {
			Node apply = nl.item(i);
			String contentTag = apply.getFirstChild().getNodeName();
			if(contentTag.equals("ci")) {
				doc.renameNode(apply, null, "mrow");
				Node af = doc.createElement("mo");
				af.setTextContent("&#x2061;");
				apply.insertBefore(af, apply.getFirstChild().getNextSibling());
				Node fence = doc.createElement("mfenced");
				while(af.getNextSibling()!=null) {
					fence.appendChild(af.getNextSibling());
				}
				apply.appendChild(fence);
				i--;
				nl = doc.getElementsByTagName("apply");
			}
		}		
	}
	
	private void timesBrackets() {
		NodeList nl = doc.getElementsByTagName("apply");
		for(int i = 0; i < nl.getLength(); i ++) {
			Node apply = nl.item(i);
			String contentTag = apply.getFirstChild().getNodeName();
			if(contentTag.equals("times")) {
				Node next = apply.getFirstChild();
				while(next.getNextSibling() != null) {
					next = next.getNextSibling();
					if(next.hasChildNodes()) {
						if(next.getFirstChild().getNodeName().equals("plus")
								|| next.getFirstChild().getNodeName().equals("minus")) {
							Node plusminus = next;
							next = doc.createElement("mfenced");
							apply.insertBefore(next, plusminus);
							next.appendChild(plusminus);
						}
					}
				}
			}
		}
		
	}
	
	private void lambda() {
		NodeList nl = doc.getElementsByTagName("apply");
		for(int i = 0; i < nl.getLength(); i ++) {
			Node apply = nl.item(i);
			String contentTag = apply.getFirstChild().getNodeName();
			if(contentTag.equals("lambda")) {
				doc.renameNode(apply, null, "mrow");
				Node lambda = apply.getFirstChild();
				Node fenced = doc.createElement("mfenced");
				while(lambda.getNextSibling()!=null) {
					fenced.appendChild(lambda.getNextSibling());
				}
				Node af = doc.createElement("mo");
				af.setTextContent("&#x2061;");
				apply.appendChild(af);
				apply.appendChild(fenced);
				i--;
			}else {
				selector(apply);
				//doc.renameNode(apply, null, "mrow");				
			}
		}
		
	}
	
	private void convertInfix(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		boolean bound = false;
		String boundtag = apply.getFirstChild().getNextSibling().getNodeName();
		if(boundtag.equals("bvar")||boundtag.equals("domainofapplication"))
			bound = true;
		
		if(getPres(contentTag)!=null && apply.getChildNodes().getLength() > 2 && !bound) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			int elements = apply.getChildNodes().getLength();
			String presText = getPres(contentTag);
			Node next = apply.getFirstChild();
			Node presOp = doc.createElement("mo");
			presOp.setTextContent(presText);
			for(int i = 1; i < elements; i ++) {
				next = next.getNextSibling();
				apply.insertBefore(presOp.cloneNode(true), next);
			}
			//Suchthat sets
			if(apply.getNextSibling()!=null&&
					apply.getNextSibling().getTextContent().equals("&#x2227;")) {
				apply.insertBefore(doc.createElement("mo"), apply.getFirstChild());
				apply.getFirstChild().setTextContent("(");
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent(")");
			}
		}else {
			mfrac(apply);
			//doc.renameNode(apply, null, "mrow");				
		}				
	}
	
	private void mfrac(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("divide")) {
			doc.renameNode(apply, null, "mfrac");
			apply.removeChild(apply.getFirstChild());
		}else {
			convertPre(apply);
			//doc.renameNode(apply, null, "mrow");				
		}	
	}
	

	private void convertPre(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(getPres(contentTag)!=null && apply.getChildNodes().getLength() == 2) {
			doc.renameNode(apply, null, "mrow");
			doc.renameNode(apply.getFirstChild(), null, "mo");
			String presText = getPres(contentTag);
			apply.getFirstChild().setTextContent(presText);
		}else {
			convertFence(apply);
			//doc.renameNode(apply, null, "mrow");				
		}	
	}
	
	private void convertFence(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(getFence(contentTag)!=null) {
			doc.renameNode(apply, null, "mrow");
			doc.renameNode(apply.getFirstChild(), null, "mo");
			
			Node sep = doc.createElement("mo");
			sep.setTextContent(",");
			Node next = apply.getFirstChild().getNextSibling().getNextSibling();
			while(next != null) {
				apply.insertBefore(sep.cloneNode(true), next);
				next = next.getNextSibling();
			}
			String open = getFence(contentTag).split(",")[0];
			apply.getFirstChild().setTextContent(open);
			String close = getFence(contentTag).split(",")[1];
			apply.appendChild(doc.createElement("mo"));
			apply.getLastChild().setTextContent(close);
		}else {
			specialFunctions(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void specialFunctions(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("laplacian") || contentTag.equals("grad")) {
			if(apply.getFirstChild().getNextSibling().getNodeName().equals("bvar")) {
				Node fence = doc.createElement("mfenced");
				Node next = apply.getFirstChild().getNextSibling();
				while(next.getNodeName().equals("bvar")) {
					fence.appendChild(next.getFirstChild());
					apply.removeChild(next);
					next = apply.getFirstChild().getNextSibling();
				}
				Node arrow = doc.createElement("mo");
				arrow.setTextContent("&#x21a6;");
				Node row = doc.createElement("mrow");
				row.appendChild(fence);
				row.appendChild(arrow);
				row.appendChild(next);
				apply.appendChild(row);
			}
		}else if(contentTag.equals("divergence")) {
			if(apply.getFirstChild().getNextSibling().getNodeName().equals("bvar")) {
				List<Node>bvars = new ArrayList<Node>();
				Node next = apply.getFirstChild().getNextSibling();
				while(next.getNodeName().equals("bvar")) {
					bvars.add(next.getFirstChild());
					apply.removeChild(next);
					next = apply.getFirstChild().getNextSibling();
				}
				Node arrow = doc.createElement("mo");
				arrow.setTextContent("&#x21a6;");
				Node table = next.getFirstChild().getFirstChild();
				Node row = table.getFirstChild();
				for(Node var : bvars) {
					Node d = row.getFirstChild();
					row.getFirstChild().insertBefore(arrow.cloneNode(true), d.getFirstChild());
					row.getFirstChild().insertBefore(var, row.getFirstChild().getFirstChild());					
					row = row.getNextSibling();
				}
				next.replaceChild(next.getFirstChild().getFirstChild(), next.getFirstChild());
			}
		}
		convertFunction(apply);
	}	

	private void convertFunction(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(getFunction(contentTag)!=null) {
			doc.renameNode(apply, null, "mrow");
			String tag = getFunction(contentTag).split(",")[3];
			doc.renameNode(apply.getFirstChild(), null, tag);
			String function = getFunction(contentTag).split(",")[1];
			boolean fenced = getFunction(contentTag).split(",")[2].equals("y");
			apply.getFirstChild().setTextContent(function);
			if(function.equals("log") && apply.getFirstChild().getNextSibling().getNodeName().equals("logbase")) {
				Node lb = apply.getFirstChild().getNextSibling();
				doc.renameNode(lb, null, "msub");
				lb.insertBefore(apply.getFirstChild(), lb.getLastChild());
			}else if(getFunction(contentTag).split(",")[0].equals("variance")||
					getFunction(contentTag).split(",")[0].equals("laplacian")) {
				Node sig = apply.getFirstChild();
				apply.insertBefore(doc.createElement("msup"), apply.getFirstChild());
				apply.getFirstChild().appendChild(sig);
				apply.getFirstChild().appendChild(doc.createElement("mn"));
				apply.getFirstChild().getLastChild().setTextContent("2");
			}
			Node af = doc.createElement("mo");
			af.setTextContent("&#x2061;");
			apply.insertBefore(af, apply.getFirstChild().getNextSibling());
			if(fenced) {
				Node fence = doc.createElement("mfenced");
				while(af.getNextSibling()!=null) {
					fence.appendChild(af.getNextSibling());
				}
				apply.appendChild(fence);
			}else if(af.getNextSibling().getNodeName().equals("apply")){
				convertInfix(af.getNextSibling());
				Node row = doc.createElement("mrow");
				Node open = doc.createElement("mo");
				open.setTextContent("(");
				Node close = doc.createElement("mo");
				close.setTextContent(")");
				row.appendChild(open);
			
				row.appendChild(af.getNextSibling());
				row.appendChild(close);
				apply.appendChild(row);		
			}
		}else {
			power(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	

	private void power(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("power")) {
			doc.renameNode(apply, null, "msup");
			apply.removeChild(apply.getFirstChild());
			if(apply.getFirstChild().getNodeName().equals("apply")) {
				Node fenced = doc.createElement("mfenced");
				fenced.appendChild(apply.getFirstChild());
				apply.insertBefore(fenced, apply.getFirstChild());
			}
		}else {
			transpose(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	private void transpose(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("transpose")) {
			doc.renameNode(apply, null, "msup");
			apply.removeChild(apply.getFirstChild());
			apply.appendChild(doc.createElement("mi"));
			apply.getLastChild().setTextContent("T");
		}else {
			selector(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}	
	
	private void selector(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("selector")) {
			doc.renameNode(apply, null, "msub");
			apply.removeChild(apply.getFirstChild());
		}else {
			restriction(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void restriction(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("restriction")) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			Node pipe = doc.createElement("mo");
			pipe.setTextContent("|");
			Node msub = doc.createElement("msub");
			msub.appendChild(pipe);
			msub.appendChild(apply.getLastChild());
			apply.appendChild(msub);
		}else {
			moment(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void moment(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("moment")) {
			doc.renameNode(apply, null, "msub");
			Node row = apply.getFirstChild();
			if(row.getNextSibling().getNodeName().equals("degree")) {
				doc.renameNode(row, null, "mrow");
				Node degree = row.getNextSibling();
				Node about = degree.getNextSibling();
				Node open = doc.createElement("mo");
				open.setTextContent("&#x27E8;");
				Node close = doc.createElement("mo");
				close.setTextContent("&#x27E9;");
				Node sup = doc.createElement("msup");			
				int elements = apply.getChildNodes().getLength() - 3;
				Node mfenced = doc.createElement("mfenced");
				if(elements == 1) {
					mfenced = apply.getLastChild();
				}else {
					while(about.getNextSibling()!=null) {
						mfenced.appendChild(about.getNextSibling());
					}
				}			
				row.appendChild(open);
				row.appendChild(sup);
				sup.appendChild(mfenced);
				sup.appendChild(degree.getFirstChild());
				row.appendChild(close);
				apply.appendChild(about.getFirstChild());
				apply.removeChild(degree);
				apply.removeChild(about);
			}else {
				doc.renameNode(apply, null, "mrow");
				Node m = doc.createElement("mi");
				m.setTextContent("moment");
				Node fenced = doc.createElement("mfenced");
				Node af = apply.getFirstChild();
				af.setTextContent("&#x2061;");
				doc.renameNode(af, null, "mo");
				apply.insertBefore(m, af);
				while(af.getNextSibling()!=null) {
					fenced.appendChild(af.getNextSibling());
				}
				apply.appendChild(fenced);
			}
		}else {
			conjugate(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}

	private void conjugate(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("conjugate")) {
			doc.renameNode(apply, null, "mover");
			apply.removeChild(apply.getFirstChild());
			apply.appendChild(doc.createElement("mo"));
			apply.getLastChild().setTextContent("&#xaf;");
		}else {
			exists(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void exists(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("exists")) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			Node row = doc.createElement("mrow");
			row.appendChild(doc.createElement("mo"));
			row.getFirstChild().setTextContent("&#x2203;");
			Node bvar = apply.getFirstChild().getFirstChild();
			apply.removeChild(apply.getFirstChild());
			row.appendChild(bvar.cloneNode(true));
			row.appendChild(doc.createElement("mo"));
			row.getLastChild().setTextContent(".");
			Node cond = doc.createElement("mrow");
			Element fenced = doc.createElement("mfenced");
			fenced.setAttribute("separators", "");
			Node domain = apply.getFirstChild().getFirstChild();
			apply.removeChild(apply.getFirstChild());
			cond.appendChild(bvar);
			cond.appendChild(doc.createElement("mo"));
			cond.getLastChild().setTextContent("&#x2208;");
			cond.appendChild(domain);
			fenced.appendChild(cond);
			row.appendChild(fenced);
			fenced.appendChild(doc.createElement("mo"));
			fenced.getLastChild().setTextContent("&#x2227;");
			fenced.appendChild(apply.getLastChild());
			apply.getParentNode().replaceChild(row, apply);
		}else {
			compose(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void compose(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("compose")) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			Node c = doc.createElement("mo");
			c.setTextContent("&#x2218;");
			int count = apply.getChildNodes().getLength();
			Node next = apply.getFirstChild();
			for(int i = 1; i < count; i ++) {
				next = next.getNextSibling();
				apply.insertBefore(c.cloneNode(true), next);
			}
			Node parent = apply.getParentNode();
			if(parent.getNodeName().equals("apply") && parent.getLastChild()==apply.getNextSibling()) {
				doc.renameNode(parent, null, "mrow");
				apply.insertBefore(doc.createElement("mo"), apply.getFirstChild());
				apply.getFirstChild().setTextContent("(");
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent(")");
				Node af = doc.createElement("mo");
				af.setTextContent("&#x2061;");
				Node fence = doc.createElement("mfenced");
				fence.appendChild(apply.getNextSibling());
				parent.appendChild(af);
				parent.appendChild(fence);
			}
			
		}else {
			inverse(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	private void inverse(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("inverse")) {
			doc.renameNode(apply, null, "msup");
			apply.removeChild(apply.getFirstChild());
			Node mrow = doc.createElement("mrow");
			mrow.appendChild(doc.createElement("mo"));
			mrow.getLastChild().setTextContent("(");
			mrow.appendChild(doc.createElement("mn"));
			mrow.getLastChild().setTextContent("-1");
			mrow.appendChild(doc.createElement("mo"));
			mrow.getLastChild().setTextContent(")");
			apply.insertBefore(mrow, apply.getFirstChild().getNextSibling());
			
			Node parent = apply.getParentNode();
			if(parent.getNodeName().equals("apply") && parent.getLastChild()==apply.getNextSibling()) {
				doc.renameNode(parent, null, "mrow");
				Node af = doc.createElement("mo");
				af.setTextContent("&#x2061;");
				Node fence = doc.createElement("mfenced");
				fence.appendChild(apply.getNextSibling());
				parent.appendChild(af);
				parent.appendChild(fence);
			}			
		}else {
			root(apply);
			//doc.renameNode(apply, null, "mrow");				
		}		
	}

	
	private void root(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("root")) {
			doc.renameNode(apply, null, "mroot");
			apply.removeChild(apply.getFirstChild());
			Node degree = apply.getFirstChild();
			apply.appendChild(degree.getFirstChild());
			apply.removeChild(degree);
			if(apply.getLastChild().getTextContent().equals("2")) {
				apply.removeChild(apply.getLastChild());
				doc.renameNode(apply, null, "msqrt");
			}
		}else {
			factorial(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}

	private void factorial(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("factorial")) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			Node f = doc.createElement("mo");
			f.setTextContent("!");
			apply.appendChild(f);
		}else {
			quotient(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void quotient(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("quotient")) {
			doc.renameNode(apply, null, "mrow");
			apply.removeChild(apply.getFirstChild());
			Node o = doc.createElement("mo");
			o.setTextContent("&#x230a;");
			Node c = doc.createElement("mo");
			c.setTextContent("&#x230b;");
			Node d = doc.createElement("mo");
			d.setTextContent("/");
			apply.insertBefore(o, apply.getFirstChild());
			apply.insertBefore(d, apply.getLastChild());
			apply.appendChild(c);
		}else {
			exp(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void exp(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("exp")) {
			doc.renameNode(apply.getFirstChild(), null, "power");
			Node e = doc.createElement("exponentiale");
			apply.insertBefore(e, apply.getLastChild());			
		}else {
			bigfloat(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void bigfloat(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("bigfloat")) {
			doc.renameNode(apply.getFirstChild(), null, "mo");
			apply.getFirstChild().setTextContent("&#xD7;");
			Node times = apply.getFirstChild();
			Node m = times.getNextSibling();
			Node r = m.getNextSibling();
			Node e = r.getNextSibling();
			Node sup = doc.createElement("msup");
			sup.appendChild(r);
			sup.appendChild(e);
			apply.appendChild(times);
			apply.appendChild(sup);
		}else {
			limit(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void limit(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("limit")) {
			Node bvar = apply.getFirstChild().getNextSibling();
			if(bvar.getNextSibling().getNodeName().equals("lowlimit")) {
				Node ll = bvar.getNextSibling();
				doc.renameNode(ll, null, "condition");
				Node tt = doc.createElement("apply");
				tt.appendChild(doc.createElement("tendsto"));
				tt.appendChild(bvar.getFirstChild().cloneNode(true));
				tt.appendChild(ll.getFirstChild());
				ll.appendChild(tt);
				limit(apply);
			}else if (bvar.getNextSibling().getNodeName().equals("condition")) {
				doc.renameNode(apply, null, "mrow");
				Node lim = apply.getFirstChild();
				doc.renameNode(lim, null, "mi");
				lim.setTextContent("lim");
				Node munder = bvar.getNextSibling();
				doc.renameNode(munder, null, "munder");
				munder.insertBefore(lim, munder.getFirstChild());
				apply.removeChild(bvar);
			}
		}else {
			tendsto(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	private void tendsto(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("tendsto")) {
			doc.renameNode(apply, null, "mrow");
			Element arrow = (Element) apply.getFirstChild();
			doc.renameNode(arrow, null, "mo");
			arrow.setTextContent("&#x2192;");
			apply.insertBefore(arrow, apply.getLastChild());
			if(arrow.getAttribute("type").equals("above")) {
				Node last = apply.getLastChild();
				apply.appendChild(doc.createElement("msup"));
				apply.getLastChild().appendChild(last);
				apply.getLastChild().appendChild(doc.createElement("mo"));
				apply.getLastChild().getLastChild().setTextContent("+");
				arrow.removeAttribute("type");
			}else if(arrow.getAttribute("type").equals("below")) {
				Node last = apply.getLastChild();
				apply.appendChild(doc.createElement("msub"));
				apply.getLastChild().appendChild(last);
				apply.getLastChild().appendChild(doc.createElement("mo"));
				apply.getLastChild().getLastChild().setTextContent("-");
				arrow.removeAttribute("type");
			}
		}else {
			integral(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void integral(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("int")) {
			Node p = apply.getParentNode();
			if(p.getNodeName().equals("apply")) {
				doc.renameNode(p, null, "mrow");
				Node bigfence = doc.createElement("mfenced");
				Node varfence = doc.createElement("mfenced");
				bigfence.appendChild(apply);
				while(p.getFirstChild()!= null) {
					varfence.appendChild(p.getFirstChild());
				}
				p.appendChild(bigfence);
				p.appendChild(doc.createElement("mo"));
				p.getLastChild().setTextContent("&#x2061;");
				p.appendChild(varfence);
			}
			
			doc.renameNode(apply, null, "mrow");
			doc.renameNode(apply.getFirstChild(), null, "mi");
			apply.getFirstChild().setTextContent("&#x222b;");
			Node integral = apply.getFirstChild();
			if(integral.getNextSibling().getNodeName().equals("interval")){
				Node interval = integral.getNextSibling();
				Node msubsup = doc.createElement("msubsup");
				msubsup.appendChild(integral);
				msubsup.appendChild(interval.getFirstChild());
				msubsup.appendChild(interval.getLastChild());	
				apply.replaceChild(msubsup, interval);
			}else if(integral.getNextSibling().getNodeName().equals("bvar")){
				Node bvar = integral.getNextSibling();
				Node lowlimit = null;
				Node uplimit = null;
				Node msubsup = null;
				if(bvar.getNextSibling().getNodeName().equals("lowlimit")) {
					lowlimit = bvar.getNextSibling();
					uplimit = lowlimit.getNextSibling();
					msubsup = doc.createElement("msubsup");
					msubsup.appendChild(integral);
					msubsup.appendChild(lowlimit.getFirstChild());
					msubsup.appendChild(uplimit.getFirstChild());
					apply.insertBefore(msubsup, apply.getFirstChild());
					apply.removeChild(lowlimit);
					apply.removeChild(uplimit);
				}else if(bvar.getNextSibling().getNodeName().equals("uplimit")) {
					uplimit = bvar.getNextSibling();
					lowlimit = uplimit.getNextSibling();
					msubsup = doc.createElement("msubsup");
					msubsup.appendChild(integral);
					msubsup.appendChild(lowlimit.getFirstChild());
					msubsup.appendChild(uplimit.getFirstChild());
					apply.insertBefore(msubsup, apply.getFirstChild());
					apply.removeChild(lowlimit);
					apply.removeChild(uplimit);
				}else {
					msubsup = integral;
				}
				apply.appendChild(doc.createElement("mi"));
				apply.getLastChild().setTextContent("d");
				apply.appendChild(bvar.getFirstChild());
				apply.removeChild(bvar);
			}else if(integral.getNextSibling().getNodeName().equals("domainofapplication")) {
				Node domain = integral.getNextSibling();
				Node msub = doc.createElement("msub");
				msub.appendChild(integral);
				msub.appendChild(domain.getFirstChild());	
				apply.replaceChild(msub, domain);
				Node bvar = msub.getNextSibling();
				apply.appendChild(doc.createElement("mi"));
				apply.getLastChild().setTextContent("d");
				apply.appendChild(bvar.getFirstChild());
				apply.removeChild(bvar);
			}
			
			
		}else {
			differential(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void differential(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("diff") ) {
			apply.removeChild(apply.getFirstChild());
			Node p = apply.getParentNode();
			if(p.getNodeName().equals("apply") && p.getFirstChild().equals(apply)) {
				doc.renameNode(p, null, "mrow");
				Node bigfence = doc.createElement("mfenced");
				Node varfence = doc.createElement("mfenced");
				bigfence.appendChild(apply);
				while(p.getFirstChild()!= null) {
					varfence.appendChild(p.getFirstChild());
				}
				p.appendChild(bigfence);
				p.appendChild(doc.createElement("mo"));
				p.getLastChild().setTextContent("&#x2061;");
				p.appendChild(varfence);
			}
			
			if(apply.getFirstChild().getNodeName().equals("ci")) {
				doc.renameNode(apply, null, "msup");
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent("&#x2032;");
			}else if(apply.getFirstChild().getNodeName().equals("bvar")){
				doc.renameNode(apply, null, "mfrac");
				Node bvar = apply.getFirstChild();
				Node degree = null;
				if(bvar.getLastChild().getNodeName().equals("degree")) {
					degree = bvar.getLastChild();
				}
				Node func = bvar.getNextSibling();
				Node row = doc.createElement("mrow");
				row.appendChild(doc.createElement("mi"));
				row.getFirstChild().setTextContent("d");
				apply.insertBefore(row, func);
				row.appendChild(func);
				if(!func.getNodeName().equals("ci")) {
					Node fenced = doc.createElement("mfenced");
					fenced.appendChild(func);
					Node parent = doc.createElement("mrow");
					apply.getParentNode().insertBefore(parent, apply);
					parent.appendChild(apply);
					parent.appendChild(doc.createElement("mo"));
					parent.getLastChild().setTextContent("&#x2062;");
					parent.appendChild(fenced);
				}
				Node brow = doc.createElement("mrow");
				brow.appendChild(doc.createElement("mi"));
				brow.getFirstChild().setTextContent("d");
				brow.appendChild(bvar.getFirstChild());
				if(degree != null) {
					Node upsup = doc.createElement("msup");
					Node losup = doc.createElement("msup");
					upsup.appendChild(row.getFirstChild());
					upsup.appendChild(degree.getFirstChild().cloneNode(true));
					row.insertBefore(upsup, row.getFirstChild());
					losup.appendChild(brow.getLastChild());
					losup.appendChild(degree.getFirstChild().cloneNode(true));
					brow.appendChild(losup);
				}
				apply.appendChild(brow);
				apply.removeChild(bvar);
			}else {
				doc.renameNode(apply, null, "mrow");
				Node func = apply.getFirstChild();
				Node d = doc.createElement("mo");
				d.setTextContent("D");
				apply.insertBefore(d, func);
				Node af = doc.createElement("mo");
				af.setTextContent("&#x2061;");
				apply.insertBefore(af, func);
				Node fence = doc.createElement("mfenced");
				apply.insertBefore(fence, func);
				fence.appendChild(func);
			}
		}else {
			partdiff(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}

	private void partdiff(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("partialdiff") ) {
			
			Node p = apply.getParentNode();
			if(p.getNodeName().equals("apply")) {
				doc.renameNode(p, null, "mrow");
				Node bigfence = doc.createElement("mfenced");
				Node varfence = doc.createElement("mfenced");
				bigfence.appendChild(apply);
				while(p.getFirstChild()!= null) {
					varfence.appendChild(p.getFirstChild());
				}
				p.appendChild(bigfence);
				p.appendChild(doc.createElement("mo"));
				p.getLastChild().setTextContent("&#x2061;");
				p.appendChild(varfence);
			}
			
			apply.removeChild(apply.getFirstChild());
			if(apply.getFirstChild().getNodeName().equals("mrow") && 
					apply.getLastChild().getNodeName().equals("lambda")) {
				Node list = apply.getFirstChild();
				list.removeChild(list.getFirstChild());
				doc.renameNode(list, null, "partialdiff");
				Node lambda = apply.getLastChild();
				Node next = lambda.getFirstChild();
				int vars = 0;
				while(next != null) {
					if(next.getNodeName().equals("bvar")) {
						vars++;
					}
					next = next.getNextSibling();
				}
				
				int[] varcount = new int[vars];
				for(int i : varcount) {
					varcount[i] = 0;
				}
				next = list.getFirstChild();
				int total = 0;
				while(next != null) {
					String s = next.getTextContent();
					int v = Integer.parseInt(s);
					varcount[v-1]++;
					list.removeChild(list.getFirstChild());
					list.removeChild(list.getFirstChild());
					next = list.getFirstChild();
					total++;
				}
				for(int i = 0; i < vars; i++) {
					next = lambda.getFirstChild();
					if(varcount[i] > 0) {
						Node degree = doc.createElement("degree");
						next.appendChild(degree);
						degree.appendChild(doc.createElement("mn"));
						degree.getFirstChild().setTextContent(varcount[i]+"");
						apply.insertBefore(next, lambda);
					}else {
						lambda.removeChild(next);
					}
				}
				Node d = doc.createElement("degree");
				d.appendChild(doc.createElement("mn"));
				d.getFirstChild().setTextContent(total+"");
				apply.insertBefore(d, lambda);
				apply.appendChild(lambda.getFirstChild());
				apply.removeChild(lambda);
				
			}else if(apply.getFirstChild().getNodeName().equals("nthdiff")) {
				Node sup = doc.createElement("msup");
				apply.insertBefore(sup, apply.getFirstChild());
				sup.appendChild(doc.createElement("mi"));
				sup.getFirstChild().setTextContent("D");
				sup.appendChild(sup.getNextSibling());
				doc.renameNode(sup.getLastChild(), null, "mrow");
				Node fenced = doc.createElement("mfenced");
				fenced.appendChild(sup.getNextSibling());
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent("&#x2061;");
				apply.appendChild(fenced);
			}else if(!apply.getFirstChild().getNodeName().equals("bvar") && 
					!apply.getLastChild().getNodeName().equals("lambda")) {
				doc.renameNode(apply, null, "mrow");
				Node sub = doc.createElement("msub");
				apply.insertBefore(sub, apply.getFirstChild());
				sub.appendChild(doc.createElement("mi"));
				sub.getFirstChild().setTextContent("D");
				sub.appendChild(sub.getNextSibling());
				sub.getLastChild().removeChild(sub.getLastChild().getFirstChild());
				sub.getLastChild().removeChild(sub.getLastChild().getLastChild());
			
			}else {
				String d = "&#x2202;";
				doc.renameNode(apply, null, "mfrac");
				List<Node>bvars = new ArrayList<Node>();
				Node degree = null;
				Node next = apply.getFirstChild();
				while(next.getNodeName().equals("bvar")) {
					bvars.add(next);
					next = next.getNextSibling();
				}
				if(next.getNodeName().equals("degree")) {
					degree = next;
					next = next.getNextSibling();
				}else {
					degree = doc.createElement("degree");
					degree.appendChild(doc.createElement("mn"));
					degree.getFirstChild().setTextContent(bvars.size()+"");
					apply.insertBefore(degree, next);
				}
				Node bottomRow = doc.createElement("mrow");
				for(Node bvar : bvars) {
					doc.renameNode(bvar, null, "mrow");
					if(bvar.getLastChild().getTextContent().equals("1")) {
						bvar.removeChild(bvar.getLastChild());
					}
					if(bvar.getLastChild().getNodeName().equals("degree")) {
						Node bd = bvar.getLastChild();
						doc.renameNode(bd, null, "msup");
						bvar.getLastChild().insertBefore(bvar.getFirstChild(), bd.getFirstChild());
					}
					bvar.insertBefore(doc.createElement("mo"), bvar.getFirstChild());
					bvar.getFirstChild().setTextContent(d);
					bottomRow.appendChild(bvar);
				}

				Node func = next;
				Node row = doc.createElement("mrow");
				row.appendChild(doc.createElement("mo"));
				row.getFirstChild().setTextContent(d);
				apply.insertBefore(row, func);
				row.appendChild(func);
				if(degree != null) {
					Node upsup = doc.createElement("msup");
					upsup.appendChild(row.getFirstChild());
					upsup.appendChild(degree.getFirstChild());
					row.insertBefore(upsup, row.getFirstChild());
					apply.removeChild(degree);
				}
				if(!func.getNodeName().equals("ci")) {
					Node fenced = doc.createElement("mfenced");
					fenced.appendChild(func);
					Node parent = doc.createElement("mrow");
					apply.getParentNode().insertBefore(parent, apply);
					parent.appendChild(apply);
					parent.appendChild(doc.createElement("mo"));
					parent.getLastChild().setTextContent("&#x2062;");
					parent.appendChild(fenced);
				}
				apply.appendChild(bottomRow);
			}
		}else {
			minmax(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void minmax(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(contentTag.equals("min")||contentTag.equals("max")) {
			doc.renameNode(apply, null, "mrow");
			doc.renameNode(apply.getFirstChild(),null,"mi");
			Node open = apply.getFirstChild();
			open.setTextContent(contentTag);
			if(open.getNextSibling().getNodeName().equals("bvar")) {
				apply.removeChild(open.getNextSibling());
				Node condition = open.getNextSibling();
				apply.appendChild(doc.createElement("mo"));
				apply.getLastChild().setTextContent("|");
				apply.appendChild(condition.getFirstChild());
				apply.removeChild(condition);
			}else	{
				Node sep = doc.createElement("mo");sep.setTextContent(",");
				Node next = open.getNextSibling().getNextSibling();
				while(next!=null) {
					apply.insertBefore(sep.cloneNode(true), next);
					next= next.getNextSibling();
				}
			}
			Node row = doc.createElement("mrow");
			Node next = open.getNextSibling();
			while(next!=null) {
				row.appendChild(next);
				next = open.getNextSibling();
			}			
			row.insertBefore(doc.createElement("mo"),row.getFirstChild());
			row.getFirstChild().setTextContent("{");
			row.appendChild(doc.createElement("mo"));
			row.getLastChild().setTextContent("}");
			apply.appendChild(row);
		}else {
			overUnder(apply);
			//doc.renameNode(apply, null, "mrow");				
		}
	}
	
	private void overUnder(Node apply) {
		String contentTag = apply.getFirstChild().getNodeName();
		if(getBigSymbol(contentTag)!=null) {
			doc.renameNode(apply, null, "mrow");
			String tag = getBigSymbol(contentTag);
			doc.renameNode(apply.getFirstChild(), null, "mo");
			String symbol = tag.split(",")[1];
			Node bvar = null;
			Node lowlimit = null;
			Node uplimit = null;
			Node condition = null;
			Node domain = null;
			Node interval = null;
			Node lambda = null;
			NodeList nl = apply.getChildNodes();
			for(int i = 0; i < nl.getLength(); i++) {
				String name = nl.item(i).getNodeName();
				switch(name) {
				case "bvar":
					bvar = nl.item(i);
					break;
				case "lowlimit":
					lowlimit = nl.item(i);
					break;
				case "uplimit":
					uplimit = nl.item(i);
					break;	
				case "condition":
					condition = nl.item(i);
					break;
				case "domainofapplication":
					domain = nl.item(i);
					break;
				case "interval":
					interval = nl.item(i);
					break;
				case "bind":
					lambda = nl.item(i);
					break;
				}
				
				if(bvar!=null && lowlimit!=null && uplimit!=null) {
					Node munderover = doc.createElement("munderover");
					munderover.appendChild(doc.createElement("mo"));
					munderover.getFirstChild().setTextContent(symbol);
					Node under = doc.createElement("mrow");
					under.appendChild(bvar.getFirstChild());
					under.appendChild(doc.createElement("mo"));
					under.getLastChild().setTextContent("=");
					under.appendChild(lowlimit.getFirstChild());
					munderover.appendChild(under);
					munderover.appendChild(uplimit.getFirstChild());
					apply.removeChild(bvar);
					apply.removeChild(lowlimit);
					apply.removeChild(uplimit);
					apply.replaceChild(munderover, apply.getFirstChild());
				}else if(bvar!=null && condition!=null) {
					Node munder = doc.createElement("munder");
					munder.appendChild(doc.createElement("mo"));
					munder.getFirstChild().setTextContent(symbol);
					Node under = condition.getFirstChild();
					munder.appendChild(under);
					apply.removeChild(bvar);
					apply.removeChild(condition);
					apply.replaceChild(munder, apply.getFirstChild());
				}else if(domain!=null) {
					Node munder = doc.createElement("munder");
					munder.appendChild(doc.createElement("mo"));
					munder.getFirstChild().setTextContent(symbol);
					Node under = domain.getFirstChild();
					munder.appendChild(under);
					apply.removeChild(domain);
					if(bvar!=null)apply.removeChild(bvar);
					apply.replaceChild(munder, apply.getFirstChild());
				}else if(interval != null && lambda != null) {
					Node munderover = doc.createElement("munderover");
					bvar = lambda.getFirstChild().getNextSibling();
					lowlimit = interval.getFirstChild();
					uplimit = interval.getLastChild();
					munderover.appendChild(doc.createElement("mo"));
					munderover.getFirstChild().setTextContent(symbol);
					Node under = doc.createElement("mrow");
					under.appendChild(bvar.getFirstChild());
					under.appendChild(doc.createElement("mo"));
					under.getLastChild().setTextContent("=");
					under.appendChild(lowlimit);
					munderover.appendChild(under);
					munderover.appendChild(uplimit);
					apply.replaceChild(munderover, apply.getFirstChild());
					apply.appendChild(lambda.getLastChild());
					apply.removeChild(lambda);
					apply.removeChild(interval);
				}
			}			
		}else {
			doc.renameNode(apply, null, "mrow");				
		}		
	}
	
	//Get mathods
	
	private String getBigSymbol(String contentTag) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("BigSymbols"));
			for(String op : lines) {
				String c = op.split(",")[0];
				if(c.equals(contentTag)) {
					return op;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String getPres(String contentTag) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("OperationTags"));
			for(String op : lines) {
				String c = op.split(",")[0];
				if(c.equals(contentTag)) {
					return op.split(",")[1];
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getFence(String contentTag) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("FenceTags"));
			for(String op : lines) {
				String c = op.split(",")[0];
				if(c.equals(contentTag)) {
					return op.split(",")[1] + "," + op.split(",")[2] ;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String getFunction(String contentTag) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("functionTags"));
			for(String op : lines) {
				String c = op.split(",")[0];
				if(c.equals(contentTag)) {
					return op;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Node getNext() {
		NodeList nl = doc.getElementsByTagName("apply");
		for(int i = 0; i < nl.getLength(); i++) {
			if(!nl.item(i).getFirstChild().hasChildNodes()) {
				return nl.item(i);
			}
		}
		return null;
	}
	

}

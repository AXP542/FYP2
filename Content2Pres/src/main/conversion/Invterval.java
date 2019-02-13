package main.conversion;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class Invterval {

	private Document doc;

	public Invterval(Document doc) {
		this.doc = doc;
		convert();
	}
	
	private Element get() {
		NodeList nl = doc.getElementsByTagName("interval");
		Element mfenced = (Element) nl.item(0);
		return mfenced;
	}

	private void convert() {
		while(get()!=null) {
			Element mfenced = get();
			doc.renameNode(mfenced, null, "mfenced");
			String closure = mfenced.getAttribute("closure");
			mfenced.removeAttribute("closure");
			String open = null;
			String close = null;
			switch(closure) {
			case "closed":
				open = "[";
				close = "]";
				break;
			case "closed-open":
				open = "[";
				close = ")";
				break;
			case "open-closed":
				open = "(";
				close = "]";
				break;
			}
			if(open!=null)mfenced.setAttribute("open", open);
			if(close!=null)mfenced.setAttribute("close", close);
		}
		
	}
	

}

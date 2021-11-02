package nl.tue.ieis.is.CMMN;
import org.jdom.Element;
import org.jdom.Namespace;

public class Event extends PlanItemDefinition { 
	
	public Event(String n,Stage c){
		super(n,c);
		this.setName(n);
	}
	
	
	public boolean isEvent(){
		return true;
	}
	
	public Element printElement(Namespace ns){
		Element el=new Element("eventListener");
		el.setNamespace(ns);
		el.setAttribute("name",getName());
		el.setAttribute("id",getId());
		return el;
	}

}

package nl.tue.ieis.is.CMMN;
import org.jdom.Element;
import org.jdom.Namespace;

public class Milestone extends PlanItemDefinition {

	public Milestone(String n,Stage c) {
		super(n,c);
		// TODO Auto-generated constructor stub
	}
	
	
	public boolean isMilestone(){
		return true;
	}
	
	public Element printElement(Namespace ns){
		Element el=new Element("milestone");
		el.setNamespace(ns);
		el.setAttribute("name",getName());
		el.setAttribute("id",getId());
		return el;
	}
}

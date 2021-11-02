package nl.tue.ieis.is.CMMN;

import org.jdom.Element;
import org.jdom.Namespace;

public class Task extends PlanItemDefinition {

	public Task(String n,Stage c) {
		super(n,c);
		// TODO Auto-generated constructor stub
	}
	
	public boolean isTask(){
		return true;
	}
	
	public Element printElement(Namespace ns){
		Element el=new Element("task");
		el.setNamespace(ns);
		el.setAttribute("name",getName());
		el.setAttribute("id",getId());
		return el;
	}
	
	
}

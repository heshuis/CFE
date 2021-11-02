package nl.tue.ieis.is.CMMN;

import java.util.HashSet;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

public class PlanItemDefinition extends CMMNElement {
	private String name;
	private Stage context;
	
	public PlanItemDefinition(String n,Stage c){
		context=c; // null if planmodel (root)
		name=n;
	}
	
	
	public void setName(String n){
		name=n;
	}

	public String getName(){
		return name;
	}
	
	public Stage getContext(){
		return context;
	}
	
	public void setContext(Stage s){
		context=s;
	}

	public boolean isCompound(){
		return false;
	}
	
	public boolean equals(PlanItemDefinition pi){
		return pi.getName().equals(name)&&pi.getClass().equals(this.getClass());
	}
	
	public void compose(PlanItemDefinition pid){// nothing to do if milestone etc., only for stage
		return;
	}
	
	public boolean isStage(){
		return false;
	}
	
	public boolean isMilestone(){
		return false;
	}
	
	public boolean isTask(){
		return false;
	}
	
	public boolean isEvent(){
		return false;
	}
	
	public boolean isSimilar(PlanItemDefinition pid) {
		if (this.name.equals(pid.getName())&&pid.getClass().equals(this.getClass())) return true;
		return false;
	}
	
	
	// true if both this and pid are stages and every child of this is also child of pid and this has more children
	public boolean isRefinedBy(PlanItemDefinition pid) {
		CaseSchema cs=pid.getContext().getCaseContext();
		if (!this.isSimilar(pid)) return false;
		if (this.isStage()) {
			Set<PlanItemDefinition> this_children=((Stage )this).getChildPlanItemDefs();
			Set<PlanItemDefinition> pid_children=((Stage )pid).getChildPlanItemDefs();
			System.out.println(this_children.size());
			System.out.println(pid_children.size());
			if (this_children.size()>=pid_children.size()) return false;
			for (PlanItemDefinition pid_this:this_children) {
				PlanItemDefinition counter_pid_this=cs.getCounterPart(pid_this);
				if (!pid_children.contains(counter_pid_this)) return false;
			}
		}
		else {
			return false;
		}
		return true;
	}
		
	public Element printElement(Namespace ns){// abstract class
		return null;
	}	
}

package nl.tue.ieis.is.CMMN;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.jdom.Element;
import org.jdom.Namespace;

public class Stage extends PlanItemDefinition {

	private Set<PlanItem> planItems;
	private Set<PlanItemDefinition> planItemDefs;
	private Set<Sentry> sentries;
	boolean isPlanModel;
	private CaseSchema caseContext;

	public Stage(String n,Stage c, CaseSchema cs) {
		super(n,c);
		planItems=new HashSet<PlanItem>();
		planItemDefs=new HashSet<PlanItemDefinition>();
		sentries=new HashSet<Sentry>();
		isPlanModel=false;
		caseContext=cs;
	}

	
	public void setPlanModel(){
		isPlanModel=true;
	}
	
	public boolean isPlanModel(){
		return isPlanModel;
	}
	
	public void addChildPlanItem(PlanItem i){
		planItems.add(i);
		caseContext.addPlanItem(i);
	}
	
	public Set<PlanItem> getChildPlanItems(){
		return planItems;
	}
	
	public void addChildPlanItemDef(PlanItemDefinition i){
		planItemDefs.add(i);
		caseContext.addPlanItemDefinition(i);
	}
	
	public void addHierarchicalChildPlanItemDef(PlanItemDefinition i){
		planItemDefs.add(i);
		if (i.isCompound()){
			Set<PlanItemDefinition> pids=((Stage)i).getChildPlanItemDefs();
			for (PlanItemDefinition pid:pids){
				if (!this.caseContext.findPlanItemDefinition(pid)) caseContext.addPlanItemDefinition(pid);
			}
			
		}
		caseContext.addPlanItemDefinition(i);
	}
	
	public Set<PlanItemDefinition> getChildPlanItemDefs(){
		return planItemDefs;
	}
	
	public void removeAll(Set<PlanItem> toremove) {
		for (PlanItem pi:toremove) {
			planItemDefs.remove(pi.getPlanItemDefinition());
		}
		planItems.removeAll(toremove);
		for (PlanItemDefinition pid:planItemDefs) {
			if (pid.isStage()) {
				((Stage)pid).removeAll(toremove);
			}
		}
	}
	
	public void addSentry(Sentry s){
		sentries.add(s);
		caseContext.addSentry(s);
	}
	
	public void removeSentry(Sentry s) {
		sentries.remove(s);
		caseContext.removeSentry(s);
	}
	
	public Set<Sentry> getSentries(){
		return sentries;
	}
	
	public Set<Sentry> getSentries(PlanItem pi){
		Set<Sentry>  ss = new HashSet();
		Set<Criterion> cs = new HashSet(pi.getEntryRefs());
		cs.addAll(pi.getExitRefs());
		for (Criterion c:cs){
			String sid=c.getSentryRef();
			for (Sentry s:sentries){
				if (ss.contains(s)) continue;
				if (s.getId().equals(sid)){
					ss.add(s);
				}
			}
		}
		return ss;
	}
	
	public void addSentries(Set<Sentry> ss){
		sentries.addAll(ss);
		caseContext.addSentries(ss);
	}
	
	public void removeSentries(Set<Sentry> ss){
		sentries.removeAll(ss);
		caseContext.removeSentries(ss);
	}
	
	public void removeAllSentries(){
		sentries.clear();
	}
	
	public Sentry getSentry(String id){
		for (Sentry s:sentries){
			if (s.getId().equals(id)){
				return s;
			}
		}
		return null;
	}
	
	// add this and all descendants to the case context of stage s
	public void setCaseContext(Stage s){
		for (PlanItemDefinition pid:planItemDefs){
			s.getCaseContext().addPlanItemDefinition(pid);
			if (pid.isStage()){
				((Stage)pid).setCaseContext(s);
			}
		}
		//repeat for planitems contained in this
		for (PlanItem pi:planItems){
			s.getCaseContext().addPlanItem(pi);
		}
		caseContext=s.caseContext;
	}
	
	public CaseSchema getCaseContext(){
		return caseContext;
	}
	
	public boolean isAtomic(){
		return planItems.isEmpty() && planItemDefs.isEmpty();
	}
	
	public boolean isCompound(){
		return !isAtomic();
	}
	
	public boolean isStage(){
		return true;
	}
	
	public boolean hasPlanItem(PlanItem p){
		for (PlanItem pi:planItems){
			if (pi.equals(p)){
				return true;
			}
		}
		return false;
	}
	
	
	public boolean verify(CaseSchema cs){
		for (PlanItem pi:planItems){
			if (!cs.findPlanItem(pi)){
				return false;
			}
		}
		for (PlanItemDefinition pid:planItemDefs){
			if (!cs.findPlanItemDefinition(pid)){
				return false;
			}
		}
		return true;
	}
	

	
	
	// assumption: name s equals name this
	public void compose(PlanItemDefinition comp_pid){		
		Stage s=(Stage)comp_pid;
		Set<PlanItem> s_planItems=s.getChildPlanItems();

		boolean found=false;
		Set<PlanItemDefinition> s_planItemDefs=s.getChildPlanItemDefs();
		for (PlanItemDefinition s_pid:s_planItemDefs){
			found=false;
			for (PlanItemDefinition pid:planItemDefs){
				if (s_pid.equals(pid)){ // s_pid and pid have same name
					pid.compose(s_pid);
					found=true;
				}
			}
			if (!found){ // 
				this.addHierarchicalChildPlanItemDef(s_pid); 
				s_pid.setContext(this);
				
				if (s_pid.isStage()){ 
					((Stage)s_pid).setCaseContext(this);
				}
			}
		}

		Set<onPart> so=new HashSet();
		Set<PlanItem> s_pi_new=new HashSet<PlanItem>();
		Set<List<PlanItem>> to_compose_list=new HashSet<List<PlanItem>>();
		found=false;
		for (PlanItem s_pi:s_planItems){
			found=false;
			for (PlanItem pi:planItems){
				if (s_pi.isSimilar(pi)){
					List<PlanItem> compose_pair=new ArrayList<PlanItem>();
					compose_pair.add(pi);
					compose_pair.add(s_pi);
					to_compose_list.add(compose_pair);
					found=true;
				}
			}
			if (!found){
				s_pi_new.add(s_pi);	
			}
		}
		for (PlanItem s_pi:s_pi_new) {
			this.addChildPlanItem(s_pi);
			PlanItemDefinition s_pid=s_pi.getPlanItemDefinition();
			s_pid.setContext(this);
			this.addChildPlanItemDef(s_pid);
			s_pi.setContext(this);
			Set<Sentry> ss=s.getSentries(s_pi);
			for (Sentry se:ss){
				this.addSentry(se);
				so.addAll(se.getOnParts());
			}
			this.addSentries(s.getSentries(s_pi)); // sentries referred to by s_pi // sentries that contain on-parts should be updated
			
		}

		for (List<PlanItem> l:to_compose_list) {
			PlanItem pi=l.get(0);
			PlanItem s_pi=l.get(1);
			pi.compose(s_pi); // sentries are modified in this function
		}
		
		for (onPart o:so){
			String sourceref=o.getSourceRef();
			PlanItem ppll=((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref);
			PlanItemDefinition pidpid=((Stage)comp_pid).getCaseContext().findPlanItemDefinitionByID(ppll.getDefinitionRef());
			String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();
			PlanItem p=this.caseContext.findPlanItemByPlanItemDefName(sourcerefName);
			if (p==null){
				System.out.println("Error: no PlanItem with name " + sourcerefName + " for "  + sourceref + " in context " + caseContext.getName());
				System.out.println("when composing with " + caseContext.getName() +" and " + comp_pid.getContext().getCaseContext().getName());
				System.exit(1);
			}
			else{
				o.setSourceRef(p.getId());
			}
		}


		

		
	}
	
	
	public PlanItemDefinition findPlanItemDefinition(String id){
		for (PlanItemDefinition pi:planItemDefs){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}

		if (!this.isPlanModel) return this.getContext().findPlanItemDefinition(id); // not found, so find in parent stage
		return null; // not found, no parent stage
	}
	
	

	
	public Element printElement(Namespace ns){
		Element el=null;
		if (isPlanModel()){
			el=new Element("casePlanModel");
		}
		else{
			el=new Element("stage");
		}
		el.setNamespace(ns);
		el.setAttribute("name",getName());
		el.setAttribute("id",getId());

		for (PlanItem pi:planItems){
			Element el2=pi.printElement(ns);
			el.addContent(el2);
		}
		for (PlanItemDefinition pid:planItemDefs){
			Element el2=null;
			if (pid instanceof Stage){
				Stage s=(Stage)pid;
				el2=s.printElement(ns);
			}
			if (pid instanceof Milestone){
				Milestone m=(Milestone)pid;
				el2=m.printElement(ns);
			}
			if (pid instanceof Task){
				Task t=(Task)pid;
				el2=t.printElement(ns);
			}
			if (pid instanceof Event){
				Event e=(Event)pid;
				el2=e.printElement(ns);
			}
			if (el2!=null){
				el.addContent(el2);
			}
		}
		for (Sentry se:sentries){
			Element el2=se.printElement(ns);
			el.addContent(el2);
		}
		return el;
	}
	

}

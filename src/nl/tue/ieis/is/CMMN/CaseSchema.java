package nl.tue.ieis.is.CMMN;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Comment;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;


public class CaseSchema {

	private String name;
	private String comment; // to store provenance info etc
	private String id;
	private Stage planModel;
	private Set<Sentry> sentries; // copy of sentries stored in each stage
	private Set<PlanItem> planitems;
	private Set<PlanItemDefinition> planitemdefs;
	private Set<List<CaseSchema>> origins;  // set of <base, variant> pairs; case schema 'this' that is feature is obtained by extracting subtracting variant from base
	
	public CaseSchema(String n){
		name=n;
		comment="";
		sentries=new HashSet<Sentry>();
		planitems=new HashSet<PlanItem>();
		planitemdefs=new HashSet<PlanItemDefinition>();
		origins=new HashSet<List<CaseSchema>>();
	}
	
	public void setName(String n){
		name=n;
	}
	
	public String getName(){
		return name;
	}
	
	public void addComment(String c){
		comment+="\n"+c;
	}
	
	public String getComment(){
		return comment;
	}
	
	public void SetID(String ids){
		id=ids;
	}
	
	public void generateID() {
		id="_"+UUID.randomUUID().toString().replace("-","");

	}
	
	public void setPlanModel(Stage s){
		planModel=s;
	}
	
	public Stage getPlanModel(){
		return planModel;
	}
	
	public Set<Sentry> getSentries(){
		return sentries;
	}
	
	public Set<PlanItem> getPlanItems(){
		return planitems;
	}
	

	public Set<PlanItem> getPlanItems(PlanItemDefinition pid){
		Set <PlanItem> pis=new HashSet<PlanItem>();
		for (PlanItem pi:planitems){
			if (pi.getPlanItemDefinition().equals(pid)){
				pis.add(pi);
			}
		}
		return pis;
	}

	public void addPlanItem(PlanItem pi){
		if (!planitems.contains(pi)) planitems.add(pi);
	}
	

	public void addPlanItemDefinition(PlanItemDefinition pi){
		if (!planitemdefs.contains(pi)) planitemdefs.add(pi);
	}

	public Set<PlanItemDefinition> getPlanItemDefinitions(){
		return planitemdefs;
	}
	
	
	public Task findTask(String name){
		for (PlanItemDefinition pi:planitemdefs){
			if ((pi instanceof Task) && pi.getName()!=null &&pi.getName().equals(name)){
				return (Task)pi;
			}
		}		
		return null;
	}
	
	
	public void addSentry(Sentry s){
		sentries.add(s);
	}
	
	public void addSentries(Set<Sentry> ss){
		sentries.addAll(ss);
	}
	
	public void removeSentries(Set<Sentry> ss){
		sentries.removeAll(ss);
	}
	
	public void removeSentry(Sentry s) {
		sentries.remove(s);
	}
	
	
	public Sentry getSentry(String id){
		for (Sentry s:sentries){
			if (s.getId().equals(id)){
				return s;
			}
		}
		return null;
	}
	
	public boolean defines(PlanItem pi) {
		if (planitems.contains(pi)) {
			boolean grounded=pi.isGroundForm(false)&&pi.isGroundForm(true);
			return grounded;
		}
		return false;
	}
	
	public boolean refines(PlanItem pi) {
		if (planitems.contains(pi)) {
			boolean grounded=pi.isGroundForm(false)&&pi.isGroundForm(true);
			return !grounded;
		}
		return false;	
	}
	
	public void resolveSourceRefs(){
		for (Sentry s:sentries){
			Set<onPart> es=s.getOnParts();
			for (onPart e:es){
				String sr=e.getSourceRef();
				if (sr!=null){
					PlanItem pi=findPlanItemByID(sr);
					if (pi!=null){
						PlanItemDefinition pid=pi.getPlanItemDefinition();
						if (pid.getName()==null) {
							System.out.println("PlanItemDefinition has no name for " +pi.getId() );
							System.exit(1);
						}
						e.setSource(pi,pid.getName());
						if (e.isMilestone()){
							e.setMilestoneSource();
						}
						if (e.isStage()){
							e.setStageSource();
						}
					}
					else{
						System.out.println("Source event with ID " + sr+ " not defined");
					}
				}
			}
		}
	}
	
	public boolean findPlanItemDefinition(PlanItemDefinition pid){
		return this.planitemdefs.contains(pid);
	}

	public PlanItemDefinition findPlanItemDefinitionByID(String id){
		for (PlanItemDefinition pi:planitemdefs){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}
		return null;
	}
	
	public PlanItemDefinition findPlanItemDefinitionByName(String n){
		for (PlanItemDefinition pi:planitemdefs){
			if (pi.getName()!=null &&pi.getName().equals(n)){
				return pi;
			}
		}
		return null;
	}
	
	public boolean findPlanItem(PlanItem pi){
		return this.planitems.contains(pi);
	}
	
	public PlanItem findPlanItemByPlanItemDefName(String name){
		for (PlanItem pi:planitems){
			PlanItemDefinition pid=this.findPlanItemDefinitionByID(pi.getDefinitionRef());
			String pidname=pid.getName();
			if (pidname==null) pidname="null";

			if (pid.getName()!=null &&pid.getName().equals(name)){
				return pi;
			}
		}
		return null;
	}
	
	

	public PlanItem findPlanItemByID(String id){
		for (PlanItem pi:planitems){
			if (pi.getId()!=null &&pi.getId().equals(id)){
				return pi;
			}
		}
		return null;
	}
	
	public boolean isDisjoint(CaseSchema cs) {
		HashSet <PlanItem> pis=(HashSet<PlanItem>) this.getPlanItems();
		for (PlanItem pi:pis) {
			PlanItem pi_cs=cs.findSimilarPlanItem(pi);
			if (pi_cs!=null){
				return false;
			}
		}
		return true;
	}
	
	public boolean isEmpty() {
		return planitems.size()==0;
	}
	
	
	public boolean isIdentity() {
		for (Sentry s:sentries) {
			if (!s.isCopy()) return false;
		}
		return true;
	}
	
	public boolean equals(CaseSchema cs) {
		return this.isRefinedBy(cs)&&cs.isRefinedBy(this);
	}
	
	
	
	// change set contains plan items of cs not in this or modified from this
	public Set<PlanItem> getChangeSet(CaseSchema cs){
		Set<PlanItem> pis_cs=cs.getPlanItems();
		HashSet<PlanItem> pis_change=new HashSet<PlanItem>();
		Set<PlanItemDefinition> stages=new HashSet<PlanItemDefinition>();
		for (PlanItem pi_cs:pis_cs) {

			PlanItem pi_this=this.findSimilarPlanItem(pi_cs);
			if (pi_this==null){ // no plan item like pi_cs in this, so add pi_cs to change set
				pis_change.add(pi_cs);
				if (!pi_cs.isRefined()) stages.add(pi_cs.getContext()); // only add stage context of pi_cs if the feature introduces this context
			}
			else {
				if (pi_this.refinesEntryCriterion(pi_cs) || pi_this.refinesExitCriterion(pi_cs)) {
					pis_change.add(pi_cs);
				}
			}
		}
		for (PlanItem pi_cs:pis_cs) {
			if (stages.contains(pi_cs.getPlanItemDefinition())) {
				pis_change.add(pi_cs);
			}
		}
		return pis_change;
	}
	
	
	public boolean touchesHierarchy(PlanItem pi1, PlanItem pi2) {
		 if (pi1.getPlanItemDefinition().isStage()) {
			 Stage s1=(Stage)pi1.getPlanItemDefinition();
			 if (s1.getChildPlanItems().contains(pi2)) return true;
		 }
		 if (pi2.getPlanItemDefinition().isStage()) {
			 Stage s2=(Stage)pi2.getPlanItemDefinition();
			 if (s2.getChildPlanItems().contains(pi1)) return true;
		 }
		 return false;
	}
	
	public boolean touchesSentry(PlanItem pi1, PlanItem pi2) {
		if (pi1.criterionReferences(pi2)) return true;
		if (pi2.criterionReferences(pi1)) return true;
		if (pi1.criterionOverlaps(pi2)) return true;
		return false;
	}

	
	public boolean touches(PlanItem pi1, PlanItem pi2) {
		return touchesSentry(pi1,pi2)||touchesHierarchy(pi1,pi2);
	}
	
	public boolean touches(Set<PlanItem> pis, PlanItem pi) {
		for (PlanItem pis_item:pis) {
			if (touches(pis_item,pi)) {
				return true;
			}
		}
		return false;
	}
	
	public void findContext(Set<PlanItem> changeset, Set<PlanItem> context, PlanItem current){
		context.add(current);
		for (PlanItem pi:changeset) {
			if (context.contains(pi)) continue;
			if (touches(context,pi)) {				
				findContext(changeset,context,pi);
			}
		}
	}
	
	public Set<Set<PlanItem>> findAllContexts(Set<PlanItem> changeset){
		Set<Set<PlanItem>> contexts=new HashSet<Set<PlanItem>>();
		Set<PlanItem> toVisit=new HashSet<PlanItem>();
		toVisit.addAll(changeset);
		for (PlanItem pi:changeset) {
			if (!toVisit.contains(pi)) continue;

			Set<PlanItem> context=new HashSet<PlanItem>();
			findContext(changeset,context,pi);
			contexts.add(context);
			toVisit.removeAll(context);
		}
		return contexts;
	}
	
	// plan items in cs that are different from plan items in this are returned
	public Set<PlanItem> diff(CaseSchema cs){
		Set<PlanItem> pis=new HashSet<PlanItem>();
		Set<PlanItem> pis_cs=cs.getPlanItems();
		for (PlanItem pi_cs:pis_cs) {
			PlanItem pi=this.findSimilarPlanItem(pi_cs);
			if (pi==null) {
				pis.add(pi_cs);
			}
			else {
				if (pi.getEntryRefs().size()!=pi_cs.getEntryRefs().size()) pis.add(pi_cs);
				if (pi.getExitRefs().size()!=pi_cs.getExitRefs().size()) pis.add(pi_cs);
				// compare sentries
				Set<Criterion> pi_criteria=pi.getEntryRefs();
				Set<Criterion> pi_cs_criteria=pi_cs.getEntryRefs();
				for (Criterion c1:pi_criteria) {
					boolean match=false;
					for (Criterion c2:pi_cs_criteria) {
						if (c1.getSentry().equals(c2.getSentry())) match=true;
					}
					if (!match) pis.add(pi_cs);
				}
				pi_criteria=pi.getExitRefs();
				pi_cs_criteria=pi_cs.getExitRefs();
				for (Criterion c1:pi_criteria) {
					boolean match=false;
					for (Criterion c2:pi_cs_criteria) {
						if (c1.getSentry().equals(c2.getSentry())) match=true;
					}
					if (!match) pis.add(pi_cs);
				}
			}
		}
		return pis;
	}
	
	
	// true if this is contained by cs
	public boolean isRefinedBy(CaseSchema cs) {
		HashSet <PlanItem> pis=(HashSet<PlanItem>) this.getPlanItems();
		HashSet <PlanItem> pis_contained=new HashSet<PlanItem>();

		for (PlanItem pi:pis) {
			PlanItem pi_cs=cs.findSimilarPlanItem(pi);
			if (pi_cs==null){
				return false;
			}
			
			// entry ref check
			Stage pi_context=pi.getContext();
			HashSet<Criterion> entryRefs=(HashSet<Criterion>)pi.getEntryRefs();
			Stage pi_cs_context=pi_cs.getContext();
			HashSet<Criterion> pi_cs_entryRefs=(HashSet<Criterion>)pi_cs.getEntryRefs();
			if (!this.isContainedBy(entryRefs, pi_cs_entryRefs)) return false;
			
			// exit ref checks
			HashSet<Criterion> exitRefs=(HashSet<Criterion>)pi.getExitRefs();
			HashSet<Criterion> pi_cs_exitRefs=(HashSet<Criterion>)pi_cs.getExitRefs();
			if (!this.isContainedBy(exitRefs, pi_cs_exitRefs)) return false;

			pis_contained.add(pi);	
			
		}
		
		// check containment hierarchy relations
		for (PlanItem pi1:pis_contained) {
			for (PlanItem pi2:pis_contained) {
				if (pi1.equals(pi2)) continue;
				if (isChildOf(pi1,pi2)) {
					PlanItem pi1_cs=cs.findSimilarPlanItem(pi1);
					PlanItem pi2_cs=cs.findSimilarPlanItem(pi1);
					if (!cs.isChildOf(pi1_cs,pi2_cs)) return false;
				}
				if (isChildOf(pi2,pi1)) {
					PlanItem pi1_cs=cs.findSimilarPlanItem(pi1);
					PlanItem pi2_cs=cs.findSimilarPlanItem(pi1);
					if (!cs.isChildOf(pi2_cs,pi1_cs)) return false;
				}
			}
		}		
		
		return true;
	}
	
	
	// assumption: isRefinedBy(cs) is true
	// null is true
	// not null is plan item that is not consistently refined
	public PlanItem isConsistentlyRefinedBy(CaseSchema cs) {
		for (PlanItem pi:planitems) {
			if (pi.getPlanItemDefinition().isEvent()) continue;
			PlanItem pi_cs=cs.findSimilarPlanItem(pi);
			if (pi_cs==null){
				System.exit(1);// assumption of refinement violated
			}
			Stage pi_context=pi.getContext();
			HashSet<Criterion> entryRefs=(HashSet<Criterion>)pi.getEntryRefs();
			HashSet<Criterion> exitRefs=(HashSet<Criterion>)pi.getExitRefs();

			Stage pi_cs_context=pi_cs.getContext();
			HashSet<Criterion> pi_cs_entryRefs=(HashSet<Criterion>)pi_cs.getEntryRefs();
			HashSet<Criterion> pi_cs_exitRefs=(HashSet<Criterion>)pi_cs.getExitRefs();

			boolean isPreservedBy=this.isPreservedBy(entryRefs, pi_cs_entryRefs) && this.isPreservedBy(exitRefs, pi_cs_exitRefs);
			boolean isExtendedBy=this.isExtendedBy(entryRefs, pi_cs_entryRefs) && this.isExtendedBy(exitRefs, pi_cs_exitRefs);
			boolean isOverriddenBy=this.isOverriddenBy(entryRefs, pi_cs_entryRefs) && this.isOverriddenBy(exitRefs, pi_cs_exitRefs);

			if (isPreservedBy && isExtendedBy) {
				return pi;
			}
			if (isPreservedBy && isOverriddenBy) {
				return pi;
			}
			if (isExtendedBy && isOverriddenBy) {
				return pi;
			}
			if (!isPreservedBy && !isExtendedBy & !isOverriddenBy) {
				return pi;
			}
		
		}
		return null;
	}
	
	// context is derived from case schema cs; assumption: isRefinedBy(cs) is true
	// null is true
	// not null is plan item that is not consistently refined
	public PlanItem isConsistentRefinement(Set<PlanItem> context) {
		for (PlanItem pi_cs:context) {
			if (pi_cs.getPlanItemDefinition().isEvent()) continue;
			PlanItem pi=this.findSimilarPlanItem(pi_cs);
			if (pi==null){
				continue; // new plan item pi_cs so no counterpart in this.
			}
			
			Stage pi_context=pi.getContext();
			HashSet<Criterion> entryRefs=(HashSet<Criterion>)pi.getEntryRefs();
			HashSet<Criterion> exitRefs=(HashSet<Criterion>)pi.getExitRefs();
			
			Stage pi_cs_context=pi_cs.getContext();
			HashSet<Criterion> pi_cs_entryRefs=(HashSet<Criterion>)pi_cs.getEntryRefs();
			HashSet<Criterion> pi_cs_exitRefs=(HashSet<Criterion>)pi_cs.getExitRefs();

			boolean isPreservedBy=this.isPreservedBy(entryRefs, pi_cs_entryRefs) && this.isPreservedBy(exitRefs, pi_cs_exitRefs);
			boolean isExtendedBy=this.isExtendedBy(entryRefs, pi_cs_entryRefs) && this.isExtendedBy(exitRefs, pi_cs_exitRefs);
			boolean isOverriddenBy=this.isOverriddenBy(entryRefs, pi_cs_entryRefs) && this.isOverriddenBy(exitRefs, pi_cs_exitRefs);

			if (isPreservedBy && isExtendedBy) {
				return pi;
			}
			if (isPreservedBy && isOverriddenBy) {
				return pi;
			}
			if (isExtendedBy && isOverriddenBy) {
				return pi;
			}
			if (!isPreservedBy && !isExtendedBy & !isOverriddenBy) {
				return pi;
			}
		
		}
		return null;
	}
	
	// true if c is completely contained in otherLc
	public boolean isCompletelyContainedBy(Criterion c, Set<Criterion> otherLc) {
		Sentry se=c.getSentry();
		String se_guard=se.getGuard();
		boolean criterionMatch=false;
		for (Criterion c2:otherLc){
			Sentry se2=c2.getSentry();		

			boolean guardContainment;
			if (se_guard.isEmpty()) {
				if (se2.getGuard().isEmpty()) {
					guardContainment=true;
				}
				else {
					guardContainment=false;
				}
			}
			else {
				String[] se_guards= se_guard.split("and");
				String[] se2_guards= se2.getGuard().split("and");
				guardContainment=true;
				for (int i=0;i<se_guards.length;i++) {     	
					boolean found=false;
					for (int j=0;j<se2_guards.length;j++) {
						if (se2_guards[j].trim().equals(se_guards[i].trim())) {
							found=true;
						}
					}
					if (!found) {
						guardContainment=false;
					}
					
				}
				for (int i=0;i<se2_guards.length;i++) {     	
					boolean found=false;
					for (int j=0;j<se_guards.length;j++) {
						if (se2_guards[i].trim().equals(se_guards[j].trim())) {
							found=true;
						}
					}
					if (!found) {
						guardContainment=false;
					}
					
				}
			}
			if (guardContainment) { // now check if on parts are contained
				HashSet<onPart> ops=(HashSet<onPart>)se.getOnParts();
				HashSet<onPart> ops2=(HashSet<onPart>)se2.getOnParts();
				if (ops.isEmpty()) {
					if (ops2.isEmpty()) {
						criterionMatch=true;
					}
					else {
						criterionMatch=false;
					}
				}
				else {

					
					boolean allMatch=true;
					for (onPart op:ops) {
						boolean match=false;

						for (onPart op2:ops2) {

							if (op.isSimilar(op2,op2.getName())){
								match=true;
								break;
							}
							else {
							}
						}
						allMatch=allMatch && match;
						if (!match) { // onPart op could not be matched with an onPart in ops2, however match might be in another entry criterion (future iteration of c2), so do not yet return false
							break;
						}
					}
					for (onPart op2:ops2) {
						boolean match=false;
						for (onPart op:ops) {
							if (op.isSimilar(op2,op2.getName())){
								match=true;
								break;
							}
						}
						allMatch=allMatch && match;
						if (!match) { // onPart op could not be matched with an onPart in ops2, however match might be in another entry criterion (future iteration of c2), so do not yet return false
							break;
						}
					}
					if (allMatch) criterionMatch=true;

				}
			}
		}
		if (!criterionMatch) { // no match for criterion c
			return false;
		}
		return true;
	}
	
	// true if c is contained in otherLc, where containment means that c or an extension of c is in otherLc
	public boolean isContainedBy(Criterion c, Set<Criterion> otherLc) {
		Sentry se=c.getSentry();
		String se_guard=se.getGuard();
		boolean criterionMatch=false;
		for (Criterion c2:otherLc){
			Sentry se2=c2.getSentry();		

			boolean guardContainment;
			if (se_guard.isEmpty()) {
				guardContainment=true;
			}
			else {
				String[] se_guards= se_guard.split("and");
				String[] se2_guards= se2.getGuard().split("and");
				guardContainment=true;
				for (int i=0;i<se_guards.length;i++) {     	
					boolean found=false;
					for (int j=0;j<se2_guards.length;j++) {
						if (se2_guards[j].trim().equals(se_guards[i].trim())) {
							found=true;
						}
					}
					if (!found) {
						guardContainment=false;
					}
				}
			}
			if (guardContainment) { // now check if on parts are contained

				HashSet<onPart> ops=(HashSet<onPart>)se.getOnParts();
				if (ops.isEmpty()) {
					criterionMatch=true;
				}
				else {
					boolean allMatch=true;
					for (onPart op:ops) {
						HashSet<onPart> ops2=(HashSet<onPart>)se2.getOnParts();
						boolean match=false;

						for (onPart op2:ops2) {
							if (op.isSimilar(op2,op2.getName())){
								match=true;
								break;
							}
						}
						allMatch=allMatch && match;
						if (!match) { // onPart op could not be matched with an onPart in ops2, however match might be in another entry criterion (future iteration of c2), so do not yet return false
							break;
						}
					}
					if (allMatch) criterionMatch=true;
				}
			}
		}
		if (!criterionMatch) { // no match for criterion c
			return false;
		}
		return true;

	}
	
	// true if each criterion in thisLc iscontained in otherLc
	public boolean isContainedBy(Set<Criterion> thisLc, Set<Criterion> otherLc) {
		for (Criterion c:thisLc){
			if (!isContainedBy(c,otherLc)) return false;
		}
		return true;
	}
	
	// true if each criterion in thisLc is completely contained in otherLc
	public boolean isCompletelyContainedBy(Set<Criterion> thisLc, Set<Criterion> otherLc) {
		for (Criterion c:thisLc){
			if (!isCompletelyContainedBy(c,otherLc)) return false;
		}
		return true;
	}
	
	
	// true if thisLc iscontained by otherLc, the sentries in otherLc\thisLc do not reference orig and do not contain a sentry from thisLc
	public boolean isPreservedBy(Set<Criterion> thisLc, Set<Criterion> otherLc) {
		if (!isCompletelyContainedBy(thisLc,otherLc)) return false;
		HashSet<Criterion> diffLc=new HashSet<Criterion>();
		for (Criterion c:otherLc) {
			if (!isContainedBy(c,thisLc)) diffLc.add(c);
		}
		for (Criterion c:diffLc) {
			if (c.getSentry().isCopy()||c.getSentry().isMerge()) return false;
		}
		for (Criterion c1:diffLc) {
			for (Criterion c2:thisLc) {
				if (c1.getSentry().contains(c2.getSentry())) return false;
			}
		}
		return true;
	}
	
	
	// true if there is a set of sentries X such that set otherLc equals the set in which each sentry in thisLC is conjuncted with a sentry in X, the sentries in X do not reference orig and do not contain sentries from thisLc
	public boolean isExtendedBy(Set<Criterion> thisLc, Set<Criterion> otherLc) {
		HashSet<Sentry> X=new HashSet<Sentry>();
		for (Criterion c1:otherLc) {
			for (Criterion c2:thisLc) {
				if (c1.getSentry().isSimilar(c2.getSentry())) return false;
				if (c1.getSentry().contains(c2.getSentry())) {
					Sentry s_new=c1.getSentry().diff(c2.getSentry());
					if (s_new.getGuard().contains("orig")) return false;
					X.add(s_new);
				}				
			}
		}
		for (Sentry x:X) {
			for (Criterion c2:thisLc) {
				if (x.contains(c2.getSentry())) return false;
			}
		}
		Set<Sentry> composed=new HashSet<Sentry>();
		for (Criterion c1:thisLc) {
			Sentry s1=c1.getSentry();
			for (Sentry x:X) {
				Sentry s_new= new Sentry();
				s_new.addOnParts(s1.getOnParts());
				s_new.addOnParts(x.getOnParts());
				s_new.setGuard(s1.getGuard()+" and " + x.getGuard());
				boolean found=false;
				for (Sentry s:composed) {
					if (s.equals(s_new)) {
						found=true;
						break;
					}
				}
				if (!found) composed.add(s_new);
			}
		}
		if (composed.size()!=otherLc.size()) return false;
		return true;
	}
	
	// true if the sentries in otherLc do not reference orig and do no contain sentries from thisLc
	public boolean isOverriddenBy(Set<Criterion> thisLc, Set<Criterion> otherLc) {
		for (Criterion c1:otherLc) {
			if (c1.getSentry().isCopy()||c1.getSentry().isMerge()) return false;
			for (Criterion c2:thisLc) {
				if (c1.getSentry().isSimilar(c2.getSentry())) return false;
				if (c1.getSentry().contains(c2.getSentry())) {
					return false;
				}				
			}
		}
		return true;
	}
	
	
	
	// true if pi1 is (in)direct child of pi2
	public boolean isChildOf(PlanItem pi1, PlanItem pi2) {
		if (pi1.equals(pi2)) return true;
		if (pi2.getPlanItemDefinition().isStage()) {
			Stage s2=(Stage)pi2.getPlanItemDefinition();
			Set<PlanItem> children_s2=s2.getChildPlanItems();
			for (PlanItem pi:children_s2) {
				boolean b = isChildOf(pi1,pi);
				if (b) return true;
			}
		}
		return false;
	}
	
	// true if pid1 is descendant of pid2
	public boolean isDescendantOf(PlanItemDefinition pid1, PlanItemDefinition pid2) {
		if (pid1.equals(pid2)) return true;
		if (pid2.isStage()) {
			Set<PlanItemDefinition> children_pid2=((Stage)pid2).getChildPlanItemDefs();
			for (PlanItemDefinition pid:children_pid2) {
				boolean b = isDescendantOf(pid1,pid);
				if (b) return true;
			}
		}
		return false;		
	}
	
	
	public CaseSchema computeIntersection(CaseSchema cs) {
		CaseSchema intersection=new CaseSchema("I_" + this.getName()+ "_" + cs.getName());
		intersection.generateID();
		HashSet <PlanItem> pis=(HashSet<PlanItem>) cs.getPlanItems();
		Stage root =new Stage(this.getPlanModel().getName(),null,intersection);
		root.setPlanModel();
		intersection.setPlanModel(root);
		HashSet<PlanItem> new_planitems=new HashSet<PlanItem>(); // plan items in core feature
		for (PlanItem pi:pis) {
			PlanItem pi_this=this.findSimilarPlanItem(pi);
			if (pi_this!=null){ // plan item like pi in this, so add pi to feature
				new_planitems.add(pi);
			}
		}
	
		HashSet<PlanItem> additionalContext=new HashSet<PlanItem>();
		for (PlanItem pi:new_planitems) {
			Set<Criterion> ec=pi.getEntryRefs();
			ec.addAll(pi.getExitRefs());
			for (Criterion c:ec) {
				Sentry s=cs.getSentry(c.getSentryRef());
				Set<onPart> ops=s.getOnParts();
				for (onPart op:ops) {
					PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
					if (!pis.contains(op_ref)) {
						additionalContext.add(op_ref);
					}
				}	
				String guard=s.getGuard();
				if (guard.isEmpty()) continue;
				String andsep = " and ";
				String[] guardParts=guard.split(andsep);
				for (String gp: guardParts) {
					PlanItem pi_gp=cs.findPlanItemByPlanItemDefName(gp);
					if (pi_gp==null) {
					}
					else {
						if (!pis.contains(pi_gp)) {
							additionalContext.add(pi_gp);
						}
					}

				}
			}
		}
		Set <PlanItem> toprocess=cs.getPlanModel().getChildPlanItems();
		for (PlanItem pi:toprocess) {
			extractFeature(intersection,intersection.getPlanModel(),new_planitems,additionalContext,pi);
		}
		for (PlanItem pi:new_planitems) {
			if (pi.getPlanItemDefinition().isEvent()) continue;
			PlanItem intersection_pi=intersection.findSimilarPlanItem(pi);
			PlanItem counter_pi=this.findSimilarPlanItem(pi);
			if (intersection_pi!=null) {
				addSentriesToIntersection(intersection,intersection_pi,pi,counter_pi);
			}
			else {
				System.out.println("Error in intersection: " + pi.getPlanItemDefinition().getName() + " undefined!");
				System.exit(1);
			}
			
		}
		intersection.resolveSourceRefs();
		intersection.removeDuplicateSentries();
		return intersection;
	}



	
	public static void resetContextCount() {
		context_count=0;
	}
	
	public static int getContextCount() {
		return context_count;
	}
	
	static int context_count=0;
	// assumption: this.isContainedBy(cs) is true
	public Set<CaseSchema> extractFeature(CaseSchema cs) {
		Set<CaseSchema> features=new HashSet<CaseSchema>();
		Set<PlanItem> changeset=this.getChangeSet(cs);
		Set<Set<PlanItem>> contexts=this.findAllContexts(changeset);
		context_count=context_count+contexts.size();
		int consistent=0;
		int i=0;
		for (Set<PlanItem> pis:contexts) {
			PlanItem refined_pi=isConsistentRefinement(pis);
			if (refined_pi!=null) {
				continue;
			}
			else {
				consistent++;
			}
			HashSet<PlanItem> additionalContext=new HashSet<PlanItem>(); // planitems of cs that are not in the changeset but referenced in some sentry of a status attribute that is in the change set
			for (PlanItem pi:pis) {
				PlanItem counter_pi=this.findPlanItemByPlanItemDefName(pi.getPlanItemDefinition().getName());
				Set<Criterion> ec=pi.getEntryRefs();
				for (Criterion c:ec) {
					Sentry s=cs.getSentry(c.getSentryRef());
					if (counter_pi!=null) {
						Set<Criterion> counter_ec=counter_pi.getEntryRefs();
						boolean criterionMatch=false;	
						for (Criterion counter_c:counter_ec) {
							Sentry counter_s=counter_c.getSentry();
							if (s.equals(counter_s)) criterionMatch=true;
						}
						if (criterionMatch) continue; // will become orig sentries in feature, so no additinal context needed
					}

					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
						if (!pis.contains(op_ref)) {
							additionalContext.add(op_ref);
						}
					}	
					String guard=s.getGuard();
					if (guard.isEmpty()) continue;
					String andsep = " and ";
					String[] guardParts=guard.split(andsep);
					for (String gp: guardParts) {
						PlanItem pi_gp=cs.findPlanItemByPlanItemDefName(gp);
						if (pi_gp!=null) {
							if (!pis.contains(pi_gp)) {
								additionalContext.add(pi_gp);
							}
						}

					}
				}
				ec=pi.getExitRefs();
				for (Criterion c:ec) {
					Sentry s=cs.getSentry(c.getSentryRef());
					if (counter_pi!=null) {
						Set<Criterion> counter_ec=counter_pi.getEntryRefs();
						boolean criterionMatch=false;	
						for (Criterion counter_c:counter_ec) {
							Sentry counter_s=counter_c.getSentry();
							if (s.equals(counter_s)) criterionMatch=true;
						}
						if (criterionMatch) continue;
					}
					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
						if (!pis.contains(op_ref)) {
							additionalContext.add(op_ref);
						}
					}	
					String guard=s.getGuard();
					if (guard.isEmpty()) continue;
					String andsep = " and ";
					String[] guardParts=guard.split(andsep);
					for (String gp: guardParts) {
						PlanItem pi_gp=cs.findPlanItemByPlanItemDefName(gp);
						if (pi_gp!=null) {
							if (!pis.contains(pi_gp)) {
								additionalContext.add(pi_gp);
							}
						}

					}
				}
			}
			HashSet<PlanItem> context=new HashSet<PlanItem>();
			context.addAll(pis);
			features.add(extractFeature(context,additionalContext,cs,i));
			i++;
		}
		return features;
	}
	
	
	public String greatestCommonPrefix(String a, String b) {
	    int minLength = Math.min(a.length(), b.length());
	    for (int i = 0; i < minLength; i++) {
	        if (a.charAt(i) != b.charAt(i)) {
	            return a.substring(0, i);
	        }
	    }
	    return a.substring(0, minLength);
	}
	
	public CaseSchema extractFeature(Set<PlanItem> context, Set<PlanItem> additionalContext, CaseSchema cs, int i) {
		String prefixname=greatestCommonPrefix(this.getName(),cs.getName());
		CaseSchema feature=new CaseSchema(this.getName()+"_"+cs.getName()+"_F"+i);
		feature.addComment("Extracted by subtracting " + this.getName() + " from " + cs.getName());
		feature.addOrigin(this, cs);
		feature.generateID();
		Stage root =new Stage(this.getPlanModel().getName(),null,feature);
		root.setPlanModel();
		feature.setPlanModel(root);		
		Set <PlanItem> toprocess=cs.getPlanModel().getChildPlanItems();
		for (PlanItem pi:toprocess) { // process all top-level nodes contained in planmodel
			extractFeature(feature,feature.getPlanModel(),context,additionalContext,pi);
		}
		for (PlanItem pi:context) {
			PlanItem feature_pi=feature.findSimilarPlanItem(pi);
			addSentriesToFeature(feature,feature_pi,context,additionalContext,pi);
		}

		for (PlanItem pi:additionalContext) {
			PlanItem feature_pi=feature.findSimilarPlanItem(pi);
			addSentriesToFeature(feature,feature_pi,context,additionalContext,pi);
		}
		feature.removeAdditionalContext();
		return feature;
	}
	

	
	// add pi to feature is context contains pi; also recursively process children of pi
	public void extractFeature( CaseSchema feature,Stage feature_context,Set<PlanItem> context,Set<PlanItem> additionalContext,PlanItem pi) {	
		if (context.contains(pi)||additionalContext.contains(pi)) { // add pi to feature
		 	Stage pi_context=pi.getContext();
			CaseSchema cs=pi.getContext().getCaseContext();
			String name=cs.findPlanItemDefinitionByID(pi.getDefinitionRef()).getName();
			PlanItemDefinition feature_pid=feature.findPlanItemDefinitionByName(name);
			if (feature_pid==null) {
				if (pi.getPlanItemDefinition().isStage()) {
					feature_pid=new Stage(name,feature_context,feature);

				}
				else if (pi.getPlanItemDefinition().isTask()) {
					feature_pid=new Task(name,feature_context);
				}
				else if (pi.getPlanItemDefinition().isMilestone()) {
					feature_pid=new Milestone(name,feature_context);
				}
				else if (pi.getPlanItemDefinition().isEvent()) {
					feature_pid=new Event(name,feature_context);
				}
				else {
					feature_pid=new PlanItemDefinition(name,feature_context);
				}
				
				feature_context.addChildPlanItemDef(feature_pid);
			}
			else {
				System.out.println("Feature_pid already defined");
				System.exit(1);

			}
			PlanItem feature_pi=new PlanItem(feature_context);
			feature_pi.setName(name);
			feature_pi.setDefinitionRef(feature_pid.getId());
			feature_context.addChildPlanItem(feature_pi);
			if (additionalContext.contains(pi)) feature_pi.setAdditionalContext();		
			if (pi.getPlanItemDefinition().isStage()) { // pi is stage
				Set<PlanItem> children=((Stage)pi.getPlanItemDefinition()).getChildPlanItems();
				for (PlanItem c:children) { // incorporate each child c of pi into feature. Only needed if c is in this
					extractFeature(feature,(Stage)feature_pid,context,additionalContext,c);
				}
			}
		}	
		else {
			if (pi.getPlanItemDefinition().isStage()) { // pi is stage
				Set<PlanItem> children=((Stage)pi.getPlanItemDefinition()).getChildPlanItems();
				for (PlanItem c:children) { // incorporate each child c of pi into feature. Only needed if c is in this
					extractFeature(feature,feature_context,context,additionalContext,c);
				}
			}
		}

	}
	
	
	public void addSentriesToIntersection(CaseSchema intersection,PlanItem intersection_pi,PlanItem pi,PlanItem counter_pi) {
		CaseSchema cs=pi.getContext().getCaseContext();
		CaseSchema counter_cs=counter_pi.getContext().getCaseContext();
		Set<Criterion> ec=pi.getEntryRefs();
		for (Criterion c:ec) {				
			Sentry s=cs.getSentry(c.getSentryRef());
			Set<Criterion> counter_ec=counter_pi.getEntryRefs();
			for (Criterion cc:counter_ec) {			
				Sentry counter_s=this.getSentry(cc.getSentryRef());
				if (s.overlaps(counter_s)) {
					Sentry s_new=new Sentry();
					Criterion c_new=new Criterion(intersection_pi);
					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						if (counter_s.containsSimilarOnPart(op)) {
							PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
							PlanItem intersection_op_ref=intersection.findSimilarPlanItem(op_ref);
							if (intersection_op_ref==null) {
								System.out.println("Undefined reference to plan item " + op_ref.getId()+ " with name " + op_ref.getPlanItemDefinition().getName());
								System.out.println("in method addSentriesToIntersection");
								System.exit(1);
							}
							onPart op_new=new onPart();
							op_new.setSourceRef(intersection_op_ref.getId());
							op_new.setStandardEventText(op.getStandardEventText());
							s_new.addOnPart(op_new);	
						}
					}
					String guard=s.getGuard();

					String new_guard=new String();
					if (!guard.isEmpty()) {
						String andsep = " and ";
						String[] guardParts=guard.split(andsep);
						for (String gp: guardParts) { 
							if (counter_s.containsSimilarGuard(gp)) {
								if (new_guard.isEmpty()) {
									new_guard=gp;
								}
								else {
									new_guard=new_guard.concat(" and " + gp );
								}
							}
						}
								
					}
					s_new.setGuard(new_guard);
					intersection_pi.getContext().addSentry(s_new);
					c_new.setSentryRef(s_new.getId());
					intersection_pi.addEntryRef(c_new);			
				}
			}	
		}
		intersection.resolveSourceRefs();// to enable comparison between sentries
		ec=intersection_pi.getEntryRefs();
		Set<Criterion> criterionToRemove=new HashSet<Criterion>();
		Set<Sentry> sentryToRemove=new HashSet<Sentry>();
		if (!ec.isEmpty()) {
			for (Criterion c1:ec) {
				Sentry s1=intersection.getSentry(c1.getSentryRef());
				for (Criterion c2:ec) {
					if (c1==c2) continue;
					Sentry s2=intersection.getSentry(c2.getSentryRef());
					if (s1.contains(s2)&&!s2.contains(s1)) {
						sentryToRemove.add(s2);
						criterionToRemove.add(c2);
					}
				}
			}
			for (Criterion c:criterionToRemove) {
				intersection_pi.removeEntryRef(c);
			}
			for (Sentry s:sentryToRemove) {
				intersection_pi.getContext().removeSentry(s);
			}
		}
		else {
			// no entry criterion
			if (!pi.getEntryRefs().isEmpty()||!counter_pi.getEntryRefs().isEmpty()) {
				// no entry criterion, so create artificial one that has a false guard
				Criterion c_new=new Criterion(intersection_pi);
				Sentry s_new=new Sentry();
				c_new.setSentryRef(s_new.getId());
				intersection_pi.getContext().addSentry(s_new);
				intersection_pi.addEntryRef(c_new);		
			}			
		}
	}

	
	// create sentries for plan item feature_pi
	public void addSentriesToFeature(CaseSchema feature,PlanItem feature_pi,Set<PlanItem> context, Set<PlanItem> additionalContext, PlanItem pi) {
		CaseSchema cs=pi.getContext().getCaseContext();
		if (this.findSimilarPlanItem(pi)!=null) {
			feature_pi.setRefined();
		}
		if (context.contains(pi)) {
			Set<Criterion> ec=pi.getEntryRefs();
			PlanItem counter_pi=this.findPlanItemByPlanItemDefName(pi.getPlanItemDefinition().getName());
			if (counter_pi==null) { // new plan item, so sentries are not modified (no orig), only references of onParts need to be updated
				for (Criterion c:ec) {				
					Sentry s=cs.getSentry(c.getSentryRef());
					Sentry s_new=new Sentry();
					Criterion c_new=new Criterion(feature_pi);
					s_new.setGuard(s.getGuard());
					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						onPart op_new=new onPart();
						PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
						PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
						if (feature_op_ref==null) {
							System.out.println("Undefined reference to plan item " + op_ref.getId());
							System.exit(1);
						}
						op_new.setSourceRef(feature_op_ref.getId());
						op_new.setStandardEventText(op.getStandardEventText());
						s_new.addOnPart(op_new);	
					}
					feature_pi.getContext().addSentry(s_new);
					c_new.setSentryRef(s_new.getId());
					feature_pi.addEntryRef(c_new);
				}
				ec=pi.getExitRefs();
				for (Criterion c:ec) {				
					Sentry s=cs.getSentry(c.getSentryRef());
					Sentry s_new=new Sentry();
					Criterion c_new=new Criterion(feature_pi);
					s_new.setGuard(s.getGuard());
					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						onPart op_new=new onPart();
						PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
						PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
						if (feature_op_ref==null) {
							System.out.println("Undefined reference to plan item " + op_ref.getId());
							System.exit(1);
						}
						op_new.setSourceRef(feature_op_ref.getId());
						op_new.setStandardEventText(op.getStandardEventText());
						s_new.addOnPart(op_new);	
					}
					feature_pi.getContext().addSentry(s_new);
					c_new.setSentryRef(s_new.getId());
					feature_pi.addExitRef(c_new);
				}
			}
			else { // refined plan item pi, so use orig statement if possible
				for (Criterion c:ec) {				
					Sentry s=cs.getSentry(c.getSentryRef());
					// find matching criterion at other side (this)
					boolean criterionMatch=false;
					Set<Criterion> counter_ec=counter_pi.getEntryRefs();
					for (Criterion cc:counter_ec) {
						Sentry s_new=new Sentry();
						Criterion c_new=new Criterion(feature_pi);					
						Sentry counter_s=this.getSentry(cc.getSentryRef());
						if (s.isSimilar(counter_s)){
							criterionMatch=true;
							s_new.setGuard("orig");
							feature_pi.getContext().addSentry(s_new);
							c_new.setSentryRef(s_new.getId());
							feature_pi.addEntryRef(c_new);
						}
					}								
					if (!criterionMatch) {
						for (Criterion cc:counter_ec) {
							Sentry s_new=new Sentry();
							Criterion c_new=new Criterion(feature_pi);

							Sentry counter_s=this.getSentry(cc.getSentryRef());
							if (s.contains(counter_s)) {
								criterionMatch=true;
								// construct a new sentry by substituting orig for counter_s
								Set<onPart> ops=s.getOnParts();
								for (onPart op:ops) {
									if (!counter_s.containsSimilarOnPart(op)) {
										PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
										PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
										if (feature_op_ref==null) {
											System.out.println("Undefined reference to plan item " + op_ref.getId() +  " having name " + op_ref.getPlanItemDefinition().getName());
											System.out.println("in method addSentriesToFeature");
											System.exit(1);
										}
										onPart op_new=new onPart();
										op_new.setSourceRef(feature_op_ref.getId());
										op_new.setStandardEventText(op.getStandardEventText());
										s_new.addOnPart(op_new);	
									}
								}
								String guard=s.getGuard();
								String new_guard=new String();
								if (!guard.isEmpty()) {
									String andsep = " and ";
									String[] guardParts=guard.split(andsep);
									for (String gp: guardParts) { 
										if (!counter_s.containsSimilarGuard(gp)) {
											if (new_guard.isEmpty()) {
												new_guard=gp;
											}
											else {
												new_guard=new_guard.concat(" and " + gp );
											}
										}
									}
								}
								if (new_guard.isEmpty()) {
									new_guard="orig";
								}
								else{
									if (!new_guard.equals("orig")) {
										new_guard=new_guard.concat(" and orig");
									}
								}
								//							}
								s_new.setGuard(new_guard);
								feature_pi.getContext().addSentry(s_new);
								c_new.setSentryRef(s_new.getId());
								feature_pi.addEntryRef(c_new);
							}
						}
					}
					if (!criterionMatch) { // copy sentry into new new sentry
						Sentry s_new=new Sentry();
						Criterion c_new=new Criterion(feature_pi);
						feature_pi.getContext().addSentry(s_new);
						Set<onPart> ops=s.getOnParts();
						for (onPart op:ops) {
							PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
							String op_name=op_ref.getPlanItemDefinition().getName();
							PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
							if (feature_op_ref==null) {
								PlanItem pi_new=new PlanItem(feature.getPlanModel());
								PlanItemDefinition pid_new=null;
								PlanItemDefinition op_ref_def=op_ref.getPlanItemDefinition(); // old plan item def type determines new plan item def type
								if (op_ref_def.isStage()) {
									pid_new=new Stage(op_name, feature.getPlanModel(), feature);
								}
								else if(op_ref_def.isTask())  {
									pid_new=new Task(op_name, feature.getPlanModel());
								}
								else if(op_ref_def.isMilestone())  {
									pid_new=new Milestone(op_name, feature.getPlanModel());
								}
								else if(op_ref_def.isEvent())  {
									pid_new=new Event(op_name, feature.getPlanModel());
								}
								
								pi_new.setName(op_name);
								pi_new.setDefinitionRef(pid_new.getId());						
								feature.getPlanModel().addChildPlanItem(pi_new);
								if (pid_new!=null) {
									feature.getPlanModel().addChildPlanItemDef(pid_new);
								}
								else {
									System.out.println("No plan item definition could be created for " + op_name );
									System.exit(1);
								}						
								feature_op_ref=pi_new;
							}
							onPart op_new=new onPart();
							op_new.setSourceRef(feature_op_ref.getId());
							op_new.setStandardEventText(op.getStandardEventText());
							s_new.addOnPart(op_new);	
							
						}
						s_new.setGuard(s.getGuard());

						c_new.setSentryRef(s_new.getId());
						feature_pi.addEntryRef(c_new);
					}
					
				}
			}

			ec=pi.getExitRefs();
			for (Criterion c:ec) {				
				Sentry s=cs.getSentry(c.getSentryRef());
				// find matching criterion at other side (this)
				boolean criterionMatch=false;
				Set<Criterion> counter_ec=counter_pi.getExitRefs();
				for (Criterion cc:counter_ec) {
					Sentry s_new=new Sentry();
					Criterion c_new=new Criterion(feature_pi);
					
					Sentry counter_s=this.getSentry(cc.getSentryRef());
					if (s.isSimilar(counter_s)){
						criterionMatch=true;
						s_new.setGuard("orig");
						feature_pi.getContext().addSentry(s_new);
						c_new.setSentryRef(s_new.getId());
						feature_pi.addExitRef(c_new);
					}
					else if (s.contains(counter_s)) {
						criterionMatch=true;

						// construct a new sentry by substituting orig for counter_s
						Set<onPart> ops=s.getOnParts();
						boolean orig_required=false;
						for (onPart op:ops) {
							if (!counter_s.containsSimilarOnPart(op)) {
								PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
								PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
								if (feature_op_ref==null) {
									System.out.println("Undefined reference to plan item " + op_ref.getId() +  "having name " + op_ref.getPlanItemDefinition().getName());
									System.out.println("in method addSentriesToFeature");
									System.exit(1);
								}
								onPart op_new=new onPart();
								op_new.setSourceRef(feature_op_ref.getId());
								op_new.setStandardEventText(op.getStandardEventText());
								s_new.addOnPart(op_new);	
							}
							else {
								orig_required=true;
							}
						}
						String guard=s.getGuard();

						String new_guard=new String();
						if (!guard.isEmpty()) {
							String andsep = " and ";
							String[] guardParts=guard.split(andsep);
							for (String gp: guardParts) { 
								if (!counter_s.containsSimilarGuard(gp)) {
									if (new_guard.isEmpty()) {
										new_guard=gp;
									}
									else {
										new_guard=new_guard.concat(" and " + gp );
									}
								}
								else {
									orig_required=true;
								}
							}
									
						}
						if (orig_required) {
							if (new_guard.isEmpty()) {
								new_guard="orig";
							}
							else{
								new_guard=new_guard.concat(" and orig");
							}
						}
						s_new.setGuard(new_guard);
						feature_pi.getContext().addSentry(s_new);
						c_new.setSentryRef(s_new.getId());
						feature_pi.addExitRef(c_new);
					}
				}
				if (!criterionMatch) { // copy sentry into new new sentry
					Sentry s_new=new Sentry();
					Criterion c_new=new Criterion(feature_pi);
					feature_pi.getContext().addSentry(s_new);
					Set<onPart> ops=s.getOnParts();
					for (onPart op:ops) {
						PlanItem op_ref=cs.findPlanItemByID(op.getSourceRef());
						String op_name=op_ref.getPlanItemDefinition().getName();
						PlanItem feature_op_ref=feature.findSimilarPlanItem(op_ref);
						if (feature_op_ref==null) {
							PlanItem pi_new=new PlanItem(feature.getPlanModel());
							PlanItemDefinition pid_new=null;
							PlanItemDefinition op_ref_def=op_ref.getPlanItemDefinition(); // old plan item def type determines new plan item def type
							if (op_ref_def.isStage()) {
								pid_new=new Stage(op_name, feature.getPlanModel(), feature);
							}
							else if(op_ref_def.isTask())  {
								pid_new=new Task(op_name, feature.getPlanModel());
							}
							else if(op_ref_def.isMilestone())  {
								pid_new=new Milestone(op_name, feature.getPlanModel());
							}
							else if(op_ref_def.isEvent())  {
								pid_new=new Event(op_name, feature.getPlanModel());
							}
							
							pi_new.setName(op_name);
							pi_new.setDefinitionRef(pid_new.getId());						
							feature.getPlanModel().addChildPlanItem(pi_new);
							if (pid_new!=null) {
								feature.getPlanModel().addChildPlanItemDef(pid_new);
							}
							else {
								System.out.println("No plan item definition could be created for " + op_name );
								System.exit(1);
							}						
							feature_op_ref=pi_new;
						}
						onPart op_new=new onPart();
						op_new.setSourceRef(feature_op_ref.getId());
						op_new.setStandardEventText(op.getStandardEventText());
						s_new.addOnPart(op_new);				
					}
					s_new.setGuard(s.getGuard());

					c_new.setSentryRef(s_new.getId());
					feature_pi.addExitRef(c_new);
				}
				
			}
		}
		if (additionalContext.contains(pi)) { // each status attribute pi in additional context only has orig sentries
			if (!pi.getEntryRefs().isEmpty()) {
				Sentry s_new=new Sentry();
				Criterion c_new=new Criterion(feature_pi);
				feature_pi.getContext().addSentry(s_new);
				s_new.setGuard("orig");
				c_new.setSentryRef(s_new.getId());
				feature_pi.addEntryRef(c_new);
			}
			if (!pi.getExitRefs().isEmpty()) {
				Sentry s_new=new Sentry();
				Criterion c_new=new Criterion(feature_pi);
				feature_pi.getContext().addSentry(s_new);
				s_new.setGuard("orig");
				c_new.setSentryRef(s_new.getId());
				feature_pi.addExitRef(c_new);
			}
		}
		feature.resolveSourceRefs();
	}
	
	// because of orig sentries, some plan items are not referenced anymore and can be removed
	public void removeAdditionalContext() {
		Set<PlanItem> toremove=new HashSet<PlanItem>();
		for (PlanItem pi:planitems) {
			if (!pi.isAdditionalContext()) continue;
			boolean found=false;
			for (Sentry s:sentries) {
				if (s.references(pi)) {
					found=true;
				}
			}
			if (!found && pi.isCopyForm() && !pi.getPlanItemDefinition().isStage()) { // pi is not referenced and only has copy sentries and has no children, to it can be removed
				toremove.add(pi);
			}
		}
		for (PlanItem pi:toremove) {
			planitemdefs.remove(pi.getPlanItemDefinition());
			Set<Criterion> ec=pi.getEntryRefs();
			ec.addAll(pi.getExitRefs());
			for (Criterion c:ec){
				pi.getContext().removeSentry(this.getSentry(c.getSentryRef()));
			}
		}
		planitems.removeAll(toremove);
		planModel.removeAll(toremove);
	}
	
	// entry or exit criteria may contain duplicate sentries
	public void removeDuplicateSentries() {
		for (PlanItem pi:planitems) {
			Set<Criterion> toremove=new HashSet<Criterion>();
			Set<Criterion> ec=pi.getEntryRefs();
			for (Criterion c1:ec){
				if (toremove.contains(c1)) continue;
				for (Criterion c2:ec) {
					if (c1==c2) continue;
					if (c1.equals(c2)) {
						toremove.add(c2);
					}
				}
			}
			for (Criterion c:toremove) {
				pi.getContext().removeSentry(this.getSentry(c.getSentryRef()));
				pi.removeEntryRef(c);
			}		
			ec=pi.getExitRefs();
			for (Criterion c1:ec){
				for (Criterion c2:ec) {
					if (c1==c2) continue;
					if (c1.equals(c2)) {
						toremove.add(c2);
					}
				}
			}
			for (Criterion c:toremove) {
				pi.getContext().removeSentry(this.getSentry(c.getSentryRef()));
				pi.removeExitRef(c);
			}			
		}	
	}
	
	
	
	public boolean owns(PlanItem ps, PlanItem pm) {
		Set<Criterion> ec=pm.getEntryRefs();
		for (Criterion c:ec) {
			Sentry s=this.getSentry(c.getSentryRef());
			if (s.hasTrigger(ps)) return true;
		}
		return false;
	}
	
	public boolean owns1(String stageOrTaskName, String milestoneName) {
		PlanItem ps=this.findPlanItemByPlanItemDefName(stageOrTaskName);
		PlanItem pm=this.findPlanItemByPlanItemDefName(milestoneName);
		Set<Criterion> ec=pm.getEntryRefs();
		for (Criterion c:ec) {
			Sentry s=this.getSentry(c.getSentryRef());
			if (s.hasTrigger(ps)) return true;
		}
		return false;
	}
	
	public boolean isMilestoneSentryConsistent(PlanItem pm1, PlanItem pm2) {
		Set<Criterion> ec=pm2.getEntryRefs();
		for (Criterion c:ec) {
			Sentry s=this.getSentry(c.getSentryRef());
			if (s.hasTrigger(pm1)) return true;
		}
		return false;	
	}
	
	
	
	public boolean isSentryConsistent(PlanItem pm1, PlanItem pm2) {
		for (Sentry s:sentries) {
			if (s.hasTrigger(pm1)&&s.hasTrigger(pm2)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isHierarchyConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if (pi.getPlanItemDefinition().isStage()&&this.owns(pi, pm2)) {
				Stage s=(Stage)pi.getPlanItemDefinition();
				if (s.hasPlanItem(pm1))	return true;
			}
		}
		return false;
	}
	
	public boolean isStageMilestoneConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if ((pi.getPlanItemDefinition().isStage()||pi.getPlanItemDefinition().isTask())&&this.owns(pi, pm2)) {
				Set<Criterion> ec=pi.getEntryRefs();
				for (Criterion c:ec) {
					Sentry se=this.getSentry(c.getSentryRef());
					if (se.hasTrigger(pm1)) return true;
				}		
			}
		}
		return false;	
	}
	
	public boolean isStageOrTaskOutputConsistent(PlanItem pm1, PlanItem pm2) {
		for (PlanItem pi:planitems) {
			if ((pi.getPlanItemDefinition().isStage()||pi.getPlanItemDefinition().isTask())&&this.owns(pi,pm1)&&this.owns(pi, pm2)) {
				return true;
			}
		}
		return false;
	}
	
	
	public void addOrigin(CaseSchema base, CaseSchema variant) {// this is obtained by subtracting variant from base
		List originPair=new ArrayList();
		originPair.add(base);
		originPair.add(variant);
		origins.add(originPair);
	}
	
	
	public Set<List<CaseSchema>> getOrigins(){
		return origins;
	}
	
	public void mergeOrigins(CaseSchema cs) {
		Set<List<CaseSchema>> cs_origins=cs.getOrigins();
		for (List<CaseSchema> l:cs_origins) {
			this.addOrigin(l.get(0), l.get(1));
			this.addComment(cs.getComment());
		}
	}
  	
	public boolean verify(){
		return planModel.verify(this);
	}
	

	public int countMilestones(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isMilestone()){
				count++;
			}
		}
		return count;
	}
	
	public int countStages(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isStage()){
				count++;
			}
		}
		return count;
	}
	
	public int countTasks(){
		int count=0;
		for (PlanItemDefinition pid:planitemdefs){
			if (pid.isTask()){
				count++;
			}
		}
		return count;
	}
	
	
	
	
	public void printStatistics(){
		System.out.println("Case schema: " + this.getName());
		System.out.println("There are " + this.planitemdefs.size() + "  plan item definitions." );
		System.out.println("                   " + this.countStages() + "  stages." );
		System.out.println("                   " + this.countTasks() + "  tasks." );
		System.out.println("                   " + this.countMilestones() + "  milestones." );
		System.out.println("There are " + this.sentries.size() + "  sentries." );
	}

	
	public PlanItem findSimilarPlanItem(PlanItem piOther) {
		String otherName=piOther.getPlanItemDefinition().getName();

		for (PlanItem pi:planitems){ 
			PlanItemDefinition pid=this.findPlanItemDefinitionByID(pi.getDefinitionRef());
			if (pid.getName().equals(otherName)) return pi;
		}
		return null;
	}
	
	public boolean hasSimilarPlanItems(Set<PlanItem> pis) {
		for (PlanItem pi:pis) {
			if (findSimilarPlanItem(pi)==null) return false;
		}
		return true;
	}
	
	public PlanItemDefinition findSimilarPlanItemDefinition(String name, String className){
		for (PlanItemDefinition pid:planitemdefs){ 
			if (pid.getName().equals(name)){
				if (pid.getClass().getName().equals(className))	return pid;
			}
		}
		return null;
	}

	
	public void compose(CaseSchema add){
		PlanItemDefinition add_pid=add.getPlanModel();
		Set<PlanItem> pid_children=((Stage)add_pid).getChildPlanItems();
		Set<PlanItemDefinition> this_parents=new HashSet<PlanItemDefinition>();
		for (PlanItem pc:pid_children) {
			PlanItem this_pc=this.findSimilarPlanItem(pc);
			if (this_pc!=null)	this_parents.add(this_pc.getContext());
		}
		if (this_parents.size()>1) {
			System.out.println("There are more than two stages to be composed!");
			System.exit(1);
		}
		PlanItemDefinition this_pid;
		if (this_parents.size()==0) {
			this_pid=this.getPlanModel();
		}
		else{
			 this_pid=this_parents.iterator().next();
		}
		this_pid.compose(add_pid);
	}
	
	
	public PlanItemDefinition getCounterPart(PlanItemDefinition planModel){		
		PlanItemDefinition counter_pid=this.findSimilarPlanItemDefinition(planModel.getName(),planModel.getClass().getName());
		return counter_pid;
	}
	
	public String printComplexity() {
		int complexity=this.planitems.size()+this.sentries.size();
		return (this.getName() + "," + complexity +"\n");
	}
	
	public Document printCMMN(){
		Namespace ns = Namespace.getNamespace("url");
        Namespace ns2 = Namespace.getNamespace("dc","http://www.omg.org/spec/CMMN/20151109/DC");
        Namespace ns3 = Namespace.getNamespace("di","http://www.omg.org/spec/CMMN/20151109/DI");
        Namespace ns4 = Namespace.getNamespace("cmmndi","http://www.omg.org/spec/CMMN/20151109/CMMNDI");
        Namespace ns5 = Namespace.getNamespace("cmmn","http://www.omg.org/spec/CMMN/20151109/MODEL");
        Namespace ns6 = Namespace.getNamespace("xsi","http://www.w3.org/2001/XMLSchema-instance");
        
        Document doc = new Document();
        Comment c=new Comment(this.getComment());
        doc.addContent(c);

        Element def=new Element("definitions",ns5);
        def.setAttribute("id",id+"_definitions");
        def.addNamespaceDeclaration(ns2);
        def.addNamespaceDeclaration(ns3);
        def.addNamespaceDeclaration(ns4);
        def.addNamespaceDeclaration(ns5);
        def.addNamespaceDeclaration(ns6);
        doc.setRootElement(def);
        
        Element caseE=new Element("case",ns5);
        caseE.setAttribute("name",name);
        caseE.setAttribute("id",id);
        def.addContent(caseE);
        
        caseE.addContent(planModel.printElement(ns5));
 
        return doc;
	}
	
}

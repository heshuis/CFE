package nl.tue.ieis.is.CMMN;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import org.jdom.Element;
import org.jdom.Namespace;



public class PlanItem extends CMMNElement{
	private String name;
	private String definitionRef;
	private Set<Criterion> entryRef;
	private Set<Criterion> exitRef;
	private Stage context;
	private boolean isAdditionalContext;
	private boolean isRefined; // true refined, false defined

	public PlanItem(Stage s){
		context=s;
		entryRef=new HashSet<Criterion>();
		exitRef=new HashSet<Criterion>();
	}
	
	public void setName(String n){
		name=n; //.replace(" ","_");
	}

	public String getName(){
		return name;
	}
	
	public void setDefinitionRef(String n){
		definitionRef=n;
	}
	
	public String getDefinitionRef(){
		return definitionRef;
	}
	
	public Stage getContext(){
		return context;
	}
	
	public void setContext(Stage s){
		context=s;
	}
	
	public PlanItemDefinition getPlanItemDefinition(){
		return context.findPlanItemDefinition(definitionRef);
	}
	
	public boolean isSimilar(PlanItem piOther) {
		return this.getPlanItemDefinition().getName().equals(piOther.getPlanItemDefinition().getName());
	}
	
	// assumption: piOther is similar to this
	public boolean hasSimilarSentries(PlanItem piOther) {
		Set<Criterion> other_cc=piOther.getEntryRefs();
		if (other_cc.size()!=entryRef.size()) return false;
		for (Criterion c:entryRef) {
			Sentry sc=c.getSentry();
			for (Criterion oc:other_cc) {
				Sentry so=oc.getSentry();
				if (!sc.isSimilar(so)) return false;
			}
		}
		other_cc=piOther.getExitRefs();
		if (other_cc.size()!=exitRef.size()) return false;
		for (Criterion c:exitRef) {
			Sentry sc=c.getSentry();
			for (Criterion oc:other_cc) {
				Sentry so=oc.getSentry();
				if (!sc.isSimilar(so)) return false;
			}
		}
		return true;
	}
	
	public void setAdditionalContext() {
		isAdditionalContext=true;
	}
	
	public boolean isAdditionalContext() {
		return isAdditionalContext;
	}
	
	public void setRefined() {
		isRefined=true;
	}
	
	public boolean isRefined() {
		return isRefined;
	}
	
	public void addEntryRef(Criterion n){
		entryRef.add(n);
	}
	
	public void removeEntryRef(Criterion n) {
		entryRef.remove(n);
	}

	public void setEntryRefs(Set<Criterion> ers){
		entryRef=ers;
	}
	
	public Set<Criterion> getEntryRefs(){
		return entryRef;
	}
	
	public void addExitRef(Criterion n){
		exitRef.add(n);
	}
	
	public void removeExitRef(Criterion n) {
		exitRef.remove(n);
	}


	public Set<Criterion> getExitRefs(){
		return exitRef;
	}
	
	public void setExitRefs(Set<Criterion> ers){
		exitRef=ers;
	}
	
	public boolean criterionReferences(PlanItem pi) {
		for (Criterion c:entryRef) {
			String sref=c.getSentryRef();
			Sentry se=context.getSentry(sref);
			if (se.references(pi)) return true;
		}
		for (Criterion c:exitRef) {
			String sref=c.getSentryRef();
			Sentry se=context.getSentry(sref);
			if (se.references(pi)) return true;
		}
		return false;
	}
	
	
	// return true if sentries of this and pi have overlap, e.g. contain same subcondition
	public boolean criterionOverlaps(PlanItem pi) {
		for (Criterion c:entryRef) {
			String se_ref=c.getSentryRef();
			Sentry se=context.getSentry(se_ref);
			for (Criterion c_pi:pi.getEntryRefs()) {
				String se_pi_ref=c_pi.getSentryRef();
				Sentry se_pi=pi.getContext().getSentry(se_pi_ref);
				if (se.overlaps(se_pi)) return true;

			}	        
		}
		return false;
	}
	
	
	public boolean isStrictCompositionForm(boolean entry){// false is exit
		return isCompositionForm(entry)&&!isExtensionForm(entry);
	}
	
	public boolean isStrictExtensionForm(boolean entry){// false is exit
		return !isCompositionForm(entry)&&isExtensionForm(entry);
	}

	
	public boolean isCompositionForm(boolean entry){// false is exit
		if (entry){
			boolean found=false;
			for (Criterion cc:entryRef){
				found=true;
				String sidc=cc.getSentryRef();
				Sentry sec=context.getSentry(sidc);
				if (!sec.isCopy()&&!sec.isMerge()) return false;
			}
			return found;
		}
		else{
			boolean found=false;
			for (Criterion cc:exitRef){
				found=true;
				String sidc=cc.getSentryRef();
				Sentry sec=context.getSentry(sidc);
				if (!sec.isCopy()&&!sec.isMerge()) return false;
			}
			return found;		
		}
	}
	
	public boolean isExtensionForm(boolean entry){// false is exit
		if (entry){
			boolean found=false;
			for (Criterion cc:entryRef){
				String sidc=cc.getSentryRef();
				Sentry sec=context.getSentry(sidc);
				if (sec.isCopy()) found=true;
				if (!sec.isCopy()&&!sec.isGround()) return false;
			}
			return found;
		}
		else{
			boolean found=false;
			for (Criterion cc:exitRef){
				String sidc=cc.getSentryRef();
				Sentry sec=context.getSentry(sidc);
				if (sec.isCopy()) found=true;
				if (!sec.isCopy()&&!sec.isGround()) return false;
			}
			return found;
		}
	}
	
	
	public boolean isGroundForm(boolean entry){// false is exit
		if (entry){
			if (!entryRef.isEmpty()) {
				for (Criterion cc:entryRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);
					if (sec.isMerge()) {
						return false;
					}
					if (sec.isCopy()) {
						return false;
					}

				}
				return true;
			}
		}
		else{
			if (!exitRef.isEmpty()) {
				for (Criterion cc:exitRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);
					if (sec.isMerge()) return false;
					if (sec.isCopy()) return false;
				}
				return true;
			}
		}
		return true; // empty entry or exit criterion; assumed to be [true]
	}
	
	public boolean isCopyForm(boolean entry) {
		if (entry){
			if (!entryRef.isEmpty()) {
				for (Criterion cc:entryRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);
					if (sec.isMerge()) {
						return false;
					}
					if (sec.isGround()) {
						return false;
					}
				}
				return true;
			}
		}
		else{
			if (!exitRef.isEmpty()) {
				for (Criterion cc:exitRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);
					if (sec.isMerge()) return false;
					if (sec.isGround()) return false;
				}
				return true;
			}
		}
		return true; // empty entry or exit criterion; assumed to be [true]
	}
	
	public boolean isCopyForm() {
		return isCopyForm(true)&&isCopyForm(false);
	}
	
	public boolean analyzeMatchingSentrySetForms(PlanItem pi){
		boolean entryExtensionMatch=isExtensionForm(true) && pi.isExtensionForm(true);
		boolean entryCompositionMatch=isCompositionForm(true) && pi.isCompositionForm(true);
		boolean status=true;
		if (!entryExtensionMatch&&!entryCompositionMatch){
			if (entryRef.isEmpty()&&pi.entryRef.isEmpty()) {
				// do nothing
			}
			else {
				System.out.print("Warning: the entry sentries of plan item "+ this.getPlanItemDefinition().getName());
				System.out.print("\nin case schemas "+ this.getContext().getCaseContext().getName() + " and " + pi.getContext().getCaseContext().getName());
				System.out.println(" do not having matching forms.");
				System.out.println(this.getContext().getCaseContext().getName()+" extensionform: "+isExtensionForm(true));
				System.out.println(pi.getContext().getCaseContext().getName()+" extensionform: "+pi.isExtensionForm(true));
				
				System.out.println(this.getContext().getCaseContext().getName()+" comp form: "+isCompositionForm(true));
				System.out.println(pi.getContext().getCaseContext().getName()+" comp form: "+pi.isCompositionForm(true));

				status=false;
			}
		}
		boolean exitExtensionMatch=isExtensionForm(false) && pi.isExtensionForm(false);
		boolean exitCompositionMatch=isCompositionForm(false) && pi.isCompositionForm(false);
		if (!exitExtensionMatch&&!exitCompositionMatch){
			if (exitRef.isEmpty()&&pi.exitRef.isEmpty()) {
				// do nothing
			}
			else {
				System.out.print("Warning: the exit sentries of plan items "+ this.getPlanItemDefinition().getName());
				System.out.print("\nin case schemas "+ this.getContext().getCaseContext().getName() + " and " + pi.getContext().getCaseContext().getName());
				System.out.println(" do not having matching forms.");
				status=false;
			}
		}
		return status;
	}
	
	
	// true if pi_cs extends this with new sentries or sentries of this with additional conjuncts
	public boolean refinesEntryCriterion(PlanItem pi_cs) {
		Stage pi_context=this.getContext();
		HashSet<Criterion> entryRefs=(HashSet<Criterion>)this.getEntryRefs();
		Stage pi_cs_context=pi_cs.getContext();
		HashSet<Criterion> pi_cs_entryRefs=(HashSet<Criterion>)pi_cs.getEntryRefs();
		return refinesCriterion(entryRefs, pi_cs_context,pi_cs_entryRefs);
	}
	
	// true if pi_cs extends this with new sentries or sentries of this with additional conjuncts
	public boolean refinesExitCriterion(PlanItem pi_cs) {
		Stage pi_context=this.getContext();
		HashSet<Criterion> exitRefs=(HashSet<Criterion>)this.getExitRefs();
		Stage pi_cs_context=pi_cs.getContext();
		HashSet<Criterion> pi_cs_exitRefs=(HashSet<Criterion>)pi_cs.getExitRefs();
		return refinesCriterion(exitRefs, pi_cs_context,pi_cs_exitRefs);
	}
	
	
	public boolean equalsCriterion(HashSet<Criterion> thisRefs, Stage pi_cs_context,HashSet<Criterion> pi_cs_Refs) {
		if (thisRefs.size()!=pi_cs_Refs.size()) return false;
		Stage pi_context=this.getContext();
		for (Criterion c_this:thisRefs){
			String sid=c_this.getSentryRef();
			Sentry se_this=pi_context.getSentry(sid);
			String se_this_guard=se_this.getGuard();
	
			boolean criterionMatch=false;
			for (Criterion c_cs:pi_cs_Refs){
				String sid_cs=c_cs.getSentryRef();
				Sentry se_cs=pi_cs_context.getSentry(sid_cs);		
				if (se_this.isSimilar(se_cs)) {
					criterionMatch=true;
				}
			}
			if (!criterionMatch) return false;
		}
		return true;		
	}
	
	// true if a criterion in thisRefs has changed and this change is a refinement of a criterion in pi_cs_Refs
	public boolean refinesCriterion(HashSet<Criterion> thisRefs, Stage pi_cs_context,HashSet<Criterion> pi_cs_Refs) {
		Stage pi_context=this.getContext();
		if (equalsCriterion(thisRefs,pi_cs_context,pi_cs_Refs)) return false; // equal criteria, so no change
		for (Criterion c_this:thisRefs){
			String sid=c_this.getSentryRef();
			Sentry se_this=pi_context.getSentry(sid);
			String se_this_guard=se_this.getGuard();
			boolean criterionMatch=false;
	
			for (Criterion c_cs:pi_cs_Refs){
				String sid_cs=c_cs.getSentryRef();
				Sentry se_cs=pi_cs_context.getSentry(sid_cs);	
				
				
				boolean guardContainment;
				if (se_this_guard.isEmpty()) {
					guardContainment=true;
				}
				else {
					String[] se_guards= se_this_guard.split("and");
					String[] se_cs_guards= se_cs.getGuard().split("and");
			        guardContainment=true;
			        for (int i=0;i<se_guards.length;i++) {     	
			        	boolean found=false;
				        for (int j=0;j<se_cs_guards.length;j++) {
				        	if (se_cs_guards[j].trim().equals(se_guards[i].trim())) {
				        		found=true;
				        	}
				        }
			        	if (!found) {
			        		guardContainment=false;
			        	}
			        }
				}
				if (guardContainment) { // now check if on parts are contained
					HashSet<onPart> ops=(HashSet<onPart>)se_this.getOnParts();
					if (ops.isEmpty()) {
						criterionMatch=true;					
					}
					else {
						for (onPart op:ops) {
							String sourceref=op.getSourceRef();
							PlanItem ppll=this.getContext().getCaseContext().findPlanItemByID(sourceref);
							PlanItemDefinition pidpid=this.getContext().getCaseContext().findPlanItemDefinitionByID(ppll.getDefinitionRef());
							String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();

							HashSet<onPart> ops2=(HashSet<onPart>)se_cs.getOnParts();
							boolean match=false;

							for (onPart op2:ops2) {
								if (!op.getStandardEventText().equals(op2.getStandardEventText())) continue;
								String sourceref2=op2.getSourceRef();
								PlanItem ppll2=pi_cs_context.getCaseContext().findPlanItemByID(sourceref2);
								PlanItemDefinition pidpid2=pi_cs_context.getCaseContext().findPlanItemDefinitionByID(ppll2.getDefinitionRef());
								String sourcerefName2= pidpid2.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();

								if (sourcerefName.equals(sourcerefName2)) {
									criterionMatch=true;
								}
							}
						}
					}
				}
			}
			if (!criterionMatch) { // no match for criterion
				return false;
			}
		}
		return true;
	}
	

	
	// precondition: pi and this are same, i.e., have the same planitemdefs, i.e., planitemdefs with the same name
	public void compose(PlanItem pi){
		Stage pi_context=pi.getContext();
		Set<Criterion> pi_entryRef=pi.getEntryRefs();
		Set<Criterion> new_entryRef=new HashSet<Criterion>();
		Set<Criterion> old_entryRef=new HashSet<Criterion>();
		Set<Sentry> new_sentries=new HashSet<Sentry>();
		Set<Sentry> old_sentries=new HashSet<Sentry>();
		for (Criterion c:entryRef){
			String sid=c.getSentryRef();
			Sentry se=context.getSentry(sid);
			old_sentries.add(se);
		}
		String orig = "\\borig\\b";
		for (Criterion c:pi_entryRef){
			String sid=c.getSentryRef();
			Sentry se=pi_context.getSentry(sid);
			Pattern pattern=Pattern.compile(orig);
			Matcher matcher=pattern.matcher(se.getGuard());
			boolean origOccurs=matcher.find();
			if (origOccurs){
				old_entryRef.add(c);
				for (Criterion cc:entryRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);
					Sentry se_new=new Sentry();
					for (onPart o:se.getOnParts()) {	
						String sourceref=o.getSourceRef();
						PlanItem ppll=pi.getContext().getCaseContext().findPlanItemByID(sourceref);
						PlanItemDefinition pidpid=ppll.getPlanItemDefinition();
						String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();
						onPart oc=o.clone();
						PlanItem p=context.getCaseContext().findPlanItemByPlanItemDefName(sourcerefName);
						if (p==null){ // p defined in context, so do nothing
							System.out.println("Error: no PlanItem with name " + sourcerefName + " for "  + sourceref + " in context " + context.getName());
							System.out.println("when composing " + this.getContext().getCaseContext().getName());
							System.exit(0);
						}
						else{
							oc.setSourceRef(p.getId());
						}
						
						oc.setSourceRef(p.getId());
						se_new.addOnPart(oc);
					}
					for (onPart o:sec.getOnParts()) {
						se_new.addOnPart(o.clone());
					}
					String ifPart=se.getGuard();
					if (!sec.getGuard().isEmpty()){
						ifPart=ifPart.replaceAll(orig, sec.getGuard());
					}
					else{//sec only contains events, no guards
						ifPart=ifPart.replaceAll(orig, "");
					}
					se_new.setGuard(ifPart);
					boolean found=false;
					for (Sentry ns:new_sentries) {
						if (ns.equals(se_new)) found=true;
					}
					if (!found) {
						Criterion c_new=new Criterion(pi);
						c_new.setSentryRef(se_new.getId());
						new_entryRef.add(c_new);
						new_sentries.add(se_new);
					}
				}
			}
			else{
				for (onPart o:se.getOnParts()) {
					String sourceref=o.getSourceRef();
					PlanItem ppll=pi.getContext().getCaseContext().findPlanItemByID(sourceref);
					PlanItemDefinition pidpid=ppll.getPlanItemDefinition();
					String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();
					PlanItem p=context.getCaseContext().findPlanItemByPlanItemDefName(sourcerefName);
					if (p==null){ // p not defined in context, so in new part; do nothing
					}
					else{
						o.setSourceRef(p.getId());
					}
				}

				new_sentries.add(se);
				
				Criterion c_new=new Criterion(this);
				c_new.setSentryRef(sid);
				new_entryRef.add(c_new);
			}			
		}
		setEntryRefs(new_entryRef);
		context.removeSentries(old_sentries);
		context.addSentries(new_sentries);
		
		Set<Criterion> pi_exitRef=pi.getExitRefs();
		Set<Criterion> new_exitRef=new HashSet<Criterion>();
		Set<Criterion> old_exitRef=new HashSet<Criterion>();
		new_sentries=new HashSet<Sentry>();
		old_sentries=new HashSet<Sentry>();
		for (Criterion c:exitRef){
			String sid=c.getSentryRef();
			Sentry se=context.getSentry(sid);
			old_sentries.add(se);
		}

		for (Criterion c:pi_exitRef){
			String sid=c.getSentryRef();
			Sentry se=pi_context.getSentry(sid);
			Pattern pattern=Pattern.compile(orig);
			Matcher matcher=pattern.matcher(se.getGuard());
			boolean origOccurs=matcher.find();
			if (origOccurs){
				old_exitRef.add(c);
				for (Criterion cc:exitRef){
					String sidc=cc.getSentryRef();
					Sentry sec=context.getSentry(sidc);

					Sentry se_new=new Sentry();
					for (onPart o:se.getOnParts()) {
						String sourceref=o.getSourceRef();
						PlanItem ppll=pi.getContext().getCaseContext().findPlanItemByID(sourceref);
						PlanItemDefinition pidpid=ppll.getPlanItemDefinition();
						String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();
						PlanItem p=context.getCaseContext().findPlanItemByPlanItemDefName(sourcerefName);
						if (p==null){
							System.out.println("Error: no PlanItem with name " + sourcerefName + " for "  + sourceref + " in context " + context.getName());
							System.out.println("when composing " + this.getContext().getCaseContext().getName());
							System.exit(1);
						}
						else{
							o.setSourceRef(p.getId());
						}
					}
					for (onPart o:sec.getOnParts()) {
						se_new.addOnPart(o.clone());
					}
					String ifPart=se.getGuard();
					if (!sec.getGuard().isEmpty()){
						ifPart=ifPart.replaceAll(orig, sec.getGuard());
					}
					else{//sec only contains events, no guards
						ifPart=ifPart.replaceAll(orig, "");
					}
					se_new.setGuard(ifPart);
					boolean found=false;
					for (Sentry ns:new_sentries) {
						if (ns.equals(se_new)) found=true;
					}
					if (!found) {
						Criterion c_new=new Criterion(pi);
						c_new.setSentryRef(se_new.getId());
						new_exitRef.add(c_new);
					
						new_sentries.add(se_new);
					}
				}
			}
			else{
				for (onPart o:se.getOnParts()) {
					
					String sourceref=o.getSourceRef();
					PlanItem ppll=pi.getContext().getCaseContext().findPlanItemByID(sourceref);
					PlanItemDefinition pidpid=ppll.getPlanItemDefinition();
					String sourcerefName= pidpid.getName()   ; // ((Stage)comp_pid).getCaseContext().findPlanItemByID(sourceref).getName();
					PlanItem p=context.getCaseContext().findPlanItemByPlanItemDefName(sourcerefName);
					if (p==null){
						// do nothing, since p is defined in new part
					}
					else{
						o.setSourceRef(p.getId());
					}
				}
				new_sentries.add(se);
				Criterion c_new=new Criterion(this);
				c_new.setSentryRef(sid);
				new_exitRef.add(c_new);
			}			
		}
		setExitRefs(new_exitRef);
		context.removeSentries(old_sentries);
		context.addSentries(new_sentries);

	}
	

	
	public Element printElement(Namespace ns){
		Element planItem=new Element("planItem",ns);
		planItem.setAttribute("id", getId());
		planItem.setAttribute("definitionRef",getDefinitionRef());
		for (Criterion c:entryRef){
			Element el2=new Element("entryCriterion");
			el2.setNamespace(ns);
			el2.setAttribute("id",c.getId());
			el2.setAttribute("sentryRef",c.getSentryRef());
			planItem.addContent(el2);
		}
		for (Criterion c:exitRef){
			Element el2=new Element("exitCriterion");
			el2.setNamespace(ns);
			el2.setAttribute("id",c.getId());
			el2.setAttribute("sentryRef",c.getSentryRef());
			planItem.addContent(el2);
		}		
		return planItem;
	}
	
	
	public Set<PlanItem> getSourceReferences(){
		Set<PlanItem> pis=new HashSet();
		for (Criterion c:entryRef){
			Sentry se=this.getContext().getSentry(c.getSentryRef());
			pis.addAll(se.computeSourceReferences());
		}
		return pis;
	}
	
}

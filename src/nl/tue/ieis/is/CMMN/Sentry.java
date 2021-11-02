package nl.tue.ieis.is.CMMN;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.Comment;


public class Sentry extends CMMNElement {
	private Set<onPart> ops;
	private String ifPart;
	String name;
	
	public Sentry(){
		ops=new HashSet<onPart>();
		ifPart="";
	}
	
	public void setName1(String s){
		name=s;
	}
	
	public String getName(){
		return name;
	}
	
		
	public void addOnPart(onPart op){
		boolean match=false;
		for (onPart o:ops) {
			if (o.isSimilar(op, o.getName())) match=true;
		}
		if (!match)	ops.add(op);
	}
	
	public void addOnParts(Set<onPart> opsa){
		ops.addAll(opsa);
	}


	public void setGuard(String g){
		ifPart=g;
	}
	
	public String getGuard(){
		return ifPart;
	}
	
	public Set<onPart> getOnParts(){
		return ops;
	}
	
	// return true if sentry (from other case schema) is similar
	public boolean isSimilar(Sentry se) {
		if (!this.getGuard().equals(se.getGuard())) return false;
		Set<onPart> ops_se=se.getOnParts();
		if (ops.size()!=ops_se.size()) return false;
		for (onPart op:ops) {
			boolean match=false;
			for (onPart op2:ops_se) {
				if (op.isSimilar(op2, op2.getName())) match=true;
			}
			if (!match) return false;
		}
		return true;
	}
	
	public boolean equals(Sentry se) {
		String se_guard=se.getGuard();
		if (se_guard.isEmpty()) {
			if (!this.getGuard().isEmpty()) return false;
		}
		else { // non empty guard, so split
			String[] se_guards= se_guard.split("and");
			String[] this_guards= this.getGuard().split("and");
	        for (int i=0;i<se_guards.length;i++) {     	
	        	boolean found=false;
		        for (int j=0;j<this_guards.length;j++) {
		        	if (this_guards[j].trim().equals(se_guards[i].trim())) {
		        		found=true;
		        	}
		        }
	        	if (!found) {
	        		return false;
	        	}
	        }
		}
		HashSet<onPart> ops=(HashSet<onPart>)se.getOnParts();
		if (ops.isEmpty()) {
			if (!this.ops.isEmpty()) return false;
		}
		else {
			for (onPart op:ops) {
				boolean match=false;
				HashSet<onPart> ops2=(HashSet<onPart>)this.getOnParts();
				for (onPart op2:ops2) {		
					if (op.isSimilar(op2, op2.getName())) match=true;

				}
				if (!match) { // onPart op could not be matched with an onPart in ops2
					return false;
				}
			}
		}		
		return true;
	}
	
	// true if this strictly contains se
	public boolean contains(Sentry se) {
		boolean guardContainment=false;
		String se_guard=se.getGuard();
		if (se_guard.isEmpty()) {
			if (!se.getOnParts().isEmpty()) {
				guardContainment=true; // check next if on parts are contained
			}
			else {
				guardContainment=false; // orig requires non-empty original sentry
			}
		}
		else { // non empty guard, so split
			String[] se_guards= se_guard.split("and");
			String[] this_guards= this.getGuard().split("and");
	        guardContainment=true;
	        for (int i=0;i<se_guards.length;i++) {     	
	        	boolean found=false;
		        for (int j=0;j<this_guards.length;j++) {
		        	if (this_guards[j].trim().equals(se_guards[i].trim())) {
		        		found=true;
		        	}
		        }
	        	if (!found) {
	        		guardContainment=false;
	        	}
	        }
		}
		if (guardContainment) {
			HashSet<onPart> ops=(HashSet<onPart>)se.getOnParts();
			if (!ops.isEmpty()) {
				for (onPart op:ops) {
					boolean match=false;
					HashSet<onPart> ops2=(HashSet<onPart>)this.getOnParts();
					for (onPart op2:ops2) {		
						if (op.isSimilar(op2, op2.getName())) match=true;

					}
					if (!match) { // onPart op could not be matched with an onPart in ops2
						return false;
					}
				}
			}
		}
		else {
			return false;
		}
		return true;
	}
	
	
	public boolean overlaps(Sentry se) {
		String se_guard=se.getGuard();
		if (!se_guard.isEmpty()) {
			String[] se_guards= se_guard.split("and");
			String[] this_guards= this.getGuard().split("and");
	        for (int i=0;i<se_guards.length;i++) {     	
		        for (int j=0;j<this_guards.length;j++) {
		        	if (this_guards[j].trim().equals(se_guards[i].trim())) {
		        		return true;
		        	}
		        }
	        }

		}
		HashSet<onPart> ops=(HashSet<onPart>)se.getOnParts();
		if (ops.isEmpty()) {
			if (se_guard.isEmpty()) {
				return false;
			}
			else {
				return false;
			}
		}
		else {
			for (onPart op:ops) {
				HashSet<onPart> ops2=(HashSet<onPart>)this.getOnParts();
				for (onPart op2:ops2) {		
					if (op.isSimilar(op2, op2.getName())) {
						return true;
					}

				}
			}
		}
		return false;
	}
	
	public boolean containsSimilarOnPart(onPart op) {	
		for (onPart this_op:ops) {
			if (op.isSimilar(this_op, this_op.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean containsSimilarGuard(String guard) {	
		if (this.getGuard().isEmpty()) {
			return false;
		}
		else {
			String[] guards= guard.split("and");
			String[] this_guards= this.getGuard().split("and");
	        for (int i=0;i<guards.length;i++) {     	
	        	boolean found=false;
		        for (int j=0;j<this_guards.length;j++) {
		        	if (this_guards[j].trim().equals(guards[i].trim())) {
		        		found=true;
		        	}
		        }
	        	if (!found) {
	        		return false;
	        	}
	        }
		}
		return true;
	}
	
	// assumption: this contains se
	public Sentry diff(Sentry se) {
		Sentry se_new=new Sentry();
		for (onPart op:ops) {
			if (!se.containsSimilarOnPart(op)) {
				se_new.addOnPart(op);
			}
		}
		String guard=getGuard();
		String new_guard=new String();
		if (!guard.isEmpty()) {
			String andsep = " and ";
			String[] guardParts=guard.split(andsep);
			for (String gp: guardParts) { 
				if (!se.containsSimilarGuard(gp)) {
					if (new_guard.isEmpty()) {
						new_guard=gp;
					}
					else {
						new_guard=new_guard.concat(" and " + gp );
					}
				}
			}
			se_new.setGuard(new_guard);
		}
		return se_new;
	}
	
	public boolean references(PlanItem pi) {
		String name=pi.getPlanItemDefinition().getName();
		for (onPart this_op:ops) {
			if (this_op.getSource().equals(pi)) {
				return true;
			}
		}	
		if (this.getGuard().isEmpty()) {
			return false;
		}
		else {
			String[] this_guard= this.getGuard().split("and");
			boolean found=false;
			for (int j=0;j<this_guard.length;j++) {
				if (this_guard[j].trim().equals(name)) {
					found=true;
				}
			}
			if (!found) {
				return false;
			}
		}
		return true;
	}
	
	public boolean isGround(){
		String orig="\\borig\\b";
		Pattern pattern=Pattern.compile(orig);
		Matcher matcher=pattern.matcher(ifPart);
		boolean origOccurs=matcher.find();
		return !origOccurs;
	}
	
	public boolean isCopy(){
		return ifPart.equals("orig")&&ops.isEmpty();
	}
	
	public boolean isMerge(){
		String and="(\\band\\b)|(&&)";
		Pattern pattern=Pattern.compile(and);
		String [] ifParts=ifPart.split(and,0);
		if (ifParts.length==1) {
			if (ops.size()==0) {// no event
				return false;
			}
		}
		boolean found=false;
		for (String s:ifParts) {
			if (s.trim().equals("orig")) found=true;
		}
		return found;
	}
	
	
	public Set<PlanItem> computeSourceReferences(){
		Set<PlanItem> references=new HashSet();
		for (onPart o:ops){
			PlanItem pi=o.getSource();
			references.add(pi);
		}
		return references;
	}
	
	public boolean hasTrigger(PlanItem p) {
		for (onPart o:ops){
			PlanItem pi=o.getSource();
			if (pi.equals(p)) {
				return true;
			}
		}
		return false;
	}
	
		
	Element printElement(Namespace ns){
		Element el=new Element("sentry");
		el.setNamespace(ns);
		el.setAttribute("id",getId());
		if (name!=null){
			el.setAttribute("name",name);
		}
		for (onPart e:ops){			
			Element el2=new Element("planItemOnPart");
			el2.setNamespace(ns);
			el2.setAttribute("id",e.getId());
			el2.setAttribute("sourceRef",e.getSourceRef());
			Comment c=new Comment("refers to plan item name: "  + e.getName());
			el2.addContent(c);
			Element el3=new Element("standardEvent");
			el3.setNamespace(ns);
			if (e.getStandardEventText()!=null){
				el3.addContent(e.getStandardEventText());
			}				
			el.addContent(el2);
			el2.addContent(el3);
		}
		if (getGuard()!=null){
			Element el4=new Element("ifPart");
			el4.setNamespace(ns);
			Element el5=new Element("condition");
			el5.addContent(getGuard());
			el5.setNamespace(ns);
			el4.addContent(el5);
			el.addContent(el4);
		}
		return el;
	}
}

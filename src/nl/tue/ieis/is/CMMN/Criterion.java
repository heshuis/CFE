package nl.tue.ieis.is.CMMN;

public class Criterion extends CMMNElement {
	private String sentryRef;
	private PlanItem owner;
	
	public Criterion(PlanItem pi){
		owner=pi;
	}
	
	public PlanItem getOwningPlanItem(){
		return owner;
	}
	
	public void setSentryRef(String s){
		sentryRef=s;
	}
	
	public String getSentryRef(){
		return sentryRef;
	}
	
	public boolean equals (Criterion c) {
		return owner.getContext().getSentry(sentryRef).equals(c.getOwningPlanItem().getContext().getSentry(c.getSentryRef()));
	}
	
	public Sentry getSentry() {
		return owner.getContext().getSentry(sentryRef);
	}
	
}

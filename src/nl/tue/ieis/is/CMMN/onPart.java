package nl.tue.ieis.is.CMMN;

public class onPart extends CMMNElement {

	private String sourceref;
	private PlanItem source;
	private boolean direction;
	private boolean isStage;
	private boolean isMilestone;
	private String standardEventText;
	private String name; // of planitemdef of source
	
	public onPart(){
		direction=true;
		isStage=false;
		isMilestone=false;
	}
	
	public onPart clone() {
		onPart copy=new onPart();
		copy.setSourceRef(this.getSourceRef());
		copy.setSource(this.getSource(),this.getName());
		copy.setDirection(this.returnDirection());
		copy.isStage=this.isStage;
		copy.isMilestone=this.isMilestone;
		copy.standardEventText=this.standardEventText;
		return copy;
	}
	
	public boolean isSimilar(onPart op, String name) {
		if (!op.getStandardEventText().equals(standardEventText)) {
			return false;
		}
		if (op.getName()==null&&name==null) return true;
		if (!op.getName().equals(this.getName())) return false;
		return true;
	}
	
	public void setSourceRef(String s){
		sourceref=s;
	}
	
	public String getSourceRef(){
		return sourceref;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName1(String n) {
		name=n;
	}
	
	public void setSource(PlanItem t, String n){ // task , stge or milestone, referred to by sourceref
		source=t;
	    name=n;
	}
	
	public PlanItem getSource(){
		return source;
	}
	
	public void setDirection(boolean plus){
		direction=plus;
	}
	
	public void setMilestoneSource(){
		isMilestone=true;
	}
	
	public void setStageSource(){
		isStage=true;
	}
	
	public boolean isStage(){
		return isStage;
	}
	
	public boolean isMilestone(){
		return isMilestone;
	}
	
	public boolean isStageMilestone(){
		return isStage || isMilestone;
	}
	
	public boolean returnDirection(){
		return direction;
	}
	
	public void setStandardEventText(String s){
		this.standardEventText=s;
	}
	
	public String getStandardEventText(){
		return this.standardEventText;
	}
	
}

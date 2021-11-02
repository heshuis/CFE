package nl.tue.ieis.is.CMMN;

import org.jdom.*;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;


import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

public class CMMNreader {
	  	public static Document getDocument(File file) {
  	      
		    SAXBuilder builder = new SAXBuilder();
		     
		    try {
		      Document doc = builder.build(file);
		      return doc;
		    }
		    // indicates a well-formedness error
		    catch (JDOMException e) { 
		      System.out.println(file + " is not well-formed.");
		      System.out.println(e.getMessage());
		    }  
		    catch (IOException e) { 
		      System.out.println(e);
		    }  
		    return new Document();
		 }
	  	
	  	
	  	 public static CaseSchema doc2cmmn(Document doc, String docname){
	 	  	
	  	  	Element root = doc.getRootElement();
	  	  	Namespace ns=root.getNamespace();
	  	  	List rootchildren=root.getChildren();
	  	  	CaseSchema cs=new CaseSchema(docname);
	  	  	
	  	  	for (int i=0;i<rootchildren.size();i++){
	  	  		Element el = (Element)rootchildren.get(i);
	  	  		String name = el.getName();
	  	  		if (name.equals("case")){
	    			cs.SetID(el.getAttributeValue("id"));
	  	  			List children=el.getChildren();
	  	  			for (int j=0;j<children.size();j++){
	  	  				Element planmodel=(Element)children.get(j);
	  	  				if (planmodel.getName().equals("casePlanModel")){
		  	  				Stage s=new Stage(planmodel.getAttributeValue("name"),null,cs);
		  	  				s.setPlanModel();
		  	  				cs.setPlanModel(s);
		  	  				cs.addPlanItemDefinition(s);
		  	  		  	  	List<Element> children2 = planmodel.getChildren();
		  	  		  	  	for (int i2=0;i2<children2.size();i2++){
		  	  		  	  		Element el_child = (Element)children2.get(i2);
		  	  		  	  		parse(el_child,s,cs,ns);
		  	  		  	  	}
		  	  		  	  		
	  	  				}
	 
	  	  			}
	  	  		}	
	  	  	}
	  	  	cs.resolveSourceRefs();
	  	  	return cs;
	  	 }
	  	 
	  	public static void parse(Element el,Stage local, CaseSchema cs,Namespace ns){
	  		String name = el.getName();
	  		if (name.equals("planItem")){
	  			PlanItem pi=new PlanItem(local);
	  			String def=el.getAttributeValue("definitionRef");
	  			pi.setDefinitionRef(def);
	  			String id=el.getAttributeValue("id");	  	  			
	  			pi.setId(id);
	  			local.addChildPlanItem(pi);
	  			List children2 = el.getChildren();
	  			for (int i2=0;i2<children2.size();i2++){	
	  				Element el2 = (Element)children2.get(i2); // entry or exit criterion
	  				parse(el2,pi,cs,ns);
	  			}	  	  			  			
	  		}
	  		if (name.equals("stage")){ //planitemdefinition
	  			String stagename=el.getAttributeValue("name");	  	  			
	  			Stage s=new Stage(stagename,local,cs);
	  			String id=el.getAttributeValue("id");	  	  			
	  			s.setId(id);
	  			local.addChildPlanItemDef(s);
	  			List children2 = el.getChildren();
	  			for (int i2=0;i2<children2.size();i2++){	  				
	  				Element el2 = (Element)children2.get(i2);
	  				parse(el2,s,cs,ns);
	  			}	  	  			
	  		}
	  		if (name.equals("sentry")){
	  			Sentry s=new Sentry();
	  			String sid=el.getAttributeValue("id");
	  			s.setId(sid);
	  			local.addSentry(s);
	  			List children2 = el.getChildren();
	  			for (int i2=0;i2<children2.size();i2++){
	  				Element el2 = (Element)children2.get(i2);
	  				String name2 = el2.getName();
	  				if (name2.equals("planItemOnPart")){
	  					onPart op = new onPart();
	  					String standardEventText=el2.getChildText("standardEvent",ns);
	  					String sr=el2.getAttributeValue("sourceRef");
	  					op.setSourceRef(sr);
	  					op.setStandardEventText(standardEventText);
	  					s.addOnPart(op);
	  				}
	  				if (name2.equals("ifPart")){
	  					String guard=el2.getChildText("condition",ns);
	  					s.setGuard(guard);
	  				}
	  			}

	  		}
	  		if (name.equals("humanTask")||name.equals("processTask")||name.equals("task")){
	  			String tname=el.getAttributeValue("name");
	  			String tid=el.getAttributeValue("id");
	  			Task t=new Task(tname,local);
	  			t.setId(tid);
	  			local.addChildPlanItemDef(t);
	  		}
	  		if (name.equals("milestone")){
	  			String mname=el.getAttributeValue("name");
	  			String mid=el.getAttributeValue("id");
	  			Milestone m=new Milestone(mname,local);
	  			m.setId(mid);
	  			local.addChildPlanItemDef(m);
	  		}
	  		if (name.equals("eventListener")){
	  			String ename=el.getAttributeValue("name");
	  			String eid=el.getAttributeValue("id");
	  			Event e=new Event(ename,local);
	  			e.setId(eid);
	  			local.addChildPlanItemDef(e);
	  		}
	  	}


	  	public static void parse(Element el,PlanItem local, CaseSchema cs,Namespace ns){
	  		String name = el.getName();
	  		if (name.equals("entryCriterion")){
	  			Criterion c=new Criterion(local);
	  			String id2=el.getAttributeValue("id");
	  			String def2=el.getAttributeValue("sentryRef");
	  			c.setId(id2);
	  			c.setSentryRef(def2);	  			
	  			local.addEntryRef(c);
	  		}
	  		if (name.equals("exitCriterion")){
	  			Criterion c=new Criterion(local);
	  			String def2=el.getAttributeValue("sentryRef");
	  			c.setSentryRef(def2);
	  			local.addExitRef(c);
	  		}
	  		if (name.equals("sentry")){
	  			System.out.println("Unexpected sentry in plan item");
	  			System.exit(1);
	  		}
	  		if (name.equals("humanTask")||name.equals("processTask")||name.equals("task")){
	  			System.out.println("Unexpected task");
	  			System.exit(1);

	  		}
	  		if (name.equals("milestone")){
	  			System.out.println("Unexpected milestone");
	  			System.exit(1);
	  		}
	  		if (name.equals("eventListener")){
	  			System.out.println("Unexpected event");
	  			System.exit(1);
	  		}

	  	}	  	 
	  	 
}

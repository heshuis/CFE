package nl.tue.ieis.is.CMMN;

import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collections;


public class FeatureGraph {
	
	private CaseSchema base;
	private List<CaseSchema> nodes;
	private PlanItem [][] before;
	private PlanItem [][] conflict;
	private int [][] permutable; // 0 not relevant, 1 not permutable, 2 permutable
	private boolean[] composable;
	
	public FeatureGraph(List<CaseSchema> cs_list) {
		nodes=new ArrayList<CaseSchema>();
		nodes.addAll(cs_list);
		initRelations();
	}
	
	public void addCaseSchema(CaseSchema cs) {
		if (!nodes.contains(cs)) nodes.add(cs);
	}
	
	public void setBaseSchema(CaseSchema cs) {
		base=cs;
	}
	
	public void initRelations() {
		before=new PlanItem[nodes.size()][nodes.size()];
		conflict=new PlanItem[nodes.size()][nodes.size()];
		permutable=new int[nodes.size()][nodes.size()];
		composable=new boolean[nodes.size()];
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				before[i][j]=null;		
				before[j][i]=null;		
				conflict[i][j]=null;
				conflict[j][i]=null;
				permutable[i][j]=0;
				permutable[j][i]=0;
				composable[i]=false;
			}
		}
	}
	
	
	public void setComposable(CaseSchema cs) {
		int i=nodes.indexOf(cs);
		composable[i]=true;
	}
	
	public boolean isComposable(CaseSchema cs) {
		int i=nodes.indexOf(cs);
		return composable[i];
	}
	
	public void setBefore(CaseSchema cs1, CaseSchema cs2,PlanItem pi) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		before[i][j]=pi;
	}
	
	public void setConflict(CaseSchema cs1, CaseSchema cs2,PlanItem pi) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		conflict[i][j]=pi;
		conflict[j][i]=pi;
	}
	
	public void setPermutable(CaseSchema cs1, CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		permutable[i][j]=2;
		permutable[j][i]=2;
	}

	public void setNotPermutable(CaseSchema cs1, CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		permutable[i][j]=1;
		permutable[j][i]=1;
	}

	
	public boolean isBefore(CaseSchema cs1, CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		return before[i][j]!=null;
	}
	
	public PlanItem getBefore(CaseSchema cs1, CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		return before[i][j];	
	}
	
	
	public boolean isConflict(CaseSchema cs1, CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		return conflict[i][j]!=null;
	}
	
	public boolean isNotPermutable(CaseSchema cs1,  CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		return permutable[i][j]==1;
	}
	
	public boolean isPermutable(CaseSchema cs1,  CaseSchema cs2) {
		int i=nodes.indexOf(cs1);
		int j=nodes.indexOf(cs2);
		return permutable[i][j]==2;
	}
	
	boolean marked[];
	
	private void initMarked() {
		marked=new boolean[nodes.size()];
		for (int i=0;i<nodes.size();i++) {
			marked[i]=false;
		}
	}
	
	public boolean isCyclic(CaseSchema cs) {
		initMarked();
		DFS(cs);
		return marked[nodes.indexOf(cs)];
	}
	
	private void DFS(CaseSchema cs) {
		marked[nodes.indexOf(cs)]=true;
		for (CaseSchema cs1:nodes) {
			if (isBefore(cs,cs1)) DFS(cs1);
		}
	}
	
	
	public boolean hasBeforeCycle() {
		return hasBeforeCycle(nodes);
	}

	
	public boolean hasBeforeCycle(List<CaseSchema> nodeList) {
	    List<CaseSchema> visitList = new ArrayList<>();
	    boolean found=false;
        boolean[] visited = new boolean[nodeList.size()]; 
        boolean[] recstack = new boolean[nodeList.size()]; 

	    for (int i = 0; i < nodeList.size(); i++) {
	    	//	      if (hasBeforeCycle(nodes.get(i), visited)) {
	        for (int j = 0; j < nodeList.size(); j++) {
	        	visited[j]=false;
	        	recstack[j]=false;
	        }
    		visitList.clear();

	    	if (hasBeforeCycle(i,visited,recstack, nodeList)) {
	    		System.out.println("\n\nFeature ["+i+"] " + nodeList.get(i).getName() + " is cyclic ");
	    		found= true;
	    	}
	    }
	    return found;
	}
	
	public boolean hasBeforeCycle(int index,boolean[] visited, boolean[] recstack, List<CaseSchema> nodeList) {
		CaseSchema csi=nodeList.get(index);
        if (recstack[index]) 
            return true; 
 
        if (visited[index]) 
            return false; 
       
        visited[index] = true; 
        recstack[index] = true; 
        	
        for (int j=0;j<nodeList.size();j++) {
    		CaseSchema csj=nodeList.get(j);
        	if (before[nodes.indexOf(csi)][nodes.indexOf(csj)]!=null) {
        		
        		if (hasBeforeCycle(j, visited, recstack, nodeList)) 
        			return true; 
        	}
        }
        recstack[index] = false; 
        return false;
	}
	
	public Set<CaseSchema> before(CaseSchema cs){
		HashSet<CaseSchema> pred= new HashSet<CaseSchema>();
		for (CaseSchema n:nodes) {
			if (isBefore(n,cs)) {
				pred.add(n);
			}
		}
		return pred;
	}
	
	public void transitiveBeforeClosure() {
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (before[i][j]!=null) {
					for (int k=0;k<nodes.size();k++) {
						if (before[j][k]!=null) {
							before[i][k]=before[i][j];
						}
					}
				}
			}
		}
	}
	
	public boolean satisfiesBeforeOrdering(List<CaseSchema> chain) {
		// ordering in chain must be consistent with before ordering
		for (int i=1;i<chain.size()-1;i++) { // i=0 is base, so can be skipped
			for (int j=i+1;j<chain.size();j++) {
				if (isBefore(chain.get(j),chain.get(i))) {
					System.out.println("\nFeature " + chain.get(j).getName() + " should be before  " + chain.get(i).getName());
					return false; // before relation violated
				}
			}
		}
		return true;
	}
	
	public boolean isWellFormed(List<CaseSchema> chain) {
		boolean wellformed=true;
		for (int i=0;i<chain.size();i++){
			CaseSchema csi=chain.get(i);
			HashSet <PlanItem> pis=(HashSet<PlanItem>) csi.getPlanItems();
			for (PlanItem pi:pis) {
				boolean refined=false; // refined=false means introduced
				for (int j=0;j<i;j++) {
					CaseSchema csj=chain.get(j);
					PlanItem pij=csj.findSimilarPlanItem(pi);
					if (pij!=null){
						refined=true; // pi is refined
					}	
				}
				if (!refined) { // pi is introduced in csi
					boolean grounded=pi.isGroundForm(false)&&pi.isGroundForm(true);
					if (!grounded) {
						wellformed=false;
					}
				}
			}
		}
		return wellformed;
	}
	


	// using Kahn's algorithm
	// assumption: no cycle
	public List<CaseSchema>  shuffleBefore(List<CaseSchema> chain) {
		List<CaseSchema> shuffledChain=new ArrayList<CaseSchema>();
		List <CaseSchema> S=new ArrayList<CaseSchema>();
		for (int i=0;i<chain.size();i++) {
			boolean found=false;
			for (int j=0;j<chain.size();j++) {
				if (isBefore(chain.get(j),chain.get(i))) {
					found=true;

				}
			}
			if (!found) {
				S.add(chain.get(i));
			}
		}
		while (!S.isEmpty()) {
			CaseSchema cs=S.get(0);
			S.remove(0);
			shuffledChain.add(cs);
			chain.remove(cs);
			List <CaseSchema> newS=new ArrayList<CaseSchema>();
			for (int i=0;i<chain.size();i++) {
				boolean found=false;
				for (int j=0;j<chain.size();j++) {
					if (isBefore(chain.get(j),chain.get(i))) {
						found=true;
					}
				}
				if (!found) {
					newS.add(chain.get(i));
				}
			}
			for (CaseSchema cs_new:newS) {
				if (!S.contains(cs_new)) {
					S.add(cs_new);
				}
			}
		}
		if (!chain.isEmpty()) {
			return new ArrayList<CaseSchema>();
		}
		return shuffledChain;
	}
	
	
	static int var;
	public void computeVariants(String directory) {
		var=0;
		List<CaseSchema> lf=new ArrayList<CaseSchema>();
		lf.addAll(nodes);
		List<CaseSchema> sub=new ArrayList<CaseSchema>();
		computeSubset(lf,0,sub,directory);
		
	}
	
	public void computeVariants(List<CaseSchema> features, int j, String directory) {
		for (CaseSchema f1:features) {
			for (CaseSchema f2:features) {
				if (this.isConflict(f1, f2)) return; // if features are conflicting, then no composition is computed
			}
		}
		boolean compute_perms=true;
		for (CaseSchema f1:features) {
			for (CaseSchema f2:features) {
				if (this.isNotPermutable(f1, f2)) { 
					compute_perms=false;
					break;
				}
			}
			if (!compute_perms) break;
		}
		if (!compute_perms) {
			// Non-permutable, so all permutations need to be computed
			Set<ArrayList<CaseSchema>> permutations=computePermutedVariant(features);
			for (ArrayList<CaseSchema> p:permutations) {
				List<CaseSchema> tocompose=new ArrayList<CaseSchema>();
				tocompose.add(base);
				tocompose.addAll(p);
				if (isWellFormed(tocompose)) {

					try {
						FileWriter fw= new FileWriter(new File(directory,base.getPlanModel().getName()+"-non-perm-comp-"+var+".txt"));
						for (int i=0;i<tocompose.size();i++) {
							fw.write(tocompose.get(i).getName()+".cmmn\n");
						}
						fw.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					var++;		
				}
			}
		}
		else {
			// Permutable so outputting one chain only 
			List<CaseSchema> tocompose=new ArrayList<CaseSchema>();
			tocompose.addAll(features);
			tocompose=shuffleBefore(tocompose);
			if (tocompose.isEmpty()) return; // cycle in before ordering
			if (!satisfiesBeforeOrdering(tocompose)) {
				System.out.println("Error in shuffling composition chain!");
				System.exit(1);
			}
			tocompose.add(0, base);
			if (isWellFormed(tocompose)) {
				try {
					FileWriter fw= new FileWriter(new File(directory,base.getPlanModel().getName().replace(" ", "-")+"-perm-comp-"+var+".txt"));
					for (int i=0;i<tocompose.size();i++) {
						fw.write(tocompose.get(i).getName()+".cmmn\n");
					}
					fw.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				var++;
			}
		}
	}
	
	public Set<ArrayList<CaseSchema>> computePermutedVariant(List<CaseSchema> features ) {
		Set<ArrayList<CaseSchema>>permutations=new HashSet<ArrayList<CaseSchema>>();
		if (features.isEmpty()) {
			ArrayList<CaseSchema> alnew=new ArrayList<CaseSchema>();
			permutations.add(alnew);
			return permutations;
		}
		else {
			CaseSchema first=features.get(0);
			features.remove(0);
			Set<ArrayList<CaseSchema>>partialpermutations=computePermutedVariant(features);
			for (ArrayList<CaseSchema> pp:partialpermutations) {
				for (int i=0;i<=pp.size();i++) {
					ArrayList<CaseSchema> alnew=new ArrayList<CaseSchema>();
					alnew.addAll(pp);
					alnew.add(i, first);
					permutations.add(alnew);
				}
			}
			return permutations;
		}
	}
	
	int i=0;
	public void computeSubset(List<CaseSchema > all, int index, List<CaseSchema> sub, String directory) {
		if (all.size()==index) {		
			ArrayList<CaseSchema> subnew=new ArrayList<CaseSchema>();
			subnew.addAll(sub);
			if (!subnew.isEmpty()) computeVariants(subnew,i,directory);
			i++;
			return;
		}
		sub.add(all.get(index));
		computeSubset(all,index+1,sub,directory);
		sub.remove(all.get(index));
		computeSubset(all,index+1,sub,directory);
	}

	public void verify() {
		int count=0;
		for (int i=0;i<nodes.size()-1;i++) {		
			for (int j=i+1;j<nodes.size();j++) {
				count++;
				boolean isbefore=false;
				boolean isconfl=false;
				boolean ispermutable=false;;
				boolean isnonpermutable=false;
				if (before[i][j]!=null) {
					isbefore=true;
					if (before[j][i]!=null) {
						System.out.println("Check x");
						System.out.println("Feature [" +i + "] " + nodes.get(i).getName() + " is before feature [" +j + "] " + nodes.get(j).getName() + " because of plan item " + before[i][j].getPlanItemDefinition().getName() );
						System.out.println("Feature [" +j + "] " + nodes.get(j).getName() + " is before feature [" +i + "] " + nodes.get(i).getName() + " because of plan item " + before[j][i].getPlanItemDefinition().getName() );
						
//						System.exit(1);
					}
				}
				if (conflict[i][j]!=null) {
					isconfl=true;
				}
				if (permutable[i][j]==1) {
					isnonpermutable=true;
				}
				if (permutable[i][j]==2) {
					ispermutable=true;
				}
				if (isbefore&&isconfl) {
					System.out.println("Check 1");
					System.exit(1);
				}
				if (isbefore&&ispermutable) {
					System.out.println("Check 2");
					System.exit(1);
				}
				if (isbefore&&isnonpermutable) {
					System.out.println("Check 3");
					System.exit(1);
				}
				if (isconfl&&ispermutable) {
					System.out.println("Check 4");
					System.exit(1);
				}
				if (isconfl&&isnonpermutable) {
					System.out.println("Check 5");
					System.exit(1);
				}
			}

		}
		System.out.println(count + " relations checked");
	}

	public void print() {
		verify();
		System.out.println("\n\nFeature graph with "+ nodes.size() +" features:");
		System.out.println("Base schema " + base.getName());
		System.out.println("\n##### Before relations ###########\n");

		int count=0;
		for (int i=0;i<nodes.size()-1;i++) {		
			for (int j=i+1;j<nodes.size();j++) {		
				if (before[i][j]!=null) {
					count++;
					System.out.println("Feature [" +i + "] " + nodes.get(i).getName() + " is before feature [" +j + "] " + nodes.get(j).getName() + " because of plan item " + before[i][j].getPlanItemDefinition().getName() );
				}
			}
		}
		for (int i=nodes.size()-1;i>0;i--) {		
			for (int j=i-1;j>=0;j--) {		
				if (before[i][j]!=null) {
					count++;
					System.out.println("Feature [" +i + "] " + nodes.get(i).getName() + " is before feature [" +j + "] " + nodes.get(j).getName() + " because of plan item " + before[i][j].getPlanItemDefinition().getName() );
				}
			}
		}
		System.out.println("There are " + count + " before relations");
		boolean cycle=this.hasBeforeCycle();
		if (cycle) {
			System.out.println("\nThe before relation induces a cycle between the features.");
		}
		else {
			System.out.println("\nThe before relation does not induce a cycle between the features.");
		}
		System.out.println("\n##### Conflicts relations ###########");
		count=0;
		for (int i=0;i<nodes.size()-1;i++) {
			
			for (int j=i+1;j<nodes.size();j++) {
				if (conflict[i][j]!=null) {
					count++;
					System.out.println("Feature [" + i +"] " + nodes.get(i).getName() + " and feature ["+ j+"] " + nodes.get(j).getName() + " are conflicting since both define plan item " + conflict[i][j].getPlanItemDefinition().getName()+".");
				}
			}
		}
		System.out.println("There are " + count + " conflict relations");

		System.out.println("\n##### Permutable/ Non-permutable relations ###########");
		for (int i=0;i<nodes.size();i++) {		
			for (int j=0;j<nodes.size();j++) {		
				System.out.print(permutable[i][j]);
			}
			System.out.println("");
		}
		count=0;
		int ncount=0;
		for (int i=0;i<nodes.size()-1;i++) {		
			for (int j=i+1;j<nodes.size();j++) {		
				if (permutable[i][j]==2) {
					System.out.println("Feature [" + i + "] "+ nodes.get(i).getName() + " and feature ["+ j+"] " + nodes.get(j).getName() + " are permutable "  );
					count++;
				}
				if (permutable[i][j]==1) {
					System.out.println("Feature [" + i +"] "  + nodes.get(i).getName() + " and feature ["+ j+"] " + nodes.get(j).getName() + " are not permutable "  );
					ncount++;
				}
			}
		}
		System.out.println("There are " + count + " permutable relations");
		System.out.println("There are " + ncount + " non-permutable relations");

	}
	
	void printComplexity(String directory) {
		try {
			FileWriter fw= new FileWriter(new File(directory,base.getPlanModel().getName()+"-complexity.txt"));
			for (CaseSchema cs:nodes) {
				fw.write(cs.printComplexity());
			}
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

package nl.tue.ieis.is.CMMN;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;


public class VariantGraph {
	
	List<CaseSchema> nodes;
	boolean [][] edges;
	
	public VariantGraph() {
		nodes=new ArrayList<CaseSchema>();
	}
	
	public void addCaseSchema(CaseSchema cs) {
		if (!nodes.contains(cs)) {
			for (CaseSchema n:nodes) {
				if (cs.isRefinedBy(n)&&n.isRefinedBy(cs)) { // cs equals n, so cs can be ignored
					return;
				}
			}
			nodes.add(cs);
		}
	}
	
	
	public void analyzeContainment() {
		edges=new boolean[nodes.size()][nodes.size()];
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				edges[i][j]=false;			
			}
		}
		for (CaseSchema csi:nodes) {
			for (CaseSchema csj:nodes) {
				if (csi==csj) continue;
				if (csi.isRefinedBy(csj)) {
					if (!csj.isRefinedBy(csi)) {
						edges[nodes.indexOf(csi)][nodes.indexOf(csj)]=true;
					}
				}
			}
		
		}
	}
	
	// assumption: analyzecontainment has been called before
	public Set<CaseSchema> findLocalBaseSchema() {
		HashSet<CaseSchema> localBaseSchemas=new HashSet<CaseSchema>();	
		for (int j=0;j<nodes.size();j++) {
			boolean hasPredecessor=false;
			for (int i=0;i<nodes.size();i++) {
				if (edges[i][j]==true) {// caseschema at index j has a predecessor
					hasPredecessor=true; 
				}
			}
			if (!hasPredecessor) {
				localBaseSchemas.add(nodes.get(j));
			}
		}
		return localBaseSchemas;
	}
	
	public void computeIntersections(String directory) {
		int x=0;
		Set<CaseSchema> localBaseSchemas=this.findLocalBaseSchema();

		while (localBaseSchemas.size()>1) {
			x++;
			List<CaseSchema> newNodes=new ArrayList();

			boolean new_variant=false;
			// calculate intersections
			for (CaseSchema n1:localBaseSchemas) {
				for (CaseSchema n2:localBaseSchemas) {
					if (n1==n2) continue;
					CaseSchema intersection=n1.computeIntersection(n2);
					newNodes.add(intersection);	
				}
			}
			// update variant graph with intersection variants
			for (CaseSchema nn:newNodes) {
				boolean found=false;
				for (CaseSchema n:nodes) {
					if (nn.equals(n)) {
						found=true;
					}
				}
				if (!found) {
					new_variant=true;
					this.addCaseSchema(nn);
					CMMNwriter cw= new CMMNwriter();
					try {
						cw.writeFileUsingJDOM(directory,nn);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				else { // variant already defined
	
				}
			}
			this.analyzeContainment();
			localBaseSchemas=this.findLocalBaseSchema();

		}
		System.out.println("Intersections computed in " + x + " iteration(s)\n");
	}
	
	
	
	public boolean isUndirectedPath(CaseSchema from, CaseSchema to) {
		this.analyzeContainment();
		boolean [][] undirectedEdges=new boolean[nodes.size()][nodes.size()];;
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				undirectedEdges[i][j]=false;			
			}
		}
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (edges[i][j]) {
					undirectedEdges[i][j]=true;
					undirectedEdges[j][i]=true;

				}
			}
		}
		
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (undirectedEdges[i][j]) {
					for (int k=0;k<nodes.size();k++) {
						if (undirectedEdges[j][k]) {
							undirectedEdges[i][k]=true;
						}
					}
				}
			}
		}
		boolean b= undirectedEdges[nodes.indexOf(from)][nodes.indexOf(to)];
		return b;
	}
	
	
	
	public void transitiveClosure() {
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (edges[i][j]) {
					for (int k=0;k<nodes.size();k++) {
						if (edges[j][k]) {
							edges[i][k]=true;
						}
					}
				}
			}
		}
	}
	
	public void transitiveReduction() {
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (edges[i][j]) {
					for (int k=0;k<nodes.size();k++) {
						if (edges[j][k]) {
							edges[i][k]=false;
						}
					}
				}
			}
		}
	}

	public List<List<CaseSchema>> getContainments(){
		List<List<CaseSchema>> list=new ArrayList<List<CaseSchema>>();
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (edges[i][j]) {
					List<CaseSchema> lcs=new ArrayList<CaseSchema>();
					lcs.add(nodes.get(i));
					lcs.add(nodes.get(j));
					list.add(lcs);
				}
			}
		}
		return list;
	}

	public void print() {
		System.out.println("Beginning variant graph");
		System.out.println("There are  " +nodes.size() + " distinct variants");
		for (int i=0;i<nodes.size();i++) {
			nodes.get(i).printStatistics();
		}
		Set<CaseSchema> localBaseSchema=this.findLocalBaseSchema();
		int count=0;
		for (int i=0;i<nodes.size();i++) {
			for (int j=0;j<nodes.size();j++) {
				if (edges[i][j]) {
					System.out.println("Case schema  " + nodes.get(i).getName() + " is refined by " + nodes.get(j).getName());
					count++;
				}
			}
		}
		System.out.println("There are " + count + " refinements");
		System.out.println("End of variant graph\n\n");

	}
}

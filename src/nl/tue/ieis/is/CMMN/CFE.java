package nl.tue.ieis.is.CMMN;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// CaseFragementEngineering class
public class CFE {

	private static String greatestCommonPrefix(String a, String b) {
	    int minLength = Math.min(a.length(), b.length());
	    for (int i = 0; i < minLength; i++) {
	        if (a.charAt(i) != b.charAt(i)) {
	            return a.substring(0, i);
	        }
	    }
	    return a.substring(0, minLength);
	}
	
	public static void process(File f, String directory) {
		FileReader fr;
		try {
			fr = new FileReader(f);
			BufferedReader br = new BufferedReader(fr);
			ArrayList<CaseSchema> filelist=new ArrayList<CaseSchema>();
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null) {
				if (!sCurrentLine.equals("")){
					File fl = new File(directory,sCurrentLine);
					String fn=""; // full filename
					try {
						fn = fl.toURI().toURL().toString();
					} catch (MalformedURLException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					fn=sCurrentLine;	
					String name=fn.substring(0,fn.indexOf(".cmmn"));
					filelist.add(CMMNreader.doc2cmmn(CMMNreader.getDocument(fl),name));
				}
			}
			boolean wellformed=true;
			for (int i=0;i<filelist.size();i++){
				CaseSchema csi=filelist.get(i);
				HashSet <PlanItem> pis=(HashSet<PlanItem>) csi.getPlanItems();
				for (PlanItem pi:pis) {
					boolean refined=false; // refined=false means introduced
					for (int j=0;j<i;j++) {
						CaseSchema csj=filelist.get(j);
						PlanItem pij=csj.findSimilarPlanItem(pi);
						if (pij!=null){
							refined=true; // pi is refined
						}	
					}
					if (!refined) { // pi is introduced in csi
						boolean grounded=pi.isGroundForm(false)&&pi.isGroundForm(true);
						if (!grounded) {
							System.out.println("Case schema " + csi.getName() + " introduces but does not define " + pi.getPlanItemDefinition().getName());
							wellformed=false;
							System.out.println(pi.getContext().getCaseContext().getName());
							System.exit(1);
						}
					}
					else {
						// search previous occurrences of similar plan items
						boolean first=true;
						for (int j=0;j<i;j++) {
							CaseSchema csj=filelist.get(j);
							PlanItem pij=csj.findSimilarPlanItem(pi);
							if (pij!=null){
								if (first) {
									first=false;
								}
								else { // csj refines pi, so analyze independence of pij and pi
									if (!pij.analyzeMatchingSentrySetForms(pi)) {
										System.out.println("Case schemas " + csj.getName() + " and " + csi.getName() + " both refine plan item " + pi.getPlanItemDefinition().getName() + " but do not have matching sentry set forms.");
										wellformed=false;
									}
								}
							}
						}
					}
				}
			}
			if (wellformed) {
				System.out.println("The composition chain in file " + f.getName() +" is well-formed.");
			}
			else {
				System.out.println("The composition chain in file " + f.getName()  + "is not well-formed.");
			}
			CaseSchema comp=null,add=null;




			for (int i=0;i<filelist.size();i++){
				add=filelist.get(i);
				//					
				if (comp==null){
					comp=add;
					comp.setName(add.getName().replace(" ", "-"));
				}
				else{
					String prefix=greatestCommonPrefix(comp.getName(),add.getName());
					String name=add.getName().substring(prefix.length());
					comp.setName(comp.getName()+"-"+name);
					comp.compose(add);
					comp.verify();
				}
			}
			comp.setName(comp.getName()+"--comp");
			System.out.println("Composition written in file "+ comp.getName()+".cmmn\n");
			CMMNwriter cw= new CMMNwriter();
			try {
				cw.writeFileUsingJDOM(directory,comp);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}


	}
	
	public static void main(String args[]){
		if (args.length>0){
			if (args[0].equals("-d")) {

				String file=args[1];
				try{
					int filesep_index=args[1].lastIndexOf(File.separator);
					String directory,filename;
					if (filesep_index>-1) {
						directory=args[1].substring(0, args[1].lastIndexOf(File.separator));
						filename=args[1].substring(args[1].lastIndexOf(File.separator)+1);
					}
					else{
						directory=".";
						filename=args[1];
					}
					File fi=new File(directory,filename);
					FileReader fr= new FileReader(fi);
					BufferedReader br = new BufferedReader(fr);

					ArrayList<CaseSchema> filelist=new ArrayList<CaseSchema>();

					
					String sCurrentLine;

					while ((sCurrentLine = br.readLine()) != null) {
				  		if (!sCurrentLine.equals("")){
				  			File f1 = new File(directory,sCurrentLine);
				  			String fn=""; // full filename
				  			try {
				  				fn = f1.toURI().toURL().toString();
				  			} catch (MalformedURLException e1) {
				  				// TODO Auto-generated catch block
				  				e1.printStackTrace();
				  			}
				  			String name=sCurrentLine.substring(0,sCurrentLine.indexOf(".cmmn"));
				  			filelist.add(CMMNreader.doc2cmmn(CMMNreader.getDocument(f1),name));
				  		}
					}
					directory+="/fragments";
					File d=new File(directory);
					if (!d.exists()) d.mkdir();
					
					VariantGraph vg=new VariantGraph();
					System.out.println("\n\n******** Processing variants ********\n\n");
					for (int i=0;i<filelist.size();i++){
						CaseSchema csi=filelist.get(i);
						vg.addCaseSchema(csi);
					}
					vg.analyzeContainment();
					vg.computeIntersections(directory);				    
				    System.out.println("\n******** Extracting features from the variants ******** ");
					List<List<CaseSchema>> list=vg.getContainments();
					List<CaseSchema> features=new ArrayList();
					int identicalfeatures=0;
					int extractedfeatures=0;
					CaseSchema.resetContextCount();
					for (List<CaseSchema> lcs:list) {
						CaseSchema csi=lcs.get(0);
						CaseSchema csj=lcs.get(1);
						Set<CaseSchema> feature_set=csi.extractFeature(csj);
						extractedfeatures+=feature_set.size();
						for (CaseSchema feature:feature_set) {
							feature.removeDuplicateSentries();
							boolean found=false;
							for (CaseSchema f:features) {
								if (f.equals(feature)) { //	Feature f already defined
									found=true;
									f.mergeOrigins(feature);
									identicalfeatures++;
								}
							}
							if (!found) {
								features.add(feature);	
							}
						}
					}
					System.out.println("\n\nThere were " + CaseSchema.getContextCount()+ " change contexts");
					System.out.println("There were " + extractedfeatures + " features extracted (" + extractedfeatures +" consistent change contexts)");
					System.out.println("There were in total  " + features.size() + " features extracted");
				    System.out.println("\n\n******** Reactoring features ******** \n");

					int c=0;
					boolean refactoringCheck=true;
					while (refactoringCheck) {
						c++;
						refactoringCheck=false;
						Set<List<CaseSchema>> tobeRefactoredFeatures=new HashSet<List<CaseSchema>>();
						
						for (CaseSchema f1:features) {
							for (CaseSchema f2:features) {
								if (f1==f2) continue;
								if (f1.equals(f2)) {
									System.out.println("Duplicate features detected");
									System.exit(1);
								};
								if (f1.isRefinedBy(f2)) {
									PlanItem pi=f1.isConsistentlyRefinedBy(f2);
									if (pi==null) {	 // all contexts must be  consistently refined, to reconstruct f2 
										List<CaseSchema> lc=new ArrayList<CaseSchema>();
										lc.add(f1);
										lc.add(f2);
										tobeRefactoredFeatures.add(lc);
									}
									else {
										System.out.println("Feature " + f1.getName() + " is refined but not consistently refined by  " + f2.getName() + "; therefore refactoring not possible");
										System.out.println("Plan item " + pi.getName() + " is not consistently refined");
									}
								}
							}
						}
						
						
						Set<CaseSchema> oldRefactoredFeatures=new HashSet<CaseSchema>();
						Set<CaseSchema> newRefactoredFeatures=new HashSet<CaseSchema>();
						for (List<CaseSchema> lrf:tobeRefactoredFeatures) {
							if (lrf.get(0).equals(lrf.get(1))) {
								System.out.println("Refactored features are equal!");
								System.exit(0);
							}
														
							Set<CaseSchema> sc=lrf.get(0).extractFeature(lrf.get(1));
							newRefactoredFeatures.addAll(sc);
							oldRefactoredFeatures.add(lrf.get(1));

						}
						Set<CaseSchema> features_old=new HashSet<CaseSchema>();
						features_old.addAll(features);
						features.removeAll(oldRefactoredFeatures);

			
						int x=0;
						// only add newly refactored feature if it is not yet in set features.
						for (CaseSchema nf:newRefactoredFeatures) {
							nf.removeDuplicateSentries();
							boolean found=false;
							for (CaseSchema f:features) {
								if (nf.equals(f)) {
									found=true;
									break;
								}
							}
							if (!found) {
								x++;
								features.add(nf);
							}
						}
						
						for (CaseSchema f1:features_old) {
							boolean found=false;
							for (CaseSchema f2:features) {
								if (f1.equals(f2)) {
									found=true;
									break;
								}
							}
							if (!found) {
								refactoringCheck=true;
								break;
							}
						}
						if (!refactoringCheck) {
							for (CaseSchema f1:features) {
								boolean found=false;
								for (CaseSchema f2:features_old) {
									if (f1.equals(f2)) {
										found=true;
										break;
									}
								}
								if (!found) {
									refactoringCheck=true;
									break;
								}
							}
						}
					}
					System.out.println("\nRefactoring iterations: " + c);
					System.out.println("Features after refactoring: " + features.size());
//					System.out.println("\n\n******** Analyzing extracted features ********\n\n");
					
					
					Set<List<CaseSchema>> tobeRefactoredFeatures=new HashSet<List<CaseSchema>>();
					Set<List<CaseSchema>> nonRefactorableFeatures=new HashSet<List<CaseSchema>>();
					for (CaseSchema f1:features) {
						for (CaseSchema f2:features) {
							if (f1==f2) continue;
							if (f1.equals(f2)) {
								System.out.println("Duplicate features detected");
								System.exit(1);
							};
							if (f1.isRefinedBy(f2)) {
								if (f1.isConsistentlyRefinedBy(f2)==null) {
									List<CaseSchema> lc=new ArrayList<CaseSchema>();
									lc.add(f1);
									lc.add(f2);
									tobeRefactoredFeatures.add(lc);
								}
								else {
									List<CaseSchema> lc=new ArrayList<CaseSchema>();
									lc.add(f2);
									lc.add(f1);
									nonRefactorableFeatures.add(lc);
								}
							}
						}
						
					}
					for (List<CaseSchema> lc:nonRefactorableFeatures) {
						System.out.println("Features    " + lc.get(0).getName() + "    and   " + lc.get(1).getName() +  "    cannot be refactored (no consistent refinement)");
					}
					Set<CaseSchema> toremove=new HashSet<CaseSchema>();
					for (CaseSchema f:features) {
						if (f.isIdentity()) {
							toremove.add(f);
						}
						for (CaseSchema f1:features) {
							if (f==f1) continue;
							
						}
					}
					features.removeAll(toremove);
					
					
					// analyze permutability
					Set<CaseSchema> bases=vg.findLocalBaseSchema();
					if (bases.size()>1) {
						System.out.println("Error: there is more than one base schema");
						System.exit(1);
					}
					if (bases.size()==0) {
						System.out.println("Error: there is no base schema");
						System.exit(1);
					}
					CaseSchema base=bases.iterator().next();
					{
						CMMNwriter cw= new CMMNwriter();
						cw.writeFileUsingJDOM(directory,base);
					}
					int i=0;
					for (CaseSchema f:features) {
						f.setName(base.getName()+"-F"+i);
						i++;
					}

					for (CaseSchema feature:features) {
						CMMNwriter cw= new CMMNwriter();
						try {
							if (feature==null) {
								System.out.println("Empty feature!");
							}
							else {
								cw.writeFileUsingJDOM(directory,feature);
							}
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					FeatureGraph fg=new FeatureGraph(features);
					fg.setBaseSchema(base);
					for (List<CaseSchema> lc:nonRefactorableFeatures) {
						PlanItem pi=lc.get(0).getPlanItems().iterator().next();// 
						System.out.println("Conflicting features " +lc.get(0).getName() + " ; " + lc.get(1).getName() );
					}
					for (CaseSchema f1:features) {
						HashSet <PlanItem> pis=(HashSet<PlanItem>) f1.getPlanItems();
						for (PlanItem pi1:pis) {
							boolean pi1_refines=f1.refines(pi1);
							if (f1.refines(pi1)) continue;
							for (CaseSchema f2:features) {
								if (f1.equals(f2)) continue;
								if (fg.isConflict(f2, f1)) continue; // conflict, so no before or permutable relation possible.
								PlanItem pi2=f2.findSimilarPlanItem(pi1);
								if (pi2!=null){
									boolean pi2_refines=f2.refines(pi2);
									if (pi2_refines) {
										fg.setBefore(f1, f2,pi1);
									}
								}
							}
						}
					}
					
					for (CaseSchema f1:features) {
						HashSet <PlanItem> pis=(HashSet<PlanItem>) f1.getPlanItems();
						for (PlanItem pi1:pis) {
							boolean pi1_refines=f1.refines(pi1);
							for (CaseSchema f2:features) {
								if (f1.equals(f2)) continue;
								if (fg.isConflict(f1, f2)) continue;
								if (fg.isBefore(f2, f1)||fg.isBefore(f1, f2)) continue;
								PlanItem pi2=f2.findSimilarPlanItem(pi1);
								if (pi2!=null){
									boolean pi2_refines=f2.refines(pi2);
									if (!pi1_refines&&!pi2_refines) {
										if (!pi1.hasSimilarSentries(pi2)) {
											fg.setConflict(f1, f2,pi1);
										}
									}
								}
							}
						}
					}					
					for (CaseSchema f1:features) {
						HashSet <PlanItem> pis=(HashSet<PlanItem>) f1.getPlanItems();
						for (PlanItem pi1:pis) {
							boolean pi1_refines=f1.refines(pi1);
							for (CaseSchema f2:features) {
								if (f1.equals(f2)) continue;
								if (fg.isConflict(f2, f1)) continue; // conflict, so no before or permutable relation possible.
								if (fg.isBefore(f1, f2)||fg.isBefore(f2, f1)) continue;
								if (!fg.isNotPermutable(f1, f2)) fg.setPermutable(f1, f2); // init to permutability
								PlanItem pi2=f2.findSimilarPlanItem(pi1);
								if (pi2!=null){
									boolean pi2_refines=f2.refines(pi2);
									if (pi1_refines&&pi2_refines) {
										if (!pi1.analyzeMatchingSentrySetForms(pi2)) {
											fg.setNotPermutable(f1, f2);
										}

									}
								}
							}
						}
					}
					
					System.out.println("\n\n******** Computing variants ********\n\n");
//					fg.printComplexity(directory);
					fg.computeVariants(directory);
					System.out.println("Composition scripts for variants written in working folder");

				}
				catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
			}
			else if (args[0].equals("-c")) {
				int index=1;
				while (index<args.length) {
					String file=args[index];
					File f=new File(file);
					if (!f.isDirectory()) {
						int filesep_index=args[index].lastIndexOf(File.separator);
						String directory,filename;
						if (filesep_index>-1) {
							directory=args[index].substring(0, args[0].lastIndexOf(File.separator));
							filename=args[index].substring(args[0].lastIndexOf(File.separator)+1);
						}
						else{
							directory=".";
							filename=args[index];
						}
						File f1=new File(directory,filename);
						process(f1,directory);
					}
					else { //file is directory, read all comp*txt files in directory
						File[] listOfFiles = f.listFiles();
						for (File f1:listOfFiles) {
							if ((f1.getName().contains("comp-")||f1.getName().contains("comp.")) && f1.getName().endsWith("txt")){
								process(f1,file);
							}
						}
					}
					index++;
				}
			}
			else if (args[0].equals("-m")){
				String folder1=args[1];
				File f1 = new File(folder1); // variants
				String folder2=args[2];
				File f2 = new File(folder2);
				File[] listOfFiles1 = f1.listFiles();

				File[] listOfFiles2 = f2.listFiles();
				ArrayList<CaseSchema> filelist1=new ArrayList<CaseSchema>();
				
				for (File f:listOfFiles1) {
					String fn=f.getName();
					fn=f.getAbsolutePath();
					if (!fn.endsWith(".cmmn")) continue;
					String name=fn.substring(0,fn.indexOf(".cmmn"));

					filelist1.add(CMMNreader.doc2cmmn(CMMNreader.getDocument(f), name));
				}

				ArrayList<CaseSchema> filelist2=new ArrayList<CaseSchema>();
				for (File f:listOfFiles2) {
					String fn=f.getName();
					fn=f.getAbsolutePath();
					if (!fn.endsWith("--comp.cmmn")) continue;
					String name=fn.substring(0,fn.indexOf(".cmmn"));

					filelist2.add(CMMNreader.doc2cmmn(CMMNreader.getDocument(f), name));
				}
							
				for (CaseSchema cs1:filelist1) {
					boolean match=false;
					String firstMatch="";
					for (CaseSchema cs2:filelist2) {
						if (cs1.equals(cs2)) {
							System.out.println("Case schemas " + cs1.getName() + " and " +  cs2.getName() +  " are equal!");
							if (!match) {
								match=true;
								firstMatch="Multiple matches for " + cs1.getName() + "    (" + cs2.getName() + ")";
							}
							else {
								System.out.println(firstMatch);
								System.out.println("Multiple matches for " + cs1.getName() + "    (" + cs2.getName() + ")");
							}
						}
					}
					if (!match) {
						System.out.println("\n\n\nNo match for " + cs1.getName());
					}
				}
				Set<CaseSchema> toremove=new HashSet<CaseSchema>();
				for (int i=0;i<filelist2.size()-1;i++) {
					CaseSchema cs1=filelist2.get(i);
					for (int j=i+1;j<filelist2.size();j++) {
						CaseSchema cs2=filelist2.get(j);
						if (cs1==cs2) continue;
						if (toremove.contains(cs2)) continue;
						if (cs1.equals(cs2)) {
							toremove.add(cs2);
						}
					}
				}
				filelist2.removeAll(toremove);
				System.out.println("Number of distinct case schemas " + filelist2.size());
	
			}
			else {
				System.out.println("Usage: \n");
				System.out.println("java -jar CFE -c|-d  [ file | directory ]");
				System.out.println("\t -c\t compose fragment CMMN files that are listed in the textfile file or are contained in the directory");
				System.out.println("\t -d\t decompose CMMN files that are listed in the textfile file or are contained in the directory into fragments, stored in subdirectory 'fragments'");
				System.out.println("\njava -jar CFE -m [directory1, directory2]");
				System.out.println("\t -m\t match CMMN files in [directory1,directory2], which are directories listing CMMN files to be matched and counted; directory1 contains the input variants, directory2 the variants composed from fragments");

			}
		}
		else {
			System.out.println("Usage: \n");
			System.out.println("java -jar CFE -c|-d  [file|directory]");
			System.out.println("\t -c\t compose fragment CMMN files that are listed in the textfile file or are contained in the directory");
			System.out.println("\t -d\t decompose CMMN files that are listed in the textfile file or are contained in the directory into fragments, stored in subdirectory 'fragments'");
			System.out.println("\njava -jar CFE -m [directory1, directory2]");
			System.out.println("\t -m\t match CMMN files in [directory1,directory2], which are directories listing CMMN files to be matched and counted; directory1 contains the input variants, directory2 the variants composed from fragments");

		}
	}
	
}

# CFE
CaseFragmentEngineering for composing and decomposing case variants 

The tool CFE (CaseFragmentEngineering) composes and decomposes CMMN models. CFE can compose a base CMMN model and refining CMMN model fragments into a new CMNN model, using feature-oriented composition. See https://research.tue.nl/en/publications/feature-oriented-composition-of-declarative-artifact-centric-proc for a paper defining the composition approach.

CFE can also decompose a set of CMMN variants into a base schema (intersection of the variants) and a minimal set of fragments that can subsequently be composed by CFE into the input variants but also additional variants. 

CFE requires the JDOM library: jdom-1.1.3.jar


Usage:

java -jar CFE -c|-d  [file|directory]

	 -c	 file: compose fragment CMMN files according to the composition script in textfile file; the first line lists a base CMMN model and the subsequent lines fragment CMMN models.
	 
	 -c	 directory: compose all composition scripts in the directory that have name '*comp*.txt'.

	 -d	 decompose CMMN files that are listed in the textfile file or are contained in the directory into fragments, stored in subdirectory 'fragments'

java -jar CFE -m [directory1, directory2]
	 -m	 match CMMN files in directory1 and directory2 ; directory1 contains the input variants, directory2 the variants composed from fragments



Directory 'examples' contains CMMN model variants. CFE -d generates CMMN files <I_name> that are intersections of variants and <name-Fx> that are features refining base CMMN file <name>.
	 

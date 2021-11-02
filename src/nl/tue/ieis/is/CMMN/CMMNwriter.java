package nl.tue.ieis.is.CMMN;
import org.jdom.*;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;

public class CMMNwriter {


	
    public void writeFileUsingJDOM(String directory,CaseSchema cs) throws IOException {
    	Document doc=cs.printCMMN();
        
        //JDOM document is ready now, lets write it to file now
        XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
        //output xml to console for debugging
        //xmlOutputter.output(doc, System.out);
        xmlOutputter.output(doc, new FileOutputStream(new File(directory,cs.getName()+".cmmn")));
    }
}

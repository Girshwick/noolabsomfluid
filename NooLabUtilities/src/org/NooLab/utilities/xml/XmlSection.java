package org.NooLab.utilities.xml;

import java.util.Vector;






/**
 * 
 * a section may be a full section (&lt;/xmlsection&gt;) or a pseudo-section ( />)
 * 
 *
 */
public class XmlSection {

	// object references ..............
	  
	
	
	// main variables / properties ....
	boolean isFull; 
	boolean containsList ; // a section may contain a list of sections or pseudo-sections  
	
	String childLabel;
 
	Vector<XmlSection> childSections = new Vector<XmlSection>() ;
	
	Vector<XmlTag> tags = new Vector<XmlTag>(); 
	
	// volatile variables .............
	
	
	// helper objects .................
	
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	public XmlSection(){
		
	}
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 

	
	
	// ------------------------------------------------------------------------

	
}

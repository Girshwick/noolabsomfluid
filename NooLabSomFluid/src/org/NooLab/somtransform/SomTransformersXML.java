package org.NooLab.somtransform;


import java.util.ArrayList;

import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.xml.XMessageAbs;

import com.jamesmurty.utils.XMLBuilder;



public class SomTransformersXML extends XMessageAbs{

	
	
	PrintLog out = new PrintLog(2,false);
	
	public SomTransformersXML( ){
		super();
			
	}
	
	
	
	// ------- facade begin ---------
	public XMLBuilder getXmlBuilder( String XFrame ){
		
		return super.getXmlBuilder(XFrame) ;
	}
	
	public String getXmlStr( XMLBuilder builder, boolean fullXML ){
		return super.getXmlStr(builder, fullXML) ;
	}
	
	
	// ------- facade end   ---------

	public String template( BasicMessageParams basics,  String action){ 
	 
		String xmlstr = "" ;
		XMLBuilder builder ;

		
		try {
 
			
			builder = getXmlBuilder( "messageboard" ).a( "name", "spela").a("role", basics.roleOfSender ); 
			
			builder.e("subscription")
			
					.up() ;
			
			  
			
			xmlstr = getXmlStr(builder, true);
			 
			
		}catch(Exception e){
		}
		
		return xmlstr;
	
	}







	public String transcodeStackedTransformation(int i, StackedTransformation stTransform) {
		String xstr = "", algoName="",outColumn="";
		
		algoName = stTransform.algorithmName;
		outColumn = stTransform.outputColumnLabel ;
		
		 
		
		return xstr;
	}


	/**
	 * this is generically/type-free usable for lists on primitives, as it 
	 * detects the type of the collected primitive
	 *
	 * @param list
	 */
	public String digestList(ArrayList<?> list) {

		String xstr = "";
		
		if ((list==null) || (list.size()==0)){
			return xstr;
		}
		
		Object obj = list.get(0) ;
		
		String cn = obj.getClass().getSimpleName() ;
		
		
		if ((cn.toLowerCase().startsWith("int")) && (cn.contains("[]"))){
			
			return xstr;
		}
		if ((cn.toLowerCase().startsWith("doub")) && (cn.contains("[]"))){
			
			return xstr;
		}
		if ((cn.toLowerCase().startsWith("str")) && (cn.contains("[]"))){

			return xstr;
		}
		
		if (cn.toLowerCase().startsWith("int")){
			xstr = digestIntList(   (ArrayList<Integer>) list);
			return xstr;
		}
		if (cn.toLowerCase().startsWith("doub")){
			xstr = digestNumList( (ArrayList<Double>) list);
			return xstr;
		}
		if (cn.toLowerCase().startsWith("str")){
			xstr = digestStringList((ArrayList<String>) list);
			return xstr;
		}
		
		return xstr;

		
	}
	
	public String digestStringList( ArrayList<String> strlist){
		String str;
		String xstr = "";
		
		if ((strlist==null) || (strlist.size()==0)){
			return xstr;
		} 
		
		
		for (int i=0;i<strlist.size();i++){
			str = strlist.get(i);
			// this is a GUID !! which we also will find in "variables"
		}
		
		return "";
	}
	public String digestNumList( ArrayList<Double> numlist){
		
		String xstr = "";
		if ((numlist==null) || (numlist.size()==0)){
			return xstr;
		} 
		
		
		for (int i=0;i<numlist.size();i++){
			double v = numlist.get(i);
		}
		
		return "";
	}
	public String digestIntList( ArrayList<Integer> ilist){
		
		String xstr = "";
		if ((ilist==null) || (ilist.size()==0)){
			return xstr;
		} 
		
		
		for (int i=0;i<ilist.size();i++){
			int vi = ilist.get(i);
		}
		
		return "";
	}
	
	
}	

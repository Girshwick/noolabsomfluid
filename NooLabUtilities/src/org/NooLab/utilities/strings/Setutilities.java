package org.NooLab.utilities.strings;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;




public 

/**
 * 
 * this is a (handling) facade for using the CollectionUtils of the Collections framework by apache
 * it provides easy access to set operations, i.e. mostly handling methods
 * 
 * 
 */
class Setutilities{
	
	protected static List<Integer> A = Arrays.asList(1, 2, 3, 4);  
	Set<String> cs = new TreeSet<String>();
	
	public Setutilities(){
		
	}
	
	protected static String set(Collection<Integer> S){
		
	    return " {" + StringUtils.join(S.iterator(), ",") + "} ";  
	} 
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public String union( Collection c1, Collection c2){
		String str;
		
		str = set(CollectionUtils.union( c1, c2));
		
		
	    return  str  ;
	}
	
	@SuppressWarnings("rawtypes")
	public int intersectionSize( Collection c1, Collection c2){
		Collection rc ;
		int result = -1;
		
		rc = CollectionUtils.intersection( c1, c2) ;
		
		result = rc.size() ;
		
		return result ;
		
	}
	
	@SuppressWarnings("unchecked")
	public int intersectionSize( String str1, String str2){
		int result = -1;
		int n,m ;
		String[] cs1,cs2;
		Collection<String> rc;
		
		
		n = str1.length();
		cs1 = new String[n];
		
		m = str2.length();
		cs2 = new String[m];
		 
		for (int i=0;i<n;i++){
			cs1[i] = str1.charAt(i)+"";
		}
		for (int i=0;i<m;i++){
			cs2[i] = str2.charAt(i)+"";
		}
		
		List<String> c1 = Arrays.asList( cs1);
		List<String> c2 = Arrays.asList( cs2 );
		 
		
		rc = CollectionUtils.intersection(  c1,  c2);
		
		result = rc.size() ; 
			
		return result ;
	}
	
	
}
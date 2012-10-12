package org.NooLab.utilities.strings;



import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import java.nio.*;
import java.nio.charset.*;
import java.text.DecimalFormat;

import java.util.*;
import java.util.regex.*;

import org.NooLab.utilities.CallbackForPrcCompletionIntf;
import org.NooLab.utilities.inifile.*;
import org.NooLab.utilities.logging.PrintLog;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.*;
import org.jsoup.Jsoup;
 
// import org.apache.commons.lang3.* ;
 
/*

File manuscriptFile = new File("manuscript.txt");
Reader reader = new FileReader( manuscriptFile );
LineNumberReader lineReader = new LineNumberReader( reader );
int numOccurences = 0;
while( lineReader.ready( ) ) {
    String line = StringUtils.lowerCase( lineReader.readLine( ) );
    numOccurences += StringUtils.countMatches( , "futility" );
}


int dist = StringUtils.getLevenshteinDistance( "Word", "World" );

StringUtils.split()

String title = StringUtils.substringBetween(htmlContent, "<title>",


Filtering a Collection with a Predicate


public class StringUtils
extends java.lang.Object

Operations on String that are null safe.

    IsEmpty/IsBlank - checks if a String contains text
    Trim/Strip - removes leading and trailing whitespace
    Equals - compares two strings null-safe
    startsWith - check if a String starts with a prefix null-safe
    endsWith - check if a String ends with a suffix null-safe
    IndexOf/LastIndexOf/Contains - null-safe index-of checks
    IndexOfAny/LastIndexOfAny/IndexOfAnyBut/LastIndexOfAnyBut - index-of any of a set of Strings
    ContainsOnly/ContainsNone/ContainsAny - does String contains only/none/any of these characters
    Substring/Left/Right/Mid - null-safe substring extractions
    SubstringBefore/SubstringAfter/SubstringBetween - substring extraction relative to other strings
    Split/Join - splits a String into an array of substrings and vice versa
    Remove/Delete - removes part of a String
    Replace/Overlay - Searches a String and replaces one String with another
    Chomp/Chop - removes the last part of a String
    LeftPad/RightPad/Center/Repeat - pads a String
    UpperCase/LowerCase/SwapCase/Capitalize/Uncapitalize - changes the case of a String
    CountMatches - counts the number of occurrences of one String in another
    IsAlpha/IsNumeric/IsWhitespace/IsAsciiPrintable - checks the characters in a String
    DefaultString - protects against a null input String
    Reverse/ReverseDelimited - reverses a String
    Abbreviate - abbreviates a string using ellipsis
    Difference - compares Strings and reports on their differences
    LevensteinDistance - the number of changes needed to change one String into another



	chr = str.substring(82, 83);
	if (chr.contentEquals(".")){
		chr = str.substring(83, 84);
		char c = chr.charAt(0);
		int cc = (int)c;
		cc = cc+0;
	}

	removeTags -> str = strgutil.regexScanner(str, "<[^>]*>", " ", 99);
*/

public class StringsUtil{  //  implements Serializable

	private static final long serialVersionUID = -8286948353171915322L;

	int startpos=0;
	
	String[] numbers = { "0","1","2","3","4","5","6","7","8","9",  } ;
	
	String effectiveSeparator ="";
	
	// result buffer
	transient Vector<ItemResult> itemresults = new Vector<ItemResult>();
	
	transient StringComparison strgcomp =  new StringComparison(); 
	
	transient localNumStuff nums = new localNumStuff();

	transient CallbackForPrcCompletionIntf displayCompletion;
	
	String lastErrorDescription = "";
	int lastErr=0;
	
	
	// ========================================================================
  	
	public void setDisplayCompletion(CallbackForPrcCompletionIntf callbackInstance) {
		displayCompletion = callbackInstance;
	}

	/**
	 * 
	 * replaces 
	 * 
	 * @param str
	 * @param position
	 * @param c
	 * @return
	 */
	public String setposition( String str, int position, String insertstr){
		String rStr = str;
		String part1="", part2="";
		
		try{
			
			if (position>str.length()){
				return str;
			}
			part1 = str.substring(0,position);
			if (position<str.length()-1){
				part2 = str.substring(position+1, str.length());
			}
			rStr = part1 + insertstr + part2 ;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		return rStr;
	}
	
	public String setposition( String str, int startpos, int endpos, String replacement){
		String rStr = str;
		
		
		
		return rStr;
	}
	

	public int getColumnsCountSimple( String str, String separator){
		
		int  cc=0;
		
		cc = 1 + StringUtils.countMatches( str, ";" );
		  
		return cc;
	}

		
	public String getEffectiveSeparator(){
	
		return effectiveSeparator;
	}
	
	public int getColumnsCount( Vector<String> strtable, int inspectrowscount){
		
		String[] separators = {";","\t"," ",","} ;
		
		int sepcount = separators.length ;
		int colcount=-1;
		int[][] frequencies = new int[inspectrowscount][sepcount];
		int[] excluded = new int[sepcount] ;
		int[] scount = new int[sepcount] ;
		int[] candidate = new int[sepcount] ;
		double[] sratio = new double[sepcount] ;
		int mx,cmx,p = -1;
		double v;
		
		
		String rowstr ;
		int z=-1; 
		
		for (int s=0;s<sepcount;s++){
			scount[s] = -1;
		}
		
		
		for (int i=0;i<inspectrowscount;i++){
			rowstr = strtable.get(i) ;
			
			if ((rowstr!=null) && (rowstr.length()>1)){
				z++;
				for (int s=0;s<sepcount;s++){
					frequencies[z][0] = getColumnsCountSimple( rowstr ,separators[s] ) ;					
				}
			}
		}
		
		
		for (int r=0;r<frequencies.length;r++){
			
			for (int s=0;s<sepcount;s++){
				if (frequencies[z][s]==0){
					excluded[s] = 1 ;
				}
			} // s->
			
		} // r->
		
		for (int s=0;s<sepcount;s++){
			if (excluded[s] == 0) {
				
				for (int r = 0; r < frequencies.length; r++) {
					if (r==0){
						scount[s]=0;
					}
					scount[s] = scount[s] + frequencies[z][s];
					
				} // r->
			}
			
		}// s->
		
		// now, we take as colcount just that measure as derived from a particular separator,
		// which is constant across all guessed rows
		for (int s=0;s<sepcount;s++){
			sratio[s] = (double)((double)(1.0*scount[s])/(double)(inspectrowscount*1.0));
			v = sratio[s];
			if (v == Math.round(v)){
				candidate[s] = (int)sratio[s] ;
			}
		}
		
		// if there is still more than 1 candidate, we take the larger one
		mx = 0;
		for (int s=0;s<sepcount;s++){
			if (candidate[s]>0){
				cmx = Math.max(mx, candidate[s]);
				if (cmx>mx){
					p=s;
				}
			}
		}
		
		if ((p>=0) && (p<candidate.length)){
		
			effectiveSeparator =  separators[p] ;
		
			colcount = candidate[p];
		}
		
		return colcount;
	}
	
	
	
	public int getsubStrFrequency( String str, String particle){
		
		
		return  getsubStrFrequency( str, particle, 0);
	}
	
	
	public int getsubStrFrequency( String str, String particle, int maxcount){
		int count=0;
		 
		int pLen, sLen, p, i=0;
		
		if (particle.length()==0){
			return count;
		}
		
		try{
			
			str = replaceAll(str,particle+particle, " " );
			
			str = str.trim() ; 
			pLen =  particle.length();
			sLen = str.length() ;
			
			i=0; p=0;
			while (i<sLen){

				p = str.indexOf(particle, i);

				if (p>=0){
					count++;
					i = p + pLen;
					
					if ((count>=maxcount) && (maxcount>0)){
						break ;
					}
				}else{
					break ;
				}
				i++;
			}

			
			
			
		}catch(Exception e){
			count = -1 ;
		}
		
		return count;
	}
	
	public int getsubStrFrequency( String str , String[] delimiters, int maxcount ){
		return getsubStrFrequency( str, delimiters, maxcount, false);
	}
	
	public int getsubStrFrequency( String str , String[] delimiters, int maxcount, boolean trimmed){
		int count = 0;
		
		
		int pLen, sLen,dLen, p,i=0,d;
		String particle;
		
		if (delimiters.length==0){
			return count;
		}
		
		try{
			 
			sLen = str.length() ;
			dLen = delimiters.length ;
			
			for (d=0;d<dLen;d++){
				
				particle = delimiters[d] ;

				pLen =  particle.length();
				
				
				i=0; p=0;
				while (i<sLen){

					p = str.indexOf(particle, i);
						if ((p<0) && (trimmed)){
							p = str.indexOf(particle.trim(), i);
						}
					if (p>=0){
						count++;
						i = i + pLen;
						if ((count>=maxcount) && (maxcount>0)){
							return count ;
						}
					}else{
						break ;
					}
					i++;
				}

			} // i -> all particles
			
			
			
		}catch(Exception e){
			count = -1 ;
		}
		
		
		return count;
	}
	
	
	
	public int containsXt( String str, String[] delimiters, int startpos, boolean islastSnip ){
		int pos = -1;
		String delimiter;
		int d,p=-1,n;
		String[] delimitersPrep = new String[10];
		
		// . . . . . .
		
		if (str.trim().length()<=1){
			return pos;
		}
		if (delimiters.length == 0){
			return pos;
		}
		
		if (startpos<0){
			startpos = 0;
		}
		if (startpos> str.length()-1){
			return pos;
		}
		
		
		// . . . . . .
		
		try{
			
			for (d=0;d<delimiters.length;d++){
				
				delimiter = delimiters[d];
				
				// check for ...  {" al.", "[num]."  } 
				
				if (delimiter.contains("[num]")){
					// prepare the String[] , replacing it by numbers from 0..9
					
					for (n=0;n<delimiters.length;n++){
						delimitersPrep[n] = delimiter.replace("[num]",""+n);
					}
					
					p = lastIndexOfparticles( str , delimitersPrep );
					
				}else{
				
					p = str.lastIndexOf( delimiter );
					
				}
				
				if (p>pos){
					// is it at last pos ?
					if (islastSnip){
					
						// if it is not -> p=0 !
					}
					pos = p;
					
				}
			}
			
			
			  
			
		}catch(Exception e){
			
		}
		
		return pos;
	}
	
	public void stringTokenizer() {
		StringTokenizer st = new StringTokenizer("A StringTokenizer sample");

		//
		// get how many tokens inside st object
		//
		System.out.println("Tokens count: " + st.countTokens());

		//
		// iterate st object to get more tokens from it
		//
		while (st.hasMoreElements()) {
			String token = st.nextElement().toString();
			System.out.println("Token = " + token);
		}

		//
		// split a date string using a forward slash as
		// delimiter
		//
		st = new StringTokenizer("2005/12/15", "/");
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			System.out.println("Token = " + token);
		}
	}
	 
	
	public String makeCharsUnique(String strIn){
		String resultStr=strIn, cstr;
		Vector<String> parts = new Vector<String> ();
		 
		
		resultStr = resultStr.trim();
		for (int i=0;i<resultStr.length();i++){
			cstr = resultStr.substring(i, i+1);
			if (parts.indexOf(cstr)<0){
				parts.add(cstr);
			} 
		}// i->
		if (parts.size() > 0) {
			resultStr="";
			for (int i = 0; i < parts.size(); i++) {
				resultStr=resultStr+parts.get(i);
			}
		}
		parts.clear();
		parts=null;
		return resultStr;
	}
	
	
	public int frequencyOfStrings( String str , String[] ofThose ){
		int freqSum=0;
		int n=0;
		
		for (int i=0;i<ofThose.length;i++){
			
			n = frequencyOfStr( str, ofThose[i]);
			if (n>=0){
				freqSum = freqSum+n;
			}
		}
		
		return freqSum;
	}
	
	public int frequencyOfStr( String str , String ofThis ){
		int frequency;
		String[] delimiters ;
		int n;
		
		// ......................................
		
		n = ofThis.length() ;
		delimiters = new String[n];
		
		
		frequency = StringUtils.countMatches(str, ofThis) ;
		
		return frequency;

	}
	
	public int[] frequenciesOfParticles( String str , String delimitersStr ){
		int[] frequencies;
		String[] delimiters ;
		int n;
		
		// ......................................
		
		n = delimitersStr.length() ;
		delimiters = new String[n];
		
		for (int i=0;i<n;i++){
			delimiters[i] = delimitersStr.substring(i,i+1) ;
		}
		
		frequencies = frequenciesOfParticles( str , delimiters ) ;
		
		return frequencies;
	}
	
	
	public int[] frequenciesOfParticles( String str , String[] delimiters ){
		int[] frequencies = new int[0] ;
		int n,p;
		String dpart;
		
		try{
			n = delimiters.length ;
			
			frequencies = new int[n] ; 
			for (int i=0;i<n;i++){ frequencies[i] = 0 ;}
			
			for (int i=0;i<n;i++){
				
				dpart = delimiters[i];
				
				p = StringUtils.countMatches(str, dpart) ;
				frequencies[i] = p ;
			} // i->
			
		}catch(Exception e){
			
		}
		
		return frequencies;
	}
		
	
	/**
	 * 
	 * allows for simple wildcard match
	 * 
	 * @param strList
	 * @param searchFor
	 * @param offset
	 * @param matchMode 0=normal, 1=... 2=... ; 3=wildcards expected at both ends; 4=wildcard and case insensitive; 5=wildcard, case insensitive and relaxed match
	 * @return
	 */
	public int indexOf(ArrayList<String> strList, String searchFor, int offset, int matchMode){
		
		int position=-1;
		String item="" ;
		boolean hb=false;
		
		
		if ((searchFor.contains("*")==false) && (matchMode<2)){
			position = strList.indexOf(searchFor) ;
		}else{
			
			for (int i=offset;i<strList.size();i++){
				item = strList.get(i);
				
				if (matchMode>=3){
					item=item.toLowerCase();
					searchFor = searchFor.toLowerCase() ;
				}
				hb = matchSimpleWildcard(searchFor, item);
				if ((hb==false) && (matchMode>=3)){
					hb = matchSimpleWildcard(item, searchFor);
				}
				if (hb){
					position=i;
					break;
				}
			}// ->
			
		}
		
		return position;
	}
	
	public int indexOf(String str, String[] snips){
		return indexOf(str, snips,0);
	}
	
	/**
	 * 
	 * returns the first position across all provided strings "searchThose" 
	 * 
	 * @param istr
	 * @param snips
	 * @param offset
	 * @return
	 */
	public int indexOf(String istr, String[] searchThose, int offset){
		int pos=-1, start,i,p,ps; 
		String str ;
		
		if (offset<0){
			start=0;
		}else{
			start=offset ;
		}
		
		if ( (start>=istr.length()) || (searchThose==null) || (searchThose.length==0)){
			return pos;
		}
		
		i=0;
		for (i=start;i<searchThose.length;i++){
			str = searchThose[i];
			p = istr.indexOf(str) ;
			if ((p>=0) && (p>=start) && 
				((p<pos) || (pos<0)) ) {
				pos=p;
			}
			
		} // i->
		
		return pos;
	}

	
	public int indexOfNth( String str, int nth, String searchThis ){
		int pos=-1;
		
		int k=0,p;
		
		p = -3 ;
		while ((k<nth) && ((p>=0) || (p==-3))){
			p = str.indexOf(searchThis,p+1) ;
			
			if (p>=0){
				k++;
			}
			pos = p;
		} // arrived at nth occurence ?
		
		
		return pos;
	}
	
	public int indexOfBefore( String istr, String searchFor, int startposition){
		
		return indexOfBefore( istr, searchFor, startposition, 1);
	}
	
	/**
	 * 
	 * 
	 * @param istr
	 * @param searchFor
	 * @param startposition
	 * @param mode 1=just the next preceding one when moving left; -1 = the first one left-hand
	 * @return
	 */
	public int indexOfBefore( String istr, String searchFor, int startposition, int mode){
		int resultPos=-1;
		String str = istr;
		int p=-1;
		
		if (startposition<0){
			return resultPos;
		}
		str = str.substring(0,startposition) ;
		
		if (mode>=0){
			p = str.lastIndexOf(searchFor);
		}
		if (mode<0){
			p = str.indexOf(searchFor);
		}
		
		resultPos = p;
		
		return resultPos;
	}
	
	public int indexOfparticles( String str, String delimiterParticles, int startAt){
		String[] delimiters = new String[delimiterParticles.length()];
		int pos=-1;
		
		for (int i=0;i<delimiterParticles.length();i++){
			delimiters[i] = delimiterParticles.substring(i,i+1);
		}
		
		pos = indexOfparticles( str, delimiters, startAt) ;
		
		return pos;
	}
	
	public int[] indexesOfParticles( String str , String  delimiters ){
		
		return indexesOfParticles( str , delimiters, 0, false);
	}
	
	public int[] indexesOfParticles( String str , String delimiters, int offset, boolean collectAll ){
		int[] positions = new int[0] ;
		int n,p,z=0;
		String dpart;
		
		
		try{
			n = delimiters.length() ;
			
			if (offset<0){
				offset=0;
			}
			if (offset>str.length()-1){
				return positions;
			}
			
			positions = new int[n] ; 
			for (int i=0;i<n;i++){ positions[i] = -1 ;}
			
			for (int i=0;i<n;i++){
				
				dpart = delimiters.substring(i, i+1);
				p = str.indexOf(dpart) ;
			
				if (p>=offset){
					positions[z] = p;
					z++;
				}
			} // i->
			
			Arrays.sort(positions) ;
			
		}catch(Exception e){
			positions = new int[0];
			e.printStackTrace();
		}
		
		return positions;
	}
	
	/**
	 * <p>
	 * returns the first index (beyond offset position) for all supplied words;</br>
	 * if there are many further positions of one of the words beyond the first after the offset positino, they
	 * will not be shown!</p> 
	 * 
	 * @param str
	 * @param words
	 * @param offset
	 * @param collectAll
	 * @return
	 */
	public int[] indexesOfStrings( String str , String[] words, int offset, boolean collectAll ){
		int[] positions = new int[0];
		int n, p,z=0;
		String dpart;
		ArrayList<Integer> pos = new ArrayList<Integer>();
		try {
			n = words.length;

			if (offset<0){
				offset=0;
			}
			if (offset>str.length()-1){
				return positions;
			}
			/*
			positions = new int[n];
			for (int i = 0; i < n; i++) {
				positions[i] = -1;
			}*/

			
			for (int i = 0; i < n; i++) {

				dpart = words[i];
	 			
				p=-3;
				while ((p>=0) || (p==-3)){
					p = str.indexOf(dpart,p+1);

					if (p >= offset) {
						pos.add(p);
						if (collectAll==false){
							break;
						}
					}
					 
				}
			} // i->
			positions = changeArrayStyle(pos,0);
			Arrays.sort(positions) ;
			
		} catch (Exception e) {
			positions = new int[0];
			e.printStackTrace();
		}

		
		
		return positions;
	}

	/**
	 * 
	 * returns all positions (beyond offset position in variable "str") of all supplied words
	 * 
	 * @param str
	 * @param words
	 * @param offset
	 * @param collectAll
	 * @return
	 */
	public int[] indexesAllOfStrings( String str , String[] words, int offset, boolean collectAll ){
		int[] positions = new int[0];
		int n, p,z=0;
		String dpart;
		ArrayList<Integer> pos = new ArrayList<Integer>();
		try {
			n = words.length;

			if (offset<0){
				offset=0;
			}
			if (offset>str.length()-1){
				return positions;
			}
			/*
			positions = new int[n];
			for (int i = 0; i < n; i++) {
				positions[i] = -1;
			}*/

			
			for (int i = 0; i < n; i++) {

				dpart = words[i];
				
				p=0;
				while (p>=0){
					p = str.indexOf(dpart,p+1);

					if (p >= offset) {
						pos.add(p);
					}
					 
				}
			} // i->

			positions = changeArrayStyle(pos,0);
			Arrays.sort(positions) ;
			
		} catch (Exception e) {
			positions = new int[0];
			e.printStackTrace();
		}

		
		
		return positions;
	}
	
	public int indexOfStrings( String str , String[] strings ){
		
		return indexOfStrings( str , strings, 0, -1, false) ;
	}

	public int indexOfStrings( String str , String[] strings, int startpos ){
		
		return indexOfStrings( str , strings, startpos, -1, false) ;
	}

	
	public int indexOfStrings( String str , String[] strings, int startpos, int nth_occurence, boolean any){
		int currpos = -1;
		 
		int[] allpositions = new int[0];
		int pLen, sLen, dLen, p, i = 0, d, count;
		String particle;

		if ((strings==null) || (strings.length == 0)) {
			return -1;
		}
 
 
		try {
			
			allpositions = new int[strings.length];
			for (d = 0; d < allpositions.length; d++) {
				allpositions[d] = -1;
			}

			sLen = str.length();
			dLen = strings.length;
			count = 0;
			
			for (d = 0; d < dLen; d++) {
				
				if (!any){
					count=0;
				}
				
				particle = strings[d];

				pLen = particle.length();

				i = 0;
				p = 0;
				
				if (startpos>0){
					i = startpos;
				}
				
				while (i < sLen) {

					p = str.indexOf(particle, i);
						if (p<0){
							 
							p = str.indexOf(particle, i);
						}
						
					if (p >= 0) {
						count++; 
						i = i + pLen;
						if ((nth_occurence > 0)) {
							
							if (count >= nth_occurence){
								allpositions[d] = p;
								currpos = p ;
								break;
							}
							
						} else{
							allpositions[d] = p;
							currpos = p ;
							break;
						}
						
					} else {
						break;
					}
					i++;
				}

			} // d -> all particles

		} catch (Exception e) {
			count = -1;
			e.printStackTrace();
		}

		currpos = 999999 ;
		for (d=0;d<allpositions.length;d++){
			if ((currpos > allpositions[d]) && (allpositions[d]>=0)){
				currpos = allpositions[d];	
			}
			
		}
		     
		if (currpos >= 999999 ){
			currpos = -1;
		}
		
		return currpos;
	}
	
	public int indexOfparticles( String str , String[] delimiters ){
		
		return indexOfparticles( str, delimiters, 0, 0, true) ;
	}

	

	public int indexOfparticles( String str , String[] delimiters, int startpos ){
		
		return indexOfparticles( str, delimiters, startpos, 0, true) ;
	}		
	
	/**
		 * 
		 * returns the "next" = first position of the members of a set of delimiters
		 * 
		 * for all items in "delimiters" the positions will be determined, then
		 * the minimum of those positions will be selected as the result
		 * 
		 * @param str
		 * @param delimiters
		 * @param startpos
		 * @param nth_occurence
		 * @param any
		 * @return
		 */
	public int indexOfparticles( String str, String[] delimiters, int startpos,
								 int nth_occurence, boolean any) {
		 lastErr = -1;
		   
		
		int currpos = 0;
		int[] allpositions;
		int pLen, sLen, dLen, p, i = 0, d, count;
		String particle;

		if ((delimiters == null) || (delimiters.length == 0)) {
			return -1;
		}

		allpositions = new int[delimiters.length];
		for (d = 0; d < allpositions.length; d++) {
			allpositions[d] = -1;
		}

		try {

			sLen = str.length();
			dLen = delimiters.length;
			count = 0;
			                                     lastErr = -2;
			for (d = 0; d < dLen; d++) {

				if (!any) {
					count = 0;
				}

				particle = delimiters[d];

				pLen = particle.length();
				
				
				if (pLen==0){
					allpositions[d] = -1;
					continue;
				}
				
				i = 0;
				p = 0;

				if (startpos > 0) {
					i = startpos;
				}
			                                     lastErr = -3;
				while (i < sLen) {

					p = str.indexOf(particle, i);
					if (p < 0) {
						// particle = particle.replace("", "");
						// == not found -> -1 into the list of positions // p = str.indexOf(particle, i);
					}

					if (p >= 0) {
						count++;
						i = i + pLen;
						if ((nth_occurence > 0)) {

							if (count >= nth_occurence) {
								allpositions[d] = p;
								currpos = p;
								break;
							}

						} else {
							allpositions[d] = p;
							currpos = p;
							break;
						}

					} else {
						break;
					}
					i++;
				} // i < sLen
				i=0;
			} // d -> all particles
			                                     lastErr = -5;
		} catch (Exception e) {
			count = -1;
			e.printStackTrace();
		}
			                                     lastErr = -6;
		for (d = 0; d < allpositions.length; d++) {
			currpos = 999999;
			if ((currpos > allpositions[d]) && (allpositions[d] >= 0)) {
				currpos = allpositions[d];
			}

		}// ->
			                                     lastErr = -7;
		int maxPosVal = ArrUtilities.arraymax(allpositions) ;
		int posOfMin = ArrUtilities.arrayMinPos(allpositions,-1 ) ;
			                                     lastErr = -8;				
		    if (posOfMin>=0){
		    	currpos  = allpositions[posOfMin] ;  
		    }
		
		if (currpos >= 999999) {
			currpos = -1;
		}
		
		lastErr = 0;
		return currpos;
	}

	/**
	 * the result array contains 2 values
	 *    [0] = position;    ...refers to the position in the string
	 *    [1] = posOfMin;    ...refers to the position in the provided array of delimiters
	 * 
	 * @param str
	 * @param delimiters
	 * @return
	 */
	public int[] indexFullOfparticles( String str , String[] delimiters   ){
		return indexFullOfparticles( str, delimiters, 0);
	}
	public int[] indexFullOfparticles( String str , String[] delimiters, int startpos  ){
		return indexFullOfparticles( str, delimiters, startpos, 0, true) ;
	}
	public int[] indexFullOfparticles( String inStr , String[] delimiters, int startpos,
			 						   int nth_occurence, boolean any){
	
		int[] resultValues = new int[2];
		int currpos = 0;
		int[] allpositions;
		int pLen, sLen, dLen, p, i = 0, d, count;
		String particle,str = inStr;

		resultValues[0] = -1;
		resultValues[1] = -1;
		if ((delimiters == null) || (delimiters.length == 0)) {
			return resultValues;
		}

		if (startpos<0){
			startpos=0;
		}
		allpositions = new int[delimiters.length];
		for (d = 0; d < allpositions.length; d++) {
			allpositions[d] = -1;
		}

		try {

			sLen = str.length();
			dLen = delimiters.length;
			count = 0;

			if (startpos>0){
				str = str.substring(startpos,str.length());
			}
			
			for (d = 0; d < dLen; d++) {

				if (!any) {
					count = 0;
				}

				particle = delimiters[d];

				pLen = particle.length();
				
				
				if (pLen==0){
					allpositions[d] = -1;
					continue;
				}
				
				i = 0;
				p = 0;

				if (startpos > 0) {
					// i = startpos;
				}

				while (i < sLen) {

					p = str.indexOf(particle, i);
					if (p < 0) {
						// particle = particle.replace("", "");
						// == not found -> -1 into the list of positions // p = str.indexOf(particle, i);
					}

					if (p >= 0) {
						count++;
						i = i + pLen;
						if ((nth_occurence > 0)) {

							if (count >= nth_occurence) {
								allpositions[d] = p;
								currpos = p;
								break;
							}

						} else {
							allpositions[d] = p;
							currpos = p;
							break;
						}

					} else {
						break;
					}
					i++;
				} // i < sLen
				i=0;
			} // d -> all particles

		} catch (Exception e) {
			count = -1;
			e.printStackTrace();
		}

		currpos = 999999;
		for (d = 0; d < allpositions.length; d++) {
			if ((currpos > allpositions[d]) && (allpositions[d] >= 0)) {
				currpos = allpositions[d];
			}

		}
		int maxPosVal = ArrUtilities.arraymax(allpositions) ;
		// we have to exclude -1
		currpos = -1;
		int posOfMin = ArrUtilities.arrayMinPos(allpositions,-1 ) ;
			if (posOfMin>=0){
				currpos  = allpositions[posOfMin] ;  
			}
		
		if (currpos >= 999999) {
			currpos = -1;
		}
		resultValues[0] = currpos+startpos;
		resultValues[1] = posOfMin;
		
		return resultValues;
	}
	
	public int firstIndexOfStrings( String str , String[] strings, int startpos ){
		
		return -1;
	}
	
	public int firstIndexOfparticles( String str , String[] delimiters, int startpos ){
		
		return firstIndexOfparticles( str, delimiters, startpos, 0, true) ;
	}	
	
	public int firstIndexOfparticles( String str , String[] delimiters, int startpos, int nth_occurence, boolean any){
		int currpos = 0;
		int[] allpositions ;
		int pLen, sLen, dLen, p,  i = 0, d, count;
		String particle;

		if (delimiters.length == 0) {
			return -1;
		}

		allpositions = new int[ delimiters.length ] ;
		for (d=0;d<allpositions.length;d++){
			allpositions[d] = -1 ;
		}
		try {

			sLen = str.length();
			dLen = delimiters.length;
			count = 0;
			
			for (d = 0; d < dLen; d++) {
				
				if (!any){
					count=0;
				}
				
				particle = delimiters[d];

				pLen = particle.length();

				i = 0;
				p = 0;
				
				if (startpos>0){
					i = startpos;
				}
				
				while (i < sLen) {

					p = str.indexOf(particle, i);

					if (p >= 0) {
						count++; 
						i = i + pLen;
						if ((count >= nth_occurence) && (nth_occurence > 0)) {
							allpositions[d] = p;
							currpos = p ;
						} else{
							allpositions[d] = p;
							currpos = p ;
						}
					} else {
						break;
					}
					i++;
				}

			} // d -> all particles

		} catch (Exception e) {
			count = -1;
		}

		// should identify the first one...
		currpos = 999999 ;
		for (d=0;d<allpositions.length;d++){
			if ((currpos > allpositions[d]) && (allpositions[d]>=0)){
				currpos = allpositions[d];	
			}
			
		}
		     
		if (currpos >= 999999 ){
			currpos = -1;
		}
		
		return currpos;
	}
	
	
	public int lastIndexOfparticles( String str , String[] delimiters ){
		
		int currpos = 0;
		int[] allpositions ;
		int pLen, sLen, dLen, p,  i = 0, d;
		String particle;
	
		if (delimiters.length == 0) {
			return -1;
		}
	
		allpositions = new int[ delimiters.length ] ;
		for (d=0;d<allpositions.length;d++){
			allpositions[d] = -1 ;
		}
		try {
	
			sLen = str.length();
			dLen = delimiters.length;
			 
			
			for (d = 0; d < dLen; d++) {
				
				 
				
				particle = delimiters[d];
	
				pLen = particle.length();
	
				i = 0;
				p = 0;
				
				if (startpos>0){
					i = startpos;
				}
				
				while (i < sLen) {
	
					p = str.lastIndexOf( particle );
					if (p<0){
						p = str.lastIndexOf( particle.trim() );
						if (p<str.trim().length()-2){
							p=-1;
						}
					}
					if (p >= 0) {
						 
						i = i + pLen;
						 
						allpositions[d] = p;
						currpos = p ;
						 
						
					} else {
						break;
					}
					i++;
				}
	
			} // d -> all particles
	
		} catch (Exception e) {
			currpos = -1;
		}
	
		currpos = -1 ;
		for (d=0;d<allpositions.length;d++){
			if ((currpos < allpositions[d]) && (allpositions[d]>=0)){
				currpos = allpositions[d];	
			}
			
		}
		     
		 
		
		return currpos;
		
	}
	
	public boolean contains( String[] strs, String costr, int colength){
		Vector<String> strv;
		
		strv = new Vector<String>(Arrays.asList(strs));
		
		return contains( strv, costr, colength) ;
	}
	
	public boolean contains( Vector<String> strs, String costr){
		return contains( strs, costr, -1) ;
	}
	
	public boolean contains( Vector<String> strs, String costr, int colength){
		boolean rb=false;
		String str1,str2 ;
		
		for (int i=0;i<strs.size();i++){
			
			if ( colength>0){
				str1 = strs.get(i).substring(0,colength) ;
				str2 = costr.substring(0,colength) ;
				
				if (str1.contains(str2)){
					rb=true;
					break;
				}
			} else{
				
			}
			
		} // i-> all items
		
		return rb;
	}

	public boolean isMemberofSimpleSet( String syntag, Vector<String> iset){
		return isMemberofSimpleSet( syntag, iset, -1);
	}
	
	public boolean isMemberofSimpleSet( String syntag, Vector<String> iset, int startLen ){
		return isMemberofSimpleSet( syntag, iset, startLen,false );
	}
	/**
	 * we need a dedicated method for this check!
	 * indexOf() won't work, since we allow for part of tags, if the requested part is short
	 * 
	 * @param syntag
	 * @param iset
	 * @return
	 */
	public boolean isMemberofSimpleSet( String syntag, Vector<String> iset, int startLen, boolean strict ){
		boolean rb=false, hb;
		String str;
		
		for (int i=0;i<iset.size();i++){
			str = iset.get(i) ;
			
			hb = true;
			if (str.length()<= startLen){
				hb = syntag.startsWith(str);
			}else{
				if (strict){
					hb = str.contentEquals(syntag) ;
				}else{
					hb = str.contains(syntag) ;
					if (hb==false){
						hb = syntag.contains(str) ;
					}
				}
			}
			if (hb){
				rb = true;
				break;
			}
		} // i->
		
		return rb;
	}
	
	public int duplicateImmediateChars(String str){
		
		
		int n=0,k;
		char c1, c2;
		
		k = str.length();
		
		c1 = str.charAt(0);
		for (int i = 1; i < k; i++) {
			c2 = str.charAt(i);
			// Check if they are equal here
			if (c1 == c2){
				n++;
			}
		}
		
		return n;
	}

	/**
	 * this finds duplicate phrases, where phrase is known in advance
	 * @param str
	 * @param phrase
	 */
	public void duplicatePhraseInStrX( String str, String phrase ){
		
		java.util.regex.Matcher m;
		Pattern p ;
	
		int matches;
		// String val = "" ;
		
		// ......................................
		
		String duplicatePattern = "\\b(\\w+) \\1\\b";
		
		p = Pattern.compile(duplicatePattern);
		matches = 0;
		
		//phrase = " this is a test ";
		
		m = p.matcher(phrase);
		 
		while (m.find()) {
			// val = ":" + m.group() + ":";
			matches++;
		}
	}
	/*
	 * finding duplicates if a delimiter is known
	var str = 'hello hello hello world hello world world';
var myNewPattern = /(\w+)\s(?=\1)/g;
	*/
	
	public void setStringComparisonEngine( StringComparison strgcomp ) {
		
		this.strgcomp = strgcomp ;
	}
	
	public ItemResult getResultsSimCalc(int index){
		return getResultsSimCalc( index, 0) ;
	}

	@SuppressWarnings("unchecked")
	public ItemResult getResultsSimCalc(int index, int actioncode){
		ItemResult ir;
		if (actioncode>=0){
			ir = itemresults.get(index) ;
		}else{
			//ir = new ItemResult();
			ir = ((Vector<ItemResult>)itemresults.clone()).get(index) ;
			itemresults.remove(index) ;
		}
		return ir;
	}

	public static boolean matchSimpleWildCard(String compareThisSnip, String toFullString) {
		boolean rB=false;
		String compareThis = compareThisSnip.trim();

		if ((compareThis.endsWith("*")) && (compareThis.startsWith("*") )){
			compareThis = compareThis.replace("*", "");
			if (compareThis.length()==0){
				rB=true;
			}else{
				rB = toFullString.contains(compareThis) ;
			}
			return rB;
		}
		
		compareThisSnip=compareThisSnip.toLowerCase() ;
		toFullString = toFullString.toLowerCase() ;
		
		if (compareThis.startsWith("*") ){
			compareThisSnip = compareThisSnip.replace("*", "");
			rB = toFullString.endsWith(compareThisSnip) ;
			return rB;
		}
		if (compareThis.endsWith("*")){
			compareThisSnip = compareThisSnip.replace("*", "");
			rB = toFullString.startsWith(compareThisSnip) ;
			return rB;
		}
		
		// if *ABC -> match to the end, if *ABC* match anywhere, if ABC* match at the beginning 
		return rB;
		
	}

	public boolean matchSimpleWildcard(String compareThisSnip, String toFullString) {
		return matchSimpleWildCard(compareThisSnip, toFullString);
	}
	
	
	// this should run multi-threaded, using multi-digester
	public int mostSimilar( Vector<String> items, String compStr, int method, Map<String,Integer> bufferMap ){
		
		int result = -1,s=0 ;
		String label;
		double simval, max=-1.0;
		int ix=-1;
		ItemResult itemresult = new ItemResult();
		
		// new index of this result item
		// return this index
		
		if ((items.size()<=0) || (compStr==null) || (compStr.length()<=1)){
			return -1;
		}
		
		itemresult.index = -3;
		
		try{
			
			try{
			if ( (s == s-1) && (bufferMap!=null) && (bufferMap.size()>0)){
				
				if (bufferMap.containsKey(compStr)){
					
						ix = bufferMap.get(compStr);

						label = items.get(ix);
						simval = strgcomp.LevenshteinSimilarity(compStr, label);

						itemresult.index = ix;
						itemresult.itemlabel = label;
						itemresult.value = simval;
					}
			}
			
			}catch(Exception e){
				e.printStackTrace();
			}
			
			if ((ix<0) || (itemresult.index > 0)){
				ix = -3 ;
				s=0;
				if (itemresult.index > 0){
					s = ix;
				}
				
				for (int i=0;i<items.size();i++){
					
					label = items.get(i);
					
					if (label != compStr ){
						
						simval = strgcomp.LevenshteinSimilarity(compStr, label) ;
						
						if (simval>=0){
						
							if (max<simval){
								max = simval;
								itemresult.index = i;
								itemresult.itemlabel = label ;
								itemresult.value = simval ;
							}
							
						}// simval result ?
						
						
					} // label != compStr ??

				} // i->
			}
			
			if (ix>=0){
				
			}
			
			
			if (itemresult != null){
				itemresults.add(itemresult);
				result = itemresults.size()-1 ;
				
				if ( (ix==-3) && (compStr!=null) && (compStr.length()>0) && (itemresult!=null) && (itemresult.index>=0)){
					// bufferMap.put(compStr, itemresult.index );
					/* cross-wise !!
 					 * 1. A.sim = 0.0001 == no match
					 * 2. B = A = 0.71   == match 
					 * =>
					 * if A is still 0.0001 -> A.index -> B 
					 */
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result ;
	}
	
	public String replaceAll( String str, String startParticle, String endParticle, String replacement){
		String rStr, bstr ;
		bstr = str;
		rStr = bstr ;
		boolean done=false;
		int z,sLen,p1,p2 ;
		
		
		z=0; sLen = str.length() ;
		 

		while (!done){
			
			if (startParticle.length()>0){
				p1 = rStr.indexOf(startParticle) ;
			}else{
				p1=0;
			}
			p2= -1;
			
			if (p1>=0){
				if (startParticle.length()>0){
					p2 = rStr.indexOf(endParticle,p1+startParticle.length()) + endParticle.length() ;
				}else{
					p2= rStr.length();
				}
				
			}
			
			if ((p1>=0) && (p2>p1) && (p2<=rStr.length())){
				str = remove( rStr, p1, p2) ;
				if (replacement.length()>0){
					str = this.insert(str, replacement, p1);
				}
			}
			 
			if ( ( str.length() == rStr.length()) ||
				 ( str.contentEquals(rStr)==true) ) {
				done = true;
			} else{
				rStr = str;
			}
			z++;
			if (z>sLen){
				break ;
			}
		}
		
		
		return rStr;
	}
	
	public static String replaceall( String str, String searchfor, String byThat ){
		return replaceall( str,searchfor, byThat,0);
	}
	public static String replaceall( String str, String searchfor, String byThat,int relaxedMode){

		String rStr = str;
		boolean done=false, twophaseRepl;
		int z,p,sLen;
		String originalReplacement ;
		 
		
		
		if ((str==null) || (searchfor.length()==0)){
			if (str==null){
				rStr = "";
			}
			return rStr;
		}
		
		z=0; sLen = str.length() ;
		
		int casesMode = 0; // -> all of byThat -> upper case
		if (relaxedMode>=1){ casesMode=2;
		if (isCharUpperCase(str, 0)){
			casesMode=1;   // // -> all of byThat -> lower case
		}
		}
		originalReplacement = byThat;
		twophaseRepl =  (byThat.indexOf(searchfor)>=0) ; // (searchfor.trim().indexOf(byThat.trim())>=0) || 
			
		if (twophaseRepl){
			byThat = "#<###>#";
		}
		
		while (!done){
			
			str = rStr.replace(searchfor, byThat);
			// 
			if ((relaxedMode>=1) && (str.contentEquals(rStr))){
				if (casesMode==2){
					str = rStr.replace(searchfor, searchfor.toUpperCase());
				}
				if (casesMode==1){
					str = rStr.replace(searchfor, searchfor.toLowerCase());
				}
			}
			rStr = str  ;
			
			p = str.indexOf(searchfor); 
			if (p<0){
				done = true;
			}
			
			z++;
			if (z>sLen){
				break ;
			}
		}
		
		if (twophaseRepl){
			byThat = "#<###>#";
			rStr = replaceall( rStr, byThat, originalReplacement) ;
		}

		return rStr ;
	}
	
	public String replaceAll( String str, String searchfor, String byThat, int relaxedMode){
		return replaceall(str, searchfor, byThat,relaxedMode); 
	}
	public String replaceAll( String str, String searchfor, String byThat){
		
		return replaceall(str, searchfor, byThat,0);
		
	}
	
	
	public String replaceAll( String str, String[] searchfors, String byThat, boolean trimmedParticles){
		String rStr = str, searchfor;
		boolean done=false;
		int z,sLen,dLen,d  ;
		
		
		if (searchfors.length==0){
			return str;
		}
	
		
		try{
			 
			sLen = str.length() ;
			dLen = searchfors.length ;
			
			for (d=0;d<dLen;d++){
				
				searchfor = searchfors[d] ;
				

				if (searchfor.length()==0){
					return rStr;
				}
				
				done=false;
				z=0; sLen = str.length() ;
				
				while (!done){
					
					str = rStr.replace(searchfor, byThat);
					
					if ((trimmedParticles) && (str.length()==rStr.length())){
						str = rStr.replace(searchfor.trim(), byThat);
					}
					
					if (str.length()!=rStr.length()){
						rStr=str;
						
					} else{
						rStr=str;
						done=true;
					}
					z++;
					if (z>sLen){
						break ;
					}
				}
				
				

			} // d -> all searchfors
			
		}catch(Exception e){
			
		}
		
		
		return rStr ;
	}
	
	
	public String collapse( Vector<String> items, String separator){
		String str ="";
		String istr;
		
		for (int i=0;i<items.size();i++){
			istr = items.get(i);
			if ( (istr!=null) && (istr.length()>0)){
				str = str + istr ;
				if (i<items.size()-1){
					str = str + separator;
				}
			}
		}
		
		return str ;
	}

	
	public String collapse( String[] items, String separator){
		String str ="";
		String istr;
		
		for (int i=0;i<items.length;i++){
			istr = items[i];
			if ( (istr!=null) && (istr.length()>0)){
				str = str + istr ;
				if (i<items.length-1){
					str = str + separator;
				}
			}
		}
		str = trimm(str,separator);
		return str ;
	}

	
	
	public String collapse( StackTraceElement[] items, String separator){
		String str ="";
		String istr;
		
		for (int i=0;i<items.length;i++){
			istr = "line "+items[i].getLineNumber() + " "+ items[i].toString() ;
			if ( (istr!=null) && (istr.length()>0)){
				str = str + istr ;
				if (i<items.length-1){
					str = str + separator;
				}
			}
		}
		str = trimm(str,separator);
		return str ;
	}
	
	
	
	public String dehyphenize(String textstr, String[] hyphens, String[] protectedPrefixes) {
	
		String diag ,text = textstr;
		int p,p1,p2;
		String leftC,rightC;
		String[] rightS = new String[2] ;
		
		// hyper self meta pre post non a 
		
		try{
	
			// pattern :
			// X"- " -> "" if X not a blank, and on both sides a char (not a num)
			// [any char except blank]-[opt. blank][anychar except num]
			p = text.indexOf("-");
			p1=0; p2=-1;
															if (displayCompletion!=null){
																displayCompletion.setCompletionPercentage( (double)(0.0)/(double)(text.length()));
															}
			while ((p>1) && (p<text.length()-2)){
				
															if (displayCompletion!=null){
																displayCompletion.setCompletionPercentage( (double)(p)/(double)(text.length())); 
															}
				p2=-1;
				p = text.indexOf("-", p1) ; // returns
				if (p>0){
					p2 = text.indexOf("-", p+1) ; 
				}
				// get surround
				if (p<0){
					break;
				}
				diag = "";
				if ((p>5) && (p<text.length()-6)){
					diag = text.substring(p-4,p+5) ;
				}
				
				if ((p2>p1) && (p2-p1<=8)){
					// future-in-the-present, 
					// we should replace them by blanks ...
					p1=p2+1;
					continue;
				}
				leftC  = text.substring(p-1,p) ;
				rightC = text.substring(p+1,p+2) ;
				rightS[0] = rightC;
				rightS[1] = text.substring(p+2,p+3) ;
				
				// 
				int rr = 0;
				if (isAlphaChar( leftC )){
					if (isAlphaChar( rightC)){
						rr = 1;
					}else{
						if (rightC.contentEquals(" ")){
							if (isAlphaChar(rightS[1])){
								rr=2;
							}
						} // ?
					}
				} // ?
				if (rr>0){
					// check for protected prefixes
					
					text = remove(text,p,p+rr) ;
					p=p-1;
					
					if ((p>5) && (p<text.length()-6)){
						diag = text.substring(p-8,p+8) ;
					}
					rr=0;
				}
				p1=p+1;
				
			} // ->
			
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		return text;
	}

	
	/**
	 * 
	 * String[] 
	 * 
	 * @param str
	 * @param delimiters
	 */
	public ArrayList<XMap>  splitStringby( String str, String[] delimiters ){
		return splitStringby( str, delimiters, new String[]{},false,false);
	}
	
	/**
	 * 
	 * @param str              the text
	 * @param delimiters       String[] splitting delimiters
	 * @param shownDelimiters  String[] of delimiting items that will be kept, is a subset of the set of splitting delimiters 
	 * @param useSepecialMark  true: uses $D$ before the delimiter for unique identification and more convenient extraction
	 * @return
	 */
	public ArrayList<XMap>  splitStringby( String str, 
										   String[] delimiters, String[] shownDelimiters, 
										   boolean keepEmpty,
										   boolean useSepecialMark ){
		 
		ArrayList<String> delimsList = new ArrayList<String>(Arrays.asList( delimiters ));
		ArrayList<String> shownDelimList = new ArrayList<String>(Arrays.asList( shownDelimiters ));
		
		
		ArrayList<XMap> parts= new ArrayList<XMap>();
		XMap xm;
		
		String temp,sc1,sc2,sc3 ,sdel, shownDelimiter, effectiveDelimiter = "", origStr="";
		int[] pps = new int[]{-1};
		int p = 0,p0,c, dp ;
		int delimPos ;
		boolean done=false,hb ;
		
		origStr = str;
		
		try{
			p0=0;
			if (str.length()==0){
				return parts;
			}
			str = str.trim(); 
			//remove all delimiters from beginning or end of str
			
			for (int i=0;i<delimiters.length;i++){
				str = trimm(str, delimiters[i]) ;
			}
			
			while (!done){
				                       // callback for percentage done
									   if (displayCompletion!=null){
										   displayCompletion.setCompletionPercentage( ((double)p)/(double)str.length()) ;
									   }
				 
				// p   = indexOfparticles( str , delimiters ,p0 ) ; // delivers the first pos-value across all particles ???
				pps = indexFullOfparticles( str , delimiters ,p0 ) ;
				p = pps[0] ;
					dp = pps[1] ; 
					shownDelimiter="";
					if (dp>=0){ 
						sdel = delimiters[dp];
						dp = shownDelimList.indexOf(sdel) ;
						if (dp>=0){
							shownDelimiter = shownDelimList.get(dp) ;
						}
					}
					
					
				if (pps.length>=2){
					delimPos = pps[1] ;
					if (delimPos>=0){
						effectiveDelimiter = delimiters[delimPos] ;
					}else{
						p=-1;
					}
				}else{
					p=-1;;
				}
				// we need the index of the delimiter in the array of delimiters as well...
				// pfi = firstIndexOfparticles( str , delimiters,p0 ) ;
				
				if ((p>=1) && (p<str.length()-1)){
					// check: NOT for numericals !!!
					// check left-hand and right-hand
					sc1="";sc2="";sc3="";
					sc1 = str.substring(p-1,p);
					if (p+2<str.length()){
						sc2 = str.substring(p+1,p+2);
						if (p+3<str.length()){
							sc3 = str.substring(p+2,p+3);
						}
					}
					
					
					hb = (this.isNumericX(sc1)) && (sc3.length()>0) && (isNumericX(sc3)) ;
					hb = hb && (sc2.contentEquals(" ")) ;
					
					if ( ((isNumericX(sc1)) && (sc2.length()>0) && (isNumericX(sc2))) ||
						 (hb==true)	
					   ){
						 // if it is numeric we should not split it !!
						if (hb){
							str = str.substring(0,p+1) + str.substring(p+2,str.length());
						}
						try{ 
							
							p = indexOfparticles( str , delimiters ,p+2 ) ;
							
						}catch(Exception e){
							String estr = str;
							int z = Math.min(str.length(),50);
							estr = estr.substring(0,z);
							System.out.println("splitting string failed at pos "+(p+2)+"+ \n"+
									           "string was : "+estr);
							p=p+2;
						}
					}
				}
				// the actual split...
				if (p>0) {
					c=0;
					
					if (p0==0)c=1;
					if (p>=str.length()-1){
						c=0;
					}
					int pb=p0;
					if (p0<0)p0=0;
					
					temp = str.substring(p0,p+c).trim() ;
					if ((effectiveDelimiter!=null) && (effectiveDelimiter.length()>0)){
						p0 = p+c+ effectiveDelimiter.length() - 1;
					}else{
						p0=-1;
					}
					if ((p0==p) && (p0>0)){
						p0=p+1;
					}
					
if (temp.length()<=1){
	p=p+1-1;
}
if (temp.contains("staggers")){
	p=p+1-1;
}
					
					
					hb = (temp.length()>0) && (delimsList.indexOf( temp )<0); 
					
					if ((hb==false) && (keepEmpty==true)){
						String putativeDelimChar = str.substring(p,p+1);
						if (delimsList.indexOf( putativeDelimChar )>=0){
							hb=true;
							temp=" "+putativeDelimChar;
						}
					}
					if (hb){
						// System.out.println(" --- "+temp);
						xm = new XMap( parts.size(), pb, temp );
						if ((shownDelimiters!=null) && (shownDelimiters.length>0) && (shownDelimiter.length()>0)){
							String spm = "";
							if (useSepecialMark){
								spm = "$D$";
							}
							xm.str = xm.str + spm + shownDelimiter;
							//  

						}
						parts.add( xm)  ;
					}
					 
				} else{
					done=true;
					
					temp = str.substring( p0, str.length() ).trim() ;
										// System.out.println(" --- "+temp);
					xm = new XMap( parts.size(), p, temp );
					parts.add( xm) ;
					 
				}
				if (p0>str.length()-1){
					done=true;
				}
			} // -> done !
			
			
		}catch(Exception e){
			int z = Math.min(origStr.length(),200);
			String estr = "a problem occured while splitting a string... \n"+
			              "source string          : "+origStr.substring(0,z).replace("\n", "")+" \n"+
			              "parts collected so far : "+parts.size()+"\n"+
			              "last position roughly  : "+pps[0]+"\n"+
			              "presumable error code  : "+lastErr;
			PrintLog.Print(1, estr);
			e.printStackTrace();
		}
		
		return parts;
	}
	
	

	public String[] splitString(String str,
	                            int trimemptyslots,
	                            int removeemptyinner){
		int i;
		boolean done=false;
		String hs1;
		String[]  _arr,return_value;
		Vector<String> _vs=new Vector<String>();
		
		
		
		if ((str==null) || (str.length()==0)){
			return null;
		}
		_arr = str.split("\t");
		
		_vs.setSize(0);

		for (i=0;i<_arr.length;i++){
			_vs.add(_arr[i]);
		}
		_vs.trimToSize();

		i=1;
		while (done==false){
			
			if ((i==1) && (trimemptyslots==1)){
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-1; 
				}
			}
			if ((i==_vs.size()-1) && (trimemptyslots==1)){ 
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-2; 
				}
			}
			if ((i>0) && (i<_vs.size()-1) && (removeemptyinner==1)){ 
				hs1 = _vs.get(i);
				if ((hs1!=null) && ( hs1.length()>0)){hs1=hs1.trim();}
				if ((hs1!=null) && (hs1.length()==0 )){
					_vs.remove(i);
					i=i-2; 
				}
			}
				
			if (i>=_vs.size()){ done=true;}
			i=i+1;
			if (i<0){i=0;}
		}
		
		return_value=new String[_vs.size()];
		
		for (i=0;i<_vs.size();i++){
			if (_vs.get(i)!=null){
			return_value[i] = _vs.get(i);
			}
			else{
				return_value[i] = "";	
			}
		}
		
		
		return return_value;
	}
	
	/**
	 * works similar as StringUtils.substringsBetween();  
	 * However, substringsBetween() of lang3 of Apache stuff does NOT work correctly, if there
	 * is a further structure (== a table) nested into the cell, which also contains &lt;/td&gt;
	 * since we may have already a correctly extracted table (which may contain another one)
	 * we need the embracing pairs of td !!!
	 * 
	 */
	public String[] substringsBetweenEmbracingPair( String istr, String open, String close, int matchingMode) {
	
		return substringsBetweenEmbracingPair( istr, open, close, matchingMode,false) ;
	}

	public String[] substringsBetweenEmbracingPair( String istr, String open, String close, int matchingMode, boolean regardUnfinshedClosings) {
		String[] closes = new String[1];
		
		closes[0] = close;
		
		return substringsBetweenEmbracingPair( istr, open, closes, matchingMode, regardUnfinshedClosings) ;
	}
	
	
	public String[] substringsBetweenEmbracingPair( String istr, String open, String[] close, 
			 										int matchingMode, 
			 										boolean regardUnfinshedClosings) {
		
		String[] substring = new String[0];
		Vector<String> substrings = new Vector<String> ();
		String str,xstr ,strc="",lastExtractAdded="";
		int i,d=0,p0,p1,p2=-1,pe,pno,z,zz,lx,x,pc,nb=-1,ne=-1,c,maxL,minL;
		boolean ok;
		int[] ppca,pp2,ppc;
		
		
		
		str = istr;
		p1=1; z=2; p2=0;
		
		try{
			substrings.clear();
			p1 = str.indexOf(open,p2);
		
			maxL=-1; minL=999999;
			for (i=0;i<close.length;i++){
				z = close[i].length();
				if (z>maxL){
					maxL=z;
				}
				if (z<minL){
					minL=z;
				}
			}
		if (Arrays.asList(close).contains(open)){d=1;};	
		z=0;
		p1 = str.indexOf(open);
		ppca = indexesOfStrings(str, close, p1+1,true);
		
		while ((p1>=0) &&(z<istr.length()+5)){
			
			
			p2=-1;
			
			if (p1>=0){
				if (matchingMode==0){
					p2 = this.indexOfStrings(str, close,p1+1); d=0;
					// p2 =  nums.arrayMin(pp2, -1) ;
					// p2 = str.indexOf(close,p1) ;
				}
				if (matchingMode==1){
					ok=false;
					pc = p1;
					zz=0; nb=0;ne=0;
					// we have to care for a situation like this one:
					// open open close close
					while ((ok==false) && (zz<nb+ne+2)){
						pc = 9999; 
						// first, we look for the next close...
						pc = this.indexOfStrings(str, close,p1+d);
						
						// pc = str.indexOf(close,pc+1) ; <= original, a single string
						// then we check, whether the open preceding that close is the same as
						// the one we started with...
						p0 = this.indexOfBefore(str, open, pc-1);
						
						// if we have reached the last potential snip ...
						if (pc<0){
							if (regardUnfinshedClosings){
								// is there any opening hereafter?
								pno = str.indexOf(open,p1+2);
								if (pno<0){
									// ok, we have reached the last potential snip, yet, the close tag is missing -> we fake it
									// since we are allowed to do (by regardUnfinshedClosings)
									pc = str.length() ;
									p0 = this.indexOfBefore(str, open, pc-1);
								}
							}
						}
						// if not, we have to continue searching for a matching close
						if ((pc>0) && (pc>p1)){
							// actually, we first have to find out, which of the closes is at position pc!!
							c=0;
							pe = pc + maxL;
							if (pe>str.length()){
								pe=str.length();
							}
							strc = str.substring(p1,pe );
							nb = frequencyOfStr(strc, open);
							
							ne=0;
							for(int f=0;f<close.length;f++){
								ne = ne + frequencyOfStr(strc, close[f]);
							}
							
						}else{
							ok=true;
						}
						
						if (d==0){
							if ((nb<0) || (nb==ne)){
								ok=true;
							}
						}else{
							if ((nb<0) || (ne-ne<=d)){
								ok=true;
							}
						}
						strc=strc+"";
						zz++;
					} // while ok ?
					p2=pc;
				}
				if (matchingMode==2){
					lx=-1;
					for(int f=0;f<close.length;f++){
						p2 = str.lastIndexOf(close[f]) ;
						if (p2>lx){
							lx=p2;
						}
					}
					p2=lx;
				} 
				
				if ((p2>0) && (p2>p1)){
					x = maxL;
					if (d>0){
						x=0;
					}
					xstr = str.substring(p1,p2 + x); // close.length());
					if ((xstr.length()>3) && (lastExtractAdded.length()>3)){
						if (lastExtractAdded.contentEquals(xstr)){
							xstr="";
							p2=-3; // this will break both loops
						}
					}
					if (xstr.length()>0){
						substrings.add(xstr);
						lastExtractAdded = xstr;
						
					}
					if (matchingMode==2){
						str = str.substring(0,p2-1-(maxL-minL));
					}
				}
			}
			z++;
			x=0;
			if (d>0){
				x=2;
			}
			p1=-3;
			if (p2>0){
				p1 = str.indexOf(open,p2+1-x);
			}
		} // while anything found ?
		
		
		
		substring = changeArrayStyle( substrings ); 
		
		}catch(Exception e){
			System.out.println(istr+" "+open+" "+ close);
			e.printStackTrace();
		}
		return substring;
	}
	
	
	public String removeStringsBetweenEmbracingPairs( String istr, String open, String close, int count){
		
		
		boolean done = false;
		int p1,p2,ps1=0,ps2=0,pe1=0,pe2=0;
		int cc=0;
		
		String[] finst = substringsBetweenEmbracingPair( istr, open, close, 2) ;
		if (finst.length==0){
			finst = substringsBetweenEmbracingPair( istr, open.toUpperCase(), close.toUpperCase(), 2) ;
		}
		
		if ((finst!=null) && (finst.length>0)){
			
			for (int i=0;i<finst.length;i++){
				istr = istr.replace( finst[i], "");
				cc++;
				if ((count>0) && (cc>=count)){
					break ;
				}
				
			} // ->
			
		}
		
		return istr;
	}
	
	public String[] changeArrayStyle( Vector<String> strvec  ){
		String[] strarr ;
		 
		strarr = new String[strvec.size()];
		if (strvec.size() > 0) {
			for (int i = 0; i < strvec.size(); i++) {
				strarr[i] = strvec.get(i);
			}
		}

		return strarr ;
	}
	
	public Vector<String> changeArrayStyle( String[] strarr  ){
		Vector<String> vs =  new Vector<String> () ;
		 
		if ((strarr!=null) && (strarr.length>0)){
			vs = new Vector<String> (Arrays.asList(strarr));
		}

		return vs ;
	}
	
	public int[] changeArrayStyle( ArrayList<Integer> pos  , int v){
		int[] intarr ;
		 
		intarr = new int[pos.size()];
		if (pos.size() > 0) {
			for (int i = 0; i < pos.size(); i++) {
				intarr[i] = pos.get(i);
			}
		}

		return intarr ;
	}
	
	public double[] changeArrayType( String[] vstr , double defaultValue, boolean blockEmptyItems ){
		
		double[] vdarr = new double[0];
		
		Vector<Double> vdo = new Vector<Double>();
		double v = defaultValue;
		String str;
		
		for (int i=0;i<vstr.length;i++){
			
			str = vstr[i].trim() ;
			
			if (str.length()==0){
				if (blockEmptyItems){
					continue;
				}
				v = defaultValue;
			}else{
				v = Double.parseDouble( str );
			}
			
			vdo.add(v);
		}
		
		
		 
	    vdarr = new double[ vdo.size() ];
	    for (int i=0;i<vdarr.length;i++){
	    	vdarr[i] = vdo.get(i) ;
	    }
	    
		return vdarr;
	}
	
	public Vector<Long> changeArrayType( Vector<String> vstr , long defaultValue, boolean blockEmptyItems ){
		
		Vector<Long> vlo = new Vector<Long>();
		long v = defaultValue;
		String str;
		
		for (int i=0;i<vstr.size();i++){
			
			str = vstr.get(i).trim() ;
			
			if (str.length()==0){
				if (blockEmptyItems){
					continue;
				}
				v = defaultValue;
			}else{
				v = Long.parseLong( str );
			}
			
			vlo.add(v);
		}
		return vlo;
	}
	
	public String insert( String str, String insertion, String pattern, int relativeposition) {
		
		return str ;
	}
	
	public String insert( String str, String insertion, int position) {
		String rstr = str;
		String s1 ="",s2;
		
		if (position < str.length()) {
			s1 = str.substring(0, position);
			s2 = str.substring(position , str.length());

			rstr = s1 + insertion + s2;
		} else {
			rstr = str + insertion;
		}
		return rstr ;
	}
	
	public String internalCleavage( String istr){
		String str = istr;
		char[] chars;
		
		chars = str.toCharArray(); 
		str = String.valueOf(chars);
		
		return str;
	}
	
	public String clean( String str, int level){
		String rStr = str;
		boolean done=false;
		int z,sLen,p;
		char[] chars ;

	
		
		str = replaceAll(str,"\n", " ") ; 
		str = replaceAll(str,"\r", " ") ; 
		str = replaceAll(str,"\t", " ") ; 
		
		
		str = replaceAll(str," ,", ",") ; 
		str = replaceAll(str,",", ", ") ;
		str = replaceAll(str," .", ".") ; 
		str = replaceAll(str,".", ". ") ;
		str = replaceAll(str,":", ": ") ;
		str = replaceAll(str,"!", "! ") ;
		
		str = replaceAll(str," -", "-") ; 
		str = replaceAll(str,"  ", " ") ; 
		
		chars = str.toCharArray();
		
		// remove "words" of length 1
			
			for (p=0;p<chars.length-2;p++){
				
				if ((chars[p]==' ') && (chars[p+1]!=' ') && (chars[p+2]==' ')){
					// chars[p+1] = ' ';
				}
			} // p-> all chars  
		
		str = String.valueOf(chars);
		rStr = str ;
		// 
		str = replaceAll(str,"-", " - ") ; 
		
		
		if (level>=2){
			// all chars outside [32 .. 180]
		}
		
		String[] searchfor = { "---", "  ", "&" };
		String byThat = "";
	 	
		
		rStr = rStr.replace(" & ", " and ");
		
		str = replaceAll(str,"  ", " ") ; 
		
		z=0; sLen = str.length() ;
		
		
		for (p=0;p<searchfor.length;p++){
			while (!done){
				
				str = rStr.replace(searchfor[p], byThat);
				
				
				
				if (str.length()!=rStr.length()){
					rStr=str;
					
				} else{
					done=true;
				}
				z++;
				if (z>sLen){
					break ;
				}
			}
			
		} // p ->
		
		// all doubles, which are not letters...
		
		
		
		
		return rStr ;
		
	}
	
	public String cleanInternal( String str){
		char[] chs;
		
		chs = str.toCharArray();
		str = new String(chs);
		
		return str;
	}

	
	public String unaccentedStr( String str){
		
		return UnaccentedStr.convertNonAscii(str);
	}
	
	
	public boolean isCharUppercase(String str, int pos){
		return isCharUpperCase(str, pos);
	}
	
	/**
	 * 
	 * 
	 * @param str 
	 * @param pos
	 * @return
	 */
	public static boolean isCharUpperCase(String str, int pos){
		boolean rb=false;
		String firstChar ;
		char cc ;
		int code ;
		
		if (pos>str.length()-1){
			return rb;
		}
		
		firstChar = str.substring(pos,pos+1) ;
		
		cc = firstChar.charAt(0);
		code = (int)cc;
		
		if ( ((code>=64) && (code<=90)) ||
		     ((code>=96) && (code<=123)) ){
		// we return TRUE only, if we have a letter char	
			rb = (firstChar.contentEquals( firstChar.toUpperCase())) ;
			
		}
		
		
		return rb;
	}
	
	public String extractCapitals(String str){
		String xstr = "" ;

		try{

			for (int i=0;i<str.length();i++){
				if (isCharUpperCase(str, i)){
					xstr = xstr + str.substring(i,i+1) ;
				}
			}

		}catch(Exception e){
		}
		return xstr;
	}
	
	

	public boolean isNounCase( String str) {
		
		return isNounCase( str," ");
		
	}
	public boolean isNounCase( String str, String separator) {
		boolean rb= false;
		int n,p;
		String istr;
		
		istr = str.trim(); 
		if (separator.length()>0){
			p = istr.indexOf(separator) ;
			if (p>1){
				istr = istr.substring(0,p);
			}
		}
			
		n = (int)(istr.charAt(0));
		if ((n>=64) && (n<93)){
			rb = true;
		}

		
		return rb;
	}
	 
	
	
	/** sets the first character to uppercase */
	public String nounCases( String str, int onlyFirstN, String separator){
		String istr = str, ostr;
		String selectedStr, restStr;
		int p;
		
		
		if (onlyFirstN>0){
			p = this.indexOfNth(str,onlyFirstN, separator );
			p = str.trim().indexOf(separator); // as soon as the above is working , remove this line!
			if (p>0){
				selectedStr = str.substring(0,p);
				restStr = str.substring(p+1, str.length());
			}else{
				selectedStr = str ;
				restStr = "" ;

			}
			
			istr = selectedStr;
			ostr = selectedStr.substring(0,1).toUpperCase() + istr.substring(1, istr.length()).toLowerCase() ;
			
			ostr = ostr+" "+ restStr ;
			
		} else{
			
			ostr = str.substring(0,1).toUpperCase() + istr.substring(1, istr.length()).toLowerCase() ;
		}
		return ostr;
	}
	
	// e.g. necessary for hashes, or MD5 (length = 32)
	public String leftPad( String str, String padwith, int length){
		
	
		while(str.length() < length){
			str = padwith + str;
		}

		return str ;
	}

	public String rightPad( String str, String padwith, int length){
		
		
		while(str.length() < length){
			str =  str + padwith;
		}

		return str ;
	}
	 
	
	/**
	 * removes non-printable characters inside a string, printable= 31<ASCII<184
	 * 
	 * @param str
	 * @param replacement
	 * @return
	 */
	public String trimInside( String str, String replacement){
	
		String rStr = str,  s1;
		char chr;
		int i,chc;
		int[] frq = new int[6];
		
		char[] chars ;
		Vector<Integer> codes = new Vector<Integer>();
		 
		
		for (i=0;i<frq.length;i++){
			frq[i] = -1 ;
		}
		
		try{
			
			
			//  char c = (char)25;
			/*
			int i = 65;
			char c = (char)i;
	
			char c = 'A';
			int i = (int)c;
			
			char c = s.charAt(0);
			String s1 = Character.toString(c);
			
			*/
		  
			
			chars = rStr.toCharArray();
			
			for (i=0;i<chars.length;i++){
				
				chc = (int)chars[i];
				if ((chc<32) || (chc>184)){
					codes.add(chc) ;
				}
			}
			
			for (i=0;i<codes.size();i++){
				chr = (char)(int)(codes.get(i));
				
				s1 = Character.toString(chr) ;
				rStr = replaceAll( rStr, s1,replacement) ;
			
			}
			
			if (replacement.length()>0){
				rStr = replaceAll( rStr, replacement+replacement, replacement) ;
			}
			rStr = replaceAll( rStr," ", replacement) ;
			
		}catch(Exception e){
			
			
		}
		
		
		return rStr;
		
	}

	/*
	 /*
	     * To create a Pattern instance we must call the static method 
	     * called compile() in the Pattern class. Pattern object is 
	     * the compiled representation of a regular expression.
	      
	    Pattern pattern = Pattern.compile("lazy");
	
	    /*
	     * The Matcher class also doesn't have the public constructor 
	     * so to create a matcher class the Patter's class matcher() 
	     * method. The Matcher object it self is the engine that match 
	     * the input string against the provided pattern.
	      
	    Matcher matcher = pattern.matcher("The quick brown fox jumps over the lazy dog");
	
	    while (matcher.find()) {
	        System.out.format("Text \"%s\" found at %d to %d.%n",
	                matcher.group(), matcher.start(), matcher.end());
	    }
	
	*/
	
	public String trimInside( String str){
		return trimInside( str, "");
	}

	/*
	 /*
	     * To create a Pattern instance we must call the static method 
	     * called compile() in the Pattern class. Pattern object is 
	     * the compiled representation of a regular expression.
	      
	    Pattern pattern = Pattern.compile("lazy");
	
	    /*
	     * The Matcher class also doesn't have the public constructor 
	     * so to create a matcher class the Patter's class matcher() 
	     * method. The Matcher object it self is the engine that match 
	     * the input string against the provided pattern.
	      
	    Matcher matcher = pattern.matcher("The quick brown fox jumps over the lazy dog");
	
	    while (matcher.find()) {
	        System.out.format("Text \"%s\" found at %d to %d.%n",
	                matcher.group(), matcher.start(), matcher.end());
	    }
	
	*/
	public String trimtrailingzeroes(String str){
		return trimTrailingZeroes(str);
	}
	public static String trimTrailingZeroes(String Str){
		String  return_value;
		boolean done=false;
		
		return_value = Str.trim();
		
		while (done==false){
			
			if (return_value.endsWith("0")){
				return_value = return_value.substring(0, return_value.length()-1);
			}
			else{
				done=true;
			}
				
			
			
		}
		
		if (return_value.endsWith(".")){
			return_value = return_value.substring(0, return_value.length()-1);
		}
		if (return_value.endsWith(",")){
			return_value = return_value.substring(0, return_value.length()-1);
		}
		
		
		return return_value;
	}
	
	public String trimm( String str, String[] removals){
		String rem, rStr = str ;
		
		for (int i=0;i<removals.length;i++){
			rem = removals[i];
			rStr = trimm(rStr,rem);
		}
		
		return rStr ;
	}
	
	public String trimm( String str, String removal){
		String rStr = str ;
		boolean  done = false;
		int p1,p2,k;
		// int cc ;
		
		char c1 ;
		
		
		if (removal.length()==0){
			return rStr;
		}
		if (rStr.length()==0){
			return rStr;
		}
	
		str = str.trim() ;
		
		try{
	
			while (done==false){
				done = false;
				
				p1 = rStr.indexOf(removal);
	
				p2 = rStr.lastIndexOf(removal);
				
				if ((( p1<0 ) || (p1>0)) ){
					done = true;
					
					if (rStr.length()<=0){
						break ;
					}
					// ca = rStr.toCharArray() ;
					
					c1 = rStr.substring(0,1).charAt(0) ;
					// cc = (int)c1;
					
					if ((c1<32) || (c1>125)){
						rStr = rStr.substring(0,rStr.length());
					}
					
					k = rStr.length() - removal.length();
					if ( p2 != k){
						done = true;
						
					}else{
						if ((p2>=0) && (p2<rStr.length())){
							rStr = rStr.substring( 0, p2) ;
							done = false;
						}else{
							
						}
					}
				}else{
					if ((p1==0) && (rStr.length()== removal.length())){
						rStr = "";
					}else{
						rStr = rStr.substring( p1+removal.length(),rStr.length()) ;
					}
				}
				
				
			} // while ->
			
			
		}catch(Exception e){
			
		}
		
		if (rStr.startsWith(removal)){
			rStr = rStr.substring(removal.length(), rStr.length()) ;
		}
		if (rStr.endsWith(removal)){
			rStr = rStr.substring(0, rStr.length() - removal.length()) ;
		}

		return rStr ;
	}

	public String trimright( String str, String ending, String replacement){
		
		String rstr = str ;
		int p;
		if ((ending.contentEquals(replacement)) || (replacement.contains(ending))){ return str; }  
		p = 1;
		
		while ((p>0) && (rstr.length()>0)){
			p = rstr.indexOf(ending) ;
			if ((p>=rstr.length()-1-ending.length()) || (rstr.endsWith(ending))){
				if (p>0){
					
					rstr = rstr.substring(0,p);
					if (replacement.length()>0){
						rstr = rstr + replacement ;
					}
				}
			}else{
				p=-1;
			}
		}
		
		return rstr ;
		
		
		
	}
	
	public String trimright( String str, String ending){
		return trimright( str, ending,"");
	}
	
	/**
	 * 
	 * @param str
	 * @param regexPattern
	 * @param scope  either "r" or "L"
	 * @return
	 */
	public String trimX( String str, String regexPattern , String scope){
		String rstr = str,part;
		boolean found=true;
		
		
		while ((found==true) && (rstr.length()>=0)){
			found=false;
			
			if (scope.toLowerCase().trim().contentEquals("r")) {
				part = rstr.substring(rstr.length() - 1, rstr.length());
				part = part.replaceAll(regexPattern, "");
				if (part.length() == 0) {
					rstr = rstr.substring(0, rstr.length() - 1);
					found = true;
				}
			}

			if (scope.toUpperCase().trim().contentEquals("L")) {
				part = rstr.substring(0, 1);
				part = part.replaceAll(regexPattern, "");
				if (part.length() == 0) {
					rstr = rstr.substring(1, rstr.length() - 1);
					found = true;
				}
			}
		}
		
		
		return rstr;
	}


	public IniStyleContent getIniStyleFile( ArrayList<String> filecontent){
		
		if ((filecontent==null) || (filecontent.size()==0)){
			return new IniStyleContent("");
		}
		String filecontentstr = filecontent.toString() ;
		IniStyleContent inifile = new IniStyleContent(filecontentstr);
		 
		return inifile;
		
	}

	
	public IniStyleSections getIniStyleSections( Vector<String> textlines){
		IniStyleContent inifile = new IniStyleContent(textlines);
		IniStyleSections  iniSections ;

		iniSections = inifile.getIniStyleSections();
		
		return iniSections;
		
	}

	public IniStyleSections getIniStyleSections( String text){
		IniStyleContent inifile = new IniStyleContent(text);
		IniStyleSections  iniSections ;

		iniSections = inifile.getIniStyleSections();
		
		return iniSections;
	}

	public boolean isPercentageX(String str){
		boolean return_value = false;
		String regex_for_perc = "^\\s*(\\d{0,2})(\\.?(\\d*))?\\s*\\%?\\s*$" ;

		// this may interfere with "getRecordbyID()" !!
		try{
		
			str = str.replace(",",".");
			if ((str.matches(regex_for_perc)) && (str.length()>0)){
				return_value = true;
			}

		}
		catch(Exception e){
			return_value = false;
			//String errmsg = e.getStackTrace() ;
			System.out.println( e.getCause() ) ;
			System.out.println( e.getMessage() ) ;
			System.out.println( e.getStackTrace() ) ;
		}
		finally{
		
		}
		return return_value ;
	}
	
	public boolean isNumericX(Object obj ) {
		boolean return_value = false;
		String cn="" ;
		
		try{
		
			cn = obj.getClass().getSimpleName().toLowerCase() ;
			
			return_value = (cn.startsWith("int")) || (cn.startsWith("double") || cn.startsWith("float")) ;
			
			if (return_value==false){
				return_value = isNumericX((String)obj) ;
			}
			
		}catch(Exception e){
		}
		
		return return_value ;
	}
	
	public boolean isNumericX(String str){
		boolean return_value = false;
		String regex_for_num = "^[-+]?\\d+([.,]?\\d+)?$";

		// this may interfere with "getRecordbyID()" !!
		try{
		
			str = str.replace(",",".");
			if ((str.matches(regex_for_num)) && (str.length()>0)){
				return_value = true;
			}

		}
		catch(Exception e){
			return_value = false;
			//String errmsg = e.getStackTrace() ;
			System.out.println( e.getCause() ) ;
			System.out.println( e.getMessage() ) ;
			System.out.println( e.getStackTrace() ) ;
		}
		finally{
		
		}
		return return_value ;		
		
	}
	
	
	public boolean isNumeric( String str){
		boolean rb=false;
		int p,code;
		char cc;
		String chr ;
		
		if (str.length()==0){
			return rb;
		}
		try{
			
		 
			rb=true;
			
			for (p=0;p<str.length();p++){
				
				chr = str.substring(p,1) ;
				
				cc = chr.charAt(0);
				code = (int)cc;
				
				if ((code>=48) && (code<=57)) {
				
					// if (numstr.contains(chr) == false) { rb = false; 	break; } // is it contained in combined number-string?
					
				} // is the char a number digit?
				else{
					rb=false;
					break;
				}
			} // p-> all chars of string to test
		
		}catch(Exception e){
			rb=false ;
		} finally{
			
		}
			
		
		return rb;
	}
	
	public String numerize( double value, int digits){
		String numstr="";
	
		numstr = String.format("%."+digits+"f", value);
		
		numstr = StringsUtil.trimTrailingZeroes(numstr);
		
		return numstr;
	}
	
	public String booleanize( boolean flag){
		String bstr="";
		
		if (flag){ bstr="true";}else{bstr="false";}
		
		return bstr;
	}
	
	public ArrayList<String> extractNumsFromString(String str){

		ArrayList<String> numbers = new ArrayList<String>();

		Pattern p = Pattern.compile("\\d+"); // does not work for real values in string, just for simple integer 
		Matcher m = p.matcher(str); 
		while (m.find()) {
		   numbers.add(m.group());
		}

		return numbers;
	}
	
	
	public int getNumFromStr( String str, int defaultVal){
		int result= defaultVal;
		
		str = str.replaceAll( "[^\\d]", "" ) ;

		str = str.trim();
		str = trimm(str, "/n") ;
		
		if ((str.length()>0) && ( isNumericX(str)) ){
			result = Integer.parseInt(str) ;
		}
		
		return result ;
	}
	
	public long getNumFromStr( String str, long defaultVal){
		long result= defaultVal;
		
		str = str.trim();
		str = trimm(str, "/n") ;
		
		if ((str.length()>0) && ( isNumericX(str)) ){
			result = Long.parseLong(str) ;
		}
		
		return result ;
	}
	
	
	
	public boolean isPageNumber(String istr){
		boolean rb=false;
		String str = istr;
		String[] parts ;
		int vi1,vi2;
		
		str = str.replaceAll(" ", "") ;
		
		if ((str.length()==0) || (str.contains("."))){
			return rb;
		}
		
		if ( str.indexOf("-")<=0){
			str = str.replace("-", "");
			rb = (str.length()<=3) && (isNumericX(str)) ;
		}else{
			str = str.replaceAll("--", "-");
			
			parts = str.split("-") ;
			
			if (parts.length==2){
				rb = isNumericX(parts[0]) && isNumericX(parts[1]) ;
				if (rb){
					vi1 = Integer.parseInt(parts[0]) ;
					vi2 = Integer.parseInt(parts[1]) ;
					rb=false;
					if ((vi1>0) && ( vi1<vi2)){
						if ((vi1>1800) && (vi2<2050) && (vi2>1800) && (vi1<2050)){
							// it is most likely a years interval
						} else{
							rb = true;
						}
					}
				}
			}
		}
		
		
		return rb;
	}
	
	public boolean isUrl(String str) {
		boolean rB=false;

		if (str.length()<3){
			return rB;
		}
		str = str.trim().toLowerCase() ;
		
		rB = str.startsWith("http://");
		if (rB==false){
			rB = str.startsWith("www.");
		}
		if (rB==false){
			rB = str.contains("www") && (this.frequencyOfStr(str, ".")>=2);
		}
		if (rB==false){
			rB = str.contains("localhost") ;
		}
		if (rB==false){
			rB = isIpAddress(str);
		}

		
		return rB;
	}


	// TODO: use regex
	private boolean isIpAddress(String str) {
		// 
		boolean rB=false;
		
		
		return rB;
	}

	public boolean containsYearValue( String str){
		boolean rb=false;
		String regex, xs;
		 
		
		// regex
		 
		/*
				^([1-9]{1}[0-9]{3}[,]?)*([1-9]{1}[0-9]{3})$
				Description 	 Matches a comma-seperated list of year(s).
				Matches 	 1999,2001,1988 | 1999
				Non-Matches 0199,1997 | 0199 | 1999,
		*/
	
		regex ="^([1-9]{1}[0-9]{3}[,]?)*([1-9]{1}[0-9]{3})$" ;
		
		xs = str.replaceAll(regex, "") ;
		
		// hb = str.matches(regex) ;
		 
		rb = str.length() > xs.length() ;
		 
		
		return rb;
	}
	
	/*
	 
	 replace any repeats
	replace any 2+ same characters one after another with only 1 same character:

	String str = "assddffffadfdd..o"; -> "asdfadfd.o"
	str = str.replaceAll("(.)\\1+", "$1"));

	
	*/

	
	/**
	 * 
	 * applies a regex to the inputstr and replaces the desired part WITHIN the found snips by the replacement
	 * 
	 * 
	 * @param istr inputstr 
	 * @param expression regex
	 * @param thisPart within regex, replace this...
	 * @param byThat 
	 * @return
	 */
	public String replaceAllX( String istr, String expression, String thisPart, String byThat){
		String handledStr = istr;
		
		
		String[] matches = new String[0];
		String str = istr;
		Scanner sx = null ;
		MatchResult result ;
		int x, replaced=0,p1,p2,n,lastpos ;
		String foundInstance,newInstance ;
		boolean replacerX = false;
		boolean found ;
		
		try{
			 
			found = true;
			n=0;
			lastpos = -1;
			
			while (found == true) {

				found = false;

				str = handledStr;
				sx = new Scanner(str);
				sx.findInLine(expression);
				result = sx.match();
  
				p1 = result.start(n);
				p2 = result.end(n);

				if ((p2 - p1 > 0) && (p1 >= 0) && (p1>lastpos)) {
					str = handledStr.substring(p1, p2);

					if (thisPart.length()>0){
						str = str.replace(thisPart, byThat);
					}else{
						str = byThat;
					}
					
					handledStr = handledStr.substring(0, p1) + str + handledStr.substring(p2, handledStr.length());
					found = true;
					n++;
					lastpos = p2;
				}
			} // sth found ->

		} catch (Exception e) {

		}
		
		return handledStr ;
	}
	
	/**
	 *  NOT FUNCTIONABLE !!
	 *  
	 *  
	 * @param str
	 * @param expression
	 * @return
	 */
	public String[] regexScannerMatches( String istr, String expression, int leftOffset, int rightOffset, int desiredLen){
		
	
		
		String[] matches = new String[0];
		String handledStr = istr, str;
		Scanner sx = null ;
		MatchResult result ;
		int p1,p2,n ;
		  
		boolean found ;
		Vector<String> foundinstance = new Vector<String> ();
		
		try{
			 
			found = true;
			n=0;
			
			while (found == true) {

				found = false;

				str = handledStr;
				sx = new Scanner(str);
				sx.findInLine(expression);
				result = sx.match();
  
				p1 = result.start(n);
				p2 = result.end(n);

				if ((p2 - p1 > 0) && (p1 >= 0)) {
					str = handledStr.substring(p1, p2);
					
					if (leftOffset>0){
						
					}
					if (rightOffset>0){
						
					}
					 
					if (desiredLen>1){
						
					}
					
					foundinstance.add(str);
					
					handledStr = handledStr.substring(0, p1) + str + handledStr.substring(p2, handledStr.length());
					found = true;
					n++;
				}
			} // sth found ->

		} catch (Exception e) {

		}
		
		// change vector to []
	 
		return matches;
	}
	
	
	public boolean regexScannerMatch( String str, String expression){
		boolean rb=false;
		Scanner sx ;
		MatchResult result =null;
		int x ;
		
		
		// char ch;
		
		try{
			 
			sx = new Scanner(str);
			
			  
		    sx.findInLine(expression);
		    
		    if (sx!=null){
		    	result = sx.match();
		    }
		    
		    x=1; 
		    while (x<=result.groupCount()){
		    	// ch =  str.charAt( result.start() );
		    	x++;
		    }
		    
		    rb = x>=2;
		}catch(IllegalStateException iex){
			// just nothing found...
		}catch(Exception e){
			e.printStackTrace();
		}
		return rb;
	}
	
	public String regexScanner( String istr, String expression, String replacementX){
		
		return regexScanner( istr, expression, replacementX, 99999);
	}
	
	public String regexScanner( String istr, String expression, String replacementX, int amount){
		String str = istr;
		Scanner sx ;
		MatchResult result ;
		int x, replaced=0 ;
		String foundInstance,newInstance ;
		boolean replacerX = false;
		
		
		try{
			
			str = str.replaceAll(expression, replacementX);
			
			replacementX = replacementX.trim() ;
			if (replacementX.indexOf("^")==0){
				replacerX = true;
			}
			
			
			sx = new Scanner(str);
			
			 // "^*([\\s]{0,1}[a-zA-Z]{3,}([0-9]{1,}))");
		    sx.findInLine(expression);
		    result = sx.match();
		    
		    x=1; 
		    while (x<=result.groupCount()){
		    	
				foundInstance = result.group(x);
				// System.out.println( foundInstance);
				if ((foundInstance != null) && (foundInstance.length() > 0)) {
					if ((amount < 0) || (replaced < amount)) {

						if (replacerX) {
							newInstance = foundInstance.replaceAll(
									replacementX, "");

							str = str.replace(foundInstance, newInstance);

						} else {
							str = str.replace(foundInstance, replacementX);
						}
						replaced++ ;
						sx.findInLine(expression);
						result = sx.match();
						x = 0;
					}
				}
				x++;
			} // x -> all matches 
		    sx.close();
			
			
		}catch(Exception e){
			
			
		}
		
		return str ;
	}
    /*
	 /*
         * To create a Pattern instance we must call the static method 
         * called compile() in the Pattern class. Pattern object is 
         * the compiled representation of a regular expression.
          
        Pattern pattern = Pattern.compile("lazy");

        /*
         * The Matcher class also doesn't have the public constructor 
         * so to create a matcher class the Patter's class matcher() 
         * method. The Matcher object it self is the engine that match 
         * the input string against the provided pattern.
          
        Matcher matcher = pattern.matcher("The quick brown fox jumps over the lazy dog");

        while (matcher.find()) {
            System.out.format("Text \"%s\" found at %d to %d.%n",
                    matcher.group(), matcher.start(), matcher.end());
        }
	
	*/
	

	public boolean isDateX(String str){
		boolean return_value = false;
		
		// dd[-./]mm[-./]yyyy ... d[-./]m[-./]yy
		String regex_for_date  = "[1,2,3]?((\\d{2})|([1-9]{1}))([/.-])(([0-2]?\\d{1})|([3][0,1]{1}))([/.-])((\\d{2})|(\\d{4}))$";
		
		// yyyy[-./]mm[-./]dd ... yy[-./]m[-./]d
		String regex_for_dater = "((\\d{2})|(\\d{4}))([/.-])(([0-2]?\\d{1})|([3][0,1]{1}))([/.-])([1,2,3]?((\\d{2})|([1-9]{1})))$";
			 
		try{
			
			str = str.replace(",",".");
			// we should extract the date if it is present...
			if ( ((str.matches(regex_for_date)) || (str.matches(regex_for_dater))) && 
				 (str.length()>0)){
				
				int[] fop = frequenciesOfParticles(str, new String[]{".","-","/"});
				int fopx = ArrUtilities.arrayMaxPos(fop);
				if (fopx>=0){
					int frqval = fop[fopx];
					if (frqval==2){
						return_value=true;
					}
				}
			}

		}
		catch(Exception e){
			return_value = false;
			e.printStackTrace() ;
		}
		
		return return_value;
	}
	
	public boolean isAlphaChar( String str){
		boolean rb=true;
		int fccode ;
		
		
		try{
			
			fccode = (int)(str.charAt(0)) ;
			
			if ( (fccode<64) ||   // not "A", even not a num char !
				 ( (fccode>88) && (fccode<95)) ||  // between "Z" and "a"
			     (fccode>121) ){  // beyond z
				
				rb = false ;
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
		
		return rb;
	}
	
	@SuppressWarnings("unused")
	public static boolean isPureAscii(String v) {
		// String convertedString;
		CharBuffer r;
		
		byte bytearray[] = v.getBytes();
		CharsetDecoder d = Charset.forName("US-ASCII").newDecoder();

		try {
			r = d.decode(ByteBuffer.wrap(bytearray));
			
			// convertedString = r.toString();
			
		} catch (CharacterCodingException e) {
			return false;
		}
		return true;
	}

	
	public int isIniStyle(String str, boolean lazycheck){
		int form=-1;
		String[] positiveTags, tagContent;
		String cstr,tagstr ;
		int falseTags=0, falseCStr=0;
		
		// [exclusion] items item2 abc [mandatory] 
		   
		positiveTags = StringUtils.substringsBetween( str, "[", "]") ;
		tagContent = StringUtils.substringsBetween( str, "]", "[") ;
		
		for (int i=0;i<positiveTags.length;i++){
			tagstr = positiveTags[i] ;
			if (tagstr.indexOf("\n")>0){
				falseTags++ ;
			}
		}

		for (int i=0;i<tagContent.length;i++){
			cstr = tagContent[i] ;
			if (cstr.indexOf("\n")==0){
				falseCStr++ ;
			}
		}
		   
		form=-3;
		
		
		if ( (falseTags==0) && (falseCStr==0) &&
			 (positiveTags.length>0) && (tagContent.length>0)){
			form=0;
		} 
		
		if (lazycheck==false){
			// we check whether the entries are formatted acc. to quasi-standard
			// all items are like item=value
		}
		return form;
	}

	
	public int isXmlStyle(String str){
		int form=-1;
		int p;
		
		if (str.length()<10){
			return form;
		}
		
		String identifier = "<?xml" ;
		
		p = str.trim().indexOf( identifier ) ;
		
		if ((p>=0) && (p<5)){
			form = 0;
		} else {
			form = -3;
		}
		
		return form;	
	}

 	
	@SuppressWarnings("unchecked")
	public String arr2text( long[] avector,
		    				String separator ) {
		
		Vector<Long> vvector;
		
		vvector = new Vector(Arrays.asList(avector));
		
		return arr2text( vvector,separator);
	}
	
	
	
	public String arr2text( Vector<Long> vector,
							String separator ) {
	
		int i;
		long vi;
		String resultStr="", hs1;
		
		for (i = 0; i < vector.size(); i++) {
			vi = vector.get(i) ;
			hs1 = ""+vi;
			
			resultStr = resultStr+hs1;
			if (i < vector.size()-1){
				resultStr = resultStr + separator ;
			}
		}
		
		return resultStr;
	}
	
	public String arr2text( String[] vector,
						    String replacement,
						    String separator ) {

		return arr2text( vector,0,replacement,
   						 false,separator, 0) ;
	}
	
	public String arr2text( String[] vector,
			   				int maxLen,
			   				String replacement,
			   				boolean enforceUniqueness,
			   				String separator,
			   				int format) {

		String[] svector;
		String return_value = "", hs1,hs2 ;
		int i, vz=1;

		if (vector==null){
			return "";
		}
		
		try{
			
			

			svector = vector.clone() ;
			
			try{
			
			if (separator.length() == 0) {
				separator = " ";  // default separator
			}
			
			
			if (replacement.length()>0) {
				for (i = 0; i < vector.length; i++) {
					hs1 = svector[i] ;
				
					if (hs1.length()==0){
					
						DecimalFormat nft = new DecimalFormat("#0000.###");
						nft.setDecimalSeparatorAlwaysShown(false);
						hs2 = nft.format(vz);
						
						hs1 = "var"+hs2; vz++;
					}
					
					if (( isAlphaChar( hs1.substring(0,1))==false ) ){
						if (hs1.length()>1){
						hs1 = hs1.substring(1, hs1.length()-1)+ replacement ;
						}else{
							hs1 = hs1+ replacement ;
						}
					}
					
					hs1 = hs1.replace(" ", replacement);
					hs1 = hs1.replace("-", replacement);
					hs1 = hs1.replace(",", replacement);
					hs1 = hs1.replace(";", replacement);
					hs1 = hs1.replace("!", replacement);
					hs1 = hs1.replace("$", replacement);

					svector[i] = hs1;
				}
				
			}
			
			if (enforceUniqueness==true){
				// 
				
			}
			
			
			
			for (i = 0; i < svector.length; i++) {
				hs1 = svector[i] ;
				
				if (maxLen>3){
					if (hs1.length()>3){
						hs1 = hs1.substring(0,2) ;
					}
				}
				
				return_value = return_value  + hs1;
				
				if (i<svector.length-1){
					return_value = return_value + separator ;
				}
			}
			}catch(Exception e){
				return_value="";
				e.printStackTrace();
			}
		}catch(Exception e){
			return_value="";
			e.printStackTrace() ;
		}
		
		return return_value;
	}
	
	
	public String arr2text( double[] vector,
							   int fracdigits,
							   boolean trim_trailingzeroes, 
							   String separator) {

		String return_value = "", hs1;
		int i;

		if (separator.length() == 0) {
			separator = " ";
		}

		for (i = 0; i < vector.length; i++) {
			hs1 = String.format("%." + fracdigits + "f", vector[i]);
			if (trim_trailingzeroes == true) {
				hs1 = trimtrailingzeroes(hs1);

			}
			return_value = return_value  + hs1;
			if (i<vector.length-1){
				return_value = return_value + separator ;
			}
		}
		return_value = return_value.replace(",", ".");
		return return_value;
	}
	
	public String contentofCollection( ArrayList<String> filecontent , String separator, boolean keepLF){
		String str, concatted="" ;
		
		for (int i=0;i<filecontent.size();i++){
			str = filecontent.get(i) ;
			if (keepLF==false){
				str = str.trim();
			}
			
			if (str.length()>0){
				concatted = concatted + str;
				if ((i<filecontent.size()-1) && (str.indexOf("\n")<str.length())){
					concatted = concatted + separator;
				}
			}
		}
		
		return concatted ;
	}
	
	
	public String checkFeasibilities( String str ){
		String rStr = str;
		
		
		String[] nonsicles = {"-","~","*","_","*","·",",","\""," "} ;
		
		try{
			rStr = rStr.trim() ;
			
			rStr = checkFeasibility( rStr ,nonsicles, 0.29, true) ; 
			
			rStr = checkFeasibility( rStr , "-") ; 
			rStr = checkFeasibility( rStr , "~") ;
			rStr = checkFeasibility( rStr , ".") ;
			rStr = checkFeasibility( rStr , "*") ;
			rStr = checkFeasibility( rStr , " ") ;
			
			// 
			
		}catch(Exception e){
			
		}
		
		return rStr ;
		
	}
	public String checkFeasibility( String str ){
		
		return checkFeasibility( str , " ") ;
	}
	
	public String checkFeasibility( String str , String particle){
		return checkFeasibility( str , " ",0.27) ;
	}
	
	public String checkFeasibility( String str , String[] particles, double limitratio){
		String rStr = str;
		int i;
		String particle ;
		
		
		for (i=0;i<particles.length;i++){
			
			particle = particles[i] ;
			rStr = checkFeasibility( rStr , particle, limitratio) ;
			
			if (rStr.length()<=0){
				break ;
			}
		}
		
		return rStr;
	}
	
	public String checkFeasibility( String str , String particle, double limitratio){
		String rStr = str;
		int frq_blanks,frq_chars;
		double r ;
		
		try{
			
			frq_blanks = getsubStrFrequency(rStr, particle, 0) ;
			frq_chars  = rStr.length() ;// - frq_blanks ;
			
			r = (double)((frq_blanks*1.0+1.0)/(frq_chars*1.0)) ;
			
			if (r > limitratio){
				rStr = "" ;
			}
			 
			
		}catch(Exception e){
			
		}
		
		return rStr ;
	}
	
	// rStr = checkFeasibility( rStr ,nonsicles) ;
	public String checkFeasibility( String str , String[] particles, double limitratio, boolean collated){
		
		String rStr = str;
		int i,frq_blanks=0,frq_chars;
		double r = 0.000 ;
		String particle = "";
		
		
		try{
			
			for (i=0;i<particles.length;i++){
				particle = particles[i] ;
				
				// if (collated)   ....  else { Math.max() }
				frq_blanks = frq_blanks + getsubStrFrequency(rStr, particle, 0) ;
				
			} // i-> all particles
			
			
			frq_chars  = rStr.length() ; // - frq_blanks ;
			
			r = (double)((frq_blanks*1.0)/(frq_chars*1.0)) ;
			
			if (r > limitratio){
				rStr = "" ;
			}
			 
			
		}catch(Exception e){
			
		}
		
		return rStr ;
	}
	
	
	public String reverse( String str){
		
		StringBuffer buffer ;
		String reverseString ;
		
		//Create a StringBuffer from the original string  
		buffer = new StringBuffer(str);  
		  
		//Reverse the contents of the StringBuffer  
		buffer = buffer.reverse();  
		  
		//Convert the StringBuffer back to a String  
		reverseString = buffer.toString();  
		
		return reverseString ;
	}
	
	public String remove( String str, String particle ){
	
		str = this.replaceAll(str, particle, "");
		return str ;
	}
	
	public String remove( String str, int startAt , int endAt ){
	
		String rstr = str , resultStr=str,rstr2,rstr1;
		
		
		if (startAt<0){
			return rstr;
		}
		if (endAt> str.length()){
			return rstr;
		}
		if (endAt<=startAt){
			return rstr;
		}
		
		if (startAt==0){
			if (endAt < str.length()){
				rstr = str.substring( endAt, str.length());
			} else{
				rstr = str.substring( endAt, str.length());
			}
			resultStr = rstr ;
		} else{
			if (endAt < str.length()){
				rstr1 = str.substring( 0,startAt);
				rstr2 = str.substring( endAt, str.length());
				
				String s1,s2;
				s1 = rstr1.substring( rstr1.length()-10,rstr1.length() ) ;
				s2 = rstr2.substring( 0,10 ) ;
				
				rstr = s1+s2 ;
				rstr = rstr1+rstr2;
				resultStr = rstr;
			} else{ 
				rstr = str.substring( 0,startAt) ;
			}
			 
		}
		
		return resultStr;
	}
	
	/**
	 * should be used only if the substring to be replaced is unique
	 * 
	 * @param str
	 * @param startAt
	 * @param endAt
	 * @return
	 */
	public String removeForSingulars( String str, int startAt , int endAt ){
		String rstr = str , resultStr=str;
		
		
		if (startAt<0){
			return rstr;
		}
		if (endAt> str.length()){
			return rstr;
		}
		
		
		if (startAt==0){
			if (endAt < str.length()){
				rstr = str.substring( endAt, str.length());
			} else{
				rstr="" ;
			}
			resultStr = rstr ;
		} else{
			if (endAt < str.length()){
				rstr = str.substring( startAt, endAt);
			} else{
				rstr = str.substring( startAt, endAt);
			}
			resultStr = str.replace( rstr,"");
		}
		
		return resultStr;
	}
	
	public String removeNonsense(String str) { // e.g. from OCR or incorrect parsing of binary file	
	
		String rStr = str;
		int i,sum=0;
		int[] frq = new int[7];
		double r=0.0;
		
		
		
		for (i=0;i<frq.length;i++){
			frq[i] = -1 ;
		}
		
		try{
			
			rStr = trimInside(rStr," ") ;
			/*  examples
				25+-rr~~-=~rr-~
				+-~~~. ,--~-=~~~--~
				~ ~"?- ~'<) ~(; ~Q ~*- ~+ '! f'
				=_ __ __. ;tf_(_t, _d;. _)--
				1   +0.   og--
				~ ~"?- ~'<) ~(; ~Q ~*- ~+ '! f'
				classifier 0~~~~~DUDUBU~~~~~ classifier
			*/

			frq[0] = getsubStrFrequency(rStr, " ", 0) ;
			frq[1] = getsubStrFrequency(rStr, "~", 0) ;
			frq[2] = getsubStrFrequency(rStr, "-", 0) ;
			frq[3] = getsubStrFrequency(rStr, "(", 0) ;
			frq[4] = getsubStrFrequency(rStr, "&", 0) ;
			frq[5] = getsubStrFrequency(rStr, "/", 0) ;
			frq[6] = getsubStrFrequency(rStr, "\"", 0) ;
			frq[7] = getsubStrFrequency(rStr, "$", 0) ;
			
			for (i=0;i<frq.length;i++){
				sum = sum + frq[i] ;
			}
			
			r = (double)(sum*1.0/(1.0*rStr.length()));
			if (r>0.4){
				rStr = ""; 
			}
		}catch(Exception e){
			
			
		}
		
		
		return rStr;
	}
	
	
	public String _check_case_in_word( String str ) {
		String rStr = str;
		String firstChar,secChar,lastChar ;
		boolean firstisCapital,secisCapital = false,lastisCapital = false ;

		if (str.length()<=0){
			return rStr ;
		}
		
		firstChar = rStr.substring(0,1);
		firstisCapital = isCharUpperCase(firstChar,0) ;

		if (str.length()>=2){
			secChar  = rStr.substring(1,2);
			secisCapital  = isCharUpperCase(secChar,0) ;
			
			lastChar  = rStr.substring(rStr.length()-2,rStr.length()-1);
			lastisCapital  = isCharUpperCase(lastChar,0) ;
		} else{
			secisCapital = firstisCapital ;
			lastisCapital = firstisCapital ;
		}
		
		  
	    // it is not an acronym ? we test the second and the last character
	    if ( (secisCapital==false) && (lastisCapital==false)){
	    	rStr = rStr.toLowerCase() ;
	    }
		
	    return rStr;
	}

	
	// ---------------------------------------------------------
	
	public int getStartpos() {
		return startpos;
	}


	public void setStartpos(int startpos) {
		this.startpos = startpos;
	}
	
	public String stream2String( InputStream inStream ){
		
		String str="";
		
		try {
			
			if (inStream != null){
				str = IOUtils.toString( inStream, "UTF-8");
			}
			
		} catch (IOException e) {
			str = ""; 
			// e.printStackTrace();
		}
		
		return str ; 
	}

	
	
	public InputStream string2Stream(String str){
		InputStream is = null;
		
		try {
	        is = new ByteArrayInputStream(str.getBytes("UTF-8"));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    return is ;
	}

	
	public String cleanHtmlParsedText(String str){
		boolean done=false;
		int iLen ;
		
		
		
		while (done == false){
			iLen = str.length() ; 
			 
			str = replaceAll(str, "  ", " ");
			str = replaceAll(str, " \n", "\n");
			str = replaceAll(str, "\n ", "\n");
			str = replaceAll(str, "\n\n", "\n");
			str = replaceAll(str, "\t", " ");
			str = replaceAll(str, "\r", " ");
			str = replaceAll(str, "  ", " ");
			
			if ((iLen == str.length()) || (str.length()==0)){
				done=true;
			}
		}
		
		return str ;
	}
	
	
	public String simpleHtml2Text( String html, boolean isDocLocation ) {
		
		String htmlstr ="";
		if (isDocLocation==false){
			
			htmlstr = Jsoup.parse(html).text();
			
		}else{
			
			
		}
		
	    return htmlstr ;
	    
	}

	
	public String removeFileExtension( String filepath, String ext){
		String rfilestr = filepath ;
		String extdotted;
		int p,m;
		
		extdotted = ext;
		rfilestr = replaceAll(rfilestr,"..",".") ;
		if (ext.indexOf(".")>0){
			extdotted = "."+ext;
		}
		
		p = rfilestr.lastIndexOf(extdotted);
		m = extdotted.length() ;
		
		if (p== rfilestr.length()-m ){
			
			rfilestr = rfilestr.substring(0,p);
		}
		
		return rfilestr ;
	}
	
	
	public String getHomePageUrl(String weburlstr, boolean removeHttp){
		String rstr;
		int pHttp, pnxtSlash;
		// int pW3
		
		rstr = weburlstr.trim().toLowerCase() ;
		
		pHttp =  rstr.indexOf("http://") ;
		pnxtSlash =  rstr.indexOf("/",9) ; 
		// pW3 =  rstr.indexOf("www") ;
		
		
		if (pnxtSlash<0){
			rstr = weburlstr ;
		} else{
		
			
			rstr = weburlstr.substring(0,pnxtSlash);
			weburlstr = rstr;
			
			if (pHttp<0){
				
			}else{
				if (removeHttp){
					rstr = weburlstr.substring(7,weburlstr.length());
					weburlstr = rstr;
				}
			}
			
			 
		}
		
		
		return weburlstr;
	}
	

	public String getWebDocNamefromURL( String weburl){
		String rstr = ""   ;
		int pSlash, pDot, pQM;
		
		try{
			/*
			            http://www.exampledepot.com/
			            http://www.exampledepot.com/egs/org.w3c.dom/WalkElem.html
			            http://www.exampledepot.com/abc.pdf
			*/
			
			rstr = "1"; 
			rstr = weburl.trim() ;
			
			pSlash = rstr.lastIndexOf("/"); 
			pDot =  rstr.lastIndexOf(".");
			
			if (pSlash+1==rstr.length()){
				// is it html content, at least roughly...
				// str = "<!DOCTYPE html";
				// str = "<body>";
				// no flash
				
				rstr = rstr + "index.html" ;
				pSlash = rstr.lastIndexOf("/"); 
			}else{
				// http://
				if ((pDot > pSlash) && (pSlash<=7)){
					rstr = rstr + "/index.html" ;
				}
			}
			
			pQM =  rstr.indexOf("?");
			
			if (pQM>0){
				rstr = rstr.substring(0, pQM-1);
			}
			
			pDot =  rstr.lastIndexOf(".");
			pSlash = rstr.lastIndexOf("/"); 
			 
			if ((pDot>0) && (pSlash>0) && (pDot > pSlash)){
				rstr = rstr.substring(pSlash+1,rstr.length()).trim();
			}
			
		}catch(Exception e){
			
		}
		 
		rstr = this.replaceAll(rstr, ":", "") ;
		rstr = this.replaceAll(rstr, "&", "") ;
		rstr = this.replaceAll(rstr, "?", "") ;
		rstr = this.replaceAll(rstr, "$", "") ;
		
		return rstr;
	}
	
	public String createPathfromUrl( String part1, String urlstring ) {
		String dirstr="" , urlbasedPath="";
		
		urlbasedPath = createDirPathfromWebDocPath(urlstring);
		
		dirstr = part1+"/" + urlbasedPath;
		
		replaceAll(dirstr,"//","/") ;
		
		return dirstr ;
	}
	

	/**
	 * 
	 * for saving the web pages, we need a directory... -> we construct
	 * a relative home directory from the top/second - level domain
	 */
	public String createDirPathfromWebDocPath( String urlstr  ){
		 
		String baseDomainStr,str,dirpath = "" ;
		int   pNextSlash,p;
		// replace ? and & from web-queries
		// but save both to page :: object
		
		urlstr = urlstr.toLowerCase() ;
		
		 
		
		pNextSlash = urlstr.indexOf("/", 9) ;
		
		baseDomainStr = urlstr.substring(0, pNextSlash ) ;
		
		baseDomainStr = baseDomainStr.replace("HTTP://","") ;
		baseDomainStr = baseDomainStr.replace("Http://","") ;
		baseDomainStr = baseDomainStr.replace("http://","") ; 
		baseDomainStr = baseDomainStr.replaceAll("[\"www\"[0-9]{0,1}].", "");

		baseDomainStr = replaceAll(baseDomainStr , "//", "/");	
		
		p = urlstr.indexOf(baseDomainStr) + baseDomainStr.length();
		str = urlstr.substring(p+1,urlstr.length());
		p = str.lastIndexOf("/") ;
		
		if (p>0){
			str = str.substring(0,p) ;
			dirpath = baseDomainStr + "/"+ str;
		} else{
			dirpath = baseDomainStr ;
		}
		dirpath = replaceAll(dirpath , "//", "/");	
		
		return dirpath ;
	}
	
	public String extractWebDocName( String urlstr ){ //Page page ){
		String rstr = ""  ;  
		int pSlash, pDot, pQM;
		
		try{
			
			 
			rstr = urlstr.trim() ;
			
			pSlash = rstr.lastIndexOf("/"); 
			
			if (pSlash+1==rstr.length()){
				// is it html content, at least roughly...
				// str = "<!DOCTYPE html";
				// str = "<body>";
				// no flash
				
				rstr = rstr + "index.html" ;
				pSlash = rstr.lastIndexOf("/"); 
			}
			
			pQM =  rstr.indexOf("?");
			
			if (pQM>0){
				rstr = rstr.substring(0, pQM-1);
			}
			
			pDot =  rstr.lastIndexOf(".");
			
			if ((pDot>0) && (pDot > pSlash)){
				
			}
			
			
		}catch(Exception e){
			
		}
		 
		
		return rstr;
	}
	

	/**
	 * 
	 * creates a 4 digit integer code from main language string identifier ( en, de, fr ... etc... )
	 * @param langStr
	 * @return
	 */
	public int getLanguageNameEncoding( String langStr ){
		String str ;
		int languageID;
		
		langStr = rightPad(langStr , "_", 2).toLowerCase() ;
		str = (((int)(langStr.charAt(0))) - 95)+"" + (((int)(langStr.charAt(1)))-95)+"";
		
		languageID = Integer.parseInt(str) ;  
	
		return languageID; 
	}

	public static String separateBeforeLast(String inStr, String separator) {
		String str = separateLast(inStr, separator) ;
		String rstr = "";
		
		if (str.length()>0){
			rstr = inStr.replace(str, "");
		}else{
			rstr = "";
		}
		return rstr;
	}
	
	public static String separateLast(String string, String separator) {
		String separatedPart = ""; 
		String[] parts;
		
		try{
			
			if (string.contains(separator)==false){
				parts = new String[]{ string } ;
			}else{
				parts = string.split(separator);
			}
			
			separatedPart = parts[parts.length-1];
			
		}catch(Exception e){
			
		}
		
		return separatedPart;
	}

	public String cleanLabelFromLocales(String instr) {
		String str ;
		str = instr;
		
		 
		str = replaceAll(str,"ü","ue");
		str = replaceAll(str,"ö","oe");
		str = replaceAll(str,"ä","ae");

		str = replaceAll(str,"Ü","Ue");
		str = replaceAll(str,"Ö","Oe");
		str = replaceAll(str,"Ä","Ae");
		
		str = StringUtils.stripAccents(str);
		
		// and finally hard-core int Uxxxx
		str = native2Ascii(str);
		
		return str;
	}

	private String native2Ascii(String str) {
		
		StringBuffer sb = new StringBuffer(str.length());
		sb.setLength(0);
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			
			sb.append(native2Ascii(c));
		}
		return (new String(sb));
	}

	private static StringBuffer native2Ascii(char charater) {
		StringBuffer sb = new StringBuffer();
		if (charater > 255) {
			sb.append("\\u");
			int lowByte = (charater >>> 8);
			sb.append(int2HexString(lowByte));
			int highByte = (charater & 0xFF);
			sb.append(int2HexString(highByte));
		} else {
			sb.append(charater);
		}
		return sb;
	}

	private static String int2HexString(int code) {
		String hexString = Integer.toHexString(code);
		if (hexString.length() == 1) hexString = "0" + hexString;
		return hexString;
	}

	public String[] getNumbers() {
		return numbers;
	}

	public void setNumbers(String[] numbers) {
		this.numbers = numbers;
	}

	public void setEffectiveSeparator(String effectiveSeparator) {
		this.effectiveSeparator = effectiveSeparator;
	}

	/**
	 * extracts the first row of a table, that is contained in a longer string;
	 * it measures the number of tabs (sep) in the rows, and uses it as the criterion to decide
	 * 
	 * @param tableStr
	 * @return
	 */
	public String getFirstRowOfTable(String tableStr, String separator) {
		String rowstr="";
		String[] rows ;
		int[] sepFreqs ;
		if (separator.length()==0)separator="\t" ;
		
		rows = tableStr.split("\n");
		sepFreqs = new int[rows.length];
		
		for (int i=0;i<rows.length;i++){
			int c = this.frequencyOfStr( rows[i], separator );
			sepFreqs[i] = c;
		}
		int[] frq = new int[sepFreqs.length] ; System.arraycopy(sepFreqs , 0, frq, 0, frq.length) ;
		Arrays.sort(frq) ; 
		double sfm = ArrUtilities.arraySum(sepFreqs)/sepFreqs.length;
		
		for (int i=0;i<rows.length;i++){
			 if (sepFreqs[i]<sfm)sepFreqs[i]=0;
		}

		int tr= -1;
		int p = ArrUtilities.arrayMaxPos(sepFreqs) ;

		// is either the value before or after equal to that at p?
		// now: the first row with that value is our target
		
		if (p<rows.length-1){
			for (int i = 0; i < rows.length; i++) {
				if (sepFreqs[i]>= sfm){
					tr=i;
					break;
				}
			}
		}

		// get it
		if (tr>=0){
			rowstr = rows[tr] ; 
		}
		
		return rowstr;
	}

	public static String notNullString(String str) {
		 
		if (str==null){
			str = new String();
			str="";
		}
		return str;
	}

	public static String getLastPartOfString(String string, String separator) {
		
		String extStr ="" ;
		int p = -1;
		if ((string!=null) && (string.length()>0)){
			p = string.lastIndexOf(separator);
		}
		if (p>0){
			extStr = string.substring(p, string.length());
		}
		
		return extStr ;
	}
	
	public static String getExtensionFromFilename(String filename) {
		return getExtensionFromFilename(filename,1);
	}
	/**
	 * 
	 * @param filename
	 * @param mode 0 = without dot, 1 = with dot
	 * @return
	 */
	public static String getExtensionFromFilename(String filename, int mode) {
		// 
		String extStr="" ;
		
		extStr = getLastPartOfString(filename,".");
		if (mode==0){
			if (extStr.indexOf(".")==0){
				extStr = extStr.substring(1,extStr.length()) ;
			}
		}
		
		return extStr;
	}

	public String[] getFilenameExtensions(String filename) {
		String[] exts = new String[0];
		String str = filename;
		int n;
		
		str = this.replaceAll(str, "..", ".").trim() ; 
		if (str.endsWith(".")){
			str = str.substring(0,str.length()-1);
		}
		
		n= this.frequencyOfStr(str, ".");
		if (n==0){
			return exts;
		}
		
		int p = str.indexOf(".") ;
		if (p<0){
			return exts;
		}
		
		str = str.substring(p,str.length()) ;

		n= this.frequencyOfStr(str, ".");
		p = str.indexOf(".");
		
		if ((n==1) && (p==0)){
			exts = new String[1] ;
			exts[0] = str;
		}else{
			if (p==0){
				str = str.substring(1,str.length()) ;
			}
			String[] extsn = str.split("\\.") ; // it is regex!! escape the escape  the dot , in order to get it as literal
			exts = extsn;
		}
		
		for (int i=0;i<exts.length;i++){
			
			exts[i] = "."+exts[i];
			exts[i] = exts[i].replace("..", ".") ;
		}
		
		return exts;
	}

	public static String padLeadingZeroes(int intValue, int zCount) {

		String format = String.format("%%0%dd", zCount);
		String result = String.format(format, intValue);
		return result;
 	}

	public boolean isYear(String str) {
		 
		boolean rB=false;
		int yValue=-1;
		
		try{
			
			if (isNumericX(str)==true){
				if ((str.indexOf(".")<0) && ((str.indexOf(",")<0))) {
					yValue = Integer.parseInt( str ) ;
					if ((yValue>1600) && (yValue<2100)){
						rB=true;
					}
				}
			}
			
		}catch(Exception e){
		}
		
		return rB;
	}

	/**
	 * this splits a string by 'splitter', but only outside of pairs of " " !
	 * 
	 * @param inStr
	 * @param splitter
	 * @return
	 */
	public String[] splitQ(String inStr, String splitter) {
		String[] splits = new String[0];
		
		String str, xstr, replstr , placeholder = "?&$<enum>$&?";
		Map<String,String> selement = new HashMap<String,String>();
		// replace content of "..."
		
		int p1,p2,n, replacesCount=0 ;
		
		if (splitter.contains("?")){
			placeholder = this.replaceAll(placeholder, "?", "#");
		}
		if (splitter.contains("$")){
			placeholder = this.replaceAll(placeholder, "$", "#");
		}
		if (splitter.contains("&")){
			placeholder = this.replaceAll(placeholder, "&", "#"); 
		}
		if (splitter.contains("#")){
			placeholder = this.replaceAll(placeholder, "&", "¨"); 
		}
		if (splitter.contains("¨")){
			placeholder = this.replaceAll(placeholder, "¨", "|"); 
		}
		
		str = inStr;
		str = this.trimm(str, splitter) ;
		
		p1= str.indexOf(splitter);
		n = this.frequencyOfStr(str, splitter) ;
		
		if ((p1>0) && (n%2==0) && (splitter.contains("\"")==false)){
			
			boolean found=true;
			while (found){
				
				p1=str.indexOf(splitter);
				p2=str.indexOf(splitter,p1+1);
				
				if ((p1>0) && (p2>p1)){
					
					xstr = str.substring(p1,p2);
					replstr = placeholder.replace("<enum>", ""+(replacesCount+1));
					selement.put(replstr, xstr);
					replacesCount++;
					p1=0;
				}else{
					break;
				}
				
			}// ->
			
		}// any ?
		
		splits = str.split(splitter);
		
		for (int i=0;i<replacesCount;i++){
			
			str  =splits[i];
			replstr = placeholder.replace("<enum>", ""+(i+1)) ;// "&$.<enum>.$&"
			
			if (str.indexOf(replstr)>0){
				str = str.replace( replstr , selement.get(replstr));
				splits[i] = str;
			}
			
		}// ->
		
		selement.clear();
		return splits;
	}

	public String getStringFromT(Object obj, String itemSeparator) {
		String content="", cn,str;
		
		if (obj==null){
			return "";
		}
		
		if (itemSeparator.length()==0){
			itemSeparator=" " ;
		}
		
		cn = obj.getClass().getSimpleName().toLowerCase();
		
		if (cn.contentEquals("string")){
			content = (String)obj;
		}
		if ((cn.contentEquals("int")) || (cn.contentEquals("integer"))){
			content = ""+(Integer)obj;
		}
		if (cn.contentEquals("double")){
			String.format("%.6f", (Double)obj);
		}
		if (cn.contentEquals("long")){
			content = ""+(Long)obj;
		}
		if (cn.contentEquals("string[]")){
			String[] strarr = (String[])obj;
			content = arr2text(strarr, "", itemSeparator);
		}
		
		if (cn.contentEquals("int[]")){
			int[] intarr = (int[])obj;
			content = ArrUtilities.arr2Text( ArrUtilities.changeArraystyle(intarr) );
		}
		if (cn.contentEquals("double[]")){
			double[] doubarr = (double[])obj;
			content = this.arr2text(doubarr, 6, true, itemSeparator);
		}
		if (cn.contentEquals("long[]")){
			long[] intarr = (long[])obj;
			content = arr2text(intarr, itemSeparator);
		}
		
		/*
		    dtstr = datetimeValue.get(); // according to defined format
			hs1 = String.format("%05d", n);
		 */
		return content;
	}

	public String removeTagfromHtml(String htmlstr, String tag, int position) {
		// 
		String html;
		int[] divP,divC;
		ArrayList<Integer> divpositions,divCloses;
		int divB,divE;
		html = htmlstr ;
		
		divP = indexesOfStrings(htmlstr, new String[]{"<div"}, position, true);
		divC = indexesOfStrings(htmlstr, new String[]{"</div>"}, position, true);
		
		divpositions = ArrUtilities.changeArraystyle(divP) ;
		divCloses = ArrUtilities.changeArraystyle(divC) ;
			
		for (int i=0;i<divpositions.size();i++){
			divB = divpositions.get(i);
			
			for (int k=0;i<divCloses.size();k++){
				divE = divCloses.get(k) ;
			}
		}
		
		return html;
	}





	
}


class localNumStuff{
	
	


	public double arrayMin( double[] valarr, double defaultValue){
		double return_value=0.0, min =  9999999999.9;
		int i;
		
		i=0;
		for ( i = 0; i < valarr.length; i++) {
			  if (min>valarr[i]){
				  min = valarr[i] ;
			  }
		}
		if (min ==  9999999999.9){
			min = defaultValue;
		}
		return_value = min ;
		
		return return_value;		
		
		
	}

	public double arrayMin( int[] valarr ){
		return arrayMin( valarr ,-1);
	}
	
	public int arrayMin( Vector<Integer> valarr, int defaultValue){
		
		if ((valarr==null) || (valarr.size()==0)){
			return defaultValue;
		}
		
		
		int[] varr = new int[valarr.size()];
		
		for (int i=0;i<varr.length;i++){
			varr[i] = (int)(valarr.get(i));
		}
		return arrayMin( varr, defaultValue) ;
	}
	
	public int arrayMin( int[] valarr, int defaultValue){
		int return_value = defaultValue, min =  999999999;
		int i;
		
		if ((valarr==null) || (valarr.length==0)){
			return return_value;
		}
		i=0;
		for ( i = 0; i < valarr.length; i++) {
			  if (min>valarr[i]){
				  min = valarr[i] ;
			  }
		}
		if (min ==  999999999){
			min = defaultValue;
		}
		return_value = min ;
		
		return return_value;		
		
		
	}
}

class UnaccentedStr {
    private static final String PLAIN_ASCII =
      "AaEeIiOoUu"    // grave
    + "AaEeIiOoUuYy"  // acute
    + "AaEeIiOoUuYy"  // circumflex
    + "AaOoNn"        // tilde
    + "AaEeIiOoUuYy"  // umlaut
    + "Aa"            // ring
    + "Cc"            // cedilla
    + "OoUu"          // double acute
    ;

    private static final String UMLAUT = "äöüÄÖÜß";
    
    
    private static final String UNICODE =
     "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
    + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
    + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177"
    + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
    + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF"
    + "\u00C5\u00E5"
    + "\u00C7\u00E7"
    + "\u0150\u0151\u0170\u0171"
    ;

    // private constructor, can't be instantiated!
    private UnaccentedStr() { }

    // remove accented from a string and replace with ascii equivalent
    public static String convertNonAscii(String s) {
       
    	if ((s == null) || (s.length()==0)){
    		return "";
    	}
    	
       StringBuilder sb = new StringBuilder();
       
       int n = s.length();
       
       for (int i = 0; i < n; i++) {
    	   
          char c = s.charAt(i);
          int pos = UNICODE.indexOf(c);
          
          if (pos > -1) {
        	  if (UMLAUT.indexOf(CharUtils.toString(c))>=0){ 
        		  sb.append(PLAIN_ASCII.charAt(pos));
        		  sb.append("e");
        	  }else{
        		  sb.append(PLAIN_ASCII.charAt(pos));
        	  }
          }
          else {
              sb.append(c);
          }
       }
       return sb.toString();
    }

    static public void test() {
       String s = "The result : È,É,Ê,Ë,Û,Ù,Ï,Î,À,Â,Ô,è,é,ê,ë,û,ù,ï,î,à,â,ô,ç";
       System.out.println(UnaccentedStr.convertNonAscii(s));
       // output :
       // The result : E,E,E,E,U,U,I,I,A,A,O,e,e,e,e,u,u,i,i,a,a,o,c
    }
    
    
}
 




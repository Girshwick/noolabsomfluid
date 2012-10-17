package org.NooLab.utilities.strings;

import java.util.*; 

import org.apache.commons.lang3.*;

 

// http://commons.apache.org/lang/api-2.4/org/apache/commons/lang/StringUtils.html

// import java.util.Dictionary;

/**
 * 
 * offers
 * - Levenshtein distances (LD) in several flavors
 *<br/><br/>
 * 
 * LD's can be calculated for differential costs, depending on edit action
 * 
 */
public class StringComparison {

	
	// main variables / properties ....

	

	Map<String,Double> costbyAction = new HashMap<String,Double>();
	
	double addCostonFirstPosition = 0.5;// 0.2;
	double adjustCostonEntailment = 0.6 ;// 0.6;
	double adjustbyInverseStemming = 0.5; // 0.7;
	
	// constraints for accelerating
	int limitCharCount;
	
	/** we test only, if the first n chars are equal   */
	int initialEqualityCharCount = 5 ; 
	
	/** we test only, if the longest common substring > max(30%,n chars)  */
	int EqualitySubStrLength = 4 ; 
	
	int fullComparisons = 0; 
	int abbreviatedComparisons = 0;
	int flythroughBreak = 0;
	
	// constants ......................

	
	
	// volatile / internal / mirror
	
	// this only works for exactly repeated pairs
	// TreeMap<String,Double> levenbuffer = new TreeMap<String,Double>() ;
	// this does not work for lots of items, beyond 1600 items performance collapses (x8..10
	// in multi-threaded environment
	
	
	int totalComparisonCount=0;
	long stoppedTime = 0 ; 
	long introTime, exitTime;
	
	// helper objects .................

	// this is contained here in this file
	Setutilities setutils = new Setutilities() ;

	// this is an inner class to "StringComparison"
	Levenshtein2 lev2 = new Levenshtein2();
	
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	

	public StringComparison() {

		// setting the default values for edit actions
		costbyAction.put("i",0.23) ; // 0.23
		costbyAction.put("d",0.36) ;  // 0.4
		costbyAction.put("c",0.68) ; // 0.65
		 
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	


	public int[] findMostSimilarStringItems( ArrayList<String> items, String compareStr ){
		int[] positions= new int[0];
	
		return positions;
	}

	public int findMostSimilarStringItem( ArrayList<String> items, String compareStr ){
		return findMostSimilarStringItem( items, compareStr ,1,0 );
	}

	public int findMostSimilarStringItem( ArrayList<String> items, String compareStr, int maxThreadCount, int offset ){
		int position=-1;
		
		String str;
		double maxSim=-1.0, iSim;
		
		for (int i=0;i<items.size();i++){
			
			str = items.get(i);
			iSim = LevenshteinSimilarity( compareStr, str);
			
			if ((iSim>0.01) && (iSim>maxSim)){
				maxSim = iSim;
				position = i;
			}
			
		} // i-> all items
		
		
		return position;
	}

	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . 	
	

	
	
	/**
	 * 
	 * returns the simple count of "edits" necessary to change str1 into str2
	 * 
	 */
	public int LevenshteinStdCount(String str1, String str2) {
		int dist;

		dist = StringUtils.getLevenshteinDistance(str1, str2);

		 
		return dist;
	}

	/**
	 * 
	 * returns a similarity value normalized by the mean length of the strings 
	 */
	public double LevenshteinStd(String str1, String str2) {
		double dist;

		dist = (double)(StringUtils.getLevenshteinDistance(str1, str2)/((str1.length()+str1.length())/2));

		return dist;
	}

	
	public int LevenshteinPosWeighted(String str1, String str2) {
		int dist = -1;
		String[][] strparts = new String[2][2];
		double[] distance = new double[4];
		double distanceAll;
		int ph1,ph2 ;
		
		
		// StringUtils.indexOfDifference
		if ((str1.length() > 4) && (str2.length() > 4)) {
			dist = 0;
			ph1 = (int)(str1.length()/2);
			ph2 = (int)(str1.length()/2);

			strparts[1][1] = str1.substring(0,ph1);
			strparts[1][2] = str1.substring(ph1,str1.length());
			strparts[2][1] = str2.substring(0,ph2);
			strparts[2][2] = str2.substring(ph2,str2.length());

			distanceAll = StringUtils.getLevenshteinDistance(str1, str2);

			distance[0] = StringUtils.getLevenshteinDistance(strparts[1][1], strparts[2][1])/ph1;
			distance[1] = StringUtils.getLevenshteinDistance(strparts[1][2], strparts[2][2])/ph1;
			distance[2] = StringUtils.getLevenshteinDistance(strparts[1][1], strparts[2][2])/ph2;
			distance[3] = StringUtils.getLevenshteinDistance(strparts[1][2], strparts[2][1])/ph2;
			

		} else {
			dist = StringUtils.getLevenshteinDistance(str1, str2);
		}

		return dist;
	}

	/**
	 * edit distance weighted by type of changes; the cost matrix has to be set
	 * before (as a Map<String,double>, delivered in various formats)<br/>
	 * 
	 * 
	 * @param str1
	 * @param str2
	 * @return
	 */
	public double LevenshteinSimilarity(String str1, String str2) {
		int dist, s1,s2;
		double dv = -1.0, meanLen;
		String _s , bufferedStr;
		
		
		bufferedStr = str1+"<::>"+str2 ;
		try{
			// we could also do it via a hash table in a database
			//if (levenbuffer.containsKey(bufferedStr))
			// dv = levenbuffer.get( bufferedStr );
			
		}catch(Exception e){
			// implicit replacement for : levenbuffer.containsKey(bufferedStr)
			// so we test it only once if it exists
			e.printStackTrace();
		}
		
		if (dv>0.0){
			return dv;
		}
		 
		introTime = System.currentTimeMillis() ;
		
		
		
		s1 = str1.length();
		s2 = str2.length();
		meanLen = (s1+s2)/2.0 ;
		
		if (s1>s2){
			_s = str1; str1=str2; str2=_s ;
		}
		
		totalComparisonCount++ ;
		
		dv = lev2.distance(str1, str2) ;
		
		//if (dv>0.101)
		{
			
			// dist = getLevenshteinRaw(str1, str2);
			
			// dv = (dist + dv)/2.0 ;
			dv = Math.max(0.0001, (1.0 -  dv/(meanLen)))  ;
			dv = Math.round(dv*100000.0)/100000.0;
			

			
		}
		
		dv = Math.min(1.0, dv);
		dv = Math.max(0.0, dv);
		 
		// levenbuffer.put(bufferedStr, dv) ;
		
		exitTime = System.currentTimeMillis() ;
		stoppedTime = stoppedTime + (exitTime - introTime ) ;
		
		
		return  dv ;
	}

	/**
	 * <p>
	 * Find the Levenshtein distance between two Strings.
	 * </p>
	 * 
	 * <p>
	 * This is the number of changes needed to change one String into another,
	 * where each change is a single character modification (deletion, insertion
	 * or substitution).
	 * </p>
	 * 
	 * This implementation of the Levenshtein distance algorithm is from <a
	 * href="http://www.merriampark.com/ldjava.htm">http://www.merriampark.com/
	 * ldjava.htm</a>
	 * </p>
	 * 
	 * <pre>
	 * StringUtils.getLevenshteinDistance(null, *)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance(*, null)             = IllegalArgumentException
	 * StringUtils.getLevenshteinDistance("","")               = 0
	 * StringUtils.getLevenshteinDistance("","a")              = 1
	 * StringUtils.getLevenshteinDistance("aaapppp", "")       = 7
	 * StringUtils.getLevenshteinDistance("frog", "fog")       = 1
	 * StringUtils.getLevenshteinDistance("fly", "ant")        = 3
	 * StringUtils.getLevenshteinDistance("elephant", "hippo") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "elephant") = 7
	 * StringUtils.getLevenshteinDistance("hippo", "zzzzzzzz") = 8
	 * StringUtils.getLevenshteinDistance("hello", "hallo")    = 1
	 * </pre>
	 * 
	 * @param s
	 *            the first String, must not be null
	 * @param t
	 *            the second String, must not be null
	 * @return result distance
	 * @throws IllegalArgumentException
	 *             if either String input <code>null</code>
	 * @since 3.0 Changed signature from getLevenshteinDistance(String, String)
	 *        to getLevenshteinDistance(CharSequence, CharSequence)
	 */
	private int getLevenshteinRaw( CharSequence s, CharSequence t) {
		if (s == null || t == null) {
			throw new IllegalArgumentException("Strings must not be null");
		}

		/*
		 * The difference between this impl. and the previous is that, rather
		 * than creating and retaining a matrix of size s.length()+1 by
		 * t.length()+1, we maintain two single-dimensional arrays of length
		 * s.length()+1. The first, d, is the 'current working' distance array
		 * that maintains the newest distance cost counts as we iterate through
		 * the characters of String s. Each time we increment the index of
		 * String t we are comparing, d is copied to p, the second int[]. Doing
		 * so allows us to retain the previous cost counts as required by the
		 * algorithm (taking the minimum of the cost count to the left, up one,
		 * and diagonally up and to the left of the current cost count being
		 * calculated). (Note that the arrays aren't really copied anymore, just
		 * switched...this is clearly much better than cloning an array or doing
		 * a System.arraycopy() each time through the outer loop.)
		 * 
		 * Effectively, the difference between the two implementations is this
		 * one does not cause an out of memory condition when calculating the LD
		 * over two very large strings.
		 */

		int n = s.length(); // length of s
		int m = t.length(); // length of t

		if (n == 0) {
			return m;
		} else if (m == 0) {
			return n;
		}

		if (n > m) {
			// swap the input strings to consume less memory
			CharSequence tmp = s;
			s = t;
			t = tmp;
			n = m;
			m = t.length();
		}

		// n < m  
		int p[] = new int[n + 1]; // 'previous' cost array, horizontally
		int d[] = new int[n + 1]; // cost array, horizontally
		int _d[]; // placeholder to assist in swapping p and d

		// indexes into strings s and t
		int i; // iterates through s
		int j; // iterates through t
		int cc, lc, dc, uc;
		
		char t_j, s_i; // jth character of t

		int cost; // cost

		for (i = 0; i <= n; i++) {
			p[i] = i;
		}

		j=0; s_i = ' ';
		
		for (j = 1; j <= m; j++) {
			
			t_j = t.charAt(j - 1);
			d[0] = j;

			for (i = 1; i <= n; i++) {
				// cost = s.charAt(i - 1) == t_j ? 0 : 1;
				s_i = s.charAt(i - 1);
				
				if (s_i == t_j){
					cost = 0;
				}else{
					cost = 1;
					// dependent on type of inequality (direction within the "matrix")
					// we have different costs here
					
					
				}
				// minimum of cell to the left+1, to the top+1, diagonally left  and up +cost
				uc = p[i - 1] + cost ;
				lc = d[i - 1] + 1 ;
				dc =  p[i] + 1 ;
				d[i] = Math.min( Math.min( lc, dc), uc);
				// here we have to modify to be able to distinguish the edits !!!
				
			}

			// "copy" current distance counts to 'previous row' distance counts
			_d = p;
			p = d;
			d = _d;
		}

		// our last action in the above loop was to switch d and p, so p now
		// actually has the most recent cost counts
		cost = p[n];
		return cost;
	}

	public String longestCommonSubstr(String str1, String str2) {

		StringBuilder sb = new StringBuilder();
		if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty())
			return "";

		// ignore case
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();

		// java initializes them already with 0
		int[][] num = new int[str1.length()][str2.length()];
		int maxlen = 0;
		int lastSubsBegin = 0;

		for (int i = 0; i < str1.length(); i++) {
			for (int j = 0; j < str2.length(); j++) {
				if (str1.charAt(i) == str2.charAt(j)) {
					if ((i == 0) || (j == 0))
						num[i][j] = 1;
					else
						num[i][j] = 1 + num[i - 1][j - 1];

					if (num[i][j] > maxlen) {
						maxlen = num[i][j];
						// generate substring from str1 => i
						int thisSubsBegin = i - num[i][j] + 1;
						if (lastSubsBegin == thisSubsBegin) {
							// if the current LCS is the same as the last time
							// this block ran
							sb.append(str1.charAt(i));
						} else {
							// this block resets the string builder if a
							// different LCS is found
							lastSubsBegin = thisSubsBegin;
							sb = new StringBuilder();
							sb.append(str1.substring(lastSubsBegin, i + 1));
						}
					}
				}
			} // j->
		} // i->

		return sb.toString();
	}

	public int longestCommonSubstrLength(String first, String second) {
	    if (first == null || second == null || first.length() == 0 || second.length() == 0) {
	        return 0;
	    }
	 
	    int maxLen = 0;
	    int fl = first.length();
	    int sl = second.length();
	    int[][] table = new int[fl][sl];
	 
	    for (int i = 0; i < fl; i++) {
	        for (int j = 0; j < sl; j++) {
	            if (first.charAt(i) == second.charAt(j)) {
	                if (i == 0 || j == 0) {
	                    table[i][j] = 1;
	                }
	                else {
	                    table[i][j] = table[i - 1][j - 1] + 1;
	                }
	                if (table[i][j] > maxLen) {
	                    maxLen = table[i][j];
	                }
	            }
	        } // j->
	    }// i->
	    return maxLen;
	}
	
	public String longestCommonSubsequence( String a, String b ) {
	    int[][] lengths = new int[a.length()+1][b.length()+1];
	 
	    // row 0 and column 0 are initialized to 0 already
	 
	    for (int i = 0; i < a.length(); i++)
	        for (int j = 0; j < b.length(); j++)
	            if (a.charAt(i) == b.charAt(j))
	                lengths[i+1][j+1] = lengths[i][j] + 1;
	            else
	                lengths[i+1][j+1] =
	                    Math.max(lengths[i+1][j], lengths[i][j+1]);
	 
	    // read the substring out from the matrix
	    StringBuffer sb = new StringBuffer();
	    for (int x = a.length(), y = b.length();
	         x != 0 && y != 0; ) {
	        if (lengths[x][y] == lengths[x-1][y])
	            x--;
	        else if (lengths[x][y] == lengths[x][y-1])
	            y--;
	        else {
	            assert a.charAt(x-1) == b.charAt(y-1);
	            sb.append(a.charAt(x-1));
	            x--;
	            y--;
	        }
	    }
	 
	    return sb.reverse().toString();
	}
	

	 public String longestCommonSubsequence2( String a, String b ) {
	        String x = a ;
	        String y = b ;
	        String lcs="" ;
	        int M = x.length();
	        int N = y.length();

	        // opt[i][j] = length of LCS of x[i..M] and y[j..N]
	        int[][] opt = new int[M+1][N+1];

	        // compute length of LCS and all subproblems via dynamic programming
	        for (int i = M-1; i >= 0; i--) {
	            for (int j = N-1; j >= 0; j--) {
	                if (x.charAt(i) == y.charAt(j))
	                    opt[i][j] = opt[i+1][j+1] + 1;
	                else 
	                    opt[i][j] = Math.max(opt[i+1][j], opt[i][j+1]);
	            }
	        }

	        // recover LCS itself and print it to standard output
	        int i = 0, j = 0;
	        while(i < M && j < N) {
	            if (x.charAt(i) == y.charAt(j)) { 
	                // System.out.print(x.charAt(i));
	            	lcs = lcs + x.substring(i,i+1) ;
	             
	                i++;
	                j++;
	            }
	            else if (opt[i+1][j] >= opt[i][j+1]) 
	            	i++;
	            else                                 
	            	j++;
	        }
	        
	        
	       return lcs;  

	    }
	 
	 
	public Map<String, Double> getCostbyAction() {
		return costbyAction;
	}

	public void setCostbyAction(Map<String, Double> costbyAction) {
		this.costbyAction = costbyAction;
	}


	public void setCostforDel( double cost) {
		costbyAction.put("d", cost);
	}
	public void setCostforIns( double cost) {
		costbyAction.put("i", cost);
	}
	public void setCostforChg( double cost) {
		costbyAction.put("c", cost);
	}

	public double getCostforDel( ) {
		return costbyAction.get("d");
	}
	public double getCostforIns() {
		return costbyAction.get("i");
	}
	public double getCostforChg() {
		return costbyAction.get("c");
	}

	public double[] getCostsAllDelInsChg(){
		double[] costs = new double[3] ;
		
		costs[0] = costbyAction.get("d");
		costs[1] = costbyAction.get("i");
		costs[2] = costbyAction.get("c");
		
		return costs;
	}
	
	public int setCostsAllDelInsChg( double[] costs){
		int result = -1;
		if (costs.length != 3){
			return -3;
		}

		for (int i=0;i<3;i++){
			if ( (costs[i]<=0.01) ){
				return -4;
			}
			if ( (costs[i] >= 0.99) ){
				return -5;
			}
			
		}
		
		costbyAction.put("d", costs[0]);
		costbyAction.put("i", costs[1]);
		costbyAction.put("c", costs[2]);

		return result;
	}

	
	public double getAddCostonFirstPosition() {
		return addCostonFirstPosition;
	}

	public void setAddCostonFirstPosition(double addCostonFirstPosition) {
		this.addCostonFirstPosition = addCostonFirstPosition;
	}

	public double getAdjustCostonEntailment() {
		return adjustCostonEntailment;
	}

	public void setAdjustCostonEntailment(double adjustCostonEntailment) {
		this.adjustCostonEntailment = adjustCostonEntailment;
	}

	public double getAdjustbyInverseStemming() {
		return adjustbyInverseStemming;
	}

	public void setAdjustbyInverseStemming(double adjustbyInverseStemming) {
		this.adjustbyInverseStemming = adjustbyInverseStemming;
	}


	public int getInitialEqualityCharCount() {
		return initialEqualityCharCount;
	}

	public void setInitialEqualityCharCount(int initialEqualityCharCount) {
		this.initialEqualityCharCount = initialEqualityCharCount;
	}

	public int getEqualitySubStrLength() {
		return EqualitySubStrLength;
	}

	public void setEqualitySubStrLength(int equalitySubStrLength) {
		EqualitySubStrLength = equalitySubStrLength;
	}

	public void setTotalComparisonCount(int totalComparisonCount) {
		this.totalComparisonCount = totalComparisonCount;
	}

	public long getStoppedTime() {
		return stoppedTime;
	}


	public int getTotalComparisonCount() {
		return totalComparisonCount;
	}


	public int getFullComparisons() {
		return fullComparisons;
	}

	public int getAbbreviatedComparisons() {
		return abbreviatedComparisons;
	}


	public int getFlythroughBreak() {
		return flythroughBreak;
	}


	// this uses a 2d array
	class Levenshtein2 {

		// Map<String,Double> costbyAction = new HashMap<String,Double>();
		
		
		@SuppressWarnings("unused")
		public double distance(String s, String t) {
			double cost    ;

			String lcs="", t_j ="", s_i = null,t_jj="", yc="",str2,str1 ;
			
			int  i,j ,k, y=0, L=0, ins=0,del=0, chg=0 ,lminpos=0, isz=-1, z1,z2;
			int minLengthST,maxLengthST;
			double  lc, dc, uc,v,c,ca, isCostadj, initEQcost=0.0 ;
			
			double cv, totalcost, adjustonEntail,  min=0,editcost=-1.0,currentRowMin = 99999999.0 ;
			double stdlev,dv,editsCount;
			
			int n = s.length();
			int m = t.length();
			double[][] d = new double[n + 1][m + 1];
			double[] RowMin ;  
			
			int[] RowMinPos , RowChangeType;
			 
			// Step 1
			if (n == 0) {
				return m;
			}

			if (m == 0) {
				return n;
			}

if ( t.contentEquals("they")){
	y=y+0;
}
//System.out.println(" -> "+s+"  "+t);			
			//  making strings of equal length does not work ???
			// making the strings to equal length, filling with blanks, in order
			// to avoid pathological similarity if one of the strings is much shorter than the other
			/*
			int sL = Math.max(n,m);
			for ( i=0;i<sL;i++){
				if (i>=s.length()){
					s=s+" ";
				}
				if (i>=t.length()){
					t=t+" ";
				}
			}
			y=t.length();
			y=s.length();
			n=sL; m=sL;
			d = new double[n + 1][m + 1];
			 */
			
			minLengthST = Math.min(s.length(),t.length()) ;
			maxLengthST = Math.max(s.length(),t.length()) ;
			
			// checking the similarity by set operation "intersection", without referring to order, and doubles
			if ((initialEqualityCharCount>0) && (s!=null) && (t!=null)){
				// convert max n chars of both strings to a set, then compare
				
				z1 = initialEqualityCharCount;
				z2 = initialEqualityCharCount;
				
				if (initialEqualityCharCount>s.length()){
					z1 = s.length();
				}
				if (initialEqualityCharCount>s.length()){
					z2 = t.length();
				}
				
				if ((z1>0) && (z2>0)){
					str1 = s.substring(0,z1); 
					str2 = t.substring(0,z2);
					
					isz = setutils.intersectionSize(str1,str2);
				}
				
				cost = (double)((z1+z2)/2.0)* Math.sqrt(initialEqualityCharCount+0.5)/5.0 +(double)((initialEqualityCharCount-isz)/initialEqualityCharCount)/2.0;
				c = cost;
				k = (int) Math.min( initialEqualityCharCount , minLengthST ); 
				for (i=0;i<k;i++){
					s_i = s.substring(i, i+1); 
					t_j = t.substring(i, i+1);
					
					if (s_i.contentEquals(t_j)==true){
						
						cost = cost - c/k ;
					}
				}
				
				
				// if (isz < Math.max(0.00001,(double)(((z1+z2)/2.0)-1.0))){ // if ==3 then <0.5 ?
				if (isz<=0){
					return Math.max(s.length(),t.length() ) ;
				}
				
				initEQcost = cost;
			}
			
			if (EqualitySubStrLength>0){
				 
				lcs = longestCommonSubstr(s,t);
				if (lcs.length() < EqualitySubStrLength){
					editcost = maxLengthST - (lcs.length()+0.8); 
					abbreviatedComparisons++;
					return editcost;
				}
			}
			

			fullComparisons++; 
			
			// Step 2
			for ( i = 0; i <= n; i++) {
				d[i][0] = i;
			}

			for ( j = 0; j <= m; j++) {
				d[0][j] = j;
			}

			// Step 3
			RowMinPos = new int[n+1] ;
			RowChangeType = new int[n+1] ;
			RowMin = new double[n+1] ;
			
			totalcost = 0.0;
			
			// n < m !!
			for (i = 1; i <= n; i++) {
				// Step 4
				currentRowMin = 99999.0;
				s_i = s.substring(i - 1, i);
				
				for (j = 1; j <= m; j++) {
					// Step 5
					t_j = t.substring(j - 1, j);
					
					if (t_j.contentEquals(s_i)) {
						cost = 0;
						 

					} else {
						// t_j <> s_i 
						cost = 1.0; 
						
					}

					// Step 6
					v = (double)Math.min( Math.min( d[i - 1][j] + 1, d[i][j - 1] + 1),  d[i - 1][j - 1] + cost);
					
					v = (double)Math.round(v*1000)/1000;
					d[i][j] = v ;
					
					uc = d[i][j - 1] + 1;
					lc = d[i - 1][j] + 1 ;
					dc = d[i - 1][j - 1] + cost; // change
					
					min = Math.min( Math.min( lc, uc), dc);
					
					
					 
					 
					min = (double)Math.round(min*100)/100.0;
					d[i][j] = min ;
					
					
					// storing, on which position we have the min
					if (currentRowMin > min){
						currentRowMin = min ;
					 	 
						RowChangeType[i] = y; 
						RowMinPos[i] = j;
						 
					}
					// get last pos with min value
					lminpos = arraylastMinPos( d[i],currentRowMin,0 );// if (i==1){ lminpos = 0;}else{ }
					RowMinPos[i] = lminpos ;
					if (lminpos<RowMin.length){
						RowMin[lminpos] = currentRowMin ; // min - arraysum(RowMin,0,i-1);
					}
					t_j = t_j + "";
				}
				 
				if (i+ins<lminpos){
					yc = "i"; y=1;
					ins++ ;
				}
				if (i>lminpos-del){
					yc = "d"; y=2;
					del++;
				}
				if (i==lminpos+ins-del){
					yc = "c"; y=3;
					
					t_jj = t.substring(lminpos - 1, lminpos);
					if (s_i.contentEquals(t_jj)==false){
						chg++;
					}
				}
				totalcost = min ;
				j=0;  s_i = s_i+""; lminpos = lminpos+0;
				
				if (currentRowMin/s.length()>0.4){
					d[n][m] = currentRowMin ;
					flythroughBreak++;
					break ;
				}
			}
			// Step 7
			
			// the raw edit cost
			editsCount = (int)Math.round( d[n][m] );
			
			// correcting for very short strings, or large difference in string length
			if (editsCount>= 0.5*(minLengthST)){
				if (lcs.length()==0){
					lcs = longestCommonSubstr(s,t);
				}
				editsCount = editsCount + Math.max(0,editsCount- lcs.length()) ;
			}
			
			if ( ((double)minLengthST< (double)(0.42*maxLengthST)) || 
			     ((minLengthST<=4) &&(maxLengthST-minLengthST>3)) ) {
				isz = setutils.intersectionSize(s,t);
				editsCount = editsCount + ((minLengthST+maxLengthST)/2-isz) ;
				
			}
			
			// addCostonFirstPosition is additive, as the name tells...
			// if the first position is equal in s,t then reduce the cost
			cost = 0.0 ;
			
			s_i = s.substring(0, 1); 
			t_j = t.substring(0, 1); 
			if (s_i.contentEquals(t_j)==false){
				  cost = addCostonFirstPosition;
			} else {
				  cost = - (Math.min(1.0, addCostonFirstPosition/(3.0))) ;
			}

			// entailment
			// a VERY primitive "stemmer", checking for entailment if we shorten the strings
			n = Math.max(1, s.length()/4);
			m = Math.max(1, t.length()/4);
			s_i = s.substring(0, n); 
			t_j = t.substring(0, m); 
			
			// .........
			adjustonEntail =  adjustCostonEntailment;
			
			if ( (((s.contains(t)) || (t.contains(s))) == false) && 
				 (((s_i.contains(t_j)) || (t_j.contains(s_i))) == false) ) {
				adjustonEntail = 1.0 ;
			}
			
			
			// first version of adjusted edit costs .........
			
			editcost =  (d[n][m] + cost + initEQcost + (ins * costbyAction.get("i")) + (del*costbyAction.get("d") + (chg*costbyAction.get("c"))) );
			editcost =  editcost * adjustonEntail ;
			
			// adjusting by longest common substring
			isCostadj = 1.0;
			if (adjustbyInverseStemming>0){
				
				if (lcs.length()==0){
					lcs = longestCommonSubstr(s,t);
				}
				if ((s.indexOf(lcs)==0) && (t.indexOf(lcs)==0)){
					isCostadj = adjustbyInverseStemming + ((1.0 - adjustbyInverseStemming) * (double)(lcs.length()/((s.length()+t.length())/2.0)));
					
					// a special mutual strengthening of common substring and occurence at pos 1
					if (cost<0){
						isCostadj = Math.max(0.1,isCostadj - ( Math.max(0.1, (double)(cost/isCostadj)))) ;
					}
					
					v = ((double)(1.0*lcs.length())/(double)(maxLengthST *1.0));
					c = ((double)(1.0*lcs.length())/(double)(minLengthST *1.0));
				 	
					// a special valuation acc. to the length of the common subsequence, and
					// a special valuation, if the common substring is identical to one of the string
					ca = (double)c - (double)(0.3/(double)v);
					if (ca<0.6){
						ca = 0.6;
					}
					isCostadj = isCostadj * (ca) ;
					
					editcost =  editcost * isCostadj ;
				}
				
			}
			 
			// editsCount == getLevenshteinRaw(s, t);
			
			dv = (editsCount + editcost)/2.0 ;
			dv = Math.max(0.0,dv);
			return dv;
		}
		
		
		private int arraylastMinPos(double[] values , double minvalue , int s ){
			int pos = -1;
			double min = 99999999.0;
			
			for (int i=s;i<values.length;i++){
				
				if (min>=values[i]){
					min = values[i] ;
					pos = i;
				}
				if ((i>s) && (values[i]>min)){
					break;
				}
				
			} // i->
			
			
			return pos;
		}
		
		private double arraysum( double[] values,int s, int t){
			double sum=0.0;
			for (int i=s;i<t;i++){
				sum = sum + (double)values[i];
			}
			return sum;
		}
	}

	
}

package org.NooLab.utilities;

 

import org.apache.commons.lang3.*;
import java.util.Arrays;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;


import org.NooLab.utilities.strings.*;



public class ArrUtilities {

	
	StringsUtil strgutil ;
	
	public ArrUtilities(){
		strgutil = new StringsUtil();
	}


	public String arr2text( float[] vector, int digits ){
		String return_value="", hs1;
		int i;
		
		 for ( i = 0; i < vector.length; i++) {
			 hs1  = String.format("%."+digits+"f", vector[i]);
			 return_value = return_value + " " + hs1; 
		 }
		return return_value;
	}
	
	public String arr2text( float[] dweights ){
		String return_value="";
		int i;
		
		 for ( i = 0; i < dweights.length; i++) {
			 return_value = return_value + " " + dweights[i]; 
		 }
		return return_value;
	}
	
	public String arr2text( double[] dweights ){
		String return_value="";
		int i;
		
		 for ( i = 0; i < dweights.length; i++) {
			 return_value = return_value + " " + dweights[i]; 
		 }
		return return_value;
	}

	public String arr2text( double[] vector , int fracdigits){
		return arr2Text(vector , fracdigits);
	}
	
	public static String arr2Text( double[] vector ,
	                        	   int fracdigits){
		
		String return_value="";
		int i;
		
		if (vector==null){
			return "";
		}
		for ( i = 0; i < vector.length; i++) {
			 return_value = return_value + " " + String.format("%."+fracdigits+"f", vector[i]); 
		 }
		return return_value.trim().replace(",",".");
	}

	public String arr2text(ArrayList<Double> vector, int fracdigits) {
		return arr2Text(vector, fracdigits);
	}
	public static String arr2Text(ArrayList<Double> vector, int fracdigits) {
		String return_value = "";
		int i;

		if (vector == null) {
			return "";
		}
		for (i = 0; i < vector.size(); i++) {
			return_value = return_value + "  " + String.format("%." + fracdigits + "f", vector.get(i));
		}
		return return_value.trim().replace(",", ".");
	}

	public String arr2text(int[] vector, int fromIndex, int toIndex) {
	
		String return_value="";
		int i;
		

		if (fromIndex<0){
			fromIndex=0;
		}
		if (toIndex<0){
			toIndex=0;
		}
		if (toIndex>vector.length-1){
			toIndex = vector.length-1 ;
		}
		if (fromIndex>toIndex){
			return "";
		}
		
		
		 for ( i = fromIndex; i <= toIndex; i++) {
			 return_value = return_value + " " + vector[i]; 
		 } 
		 
		return return_value;
	}
	
	public String arr2text(ArrayList<Integer> selectedSet) {
		String return_value ="";
		int n;
		int[] ints = new int[selectedSet.size()];
		Object[] objints = new Object[selectedSet.size()];;
		
		objints = selectedSet.toArray();
		
		for ( int i = 0; i <  objints.length; i++) {
			 return_value = return_value + " " + ((Integer)objints[i]).intValue(); 
		 } 
		  
		return return_value ;
	}
	
	public String arr2text( int[] vector ){

		return arr2text( vector, 0, vector.length-1 );
	}
	

	public String arr2text( String[] vector ){
		String return_value="";
		int i;
		
		 for ( i = 0; i < vector.length; i++) {
			 return_value = return_value + vector[i]; 
			
			 if (i<vector.length-1){
				 return_value = return_value + " " ;
			 }
			 
		 }
		return return_value.trim() ;
	}

	public String arr2text( String[] vector,
	                        String separator){
		
		String return_value="";
		int i;
		
		if (separator.length()==0){
			separator="\t";
		}
		
		 for ( i = 0; i < vector.length; i++) {
			 
			 return_value = return_value + vector[i];
			 
			 if (i<vector.length-1){
				 return_value = return_value + separator ;
			 }
		 }
		return return_value;
	}
	
	

	
	public String arr2text( double[] vector,
	                        int fracdigits,
	                        boolean trim_trailingzeroes,
	                        String separator){
		
		String return_value="",hs1;
		int i;
		
		try{
			
			if (separator.length()==0){
				separator=" ";
			}
			
			for ( i = 0; i < vector.length; i++) {
				 hs1 = String.format("%."+fracdigits+"f",vector[i]);

				 if (hs1==null){
					 hs1="" ;
				 }
				 
				 if (trim_trailingzeroes==true){
					hs1 = strgutil.trimtrailingzeroes(hs1); 
					 
				 }
				 return_value = return_value + hs1; 
				 
				 if (i<vector.length-1){
					 return_value = return_value + separator ;
				 }
			}
			return_value = return_value.replace(",", ".");
			
		}catch(Exception e){
			e.printStackTrace() ;
		}

		return return_value;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TreeMap<String,String> buildMap( TreeMap map , String[] arr1, String[] arr2){
		
		for (int i=0;i<arr1.length;i++){
			map.put(i+": "+arr1[i], arr2[i]);
		}
		
		return map;
	}
	
	public String map2text( Map<String,String> map, 
					        String itemseparator,
					        String groupseparator,
					        boolean usebrackets ){
		String str = "",itstr, kstr, vstr;
		int n,z;
		
		n = map.size();
		z = 0;
		for (Map.Entry<String, String> entry : map.entrySet()) {
        	kstr = entry.getKey() ; 
       		vstr = entry.getValue();
       		
       		itstr = kstr+itemseparator+vstr;
       		
       		if (usebrackets){
       			itstr = "("+itstr+")";
       		}
       		str = str + itstr ;
       		
       		if (z<n-1){
       			str = str + groupseparator ;
       		}
        }
		
		return str;
	}
	
	public boolean deleteMapEntry( Map<String,String> dmap, String keystr, String objstr){
		String str ;
		boolean rb = false;
		
		str = dmap.remove( keystr );
		
		if (str.contentEquals(objstr)){
			if (dmap.containsKey(str)==false){
				rb = true;
			}
		}
		
		return rb;
		/*
		for (Iterator<Map.Entry<String,Boolean>> i = myMap.entrySet().iterator(); i.hasNext(); ) {  
    		Map.Entry<String,Boolean> entry = i.next();  
    		if (!entry.getValue()){  
        		String o = entry.getKey();  
        		i.remove();  
        		System.out.println("key with False-Value "+ o);  
    		}  
		}  
		
		*/
	}
	
	public double[] textArrValues( String[] strings  ){
		
		return textArrValues( strings, 0, strings.length );
	}

	/**
	 * 
	 * returns null if a problem occurred
	 * 
	 * @param strings
	 * @param offset
	 * @return
	 */
	public double[] textArrValues( String[] strings, int offset  ){
		double[] dv;
		try{
		
			dv = textArrValues( strings, offset, strings.length );
			
		}catch(Exception e){
			dv = null ;
		}
		
		return dv;
	}
	
	
	public double[] textArrValues( String[] strings, int offset, int limit ){
		
		double[] values = new double[ limit-offset];
		String str;
		
		
		for (int i=0;i<values.length;i++){
			
			if (i<strings.length){
				str = strings[i+offset] ;
				values[i] = Double.parseDouble(str) ;
			}
			
		} // i ->
		
		
		return values;
	}
	
	
	public String[] values2StringArr( double[] values ){
		
		String[] strings = new String[values.length];
		
		
		
		return strings ;
	}
    
    
	public String[] changeArrayStyle( Vector<String> strvec , String e ){
		String[] strarr ;
		 
		strarr = new String[strvec.size()];
		if (strvec.size() > 0) {
			for (int i = 0; i < strvec.size(); i++) {
				strarr[i] = strvec.get(i);
			}
		}

		return strarr ;
	}
	
 
	
	public double[] changeArrayStyle( Vector<Double> vvec, double i ){
		double[] strval ;
		 
		strval = new double[vvec.size()];
		if (vvec.size() > 0) {
			for (int k = 0; k < vvec.size(); k++) {
				strval[k] = vvec.get(k);
			}
		}
		return strval ;
	}
	
	public double[] changeArrayStyle( ArrayList<Double> vvec ){
		double[] strval ;
		 
		 
		strval = new double[vvec.size()];
		if (vvec.size() > 0) {
			for (int k = 0; k < vvec.size(); k++) {
				strval[k] = vvec.get(k);
			}
		}
		return strval ;
	}

	public int[] changeArrayStyle( Vector<Integer> strvec, int i ){
		int[] strval ;
		 
		strval = new int[strvec.size()];
		if (strvec.size() > 0) {
			for (int k = 0; k < strvec.size(); k++) {
				strval[k] = strvec.get(k);
			}
		}

		return strval ;
	}

	/**
	 * inclusive type conversion
	 * 
	 * @param ivalues
	 * @param i
	 * @return
	 */
	public ArrayList<Double> changeArrayStyle( ArrayList<Integer> ivalues, int i ){
		ArrayList<Double>  dval = new ArrayList<Double>();
		 
		if (ivalues.size() > 0) {
			for (int k = 0; k < ivalues.size(); k++) {
				dval.add( (double)ivalues.get(k) );
			}
		}

		return dval ;
	}
	
	public long[] changeArrayStyle( Vector<Long> strvec, long i ){
		long[] strval ;
		 
		strval = new long[strvec.size()];
		if (strvec.size() > 0) {
			for (int k = 0; k < strvec.size(); k++) {
				strval[k] = strvec.get(k);
			}
		}

		return strval ;
	}
	
	public static int[] changeArraystyle(  ArrayList<Integer> values) {
		
		int[] vi = new int[values.size()];
		
		for(int i=0;i<vi.length;i++){
			vi[i] = values.get(i) ;
		}
		
		return vi;
	}
	public static ArrayList<Double> changeArraystyle(double[] values) {
		ArrayList<Double> xa = new ArrayList<Double>();
		
		for (int i=0;i<values.length;i++){
			xa.add(values[i]) ;
		}
		return xa;
	}
	
	public static ArrayList<Integer> changeArraystyle(int[] values) {
		ArrayList<Integer> xa = new ArrayList<Integer>();
		
		for (int i=0;i<values.length;i++){
			xa.add(values[i]) ;
		}
		return xa;
	}
	public ArrayList<Double> changeArrayStyle(double[] values) {
		return changeArraystyle(values);
	}

	public ArrayList<Integer> changeArrayStyle( int[] values) {
		return changeArraystyle(values);
	}

	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ArrayList<Integer> importObjectedIntList( Object objindexes){
		ArrayList<Integer> ixesL = new ArrayList<Integer>();
		int[] ixesA ;
		int dtype= -1;

		try{
			dtype = 1;
			ixesL = (ArrayList<Integer>)objindexes;
			
		}catch(Exception e){ 
			dtype = -2;
			 
		}
		
		if (dtype<0){
			try{
				dtype = 2;
				ixesA = (int[])objindexes;
				ixesL = new ArrayList(Arrays.asList(ixesA));
			
			}catch(Exception e){ 
				dtype = -3;
		 
			}
		}
		
		return ixesL;
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	public int[] importObjectedIntArr( Object objindexes){
		ArrayList<Integer> ixesL = new ArrayList<Integer>();
		int[] ixesA = new int[0];
		int dtype= -1;

		try{
			dtype = 1;
			ixesL = (ArrayList<Integer>)objindexes;
			
			ixesA = new int[ixesL.size()];
			for (int i=0;i<ixesL.size();i++){
				ixesA[i] = (int)ixesL.get(i) ;
			}
			
		}catch(Exception e){ 
			dtype = -2;
			 
		}
		
		if (dtype<0){
			try{
				dtype = 2;
				ixesA = (int[])objindexes;
				
			
			}catch(Exception e){ 
				dtype = -3;
		 
			}
		}
		
		return ixesA;
	}
	
	/**
	 * 
	 * expects a series (dimension-0) of intervals, that are given as min-max
	 * in dimension-1, where the min is expected at index=0 and max at the last 
	 * position with index = len-1
	 * 
	 * @param v the values
	 * @param intervals the 2-dimensional array borderMode
	 * @param borderMode  0= borders included,  
	 *                    1=left border excluded, 
	 *                    2=right border excluded, 
	 *                    3=both borders excluded
	 * @return
	 */
	public int intervalIndexOf( double value, double[][] intervals, int borderMode) {
		 
		int position=-1;
		double min,max ;
		int kp;
		boolean hb;
		
		if ((intervals==null) || (intervals.length==0)){
			return -3 ;
		}
		if (intervals[0].length==0){
			return -4 ;
		}
		if (borderMode<0)borderMode=0;
		if (borderMode>3)borderMode=3;
		
		kp = intervals[0].length-1;
		          
		for (int i=0;i<intervals.length;i++){
			
			min = intervals[i][0];
			max = intervals[i][kp];
			
			hb = false;
			switch (borderMode) {
				case 0: { hb = (value>=min) && ( value<=max ); }; 
				case 1: { hb = (value> min) && ( value<=max ); };
				case 2: { hb = (value>=min) && ( value< max ); };
				case 3: { hb = (value> min) && ( value< max ); };
			}
			
			if (hb){
				position = i;
				break;
			}

		} // i->
		
		return position;
	}

	
	public int arrValuePos( double[] valarr, double value){
		
		int pos=-1;
		
		for (int i=0;i<valarr.length;i++){
			if (valarr[i] == value){
				pos = i;
				break;
			}
		}
		
		return pos;
		
	}
	
	public int arrValuePos( int[] valarr, double value){
		return arrValuepos(valarr, value);
	}
	public static int arrValuepos( int[] valarr, double value){
		
		int pos=-1;
		
		for (int i=0;i<valarr.length;i++){
			if (valarr[i] == value){
				pos = i;
				break;
			}
		}
		
		return pos;
		
	}
	
	public int arrValuePos(String[] valarr, String str, int mode) {
		int pos=-1;
		boolean hb;
		
		if (str.length()==0){
			return pos;
		}
		if (valarr.length==0){
			return pos;
		}

		for (int i=0;i<valarr.length;i++){
			
			hb = true;
			if (mode<=0){
				hb = (valarr[i].contentEquals(str));
			}
			if (mode==1){
				hb = (valarr[i].toLowerCase().contentEquals(str.toLowerCase()));
			}

			if (mode==3){
				hb = (valarr[i].toLowerCase().startsWith( str.toLowerCase()));
							}
			if (mode==4){
				hb = (valarr[i].toLowerCase().contains( str.toLowerCase()));
				if (hb==false){
					// reverse
				}

			}
			
			if (mode==6){
				int len = (int) (str.length() *0.7);
				if (len<1)len=1;
				String str0 = str.substring(0,len);
				hb = (valarr[i].toLowerCase().startsWith( str0.toLowerCase()));
				
			}
			if (mode==7){
				int len = (int) (str.length() *0.7);
				if (len<1)len=1;
				String str0 = str.substring(0,len);
				hb = (valarr[i].toLowerCase().contains( str0.toLowerCase()));
				
				if (hb==false){
					// reverse
				}
				
			}

			
			if (hb){
				pos = i;
				break;
			}
		}
		
		return pos;
	}
	
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

	public int arrayMin( int[] valarr ){
		return arrayMin( valarr ,-1);
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


	public double arrayMin( Vector<Double> valarr, double defaultValue){
		
		if ((valarr==null) || (valarr.size()==0)){
			return defaultValue;
		}
		
		
		double[] varr ;
		
		varr = this.changeArrayStyle(valarr, 0.0) ;
		
		return arrayMin( varr, defaultValue) ;
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

	public double arrayMax( double[] valarr){
		return arrayMax( valarr, -1.0) ;
	}
	
	public double arrayMax( double[] valarr, double defaultValue){
		double return_value=0.0, max = -9999999999.9;
		int i;
		
		i=0;
		for ( i = 0; i < valarr.length; i++) {
			  if (max<valarr[i]){
				  max = valarr[i] ;
			  }
		}
		if (max == -9999999999.9){
			max = defaultValue;
		}
		return_value = max ;
		
		return return_value;		
		
		
	}

	public static int arraymax( int[] valarr ){
		return arraymax( valarr ,-1);
	}
	
	public int arrayMax( int[] valarr ){
		return arrayMax( valarr ,-1);
	}
	


	public int arrayMax( int[] valarr, int defaultValue){
		return arraymax( valarr ,-1);
	}
	public static int arraymax( int[] valarr, int defaultValue){
		int return_value = defaultValue, max =  -999999999;
		int i;
		
		if ((valarr==null) || (valarr.length==0)){
			return return_value;
		}
		i=0;
		for ( i = 0; i < valarr.length; i++) {
			  if (max<valarr[i]){
				  max = valarr[i] ;
			  }
		}
		if (max ==  999999999){
			max = defaultValue;
		}
		return_value = max ;
		
		return return_value;		
		
		
	}
	

	public double arrayMax( Vector<Double> valarr, double defaultValue){
		
		if ((valarr==null) || (valarr.size()==0)){
			return defaultValue;
		}
		 
		double[] varr ;
		
		varr = this.changeArrayStyle(valarr, 0.0) ;
		
		return arrayMax( varr, defaultValue) ;
	}
	
	public int arrayMax( Vector<Integer> valarr, int defaultValue){
		
		if ((valarr==null) || (valarr.size()==0)){
			return defaultValue;
		}
		 
		int[] varr ;
		
		varr = this.changeArrayStyle(valarr, 0) ;
		
		return arrayMax( varr, defaultValue) ;
	}
	
	public int arrayMaxPos( ArrayList<Double> valarr  ){
		Vector<Double> vvalarr = new Vector<Double> (valarr) ;
		return arrayMaxPos(vvalarr);
	}
	public int arrayMaxPos( Vector<Double> valarr  ){
		
		int pos =-1;
		double defaultValue, maxval;
		
		if ((valarr==null) || (valarr.size()==0)){
			return -1;
		}
		 
		double[] varr ;
		
		varr = this.changeArrayStyle(valarr, 0.0) ;
		
		maxval = arrayMax( varr, 0.0) ;
		
		pos = arrValuePos( varr, maxval);
		
		return pos ;
	}
	

	public int arrayMaxPos( Vector<Integer> valarr, int marker ){
		
		int pos =-1;
		int defaultValue, maxval;
		
		if ((valarr==null) || (valarr.size()==0)){
			return -1;
		}
		 
		int[] varr ;
		
		varr = this.changeArrayStyle(valarr, 0) ;
		
		maxval = arrayMax( varr, 0) ;
		
		pos = arrValuePos( varr, maxval);
		
		return pos ;
	}
	

	public int arrayMaxPos( double[] valarr, double defaultValue  ){
		
		int pos =-1;
		double maxval;
		
		if ((valarr==null) || (valarr.length==0)){
			return -1;
		}
		  
		maxval = arrayMax( valarr,0.0 ) ;
		
		pos = arrValuePos( valarr, maxval);
		
		return pos ;
	}
	
	public static int arrayMaxpos( int[] valarr  ){
		int pos =-1;
		double maxval;
		
		if ((valarr==null) || (valarr.length==0)){
			return -1;
		}
		  
		maxval = arraymax( valarr ) ;
		
		pos = arrValuepos( valarr, maxval);
		
		return pos ;
		
	}
	public int arrayMaxPos( int[] valarr  ){
		
		return arrayMaxpos(valarr)  ;
	}
	
	public int arrayMinPos( int[] valarr  ){
		
		int pos =-1;
		double  maxval;
		
		if ((valarr==null) || (valarr.length==0)){
			return -1;
		}
		  
		maxval = arrayMin( valarr ) ;
		
		pos = arrValuePos( valarr, maxval);
		
		return pos ;
	}
	
	public int arrayMinPos( double[] valarr  ){
		
		int pos =-1;
		double  maxval;
		
		if ((valarr==null) || (valarr.length==0)){
			return -1;
		}
		  
		maxval = arrayMin( valarr ,0.0) ;
		
		pos = arrValuePos( valarr, maxval);
		
		return pos ;
	}
	public int arraySum( int[] int_arr){
		int return_value=0;
		int i;
		
		i=0;
		for ( i = 0; i < int_arr.length; i++) {
			 return_value = return_value + int_arr[i];
		}
		return return_value;		
	}

	
	public double arraySum( double[] d_arr){
		return arraysum(d_arr);
	}

	public static double arraysum( double[] d_arr){

		double return_value=0;
		int i;
		
		i=0;
		for ( i = 0; i < d_arr.length; i++) {
			 return_value = return_value + d_arr[i];
		}
		return return_value;		
		
		
	}
	
	public int arraySum( ArrayList<Integer> int_arr){
		int return_value=0;
		int i;
		
		i=0;
		for ( i = 0; i < int_arr.size(); i++) {
			 return_value = return_value + int_arr.get(i);
		}
		return return_value;		
	}
	public int arraySum( Vector<Integer> int_arr){
		int return_value=0;
		int i;
		ArrayList<Integer> int_arl = new ArrayList<Integer>(int_arr);

		return arraySum(int_arl);		
	}

	
	public int valueFrequency( Vector<Integer> vilist, int dpi) {
		int freq = 0;
		
		for (int i=0;i<vilist.size();i++){
			if (vilist.get(i)==dpi){
				freq++;
			}
		}
		
		return freq;
	}


	public double[][] arrayTranspose(double[] values) {
	
		return null;
	}


	public double[][] arrayTranspose(double[][] values) {
		double[][] tdata = new double[0][0];
		double v;
		int c,r ;
		
		try{
			
			c = values.length ;
			r = values[0].length ;
			
			tdata = new double[r][c];
			
			for (int i=0;i<c;i++){
				
				for (int j=0;j<r;j++){
					v = values[i][j] ;
					tdata[j][i] = v;
				}
			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return tdata;
	}


	public int[] arrScale(int[] arrvalues, int scaleratio) {
		int[] resultValues = new int[arrvalues.length] ;
		
		
		for (int i=0;i<arrvalues.length;i++){
			resultValues[i] = (int) Math.round( (double)arrvalues[i] * scaleratio) ;
		}
		return resultValues;
	}


	public boolean intervalContains( int value, Map<Integer,Integer> intervals){
		boolean rb=false, hb;
		
		int n,z, v1,v2;
		
		n = intervals.size();
		z = 0;
		for (Map.Entry<Integer,Integer> entry : intervals.entrySet()) {
        	v1 = entry.getKey() ; 
       		v2 = entry.getValue();
       		
       		hb = ((value>=v1) && (value<=v2));
       		
       		if (hb){
       			rb = hb ;
       			break ;
       		}
		}
		
		return rb;
	}
	
	public int getIntervalBorder( Map<Integer,Integer> intervals, int valueInside, String xSide){
		int value = -1;
		boolean hb=false ;
		
		xSide = xSide.toUpperCase().substring(0,1);
		
		int n,z, v1,v2;
		
		n = intervals.size();
		z = 0;
		for (Map.Entry<Integer,Integer> entry : intervals.entrySet()) {
        	v1 = entry.getKey() ; 
       		v2 = entry.getValue();
       		
       		hb = ((valueInside>=v1) && (valueInside<=v2));
       		
       		if (hb){
       			 
       			if (xSide.contentEquals("R")){
       				value = v2 ;
       			}
       			if (xSide.contentEquals("L")){
       				value = v1 ;
       			}
       		}
		}
		
		
		return value;
	}
	
	public String[] resizeArray( int _new_size,
	                             String original[],
	                             int firstPosition) {
	    int length,p=0,_max;
	    String[] newArray;
	    
	    if (original==null){return null;}
	    if (_new_size<0){return original;}
	    

	    
	    newArray = new String[_new_size];

	    length = original.length;
	    
	    if (_new_size>0){
		      if ( length==_new_size ){
		    	  newArray = original;
		      }
		      else {
		    	  
		    	  _max = _new_size ; //+ firstPosition;
		    	  
		    	  for (p=0;p<_max;p++){
		    		  if (p<newArray.length){
		    			  if (p+firstPosition<original.length){
		    				   newArray[p] = original[p+firstPosition];
		    			  }
		    			  else{
		    				  newArray[p] = "";
		    			  }
		    		  }
		    	  }
		      }
	    }
	    else
	    {
	    	
	    	newArray=null;
	    }
	    return newArray;
		
	}
	
	public String[] resizeArray( int _new_size, String original[]) {
		    int length,p=0;
		    String[] newArray;
		    
		    if (original==null){return null;}
		    if (_new_size<0){return original;}
		    
		    newArray = new String[_new_size];

		    length = original.length;
		    if (_new_size>0){
			      if ( length==_new_size ){
			    	  newArray = original;
			      }
			      else {
			    	// this crashes if the new array is smaller....  System.arraycopy(original, 0, newArray, 0, length);
			    	   for (p=0;p<_new_size;p++){
			    		   if (p<newArray.length){
			    			   if (p<original.length){
			    				   newArray[p] = original[p];
			    			   }
			    			   else{
			    				   newArray[p] = "";
			    			   }
			    		   }
			    	   }
			      }
		    }
		    else
		    {
		    	
		    	newArray=null;
		    }
		    return newArray;
	}
	 	
	
	public double[] resizeArray( int _new_size, double[] original) {
		    int length,p=0;
		    double[] newArray;

		    if (original==null){return null;}
		    if (_new_size<0){return original;}
		    
		    newArray = new double[_new_size];

		    length = original.length;
		    if (_new_size>0){
			      if ( length==_new_size ){
			    	  newArray = original;
			      }
			      else {
			    	// this crashes if the new array is smaller....  System.arraycopy(original, 0, newArray, 0, length);
			    	   for (p=0;p<_new_size;p++){
			    		   if (p<newArray.length){
			    			   if (p<original.length){
			    				   newArray[p] = original[p];
			    			   }
			    			   else{
			    				   newArray[p] = 0;
			    			   }
			    		   }
			    	   }
			      }
		    }
		    else
		    {
		    	
		    	newArray=null;
		    }
		    return newArray;   
	}

	
	public int[] resizeArray( int _new_size, int[] original) {
	    int length,p=0;
	    int[] newArray;

	    if (original==null){return null;}
	    if (_new_size<0){return original;}
	    
	    newArray = new int[_new_size];

	    length = original.length;
	    if (_new_size>0){
		      if ( length==_new_size ){
		    	  newArray = original;
		      }
		      else {
		    	// this crashes if the new array is smaller....  System.arraycopy(original, 0, newArray, 0, length);
		    	   for (p=0;p<_new_size;p++){
		    		   if (p<newArray.length){
		    			   if (p<original.length){
		    				   newArray[p] = original[p];
		    			   }
		    			   else{
		    				   newArray[p] = 0;
		    			   }
		    		   }
		    	   }
		      }
	    }
	    else
	    {
	    	
	    	newArray=null;
	    }
	    return newArray;   
}

	
	public void trimRight( String[] strarr){
		
	}
	
	public String[] rewriteFilteredArray( String[] arr, int[] filter){
		String[] newarr  ;
		int p,z,i, removals=0;
		
		// this could be cached...
		if ((filter!=null) && (filter.length>0)){
			for (i=0;i<filter.length;i++){
				if (filter[i]<=0){
					removals++;
				}
			}
		}
		
		// creating the new array using the correct size param
		p = arr.length-removals; 
		newarr = new String[p] ;
		
		z=-1;
		for (i=0;i<arr.length;i++){
			
			if (  ((i<filter.length) && (filter[i]>=1)) ||
				  (i>filter.length) ){
				z++;
				newarr[z] = arr[i];
			}
			
			
		} // i-> all of original array positions
		
		
		
		return newarr ;
	}
	
	
	

	
	public double[] rewriteFilteredArray( double[] arr, int[] filter){
		double[] newarr  ;
		int p,z,i, removals=0;
		
		// this could be cached...
		if ((filter!=null) && (filter.length>0)){
			for (i=0;i<filter.length;i++){
				if (filter[i]<=0){
					removals++;
				}
			}
		}
		
		// creating the new array using the correct size param
		p = arr.length-removals; 
		newarr = new double[p] ;
		
		z=-1;
		for (i=0;i<arr.length;i++){
			
			if (  ((i<filter.length) && (filter[i]>=1)) ||
				  (i>filter.length) ){
				z++;
				newarr[z] = arr[i];
			}
			
			
		} // i-> all of original array positions
		
		
		
		return newarr ;
	}
	
	public double[] clearArray(double[] arr){
		int k;
		
		if (arr==null){return null;}
		
		for (k=0;k<arr.length;k++){
			arr[k]=0;
		}
		
		return arr;
	}
	
	/**
	* Real locates an array with a new size, and copies the contents
	* of the old array to the new array.
	* @param oldArray  the old array, to be reallocated.
	* @param newSize   the new array size.
	* @return          A new array with the same contents.
	*/
	@SuppressWarnings("unused")
	private static Object resizeArray (Object oldArray, int newSize) {
	   int oldSize = java.lang.reflect.Array.getLength(oldArray);
	   
	   Class elementType = oldArray.getClass().getComponentType();
	   
	   Object newArray = java.lang.reflect.Array.newInstance(  elementType,newSize);
	   
	   int preserveLength = Math.min(oldSize,newSize);
	   
	   if (preserveLength > 0)
	      System.arraycopy (oldArray,0,newArray,0,preserveLength);
	   
	   return newArray; 
	}

	public double[] arraycopy( double[] arr, int offset){
		
		int r;
		 
		double[] arrout = null ;
		
		 
		arrout = new double[arr.length+offset] ;
			
			for (r=0;r<arr.length;r++){
				arrout[r+offset] = arr[r] ;
			}
			
		 return arrout;			
	}

	public double[] arraycopy( double[] arr){
		int r;
		 
		double[] arrout = null ;
		
		if (arr!=null){ 
			arrout = new double[arr.length];

			for (r = 0; r < arr.length; r++) {
				arrout[r] = arr[r];
			}
		}
		return arrout;		
	}

	public double[] arraycopyT( float[] arr){
		int r;
		 
		double[] arrout = null ;
		
		 
		arrout = new double[arr.length] ;
			
			for (r=0;r<arr.length;r++){
				arrout[r] = arr[r] ;
			}
			
		 return arrout;		
	}

	
	public float[] arraycopy( float[] arr){
		int r;
		 
		float[] arrout = null ;
		
		 
		arrout = new float[arr.length] ;
			
			for (r=0;r<arr.length;r++){
				arrout[r] = arr[r] ;
			}
			
		 return arrout;		
	}
	

	public String[] arraycopy( String[] arr){
		int r;
		 
		String[] arrout = null ;
		
		 
		arrout = new String[arr.length] ;
			
			for (r=0;r<arr.length;r++){
				arrout[r] = arr[r] ;
			}
			
		 return arrout;		
	}

	
	
/*  re-allocating a 2dim array
 *   int a[][] = new int[2][3];
  //...
  a = (int[][])resizeArray(a,20);
    // new array is [20][3]
  for (int i=0; i<a.length; i++) {
     if (a[i] == null)
        a[i] = new int[30];
      else a[i] = (int[])resizeArray(a[i],30); }
   // new array is [20][30]
 * */
	
	



	

	  private byte[] resizeArray( int _new_size,
	                            byte[] original) {
		
		    int length ;
		    byte[] newArray ;
		    
		    if (original==null){
		    	return null ;
		    }
		    length = original.length;
		    newArray = new byte[_new_size];
		    if (_new_size>0){
			      if ( length==_new_size ){
			    	  newArray = original;
			      }
			      else {
			    	  System.arraycopy(original, 0, newArray, 0, length);
			      }
		    }
		    else
		    {
		    	
		    	newArray=null;
		    }
		    return newArray;
		  }
	
	@SuppressWarnings("unused")
	private byte[] inserttoByteArrayAt( int p,
	                                    byte[] insertedbytes,
	                                    byte[] original){
		
		byte[] _arr; 
		int z,sz,i;
		     
		z = insertedbytes.length;
		sz = original.length;
		
		_arr = resizeArray( sz+z, original);
		
		for (i=sz;i>p;i--){
			_arr[i] = _arr[i-1];
			
		}
		for (i=0;i<z;i++){
			_arr[i+p]=insertedbytes[i];
		}
		
		return _arr;
	}
	  


    public  int[] SortArrayWithOrder ( int[] intarray) {
         
        //
        // Sort the points array, the default order is in ascending order.
    
        //
        Arrays.sort( intarray );
        // System.out.println(Arrays.toString(points));
        return intarray;
        //
        // Sort the points array in descending order.
        // [94, 70, 64, 53, 44]
        //
        // Arrays.sort( intarray, Collections.reverseOrder());
        // System.out.println(Arrays.toString(points));
    }


    public  int checkTypeOfListItems( Object arrObject){
    	
    	int result = -1;
    	Object object ;
    	ArrayList<Object> arr;
    	String cname ;
    	try {
			
    		arr = (ArrayList<Object>) arrObject;
    	
    		for (int i=0;i<arr.size();i++){
    			object = arr.get(i); // T1
    			cname = object.getClass().getName();
    			
    		}
    		
    	}catch(Exception e){
    		e.printStackTrace() ;
    	}
    	
    	
    	return result;
    }
    		
    /**
     * 
     * 
     * @param <T>
     * @param arrObject
     * @param clazz
     * @param checkFullFormat false: just checking data type, true: checking if it can be converted 
     * @return
     */
	@SuppressWarnings("unchecked")
	public  <T1,T2>  int checkTypeOfListItems( Object arrObject, 
											 Class<T1> clazzFormatToCheckFor , 
											 Class<T2> clazzFormatToAvoid){
												 

		 
		String emsgstr = null, objcname, className ,targetClassname="";
		T1 object = null ; // T1
		ArrayList<Object> arr;
		T2 targetInstance = null; // T2
		int deniedConversions = 0;
		 
		
		// T object = rqClass.newInstance();
		// set properties via reflection.
		
		try {
			
		arr = (ArrayList<Object>) arrObject;
		
		className = clazzFormatToCheckFor.getSimpleName();
		
		if (clazzFormatToAvoid!=null){
			targetClassname = clazzFormatToAvoid.getSimpleName() ;
		
				//targetInstance = (T2)targetclazz.newInstance();
		}
		
		for (int i=0;i<arr.size();i++){
			object = (T1) arr.get(i); // T1
			
			objcname = object.getClass().getSimpleName();
			
			if (objcname.contentEquals(className)==false){
				return -1;
			}
			
			// conversions from num to String are always possible, 
			// so we need to check just the other way around
			if (className.contentEquals("String")){
				
				try{
					if (targetClassname.toLowerCase().contentEquals("double")){
						double v = Double.parseDouble((String) object ) ;
					}
					if (targetClassname.toLowerCase().startsWith( "int")){
						int vi = Integer.parseInt( (String) object ) ;
					}
					// ;(double)((Double) clazzFormatToAvoid.cast(object));
					// double v = (Double) object;
				}catch(Exception e){
					deniedConversions++;
				}
			}
		}

		} catch ( Exception e) {
			 
			e.printStackTrace();
		}  
	 
		
		return deniedConversions;
	}


	public double[][] changeDimensions(double[][] values, int dimension, int newSize, int offset) {
							   	// other flavors:	defining columns directly : int[] columns
								// 					start, end
		double[][] tdata = new double[0][0] ;
		
		if (dimension==0){
			tdata = new double[newSize][values[0].length];
			
			for (int i=offset;i<newSize+offset;i++){
				System.arraycopy(values[i], 0, tdata[i-offset], 0, values[i].length);
			}
		}
		if (dimension==1){
			tdata = new double[values.length][newSize];

			for (int i=0;i<values.length;i++){
				System.arraycopy(values[i], 0, tdata[i], offset, values[i].length-offset);
			}
			
		}
		
		
		return tdata;
	}

	public double[] fillArray( int steps, double lo, double hi) {
		
		return fillarray( steps, lo, hi);
	}
	
	public static double[] fillarray(int steps, double lo, double hi) {
		double[] values = new double[steps+1];
		double ddx,dx;
		
		try{
			
			dx  = (hi-lo)/((double)steps) ;
			ddx = (hi-lo) ;
			
			values[steps] = hi;
			for (int i=steps-1;i>0;i--){
				values[i] = values[i+1] - (values[i+1]-lo)/2.0;
			}
			values[0] = lo;
			
		}catch(Exception e){
			
		}
		
		
		
		return values;
	}


	public static String arrayToString( double[]... v ) {
		String outStr = "", rStr, vs;

		try{

			for (int i = 0; i < v.length; i++) {
				rStr = "" ;
				for (int j = 0; j < v[i].length ; j++){
					vs = String.format( "%.4f",v[i][j]);
					if (v[i][j]>0)vs=" "+vs ;
					rStr = rStr + vs + " ";
				}
				
				outStr = outStr + rStr ;
				if (i < v.length)
					outStr = outStr + "\n" ;
			}
			
		}catch(Exception e){
			outStr = "failure in attempting to create a string from array !";
		}
		
		return outStr;
	}


	public static String toString(double[]... v) {
		String outStr="" ;
		
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < v.length; i++) {
			for (int j = 0; j < v[i].length - 1; j++)
				str.append(v[i][j] + " ");
			str.append(v[i][v[i].length - 1]);
			if (i < v.length - 1)
				str.append("\n");
		}
		outStr = str.toString() ;
		return outStr ;
	}


	
}

/*



      /**
       * Returns the sum of all the element in the collection. Every element must extend java.lang.Number or this method
      * will throw an exception.
      *
      * @param collection a collection containing only classes extending java.lang.Number
      * @return the sum of all the elements in the collection
      
     public static double sum(final Object collection)
 	      {
         double sum = 0;
         if (collection != null)
	          {
            if (collection instanceof Collection)
 	              {
                 sum = sum(collection);
             }
             else if (collection.getClass().isArray())
 	              {
                 sum = sum(Arrays.asList((Object[])collection));
             }
         }
        return sum;
     }
 
    /**
      * Returns the sum of all the element in the collection. Every element must extend java.lang.Number or this method
      * will throw an exception.
     *
      * @param collection a collection containing only classes extending java.lang.Number
      * @return the sum of all the elements in the collection
      
     public static double sum(final Collection collection)
 	      {
         double sum = 0;
         if (collection != null && !collection.isEmpty())
	          {
             for (final Iterator iterator = collection.iterator(); iterator.hasNext();)
	              {
                 Object object = iterator.next();
               if (object instanceof Number)
 	                  {
                     sum += ((Number)object).doubleValue();
                }
                 else
 	                  {
                     throw new UnsupportedOperationException(
                         "In order to calculate the sum of a collection\'s elements " +
                         "all of them must extend java.lang.Number, found: " + object.getClass().getName());
                 }
             }
        }
         return sum;
     }

*/


















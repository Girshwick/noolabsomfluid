package org.NooLab.somtransform.algo.intf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.NooLab.utilities.objects.StringedObjects;
import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;

public class AlgorithmParameter 
									implements 	AlgorithmParameterIntf, 
												Serializable{

	
	private static final long serialVersionUID = 6381743099970919337L;

	// TODO: should not be "naked" strings, but (pseudo-)maps with a descriptor,
	String label = "";   
	
	String typeLabel = "" ; // as used in instanceOf, class().name
	
	double numValue = -1.0 ;
	String strValue = "" ;

	int[]    intValues = new int[0] ;
	double[] numValues = new double[0] ;
	String[] strValues = new String[0] ;
	
	ArrayList<Object> list = new ArrayList<Object>();

	ArrayList<ValuePair> valuePairs = new ArrayList<ValuePair>() ;
	
	BidiMap map = new DualHashBidiMap();
	/*
	 MapIterator it = map.mapIterator();
 		while (it.hasNext()) {
   			Object key = it.next();
   			Object value = it.getValue();
   			it.setValue("newValue");
 		}
	*/
	/** this object slot allows to pass any class  structure as parameters,
	 * yet, this class has to be declared as "Serializable", and it should 
	 * in fact be serializable (complete list of setters/getters)  
	 */
	public Object obj=null; 
	
	transient StringedObjects strobj = new StringedObjects();
	
	// ========================================================================
	public AlgorithmParameter(){
		
	}
	
	/** for cloning , incl. on the level of the items */
	@SuppressWarnings("unchecked")
	public AlgorithmParameter(AlgorithmParameter inParamSet) {
	
		label = inParamSet.label;   
		
		typeLabel = inParamSet.typeLabel ;
		
		numValue = inParamSet.numValue ;
		strValue = inParamSet.strValue ;

		intValues = new int[inParamSet.intValues.length] ;
				    if (intValues.length>0){
				    	System.arraycopy(inParamSet.intValues, 0, intValues, 0, intValues.length);
				    }
		numValues = new double[inParamSet.numValues.length] ;
				    if (numValues.length>0){
				    	System.arraycopy(inParamSet.numValues, 0, numValues, 0, numValues.length);
				    }

		strValues = new String[inParamSet.strValues.length] ;
				    if (strValues.length>0){
				    	System.arraycopy(inParamSet.strValues, 0, strValues, 0, strValues.length);
				    }
		            
		list = (ArrayList<Object>) strobj.decode( strobj.encode(inParamSet.list));

		valuePairs = (ArrayList<ValuePair>) strobj.decode( strobj.encode(inParamSet.valuePairs));  ;
		
		map = new DualHashBidiMap(inParamSet.map);
		 
		Object obj = strobj.decode( strobj.encode(inParamSet.obj)); 
		
		
	}
	// ========================================================================
	
	
	




	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return the typeLabel
	 */
	public String getTypeLabel() {
		return typeLabel;
	}

	/**
	 * @param typeLabel the typeLabel to set
	 */
	public void setTypeLabel(String typeLabel) {
		this.typeLabel = typeLabel;
	}

	/**
	 * @return the numValue
	 */
	public double getNumValue() {
		return numValue;
	}

	/**
	 * @param numValue the numValue to set
	 */
	public void setNumValue(double numValue) {
		this.numValue = numValue;
	}

	/**
	 * @return the strValue
	 */
	public String getStrValue() {
		return strValue;
	}

	/**
	 * @param strValue the strValue to set
	 */
	public void setStrValue(String strVal) {
		strValue = strVal;
	}



	public int[] getIntValues() {
		return intValues;
	}



	public void setIntValues(int[] iValues) {
		
		intValues = new int[iValues.length] ;
		System.arraycopy( iValues, 0, intValues, 0, intValues.length) ;
	}



	public double[] getNumValues() {
		return numValues;
	}



	public void setNumValues(double[] nValues) {
		numValues = new double[nValues.length] ;
		System.arraycopy( nValues, 0, numValues, 0, numValues.length) ;
	}


	public String[] getStrValues() {
		return strValues;
	}



	public void setStrValues(String[] sValues) {
		 
		strValues = new String[sValues.length] ;
		System.arraycopy( sValues, 0, strValues, 0, strValues.length) ;

	}



	public ArrayList<ValuePair> getValuePairs() {
		return valuePairs;
	}



	public ArrayList<Object> getList() {
		return list;
	}



	public void setList(ArrayList<Object> list) {
		this.list = list;
	}



	public Object getObj() {
		return obj;
	}



	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
	public void putValuePair(int vi, String str) {
		
		int ix = valuePairs.size() ;
		ValuePair vp = new ValuePair(ix,vi,str );
		valuePairs.add(vp) ;
	}


	class ValuePair implements Serializable{
		 
		private static final long serialVersionUID = -4552648532679913901L;
		
		
		int index ;
		boolean visible=true;
		
		Object key;
		Object value;
		
		// --------------------------------------------------------------------
		public ValuePair( Object vkey, Object vval ){
			put( vkey, vval);
		}
		
		public ValuePair( int ix, Object vkey, Object vval ){
			put( vkey, vval);
			index = ix;
		}
		
		public ValuePair( int ix){
			index = ix;
		}
		public ValuePair(){
		}
		// --------------------------------------------------------------------

		public void put( Object vkey, Object vval){
			  
			key = strobj.decode( strobj.encode(vkey));
			value = strobj.decode( strobj.encode(vval));
			
			
		}
		
		public int getIndex() {
			return index;
		}


		public void setIndex(int index) {
			this.index = index;
		}


		public boolean isVisible() {
			return visible;
		}


		public void setVisible(boolean visible) {
			this.visible = visible;
		}


		public Object getKey() {
			return key;
		}


		public void setKey(Object key) {
			this.key = key;
		}


		public Object getValue() {
			return value;
		}


		public void setValue(Object value) {
			this.value = value;
		}
	}


	public BidiMap getMap() {
		return map;
	}



	public void setMap(BidiMap map) {
		this.map = map;
	}



	public void setValuePairs(ArrayList<ValuePair> valuePairs) {
		this.valuePairs = valuePairs;
	}
	
}

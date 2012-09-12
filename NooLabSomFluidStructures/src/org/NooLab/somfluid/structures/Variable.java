package org.NooLab.somfluid.structures;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
 

public class Variable implements Serializable{
	
	private static final long serialVersionUID = 4793294807647874956L;

	public static final int _VARIABLE_SCALE_REAL     = 1 ;
	public static final int _VARIABLE_SCALE_NOMINAL  = 2 ;
	public static final int _VARIABLE_SCALE_BINARY   = 3 ;
	public static final int _VARIABLE_SCALE_CIRCULAR = 5 ;
	public static final int _VARIABLE_SCALE_COMPLEX  = 7 ;
	
	
	private int index ; 

	private int valueScaleNiveau = 0 ;    
	private int rawFormat = -1; 
	// one of the value as defined as constants in DataTable
	// e.g. __FORMAT_INT = 2;  __FORMAT_ORD = 3;
	
	private double minimum;
	private double maximum;

	private double selectionWeight;
	private int    selectionCount;

	
	private String  label;
	private boolean isTV=false;
	private boolean isID=false;
	private boolean Used=false;
	private boolean IDnotnormalized=false ;
	
	private int     mvCount;
	private double  median;
	private boolean isDerived=false;
	
	/** the ID of the Transformation Stack as it is kept by the TransformationModel
	 *  SomTransformer maintains a map that links variables and transformations 
	 */
	private  String parentTransformID = ""; 
	
	
	// such, the variable can move itself, and inform all other variables about changed indexes!
	ArrayList<Variable> parentCollection ;

	private boolean isTVcandidate;

	private boolean isIndexcandidate;

	
	private int isEmpty = 0 ;

	private int serialID = -1 ;

	String[] parentItems = new String[0];

	
	// ========================================================================
	public Variable(){
		
	}
	
	public Variable( Variable iitem) {


		index = iitem.index; 

		valueScaleNiveau = iitem.valueScaleNiveau ;
		
		minimum = iitem.minimum ;
		maximum = iitem.maximum ;
		median = iitem.median ;
		
		selectionWeight = iitem.selectionWeight ;
		selectionCount = iitem.selectionCount ;
		
		label = iitem.label ;
		isTV = iitem.isTV ;
		isID =iitem.isID ;
		Used = iitem.Used ;
		IDnotnormalized = iitem.IDnotnormalized ;
		
		mvCount = iitem.mvCount ;
		
		rawFormat = iitem.rawFormat ;
		valueScaleNiveau = iitem.valueScaleNiveau  ;
		
		
		isDerived = iitem.isDerived ;
		parentTransformID = iitem.parentTransformID ;
		
		parentCollection = new ArrayList<Variable>();
		if (iitem.parentCollection!=null){
			parentCollection.addAll(iitem.parentCollection);
		}

		isTVcandidate = iitem.isTVcandidate ;

		isIndexcandidate = iitem.isIndexcandidate ;

		isEmpty = iitem.isEmpty ;

		serialID = iitem.serialID ;
		
	}
	// ========================================================================	

	
	public int getIndex(){
		return index  ;
	}
	public void setIndex( int ix){
		index = ix ;
	}
	
	public ArrayList<Variable> getParentCollection() {
		return parentCollection;
	}


	public void setParentCollection(ArrayList<Variable> parentcollection) {
		// this.parentCollection = new ArrayList<Variable>(parentCollection);
		this.parentCollection = parentcollection;
	}
	
	
	public void setParentItems(String[] varLabels) {

		
		if ((varLabels==null) || (varLabels.length==0)){
			return;
		}
		parentItems = new String[varLabels.length] ;
		System.arraycopy(varLabels, 0, parentItems, 0, parentItems.length) ;
		
	}

	/**
	 * @return the parentItems
	 */
	public String[] getParentItems() {
		return parentItems;
		 
	}

	public void setMinimum(double minimum) {
		this.minimum = minimum;
	}
	public double getMinimum() {
		return minimum;
	}
	public void setMaximum(double maximum) {
		this.maximum = maximum;
	}
	public double getMaximum() {
		return maximum;
	}
	 
	public void setLabel(String label) {
		this.label = label;
	}
	public String getLabel() {
		return label;
	}
	public void setTV(boolean isTV) {
		this.isTV = isTV;
	}
	public boolean isTVcandidate() {
		return isTVcandidate;
	}

	public boolean isIndexcandidate() {
		return isIndexcandidate;
	}

	public boolean isTV() {
		return isTV;
	}
	public void setTVcandidate(boolean flag ) {
		 
		isTVcandidate = flag;
	}

	public void setID(boolean isID) {
		this.isID = isID;
	}
	public boolean isID() {
		
		return isID;
	}
	/**
	 * @return the parentTransformID
	 */
	public String getParentTransformID() {
		return parentTransformID;
	}

	/**
	 * @param parentTransformID the parentTransformID to set
	 */
	public void setParentTransformID(String parentTransformID) {
		this.parentTransformID = parentTransformID;
	}

	public void setUsed(boolean used) {
		Used = used;
	}
	public boolean isUsed() {
		return Used;
	}


	public boolean getIDnotnormalized() {
		return IDnotnormalized;
	}


	public void setIDnotnormalized(boolean iDnotnormalized) {
		IDnotnormalized = iDnotnormalized;
	}


	 
	public void setSelectionWeight(double weight) {
		this.selectionWeight = weight;
	}
	public int getSelectionCount() {
		return selectionCount;
	}
 
	public double getSelectionWeight() {
		return selectionWeight;
	}

	public void setSelectionCount(int n) {
		this.selectionCount = n;
	}

	public int getMvCount() {
		return mvCount;
	}

	public void setMvCount(int mvCount) {
		this.mvCount = mvCount;
	}

	public double getMedian() {
		return median;
	}

	public void setMedian(double median) {
		this.median = median;
	}

	public boolean isDerived() {
		return isDerived;
	}

	public void setDerived(boolean isDerived) {
		this.isDerived = isDerived;
	}

 

	public void setIndexcandidate(boolean flag) {
		
		isIndexcandidate = flag ;
	}

	public void setIsEmpty(int flagValue) {
		// 
		isEmpty = flagValue;
	}

	public int getIsEmpty() {
		return isEmpty;
	}

	/**
	 * @return the rawFormat
	 */
	public int getRawFormat() {
		return rawFormat;
	}

	/**
	 * @param rawFormat the rawFormat to set
	 */
	public void setRawFormat(int rawFormat) {
		this.rawFormat = rawFormat;
	}

	public int getValueScaleNiveau() {
		return valueScaleNiveau;
	}

	public void setValueScaleNiveau(int valueScaleNiveau) {
		this.valueScaleNiveau = valueScaleNiveau;
	}

	public void setVariableSerialID(int idv) {
		 
		serialID = idv ;
	}

	public int getSerialID() {
		return serialID;
	}

	public void setSerialID(int serialID) {
		this.serialID = serialID;
	}


}

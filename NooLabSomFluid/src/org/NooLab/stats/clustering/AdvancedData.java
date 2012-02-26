package org.NooLab.stats.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;



public class AdvancedData<T extends Clusterable<T>> implements Serializable  {

	private static final long serialVersionUID = -5986880702403304749L;
	
	private List<EuclideanDoublePoint> rawPoints = new ArrayList<EuclideanDoublePoint>() ;
	ArrayList<Descriptor> descriptions = new ArrayList<Descriptor>(); 

	
	int[] useIndicator = new int[0];
	MissingValue missingValue ;
	private boolean normalizedData;
	boolean isOutdated = true;
	private int numPoints;

	
	
	// ========================================================================
	public AdvancedData() {
		
	}
	public T getPoint(int index) {
		return (T) rawPoints.get(index);
	}
	// ========================================================================

	
 
	
    public void clear() {
		 
    	rawPoints.clear();
    	descriptions.clear() ;
	}




	public void setOutdated(boolean flag) {
		 
		isOutdated = flag;
	}

    public boolean isOutdated() {
	 
		return isOutdated;
	}




	public int getNumPoints() {
		return numPoints;
	}
	public void setNumPoints(int numpoints) {
		numPoints = numpoints;
		
	}
	public  void addPoint( T point) {
    	// new double[] { 1,1959,265100, 11.6 , 0.6 }
    	// double[] vv = point.getValues();
    	// EuclideanDoublePoint  eup = new EuclideanDoublePoint( point.getValues() );
    	rawPoints.add( new EuclideanDoublePoint( point.getValues() ) );
    }
    
	public void normalize(){
		double[] values;
		int k;
		Descriptor d;
		EuclideanDoublePoint eup = rawPoints.get(0);
		
		int n = eup.getValues().length;
		
		for (int i=0;i<n;i++){
			d = new Descriptor(i);
			descriptions.add(d) ;
		}
		 
		if (useIndicator.length==0){
			
			useIndicator = new int[n];
			for (int i=0;i<n;i++){useIndicator[i]=1;}
		}
		
		k=0;
		for (int i=0;i<rawPoints.size();i++){
			
			eup = rawPoints.get(i);
			values = eup.getPoint() ;
			
			for (int p=0;p<values.length;p++){
				
				d = descriptions.get(p) ;
				
				if (useIndicator[p]<=0){
					d.min=0.0; d.max=0.0;
					continue;
				}
				if (MissingValue.isActive()){
					if (values[p]==MissingValue.getValue()){
						d.min=0.0; d.max=0.0;
						continue;
					}
				}
				
				if (d.min>values[p])d.min=values[p];
				if (d.max<values[p])d.max=values[p];
				d.n++;
				d.available = true;
			} // -> all fields, except those not used 
		} // i -> all rawPoints : creating description
		k=0;
		for (int i=0;i<rawPoints.size();i++){
			
			eup = rawPoints.get(i);
			values = eup.getPoint() ;
			
			for (int p=0;p<values.length;p++){
				if (useIndicator[p]<=0){
					continue;
				}
				if (MissingValue.isActive()){
					if (values[p]==MissingValue.getValue()){
						continue;
					}
				}
				d = descriptions.get(p) ;
				// henceforth we will ALWAYS refer to the data through this structure
				// so, either we just transfer the raw values (i.e. no change here), or we normalize it
				if (normalizedData){
					if (d.min==d.max){
						d.available = false;
					}
					if (d.available){
						values[p]= (values[p] - d.min)/(d.max-d.min);
						eup.setValues( values ); 
						// rawPoints.set(i, eup);
						 
					}
				}
				
			} // -> all fields, except those not used
		} // i -> all rawPoints
		
		k=0;
	}


	public Collection<EuclideanDoublePoint> getRawPoints() {
		return rawPoints;
	}
	
	
	public void setUseIndicator(int[] useindicator) {
		 
		useIndicator = new int[useindicator.length] ;
		System.arraycopy(useindicator, 0, useIndicator, 0, useIndicator.length);
		 
	}


	public void setMissingValue(MissingValue mv) {
		missingValue = mv;
		
	}


	public void useNormalizedData(boolean flag) {
		normalizedData = flag ;
		
	}


	class Descriptor{
		public int n=0;
		int index;
		public double min = 9999999999999.09, max = -99999999999999.09 ;
		boolean available = false;
		
		public Descriptor(int i) {
			 index = i;
		}

		
	}
}

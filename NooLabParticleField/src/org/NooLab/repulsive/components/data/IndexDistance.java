package org.NooLab.repulsive.components.data;

import java.io.Serializable;

import org.NooLab.utilities.net.GUID;



/**
 * 
 * 
 * @param index
 * @param distance
 * @param guidStr
 * 
 */
public class IndexDistance implements Serializable, IndexDistanceIntf{

	private static final long serialVersionUID = 2936502952040746948L;

	int index = -1;
	int secindex = -1;
	
	double distance = -1.0;
	String guidStr="";
	
	/**
	 * the constructor requires the data to fill the new element
	 * 
	 * @param index
	 * @param distance
	 * @param guidStr
	 */
	public IndexDistance(int index ,double distance, String guidStr){
		
		this.index = index ;  
		this.distance = distance ; 
		this.guidStr = guidStr ;
	}

	
	
	public IndexDistance(int primIndex, int secIndex, double dValue) {

		index = primIndex;
		secindex = secIndex;
		
		distance = dValue;
		
		guidStr = GUID.randomvalue() ;
	}

	

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getGuidStr() {
		return guidStr;
	}

	public void setGuidStr(String guidStr) {
		this.guidStr = guidStr;
	}
	
	
}

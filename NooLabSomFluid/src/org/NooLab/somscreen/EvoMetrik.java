package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;



/**
 * 
 * this class describes a single step,
 * holding all the steps allows to change the ECR on the fly, and to recalculate the costs
 * 
 * @author kwa
 *
 */
public class EvoMetrik  implements Serializable{

	private static final long serialVersionUID = -831642790269374541L;

	// as long as the ECR is not changed don the fly, these two values are identical
	double actualScore; // 
	double mainScore;
	
	// could be quite expensive regarding space... will be stored only in EvoBasics, or the parent class
	// ArrayList<Double>  evoWeights = new ArrayList<Double>() ; 
	// ArrayList<Integer> evoCounts = new ArrayList<Integer>() ; 

	SomQualityData sqData;

	ArrayList<Double> usageVector;

	int index = -1;
	int step = -1;
	int loopCount;
	
	ArrayList<Integer> varIndexes = new ArrayList<Integer>();

	
 
	
	// 
	
	// ========================================================================
	public EvoMetrik(){
		
	}
	// ========================================================================	



	public EvoMetrik(EvoMetrik prevMetrik) {
	 
		actualScore = prevMetrik.actualScore;
		mainScore = prevMetrik.mainScore ;
		
		sqData = new SomQualityData(prevMetrik.sqData);

		usageVector = new ArrayList<Double> (prevMetrik.usageVector);
		varIndexes = new ArrayList<Integer>(prevMetrik.varIndexes);
	}



	/**
	 * @return the actualScore
	 */
	public double getActualScore() {
		return actualScore;
	}



	/**
	 * @param actualScore the actualScore to set
	 */
	public void setActualScore(double actualScore) {
		this.actualScore = actualScore;
	}



	/**
	 * @return the mainScore
	 */
	public double getMainScore() {
		return mainScore;
	}



	/**
	 * @param mainScore the mainScore to set
	 */
	public void setMainScore(double mainScore) {
		this.mainScore = mainScore;
	}



	/**
	 * @return the sqData
	 */
	public SomQualityData getSqData() {
		return sqData;
	}



	/**
	 * @param sqData the sqData to set
	 */
	public void setSqData(SomQualityData sqData) {
		this.sqData = sqData;
	}



	/**
	 * @return the usageVector
	 */
	public ArrayList<Double> getUsageVector() {
		return usageVector;
	}



	/**
	 * @param usageVector the usageVector to set
	 */
	public void setUsageVector(ArrayList<Double> usageVector) {
		this.usageVector = usageVector;
	}



	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}



	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}



	public int getStep() {
		return step;
	}



	public void setStep(int step) {
		this.step = step;
	}



	/**
	 * @return the varIndexes
	 */
	public ArrayList<Integer> getVarIndexes() {
		return varIndexes;
	}



	/**
	 * @param varIndexes the varIndexes to set
	 */
	public void setVarIndexes(ArrayList<Integer> varIndexes) {
		this.varIndexes = varIndexes;
	}



	public void setLoopCount(int loopcount) {
		loopCount = loopcount ;
	}



	public int getLoopCount() {
		return loopCount;
	}
	
}

package org.NooLab.somfluid.properties;

import java.io.Serializable;


public class ValidationSettings  implements Serializable{

	private static final long serialVersionUID = -5840636933514195606L;
	
	/**  classic split of data into 2 parts, for training and validation */
	public static final int _VALIDATE_SINGLE_SAMPLE_PROB  = 1;
	
	/**  additionally (!) classic 2-sample split: constraints from where to draw the sample;
	 *  to useful for time series analysis */
	public static final int _VALIDATE_SINGLE_SAMPLE_BLOCK = 3;
	
	/** ?  */
	public static final int _VALIDATE_SINGLE_SAMPLE_META  = 5;
	
	 
	
	boolean activation = false;
	
	int validationStyle = 1;
	
	double portion = 21.0 ;
	
	int sampleNotBeforeIndex = -1;
	int sampleNotBeyondIndex = -1;

	double[] vParameters;

	private int repeats;

	private boolean sampleSizeAutoAdjust;
	
	
	// ========================================================================
	public ValidationSettings(ModelingSettings modelingSettings) {
		 
	}
	// ========================================================================	

	public void setActivation(boolean flag) {
		activation = flag ;
	}
	public boolean getActivation() {
		return activation;
	}
	public boolean isActivation() {
		return activation;
	}

	public int getValidationStyle() {
		return validationStyle;
	}

	public void setValidationStyle(int validationStyle) {
		this.validationStyle = validationStyle;
	}

	public double getPortion() {
		return portion;
	}

	public void setPortion(double portion) {
		this.portion = portion;
	}

	public int getSampleNotBeforeIndex() {
		return sampleNotBeforeIndex;
	}

	public void setSampleNotBeforeIndex(int sampleNotBeforeIndex) {
		this.sampleNotBeforeIndex = sampleNotBeforeIndex;
	}

	public int getSampleNotBeyondIndex() {
		return sampleNotBeyondIndex;
	}

	public void setSampleNotBeyondIndex(int sampleNotBeyondIndex) {
		this.sampleNotBeyondIndex = sampleNotBeyondIndex;
	}

	public void setParameters(double[] parameters) {
		// 
		if (parameters.length>0){
			vParameters = new double[parameters.length ];
			System.arraycopy(parameters, 0, vParameters, 0, parameters.length );
		}
		  
	}

	public void setRepeats(int repeats) {

		this.repeats = repeats;
	}

	public int getRepeats() {
		return repeats;
	}

	public void setSampleSizeAutoAdjust(boolean flag) {
		sampleSizeAutoAdjust = flag;
	}

	public boolean isSampleSizeAutoAdjust() {
		return sampleSizeAutoAdjust;
	}

	public double[] getvParameters() {
		return vParameters;
	}

	public void setvParameters(double[] vParameters) {
		this.vParameters = vParameters;
	}

	
	
}

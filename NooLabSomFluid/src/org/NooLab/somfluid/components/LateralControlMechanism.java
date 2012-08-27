package org.NooLab.somfluid.components;

import java.util.Arrays;


/**
 * 
 * The LCM determines the probabilistic spreading of information ("influence")
 * starting from a neuron that changed its state/content as expressed
 * by its intensionality
 * 
 * It could be Gaussian, exponentially decaying, or shaped like a "mexican hat", which
 * then would represent the principle of lateral inhibition.
 * 
 */
public class LateralControlMechanism implements LateralControlMechanismIntf{

	int lcmShape = 1;
	/** 
	 * 1=smooth == approaching 0 even for small radius, 
	 * 2=cut==assuming range of mimimumRadius, then cut at effective radius  */
	int incompleteShapesTreatment = 1;
	
	
	boolean noiseActive=false;
	
	
	/** 
	 * this measure is the multiple of the average distance between particles;
	 * if it is  
	 */
	double minimumRadiusForFullShape = 5.0 ; 
	
	double[][] lcmParameters = new double[3][0];
	
	// ========================================================================
	public LateralControlMechanism( int shape ){
		lcmShape = shape;
		
		if (lcmShape<1)lcmShape=1;
		if (lcmShape>3)lcmShape=3;
	}
	// ========================================================================

	
	/**
	 * these unlabeled parameters have a different meaning according to the selected shape; </br>
	 * GAUSS  : the normalized virtual height = value at the center (d=0), which described the
	 *          maximum of the influence to the next neighbour; </br>
	 * EXPON  : the normalized virtual height   </br> 
	 * MEXHAT : 1. the normalized virtual height, 
	 *          2. the minimum value () could be negative, 
	 *          3. the value at d=r, usually 0, if min<0, but could be positive or negative  </br>
	 * 
	 * 
	 */
	public void setLcmMethodParameters(  double[] mParameters){
		
		int sp = lcmShape-1;
		if (sp<0)sp=0;
		if (sp>2)sp=2;
		
		lcmParameters[sp] = new double[mParameters.length];
		lcmParameters[sp] = Arrays.copyOf(mParameters, mParameters.length);
	}
	
	public void activateNoisePercent(){
		noiseActive = true;
	}
	public void deactivateNoisePercent(){
		noiseActive = false;
	}

	public double calculateLateralInfluence( double distance ){
		return calculateLateralInfluence(  distance, new double[]{} );
	}
	
 
	
	
	public double calculateLateralInfluence( double distance, double[] parameters){
		
		
		if (lcmShape==_LCM_SHAPE_GAUSS){
			
			return calculateLcmGauss(distance, parameters);
			
		}
		if (lcmShape==_LCM_SHAPE_EXPON){
			// gauss with negative m
			return calculateLcmExponential(distance, parameters);
		}
		if (lcmShape==_LCM_SHAPE_MEXHAT){
			// a stretched and sqrt warped cosine of different length
			return calculateLcmMexicanCosine(distance, parameters);
		}
		
		return 1.0;
	}
	
	  
	
	private double calculateLcmMexicanCosine(double distance, double[] parameters) {
		 
		return 0;
	}


	private double calculateLcmExponential(double distance, double[] parameters) {
	 
		return 0;
	}


	/**
	 * 
	 * 
	 * @param distance
	 * @param parameters
	 * @return
	 */
	private double calculateLcmGauss(double distance, double[] parameters) {
		// =(1/(s*SQRT(2*PI()))*EXP(-(B10-m)*(B10-m)/(2*s*s)))
		
		double m,s;
		// lcmParameters[sp]
		
		
		return 0;
	}
	
	// ------------------------------------------------------------------------

	public int getLcmShape() {
		return lcmShape;
	}



	public void setLcmShape(int lcmShape) {
		this.lcmShape = lcmShape;
	}



	public int getIncompleteShapesTreatment() {
		return incompleteShapesTreatment;
	}



	public void setIncompleteShapesTreatment(int incompleteShapesTreatment) {
		this.incompleteShapesTreatment = incompleteShapesTreatment;
	}



	public double getMinimumRadiusForFullShape() {
		return minimumRadiusForFullShape;
	}



	public void setMinimumRadiusForFullShape(double minimumRadiusForFullShape) {
		this.minimumRadiusForFullShape = minimumRadiusForFullShape;
	}
	
	


	
	
	
	
	
	
	
}

package org.NooLab.somfluid.components;

public interface LateralControlMechanismIntf {

	public static final int _LCM_SHAPE_GAUSS = 1;
	public static final int _LCM_SHAPE_EXPON = 2;
	public static final int _LCM_SHAPE_MEXHAT = 3;
	
	public static final int _LCM_SHAPE_DECAY_SMOOTH = 1;
	public static final int _LCM_SHAPE_DECAY_CUT = 2;

	
	// calculate the influence for the selected shape, represented by the object
	public double calculateLateralInfluence( double distance);

	public void setLcmMethodParameters( double[] mParameters);
	
	/**
	 * selecting the shape for the LCM:
	 * GAUSS = 1, EXPON = 2, MEXHAT = 3;
	 * for a fine tuning of the LCM, additional parameters can be supplied for each of them
	 * via "setLcmMethodParameters()"
	 */
	public void setLcmShape(int lcmShape);
	public int getLcmShape();

	
	/**
	 * this overrules the setting for minimumRadiusForFullShape, if there is a conflict
	 * 1=smooth == approaching 0 even for small radius, 
	 * 2=cut==assuming range of mimimumRadius, then cut at effective radius  
	 */
	public void setIncompleteShapesTreatment(int incompleteShapesTreatment);
	public int getIncompleteShapesTreatment();
	
	
	
	/** 
	 * this measure is the multiple of the average distance between particles;
	 * if it is  
	 */
	public void setMinimumRadiusForFullShape(double minimumRadiusForFullShape);
	public double getMinimumRadiusForFullShape();
	
}

package org.NooLab.somfluid.core.engines.det;






public class ClassificationSettings {

	public static final int _TARGETMODE_NONE   = -3;
	public static final int _TARGETMODE_NOTSET =  0;
	
	/** just 1 class , often from binary criterion */
	public static final int _TARGETMODE_SINGLE =  1;
	
	/** multi class, often covering all possible values, ordinal scale of support points */
	public static final int _TARGETMODE_MULTI  =  2;
	
	/** regression, i.e. similar to multi, but continuous cost function (real scale of target values)
	 *  could be beta controlled!  
	 *  example: the SOM should establish a prediction of a score, instead of a binary action, or a predefined class;
	 *  then, the (linear? exponential?) regression from observed score to predicted score is being investigated 
	 */
	public static final int _TARGETMODE_REGR   =  5;
	
	/** -3 = not possible, disabled ;
	 *   0 = not set, 
	 *   1 = single class (still multiple target groups for this class possible) TV has been set
	 *   2 = multi class 
	 */
	int targetMode  = 0 ;
	
	String activeTargetVariable = "" ;
	
	// min max of the interval [0|1][ min|max]
	double[][] 		TGdefinition ;
	String[] 		TGlabels;
	
	double 			ecr=0.2f;   // actually, this could be an array too, since for various
							    // classes in an ordinally segmented TV, we could have different tolerances !!!
	double[] 		ECRs=null;  // for _TARGETMODE_MULTI 
	
	int maxTypeIcount  = -1;
	int maxTypeIIcount = -1;
	
	boolean fullSpelaDiagnostics = false;
	
	
	// ========================================================================
	public ClassificationSettings(){
		
		
	}
	// ========================================================================	


	
	
	public int getTargetMode() {
		return targetMode;
	}


	public void setTargetMode(int targetedModeling) {
		this.targetMode = targetedModeling;
	}


	public String getActiveTargetVariable() {
		return activeTargetVariable;
	}


	public void setActiveTargetVariable(String activeTargetVariable) {
		this.activeTargetVariable = activeTargetVariable;
	}


	public double[][] getTGdefinition() {
		return TGdefinition;
	}
	public void setTGdefinition(double[][] tGdefinition) {
		TGdefinition = tGdefinition;
	}
	
	public double[][] getTargetGroupDefinition() {
		return TGdefinition;
	}

	public void setTargetGroupDefinition(double[][] tGdefinition) {
		
	}
	public void setSingleTargetGroupDefinition(double min, double max) {
		
	}




	public String[] getTGlabels() {
		return TGlabels;
	}


	public void setTGlabels(String[] tGlabels) {
		TGlabels = tGlabels;
	}


	public void setErrorCostRatioRiskPreference(double _ecr) {
		ecr = _ecr;
		
	}
	public double getECR() {
		return ecr;
	}


	public void setECR(double _ecr) {
		ecr = _ecr;
	}


	public double[] getECRs() {
		return ECRs;
	}


	public void setECRs(double[] _ecrs) {
		ECRs = _ecrs;
	}


	public int getMaxTypeIcount() {
		return maxTypeIcount;
	}


	public void setMaxTypeIcount(int maxTypeIcount) {
		this.maxTypeIcount = maxTypeIcount;
	}


	public int getMaxTypeIIcount() {
		return maxTypeIIcount;
	}


	public void setMaxTypeIIcount(int maxTypeIIcount) {
		this.maxTypeIIcount = maxTypeIIcount;
	}


	public void setFullSpelaDiagnostics(boolean flag) {
		fullSpelaDiagnostics = flag;
		
	}
	public boolean isFullSpelaDiagnostics() {
		return fullSpelaDiagnostics;
	}
	public boolean getFullSpelaDiagnostics() {
		return fullSpelaDiagnostics;
	}
	
	
	
}

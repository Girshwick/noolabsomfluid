package org.NooLab.somfluid.core.engines.det.results;

import java.io.Serializable;



public class RoC implements Serializable {

	private static final long serialVersionUID = 4835688308728673719L;

	double[][] rocCurve; // this also provides the info about the singularity TP @ FP=0
	/** so-called Area under Curve */
	double AuC = 0.0;
	/** info about the singularity "TP @ FP=0" */
	double tpSingularityPower = 0.0;
	/** 
	 * describes the slope of the RoC for the first 2 support points;</br>
	 * is used for comparing models in evo optimizer
	 */
	double riskDynamics = 0.0;
	
	// ========================================================================
	public RoC(){
		
	}

	public RoC(RoC iroc) {
	
		rocCurve = iroc.getRocCurve() ;
		tpSingularityPower = iroc.tpSingularityPower;
		riskDynamics = iroc.riskDynamics ;
		AuC = iroc.AuC ;
	}
	// ========================================================================
	
	/**
	 * 
	 * returns a copy of the double[][] array that defines the RoC curve
	 * 
	 * @return
	 */
	public double[][] getRocCurve() {
		double[][] xRoc = new double[0][0] ;
		
		if (rocCurve!=null){
			int z = rocCurve[0].length;
			xRoc = new double[2][z];

			System.arraycopy(rocCurve[0], 0, xRoc[0], 0, z);
			System.arraycopy(rocCurve[1], 0, xRoc[1], 0, z);
		}
		return xRoc;
	}

	public void setRocCurve(double[][] rocCurve) {
		this.rocCurve = rocCurve;
	}

	public double getAuC() {
		return AuC;
	}

	public void setAuC(double auC) {
		AuC = auC;
	}

	public double getTpSingularityPower() {
		return tpSingularityPower;
	}

	public void setTpSingularityPower(double tpSingularityPower) {
		this.tpSingularityPower = tpSingularityPower;
	}

	public double getRiskDynamics() {
		return riskDynamics;
	}

	public void setRiskDynamics(double riskDynamics) {
		this.riskDynamics = riskDynamics;
	}

	public double getTpSingularityValue() {
		double tps = -1 ;
		
		try{

			if ((rocCurve!=null) && (rocCurve.length>0)){
				tps = rocCurve[1][0] ;
			}

		}catch(Exception e){
		}
		
		return tps;
	}
}

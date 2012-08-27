package org.NooLab.somfluid.components.post;

import java.io.Serializable;
import java.util.ArrayList;


import org.NooLab.math3.exception.NoDataException;
import org.NooLab.math3.exception.NullArgumentException;
import org.NooLab.math3.stat.inference.MannWhitneyUTest;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.apache.commons.math.exception.ConvergenceException;

/**
 * http://vassarstats.net/tabs_rz.html :
 * 
 * For any particular value of r, the Pearson product-moment correlation coefficient, this section will 
 * perform the Fisher r-to-z transformation according to the formula
        zr = (1/2)[loge(1+r) - loge(1-r)]
  If a value of N is entered (optional), it will also calculate the standard error of zr as
        SEzr = 1/sqrt[N-3]
        
 *     z          significance one tailed
 *   -1.65    ->     0.0495
 * 
 * 
 * 
 * 
 * 
 */
public class VariableContrast implements Serializable{

	
	private static final long serialVersionUID = 7334530628964198986L;

	BasicStatisticalDescription[] statisticalDescription ;
	
	transient VariableContrasts parentVCS;
	
	String variableLabel = "";

	boolean isUsed = false;
	int variableIndex;
	
	// ArrayList<ArrayList<Double>> 
	double[][] groupedValues ; 
	double mwuStats=0.0;
	double[] pValueMWU = new double[3] ;
	
	transient Variable variable;
	
	// ------------------------------------------------------------------------
	public VariableContrast(VariableContrasts vcs){
		
		parentVCS = vcs;
		
		groupedValues = new double[0][0];

		statisticalDescription = new BasicStatisticalDescription[3] ; 
		for (int i=0;i<3;i++){
			statisticalDescription[i] = new BasicStatisticalDescription(true); 
			pValueMWU[i] = 1.0 ;
		}
	}
	// ------------------------------------------------------------------------


	public void calculateSummaryStatistics() {
		
		
		// test should respect missing values
		MannWhitneyUTest mwu = new MannWhitneyUTest();
		
		// [0] TV data  [1]data of variable
		try {
			pValueMWU[0] = mwu.mannWhitneyUTest( groupedValues[1], groupedValues[0]) ;
			mwuStats = mwu.getMannWhitneyUvalue();
			// within target group
			statisticalDescription[0].introduceValues( groupedValues[0] );
			// within non-target group
			statisticalDescription[1].introduceValues( groupedValues[1] );
			// across all
			statisticalDescription[2].introduceValues( groupedValues[0] );
			statisticalDescription[2].introduceValues( groupedValues[1] ); 
			
			double _mean ;
			_mean = statisticalDescription[0].getMean() ;
			_mean = statisticalDescription[1].getMean() ;
			_mean = statisticalDescription[2].getMean() ;
			
			_mean = _mean +1-1 ;
			
			// values from records in target group against all
			pValueMWU[1] = mwu.mannWhitneyUTest( groupedValues[0], groupedValues[2]) ;
			// values from records in non-target group against all
			pValueMWU[2]= mwu.mannWhitneyUTest( groupedValues[1], groupedValues[2]) ;
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
		
		
	}


	public String getVariableLabel() {
		return variableLabel;
	}

	public void setVariableLabel(String variableLabel) {
		this.variableLabel = variableLabel;
	}


	public void setUsed(boolean flag) {
		isUsed = flag;
	}

	public boolean isUsed() {
		return isUsed;
	}


	public void setVariableIndex(int index) {
		variableIndex = index;
	}

	public int getVariableIndex() {
		return variableIndex;
	}


	public void setVariableReference(Variable v) {
		variable = v;
	}


	public double[][] getGroupedValues() {
		return groupedValues;
	}


	public void setGroupedValues( double[][] groupedvalues) {
		
		groupedValues = new double[groupedvalues.length][groupedvalues[0].length] ;
		System.arraycopy(groupedvalues, 0, groupedValues, 0, groupedvalues.length);
		
		for (int i=0;i<groupedvalues.length;i++){
			System.arraycopy(groupedvalues[i], 0, groupedValues[i], 0, groupedvalues[i].length );
		}
		
	}


	public void initializeGroupedValues(double initVal) {

		for (int i=0;i<groupedValues.length;i++){
			
			for (int k=0;k<groupedValues[i].length;k++){
				groupedValues[i][k] = initVal;
			}
			
		}
	}

}


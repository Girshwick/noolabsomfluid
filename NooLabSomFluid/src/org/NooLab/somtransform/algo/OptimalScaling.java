package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;
import org.NooLab.utilities.datatypes.IndexedDistances;



public class OptimalScaling extends AlgoTransformationAbstract {

	private static final long serialVersionUID = 1760170401611309457L;
	
	int typeInfo = AlgorithmIntf._ALGOTYPE_VALUE ;
	
	String versionStr = "1.00.01" ;

	String tvLabel = "";
	
	
	// ------------------------------------------------------------------------
	public OptimalScaling(){
		
		parameters = new AlgorithmParameters(this);
	}
	// ------------------------------------------------------------------------


	@Override
	public int getType() {
		return typeInfo;
	}


	@Override
	public String getVersion() {
		 
		return versionStr;
	}

 


	@Override
	public int calculate() {
		
		double tvVal, scaleVal;
		int nTVal=0;
		int nScaleValues;
		IndexedDistances ixds ;
		//								   rows	  columns
		int[][] contingencyTable ;
		
		tvLabel = parameters.getLabel( "tv:") ;
		
		/* create contingency table, needs to be saved in transformation mode as a parameter !!
		 * 
		 *                       value1 in col     value2 in col    value3 in col
		 *      value 1 in TV         c-v1-t1           c-v2-t1        c-v3-t1     
		 *      value 2 in TV         c-v1-t2           c-v2-t2        c-v3-t2
		 *      value 3 in TV         c-v1-t3           c-v2-t3        c-v3-t3
		 *           ... 
		 * 
		 *      c-v(k)-t(i) are relative freuencies for the joint (conditional) observations!
		 *      
		 * 	    examples for scale value for replacement:
		 * 
		 * 		value1 in col ->   c-v1-t1/( sum[c-v1-t(i)] )  ... relative frequency of c-v1
		 * 
		 */
		
		// get number of different values in target variable column
		// if # of different values >100 we limit it to 100
		
		   nTVal = 0;
		   nScaleValues = 0;
		   
		   contingencyTable = new int[nTVal+1][nScaleValues+1];
		   
		// calculate column sums
		   
		   
		// create scaled value
		
		for (int i=0;i<values.size();i++){
			
			tvVal = 0;
			scaleVal = values.get(i) ;
			
			// get "coordinates" in contingency table
			
			// add 1 tick to the respective cell in the c'table
			
		}
		
		// calculate the recoded values
		for (int i=0;i<values.size();i++){
			
			scaleVal = values.get(i) ;
			
			// get "coordinate"= column index in contingency table
			
			
			// get contingency ratio 
			
			
			// set it as out value 
		}
		
		// 
		
		return 0;
	}


	@Override
	public ArrayList<Double> getValues(int part) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<Double> getTransformedValues() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public AlgorithmParameters getParameters() {
		 
		return parameters;
	}


	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}

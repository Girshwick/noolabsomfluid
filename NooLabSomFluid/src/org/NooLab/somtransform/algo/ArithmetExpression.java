package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.somsprite.Evaluator;
import org.NooLab.somtransform.algo.intf.AlgoTransformationAbstract;
import org.NooLab.somtransform.algo.intf.AlgorithmParameter;
import org.NooLab.somtransform.algo.intf.AlgorithmParameters;



/**
 * 
 *  a general formula using interpreter variables and logic
 * 
 * 
 * 
 *
 */

public class ArithmetExpression extends AlgoTransformationAbstract{

	private static final long serialVersionUID = -668560046926702153L;
	
	String versionStr = "1.00.01" ;
	
	
	// ------------------------------------------------------------------------
	public ArithmetExpression(){
		parameters = new AlgorithmParameters(this) ;
	}
	// ------------------------------------------------------------------------


	@Override
	public String getVersion() {
		return versionStr;
	}


	@Override
	public int calculate() {
		int result = -1;
		double v, value1=-1.0,value2=-1.0, resultValue = -1;
		String expression;
		Object obj ;
		AlgorithmParameter  ap = null ;
		ArrayList<AlgorithmParameter> aps;
		int errCount=0;
		
		try{
			outvalues.clear();
			
			aps = parameters.getItems() ;
			if (aps.size()>0) ap = aps.get(0);
			expression = ap.getLabel(); // e.g. (1+a-b)/(1+a+b), a+b etc...
			// if (aps.size()>1) ap = aps.get(1);
			
			Evaluator evaluator = new Evaluator();
			evaluator.createFunction("expression", expression) ;
			 
			for (int i=0;i<values.size(); i++){
				
				resultValue = -1.0 ;
				
				try{
					
					
					value2 = -1.0;
					value1 = values.get(i) ;
					if ((valueTable.size()>1) && (i<valueTable.get(1).size())){
						value2 = valueTable.get(1).get(i);
					}
					// 1 or even 2 records are missing ???
					
					if (( value1 != -1.0) && ( value2 != -1.0)){
						obj = evaluator.eval("expression",  value1,value2) ;
						if ((obj!=null) && ( obj instanceof Double)){
							resultValue = (Double)obj ;
						}else{
							System.out.println("ArithmetExpression(), function not found, definition was : "+expression);
							errCount = values.size();
							break;
						}
					}else{
						resultValue = -1.0 ;
					}
					
					
					
				}catch(Exception e){
					
					errCount++;
				}
				// dependent on option, throw exception or continue,
				
				outvalues.add(resultValue);
				
			}// -> all values
			
			result = errCount;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return result;
	}
	


	@Override
	public ArrayList<Double> getDescriptiveResults() {
		// TODO Auto-generated method stub
		return null;
	}

}

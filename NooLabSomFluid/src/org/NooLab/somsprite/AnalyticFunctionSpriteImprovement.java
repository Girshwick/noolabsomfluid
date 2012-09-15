package org.NooLab.somsprite;

import org.NooLab.somfluid.structures.AnalyticFunctionSpriteImprovementIntf;

 
public class AnalyticFunctionSpriteImprovement implements AnalyticFunctionSpriteImprovementIntf{
	
	
	public int varIndex1 = -1;
	public int varIndex2 = -1;
	
	public int funcIndex1 = -1;
	public int funcIndex2 = -1;

	public double  estimatedImprovement = -1.0;
	private String expression ="";
	private String expressionName ="";

	
	// ------------------------------------------------------------------------
	public AnalyticFunctionSpriteImprovement( int varIndex1, int varIndex2, double estimatedImprovement){
		
		this.varIndex1 =  varIndex1;
		this.varIndex2 =  varIndex2;
		this.estimatedImprovement = estimatedImprovement ; 
	}
	// ------------------------------------------------------------------------

	public AnalyticFunctionSpriteImprovement( AnalyticFunctionSpriteImprovementIntf fs) {

		varIndex1 = fs.getVarIndex1()  ;
		varIndex2 = fs.getVarIndex2()   ;
		
		funcIndex1 = fs.getFuncIndex1()  ;
		funcIndex2 = fs.getFuncIndex2()   ;

		estimatedImprovement = fs.getEstimatedImprovement()  ;
		expression = fs.getExpression()  ;
		expressionName = fs.getExpressionName()  ;

	}

	@Override
	public void setVariables( int... varindex){
		varIndex1 = varindex[0] ;
		varIndex2 = varindex[1] ;
		
	}

	@Override
	public void setFunctions( int... funcindex){
		funcIndex1 = funcindex[0] ;
		funcIndex1 = funcindex[1] ;
		
	}
	
	@Override
	public int getVarIndex1() {
		return varIndex1;
	}


	
	@Override
	public void setVarIndex1(int varIndex1) {
		this.varIndex1 = varIndex1;
	}


	
	@Override
	public int getVarIndex2() {
		return varIndex2;
	}


	
	@Override
	public void setVarIndex2(int varIndex2) {
		this.varIndex2 = varIndex2;
	}


	
	@Override
	public double getEstimatedImprovement() {
		return estimatedImprovement;
	}


	
	@Override
	public void setEstimatedImprovement(double estimatedImprovement) {
		this.estimatedImprovement = estimatedImprovement;
	}

	
	@Override
	public int getFuncIndex1() {
		return funcIndex1;
	}

	
	@Override
	public void setFuncIndex1(int funcIndex1) {
		this.funcIndex1 = funcIndex1;
	}

	
	@Override
	public int getFuncIndex2() {
		return funcIndex2;
	}

	
	@Override
	public void setFuncIndex2(int funcIndex2) {
		this.funcIndex2 = funcIndex2;
	}

	
	@Override
	public void setExpression(String expression) {
		
		this.expression = expression;
	}

	
	@Override
	public void setExpressionName(String expressionName) {
		 
		this.expressionName = expressionName;
	}

	
	@Override
	public String getExpression() {
		return expression;
	}

	
	@Override
	public String getExpressionName() {
		return expressionName;
	}
	
	@Override
	public boolean isEqual( AnalyticFunctionSpriteImprovementIntf fs){
		boolean rB=true;
		
		rB = (varIndex1 == fs.getVarIndex1());
		if (rB){ rB = varIndex2 == fs.getVarIndex2() ;  } ;
		if (rB){ rB = funcIndex1 == fs.getFuncIndex1() ;  } ;
		if (rB){ rB = funcIndex2 == fs.getFuncIndex2() ;  } ;
		
		if (rB){ 
			if (fs.getExpression().length()>0){
				rB = expression.contentEquals(fs.getExpression());  
			} ;
		}
		if (rB){ 
			if (fs.getExpression().length()>0){
				rB = expressionName.contentEquals(fs.getExpression());  
			} ;
		}
 
		return rB;
	}
}

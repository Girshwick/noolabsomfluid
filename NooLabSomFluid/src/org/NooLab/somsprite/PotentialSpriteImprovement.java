package org.NooLab.somsprite;

public class PotentialSpriteImprovement {
	
	
	public int varIndex1;
	public int varIndex2 ;
	
	public int funcIndex1;
	public int funcIndex2 ;

	public double  estimatedImprovement;
	private String expression;
	private String expressionName;

	
	// ------------------------------------------------------------------------
	public PotentialSpriteImprovement( int varIndex1, int varIndex2, double estimatedImprovement){
		
		this.varIndex1 =  varIndex1;
		this.varIndex2 =  varIndex2;
		this.estimatedImprovement = estimatedImprovement ; 
	}
	// ------------------------------------------------------------------------

	public void setVariables( int... varindex){
		varIndex1 = varindex[0] ;
		varIndex2 = varindex[1] ;
		
	}

	public void setFunctions( int... funcindex){
		funcIndex1 = funcindex[0] ;
		funcIndex1 = funcindex[1] ;
		
	}
	
	public int getVarIndex1() {
		return varIndex1;
	}


	public void setVarIndex1(int varIndex1) {
		this.varIndex1 = varIndex1;
	}


	public int getVarIndex2() {
		return varIndex2;
	}


	public void setVarIndex2(int varIndex2) {
		this.varIndex2 = varIndex2;
	}


	public double getEstimatedImprovement() {
		return estimatedImprovement;
	}


	public void setEstimatedImprovement(double estimatedImprovement) {
		this.estimatedImprovement = estimatedImprovement;
	}

	public int getFuncIndex1() {
		return funcIndex1;
	}

	public void setFuncIndex1(int funcIndex1) {
		this.funcIndex1 = funcIndex1;
	}

	public int getFuncIndex2() {
		return funcIndex2;
	}

	public void setFuncIndex2(int funcIndex2) {
		this.funcIndex2 = funcIndex2;
	}

	public void setExpression(String expression) {
		
		this.expression = expression;
	}

	public void setExpressionName(String expressionName) {
		 
		this.expressionName = expressionName;
	}

	public String getExpression() {
		return expression;
	}

	public String getExpressionName() {
		return expressionName;
	}
	
	
}

package org.NooLab.somtransform;

import java.io.Serializable;




public class CandidateTransformation implements Serializable{

	private static final long serialVersionUID = 6502046200729357592L; 
	
	int demandForFreshStack = -1;
	String expression, expressionName;
	String[] variablesStr = new String[2]; 
	int[] variablesIx     = new int[2];
	
	
	// ------------------------------------------------------------------------
	public CandidateTransformation(){
		
	}
	
	public CandidateTransformation(String exprName, String expr, int[] varix, String[] varStr) {
		
		System.arraycopy(varix,  0, variablesIx,  0, 2);
		System.arraycopy(varStr, 0, variablesStr, 0, 2);
		expression = expr;
		expressionName = exprName;
	}
	// ------------------------------------------------------------------------

	
	public String getExpression() {
		return expression;
	}

	public void setExpression(String expression) {
		this.expression = expression;
	}

	public String[] getVariablesStr() {
		return variablesStr;
	}

	public void setVariablesStr(String[] variablesStr) {
		this.variablesStr = variablesStr;
	}

	public int[] getVariablesIx() {
		return variablesIx;
	}

	public void setVariablesIx(int[] variablesIx) {
		this.variablesIx = variablesIx;
	}

	/**
	 * @return the demandForFreshStack
	 */
	public int getDemandForFreshStack() {
		return demandForFreshStack;
	}

	/**
	 * @param demandForFreshStack the demandForFreshStack to set
	 */
	public void setDemandForFreshStack(int demandForFreshStack) {
		this.demandForFreshStack = demandForFreshStack;
	}

	/**
	 * @return the expressionName
	 */
	public String getExpressionName() {
		return expressionName;
	}

	/**
	 * @param expressionName the expressionName to set
	 */
	public void setExpressionName(String expressionName) {
		this.expressionName = expressionName;
	}
	
	
	
}

package org.NooLab.somfluid.structures;

 

public interface AnalyticFunctionSpriteImprovementIntf {

	void setVariables(int[] varindex);

	void setFunctions(int[] funcindex);

	int getVarIndex1();

	boolean isEqual(AnalyticFunctionSpriteImprovementIntf fs);

	String getExpressionName();

	String getExpression();

	void setExpressionName(String expressionName);

	void setVarIndex1(int varIndex1);

	int getVarIndex2();

	void setVarIndex2(int varIndex2);

	double getEstimatedImprovement();

	void setEstimatedImprovement(double estimatedImprovement);

	int getFuncIndex1();

	void setFuncIndex1(int funcIndex1);

	int getFuncIndex2();

	void setFuncIndex2(int funcIndex2);

	void setExpression(String expression);

}

package org.NooLab.somfluid.core.categories.intensionality;

import java.util.ArrayList;

import org.NooLab.somfluid.structures.Variable;

public interface ProfileVectorIntf {

	public long getLastUpdateTime();

	public void setLastUpdateTime(long lastUpdateTime);

	public long getLastRecalcTime();

	public void setLastRecalcTime(long lastRecalcTime);

	public ArrayList<Variable> getVariables();

	public void setVariables(ArrayList<Variable> variables);

	/** return the complete profile */
	public ArrayList<Double> getValues();
 
	public void setValues(ArrayList<Double> values);

	public int getLastExtDataValueIndex();

	public void setLastExtDataValueIndex(int lastExtDataValueIndex);

	/** return the labels of all variables */
	public ArrayList<String> getVariablesStr();
 
	public void setVariablesStr(ArrayList<String> variablesStr);

	public ArrayList<Double> getCompoundValues();

	public void setCompoundValues(ArrayList<Double> compoundValues);

	public ArrayList<String> getIntSomNodeProperties();

	public ArrayList<Double> getIntSomNodePropValues();

	public void changeProfile( ArrayList<Double> dataVector, int count, int direction);


}

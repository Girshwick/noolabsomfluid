package org.NooLab.somfluid.core.categories.intensionality;

import java.util.ArrayList;

import org.NooLab.somfluid.data.Variable;

public interface ProfileVectorIntf {

	public long getLastUpdateTime();

	public void setLastUpdateTime(long lastUpdateTime);

	public long getLastRecalcTime();

	public void setLastRecalcTime(long lastRecalcTime);

	public ArrayList<Variable> getVariables();

	public void setVariables(ArrayList<Variable> variables);

	public ArrayList<Double> getValues();

	public void setValues(ArrayList<Double> values);

	public int getLastExtDataValueIndex();

	public void setLastExtDataValueIndex(int lastExtDataValueIndex);

	public ArrayList<String> getVariablesStr();

	public void setVariablesStr(ArrayList<String> variablesStr);

	public ArrayList<Double> getCompoundValues();

	public void setCompoundValues(ArrayList<Double> compoundValues);

	public ArrayList<String> getIntSomNodeProperties();

	public ArrayList<Double> getIntSomNodePropValues();


}

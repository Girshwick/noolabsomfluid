package org.NooLab.somfluid.core.engines;

import java.util.ArrayList;

import org.NooLab.somfluid.core.categories.extensionality.BasicSimpleStatisticalDescription;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;



public interface NodeStatisticsIntf {

	void resetFieldStatisticsAll();

	void setFieldValues(ArrayList<?> arrayList);

	ArrayList<?> getFieldValues();

	void setVariables(ArrayList<Variable> vars);

	void removeRecordData(ArrayList<Double> xDataVector);

	void addFieldValues(BasicStatisticalDescription basicStatisticalDescription);

 

}

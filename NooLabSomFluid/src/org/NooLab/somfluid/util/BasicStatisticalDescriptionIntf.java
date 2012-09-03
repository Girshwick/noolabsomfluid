package org.NooLab.somfluid.util;

import java.util.ArrayList;

public interface BasicStatisticalDescriptionIntf {

	void clear();


	double getMean();

	int getCount();

	void reset();

	
	void introduceValue(double fieldValue);
	
	void introduceValues(ArrayList<Double> fieldValues);


	void resetFieldStatisticsAll();

}

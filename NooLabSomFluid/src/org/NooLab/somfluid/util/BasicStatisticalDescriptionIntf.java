package org.NooLab.somfluid.util;

import java.util.ArrayList;

public interface BasicStatisticalDescriptionIntf {

	void clear();

	void introduceValue(double fieldValue);

	double getMean();

	int getCount();

	void reset();

	void introduceValues(ArrayList<Double> fieldValues);

}

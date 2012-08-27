package org.NooLab.somfluid.app;

import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationResults;



public interface SomAppUsageIntf extends SomApplicationBasicsIntf{

	public SomApplicationResults classify( String[] fields, double[] data);

	public SomApplicationResults classify( String[] fields, double[][] tabledata);

	public String classify( SomApplicationEventIntf resultsEvent, String[] fields, double[] data);
	
	public String classify( SomApplicationEventIntf resultsEvent, ArrayList<String> fields, ArrayList<Double> data);
	
	
	
}

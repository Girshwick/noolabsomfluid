package org.NooLab.somfluid;

import org.NooLab.somfluid.core.engines.det.ModelingSettingsIntf;




public interface SomFluidTaskIntf 	extends 	ModelingSettingsIntf,
												ModelingResultsIntf{

	public static final int _SOURCE_DB_DATATABLE = 1;
	public static final int _SOURCE_DB_SOMNODES  = 2;
	
	public static final int _TARGET_DB_HISTODOC  = 1; // which is a data table
	public static final int _TARGET_DB_MARKOVDOC = 5; // which is a data table
	
	public void setResumeMode(int modeOnOff);

	int getSourceDatabaseType();

	String getSourceDatabaseName();
	
}

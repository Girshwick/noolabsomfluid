package org.NooLab.somfluid;

import org.NooLab.somfluid.core.engines.det.ModelingSettingsIntf;




public interface SomFluidTaskIntf 	extends 	ModelingSettingsIntf,
												ModelingResultsIntf{

	public void setResumeMode(int modeOnOff);
	
}

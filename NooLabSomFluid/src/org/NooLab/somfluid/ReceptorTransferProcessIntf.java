package org.NooLab.somfluid;


import org.NooLab.somfluid.core.engines.det.SomHostIntf;




public interface ReceptorTransferProcessIntf {

	
	public void start(SomHostIntf somhost, int dbStructureCode);
	
	public void stop();

	public void triggerNextRead();


	
}

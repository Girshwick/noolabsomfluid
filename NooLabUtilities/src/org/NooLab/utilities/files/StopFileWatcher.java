package org.NooLab.utilities.files;

import java.util.Observable;
import java.util.Observer;
  

/**
 * 
 * this class observes a directory for just the appearance of one particular file: "stop",
 * or the filename that has been provided 
 * 
 *
 */
public class StopFileWatcher {
	
	StopFileWatcher filewatcher;
	Observer observer;
	private StopFileObserverProcess obsproc;
	boolean isObserving = false, respectExistingFile;
	int period = 1000;
	String filepath = "";
	
	Thread stopfilThrd;
	
	org.NooLab.utilities.logging.PrintLog out = new org.NooLab.utilities.logging.PrintLog(2,false);
	DFutils fileutil  = new DFutils();
	private String observedFile="";
	
	// ========================================================================
	public StopFileWatcher( Observer observer, 
							String observedFolder, String filename, 
							boolean respectExistingFile,
							int milliperiod) throws Exception {
		 
		out.delay(10);
		
		filewatcher = this;
		period = milliperiod;
		this.observer = observer;
		this.respectExistingFile = respectExistingFile;
		
		
		
		if (filename.length()==0){
			filename="stop" ;
		}
		if (DFutils.folderExists(observedFolder)){
			filepath = DFutils.createPath( observedFolder, filename) ; 
		}else{
			throw(new Exception("StopFileWatcher: Provided folder does not exist."));
		}
		
		observedFile = filepath;
		
		obsproc = new StopFileObserverProcess();
		if (obsproc.countObservers()==0){
			throw(new Exception("StopFileWatcher: Observation could not be initialized."));
		}
		
		
	}

	public void close(){
		isObserving = false;
		out.delay(20);
	}
	
	class StopFileObserverProcess extends Observable implements Runnable{

		
		public StopFileObserverProcess(){
			
			if (observer!=null){
				addObserver(observer);
			}
			
			
			
			if (respectExistingFile==false){
				if (fileutil.fileexists(filepath)){
					fileutil.deleteFile(filepath);
				}
			}
			stopfilThrd = new Thread(this,"stopfilThrd");
			stopfilThrd.start() ;
		}
		
		@Override
		public void run() {
			//  
			boolean isInforming=false;
			isObserving = true;
			
			while (isObserving){
			
				try{
					
					if ((isInforming==false) && (DFutils.fileExists( filepath ))){
						isInforming = true;
						out.print(3, "informing the observer (id:"+observer.getClass().getName()+")");
						
						setChanged();
						 
						this.notifyObservers(filewatcher.getClass().getSimpleName()) ;
						
						out.delay(5*period);
						fileutil.deleteFile(filepath) ;
						
						isInforming = false;
					}
					out.delay(period);
					
				}catch(Exception e){}
			}
			
		}
		
	}

	public boolean isObserving() {
		return isObserving ;
	}

	public String getObservedFile() {
		 
		return observedFile;
	}
}

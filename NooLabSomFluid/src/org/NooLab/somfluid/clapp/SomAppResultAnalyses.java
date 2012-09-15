package org.NooLab.somfluid.clapp;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomApplicationIntf;
import org.NooLab.somfluid.tasks.SomFluidTask;




public class SomAppResultAnalyses implements Serializable{

	private static final long serialVersionUID = 1821212339914303437L;
	
	
	transient SomApplicationIntf somApplication ; 
	SomFluidTask sfTask;
	
	
	ArrayList<SomAppResultAnalysis> items = new ArrayList<SomAppResultAnalysis> (); 
	
	// ========================================================================
	public SomAppResultAnalyses( SomApplicationIntf application ){
		
		somApplication = application ;
		sfTask = somApplication.getSomFluidTask();
		
	}
	// ========================================================================
	
	
	
	public ArrayList<SomAppResultAnalysis> getItems() {
		return items;
	}

	public void setItems(ArrayList<SomAppResultAnalysis> items) {
		this.items = items;
	}
	
	public void add(SomAppResultAnalysis resultAnalysis) {

		items.add(resultAnalysis) ;
	}
	
	
	
	// ------------------------------------------------------------------------
	

	public SomFluidTask getSfTask() {
		return sfTask;
	}

	public void setSfTask(SomFluidTask sfTask) {
		this.sfTask = sfTask;
	}




	
}

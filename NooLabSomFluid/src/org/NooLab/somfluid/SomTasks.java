package org.NooLab.somfluid;

import java.io.Serializable;
import java.util.ArrayList;



public class SomTasks implements Serializable{

	private static final long serialVersionUID = 6421149822489193871L;


	ArrayList<SomFluidTask> taskitems = new ArrayList<SomFluidTask>(); 
	
	SomFluidFactory sfFactory;
	

	// ------------------------------------------------------------------------
	public SomTasks(SomFluidFactory factory) {
		
		sfFactory = factory;
		
		
	}
	// ------------------------------------------------------------------------

	public void add(SomFluidTask sfTask) {
		 
		taskitems.add(sfTask);
	}

	public int size(){
		return taskitems.size();
	}
	public void clear(){
		taskitems.clear();
	}
	public SomFluidTask getItem( int index){
		return taskitems.get(index);
	}
	public void remove( int index ){
		 taskitems.remove(index) ;
	}
	
	
}

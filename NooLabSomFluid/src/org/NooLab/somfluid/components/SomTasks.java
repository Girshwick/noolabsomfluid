package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidTask;



public class SomTasks implements Serializable{

	private static final long serialVersionUID = 6421149822489193871L;


	ArrayList<SomFluidTask> taskitems = new ArrayList<SomFluidTask>(); 
	
	transient SomFluidFactory sfFactory;
	

	// ------------------------------------------------------------------------
	public SomTasks(SomFluidFactory factory) {
		
		sfFactory = factory;
		
		
	}
	// ------------------------------------------------------------------------

	public int size(){
		return taskitems.size();
	}

	public SomFluidTask getItemByGuid(String taskGuid) {
		SomFluidTask sft=null, item;
		
		for (int i=0;i<taskitems.size();i++){
			item = taskitems.get(0) ;
			if (item!=null){
				if (item.getGuidID().contentEquals(taskGuid)){
					sft=item;
					break;
				}
			}
		}
		return sft;
	}

	
	public SomFluidTask getNextOpenTask(){
		SomFluidTask sft=null, item;
		
		for (int i=0;i<taskitems.size();i++){
			item = taskitems.get(0) ;
			if (item!=null){
				if (item.isCompleted()==false){
					sft=item;
					break;
				}
			}
		}
		return sft;
	}
	
	public int removeCompletedTasks(){
		int openTasks=0;
		SomFluidTask item;
		
		int i=taskitems.size()-1;
		while (i>=0){
		
			item = taskitems.get(0) ;
			if ((item!=null) || ( item.isCompleted() )){
				taskitems.remove(i) ;
			}
			i--;
		}
		return openTasks ;
		
	}
	
	
	public void add(SomFluidTask sfTask) {
		 
		taskitems.add(sfTask);
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

	public ArrayList<SomFluidTask> getTaskitems() {
		return taskitems;
	}
	
	
}

package org.NooLab.somfluid.components;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidTask;



public class SomTasks implements Serializable{

	private static final long serialVersionUID = 6421149822489193871L;


	ArrayList<SomFluidTask> taskitems = new ArrayList<SomFluidTask>(); 
	
	transient SomFluidFactory sfFactory;


	boolean stopped = false;
	

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
		 
		SomFluidTask sft = getItemByGuid(sfTask.getGuidID());
		if (sft==null){
			taskitems.add(sfTask);
		}
	}

	public void clear(){
		taskitems.clear();
	}
	public SomFluidTask getItem( int index){
		SomFluidTask sft=null;
		
		if ((index>=0) && (index<taskitems.size())){
			sft = taskitems.get(index);
		}
		
		return sft;
	}
	public void remove( int index ){
		
		if ((index>=0) && (index<taskitems.size())){
			 taskitems.remove(index) ;
		}
	}

	public ArrayList<SomFluidTask> getTaskitems() {
		return taskitems;
	}

	public SomFluidFactory getSfFactory() {
		return sfFactory;
	}

	public void setSfFactory(SomFluidFactory sfFactory) {
		this.sfFactory = sfFactory;
	}

	public boolean isStopped() {
		return stopped;
	}

	public void setStopped(boolean stopped) {
		this.stopped = stopped;
	}

	public void setTaskitems(ArrayList<SomFluidTask> taskitems) {
		this.taskitems = taskitems;
	}
	
	
}

package org.NooLab.somfluid.tasks;


import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.SomFluidFactory;




public class SomSubTasks implements Serializable{
	

	ArrayList<SomFluidSubTask> taskitems = new ArrayList<SomFluidSubTask>(); 
	
	transient SomFluidFactory sfFactory;


	boolean stopped = false;

	
	// ----------------------------------------------------
	public SomSubTasks(){
		
	}
	// ----------------------------------------------------	
	

	public int size(){
		return taskitems.size();
	}

	public SomFluidSubTask getItemByGuid(String taskGuid) {
		SomFluidSubTask sft=null, item;
		
		for (int i=0;i<taskitems.size();i++){
			item = taskitems.get(0) ;
			if (item!=null){
				if (item.getGuid().contentEquals(taskGuid)){
					sft=item;
					break;
				}
			}
		}
		return sft;
	}

	
	public SomFluidSubTask getNextOpenTask(){
		SomFluidSubTask sft=null, item;
		
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
		SomFluidSubTask item;
		
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
	
	
	public void add(SomFluidSubTask sfTask) {
		 
		SomFluidSubTask sft = getItemByGuid(sfTask.getGuid());
		if (sft==null){
			taskitems.add(sfTask);
		}
	}

	public void clear(){
		taskitems.clear();
	}
	
	public SomFluidSubTask getItem( int index){
		SomFluidSubTask sft=null;
		
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

	public ArrayList<SomFluidSubTask> getTaskitems() {
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

	public void setTaskitems(ArrayList<SomFluidSubTask> taskitems) {
		this.taskitems = taskitems;
	}

	
	
}

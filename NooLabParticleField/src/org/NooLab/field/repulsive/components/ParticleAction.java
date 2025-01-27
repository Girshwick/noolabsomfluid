package org.NooLab.field.repulsive.components;

import java.util.ArrayList;

import org.NooLab.field.interfaces.RepulsionFieldEventsIntf;
import org.NooLab.field.repulsive.RepulsionFieldCore;
import org.NooLab.utilities.logging.PrintLog;

public class ParticleAction {

	ArrayList<ActionDescriptor> addQueue = new ArrayList<ActionDescriptor>(); 
	ArrayList<ActionDescriptor> delQueue = new ArrayList<ActionDescriptor>(); 
	
	RepulsionFieldCore rfield;
	SurroundBuffers surroundBuffers;
	
	Waiter delWaiter;
	Waiter addWaiter;
	
	PrintLog out;
	public ParticleAction( RepulsionFieldCore rf){
	
		rfield = rf;
	
		surroundBuffers = rfield.getSurroundBuffers() ;
		
		out = rfield.out ;
		
		delWaiter = new Waiter(delQueue, RepulsionFieldEventsIntf._FIELDACTION_DEL);
		addWaiter = new Waiter(addQueue, RepulsionFieldEventsIntf._FIELDACTION_ADD);
		
		
		
	}
	
	private void removeEmptyItems( ArrayList<ActionDescriptor> queue){
		
		for (int i=queue.size()-1;i>=0;i--){
			if (queue.get(i).getActionCode()<=0){
				queue.remove(i) ;
			}
		}
	}
	
	public int size(){
		return addQueue.size() + delQueue.size();
	}
	
	/** use "a" for the "add" queue and "d" for the "delete" queue */
	public int size( String ids){
		int n= -1;
		
		if (ids.toLowerCase().startsWith("a")){
			n = addQueue.size();  
		}
		if (ids.toLowerCase().startsWith("d")){
			n = delQueue.size();  
		}
		
		
		return n;
	}

	synchronized public void add(ActionDescriptor ad){
		
		if (ad.getActionCode() == RepulsionFieldEventsIntf._FIELDACTION_ADD){
			
			
			for (int i=0;i<addQueue.size();i++){
				if ( (ad.x>0) && (addQueue.get(i).x==ad.x) && (ad.y>0) &&(addQueue.get(i).y==ad.y)){
					// ad.x = ad.x + Math.random()*15;
					// ad.y = ad.y + Math.random()*15;
					ad.x = ad.x + rfield.getStatsSampler().randomUniSimple()*15;
					ad.y = ad.y + rfield.getStatsSampler().randomUniSimple()*15;
				}
			}
			addQueue.add(ad);
			
			out.print(3, "addQueue size now : "+addQueue.size());
		}
		if (ad.getActionCode() == RepulsionFieldEventsIntf._FIELDACTION_DEL){
			removeEmptyItems(delQueue);
			
			for (int i=0;i<delQueue.size();i++){
				
			}
			delQueue.add(ad);
			out.print(3, "delQueue size now : "+delQueue.size());
		}  
		
	}
	
	protected void handlingDelQueue(){
		int ac,t,i,ix;
		int[] indexes;
		
		
		t = delQueue.size();
		if (t<=0)return;
		
		int z=0;
		for (int k=0;k<t;k++){
			ac = delQueue.get(k).getActionCode() ;
			if (ac>0){
				z++;
			}
		}
		
		if (z<=0)return;
		
		try{
			
			indexes = new int[z];
			for (int k=0;k<z;k++){
				indexes[k] = -1;
			}
			
			i=0;
			while (i<delQueue.size()){
				ac = delQueue.get(i).getActionCode() ;
				
				if (ac>0){
					ix = delQueue.get(i).getIndex();
					if (ix>=0){
						if (i<=indexes.length){
							indexes[i] = ix;
							delQueue.get(i).clear();
						}
					}else{
						delQueue.get(i).setActionCode(-1);
					}
					
				}else{
					delQueue.remove(i);
					i--;
				}
				i++;
			}
		}catch(Exception e){
			removeEmptyItems(delQueue);
			return;
		}
		

		
		if (indexes.length>0){
			rfield.deleteParticle(indexes);
		}
		removeEmptyItems(delQueue);
	}
	
	protected void handlingAddQueue(){
		int t,ix,ac,k;
		int[] xs, ys;
		double x,y;
		
		t = addQueue.size();
		if (t<=0)return;
		
		int z=0;
		try{
			k=0; 
			while (k<t){
				ac = addQueue.get(k).getActionCode() ;
				if ((ac>0) && (addQueue.get(k).getIndex()>=0)){
					// no need to check   addQueue.get(k).x,y , since they often will be -1;
					z++;
				}else{ addQueue.remove(k); k--;}
				k++;
			}
			
		}catch(Exception e){
			removeEmptyItems(addQueue);
			return;
		}
		if (z<=0)return;
		
		xs = new int[z]; 
		ys = new int[z];
		
		for (int i=0;i<xs.length;i++){
			xs[i] = -3;
		}
		k=0;
		for (int i=0;i<z;i++){
			
			ac = addQueue.get(i).getActionCode() ;
			
			if (ac>0){
				x = addQueue.get(i).getX();
				y = addQueue.get(i).getY();
				if ( (x==-1.0) ||
				     ((x>0.0) && (y>0.0))){
						xs[i] = (int)x;
						ys[i] = (int)y;
						k++;
				}
				addQueue.get(i).clear();
				
			}
		}
		removeEmptyItems(addQueue);
		if ((xs.length>0) && (k>0)){
			// resize array
			rfield.addParticles( xs, ys);
		}
	}

	
	class Waiter implements Runnable{
		
		ArrayList<ActionDescriptor> queue;
		boolean isRunning=false;
		int qType;
		
		Thread wqThrd;
		
		public Waiter( ArrayList<ActionDescriptor> queue, int qtype){
			this.queue = queue;
			qType = qtype;
			
			wqThrd = new Thread(this,"wqThrd");
			wqThrd.start();
			out.print(3, "buffer queue for actions of type <"+qtype+"> is now running ...");
		}
		
		public void go(){
			
			
			if (qType == RepulsionFieldEventsIntf._FIELDACTION_DEL){
				handlingDelQueue();
			}
			if (qType == RepulsionFieldEventsIntf._FIELDACTION_ADD){
				handlingAddQueue();
			}
			
		}

		@Override
		public void run() {
			boolean isWorking=false;
			
			isRunning=true;
			
			while (isRunning){
				
				if ((isWorking==false) && (rfield.isFieldLayoutFrozen() )){
					isWorking=true;
					
					if ((surroundBuffers!=null) && (surroundBuffers.updateIsPending())){
						if (surroundBuffers.getUpdating()>0){
							continue;
						}
					}

						go();
						rfield.out.delay(20) ;
						isWorking=false;
					
				}
				
				rfield.out.delay(10) ;
			}
			
		}
		
	}
	
}

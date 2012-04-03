package org.NooLab.somfluid.env.communication;


import java.util.ArrayList;
 
import org.NooLab.somfluid.core.categories.connex.NetworkMessageIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.nodes.MetaNodeIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.utilities.logging.PrintLog;


// extends Observable

public class NodesInformer {

	protected ArrayList<NetworkMessageIntf> observingNodes = new ArrayList<NetworkMessageIntf>(); 
	
	protected ArrayList<QTask> taskQueue = new ArrayList<QTask>(); 
	
	protected ArrayList<Integer> removingNodeIsBlocked = new ArrayList<Integer>() ;
	
	MessageServer msgSrv ;
	
	boolean isRunning=false;
	 
	PrintLog out = new PrintLog (2,true);
	
	
	// ------------------------------------------------------------------------
	public NodesInformer(){
	
		msgSrv = new MessageServer(this)  ;
		
	}

	// ------------------------------------------------------------------------
	
	
	public void registerNodeinNodesInformer( NetworkMessageIntf node){
		
		observingNodes.add(node) ;
		  
		
	}

	public void unregisterNode( NetworkMessageIntf node ){
		
		int ix = observingNodes.indexOf(node);
		if (ix>=0){
			int z=0;
			while ((z<10000) && (removingNodeIsBlocked.indexOf(ix)>=0)){ z++;}
			observingNodes.remove(ix);
		}
	}
	
	public void stop(){
		isRunning=false;
	}
	
	// ------------------------------------------------------------------------

	
	// Step 1 : taking the notification , inserting it to a queue
	
	public void notifyNodeByIndex( int index, NodeTask task ){
		taskQueue.add( new QTask( 1, index,-1,task) );
	}

	public void notifyNodeBySerial( long numId, NodeTask task){
		taskQueue.add( new QTask( 2, -1,numId, task) );
	}
	
	public void notifyAllNodes( NodeTask task){
		taskQueue.add( new QTask( 0, -1,-1, task) );
	}
	
	synchronized protected void _blockRemovalForIndex( int ix ){

		if (removingNodeIsBlocked.indexOf(ix) < 0) {
			removingNodeIsBlocked.add(ix);
		}

	}
	synchronized protected void _unblockRemovalForIndex( int ix ){
		
		int rix = removingNodeIsBlocked.indexOf(ix);
		while (rix>=0){
			removingNodeIsBlocked.remove(rix) ;
			rix = removingNodeIsBlocked.indexOf(ix);
		}
	}

	// ========================================================================

	/**
	 * @return the msgSrv
	 */
	protected void stopMsgSrv() {
		msgSrv.stop() ;
		taskQueue.clear();
		observingNodes.clear() ;
		removingNodeIsBlocked.clear();
		out=null;
	}

	class ServiceLoop implements Runnable{

		QTask qTask;
		
		Thread srvinfThrd;
		public ServiceLoop(QTask qTask){
		
			this.qTask = qTask;
			
			srvinfThrd = new Thread(this,"srvinfThrd-"+(taskQueue.size()+1)) ;
			srvinfThrd.start();
		}
		
		// ========================================================================
		
		@SuppressWarnings("unchecked")
		public void releaseEvent( NetworkMessageIntf observingNode, int taskID, NodeTask task) {
			
			if (observingNode==null){
				return;
			}
			try{
				
				if (taskID== NodeTask._TASK_RNDINIT){
					observingNode.onRequestForRandomInit( task.obj1 );
				}
				if (taskID== NodeTask._TASK_SETVAR){
					observingNode.onDefiningFeatureSet( task.obj1, (DataHandlingPropertiesIntf) task.obj2);
					
				} 

				if (taskID== NodeTask._TASK_SETTV){
					observingNode.onDefiningTargetVar( task.obj1 );
					
				}
				if (taskID== NodeTask._TASK_SETDATA){
					observingNode.onSendingDataObject( task.obj1, (DataHandlingPropertiesIntf) task.obj2);
				}

				if (taskID== NodeTask._TASK_ADAPT){
					// sends a WeightVectorIntf, together with some params
					observingNode.onRequestForAdaptingWeightVector( task.obj1 , task.obj1 );
				}
					 
				if (taskID== NodeTask._TASK_UPDATE){
					observingNode.onRequestForDedicatedUpdate();
				}
				if (taskID== NodeTask._TASK_CLEAR){
					observingNode.onRequestForMemoryReset();
				}
				if (taskID== NodeTask._TASK_REMOVE){
					if (task.obj1!=null){
						observingNode.onRequestForDataRemoval( (ArrayList<Long>)task.obj1 );
					}else{
						observingNode.onRequestForDataRemoval( );
					}
				}
				if (taskID== NodeTask._TASK_STATECHG){
					observingNode.onArrivalOfChemicalStimulus();
					
					observingNode.onRequestForChangingActivityLevel();
				}
				
			}catch(Exception e){
				// educated silence...
				e.printStackTrace();
			}

			
			
		}
		 
		public void handlingQueuedTask( QTask qTask){
			
			int startIndex , endIndex;
			int taskID = qTask.task.taskID;
			
			
			if (qTask.criterion==0){ // all nodes
				 
				
				for (int i=0;i<observingNodes.size();i++){
				
					_blockRemovalForIndex( i );
					
					// if (i==5)
					{
											out.print(4, "NodesInformer, releaseEvent() for node <"+i+"> ("+observingNodes.get(i).toString()+")");
						releaseEvent( observingNodes.get(i), taskID,qTask.task );
					}
					_unblockRemovalForIndex( i );
					
				}// i-> all registered nodes
				
			}
			if (qTask.criterion==1){ // by index
				
				int ix = qTask.index;
				
				releaseEvent( observingNodes.get(ix), taskID,qTask.task );
			}
			
			if (qTask.criterion==2){ // all numID
				long  nID = qTask.numID ;
				
				
			}
			
		}

		
		private void perform(){
			
			handlingQueuedTask( qTask);
			
			qTask=null;
		}
		
		@Override
		public void run() {
			 perform();
			
		}
	
		
	}

	// ========================================================================
	 
	class MessageServer implements Runnable{
		// we need to hide the Runable interface in order to make it possible to subclass the NodesInformer
		NodesInformer parent;
		Thread nodinfThrd;
		boolean isworking;
		
		public MessageServer( NodesInformer parent){
		
			this.parent = parent;
			nodinfThrd = new Thread(this,"nodinfThrd") ;
			nodinfThrd.start();
		}
		
		
		public void stop(){
			isRunning = false;
			while (isworking){
				out.delay(1);
			}
			taskQueue.clear() ;
		}
		
		@Override
		public void run() {
			isworking=false;
			isRunning = true;
			try{
				
				while ((isRunning) && (parent!=null)){
					
					if (taskQueue.size()>0){
						isworking=true;
						// Step 2 : working through the internal queue of notifications
						// 	        selecting the first one, handling it in its own data space         
						new ServiceLoop( taskQueue.get(0) );
						
						taskQueue.remove( 0 );
						
						isworking=false;
						
					}else{
						// a mini delay only if nothing is to be done
						delay(2) ;
					}
					
				}
				// Thread.currentThread().join();
			}catch(Exception e){
				isRunning = false;
				isworking=false;
			}
		}
		
		
	} // innner class MessageServer
	
	class QTask{
		
		int criterion=-1;
		int index = -1 ;
		long numID =-1 ;
		NodeTask task = null;
		
		 
		public QTask( int crit, int ix, long numid, NodeTask task) {
		 
			criterion = crit ;
			index = ix;
			numID = numid;
			this.task = task;
		}
		
	}  
	

	@SuppressWarnings("static-access")
	public void delay(int millis){
		try {
			Thread.currentThread().yield();
			Thread.currentThread().sleep(millis);
		} catch (Exception e) {}
	}
	

	@SuppressWarnings("static-access")
	public void minidelay(int nanos){
		try {
			Thread.currentThread().yield();
			Thread.currentThread().sleep(0,nanos);
		} catch (Exception e) {}
	}
	

	
}



/*


setChanged
protected void setChanged()Marks this Observable object as having been changed; the hasChanged method will now return true. 


--------------------------------------------------------------------------------

clearChanged
protected void clearChanged()Indicates that this object has no longer changed, or that it has already notified all of its observers of its most recent change, so that the hasChanged method will now return false. This method is called automatically by the notifyObservers methods. 

See Also:
notifyObservers(), notifyObservers(java.lang.Object)

--------------------------------------------------------------------------------

hasChanged
public boolean hasChanged()Tests if this object has changed. 

Returns:
true if and only if the setChanged method has been called more recently than the clearChanged method on this object; false otherwise.
See Also:
clearChanged(), setChanged()


*/
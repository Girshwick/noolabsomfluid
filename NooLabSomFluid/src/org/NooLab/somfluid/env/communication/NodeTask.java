package org.NooLab.somfluid.env.communication;


public class NodeTask {

	
	public final static int _TASK_RNDINIT  = 1;
	
	public final static int _TASK_SETDATA  = 5;
	public final static int _TASK_SETVAR   = 7;
	public final static int _TASK_SETTV    = 8;
	
	public final static int _TASK_ADAPT    = 9;
	public final static int _TASK_UPDATE   = 10;
	public final static int _TASK_CLEAR    = 11;
	public final static int _TASK_REMOVE   = 15;
	public final static int _TASK_STIMULE  = 20;
	public final static int _TASK_STATECHG = 21;

	
	int taskID = -1;
	
	Object obj1=null, obj2=null ; 
	
	
	public NodeTask( int tc){
		taskID = tc;
	}
	public NodeTask( int tc, Object o){
		taskID = tc;
		obj1 = o;
	}
	
	public NodeTask( int tc, Object o1, Object o2){

		taskID = tc;
		obj1 = o1;
		obj2 = o2;
	}

	public int getTask() {
		return taskID;
	}

	public Object getObj1() {
		return obj1;
	}

	public Object getObj2() {
		return obj2;
	}

	
	
}



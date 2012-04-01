package org.NooLab.repulsive.intf.main;

public interface RepulsionFieldEventsIntf {

	public static final int _FIELDACTION_ADD = 1;
	public static final int _FIELDACTION_DEL = 2;
	public static final int _FIELDACTION_MRG = 3;
	public static final int _FIELDACTION_MOV = 4;
	
	public static final int _FIELDSTATE_ACC = 1;
	public static final int _FIELDSTATE_DLY = 3;
	public static final int _FIELDSTATE_DEN = 5;
	public static final int _FIELDSTATE_FIN = 8;
	
	
	
	public void onLayoutCompleted( int flag ); 

	public void onSelectionRequestCompleted(Object results);
	
	public void onAreaSizeChanged( Object observable, int width, int height);
	
	public void onActionAccepted( int action, int state, Object param);
	
	public void statusMessage( String msg);

	public void onCalculationsCompleted();
	
	public boolean getInitComplete();
}

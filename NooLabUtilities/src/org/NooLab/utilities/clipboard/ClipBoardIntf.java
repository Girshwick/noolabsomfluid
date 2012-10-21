package org.NooLab.utilities.clipboard;

public interface ClipBoardIntf {

	public static final int CB_CONTENT_TYPE_STRING = 1;
	public static final int CB_CONTENT_TYPE_IMAGE  = 2;
	public static final int CB_CONTENT_TYPE_LIST   = 3;
	
	
	public void clipboardContentStrEvent( String str);
	// ABC789
	// void clipboardContentStrEvent(ClipBoardListener clipp, String str);
	
	public void clipboardContentImgEvent( Object obj );
	
	public void clipboardProcessStopped( boolean alive) ;
	
	

	
}

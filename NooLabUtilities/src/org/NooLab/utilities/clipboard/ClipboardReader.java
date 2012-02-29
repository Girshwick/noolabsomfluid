package org.NooLab.utilities.clipboard;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

 
import org.NooLab.utilities.strings.MD5Content;



// http://www.exampledepot.com/egs/java.awt.datatransfer/pkg.html


// http://code.google.com/p/jintellitype/

// https://bitbucket.org/agynamix/ossupport-connector/src
	


public class ClipboardReader {

	private Method clipboardEventMethod;
	private ClipboardOwner owner ;
	
	int timeOut = 2300 ;
	
	Clipboard clipboard ;
	
	int deliveredContentID = 0;
	
	String lastClipboardText = "" ;
	
	RemovalTimer removalTimer ;
	
	MD5Content md5Content = new MD5Content();
	
	Map<String,Integer> mappedContent = new TreeMap<String,Integer>();
	
	private ClipboardSupervisor csv ;
	
	ClipBoardIntf callingInstance;
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public ClipboardReader( ClipBoardIntf co  ){
		
		callingInstance = co;
		
		setupProcessingEvent( co );
		
		csv = new ClipboardSupervisor();
		
		// checkingClipboard() ;
	}
	
	// . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .

	public void destroy(){
		
		if (csv!=null){
			csv.stopSupervising();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setupProcessingEvent( ClipBoardIntf coi){ // ContentCrawlerCallerIntf
 
		
		try {
			 
			Class clazz1 = coi.getClass(); 
			
			
			// clipboardEventMethod = clazz1.getMethod( "clipboardContentEvent", String.class ) ; // this.getClass
			
			 
			// note, that this does not work, if we will try to invoke a method within a non-public class
			
		} catch (Exception e) {
			System.out.println("you may create a method \"clipboardContentEvent()\"...");
			e.printStackTrace();
		}

		
	}
	 
	
	

	 
	 public String getClipboardContents() {
		    
		String result = "";
		boolean hasTransferableText ; 
		Clipboard clipboard ;
		
		
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// odd: the Object param of getContents is not currently used
		Transferable contents = clipboard.getContents(null);
		hasTransferableText = (contents != null) && 
							  (contents.isDataFlavorSupported(DataFlavor.stringFlavor)) ;
		
		if (hasTransferableText) {
			try {
				
				result = (String) contents.getTransferData(DataFlavor.stringFlavor);
				
			} catch (UnsupportedFlavorException ex) {
				// highly unlikely since we are using a standard DataFlavor
				System.out.println(ex);
				ex.printStackTrace();
			} catch (IOException ex) {
				System.out.println(ex);
				ex.printStackTrace();
			}
		}
		return result;
	}
	 
	  
	private void clearClipboard(){

		setClipboard("");
	}
	
	private void setClipboard(String str) {
		StringSelection strsel ;
		
		
		strsel = new StringSelection(str);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(strsel, null);
		
	}

	// If an image is on the system clipboard, this method returns it;
	// otherwise it returns null.
	public static Image getClipboardImg() {
		Transferable t ;
		Image cbimg ;
		
		t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);

	    try {
	        if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
	            cbimg = (Image)t.getTransferData(DataFlavor.imageFlavor);
	            return cbimg;
	        }
	    } catch (UnsupportedFlavorException e) {
	    } catch (IOException e) {
	    }
	    return null;
	}
	
	 

	// This class is used to hold an image while on the clipboard.
	public static class ImageSelection implements Transferable {
	    private Image image;

	    public ImageSelection(Image image) {
	        this.image = image;
	    }

	    // Returns supported flavors
	    public DataFlavor[] getTransferDataFlavors() {
	        return new DataFlavor[]{DataFlavor.imageFlavor};
	    }

	    // Returns true if flavor is supported
	    public boolean isDataFlavorSupported(DataFlavor flavor) {
	        return DataFlavor.imageFlavor.equals(flavor);
	    }

	    // Returns image
	    public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
	        if (!DataFlavor.imageFlavor.equals(flavor)) {
	            throw new UnsupportedFlavorException(flavor);
	        }
	        return image;
	    }
	}
	/*
	public boolean checkingClipboard(){
		boolean rb=false;
		StringSelection ss ;
		
		// Create a clipboard owner
		owner = new ClipboardOwnerInstance();

		// Set a string on the system clipboard and include the owner object
		ss = new StringSelection("A String");
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard(); // .setContents(ss, owner);
		
		return rb;
	}
	*/
	
	public String getLastClipboardText() {
		return lastClipboardText;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		this.timeOut = timeOut;
	}

	protected String qualifyClipboardString( String str ){
		String _md5;
		
		
		try {
			
			_md5 = 	md5Content.getDigestforStr(  str )  ;
			
			if (mappedContent.containsKey( _md5)){
				str = "";
			}else{
				deliveredContentID++; 
				mappedContent.put(_md5,deliveredContentID);
				clearClipboard();
				new RemovalTimer( timeOut, mappedContent, deliveredContentID );
			}

		} catch (NoSuchAlgorithmException e) {
			 
			e.printStackTrace();
		} catch (IOException e) {
			 
			e.printStackTrace();
		}
		 

		
		return str ;
	}
	

	class ClipboardSupervisor implements Runnable{
		
		Thread csvThrd;
		boolean alive = false;
		
		public ClipboardSupervisor(){
			
			csvThrd = new Thread(this);
			csvThrd.start();
		}

		public void stopSupervising(){
			csvThrd.interrupt();
		}
		
		@SuppressWarnings("static-access")
		public void run() {
			int z=1;
			boolean readingAllowed = true;
			String str ;
			try{
				
				alive  = true;
				
				while (alive){
					csvThrd.sleep(50);
					
					if (readingAllowed){
						if (z % 40 == 0) {
							System.out.print(".");
						}
						if (z % 200 == 0) {
							System.out.println();
							z = 1;
						}

						str = getClipboardContents();

						if (str.length() > 0) {
							readingAllowed = false;
							// if we did not see it, then no changes, if we know
							// it, the string will be empty then
							str = qualifyClipboardString(str);

							if (str.length() > 0) {
								
								((ClipBoardIntf) callingInstance).clipboardContentStrEvent(str);
								/*
								if (clipboardEventMethod != null) {
									clipboardEventMethod.invoke( callingInstance, str);
									
								} else {
									System.out.println("Trying to invoke call-back failed for content : "+ str);
								}
								*/
							}
							readingAllowed = true;
						}

					}
					z++;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
			System.out.println("thread stopped and left...");
		}
		 
	} // end inner class ClipboardSupervisor

	
	
	class RemovalTimer{
		
		Timer timer;
		
		int revomalsID;
		Map<String,Integer> mappedContent;
		
		public RemovalTimer( int timeout, Map<String,Integer> mappedcontent, int id){
			mappedContent = mappedcontent ;
			revomalsID = id;

			startInstance( timeout );
			
		}
		
		public void startInstance( int timeout ){
			
			
			timer = new Timer();
			timer.schedule(new removalTask(), timeout);
		}
		
		
		
		class removalTask extends TimerTask {
			
			@SuppressWarnings("rawtypes")
			public void run() {
				Iterator it ;
				String str, previous_content;
				int id;
				Map.Entry pairs ;
				
				// System.out.println("going to remove old content from map (id="+revomalsID+")");
				
				it = mappedContent.entrySet().iterator();
				
			    while (it.hasNext()) {
			        pairs = (Map.Entry)it.next();
			    
			        previous_content = (String) pairs.getKey();
			        
			        id  = (int)(Integer) pairs.getValue();
			        
			        if (id==revomalsID){
			        	// System.out.println("old content (id="+revomalsID+") removed from map.");
			        	mappedContent.remove( pairs.getKey() ) ;
			        	//clearClipboard()
			        	break;
			        }
			    }

				timer.cancel();
			}
		}
		
	}
} // ClipboardReader


//This class serves as the clipboard owner.
class ClipboardOwnerInstance implements ClipboardOwner {
   
	public ClipboardOwnerInstance(){
		
	}
	
	// This method is called when this object is no longer
    // the owner of the item on the system clipboard.
   
	public void lostOwnership( 	java.awt.datatransfer.Clipboard clipboard,
								Transferable contents) {
	}
	
}


class RemovalTimer{
	
	Timer timer;
	
	int revomalsID;
	Map<String,Integer> mappedContent;
	
	public RemovalTimer( int timeout, Map<String,Integer> mappedcontent, int id){
		mappedContent = mappedcontent ;
		revomalsID = id;

		startInstance( timeout );
		
	}
	
	public void startInstance( int timeout ){
		
		
		timer = new Timer();
		timer.schedule(new removalTask(), timeout);
	}
	
	
	
	class removalTask extends TimerTask {
		
		@SuppressWarnings("rawtypes")
		public void run() {
			Iterator it ;
			String str, previous_content;
			int id;
			Map.Entry pairs ;
			
			// System.out.println("going to remove old content from map (id="+revomalsID+")");
			
			it = mappedContent.entrySet().iterator();
			
		    while (it.hasNext()) {
		        pairs = (Map.Entry)it.next();
		    
		        previous_content = (String) pairs.getKey();
		        
		        id  = (int)(Integer) pairs.getValue();
		        
		        if (id==revomalsID){
		        	// System.out.println("old content (id="+revomalsID+") removed from map.");
		        	mappedContent.remove( pairs.getKey() ) ;
		        	//clearClipboard()
		        	break;
		        }
		    }

			timer.cancel();
		}
	}
	
}

package org.NooLab.utilities.clipboard;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.AWTEventListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.NooLab.utilities.clipboard.ClipBoardListener.RemovalTimer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.process.OSDetector;

/**
 * 
 * Note that on Mac we need a periodic check for changed content, possibly with
 * removal of previous content
 * 
 * http://clipall.googlecode.com/svn-history/r16/trunk/ClipAll/src/dev/clipall/
 * business/DefaultClipboardOwner.java
 * 
 */

public class ClipBoardListener implements Runnable, ClipboardOwner {

	static ClipBoardIntf callingInstance = null;

	int timeOut = -1;
	boolean isRunning = false;
	boolean opSysOSX = false;

	static CBoardProperties cBoardProperties;
	private static ClipBoardListener owner;

	private static String appRelativeLocalName;

	int systemDelay = 200;
	Clipboard sysClip ;

	ArrayList<String> contentBufferStr = new ArrayList<String>();
	int buffersize = 2;

	private String lastClipboardText = "";
	boolean nextActionBlocked=false;
	
	private RemovalTimer removalTimer;
	boolean removalIsScheduled = false;

	Thread clippThrd;
	PrintLog out = new PrintLog(2, true);

	// ------------------------------------------------------------------------

	public static void main(String[] args) {
		ClipBoardListener b = new ClipBoardListener(null);
		b.start();
	}

	// ========================================================================

	public static ClipBoardListener getInstance(String localName,
			ClipBoardIntf callinginstance, CBoardProperties cbProps) {
		callingInstance = callinginstance;
		appRelativeLocalName = localName;
		return init(cbProps);
	}

	public static ClipBoardListener getInstance( ClipBoardIntf callinginstance,
												 CBoardProperties cbProps) {
		callingInstance = callinginstance;
		return init(cbProps);
	}

	public static ClipBoardListener getInstance(String localName,
			CBoardProperties cbProps) {
		return init(cbProps);
	}

	public static ClipBoardListener getInstance(CBoardProperties cbProps) {

		return init(cbProps);
	}

	private void close() {
		removalIsScheduled = false;
		// csvIsRunning = false;
		callingInstance = null;
		out.print(1, "clipoard is shutting down...");
		isRunning=false;
		
		FlavorListener[] fL = sysClip.getFlavorListeners() ;

    	if ((fL!=null) && (fL.length>0)){
    		for (int i=0;i<fL.length;i++){
    			sysClip.removeFlavorListener( fL[i]);
    		}
    	}
    	
    	
	}

	private static ClipBoardListener init(CBoardProperties cbProps) {

		if (cbProps == null) {
			if (cBoardProperties == null) {
				cBoardProperties = new CBoardProperties();
			}
		} else {
			cBoardProperties = cbProps;
		}

		if (owner==null){
			owner = new ClipBoardListener(callingInstance);
		}
		return owner;

	}

	private ClipBoardListener(String localname, ClipBoardIntf co) {

		callingInstance = co;

		prepare();
		cBoardProperties = provideProperties();
		cBoardProperties.setFollowUp(1);

	}

	private ClipBoardListener(ClipBoardIntf co) {
		super();

		// constructor is private, because it should exist only once!
		callingInstance = co;
		sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
		prepare();

		cBoardProperties = provideProperties();
		cBoardProperties.setFollowUp(1);

	}

	// ========================================================================

	public static CBoardProperties getPropertiesInstance() {
		return provideProperties();
	}

	public static CBoardProperties getProperties() {
		return provideProperties();
	}

	private static CBoardProperties provideProperties() {
		if (cBoardProperties == null) {
			cBoardProperties = new CBoardProperties();
		}
		return (CBoardProperties) cBoardProperties;
	}

	// ------------------------------------------------------------------------

	private void prepare() {

		out.setPrefix("[Clipp]");

		opSysOSX = OSDetector.isMac();

		removalTimer = new RemovalTimer(this, timeOut);
	}

	// ========================================================================

	public void clearClipboard() {
		StringSelection reply;

		reply = new StringSelection("");
		sysClip.setContents(reply, this);

	}

	public void stopListening() {

		// isRunning=false; // ABC789
		removeListener();
	}

	public void removeListener() {
		callingInstance = null;
	}

	public void addListener(ClipBoardIntf co) {
		callingInstance = co;
	}

	public void start() {

		initiateInitialFetch();

		out.delay(50);
		clippThrd = new Thread(this, "clippThrd");
		clippThrd.start();
	}

	public void run() {

		if (timeOut > 0) {

			(new TimeOutSuperVisor()).start();
		}
		isRunning = true;
		
		if (sysClip!=null){
			
			Transferable trans = sysClip.getContents(this);
			if (trans!=null){
				
				try {

					
					processContents(1,trans) ;  //ABC789

				} catch (Exception e) {
					e.printStackTrace();
				} 
			}
			regainOwnership(trans);
		}
		

		out.print(2, "Listening to board...");

		while (isRunning) {
			PrintLog.Delay(5);
		}
		sysClip = null;
		out.print(2, "Stopping listening to board.");
	}

	/**
	 * 
	 * this event happens if another application copies to the clipboard
	 */
	public void lostOwnership(Clipboard c, Transferable t) {

		if (sysClip == null) {
			return;
		}

		if (waitBriefly() < 0) {
			waitBriefly();
		}

		Transferable contents = sysClip.getContents(this); // EXCEPTION
		StringSelection reply;

		try {
			if (callingInstance != null) {
				if (nextActionBlocked){
					clearClipboard();
				}
				processContents(2,contents);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (callingInstance != null) {
			regainOwnership(contents);
		}
	}

	/**
	 * Place a String on the clipboard, and make this class the owner of the
	 * Clipboard's contents.
	 */
	public void setClipboardContents(String aString) {

		StringSelection stringSelection = new StringSelection(aString);

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		clipboard.setContents(stringSelection, this);
	}

	/**
	 * for periodic checking e.g. on MAC
	 * 
	 * @return
	 */
	public String getClipboardContents() {

		Transferable contents = null;
		String result = "";
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

		// odd: the Object param of getContents is not currently used

		// contents = clipboard.getContents(null);
		if (sysClip != null) {
			contents = sysClip.getContents(this);
		}

		boolean hasTransferableText = (contents != null)
				&& contents.isDataFlavorSupported(DataFlavor.stringFlavor);

		if (hasTransferableText) {
			try {

				result = (String) contents
						.getTransferData(DataFlavor.stringFlavor);
				lastClipboardText = result;

			} catch (UnsupportedFlavorException ex) {
				// highly unlikely since we are using a standard DataFlavor
				System.err.println(ex);
				// ex.printStackTrace();
			} catch (IOException ex) {
				System.err.println(ex);
				// ex.printStackTrace();
			}
		}
		return result;
	}

	void processContents(int i,Transferable t) throws UnsupportedFlavorException,
			IOException, Exception {

		String cbStr = "";

		out.print(3, "Clipboard, processContents(), param = "+i);
		
		if (nextActionBlocked){
			nextActionBlocked=false;
			// return;
		}
		if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {

			// FIXME java.lang.ClassCastException: java.lang.String cannot be
			// cast to java.util.List
			ArrayList<File> list = (ArrayList<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

			// GenericMediator.getInstance().itemsToClipboardEvent(list);
			// System.out.println("files -->  " + Utils.filesToString(list));

		}

		if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {

			cbStr = (String) t.getTransferData(DataFlavor.stringFlavor);
			// GenericMediator.getInstance().itemsToClipboardEvent((String)tr.getTransferData(DataFlavor.stringFlavor));
			// System.out.println("Processing: " +
			// tr.getTransferData(DataFlavor.stringFlavor));

			if ((contentIsBufferedStr(cbStr))
					&& (cBoardProperties.isBlockIdenticalContent())) {
				return;
				// we do not inform the calling instance
			}

			lastClipboardText = cbStr;
			if (callingInstance != null) {
				((ClipBoardIntf) callingInstance).clipboardContentStrEvent(cbStr);
			}

		}

		if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {

			Object imgobj = (Object) t.getTransferData(DataFlavor.imageFlavor);

			// get mime type from the Transferable
			
			if ((callingInstance!=null) && (imgobj!=null)){
				((ClipBoardIntf) callingInstance).clipboardContentImgEvent(imgobj);
			}
		}

		// System.out.println("CB listener processing : " +
		// t.toString().replace("sun.awt.datatransfer.", "") + "  -> " + cbStr);

	}

	private boolean contentIsBufferedStr(String cbStr) {
		//
		boolean rB = false;
		String str;
		for (int i = 0; i < contentBufferStr.size(); i++) {
			str = contentBufferStr.get(i);
			if (str.contentEquals(cbStr)) {
				rB = true;
			}
		}
		if (rB == false) {
			if (contentBufferStr.size() > 0) {
				contentBufferStr.remove(0);
			}
			contentBufferStr.add(cbStr);
		} else {
			if (contentBufferStr.size() > 0) {
				contentBufferStr.remove(0);
			}
		}

		return rB;
	}

	void regainOwnership(Transferable t) {

		StringSelection reply;

		if (sysClip == null) {
			return;
		}

		if (cBoardProperties.getFollowUp() == CBoardPropertiesIntf._FOLLOWUP_CLEAR_AFTER_FETCHING) {
			reply = new StringSelection("");
			sysClip.setContents(reply, this);
		} else {
			sysClip.setContents(t, this);
		}

	}

	public String getLastClipboardText() {
		return lastClipboardText;
	}

	public void setRunning(boolean isRunning) {
		this.isRunning = isRunning;
	}

	private int waitBriefly() {
		int r = 0;
		try {
			Thread.sleep(systemDelay);
		} catch (Exception e) {
			if (systemDelay < 500) {
				systemDelay = systemDelay + 50;
			}
			r = -3;
		}
		return r;
	}

	private void initiateInitialFetch() {

		if (cBoardProperties.getAllowForFetchOnStart()) {
			out.print(2, "Preparing board for initial fetch...");

			new InitialFetch();
		}

	}

	// --------------------------------------------------------------
	// this is started from the constructor!
	// it waits a bit and then tries to get the clipboard,
	class InitialFetch implements Runnable {

		Thread ifThrd;

		// ................................................
		public InitialFetch() {

			ifThrd = new Thread(this, "ifThrd");
			ifThrd.start();
		}

		// ................................................

		@Override
		public void run() {

			PrintLog.Delay(120);
			out.print(3, "Performing initial fetch...");
			String str = getClipboardContents();

			if ((str != null) && (str.length() > 0)) {
				out.print(4, "S.TH.found on initial fetch (len=" + str.length()
						+ ")...");
				if (callingInstance != null) {
					((ClipBoardIntf) callingInstance).clipboardContentStrEvent(str);
				}
				// out.delay(4000);
				stopListening();
			} else {
				out.print(3, "Nothing found on initial fetch...");
			}
		}
	}

	// --------------------------------------------------------------

	public boolean isNextActionBlocked() {
		return nextActionBlocked;
	}

	public void setNextActionBlocked(boolean nextActionBlocked) {
		this.nextActionBlocked = nextActionBlocked;
	}

	public int getTimeOut() {
		return timeOut;
	}

	public void setTimeOut(int timeOut) {
		if (sysClip==null){  // ABC789
			sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();
		}
		this.timeOut = timeOut;
	}

	// --------------------------------------------------------------
	// this one actually without a java timer... just dojo java loop
	class RemovalTimer implements Runnable {

		Thread cliprtThrd;
		int revomalsID;
		long now;
		boolean removalIsScheduled = false;

		ClipBoardListener clipListener;
		int timeOut;
		String rrStr = "";

		public RemovalTimer(ClipBoardListener listener, int timeout) {

			clipListener = listener;

			timeOut = timeout;
			cliprtThrd = new Thread(this, "cliprtThrd");
			startInstance();

		}

		public void setTimeOut(int timeout) {

			timeOut = timeout;
			now = System.currentTimeMillis();
		}

		public void startInstance() {

			// clipListener.out.print(2,"\n\nRemovalTimer: clipboard reader now waiting for "+(timeOut/1000.0)+" seconds");
			now = System.currentTimeMillis();
			cliprtThrd.start();
		}

		public void stop() {
			removalIsScheduled = false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void run() {
			//
			Iterator it = null;
			;
			String str, previous_content;
			int id;

			removalIsScheduled = true;
			long dT = -1;
			// clipListener.out.print(2,"timing out has been started");

			while ((removalIsScheduled) && (dT < timeOut)) {// &&
															// (clipListener.csv.alive)){

				dT = System.currentTimeMillis() - now;

				PrintLog.Delay(10);

			}
			removalIsScheduled = false;
			// if (
			// clipListener.reasonStr = "removal timer "+ rrStr;
			// clipListener.csv.alive=false;
			// clipListener.out.print(2,"clipboard reader has been stopped and removed.");
		}

	}

	// --------------------------------------------------------------

	// --------------------------------------------------------------
	class TimeOutSuperVisor implements Runnable {

		boolean csvIsRunning = false;
		Thread cblTimeoutThrd;
		long csvStartTime;

		public TimeOutSuperVisor() {
			cblTimeoutThrd = new Thread(this, "cblTimeoutThrd");
		}

		public void start() {
			csvStartTime = System.currentTimeMillis();
			cblTimeoutThrd.start();
		}

		@Override
		public void run() {
			csvIsRunning = true;
			while (csvIsRunning) {
				PrintLog.Delay(5);
				if (System.currentTimeMillis() - csvStartTime > timeOut) {
					csvIsRunning = false;
				}
			}
			isRunning = false;
		}

	}
	// --------------------------------------------------------------
}

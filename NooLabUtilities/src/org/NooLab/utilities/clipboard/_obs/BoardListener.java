package org.NooLab.utilities.clipboard._obs;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;

import org.NooLab.utilities.clipboard.CBoardProperties;
import org.NooLab.utilities.clipboard.CBoardPropertiesIntf;

class BoardListener extends Thread implements ClipboardOwner {

	
	
	int lastState = 1;
	int systemDelay=200;
	
	int followUp = 0;
	
	static CBoardProperties cBoardProperties;
	
	StringSelection reply ;
	Clipboard sysClip = Toolkit.getDefaultToolkit().getSystemClipboard();

	
	// ========================================================================
	public BoardListener(){
		cBoardProperties = new CBoardProperties ();
		cBoardProperties.setFollowUp( 1 );
	}
	// ========================================================================
	
	public static CBoardPropertiesIntf getPropertiesInstance(){
		return (CBoardPropertiesIntf)cBoardProperties;
	}
	// ------------------------------------------------------------------------

	
	
	
	// ------------------------------------------------------------------------
	public void run() {
		Transferable trans = sysClip.getContents(this);
		regainOwnership(trans);
		System.out.println("Listening to board...");
		while (true) {
		}
	}

	public void lostOwnership(Clipboard c, Transferable t) {

		if (waitBriefly()<0){
			waitBriefly();
		}

		lastState = 1;
		Transferable reply,contents = sysClip.getContents(this); // EXCEPTION

		try {

			processContents(contents);
			
			if (followUp == CBoardPropertiesIntf._FOLLOWUP_CLEAR_AFTER_FETCHING){
				reply = new StringSelection( "" );
			}else{
				reply = contents;
			}
			sysClip.setContents( reply, this );
			
			lastState = 0;
		} catch (Exception e) {
			lastState = -7;
			String estr = e.getMessage();
			if (estr.contains("could not open")){
				System.err.println("Transfer via clipboard failed, please reactivate and repeat.");
			}else{
				System.err.println("Transfer via clipboard failed, please reactivate and repeat.");
			}
		}

		regainOwnership(contents);
	}

	private int waitBriefly() {
		int r=0;
		try {
			Thread.sleep(systemDelay);
		} catch (Exception e) {
			if (systemDelay < 500) {
				systemDelay = systemDelay + 50;
			}
			r=-3;
		}
		return r;
	}

	void processContents(Transferable t) throws UnsupportedFlavorException, IOException {

		String cbStr = (String) t.getTransferData(DataFlavor.stringFlavor);
		System.out.println("Processing: " + t.toString().replace("sun.awt.datatransfer.", "") + "  -> " + cbStr);

	}

	void regainOwnership(Transferable t) {
		sysClip.setContents(t, this);
	}

	public static void main(String[] args) {
		BoardListener b = new BoardListener();
		b.start();
	}
}
package org.NooLab.utilities.clipboard._obs;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.io.IOException;


/**
 * 
 * Note that on Mac we need a periodic check for changed content,
 * possibly with removal of previous content
 * 
 *
 */
public class _ClipBoardListener implements Runnable, ClipboardOwner {

	boolean isRunning=false;
	Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	
	Thread cbThrd;
	public _ClipBoardListener(){
		cbThrd = new Thread (this,"cbThrd ");	
	}

	public void start() {
		cbThrd.start();
	}

	@Override
	public void run() {
		Transferable selection = systemClipboard.getContents(this);
		gainOwnership(selection);
		isRunning=true;
		while (isRunning) {
			try {
				Thread.sleep(1);
			} catch (Exception e) {}

		}
	}

	public void gainOwnership(Transferable t) {
		try {
			Thread.sleep(100);
		} catch (Exception e) {}
		systemClipboard.setContents(t, this);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		try {
			System.out.println((String) clipboard.getData(DataFlavor.stringFlavor));
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		gainOwnership(contents);
		/*
		isRunning=false; 
		try {
			Thread.sleep(10);
		} catch (Exception e) {}
		cbThrd.start();
		*/
	}

}
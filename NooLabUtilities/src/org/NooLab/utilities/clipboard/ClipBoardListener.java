package org.NooLab.utilities.clipboard;

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
public class ClipBoardListener extends Thread implements ClipboardOwner {

	Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

	public void run() {
		Transferable selection = systemClipboard.getContents(this);
		gainOwnership(selection);
		while (true) {
		}
	}

	public void gainOwnership(Transferable t) {
		try {
			this.sleep(100);
		} catch (InterruptedException e) {
		}
		systemClipboard.setContents(t, this);
	}

	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		try {
			System.out.println((String) clipboard.getData(DataFlavor.stringFlavor));
		} catch (UnsupportedFlavorException e) {
		} catch (IOException e) {
		}
		gainOwnership(contents);
	}
}
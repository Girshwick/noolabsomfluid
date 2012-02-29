package org.NooLab.utilities.clipboard;

import java.awt.*;
import java.awt.datatransfer.*;
import java.io.IOException;


// http://www.exampledepot.com/egs/java.awt.datatransfer/pkg.html


public class ClipboardWriter {

	 
	// This method writes a string to the system clipboard.
	// otherwise it returns null.
	public static void setClipboard(String str) {
	    StringSelection ss = new StringSelection(str);
	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, null);
	}
	 
	
	// This method writes a image to the system clipboard.
	// otherwise it returns null.
	public static void setClipboard(Image image) {
	    ImageSelection imgSel = new ImageSelection(image);
	    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
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
	
	public void checkingClipboard(){
		ClipboardOwner owner ;
		
		
		// Create a clipboard owner
		owner = new ClipboardWriterOwner();

		// Set a string on the system clipboard and include the owner object
		StringSelection ss = new StringSelection("A String");
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(ss, owner);
		
		
	}
}


//This class serves as the clipboard owner.
class ClipboardWriterOwner implements ClipboardOwner {
   
	// This method is called when this object is no longer
    // the owner of the item on the system clipboard.
   
	public void lostOwnership( 	java.awt.datatransfer.Clipboard clipboard,
								Transferable contents) {
		// To retrieve the contents, see
        // Getting and Setting Text on the System Clipboard
		
	}
}

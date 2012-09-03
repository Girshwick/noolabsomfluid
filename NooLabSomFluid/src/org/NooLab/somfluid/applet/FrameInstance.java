package org.NooLab.somfluid.applet;

import javax.swing.JFrame;

import processing.core.PApplet;


/**
 * 
 * 
 * http://wiki.processing.org/w/Swing_JSliders
 * 
 * 
 *
 */
public class FrameInstance {

	
	public void test(PApplet _applet){
		PApplet p1,p2;
		
	    JFrame f1 = new JFrame();
	    f1.setBounds(100, 100, 600, 200);
	    // myPAppletClass p1 = new  myPAppletClass ();
	    p1 = new  PApplet ();
	    p1.init();
	    f1.add(p1);
	    f1.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    f1.setVisible(true);

	    JFrame f2 = new JFrame();
	    f2.setBounds(10, 10, 600, 200);
	    p2 = new  PApplet();
	    p2.init();
	    f2.add(p2);
	    f2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	    f2.setVisible(true);
	    
	    /*
	    myframe.addWindowListener(new java.awt.event.WindowAdapter() {
        	public void windowClosing(java.awt.event.WindowEvent e) {
            	if (openWindows == 1) {
                    // Terminate when the last window is closed.
                	System.exit(0);  
            	}
            	g_openWindows--;
        	}
    	});
	     */
	    /*
	     if you could have a parent frame from which multiple child frames are 
	     launched, the parent frame could have setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	     */
	}
}

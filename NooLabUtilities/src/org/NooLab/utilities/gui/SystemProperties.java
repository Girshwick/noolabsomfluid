package org.NooLab.utilities.gui;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;

import org.NooLab.utilities.net.connex.NicAddresses;
import org.NooLab.utilities.process.OSDetector;

public class SystemProperties {

	static Dimension[] screenSizes = new Dimension[1] ;
	private static String macAddress;
	
	
	public static boolean isOSX(){
		return  OSDetector.isMac()  ;
	}
	public static boolean isLinux(){
		return  OSDetector.isLinux()  ;
	}
	public static boolean isWin(){
		return  OSDetector.isWindows()  ;
	}
	
	public static String operatingSystemName(){
		String osName = "";
		
		if (OSDetector.isMac()){ osName = "osx"; };
		if (OSDetector.isWindows()){ osName = "windows"; };
		if (OSDetector.isLinux()){ osName = "unix"; };
		
		return osName;
	}

	public static Dimension screenSize(){
		Dimension d = new Dimension();
		
		d.width = 800;
		d.height= 600;
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();

		screenSizes = new Dimension[gs.length] ;
		
		// Get size of each screen
		for (int i=0; i<gs.length; i++) {
		    DisplayMode dm = gs[i].getDisplayMode();
		    int screenWidth = dm.getWidth();
		    int screenHeight = dm.getHeight();

		    screenSizes[i] = new Dimension();
		    screenSizes[i].width  = screenWidth;
		    screenSizes[i].height = screenHeight;
		}
		
		return d;
	}

	public static Dimension[] getScreenSizes() {
		return screenSizes;
	}

	public static int getScreenHeight() {
		// 
		Dimension sz ;
		
		if ((screenSizes==null) || (screenSizes.length==0)){
			sz = screenSize();
		}else{
			sz = screenSizes[0];
			if (sz==null){
				sz = getBasicScreenSize();
			}
		}
		return sz.height;
	}
	
	public static int getScreenWidth() {
		// 
		Dimension sz ;
		
		if ((screenSizes==null) || (screenSizes.length==0)){
			sz = screenSize();
		}else{
			sz = screenSizes[0];
			if (sz==null){
				sz = getBasicScreenSize();
			}
		}
		return sz.width;
	}
	

	private static Dimension getBasicScreenSize() {
		
	    Toolkit tk = Toolkit.getDefaultToolkit();
	    Dimension d = tk.getScreenSize();
	    screenSizes[0] = new Dimension();
	    screenSizes[0] = d;
	    
	    return d;
	}

	public static String macAddress() {
		// 
		macAddress = NicAddresses.getMac();
		return macAddress;
	}

	
}

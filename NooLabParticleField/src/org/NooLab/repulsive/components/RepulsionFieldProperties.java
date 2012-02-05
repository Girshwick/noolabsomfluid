package org.NooLab.repulsive.components;

import java.io.Serializable;
import java.util.Random;

import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.CreateLogging;
import org.NooLab.utilities.logging.LogControl;
import org.NooLab.utilities.strings.StringsUtil;
 


public class RepulsionFieldProperties  implements Serializable{

	private static final long serialVersionUID = 6184779922611000235L;

	transient public static String __properties_FILE = "repulsionfield.properties" ;
	
	String homePath = "";
	String configPath = "";
	
	boolean fieldIsRandom = false;
	// this sets the relative strength of the randomization
	double relativeRandomness = 1.0 ;
	
	
	Random random = new Random();
	
	transient public DFutils fileutil = new DFutils();
	transient public StringsUtil strgutil = new StringsUtil(); 
	
	// ========================================================================
	public RepulsionFieldProperties(){
		random.setSeed(7531);
		random.nextGaussian() ;
	}
	// ========================================================================
	
	
	public void setHomePath(String path){
		if ((path.length()==0) || (fileutil.fileexists(path)==false)){
			path = getHomePath();
		}
		homePath = path;
		
		initLog();
	}
	
	private void initLog(){
		
		CreateLogging.setDebugLevel( LogControl.loggerLevel ) ;
		// like so, if required
		// LogControl.cLogger.setLogging( "org.apache.commons");
	}
	
	public String getHomePath(){
		
		if ((homePath.length()==0) || (fileutil.fileexists(homePath)==false)){
			homePath = fileutil.createPath( System.getProperty("user.home"), "rf/");
		}
		
		configPath = fileutil.createPath(homePath , "config/" );
	
		return homePath;
	}


	public boolean getFieldIsRandom() {
		return fieldIsRandom;
	}
	public void setFieldIsRandom(boolean isRandom) {
		this.fieldIsRandom = isRandom;
	}

	public double getRelativeRandomness() {
		return relativeRandomness;
	}


	public void setRelativeRandomness(double relativeRandomness) {
		this.relativeRandomness = relativeRandomness;
	}


	public Random getRandom() {
		return random;
	}



	
	
	
	
}

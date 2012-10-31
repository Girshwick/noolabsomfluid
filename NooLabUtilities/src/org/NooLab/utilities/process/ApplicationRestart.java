package org.NooLab.utilities.process;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;



public class ApplicationRestart {

	String binPath = "";
	private Class callingClass;
	private org.NooLab.utilities.process.CloseOperationIntf appCloseCall;

	public ApplicationRestart(Class clzz, String jarPath) {
		callingClass = clzz;
		binPath = jarPath;
	}

	public void go() throws URISyntaxException, IOException{
		
	  String javaBin = System.getProperty("java.home") ;// + File.separator + "bin" + File.separator + "java";
	  
	  if (binPath.length()>0){
		  javaBin = binPath; 
	  }
	  File currentJar = new File(callingClass.getProtectionDomain().getCodeSource().getLocation().toURI());

	  
	  /* is it a jar file? */
	  if(!currentJar.getName().endsWith(".jar")){
		  return;
	  }

	  
	  String appCall = currentJar.getPath() + " -restart"; 
	  /*
	   *  we need a small command, such that the restarted instance waits briefly (4 seconds)
	   *  right after the start
	   *  
	   *  in order to wait for this instance to close all satellites
	   */
	  
	  /* Build command: java -jar application.jar */
	  final ArrayList<String> command = new ArrayList<String>();
	  command.add(javaBin);
	  command.add("-jar");
	  command.add( appCall);

	  
	  final ProcessBuilder builder = new ProcessBuilder(command);
	  builder.start();
	  
	  if (appCloseCall!=null){
		  appCloseCall.close() ;
	  }else{
		  System.exit(0);
	  }
	}

	public void setCloseOperation(CloseOperationIntf app) {
		// 
		appCloseCall = app;
	}
}

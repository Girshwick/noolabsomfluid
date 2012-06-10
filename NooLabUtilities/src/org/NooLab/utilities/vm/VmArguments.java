package org.NooLab.utilities.vm;


import java.awt.List;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;

import java.lang.management.ManagementFactory;

import javax.management.ObjectName;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeDataSupport;


/**
 * 
 * MXBean utilities
 * HotSpotDiagnosticMBean : http://docs.oracle.com/javase/6/docs/jre/api/management/extension/index.html
 * 
 * ManagementFactory.getRuntimeMXBean()
 * getInputArguments()
 *
 */
public class VmArguments {

	public VmArguments(){
		
	}
	
	public ArrayList<String> getall(){
		return getAll();
	}
	public static ArrayList<String> getAll(){
		
		RuntimeMXBean RuntimemxBean = ManagementFactory.getRuntimeMXBean();
		ArrayList<String> arguments = (ArrayList<String>) RuntimemxBean.getInputArguments();

		return arguments;
	}
	
	public String mainClass(){
		return System.getProperty("sun.java.command");
	}
	
	
	public static void hotspotTest() throws Exception{
        printHotSpotOption("MaxHeapFreeRatio");
        printHotSpotOption("SurvivorRatio");
        printHotSpotOptions();
    }

    private static void printHotSpotOption(String option) throws Exception {
        ObjectName name = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        String operationName = "getVMOption";
        Object [] params = new Object [] {option};
        String [] signature = new String[] {String.class.getName()};
        Object result = ManagementFactory.getPlatformMBeanServer().invoke(name, operationName, params, signature);
        CompositeDataSupport data = (CompositeDataSupport) result;

        System.out.println(option);
        System.out.println("- Value: "+data.get("value"));
        System.out.println("- Origin: "+data.get("origin"));
    }

    private static void printHotSpotOptions() throws Exception {
        ObjectName name = new ObjectName("com.sun.management:type=HotSpotDiagnostic");
        String attributeName = "DiagnosticOptions";
        Object result = ManagementFactory.getPlatformMBeanServer().getAttribute(name, attributeName);
        CompositeData [] array = (CompositeData[]) result;
        for (CompositeData d : array) {
            System.out.println(d.get("name"));
            System.out.println("- Value: "+d.get("value"));
            System.out.println("- Origin: "+d.get("origin"));
        }
    }

    public String showShellStartupScript( String osLabel){
    	// also see  launch4j , which creates an exe, dmg
    	String winStr= "; \n" +  
    				"; my startscript  \n" + 
    				";  \n" + 
    				"JCMD=%JAVA_HOME%/bin/javaw  \n" + 
    				"JFLAGS=-Xss2048k -Xms64m -Xmx256m -Xmn50m -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy  \n" + 
    				"JPROG=-jar GameEngine.jar \n" +  
    	   
    				"%JCMD% %JFLAGS% %JPROG% \n"  ;
    	
    	String unixStr ="#!/bin/bash    \n" + 
						"#    \n" + 
						"# my startscript    \n" + 
						"#    \n" + 
						"JCMD=$JAVA_HOME/bin/javaw    \n" + 
						"JFLAGS=-Xss2048k -Xms64m -Xmx256m -Xmn50m -XX:+UseParallelGC -XX:+UseAdaptiveSizePolicy    \n" + 
						"JPROG=-jar GameEngine.jar    \n" + 
						"$JCMD $JFLAGS $JPROG ";
    	
    	 return winStr;
    }
    
}

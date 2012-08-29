package org.NooLab.utilities.process;

import java.io.File;
import java.io.IOException;
import java.util.List;

import jlibs.core.lang.JavaProcessBuilder;

public class JPBdemo {

	public JPBdemo() throws IOException{
		
		go();
		
	}
	
	public static void main(String[] args) throws IOException {
		 

		(new JPBdemo()).go() ;
	}
	
	public void go() throws IOException{
		
		JavaProcessBuilder jvm = new JavaProcessBuilder();
		
		/*
		JavaProcessBuilder is preconfigured with current java home and current working directory initially.
		you can change them as below:
		 */
		// jvm.javaHome(new File("c:/jdk5")); // to configure java home
		jvm.workingDir(new File("D:/data")); // to configure working directory

		//to configure various attributes:
        
		 
		// to configure classpath
		jvm.classpath("lib/jlibs-core.jar") // relative path from configured working dir
		   .classpath(new File("c:/myproject/lib/jlibs-xml.jar"));
		 
		
		// to get configured classpath
		List<File> classpath = jvm.classpath();

		// to configure additional classpath
		jvm.endorsedDir("lib/endorsed")
		   .extDir("lib/ext")
		   .libraryPath("lib/native")
		   .bootClasspath("lib/boot/xerces.jar")
		   .appendBootClasspath("lib/boot/xalan.jar")
		   .prependBootClasspath("lib/boot/dom.jar");

		// to configure System Properties
		jvm.systemProperty("myprop", "myvalue")
		   .systemProperty("myflag");

		// to configure heap and vmtype
		jvm.initialHeap(512); // or jvm.initialHeap("512m");
		jvm.maxHeap(1024); // or jvm.maxHeap("1024m");
		jvm.client(); // to use -client
		jvm.server(); // to use -server

		// to configure remote debugging
		jvm.debugPort(7000).debugSuspend(true);

		// to configure any additional jvm args
		jvm.jvmArg("-Xgc:somealgo");

		// to configure mainclass and its arguments
		jvm.mainClass("example.MyTest")
		   .arg("-xvf")
		   .arg("testDir");

		// to get the created command:
		String command[] = jvm.command();

		// to launch it
		Process p = jvm.launch(System.out, System.err);
	}
}

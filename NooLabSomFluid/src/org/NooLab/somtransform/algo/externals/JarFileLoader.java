package org.NooLab.somtransform.algo.externals;

import java.net.URL;
import java.io.IOException;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

import org.NooLab.somfluid.SomFluidPluginSettingsIntf;


/**
 * 
 * 
 * use:  cl.addFile("c:/java/testability-explorer/testability-explorer-1.3.2.jar");
 * 
 *
 */
public class JarFileLoader extends URLClassLoader {
	
	SomFluidPluginSettingsIntf pluginSettings;
	JarFileLoader jcl ;
	
	
	// ========================================================================
	public JarFileLoader(URL[] urls) {
		super(urls);
	}

	public JarFileLoader( SomFluidPluginSettingsIntf pluginSettings  ) {
		super( new URL[]{});
		
		this.pluginSettings = pluginSettings;
		jcl = this;
	}
	// ========================================================================
	
	
	
	public void addFile(String packagePathInJar) throws MalformedURLException {
		String urlPath = "jar:file:/" + packagePathInJar + "!/";
		addURL(new URL(urlPath));
	}

	public void addJarSource( String filePath ) throws MalformedURLException{
	
		jcl.addFile( filePath ) ;
		 
	}
	
	public void loadJars( ){
		
		try {
			URL urls[] = {};

			JarFileLoader cl = new JarFileLoader(urls);
			cl.addFile("/opt/mysql-connector-java-5.0.4/mysql-connector-java-5.0.4-bin.jar");
			System.out.println("Second attempt...");
			cl.loadClass("org.gjt.mm.mysql.Driver");
			System.out.println("Success!");
		} catch (Exception ex) {
			System.out.println("Failed.");
			ex.printStackTrace();
		}
	}
	
	
	
	public static void test0() {
		try {
			System.out.println("First attempt...");
			Class.forName("org.gjt.mm.mysql.Driver");
		} catch (Exception ex) {
			System.out.println("Failed.");
		}

		try {
			URL urls[] = {};

			JarFileLoader cl = new JarFileLoader(urls);
			cl.addFile("/opt/mysql-connector-java-5.0.4/mysql-connector-java-5.0.4-bin.jar");
			System.out.println("Second attempt...");
			cl.loadClass("org.gjt.mm.mysql.Driver");
			System.out.println("Success!");
		} catch (Exception ex) {
			System.out.println("Failed.");
			ex.printStackTrace();
		}
	}
}
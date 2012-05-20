package org.NooLab.somtransform.algo.externals;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Vector;

import org.NooLab.somfluid.SomFluidPluginSettingsIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.app.SomAppProperties;
import org.NooLab.somfluid.util.XmlStringHandling;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.files.DirectoryContent;
import org.NooLab.utilities.strings.StringsUtil;
import org.w3c.dom.Node;

import java.util.*;
import java.util.jar.JarFile;

/**
 * 
 * Actually, this loads the algorithms only the first time.
 * TODO: The loaded algorithm need a reference to this loader in order to create further instances if necessary...
 *       and it will be likely necessary: each stack needs its own instance!! 
 * 
 * 
 * 
 * 
 * ATTENTION: do not forget to rebuild the project before exporting the algorithm package via fatjar !!! 
 * 
 * http://stackoverflow.com/questions/3717476/how-can-i-load-a-class-from-a-jar-archive-which-implements-my-interface-java
 * 
 * >>>>>  http://snippets.dzone.com/posts/show/3574
 *
 */
public class AlgorithmPluginsLoader implements PluginLoaderIntf{
	
	boolean pluginsAvailable = false;
	SomFluidPluginSettingsIntf pluginSettings;
	SomFluidProperties sfProperties;
	
	ArrayList<String> jarfiles = new ArrayList<String>();
	
	JarFileLoader jLoader ;
	
	DFutils fileutil = new DFutils();
	
	// ========================================================================
	public AlgorithmPluginsLoader( SomFluidProperties properties) throws MalformedURLException, ClassNotFoundException{
		 
		init( properties, false );
	}
	
	public AlgorithmPluginsLoader(SomFluidProperties properties, boolean loadCatalog) {
		 
		init( properties, loadCatalog);
	}

	public AlgorithmPluginsLoader(SomAppProperties clappProperties, boolean loadCatalog) {
		
		init( clappProperties.getPropertiesConnection(), loadCatalog);
	}

	private void init( SomFluidProperties properties, boolean loadCatalog){
		
		sfProperties = properties ;
		pluginSettings = sfProperties.getPluginSettings() ;
		
		jLoader = new JarFileLoader( pluginSettings ) ;
		
		if (loadCatalog){
			loadCatalogDescriptions() ;
		}
	}
	
	// ========================================================================

	

	public void load() throws MalformedURLException{
		 
		
		
		getAvailableJarFiles();
		
		loadAlgorithmJars();
	}

	
	
	public void loadCatalogDescriptions(){
		
		String rawXmlStr="", xfile="", jarfilename,pkgname,namedItem, str = "",grouplabel;
		XmlStringHandling xMsg = new XmlStringHandling() ;
		
		Vector<Object> xmlContentItems, algorithmSectionNodes; 

		
		xfile = pluginSettings.getCatalogFileName() ;
		try {
			
			if (fileutil.fileexists(xfile)){
				rawXmlStr = fileutil.readFile2String(xfile) ;
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (xMsg.isXML(rawXmlStr)==false){
			return;
		}
		
		 
		xMsg.setContentRoot("somtransformer") ;
		xmlContentItems = xMsg.getItemsList(rawXmlStr, "//somtransformer", "/algorithm", "name") ;
		// <algorithm name="RunningMean"> <algorithm name="abc">, returns: "RunningMean","abc"   
		 
		
		for (int i=0;i<xmlContentItems.size();i++){
			Object obj = xmlContentItems.get(i) ;
			
			try{
				namedItem = (String)obj;	
			}catch(Exception e){
				namedItem = "";
			}
			
			/*
			 	<algorithm name="RunningMean"> 
    				<jar name="NooLabTransformAlgo.jar" />
    				<package name="org.noolab.algoplug.timeseries"/>
    				<group name="timeseries"/>
				</algorithm>
			 */
			if (namedItem.length()>0){
				// this would expect that "jar" is an attribute rather than a sub-tag-node as in our case
				// str = xMsg.getSpecifiedInfo(rawXmlStr, "//somtransformer/algorithm", "name", namedItem,"jar");
				// hence we need to get it first as a list of nodes which has just 1 member 

				algorithmSectionNodes = xMsg.selectListFromSpecifiedItem( rawXmlStr, "//somtransformer/algorithm","name", namedItem,"jar");
				jarfilename = xMsg.getSpecifiedItemInfo(algorithmSectionNodes.get(0), "name");
				
				algorithmSectionNodes = xMsg.selectListFromSpecifiedItem( rawXmlStr, "//somtransformer/algorithm","name", namedItem,"package");
				pkgname = xMsg.getSpecifiedItemInfo(algorithmSectionNodes.get(0), "name");
				
				algorithmSectionNodes = xMsg.selectListFromSpecifiedItem( rawXmlStr, "//somtransformer/algorithm","name", namedItem,"group");
				grouplabel = xMsg.getSpecifiedItemInfo(algorithmSectionNodes.get(0), "name");
				
				sfProperties.getPluginSettings().addJarContentDeclaration( jarfilename, namedItem, pkgname, grouplabel) ;
						                                            // "NooLabTransformAlgo.jar", "RunningMean", "org.noolab.algoplug.timeseries","timeseries") ;
			}
				 
			 
			pluginsAvailable = true;
		} // all "algorithm" entries in xml file
		
		// sfProperties.getPluginSettings().addJarContentDeclaration( "NooLabTransformAlgo.jar", "RunningMean", "org.noolab.algoplug.timeseries","timeseries") ;

		str="";
		
	}
	 

	private void loadAlgorithmJars() throws MalformedURLException {
		
		String className ;
		String[] classNameEntry ;
		ArrayList<String[]> classNameEntries;
		
		// reset "map", which then will be used in StackedTransformation.loadAlgoClass()
		// packagePath = "org.NooLab.somtransform.algo." ;
		
		// 
		if (jarfiles.size()==0){
			return ;
		}

		
		try {

			for (int i = 0; i < jarfiles.size(); i++) {

				jLoader.addJarSource( jarfiles.get(i) );
				
				classNameEntries = getClassJarContent( jarfiles.get(i) ) ;
				 
				for (int c=0;c<classNameEntries.size();c++){
					
					classNameEntry = classNameEntries.get(c);
					

					if ( classNameEntry[0].length()>0){
						try {
							
							Class<?> pClass = jLoader.loadClass( classNameEntry[0] ); // "org.gjt.mm.mysql.Driver");
							
							// if not successful, we would not see this:
							pluginSettings.registerPluginClass( classNameEntry[1] , pClass);
							 
							
						}catch(ClassNotFoundException e){
							// log as not loaded ...
						}
						 
					}
				} // all entries from jarfile
				 
					

			} // i-> all avail jarfiles
			
		} catch (Exception e) {
			 
			e.printStackTrace();
		}
	}


	private ArrayList<String[]>  getClassJarContent(String jfile) {
		String classname="",classpath="" ;
		String[] classNameEntry ;
		ArrayList<String[]> classNameEntries = new ArrayList<String[]>();
		
		int z=0;
		Object jarObj, declaredObj ;
		 
		try {

			JarFile jarFile = new JarFile(jfile);

			Enumeration enumj = jarFile.entries();

			while (enumj.hasMoreElements()) {

				jarObj = enumj.nextElement();
				// System.out.println("Jar Element = " + jarObj);
				
				// (cn.contains("string")) &&
				try{
					
					String cnp = jarObj.toString(); //  jarObj.getClass().getSimpleName().toLowerCase();
					boolean hb = cnp.toLowerCase().trim().endsWith(".class") ;
					if ( hb ){
						
						classname = StringsUtil.separateLast( cnp ,"/") ;
						
						if (pluginSettings.getPluginClasses().containsValue(classname)){
							continue;
						}
						
						classpath = classname ;
						classname = classname.replace(".class","");
						classpath = classpath.replace( classname, "").replace("/", ".") ;
							
						// any match from the list of declarations?
						declaredObj = pluginSettings.getAlgoDeclarations().getObject(classname);
						if (declaredObj!=null){
							// System.out.println("Found Element = " + jarObj);
							classNameEntry = new String[2];
							classNameEntry[0] = cnp.replace("/", ".").replace(".class","") ;
							classNameEntry[1] = classname;
							z++;
							classNameEntries.add(classNameEntry) ;
							// break;
						}
					} // ? a class file ?

				}catch(Exception e){
				}
				

			} // while next ...

		} catch (Exception e) {

			System.out.println("Exception = " + e);

		}

		return classNameEntries;
	}
	
	


	private void getAvailableJarFiles() {

		DirectoryContent dc = new DirectoryContent();
		try{
			jarfiles.clear();
			jarfiles.addAll( dc.getFileList( "*", "jar", pluginSettings.getBaseFilePath()) );
		}catch(Exception e){
			jarfiles = new ArrayList<String>();
		}
	}

	public boolean isPluginsAvailable() {
		return pluginsAvailable;
	}
	
	
	
	
}

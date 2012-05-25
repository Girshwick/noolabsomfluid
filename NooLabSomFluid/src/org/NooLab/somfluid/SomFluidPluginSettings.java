package org.NooLab.somfluid;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import org.NooLab.somfluid.properties.SettingsTransporter;
import org.NooLab.somtransform.TransformationEnv;
import org.NooLab.somtransform.TransformationEnvIntf;
import org.NooLab.utilities.datatypes.SerialMap;
import org.NooLab.utilities.datatypes.SerialMapItemIntf;
import org.NooLab.utilities.files.DFutils;

import com.jamesmurty.utils.XMLBuilder;



/**
 * 
 * 
 * http://java.ittoolbox.com/groups/technical-functional/java-l/sample-program-to-read-contents-of-file-inside-jar-or-zip-file-1048092
 * 
 *
 */
public class SomFluidPluginSettings implements 	Serializable,
												SomFluidPluginSettingsIntf {

	
	private static final long serialVersionUID = 1L;
	
	String baseFilePath = "" ;
	
	String catalogFileName = "catalog.dat" ;
	
	String internalJarPackagePath = "" ;
	
	SerialMap algoDeclarations = new SerialMap();

	ArrayList<String> loadedPluginClasses = new ArrayList<String>();
	
	TransformationEnv transformationOriginator = new TransformationEnv();
 
	
	
	// ========================================================================
	public SomFluidPluginSettings(){
	
		
	}
	// ========================================================================

	public String getBaseFilePath() {
		return baseFilePath;
	}

	public void setBaseFilePath(String basePath) {
		baseFilePath = basePath;
	}

	public void setBaseFilePath(String basePath, String catalogfilename) {
		String cfPath = basePath;
		
		cfPath = DFutils.createPath(cfPath, catalogfilename) ;
		
		catalogFileName = cfPath;
		
		baseFilePath = DFutils.createPath( basePath, "transforms/") ;
	}

	public String getCatalogFileName() {
		return catalogFileName;
	}

	public String getGlobalInternalJarPackagePath() {
		return internalJarPackagePath;
	}

	public void setGlobalInternalJarPackagePath( String packagePath) {
		internalJarPackagePath = packagePath;
	}

	/**
	 * use this if the plugin contains only 1 file, AND if the file is named as the contained main class!
	 */
	public void addJarContentDeclaration( String className, String packagePath) {
		 
		addJarContentDeclaration( "", className, packagePath, "") ;
	}
	
	/**
	 * 
	 * use this if the plugin contains only 2+ files, OR if the file is NOT named as the contained main class!
	 * i.o.w., this covers the more general setting... 
	 * 
	 * @param filename
	 * @param className
	 * @param packagePath
	 */
	public void addJarContentDeclaration( String filename, String className, String packagePath, String grouplabel) {	
		SerialMapItemIntf item ;
		
		try {
			
			if (filename.length()==0){
				filename = className + ".jar" ;
			}
			if (filename.toLowerCase().endsWith(".jar")==false){
				filename = filename + ".jar" ;
			}
			
			item = algoDeclarations.addNewItem( className, new String[] {packagePath,filename}) ;
			item.setGroupLabel(grouplabel);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public SerialMap getAlgoDeclarations() {
		return algoDeclarations;
	}

	public TransformationEnv getTransformationOriginator() {
		return transformationOriginator;
	}

	public void unRegisterPluginClass( String classname ){
		
	}
	public void unRegisterPluginClass( Class<?> loadedClass){
		
	}

	public boolean registerPluginClass( String classname, Class<?> loadedClass){
		boolean rB=false;
		
		try{
			
			if (loadedPluginClasses.indexOf( classname)<0){
				loadedPluginClasses.add(classname) ;
				transformationOriginator.getPluginClasses().put(classname, loadedClass) ;
			}
			
			rB=true;
		}catch(Exception e){
			rB=false;
		}
		return rB;
	}
	
	
	public ArrayList<String> getLoadedPluginClasses() {
		return loadedPluginClasses;
	}
	
	@Override
	public Map<String, Class<?>> getPluginClasses() {
		return transformationOriginator.getPluginClasses();
	}

	@Override
	public Class<?> getPluginClassByName(String classname) {
		return transformationOriginator.getPluginClassByName(classname);
	}

	@Override
	public boolean isAlgorithmPluggedin(String algoname) {
		return transformationOriginator.isAlgorithmPluggedin(algoname);
	}

	public XMLBuilder exportPropertiesAsXBuilder(SettingsTransporter settingsTransporter) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getInternalJarPackagePath() {
		return internalJarPackagePath;
	}

	public void setInternalJarPackagePath(String internalJarPackagePath) {
		this.internalJarPackagePath = internalJarPackagePath;
	}

	public void setCatalogFileName(String catalogFileName) {
		this.catalogFileName = catalogFileName;
	}

	public void setAlgoDeclarations(SerialMap algoDeclarations) {
		this.algoDeclarations = algoDeclarations;
	}

	public void setLoadedPluginClasses(ArrayList<String> loadedPluginClasses) {
		this.loadedPluginClasses = loadedPluginClasses;
	}

	public void setTransformationOriginator(TransformationEnv transformationOriginator) {
		this.transformationOriginator = transformationOriginator;
	}



	
	
	
}

package org.NooLab.somfluid;

import java.util.ArrayList;
import java.util.Map;

import org.NooLab.somtransform.TransformationEnvIntf;
import org.NooLab.utilities.datatypes.SerialMap;




public interface SomFluidPluginSettingsIntf extends TransformationEnvIntf{
	
	
	public String getBaseFilePath() ;

	public void setBaseFilePath(String basePath) ;
	
	public String getCatalogFileName();
	
	public String getGlobalInternalJarPackagePath() ;

	public void setGlobalInternalJarPackagePath(String packagePath) ;
	
	public void addJarContentDeclaration( String className, String packagePath);
	
	public SerialMap getAlgoDeclarations();

	// public ArrayList<String> getLoadedPluginClasses();
	// public Map<String, Class<?>> getPluginClassesMap();

	public void unRegisterPluginClass( String classname ) ;

	public void unRegisterPluginClass( Class<?> loadedClass) ;
	
	public boolean registerPluginClass( String classname, Class<?> loadedClass) ;
	
}

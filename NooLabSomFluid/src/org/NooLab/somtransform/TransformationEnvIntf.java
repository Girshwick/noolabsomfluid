package org.NooLab.somtransform;

import java.util.Map;




public interface TransformationEnvIntf {

	public Map<String, Class<?>> getPluginClasses() ;

	public Class<?> getPluginClassByName( String classname) ;

	public boolean isAlgorithmPluggedin(String algoname);
	
	
	
}

package org.NooLab.somtransform;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;



public class TransformationEnv implements TransformationEnvIntf, Serializable{

	private static final long serialVersionUID = 8799094912041683258L;
	
	private Map<String, Class<?>> pluginClasses = new TreeMap<String,Class<?>>(); 
	
	
	public TransformationEnv(){
		
	}


	public Map<String, Class<?>> getPluginClasses() {
		return pluginClasses;
	}
	
	public Class<?> getPluginClassByName( String classname) {
		Class<?> clz = null ;
		
		if (pluginClasses.containsKey(classname)){
			clz = pluginClasses.get(classname) ;
		}
		
		return clz;
	}


	@Override
	public boolean isAlgorithmPluggedin(String algoname) {
		 
		return pluginClasses.containsKey(algoname);
	}


	public void setPluginClasses(Map<String, Class<?>> pluginClasses) {
		this.pluginClasses = pluginClasses;
	}
	
}

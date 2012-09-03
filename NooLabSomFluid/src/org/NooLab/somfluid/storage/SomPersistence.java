package org.NooLab.somfluid.storage;



import org.NooLab.somfluid.SomFluidProperties;




public class SomPersistence {

	SomFluidProperties sfProperties;
	
	public SomPersistence(SomFluidProperties props){
		sfProperties = props ;
	}
	
	
	/**
	 * 
	 * the whole serialized content of the SOM
	 * 
	 * @return
	 */
	public String getFilenameForSomContent(){
		
		return "";
	}
	
	public String getFilenameForSomProperties(){
		
		return "";
	}
	
	public String getFilenameForSomMap(){
		
		return "";
	}

	/**
	 * the collected items
	 * 
	 * @return
	 */
	public String getFilenameForSomData(){
		
		return "";
	}

	
	public void storeObject( Object obj, String filename){
		
	}
	
	public Object loadObject( String filename){
		Object obj;
		
		
		return null;
	}
	
	
}

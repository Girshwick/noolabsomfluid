package org.NooLab.somtransform;

import java.io.Serializable;
 
import java.util.ArrayList;
import java.util.Arrays;

import org.NooLab.somfluid.SomFluidPluginSettings;
import org.NooLab.somtransform.algo.clazz.AlgoClassLoader;
import org.NooLab.somtransform.algo.intf.AlgoColumnWriterIntf;
import org.NooLab.somtransform.algo.intf.AlgoMeasurementIntf;
import org.NooLab.somtransform.algo.intf.AlgoTransformationIntf;
import org.NooLab.somtransform.algo.intf.AlgorithmIntf;
 


/**
 * 
 * a "StackedTransformation" is a container that provides 1 out of 3 possible standardized interfaces:
 * 
 * - for purely passive measurement like StatisticalDescriptionStandard (formerly: StandardStatistics), no transformation is applied
 * - a classical transformation, holding a column of in-values, 1+ buffered columns of values and precisely 1 column of out-values 
 * - for generating columns: as a simply copy of values into a new column, or writing  
 * 
 * column writers are used to create new columns on behalf of preceding algorithms that produce several columns
 * 
 * The division of labor and structure is organized in this way: 
 *                                "StackedTransformation"    Algorithm
 *  structures to hold the data           yes                   no  
 *  method to treat the data              no	                yes
 *  
 *     
 */
public class StackedTransformation  implements Serializable{             
 
	private static final long serialVersionUID = 3159521555594775507L;

	transient SomFluidPluginSettings pluginSettings ;
	transient TransformationEnvIntf transformOriginator ;
	
	/** its indeed an ID , not an index position ! */
	int serialID = -1; 
	String idString;
	
	
	/**  this package path we have to make dynamic */ 
	String packagePath = "org.NooLab.somtransform.algo." ;

	
	/** Reflection is used to create an instance  */
	String algorithmName = "";
	Object algorithm ;
	
	/** 
	 * 0=passive, 1=value transform, 2=column writer, determines the interface and thus the handling of data
	 */
	int algorithmType = -1;

	
	ArrayList<String>  inputColumnLabels = new ArrayList<String>();
	ArrayList<Integer> inputColumns = new ArrayList<Integer>();
	ArrayList<String>  inputColumnIDs = new ArrayList<String>();
	
	String   outputColumnLabel;
	String 	 outputColumnId;
	
	int stackPosForInData = -1;  // -1 = last
	ArrayList<ArrayList<?>> inData = new ArrayList<ArrayList<?>>();
	
	ArrayList<ArrayList<Double>> bufferedData;
	ArrayList<Double> outData = new ArrayList<Double>();

	int inFormat  = -1 ;
	int outFormat = -1 ;

	/** count of missing values in in-data [=0] or out-data [=1] */
	int[] mvCount= new int[2];
	
	DataDescription dataDescription = new DataDescription();

	public boolean multiVarInput = false;

	
	
	
	// ========================================================================
	public StackedTransformation( SomFluidPluginSettings pluginsettings){
		// we need access to pluginsettings 
		pluginSettings = pluginsettings ;
		transformOriginator = pluginSettings.getTransformationOriginator() ;
		
	}
	// ========================================================================



	public void createAlgoObject(int algorithmType) {
		 
		// Class<?> c = null;
		// Method[] mm;
		
		try {
			 
			 
		    algorithm = loadAlgoClass( algorithmName, algorithmType );

		    
		    
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	
	public void createOutData(ArrayList<Double> values) {
		 
		//  ArrayList<ArrayList<Double>> bufferedData;
		if (values!=null){
			outData.clear() ;
			outData.addAll( values );
		}
		
	}
	
	public void update(){
		
	}

	public void getAlgoInstance( String algoname ){
				
	}
	
	/**
	 * @return the inputColumnLabels
	 */
	public ArrayList<String>  getInputColumnLabels() {
		return inputColumnLabels;
	}


	/**
	 * @param inputColumnLabels the inputColumnLabels to set
	 */
	public void setInputColumnLabels(String[] columnLabels) {
		
		if ((columnLabels!=null) && (columnLabels.length>0)){
			inputColumnLabels = new ArrayList<String>( Arrays.asList(columnLabels));
		}
	}
	
	public void setInputColumnLabels(ArrayList<String> columnLabels) {
		
		if ((columnLabels!=null) && (columnLabels.size()>0)){
			inputColumnLabels = new ArrayList<String>();
			inputColumnLabels.addAll(columnLabels) ;
		}
		
	}
	 


	/**
	 * @return the outputColumnLabel
	 */
	public String getOutputColumnLabel() {
		return outputColumnLabel;
	}



	/**
	 * @param outputColumnLabel the outputColumnLabel to set
	 */
	public void setOutputColumnLabel(String outputColumnLabel) {
		this.outputColumnLabel = outputColumnLabel;
	}



	/**
	 * @return the stackPosForInData
	 */
	public int getStackPosForInData() {
		return stackPosForInData;
	}



	/**
	 * @param stackPosForInData the stackPosForInData to set
	 */
	public void setStackPosForInData(int stackPosForInData) {
		this.stackPosForInData = stackPosForInData;
	}




	/**
	 * @return the serialID
	 */
	public int getSerialID() {
		return serialID;
	}



	/**
	 * @return the algorithmName
	 */
	public String getAlgorithmName() {
		return algorithmName;
	}



	/**
	 * @return the algorithm
	 */
	public Object getAlgorithm() {
		return algorithm;
	}



	/**
	 * @return the algorithmType
	 */
	public int getAlgorithmType() {
		return algorithmType;
	}



	/**
	 * @return the inputColumns
	 */
	public ArrayList<Integer> getInputColumns() {
		return inputColumns;
	}



	/**
	 * @return the inData
	 */
	public ArrayList<ArrayList<?>> getInData() {
		return inData;
	}



	/**
	 * @return the bufferedData
	 */
	public ArrayList<ArrayList<Double>> getBufferedData() {
		return bufferedData;
	}



	/**
	 * @return the outData
	 */
	public ArrayList<Double> getOutData() {
		return outData;
	}



	/**
	 * @return the idString
	 */
	public String getIdString() {
		return idString;
	}



	/**
	 * @return the inFormat
	 */
	public int getInFormat() {
		return inFormat;
	}



	/**
	 * @param inFormat the inFormat to set
	 */
	public void setInFormat(int inFormat) {
		this.inFormat = inFormat;
	}



	/**
	 * @return the outFormat
	 */
	public int getOutFormat() {
		return outFormat;
	}



	/**
	 * @param outFormat the outFormat to set
	 */
	public void setOutFormat(int outFormat) {
		this.outFormat = outFormat;
	}



	/**
	 * @return the dataDescription
	 */
	public DataDescription getDataDescription() {
		return dataDescription;
	}



	public void _main() throws Exception {
	   // String s = getInstanceOf(String.class);
	}

	public <T> T getInstanceOf (Class<T> clazz) throws Exception {
	    return clazz.newInstance();
	}

	
	
	@SuppressWarnings({ "rawtypes" })
	public Object loadAlgoClass( String algoname, int algorithmType  ) 
													throws 	ClassNotFoundException,
															IllegalAccessException, 
															InstantiationException {
		
		ClassLoader parentClassLoader = null ;
		AlgoClassLoader classLoader ;
		
		Class algoObjectClass = null;
		
		AlgoColumnWriterIntf algoWriterObject = null ;
		AlgoTransformationIntf algoValueTransformObject = null ;
		AlgoMeasurementIntf algoMeasureObject=null;
		AlgorithmIntf algoGenObject=null;
		
		String classToLoad ;
		
		// is it a plugin class ?
		
		 
		if (transformOriginator.isAlgorithmPluggedin(algoname)){
			
			// classToLoad = packagePath + algoname ;
			
			// use "algoname" to look up in pluginSettings.getLoadedPluginClasses()
			algoObjectClass = transformOriginator.getPluginClassByName(algoname) ;
			classLoader = new AlgoClassLoader( algoObjectClass.getClassLoader());
			parentClassLoader =	classLoader.getParent() ; 
			
			classToLoad = algoObjectClass.getName() ;
			
			// TODO and now we have to create a further instance for the next request, otherwise
			//      the permanent settings will overlap, and no parallel execution would be possible
			
			
		}else{
			classToLoad = packagePath + algoname ;
			
			parentClassLoader = AlgoClassLoader.class.getClassLoader();

			classLoader = new AlgoClassLoader(parentClassLoader);

			algoObjectClass = classLoader.loadClass(classToLoad);
		}
		
		/*
		if (algorithmType == AlgorithmIntf._ALGOTYPE_GENERIC){
			// this is just used for reading/returning the type of the desired algorithm
			algoGenObject  = (AlgorithmIntf) algoObjectClass.newInstance();
		}
		
		if (algorithmType == AlgorithmIntf._ALGOTYPE_PASSIVE){
			algoMeasureObject = (AlgoMeasurementIntf) algoObjectClass.newInstance();
		}
		if (algorithmType == AlgorithmIntf._ALGOTYPE_VALUE){
			algoValueTransformObject = (AlgoTransformationIntf) algoObjectClass.newInstance();
		}
		if (algorithmType == AlgorithmIntf._ALGOTYPE_WRITER){
			algoWriterObject = (AlgoColumnWriterIntf) algoObjectClass.newInstance();
		}
		*/
		// create new class loader so classes can be reloaded.
		// classLoader = new AlgoClassLoader(parentClassLoader);
		// algoObjectClass = classLoader.loadClass( classToLoad );

		Object algoObject = null;
		
		if (algorithmType == AlgorithmIntf._ALGOTYPE_GENERIC){
			// this is just used for reading/returning the type of the desired algorithm
			algoGenObject  = (AlgorithmIntf) algoObjectClass.newInstance();
			algoObject = (Object)algoGenObject ;
		}
		if (algorithmType == AlgorithmIntf._ALGOTYPE_PASSIVE){
			algoMeasureObject = (AlgoMeasurementIntf) algoObjectClass.newInstance();
			algoObject = (Object)algoMeasureObject ;
		}
		if (algorithmType == AlgorithmIntf._ALGOTYPE_VALUE){
			algoValueTransformObject = (AlgoTransformationIntf) algoObjectClass.newInstance();
			algoObject = (Object)algoValueTransformObject ;
		}
		if (algorithmType == AlgorithmIntf._ALGOTYPE_WRITER){
			algoWriterObject = (AlgoColumnWriterIntf) algoObjectClass.newInstance();
			algoObject = (Object)algoWriterObject ;
		}
		
		
		return algoObject;
	}



	/**
	 * @return the outputColumnId
	 */
	public String getOutputColumnId() {
		return outputColumnId;
	}



	/**
	 * @param outputColumnId the outputColumnId to set
	 */
	public void setOutputColumnId(String outputColumnId) {
		this.outputColumnId = outputColumnId;
	}



	public String getPackagePath() {
		return packagePath;
	}



	public void setPackagePath(String packagePath) {
		this.packagePath = packagePath;
	}

 

}

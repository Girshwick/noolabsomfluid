package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;

import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.env.communication.NodeObserverIntf;




public interface MetaNodeIntf extends NodeObserverIntf {

	public final static int _NODE_SPLITMODE_MINIMAL  = 1;
	public final static int _NODE_SPLITMODE_BALANCED = 3;
	
	
	public long getSerialID();
	
	
	public int getActivation() ;
  
	public <T> T getInfoFromNode(Class<T> theClass, int infoID ) throws IllegalAccessException, 
																		InstantiationException  ;
	// such we can define the return the desired info that match the provided class info
	// String string = getInstance(String.class);
	

	
	public ProfileVectorIntf getProfileVector()  ;

	public IntensionalitySurfaceIntf getIntensionality()  ;

	public SimilarityIntf getSimilarity()  ;

	public MetaNodeConnectivityIntf getMetaNodeConnex() ;

	public ExtensionalityDynamicsIntf getExtensionality()  ;

	public ArrayList<Long> getExtensionRecordsIndexValues() ;

	public DataSourceIntf getSomData() ;

	public String getTargetVariableLabel() ;

	public void setContentSensitiveInfluence(boolean flag);

	public void adjustProfile( ArrayList<Double> datarecord,
							   int nodeIndex,
							   double learningrate, double influence, 
							   double sizeFactor, int i);

	public void insertDataAndAdjust( ArrayList<Double> dataNewRecord,
			 						 // int nodeIndex,
			 						 int recordIndexInTable,
			 						 int ithWinner,
			 						 double learningrate,
			 						 int fillingLimitForMeanStyle);

	void cleanInitializationByUsageVector( ArrayList<Double> usagevector);

	/*
	public void removeDataAndAdjust( ArrayList<Double> dataNewRecord,
			 						 int nodeIndex,
			 						 int recordIndexInTable,
			 						 double learningrate ) ;
	*/
	public void removeDataAndAdjust( int recordIndexInTable,  double learningrate ) ;

	public ArrayList<Integer> exportDataFromNode(int countOfRecords, int quality, boolean removeExports);

	public ArrayList<Integer> exportDataFromNode(double smallestPortion, double largestPortion, int quality, boolean removeExports);

	public void importDataByIndex( ArrayList<Integer> recordIndexes );

	public ArrayList<Double> getTargetVariableValues();
	
	
}

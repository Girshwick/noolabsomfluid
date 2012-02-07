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

	public long getSerialID();
	
  
	public <T> T getInfoFromNode(Class<T> theClass, int infoID ) throws IllegalAccessException, 
																		InstantiationException  ;
	// such we can define the return the desired info that match the provided class info
	// String string = getInstance(String.class);
	

	
	public ProfileVectorIntf getProfileVector()  ;

	public IntensionalitySurfaceIntf getIntensionality()  ;

	public SimilarityIntf getSimilarity()  ;

	public MetaNodeConnectivityIntf getMetaNodeConnex() ;

	public ExtensionalityDynamicsIntf getExtensionality()  ;

	public ArrayList<Long> getSdoIndexValues() ;

	public DataSourceIntf getSomData() ;

	public String getTargetVariableLabel() ;

	public void adjustProfile( ArrayList<Double> datarecord,
							   double learningrate, double influence, 
							   double sizeFactor, int i);


	public void setContentSensitiveInfluence(boolean flag);

}

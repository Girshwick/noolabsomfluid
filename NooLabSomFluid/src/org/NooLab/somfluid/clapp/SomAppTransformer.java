package org.NooLab.somfluid.clapp;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
import org.NooLab.somfluid.data.DataTable;
import org.NooLab.somfluid.data.DataTableCol;
import org.NooLab.somfluid.env.data.NormValueRangesIntf;
import org.NooLab.somsprite.AnalyticFunctionTransformationsIntf;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.somtransform.SomTransformerAbstract;
import org.NooLab.somtransform.SomTransformerIntf;
import org.NooLab.somtransform.TransformationModel;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;


/**
 * 
 * taking a table and transforming them the incoming data
 * 
 * 
 * 
 *
 */
public class SomAppTransformer 
								extends 
											SomTransformerAbstract
											
								implements 	
											Serializable
											{

	private static final long serialVersionUID = 3714702636726225524L;

	SomAppProperties soappProperties ;
	
	SomAppTransforms soappTransforms;
	 
	 
	ArrayList<String> requiredVariables = new ArrayList<String>();	
	ArrayList<String> requiredchains    = new ArrayList<String>();		
	
	
	String modelname;

	int nodeCount;
	
	 
	
	// ========================================================================
	public SomAppTransformer( SomDataObject sdo, SomAppProperties properties) throws Exception{
		super( sdo, properties);              // = SomFluidAppGeneralPropertiesIntf
		
		
		soappProperties = properties;
		soappTransforms = new SomAppTransforms( this ) ; // 
		
		if (sdo==null){
			somData = new SomDataObject(soappProperties,properties.getSfFactory().getSfProperties()); // we will fill the data later
		}else{
			somData = sdo; 
		}
		
		// this structure we will have to reconstruct from the file
		transformationModel = new TransformationModel( (SomTransformerIntf)this, somData);
		
		
	}
	// ========================================================================	





	public void createNodes() {
		soappTransforms.nodes = new ArrayList<SomAppAlgorithm>();
		
		for(int i=0;i<nodeCount;i++){
			
			SomAppAlgorithm sta = new SomAppAlgorithm(i);
			soappTransforms.nodes.add(sta);
		}
		
	}

	@Override
	public SomTransformer getSelfReference() {
		
		return null;
	}

	@Override
	public int getDerivationLevel() {
		return 0;
	}

	public SomAppProperties getSoappProperties() {
		return soappProperties;
	}

	public void setSoappProperties(SomAppProperties soappProperties) {
		this.soappProperties = soappProperties;
	}

	public SomAppTransforms getSoappTransforms() {
		return soappTransforms;
	}

	public void setSoappTransforms(SomAppTransforms soappTransforms) {
		this.soappTransforms = soappTransforms;
	}

	public ArrayList<String> getRequiredVariables() {
		return requiredVariables;
	}

	public void setRequiredVariables(ArrayList<String> requiredVariables) {
		this.requiredVariables = requiredVariables;
	}

	public String getModelname() {
		return modelname;
	}

	public void setModelname(String modelname) {
		this.modelname = modelname;
	}

	public int getNodeCount() {
		return nodeCount;
	}

	public void setNodeCount(int nodeCount) {
		this.nodeCount = nodeCount;
	}


	
	// ========================================================================
	 
	
	
	
	// ========================================================================
 
	
	
	
}

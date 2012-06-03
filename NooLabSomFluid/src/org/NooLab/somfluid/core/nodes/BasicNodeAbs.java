package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;

import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.imports.ExtensionalityDynamicsImportIntf;
import org.NooLab.somfluid.core.categories.imports.IntensionalitySurfaceImportIntf;
import org.NooLab.somfluid.core.categories.imports.SimilarityImportIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;


/**
 * 
 * this basic node we use also in simple clustering stuff, e.g. in somsprite,
 * or in clustering tasks around the SOM or upon the data within its nodes
 * 
 */
public abstract class BasicNodeAbs implements 
												// profiles, weights, usevectors = variable selection, optionally specific for each node
												IntensionalitySurfaceImportIntf,
												// in-process preparations of dynamic variables that are derived from 
												// the actual list represented by the node: correlations between vars, statistical properties
												ExtensionalityDynamicsImportIntf,
												// the similarity functional 
												SimilarityImportIntf  {
												// degree, style + principles, topology and range of connections

	
	DataSourceIntf somData;
	
	
	private ArrayList<String> variableLabels = new ArrayList<String>();
	// needs to get translated into index values that refer to the DataTable
	
	
	// everything is in a dedicated interface
	ProfileVectorIntf profileVector ;
	
	String targetVariableLabel="" ; 
 
	
	IntensionalitySurfaceIntf intensionality ;
	SimilarityIntf similarity ;
	MetaNodeConnectivityIntf metaNodeConnex ; 
	ExtensionalityDynamicsIntf extensionality ; 
	
	
	
	public BasicNodeAbs(){
		
	}

	/**
	 * 
	 * 
	 * @param serialID
	 */
	public void initializeStructures(long serialID) {
		 

		// we have to make the similarity 
		similarity     = importSimilarityConcepts(serialID);  // Similarity@1db6942

		intensionality = importIntensionalitySurface(serialID); // IntensionalitySurface@1042fcc

		// IntensionalitySurfaceIntf  intensy = importIntensionalitySurface();
		// String str = intensy.toString();  // IntensionalitySurface@8f3d27
		
		extensionality = importExtensionalityDynamics(serialID) ;
	
		profileVector = intensionality.getProfileVector(); 
		
		int nd=0;
		for (int i=0;i<nd;i++){ 
			
			// profileVector.getValues().add(0.5) ;
		
			
		}
		
		
	}

	public void setVariableLabels(ArrayList<String> varLabels) {
		variableLabels = varLabels;
	}

	public ArrayList<String> getVariableLabels() {
		return variableLabels;
	}
	
	
	
	
}

package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;
import java.util.Random;

import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivity;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamics;
import org.NooLab.somfluid.core.categories.extensionality.ExtensionalityDynamicsIntf;
import org.NooLab.somfluid.core.categories.imports.ExtensionalityDynamicsImportIntf;
import org.NooLab.somfluid.core.categories.imports.IntensionalitySurfaceImportIntf;
import org.NooLab.somfluid.core.categories.imports.MetaNodeConnectivityImportIntf;
import org.NooLab.somfluid.core.categories.imports.SimilarityImportIntf;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurface;
import org.NooLab.somfluid.core.categories.intensionality.IntensionalitySurfaceIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.categories.similarity.SimilarityIntf;
import org.NooLab.somfluid.core.engines.NodeStatistics;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.objects.StringedObjects;


/**
 * 
 * TODO necessary methods
 * 
 * calcPrototypeProportion(float[] prototypes)

 *
 *               
 *               	
 * 
 */
public abstract class AbstractMetaNode 
										implements  
											        // basic access to structures
													MetaNodeIntf,
													// profiles, weights, usevectors = variable selection, optionally specific for each node
													IntensionalitySurfaceImportIntf,
													// in-process preparations of dynamic variables that are derived from 
													// the actual list represented by the node: correlations between vars, statistical properties
													ExtensionalityDynamicsImportIntf,
													// the similarity functional 
													SimilarityImportIntf,
													// degree, style + principles, topology and range of connections
													MetaNodeConnectivityImportIntf
													// for messages from the network level... (assemption: everything on the same machine)
													// direct commands, data uptake, electrical waves, chemical stimuli, activity triggers, polarity
																						{

	
	LatticePropertiesIntf latticeProperties;
	VirtualLattice virtualLattice;
	
	DataSourceIntf somData;
	
	long serialID=1;
	long numID ;
	
	ArrayList<String> variableLabels = new ArrayList<String>();
	// needs to get translated into index values that refer to the DataTable
	
	// everything is in a dedicated interface
	ProfileVectorIntf profileVector ;
	
	String targetVariableLabel="" ; 
 
	
	IntensionalitySurfaceIntf intensionality ;
	SimilarityIntf similarity ;
	MetaNodeConnectivityIntf metaNodeConnex ; 
	ExtensionalityDynamicsIntf extensionality ; 
	
	Autonomy autonomy;
	
	String openLatticeFutureGuid ="";
	int openLatticeFutureTask = -1 ;
	
	StringedObjects sobj = new StringedObjects();
	PrintLog out ;
	
	
	// ========================================================================
	public AbstractMetaNode( VirtualLattice vnodes , DataSourceIntf somdata){
		
		numID = SerialGuid.numericalValue() ;
		
		somData = somdata;
		
		virtualLattice = vnodes;
		// creating a monotonic
		if (virtualLattice.size()>0){
			serialID = virtualLattice.getNode( virtualLattice.size()-1).getSerialID()+1 ;
		}
	
		
		// we have to make the similarity 
		similarity     = importSimilarityConcepts(serialID);  // Similarity@1db6942

		intensionality = importIntensionalitySurface(serialID); // IntensionalitySurface@1042fcc

		IntensionalitySurfaceIntf  intensy = importIntensionalitySurface();
		String str = intensy.toString();  // IntensionalitySurface@8f3d27
		
		extensionality = importExtensionalityDynamics(serialID) ;
		metaNodeConnex = importMetaNodeConnectivity(serialID); 
		
		/*
		metaNodeConnex = importMetaNodeConnectivity(); 
		extensionality = importExtensionalityDynamics() ;
		*/
		
		 
		// extensionality = new ExtensionalityDynamics(somData) ;
		// metaNodeConnex = new MetaNodeConnectivity() ;
		
		
		// just an abbreviation
		profileVector = intensionality.getProfileVector(); 
		
		out = virtualLattice.getOut();
		autonomy = new Autonomy(this);
	}
	 

 


	protected Object decodeMsgObject( Object stringedObj){
		
		String encObjStr = (String)stringedObj;
		Object dcobj=null;
		
		if (encObjStr.length()>5){
			 dcobj = sobj.decode(encObjStr);
		}
		
		return dcobj ;
	}

	public LatticePropertiesIntf getLatticeProperties() {
		return latticeProperties;
	}



	public void setLatticeProperties(LatticePropertiesIntf latticeProperties) {
		this.latticeProperties = latticeProperties;
	}



	public long getSerialID() {
		return serialID;
	}



	public long getNumID() {
		return numID;
	}
 
	// ------------------------------------------------------------------------
	
	/** should be called in the name space of the node thread, so we need a private message queue here, too  */
	public void initializeSOMnode() {
		
		NodeStatistics nodeStats ;
		
		int _vectorsize = variableLabels.size() ;
		Random _rnd = virtualLattice.getRndInstance() ;
		int k;
		
		/*
		 * 	ArrayList<Variable> variables = new ArrayList<Variable>(); 
			ArrayList<Double>   values  
		 */
		profileVector.setVariablesStr(  new ArrayList<String>(variableLabels) );
		int n = profileVector.getVariablesStr().size() ;
		
		nodeStats = extensionality.getStatistics() ;
		
		nodeStats.setFieldValues( new ArrayList<BasicStatisticalDescription>() );
		
		
		// this.intensionality.
		ArrayList<Variable> vars = intensionality.getProfileVector().getVariables() ;
		
		for (int i=0;i<n;i++){
		
		//  nextGaussian: centered at 0.0 with a standard deviation of 1.0, so we transform it a bit
			// that does not make much sense...
			
			double vv = org.math.array.StatisticSample.randomNormal(1, 0.5, 0.32)[0];
			
			vv = Math.round(vv*1000000.0)/1000000.0;
			vv = Math.min(1.0, vv);
			vv = Math.max(0.0, vv);
			
			// if it is not TV, and not Index
			boolean isFreeDataValue = true;
			
			if (isFreeDataValue){
				profileVector.getValues().add( vv ) ;
			}else{

			}
			
			nodeStats.getFieldValues().add( new BasicStatisticalDescription() ) ;
			nodeStats.setVariables(vars) ;
		}
											out.print(3, "node <" + serialID + "> initialized.");
		profileVector.setLastExtDataValueIndex( profileVector.getVariablesStr().size()-1 );		
		
		int np = profileVector.getValues().size() ;
		intensionality.prepareWeightVector();
	}
 
	
	
	public void takeLatticeFuture(String guid, int taskId) {
		 
		openLatticeFutureGuid = guid;
		openLatticeFutureTask = taskId;
	}
	
	
	// ========================================================================
	class Autonomy implements Runnable{

		
		AbstractMetaNode parent;
		boolean isRunning = false;
		Thread mNodeAutoThrd;
		
		
		public Autonomy ( AbstractMetaNode parent){
			
			this.parent = parent;
			
			mNodeAutoThrd = new Thread(this,"mNodeAutoThrd-"+parent.numID) ;
			// mNodeAutoThrd.start();
		}
		@Override
		public void run() {
			 
			isRunning = true;
			
			try{
				
				while (isRunning){
					
					
					parent.out.delay(20) ;
				} // ->
				
			}catch(Exception e){
				
			}
			
		}
		
		
	}


	public DataSourceIntf getSomData() {
		return somData;
	}


	public ProfileVectorIntf getProfileVector() {
		return profileVector;
	}


	public String getTargetVariableLabel() {
		return targetVariableLabel;
	}


 


	public IntensionalitySurfaceIntf getIntensionality() {
		return intensionality;
	}


	public SimilarityIntf getSimilarity() {
		
		return similarity;
	}


	public MetaNodeConnectivityIntf getMetaNodeConnex() {
		return metaNodeConnex;
	}


	public ExtensionalityDynamicsIntf getExtensionality() {
		return extensionality;
	}
	

}




















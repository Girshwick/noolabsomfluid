package org.NooLab.somfluid.core.nodes;

import java.util.ArrayList;
import java.util.Random;

import org.NooLab.field.FieldIntf;
import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivity;
import org.NooLab.somfluid.core.categories.connex.MetaNodeConnectivityIntf;
import org.NooLab.somfluid.core.categories.extensionality.BasicSimpleStatisticalDescription;
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
import org.NooLab.somfluid.core.engines.NodeStatisticsFactory;
import org.NooLab.somfluid.core.engines.NodeStatisticsIntf;

import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.util.BasicStatisticalDescription;
import org.NooLab.somfluid.util.BasicStatisticalDescriptionIntf;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;
import org.NooLab.utilities.objects.StringedObjects;
import org.math.array.StatisticSample;

 

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
public abstract class AbstractMetaNode  extends 
                                                    BasicNodeAbs
										implements  
											        // basic access to structures
													MetaNodeIntf,
													
													MetaNodeConnectivityImportIntf
													// for messages from the network level... (assemption: everything on the same machine)
													// direct commands, data uptake, electrical waves, chemical stimuli, activity triggers, polarity
																						{

	
	LatticePropertiesIntf latticeProperties;
	VirtualLattice virtualLattice;
	
	 
	
	long serialID=1;
	long numID ;
	
	
	

	
	Autonomy autonomy;
	
	int activationState = 0;
	
	String openLatticeFutureGuid ="";
	int openLatticeFutureTask = -1 ;
	
	int somType = 0;
	
	StatisticSample sampler;
	Random nRandom ;
	
	PrintLog out ;
	private BasicStatisticalDescription bsd;
	
	
	// ========================================================================
	public AbstractMetaNode( VirtualLattice vnodes , DataSourceIntf somdata){
		super();          // VirtualLattice
		
		virtualLattice = vnodes;
		
		
		somType = virtualLattice.getLatticeProperties().getSomType() ;
		
		numID = SerialGuid.numericalValue() ;
		
		somData = somdata;
		
		
		// creating a monotonic
		if (virtualLattice.size()>0){
			serialID = virtualLattice.getNode( virtualLattice.size()-1).getSerialID()+1 ;
		}
	
		initializeStructures(serialID);
		
		initRandom();
		
		metaNodeConnex = importMetaNodeConnectivity(serialID); 
		
		/*
		metaNodeConnex = importMetaNodeConnectivity(); 
		extensionality = importExtensionalityDynamics() ;
		*/
		
		 
		// extensionality = new ExtensionalityDynamics(somData) ;
		// metaNodeConnex = new MetaNodeConnectivity() ;
		
		
		// just an abbreviation
		
		
		out = virtualLattice.getOut();
		autonomy = new Autonomy(this);
	}
	 
 
	public AbstractMetaNode(LatticeIntf nodeCollection, DataSourceIntf somdata ) {
		super();  

		
		numID = SerialGuid.numericalValue() ;
		
		somData = somdata;
		
		virtualLattice = (VirtualLattice) nodeCollection;

		somType = virtualLattice.getLatticeProperties().getSomType() ; 
		
		initRandom();
		
		initializeStructures(serialID);
			 	
		out = virtualLattice.getOut();
	}



	private void initRandom() {
		//
		
		if (virtualLattice.getRndInstance()!=null){ // && stability requirement Option  
			nRandom = virtualLattice.getRndInstance() ;
		}else{
			nRandom = new Random();
			nRandom.setSeed( serialID );
		}
		
		if (virtualLattice.getRndSamplerInstance()!=null){
			sampler = virtualLattice.getRndSamplerInstance();
		}else{
			sampler = new StatisticSample(4359) ;
		}
		
		
	}


	protected Object decodeMsgObject( Object stringedObj){
		StringedObjects sobj = new StringedObjects();
		
		String encObjStr = (String)stringedObj;
		Object dcobj=null;
		
		if (encObjStr.length()>5){
			 dcobj = sobj.decode(encObjStr);
		}
		
		sobj = null;
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
	

	
	
	public void setSerialID(long serialID) {
		this.serialID = serialID;
	}





	public void setNumID(long numID) {
		this.numID = numID;
	}





	/**
	 * if based on listener mechanism: then it should be called in the name space of the node thread, so we need a private message queue here, too  
	 * 
	 * @param asymmetryMode -1,0 = none, 1=values in direction a*b, 2 = values in direction a*b, and variances in direction b*c
	 */
	public void initializeSOMnode( int asymmetryMode ) {
		
		NodeStatisticsIntf nodeStats ;
		
		// int _vectorsize = variableLabels.size() ;
		
		int vn = this.virtualLattice.getSimilarityConcepts().getUsageIndicationVector().size();
		
		/*
		 * 	ArrayList<Variable> variables = new ArrayList<Variable>(); 
			ArrayList<Double>   values  
			we create a new one if we need the possibility for different vectors in the map 
		 */
		
		boolean sameFeatures = virtualLattice.getLatticeProperties().isAssignatesHomogeneous();
		
		if (sameFeatures ==false){
			profileVector.setVariablesStr(  new ArrayList<String>(getVariableLabels()) );
		}else{
			profileVector.setVariablesStr( getVariableLabels() );
		}
		
		
		
		int n = profileVector.getVariablesStr().size() ;
		
		nodeStats = extensionality.getStatistics() ;
		
		// the interface to the basic statistical description is either "basic normal" (incl histograms etc) or "basic simple" (without histograms etc) 
		nodeStats.setFieldValues( (ArrayList<BasicStatisticalDescriptionIntf>) NodeStatisticsFactory.getBasicStatisticalDescription(somType)  );
		/*
		if (this.somType == FieldIntf._SOMTYPE_MONO){
			nodeStats.set FieldValues( (ArrayList<BasicStatisticalDescriptionIntf>) NodeStatisticsFactory.getBasicStatisticalDescription(somType)  );
		}
		if (this.somType == FieldIntf._SOMTYPE_PROB){
			nodeStats.set FieldValues( (ArrayList<BasicStatisticalDescriptionIntf>) NodeStatisticsFactory.getBasicStatisticalDescription(somType)  );
		}
		*/
		
		 
		
		// this.intensionality.
		ArrayList<Variable> vars = intensionality.getProfileVector().getVariables() ;
		
		for (int i=0;i<n;i++){
		
		//  nextGaussian: centered at 0.0 with a standard deviation of 1.0, so we transform it a bit
			// that does not make much sense...
			
			// there is a tendency that a smaller std dev here leads to smaller TP-zero values...
			// would have expected sth different
			
			
			double sd = virtualLattice.getStDevForNodeInitialization()  * virtualLattice.getInitialRandomDivergence(); 
			//            default = 0.26;                                     default = 1.0
			
			// org.math.array.StatisticSample.
			
			double vv = sampler.randomnormal(1, 0.5, sd)[0];
			// comes from JMathTools:  http://jmathtools.berlios.de/doku.php
			
			vv = Math.round(vv*1000000.0)/1000000.0;
			double v0 = vv;
			
			vv = Math.min(0.98, vv);
			vv = Math.max(0.02, vv);
if (v0!=vv){
	int kk;
	kk=0;
}
			
			// if it is not TV, and not Index
			boolean isFreeDataValue = true;
			
			if (isFreeDataValue){
				if (i>=profileVector.getValues().size()){
					profileVector.getValues().add( vv ) ;
				}else{
					profileVector.getValues().set( i,vv ) ;
				}
			}else{

			}


			if (this.somType == FieldIntf._SOMTYPE_MONO){
			
				bsd = new BasicStatisticalDescription(false);
				ArrayList<BasicStatisticalDescription> fvalues = (ArrayList<BasicStatisticalDescription>) nodeStats.getFieldValues();
				fvalues.add( bsd ) ;
				// ( (ArrayList<BasicStatisticalDescriptionIntf>) NodeStatisticsFactory.getBasicStatisticalDescription(somType)  );
			}
			if (this.somType == FieldIntf._SOMTYPE_PROB){
				BasicSimpleStatisticalDescription bssd = new BasicSimpleStatisticalDescription();
				ArrayList<BasicSimpleStatisticalDescription> fvalues = (ArrayList<BasicSimpleStatisticalDescription>) nodeStats.getFieldValues();
				fvalues.add( bssd ) ;

			}
			
			
			nodeStats.setVariables(vars) ;
		} // i-> all vector positions
											out.print(4, "node <" + serialID + "> initialized.");
		profileVector.setLastExtDataValueIndex( profileVector.getVariablesStr().size()-1 );		
		
		int np = profileVector.getValues().size() ;
		if (np==0){
			ArrayList<Double> values ;
			// profileVector.setValues( values );
		}
		intensionality.prepareWeightVector();
	}
 
	
	
	public void takeLatticeFuture(String guid, int taskId) {
		 
		openLatticeFutureGuid = guid;
		openLatticeFutureTask = taskId;
	}
	
	public void stopAutonomyProcess(){
		autonomy.isRunning=false;
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

	public VirtualLattice getVirtualLattice() {
		return virtualLattice;
	}


	/** ony nodes with an activationState>=0 will be considered in any learning */
	public void setActivation(int stateflag) {
		 
		activationState = stateflag;
	}
	
	public int getActivation() {
		return activationState ;
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




















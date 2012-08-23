package org.NooLab.somfluid.core.engines.det;

import java.util.ArrayList;

import org.NooLab.field.FieldIntf;
import org.NooLab.field.FieldParticleIntf;
import org.NooLab.field.fixed.components.FixedFieldParticlesIntf;
import org.NooLab.field.interfaces.PhysicalGridFieldIntf;
import org.NooLab.field.repulsive.intf.particles.RepFieldParticlesIntf;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.DataSourceIntf;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.SomDataObjectIntf;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.categories.intensionality.ProfileVectorIntf;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.somfluid.data.Variable;
import org.NooLab.somfluid.env.communication.NodesInformer;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;



public class LatticePreparation {

	SomProcessIntf somProcess ;
	SomHostIntf somHost;
	SomFluidProperties sfProperties ;
	
	private LatticePropertiesIntf latticeProperties;
	long numericID = 0L;
	private SomFluidTask sfTask;
	
	
	private PrintLog out;
	private SomDataObject somDataObject;
	private NodesInformer nodesInformer;
	
	// ========================================================================
	public LatticePreparation( SomProcessIntf somProcess, SomHostIntf somHost, SomFluidProperties sfProperties){
		
		this.somHost = somHost;
		this.sfProperties = sfProperties ;
		this.somProcess = somProcess;
		
		somDataObject = somHost.getSomDataObj() ;
		
		out = somHost.getSomFluid().getOut() ;
	}
	// ========================================================================


	public VirtualLattice getLattice() {
		
		PhysicalGridFieldIntf particleField;
		VirtualLattice somLattice ;
		
		int initialNodeCount ;
		boolean displayNodeCount ;
		
		
		
		
		initialNodeCount = sfProperties.getInitialNodeCount();
		
		particleField = somHost.getSomFluid().getParticleField( ) ;
		
		latticeProperties = (LatticePropertiesIntf)sfProperties;
		
		somLattice = new VirtualLattice( somProcess,latticeProperties,(int) (100+numericID));
 		  
		somLattice.setFieldIsDynamic( sfProperties.getSomGridType() == FieldIntf._SOM_GRIDTYPE_FLUID ) ;
		

		sfTask = somHost.getSfTask() ;
		displayNodeCount = sfTask.getCounter()<=0;
		
		createVirtualLattice( somLattice, particleField, initialNodeCount , displayNodeCount);
		
		return somLattice;
	}
	
	public void setNodesInformer( NodesInformer ni){
		nodesInformer = ni;
	}
	
	/**  
	 * 
	 * connecting the SOM nodes to the particles collection
	 * 
	 * Note that the particles in the field are just containers! They are not identical to nodes.
	 * Hence, our nodes need to be attached to the particles  
	 */
	@SuppressWarnings("unchecked")
	private void createVirtualLattice( VirtualLattice somLattice, PhysicalGridFieldIntf particleField, int initialNodeCount, boolean showNodeCount) {
		
		MetaNode mnode;
		long idbase;
		FieldParticleIntf particle=null;
		
		somDataObject = somHost.getSomDataObj() ;
		 									if ((showNodeCount) || (somHost.getSfTask().getCounter()<=3)){ 
		 										out.print(2, "creating the logical som lattice for "+initialNodeCount+" nodes...");
		 									}
		for (int i=0;i<initialNodeCount;i++){
			
			mnode = new MetaNode( somLattice, (DataSourceIntf) somDataObject  ); 
			somLattice.addNode(mnode) ;
						
											if(initialNodeCount>20000){
												out.printprc(2, i, initialNodeCount, (int) (((double)initialNodeCount)/(5.0)), "");
											}
											out.print(4,"Node <"+i+">, serial = "+mnode.getSerialID());
			try{
				// requires NodesInformer
				nodesInformer.registerNodeinNodesInformer( mnode );
				 
				int somtype = sfProperties.getSomGridType();
				// the same as somLattice.getLatticeProperties().getSomType()
				
				ArrayList<Variable> v=null;
				
				if ( somtype == FieldIntf._SOMTYPE_MONO){
					v = somDataObject.getVariableItems() ;	
				}else{
					v = somDataObject.getVariableItemsReference();
				}
				v = somDataObject.getVariableItemsReference();
				
				// ArrayList<Variable> vari = somDataObject.getVariableItems();
				ProfileVectorIntf pv = mnode.getIntensionality().getProfileVector();
				// pv.setVariables( vari ) ;
				pv.setVariables( v ) ;
				
				mnode.getExtensionality().getStatistics().setVariables(v);
				
				ArrayList<Double> values = ArrUtilities.createCollection( v.size(), 0.5) ;
				pv.setValues(values);
				
				
				Object obj = particleField.getParticles();
				 
				if (somtype == FieldIntf._SOM_GRIDTYPE_FLUID){
					particle = ((RepFieldParticlesIntf)obj).get(i);
				}
				if (somtype == FieldIntf._SOM_GRIDTYPE_FIXED){
					particle = (( FixedFieldParticlesIntf)obj).get(i);
				}
				
				if (particle!=null){
					particle.setIndexOfDataObject( mnode.getSerialID() );
				}
				
			}catch(Exception e){
				e.printStackTrace() ;
			}
			 
		}
		 
		double d = particleField.getAverageDistanceBetweenParticles();
		somLattice.setAveragePhysicalDistance(d);
		
											out.print(3, "logical som lattice created.");
		// 
	}

	
}

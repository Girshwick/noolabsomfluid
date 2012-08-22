package org.NooLab.somfluid.astor;

import java.util.ArrayList;
import java.util.Collections;

import org.NooLab.somfluid.SomFluid;
import org.NooLab.somfluid.SomFluidFactory;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.somfluid.SomFluidTask;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.components.VirtualLattice;
import org.NooLab.somfluid.core.SomProcessIntf;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.LatticePreparation;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.somfluid.core.engines.det.SomTargetedModeling;
import org.NooLab.somfluid.core.engines.det.results.ModelProperties;
import org.NooLab.somfluid.core.nodes.LatticeProperties;
import org.NooLab.somfluid.core.nodes.LatticePropertiesIntf;
import org.NooLab.somfluid.data.Variables;
import org.NooLab.somsprite.ProcessCompletionMsgIntf;
import org.NooLab.somtransform.SomFluidAppGeneralPropertiesIntf;
import org.NooLab.somtransform.SomTransformer;
import org.NooLab.utilities.logging.PrintLog;
import org.NooLab.utilities.logging.SerialGuid;



/**
 * 
 * This takes the same role as the ModelOptimizer or the SimpleSingleModel
 * 
 * 
 * the idea is to keep most of the infrastructure (DSom+ ... the Lattice, nodes), just the loops provided by
 * ModelOptimizer are abandoned, do not take place.
 * 
 * However, we use a different implementation for the BasicStatistics: without histograms in the node
 * Due to memory issues, we outsource the histograms into a different class, where we can deal 
 * more MEM-efficiently with it,... although we need additional effort to keep it up to date
 *  
 * 
 * Another issue that is important right from the beginning is persistence;
 * 
 * Data handling is very different to ModelOptimizer
 * 
 * If the count of nodes is beyond a certain threshold, say 150 or so, the
 * search for the best matching unit BMU will be organized completely different
 * In this case, 5..8..10 sustained processes are created, which run a queue.
 * The nodes are assigned in a fixed manner ( needs update in case of growth)
 * A particular record then is provided to each of these processes, and each of them
 * creates its own list of BMU, which then are matched  
 *  
 * A second issue is the missing target variable.
 * The validation is not based on TV. 
 *  
 * 
 * modeling needs internal statistics, such that variance is controlled 
 * additionally, we use variance of variances as a target target variable for intermittent optimization
 * 
 * the focus here is on incremental learning, that is, a proper update service
 * primary data source is a database
 * 
 * 
 * 
 * 
 * also, a second version will be made available for further processing, which is a "crystallization"
 * based upon adhoc multi-criteria optimization:
 *    given an initial SOM, a threshold is chosen dynamically, such that 
 *    - the distance between clusters is in a certain range
 *    - clusters comprise at least 5(+) nodes
 *    - the contrast (variance of diff) between clusters and valleys is larger than those between nodes 
 * 
 * This class is just an app-like wrapper, organizing references, processes and classes
 * 
 * 
 *  implements 
 */
public class SomAssociativeStorage implements SomHostIntf, ProcessCompletionMsgIntf{

	SomFluid somFluid ;
	SomFluidTask sfTask;
	SomFluidFactory sfFactory;
	
	// using observer in its own thread, is part of SomDataObj
	SomDataStreamer somDataStreamer ; 
	SomDataObject somDataObj;
		
	
	SomProcessIntf somProcess;
	
	SomTransformer somTransformer;
	
	SomFluidProperties sfProperties ;
	
	DSom dSom ;
	
	VirtualLattice astorSomLattice;
	LatticePropertiesIntf latticeProps; 
	
	
	PrintLog out = new PrintLog(2,true);
	private LatticePropertiesIntf latticeProperties;
	private SomAstor somAstor;
	
	private ArrayList<Integer> usedVariables = new ArrayList<Integer>();
	
	// ========================================================================
	public SomAssociativeStorage( SomFluid somfluid, SomFluidFactory factory,
								  SomFluidProperties properties) {
		
		somFluid = somfluid ;
		sfFactory = factory ;
		sfProperties = properties;
		
		latticeProps  = new LatticeProperties();
		
	
		
	}
	// ========================================================================
	
	// care for data connection, incoming data link : we are learning incrementally !!

	
	
	public void perform() throws Exception{
		
		
		long serialID=0;
		serialID = SerialGuid.numericalValue();
		
		SomTargetedModeling stm;
		
		sfTask.setCallerStatus(0) ;
		 
		
		try{
		

			somAstor = new SomAstor( this, sfFactory, sfProperties, sfTask, serialID);
			
			
			somAstor.prepare(usedVariables);
			
			String guid = somAstor.start();
			
			out.print(2, "\nSom-Astor  is running , identifier: "+guid) ; 

			
			while (somAstor.isRunning()==true){
				out.delay(10);
			}

			// ensure persistence
			
			
			// clear structures, stop threads
			somAstor.clear() ;
			
			
		}catch(Exception e){
			// restart option ... ?
			if (out.getPrintlevel()>=1){
				e.printStackTrace();
			}
		}
		
		somAstor = null;
	}
	

	public void setInitialVariableSelection(ArrayList<String>  vs){
		
		//ArrayList<Double> initialUsageIndicator = new ArrayList<Double>();
		
		if (somDataObj==null){
			return;
		}
		Variables variables = somDataObj.getVariables() ;
		String label;
		
		
		for (int i=0;i<vs.size();i++){
			label = vs.get(i);
			int ix = variables.getIndexByLabel(label);
			usedVariables.add(ix) ;
		}
		Collections.sort(usedVariables); 
		usedVariables.trimToSize();
		
	}

	
	@Override
	public void processCompleted(Object processObj, Object msg) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTargetedModelingCompleted(ModelProperties results) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public SomFluidTask getSfTask() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SomFluidFactory getSfFactory() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SomFluid getSomFluid() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SomFluidProperties getSfProperties() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ModelProperties getSomResults() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SomDataObject getSomDataObj() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public SomProcessIntf getSomProcess() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ModelProperties getResults() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getOutResultsAsXml(boolean asHtmlTable) {
		// TODO Auto-generated method stub
		return null;
	}

	 
	
	 
	
}

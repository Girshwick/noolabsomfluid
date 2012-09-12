package org.NooLab.somfluid.clapp;

import java.io.Serializable;
import java.util.ArrayList;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.nodes.MetaNode;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.files.DFutils;
import org.NooLab.utilities.logging.PrintLog;




public class SomAppResultAnalysis 	implements 	Serializable,
												SomAppResultAnalysisIntf{

	private static final long serialVersionUID = -8226110251164721364L;

	transient SomAppSomObject  somObject ;
	transient SomAppProperties soappProperties ; 
	transient SomDataObject    somData;
	
	IndexDistance bmu ;
	IndexedDistances similarNodes;
	int bmuIndex = -1;
	ArrayList<Double> bmuProfile = new ArrayList<Double>();
	ArrayList<Double> datavalues = new ArrayList<Double>();
	
	int state = _RESULTS_INIT;

	private double reliability;
	
	
	transient PrintLog out;
	// ========================================================================
	public SomAppResultAnalysis( SomDataObject somdata ,SomAppProperties soappProp, SomAppSomObject somObj, IndexedDistances bmu) {
		similarNodes = bmu;
		somObject = somObj;
		soappProperties = soappProp;
		somData = somdata;

		out = somObject.out;
		out.setShowTimeStamp(false);
	}
	// ========================================================================
	
	
	public SomAppResultAnalysis prepare(int workerindex, ArrayList<Double> cvalues) {
		IndexDistance sbmu ;
		int nodeIndex, nodesize,sbmuIndex;
		double nodePpv, toleratedEcr;
 
		
		datavalues.addAll( cvalues ) ;
		
		if (similarNodes.size()==0){
			state = _RESULTS_NORESULTS ;
			return this;
		}
		
		bmu = new  IndexDistance(similarNodes.getItem(0)) ;
		
		bmuIndex = bmu.getIndex();
		
		for (int i=0;i<similarNodes.size();i++){
		
			sbmu = similarNodes.getItem(i) ;
			sbmuIndex = sbmu.getIndex();
			nodeIndex = sbmu.getIndex() ;
			MetaNode node = somObject.soappNodes.getNode(nodeIndex) ;
			node.getExtensionality().getListOfRecords().add(workerindex);
			nodePpv = node.getExtensionality().getPPV();
			
			toleratedEcr = soappProperties.getRiskAttitudeByECR();
			// calculate reliability score
			reliability = 1.0 ;
			nodesize = node.getExtensionality().getSupport() ;
			
			if (i<1){
				String outstr = "";
				if (i==0){
					outstr = "record index: \t"+workerindex + "\t  bmu: \t"+i +"\t node index: \t"+sbmuIndex+"   \tnodePpv = \t"+String.format("%.2f", nodePpv)+" \t"+cvalues.get(4) ;
				}else{
					outstr = ".             \t"+               "\t   bmu: \t"+i+"\t node index: \t"+sbmuIndex+"   \tnodePpv = \t"+String.format("%.2f", nodePpv)+"" ;
				}
				
				out.print(2, outstr);
				out.collectPrintOut( outstr );
			}
		} // -> all bmu
		
		
		
		return this;
	}


	public IndexDistance getBmu() {
		return bmu;
	}


	public void setBmu(IndexDistance bmu) {
		this.bmu = bmu;
	}


	public IndexedDistances getSimilarNodes() {
		return similarNodes;
	}


	public void setSimilarNodes(IndexedDistances similarNodes) {
		this.similarNodes = similarNodes;
	}


	public int getBmuIndex() {
		return bmuIndex;
	}


	public void setBmuIndex(int bmuIndex) {
		this.bmuIndex = bmuIndex;
	}


	public ArrayList<Double> getBmuProfile() {
		return bmuProfile;
	}


	public void setBmuProfile(ArrayList<Double> bmuProfile) {
		this.bmuProfile = bmuProfile;
	}


	public ArrayList<Double> getDatavalues() {
		return datavalues;
	}


	public void setDatavalues(ArrayList<Double> datavalues) {
		this.datavalues = datavalues;
	}


	public int getState() {
		return state;
	}


	public void setState(int state) {
		this.state = state;
	}


	public double getReliability() {
		return reliability;
	}


	public void setReliability(double reliability) {
		this.reliability = reliability;
	}

}

package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.NooLab.somfluid.OutputSettings;
import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.strings.ArrUtilities;




public class MetricsHistory implements Serializable{
 
	private static final long serialVersionUID = 4890338091318697906L;

	transient SomHostIntf somHost;
	transient EvoMetrices evoMetrices;
	
	transient SomDataObject somData;
	OutputSettings outsettings ; 
	
	ArrayList<HistoryItem> items = new ArrayList<HistoryItem>(); 
	
	int tvVariableIndex = -1 ;
	double ecr ;
	int cases=0, observationCount=0;
	
	/** label of all variables  */
	ArrayList<String> varLabels;


	
 	
	// ========================================================================
	public MetricsHistory( SomHostIntf somhost, EvoMetrices ems) {
		evoMetrices = ems;
		somHost = somhost;
		
		outsettings = somHost.getSfProperties().getOutputSettings();
		
		somData = somHost.getSomDataObj();
		varLabels = somData.getVariablesLabels() ;
		tvVariableIndex = somData.getVariables().getTvColumnIndex() ;
		
		outsettings.initializeCatalog();
	}

	public MetricsHistory(){
		
	}
	// ========================================================================

	public void addEvoMetrikAsItem( EvoMetrik em ){
		HistoryItem hitem = new HistoryItem();
		
		try{
			

			hitem.index  = items.size() ;
			hitem.step   = em.step ;
			hitem.loopix = em.loopCount;
			hitem.score  = em.actualScore ;
			hitem.falseNegatives = em.sqData.fn ;
			hitem.falsePositives = em.sqData.fp;
			hitem.trueNegatives  = em.sqData.tn ;
			hitem.truePositives  = em.sqData.tp;

			if (items.size()==0) {
				ecr = em.sqData.ecr;
				cases = em.sqData.ccases ;
				observationCount = em.sqData.samplesize ;
			}
			hitem.rocAuC =  em.sqData.rocAuC ;
			
			hitem.fnRate = (double)(hitem.falseNegatives)/(double)(cases);
			
			if (hitem.truePositives + hitem.falsePositives>0){
				hitem.fpRate = (double)(hitem.falsePositives)/(double)(hitem.truePositives + hitem.falsePositives) ;
			}
			
			hitem.tpRate = (double)(hitem.truePositives)/(double)cases ;
			hitem.tnRate = (double)(hitem.trueNegatives)/(double)(observationCount-cases);

			if ((hitem.truePositives+hitem.falsePositives)>0){
				hitem.ppv = (double)(hitem.truePositives)/(double)(hitem.truePositives+hitem.falsePositives) ;
			}
			
			hitem.sensitivity = hitem.tpRate ;
			hitem.specificity = hitem.tnRate ;
				
			if ((hitem.truePositives+hitem.falsePositives)>0){
				// implied risk ???
				hitem.risk = (double)(hitem.truePositives)/(double)(hitem.truePositives + hitem.falsePositives) ;
			}
			
			hitem.variableIndexes = (int[]) ArrUtilities.changeArraystyle( em.varIndexes );
			items.add(hitem) ;
			
		}catch(Exception e){
			
		}
		
		
	}
	
	
	
	public ArrayList<HistoryItem> getItems() {
		return items;
	}

	public EvoMetrices getEvoMetrices() {
		return evoMetrices;
	}

	public void setEvoMetrices(EvoMetrices evoMetrices) {
		this.evoMetrices = evoMetrices;
	}

	public int getTvVariableIndex() {
		return tvVariableIndex;
	}

	public void setTvVariableIndex(int tvVariableIndex) {
		this.tvVariableIndex = tvVariableIndex;
	}

	public void setItems(ArrayList<HistoryItem> items) {
		this.items = items;
	}
	
	// ........................................................................
	 

	public double getEcr() {
		return ecr;
	}

	public void setEcr(double ecr) {
		this.ecr = ecr;
	}

	public int getObservationCount() {
		return observationCount;
	}

	public void setObservationCount(int observationCount) {
		this.observationCount = observationCount;
	}

	public ArrayList<String> getVarLabels() {
		return varLabels;
	}

	public void setVarLabels(ArrayList<String> varLabels) {
		this.varLabels = varLabels;
	}



	public void setOutputColumns(Map<String, Integer> outdef) {
		// TODO Auto-generated method stub
		
	}



	public ArrayList<String> createHeaderRow() {
		ArrayList<String> strlist = new ArrayList<String> ();
		int sxi;
		IndexedDistances cfs = new IndexedDistances();
		
		// we need to store it into a temp list first, since the order cold be changed
		for (int i=0;i<outsettings.getCatalogFields().size();i++){
			sxi = outsettings.getCatalogFields().getItem(i).getSecindex();
			if (sxi>0){
				cfs.add( outsettings.getCatalogFields().getItem(i) ) ;
			}
		}
		
		// sorting
		cfs.sort(1) ;
		
		// creating
		for (int i=0;i<cfs.size();i++){
			sxi = cfs.getItem(i).getSecindex();
			 
			strlist.add( cfs.getItem(i).getGuidStr() ) ;
		}
		return strlist;
	}

	
	public ArrayList<ArrayList<String>> createTableRows( MetricsHistory emHistory, 
														 ArrayList<String> headers) {
		ArrayList<ArrayList<String>> tableRows = new ArrayList<ArrayList<String>> ();
		ArrayList<String> singleRow;
		HistoryItem mh ;
		int sxi;
		String str ;
		IndexedDistances cfs = new IndexedDistances();
		
		// we need to store it into a temp list first, since the order cold be changed
		for (int i=0;i<outsettings.getCatalogFields().size();i++){
			sxi = outsettings.getCatalogFields().getItem(i).getSecindex();
			if (sxi>0){
				cfs.add( outsettings.getCatalogFields().getItem(i) ) ;
			}
		}
		// sorting
		 
		cfs.sort(1) ; 
		
		
		// translating into string table = single string
 		int nm = emHistory.items.size() ;
		
		for (int m=0;m<nm;m++){
			mh = emHistory.items.get(m);
			singleRow = new ArrayList<String>();
			
			// creating
			for (int i=0;i<cfs.size();i++){
				sxi = cfs.getItem(i).getIndex();
				
				try{
					str = getFieldAsString(mh,sxi);
				}catch(Exception e){
					str="";
				}
				
				if ((str.length()==0) || (str.contentEquals("NaN"))){
					str="not.def.";
				}
				singleRow.add(str) ;
				 
			}
			tableRows.add(singleRow) ;
		}
		
		return tableRows;
	}

	private String getFieldAsString(  HistoryItem historyItem, int fix) {
		String fieldStr="";
		
		try{
			
			if (fix==1){ fieldStr = ""+historyItem.index ; }
			if (fix==2){ fieldStr = ""+historyItem.step ; }
			if (fix==3){ fieldStr = String.format("%.3f", historyItem.score ); }
			if (fix==4){ fieldStr = ArrUtilities.arr2Text( ArrUtilities.changeArraystyle(historyItem.variableIndexes)) ; }
			if (fix==5){ fieldStr = ""+historyItem.truePositives ; }
			if (fix==6){ fieldStr = ""+historyItem.trueNegatives ; }
			if (fix==7){ fieldStr = ""+historyItem.falsePositives ; }
			if (fix==8){ fieldStr = ""+historyItem.falseNegatives ; }
			if (fix==9){ fieldStr =  String.format("%.4f", historyItem.tpRate ); }
			if (fix==10){ fieldStr = String.format("%.4f", historyItem.tnRate ); }
			if (fix==11){ fieldStr = String.format("%.4f", historyItem.fpRate ); }
			if (fix==12){ fieldStr = String.format("%.4f", historyItem.fnRate ); }
			if (fix==13){ fieldStr = String.format("%.3f", historyItem.ppv ); }
			if (fix==14){ fieldStr = String.format("%.3f", historyItem.npv ); }
			if (fix==15){ fieldStr = String.format("%.3f", historyItem.sensitivity ); }
			if (fix==16){ fieldStr = String.format("%.3f", historyItem.specificity ); }
			if (fix==17){ fieldStr = String.format("%.4f", historyItem.rocAuC ); }
			if (fix==18){ fieldStr = String.format("%.4f", historyItem.rocSTP ); }
			if (fix==19){ fieldStr = String.format("%.3f", historyItem.risk ); }
			
		}catch(Exception e){
			
		}
		
		
		return fieldStr;
	}
	
	
}










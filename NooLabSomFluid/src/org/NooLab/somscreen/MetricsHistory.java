package org.NooLab.somscreen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.NooLab.somfluid.components.SomDataObject;
import org.NooLab.somfluid.core.engines.det.SomHostIntf;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;




public class MetricsHistory implements Serializable{
 
	private static final long serialVersionUID = 4890338091318697906L;

	transient SomHostIntf somHost;
	transient EvoMetrices evoMetrices;
	
	transient SomDataObject somData;
	
	ArrayList<HistoryItem> items = new ArrayList<HistoryItem>(); 
	
	int tvVariableIndex = -1 ;
	double ecr ;
	int cases=0, observationCount=0;
	
	/** label of all variables  */
	ArrayList<String> varLabels;

	/** for organizing the output */
	transient IndexedDistances catalogFields;
	
 	
	// ========================================================================
	public MetricsHistory( SomHostIntf somhost, EvoMetrices ems) {
		evoMetrices = ems;
		somHost = somhost;
		
		somData = somHost.getSomDataObj();
		varLabels = somData.getVariablesLabels() ;
		tvVariableIndex = somData.getVariables().getTvColumnIndex() ;
		
		initializeCatalog();
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
	 
	public void defineOutCatalogSelection( String[] outitems){
		
		
	}
	
	public void addOutCatalogSelection( String outitem){
		
		
	}

	public void initializeCatalog(){
	
		IndexDistance cf ;
		catalogFields = new IndexedDistances();
		 
		// the secondary index is used to indicate whether it should be used or not
		// the score field is used to determine the column's position in the output 
		cf = new IndexDistance(1,  0, 1,"index");         	catalogFields.add(cf) ;
		cf = new IndexDistance(2,  0, 2,"step"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(3,  0, 3,"score"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(4,  0, 4,"variableindexes"); catalogFields.add(cf) ;
		cf = new IndexDistance(5,  0, 5,"truepositives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(6,  0, 6,"truenegatives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(7,  0, 7,"falsepositives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(8,  0, 8,"falsenegatives"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(9,  0, 9,"tprate"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(10, 0, 10,"tnrate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(11, 0, 11,"fprate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(12, 0, 12,"fnrate"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(13, 0, 13,"ppv"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(14, 0, 14,"npv"); 			catalogFields.add(cf) ;
		cf = new IndexDistance(15, 0, 15,"sensitivity"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(16, 0, 16,"specificity"); 	catalogFields.add(cf) ;
		cf = new IndexDistance(17, 0, 17,"rocauc"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(18, 0, 18,"rocstp"); 		catalogFields.add(cf) ;
		cf = new IndexDistance(19, 0, 19,"risk"); 			catalogFields.add(cf) ;
	}

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

	public IndexedDistances getCatalogFields() {
		return catalogFields;
	}

	public ArrayList<String> getAllFieldLabels() {
		return catalogFields.getAllFieldLabels() ;
	}

	public int setOutputColumn(String fieldLabel, int columnPosition) {
		int result=-1;
		int ix ;
		ix = catalogFields.getIndexByStr(fieldLabel) ;
		
		if (ix>=0){
			catalogFields.getItem(ix).setSecindex(1);
			catalogFields.getItem(ix).setDistance(columnPosition);
			result=0;
		}
		return result;
	}

	public void setOutputColumns(Map<String, Integer> outdef) {
		// TODO Auto-generated method stub
		
	}

	public void resetOutputDefinition() {
		int mode = 1;
		if (mode<0){
			//catalogFields.clear();
			//return;
		}
		for (int i=0;i<catalogFields.size();i++){
			catalogFields.getItem(i).setSecindex(0) ;
		}
	}

	public ArrayList<String> createHeaderRow() {
		ArrayList<String> strlist = new ArrayList<String> ();
		int sxi;
		IndexedDistances cfs = new IndexedDistances();
		
		// we need to store it into a temp list first, since the order cold be changed
		for (int i=0;i<catalogFields.size();i++){
			sxi = catalogFields.getItem(i).getSecindex();
			if (sxi>0){
				cfs.add( catalogFields.getItem(i) ) ;
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
		for (int i=0;i<catalogFields.size();i++){
			sxi = catalogFields.getItem(i).getSecindex();
			if (sxi>0){
				cfs.add( catalogFields.getItem(i) ) ;
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










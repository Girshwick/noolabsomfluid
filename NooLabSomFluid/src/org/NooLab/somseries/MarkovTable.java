package org.NooLab.somseries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.NooLab.field.repulsive.components.data.PointXY;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.math.array.DoubleArray;



/**
 * this class provides basic functionalities around markov transition tables
 * 
 * 
 *
 */
public class MarkovTable {
	
	/** no re-current loops, strictly following the max of a row  */
	public static final int _PATH_MODE_MAXVAL  = 1 ;
	
	/** no re-current loops, strictly following any cell in the row in order to find the maximum path  */
	public static final int _PATH_MODE_MAXPATH = 2 ;
	
	/** strictly increasing row, column indexes */
	public static final int _PATH_MODE_INC = 3 ;
	
	/** any kind of index is allowed, following the path "markovian" = randomly;
	 *  from that, an estimated most-probable sequence is determined
	 */
	public static final int _PATH_MODE_SIM = 5 ;
	
	// ....................................................
	
	//DSom dSom;
	SomMapTable somMapTable ;
	
	double[][] mTable;
	
	ArrayList<MarkovTableCells> mcs = new ArrayList<MarkovTableCells>();
	ArrayList<Integer> salientVariables = new ArrayList<Integer>(); 
	
	int pathFollowMode = -1;
	
	
	// ========================================================================
	public MarkovTable( SomMapTable sTable) {
		
		somMapTable = sTable ;
	}
	// ========================================================================
	
	
	public void setTable(double[][] _table) throws Exception{
		boolean hb=true;
		String estr ="";
		
		if ((_table==null) || ( _table.length<=1) || ( _table[0].length<=1)){
			hb = false;
			estr = "table is empty or too small.";
		}
		if ( _table.length != _table[0].length){
			hb = false;
			estr = "dimensions do not match.";
		}
		
		if (hb==false){
			throw(new Exception("Error in MarkovTable: "+estr)) ;
		}
		mTable = DoubleArray.copy( _table );
		
	}
	 

	public void identifyWeightedPaths(  ) {
		int n;
		MarkovTableCells mtcs = new MarkovTableCells();
		MarkovTableCell  mc;
		boolean isNormal=false;
		
		try{
		

			// check table: is it normalized
			isNormal = tableIsNormalized();
			
			if ((isNormal) && (pathFollowMode>=_PATH_MODE_SIM)){
				determineExpectablePaths();
			}else{
				followPathByMaxs();
				
				// now determining the sets: without tvindex, per sequence a separate set
				n=0;
				while (n<mcs.size()){
					mtcs = mcs.get(n) ;
					for (int i=0;i<mtcs.items.size();i++){
						mc = mtcs.items.get(i) ;
						// put the coord pair int result list, if it is not already 
						
						if ((salientVariables.indexOf(mc.x)<0) && (somMapTable.tvIndex != mc.x)){
							salientVariables.add(mc.x);
						}
						if ((salientVariables.indexOf(mc.y)<0)&& (somMapTable.tvIndex != mc.y)){
							salientVariables.add(mc.y);
						}
					}
					n++;
				} // n ->

			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		n=0;
	}


	
	public ArrayList<Integer> getSalientVariables() {
		return salientVariables;
	}
	
	
	@SuppressWarnings("unchecked")
	private void followPathByMaxs() {
		
		int m,n, startCol;
		boolean _follow, hb;
		double pvalue;
		
		double[] rowMaxima;
		
		MarkovTableCells _mcs = new MarkovTableCells();
		MarkovTableCell  mc;
		MarkovTableCells seqCells = new MarkovTableCells();
		
		ArrayList<MarkovTableCells> sequences = new ArrayList<MarkovTableCells>();
		
		n=0;
		// row by row we scan for possible entry points
		for (int r=0; r<mTable.length;r++){
			
			for (int rc=0;rc<mTable[r].length;rc++){
				
				pvalue = mTable[r][rc] ;
				mc = new MarkovTableCell(r,rc) ; // 
				_follow = (pvalue>0.0) && (seqCells.getItemIndex(r, rc)<0); 
				
				if (_follow){
					if (pathFollowMode == _PATH_MODE_MAXVAL) {
						// we have to remember of last max in this row -> rowMaxima[]
						startCol = DoubleArray.maxIndexBeyondIndex(mTable[r], 0) ; 
					} else{
						if (pathFollowMode == _PATH_MODE_INC) { 
							startCol = r+1 ;
						}else{
							startCol = DoubleArray.maxIndex( mTable[r] );
						}
					}
				}
				
				
				if (_follow){
					
					_mcs.items.add(mc);
					unfoldPath(r,rc, 0, seqCells, _mcs);
					n = seqCells.size() ;
					seqCells.calculate() ;
					
					if ((seqCells.pSum>0.6) && (seqCells.size()>=3)){
						sequences.add( new MarkovTableCells(seqCells) ) ;
					}
					seqCells.items.clear();
					seqCells.pSum = 0.0;
				}
			}
		} // r -> all primal touches
		
		Collections.sort(sequences, new MtcComparator());
		
		MarkovTableCell mtc2,mtc1;
		n=0;
		while (n<sequences.size()-1){
			
			
			for (int i=1;i<sequences.get(n).items.size();i++){
				
				mtc1 = sequences.get(n).items.get(i) ;
				// now check for this item (x,y = MarkovCell) whether it appears in any of the 
				// following (sorted!!) entries in sequences
				pvalue = sequences.get(n).pSum ; 
				m=n+1;
				while (m<sequences.size()){
				
					for (int k=0;k<sequences.get(m).items.size();k++){
						hb = false;
						mtc2 = sequences.get(m).items.get(k);
						hb = (mtc1.x==mtc2.x) && (mtc1.y==mtc2.y);
						
						if (hb){ 
							if (k<=1) {
								hb = sequences.get(m).pSum > pvalue * 0.92   ; 
							} else{
								hb = sequences.get(m).pSum > pvalue * 0.37   ;
							}
						}
						
						if (hb){
							// drop the whole sequence
							sequences.remove(m) ;
							m--;
							break;
						}
					}
					m++;
				}
				
			} // i-> all items of m-sequene
			
			n++;
		}
		
		// now determining the sets: without 0 and without tvindex
		n=0;
		while (n<sequences.size() ){
			sequences.get(n).items.remove(0) ;
			n++;
		}

		n= sequences.size();
		
		mcs = new ArrayList<MarkovTableCells>(sequences) ;
		 
	}

	
	
	@SuppressWarnings("rawtypes")
	class MtcComparator implements Comparator{

		@Override
		public int compare(Object arg0, Object arg1) {
			
			int result=0;
			double v1,v2 ;
			
			MarkovTableCells mtcs1, mtcs2 ;
			
			
			mtcs1 = (MarkovTableCells)arg0;
			mtcs2 = (MarkovTableCells)arg1;
			
			v1 = mtcs1.pSum ;
			v2 = mtcs2.pSum ;
			
			if (v1>=v2){
				result = -1;
			}
			if (v1<v2){
				result = 1;
			}
			
			return result;
		}
		
	}
	/**
	 * 
	 * this looks for the path under the constraint of that is, there are NO loops
	 * 
	 * increasing r,c-indexes
	 * 
	 * @param r
	 * @param c
	 * @param step
	 * @param seqCells
	 */
	private void unfoldPath( int r, int c, int step, MarkovTableCells seqCells , MarkovTableCells _mcs){
		
		MarkovTableCell  mc;
		int mxi,startCol=0;
		double mx, pvalue;
		
		//  no re-current loops  
		// pathFollowMode == _PATH_MODE_MAX 
		
		// strictly increasing row, column indexes 
		// pathFollowMode == _PATH_MODE_INC 
		
		if ((pathFollowMode == _PATH_MODE_INC) && (r>=mTable.length-1)){
			step--;
			return ;
		}

		if (c<0){
			// dependent on pathFollowMode, we start with ix=0 or with x=r+1
			if (pathFollowMode == _PATH_MODE_INC) {
				startCol = r+1; 
			} else{
				startCol = 0;
			}
			mx = mTable[r][startCol] ;
			mxi = startCol;
			for (int i=startCol;i<mTable.length;i++){
				if ((mx< mTable[r][i]) && (seqCells.getItemIndex(r,i)<0) ){
					// && (_mcs.getItemIndex(r,i)<0)
					mx = mTable[r][i];
					mxi = i;
				}
			}
			c=mxi;
			
			if ((pathFollowMode == _PATH_MODE_MAXPATH ) && (r>=mTable.length)){
				mxi = seqCells.getItemIndex(r,c) ;
				if (mxi>=0){
					step--;
					return ;
				}
			}

		} // c<0 ?
		
		if ((r>=0) && (c>=0)){
			
			pvalue = mTable[r][c];
			mc = new MarkovTableCell(r,c, pvalue) ;
		
			seqCells.add(mc) ;
		
			unfoldPath( c, -1, step++, seqCells, _mcs) ;
		}
	}
	
	
	private void determineExpectablePaths() {
		//  
		
	}
	
	
	private boolean tableIsNormalized(){
		boolean rB = true;
		double sums[] ;
		double s;
		
		
		sums = new double[mTable.length] ;
		
		// row by row we scan for possible entry points
		for (int r=0; r<mTable.length;r++){
			s=0;
			for (int rc=0;rc<mTable[r].length;rc++){
				 s=s+mTable[r][rc];
			}
			sums[r] = s ;
			if (s>1.0){
				rB=false ;
				break ;
			}
		} // r -> all primal touches 
		
		if (rB){
			for (int i=0;i<sums.length;i++){
				if ((sums[i]>1.0) || ((sums[i]<0.9999) && (sums[i]!=0.0)) || (sums[i]<0)){
					rB=false ;
					break ;
				}
			}
		}
		return rB;
	}


	public int getPathFollowMode() {
		return pathFollowMode;
	}


	public void setPathFollowMode(int pathFollowMode) {
		this.pathFollowMode = pathFollowMode;
	}
	
}

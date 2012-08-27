package org.NooLab.somscreen.linear;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomMapTable;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.logging.PrintLog;

import org.apache.commons.collections.CollectionUtils;
import org.math.array.*;

import Jama.EigenvalueDecomposition;

// static import of all array methods : linear algebra and statistics
import static org.math.array.DoubleArray.maxIndices;
import static org.math.array.LinearAlgebra.*;
import static org.math.array.StatisticSample.*;
import org.math.array.DoubleArray;

/**
 * PCA is in JMathTools incl example
 * 
 */
public class PCA {

	 
	SomMapTable somMapTable ;

	ArrayList<int[]> relatedVariablesSet ;
	ArrayList<Integer> largeUnifiedSet = new ArrayList<Integer> (); 
	// ............................................
	
	LinearAlgebra linAlg = new LinearAlgebra();
	
	double[][] X; 		// initial datas : lines = events and columns = variables
	double[] meanX, stdevX;

	double[][] Z; 		// X centered reduced
	double[][] cov; 	// Z covariance matrix
	double[][] U; 		// projection matrix
	double[][] uT; 		// tranposed U ;
	double[] info; 		// information matrix
	
	
	PrintLog out = new PrintLog(2, false);
	
	// ========================================================================
	public PCA(   SomMapTable mapTable){
	
		 
		somMapTable = mapTable ;
		 
		 
		init( somMapTable.values ) ;
		
	}
	
	/** only for test purposes ... */
	public PCA(){
		
		test();
	}
	// ========================================================================	

 
	public void init(double[][] _X) {
		X = _X;
 
		if (_X.length==0){
			stdevX = new double[0]; 
			meanX= new double[0];
			cov = new double[1][0];
			Z = new double[1][0];
			return;
		}
		
		stdevX = stddeviation(X);
		meanX = mean(X);
 
		Z = center_reduce(X);
 
		cov = covariance(Z);
 
	}
	
	
	public void calculate(){
		
		try{
			if (cov[0].length>0){
				EigenvalueDecomposition e = eigen(cov);
				U = transpose(e.getV().getArray());
			
				info = e.getRealEigenvalues(); 
				// covariance matrix is symmetric, so only real eigenvalues...

				uT = transpose(U);
			}
		}catch(Exception e){
			e.printStackTrace() ;
		}
		
		print();
	}
	
	/**
	 *  using the Ut -matrix, we have to find sets of potentially 
	 * 
	 *  1. top 3 columns regarding the vertical "sum" (variance explained)
	 *  2. max in TV row
	 *  3. top-3 columns c.o.v in loading AND pos.sum.
	 *  4. top-3 OR (>15%) of relative row sums 
	 *  
	 *  for each of those criteria,  select the max loadings (values in columns) being > 0.3,
	 *  and select the variables of the respective rows into distinct sets
	 * 
	 *  this creates 4 lists of sets of variables
	 * 
	 *  for each of those sets,
	 *  create new variables by transforming them by log(multiplicate[sets])  
	 *   - by the cross-product for definng the combinatorial transformation
	 *   - the full (sum;product) combination from "all" (but max 5 ~ top-5) elements
	 *  
	 *  
	 *  https://onlinecourses.science.psu.edu/stat857/book/export/html/11
	 * 
	 */
	public ArrayList<int[]> prepareResults() throws Exception{
		int ix,topN, sc;
		
		double[] rowMargins, colMargins, colVariances ; 
		int[] rowSumSelection , colSumSelection, colVarSelection,ccol , luv,rowSelection;
		ArrayList<int[]> rowSelectionSet = new ArrayList<int[]>();
		
		// calculate margins of table uT
		
		rowMargins = DoubleArray.marginalSumRow(uT) ;
		colMargins = DoubleArray.marginalSumCol(uT) ;
		
		colVariances = DoubleArray.marginalVariancesCol( uT) ;
		
		topN = 3 ;
		 
		rowSumSelection = DoubleArray.maxIndices( rowMargins, 2);
		// these rows directly indicate a set
		rowSelectionSet.add(rowSumSelection) ;
		
		
		// now the columns ...
		colSumSelection = DoubleArray.maxIndices( colMargins, topN );	
		colVarSelection = DoubleArray.maxIndices( colVariances, topN);
		
		// these arrays now indicate the column for which we look for the topN cells.
		// these topN cells are in defined rows, so the index othose topN values in the columns 
		//   indicate the variables
		// per column we get a set of topN, these sets we should collect and describe it by a score 
		//   that reflects its relevance
		ccol = DoubleArray.mergeValues(colSumSelection, colVarSelection) ;
		
		for (int i=0;i<ccol.length;i++){
			ix = ccol[i] ;
			// now get the top 3 of the column indivated by index ix
			rowSelection = DoubleArray.maxIndices( uT[ix], 2 );
			Arrays.sort(rowSelection);
			if ( Arrays.binarySearch( rowSelection, somMapTable.tvIndex)<0){
				rowSelectionSet.add(rowSelection) ;
			}
		}
		
		// remove double entries (whole sets must be equal, or completely contained in another one)
		int i=0;
		luv = rowSelectionSet.get(0) ;
		while (i<rowSelectionSet.size()-1){
			int k=i+1;
			while (k<rowSelectionSet.size()){
				sc = setContainment( rowSelectionSet.get(i),rowSelectionSet.get(k)) ;
				if ( sc !=0){
					if (sc>0){
						rowSelectionSet.remove(k) ;
						k--;
					}else{
						rowSelectionSet.remove(i) ;
						i--;
						break; // return to the i loop
					}
				}
				k++;
			} // i,=k -> all
			i++;
			if (i<rowSelectionSet.size()){
				luv = DoubleArray.mergeValues(luv, rowSelectionSet.get(i)) ;
			}
		} // i -> almost all
		
		for (int j=0;j<ccol.length;j++){
			if ((luv!=null) && (j<luv.length) && (largeUnifiedSet.indexOf(luv[j] )<0)){
				largeUnifiedSet.add( luv[j]) ;   	
			}
		}
		if (largeUnifiedSet.size()>0){
			Collections.sort(largeUnifiedSet) ;
			relatedVariablesSet = rowSelectionSet;
		}
		
		return rowSelectionSet;
		
	}

	@SuppressWarnings("unchecked")
	private int setContainment( int[] values1, int[] values2 ) {
		int result = 0;
		
		ArrayList<Integer> differenceSet ;
		ArrayList<Integer> ixes1 = new ArrayList<Integer>();
		ArrayList<Integer> ixes2 = new ArrayList<Integer>();
		
		int vi1,vi2;
		
		vi1= values1.length;
		vi2= values2.length;
		
		for (int i=0;i<values1.length;i++){
			if (vi1>=vi2){
				ixes1.add(values1[i]) ;
			}else{
				ixes1.add(values2[i]) ;
			}
		}
		for (int i=0;i<values2.length;i++){
			if (vi1<vi2){
				ixes2.add(values1[i]) ;
			}else{
				ixes2.add(values2[i]) ;
			}
		}
		
		// now the larger of the two sets is in the first list
		// also: union, intersection
		differenceSet = (ArrayList<Integer>) CollectionUtils.subtract( ixes1, ixes2);
		
		if (differenceSet.size()==0){
			if (vi1>=vi2){
				result =  1;
			}else{
				result = -1;
			}
			
		} 
		
		return result;
	}

	public double[] getVectorInformation() {
		 
		return info;
	}

	public double[] getVector(int i) {
		 
		return U[i];
	}

	public double[][] getVectors(int[] vix) {
		
		return uT ;
	}


	// ------------------------------------------------------------------------
	// normalization of x relatively to X mean and standard deviation
	public double[][] center_reduce(double[][] x) {
		
		double[][] y = new double[x.length][x[0].length];
		
		for (int i = 0; i < y.length; i++){
			
			for (int j = 0; j < y[i].length; j++){
				if (stdevX[j]>0){
					y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
				}else{
					y[i][j] = 0.0;
				}
			}
			
		}
		return y;
	}
 
	// de-normalization of y relatively to X mean and standard deviation
	public double[] inv_center_reduce(double[] y) {
		return inv_center_reduce(new double[][] { y })[0];
	}
 
	// de-normalization of y relatively to X mean and standard deviation
	public double[][] inv_center_reduce(double[][] y) {
		double[][] x = new double[y.length][y[0].length];
		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < x[i].length; j++)
				x[i][j] = (y[i][j] * stdevX[j]) + meanX[j];
		return x;
	}
	/*
	private void view() {
		// Plot
		Plot2DPanel plot = new Plot2DPanel();
 
		// initial Datas plot
		plot.addScatterPlot("datas", X);
 
		// line plot of principal directions
		plot.addLinePlot(Math.rint(info[0] * 100 / sum(info)) + " %", meanX, inv_center_reduce(U[0]));
		plot.addLinePlot(Math.rint(info[1] * 100 / sum(info)) + " %", meanX, inv_center_reduce(U[1]));
 
		// display in JFrame
		new FrameView(plot);
	}
	 */

	private void print() {
		// Command line display of results
		try{
			String prjV    = ArrUtilities.arrayToString( uT ) ;
			String prjInfo = ArrUtilities.arrayToString( info ) ;
		
			out.print(4,"projection vectors\n" + prjV);
			out.print(4,"information per projection vector\n" + prjInfo );
		}catch(Exception e){}
	}

	public int[] getFilteredVectorIndices(double threshold, int exclIndex) {
		int[] selectedIx = new int[0];
		ArrayList<Integer> selix = new ArrayList<Integer> (); 
		
		for (int i=0;i<info.length;i++){
			if (info[i] > threshold){
				if (i!=exclIndex){
					selix.add(i) ;
				}
			}
		}
		selectedIx = (int[]) ArrUtilities.changeArraystyle(selix) ;
		
		return selectedIx;
	}
 
	
	public ArrayList<int[]> getRelatedVariablesSet() {
		return relatedVariablesSet;
	}

	public ArrayList<Integer> getLargeUnifiedSet() {
		return largeUnifiedSet;
	}

	public void test() {
		double[][] xinit = random(1000, 2, 0, 10);
 
		// artificial initialization of relations
		double[][] x = new double[xinit.length][];
		for (int i = 0; i < x.length; i++){
			x[i] = new double[] { xinit[i][0] + xinit[i][1], xinit[i][1] };
		}
 
		init(x);
		
		calculate();
		
		print();
		// view();
	}
    
}


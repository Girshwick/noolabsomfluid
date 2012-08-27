package org.NooLab.somscreen.linear;

import java.util.ArrayList;

import org.NooLab.math3.linear.Array2DRowRealMatrix;
import org.NooLab.math3.linear.DecompositionSolver;
import org.NooLab.math3.linear.RealMatrix;
import org.NooLab.math3.linear.SingularValueDecomposition;

import org.NooLab.somfluid.core.categories.similarity.Similarity;
import org.NooLab.somfluid.core.engines.det.DSom;
import org.NooLab.somfluid.core.engines.det.SomMapTable;

import org.NooLab.somseries.MarkovTable;
import org.NooLab.utilities.ArrUtilities;
import org.NooLab.utilities.datatypes.IndexDistance;
import org.NooLab.utilities.datatypes.IndexedDistances;
import org.NooLab.utilities.logging.PrintLog;


/**
 * http://david-moriarty.com/Cholesky.html
 * http://www.riskglossary.com/link/cholesky_factorization.htm
 * 
 * 
 *
 */
public class Decomposition {

	//DSom dSom;
	SomMapTable somMapTable ;

	MarkovTable markovian; 
	
	// the resulting selection of variables, denoted by their indices in somMapTable
	ArrayList<Integer> selection = new ArrayList<Integer>();
	
	PrintLog out = new PrintLog(2,true);
	
	private int[] stdevX;
	private double[] meanX;
	
	public Decomposition(  SomMapTable mapTable){
	
		 
		somMapTable = mapTable ;
		 
	}
	
	public void calculate(){

		int n;
		double max,min ,v, simile;
		double upperLimit,lowerLimit; 
		IndexDistance ixd ; 
		IndexedDistances ixds,ixdT;
		
		SingularValueDecomposition svd ;
		DecompositionSolver  dcs;
		RealMatrix rmatrix , vM ,sM,uM ;
		double[][] svda, d = somMapTable.values, vtSim;
		double[] sv, meanX, stdevX ;
		
		ArrayList<Double> vector1 = new ArrayList<Double>(), vector2 = new ArrayList<Double> (); 
		
		if ((d==null) || (d.length<=1)){
			return;
		}
		// we should subtract mean for each column
		// in each column, we should divide by stdev
										out.print(3,"calculating linear matrix description for som ...");
		rmatrix = new Array2DRowRealMatrix( d ) ;
		
		svd = new SingularValueDecomposition(rmatrix) ;
		
		sv = svd.getSingularValues(); // the values on the diagonal of S, meiste Variation in der 1. Dimension
									  // can be used to determine the number of k for reconstruction = number of params,
									  // and since we have the TV included, the largest % drop between s(i)
		                              // in a linear approach, we may drop those variables with small s-values, 
									  // say, <0.05, or <(2nd value)/25 
		/*
		   	- U (n×n) und V (m×m), mit n=rows, m=columns
		   	- Zeilen von VT (Spalten von V) können als Einflussfaktoren aufgefasst werden, die den Daten zugrunde liegen
				(sie definieren neue Achsen im R^m )
				- Zeilen von U als Koordinaten der Objekte bzgl. dieser Achsen
		 */
		vM = svd.getVT();
		sM = svd.getS();  
		uM = svd.getU() ; // coordinates of clusters (rows) in the space defined by VT
						  // for each var there is 1 col, 	
						  // the difference to A is interesting, the looking for the largest difference ...
		 
		 
		
		svda = vM.getData() ;
		for (int i=0;i<svda.length;i++){
			min = 9999999.09;
			max = -99999.09 ;
			for (int j=0;j<svda.length;j++){
				if (min>svda[i][j]) min = svda[i][j] ;
				if (max<svda[i][j]) max = svda[i][j] ;
			}
			
			for (int j=0;j<svda.length;j++){
				v = (svda[i][j]-min)/(max-min) ;
				v = Math.round( v * 1000.0)/1000.0 ;
				svda[i][j] = v ;
			}
		}
		// -> rows of VT need to be checked for similarity to TV (row by tvIndex) 
		//         (need first to be lin normalized for [0..1] as there are neg values)
		//    especially for the similarity with TV : we need to include TV
		
		// the most dissimilar and the most similar variables 
		
		/*    A = USV^T
			  A = Datenmatrix
			  only considering k-elements -> reconstructing data without linear noise
			  
					m-dimensionale Einheitskugel
						- wird durch VT "verdreht"
						- und durch S skaliert
					passt dann „über“ die Daten
					
			  	  So the same singular values are also the square roots of the eigen-values of AAt and
 			  the eigenvectors of AAt are the columns of U

		*/
										out.print(3,"evaluating matrix description ...");
		Similarity sim = new Similarity();
		vtSim = new double[svda.length][svda.length] ;
		min = 9999999.09;
		max = -99999.09 ; 
		
		// creating a similarity matrix, comparing VT(i) , VT(j)
		for (int i=0;i<svda.length-1;i++){
		
			// profile i
			// -> ArrayList<Double>: clear(), transfer 
			vector1 = ArrUtilities.changeArraystyle( svda[i]) ; vector1.trimToSize() ;
			for (int j=i+1;j<svda.length;j++){
				// profile j
				// -> ArrayList<Double>
				vector2 = ArrUtilities.changeArraystyle( svda[j]) ;vector2.trimToSize() ;
				simile = sim.similarityWithinDomain(vector1, vector2, false) ;
				
				simile = Math.round( simile * 1000.0)/1000.0 ; 
				vtSim[i][j] = simile ; 
				
				if (min>simile )min = simile ;
				if (max<simile )max = simile ;
			}
			
		} // i-> svda all
		
		upperLimit = (max) - (max-min)*0.1 ;
		lowerLimit = (min) + (max-min)*0.1 ;
		// copy-extract anti-/similars: by 10% of value span, or 15% of items, whatever first 
		// it is a symmetric matrix, such we need 1 integer (enum of var) 1 double (similarity)
		
		int tvIndex,nc=0; 
		// this: tvIndex = dSom.getSomLattice().getNode(0).getSimilarity().getIndexTargetVariable() ;
		//       would point to the original table, however, we are working on an extract here (only used variables)
		//       that is contained in somMapTable
		tvIndex = somMapTable.tvIndex ; 
		ixds = new IndexedDistances();
		ixdT = new IndexedDistances();
		// ;
		boolean tchk;
		double vtsimSum=0;
		for (int i=0;i<vtSim.length-1;i++){
			
			 
			for (int j=i+1;j<vtSim.length;j++){
				tchk = false ;
				v = vtSim[i][j];
				if (v>0){
					v = (v-min)/(max-min) ;
					tchk = (v>0.71) || (v<0.19);
					 
					}
				if (tchk==false){
					if (j!=tvIndex){
						vtSim[i][j]=0;
					}
					
				}else{
					ixd = new IndexDistance(i,j, vtSim[i][j]);
					ixd.setGuidStr("");
					ixds.getItems().add(ixd) ;
					vtsimSum = vtsimSum + vtSim[i][j];
				}
				if (j==tvIndex){
					ixd = new IndexDistance(i,j, vtSim[i][j]);
					ixd.setGuidStr("tv");
					ixdT.getItems().add(ixd) ;
					ixds.getItems().add(ixd) ;
				}
			}
		}
		
		double vtsMean = vtsimSum/ nc;
		/*
		 * this table "vtSim" now expresses an "information collection graph"
		 * the longer the possible path and the larger the path the stronger is the flow of information
		 * through this path towards the target variable
		 * 		 * 
		 *  for doing that we interpret it as a Markovian transition matrix,
		 *  (-> while passing through, we could/have to collect the paths as series of integers, in order to avoid double scans)
		 * 
		 *  screening this list : "simply" removing entries which create a path smaller than L
		 *  L is dependent on the size of the matrix = count of variables 
		 * 
		 */
											out.print(3,"evaluating matrix description (2)...");
		try{
		
			markovian = new MarkovTable(somMapTable);
			markovian.setTable( vtSim );
			// normalizing is not necessary 
			
			markovian.setPathFollowMode( MarkovTable._PATH_MODE_INC) ;
			
			markovian.identifyWeightedPaths();
			
			selection.addAll( markovian.getSalientVariables() );
			
		}catch(Exception e){
			
		}
	  		
		v=0;
		
											out.print(3,"evaluating matrix description completed.");
	}
	
	public ArrayList<Integer> getSelection() {
		return selection;
	}

	public double[][] center_reduce(double[][] x) {
		
		double[][] y = new double[x.length][x[0].length];
		for (int i = 0; i < y.length; i++){
			for (int j = 0; j < y[i].length; j++){
				// TODO : y[i][j] = (x[i][j] - meanX[j]) / stdevX[j];
			}
		}
		return y;
	}
}
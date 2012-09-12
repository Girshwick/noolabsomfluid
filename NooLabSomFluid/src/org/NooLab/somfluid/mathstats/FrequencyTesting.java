package org.NooLab.somfluid.mathstats;



class FTable{

	int[][] rawValues ;
	double[][] cells;
	double[] rowMargins, colMargins; ;
	
	double tableTotal;
	
	
	public FTable(int rowCount, int colCount){
		
		rawValues = new int[rowCount][colCount] ;
		
		cells = new double[rowCount][colCount] ;
	
		rowMargins = new double[rowCount] ;
		colMargins = new double[colCount] ;
		
		
	}


	public int rowsum( int[][] values, int rowIndex ) {
		
		int sum = 0;
		
		for (int i=0;i<values[rowIndex].length;i++){
			sum = sum + values[rowIndex][i] ;
		}
		
		return sum;
	}
	public double rowsum( double[][] values, int rowIndex ) {
		
		double sum = 0;
		
		for (int i=0;i<values[rowIndex].length;i++){
			sum = sum + values[rowIndex][i] ;
		}
		
		return sum;
	}


	public double colsum(int[][] values, int colIndex) {
		int sum = 0;
		
		for (int i=0;i<values[i].length;i++){
			sum = sum + values[i][colIndex] ;
		}
		
		return sum;
	}

	
}

public abstract class FrequencyTesting {
	
	int rowCount,colCount = -1;
	
	FTable table;
	
	
	double significance = -1.0 ;
	boolean resultAvailable=false;
	
	
	// ========================================================================
	public FrequencyTesting( int rowcount, int colcount ){
		
		rowCount = rowcount ;
		colCount = colcount ;
		
		table = new FTable(rowCount,colCount); 
		
	}
	// ========================================================================


	public void importData(int[][] _table) {
	 
		System.arraycopy(_table, 0, table.rawValues , 0, _table.length) ;
		
		for (int i=0;i<_table.length;i++){
			System.arraycopy(_table[i], 0, table.rawValues[i] , 0, _table[0].length) ;
		}
		
		
	}


	
	
	
	
	
	
	
	
	
}

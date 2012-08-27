package org.NooLab.somfluid.mathstats;

import java.util.ArrayList;



/**
 * 
 * this class performs a goodness of fit test according to Sokal & Rohlf, Biometry
 * 
 * Their G-Test is superior to the standard Chi^2 test
 * 
 * -> i is not tested yet, just ported from delphi
 * 
 * we use it for
 * - detecting clusters that behave "differently" in targeted modeling
 * - in SomSprite as a robust non-parametric measure of association
 * 
 */
public class GTest_RxC extends FrequencyTesting {

	private double GStatistics;
	private int degreesOfFreedom;

	// ========================================================================
	public GTest_RxC(int rowcount, int colcount) {
		super(rowcount, colcount);

	}

	// ========================================================================

	public GTest_RxC setData(int[] column1, int[] column2) throws Exception {
		if ((column1.length <= 1) || (column1.length != column2.length)) {
			throw (new Exception("invalid data!"));
		}

		int[][] _table = new int[column1.length][2];

		for (int r = 0; r < column1.length; r++) {
			_table[r][0] = column1[r];
			_table[r][0] = column2[r];
		}

		importData(_table);

		return this;
	}

	public GTest_RxC setData(int[][] freqTable) throws Exception {

		if ((freqTable.length <= 1)) {
			throw (new Exception("invalid data!"));
		}

		importData(freqTable);
		return this;
	}

	public GTest_RxC setData(ArrayList<int[]> freqTable) throws Exception {

		if ((freqTable.size() <= 1) || (freqTable.get(0).length <= 1)) {
			throw (new Exception("invalid data!"));
		}

		int[][] _table = new int[freqTable.size()][freqTable.get(0).length];

		for (int r = 0; r < freqTable.size(); r++) {

			_table[r][0] = freqTable.get(r)[0];
			_table[r][1] = freqTable.get(r)[1]; // allow more columns
		}

		importData(_table);
		return this;
	}

	public void perform() {
		
		if ((rowCount==2) && (colCount==2)){
			perform_2x2() ;
		}else{
			perform_RxC() ;
		}
	}

	
	public void perform_2x2() {
		
	}
	
	public void perform_RxC() {

		int tfreq = 0;
		double v, cell_freqs_sum = 0, col_freqs_sum = 0, row_freqs_sum = 0;
		double G_stat = -1.0;

		resultAvailable = false;

		for (int z = 0; z < rowCount; z++) {

			for (int s = 0; s < colCount; s++) {

				v = table.rawValues[z][s];

				tfreq = tfreq + (int) (v);

				if (v <= 0) {
					table.cells[z][s] = 0;
				} else {
					table.cells[z][s] = v * Math.log1p(v);
				}

				cell_freqs_sum = cell_freqs_sum + table.cells[z][s];

			}
		}

		if (tfreq == 0) {
			return;
		}

		for (int z = 0; z < rowCount; z++) {

			v = table.rowsum(table.rawValues, z);

			if (v == 0) {
				table.rowMargins[z] = 0;
			} else {
				table.rowMargins[z] = v * Math.log1p(v);
			}

			row_freqs_sum = row_freqs_sum + table.rowMargins[z];

		}

		for (int s = 0; s < colCount; s++) {

			v = table.colsum(table.rawValues, s);

			if (v == 0) {
				table.colMargins[s] = 0;
			} else {
				table.colMargins[s] = v * Math.log1p(v);
			}

			col_freqs_sum = col_freqs_sum + table.colMargins[s];
		}

		table.tableTotal = tfreq * Math.log1p(tfreq);

		G_stat = 2 * (cell_freqs_sum - row_freqs_sum - col_freqs_sum + table.tableTotal);

		// calculating Williams'-correction}
		double zt1 = 0, zt2 = 0;

		for (int s = 0; s < colCount; s++) {

			v = table.colsum(table.rawValues, s);
			v = 1 / v;
			zt1 = zt1 + v;
			
		}
		zt1 = tfreq * zt1 - 1;

		int ze_count = 0;
		for (int z = 0; z < rowCount; z++) {

			v = table.rowsum(table.rawValues, z);
			if (v > 0) {
				v = 1 / v;
				zt2 = zt2 + v;
			} else {
				ze_count++;
			}
		}

		if (ze_count == rowCount) {
			return;
		}
		zt2 = tfreq * zt2 - 1;

		double q = 1 + (zt1 * zt2)
				/ (6 * (tfreq) * (rowCount - 1) * (colCount - 1));

		if (q > 0) {
			G_stat = G_stat / q;
		}

		// determining the alpha level by referring to the X2 distribution

		
		
		// 
		
		// filling the result array(s) (string[], double[]), and the result variables
		
		int df = (rowCount - 1) * (colCount - 1) ;
		
		SignificanceTesting test = new SignificanceTesting();
		
		
		significance = test.againstDistribution( SignificanceTesting._DISTRIBUTION_CHI2, G_stat, df  );
		
		GStatistics = G_stat;
		degreesOfFreedom = df;
		resultAvailable = true;
	}

	public double getGStatistics() {
		return GStatistics;
	}

	public void setGStatistics(double gStatistics) {
		GStatistics = gStatistics;
	}

	public int getDegreesOfFreedom() {
		return degreesOfFreedom;
	}

	public void setDegreesOfFreedom(int degreesOfFreedom) {
		this.degreesOfFreedom = degreesOfFreedom;
	}

}

/*
 
                                                            err = 19;
      { getting the alpha-value for d.f.= 1  and  G_stat
        file "CHIALPHA.TAB" in directory of exe }

           df = round((c-1)*(r-1)) ;
   _res = score_at_critical_chi2_value( G_stat,df,
                                         crit_val, sig_level_str,2 ) ;
                                                            err = 20;

      { smaller than the critical value => n.s.}
                                             err = 0 ;
 
    str(g_stat:5:3,hs1) ;
         cells[0,1] = 'G(adj)' ;
         cells[1,1] = hs1 ;
    str(crit_val:5:3,hs1) ;
         cells[0,2] = 'crit.val.' ;
         cells[1,2] = hs1 ;

         cells[0,3] = 'd.f.' ;
         cells[1,3] = inttostr(df) ;

         cells[0,4] = 'level of sign.' ;
         if sig_level_str='n.s.' then
            sig_level_str='not significant' ;
         cells[1,4] = sig_level_str ;
 
*/
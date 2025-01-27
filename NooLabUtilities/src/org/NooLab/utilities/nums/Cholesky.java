package org.NooLab.utilities.nums;

import java.util.Arrays;


/**
 * 
 * from Rosetta Code
 * @author kwa
 *
 */
public class Cholesky {
	
	public static double[][] chol(double[][] a){
		int m = a.length;
		double[][] cl = new double[m][m]; //automatically initialzed to 0's
		for(int i = 0; i< m;i++){
			for(int k = 0; k < (i+1); k++){
				double sum = 0;
				for(int j = 0; j < k; j++){
					sum += cl[i][j] * cl[k][j];
				}
				cl[i][k] = (i == k) ? Math.sqrt(a[i][i] - sum) :
					(1.0 / cl[k][k] * (a[i][k] - sum));
			}
		}
		return cl;
	}
 
	public static void main(String[] args){
		double[][] test1 = {{25, 15, -5},
							{15, 18, 0},
							{-5, 0, 11}};
		System.out.println(Arrays.deepToString(chol(test1)));
		double[][] test2 = {{18, 22, 54, 42},
							{22, 70, 86, 62},
							{54, 86, 174, 134},
							{42, 62, 134, 106}};
		System.out.println(Arrays.deepToString(chol(test2)));
	}
}

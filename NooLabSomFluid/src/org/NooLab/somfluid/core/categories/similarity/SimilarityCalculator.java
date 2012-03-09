package org.NooLab.somfluid.core.categories.similarity;

import java.util.ArrayList;

public class SimilarityCalculator{
	
	ArrayList<Double> vector1, vector2 ;
	ArrayList<Double> useIntensity ;
	
	int indexIdColumn=-1, indexTargetVariable=-1;
			
	public SimilarityCalculator( ArrayList<Double> _vector1, ArrayList<Double> _vector2 ){
		
		vector1 = _vector1;
		vector2 = _vector2;
		
		useIntensity = new ArrayList<Double>();
		for (int i=0;i<vector1.size();i++){
			useIntensity.add(1.0) ;
		}
	}
	
	public SimilarityCalculator( ArrayList<Double> _vector1, 
								 ArrayList<Double> _vector2 ,
								 ArrayList<Double> _useIntensity ){
		
		vector1 = _vector1;
		vector2 = _vector2;
		
		useIntensity = _useIntensity ;
	}


	public SimilarityCalculator defineSpecialVariables( int indexId, int indextv, ArrayList<Double> _useIntensity ){
		indexIdColumn = indexId;
		indexTargetVariable = indextv;
		useIntensity = _useIntensity ;
		return this;
	}
	
	public SimilarityCalculator defineSpecialVariables( int indexId, int indextv){
		indexIdColumn = indexId;
		indexTargetVariable = indextv;
		return this;
	}	
	
	public double calc(ArrayList<Double> useIntensity) {
		
		double d ;
		
		d = advancedDistance( vector1,vector2, useIntensity ) ;
		return d;
	}

	public double calc() {
		
		double d ;
		//ArrayList<Double> useIntensity = new ArrayList( Arrays.asList( ));
		
		d = advancedDistance( vector1,vector2, useIntensity ) ;
		return d;
	}

	private double advancedDistance( ArrayList<Double> vector1,
									 ArrayList<Double> vector2, ArrayList<Double> useIntensity) {

		if (vector1.size() != vector2.size()) {
			System.out.println( "Error! vector1.length (" + vector1.size()+") "+
								"<> vectorsize (" + vector2.size() + ")");
			return -1;
		}
		double c, d, d0, df, ic1 = 0, ic2 = 0, iq;
		int i,u=0, z, distanceMeth, fvp=-1;

		distanceMeth = 2;
		d = 0;
		z = 0;
		d0 = 0;

		for (i = 0; i < vector2.size(); i++) {

			if ((i < useIntensity.size()) ) {
				double ui = useIntensity.get(i);
				if (ui <= 0.0){
					u++;
					continue;
				}
			}
			if (fvp<0){fvp=i;}
			if ((vector1.get(i) < 0.0) || (vector2.get(i) < 0.0)) {
				if ((vector1.get(i) > -4) && (vector2.get(i) > -4)
						&& (i != indexIdColumn)
						&& (i != indexTargetVariable)) {
					d = d + 0.6;
				}
			} else {
				c = vector1.get(i) - vector2.get(i);

				d = d + c * c;
				if (c > 0.5) {
					c = c + ((Math.sqrt(c - 0.5)) * 0.812);
					if (c > 1) {
						c = 1;
					}
				}
				d0 = d0 + Math.abs(c);
				z = z + 1;

				if (distanceMeth >= 2) {
					c = 0;
					if (i < vector2.size() - 1) {
						ic1 = vector1.get(i) - vector1.get(i + 1);
						ic2 = vector2.get(i) - vector2.get(i + 1);
					} else {
						ic1 = vector1.get(i) - vector1.get(fvp); // TODO not 0, but first non-Index-column  id
																
																 
																
						ic2 = vector2.get(i) - vector2.get(fvp);
					}
					c = Math.abs(ic1 - ic2);

					if (ic2 != 0) {
						iq = ic1 / ic2;
						if (iq < 0) {
							c = c + Math.sqrt(c / 2);
							if (c > 0.5) {
								c = 0.5;
							}
						}
					}

				} // distanceMeth>=2
				d = d + 0.3 * Math.abs(c);
			}
		}

		df = d / z + 0.2 * (d0 / z);
		return df;
	}
	
} // inner class SimilarityCalculator

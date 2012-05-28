package org.NooLab.somfluid.core.categories.similarity;

import java.util.ArrayList;

public class SimilarityCalculator{
	
	ArrayList<Double> vector1, vector2 ;
	ArrayList<Double> useIntensity ;
	
	int indexIdColumn=-1, indexTargetVariable=-1;
	public boolean suppressSQRT;
			
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
		
		if ( (vector1==null) || (vector2==null) || ((vector1.size() != vector2.size()))) {
			System.out.println( "Error! vector1.length (" + vector1.size()+") "+
								"<> vectorsize (" + vector2.size() + ")");
			return -1;
		}
		double c, d, d0, df, ic1 = 0, ic2 = 0, iq;
		int i,u=0, z, distanceMeth, fvp=-1;
		double ui,vv1,vv2 ;
		
		ArrayList<Double> differencedValues1 = new ArrayList<Double>();
		ArrayList<Double> differencedValues2 = new ArrayList<Double>();
		
		distanceMeth = 2;
		d = 0;
		z = 0;
		d0 = 0;
		ui=-1.0;
		
		for (i = 0; i < vector2.size(); i++) {
			 
			if ((i < useIntensity.size()) ) {
				ui = useIntensity.get(i);
				
				if (ui <= 0.0){
					if (ui!=-2.0){
						u++;
						continue;
					}
				}
			}
			if (fvp<0){fvp=i;}
			if ((vector1.get(i) < 0.0) || (vector2.get(i) < 0.0)) {
				if ( (i != indexTargetVariable) && (vector1.get(i) > -4) && (vector2.get(i) > -4)
						&& (i != indexIdColumn)
						&& (i != indexTargetVariable)
						) {
					if ((vector1.get(i) < 0.0) && (vector2.get(i) < 0.0)){
						d = d + 0.15;
					}else{
						d = d + 0.6;
					}
				}
			} else {
				if (ui==-2.0){ // -2 is our encoding for the target variable
					continue;
				}
				vv1 = vector1.get(i) ;
				vv2 = vector2.get(i) ;
				c = vv1 - vv2;

				differencedValues1.add(vv1);
				differencedValues2.add(vv2);
				
				d = d + c * c;
				if (c > 0.5) {
					double _contrast = ((Math.sqrt(c - 0.5)) * 0.618);// max approx 0.44
					_contrast = _contrast*_contrast;  // max approx 0.19
					c = c + _contrast;
					if (c > 1) {
						c = 1;
					}
				}
				d0 = d0 + Math.abs(c);
				z = z + 1;

				if (distanceMeth >= 2) {
					c = 0;
					/* what now follows is a replacement for the expensive "correlation"
					 * as the corr., it reflects the form factor of the data series
					 */
					int ds = differencedValues1.size()-1;
					// describing the slope between two subsequent points
					if (i < vector2.size() - 1) {
						
						if (ds>1){
						
							ic1 = differencedValues1.get( ds) - differencedValues1.get( ds-1);
							ic2 = differencedValues2.get( ds) - differencedValues2.get( ds-1);
						}
					} else {
						ic1 = differencedValues1.get( ds) - differencedValues1.get( 0);
						ic2 = differencedValues2.get( ds) - differencedValues2.get( 0);
					}
					// comparing the slopes between vectors
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
				
				// weighting the form factor by around 23% 
				d = (4.0*d + 1.0 * Math.abs(c))/5;
			}
		}
		// normalizing the distance per position !!!! 
		// that's important to compare the variance of distance values across models where the feature vector is of different length
		// emphasizing strong contrasts by 18% (1/6)
		df = d / (double)z + 0.2 * (d0 / (double)z);
		
		differencedValues1.clear();
		differencedValues2.clear();
		
		return df;
	}
	
} // inner class SimilarityCalculator

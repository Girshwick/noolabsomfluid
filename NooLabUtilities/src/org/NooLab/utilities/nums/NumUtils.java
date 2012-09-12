package org.NooLab.utilities.nums;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class NumUtils {

	
	public NumUtils(){
		
	}
	
	public static double lazyVariance( double sum,
            					double sqsum,
            					int n){
		return  sqsum /n - (sum/n)*(sum/n) ;
		
	}
	public double lazyvariance( double sum,
	                            double sqsum,
	                            int n){
		return  lazyVariance(sum,sqsum,n) ;
	}

	
	/**
	 * 
	 * index-0 = _min; </br>
	 * index-1 = _max; </br>
	 * index-2 = mean; </br>
	 * index-3 = variance; </br>
	 * index-4 = coeffvar; </br>
	 * index-5 = median; </br>
	 *  </br>
	 * @param values
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static double[] simpleFirstMoments( ArrayList<?> values){
		
		double[] results = new double[8];
		
		double v,sum=0,sqsum=0,variance=0,mean=0, median=0,coeffvar=0;
		int n=0,mvc=0,maxat=0,minat=0;
		
		for (int i=0;i<results.length;i++){
			results[i] = -1;
		}

		if (values.size()<=1){
			return results;
		}
		
		ArrayList<Double> dvalues = new ArrayList<Double> () ;
		
		String cn = values.get(0).getClass().getSimpleName().toLowerCase();
		if (cn.startsWith("double")){
			dvalues.addAll((Collection<? extends Double>) values);
		}
		if (cn.startsWith("float")){
			for (int i=0;i<values.size();i++){
				dvalues.set(i, (Double) values.get(i));
			}
		}
		if (cn.startsWith("int")){
			double dv =0.0;
			
			for (int i=0;i<values.size();i++){
				dv = (1.0*(int)((Integer)values.get(i)));
				if (i>=dvalues.size()){
					dvalues.add(dv);
				}else{
					dvalues.set(i, dv);
				}
			}
		}
			
		double _min=99999999.09, _max=-99999999999.09, psum=0, qpsum=0;
		
		Collections.sort( dvalues );
		
		for (int i=0;i<dvalues.size();i++){
			v = dvalues.get(i);
			if (v != -1) {
				if (_min > v) {
					_min = v;
					minat=i;
				}
				if (_max < v) {
					_max = v;
					maxat=i;
				}
				psum = psum + v;
				qpsum = qpsum + 1.0* (v * v);
				n++;
				if (n - ((dvalues.size()+mvc)/2)<=1){
					median=v;
				}
			}else{
				mvc++;
			}
		}
		if (n > 0) {
			variance = qpsum / (double)n - ((double)psum / (double)n) * ((double)psum / (double)n);
			mean = psum / (double)n;
			if (mean != 0.0) coeffvar = variance / (double)mean;

			results[0] = _min;
			results[1] = _max;
			results[2] = mean;
			results[3] = variance;
			results[4] = coeffvar;
			results[5] = median;

			results[6] = minat;
			results[7] = maxat;
		}
		return results;
	}

	public static double informationMeasureOnDistribution(int[] frequencies) {
		
		double[] dvalues = new double[frequencies.length] ; 
		double infoVal = -1.0, v,_min,_max;
		
		if ((frequencies==null) || (frequencies.length<=1)){
			return infoVal;
		}
		_max=-999999.09; _min= 999999.09;
		for (int i=0;i<frequencies.length;i++){
			v = frequencies[i];
			if (v!=-1.0){
				if (v>_max)_max=v;
				if (v<_min)_min=v;
			}
		}
		
			for (int i=0;i<frequencies.length;i++){
				v = frequencies[i];
				if ((_max-_min)!=0.0){
					v = (v-_min)/(_max-_min);
				}else{
					v=0.0;
				}
				dvalues[i] = v;
			}
		
		infoVal = 0.0;
		
		for (int i=1;i<dvalues.length;i++){
			v = dvalues[i]-dvalues[i-1];
			if ((v!=0) && (!(dvalues[i]==0.0) && (dvalues[i-1]==0))){
				v = -v * Math.log(v) ;
				infoVal = infoVal + v;
			}
		}
		
		return infoVal;
	}
	
}

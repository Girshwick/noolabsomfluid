package org.NooLab.somtransform.algo;

import java.util.ArrayList;

import org.NooLab.utilities.ArrUtilities;

public class MinPositions {

	double[] values = new double[0];
	
	ArrUtilities arrutil = new ArrUtilities();
	
	// ========================================================================
	public MinPositions(){
		
	}
	// ========================================================================
	
	public MinPositions setData( int[] ivalues){
		values = new double[ivalues.length];
		for (int i=0;i<ivalues.length;i++){
			values[i] = (double)ivalues[i] ;
		}
		return this;
	}
	public MinPositions setData( double[] dvalues){
		/*
		values = new double[dvalues.length];
		for (int i=0;i<dvalues.length;i++){
			values[i] = dvalues[i] ;
		}
		*/
		values = new double[dvalues.length];
		System.arraycopy(dvalues, 0, values, 0, dvalues.length) ;
		return this;
	}
	
	
	/**
	 * 
	 * 
	 * @param contrastByMean the denominator of the fraction of the mean, which then serves as contrast criterion; 
	 *                       sth elese would be a fraction of stdev
	 * @param windowSize within window size, the max of the window should be in its "middle" part: this excludes 
	 *                   monotic increasing/decreasing functions which produce min/max at the borders of the moving window  
	 * @param maxN       the maximum of modi that we allow
	 * @return
	 */
	public int[] identifyModi( double contrastByMean, int windowSize, int maxN) {
		int p,leftWindowPos=0;
		double v1,v2 ; 
		int[] modi = new int[0];
		boolean leftOK=false;
		
		// index of maximum within window
		int winMaxPosIndex = -1; 
		
		// the arlist absolute position of the next max in _freq
		// except: it is larger, and if it is larger, the previous must be swept out
		int earliestNextMaxIndex = -1; 
		
		ArrayList<Integer> modiList = new ArrayList<Integer>();
		ArrayList<Double> window = new ArrayList<Double>();  
		
		double[] _vals = new double[values.length] ;
		
		System.arraycopy(values, 0, _vals, 0, values.length) ;
		
		double _mean  = arrutil.arraySum( _vals)/(_vals.length) ;
		double contrast = _mean * contrastByMean ; 
		 
		for (int i=0;i<_vals.length;i++){
if (i>=89){
	// 89..95
	int k;
	k=0;
}
			window.add( (double)_vals[i]) ;
			if (window.size()>windowSize){ 
				window.remove(0);
				leftWindowPos++;
				if (winMaxPosIndex==0){
					// determine new max
				}else{
					winMaxPosIndex-- ;
					
				}
			}
			
			if (i>=windowSize-1){
				leftOK = true;
			} else{ continue; }

			p = arrutil.arrayMaxPos(window) ;
			if ((p>=0) && (window.get(p)<=0)){
				p=-1; // indicates, that there is no max available
				earliestNextMaxIndex = -1;
			}
			// arrayMax( window, -1) ;
			if (winMaxPosIndex<0){
				winMaxPosIndex = p;
				earliestNextMaxIndex = p+leftWindowPos+windowSize-1; 
				modiList.add( p+leftWindowPos );
			}
			if ((p>=0) && (p!=winMaxPosIndex) && (p-winMaxPosIndex<windowSize)){
				v2=0.0;
				v1 = window.get(p);
				if (winMaxPosIndex>=0){
					v2 = window.get(winMaxPosIndex);
					if (p+leftWindowPos < modiList.get(modiList.size()-1)+windowSize){
						v2 = _vals[ modiList.get(modiList.size()-1) ]; //
					}
				}
				if (v1>v2+contrast){
					winMaxPosIndex = p;
					if ((leftWindowPos+p - modiList.get(modiList.size()-1) >windowSize) || (earliestNextMaxIndex<0)){
						modiList.add( p+leftWindowPos );
						earliestNextMaxIndex = p+leftWindowPos+windowSize-1; 
					}else{
						modiList.set(modiList.size()-1, leftWindowPos+p);	
					}
					
				}
			}
			 
		} // i-> _freq 
		
		// correction for a seies of absolutely identical values
		// such that the modus is in the mid of such an area
		
		
		
		// 
		if (modiList.size()>0){
			modi = new int[modiList.size()];
			for (int i=0;i<modiList.size();i++){
				modi[i] = modiList.get(i);
			}
		}
		return modi;
	}
	
	/**
	 * 
	 * returns the positions of the minima interpreted as inverse modi
	 * 
	 * @param contrastByMean
	 * @param windowSize
	 * @param maxN
	 * @return
	 */
	public int[] identifyMindi( double contrastByMean, int windowSize, int maxN) {
		int[] mindi = new int[0];
		double avgRaw= -1.0, dv,nmin,nmax;
		// strategy: flip vertically at the average line of the values,
		// and apply modi search
		
		avgRaw = arrutil.arraySum( values)/(values.length) ;
		
		double[] _vals = new double[values.length] ;
		System.arraycopy(values, 0, _vals, 0, values.length) ;

		nmin = 999999999999.09;
		nmax = -999999999999.09;
		for (int i=0;i<values.length;i++){
			dv = _vals[i] - avgRaw;
			if (dv<0){
				_vals[i] = avgRaw + Math.abs( dv ) ;
			}else{
				_vals[i] = avgRaw - Math.abs( dv ) ;
			}
			if (nmin>_vals[i]){
				nmin=_vals[i];
			}
			if (nmin>_vals[i]){
				nmin=_vals[i];
			}
		} // i->
		nmin=nmin-0.001;
		nmax=nmax+0.001;
		for (int i=0;i<_vals.length;i++){
			_vals[i] = (_vals[i]-nmin)/(nmax-nmin);  
		}
		
		mindi = identifyModi( contrastByMean, windowSize, maxN) ;
		
		return mindi;
	}
}
















package org.NooLab.repulsive.components.infra;

import java.util.Comparator;

import org.NooLab.repulsive.components.Coordinate2D;

public class C2DComparable implements Comparator<Coordinate2D>{

	@Override
	public int compare( Coordinate2D c1, Coordinate2D c2 ) {
		int result=0;
		int compareMode = c1.getCompareMode() ;
		
		if (compareMode==Coordinate2D._COMPARE_X){
			if (c1.cxValue > c2.cxValue)result =  1;
			if (c1.cxValue == c2.cxValue)result = 0;
			if (c1.cxValue < c2.cxValue)result = -1;
		}
		if (compareMode==Coordinate2D._COMPARE_Y){
			if (c1.cyValue > c2.cyValue)result =  1;
			if (c1.cyValue == c2.cyValue)result = 0;
			if (c1.cxValue < c2.cyValue)result = -1;
		}
		
		if (compareMode==Coordinate2D._COMPARE_D){
			double c1_TempDistance = -1.0, c2_TempDistance = -1.0 ;
			
			// get d values by active hash
			
			c1_TempDistance = c1.getDistanceValue(c1.activeHash);
			c2_TempDistance = c2.getDistanceValue(c2.activeHash);
			if ((c2_TempDistance==-1.0) || (c2_TempDistance==-1.0)){
				result=0;
			}else{
				if (c1_TempDistance > c2_TempDistance)result =  1;
				if (c1_TempDistance == c2_TempDistance)result = 0;
				if (c1_TempDistance < c2_TempDistance)result = -1;
			}
		}
		return result;
	}

}

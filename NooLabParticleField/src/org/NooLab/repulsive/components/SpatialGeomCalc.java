package org.NooLab.repulsive.components;


 

public class SpatialGeomCalc {

	int width, height;
	
	int borderMode = ParticleGrid.__BORDER_ALL ;
	
	
	
	public SpatialGeomCalc( int width, int height,int borders ){
		this.width  = width ;
		this.height = height ;
		borderMode =borders ;
	}
	
	

	public double getLinearDistanceX(double x1, double x2){
		return getLinearDistance(x1, x2, width);
	}
	public double getLinearDistanceY(double y1, double y2){
		return getLinearDistance(y1, y2, height);
	}
	
	
	public double distance( double x1 , double y1, double x2 , double y2){
		
		return -1;
	}
	
	public double getLinearDistance(double x1, double x2, int maxDist) {
		double result = -1.0;
		double xd0, w;
		
		
		
		result = ( x1 - x2) ;
		
		if (borderMode == ParticleGrid.__BORDER_NONE)
		{
			
			
			w = (double)(maxDist*1.0) ;
			if ( Math.abs(result) > w / 2.0) {
				// initial distance larger than 50% of the width of the area?
				// -> so it could be just at the left and the right border ->
				// subtract the area width
				
				xd0 = (w - Math.max(x1,x2) + (Math.min(x1,x2))) ;
				if (Math.abs(xd0) < Math.abs(result)){
					result = xd0;
				}
			}
		}
		
		return result;
	}


	public double[] adjustSpatialPositionsToBorderSettings(double xpos, double ypos, double radius, int neighborhoodBorderMode) {
		
		double[] xyPos = new double[2] ;

		if (neighborhoodBorderMode == ParticleGrid.__BORDER_NONE) {
			if (xpos > width ) {    
				xpos = xpos - width; // screen wrap
			} else {
				if (xpos < 0) {
					xpos = xpos + width;
				}
			}
			if (ypos > height ) {
				ypos = ypos - height;
			} else {
				if (ypos < 0) {
					ypos = ypos + height;
				}
			}
			double rf=1.6;
			
			// if (xpos<=1)xpos=1;
			if (xpos<=(radius/(rf*1.1)))xpos=(radius/(rf*1.1));
			if (xpos>=width-(radius/rf))xpos=width-(radius/rf);

			//if (ypos<=1)ypos=1;
			if (ypos<=(radius/(rf*1.1)))ypos=(radius/(rf*1.1));
			if (ypos>=height-(radius/rf))ypos=height-(radius/rf);
		} // __BORDER_NONE ?
			
		xyPos[0] = xpos ;
		xyPos[1] = ypos ;
		
		return xyPos;
	}

}

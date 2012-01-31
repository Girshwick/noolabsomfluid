package org.NooLab.repulsive.components.topology.geom;

import java.awt.geom.Point2D;


/**
 * 
 * this is needed to get the implicitly activated nodes in a SOM when
 * traveling between two distant nodes on a straight line
 * 
 * 
 * 
 */
public class DistancePointToLine {

	public static double distanceToSegment( double x3, double y3, double x1,
											double y1, double x2, double y2) {
		final Point2D p3 = new Point2D.Double(x3, y3);
		final Point2D p1 = new Point2D.Double(x1, y1);
		final Point2D p2 = new Point2D.Double(x2, y2);
		return distanceToSegment(p1, p2, p3);
	}

	/**
	 * Returns the distance of p3 to the segment defined by p1,p2;
	 * 
	 * @param p1
	 *            First point of the segment
	 * @param p2
	 *            Second point of the segment
	 * @param p3
	 *            Point to which we want to know the distance of the segment
	 *            defined by p1,p2
	 * @return The distance of p3 to the segment defined by p1,p2
	 */
	public static double distanceToSegment(Point2D p1, Point2D p2, Point2D p3) {

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();

		if ((xDelta == 0) && (yDelta == 0)) {
			throw new IllegalArgumentException( "p1 and p2 cannot be the same point");
		}

		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0) {
			closestPoint = p1;
		} else if (u > 1) {
			closestPoint = p2;
		} else {
			closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return closestPoint.distance(p3);
	}

	 
	
	@SuppressWarnings("unused")
	private static void test(String[] args) {
		// Test example
		System.out.println(String.format(
				"Distance from 5,5 to (10,10)-(20,20): %f",
				distanceToSegment(5, 5, 10, 10, 20, 20)));
		System.out.println(String.format(
				"Distance from 15,15 to (10,10)-(20,20): %f",
				distanceToSegment(15, 15, 10, 10, 20, 20)));
		System.out.println(String.format(
				"Distance from 15,15 to (20,10)-(20,20): %f",
				distanceToSegment(15, 15, 20, 10, 20, 20)));
		System.out.println(String.format(
				"Distance from 0,15 to (20,10)-(20,20): %f",
				distanceToSegment(0, 15, 20, 10, 20, 20)));
		System.out.println(String.format(
				"Distance from 0,25 to (20,10)-(20,20): %f",
				distanceToSegment(0, 25, 20, 10, 20, 20)));
		System.out.println(String.format(
				"Distance from -13,-25 to (-50,10)-(20,20): %f",
				distanceToSegment(-13, -25, -50, 10, 20, 20)));

		// Should give:
		// Distance from 5,5 to (10,10)-(20,20): 7.071068
		// Distance from 15,15 to (10,10)-(20,20): 0.000000
		// Distance from 15,15 to (20,10)-(20,20): 5.000000
		// Distance from 0,15 to (20,10)-(20,20): 20.000000
		// Distance from 0,25 to (20,10)-(20,20): 20.615528
		// Distance from -13,-25 to (-50,10)-(20,20): 39.880822
	}

}

/*

function PntToSegmentDistance(Px, Py, x1, y1, x2, y2: Double): Double;
var 
  Ratio: Double;
  Dx: Double;
  Dy: Double;
begin
  if IsEqual(x1, x2) and IsEqual(y1, y2) then
  begin
    Result := Distance(Px, Py, x1, y1);
  end
  else
  begin
    Dx    := x2 - x1;
    Dy    := y2 - y1;
    Ratio := ((Px - x1) * Dx + (Py - y1) * Dy) / (Dx * Dx + Dy * Dy);
    if Ratio < 0 then Result := Distance(Px, Py, x1, y1)
    else if Ratio > 1 then Result := Distance(Px, Py, x2, y2)
    else
      Result := Distance(Px, Py, (1 - Ratio) * x1 + Ratio * x2,
        (1 - Ratio) * y1 + Ratio * y2);
  end;
end;
*/
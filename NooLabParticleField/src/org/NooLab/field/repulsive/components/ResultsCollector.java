package org.NooLab.field.repulsive.components;

import java.util.HashMap;
import java.util.Map;

import org.NooLab.field.repulsive.components.data.SurroundResults;




/** 
 * each single request to the RepulsionField returns a result object, that will be delivered in this event sink;
 * in order to make the process of digesting the results more convenient, we additionally
 * may use a ResultCollecting object;
 * this ResultCollecting object can be queried later, and in an asynchronous manner;
 * it also offers an Event-map, that is, for each kind of request, one can define a key, such that
 * a particular class is triggered and the key is transmitted
 * 
 * 
 * the ResultsCollector also offers "Futures", which are methods that wrap a class
 * which waits until he results for a particularly issued gui are returned.
 * 
 * 
 * 
 */

public class ResultsCollector {

	Map<Integer, Object> pointsOfDelivery = new HashMap<Integer, Object>();   
	
	public ResultsCollector(){
		
	}
	
	public void takeResultsPackage( SurroundResults sr){
		
	}
	
	
}

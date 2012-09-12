package org.NooLab.somfluid.core.categories.connex;

import java.util.ArrayList;

import org.NooLab.somfluid.core.nodes.InternalSomMessageIntf;
import org.NooLab.somfluid.data.DataHandlingPropertiesIntf;
 

/**
 * 
 * This interface provides events that flow top-down, from the level of the
 * network to the level of (meta-) nodes
 * 
 * it provides the event structures for  
 * - direct commands, like data uptake, data deletion, memory reset, 
 * - electrical waves, chemical stimuli, activity parameters like "intensity" 
 * - activity triggers, polarity of transfer : + -> +, + -> - , - -> +, - -> -
 * 
 * Note that the propagation of the information about the weight vector 
 * (absolute or relative) is *also* subject to the inter-node communication !!!
 * 
 * 
 * remember:
 * Nodes do not send data, only informaton about weight vectors
 * 
 */
public interface NetworkMessageIntf extends InternalSomMessageIntf{

	
	/** The node receives a message that is comprised by a reference to a data object and
	 *  a suitable class to read these data = to extract the variables and the records (1+);
	 *  on acceptance, the return value = 0, if the node denies acceptance, the return value = -3;
	 *  on error conditions return value -7 */
	public void onSendingDataObject( Object data, DataHandlingPropertiesIntf datahandler);

	/** important part of SOM mechanism */
	public void onRequestForAdaptingWeightVector(Object obj, Object params);

	/** trigger a recalculation to be sure */
	public void onRequestForDedicatedUpdate();

	
	/** if the data index list is null or empty, all data will be removed */
	public void onRequestForDataRemoval( ArrayList<Long> dataIndex );
	public void onRequestForDataRemoval( );
	
	/**  the node forgets everything, including its weight vector */
	public void onRequestForMemoryReset();

	
	/** during startup  */
	public void onRequestForRandomInit(Object obj);

	/** during startup  */
	public void onDefiningFeatureSet(Object obj1, DataHandlingPropertiesIntf obj2);
	
	/** during startup  */
	public void onDefiningTargetVar(Object obj1);


	// the following influences are not specified so far
	
	public void onArrivalOfChemicalStimulus();
	
	public void onRequestForChangingActivityLevel();

}

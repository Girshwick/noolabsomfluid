package org.NooLab.somfluid.env.communication;

import java.util.Vector;

import org.NooLab.compare.utilities.math.DistanceControlProperties;
import org.NooLab.glue.common.DataContainer;
import org.NooLab.glue.components.MessageBoardFactoryProperties;
import org.NooLab.glue.components.pkg.TaskPackage;
import org.NooLab.glue.instances.ParticipantFactory;
import org.NooLab.glue.instances.ParticipantReceptorIntf;
import org.NooLab.glue.instances.SubscribersIntf;
import org.NooLab.glue.subscription.Future;
import org.NooLab.glue.subscription.FutureIntf;
import org.NooLab.glue.subscription.FuturesIntf;
import org.NooLab.glue.subscription.context.Context;
import org.NooLab.glue.subscription.context.ContextInfra;
import org.NooLab.glue.subscription.context.ContextIntf;
import org.NooLab.glue.subscription.publisher.SubscriptionPublisher;
import org.NooLab.glue.transaction.Transaction;
import org.NooLab.somfluid.SomFluidProperties;
import org.NooLab.utilities.logging.PrintLog;




public class GlueClientAdaptor {

	public static final int _GLUEX_SOURCE   = 1;
	public static final int _GLUEX_RECEPTOR = 2;
	public static final int _GLUEX_DUAL     = 3;
	
	int glueType=0;
	SomFluidProperties sfProperties;
	
	GlueReceptorInstance glueClient ;
	GlueBindings glueBindings ;
	
	
	public GlueClientAdaptor( GlueBindings binding, SomFluidProperties sfProps ){
		sfProperties = sfProps;
		glueBindings = binding ;
	}
	
	public void start(){
		glueClient = new GlueReceptorInstance( glueBindings ); 
		// TODO: we also have sources, and duals
		 
		if (glueClient.connected==false){
			while (glueClient.connected==false){
				glueClient.out.delay(100) ;
			}
		}
		 
	}
	
	
}

 
 
  
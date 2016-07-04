package org.spiderplan.executor;

import org.spiderplan.representation.ConstraintDatabase;

/**
 * Proxy for classes that connect to middlewares, execute reactors, etc.
 * 
 * @author Uwe Köckemann
 */
public interface CommunicationProxy {	
	/**
	 * Initialize communication proxy.
	 * 
	 * @param cdb {@link ConstraintDatabase} that may contain information required to setup
	 * communication.
	 */
	public void initialize( ConstraintDatabase cdb );
	
	/**
	 * Communicate with some outside source (e.g., a middleware). 
	 * @param t Current execution time (in ms)
	 * @param cdb  Current context of execution. Context may be extended or communicated during update.
	 * 
	 * @return A possibly modified version of <code>cdb</code>. 
	 */
	public ConstraintDatabase update( long t, ConstraintDatabase cdb );
	
}

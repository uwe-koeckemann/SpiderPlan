/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.spiderplan.executor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.spiderplan.modules.solvers.Module;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.tools.logging.Logger;

/**
 * Interface for a manager that represents connection to
 * some external software (implemented, e.g., by ROSExecutionManager).
 * 
 * This should also manage all relevant reactors (e.g., ROSGoal, ROSPublish, ...).
 * 
 * @author Uwe Köckemann
 *
 */
public abstract class ExecutionManager {
	protected String name;
//	protected ConfigurationManager cM;
	
//	protected ArrayList<ParameterDescription> parameterDesc = new ArrayList<ParameterDescription>();

	protected boolean keepTimes = false;
	protected boolean keepStats = false;
	protected boolean verbose = false;
	protected int verbosity = 0;
	
	private Map<Statement,Collection<Expression>> addedConstraints = new HashMap<Statement, Collection<Expression>>();
		
	protected List<Reactor> reactors = new ArrayList<Reactor>();
	protected ArrayList<Statement> hasReactorList = new ArrayList<Statement>();
	protected ArrayList<Statement> doneList = new ArrayList<Statement>();
	protected ArrayList<Statement> execList = new ArrayList<Statement>();
	protected ArrayList<Statement> startedList = new ArrayList<Statement>();
	
	/**
	 * Initialize execution manager with a name
	 * @param name
	 */
	public ExecutionManager( String name ) {
		this.name = name;
	}
	
	/**
	 * Initialize this execution manager by providing a CDB.
	 * @param cdb a constraint database containing all relevant information
	 */
	public abstract void initialize( ConstraintDatabase cdb );
	
	
	/************************************************************************************************
	 * Receive new information, send information to external programs.
	 * 
	 * Update all reactors. Usually overwritten in sub-classes and called
	 * from overriding method in case generic Reactor update is sufficient.
	 * Otherwise sub-classes are expected to take care of their reactor updates
	 * themselves.
	 * 
	 * @param t 
	 * @param execDB 
	 * @return <code>true</code> if a reactor finished during the update, <code>false</code> otherwise
	 ************************************************************************************************/
	public boolean update( long t, ConstraintDatabase execDB ) {
		ValueLookup propagatedTimes = execDB.getUnique(ValueLookup.class);
		ArrayList<Reactor> remList = new ArrayList<Reactor>();
		boolean someoneDone = false;
		for ( Reactor r : reactors ) {
			
			r.printSetting(name, Logger.depth, verbose);
			if ( r.getState() == Reactor.State.Done ) {
				doneList.add(r.getTarget());
				remList.add(r);
				someoneDone = true;
				
			} else {
				Collection<Expression> addedCons;
				long EST, LST, EET, LET;

				EST = propagatedTimes.getEST(r.getTarget().getKey());
				LST = propagatedTimes.getLST(r.getTarget().getKey());
				EET = propagatedTimes.getEET(r.getTarget().getKey());
				LET = propagatedTimes.getLET(r.getTarget().getKey());
				
				if ( verbose ) Logger.msg(getName(), "@t=" + t + " (BEFORE) >>> " + r, 1);
				addedCons = r.update(t, EST, LST, EET, LET, execDB);
				if ( verbose ) Logger.msg(getName(), "@t=" + t + " (AFTER)  >>> " + r, 1);
				
				if ( !r.getState().equals(Reactor.State.NotStarted) && !startedList.contains(r.getTarget()) ) {
					startedList.add(r.getTarget());
				}
				
				Collection<Expression> store = new ArrayList<Expression>();
				store.addAll(addedCons);
				addedConstraints.put(r.getTarget(), store);
				
			}
		}
		reactors.removeAll(remList);
		
		return someoneDone;
	}
	
	/**
	 * Returns the name of this {@link Module}
	 * @return The name of this {@link Module}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Convenient for using Module's name for some prints 
	 * @return
	 */
	protected String msg( String s ) {
		return "["+name+"] "+s;
	}
	
	/**
	 * Turn logging on/off
	 * @param verbose 
	 */
	public void setVerbose( boolean verbose ) {
		this.verbose = verbose;
	}
	
	/**
	 * Set level of detail of log messages
	 * @param verbosity
	 */
	public void setVerbosity( int verbosity) {
		this.verbosity = verbosity;
	}
		 
//	/**
//	 * Get all reactors currently used by this manager.
//	 * @return collection of reactors
//	 */
//	public abstract Collection<Reactor> getReactors();

}

/*******************************************************************************
 * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
 *  
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package org.spiderplan.modules.solvers;

import java.util.ArrayList;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.configuration.ParameterDescription;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.tools.logging.Logger;

/**
 * Represents an iterator over resolvers. To address
 * a flaw solvers will provide an implementation of this
 * class that will provide one resolver each time next()
 * is called. 
 * 
 * @author Uwe Köckemann
 */
public abstract class ResolverIterator {

	protected String name;
	protected ConfigurationManager cM;
	
//	protected ArrayList<String> constraintTypes = new ArrayList<String>();
	protected ArrayList<ParameterDescription> parameterDesc = new ArrayList<ParameterDescription>();
	protected boolean staticModule = false;
	
	
	protected boolean keepStats = false;
	protected boolean keepTimes = false;
	protected boolean verbose = false;
	protected int verbosity = 0;
	
	protected static boolean killFlag = false;
	
	/**
	 * Create a new {@link ResolverIterator} given its name and a {@link ConfigurationManager}. 
	 * @param name Name of the new iterator
	 * @param cM A {@link ConfigurationManager}
	 */
	public ResolverIterator( String name, ConfigurationManager cM ) {
		this.name = name;
		this.cM = cM;
		
		parameterDesc.add( new ParameterDescription("keepTimes", "boolean", "false", "Switch on/off recording of times.") );
		parameterDesc.add( new ParameterDescription("keepStatistics", "boolean", "true", "Switch on/off recording of module statistics.") );
		parameterDesc.add( new ParameterDescription("verbose", "boolean", "false", "Switch on/off verbose mode.") );
		parameterDesc.add( new ParameterDescription("verbosity", "int", "0", "Only messages m with smaller or equal importance p are printed using print(m,p) method).") );
		parameterDesc.add( new ParameterDescription("static", "boolean", "false", "Static modules will have only one instance during runtime.") );		
		
		if ( cM.hasAttribute(name, "keepStatistics")  ) {
			this.keepStats = cM.getBoolean(name, "keepStatistics");
		}
		if ( cM.hasAttribute(name, "verbose")  ) {
			this.verbose = cM.getBoolean(name, "verbose");
		}
		if ( cM.hasAttribute(name, "verbosity")  ) {
			this.verbosity = cM.getInt(name, "verbosity");
		}
		if ( cM.hasAttribute(name, "keepTimes")  ) {
			this.keepTimes = cM.getBoolean(name, "keepTimes");
		}	
		if ( cM.hasAttribute(name, "static")  ) {
			this.staticModule = cM.getBoolean(name, "static");
		}
		
		if ( this.verbose ) {
			Logger.registerSource(name, this.verbosity);
		}
	}
	
	/**
	 * Return next {@link Resolver} that resolves flaws associated 
	 * to the {@link Module}. Calling this is identical to calling <code>next(null)</code>.
	 * @return A {@link Resolver} that removes (a set of) flaw
	 */
	public Resolver next() {
		return this.next(null);
	}
		
	/**
	 * Return next {@link Resolver} that resolved the flaw associated 
	 * to this instance.
	 * @param C A collection of additional {@link Expression}s that need to be considered 
	 * or <code>null</code>.
	 * @return A {@link Resolver} that removes a flaw.
	 */	
	public abstract Resolver next( ConstraintDatabase C );
		
	/**
	 * Returns the name of this {@link ResolverIterator}
	 * @return The name of this {@link ResolverIterator}
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Test if all non-optional parameters are present in configuration.
	 * @param cM
	 */
	public void testParameters( ConfigurationManager cM ) {
		for ( ParameterDescription pD : this.parameterDesc ) {
			if ( !pD.isOptional() ) {
				if ( !cM.hasAttribute(this.getName(), pD.getName())  ) {
					throw new IllegalStateException("Module " + this.getName() + " is missing non-optional parameter " + pD.getName() + "\n ->" + pD);
				}
			}
		}
	}
	
	/**
	 * Convenient for using Module's name for some prints 
	 * @return
	 */
	protected String msg( String s ) {
		return "["+name+"] "+s;
	}
	/**
	 * Check if this {@link ResolverIterator} supports a specific option set.
	 * @param oName Name of the option.
	 * @return <code>true</code> if option is supported, <code>false</code> otherwise.
	 */
	public boolean hasOption( String oName ) {
		for ( ParameterDescription o : parameterDesc ) {
			if ( o.getName().equals(oName) ) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Get all supported options
	 * @return A Collection of {@link ParameterDescription}s
	 */
	public ArrayList<ParameterDescription> getOptions() {
		return parameterDesc;
	}
	
	
	/**
	 * Print message if {@link Module} is verbose and has given verbosity level
	 * @param s Message string
	 * @param level Required verbosity level to print this message
	 */
	public void print( String s, int level ) {
		if ( this.verbose ) {
			Logger.msg(getName(), s, level); 
		}
	}
	
	/**
	 * Setting this static flag to true every module 
	 * will return right after run() is
	 * invoked and set "Killed" as output signal.
	 * @param killFlag
	 */
	public static void setKillFlag( boolean killFlag ) {
		Module.killFlag = killFlag;
	}
	/**
	 * Check if the (static) killFlag is set (setting the killFlag
	 * will make any {@link Module} return immediately when called. 
	 * @return <code>true</code> if killFlag is set, <code>false</code> otherwise.
	 */
	public static boolean getKillFlag() {
		return killFlag;
	}
}

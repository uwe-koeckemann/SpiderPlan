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
package org.spiderplan.modules;

import java.util.Collection;
import java.util.HashMap;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.constraints.Cost;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;
import org.spiderplan.tools.statistics.Statistics;

/**
 * Simple evaluation of {@link Cost} constraints.
 * 
 * @author Uwe Köckemann
 */
public class CostSolver extends Module implements SolverInterface {
		
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public CostSolver( String name, ConfigurationManager cM ) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		core.setResultingState(getName(), this.testAndResolve(core).getState());
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {

//		this(Relation.valueOf(relForm.name()), relForm, relForm.getTerms()[1]);
		
		Collection<Cost> costCons = core.getContext().get(Cost.class);
		HashMap<Term,Double> costs = new HashMap<Term,Double>();
		
		Term costSymbol;
		Term valueSymbol;
				
		if ( verbose ) Logger.msg(getName(), "Calculating... ", 1);
		
		for ( Cost cost : costCons ) {
			Atomic relation = cost.getRelation();
			String operator = cost.getRelation().name();
			
			if ( operator.equals("add") 
			  || operator.equals("sub") ) {
				
				if ( verbose ) Logger.msg(getName(), "    " + cost, 1);
				
				costSymbol = relation.getArg(0);
				valueSymbol = relation.getArg(1);
				
				double addedValue = 0.0;
				
				if ( valueSymbol.isConstant() ) {
					try  {  
						Integer v = Integer.parseInt( valueSymbol.toString() );  
						addedValue = v.doubleValue();  
					} catch( Exception eNotInt ) {  
						try  {
							Double v = Double.parseDouble( valueSymbol.toString() );  
							addedValue = v.doubleValue();
						} catch( Exception eNotDouble ) {
							throw new IllegalStateException("Value term "+valueSymbol+" in cost constraint " + cost + " is a constant but not parsable as double or int.");
						}
			        }  
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
				}
				
				Double currentValue = costs.get(costSymbol);
				if ( currentValue == null ) {
					currentValue = 0.0;
					costs.put(costSymbol, currentValue);
				}
				
				double modifier = 1.0;
				if ( relation.equals("sub") ) {
					modifier = -1.0;
				}
					
				currentValue +=	modifier * addedValue;
				
				costs.put(costSymbol, currentValue);
			}
		}
		
		if ( keepStats ) {
			for ( Term costVar : costs.keySet() ) {
				Statistics.setDouble(msg(costVar.toString()), costs.get(costVar));
			}
		}
		
		if ( verbose ) {
			Logger.msg(getName(),"Resulting values:", 1);
			for ( Term key : costs.keySet() ) {
				Logger.msg(getName(),"    " + key + " = " + costs.get(key), 1);
			}
		}

		boolean atLeastOneViolation = false;

		if ( verbose ) Logger.msg(getName(), "Checking inequalities... ", 1);
				
		for ( Cost  cost : core.getContext().get(Cost.class) ) {
			Atomic relation = cost.getRelation();
			String operator = cost.getRelation().name();
			
			if ( operator.equals("less-than")  
			||   operator.equals("less-than-or-equals")
			||   operator.equals("greater-than")
			||   operator.equals("greater-than-or-equals") ) {
									
				if ( verbose ) Logger.msg(getName(), "    " + cost, 1);
						
				costSymbol = relation.getArg(0);
				valueSymbol = relation.getArg(1);
				
				boolean ignored = false;
				
				Double costValueEntry = costs.get(costSymbol);
				double costValue = 0.0;
				if ( costValueEntry != null ) {
					costValue = costValueEntry;
				}
				
				double compareValue = 0.0;
				
				if ( valueSymbol.isConstant() ) {
					try  {  
						Integer v = Integer.parseInt( valueSymbol.toString() );  
						compareValue = v.doubleValue();  
					} catch( Exception eNotInt ) {  
						try  {
							Double v = Double.parseDouble( valueSymbol.toString() );  
							compareValue = v.doubleValue();
						} catch( Exception eNotDouble ) {
							throw new IllegalStateException("Value term "+valueSymbol+" in cost constraint " + cost + " is a constant but not parsable as double or int.");
						}
			        }  
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
					ignored = true;
				}
				
				if ( !ignored ) {
					if ( operator.equals("less-than") ) { 
						atLeastOneViolation = !(costValue < compareValue);
					} else if ( operator.equals("less-than-or-equals") ) { 
						atLeastOneViolation = !(costValue <= compareValue);
					} else if ( operator.equals("greater-than") ) { 
						atLeastOneViolation = !(costValue > compareValue);
					} else if ( operator.equals("greater-than-or-equals") ) { 
						atLeastOneViolation = !(costValue >= compareValue);
					}
					if ( atLeastOneViolation ) {
						if ( verbose ) Logger.msg(getName(), "    fail!", 1);
						break;
					}
				}
			}
		}
		
		SolverResult result;
		
		if ( !atLeastOneViolation ) {
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			result = new SolverResult(State.Consistent);
		} else {
			core.setResultingState(this.getName(), State.Inconsistent);
			result = new SolverResult(State.Inconsistent);
		}
		
		return result;
	}

}

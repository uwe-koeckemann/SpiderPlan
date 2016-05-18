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
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.SingleResolver;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ExpressionTypes.CostRelation;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.cost.Cost;
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
		
		ValueLookup valueLookup = core.getContext().getUnique(ValueLookup.class);
		
		Collection<Cost> costCons = core.getContext().get(Cost.class);
		HashMap<Term,Double> costs = new HashMap<Term,Double>();
		
		Term costSymbol;
		Term valueSymbol;
				
		if ( verbose ) Logger.msg(getName(), "Calculating... ", 1);
		
		for ( Cost cost : costCons ) {
			Atomic relation = cost.getConstraint();
				
			if ( cost.getRelation().equals(CostRelation.Add)
				|| cost.getRelation().equals(CostRelation.Sub) ) {
				
				if ( verbose ) Logger.msg(getName(), "    " + cost, 1);
				
				costSymbol = relation.getArg(0);
				valueSymbol = relation.getArg(1);
				
				Double addedValueDouble = null;
				Long addedValueLong = null;
				
				if ( !valueSymbol.isVariable() ) {
					try  {  
						addedValueLong = Long.parseLong( valueSymbol.toString() ); 
					} catch( Exception e ) { }
					try  {
						addedValueDouble = Double.parseDouble( valueSymbol.toString() );  
					} catch( Exception e ) { }
					if ( addedValueLong == null ) {
						if ( valueLookup.hasIntVariable(valueSymbol) ) {
							addedValueLong = valueLookup.getInt(valueSymbol);
						}
					}
					if ( addedValueDouble == null ) {
						if ( valueLookup.hasFloatVariable(valueSymbol) ) {
							addedValueDouble = valueLookup.getFloat(valueSymbol);
						}
					}						
					
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
				}
				
//				if ( valueSymbol.isConstant() ) {
//					try  {  
//						Integer v = Integer.parseInt( valueSymbol.toString() );  
//						addedValue = v.doubleValue();  
//					} catch( Exception eNotInt ) {  
//						try  {
//							Double v = Double.parseDouble( valueSymbol.toString() );  
//							addedValue = v.doubleValue();
//						} catch( Exception eNotDouble ) {
//							throw new IllegalStateException("Value term "+valueSymbol+" in cost constraint " + cost + " is a constant but not parsable as double or int.");
//						}
//			        }  
//				} else {
//					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
//				}
//				System.out.println("=========================");
//				System.out.println(valueLookup);
//				System.out.println(valueSymbol);
//				System.out.println(addedValueLong);
//				System.out.println(addedValueDouble);
				
				Double currentValue = costs.get(costSymbol);
				if ( currentValue == null ) {
					currentValue = 0.0;
					costs.put(costSymbol, currentValue);
				}
				
				double modifier = 1.0;
				if ( cost.getRelation().equals(CostRelation.Sub) ) {
					modifier = -1.0;
				}
				
				if ( addedValueDouble != null )
					currentValue +=	modifier * addedValueDouble;
				else if ( addedValueLong != null )
					currentValue +=	modifier * addedValueLong;
				
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
			Atomic relation = cost.getConstraint();
			
			if ( cost.getRelation().equals(CostRelation.LessThan) 
			||   cost.getRelation().equals(CostRelation.LessThanOrEquals)
			||   cost.getRelation().equals(CostRelation.GreaterThan)
			||   cost.getRelation().equals(CostRelation.GreaterThanOrEquals) ) {
									
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
					if ( cost.getRelation().equals(CostRelation.LessThan) ) { 
						atLeastOneViolation = !(costValue < compareValue);
					} else if (cost.getRelation().equals(CostRelation.LessThanOrEquals) ) { 
						atLeastOneViolation = !(costValue <= compareValue);
					} else if ( cost.getRelation().equals(CostRelation.GreaterThan) ) { 
						atLeastOneViolation = !(costValue > compareValue);
					} else if ( cost.getRelation().equals(CostRelation.GreaterThanOrEquals) ) { 
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
			ValueLookup vLookup = core.getContext().getUnique(ValueLookup.class);
			if ( vLookup == null ) {
				vLookup = new ValueLookup();
			}
			for ( Term costVar : costs.keySet() ) {
				vLookup.putFloat(costVar, costs.get(costVar));
			}
			
			ConstraintDatabase resCDB = new ConstraintDatabase();
			resCDB.add(vLookup);
			
			Resolver resolver = new Resolver(resCDB);
			SingleResolver singleRes = new SingleResolver(resolver, this.getName(), cM);
			
			
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			result = new SolverResult(State.Consistent, singleRes);
		} else {
			core.setResultingState(this.getName(), State.Inconsistent);
			result = new SolverResult(State.Inconsistent);
		}
		
		return result;
	}

}

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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.ResolverIterator;
import org.spiderplan.modules.solvers.ResolverList;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.expressions.ExpressionTypes.MathRelation;
import org.spiderplan.representation.expressions.math.MathConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.tools.logging.Logger;

/**
 * Handles simple {@link MathConstraint}s
 * 
 * @author Uwe Köckemann
 *
 */
public class MathSolver extends Module implements SolverInterface {
	
	/**
	 * Create new instance by providing name and configuration manager.
	 * @param name The name of this {@link Module}
	 * @param cM A {@link ConfigurationManager}
	 */
	public MathSolver(String name, ConfigurationManager cM) {
		super(name, cM);
	}

	@Override
	public Core run(Core core) {
		if ( killFlag ) {
			core.setResultingState(this.getName(), State.Killed);
			return core;
		}
		if ( verbose ) Logger.depth++;
		
		SolverResult result = this.testAndResolve(core);
		
		if ( result.getState().equals(State.Consistent) ) {
			if ( result.getResolverIterator() != null ) {
				core.getContext().substitute(result.getResolverIterator().next().getSubstitution());
			}
			core.setResultingState(getName(), State.Consistent);
		} else {
			core.setResultingState(getName(), State.Inconsistent);
		}
		
		
		if ( verbose ) Logger.depth--;
		return core;
	}

	@Override
	public SolverResult testAndResolve(Core core) {
		boolean isConsistent = true;
		
		Collection<MathConstraint> C = core.getContext().get(MathConstraint.class);
		
		Substitution theta = new Substitution();
		
		for ( MathConstraint gC : C ) {
			Atomic r = gC.getConstraint().substitute(theta);			
			if ( verbose ) Logger.msg(getName(),"Solving: " + r, 1);
			
			Term a = r.getArg(0);
			Term b = r.getArg(1);
			Term c = r.getArg(2);
			
			boolean integerInput = false, floatInput = false, integerOutput = false, floatOutput = false;
			int aInt = -10000 ,bInt = -10000 ,cInt = -10000;
			double aFloat = -1.0, bFloat = -1.0, cFloat = -1.0;
			
			/**
			 * Checking types:
			 */
			if ( a.isVariable() || b.isVariable() ) {
				if ( verbose ) Logger.msg(getName(),"Skipping because " + r + " has variable arguments.", 1);
				continue;
			} 
			
			try {
				aInt = Integer.valueOf(a.toString()).intValue();
				bInt = Integer.valueOf(b.toString()).intValue();
				integerInput = true;
				floatInput = false;
			} catch ( NumberFormatException e ) {
				integerInput = false;
				
				try {
					aFloat= Double.valueOf(a.toString()).doubleValue();
					bFloat = Double.valueOf(b.toString()).doubleValue();
					floatInput = true;
				} catch ( NumberFormatException e2 ) {
					throw new IllegalStateException("MathConstraint " + r + " needs two integers or two floats as first two arguments.");
				}
			}
			
			if ( !c.isVariable() ) {
				try {
					cInt = Integer.valueOf(c.toString()).intValue();
					integerOutput = true;
					floatOutput = false;
					
					if ( floatInput ) {
						throw new IllegalStateException("MathConstraint " + r + " type missmatch between argument 3 (integer) and arguments 1 and 2 (float).");
					}
				} catch ( NumberFormatException e ) {					
					integerOutput = false;

					try {
						cFloat= Double.valueOf(c.toString()).doubleValue();
						
						if ( integerInput ) {
							throw new IllegalStateException("MathConstraint " + r + " type missmatch between argument 3 (float) and arguments 1 and 2 (integers).");
						}

						floatOutput = true;
					} catch ( NumberFormatException e2 ) {
						throw new IllegalStateException("MathConstraint " + r + " has constant output (arg3) that is neither integer nor float.");
					}

					
				}
			}

			/**
			 * Compute:
			 */			
			int rInt = -10000;
			double rFloat = -1.0;
			
			if ( gC.getRelation().equals(MathRelation.Addition) ) { //r.getUniqueName().equals("add/3") ) {
				if ( integerInput ) {
					rInt = aInt + bInt;
				} else if ( floatInput ) {
					rFloat = aFloat + bFloat;
				} else {
					throw new IllegalStateException("MathConstraint " + r + " has type problems. This should never happen if types are tested correctly earlier in the same class.");
				}
			} else if ( gC.getRelation().equals(MathRelation.Subtraction) ) {
				if ( integerInput ) {
					rInt = aInt - bInt;
				} else if ( floatInput ) {
					rFloat = aFloat - bFloat;
				} else {
					throw new IllegalStateException("MathConstraint " + r + " has type problems. This should never happen if types are tested correctly earlier in the same class.");
				}
			} else if ( gC.getRelation().equals(MathRelation.Multiplication) ) {
				if ( integerInput ) {
					rInt = aInt * bInt;
				} else if ( floatInput ) {
					rFloat = aFloat * bFloat;
				} else {
					throw new IllegalStateException("MathConstraint " + r + " has type problems. This should never happen if types are tested correctly earlier in the same class.");
				}
			} if ( gC.getRelation().equals(MathRelation.Division) ) {
				if ( integerInput ) {
					rInt = aInt / bInt;
				} else if ( floatInput ) {
					rFloat = aFloat / bFloat;
				} else {
					throw new IllegalStateException("MathConstraint " + r + " has type problems. This should never happen if types are tested correctly earlier in the same class.");
				}
			} else if ( gC.getRelation().equals(MathRelation.Modulo) ) {
				if ( integerInput ) {
					rInt = aInt % bInt;
				} else {
					throw new IllegalStateException("MathConstraint " + r + " not supported for float arguments.");
				}
			} 
			
			/**
			 * Process results:
			 */
			if ( integerInput && integerOutput && rInt != cInt ) {	// If result was picked before was it correct?
				isConsistent = false;
				break;
			} else if ( integerInput ) {							// Substitute result with computed 
				theta.add(c, Term.createInteger(rInt));
			} else if ( floatInput && floatOutput && rFloat!= cFloat ) {	// If result was picked before was it correct?
				isConsistent = false;
				break;
			} else if ( floatInput ) {							// Substitute result with computed 
				theta.add(c, Term.createFloat(rFloat));
			}
			
		}
		
		boolean atLeastOneViolation = false;
		
		if ( verbose ) Logger.msg(getName(), "Checking inequalities... ", 1);
		for ( MathConstraint  mathCon : core.getContext().get(MathConstraint.class) ) {
			Atomic relation = mathCon.getConstraint();
//			String operator = mathCon.getConstraint().name();
			
//			if ( operator.equals("less-than")  			||   operator.equals("less-than-or-equals")			||   operator.equals("greater-than")			||   operator.equals("greater-than-or-equals") ) {
				
			if ( mathCon.getRelation().equals(MathRelation.LessThan) 
				|| mathCon.getRelation().equals(MathRelation.LessThanOrEquals) 
				|| mathCon.getRelation().equals(MathRelation.GreaterThan)
				|| mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) {
				
				
				if ( verbose ) Logger.msg(getName(), "    " + mathCon, 1);
						
				Term xTerm = relation.getArg(0);
				Term yTerm = relation.getArg(1);
				
				boolean ignored = false;
				
				double xValue = 0.0;
				double yValue = 0.0;
				
				if ( xTerm.isConstant() ) {
					try  {  
						Integer v = Integer.parseInt( xTerm.toString() );  
						xValue = v.doubleValue();  
					} catch( Exception eNotInt ) {  
						try  {
							Double v = Double.parseDouble( yTerm.toString() );  
							xValue = v.doubleValue();
						} catch( Exception eNotDouble ) {
							throw new IllegalStateException("Value term "+yTerm+" in cost constraint " + mathCon + " is a constant but not parsable as double or int.");
						}
			        }  
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
					ignored = true;
				}
				
				if ( yTerm.isConstant() ) {
					try  {  
						Integer v = Integer.parseInt( yTerm.toString() );  
						yValue = v.doubleValue();  
					} catch( Exception eNotInt ) {  
						try  {
							Double v = Double.parseDouble( yTerm.toString() );  
							yValue = v.doubleValue();
						} catch( Exception eNotDouble ) {
							throw new IllegalStateException("Value term "+yTerm+" in cost constraint " + mathCon + " is a constant but not parsable as double or int.");
						}
			        }  
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
					ignored = true;
				}
				
				if ( !ignored ) {
					if ( mathCon.getRelation().equals(MathRelation.LessThan) ) { 
						atLeastOneViolation = !(xValue < yValue);
					} else if ( mathCon.getRelation().equals(MathRelation.LessThanOrEquals) ) { 
						atLeastOneViolation = !(xValue <= yValue);
					} else if ( mathCon.getRelation().equals(MathRelation.GreaterThan) ) { 
						atLeastOneViolation = !(xValue > yValue);
					} else if ( mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) { 
						atLeastOneViolation = !(xValue >= yValue);
					}
					if ( atLeastOneViolation ) {
						if ( verbose ) Logger.msg(getName(), "    fail!", 1);
						break;
					}
				}
			}
		}
		
		State state;
		ResolverIterator resolverIterator = null;
		
		if ( !theta.isEmpty() ) {
			List<Resolver> rList = new ArrayList<Resolver>();
			Resolver resolver = new Resolver(theta);
			rList.add(resolver);
			resolverIterator = new ResolverList(rList, this.getName(),this.cM);
		}
		
		if ( isConsistent ) {
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			state = State.Consistent;

		} else {
			if ( verbose ) Logger.msg(getName(), "Inconsistent", 0);
			state = State.Inconsistent;
		}
		return new SolverResult(state,resolverIterator);
	}
	
}

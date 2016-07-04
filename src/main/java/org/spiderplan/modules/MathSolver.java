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
import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.modules.solvers.SingleResolver;
import org.spiderplan.modules.solvers.SolverInterface;
import org.spiderplan.modules.solvers.SolverResult;
import org.spiderplan.modules.solvers.Core.State;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.expressions.ExpressionTypes.MathRelation;
import org.spiderplan.representation.expressions.math.MathConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.FloatTerm;
import org.spiderplan.representation.logic.IntegerTerm;
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
	ValueLookup evalMap;
	
	private long evaluateIntegerTerm( Term expression ) {
		
		if ( expression instanceof IntegerTerm ) {
			IntegerTerm value = (IntegerTerm)expression;
			return value.getValue();
		} 
		
		if ( expression instanceof FloatTerm ) {
			FloatTerm value = (FloatTerm)expression;
			return value.getValue().longValue();
		}
		
		if ( expression.isConstant() ) {
			Long r = null;
			r = evalMap.getInt(expression);
			if ( r == null ) {
				throw new IllegalArgumentException("Failed to lookup value of '" + expression + "' (not yet computed?).");
			}
			return r.longValue();
		}
		
		if ( expression.getNumArgs() == 1
				&& (expression.getName().equals("EST") 
				|| expression.getName().equals("LST") 
				|| expression.getName().equals("EET") 
				|| expression.getName().equals("LET")) ) {
			Long r = null;
			if ( expression.getName().equals("EST") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = evalMap.getEST(expression.getArg(0));
			}
			if ( expression.getName().equals("LST") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = evalMap.getLST(expression.getArg(0));
			}
			if ( expression.getName().equals("EET") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = evalMap.getEET(expression.getArg(0));
			}
			if ( expression.getName().equals("LET") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = evalMap.getLET(expression.getArg(0));
			}
			if ( r == null ) {
				throw new IllegalArgumentException("Failed to lookup value of '" + expression + "' (not yet computed?).");
			}
			return r.longValue();
		} else if ( expression.getNumArgs() == 2 ) { // TODO: allow >2 args with clisp interpretation
			if ( expression.getName().equals("add") || expression.getName().equals("+") ) {
				return evaluateIntegerTerm(expression.getArg(0)) + evaluateIntegerTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("sub") || expression.getName().equals("-") ) {
				return evaluateIntegerTerm(expression.getArg(0)) - evaluateIntegerTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("div") || expression.getName().equals("/") ) {
				return evaluateIntegerTerm(expression.getArg(0)) / evaluateIntegerTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("mult") || expression.getName().equals("*") ) {
				return evaluateIntegerTerm(expression.getArg(0)) * evaluateIntegerTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("mod") || expression.getName().equals("%") ) {
				return evaluateIntegerTerm(expression.getArg(0)) % evaluateIntegerTerm(expression.getArg(1));
			}
		} 
		
		Long r = null;
		r = evalMap.getInt(expression);
		if ( r == null ) {
			throw new IllegalArgumentException("Failed to lookup value of '" + expression + "' (not yet computed?).");
		}
		return r.longValue();
	}
	
	private double evaluateFloatTerm( Term expression ) {
		
		if ( expression instanceof IntegerTerm ) {
			IntegerTerm value = (IntegerTerm)expression;
			return value.getValue();
		} 
		
		if ( expression instanceof FloatTerm ) {
			FloatTerm value = (FloatTerm)expression;
			return value.getValue().doubleValue();
		}
		
		if ( expression.isConstant() ) {
			Double r = null;
			r = evalMap.getFloat(expression);
			if ( r == null ) {
				throw new IllegalArgumentException("Failed to lookup value of '" + expression + "' (not yet computed?).");
			}
			return r.longValue();
		}
		
		if ( expression.getNumArgs() == 1
			&& (expression.getName().equals("EST") 
			|| expression.getName().equals("LST") 
			|| expression.getName().equals("EET") 
			|| expression.getName().equals("LET")) ) {
			Double r = null;
			if ( expression.getName().equals("EST") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = (double)evalMap.getEST(expression.getArg(0));
			}
			if ( expression.getName().equals("LST") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = (double)evalMap.getLST(expression.getArg(0));
			}
			if ( expression.getName().equals("EET") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = (double)evalMap.getEET(expression.getArg(0));
			}
			if ( expression.getName().equals("LET") && evalMap.hasInterval(expression.getArg(0)) ) {
				r = (double)evalMap.getLET(expression.getArg(0));
			}
			if ( r == null ) {
				throw new IllegalArgumentException("Failed to lookup value of " + expression + " (not yet computed?).");
			}
			return r.longValue();
		} else if ( expression.getNumArgs() == 2 ) { // TODO: allow >2 args with clisp interpretation
			if ( expression.getName().equals("add") || expression.getName().equals("+") ) {
				return evaluateFloatTerm(expression.getArg(0)) + evaluateFloatTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("sub") || expression.getName().equals("-") ) {
				return evaluateFloatTerm(expression.getArg(0)) - evaluateFloatTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("div") || expression.getName().equals("/") ) {
				return evaluateFloatTerm(expression.getArg(0)) / evaluateFloatTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("mult") || expression.getName().equals("*") ) {
				return evaluateFloatTerm(expression.getArg(0)) * evaluateFloatTerm(expression.getArg(1));
			}
			
			if ( expression.getName().equals("mod") || expression.getName().equals("%") ) {
				return evaluateFloatTerm(expression.getArg(0)) % evaluateFloatTerm(expression.getArg(1));
			}
		} 
		Double r = null;
		r = evalMap.getFloat(expression);
		if ( r == null ) {
			throw new IllegalArgumentException("Failed to lookup value of '" + expression + "' (not yet computed?).");
		}
		return r;
	}
	@Override
	public SolverResult testAndResolve(Core core) {
		boolean isConsistent = true;
		
		Collection<MathConstraint> C = core.getContext().get(MathConstraint.class);
		
//		List<TemporalIntervalLookup> tiLookUp = core.getContext().get(TemporalIntervalLookup.class);
//		
//		if ( !tiLookUp.isEmpty() ) {
//			this.temporalIntervals = core.getContext().get(TemporalIntervalLookup.class).get(0);
//		} else {
//			this.temporalIntervals = null;
//		}
		
		ValueLookup valueLookUp = core.getContext().getUnique(ValueLookup.class);
		
		if ( valueLookUp != null ) {
			this.evalMap = core.getContext().get(ValueLookup.class).get(0).copy();
		} else {
			this.evalMap = new ValueLookup();
		}
				
		Substitution theta = new Substitution();
		
		for ( MathConstraint gC : C ) {
			Atomic r = gC.getConstraint().substitute(theta);
			if ( verbose ) Logger.msg(getName(),"Evaluating: " + r, 1);
			
			if ( gC.getRelation().equals(MathRelation.EvalInt) ) {
				Term target = r.getArg(0);
				Term expression = r.getArg(1);
				
				try {
					long result = evaluateIntegerTerm(expression);
					
					if ( target.isVariable() ) {
						theta.add(target, Term.createInteger(result));
					} else {
						evalMap.putInt(target, result);
					}
					
					if ( verbose ) Logger.msg(getName(),"Result: " + target + " = " + result , 1);
					
				} catch ( IllegalArgumentException e ) {  
					if ( verbose ) Logger.msg(getName(),"Skipping: Contains variable terms or not (yet) computed values.", 1);
				}				
				
			} else if ( gC.getRelation().equals(MathRelation.EvalFloat) ) {
					Term target = r.getArg(0);
					Term expression = r.getArg(1);
					
				try {
					double result = evaluateFloatTerm(expression);
					
					if ( target.isVariable() ) {
						theta.add(target, Term.createFloat(result));
					} else {
						evalMap.putFloat(target, result);
					}
					
				if ( verbose ) Logger.msg(getName(),"Result: " + target + " = " + result , 1);
					
				} catch ( IllegalArgumentException e ) {  
					if ( verbose ) Logger.msg(getName(),"Skipping: Contains variable terms or not (yet) computed values.", 1);
				}		
					
			} 
		}
		
		boolean atLeastOneViolation = false;
		
		if ( verbose ) Logger.msg(getName(), "Checking inequalities... ", 1);
		for ( MathConstraint  mathCon : core.getContext().get(MathConstraint.class) ) {
			Atomic relation = mathCon.getConstraint();

			if ( mathCon.getRelation().equals(MathRelation.LessThan) 
				|| mathCon.getRelation().equals(MathRelation.LessThanOrEquals) 
				|| mathCon.getRelation().equals(MathRelation.GreaterThan)
				|| mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) {
								
				if ( verbose ) Logger.msg(getName(), "    " + mathCon, 1);
						
				Term xTerm = relation.getArg(0);
				Term yTerm = relation.getArg(1);
				
				boolean ignored = false;
				
				Double xDouble = null;
				Double yDouble = null;
				Long xLong = null;
				Long yLong = null;
				
				if ( !xTerm.isVariable() ) {
					try  {  
						xLong = Long.parseLong( xTerm.toString() );  
					} catch( Exception e ) { }
					try  {
						xDouble = Double.parseDouble( xTerm.toString() );
					} catch( Exception e ) { }
					if ( xLong == null ) {
						if ( evalMap.hasIntVariable(xTerm) ) {
							xLong = evalMap.getInt(xTerm);
						}
					}
					if ( xDouble == null ) {
						if ( evalMap.hasFloatVariable(xTerm) ) {
							xDouble = evalMap.getFloat(xTerm);
						}
					}
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
					ignored = true;
				}
				
				
				if ( !yTerm.isVariable() ) {
					try  {  
						yLong = Long.parseLong( yTerm.toString() ); 
					} catch( Exception e ) { }
					try  {
						yDouble = Double.parseDouble( yTerm.toString() );  
					} catch( Exception e ) { }
					if ( yLong == null ) {
						if ( evalMap.hasIntVariable(yTerm) ) {
							yLong = evalMap.getInt(yTerm);
						}
					}
					if ( yDouble == null ) {
						if ( evalMap.hasFloatVariable(yTerm) ) {
							yDouble = evalMap.getFloat(yTerm);
						}
					}						
					
				} else {
					if ( verbose ) Logger.msg(getName(), "    variable value -> ignored", 1);
					ignored = true;
				}
			
				ignored = (xLong == null && xDouble == null) || (yLong == null && yDouble == null);
				
				if ( !ignored ) {
					if ( xLong != null && yLong != null ) {
						if ( mathCon.getRelation().equals(MathRelation.LessThan) ) { 
							atLeastOneViolation = !(xLong < yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.LessThanOrEquals) ) { 
							atLeastOneViolation = !(xLong <= yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThan) ) { 
							atLeastOneViolation = !(xLong > yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) { 
							atLeastOneViolation = !(xLong >= yLong);
						}
					} else if ( xLong != null ) {
						if ( mathCon.getRelation().equals(MathRelation.LessThan) ) { 
							atLeastOneViolation = !(xLong < yDouble);
						} else if ( mathCon.getRelation().equals(MathRelation.LessThanOrEquals) ) { 
							atLeastOneViolation = !(xLong <= yDouble);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThan) ) { 
							atLeastOneViolation = !(xLong > yDouble);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) { 
							atLeastOneViolation = !(xLong >= yDouble);
						}
					}  else if ( yLong != null ) {	
						if ( mathCon.getRelation().equals(MathRelation.LessThan) ) { 
							atLeastOneViolation = !(xDouble < yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.LessThanOrEquals) ) { 
							atLeastOneViolation = !(xDouble <= yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThan) ) { 
							atLeastOneViolation = !(xDouble > yLong);
						} else if ( mathCon.getRelation().equals(MathRelation.GreaterThanOrEquals) ) { 
							atLeastOneViolation = !(xDouble >= yLong);
						}
					}
					if ( atLeastOneViolation ) {
						if ( verbose ) Logger.msg(getName(), "    fail!", 1);
						break;
					}
				}
			}
		}
		
		State state;
		SingleResolver r = null;
		
		if ( !theta.isEmpty() ) {
			ConstraintDatabase resCDB = new ConstraintDatabase();
			resCDB.add(evalMap);
			Resolver resolver = new Resolver(theta, resCDB);
			r = new SingleResolver(resolver, this.getName(),this.cM);
		} else {
			ConstraintDatabase resCDB = new ConstraintDatabase();
			resCDB.add(evalMap);
			Resolver resolver = new Resolver(resCDB);
			r = new SingleResolver(resolver, this.getName(),this.cM);
		}
		
		if ( isConsistent ) {
			if ( verbose ) Logger.msg(getName(), "Consistent", 0);
			state = State.Consistent;  // TODO: should be searching when resolver added (only on change)

		} else {
			if ( verbose ) Logger.msg(getName(), "Inconsistent", 0);
			state = State.Inconsistent;
		}
		return new SolverResult(state,r);
	}
	
}

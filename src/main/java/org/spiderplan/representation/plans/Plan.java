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
package org.spiderplan.representation.plans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.ForwardPlanningSearch;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.Operator;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.expressions.temporal.Interval;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;

/**
 * Represents a plan and a set of constraints required by this plan.
 * Usually the constraint set includes temporal constraints that resemble
 * causal links.
 * 
 * @author Uwe Köckemann
 *
 */
public class Plan {
	
	private ArrayList<Operator> A = new ArrayList<Operator>();	
	private ConstraintDatabase C = new ConstraintDatabase();
	
	private final static Term THIS = Term.createVariable("THIS");
	
	private final static Term NewOperatorInterval = Term.createVariable("KEY_OP");
	private final static Term NewPreconditionInterval = Term.createVariable("KEY_P");
	private final static Term NewEffectInterval = Term.createVariable("KEY_E");
	
	/**
	 * Create new empty plan.
	 */
	public Plan() {}	
	
	/**
	 * Get the list of actions of this plan.
	 * @return list of operators
	 */
	public ArrayList<Operator> getActions() {
		return A;
	}
	
	/**
	 * Get the constraints of this plan.
	 * @return set of constraints
	 */
	public ConstraintDatabase getConstraints() {
		return C;
	}
	
	/**
	 * Add an action to this plan.
	 * @param a action to be added
	 */
	public void addAction( Operator a ) {
		A.add(a);
	}
	
	/**
	 * Remove the ith action from this plan.
	 * @param i
	 */
	public void removeAction( int i ) {
		A.remove(i);
	}
	
	/**
	 * Add a list of actions to the plan.
	 * @param A
	 */
	public void addActions( ArrayList<Operator> A ) {
		this.A.addAll(A);
	}
	
	/**
	 * Add a constraint to the plan.
	 * @param c
	 */
	public void addConstraint( Expression c ) {
		C.add(c);
	}
	
	/**
	 * Remove a constraint from the plan.
	 * @param c
	 */
	public void removeConstraint( Expression c ) {
		C.remove(c);
	}	
	
	/**
	 * Add a set of constraints to the plan.
	 * @param C
	 */
	public void addConstraints( Collection<Expression> C ) {
		this.C.addAll(C);
	}
		
	/**
	 * Create plan from a sequential plan. 
	 * Sequential plans are created by heuristic forward planners ({@link ForwardPlanningSearch}).
	 * 
	 * @param p sequential plan
	 * @param O set of operator definitions
	 * @param planID ID of the plan to keep actions unique
	 */
	public Plan( SequentialPlan p, Collection<Operator> O, long planID ) {	
		HashMap<String,List<Statement>> lastChangingStatement = new HashMap<String, List<Statement>>();
		Substitution theta = new Substitution();
		long nextFreeID = 0;
		
		for ( int i = 0 ; i < p.length() ; i++ ) {
//			for ( int j = 0 ; j < p.size(i); j++ ) {
				/*
				 * Find operator, substitute & add
				 */
				Operator a = null;

				for ( Operator o : O ) {
					Substitution sub = o.getName().match(p.getAtomic(i)); 
					if ( sub != null ) {
						a = o.copy();
						break;
					}
				}
				
				if ( a == null ) {
					throw new IllegalStateException("Operator " + p.getAtomic(i).toString() + " does not exist.");
				}
	
				a.substitute(p.getSubstitution(i));
				a.makeUniqueVariables(planID);
				a.makeUniqueVariables(nextFreeID++);
//				a.makeUniqueEffectKeys(UniqueID.getID());
				a.makeEffectIntervalKeysGround();
//				a.makeKeysGround();
				
				Substitution substTHIS = new Substitution();
				substTHIS.add(THIS, a.getLabel());
				a.substitute(substTHIS);
					
				A.add(a);
				
				/*
				 * Map preconditions to previous effects that provide preconditions
				 */
				for ( Statement pre : a.getPreconditions() ) {
					if ( pre.getKey().isVariable() ) {
						Statement last = null;
						List<Statement> changeHistory = lastChangingStatement.get(pre.getVariable().toString());
						
						/**
						 * Try to find value that matches variable and value of unlinked precondition.
						 * (If no such statement is found we expect it to be linked to initial CDB.)
						 */
						if ( changeHistory != null ) {
							for ( int k = changeHistory.size()-1; k >= 0 ; k-- ) {
								if ( pre.getVariable().equals(changeHistory.get(k).getVariable())
									&& pre.getValue().equals(changeHistory.get(k).getValue()) ) {							
									last = changeHistory.get(k);
									break;
								}
							}
						}				
			
						/*
						 * Mapped to a previous effect, so we change labels accordingly
						 * (This establishes a causal link)
						 */
						if ( last != null ) {
							AllenConstraint tC = new AllenConstraint(last.getKey(), pre.getKey(),  org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation.Equals );
							C.add( tC );
							
							theta.add(pre.getKey(),pre.getKey().makeConstant());
							
						/* Since this is not an effect, it has to be grounded in initial 
						 * ConstraintDatabase, so we'll make it a variable
						 */
						} else {
							Term variable = Term.createVariable(pre.getKey().toString());
							theta.add(pre.getKey(), variable);
						}
					}
				}

				/*
				 * Overwrite lastChangingStatement with new effects
				 */
				for ( Statement eff : a.getEffects() ) {
					theta.add(eff.getKey(), eff.getKey().makeConstant());
					
					if ( !lastChangingStatement.containsKey(eff.getVariable().toString())) {
						lastChangingStatement.put(eff.getVariable().toString(),new ArrayList<Statement>());
					}
					lastChangingStatement.get(eff.getVariable().toString()).add(eff);
				}
//			}
		}
		this.substitute(theta);

//		for ( Operator a : this.getActions() ) {
//			
//			System.out.println("==================================");
//			System.out.println("Before");
//			System.out.println("==================================");
//			System.out.println(a);
//			a.makeUniqueVariables(UniqueID.getID());
//			a.makeUniqueEffectKeys(UniqueID.getID());
//			
//			System.out.println("==================================");
//			System.out.println("After");
//			System.out.println("==================================");
//			System.out.println(a);
//		}
	}
	
	/**
	 * Apply plan to a context {@link ConstraintDatabase} and return the result. Input context remains unchanged.
	 * <p>
	 * Note: Does not include any applicability test.
	 * @param context A context.
	 * @return Input context after adding preconditions, effects and constraints of all actions and the constraints of this plan.
	 */
	public ConstraintDatabase apply( ConstraintDatabase context ) {
		ConstraintDatabase app = context.copy();
		for ( Operator a : this.getActions() ) {
			for ( Statement e : a.getEffects() ) {
				app.add(e);
			}
//			app.addStatements(a.getEffects());
			for ( Statement p : a.getPreconditions() ) {
				app.add(p);
			}
//			app.addStatements(a.getPreconditions());
			app.add(a.getNameStateVariable());
			app.addAll(a.getConstraints());
		}
		app.addAll(this.getConstraints());
		return app;
	}
	
	
	/**
	 * Return a sub-plan of length n (i.e., including the first n actions).
	 * @param n
	 * @return the sub-plan
	 */
	public Plan getSubPlan( int n ) {
		Plan p = new Plan();
		for ( int i = 0 ; i < n ; i++ ) {
			p.addAction(this.getActions().get(i).copy());
		}
		for ( Expression c : this.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				boolean foundFromKey = false;
				boolean foundToKey = false;
				
				for ( Operator a : p.getActions() ) {
					if ( !foundFromKey && a.hasStatementWithKey(tC.getFrom())) {
						foundFromKey = true;
					}
					if ( !foundToKey && a.hasStatementWithKey(tC.getTo())) {
						foundToKey = true;
					}
					if ( foundFromKey && foundToKey ) {
						p.addConstraint(c);
						break;
					}
				}
				
			} else {
				p.addConstraint(c);
			}
		}
		
		return p;
	}
	
	/**
	 * Determines if the {@link Plan} sub is matching 
	 * a sub-sequence of, or is equal to this {@link Plan}.
	 * 
	 * @param sub
	 * @return <code>true</code> if sub can be matched to a sub-sequence of this plan, <code>false</code> otherwise
	 */
	public boolean isMatchingSubPlan( Plan sub ) {
//		if ( sub.getActions().size() != this.getActions().size() ) {
//			return false;
//		}
		
		Substitution theta = new Substitution();
		
		for ( int i = 0 ; i < sub.getActions().size(); i++ ) {
			Operator a1 = sub.getActions().get(i).copy(); 
			Operator a2 = this.getActions().get(i).copy();
			
			a1.substitute(theta);
			a2.substitute(theta);
			
			
			Substitution subst = a1.getName().match(a2.getName()); 
			
			
			if ( subst == null ) {
				return false;
			}
			
			if ( !theta.add(subst) ) {
				return false;
			}
			
		}
		return true;
	}
	
//	public boolean isMatchingSubPlan( Plan sub ) {
//		if ( sub.getActions().size() > this.getActions().size() ) {
//			return false;
//		}
//		for ( int i = 0 ; i < sub.getActions().size(); i++ ) {
//			Operator a1 = sub.getActions().get(i); 
//			Operator a2 = this.getActions().get(i);
//			
//			if ( a1.getName().match(a2.getName()) == null ) {
//				return false;
//			}
//		}
//		return true;
//	}
	
	/**
	 * Create a sequential plan from this plan.
	 * @return a sequential plan
	 */
	public SequentialPlan getSequentialPlan() {
		SequentialPlan p = new SequentialPlan();
		
		for ( Operator a : this.A ) {
			p.add(a.getName(), a.getSubstitution());
		}
		
		return p;
	}
	
	/**
	 * Merge plan into a single operator that is ground except for precondition keys.
	 * @param name name of the new operator
	 * @return operator definition that includes the entire plan
	 */
	public Operator mergeIntoOperator( String name ) {
		Operator o = new Operator();
		ArrayList<Term> args = new ArrayList<Term>();
		
		for ( Operator a : this.A ) {
			for ( int i = 0 ; i < a.getName().getNumArgs() ; i++ ) {
				args.add(a.getName().getArg(i));
			}
			
			for ( Statement p : a.getPreconditions() ) {
				if ( p.isGround() ) {
					o.addEffect(p);
				} else {
					o.addPrecondition(p);
				}
			}
			for ( Statement e : a.getEffects() ) {
				o.addEffect(e);
			}
		}
		
		o.setName( new Atomic(name+args.toString().replace("[", "(").replace("]", ")").replace(" ", "")) );
		
		return o;
	}
	
	/**
	 * Lift all occurrences of {@link Term} t to a variable.
	 * @param t The {@link Term} that will be lifted.
	 */
	public void liftTerm( Term t ) {
		Substitution subst = new Substitution();
		Term variable = Term.createVariable(t.getName().toString());
		subst.add(t, variable);
		Term.setAllowConstantSubstitution(true);
		this.substitute(subst);
		Term.setAllowConstantSubstitution(false);
	}
	
	/**
	 * Creates a new operator from this plan. This is done by creating a lifted
	 * version of the plan. Preconditions in the new operator are preconditions
	 * from the plan that are not provided from other actions in the plan.
	 * @param newOpName		Predicate name of the new {@link Operator}'s name
	 * @param O				Non-ground operators used to create the lifted version or the plan.
	 * @param tM			{@link TypeManager} provides types and is extended with bindings of
	 * 						the new operator name. 
	 * @return				A new operator representing a lifted version of this plan 
	 * 						and all its constraints.
	 */
	public Operator createOperator(  String newOpName, Collection<Operator> O, TypeManager tM ) {
			
		ArrayList<Operator> nongroundA = new ArrayList<Operator>();
		Map<Term,Statement> nonGroundStatementMap = new HashMap<Term, Statement>();
		
		ConstraintDatabase nongroundOpConstraints = new ConstraintDatabase();
		
		/**
		 *  - Create a lifted version of each action. 
		 * 		Makes all variables unique (using varCounter),
		 * 		so we can add them to a single operator without 
		 * 		ambiguity. 
		 *  - Collects the non-temporal constraints of each operator
		 * 		in a single set, so we can add them in the end.
		 * 	- Collect keys of all preconditions and effects
		 * 		and creates a mapping from keys to statements
		 * 		for later use.  
		 */
		Map<Term,Statement> keyMap = new HashMap<Term, Statement>();		
		Set<Term> preconditionKeys = new HashSet<Term>();
		Set<Term> effectKeys = new HashSet<Term>();		
		int varCounter = 0;
		int disconnectedVarCounter = 0;
		for ( Operator a : A ) {
			
			for ( Statement p : a.getPreconditions() ) {
				preconditionKeys.add(p.getKey());
				keyMap.put(p.getKey(), p);
			}
			for ( Statement e : a.getEffects() ) {
				effectKeys.add(e.getKey());
				keyMap.put(e.getKey(), e);
			}
			
			for ( Operator o : O ) {
				if ( o.getName().getUniqueName().equals(a.getName().getUniqueName()) ) {
					
					Operator nonGroundOp = o.copy();
					Substitution theta = new Substitution();
					
					for ( int i = 0 ; i < nonGroundOp.getName().getNumArgs() ; i++ ) {
						Term arg = nonGroundOp.getName().getArg(i);
						theta.add(arg, arg.makeUnique(varCounter));
					}
					
					varCounter++;
					nonGroundOp.substitute(theta);
					nonGroundStatementMap.put(a.getNameStateVariable().getKey(), nonGroundOp.getNameStateVariable());
					nongroundA.add(nonGroundOp);
					for ( int i = 0 ; i < a.getPreconditions().size() ; i++ ) {
						nonGroundStatementMap.put( a.getPreconditions().get(i).getKey(), nonGroundOp.getPreconditions().get(i));
					}
					for ( int i = 0 ; i < a.getEffects().size() ; i++ ) {
						nonGroundStatementMap.put( a.getEffects().get(i).getKey(), nonGroundOp.getEffects().get(i));
					}
					
					/**
					 * Replace all variables in RelationalConstraints that are not connected to
					 * operator arguments with unique variables to avoid mixing them up with 
					 * other variables in different operators. 
					 */
					ArrayList<Term> argVars = new ArrayList<Term>();
					for ( int i = 0 ; i < nonGroundOp.getName().getNumArgs() ; i++ ) {
						argVars.add(nonGroundOp.getName().getArg(i));
					}

					ArrayList<Term> checkList = new ArrayList<Term>();
					for ( Expression c : nonGroundOp.getConstraints() ) {
						if ( c instanceof PrologConstraint ) {
							PrologConstraint rC = (PrologConstraint)c;
							
							for ( int i = 0 ; i < rC.getRelation().getNumArgs() ; i++ ) {
								Term t = rC.getRelation().getArg(i);
								if ( 	t.isVariable() 
										&& !argVars.contains(t) 
										&& !checkList.contains(t) ) {
									Substitution theta1 = new Substitution();
//									Term uniqueArg = new Term(t.toString()+"_"+disconnectedVarCounter);
									Term uniqueArg = t.makeUnique(disconnectedVarCounter);
									
									disconnectedVarCounter++;
									theta1.add(t, uniqueArg);
									nonGroundOp.substitute(theta1);
									
									checkList.add(uniqueArg);
								}
							}
						}
					}
					
					/**
					 * Get non temporal constraints with right variables to add them later.
					 */
					for ( Expression c : nonGroundOp.getConstraints() ) {
						if ( ! (c instanceof AllenConstraint) ) {
							nongroundOpConstraints.add(c);
						}
					}					
				}
			}
		}

		/**
		 * Decide which statements become effects.
		 * Collect precondition and effect pairs in the old plan,
		 * to merge them later and avoid adding preconditions that
		 * are provided inside the new operator.  
		 */
		ArrayList<ArrayList<Term>> canBeMerged = new ArrayList<ArrayList<Term>>();
		Set<Term> newEffectKeys = new HashSet<Term>();	
		Set<Term> internallyConnectedKeys = new HashSet<Term>();
		for ( AllenConstraint tC : this.C.get(AllenConstraint.class) ) {
			if ( tC.isBinary() && tC.getRelation().equals(org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation.Equals) ) {
				Term from = tC.getFrom();
				Term to = tC.getTo();
				
//				if ( ! (preconditionKeys.contains(from) && preconditionKeys.contains(to)) ) {
//					System.out.println("Adding " + from + " and " + to);
//					newEffectKeys.add(from);
//					newEffectKeys.add(to);
//				} 
				/**
				 * Preconditions that are provided by effects are effects
				 * in newly created operator:
				 */
				if ( (preconditionKeys.contains(from) && effectKeys.contains(to))
					|| (preconditionKeys.contains(to) && effectKeys.contains(from)) ) {
					newEffectKeys.add(from);
					newEffectKeys.add(to);
				} 
				
				/**
				 * Temporally equal assignments of the same ground value to the same ground variable 
				 * can be merged in the lifted version as well to reduce number of variables.
				 */
				if (   keyMap.containsKey(from) && keyMap.containsKey(to) 
					&& keyMap.get(from).getVariable().equals(keyMap.get(to).getVariable())
					&& keyMap.get(from).getValue().equals(keyMap.get(to).getValue()) ) {
					
					ArrayList<Term> pair = new ArrayList<Term>();
					pair.add(from);
					pair.add(to);
					canBeMerged.add(pair);
				}
			}
			
			internallyConnectedKeys.add(tC.getTo());
		
		}
		
		/**
		 * Decide which statements become preconditions.
		 */
		Set<Term> newPreconditionKeys = new HashSet<Term>();
		
		for ( Term pKey : preconditionKeys ) {
			if ( !newEffectKeys.contains(pKey) ) {
				newPreconditionKeys.add(pKey);
			}
		}
		
		Substitution theta = new Substitution();
		Operator o = new Operator();
		Map<Term,Statement> groundLabelToAddedStatementsMap = new HashMap<Term, Statement>();
		
		int effCount = 0;
		int preCount = 0;
		
		/**
		 * Create preconditions of new operator, which are all statements that are
		 * preconditions in the plans operators and are not set equal to any effects
		 * inside the plan (i.e. they need to be provided from outside of the plan)
		 */
//		ArrayList<Term> knownGroups = new ArrayList<Term>();
		for ( Term preKey : newPreconditionKeys ) {
			Statement pGround = keyMap.get(preKey);
			Statement p = nonGroundStatementMap.get(preKey);
			
			Term pLabel = p.getKey();
			Term groundLabel = pGround.getKey();
			
//			pLabel = new Term("KEY_P_" + preCount);
			pLabel = NewPreconditionInterval.makeUnique(preCount);
			preCount++;
			
			theta.add(groundLabel, pLabel);
			
			Substitution thetaPre = new Substitution();
			thetaPre.add(p.getKey(), pLabel);
			p = p.substitute(thetaPre);			
			
			groundLabelToAddedStatementsMap.put(groundLabel, p);
			o.addPrecondition(p);
			
//			System.out.println(p);
		}
		
//		System.out.println(theta);
	
		/**
		 * Everything that was not added as a precondition becomes an effect.
		 * Additionally we merge statements that are temporally equal and assign
		 * the same value to the same variable. This reduces the number of
		 * arguments in the final operator.
		 */
		newEffectKeys.addAll(effectKeys);
		for ( Term effKey : newEffectKeys ) {
			if ( keyMap.containsKey(effKey) && nonGroundStatementMap.containsKey(effKey) ) {
				Statement eGround = keyMap.get(effKey);
				Statement e = nonGroundStatementMap.get(effKey);
				
				Term eLabel = e.getKey();
				Term groundLabel = eGround.getKey();
				
				groundLabelToAddedStatementsMap.put(groundLabel, e);
				
				boolean needToAddThis = true;
	//			Statement existingStatement = null;
				
				/**
				 * See if we can merge e with something existing
				 */
				for ( ArrayList<Term> pair : canBeMerged ) {
					if ( pair.contains(groundLabel) ) {					
						
						Term otherTerm;
						
						if ( pair.get(0).equals(groundLabel) ) {
							otherTerm = pair.get(1);
						} else {
							otherTerm = pair.get(0);
						}
	
						Statement otherStatement = groundLabelToAddedStatementsMap.get(otherTerm);
	
						if ( theta.getMap().containsKey(otherTerm)) {
							/** 
							 * e can be merged with a statement already added.
							 * This means we add substitutions to theta that makes
							 * e equal with its merging partner.
							 * 
							 * Example of merging for
							 * Temporal constraint: k1 equals k2
							 * Statement added: k1: < sv(c1), val1 >	with lifted version: KEY_1: < sv(C1), VAL1 >  
							 * Statement e:     k2: < sv(c1), val1 >	with lifted version: KEY_2: < sv(C2), VAL2 > 
							 * We should add substitutions { KEY_2/KEY_1, C2/C1, VAL2/VAL1 } to theta.
							 * making the statements equal in the lifted version as well.
							 * 
							 * While we will not add statement KEY_2 in the example, we still have to substitute all
							 * other constraints of the operator, which makes it necessary to create the
							 * corresponding substitutions.
							 * 
							 * Note: Multiple merging may lead to situations where we want to add a 
							 * substitution x/z when x/y already exists.
							 * Here we can resolve this by instead adding z/y, since x, y and z will be
							 * the same in the end. This is taken care of in the addAndMergeVariables(x,y)
							 * method that recursively attempts to find the best place to add the new 
							 * substitution (e.g., if in the above example y/a already exists it will add z/a). 
							 */
							
							needToAddThis = false;
	//						existingStatement = otherStatement;
							Substitution labelSubst = new Substitution();
							labelSubst.add(groundLabel, theta.getMap().get(otherTerm));
							if ( !theta.add(labelSubst) ) {
								theta.addAndMergeVariables(groundLabel, theta.getMap().get(otherTerm));
							}
							
							/**
							 * Reduce to one variable for each equal Statement.
							 * If a substitution x/y exists and we want to add x/z
							 * we flip the second one to z/x to make the overall substitution
							 * legal (no ambiguity) 
							 */
							Substitution valueSubst = e.getValue().match(otherStatement.getValue());
							Substitution variableSubst = e.getVariable().match(otherStatement.getVariable());
									
							if ( !theta.add(valueSubst) ) {
								ArrayList<Term> keys = new ArrayList<Term>();
								keys.addAll(valueSubst.getMap().keySet());
								Term from = keys.get(0);
								Term to = valueSubst.substitute(from);
								if ( to == null ) 
									to = from;
								theta.addAndMergeVariables(from,to);
							}
				
							if ( !theta.add(variableSubst) ) {
								ArrayList<Term> keys = new ArrayList<Term>();
								keys.addAll(variableSubst.getMap().keySet());
								
								for ( Term from : keys ) {							
									theta.addAndMergeVariables(from, variableSubst.getMap().get(from));
								}
							}
						}
					}
				}	
				/**
				 * Add only if not merged with something existing.
				 */
				if ( needToAddThis ) {
//					eLabel = new Term("KEY_E_" + effCount);
					eLabel = NewEffectInterval.makeUnique(effCount);
					effCount++;
				
					if ( !o.getEffects().contains(e)) {
						theta.add(groundLabel, eLabel);
						
						Substitution thetaEff = new Substitution();
						thetaEff.add(e.getKey(), eLabel);
						e = e.substitute(thetaEff);		
						

						o.addEffect(e);
						groundLabelToAddedStatementsMap.put(groundLabel, e);
					}
				} 
			}
		}
		
		/**
		 * Collect types and add temporal constraints between new operator THIS
		 * and the operators it contains (KEY_OP_xxx)
		 */
		int opCounter = 0;
		ArrayList<Type> types = new ArrayList<Type>();
		ArrayList<Term> newOpArgs = new ArrayList<Term>();
		for ( Operator a : this.A ) {
			Statement nameGround = a.getNameStateVariable();
			Statement name = nonGroundStatementMap.get(nameGround.getKey());
//			name.setKey( new Term("KEY_OP_" + opCounter));
//			name.setKey( NewOperatorInterval.makeUnique(opCounter));
			name = new Statement( NewOperatorInterval.makeUnique(opCounter), name.getVariable(), name.getValue() );
			
			Interval bounds[] = new Interval[2];
			bounds[0] = new Interval("[1,inf]");
			bounds[1] = new Interval("[1,inf]");			
			
			o.getConstraints().add(new AllenConstraint(name.getKey(), THIS, org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation.During, bounds));
			opCounter++;
			
			for ( int i = 0 ;  i < a.getName().getNumArgs() ; i++ ) {
				newOpArgs.add(name.getVariable().getArg(i));
				types.add(tM.getPredicateTypes(a.getName().getUniqueName(), i));
			}
			
			theta.add( nameGround.getKey(), name.getKey() );
			
			o.addEffect(name);
					
			for ( AllenConstraint groundC : a.getConstraints().get(AllenConstraint.class) ) {
				o.getConstraints().add(groundC);
			}
		}
			
		/**
		 * Add constraints
		 */
		o.getConstraints().addAll(this.getConstraints());
		o.getConstraints().addAll(nongroundOpConstraints);
			
		/**
		 * Remove duplicates from arguments and attach types to new operator.
		 */
		o.setName(new Atomic("foo")); 	// dummy name to allow substitution before we created the real name
		o.substitute(theta);
		for ( int i = 0 ; i < newOpArgs.size()-1; i++ ) {
			for ( int j = i+1 ; j < newOpArgs.size() ; j++ ) {
				if ( newOpArgs.get(i).equals(newOpArgs.get(j)) ) {
					newOpArgs.remove(i);
					types.remove(i);
					i--;
					break;
				}
			}
		}
//		o.setName(new Atomic(newOpName+newOpArgs.toString().replace("[", "(").replace("]", ")").replace(" ", "")));
		
		o.setName(new Atomic(newOpName, newOpArgs.toArray(new Term[newOpArgs.size()])));
		
		tM.attachTypes(o.getName().getUniqueName(), types);
		
		/**
		 * Remove trivial temporal equals constraints (A equals A) 
		 */
		ArrayList<Expression> remList = new ArrayList<Expression>();
		for ( Expression c : o.getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tC = (AllenConstraint)c;
				if ( tC.getRelation().equals(org.spiderplan.representation.expressions.ExpressionTypes.TemporalRelation.Equals) ) {
					if ( tC.getFrom().equals(tC.getTo()) ) {
						remList.add(tC);
					}
				}
			}
		}
		o.getConstraints().removeAll(remList);
		
		/**
		 * Finally, we filter out duplicate statements from preconditions and effects
		 */
		ArrayList<Statement> filtered = new ArrayList<Statement>();
		for ( Statement p : o.getPreconditions() ) {
			if ( !filtered.contains(p) ) {
				filtered.add(p);
			}
		}
		o.getPreconditions().clear();
		o.addPreconditions(filtered);
		
		filtered.clear();
		for ( Statement e : o.getEffects() ) {
			if ( !filtered.contains(e) ) {
				filtered.add(e);
			}
		}
		o.getEffects().clear();
		o.addEffects(filtered);
	
//		System.out.println("===========NEW-OPERATOR=============");
//		System.out.println(o);
		return o;
	}
		
//	public void draw() {
//		AbstractTypedGraph<String, String> g;
////		Vector<AbstractTypedGraph<String,String>> history = new Vector<AbstractTypedGraph<String,String>>(); 	
//		
//		HashMap<String,String> edgeLabels = new HashMap<String, String>();
//		g = new DirectedSparseMultigraph<String,String>();
////		GraphTools<String,String> cG = new GraphTools<String,String>();
//		int c = 0;
//		for ( Operator a : A ) {
//			for ( Statement pre : a.getPreconditions() ) {
//				g.addEdge(""+(c++), pre.toString(), a.getNameStateVariable().toString() );
//				edgeLabels.put(""+(c-1), "p");
//			}
//			for ( Statement eff : a.getEffects() ) {
//				g.addEdge(""+(c++), a.getNameStateVariable().toString(), eff.toString());
//				edgeLabels.put(""+(c-1), "e");
//			}
//		}
//		for ( AllenConstraint con : C.get(AllenConstraint.class) ) {
//			AllenConstraint tC = con;
//			g.addEdge(""+(c++), getStatement(tC.getFrom()).toString(), getStatement(tC.getTo()).toString() ); 
//			edgeLabels.put(""+(c-1), tC.getRelation().toString());
//		}
//		
//		new GraphFrame<String,String>(g, null,  "Plan", GraphFrame.LayoutClass.ISOM, edgeLabels);
//	}
	
	/**
	 * Returns a copy of this plan.
	 * @return the copy
	 */
	public Plan copy() {
		Plan pC = new Plan();
		for ( Operator a : A ) {
			pC.addAction(a.copy());
		}
		
		pC.C = this.C.copy();
		return pC;
	}
	
//	private Statement getStatement( Term key ) {
//		for ( Operator a : A ) {
//			for ( Statement p : a.getPreconditions() ) {
//				if ( p.getKey().equals(key) ) {
//					return p;
//				}
//			}
//			for ( Statement e : a.getEffects() ) {
//				if ( e.getKey().equals(key) ) {
//					return e;
//				}
//			}
//		}
//		return null;
//	}
	
//	public boolean hasMatchingNames( Plan p ) {
//		if ( this.getActions().size() != p.getActions().size() ) {
//			return false;
//		}
//		for ( int i = 0 ; i < this.getActions().size() ; i++ ) {
//			Substitution theta = this.getActions().get(i).getName().match(p.getActions().get(i).getName());
//			if ( theta == null ) {
//				return false;
//			}
//		}
//		return true;
//	}

	/**
	 * Apply a substitution to this plan.
	 * @param theta the substitution
	 */
	public void substitute(Substitution theta) {
		for ( Operator a : A ) {
			a.substitute(theta);
		}
		
		C.substitute(theta);
	}
	
	@Override 
	public int hashCode() {
		return this.toString().hashCode();
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof Plan ) {
			Plan p = (Plan)o;
			if ( this.A.size() != p.A.size() ) {
				return false;
			}
			for ( int i = 0 ; i < this.A.size() ; i++ ) {
				if ( !this.A.get(i).equals(p.A.get(i))) {
					return false;
				}
			}
			if ( !this.C.equals(p.C) ) {
				return false;
			}
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		for ( Operator a : A ) {
			r.append(a.toString());
			r.append("\n");
		}
		
		for ( Expression c : C ) {
			r.append(c.toString());
			r.append("\n");
		}
		
		String str = r.toString();
		if ( str.equals("")) {
			return "empty plan";
		}
		return str;
	}
}

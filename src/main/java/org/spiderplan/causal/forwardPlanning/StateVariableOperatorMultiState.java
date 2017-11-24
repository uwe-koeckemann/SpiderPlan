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
package org.spiderplan.causal.forwardPlanning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.spiderplan.representation.expressions.domain.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.GenericComboBuilder;


/**
 * State-variable based operator that uses sequence of values as effects
 * rather than single values. This is used to preserve some temporal information
 * during heuristic forward planning while still being able to use heuristics
 * without change.
 * 
 * @author Uwe Köckemann
 *
 */
public class StateVariableOperatorMultiState {

	private Term name;
	
	private Map<Term,Term> preconditions = new HashMap<Term, Term>(); 
	private Map<Term,List<Term>> effects = new HashMap<Term, List<Term>>();
	
	private Substitution theta = new Substitution();
	
	/**
	 * Get the name of the operator.
	 * @return the name
	 */
	public Term getName() {
		return name;
	}
	
	/**
	 * Set the name of the operator.
	 * @param name the name
	 */
	public void setName(Term name) {
		this.name = name;
	}
	
	/**
	 * Get substitution that was applied to the operator.
	 * @return previously applied substitution
	 */
	public Substitution getSubstitution() {
		return theta;
	}
	
	/**
	 * Set substitution that was applied to the operator.
	 * @param theta the substitution
	 */
	public void setSubstitution( Substitution theta ) {
		this.theta = theta;
	}
	
	/**
	 * Get the preconditions that need to be fulfilled to apply this operator.
	 * @return map from state-variables to values representing the preconditions
	 */
	public Map<Term,Term> getPreconditions() {
		return preconditions;
	}
	
	/**
	 * Get the effects of applying this operator.
	 * @return map from state-variables to sequences of values representing the effects
	 */
	public Map<Term,List<Term>> getEffects() {
		return effects;
	}	
	
//	public Collection<StateVariableOperatorMultiState> getApplicableActionsFromMultiState ( HashMap<Atomic,List<Term>> s, TypeManager tM ) {
//		Collection<StateVariableOperatorMultiState> r = new ArrayList<StateVariableOperatorMultiState>();
//		
//		ArrayList<ArrayList<Substitution>> matchingSubs = new ArrayList<ArrayList<Substitution>>();
//		
//		ArrayList<Atomic> preSVs = new ArrayList<Atomic>();
//		preSVs.addAll(this.preconditions.keySet());
//		
//		for ( int i = 0 ; i < preSVs.size() ; i++ ) {
//			matchingSubs.add( new ArrayList<Substitution>() );
//			
//			Atomic preSV = preSVs.get(i);
//			Term preVal = this.preconditions.get(preSV);
//			
//			for ( Atomic sv : s.keySet() ) {
//				Substitution theta = preSV.match(sv);
//				if ( theta != null ) {
//					for ( Term possibleStateVal : s.get(sv) ) {
//						Substitution theta1 = theta.copy();
//						
//						if ( theta1.add( preVal.match(possibleStateVal) ) ) {
//							matchingSubs.get(i).add(theta1);
//						}
//					}
//				}				
//			}
//		}
//		
//		GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
//		
//		ArrayList<ArrayList<Substitution>> combos = cB.getCombos(matchingSubs);
//		
//		for ( ArrayList<Substitution> combo : combos ) {
//			Substitution theta = new Substitution();
//			
//			/**
//			 * Check if this is a working substitution 
//			 * (not assigning different values to the same variable)
//			 */
//			boolean working = true;
//			for ( Substitution theta1 : combo ) {
//				working = theta.add(theta1);
//				if ( !working ) {
//					break;
//				}
//			}
//			
//			if ( working ) {	
//				/**
//				 * May still not be ground, because preconditions do not constrain every variable,
//				 * so we get all Substitutions that make name ground and create an action for 
//				 * each of them.
//				 */
//				Atomic nameCopy = this.getName().substitute(theta);
//				Collection<Substitution> groundSubst = tM.getAllGroundSubstitutions(nameCopy);
//				
//				if ( !groundSubst.isEmpty() ) {
//					for ( Substitution theta1 : groundSubst ) {		
//						Substitution thetaGround = theta.copy();
//						working = thetaGround.add(theta1);
//						
//						if ( working && this.isCompatibleSubstitution(theta) ) {							
//							StateVariableOperatorMultiState svoCopy = this.copy();
//							svoCopy.substitute(thetaGround);
//							r.add(svoCopy);
//						}
//					}
//				} else {
//					if ( this.isCompatibleSubstitution(theta) ) {
//						StateVariableOperatorMultiState svoCopy = this.copy();
//						svoCopy.substitute(theta);
//						r.add(svoCopy);
//					}
//				}
//			}
//		}
//		
//		return r;
//	}
//	
	/**
	 * Find all applicable (partially) ground operators to a given state. Only grounds variables if they appear in preconditions.
	 * @param s the state
	 * @param tM type manager
	 * @return substitutions of this operator that are applicable to the given state 
	 */
	public Collection<StateVariableOperatorMultiState> getApplicablePartialGroundFromMultiState ( HashMap<Term,Collection<Term>> s, TypeManager tM ) {
		Collection<StateVariableOperatorMultiState> r = new ArrayList<StateVariableOperatorMultiState>();
		
		/**
		 * If there are no preconditions we can not ground any variables and just return
		 * this operator.
		 */
		if ( this.preconditions.isEmpty() ) {
			r.add( this.copy() );
			return r;
		}
		  
		HashMap<Term,Type> realTypeLookUp = new HashMap<Term, Type>();
		
		for ( int i = 0 ; i < this.name.getNumArgs() ; i++ ) {
			realTypeLookUp.put( this.name.getArg(i), tM.getPredicateTypes(this.name.getUniqueName(), i) );
		}
		
		List<List<Substitution>> matchingSubs = new ArrayList<List<Substitution>>();
		
		ArrayList<Term> preSVs = new ArrayList<Term>();
		preSVs.addAll(this.preconditions.keySet());
		for ( int i = 0 ; i < preSVs.size() ; i++ ) {
			matchingSubs.add( new ArrayList<Substitution>() );
			
			Term preSV = preSVs.get(i);
			
			
			
			Term preVal = this.preconditions.get(preSV);
			
			for ( Term sv : s.keySet() ) {
				Substitution theta = preSV.match(sv);
						
				if ( theta != null ) {
					
					boolean working = true;
					/**
					 * Check if assigned values are really in domain and discard if not.
					 */
					Term nameCopy = this.getName().substitute(theta);
					
					for ( int j = 0 ; j < nameCopy.getNumArgs() ; j++ ) {
						Term t = nameCopy.getArg(j);
						if ( t.isGround() ) {
							if ( !tM.getPredicateTypes( nameCopy.getUniqueName(), j).contains(t, tM) ) {
								working = false;
								break;
							}
						}
					}
					
					if ( working ) {
						for ( Term possibleStateVal : s.get(sv) ) {
							Substitution theta1 = theta.copy();

							if ( theta1.add( preVal.match(possibleStateVal) ) ) {
								matchingSubs.get(i).add(theta1);
							}
						}
					}
				}				
			}
			if ( matchingSubs.get(i).isEmpty() ) {		// No matching state variable assignment for this precondition
				return r;
			}
		}
		
		GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
	
		List<List<Substitution>> combos = cB.getCombos(matchingSubs);
		
		for ( List<Substitution> combo : combos ) {
			Substitution theta = new Substitution();
			
			/**
			 * Check if this is a working substitution 
			 * (not assigning different values to the same variable)
			 */
			boolean working = true;
			for ( Substitution theta1 : combo ) {
				working = theta.add(theta1);
				if ( !working ) {
					break;
				}
			}
			
			working = working && this.isCompatibleSubstitution(theta);
			
			if ( working ) {	
				StateVariableOperatorMultiState svoCopy = this.copy();
				svoCopy.substitute(theta);
				
				/**
				 * Check if assigned values are really in domain and discard if not.
				 */
				for ( int i = 0 ; i < svoCopy.getName().getNumArgs() ; i++ ) {
					Term t = svoCopy.getName().getArg(i);
					if ( t.isGround() ) {
						if ( !tM.getPredicateTypes( svoCopy.getName().getUniqueName(), i).contains(t, tM) ) {
							working = false;
							break;
						}
					}
				}
				
				if ( working ) {
					r.add(svoCopy);
				}
			}
		}
		
		return r;
	}
	/**
	 * Get all actions by creating combinations of
	 * assignments to all open variables. 
	 * 
	 * @param tM {@link TypeManager} that knows the domains of variables.
	 * @return
	 */
//	public Collection<StateVariableOperatorMultiState> getAllGround ( TypeManager tM ) {
//		Collection<StateVariableOperatorMultiState> r = new ArrayList<StateVariableOperatorMultiState>();
//		
//		if ( this.name.isGround() ) {
//			r.add(this);
//			return r;
//		}
//		
//		Collection<Substitution> subst = tM.getAllGroundSubstitutions(this.getName());
//		
//		for ( Substitution theta : subst ) {
//			StateVariableOperatorMultiState svoCopy = this.copy();
//			svoCopy.substitute(theta);
//			r.add(svoCopy);
//		}
//		
//		return r;
//	}
	
	

	
//	public boolean isMutex( StateVariableOperatorMultiState a ) {
//		// Competing needs
//		for ( Atomic p1 : a.getPreconditions().keySet() ) {
//			if ( this.getPreconditions().containsKey(p1) && ! this.getPreconditions().get(p1).equals(a.getPreconditions().get(p1)) ) {
//				return true;
//			}
//		}
//		// Competing effects
//		for ( Atomic e1 : a.getEffects().keySet() ) {
//			if ( this.getEffects().containsKey(e1) && ! this.getEffects().get(e1).equals(a.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//		// Interference with precondition of this
//		for ( Atomic e1 : a.getEffects().keySet() ) {
//			if ( this.getPreconditions().containsKey(e1) && ! this.getPreconditions().get(e1).equals(a.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//
//		// Interference with preconditions of a
//		for ( Atomic e1 : this.getEffects().keySet() ) {
//			if ( a.getPreconditions().containsKey(e1) && ! a.getPreconditions().get(e1).equals(this.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//
//		
//		return false;
//	}

	/**
	 * Apply a substitution to this operator.
	 * @param theta substitution
	 */
	public void substitute( Substitution theta ) {
		HashMap<Term,Term> newPre = new HashMap<Term, Term>();
		HashMap<Term,List<Term>> newEff = new HashMap<Term, List<Term>>();

		this.name = this.name.substitute(theta);
		
		for ( Term a : this.preconditions.keySet() ) {
			Term varSub = a.substitute(theta);
			Term valSub = this.preconditions.get(a);
			valSub = valSub.substitute(theta);

			newPre.put(varSub, valSub);
		}
		
		for ( Term a : this.effects.keySet() ) {
			Term varSub = a.substitute(theta);
			List<Term> effCopy = new ArrayList<Term>();
			for ( Term e : this.effects.get(a) ) {
				Term newEffTerm = e;
				newEffTerm = newEffTerm.substitute(theta);
				effCopy.add(newEffTerm);
			}

			newEff.put(varSub, effCopy);
		}
		
		this.effects = newEff;
		this.preconditions = newPre;
		
		if ( this.theta != null ) {
			this.theta.add(theta);
		} else {
			this.theta = theta;
		}	
	}
	
	/**
	 * Copy this operator.
	 * @return a copy
	 */
	public StateVariableOperatorMultiState copy() {
		HashMap<Term,Term> newPre = new HashMap<Term, Term>();
		HashMap<Term,List<Term>> newEff = new HashMap<Term, List<Term>>();
		
		StateVariableOperatorMultiState c = new StateVariableOperatorMultiState();
		
		c.setName(this.name);
		
		for ( Term a : this.preconditions.keySet() ) {
			Term valSub = this.preconditions.get(a);
			newPre.put(a, valSub);
		}
		
		for ( Term a : this.effects.keySet() ) {
			List<Term> effCopy = new ArrayList<Term>();
			for ( Term e : this.effects.get(a) ) {
				Term newEffTerm = e.substitute(theta);
				effCopy.add(newEffTerm);
			}
			
			newEff.put(a, effCopy);
		}
		
		c.effects = newEff;
		c.preconditions = newPre;
		
		if ( this.theta != null ) { 
			c.theta = this.theta.copy();
		} else {
			c.theta = null;
		}
		
		return c;
	}
	
//	public boolean isMutex( StateVariableOperatorMultiState a ) {
//		// Competing needs
//		for ( Atomic p1 : a.getPreconditions().keySet() ) {
//			if ( this.getPreconditions().containsKey(p1) && ! this.getPreconditions().get(p1).equals(a.getPreconditions().get(p1)) ) {
//				return true;
//			}
//		}
//		// Competing effects
//		for ( Atomic e1 : a.getEffects().keySet() ) {
//			if ( this.getEffects().containsKey(e1) && ! this.getEffects().get(e1).equals(a.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//		// Interference with precondition of this
//		for ( Atomic e1 : a.getEffects().keySet() ) {
//			if ( this.getPreconditions().containsKey(e1) && ! this.getPreconditions().get(e1).equals(a.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//
//		// Interference with preconditions of a
//		for ( Atomic e1 : this.getEffects().keySet() ) {
//			if ( a.getPreconditions().containsKey(e1) && ! a.getPreconditions().get(e1).equals(this.getEffects().get(e1)) ) {
//				return true;
//			}
//		}
//
//		
//		return false;
//	}

	@Override
	public String toString() {
		String r = name.toString() + "\n";
		r += "Preconditions: \n";
		
		ArrayList<String> preOrdered = new ArrayList<String>();
		for ( Term v : this.preconditions.keySet() ) {
			preOrdered.add(v.toString() + " <- " + preconditions.get(v));
		}
		ArrayList<String> effOrdered = new ArrayList<String>();
		for ( Term v : this.effects.keySet() ) {
			effOrdered.add(v.toString() + " <- " + effects.get(v));
		}
		Collections.sort(preOrdered);
		Collections.sort(effOrdered);		
		
		for ( String s : preOrdered ) {
			r += s + "\n";
		}
		r += "Effects: \n";
		for ( String s : effOrdered  ) {
			r += s + "\n";
		}
		
		return r;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof StateVariableOperatorMultiState ) {
			StateVariableOperatorMultiState o2 = (StateVariableOperatorMultiState)o;
			return o2.name.equals(this.name); // && o2.preconditions.equals(this.preconditions) && o2.effects.equals(this.effects);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
//		String s = name.toString() + "," + preconditions.hashCode() + "," + effects.hashCode();
		return name.hashCode();
	}
	
	/**
	 * {@link Substitution} is only compatible if it does not merge
	 * two distinct preconditions or effects into one.
	 * @param theta
	 * @return
	 */
	private boolean isCompatibleSubstitution( Substitution theta ) {
		HashSet<Term> varSet = new HashSet<Term>();
		
		for ( Term a : this.preconditions.keySet() ) {
			Term c = a.substitute(theta);
			if ( varSet.contains(c) ) {
				return false;
			}
			varSet.add(c);
		}
		varSet = new HashSet<Term>();
		for ( Term a : this.effects.keySet() ) {
			Term c = a.substitute(theta);
			if ( varSet.contains(c) ) {
				return false;
			}
			varSet.add(c);
		}
		return true;
	}
}

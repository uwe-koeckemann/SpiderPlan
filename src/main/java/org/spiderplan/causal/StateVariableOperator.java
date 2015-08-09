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
package org.spiderplan.causal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.Type;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.tools.GenericComboBuilder;


public class StateVariableOperator {

	private Atomic name;
	
	private Map<Atomic,Term> preconditions = new HashMap<Atomic, Term>(); 
	private Map<Atomic,Term> effects = new HashMap<Atomic, Term>();
	
	private Substitution theta;
	
	public Atomic getName() {
		return name;
	}
	
	public void setName(Atomic name) {
		this.name = name;
	}
	
	public Substitution getSubstitution() {
		return theta;
	}
	
	public void setSubstitution( Substitution theta ) {
		this.theta = theta;
	}
	
	public Map<Atomic,Term> getPreconditions() {
		return preconditions;
	}
	
	public Map<Atomic,Term> getEffects() {
		return effects;
	}	
	
	public Collection<StateVariableOperator> getApplicableActions ( Map<Atomic,Term> s ) {
		Collection<StateVariableOperator> r = new ArrayList<StateVariableOperator>();
		
		ArrayList<ArrayList<Substitution>> matchingSubs = new ArrayList<ArrayList<Substitution>>();
		
		ArrayList<Atomic> preSVs = new ArrayList<Atomic>();
		preSVs.addAll(this.preconditions.keySet());
		
		for ( int i = 0 ; i < preSVs.size() ; i++ ) {
			matchingSubs.add( new ArrayList<Substitution>() );
			
			Atomic preSV = preSVs.get(i);
			Term preVal = this.preconditions.get(preSV);
			
			for ( Atomic sv : s.keySet() ) {
				Substitution theta = preSV.match(sv);
				if ( theta != null ) {
					if ( theta.add( preVal.match(s.get(sv)) ) ) {
						matchingSubs.get(i).add(theta);
					}
				}				
			}
		}
		
		GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
		
		ArrayList<ArrayList<Substitution>> combos = cB.getCombos(matchingSubs);
		
		for ( ArrayList<Substitution> combo : combos ) {
			Substitution theta = new Substitution();
			
			boolean working = true;
			for ( Substitution theta1 : combo ) {
				working = theta.add(theta1);
				if ( !working ) {
					break;
				}
			}
			
			if ( working && this.isCompatibleSubstitution(theta) ) {
				StateVariableOperator svoCopy = this.copy();
				svoCopy.substitute(theta);
				r.add(svoCopy);
			}
		}
		
		return r;
	}
	
	public Collection<StateVariableOperator> getApplicableActionsFromMultiState ( HashMap<Atomic,Collection<Term>> s, TypeManager tM ) {
		Collection<StateVariableOperator> r = new ArrayList<StateVariableOperator>();
		
		ArrayList<ArrayList<Substitution>> matchingSubs = new ArrayList<ArrayList<Substitution>>();
		
		ArrayList<Atomic> preSVs = new ArrayList<Atomic>();
		preSVs.addAll(this.preconditions.keySet());
		
		for ( int i = 0 ; i < preSVs.size() ; i++ ) {
			matchingSubs.add( new ArrayList<Substitution>() );
			
			Atomic preSV = preSVs.get(i);
			Term preVal = this.preconditions.get(preSV);
			
			for ( Atomic sv : s.keySet() ) {
				Substitution theta = preSV.match(sv);
				if ( theta != null ) {
					for ( Term possibleStateVal : s.get(sv) ) {
						Substitution theta1 = theta.copy();
						
						if ( theta1.add( preVal.match(possibleStateVal) ) ) {
							matchingSubs.get(i).add(theta1);
						}
					}
				}				
			}
		}
		
		GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
		
		ArrayList<ArrayList<Substitution>> combos = cB.getCombos(matchingSubs);
		
		for ( ArrayList<Substitution> combo : combos ) {
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
			
			if ( working ) {	
				/**
				 * May still not be ground, because preconditions do not constrain every variable,
				 * so we get all Substitutions that make name ground and create an action for 
				 * each of them.
				 */
				Atomic nameCopy = this.getName().substitute(theta);
				Collection<Substitution> groundSubst = tM.getAllGroundSubstitutions(nameCopy);
				
				if ( !groundSubst.isEmpty() ) {
					for ( Substitution theta1 : groundSubst ) {		
						Substitution thetaGround = theta.copy();
						working = thetaGround.add(theta1);
						
						if ( working && this.isCompatibleSubstitution(theta) ) {							
							StateVariableOperator svoCopy = this.copy();
							svoCopy.substitute(thetaGround);
							r.add(svoCopy);
						}
					}
				} else {
					if ( this.isCompatibleSubstitution(theta) ) {
						StateVariableOperator svoCopy = this.copy();
						svoCopy.substitute(theta);
						r.add(svoCopy);
					}
				}
			}
		}
		
		return r;
	}
	
	/**
	 * Given {@link Atomic} variables with a set of possible value {@link Term}s find 
	 * all applicable (partially) ground operators. Only grounds variables if they
	 * appear in preconditions.
	 * @param s
	 * @return
	 */
	public Collection<StateVariableOperator> getApplicablePartialGroundFromMultiState ( HashMap<Atomic,Collection<Term>> s, TypeManager tM ) {
			
		Collection<StateVariableOperator> r = new ArrayList<StateVariableOperator>();
		
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
		
		ArrayList<Atomic> preSVs = new ArrayList<Atomic>();
		preSVs.addAll(this.preconditions.keySet());
		for ( int i = 0 ; i < preSVs.size() ; i++ ) {
			matchingSubs.add( new ArrayList<Substitution>() );
			
			Atomic preSV = preSVs.get(i);
			Term preVal = this.preconditions.get(preSV);
			
			for ( Atomic sv : s.keySet() ) {
				Substitution theta = preSV.match(sv);
						
				if ( theta != null ) {					
					boolean working = true;
					/**
					 * Check if assigned values are really in domain and discard if not.
					 */
					Atomic nameCopy = this.getName().substitute(theta);

					for ( int j = 0 ; j < nameCopy.getNumArgs() ; j++ ) {
						Term t = nameCopy.getArg(j);
						if ( t.isGround() ) {
							if ( !tM.getPredicateTypes( nameCopy.getUniqueName(), j).contains(t) ) {
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

		GenericSingleNodeSearch<Substitution> search = new GenericSingleNodeSearch<Substitution>(matchingSubs);
		int count = 0;
		int countApp = 0;
		while ( true ) {
			Substitution theta = new Substitution();
			boolean working = true;
			for ( Substitution sub : search.getAssignment() ) {
				if ( !theta.add(sub) ) {
					working = false;
					break;
				}
			}
			
			if ( search.success() && working ) {
				working = working && this.isCompatibleSubstitution(theta);
				if ( working ) {
					StateVariableOperator svoCopy = this.copy();
					svoCopy.substitute(theta);
					
					/**
					 * Check if assigned values are really in domain and discard if not.
					 */
					for ( int i = 0 ; i < svoCopy.getName().getNumArgs() ; i++ ) {
						Term t = svoCopy.getName().getArg(i);
						if ( t.isGround() ) {
							if ( !tM.getPredicateTypes( svoCopy.getName().getUniqueName(), i).contains(t) ) {
								working = false;
								break;
							}
						}
					}
					
					if ( working ) {
						r.add(svoCopy);
						countApp++;
						working = false;
					}
				}
			}
			count++;
			boolean done = search.advance(working);
			
			if ( done && search.failure() ) {
				break;	// nothing left to try
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
	public Collection<StateVariableOperator> getAllGround ( TypeManager tM ) {
		Collection<StateVariableOperator> r = new ArrayList<StateVariableOperator>();
		
		if ( this.name.isGround() ) {
			r.add(this);
			return r;
		}
		
		Collection<Substitution> subst = tM.getAllGroundSubstitutions(this.getName());
		
		for ( Substitution theta : subst ) {
			StateVariableOperator svoCopy = this.copy();
			svoCopy.substitute(theta);
			r.add(svoCopy);
		}
		
		return r;
	}
	/**
	 * {@link Substitution} is only compatible if it does not merge
	 * two distinct preconditions or effects into one.
	 * @param theta
	 * @return
	 */
	public boolean isCompatibleSubstitution( Substitution theta ) {
		HashSet<Atomic> varSet = new HashSet<Atomic>();
		
		for ( Atomic a : this.preconditions.keySet() ) {
			Atomic c = a.substitute(theta);
			if ( varSet.contains(c) ) {
				return false;
			}
			varSet.add(c);
		}
		varSet = new HashSet<Atomic>();
		for ( Atomic a : this.effects.keySet() ) {
			Atomic c = a.substitute(theta);
			if ( varSet.contains(c) ) {
				return false;
			}
			varSet.add(c);
		}
		return true;
	}

	public void substitute( Substitution theta ) {
		HashMap<Atomic,Term> newPre = new HashMap<Atomic, Term>();
		HashMap<Atomic,Term> newEff = new HashMap<Atomic, Term>();

		this.name = this.name.substitute(theta);
		
		for ( Atomic a : this.preconditions.keySet() ) {
			Atomic varSub = a.substitute(theta);
			Term valSub = this.preconditions.get(a);
			valSub = valSub.substitute(theta);

			newPre.put(varSub, valSub);
		}
		
		for ( Atomic a : this.effects.keySet() ) {
			Atomic varSub = a.substitute(theta);
			Term valSub = this.effects.get(a);
			valSub = valSub.substitute(theta);

			newEff.put(varSub, valSub);
		}
		
		this.effects = newEff;
		this.preconditions = newPre;
		
		if ( this.theta != null ) {
			this.theta.add(theta);
		} else {
			this.theta = theta;
		}	
	}
	
	public StateVariableOperator copy() {
		HashMap<Atomic,Term> newPre = new HashMap<Atomic, Term>();
		HashMap<Atomic,Term> newEff = new HashMap<Atomic, Term>();
		
		StateVariableOperator c = new StateVariableOperator();
		
		c.setName(this.name);
		
		for ( Atomic a : this.preconditions.keySet() ) {
			Term valSub = this.preconditions.get(a);
			newPre.put(a, valSub);
		}
		
		for ( Atomic a : this.effects.keySet() ) {
			Term valSub = this.effects.get(a);
			newEff.put(a, valSub);
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
	
	public boolean isMutex( StateVariableOperator a ) {
		// Competing needs
		for ( Atomic p1 : a.getPreconditions().keySet() ) {
			if ( this.getPreconditions().containsKey(p1) && ! this.getPreconditions().get(p1).equals(a.getPreconditions().get(p1)) ) {
				return true;
			}
		}
		// Competing effects
		for ( Atomic e1 : a.getEffects().keySet() ) {
			if ( this.getEffects().containsKey(e1) && ! this.getEffects().get(e1).equals(a.getEffects().get(e1)) ) {
				return true;
			}
		}
		// Interference with precondition of this
		for ( Atomic e1 : a.getEffects().keySet() ) {
			if ( this.getPreconditions().containsKey(e1) && ! this.getPreconditions().get(e1).equals(a.getEffects().get(e1)) ) {
				return true;
			}
		}

		// Interference with preconditions of a
		for ( Atomic e1 : this.getEffects().keySet() ) {
			if ( a.getPreconditions().containsKey(e1) && ! a.getPreconditions().get(e1).equals(this.getEffects().get(e1)) ) {
				return true;
			}
		}

		
		return false;
	}

	@Override
	public String toString() {
		String r = name.toString() + "\n";
		r += "Preconditions: \n";
		
		for ( Atomic v : this.preconditions.keySet() ) {
			r += v.toString() + " <- " + preconditions.get(v).toString() + "\n";
		}
		r += "Effects: \n";
		for ( Atomic v : this.effects.keySet() ) {
			r += v.toString() + " <- " + effects.get(v).toString() + "\n";
		}
		
		return r;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof StateVariableOperator ) {
			StateVariableOperator o2 = (StateVariableOperator)o;
			return o2.name.equals(this.name); // && o2.preconditions.equals(this.preconditions) && o2.effects.equals(this.effects);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
//		String s = name.toString() + "," + preconditions.hashCode() + "," + effects.hashCode();
		return name.hashCode();
	}
}

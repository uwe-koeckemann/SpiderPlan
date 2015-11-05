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
package org.spiderplan.representation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.causal.forwardPlanning.StateVariableOperator;
import org.spiderplan.causal.forwardPlanning.StateVariableOperatorMultiState;
import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.minizinc.MiniZincInput;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.expressions.resources.ReusableResourceCapacity;
import org.spiderplan.representation.expressions.temporal.AllenConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.types.TypeManager;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.UniqueID;



/**
 * An operator describes a template of change from one {@link ConstraintDatabase} 
 * into another.
 * 
 * @author Uwe Köckemann
 *
 */
public class Operator extends Expression implements Substitutable {
	
	final private static Term ConstraintType = Term.createConstant("action");
		
	final private static Term ThisInterval = Term.createVariable("THIS");
	final private static Term Time1 = Term.createInteger(1);
	final private static Term True = Term.createConstant("true");
	
	private Atomic name;
	private Term intervalKey = ThisInterval;
	private Substitution theta = new Substitution();
	
	private ArrayList<Statement> P = new ArrayList<Statement>();
	private ArrayList<Statement> E = new ArrayList<Statement>();
//	private ArrayList<Constraint> C = new ArrayList<Constraint>();
	private ConstraintDatabase C = new ConstraintDatabase();
	
	/**
	 * Default constructor
	 */
	public Operator() { super(ConstraintType); }
	
	/**
	 * Set the name variable of the operator.
	 * @param name An {@link Atomic} representing the name.
	 */
	public void setName( Atomic name  ) {
		this.name = name;
	}
	
	/**
	 * Get the name variable of the operator.
	 * @return An {@link Atomic} variable.
	 */
	public Atomic getName() {
		return this.name;
	}
		
	/**
	 * Set the interval key {@link Term} of this operator.
	 * @param intervalKey A {@link Term} representing the interval during which the operator is executed.
	 */
	public void setLabel( Term intervalKey ) {
		this.intervalKey = intervalKey;
	}
	
	/**
	 * Get the interval key {@link Term} of this operator.
	 * @return A {@link Term} representing the interval during which the operator is executed.
	 */
	public Term getLabel() {
		return intervalKey;
	}
	
	/**
	 * Add a precondition {@link Statement} to this {@link Operator}. 
	 * @param p A {@link Statement} which has to be present in a {@link ConstraintDatabase} in order
	 * to apply this {@link Operator}.
	 */
	public void addPrecondition( Statement p ) {
		P.add(p);
	}
	
	/**
	 * Add an effect {@link Statement} to this {@link Operator}.
	 * @param e An effect that will be added to a {@link ConstraintDatabase} if this {@link Operator}
	 * is applied to it.
	 */
	public void addEffect( Statement e ) {
		E.add(e);
	}
	
	/**
	 * Add a {@link Expression} to this {@link Operator}.
	 * @param c A {@link Expression} that has to be satisfied in order to apply this {@link Operator}.
	 */
	public void addConstraint( Expression c ) {
		C.add(c);
	}
	
	/**
	 * Add multiple precondition {@link Statement}s at once.
	 * @param P A {@link Collection} of {@link Statement}s that have to be 
	 * present in a {@link ConstraintDatabase}
	 * in order to apply this {@link Operator}.
	 */
	public void addPreconditions( Collection<Statement> P ) {
		this.P.addAll(P);
	}
	
	/**
	 * Add multiple effect {@link Statement}s at once.
	 * @param E A {@link Collection} of {@link Statement}s that will be added to a
	 * {@link ConstraintDatabase}
	 * if this {@link Operator} is applied to it.
	 */
	public void addEffects( Collection<Statement> E ) {
		this.E.addAll(E);
	}
	
	/**
	 * multiple {@link Expression}s to this {@link Operator}.
	 * @param C A {@link Collection} of {@link Expression}s that have to be satisfied in order 
	 * to apply this {@link Operator}.
	 */
	public void addConstraints( Collection<Expression> C ) {
		this.C.addAll(C);
	}
	
	/**
	 * Get all precondition {@link Statement}s of this {@link Operator}.
	 * @return A {@link Collection} of preconditions that have to be 
	 * present in a {@link ConstraintDatabase} in order to apply this {@link Operator} to it.
	 */
	public ArrayList<Statement> getPreconditions( ) {
		return this.P;
	}
	
	/**
	 * Get all effect {@link Statement}s of this {@link Operator}.
	 * @return A {@link Collection} of effects that will be added to 
	 * a {@link ConstraintDatabase} when this {@link Operator} is applied to it.
	 */
	public ArrayList<Statement> getEffects( ) {
		return this.E;
	}
	
	/**
	 * Get all {@link Expression}s of this {@link Operator}. 
	 * @return A {@link Collection} of {@link Expression} that have to be satisfied in order to apply
	 * this {@link Operator}. 
	 */
	public ConstraintDatabase getConstraints( ) {
		return this.C;
	}
	
	@Override
	public Collection<Term> getVariableTerms() {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Statement s : this.getPreconditions() ) {
			r.addAll(s.getVariableTerms());
		}
		for ( Statement s : this.getEffects() ) {
			r.addAll(s.getVariableTerms());
		}
		for ( Expression c : this.getConstraints() ) {
			r.addAll(c.getVariableTerms() );
		}
		return r;		
	}
	@Override
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Statement s : this.getPreconditions() ) {
			r.addAll(s.getGroundTerms());
		}
		for ( Statement s : this.getEffects() ) {
			r.addAll(s.getGroundTerms());
		}
		for ( Expression c : this.getConstraints() ) {
			r.addAll(c.getGroundTerms() );
		}
		return r;
	}
	@Override
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		for ( Statement s : this.getPreconditions() ) {
			r.add(s.getVariable());
		}
		for ( Statement s : this.getEffects() ) {
			r.add(s.getVariable());
		}
		for ( Expression c : this.getConstraints() ) {
			r.addAll(c.getAtomics());
		}
		return r;
	}
	
	/**
	 * Checks if precondition {@link Statement}s are ground.
	 * @return <i>true</i> if all preconditions are ground, <i>false</i> otherwise.
	 */
	public boolean preconditionsGround() {
		for ( Statement s : P ) {
			if ( !s.isGround() ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if effect {@link Statement}s are ground.
	 * @return <i>true</i> if all preconditions are ground, <i>false</i> otherwise.
	 */
	public boolean effectsGround() {
		for ( Statement s : E ) {
			if ( !s.isGround() ) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Checks if all elements of this {@link Operator} are ground.
	 * @return <i>true</i> iff name, preconditions, effects and constraints are ground, <i>false</i> otherwise.
	 */
	public boolean isGround() {
		if ( !this.name.isGround() ) { 
			return false;
		} 
		
		for ( Statement p : P ) {
			if ( !p.isGround() ) {
				return false;
			}
		}
		for ( Statement e : E ) {
			if ( !e.isGround() ) {
				return false;
			}
		}			
		
		for ( Expression c : C ) {
			if ( c instanceof Matchable ) {
				Matchable cLit = (Matchable)c;
				if ( !cLit.isGround() ) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Get the {@link Statement} representing the execution interval of this {@link Operator}.
	 * @return a {@link Statement} that represents the execution of this {@link Operator} during 
	 * a temporal interval.
	 */
	public Statement getNameStateVariable() {
		Statement name = new Statement(this.intervalKey,this.name, True);
		return name;
	}
	
	/**
	 * Get {@link Operator}s whose preconditions are a subset of a {@link Collection} of {@link Statement}s.
	 * @param F a {@link Collection} of {@link Statement}s.
	 * @param tM the {@link TypeManager}.
	 * @return a {@link Collection} of {@link Operator}s whose preconditions are a subset of <i>F</i>
	 */
	public Collection<Operator> getApplicable( Collection<Statement> F, TypeManager tM ) {
		ArrayList<Operator> r = new ArrayList<Operator>();

		ArrayList<ArrayList<Substitution>> subs = new ArrayList<ArrayList<Substitution>>();
		
		boolean foundAtLeastOneCombo = true;
		
		int index = 0;
		for ( Statement p : this.P ) {
			subs.add(new ArrayList<Substitution>());
			index = subs.size()-1;

			for ( Statement s : F ) {
				Substitution theta = new Substitution();
				theta = p.match(s);
				if ( theta != null ) {
					subs.get(index).add(theta);
				}
			}
			if ( subs.get(index).isEmpty() ) {
				foundAtLeastOneCombo = false;
				break;
			}
		}
		
		if ( foundAtLeastOneCombo ) {
			GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
			
			ArrayList<ArrayList<Substitution>> combos = cB.getCombos(subs);
			
			for ( ArrayList<Substitution> thetaCombo : combos ) {
				Substitution theta = new Substitution();
				boolean success = true;
				for ( Substitution part : thetaCombo ) {
					if ( !theta.add(part) ) {
						success = false;
						break;
					}
				}
				if ( success ) {
					Operator oCopy = this.copy();
					oCopy.substitute(theta);
					r.add(oCopy);
				}
			}
		}
		
		return r;
	}
	
	/**
	 * Make all interval key {@link Term}s used in this {@link Operator} unique
	 * by attaching <i>ID</i> to them. 
	 * @param ID A number that is attached to all key {@link Term}s.
	 */
//	public void makeUniqueKeys( long ID ) {
//		if ( !intervalKey.isGround() ) {
//			intervalKey = intervalKey.makeUnique(ID);
//		}
//	
//		for ( Statement p : P ) {
//			if ( !p.getKey().isGround() ) {
//				p.setKey(p.getKey().makeUnique(ID));
////				p.setKey( new Term( p.getKey().makeUnique(ID) );
//			}
//		}
//		for ( Statement e : E ) {
//			if ( !e.getKey().isGround() ) {
//				e.setKey(e.getKey().makeUnique(ID));
////				e.setKey( new Term( e.getKey().toString() + "_" + ID));	
//			}			
//		}
//		for ( Constraint c : C ) {
//			if ( c instanceof AllenConstraint ) {
//				AllenConstraint tC = (AllenConstraint)c;
//				if ( !tC.getFrom().isGround() ) {
//					tC.setFrom(tC.getFrom().makeUnique(ID));
////					tC.setFrom( new Term(tC.getFrom().toString() + "_" + ID) );	
//				}	
//				
//				if ( tC.isBinary() ) {
//					if ( !tC.getTo().isGround() ) {
//						tC.setTo(tC.getTo().makeUnique(ID));
////						tC.setTo( new Term(tC.getTo().toString() +  "_" + ID) );
//					}
//				}
//			}
//		}
//	}
	
	/**
	 * Make all interval key {@link Term}s used in this {@link Operator}'s effects unique
	 * by attaching <i>ID</i> to them. 
	 * @param ID A number that is attached to all key {@link Term}s.
	 */
	public void makeUniqueEffectKeys( long ID ) {
		Substitution sub = new Substitution();
		
		if ( !intervalKey.isGround() ) {
			sub.add(intervalKey,intervalKey.makeUnique(ID));
		}
		for ( Statement p : P ) {
			if ( !p.isGround() ) {
				sub.add(p.getKey(),p.getKey().makeUnique(ID));
			}
		}
		for ( Statement e : E ) {
			if ( !e.isGround() ) {
				sub.add(e.getKey(),e.getKey().makeUnique(ID));
			}
		}
		
		this.substitute(sub);
		
//		for ( Constraint c : C ) {
//			if ( c instanceof AllenConstraint ) {
//				AllenConstraint tC = (AllenConstraint)c;
//				if ( !tC.getFrom().isGround() ) {
//					tC.getFrom().makeUnique(ID);
////					tC.setFrom( new Term(tC.getFrom().toString() + "_" + ID) );
//				}
//				if ( tC.isBinary() && !tC.getTo().isGround() ) {
//					tC.getTo().makeUnique(ID);
////					tC.setTo( new Term(tC.getTo().toString() +  "_" + ID) );
//				}
//			}
//		}
	}
	
	/**
	 * Make all interval key {@link Term}s ground.
	 */
	public void makeKeysGround() {
		Substitution sub = new Substitution();
		if ( this.intervalKey.isVariable() ) {
			sub.add(this.intervalKey, this.intervalKey.makeConstant());
		}
		
		for ( Statement e : E ) {
			sub.add(e.getKey(), e.getKey().makeConstant());
		}
		for ( Statement p : P ) {
			sub.add(p.getKey(), p.getKey().makeConstant());
		}
		this.substitute(sub);
	}
	
	/**
	 * Ground the terms of all interval keys of
	 * effect statements.
	 */
	public void makeEffectIntervalKeysGround() {
		Substitution sub = new Substitution();
		if ( this.intervalKey.isVariable() ) {
			sub.add(this.intervalKey, this.intervalKey.makeConstant());
		}
		
		for ( Statement e : E ) {
			sub.add(e.getKey(), e.getKey().makeConstant());
		}
		this.substitute(sub);
	}
	
	/**
	 * Make all variable terms of this operator unique. 
	 * Needed to make sure that adding a partially ground
	 * operator does not cause problems because it uses
	 * existing variable names.
	 * @param ID An ID (usually acquired from {@link UniqueID}).
	 */
	public void makeUniqueVariables( long ID ) {
		Set<Term> vars = new HashSet<Term>();
		vars.addAll(this.getName().getVariableTerms());
//		if ( this.intervalKey.isVariable() )
//			vars.add(this.intervalKey);
		for ( Statement s : this.getPreconditions() ) {
//			if ( s.getKey().isVariable() ) {
//				vars.add(s.getKey());
//			}
			vars.addAll(s.getVariable().getVariableTerms());
			vars.addAll(s.getValue().getVariables());
		}
		for ( Statement s : this.getEffects() ) {
//			if ( s.getKey().isVariable() ) {
//				vars.add(s.getKey());
//			}
			vars.addAll(s.getVariable().getVariableTerms());
			vars.addAll(s.getValue().getVariables());
		}
		for ( Expression c : this.getConstraints() ) {
			vars.addAll(c.getVariableTerms());
		}
		Substitution theta = new Substitution();
		for ( Term var : vars ) {
			theta.add(var, var.makeUnique(ID));
		}
		this.substitute(theta);
	}
	
	/**
	 * Checks if this operator has a precondition or effect
	 * with interval {@link Term} <i>key</i>.
	 * @param key The key {@link Term} of an interval.
	 * @return <i>true</i> iff there exists a {@link Statement} in 
	 * the preconditions and effects of this {@link Operator} that
	 * uses <i>key</i> as interval, <i>false</i> otherwise.
	 */
	public boolean hasStatementWithKey( Term key ) {
		for ( Statement s : this.P ) {
			if ( s.getKey().equals(key) ) {
				return true;
			}
		}
		for ( Statement s : this.E ) {
			if ( s.getKey().equals(key) ) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get a simplified version of this {@link Operator}, reduced to preconditions and effects in 
	 * form of state variable mappings. All {@link Expression}s are  ignored.
	 * @param usedVars set of unique names of variables that are used by this operator
	 * @return a {@link StateVariableOperator} 
	 */
	public StateVariableOperator getStateVariableBasedOperator( HashSet<String> usedVars ) {
		StateVariableOperator o = new StateVariableOperator();
		
		o.setName(this.name);
		
		for ( Statement p : this.getPreconditions() ) {
			if ( usedVars.contains( p.getVariable().getUniqueName() ) ) {
				o.getPreconditions().put( p.getVariable(), p.getValue() );
			}
		}
		for ( Statement e : this.getEffects() ) {
			if ( usedVars.contains( e.getVariable().getUniqueName() ) ) {
				o.getEffects().put( e.getVariable(), e.getValue() );
			}
		}
		
		o.setSubstitution(this.theta);

		return o;
	}
	
	/**
	 * Get a slightly less simplified version of this {@link Operator}, reduced to preconditions and effects in 
	 * form of state variable mappings. All {@link Expression}s are  ignored.
	 * @param usedVars usedVars set of unique names of variables that are used by this operator
	 * @return a {@link StateVariableOperator}
	 */
	public StateVariableOperatorMultiState getStateVariableBasedOperatorWithMultipleEffectValues( Collection<String> usedVars ) {
		StateVariableOperatorMultiState o = new StateVariableOperatorMultiState();
		
		o.setName(this.name);
		
		for ( Statement p : this.getPreconditions() ) {
			if ( usedVars.contains( p.getVariable().getUniqueName() ) ) {
				o.getPreconditions().put( p.getVariable(), p.getValue() );
			}
		}
		for ( Statement e : this.getEffects() ) {
			if ( usedVars.contains( e.getVariable().getUniqueName() ) ) {
				if ( !o.getEffects().containsKey(e.getVariable())) {
					List<Term> newEffectList = new ArrayList<Term>();
					newEffectList.add(e.getValue());
					o.getEffects().put(e.getVariable(), newEffectList);
				} else {
					o.getEffects().get( e.getVariable() ).add( e.getValue() );
				}
			}
		}
		
		o.setSubstitution(this.theta);

		return o;
	}
	
	/**
	 * Get a slightly less simplified version of this {@link Operator}, reduced to preconditions and effects in 
	 * form of state variable mappings. All {@link Expression}s are  ignored.
	 * @return A {@link StateVariableOperator} that can be used e.g. by {@link ForwardPlanningSearch}.
	 */
	public StateVariableOperatorMultiState getStateVariableBasedOperatorWithSingleEffectValue( Collection<String> usedVars ) {
		StateVariableOperatorMultiState o = new StateVariableOperatorMultiState();
		
		o.setName(this.name);
		
		for ( Statement p : this.getPreconditions() ) {
			if ( usedVars.contains( p.getVariable().getUniqueName() ) ) {
				o.getPreconditions().put( p.getVariable(), p.getValue() );
			}
		}
		for ( Statement e : this.getEffects() ) {
			if ( usedVars.contains( e.getVariable().getUniqueName() ) ) {
				List<Term> newEffectList = new ArrayList<Term>();
				newEffectList.add(e.getValue() );
				o.getEffects().put( e.getVariable(), newEffectList );
			}
		}
		
		o.setSubstitution(this.theta);

		return o;
	}
	
	
	/**
	 * Match this operator to another operator.
	 * @param o An {@link Operator} to be matched to this {@link Operator}.
	 * @return A {@link Substitution} that makes <i>this</i> and <i>o</i>
	 * equal or <i>null</i> if such a {@link Substitution} does not exist.
	 */
	public Substitution match( Operator o ) {
				
		Substitution theta = this.getName().match(o.getName());
		
		if ( theta == null ) {
			return null;
		}
		
		for ( int i = 0 ; i < this.getPreconditions().size() ; i++ ) {
			if ( !theta.add(this.getPreconditions().get(i).match( o.getPreconditions().get(i) )) ) {
				return null;
			}
		}
		
		for ( int i = 0 ; i < this.getEffects().size() ; i++ ) {
			if ( ! theta.add( this.getEffects().get(i).match( o.getEffects().get(i))) ) {
				return null;
			}
		}
		
		ArrayList<Matchable> thisMatchable = new ArrayList<Matchable>();
		ArrayList<Matchable> oMatchable = new ArrayList<Matchable>();
		
		// TODO: getMatchable bugged because of ConstraintDatabase keySet() issue
		// -> Random behavior if order in two keySets different for the two calls of getMatchable()
	
		thisMatchable.addAll(this.C.getMatchable());
		oMatchable.addAll(o.C.getMatchable());
		
		for ( int i = 0 ; i < thisMatchable.size() ; i++ ) {
			if ( thisMatchable.get(i) instanceof Matchable ) {
				if ( ! theta.add( thisMatchable.get(i).match( (Expression)oMatchable.get(i))) ) {
					return null;
				}
			} else {
				if ( !thisMatchable.get(i).equals( oMatchable.get(i)) ) {
					return null;
				}
			}
		}		
		
		return theta;	
	}
		
	/**
	 * Sets all open variables to the most relaxed values
	 * where possible (e.g. bounds of {@link AllenConstraint}s
	 * and amounts of {@link ResourceUsage}s).
	 */
	public void setOpenVariablesToMostRelaxed( ConstraintDatabase cDB ) {
		HashMap<Atomic,Integer> rrCapacities = new HashMap<Atomic,Integer>();
		for ( ReusableResourceCapacity rrC : cDB.get(ReusableResourceCapacity.class)) {
			rrCapacities.put(rrC.getVariable(),rrC.getCapacity());
		}
		
		/**
		 * Every resource usage that was lifted is set to minimum (least constrained) 
		 */
		for ( int i = 0 ; i < E.size() ; i++ ) {
			Statement s = E.get(i);
			if ( rrCapacities.keySet().contains(s.getVariable()) ) {
				if ( s.getValue().isVariable() ) {
					E.set(i, new Statement(s.getKey(), s.getVariable(), Time1));
				}

			}
		}
		
		/**
		 * Every temporal bound that was lifted is set to most relaxed ground value (i.e. 0 or inf) (least constrained) 
		 */
		for ( Expression c : getConstraints() ) {
			if ( c instanceof AllenConstraint ) {
				AllenConstraint tc = (AllenConstraint)c;
				tc.setVariableBoundsToMostRelaxed();
			}
		}
	}
	
	/**
	 * Lift argument <i>i</i> of this {@link Operator} along with other 
	 * variable {@link Term}s that can only be grounded when 
	 * argument <i>i</i>  is instantiated.
	 *  
	 * @param i			Index of variable to lift
	 * @param operator	Uninstantiated version of this {@link Operator}
	 * @return			This {@link Operator} with lifted argument <i>i</i>
	 */
	public Operator liftVariable( int i, Operator operator ) {
		/**
		 * Match
		 */
		Substitution theta = operator.match(this);
		/**
		 * Remove substitution of lifted variable
		 */
		theta.remove(operator.getName().getArg(i));
		/**
		 * Create substituted operator
		 */
		Operator oCopy = operator.copy();
		oCopy.substitute(theta);
		/**
		 * Arguments of (non-ground) operator
		 */
		HashSet<Term> opArgs = new HashSet<Term>();
		for ( int j = 0 ; j < operator.getName().getNumArgs() ; j++ ) {
			opArgs.add(operator.getName().getArg(j));
		}

		/**
		 * We are forced to lift other variables in case they occur in the same relational constraint.
		 * In this case we have to assume that they cannot be grounded without the lifted variable.
		 * This has to be done until every term that needs to be lifted is found. We only need to lift
		 * terms that are not arguments of the operator.
		 * 
		 * The following while-loop keeps lifting oCopy until no more necessary change is found.
		 */
		HashSet<Term> needToLift = new HashSet<Term>();
		
		ArrayList<PrologConstraint> oCons = new ArrayList<PrologConstraint>();
		ArrayList<PrologConstraint> oCopyCons = new ArrayList<PrologConstraint>();
		oCons.addAll(operator.getConstraints().get(PrologConstraint.class));
		oCopyCons.addAll(oCopy.getConstraints().get(PrologConstraint.class));
		
		boolean change = true;
		while ( change ) {
			change = false;
			
			for ( int j = 0; j < oCopyCons.size(); j++ ) {
				if ( oCopyCons.get(j) instanceof PrologConstraint ) {
					PrologConstraint rc = oCopyCons.get(j);
									
					Atomic l = rc.getRelation();
					
					if ( !l.isGround() ) {
						HashSet<Term> variables = new HashSet<Term>();
						PrologConstraint rcOp = oCons.get(j);
						
						variables.addAll(rcOp.getVariableTerms());
						
//						for ( Term t : rcOp.getRelation().getArgs() ) {
//							variables.addAll(t.getVariables());
//						}
						
						for ( Term var : variables ) {
							if ( !opArgs.contains(var) && !needToLift.contains(var)) {
								needToLift.add(var);
								change = true;
							}
						}
						
					}
				}
			}
			
			for ( Term v : needToLift ) {
				theta.remove(v);
				oCopy = operator.copy();
				oCopy.substitute(theta);
			}
		}
		
		return oCopy;
	}
	
	/**
	 * Get all actions by creating combinations of
	 * assignments to all open variables. 
	 * 
	 * @param tM {@link TypeManager} that knows the domains of variables.
	 * @return A {@link Collection} of ground {@link Operator}s (actions).
	 */
	public Collection<Operator> getAllGround ( TypeManager tM ) {
		Collection<Operator> r = new ArrayList<Operator>();
		
		if ( this.name.isGround() ) {
			r.add(this);
			return r;
		}
		
		Collection<Substitution> subst = tM.getAllGroundSubstitutions(this.getName());
		
		for ( Substitution theta : subst ) {
			Operator svoCopy = this.copy();
			svoCopy.substitute(theta);
			r.add(svoCopy);
		}
		
		return r;
	}
	
	/**
	 * Get {@link Substitution} that was used on this
	 * {@link Operator}.
	 * @return A {@link Substitution} containing all 
	 * changes made to the variable {@link Term} in this
	 * {@link Operator}.
	 */
	public Substitution getSubstitution() {
		return theta;
	}
	
	/**
	 * Return a copy of this {@link Operator}.
	 * @return A copy of this {@link Operator} that can be changed
	 * without changing the original.
	 */
	public Operator copy() {
		Operator oCopy = new Operator();
		
		oCopy.name = this.name;
		oCopy.intervalKey = this.intervalKey;
		
		for ( Statement p : P ) {
			oCopy.P.add(p);
		}
		for ( Statement e : E ) {
			oCopy.E.add(e);
		}
		for ( Expression c : C ) { // TODO: should use ConstraintCollection...
			if ( c instanceof Mutable ) {
				oCopy.C.add(((Mutable)c).copy());
			} else {
				oCopy.C.add(c);
			}
		}
		oCopy.theta = theta.copy();
		return oCopy;
	}
	
	@Override
	public Expression substitute(Substitution theta) {
		this.name = this.name.substitute(theta);
		this.intervalKey = this.intervalKey.substitute(theta);
		for ( int i = 0 ; i < P.size() ; i++ ) {
			P.set(i, P.get(i).substitute(theta));
		}
		for ( int i = 0 ; i < E.size() ; i++ ) {
			E.set(i, E.get(i).substitute(theta));
		}
		
		this.C.substitute(theta);
		
//		for ( int i = 0 ; i < C.size() ; i++ ) {
//			Constraint c = C.get(i);
//			if ( c instanceof Substitutable ) {
//				Substitutable substC = (Substitutable)c;
//				C.set(i, substC.substitute(theta));
//			}
//		}	
		
		if ( this.theta != null ) {
			this.theta.add(theta);
		} else {
			this.theta = theta.copy();
		}
		return this;
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof Operator ) {
			Operator oO = (Operator)o;
			return oO.intervalKey.equals(this.intervalKey) && oO.name.equals(this.name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		int result = 11;
		result = 37*result+this.intervalKey.hashCode();
		result = 37*result+this.name.hashCode();
		return result;
	}
	
	@Override
	public String toString() {
		
		StringBuilder r = new StringBuilder();
		
		r.append("(operator\n\t");
		r.append(this.getName() + "\n");
		r.append("\t(preconditions\n");
		for ( Statement a : P ) {
			r.append("\t\t" + a.toString() + "\n");
		}
		r.append("\t)\n");
		r.append("\t(effects\n");
		for ( Statement a : E ) {
			r.append("\t\t" + a.toString() + "\n");
		}
		r.append("\t)\n");
		
		Map<String,Collection<Expression>> typeMap = new HashMap<String, Collection<Expression>>();
		for ( Expression c : C ) {
			Term conType = c.getType();
			String conTypeString = conType.toString();
			if ( conType.equals(ExpressionTypes.Prolog) ) {
				PrologConstraint pc = (PrologConstraint)c;
				conTypeString += " " + pc.getProgramID();
			} else if  ( conType.equals(ExpressionTypes.MiniZinc) ) {
				MiniZincInput mc = (MiniZincInput)c;
				conTypeString += " " + mc.getProgramID();
			} 
			Collection<Expression> Col = typeMap.get(conTypeString);
			if ( Col == null ) {
				Col = new ArrayList<Expression>();
				typeMap.put(conTypeString, Col);
			}
			Col.add(c);
		}
		r.append("\t(constraints\n");
		for ( String conType : typeMap.keySet() ) {
			if ( !conType.equals(Term.createConstant("conditional"))) {
				r.append("\t\t(");
				r.append(conType);
			}
				
			for ( Expression c : typeMap.get(conType) ) {
				r.append("\n");
				if ( !conType.equals(Term.createConstant("conditional")) && !conType.equals(Term.createConstant("include"))) {
					String s = "\t\t\t"+c.toString().toString().replace("\n", "\n\t\t\t");
					r.append(s);
				} else {
					r.append(c.toString());
				}				
			}
			if ( !conType.equals(Term.createConstant("conditional"))) {
				r.append("\n\t\t)\n");
			} else {
				r.append("\n");
			}
		}
		r.append("\t)\n)");
		
		
		return r.toString();
	}
}

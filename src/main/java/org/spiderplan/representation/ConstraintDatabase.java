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

import org.spiderplan.modules.solvers.Resolver;
import org.spiderplan.representation.constraints.Constraint;
import org.spiderplan.representation.constraints.ConstraintCollection;
import org.spiderplan.representation.constraints.ConstraintTypes;
import org.spiderplan.representation.constraints.IncludedProgram;
import org.spiderplan.representation.constraints.MiniZincConstraint;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.constraints.PrologConstraint;
import org.spiderplan.representation.constraints.Statement;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.plans.OrderedPlan;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.GenericComboIterator;
import org.spiderplan.tools.profiler.Profiler;

/** 
 * Represents a set of Statements and TemporalConstraints between them.
 * 
 * Also has support for adding {@link OrderedPlan}s, taking graph snapshots of the current state of the {@link ConstraintDatabase}
 * and graph visualization of the {@link ConstraintDatabase} (including history of all snapshots)
 * 
 * @author Uwe Köckemann
 *
 */
public class ConstraintDatabase {
	
//	private Set<Statement> F = new HashSet<Statement>();
	private ConstraintCollection C = new ConstraintCollection();	
//	private HashMap<Term,Statement> statementLookup = new HashMap<Term, Statement>();
		
	/**
	 * Add a {@link Constraint}
	 * @param c The {@link Constraint} to be added
	 */
	public void add( Constraint c ) {
//		if ( !C.contains(c) ) {
			C.add(c);
//		}
	}
	
	/**
	 * Remove a {@link Constraint}
	 * @param c The {@link Constraint} to be removed
	 */
	public void remove( Constraint c ) {
		C.remove(c);
	}
	
	/**
	 * Add a {@link Collection} of {@link Statement}s S
	 * @param S
	 */
	@Deprecated
	public void addStatements( Collection<Statement> S ) {
		for ( Statement s : S ) {
			this.C.add(s);
//			this.add(s);
		}
	}
	
	/**
	 * Add a {@link Collection} of {@link Constraint}s C
	 * @param C
	 */
	public void addConstraints( Collection<Constraint> C ) {
		ArrayList<Constraint> newCons = new ArrayList<Constraint>();
		for ( Constraint c : C ) {
			if ( !this.C.contains(c) ) {
				newCons.add(c);
			}
		}
		this.C.addAll(newCons);
	}
	
	/**
	 * Add another {@link ConstraintDatabase} to this one. Does not re-add existing {@link Statement}s. 
	 * @param cDB
	 */
	public void add( ConstraintDatabase cDB ) {
		for ( Statement s1 : cDB.C.get(Statement.class) ) {
			boolean old = false;
			for ( Statement s2 : this.C.get(Statement.class) ) {
				if ( s1.equals(s2) ) {
					old = true;
					break;
				}
			}
			if ( !old ) {
				this.C.add(s1);
			}
		}
		for ( Constraint c : cDB.C )
			this.add(c);
	}
	
	/**
	 * Apply {@link Resolver} to this {@link ConstraintDatabase}.
	 * 
	 * @param r The {@link Resolver} to be added.
	 * @param theta A {@link Substitution}.
	 * @return The result of adding {@link Resolver}'s {@link ConstraintDatabase} and applying
	 * its substitution
	 */
	public void apply( Resolver r ) {
		Profiler.probe(0);
		r.apply(this);
	}
	
	/**
	 * Returns the {@link List} of {@link Statement}s in this {@link ConstraintDatabase}
	 * @return
	 */
	@Deprecated
	public Collection<Statement> getStatements() {
		return this.C.get(Statement.class);
	}
	
	/**
	 * Returns the {@link ConstraintCollection} of this {@link ConstraintDatabase}
	 * @return
	 */
	public ConstraintCollection getConstraints() {
		return this.C;
	}
				
	/**
	 * Get {@link Statement} using this unique key.
	 * @param key {@link Term} representation of the key.
	 * @return The corresponding {@link Statement}
	 * @throws MissingStatement In case {@link Statement} does not exist.
	 */
	public Statement getStatement( Term key ) {
//		Statement r = statementLookup.get(key);
		
//		if ( r != null ) {
//			return r;
//		}
		
		for ( Statement s : this.C.get(Statement.class) ) {
			if ( key.equals(s.getKey()))  {
//				statementLookup.put(key, s);
				return s;
			}
		}
		throw new IllegalArgumentException("Statement with label " + key + " does not exist.");
	}
	
	/**
	 * Get all {@link Statement}s using this key.
	 * @param key String representation of the key.
	 * @return List of {@link Statement}s
	 */
	public ArrayList<Statement> getStatements( Term key ) {
		ArrayList<Statement> r = new ArrayList<Statement>();
		for ( Statement s : this.C.get(Statement.class)) {
			if ( key.equals(s.getKey() )  ) {
				r.add(s);
			} 
		}
		return r;
	}
	
	/**
	 * Checks if a {@link Statement} with a certain <i>key</i> {@link Term} exits.
	 * @param key A {@link Term} representing a temporal interval.   
	 * @return <i>true</i> if there exists a {@link Statement} with key 
	 * {@link Term} <i>key</i>, false otherwise.
	 */
	public boolean hasKey( Term key ) {
		for ( Statement s : this.C.get(Statement.class) ) {
			if ( s.getKey().equals(key)) {
				return true;
			}
		}
		for ( OpenGoal og : this.C.get(OpenGoal.class) ) {
			if ( og.getStatement().getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Checks if there are any {@link Constraint}s or {@link Statement}s
	 * in <i>this</i> {@link ConstraintDatabase}.	
	 * @return <i>true</i> if there are no {@link Constraint}s and no {@link Statement}s
	 * in <i>this</i> {@link ConstraintDatabase}, <i>false</i> otherwise
	 */
	public boolean isEmpty() {
		return C.isEmpty();
	}
	
	/**
	 * Get all keys of {@link Statement}s in F that match {@link Statement} a
	 * @param a
	 * @return
	 */
	public ArrayList<Term> getMatchingKeys( Statement a ) {
		ArrayList<Term> r = new ArrayList<Term>();
		for ( Statement s : this.C.get(Statement.class) ) {
			if ( a.match(s) != null ) {
				r.add(s.getKey());
			} 
		}
		return r;
	}
	
	/**
	 * Get all variable {@link Term}s in this {@link ConstraintDatabase}.
	 * @return
	 */
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Constraint c : C ) {
			r.addAll(c.getVariableTerms());
		}
		return r;
	}
	/**
	 * Get all ground {@link Term}s in this {@link ConstraintDatabase}.
	 * @return
	 */
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Constraint c : C ) {
			r.addAll(c.getGroundTerms());
		}
		return r;
	}
	/**
	 * Get all {@link Atomic}s in this {@link ConstraintDatabase}.
	 * @return
	 */
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		for ( Constraint c : C ) {
			r.addAll(c.getAtomics());
		}
		return r;
	}
	
	/**
	 * Find all {@link Substitution}s that make {@link Statement}s in {@link ConstraintDatabase} <i>cDB</i> 
	 * match <i>this</i> {@link ConstraintDatabase}. 
	 * @param cDB A {@link ConstraintDatabase} to be matched against <i>this</i> {@link ConstraintDatabase}.
	 * @return A {@link Collection} of {@link Substitution}s that make the {@link Statement}s in <i>cDB</i> 
	 * a subset of the {@link Statement}s in <i>this</i>.
	 * 
	 */
	public Collection<Substitution> getSubstitutions( ConstraintDatabase cDB ) {
		ArrayList<Substitution> r = new ArrayList<Substitution>();
		
		/**
		 * No statements -> trivial substitution
		 */
		if ( cDB.C.get(Statement.class).isEmpty() ) {
			r.add( new Substitution() );
			return r;
		}
		
		/**
		 * Find all possible substitutions for each statement 
		 */
		ArrayList<ArrayList<Substitution>> statementSubstitutions = new ArrayList<ArrayList<Substitution>>();
		for ( Statement s : cDB.C.get(Statement.class) ) {
			ArrayList<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.C.get(Statement.class) ) {
				Substitution theta = s.match(e);
				if ( theta != null ) {
					tmp.add(theta);
				}
			}
			if ( !tmp.isEmpty() )
				statementSubstitutions.add(tmp);
			else
				return r;
		}
		
		/**
		 * Combine them
		 */
		GenericComboBuilder<Substitution> cB = new GenericComboBuilder<Substitution>();
		ArrayList<ArrayList<Substitution>> combos = cB.getCombos(statementSubstitutions);
		
		/**
		 * Keep all legal combinations
		 */
		for ( ArrayList<Substitution> s : combos ) {
			Substitution theta = new Substitution();
			
			for ( Substitution theta_i : s ) {
				if ( ! theta.add(theta_i) ) {
					theta = null;
					break;
				}
			}
			
			if ( theta != null ) {
				r.add(theta);
			}
		}
		
		return r;
	}
	
	
	/**
	 * Find all {@link Substitution}s that make {@link Statement}s in {@link ConstraintDatabase} <i>cDB</i> 
	 * match <i>this</i> {@link ConstraintDatabase}. 
	 * @param cDB A {@link ConstraintDatabase} to be matched against <i>this</i> {@link ConstraintDatabase}.
	 * @return A {@link Collection} of {@link Substitution}s that make the {@link Statement}s in <i>cDB</i> 
	 * a subset of the {@link Statement}s in <i>this</i>.
	 * 
	 */
	public GenericComboIterator<Substitution> getSubstitutionIterator( ConstraintDatabase cDB ) {
		
		/**
		 * No statements -> trivial substitution
		 */
		if ( cDB.C.get(Statement.class).isEmpty() ) {
			List<List<Substitution>> toCombine = new ArrayList<List<Substitution>>();
			List<Substitution> r = new ArrayList<Substitution>();
			r.add(new Substitution());
			toCombine.add(r);			
			return new GenericComboIterator<Substitution>(toCombine);
		}
		
		/**
		 * Find all possible substitutions for each statement 
		 */
		List<List<Substitution>> statementSubstitutions = new ArrayList<List<Substitution>>();
		for ( Statement s : cDB.C.get(Statement.class) ) {
			List<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.C.get(Statement.class) ) {
				Substitution theta = s.match(e);
				if ( theta != null ) {
					tmp.add(theta);
				}
			}
			if ( !tmp.isEmpty() ) {
				statementSubstitutions.add(tmp);
			} else {
				List<Substitution> r = new ArrayList<Substitution>();
				List<List<Substitution>> toCombine = new ArrayList<List<Substitution>>();
				toCombine.add(r);			
				return new GenericComboIterator<Substitution>(toCombine);
			}
		}
		GenericComboIterator<Substitution> it = new GenericComboIterator<Substitution>(statementSubstitutions); 
		return it;
	}
	
	public GenericSingleNodeSearch<Substitution> getSubstitutionSearch( ConstraintDatabase cDB ) {
		/**
		 * No statements -> trivial substitution
		 */
		if ( cDB.C.get(Statement.class).isEmpty() ) {
			List<List<Substitution>> toCombine = new ArrayList<List<Substitution>>();
			List<Substitution> r = new ArrayList<Substitution>();
			r.add(new Substitution());
			toCombine.add(r);			
			return new GenericSingleNodeSearch<Substitution>(toCombine);
		}
		
		/**
		 * Find all possible substitutions for each statement 
		 */
		List<List<Substitution>> statementSubstitutions = new ArrayList<List<Substitution>>();
		for ( Statement s : cDB.C.get(Statement.class) ) {
			List<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.C.get(Statement.class) ) {
				Substitution theta = s.match(e);
				if ( theta != null ) {
					tmp.add(theta);
				}
			}
			if ( !tmp.isEmpty() ) {
				statementSubstitutions.add(tmp);
			} else {
				return null;
			}
		}
		GenericSingleNodeSearch<Substitution> it = new GenericSingleNodeSearch<Substitution>(statementSubstitutions); 
		return it;
	}
		
	/**
	 * Returns {@link ConstraintDatabase} containing {@link Statement}s and {@link Constraint}s
	 * that are in this {@link ConstraintDatabase} but not in cDB.
	 * @param cDB Another {@link ConstraintDatabase}.
	 * @return
	 */
	public ConstraintDatabase difference( ConstraintDatabase cDB ) {
		ConstraintDatabase diff = new ConstraintDatabase();
		
		for ( Constraint c : this.C ) {
			if ( !cDB.getConstraints().contains(c) ) {
				diff.add(c);
			}
		}
		
		return diff;
	}
	
	/**
	 * Get all {@link IncludedProgram} constraints for a set of program IDs and return
	 * a mapping from program IDs to a {@link ConstraintCollection} of {@link IncludedProgram}s.
	 * @param programIDs The program IDs that we want to retriev from the {@link ConstraintDatabase}
	 * @return A mapping from program IDs to a {@link ConstraintCollection} of {@link IncludedProgram}s.
	 */
	public Map<Term,ConstraintCollection> getIncludedPrograms( Collection<Term> programIDs ) {
		Map<Term,ConstraintCollection> conCollection = new HashMap<Term, ConstraintCollection>();
		for ( IncludedProgram pC : this.getConstraints().get( IncludedProgram.class ) ) {
			Term programID = pC.getName();
			if ( programIDs.contains(programID) ) {
				if ( !conCollection.containsKey(programID) ) {
					conCollection.put(programID, new ConstraintCollection());
				}
				conCollection.get(programID).add(pC);
			}
		}
		return conCollection;
	}
	
	/**
	 * Remove duplicate {@link Statement}s which
	 * can occur due to substitution.
	 */
	private void removeDuplicates() {
		List<Statement> newF = new ArrayList<Statement>();
		
		for ( Statement s : this.C.get(Statement.class) ) {
			if ( !newF.contains(s) ) {
				newF.add(s);
			}
		}
		this.C.get(Statement.class).clear();
		this.C.addAll(newF);
	}
	
	/**
	 * Check if a unique key is used twice
	 * which is an illegal state.
	 * @return
	 */
	private void checkForMultipleUniqueKeys( Substitution theta ) {
		Collection<Term> keys = new HashSet<Term>();
		
		for ( Statement s:  this.C.get(Statement.class) ){
			if ( keys.contains( s.getKey()) ) {
				
				String message = "Substitution leads to duplicate unique keys for different instances! " + theta + "\n";
				for ( Statement s1:  this.C.get(Statement.class) ){
					if ( s1.getKey().equals(s.getKey()) )
					message += "\t" + s1 + "\n";
				}	
				
				throw new IllegalStateException(message);		
			}
			keys.add(s.getKey());
		}
	}
	public Map<Class,Integer> getConstraintCount() {
		return C.getConstraintCount();
	}
	
	public void setToConstraintCount( Map<Class,Integer> cCount ) {
		C.setToConstraintCount(cCount);
	}
	

	/**
	 * Return a copy of this {@link ConstraintDatabase}
	 * @return The copy.
	 */
	public ConstraintDatabase copy() {
//		Profiler.probe(0);
		ConstraintDatabase cDB = new ConstraintDatabase();	
		cDB.C = this.C.copy();
		return cDB;
	}
	
	public void substitute(Substitution theta) {
		C.substitute(theta);
		
		/* Substitution may make 2 statements with variable terms equal... */
		removeDuplicates();
		/* Checks is substitution results in illegal state and may throw exception. */
		checkForMultipleUniqueKeys(theta);
	}
	
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof ConstraintDatabase ) {
			boolean r =  this.toString().equals(o.toString());
			return r;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		Profiler.probe(0);
		return this.toString().hashCode(); //TODO: hack
	}

//	@Override
//	public String toString() {
//		String r = "";
//		for ( Statement ta : F ) {
//			r += "[" + ta.getClass().getSimpleName() + "] " + ta.toString() + "\n";
//		}
//		for ( Constraint c : C ) {
//			String description = "";
//			if ( !c.getDescription().equals("") ) {
//				description = " ("+c.getDescription()+")";
//			}
//			r += "[" + c.getClass().getSimpleName() + "] " + c.toString() + description + "\n";
//		}
//		if ( r.isEmpty() ) {
//			r = "Empty constraint database ";
//		}
//		return r.substring(0, r.length()-1);
//	}
	
	@Override
	public String toString() {
		ArrayList<Term> ordering = new ArrayList<Term>();
		ordering.add(ConstraintTypes.Domain);
		ordering.add(ConstraintTypes.Statement);
		ordering.add(ConstraintTypes.Goal);
		ordering.add(ConstraintTypes.Temporal);
		ordering.add(ConstraintTypes.Resource);
		ordering.add(ConstraintTypes.Prolog);
		ordering.add(ConstraintTypes.Math);
		ordering.add(ConstraintTypes.Cost);
		ordering.add(ConstraintTypes.Set);
		ordering.add(ConstraintTypes.Graph);
		ordering.add(ConstraintTypes.MiniZinc);
		ordering.add(ConstraintTypes.Conditional);
		ordering.add(ConstraintTypes.Simulation);
		ordering.add(ConstraintTypes.IncludedProgram);
		
		ArrayList<String> keyList = new ArrayList<String>();
		
		Map<String,Collection<Constraint>> typeMap = new HashMap<String, Collection<Constraint>>();
		
		for ( Term conType : ordering ) {
			for ( Constraint c : C ) {
				if ( c.getType().equals(conType) ) {
					String conTypeString = conType.toString();
					if ( conType.equals(ConstraintTypes.Prolog) ) {
						PrologConstraint pc = (PrologConstraint)c;
						conTypeString += " " + pc.getProgramID();
					} else if  ( conType.equals(ConstraintTypes.MiniZinc) ) {
						MiniZincConstraint mc = (MiniZincConstraint)c;
						conTypeString += " " + mc.getProgramID();
					} 
					if ( !keyList.contains(conTypeString) ) {
						keyList.add(conTypeString);
					}
					Collection<Constraint> Col = typeMap.get(conTypeString);
					if ( Col == null ) {
						Col = new ArrayList<Constraint>();
						typeMap.put(conTypeString, Col);
					}
					Col.add(c);
				}
			}
		}
		for ( Constraint c : C ) {
			Term conType = c.getType();
			if ( !ordering.contains(conType) ) {
				String conTypeString = conType.toString();
				if ( conType.equals(ConstraintTypes.Prolog) ) {
					PrologConstraint pc = (PrologConstraint)c;
					conTypeString += " " + pc.getProgramID();
				} else if  ( conType.equals(ConstraintTypes.MiniZinc) ) {
					MiniZincConstraint mc = (MiniZincConstraint)c;
					conTypeString += " " + mc.getProgramID();
				} 
				if ( !keyList.contains(conTypeString) ) {
					keyList.add(conTypeString);
				}
				Collection<Constraint> Col = typeMap.get(conTypeString);
				if ( Col == null ) {
					Col = new ArrayList<Constraint>();
					typeMap.put(conTypeString, Col);
				}
				Col.add(c);
			}
		}

		
		StringBuilder r = new StringBuilder();
		
		for ( String conType : keyList ) {
			if ( !conType.equals("conditional") && !conType.equals("simulation") ) {
				r.append("(");
				r.append(conType);
			}
				
			for ( Constraint c : typeMap.get(conType) ) {
				r.append("\n");
				if ( !conType.equals("conditional") && !conType.equals("include") && !conType.equals("simulation")) {
					String s = "\t"+c.toString().toString().replace("\n", "\n\t");
					r.append(s);
				} else {
					r.append(c.toString());
				}				
			}
			if ( !conType.equals("conditional") && !conType.equals("simulation")) {
				r.append("\n)\n");
			} else {
				r.append("\n");
			}
		}
		
		return r.toString();
	}
}



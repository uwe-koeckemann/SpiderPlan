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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.spiderplan.representation.expressions.Expression;
import org.spiderplan.representation.expressions.ExpressionTypes;
import org.spiderplan.representation.expressions.Statement;
import org.spiderplan.representation.expressions.causal.OpenGoal;
import org.spiderplan.representation.expressions.interfaces.Assertable;
import org.spiderplan.representation.expressions.interfaces.Matchable;
import org.spiderplan.representation.expressions.interfaces.Mutable;
import org.spiderplan.representation.expressions.interfaces.Substitutable;
import org.spiderplan.representation.expressions.minizinc.MiniZincInput;
import org.spiderplan.representation.expressions.misc.Asserted;
import org.spiderplan.representation.expressions.programs.IncludedProgram;
import org.spiderplan.representation.expressions.prolog.PrologConstraint;
import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Substitution;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.search.GenericSingleNodeSearch;
import org.spiderplan.tools.GenericComboBuilder;
import org.spiderplan.tools.GenericComboIterator;
import org.spiderplan.tools.Global;

/** 
 * Stores all types of constraints. Internally uses one list per class which allows a quick retrieval of
 * all constraints of a specific class. 
 * 
 * @author Uwe Köckemann
 *
 */
public class ConstraintDatabase implements Collection<Expression> {
	
	private Map<Class<? extends Expression>,List<Expression>> Cmap = new HashMap<Class<? extends Expression>, List<Expression>>();
	private static ArrayList<Class<? extends Expression>> KeyList = new ArrayList<Class<? extends Expression>>();
	
	/**
	 * Add another {@link ConstraintDatabase} to this one. Does not re-add existing {@link Statement}s. 
	 * @param cDB
	 */
	public void add( ConstraintDatabase cDB ) {
		for ( Statement s1 : cDB.get(Statement.class) ) {
			boolean old = false;
			for ( Statement s2 : this.get(Statement.class) ) {
				if ( s1.equals(s2) ) {
					old = true;
					break;
				}
			}
			if ( !old ) {
				this.add(s1);
			}
		}
		for ( Expression c : cDB )
			this.add(c);
	}
		
					
	/**
	 * Get statement for some interval.
	 * @param interval term representing interval.
	 * @return The corresponding {@link Statement}
	 */
	public Statement getStatement( Term interval ) {	
		for ( Statement s : this.get(Statement.class) ) {
			if ( interval.equals(s.getKey()))  {
				return s;
			}
		}
		throw new IllegalArgumentException("Statement with label " + interval + " does not exist.");
	}
	
	/**
	 * Get all {@link Statement}s using this key.
	 * @param key String representation of the key.
	 * @return List of {@link Statement}s
	 */
	public ArrayList<Statement> getStatements( Term key ) {
		ArrayList<Statement> r = new ArrayList<Statement>();
		for ( Statement s : this.get(Statement.class)) {
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
		for ( Statement s : this.get(Statement.class) ) {
			if ( s.getKey().equals(key)) {
				return true;
			}
		}
		for ( OpenGoal og : this.get(OpenGoal.class) ) {
			if ( og.getStatement().getKey().equals(key)) {
				return true;
			}
		}
		return false;
	}
			
	/**
	 * Get all variable {@link Term}s in this constraint database.
	 * @return all variables used in this constraint database
	 */
	public Collection<Term> getVariableTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Expression c : this ) {
			r.addAll(c.getVariableTerms());
		}
		return r;
	}
	/**
	 * Get all ground {@link Term}s in this constraint database.
	 * @return all ground terms in this constraint database
	 */
	public Collection<Term> getGroundTerms() {
		Set<Term> r = new HashSet<Term>();
		for ( Expression c : this ) {
			r.addAll(c.getGroundTerms());
		}
		return r;
	}
	/**
	 * Get all {@link Atomic}s that appear in the constraints of this constraint database.
	 * @return all {@link Atomic}s in this constraint database
	 */
	public Collection<Atomic> getAtomics() {
		Set<Atomic> r = new HashSet<Atomic>();
		for ( Expression c : this ) {
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
		if ( cDB.get(Statement.class).isEmpty() ) {
			r.add( new Substitution() );
			return r;
		}
		
		/**
		 * Find all possible substitutions for each statement 
		 */
		ArrayList<ArrayList<Substitution>> statementSubstitutions = new ArrayList<ArrayList<Substitution>>();
		for ( Statement s : cDB.get(Statement.class) ) {
			ArrayList<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.get(Statement.class) ) {
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
		if ( cDB.get(Statement.class).isEmpty() ) {
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
		for ( Statement s : cDB.get(Statement.class) ) {
			List<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.get(Statement.class) ) {
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
	
	
	/**
	 * Returns an initialized search over substitutions that match the statements from another
	 * constraint database to this one.
	 * @param cDB constraint database that is matched to this one
	 * @return search over substitutions that match the statements in the given constraint database to this one 
	 */
	public GenericSingleNodeSearch<Substitution> getSubstitutionSearch( ConstraintDatabase cDB ) {
		/**
		 * No statements -> trivial substitution
		 */
		if ( cDB.get(Statement.class).isEmpty() ) {
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
		for ( Statement s : cDB.get(Statement.class) ) {
			List<Substitution> tmp = new ArrayList<Substitution>();
			for ( Statement e : this.get(Statement.class) ) {
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
	 * Returns constraint database containing {@link Statement}s and {@link Expression}s
	 * that are in this constraint database but not in cDB.
	 * @param cDB Another constraint database
	 * @return the difference between two constraint databases
	 */
	public ConstraintDatabase difference( ConstraintDatabase cDB ) {
		ConstraintDatabase diff = new ConstraintDatabase();
		
		for ( Expression c : this ) {
			if ( !cDB.contains(c) ) {
				diff.add(c);
			}
		}
		
		return diff;
	}
	
	/**
	 * Get all {@link IncludedProgram} constraints for a set of program IDs and return
	 * a mapping from program IDs to a {@link ConstraintDatabase} of {@link IncludedProgram}s.
	 * @param programIDs The program IDs that we want to retrieve from the {@link ConstraintDatabase}
	 * @return A mapping from program IDs to a {@link ConstraintDatabase} of {@link IncludedProgram}s.
	 */
	public Map<Term,ConstraintDatabase> getIncludedPrograms( Collection<Term> programIDs ) {
		Map<Term,ConstraintDatabase> conCollection = new HashMap<Term, ConstraintDatabase>();
		for ( IncludedProgram pC : this.get( IncludedProgram.class ) ) {
			Term programID = pC.getName();
			if ( programIDs.contains(programID) ) {
				if ( !conCollection.containsKey(programID) ) {
					conCollection.put(programID, new ConstraintDatabase());
				}
				conCollection.get(programID).add(pC);
			}
		}
		return conCollection;
	}
	
//	/**
//	 * Remove duplicate {@link Statement}s which
//	 * can occur due to substitution.
//	 */
//	private void removeDuplicates() {
//		List<Statement> newF = new ArrayList<Statement>();
//		
//		for ( Statement s : this.get(Statement.class) ) {
//			if ( !newF.contains(s) ) {
//				newF.add(s);
//			}
//		}
//		this.get(Statement.class).clear();
//		this.addAll(newF);
//	}
//	
//	/**
//	 * Check if a unique key is used twice
//	 * which is an illegal state.
//	 * @return
//	 */
//	private void checkForMultipleUniqueKeys( Substitution theta ) {
//		Collection<Term> keys = new HashSet<Term>();
//		
//		for ( Statement s:  this.get(Statement.class) ){
//			if ( keys.contains( s.getKey()) ) {
//				
//				String message = "Substitution leads to duplicate unique keys for different instances! " + theta + "\n";
//				for ( Statement s1:  this.get(Statement.class) ){
//					if ( s1.getKey().equals(s.getKey()) )
//					message += "\t" + s1 + "\n";
//				}	
//				
//				throw new IllegalStateException(message);		
//			}
//			keys.add(s.getKey());
//		}
//	}
	
	@Override
	public String toString() {
		ArrayList<Term> ordering = new ArrayList<Term>();
		ordering.add(ExpressionTypes.Domain);
		ordering.add(ExpressionTypes.Statement);
		ordering.add(ExpressionTypes.Goal);
		ordering.add(ExpressionTypes.Temporal);
		ordering.add(ExpressionTypes.Resource);
		ordering.add(ExpressionTypes.Prolog);
		ordering.add(ExpressionTypes.Math);
		ordering.add(ExpressionTypes.Cost);
		ordering.add(ExpressionTypes.Set);
		ordering.add(ExpressionTypes.Graph);
		ordering.add(ExpressionTypes.MiniZinc);
		ordering.add(ExpressionTypes.Conditional);
		ordering.add(ExpressionTypes.Simulation);
		ordering.add(ExpressionTypes.IncludedProgram);
		
		ArrayList<String> keyList = new ArrayList<String>();
		
		Map<String,Collection<Expression>> typeMap = new HashMap<String, Collection<Expression>>();
		
		for ( Term conType : ordering ) {
			for ( Expression c : this ) {
				if ( c.getType().equals(conType) ) {
					String conTypeString = conType.toString();
					if ( conType.equals(ExpressionTypes.Prolog) ) {
						PrologConstraint pc = (PrologConstraint)c;
						conTypeString += " " + pc.getProgramID();
					} else if  ( conType.equals(ExpressionTypes.MiniZinc) ) {
						MiniZincInput mc = (MiniZincInput)c;
						conTypeString += " " + mc.getProgramID();
					} 
					if ( !keyList.contains(conTypeString) ) {
						keyList.add(conTypeString);
					}
					Collection<Expression> Col = typeMap.get(conTypeString);
					if ( Col == null ) {
						Col = new ArrayList<Expression>();
						typeMap.put(conTypeString, Col);
					}
					Col.add(c);
				}
			}
		}
		for ( Expression c : this ) {
			Term conType = c.getType();
			if ( !ordering.contains(conType) ) {
				String conTypeString = conType.toString();
				if ( conType.equals(ExpressionTypes.Prolog) ) {
					PrologConstraint pc = (PrologConstraint)c;
					conTypeString += " " + pc.getProgramID();
				} else if  ( conType.equals(ExpressionTypes.MiniZinc) ) {
					MiniZincInput mc = (MiniZincInput)c;
					conTypeString += " " + mc.getProgramID();
				} 
				if ( !keyList.contains(conTypeString) ) {
					keyList.add(conTypeString);
				}
				Collection<Expression> Col = typeMap.get(conTypeString);
				if ( Col == null ) {
					Col = new ArrayList<Expression>();
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
				
			for ( Expression c : typeMap.get(conType) ) {
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
	
	/**
	 * Get all constraints that implement the {@link Matchable} interface.
	 * @return all matchable constraints
	 */
	public Collection<Matchable> getMatchable() {
		ArrayList<Matchable> r = new ArrayList<Matchable>();
		
		for ( Class<? extends Expression> cl : KeyList ) {
			List<Expression> C = this.Cmap.get(cl);
			if ( C != null ) {
				for ( Expression c : C ) {
					if ( c.isMatchable() ) { //c instanceof Matchable ) {
						r.add( (Matchable)c);
					}
				}
			}
		}
		return r;
	}
	
	/**
	 * Remove all constraints with a specific class.
	 * @param type constraint class that should be removed
	 */
	public <T extends Expression> void removeType( Class<T> type ) {
		this.Cmap.remove(type);
	}
	
	/**
	 * Get all constraints of a given class
	 * @param c class of constraint
	 * @return list of all constraints of class <code>c</code>, empty list if none exist
	 */	
	public <T extends Expression> List<T> get( Class<T> c ) {
		@SuppressWarnings("unchecked")
		ArrayList<T> r = (ArrayList<T>)Cmap.get(c); 
		if ( r == null ) {
			r = new ArrayList<T>();
		}
		return r; 
	}
	
	
	/**
	 * Get map from constraint classes to their count.
	 * @return size of all internal constraint lists
	 */
	public Map<Class<? extends Expression>,Integer> getConstraintCount() {
		Map<Class<? extends Expression>,Integer> r = new HashMap<Class<? extends Expression>, Integer>();
		
		for ( Class<? extends Expression> cl : Cmap.keySet() ) {
			r.put(cl,Cmap.get(cl).size());
		}
		return r;
	}
	
	/**
	 * Given a count map (see <code>getConstraintCount()</code>), remove 
	 * constraints from all lists until they have the same size as in the 
	 * given count map.
	 * @param cCount a map from constraint classes to the target sizes of constraint lists
	 */
	public void setToConstraintCount( Map<Class<? extends Expression>,Integer> cCount ) {
	
		for (  Class<? extends Expression> cl : cCount.keySet() ) {
			List<Expression> L = this.Cmap.get(cl);
			for ( int i = L.size()-1 ; i >= cCount.get(cl) ; i-- ) {
				L.remove(i);
			}
		}
		
		for ( Class<? extends Expression> cl : this.Cmap.keySet() ) {
			if ( !cCount.keySet().contains(cl) ) {
				this.Cmap.get(cl).clear();
			}
		}
	}
	
	
//	private static int copyDepth = 0;
	
	/**
	 * Create a copy of this constraint database.
	 * @return the copy
	 */
	public ConstraintDatabase copy() {
//		Profiler.probe(1);
//		copyDepth++;
		
//		StopWatch.start("Copy Collection");
		
		ConstraintDatabase C = new ConstraintDatabase();
		for ( Class<? extends Expression> cl : KeyList ) {
//			StopWatch.start(("Copy class " + copyDepth + " " + cl.toString()));
			List<Expression> thisC = Cmap.get(cl);
			if ( thisC != null ) {
				ArrayList<Expression> Col = new ArrayList<Expression>(thisC.size());
				C.Cmap.put(cl,Col);
				
				if ( !thisC.isEmpty() ) {
	//				StopWatch.start("instanceof");
//					boolean needCopy =  (thisC.get(0) instanceof Mutable);
	//				StopWatch.stop("instanceof");
					
					if ( thisC.get(0).isMutable() ) { 
						for ( Expression c : thisC ) {
	//						StopWatch.stop(("Copy class " + copyDepth + " " + cl.toString()));
							Col.add( ((Mutable)c).copy());
	//						StopWatch.start(("Copy class " + copyDepth + " " + cl.toString()));
						}
					} else {
	//					StopWatch.stop( ("Copy class " + copyDepth + " " + cl.toString()));
						Col.addAll(thisC);
	//					StopWatch.start(("Copy class " + copyDepth + " " + cl.toString()));
					}
				} 
			}
//			StopWatch.stop(("Copy class " + copyDepth + " " + cl.toString()));
		}
//		StopWatch.stop("Copy Collection");
//		copyDepth--;
		return C;
	}

	@Override
	public boolean add(Expression arg0) { 
		List<Expression> C = Cmap.get(arg0.getClass());
		if ( C == null || (arg0.isUnique()) ) { // instanceof Unique ))  {
			if ( !KeyList.contains(arg0.getClass()) ) {
				KeyList.add(arg0.getClass());
			}
			C = new ArrayList<Expression>();
			Cmap.put(arg0.getClass(), C);
		}
		if ( arg0.isRepeatable() ) { // instanceof Repeatable ) {
			return C.add(arg0);
		}
		if ( !C.contains(arg0) ) {
			return C.add(arg0);  
		} else {
			return false;
		}
	}
		
	@Override
	public boolean addAll(Collection<? extends Expression> arg0) { 
		boolean r = false;
		boolean singleAddChange = false;
		for ( Expression c : arg0 ) {
			singleAddChange = this.add(c); 
			r = r || singleAddChange;
		}
		return r; 	
	}

	@Override
	public void clear() {  Cmap.clear();  }

	@Override
	public boolean contains(Object arg0) {
		List<Expression> C = Cmap.get(arg0.getClass());
		if ( C == null ) {
//			C = new ArrayList<Constraint>();
//			Cmap.put(arg0.getClass(), C);
			return false;
		}
		return C.contains(arg0); 
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		for ( Object c : arg0 ) {
			if ( !this.contains(c) ) {
				return false;
			}
		}
		return true; 	
	}
		
	@Override
	public boolean isEmpty() {
		if ( Cmap.isEmpty() ) {
			return true;
		} 
		for ( Class<? extends Expression> key : KeyList ) {
			List<Expression> C = Cmap.get(key);
			if ( C != null ) {
				if ( !Cmap.get(key).isEmpty() ) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean remove(Object arg0) {
		List<Expression> C = Cmap.get(arg0.getClass());
		if ( C == null ) {
//			C = new ArrayList<Constraint>();
//			Cmap.put(arg0.getClass(), C);
			return false;
		}
		return C.remove(arg0); 
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean r = false;
		boolean singleRemoveChange = false;
		for ( Object c : arg0 ) {
			singleRemoveChange = this.remove(c); 
			r = r || singleRemoveChange;
		}
		return r; 	
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean r = false;
		boolean singleRemoveChange = false;
		for ( Class<? extends Expression> cl : KeyList ) {
			singleRemoveChange = Cmap.get(cl).removeAll(arg0);
			r = r || singleRemoveChange;
		}
		return r;  
	}

	@Override
	public int size() { 
		int size = 0;
		for ( Class<? extends Expression> cl : KeyList ) {
			List<Expression> C = Cmap.get(cl); 
			if ( C != null ) {
				size += Cmap.get(cl).size();
			}
		}
		return size;
	}
		
	@Override
	public Iterator<Expression> iterator() {
		ArrayList<Expression> C = new ArrayList<Expression>();
		for ( Class<? extends Expression> cl : KeyList ) {
			List<Expression> C_cl = Cmap.get(cl); 
			if ( C_cl != null )
			C.addAll(C_cl);
		}
		return C.iterator();
	}

	@Override
	public Object[] toArray() {
		ArrayList<Expression> C = new ArrayList<Expression>();
		for ( Class<? extends Expression> cl : KeyList ) {
			C.addAll(Cmap.get(cl));
		}
		return C.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		ArrayList<Expression> C = new ArrayList<Expression>();
		for ( Class<? extends Expression> cl : KeyList ) {
			C.addAll(Cmap.get(cl));
		}
		return C.toArray(arg0);
	}
	
	@Override
	public int hashCode() {
		return this.Cmap.hashCode();
	}
	@Override
	public boolean equals( Object o ) {
		if ( o instanceof ConstraintDatabase ) {
			ConstraintDatabase C = (ConstraintDatabase)o;
			return this.Cmap.equals(C.Cmap);
		}
		return false;
	}
	
	
	/**
	 * Perform substitution on all constraints.
	 * @param theta the substitution
	 */
	public void substitute(Substitution theta) {
		for ( Class<? extends Expression> cl : KeyList ) {
			List<Expression> C = Cmap.get(cl); 
			if ( C != null ) {
				for ( int i = 0 ; i < C.size() ;i++ ) {
					if ( C.get(i).isSubstitutable() ) { // instanceof Substitutable ) {
						C.set(i, ((Substitutable)C.get(i)).substitute(theta));
					} else {
						continue;
					}
				}
			}
		}
	}
	
	/**
	 * Apply an {@link Asserted} constraint. Asserted constraints will no longer
	 * be considered by solvers.
	 * @param a constraint that asserts another constraint
	 */
	public void processAsserted( Asserted a ) {
//		StopWatch.start("processAsserted");
		for ( Class<? extends Expression> cl : KeyList ) {
			List<Expression> C = Cmap.get(cl);
			if ( C != null ) {
				for ( int i = 0 ; i < C.size() ;i++ ) {
					if ( C.get(i).isAssertable() ) { // instanceof Assertable ) {
						if ( a.appliesTo(C.get(i))) {
							C.set(i, ((Assertable)C.get(i)).setAsserted(true));
						}
					} else {
						continue;
					}
				}
			}
		}
//		StopWatch.stop("processAsserted");
	}
	
	/**
	 * Reset static list of keys. Used when solving more than one problem 
	 * without restarting the planner. In most cases one should use
	 * {@link Global}.resetStatics() instead which takes care of all static
	 * information that needs to be reset.
	 */
	public static void resetStatic() {
		KeyList = new ArrayList<Class<? extends Expression>>();
	}
}



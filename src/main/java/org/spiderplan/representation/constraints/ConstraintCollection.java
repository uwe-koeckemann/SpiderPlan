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
package org.spiderplan.representation.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.spiderplan.representation.constraints.constraintInterfaces.Assertable;
import org.spiderplan.representation.constraints.constraintInterfaces.Matchable;
import org.spiderplan.representation.constraints.constraintInterfaces.Mutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Repeatable;
import org.spiderplan.representation.constraints.constraintInterfaces.Substitutable;
import org.spiderplan.representation.constraints.constraintInterfaces.Unique;
import org.spiderplan.representation.logic.Substitution;

/**
 * Implements Collection<Constraint> and adds some convenience methods, 
 * such as getting all constraints of a certain class, getting all
 * constraints that implement {@link Substitutable} or getting all 
 * constraints that implement {@link Matchable}.
 * 
 * Type of internal storage can be selected by choosing from {@link StorageClass}
 * when constructing the {@link ConstraintCollection} instance.
 * 
 * @author Uwe Koeckemann
 *
 */
public class ConstraintCollection implements Collection<Constraint> {
	
	private Map<Class,List<Constraint>> Cmap = new HashMap<Class, List<Constraint>>();
		
	public Collection<Matchable> getMatchable() {
		ArrayList<Matchable> r = new ArrayList<Matchable>();
		
		for ( Class cl : this.Cmap.keySet() ) {
			for ( Constraint c : this.Cmap.get(cl) ) {
				if ( c instanceof Matchable ) {
					r.add( (Matchable)c);
				}
			}
		}
		return r;
	}
	
	public <T extends Constraint> void removeType( Class<T> type ) {
		this.Cmap.remove(type);
	}
	
	/**
	 * Get all constraints of Class T
	 * @param type
	 * @return
	 */	
	public <T extends Constraint> List<T> get( Class<T> type ) {
		ArrayList r = (ArrayList) Cmap.get(type); 
		if ( r == null ) {
			r = new ArrayList<Constraint>();
		}
		return r; 
	}
	
	
	public Map<Class,Integer> getConstraintCount() {
		Map<Class,Integer> r = new HashMap<Class, Integer>();
		
		for ( Class cl : this.Cmap.keySet() ) {
			r.put(cl,Cmap.get(cl).size());
		}
		return r;
	}
	
	public void setToConstraintCount( Map<Class,Integer> cCount ) {
	
		for (  Class cl : cCount.keySet() ) {
			List<Constraint> L = this.Cmap.get(cl);
			for ( int i = L.size()-1 ; i >= cCount.get(cl) ; i-- ) {
				L.remove(i);
			}
		}
		
		for ( Class cl : this.Cmap.keySet() ) {
			if ( !cCount.keySet().contains(cl) ) {
				this.Cmap.get(cl).clear();
			}
		}
	}
	
	
//	private static int copyDepth = 0;
	
	public ConstraintCollection copy() {
//		Profiler.probe(1);
//		copyDepth++;
		
//		StopWatch.start("Copy Collection");
		
		ConstraintCollection C = new ConstraintCollection();
		for ( Class cl : this.Cmap.keySet() ) {
//			StopWatch.start(("Copy class " + copyDepth + " " + cl.toString()));
			List<Constraint> thisC = Cmap.get(cl);
	
			ArrayList<Constraint> Col = new ArrayList<Constraint>(thisC.size());
			C.Cmap.put(cl,Col);
			
			if ( !thisC.isEmpty() ) {
//				StopWatch.start("instanceof");
				boolean needCopy =  (thisC.get(0) instanceof Mutable);
//				StopWatch.stop("instanceof");
				
				if ( needCopy ) { 
					for ( Constraint c : thisC ) {
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
			
//			StopWatch.stop(("Copy class " + copyDepth + " " + cl.toString()));
		}
//		StopWatch.stop("Copy Collection");
//		copyDepth--;
		return C;
	}

	@Override
	public boolean add(Constraint arg0) { 
		List<Constraint> C = Cmap.get(arg0.getClass());
		if ( C == null || (arg0 instanceof Unique ))  {
			C = new ArrayList<Constraint>();
			Cmap.put(arg0.getClass(), C);
		}
		if ( arg0 instanceof Repeatable ) {
			return C.add(arg0);
		}
		if ( !C.contains(arg0) ) {
			return C.add(arg0);  
		} else {
			return false;
		}
	}
		
	@Override
	public boolean addAll(Collection<? extends Constraint> arg0) { 
		boolean r = true;
		for ( Constraint c : arg0 ) {
			r = r && this.add(c);
		}
		return r; 	
	}

	@Override
	public void clear() {  Cmap.clear();  }

	@Override
	public boolean contains(Object arg0) {
		List<Constraint> C = Cmap.get(arg0.getClass());
		if ( C == null ) {
			C = new ArrayList<Constraint>();
			Cmap.put(arg0.getClass(), C);
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
		for ( Class key : Cmap.keySet() ) {
			if ( !Cmap.get(key).isEmpty() ) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean remove(Object arg0) {
		List<Constraint> C = Cmap.get(arg0.getClass());
		if ( C == null ) {
			C = new ArrayList<Constraint>();
			Cmap.put(arg0.getClass(), C);
		}
		return C.remove(arg0); 
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		boolean r = true;
		for ( Object c : arg0 ) {
			r = r && this.remove(c);
		}
		return r; 	
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		boolean r = true;
		for ( Class cl : Cmap.keySet() ) {
			r = r && Cmap.get(cl).removeAll(arg0);
		}
		return r;  
	}

	@Override
	public int size() { 
		int size = 0;
		for ( Class cl : Cmap.keySet() ) {
			size += Cmap.get(cl).size();
		}
		return size;
	}
	
	@Override
	public String toString() {
		StringBuilder r = new StringBuilder();
		for ( Class cl : Cmap.keySet() ) { 
			for ( Constraint c : this.Cmap.get(cl) ) {
				r.append("[");
				r.append(c.getClass().getSimpleName());
				r.append("] ");
				r.append(c.toString());
				r.append("\n");
			}
		}
		return r.toString();
	}

	@Override
	public Iterator<Constraint> iterator() {
		ArrayList<Constraint> C = new ArrayList<Constraint>();
		for ( Class cl : Cmap.keySet() ) {
			C.addAll(Cmap.get(cl));
		}
		return C.iterator();
	}

	@Override
	public Object[] toArray() {
		ArrayList<Constraint> C = new ArrayList<Constraint>();
		for ( Class cl : Cmap.keySet() ) {
			C.addAll(Cmap.get(cl));
		}
		return C.toArray();
	}

	@Override
	public <T> T[] toArray(T[] arg0) {
		ArrayList<Constraint> C = new ArrayList<Constraint>();
		for ( Class cl : Cmap.keySet() ) {
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
		if ( o instanceof ConstraintCollection ) {
			ConstraintCollection C = (ConstraintCollection)o;
			return this.Cmap.equals(C.Cmap);
		}
		return false;
	}
	
	public void substitute(Substitution theta) {
		for ( Class cl : this.Cmap.keySet() ) {
//			if ( cl.isAssignableFrom(Substitutable.class) ) {
//				System.out.println(cl.getSimpleName());
				List<Constraint> C = Cmap.get(cl); 
				for ( int i = 0 ; i < C.size() ;i++ ) {
					if ( C.get(i) instanceof Substitutable ) {
						C.set(i, ((Substitutable)C.get(i)).substitute(theta));
					} else {
						continue;
					}
				}
//			}
		}
	}
	
	public void processAsserted( Asserted a ) {
//		StopWatch.start("processAsserted");
		for ( Class cl : this.Cmap.keySet() ) {
			List<Constraint> C = Cmap.get(cl); 
			for ( int i = 0 ; i < C.size() ;i++ ) {
				if ( C.get(i) instanceof Assertable ) {
					if ( a.appliesTo(C.get(i))) {
						C.set(i, ((Assertable)C.get(i)).setAsserted(true));
					}
				} else {
					continue;
				}
			}
		}
//		StopWatch.stop("processAsserted");
	}
}

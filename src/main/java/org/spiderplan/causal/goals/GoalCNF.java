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
package org.spiderplan.causal.goals;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A conjunction of {@link Goal}s.
 * 
 * @author Uwe Köckemann
 *
 */
public class GoalCNF implements List<Goal> {
		
		private ArrayList<Goal> C;
		private StorageClass storageClass;
		
		public enum StorageClass { ArrayList, LinkedList, HashSet };
		
		public GoalCNF( ) {
			C = new ArrayList<Goal>();
		}
		
		/**
		 * Set a {@link Goal}'s reached state to true or false.
		 * @param g A {@link Goal}
		 * @param reached true if {@link Goal} has been reached. 
		 */
		public void setReached( Goal g, boolean reached ) {
			for ( Goal gPart : C ) {
				gPart.setReached(g, reached);
			}
		}
		
		/**
		 * Select {@link StorageClass} of this class.
		 * @return
		 */
		public StorageClass getStorageClass() {
			return this.storageClass;
		}
		
		/**
		 * Create a copy of this object.
		 * @return
		 */
		public GoalCNF copy() {
			GoalCNF cnfCopy = new GoalCNF();
			for ( Goal c : this.C ) {
				cnfCopy.add(c.copy());
			}			
			return cnfCopy;
		}
		
		@Override
		public String toString() { return C.toString(); }
		@Override
		public boolean add(Goal arg0) { 	return C.add(arg0);  }
		@Override
		public boolean addAll(Collection<? extends Goal> arg0) { return C.addAll(arg0); 	}
		@Override
		public void clear() {  C.clear();  }
		@Override
		public boolean contains(Object arg0) { return C.contains(arg0); }
		@Override
		public boolean containsAll(Collection<?> arg0) { return C.containsAll(arg0);  }
		@Override
		public boolean isEmpty() { return C.isEmpty();  }
		@Override
		public Iterator<Goal> iterator() { return C.iterator();  }
		@Override
		public boolean remove(Object arg0) {  return C.remove(arg0); }
		@Override
		public boolean removeAll(Collection<?> arg0) {  return C.removeAll(arg0);  }
		@Override
		public boolean retainAll(Collection<?> arg0) {  return C.retainAll(arg0);  }
		@Override
		public int size() { return C.size(); 	}
		@Override
		public Object[] toArray() { return C.toArray();  }
		@Override
		public <T> T[] toArray(T[] arg0) {  return C.toArray(arg0);  	}
		@Override
		public void add(int arg0, Goal arg1) { this.C.add(arg0, arg1); }
		@Override
		public boolean addAll(int arg0, Collection<? extends Goal> arg1) { return this.C.addAll(arg0, arg1); }
		@Override
		public Goal get(int arg0) {	return this.C.get(arg0); 	}
		@Override
		public int indexOf(Object arg0) { 	return this.C.indexOf(arg0); }
		@Override
		public int lastIndexOf(Object arg0) { return this.C.lastIndexOf(arg0); 	}
		@Override
		public ListIterator<Goal> listIterator() { return this.C.listIterator(); 		}
		@Override
		public ListIterator<Goal> listIterator(int arg0) { return this.C.listIterator(arg0); 		}
		@Override
		public Goal remove(int arg0) { return this.C.remove(arg0); 		}
		@Override
		public Goal set(int arg0, Goal arg1) { return this.C.set(arg0, arg1); 		}
		@Override
		public List<Goal> subList(int arg0, int arg1) { 			return this.C.subList(arg0, arg1); 		}	
}

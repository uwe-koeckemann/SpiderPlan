///*******************************************************************************
// * Copyright (c) 2015 Uwe Köckemann <uwe.kockemann@oru.se>
// *  
// * Permission is hereby granted, free of charge, to any person obtaining
// * a copy of this software and associated documentation files (the
// * "Software"), to deal in the Software without restriction, including
// * without limitation the rights to use, copy, modify, merge, publish,
// * distribute, sublicense, and/or sell copies of the Software, and to
// * permit persons to whom the Software is furnished to do so, subject to
// * the following conditions:
// *
// * The above copyright notice and this permission notice shall be
// * included in all copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
// * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
// *******************************************************************************/
//package org.spiderplan.representation.plans;
//
//import java.util.ArrayList;
//
//import org.spiderplan.representation.Operator;
//import org.spiderplan.representation.logic.Atomic;
//import org.spiderplan.representation.logic.Substitution;
//
///**
// * Interface for plan that consist of {@link Atomic} operator names, {@link Substitution}s to create actions given the operator,
// * and a unique ID allowing to make symbols in operators unique.
// * <br><br>
// * <b>Note:</b> Independent on the type of operator, which could e.g. be of class {@link Operator}. 
// * The only connection to the operator is the {@link Atomic} <i>name</i>. 
// * @author Uwe Köckemann
// *
// */
//public abstract class OrderedPlan {
//	
//	/**
//	 * Add action to plan.
//	 * @param name name of the action
//	 * @param theta substitution
//	 */
//	public abstract void add( Atomic name, Substitution theta );
//	/**
//	 * Add action to plan.
//	 * @param name name of the action
//	 * @param theta substitution
//	 * @param ID unique ID of action
//	 */
//	public abstract void add( Atomic name, Substitution theta, Integer ID );
//	/**
//	 * Add action to plan.
//	 * @param i where to add action
//	 * @param name name of the action
//	 * @param theta substitution
//	 */
//	public abstract void add( int i, Atomic name, Substitution theta );
//	/**
//	 * Add action to plan.
//	 * @param i where to add action
//	 * @param name name of the action
//	 * @param theta substitution
//	 * @param ID unique ID of action
//	 */
//	public abstract void add( int i, Atomic name, Substitution theta, Integer ID );
//	
//	/**
//	 * Test if layer <code>i</code> of the plan contains an action.
//	 * @param i
//	 * @param name
//	 * @return <code>true</code> if the action is in layer <code>i</code>, <code>false</code> otherwise
//	 */
//	public abstract boolean contains( int i , Atomic name );
//
//	/**
//	 * Remove action layer <code>i</code>.
//	 * @param i
//	 */
//	public abstract void remove( int i );
//	/**
//	 * Remove action <code>j</code> in layer <code>i</code>.
//	 * @param i
//	 * @param j
//	 */
//	public abstract void remove( int i, int j );
//		
//	/**
//	 * Get all action names in layer <code>i</code> as strings.
//	 * @param i
//	 * @return list of action name strings
//	 */
//	public abstract ArrayList<String> getNames( int i );
//	public abstract ArrayList<Atomic> getAtomics( int i );
//	public abstract ArrayList<Substitution> getSubstitutions( int i );
//	public abstract ArrayList<Integer> getUniqueIDs( int i );
//	
//	public abstract String getName( int i, int j );
//	public abstract Atomic getAtomic( int i, int j );
//	public abstract Substitution getSubstitution( int i, int j );
//	public abstract Integer getUniqueID( int i, int j );
//		
//	public abstract int length();
//	public abstract int size( int i );
//	
//	public abstract SequentialPlan getSequentialPlan();
//	
//	@Override
//	public abstract String toString();
//	public abstract OrderedPlan copy();
//
//	public abstract boolean isEmpty();
//}

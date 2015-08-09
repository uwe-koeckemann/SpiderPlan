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
//package org.spiderplan.causal.pocl;
//
//import org.spiderplan.representation.Operator;
//import org.spiderplan.representation.constraints.AllenConstraint;
//import org.spiderplan.representation.logic.Substitutable;
//import org.spiderplan.representation.logic.Substitution;
//
///**
// * Simple data-structure that stores POCL resolvers
// * 
// * @author 
// */
//public class Resolver implements Substitutable {
//
//	/**
//	 * Substitution
//	 */
//	public Substitution theta = null;;
//	/**
//	 * Added action
//	 */
//	public Operator action = null;
//	/**
//	 * Added causal link
//	 */
//	public AllenConstraint causalLink = null;	
//	
//	@Override
//	public String toString() {
//		String s = "(";
//		if ( action != null ) {
//			s += action.getName().toString() + ","; 
//		} else {
//			s += "-,";
//		}
//		if ( causalLink != null ) {
//			s += causalLink.toString() + ",";
//		} else {
//			s += "-,";
//		}		
//		if ( theta != null ) {
//			s += theta.toString();
//		} else {
//			s += "-";
//		}
//		return s + ")";
//	}
//	
//	@Override
//	public void substitute(Substitution theta) {
//		if ( action != null ) {
//			action.substitute(theta); 
//		}
//		if ( causalLink != null ) {
//			causalLink.substitute(theta);
//		}
//	}
//	
//	public Resolver copy() {
//		Resolver r = new Resolver();
//		if ( theta != null ) {
//			r.theta = this.theta.copy();
//		}
//		if ( action != null ) {
//			r.action = this.action.copy(); 
//		}
//		if ( causalLink != null ) {
//			r.causalLink = this.causalLink.copy();
//		}
//		return r;
//	}
//}

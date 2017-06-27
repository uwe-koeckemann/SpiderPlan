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
package org.spiderplan.representation.types;

import java.util.ArrayList;
import java.util.List;

import org.spiderplan.representation.logic.Term;


/**
 * Type with domain consisting of a set of symbols.
 * @author Uwe Köckemann
 */
public class EnumType extends Type {
	protected ArrayList<Term> D = new ArrayList<Term>();
	
	/**
	 * Create new type
	 */
	public EnumType() {}; 
	
	/**
	 * Extend the domain with a new term. 
	 * @param t Term to add to the domain
	 */
	public void addToDomain( Term t ) {
		if ( !this.D.contains(t) )
			this.D.add(t);
	}
	
	/**
	 * Get the domain as it is kept by the type.
	 * This means that sub-types will not be expanded.
	 * If possible it will be more efficient to work with 
	 * this method rather than generateDomain which will
	 * expand all types which can be combinatorial. 
	 * 
	 * @return all unexpanded members of the domain 
	 */
	public List<Term> getRawDomain( ) {
		List<Term> r = new ArrayList<Term>();
		for ( Term t : D ) {
			r.add(t);
		}
		return r;
	}
	
	@Override
	public boolean contains( Term s, TypeManager tM ) {
		if ( D.contains(s) )
			return true;
		
		for ( Term x : this.D ) {
			if ( !x.isComplex() ) {
				if ( tM.hasTypeWithName(x) ) {
					if ( tM.getTypeByName(x).contains(s, tM) ) {
						return true;
					}
				}
			}
		}

		for ( Term x : this.D ) {
			boolean termMatches = true;
			if ( x.getName().equals(s.getName()) && x.getNumArgs() == s.getNumArgs() ) {
				for ( int i = 0 ; i < x.getNumArgs() ; i++ ) {
					if ( x.getArg(i).equals(s.getArg(i)) )
						continue;
					if ( tM.hasTypeWithName(x.getArg(i)) ) {
						if ( tM.getTypeByName(x.getArg(i)).contains(s.getArg(i), tM) ) {
							continue;
						}
					}
					termMatches = false;
					break;
				}
			} else {
				termMatches = false;
			}
			if ( termMatches ) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		if ( D.size() > 0 ) {
			String r = name + " = { " + D.get(0);
			
			for ( int i = 1 ; i < D.size(); i++ ) {
				r += ", " + D.get(i);
			}
			r += " }";
			
			return r;
		} else {
			return name + " = {}";
		}
	}

	

	@Override
	public List<Term> generateDomain( TypeManager tM ) {

		List<Term> r = new ArrayList<Term>();
		for ( Term  s : this.D ) {
			r.addAll(tM.getAllGroundCombos(s));
		}
		return r;
	}
}

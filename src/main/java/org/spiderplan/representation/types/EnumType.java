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
package org.spiderplan.representation.types;

import java.util.ArrayList;

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
	
//	public EnumType( String name, String domainCSV ) {
//		super.name = new Term(name);
//
//		for ( String c : domainCSV.replace("\t", "").replace(" ", "").trim().split(",") ) {
//			D.add(new Term(c,false));
//		}
//	}
//	
//	public EnumType( Term name, String domainCSV ) {
//		super.name = name;
//		
//		String[] dList = domainCSV.replace("\t", "").replace(" ", "").trim().split(",");
//		for ( String c : dList ) {
//			D.add(new Term(c));
//		}
//	}
	
//	public EnumType( String s ) {
//		String[] dList = s.replace("\t", "").replace(" ", "").trim().split(",");
//		for ( String c : dList ) {
//			D.add(new Term(c));
//		}
//	}
	
	@Override
	public boolean contains(Term s) {
		return D.contains(s);
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

//	@Override
//	public ArrayList<String> getStringDomain() {
//		return D;
//	}

	@Override
	public ArrayList<Term> getDomain() {
		return D;
//		ArrayList<Term> r = new ArrayList<Term>();
//		for ( String  s : this.D ) {
//			r.add(new Term(s));
//		}
//		return r;
	}
}

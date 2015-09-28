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

import org.spiderplan.representation.logic.Atomic;
import org.spiderplan.representation.logic.Term;

public class SupportedExpressions {
	
	private Term constraintType;
	private ArrayList<ArrayList<Object>> supported = new ArrayList<ArrayList<Object>>();
	private String generalHelpText = null;
	
	public SupportedExpressions( Term constraintType ) {
		this.constraintType = constraintType;
	}
	
	public void add( String uniqueName, String exampleUsage, String helpText, Object relation, String className ) {
		ArrayList<Object> entry = new ArrayList<Object>();
		entry.add(uniqueName);
		entry.add(exampleUsage);
		entry.add(helpText);
		entry.add(relation);
		entry.add(className);
		supported.add(entry);
	}
	
	public void add( String uniqueName, String exampleUsage, String helpText, Object relation, Class<? extends Constraint> c ) {
		ArrayList<Object> entry = new ArrayList<Object>();
		entry.add(uniqueName);
		entry.add(exampleUsage);
		entry.add(helpText);
		entry.add(relation);
		entry.add(c.getSimpleName());
		supported.add(entry);
	}
	
	public void setGeneralHelpText( String s ) {
		this.generalHelpText = s;
	}
	
	public void assertSupported( Atomic a, Class<? extends Constraint> c ) {
		this.assertSupported( a, c.getSimpleName() );	
	}
	
	public void assertSupported( Atomic a, String simpleClassName ) {
		boolean isSupported = false;
		for ( ArrayList<Object> entry : supported ) {
			if ( a.getUniqueName().equals(entry.get(0)) && simpleClassName.equals(entry.get(4)) ) {
				isSupported = true;
				break;
			}
		}
		if ( !isSupported ) {
			String s = a.toString() + " ("+a.getUniqueName()+") not supported. List of supported relations:\n" + this.toString();
			throw new IllegalArgumentException(s);
		}
	}	
	
	@Override
	public String toString() {
		String s = "==========================================\n";
		s += "Constraint type: " + this.constraintType + "\n";
		s += "==========================================\n";
		if ( generalHelpText != null ) {
			s += generalHelpText + "\n"; 
			s += "==========================================\n";
		}
		
		for (  ArrayList<Object> entry : supported ) {
			s += entry.get(0) + "\n\tUsage: " + entry.get(1) + "\n\tMeaning: " + entry.get(2) + "\n\tInternal representation: " + entry.get(3).getClass().getSimpleName() +"." + entry.get(3) + "\n\tImplemented by: " + entry.get(4) + "\n";
		}
		return s;
	}
}

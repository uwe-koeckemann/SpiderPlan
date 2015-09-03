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
package org.spiderplan.causal.pocl.flaws;

import java.util.Collection;
import org.spiderplan.representation.constraints.OpenGoal;
import org.spiderplan.representation.logic.Substitution;

/**
 * Contains all flaws. Implementing classes take care of flaw selection strategy.
 * 
 * @author Uwe K&ouml;ckemann
 *
 */
public abstract class FlawCollection {
	public abstract void add( OpenGoal openGoal );
	
	public abstract OpenGoal select();
	
	public abstract FlawCollection copy();
	
	public abstract boolean isEmpty();
	
	public abstract int size();
	
	public abstract Collection<OpenGoal> getAll();
	
	public abstract void substitute( Substitution theta );
}


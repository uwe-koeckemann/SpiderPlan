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
package org.spiderplan;

import org.spiderplan.modules.configuration.FlowRule;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Core.State;

import junit.framework.TestCase;

@SuppressWarnings("javadoc")
public class TestFlowRules extends TestCase {

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}
	
	public void testSimpleRule() {
		FlowRule r = new FlowRule("START => B");
		Core c = new Core();
		assertTrue( r.applies("START" , c));
		assertFalse( r.applies("Module", c));
	}
	
	public void testRuleWithCondition() {
		FlowRule r = new FlowRule("A => C.Consistent => B");
		Core c = new Core();
		assertFalse( r.applies("A", c) );
		c.setResultingState("C", State.Consistent);
		assertTrue( r.applies("A", c) );
	}
		
		
}


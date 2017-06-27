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
package org.spiderplan;

import java.util.ArrayList;

import org.spiderplan.modules.configuration.ConfigurationManager;
import org.spiderplan.modules.solvers.Core;
import org.spiderplan.modules.solvers.Module;
import org.spiderplan.modules.tools.ModuleFactory;
import org.spiderplan.representation.ConstraintDatabase;
import org.spiderplan.representation.expressions.ValueLookup;
import org.spiderplan.representation.logic.Term;
import org.spiderplan.representation.parser.Compile;

// contents from imports.java will end up after this comment


import junit.framework.TestCase;

/**
 * Automatically generated. Any changes will be overwritten the 
 * next time you run gradle.
 */
public class TestAutoGenTemporal extends TestCase {
	
	@Override
	public void setUp() throws Exception {
	}  

	@Override
	public void tearDown() throws Exception { 
	}
	
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
	/**
	 * Testing the starts and started-by constraints.
	 */ 	
	public void testStartsStartedByConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/StartsStartedByConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("Iref")) == 10);
		assertTrue(valueLookup.getLST(Term.createConstant("Iref")) == 10);
		assertTrue(valueLookup.getEET(Term.createConstant("Iref")) == 20);
		assertTrue(valueLookup.getLET(Term.createConstant("Iref")) == 20);
		assertTrue(valueLookup.getEST(Term.createConstant("Iref")) == 10);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 10);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 15);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 15);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 10);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 10);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 15);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 15);
		
	}		
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
	/**
	 * Testing the before and after constraints.
	 */ 	
	public void testBeforeAfterConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/BeforeAfterConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 68);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 10);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 78);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 11);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 79);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 21);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 89);
		assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 22);
		assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 90);
		assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 32);
		assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 100);
		
	}		
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
	/**
	 * Testing the meets constraint.
	 */ 	
	public void testMeetsInconsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/MeetsInconsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Testing the overlaps and overlapped-by constraints.
	 */ 	
	public void testOverlapsContainsConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/OverlapsContainsConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 87);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 10);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 97);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 2);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 89);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 12);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 99);
		assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 3);
		assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 90);
		assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 13);
		assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 100);
		
	}		
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
	/**
	 * Tests a simple at constraint.
	 */ 	
	public void testAtConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/AtConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
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
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 30);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 50);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 70);
		
	}		
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
	/**
	 * Testing the starts and started-by constraints. This one fails because
	 * the starting interval has to be shorter than the one it starts (which
	 * is not the case here).
	 */ 	
	public void testStartsStartedByInconsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/StartsStartedByInconsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Testing the before and after constraints.
	 */ 	
	public void testBeforeAfterInconsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/BeforeAfterInconsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Tests a simple duration constraint.
	 */ 	
	public void testDurationConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/DurationConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
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
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 80);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 20);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 100);
		
	}		
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
	/**
	 * Tests two duration constraints leading to inconsistency.
	 */ 	
	public void testDurationInconsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/DurationInconsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Tests a simple release constraint.
	 */ 	
	public void testReleaseInonsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/ReleaseInonsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Tests a simple release constraint.
	 */ 	
	public void testReleaseConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/ReleaseConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 35);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 40);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 35);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 100);
		
	}		
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
	/**
	 * Testing the overlaps and overlapped-by constraints.
	 */ 	
	public void testOverlapsContainsInconsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/OverlapsContainsInconsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		
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
	/**
	 * Testing the meets constraint.
	 */ 	
	public void testMeetsConsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/MeetsConsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Consistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		ConstraintDatabase resultCDB = resultCore.getContext();
		ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
		assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
		assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 70);
		assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 10);
		assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 80);
		assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 10);
		assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 80);
		assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 20);
		assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 90);
		assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 20);
		assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 90);
		assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 30);
		assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 100);
		
	}		
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
	/**
	 * Tests a simple duration constraint.
	 */ 	
	public void testAtInonsistent() {
		String plannerFilename = "./domains/test-cases/Temporal/temporal.spider";
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
		domainFiles.add("./domains/test-cases/Temporal/AtInonsistent/temporal.uddl");

	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("Inconsistent"));
		// Code from .java files in the test case folder will end up below (except imports.java)
		
	}		

}


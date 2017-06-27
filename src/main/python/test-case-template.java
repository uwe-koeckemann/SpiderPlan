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
<TEST-CASE-COMMENT> 	
	public void test<TEST-CASE-NAME>() {
		<LOAD-PLANNER>
		
		ArrayList<String> domainFiles = new ArrayList<String>(); 
<LOAD-DOMAIN-FILES>
	
		Compile.compile( domainFiles, plannerFilename );
		ConfigurationManager oM = Compile.getPlannerConfig();
		Module main = ModuleFactory.initModule("main", oM);
		Core initCore = Compile.getCore();
		Core resultCore = main.run(initCore);
		String resultStr = resultCore.getResultingState("main").toString();
		assertTrue(resultStr.equals("<EXPECTED-RESULT>"));
		// Code from .java files in the test case folder will end up below (except imports.java)
<EXTRA-TESTS-JAVA>
	}		

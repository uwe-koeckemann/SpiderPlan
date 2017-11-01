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

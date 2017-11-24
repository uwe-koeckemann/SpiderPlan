#!/bin/python
# -*- coding: utf-8 -*-

#Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>

#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:

#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.

#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

# Automatically create test cases based on what is found in 
#		<SPIDERPLAN>/domains/test-cases
# Should allow creating test cases very easily without hand-creating
# CSBs in Java code.
# Creates one JUnit test file for each sub-folder.
# Expected outcome is stored in "result" file.
# 	Ŝhould be either "SATISFIABLE" or "INCONSISTENT"

# TODO:
# Load all files
# Create java code by substituting into template
#		Load single spider
# 	Load all uddl
# 	Add correct assertion
#   Substitute additional .java files (containing assertions) 
# Call as part of gradle 

import sys
import os
import glob


def complieAllCases(dirName):
	allFiles = []
	testCases = {}
	for root, dirnames, filenames in os.walk(dirName):
	#for root, dirnames, filenames in os.walk('Test'):	
		#for dirname in dirnames:
		for filename in filenames:
			allFiles.append(os.path.join(root, filename))
			#problemDirs.append(dirname)
		
	for fName in allFiles:
		tmp = fName.split("/")
		if len(tmp) == 6:
			collectionName = tmp[3]
			testCaseName = tmp[4]
			testCaseFile = tmp[5]
			
			if not collectionName in testCases.keys():
				testCases[collectionName] = {}
			if not testCaseName in testCases[collectionName].keys():
				testCases[collectionName][testCaseName] = {}
			testCases[collectionName][testCaseName][testCaseFile] = fName
			
				
		elif len(tmp) == 5:
			collectionName = tmp[3]
			fileNameShort = tmp[4]
			if not collectionName in testCases.keys():
				testCases[collectionName] = {}
			if not "all" in testCases[collectionName].keys():
				testCases[collectionName]["all"] = {}
			testCases[collectionName]["all"][fileNameShort] = fName
			print "Added: ", fName, "with key", fileNameShort
	for collectionName in testCases.keys():
		print "Collection:", collectionName
		
		f = open("./src/main/python/test-case-collection-template.java", "r")
		collectionStr = f.read()
		f.close()
		
		collectionStr = collectionStr.replace("<TEST-COLLECTION-NAME>", collectionName)
		
		javaImportStr = ""
		if "all" in testCases[collectionName].keys() and "imports.java" in testCases[collectionName]["all"].keys():
			f = open(testCases[collectionName]["all"]["imports.java"],"r")
			javaImportStr = f.read()
			f.close()
						
		collectionStr = collectionStr.replace("<EXTRA-IMPORTS-JAVA>", javaImportStr)
		
		testCasesStr = ""		
		
		for testCaseName in testCases[collectionName].keys():
			if testCaseName != "all":
				print "\tTestCase:", testCaseName
				
				f = open("./src/main/python/test-case-template.java", "r")
				testCaseStr = f.read()
				f.close()
				
				foundPlanner = False
				foundDomain = False
				foundComment = False
				foundResult = False
				foundJava = False
				foundDoc = False
				
				loadDomainStr = ""
				javaStr = ""
				javaImportStr = ""
				
				testCaseStr = testCaseStr.replace("<TEST-CASE-NAME>", testCaseName)
				
				for fNameShort in testCases[collectionName][testCaseName].keys():
					fName = testCases[collectionName][testCaseName][fNameShort]
					print "\t\t\t",fName
					if fNameShort == "result":
						foundResult = True
						f = open(fName, "r")
						resultStr = f.read()
						f.close()
						resultStr = resultStr.replace("\n", "")
						testCaseStr = testCaseStr.replace("<EXPECTED-RESULT>",resultStr)
					elif fNameShort == "documentation.txt":
						foundDoc = True
						f = open(fName, "r")
						docStr = f.read()
						f.close()
						docStr = "\t/**\n\t * " + docStr.replace("\n", "\n\t * ")
						docStr = docStr[0:-1] + "/"
						testCaseStr = testCaseStr.replace("<TEST-CASE-COMMENT>", docStr)
					elif fNameShort.split(".")[-1] == "spider":
						foundPlanner = True
						testCaseStr = testCaseStr.replace("<LOAD-PLANNER>", 'String plannerFilename = "%s";' %(fName))
					elif fNameShort.split(".")[-1] == "uddl":
						foundDomain = True
						loadDomainStr += '\t\tdomainFiles.add("%s");\n' %(fName)	
					elif fNameShort.split(".")[-1] == "java":
						if not fNameShort == "imports.java":
							foundJava = True	
							f = open(fName, "r")
							javaStr += f.read()
							f.close()
				if "all" in testCases[collectionName].keys():
					for fNameShort in testCases[collectionName]["all"].keys():
						fName = testCases[collectionName]["all"][fNameShort]
						print "\t\t\t",fName
						if fNameShort == "result":
							foundResult = True
							f = open(fName, "r")
							resultStr = f.read()
							f.close()
							resultStr = resultStr.replace("\n", "")
							testCaseStr = testCaseStr.replace("<EXPECTED-RESULT>",resultStr)
						elif fNameShort == "documentation.txt":
							foundDoc = True
							f = open(fName, "r")
							docStr = f.read()
							f.close()
							docStr = "\t/**\n\t * " + docStr.replace("\n", "\n\t * ")
							docStr = docStr[0:-1] + "/"
							testCaseStr = testCaseStr.replace("<TEST-CASE-COMMENT>", docStr)
						elif fNameShort.split(".")[-1] == "spider":
							foundPlanner = True
							testCaseStr = testCaseStr.replace("<LOAD-PLANNER>", 'String plannerFilename = "%s";' %(fName))
						elif fNameShort.split(".")[-1] == "uddl":
							foundDomain = True
							loadDomainStr += '\t\tdomainFiles.add("%s");\n' %(fName)	
						elif fNameShort.split(".")[-1] == "java":
							if not fNameShort == "imports.java":
								foundJava = True	
								f = open(fName, "r")
								javaStr += f.read()
								f.close()	
						#print "\t\t",fNameShort,testCases[collectionName][testCaseName][fNameShort]
				
				if not foundDoc:
					docStr = "\t/**\n\t * TODO: add documentation.txt with comment to test case folder to put something here.\n\t */"
					testCaseStr = testCaseStr.replace("<TEST-CASE-COMMENT>", docStr)
				if not foundResult:
					print "ERROR: No expected result found.\nPut a file 'result' in the test case folder that contains either 'Consistent' or 'Inconsistent' depending on the expected outcome of the test case. Exiting..."
					sys.exit()
				if not foundPlanner:
					print "ERROR: No planner definition found. Make sure test case folder contains a .spider file that defines the planner. Exiting..."
					sys.exit()
				if not foundDomain:
					print "ERROR: No domain definition found. Make sure test case folder contains at lease one .uddl file that contains a domain definition. Exiting..."
					sys.exit()
				
				testCaseStr = testCaseStr.replace("<LOAD-DOMAIN-FILES>", loadDomainStr)
				testCaseStr = testCaseStr.replace("<EXTRA-TESTS-JAVA>", "\t\t"+javaStr.replace("\n","\n\t\t"))
				testCasesStr += testCaseStr
				
		testCaseStr = testCaseStr.replace("<EXTRA-IMPORTS-JAVA>", javaImportStr)
		collectionStr = collectionStr.replace("<TEST-CASES>", testCasesStr)
		outName = "./src/test/java/org/spiderplan/TestAutoGen%s.java"%(collectionName)
		f = open(outName, "w")
		f.write(collectionStr)
		f.close()
		print "\tWrote to:", outName

complieAllCases("./domains/test-cases/")	
	
	
#for root, dirs, files in os.walk("."):
    #path = root.split(os.sep)
    #print((len(path) - 1) * '---', os.path.basename(root))
    #for file in files:
        #print(len(path) * '---', file)
	


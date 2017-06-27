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

import os
import sys
import fnmatch

def getAllJavafiles(dirName):
	allJavaFiles = []
	for root, dirnames, filenames in os.walk(dirName):
	#for root, dirnames, filenames in os.walk('Test'):	
		for filename in fnmatch.filter(filenames, '*.java'):
			allJavaFiles.append(os.path.join(root, filename))
	return allJavaFiles

toCompile = ["domain_v4", "pddl", "planner", "experiment"]

for dirName in toCompile:
	print dirName
	os.system("rm ../java/org/spiderplan/representation/parser/"+dirName+"/*.java")
	os.system("javacc -OUTPUT_DIRECTORY='../java/org/spiderplan/representation/parser/"+dirName+"/' ../javacc/"+dirName+".jj")

	allGenFiles = getAllJavafiles('../java/org/spiderplan/representation/parser/' + dirName)
	
	for fName in allGenFiles:
		f = open(fName, "r")
		lines = f.readlines()
		f.close()
		
		argMax = 0
		for i in range(len(lines)):
			if "import " in lines[i] or "package " in lines[i]:
				argMax = i
		
		f = open(fName, "w")
		found = False
		for i in range(len(lines)):
			if i == argMax:
				f.write(lines[i] + '@SuppressWarnings("all")\n')
			else:
				f.write(lines[i])
		f.close()
		
		
		
		
		
		
		

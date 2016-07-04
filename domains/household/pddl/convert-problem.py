import sys
import glob

def files2problems(exp):
	l = glob.glob(exp)
	l.sort()
	return l
	
f = open("fixed-relations.pddl")
fixedRelationsStr = f.read()
f.close()
f = open("preferences.pddl")
preferencesStr = f.read()
f.close()
problems = files2problems(sys.argv[1])

for fName in problems:
	f = open(fName)
	allContent = f.read()
	f.close()
	
	taskTypes = allContent.split("(:domain")[1].split("(:statement")[0].split("( enum robotTask ( list ")[1].split(")")[0].replace("\n"," ").replace("_","-") + " - robot-task"
	
	s = "(define (problem "+fName.replace("/","-").replace(".","-")+")\n\t(:domain household)\n\t(:objects\n\t\t"+taskTypes+"\n\t)"
	
	initialState = ""
	
	statementMap = {}
	

	for statement in allContent.split("(:statement")[1].split("(:goal")[0].split("\n"):
		statement = statement.strip()
		statement = statement.replace("\t","").replace("\n","")
		if not (statement == "" or statement == ")"):
			if "key" in statement and not "Batch" in statement:
				initialStatePart = "\t\t(" + statement.split(" ( ")[1].replace(" ) "," ").replace(" )",")") + "\n"
				#print initialStatePart
				initialState +=initialStatePart  
			elif not "Batch" in statement:
				key = statement.split("(")[1].split(" ( ")[0].strip()
				#print key
				var = "(" + statement.split(" ( ")[1].split(" true")[0].strip().replace(" )",")")
				#print var
				statementMap[key] = var.replace("humanActivity","human-activity").replace("out)","being-out)")
				#print statement
	#print "===="
	for tc in allContent.split("(:temporal")[1].split("(:cost")[0].split("\n"):
		tc = tc.strip()
		if "( at" in tc and "] [" in tc:
			key = tc.split("at ")[1].split(" [")[0]

			tStart = tc.split("[")[1].split(" ")[0]
			tEnd = tc.split("[")[2].split(" ")[0]
			
			initialStatePart1 = "\t\t(at " + tStart + " " + statementMap[key] + ")"
			
			initialStatePart2 = "\t\t(at " + tEnd + " (not " + statementMap[key] + "))"
			
			initialState += initialStatePart1 + "\n"
			initialState += initialStatePart2 + "\n"
	goals = "\n\t(:goal (and"	
	for goal in allContent.split("(:goal")[1].split("(:temporal")[0].split("\n"):
		goal = goal.strip()
		if "state" in goal:
			goal = "(" + goal.split(" ( ")[1].split(" ) ")[0] + " finished)"
			#print goal
			goals += "\n\t\t" + goal
			#print key
			#print tStart,tEnd
			
			#print initialStatePart1
			#print initialStatePart2
			#print tc 
	#print "===="
	#print goals
	
	s += "\n\t(:init\n"
	
	s += initialState + "\n"
	s += fixedRelationsStr + "\n"
	s += "\n\t)"
	
	s += goals + "\n\t))\n"
	
	s += preferencesStr + "\n"
	
	s += "\n)" # closing (define 
	
	print s
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
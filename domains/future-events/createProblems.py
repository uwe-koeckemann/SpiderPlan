

def create(numLocs,moveTime):
	pStr = ""
	eventTime = (numLocs-2)*moveTime+moveTime//2
	
	pStr += "(define (problem future-events-test01)\n"
	pStr += "(:domain futue-events)\n"
	pStr += "\n"
	pStr += "(:objects \n"
	pStr += "r1  - robot\n"
	for i in range(1,numLocs):
		pStr += "l%d " %(i)
	pStr += "l%d - location\n" %(numLocs)
	pStr += "door1 none - door\n"
	pStr += "k1 - key\n"
	pStr += ")\n"
	pStr += "\n"
	pStr += "(:init\n"
	pStr += "(robot-at r1 l1)\n"
	pStr += "(key-at k1 l1)\n"
	pStr += "\n"
	pStr += "(is-open door1)\n"
	pStr += "(is-open none)\n"
	pStr += "\n"
	for i in range(1,numLocs-1):
		pStr += "(door-between l%d l%d none)\n" %(i,i+1)
		pStr += "(door-between l%d l%d none)\n" %(i+1,i)
	pStr += "(door-between l%d l%d door1)\n" %(numLocs	,numLocs-1)
	pStr += "(door-between l%d l%d door1)\n" %(numLocs-1,numLocs)
	pStr += "\n"
	pStr += "(door-at door1 l%d)\n" %(numLocs)
	pStr += "(door-at door1 l%d)\n" %(numLocs-1)
	pStr += "\n"
	pStr += "(needs-key door1 k1)\n"
	pStr += "\n"
	pStr += "(at %d (not (is-open door1)))\n" %(eventTime)
	pStr += ")\n"
	pStr += "\n"
	pStr += "(:goal\n"
	pStr += "(and\n"
	pStr += "(robot-at r1 l"+str(numLocs)+")\n"
	pStr += "))\n"
	pStr += ")\n"
	return pStr
	
for numLocs in range(3,100+1):
	pStr = create(numLocs,1000)
	f = open("p"+str(numLocs)+".pddl", "w")
	f.write(pStr)
	f.close()
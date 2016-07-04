import sys
import os

maxNumPairs = int(sys.argv[1])
numVars = int(sys.argv[2])
targetDir = sys.argv[3]

os.system("mkdir " + targetDir)

def addZeros(n,m):
	s = str(n)
	while len(s) < m:
		s = "0" + s
	return s

for numPairs in range(1,maxNumPairs+1):
	intCounter = 0
	m = {}
	s = "(:initial-context\n"

	s += "\t(:statement\n"
	for i in range(numPairs):
		for j in range(1,numVars+1):
			intName = "Ix%d_%d"%(j,i)
			m[intName] = intCounter
			intCounter += 1	
			s += "\t\t(%d (x%d))\n" %(m[intName],j)
			#s += "\t\t(Ix%d_%d (x%d))\n" %(j,i,j)
	s += "\t)\n"

	s += "\t(:temporal\n"
	for i in range(numPairs):
		t1 = i*4
		t2 = i*4+3
		for j in range(1,numVars+1):
			intName = "Ix%d_%d"%(j,i)
			s += "\t\t(at %d [%d %d] [%d %d])\n" %(m[intName],t1,t2,t1,t2)
			s += "\t\t(duration %d [1 1])\n" %(m[intName])
			
			#s += "\t\t(at Ix%d_%d [%d %d] [%d %d])\n" %(j,i,t1,t2,t1,t2)
			#s += "\t\t(duration Ix%d_%d [1 1])\n" %(j,i)
	s += "\t)\n"
	s += ")"

	fName = targetDir + "/test" + addZeros(numPairs,3) + ".uddl"
	
	f = open(fName,"w")
	f.write(s)
	f.close()
	
	
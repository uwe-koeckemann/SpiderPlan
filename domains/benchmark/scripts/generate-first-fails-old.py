import sys
import os

maxNumPairs = int(sys.argv[1])
#numVars = int(sys.argv[2])
targetDir = sys.argv[2]

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
		intNameX = "Ix%d"%(i)
		intNameY = "Iy%d"%(i)
		m[intNameX] = intCounter
		intCounter += 1
		m[intNameY] = intCounter
		intCounter += 1	
		s += "\t\t(%d (x1))\n" %(m[intNameX])
		s += "\t\t(%d (x2))\n" %(m[intNameY])
		
		#s += "\t\t(Ix%d (x1))\n" %(i)
		#s += "\t\t(Iy%d (x2))\n" %(i)
	s += "\t)\n"

	s += "\t(:temporal\n"
	for i in range(numPairs):
		intNameX = "Ix%d"%(i)
		intNameY = "Iy%d"%(i)
		
		t1 = i*6+1
		t2 = i*6+5
		t3 = i*6
		t4 = i*6+3
		s += "\t\t(at %d [%d %d] [%d %d])\n" %(m[intNameX],t1,t2,t1,t2)
		s += "\t\t(at %d [%d %d] [%d %d])\n" %(m[intNameY],t3,t4,t3,t4)
		s += "\t\t(duration %d [2 2])\n" %(m[intNameX])
		s += "\t\t(duration %d [2 2])\n" %(m[intNameY])
	s += "\t)\n"
	s += ")"

	fName = targetDir + "/test" + addZeros(numPairs,3) + ".uddl"
	
	f = open(fName,"w")
	f.write(s)
	f.close()
	
	
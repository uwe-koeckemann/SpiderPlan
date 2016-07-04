import os
import random
import sys

numProblems = 20
numGoalsList = [5,10,15,20,25,30,35,40]
minST = 100
maxST = 10000
minDuration = 50
maxDuration = 100

n_sensors = 10
n_concepts = 30
n_edges = 60
n_objects = 15
n_locations = 10
n_configurations = 5
p_dependency = 0.1

minLinkCost = 0
maxLinkCost = 100

locationPrefix = "l"
objectPrefix = "o"
configurationPrefix = "config"
conceptPrefix = "c"
sensorPrefix = "s"

targetFolderDomain = "./"
targetFolderProblem = "./problems/"



def createDOTfile(m,nameMap):
	s = "digraph information {\n"
	for i in range(len(m)):
		s += '\t%d [label="%s"]\n' %(i, nameMap[i])
	for i in range(len(m)):
		for j in range(len(m)):
			if m[i][j] == 1:
				s += "\t%d -> %d;\n" %(i,j)
	s += "}"
	f = open(targetFolderDomain + "information.dot", "w")
	f.write(s)
	f.close()

def getStats(m,nameMap):
	inDegreeListSensors = []
	outDegreeListSensors = []
	inDegreeListConcepts = []
	outDegreeListConcepts = []
	
	for i in range(len(m)):
		outDegree = sum(m[i])
		inDegree = 0
		for j in range(len(m)):
			inDegree += m[j][i]
		
		if i < n_sensors:
			inDegreeListSensors.append(inDegree)
			outDegreeListSensors.append(outDegree)
		else:
			inDegreeListConcepts.append(inDegree)
			outDegreeListConcepts.append(outDegree)
			
		
		print "%s has %d in and %d out edges" %(nameMap[i], inDegree, outDegree)
	
	print "In-degree (Sensors): min=%d max=%d avg=%f" %(min(inDegreeListSensors), max(inDegreeListSensors), float(sum(inDegreeListSensors)) / float(len(inDegreeListSensors)))
	print "Out-degree (Sensors): min=%d max=%d avg=%f" %(min(outDegreeListSensors), max(outDegreeListSensors), float(sum(outDegreeListSensors)) / float(len(outDegreeListSensors)))
	
	print "In-degree (Concepts): min=%d max=%d avg=%f" %(min(inDegreeListConcepts), max(inDegreeListConcepts), float(sum(inDegreeListConcepts)) / float(len(inDegreeListConcepts)))
	print "Out-degree (Concepts): min=%d max=%d avg=%f" %(min(outDegreeListConcepts), max(outDegreeListConcepts), float(sum(outDegreeListConcepts)) / float(len(outDegreeListConcepts)))

def generateDomain():
	good = False

	while not good:
		dim = n_sensors + n_concepts
		nameMap = []
		m = []
		for i in range(dim):
			if i < n_sensors:
				nameMap.append("%s%d" %(sensorPrefix,i+1))
			else:
				nameMap.append("%s%d" %(conceptPrefix,i-n_sensors+1))
			m.append([0]*dim)
			
		added_edges = 0
		# Make sure every concepts has a source
		for v_dest in range(n_sensors,n_sensors+n_concepts):
			v_source = random.randint(0,v_dest-1)
			added_edges += 1
			m[v_source][v_dest] = 1
			
		while added_edges < n_edges:
			v_source = random.randint(0,dim-1)
			v_dest = random.randint(0,dim-1)
			
			if v_source < v_dest and v_dest >= n_sensors and m[v_source][v_dest] == 0:
				added_edges += 1
				m[v_source][v_dest] = 1
				
		# Constraints
		inDegreeListConcepts = []
		for i in range(len(m)):
			inDegree = 0
			for j in range(len(m)):
				inDegree += m[j][i]
			if i >= n_sensors:
				inDegreeListConcepts.append(inDegree)
				
		# Constraints
		outDegreeListSensors = []
		for i in range(n_sensors):
			outDegreeListSensors.append(sum(m[i]))
				
					
				
		good = True
		if min(inDegreeListConcepts) == 0 or min(outDegreeListSensors) == 0:
			good = False
		else:
			print "Good!"
			
		dependencies = {}		
		mobileSensors = []
		canTarget = []
		needsConfig = []
		movableObjects = []
			
		print "Adding dependencies..."
		for i in range(0,n_sensors):
			for j in range(n_sensors,dim):
				if m[i][j] == 1:
					sample = random.random()
					if sample < p_dependency:
						dependency = random.randint(1,3)
						if dependency == 1: # location
							randomLocation = random.randint(1,n_locations)
							dependencyTerm = "atLocation(%s%d)"%(locationPrefix,randomLocation)
							if not i in mobileSensors:
								mobileSensors.append(i)
						elif dependency == 2: # targeted
							randomObject = random.randint(1,n_objects)
							dependencyTerm = "reqTargeting(%s%d)"%(objectPrefix,randomObject)
							if not i in canTarget:
								canTarget.append(i)
							sample2 = random.random()
							#if sample2 < 0.4:
							if not i in mobileSensors:
								mobileSensors.append(i)

							obj = "%s%d"%(objectPrefix,randomObject)
							if not obj in movableObjects:
								movableObjects.append(obj)
							if not nameMap[i] in movableObjects:
								movableObjects.append(nameMap[i])
								
						elif dependency == 3: # configuration
							randomConfig = random.randint(1,n_configurations)
							dependencyTerm = "hasConfig(%s%d)"%(configurationPrefix,randomConfig)
							if not i in needsConfig:
								needsConfig.append(i)
						dependencies[(i,j)] = dependencyTerm
						#nameMap[i] = "(%s %s)" %(nameMap[i], dependencyTerm)
						#for j in range(0, dim):
							#if m[i][j] == 1:
								#if not j in arguments.keys():
									#arguments[j] = []
								#if not dependencyTerm in arguments[j]:
									#arguments[j].append(dependencyTerm)
		#for i in range(dim):
			#if i in arguments.keys():
				#newName = "(" + nameMap[i]
				#for term in arguments[i]:
					#newName += " " + term
				#newName += ")"
				#nameMap[i] = newName
		
	getStats(m, nameMap)

	print "Connecting locations..."
	mLocs = []
	for i in range(n_locations):
		mLocs.append([0]*n_locations)
		
	connected = [1]
	not_connected = range(2,n_locations+1)
	while len(connected) < n_locations:
		pickA = random.randint(0,len(connected)-1)
		pickB = random.randint(0,len(not_connected)-1)
		
		l1 = connected[pickA]
		l2 = not_connected[pickB]
		
		mLocs[l1-1][l2-1] = 1
		
		connected.append(not_connected[pickB])
		del not_connected[pickB]
		
	print "Selecting starting locations for objects..."
	objectLocations = []
	for i in range(n_objects):
		pick = random.randint(1,n_locations)
		objectLocations.append("%s%d" %(locationPrefix,pick))

	print "Selecting starting configurations for sensors..."
	sensorConfigurations = []
	for i in range(len(needsConfig)):
		pick = random.randint(1,n_configurations)
		sensorConfigurations.append("%s%d" %(configurationPrefix,pick))
		
	print "Selecting starting locations for movable sensors..."
	sensorLocations = []
	for i in range(n_sensors):
		pick = random.randint(1,n_locations)
		sensorLocations.append("%s%d" %(locationPrefix,pick))

	print "Creating domain file."

	domain_str = "(:initial-context\n\t(:domain\n"
	domain_str += "\t\t(enum sensor {"

	for i in range(n_sensors):
		domain_str += " " + nameMap[i]
	domain_str += " })\n"

	domain_str += "\t\t(enum concept {"
	for i in range(n_sensors, dim):
		domain_str += " " + nameMap[i]
	domain_str += " })\n"

	domain_str += "\t\t(enum location {"
	for i in range(n_locations):
		domain_str += " %s%d" %(locationPrefix, (i+1))
	domain_str += " })\n"

	domain_str += "\t\t(enum objects { none"
	for i in range(n_objects):
		domain_str += " %s%d" %(objectPrefix, (i+1))
	domain_str += " })\n"

	domain_str += "\t\t(enum configuration {"
	for i in range(n_objects):
		domain_str += " %s%d" %(configurationPrefix, (i+1))
	domain_str += " })\n"
	domain_str += "\t)\n"

	print domain_str

	domain_str += "\t(:statement\n"
	print "Creating initial statements..."
	for i in range(n_objects):
		domain_str += "\t\t(|s0 (at %s%i) %s)\n" %(objectPrefix,(i+1),objectLocations[i])
	for i in range(n_sensors):
		domain_str += "\t\t(|s0 (at %s%i) %s)\n" %(sensorPrefix,(i+1),sensorLocations[i])
	for i in range(len(needsConfig)):
		domain_str += "\t\t(|s0 (config %s%i) %s)\n" %(sensorPrefix,(needsConfig[i]+1),sensorConfigurations[i])
	for i in range(len(canTarget)):
		domain_str += "\t\t(|s0 (targeting %s%i) none)\n" %(sensorPrefix,(canTarget[i]+1))
	domain_str += "\t)\n"
	domain_str += "\t(:temporal\n"
	domain_str += "\t\t(release s0 [0 inf])\n"
	domain_str += "\t)\n)"



	f = open(targetFolderDomain + "domain-generated.uddl", "w")	
	f.write(domain_str)
	f.close()

	print domain_str
		
	print "Creating Prolog code..."

	csp_ic_str = ""
	
	csp_ic_tmpl = "\t(:ic\n"
	csp_ic_tmpl += "\t\t(target-<REQ> ?I <SENSOR> <CONCEPT>)\n"
	csp_ic_tmpl += "\t\t(:condition (:statement (?I (link <CONCEPT> {<SENSOR>} <COST>))))\n"
	csp_ic_tmpl += "\t\t(:resolver\n"
	csp_ic_tmpl += "\t\t\t(:goal (?G (<SV> <SENSOR>) <VAL>))\n"
	csp_ic_tmpl += "\t\t\t(:temporal (during ?I ?G [1 inf] [1 inf]))))\n"
	
	csp_str = "(:initial-context\n\t(:configuration-planning\n"
	prolog_str = ""

	for i in range(len(mobileSensors)):
		prolog_str += "isMobile(%s).\n" %(nameMap[mobileSensors[i]].split(" ")[0].replace("(",""))
		
	prolog_str += "\n"
		
	for i in range(len(canTarget)):
		prolog_str += "canTarget(%s).\n" %(nameMap[canTarget[i]].split(" ")[0].replace("(",""))

	prolog_str += "\n"
		
	for oTerm in movableObjects:
		prolog_str += "isMovable(%s).\n" %(oTerm)
		
	prolog_str += "\n"
		
	for i in range(n_locations):
		for j in range(n_locations):
			if mLocs[i][j] == 1:
				prolog_str += "adjacencyTable(%s%i,%s%i).\n" %(locationPrefix,i+1,locationPrefix,j+1)
				
	prolog_str += "\n"

	prolog_str += "adjacent(A,B) :- adjacencyTable(A,B).\n"
	prolog_str += "adjacent(A,B) :- adjacencyTable(B,A).\n"

	prolog_str += "\n"

	for i in range(n_sensors):
		cost = random.randint(minLinkCost,maxLinkCost)
		csp_str += "\t\t(link %s%d {} %d)\n" %(sensorPrefix,(i+1),cost)
	
	for i in range(n_sensors):
		for j in range(n_sensors,dim):
			if m[i][j]:
				cost = random.randint(minLinkCost,maxLinkCost)
				if not (i,j) in dependencies.keys():
					prolog_str += "capabilitySensor(%s%d,none,%s%d,%d).\n" %(sensorPrefix,(i+1),conceptPrefix,(j-n_sensors+1),cost)
					csp_str += "\t\t(link %s%d {%s%d} %d)\n" %(conceptPrefix,(j-n_sensors+1),sensorPrefix,(i+1),cost)
				else:
					prolog_str += "capabilitySensor(%s%d,%s,%s%d,%d).\n" %(sensorPrefix,(i+1),dependencies[(i,j)],conceptPrefix,(j-n_sensors+1),cost)
					csp_str += "\t\t(link %s%d {%s%d} %d)\n" %(conceptPrefix,(j-n_sensors+1),sensorPrefix,(i+1),cost)
					
					depStr = "("+(dependencies[(i,j)].replace("("," "))
					req = dependencies[(i,j)].split("(")[0]
					value = dependencies[(i,j)].split("(")[1].split(")")[0]
					variable = None
					if req == "hasConfig":
						variable = "config"
					elif req == "atLocation":
						variable = "at"
					elif req == "reqTargeting":
						variable = "targeting"
						
					sensorStr = "%s%d" % (sensorPrefix,(i+1))
					conceptStr = "%s%d" %(conceptPrefix,(j-n_sensors+1))
					
					icStr = csp_ic_tmpl.replace("<REQ>", req).replace("<VAL>",value).replace("<SV>",variable).replace("<SENSOR>",sensorStr).replace("<CONCEPT>",conceptStr).replace("<COST>",str(cost))
					csp_ic_str += icStr
					
					
	for i in range(n_sensors,dim):
		inEdges = []
		for j in range(n_sensors,dim):
			if m[j][i] == 1:
				inEdges.append(j)
		
		if len(inEdges) > 0:
			numPartitions = random.randint(1,max(1,len(inEdges)//2))
			partitions = []
			for j in range(numPartitions):
				partitions.append([inEdges[j]])
			for j in range(numPartitions,len(inEdges)):
				pick = random.randint(0,numPartitions-1)
				partitions[pick].append(inEdges[j])
				
			for p in partitions:
				cost = random.randint(minLinkCost,maxLinkCost)
				tmpCSP = "\t\t(link %s%d {" %(conceptPrefix,i-n_sensors+1)
				tmp = "capability%d(" %(len(p))
				for j in range(len(p)):
					tmp += "%s%d," %(conceptPrefix,(p[j]-n_sensors+1))
					tmpCSP += " %s%d" %(conceptPrefix,(p[j]-n_sensors+1))
				tmp += "%s%d,%d).\n" %(conceptPrefix,i-n_sensors+1,cost)
				tmpCSP += " } %d)\n" %(cost)
				prolog_str += tmp
				csp_str += tmpCSP
				
				
			print partitions
	
	csp_str += "\t)\n"
	csp_str += csp_ic_str
	csp_str += ")"
	
	f = open(targetFolderDomain + "kb.prolog", "w")	
	f.write(prolog_str)
	f.close()
	
	f = open(targetFolderDomain + "domain-csp.uddl", "w")	
	f.write(csp_str)
	f.close()
		
	print prolog_str
		


	print "Creating dot file."
	createDOTfile(m, nameMap)
	os.system("dot -Tps information.dot  -o test.ps")
	print "Done."

	


def addZeros(s,n):
	return "0"*(n-len(s))+s
	
def generateProblems():
	possibleGoals = []
	for i in range(n_concepts):
		possibleGoals.append("%s%d"%(conceptPrefix,i+1))

	for numGoals in numGoalsList:
		for p in range(1,numProblems+1):
			G = []
			G_CSP = []
			TCs = []
			
			for i in range(1, numGoals+1):
				pick = random.randint(0,len(possibleGoals)-1)
				st = random.randint(minST,maxST)
				dur = random.randint(minDuration,maxDuration)
				
				g = "(G%d (inferring %s))" % (i,possibleGoals[pick])
				tc = "(at G%d [0 %d] [%d inf])" % (i,st,st+dur)
				
				g_csp = "(goal G%d %s)" %(i, possibleGoals[pick])
				
				G_CSP.append(g_csp)
				G.append(g)
				TCs.append(tc)

			
			s1 = "(:initial-context\n\t(:goal\n"
			s2 = "(:initial-context\n\t(:task\n"
			s3 = "(:initial-context\n\t(:configuration-planning\n"
			
			
			for g in G:
				s1 += "\t\t" + g + "\n"
				s2 += "\t\t" + g + "\n"
			for g in G_CSP:
				s3 += "\t\t" + g + "\n"
				
			s3 += "\t)\n\t(:statement\n"
			for g in G:
				s3 += "\t\t" + g + "\n"
				
			s1 += "\t)\n\t(:temporal\n"
			s2 += "\t)\n\t(:temporal\n"
			s3 += "\t)\n\t(:temporal\n"
				
			for tc in TCs:
				s1 += "\t\t" + tc + "\n"
				s2 += "\t\t" + tc + "\n"
				s3 += "\t\t" + tc + "\n"

			s1 += "\n\t)\n)"
			s2 += "\n\t)\n)"
			s3 += "\n\t)\n)"
			
			fName1 = "goal-problem-g%s-%s.uddl" %(addZeros(str(numGoals),3),addZeros(str(p),3))
			fName2 = "task-problem-g%s-%s.uddl" %(addZeros(str(numGoals),3),addZeros(str(p),3))
			fName3 = "csp-problem-g%s-%s.uddl" %(addZeros(str(numGoals),3),addZeros(str(p),3))
			
			f = open(targetFolderProblem + "goal-based/" + fName1, "w")
			f.write(s1)
			f.close()
			
			f = open(targetFolderProblem + "task-based/" + fName2, "w")
			f.write(s2)
			f.close()
			
			f = open(targetFolderProblem + "csp-based/" + fName3, "w")
			f.write(s3)
			f.close()

generateDomain()
generateProblems()



notEqual(A,B) :- A \== B.

adjacent(L1,L2) :- adjacencyTable(L1,L2).
adjacent(L1,L2) :- adjacencyTable(L2,L1).

adjacencyTable(out,floor1).
adjacencyTable(floor1,floor2).
adjacencyTable(basement,floor1).
adjacencyTable(basement,floor2).

adjacencyTable(floor1,bathroom1).
adjacencyTable(floor2,bathroom2).
adjacencyTable(floor1,livingRoom).
adjacencyTable(floor1,diningRoom).
adjacencyTable(diningRoom,kitchen).

adjacencyTable(floor1,kidsRoom1).
adjacencyTable(floor1,kidsRoom2).
adjacencyTable(floor2,bedroomParents).
adjacencyTable(floor2,study).

adjacencyTable(floor2,bedroomGrandparents1).
adjacencyTable(floor2,bedroomGrandparents2).
adjacencyTable(bedroomGrandparents1,bathroom3).
adjacencyTable(bedroomGrandparents2,bathroom4).

adjacencyTable(basement,robotRoom).
adjacencyTable(basement,laundryRoom).
adjacencyTable(basement,storage).

movingTimeTable(r1,floor1,floor2,5).
movingTimeTable(r1,floor1,basement,5).
movingTimeTable(r1,basement,floor2,5).
movingTimeTable(r2,floor1,floor2,5).
movingTimeTable(r2,floor1,basement,5).
movingTimeTable(r2,basement,floor2,5).
movingTimeTable(r3,floor1,floor2,5).
movingTimeTable(r3,floor1,basement,5).
movingTimeTable(r3,basement,floor2,5).
movingTimeTable(r4,floor1,floor2,5).
movingTimeTable(r4,floor1,basement,5).
movingTimeTable(r4,basement,floor2,5).

defaultMovingtime(r1,1).
defaultMovingtime(r2,1).
defaultMovingtime(r3,1).
defaultMovingtime(r4,1).

movingTime(R,L1,L2,T) :- movingTimeTable(R,L1,L2,T).
movingTime(R,L1,L2,T) :- movingTimeTable(R,L2,L1,T).
movingTime(R,L1,L2,T) :- defaultMovingtime(R,T).

capability(r1,vaccuum).
capability(r1,recharge_r1).
capability(r2,sortStuff).
capability(r2,laundry).
capability(r2,collectLaundry).
capability(r2,recharge_r2).
capability(r3,collectTrash).
capability(r3,takeOutTrash).
capability(r3,cleanRoom).
capability(r3,recharge_r3).
capability(r4,entertain).
capability(r4,assist).
capability(r4,recharge_r4).

executionTime(r1,vaccuum,30).
executionTime(r1,recharge_r1,120).
executionTime(r2,sortStuff,60).
executionTime(r2,laundry,60).
executionTime(r2,collectLaundry,10).
executionTime(r2,recharge_r2,240).
executionTime(r3,collectTrash,60).
executionTime(r3,takeOutTrash,5).
executionTime(r3,cleanRoom,40).
executionTime(r3,recharge_r3,60).
executionTime(r4,entertain,60).
executionTime(r4,assist,20).
executionTime(r4,recharge_r4,120).

married(mother,father).
married(grandmother1,grandfather1).
married(grandmother2,grandfather2).

locationType(bedroomParents,bedroom).
locationType(bedroomGrandparents1,bedroom).
locationType(bedroomGrandparents2,bedroom).

locationType(bathroom1,bathroom).
locationType(bathroom2,bathroom).
locationType(bathroom3,bathroom).
locationType(bathroom4,bathroom).

locationType(kidsRoom1,kidsRoom).
locationType(kidsRoom2,kidsRoom).

hasRole(mother,parent).
hasRole(father,parent).
hasRole(kid1,kid).
hasRole(kid2,kid).
hasRole(kid3,kid).
hasRole(kid4,kid).
hasRole(infant1,infant).
hasRole(infant2,infant).
hasRole(grandmother1,grandparent).
hasRole(grandmother2,grandparent).
hasRole(grandfather1,grandparent).
hasRole(grandfather2,grandparent).

hasProperty(mother,lightSleeper).
hasProperty(father,heavySleeper).
hasProperty(grandfather1,heavySleeper).
hasProperty(grandmother1,lightSleeper).

hasProperty(grandfather1,hatesRobots).

hasProperty(mother,lightSleeper).
hasProperty(father,lightSleeper).
hasProperty(kid1,normalSleeper).
hasProperty(kid2,normalSleeper).
hasProperty(kid3,normalSleeper).
hasProperty(kid4,normalSleeper).
hasProperty(infant1,lightSleeper).
hasProperty(infant2,lightSleeper).
hasProperty(grandmother1,normalSleeper).
hasProperty(grandmother2,heavySleeper).
hasProperty(grandfather1,normalSleeper).
hasProperty(grandfather2,heavySleeper).

hasProperty(kid1,easilyDistracted).
hasProperty(kid2,easilyDistracted).
hasProperty(kid3,easilyDistracted).
hasProperty(kid4,easilyDistracted).
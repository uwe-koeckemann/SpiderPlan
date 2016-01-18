notEqual(A,B) :- A \== B.
riskGreaterOrEqual(high,high).
riskGreaterOrEqual(high,medium).
riskGreaterOrEqual(high,low).
riskGreaterOrEqual(high,none).
riskGreaterOrEqual(medium,medium).
riskGreaterOrEqual(medium,low).
riskGreaterOrEqual(medium,none).
riskGreaterOrEqual(low,low).
riskGreaterOrEqual(low,none).

risk(labWork1,none).
risk(labWork2,low).				
risk(labWork3,medium).				
risk(labWork4,high).		

adjacencyTable(basement,lab1).
adjacencyTable(basement,lab2).

adjacencyTable(corridor1,basement).
adjacencyTable(corridor1,corridor2).
adjacencyTable(corridor1,corridor3).
adjacencyTable(corridor1,corridor4).
adjacencyTable(corridor2,basement).
adjacencyTable(corridor2,corridor3).
adjacencyTable(corridor2,corridor4).
adjacencyTable(corridor3,basement).
adjacencyTable(corridor3,corridor4).
adjacencyTable(corridor4,basement).

adjacencyTable(corridor1,office1_1).
adjacencyTable(corridor1,office1_2).
adjacencyTable(corridor1,office1_3).
adjacencyTable(corridor1,office1_4).
adjacencyTable(corridor1,office1_5).
adjacencyTable(corridor1,office1_6).
adjacencyTable(corridor1,office1_7).
adjacencyTable(corridor1,office1_8).
adjacencyTable(corridor1,office1_9).
adjacencyTable(corridor1,office1_10).

adjacencyTable(corridor2,office2_1).
adjacencyTable(corridor2,office2_2).
adjacencyTable(corridor2,office2_3).
adjacencyTable(corridor2,office2_4).
adjacencyTable(corridor2,office2_5).
adjacencyTable(corridor2,office2_6).
adjacencyTable(corridor2,office2_7).
adjacencyTable(corridor2,office2_8).
adjacencyTable(corridor2,office2_9).
adjacencyTable(corridor2,office2_10).

adjacencyTable(meetingRoom1,corridor1).
adjacencyTable(meetingRoom2,corridor1).

adjacent(L1,L2) :- adjacencyTable(L1,L2).
adjacent(L2,L1) :- adjacencyTable(L1,L2).

isMobile(r1).
isMobile(r2).
isMobile(r3).
isMobile(r4).

isMeetingRoom(meetingRoom1).
isMeetingRoom(meetingRoom2).

taskDuration(clean,30).
taskDuration(prepare,10).				
taskDuration(serveCoffee,5).				
taskDuration(labAssistance,1).
objectSize(file,1).
objectSize(parcel,2).
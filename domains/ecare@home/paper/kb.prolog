isMobile(s4).
isMobile(s7).
isMobile(s10).

canTarget(s4).
canTarget(s7).

isMovable(o11).
isMovable(s4).
isMovable(o15).
isMovable(s7).

adjacencyTable(l1,l6).
adjacencyTable(l1,l7).
adjacencyTable(l1,l8).
adjacencyTable(l1,l9).
adjacencyTable(l1,l10).
adjacencyTable(l8,l2).
adjacencyTable(l9,l4).
adjacencyTable(l9,l5).
adjacencyTable(l10,l3).

adjacent(A,B) :- adjacencyTable(A,B).
adjacent(A,B) :- adjacencyTable(B,A).

capabilitySensor(s1,none,c15,51).
capabilitySensor(s2,none,c26,68).
capabilitySensor(s2,none,c30,22).
capabilitySensor(s3,none,c4,91).
capabilitySensor(s3,none,c6,62).
capabilitySensor(s4,reqTargeting(o11),c2,60).
capabilitySensor(s4,none,c9,12).
capabilitySensor(s5,none,c10,64).
capabilitySensor(s5,none,c13,51).
capabilitySensor(s5,none,c20,85).
capabilitySensor(s6,none,c1,78).
capabilitySensor(s6,none,c6,45).
capabilitySensor(s6,none,c10,8).
capabilitySensor(s6,none,c16,13).
capabilitySensor(s7,none,c24,43).
capabilitySensor(s7,reqTargeting(o15),c26,44).
capabilitySensor(s8,none,c5,52).
capabilitySensor(s8,none,c8,56).
capabilitySensor(s8,hasConfig(config1),c10,57).
capabilitySensor(s8,none,c12,86).
capabilitySensor(s9,none,c3,53).
capabilitySensor(s9,none,c10,24).
capabilitySensor(s9,none,c11,18).
capabilitySensor(s9,none,c18,34).
capabilitySensor(s10,none,c14,59).
capabilitySensor(s10,atLocation(l4),c28,24).
capability1(c1,c4,82).
capability1(c6,c7,87).
capability1(c6,c13,75).
capability1(c12,c14,76).
capability1(c3,c15,52).
capability1(c1,c16,46).
capability3(c3,c4,c12,c17,61).
capability3(c1,c5,c7,c19,88).
capability3(c11,c15,c17,c21,17).
capability2(c1,c11,c22,78).
capability2(c20,c21,c23,81).
capability1(c22,c24,96).
capability5(c3,c10,c16,c22,c24,c25,24).
capability2(c9,c25,c26,54).
capability3(c7,c13,c20,c27,67).
capability3(c4,c5,c19,c29,63).
capability1(c8,c30,46).

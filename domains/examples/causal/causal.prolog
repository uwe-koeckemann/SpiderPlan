adjacencyTable(l1,l2).
adjacencyTable(l2,l3).
adjacencyTable(l3,l4).
adjacencyTable(l4,l5).
adjacencyTable(l5,l6).
adjacencyTable(l6,l7).
adjacencyTable(l7,l8).
adjacencyTable(l8,l9).
adjacencyTable(l5,l10).

adjacent(A,B) :- adjacencyTable(A,B).
adjacent(A,B) :- adjacencyTable(B,A).

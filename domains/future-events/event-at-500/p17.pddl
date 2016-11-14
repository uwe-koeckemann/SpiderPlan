(define (problem future-events-test01)
(:domain futue-events)

(:objects 
r1  - robot
l1 l2 l3 l4 l5 l6 l7 l8 l9 l10 l11 l12 l13 l14 l15 l16 l17 - location
door1 none - door
k1 - key
)

(:init
(robot-at r1 l1)
(key-at k1 l1)

(is-open door1)
(is-open none)

(door-between l1 l2 none)
(door-between l2 l1 none)
(door-between l2 l3 none)
(door-between l3 l2 none)
(door-between l3 l4 none)
(door-between l4 l3 none)
(door-between l4 l5 none)
(door-between l5 l4 none)
(door-between l5 l6 none)
(door-between l6 l5 none)
(door-between l6 l7 none)
(door-between l7 l6 none)
(door-between l7 l8 none)
(door-between l8 l7 none)
(door-between l8 l9 none)
(door-between l9 l8 none)
(door-between l9 l10 none)
(door-between l10 l9 none)
(door-between l10 l11 none)
(door-between l11 l10 none)
(door-between l11 l12 none)
(door-between l12 l11 none)
(door-between l12 l13 none)
(door-between l13 l12 none)
(door-between l13 l14 none)
(door-between l14 l13 none)
(door-between l14 l15 none)
(door-between l15 l14 none)
(door-between l15 l16 none)
(door-between l16 l15 none)
(door-between l17 l16 door1)
(door-between l16 l17 door1)

(door-at door1 l17)
(door-at door1 l16)

(needs-key door1 k1)

(at 15500 (not (is-open door1)))
)

(:goal
(and
(robot-at r1 l17)
))
)
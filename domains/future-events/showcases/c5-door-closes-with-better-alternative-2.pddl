(define (problem future-events-test01)
(:domain futue-events)

(:objects 
r1  - robot
l1 l2 l3 l4 l5 l6 l7 l8 l9 l10 l-detour-1 l-detour-2 - location
door1 none - door
k1 - key
)

(:init
(robot-at r1 l1)
(key-at k1 l1)

(is-open door1)
(is-open none)

(door-between l9 l-detour-1 none)
(door-between l-detour-1 l9 none)

(door-between l-detour-1 l-detour-2 none)
(door-between l-detour-2 l-detour-1 none)

(door-between l-detour-2 l10 none)
(door-between l10 l-detour-2 none)

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

(door-between l9 l10 door1)
(door-between l10 l9 door1)

(door-at door1 l9)
(door-at door1 l10)

(needs-key door1 k1)

(at 8500 (not (is-open door1)))

)

(:goal
(and
(robot-at r1 l10)
))
)

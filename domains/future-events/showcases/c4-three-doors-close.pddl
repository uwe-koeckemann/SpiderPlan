(define (problem future-events-test01)
(:domain futue-events)

(:objects 
r1  - robot
l1 l2 l3 l4 l5 - location
door1 door2 door3 none - door
k1 k2 k3 - key
)

(:init
(robot-at r1 l1)
(key-at k1 l1)
(key-at k2 l1)
(key-at k3 l1)

(is-open door1)
(is-open door2)
(is-open door3)
(is-open none)

(door-between l1 l2 none)
(door-between l2 l1 none)
(door-between l2 l3 door1)
(door-between l3 l2 door1)
(door-between l4 l3 door2)
(door-between l3 l4 door2)
(door-between l5 l4 door3)
(door-between l4 l5 door3)

(door-at door1 l2)
(door-at door1 l3)

(door-at door2 l4)
(door-at door2 l3)

(door-at door3 l5)
(door-at door3 l4)

(needs-key door1 k1)
(needs-key door2 k2)
(needs-key door3 k3)

(at 2000 (not (is-open door1)))
(at 3000 (not (is-open door2)))
(at 4000 (not (is-open door3)))
)

(:goal
(and
(robot-at r1 l5)
))
)

(define (problem future-events-test01)
(:domain futue-events)

(:objects 
r1  - robot
l1 l2 l3 l4 l5 l6 l7 - location
door1 none - door
k1 k2 - key
)

(:init
(robot-at r1 l1)
(key-at k1 l1)
(key-at k2 l1)

(is-open door1)
(is-open none)

(door-between l1 l2 door1)
(door-between l2 l1 door1)

(door-between l2 l3 door1)
(door-between l3 l2 door1)

(door-between l3 l4 door1)
(door-between l4 l3 door1)

(door-between l4 l5 door1)
(door-between l5 l4 door1)

(door-between l5 l6 door1)
(door-between l6 l5 door1)

(door-between l6 l7 door1)
(door-between l7 l6 door1)

(at 1500 (not (is-open door1)))
(at 3000 (is-open door1))
(at 4500 (not (is-open door1)))
(at 6000 (is-open door1))
(at 7500 (not (is-open door1)))
(at 9000 (is-open door1))
(at 10500 (not (is-open door1)))
(at 12000 (is-open door1))
(at 13500 (not (is-open door1)))
(at 15000 (is-open door1))
(at 16500 (not (is-open door1)))


)

(:goal
(and
(robot-at r1 l7)
))
)

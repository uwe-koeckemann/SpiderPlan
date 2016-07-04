(define (problem test-problem)
(:domain test-domain)

(:objects 
	r1  - robot
	l1, l2, l3 - location
	door1, none - door
)

(:init
	(robot-at r1 l1)

	(is-open none)
	(is-open door1)

	(at 15 (not (is-open door1))) 

	(door-between l1 l2 none)
	(door-between l2 l1 none)
	(door-between l2 l3 door1)
	(door-between l3 l2 door1)

	(door-at door1 l2)
	(door-at door1 l3)
)


(:goal
(and
	(robot-at r1 l3)
))
)
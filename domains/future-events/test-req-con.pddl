(define (problem future-events-test01)
(:domain futue-events)

(:objects 
r1,r2  - robot
l1,l2 - location
)

(:init
(robot-at r1 l1)
(robot-at r2 l2)
)


(:goal
(and

(robot-at r1 l2)
(robot-at r2 l1)

))
)
;;Created by Uwe Köckemann

(define (domain household)
 (:requirements :typing :durative-actions :constraints :fluents :timed-initial-literals :universal-preconditions :negative-preconditions)


 (:types 	robot location location-type robot-task robot-task-class
					robot-task-state human property role activity agent 
					- object)

(:predicates 	
		(at ?r - robot ?l - location)
		(adjacent ?l1 - location ?l2 - location)
	)
	(:functions 
		(execution-time ?r - robot ?c - robot-task-class) - number
		(movement-time ?l1 ?l2 - location) - number
	)
	(:constants 
		processing waiting finished - robot-task-state

		r1 r2 r3 r4 - robot

		robotroom floor1 floor2 basement livingRoom kitchen diningRoom bedroomParents bedroomGrandparents1 bedroomGrandparents2 kidsRoom1 kidsRoom2 bathroom1 bathroom2 bathroom3 bathroom4 laundryRoom storage out study - location

		bedroom bathroom kidsRoom - location-type

		vaccuum sortStuff assist collectTrash takeOutTrash collectLaundry laundry cleanRoom entertain recharge-r1 recharge-r2 recharge-r3 recharge-r4 - robot-task-class

		mother father kid1 kid2 kid3 kid4 infant1 infant2 grandmother1 grandmother2 grandfather1 grandfather2 - human

		lightSleeper normalSleeper heavySleeper easilyDistracted hatesRobots - property

		parent grandparent kid infant - role

		idle being-out eating sleeping reading working cooking playing usingBathroom - activity
	)


	(:durative-action move
		:parameters (?r - robot ?l1 - location ?l2 - location)
		:duration (= ?duration (movement-time ?l1 ?l2)) 
		:condition (and (at start (at ?r ?l1))
										(over all (adjacent ?l1 ?l2)))
		:effect (and 		(at start (not (at ?r ?l1)))
										(at end (at ?r ?l2)))
	)
)
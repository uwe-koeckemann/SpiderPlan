;; Created by Uwe Köckemann

;; No solution. Tests if constraints are considered at all.

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		(human-activity)
		(ready r1)
	)
	(:goal (task-is-done))
)

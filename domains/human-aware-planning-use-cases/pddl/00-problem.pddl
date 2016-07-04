;; Created by Uwe Köckemann

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		(ready r1)
	)
	(:goal (task-is-done))
)

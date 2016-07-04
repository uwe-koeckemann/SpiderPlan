;; Created by Uwe Köckemann

;; Solution: 200.001: (solve-task r1)  [100.000]

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		(at 0 (not (no-human-activity)))
		(at 200 (no-human-activity)) ;; Any valid plan has to start after this
		(ready r1)
	)
	(:goal (task-is-done))
)
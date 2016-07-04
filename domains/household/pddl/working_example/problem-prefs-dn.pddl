;; Created by Uwe Köckemann

;; (A)+(B1)+(C):

;; Expected solution: 
;; Cost: 0.000
;; 200.001: (solve-task r1)  [100.000] 
;; -or-
;; Cost: >0.000
;; 0.002: (solve-task r1)  [100.000] (cost: 0)

;; Optic solution: 
;; Cost: 0.000
;; 0.002: (solve-task r1)  [100.000] 

;; (A)+(B2)+(C):

;; Expected solution == Optic solution: 
;; Cost: 0.000
;; 0.002: (solve-task r1)  [100.000] 

;; (C) 
;; Expected solution == Optic solution: 
;; Cost: 0.000
;; 200.001: (solve-task r1)  [100.000]  

(define (problem pfile0)
 (:domain household)
	(:objects r1 - robot)
	(:init 
		;;(at 0 (not-human-activity)) ;; (A)
		;;(at 10 (not (not-human-activity))) ;; (B1)
		;;(at 50 (not (not-human-activity))) ;; (B2)
		(at 200 (not-human-activity)) ;; (C)
		(ready r1)
	)
	(:constraints
		(preference p1 
			(always
				(not 
					(and 
						(not (not-human-activity))
						(making-noise)
					)
				)
			)
		)
	)

	(:goal (task-is-done))
  (:metric minimize (* 10000 (is-violated p1)))
)


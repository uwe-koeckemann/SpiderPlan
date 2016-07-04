	;; Preferences
	(:constraints
		;; IC ( LightSleeper ?H ?L ?T ?C ?I1 ?I2 )
		(preference LightSleeper-1
			(always
				(not 
					(and 
						(humanActivity ?H ?L sleeping)
						(state ?T vaccuum ?L processing)
						(hasProperty ?H normalSleeper)
					)
				)	
			)
		)
		(preference LightSleeper-2
			(always
				(not 
					(and 
						(humanActivity ?H ?L sleeping)
						(state ?T cleanRoom ?L processing)
						(hasProperty ?H normalSleeper)
					)
				)	
			)
		)
		(preference LightSleeper-3
			(always
				(not 
					(and 
						(humanActivity ?H ?L sleeping)
						(state ?T sortStuff ?L processing)
						(hasProperty ?H normalSleeper)
					)
				)	
			)
		)
		;; IC ( LetMeRead ?H ?L ?T ?I1 ?I2 )
		(preference LetMeRead-1
			(always
				(not 
					(and 
						(humanActivity ?H ?L reading)
						(state ?T vaccuum ?L processing)
					)
				)	
			)
		)
	)
	(:metric minimize (+ 
		(* 100000000 (is-violated LightSleeper-1))
		(* 100000000 (is-violated LightSleeper-2))
		(* 100000000 (is-violated LightSleeper-3))
		(* 10 (is-violated LetMeRead-1))
	))
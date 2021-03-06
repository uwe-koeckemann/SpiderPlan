ConstraintDatabase resultCDB = resultCore.getContext();
ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 20);
assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 40);
assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 60);
assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 40);
assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 60);
assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 80);
assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 100);
assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 0);
assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 20);
assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 40);
assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 60);

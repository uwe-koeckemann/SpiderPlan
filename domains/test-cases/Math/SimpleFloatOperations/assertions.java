ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);
assertTrue(values.getFloat(Term.createConstant("d")) == 2.5);
assertTrue(values.getFloat(Term.createConstant("x")) == 2.0);
assertTrue(values.getFloat(Term.createConstant("y")) == 0.25);
assertTrue(values.getFloat(Term.createConstant("z")) == 0.75);

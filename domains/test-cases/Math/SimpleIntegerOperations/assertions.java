ValueLookup values = resultCore.getContext().getUnique(ValueLookup.class);
assertTrue(values.getInt(Term.createConstant("d")) == 21);
assertTrue(values.getInt(Term.createConstant("x")) == 42);
assertTrue(values.getInt(Term.createConstant("y")) == 420);
assertTrue(values.getInt(Term.createConstant("z")) == 42);
assertTrue(values.getInt(Term.createConstant("m")) == 2);


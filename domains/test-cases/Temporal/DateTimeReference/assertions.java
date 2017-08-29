/*******************************************************************************
 * Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
ConstraintDatabase resultCDB = resultCore.getContext();
ValueLookup valueLookup = resultCDB.getUnique(ValueLookup.class);
assertTrue(valueLookup.getEST(Term.createConstant("I1")) == 0);
assertTrue(valueLookup.getLST(Term.createConstant("I1")) == 755994);
assertTrue(valueLookup.getEET(Term.createConstant("I1")) == 21600);
assertTrue(valueLookup.getLET(Term.createConstant("I1")) == 777594);
assertTrue(valueLookup.getEST(Term.createConstant("I2")) == 21606);
assertTrue(valueLookup.getLST(Term.createConstant("I2")) == 777600);
assertTrue(valueLookup.getEET(Term.createConstant("I2")) == 21606);
assertTrue(valueLookup.getLET(Term.createConstant("I2")) == 777600);
assertTrue(valueLookup.getEST(Term.createConstant("I3")) == 360);
assertTrue(valueLookup.getLST(Term.createConstant("I3")) == 720);
assertTrue(valueLookup.getEET(Term.createConstant("I3")) == 360);
assertTrue(valueLookup.getLET(Term.createConstant("I3")) == 864000);
assertTrue(valueLookup.getEST(Term.createConstant("I4")) == 108006);
assertTrue(valueLookup.getLST(Term.createConstant("I4")) == 864000);
assertTrue(valueLookup.getEET(Term.createConstant("I4")) == 108006);
assertTrue(valueLookup.getLET(Term.createConstant("I4")) == 864000);

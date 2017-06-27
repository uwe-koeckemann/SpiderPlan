#!/bin/python
# -*- coding: utf-8 -*-

#Copyright (c) 2015-2017 Uwe Köckemann <uwe.kockemann@oru.se>

#Permission is hereby granted, free of charge, to any person obtaining a copy
#of this software and associated documentation files (the "Software"), to deal
#in the Software without restriction, including without limitation the rights
#to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
#copies of the Software, and to permit persons to whom the Software is
#furnished to do so, subject to the following conditions:

#The above copyright notice and this permission notice shall be included in all
#copies or substantial portions of the Software.

#THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
#IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
#FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
#AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
#LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
#OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
#SOFTWARE.

import sys

import matplotlib.pyplot as plt
import matplotlib
from matplotlib.dates import DateFormatter, MinuteLocator, SecondLocator
import numpy as np
from StringIO import StringIO
import datetime as dt

earliestTimelines = True

inputFilename = sys.argv[1]
f = open(inputFilename)
lines = []
s = ""
for l in f.readlines():
	l = l.replace("\n","")
	if not l in lines:
		lines.append(l)
		s += l + "\n"
a = StringIO(s)		
f.close() 

print s

data = np.genfromtxt(a, names=['variable', 'EST', 'LST', 'EET', 'LET', 'value'], dtype=None, delimiter='|')

data.sort()

variable, value, EST, LST, EET, LET = data['variable'], data['value'], data['EST'], data['LST'], data['EET'], data['LET']


print data

r = range(len(variable))
r.reverse()

y = (np.array(r) + 1) / float(len(variable) + 1)

#Plot function
def timelines(y, xstart, xstop, color='b', linestyles=u'solid', vLenght=0.03):
    plt.hlines(y, xstart, xstop, color, lw=4, linestyles=linestyles)
    plt.vlines(xstart, y+vLenght, y-vLenght, color, lw=2, linestyles=linestyles)
    plt.vlines(xstop, y+vLenght, y-vLenght, color, lw=2, linestyles=linestyles)

def labels(y, xstart,xstop, value,color='b'):
	for i in range(len(y)):
		plt.text( xstart[i]+(float(xstop[i]-xstart[i])/2.0), y[i], value[i], size=13, color=color, ha="center", va="center",bbox = dict(facecolor='white', edgecolor='black', alpha=0.7, boxstyle='round'))
		
 
#for i in range(len(value)):
	#value[i] = variable[i] + " := " + value[i]
 
if earliestTimelines:
	timelines(y, EST, EET, 'k')
	labels(y,EST,EET,value, 'k')	
else:
	timelines(y, LST, EET, 'k', vLenght=0.03)
	timelines(y, EST, LET, 'k', vLenght=0.05)
	#labels(y,EST,LET,value, 'k')



#ax = plt.gca()

if earliestTimelines:
	delta = (EST.max() - EET.min())/10
else:
	delta = (LET.max() - EST.min())/10


font = {'family' : 'normal',
        'weight' : 'bold',
        'size'   : 13}

matplotlib.rc('font', **font)

varNamesInPlot = [variable[0]]
for i in range(1,len(variable)):
	if variable[i] != variable[i-1]:
		varNamesInPlot.append(variable[i])
	else:
		varNamesInPlot.append("")
		
print varNamesInPlot


plt.yticks(y, varNamesInPlot)
plt.ylim(0,1)

if earliestTimelines:
	plt.xlim(EST.min()-delta, EET.max()+delta)
else:
	plt.xlim(EST.min()-delta, LET.max()+delta)
	
plt.grid()
plt.xlabel('Time')
plt.show()






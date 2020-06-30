# -*- coding: utf-8 -*-
"""
Created on Mon Jun 29 16:28:09 2020

@author: N2-LiveCell
"""

import matplotlib.pyplot as plt
from scipy import optimize
import numpy as np
import pandas as pd


plt.plot(data[:,0], data[:,1])
plt.xlabel("microns")
plt.ylabel("PFS offset")

# a = stats.linregress(data[:,0], data[:,1])

# def fun(x):
#     return x*a.slope + a.intercept
    
x = np.linspace(data[:,0].min(), data[:,0].max())
# plt.plot(x, fun(x))

def q(x, c0, c1, c2):
    return c0 + x * c1 + c2 * x**2

p0 = optimize.curve_fit(q, data[:,0], data[:,1])[0]
print(p0)

def Q(microns):
    """converts microns (micron 0 at offset 0) to an offset"""
    return q(microns, *p0)

def iQ(offset):
    """inverse of Q to convert offset to relative microns."""
    c, b, a = tuple(p0)
    return (-b + np.sqrt(b**2 - 4*a*(c-y))) / (2*a)

plt.plot(x, q(x, *p0))

def getOffsetForMicron(currentOffset, relMicron):
    """from our current offset and a desired relative move get the new offset."""
    currentRelMicron = iQ(currentOffset)
    m = currentRelMicron + relMicron
    print(m)
    newOffset = Q(m)
    print(newOffset)
    return newOffset
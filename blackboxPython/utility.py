#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jun 17 09:10:40 2020

@author: giuliano
"""

import numpy as np
import random

def initVector(dim): 
    vector = []
    for i in range(dim):
        vector.append(round(random.uniform(-100,101),2))
    return vector

def selectNeighbour(vector, stride):
    for i in range(len(vector)):
        vector[i] += round(random.uniform(-2*stride,2*stride), 2)
    return vector
    

def avgValues(matrix):
    length = max(map(len, matrix))
    avg = np.array([x + [min(x)]*(length-len(x)) for x in matrix])
    mat = list(avg)
    avgValues = avg.mean(0)
    return avgValues
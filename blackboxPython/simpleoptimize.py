#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Tue Jun 16 15:06:52 2020

@author: giuliano
"""

import random
import sys
import numpy as np


def createInput(i, dim):
    inputVector = []
    for x in range(dim):
        inputVector.append(str(i))
    inputVector = ' '.join(inputVector)
    return inputVector + '\n'

def blackboxInput(bb, bb_id, dim):
    results = []
    for x in range(1,10):
        vector = createInput(x,dim)
        bb.stdin.write(vector)
        bb.stdin.flush()
        output = bb.stdout.readline()
        results.append((bb_id,x,output))
    return results

def startExperiment(bb, bb_dim):
    experiments = []
    for num in range(10):
        optResult = optimize(bb, bb_dim)
        experiments.append(optResult)
    avg = avgValues(experiments)
    return avg

def optimize(bb, bb_dim):
    results = []
    minValue = sys.float_info.max
    for x in range(300):
        rd = random.randrange(-100,101)
        vector = createInput(rd,bb_dim)
        bb.stdin.write(vector)
        bb.stdin.flush()
        output = float(bb.stdout.readline())
        if output < minValue:
            minValue = output
        results.append(minValue)
    return results

def avgValues(matrix):
    avg = np.array(matrix)
    avg = avg.mean(0)
    return avg
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jun 17 16:12:47 2020

@author: giuliano
"""

import utility
import math
import random

class SimulatedAnnealing:
    
    def __init__(self, stride, i_max, eta, alpha, cooling):
        self.stride = stride
        self.i_max = i_max
        self.eta = eta
        self.alpha = alpha
        self.cooling = cooling
        self.vector = []
        self.result = []
        self.matrix = []
        self.avg = []
        
    def initSA(self, bb, dim):
        for i in range(10):
            self.startSA(bb, dim)
            self.matrix.append(self.result)
        self.avg = utility.avgValues(self.matrix)
        
    def startSA(self, bb, dim):
        self.result = []
        self.vector = utility.initVector(dim)
        bb.stdin.write(' '.join([str(x) for x in self.vector]) + '\n')
        bb_out = bb.stdout.readline()
        self.result.append(float(bb_out))
        for i in range(self.i_max):
            best = self.result[-1]
            if self.cooling == "linear":
                t = self.linear(i)
            else:
                t = self.exponentially(i)
            bb.stdin.write(' '.join([str(x) for x in self.vector]) + '\n')
            bb_vector = float(bb.stdout.readline())
            neighbour = utility.selectNeighbour(self.vector.copy(), self.stride)
            bb.stdin.write(' '.join([str(x) for x in neighbour]) + '\n')
            bb_neighbour = float(bb.stdout.readline())
            if bb_neighbour <= bb_vector:
                self.vector = neighbour
                bb_vector = bb_neighbour
            else:
                prob = self.acceptProb(bb_neighbour-bb_vector, t)
                if prob < random.random():
                    self.vector = neighbour
                    bb_vector = bb_neighbour
            if bb_vector < self.result[-1]:
                best = bb_vector
            self.result.append(best)
                
    def linear(self, i):
        return self.i_max - self.eta * i
    
    def exponentially(self, i):
        return self.i_max * self.alpha ** i
    
    def acceptProb(self, delta, t):
        return math.exp(-(delta/t))
        
#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jun 17 09:19:22 2020

@author: giuliano
"""

import utility
import sys

class SteepestHillClimber:
    
    def __init__(self, stride, i_max):
        self.stride = stride
        self.i_max = i_max
        self.vector = []
        self.result = []
        self.matrix = []
        self.avg = []
    
    def initHC(self, bb, dim):
        """ 
        execute ten experiments and set average values

        Parameters
        ----------
        bb : running blackbox process
        dim : int
            size of input vector

        Returns
        -------
        None.

        """ 
        for i in range(10):
            self.steepestHC(bb, dim)
            self.matrix.append(self.result)
        self.avg = utility.avgValues(self.matrix)
            
    
    def steepestHC(self, bb, dim):
        """
        run until no k found, atmost i_max times

        Parameters
        ----------
        bb : running blackbox process
        dim : int
            size of input vector

        Returns
        -------
        None.

        """
        self.vector = utility.initVector(dim)
        bb.stdin.write(' '.join([str(x) for x in self.vector]) + '\n')
        self.result = []
        bb_out = bb.stdout.readline()
        self.result.append(float(bb_out))
        for i in range(self.i_max): 
            out = self.findOpt(bb, dim)
            # if no k found then exit
            if out[0] == -1:
                break 
            else:
                self.vector[out[0]] = out[2]
                self.result.append(out[1])
                
    def findOpt(self, bb, dim):
        """
        finds the best k (index of vector)

        Parameters
        ----------
        bb : running blackbox process
        dim : int
            size of input vector

        Returns
        -------
        out : tuple
            if no k found: return out[0] == -1
            else: return out[0] != -1

        """
        out = (-1, self.result[-1])
        for k in range(dim):
            cp = self.vector.copy()
            bb_out = self.optstride(bb, cp, k, self.stride)
            # check for positive stride
            if bb_out <= self.result[-1] and bb_out <= out[1]:
                cp[k] = min(self.vector[k]+self.stride, 100)
                out = (k, bb_out, cp[k])
            cp = self.vector.copy()
            bb_out = self.optstride(bb, cp, k, -self.stride)
            # check for negative stride
            if bb_out <= self.result[-1] and bb_out <= out[1]:
                cp[k] = max(self.vector[k]-self.stride, -100)
                out = (k, bb_out, cp[k])
        return out
            
    def optstride(self, bb, vector, k, stride):
        """
        retrieve blackbox output for shifted vector

        Parameters
        ----------
        bb : process
        vector : list
            int values
        k : int
            current index
        stride : stride
            +/- stride

        Returns
        -------
        result : float
            blackbox output

        """
        vector[k] += stride
        # dont exceed boundery
        vector[k] = min(vector[k], 100)
        vector[k] = max(vector[k], -100)
        bb.stdin.write(' '.join([str(x) for x in vector]) + '\n')
        result = float(bb.stdout.readline())
        return result
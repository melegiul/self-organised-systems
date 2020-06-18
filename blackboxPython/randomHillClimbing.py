#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jun 17 09:31:40 2020

@author: giuliano
"""

import utility
import steepHillClimbing as steepest
import sys

class RandomHillClimber:
    
    def __init__(self, stride, i_max, j_max):
        self.i_max = i_max
        self.j_max = j_max
        self.stride = stride
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
            out = self.randomHC(bb, dim)
            self.matrix.append(out)
        self.avg = utility.avgValues(self.matrix)
        
    def randomHC(self, bb, dim):
        """ retrieve results from steepest hill climber

        Parameters
        ----------
        bb : running blackbox process
        dim : int
            size of input vector

        Returns
        -------
        result : list
            measured blackbox output

        """
        result = []
        minValue = sys.float_info.max
        for i in range(int(self.i_max/self.j_max)):
            steepInstance = steepest.SteepestHillClimber(self.stride, self.i_max)
            steepInstance.steepestHC(bb, dim)
            if steepInstance.result[-1] <= minValue:
                minValue = steepInstance.result[-1]
            result.append(minValue)
        return result
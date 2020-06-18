#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Jun 13 13:57:42 2020

@author: giuliano
"""

import csv
import subprocess as sp
from subprocess import PIPE
import simpleoptimize
import simpleHillClimbing as simple
import steepHillClimbing as steep
import randomHillClimbing as randomHC
import simulatedAnnealing as sa 

def initInput(dimensions):
    boxResults = []
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        result = simpleoptimize.blackboxInput(process, bb_id, bb_dim)
        boxResults.append(result)
        process.terminate()
    return boxResults

def initOptimize(dimensions):
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        array = simpleoptimize.startExperiment(process, bb_id, bb_dim)
        writeCSV(array, bb_id, "simOpt")
        process.terminate()

def initSimpleHC(dimensions, stride, i_max):
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        climber = simple.SimpleHillClimber(stride, i_max)
        climber.initHC(process, bb_dim)
        writeCSV(climber.avg, bb_id, "simHC", stride)
        process.terminate()
        
def initSteepestHC(dimensions, stride, i_max):
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        climber = steep.SteepestHillClimber(stride, i_max)
        climber.initHC(process, bb_dim)
        writeCSV(climber.avg, bb_id, "steepHC", stride)
        process.terminate()
        
def initRandomtHC(dimensions, stride, i_max, j_max):
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        climber = randomHC.RandomHillClimber(stride, i_max, j_max)
        climber.initHC(process, bb_dim)
        writeCSV(climber.avg, bb_id, "randomHC", stride, j_max_val=j_max)
        process.terminate()
        
def initAnnealing(dimensions, stride, i_max, eta, alpha, cooling):
    for bb_id, bb_dim in dimensions.items():
        process = runBlackBox(bb_id)
        saInstance = sa.SimulatedAnnealing(stride, i_max, eta, alpha, cooling)
        saInstance.initSA(process, bb_dim)
        writeCSV(saInstance.avg, bb_id, "annealing", stride, cooling=cooling, eta=eta, alpha=alpha)
        process.terminate()
                
def main():
    dimensions = {0:3, 1:5, 2:2, 3:10, 4:2}
    stride = 10
    i_max = 500
    j_max = 50
    eta = 0.8
    alpha = 0.8
    cooling = "exponentially"
    #initSimpleHC(dimensions, stride, i_max)
    #initSteepestHC(dimensions, stride, i_max)
    #initRandomtHC(dimensions, stride, i_max, j_max)
    initAnnealing(dimensions, stride, i_max, eta, alpha, cooling)
    #result = initInput(dimensions)
    #initOptimize(dimensions)
    
def runBlackBox(bb_id):
    string = 'docker run -i bb -b {bb}'.format(bb=bb_id)
    bb = sp.Popen(string,
                  bufsize=0,
                  shell=True,
                  stdin=PIPE, 
                  stdout=PIPE,
                  text=True)
    return bb
    
def writeCSV(array, bb_id, opt_type, strideVal, j_max_val=0, cooling=None, eta=None, alpha=None):
    #for index in range(matrix.shape[0]):
    if j_max_val == 0 and cooling == None:
        fileName = './{folder}/bb{bbid}-{stride}.csv'.format(bbid=bb_id, folder=opt_type, stride=strideVal)
    elif cooling == None:
        fileName = './{folder}/bb{bbid}-{stride}-{j_max}.csv'.format(bbid=bb_id, folder=opt_type, stride=strideVal, j_max=j_max_val)
    else:
        fileName = './{folder}/bb{bbid}-{eta}-{alpha}.csv'.format(bbid=bb_id, folder=cooling, eta=eta, alpha=alpha)
    #row = matrix[index,:]
    data = [(k,array[k]) for k in range(array.shape[0])]
    #np.savetxt(filename, row, delimiter=',')
    with open(fileName, "w") as file:
        writer = csv.writer(file, delimiter=',')
        writer.writerow(('iteration','value'))
        for entry in data:
            writer.writerow(entry)
    
if __name__ == "__main__":
    main()
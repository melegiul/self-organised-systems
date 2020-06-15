#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Sat Jun 13 13:57:42 2020

@author: giuliano
"""

import subprocess as sp
from subprocess import PIPE
import random
import sys
import numpy as np
import csv

#output = sp.call("ls -l", shell=True)
dimensions = {0:3, 1:5, 2:2, 3:10, 4:2}

def createInput(i, dim):
    inputVector = []
    for x in range(dim):
        inputVector.append(str(i))
    inputVector = ' '.join(inputVector)
    return inputVector + '\n'

#print(dimensions)
#args = ['docker', 'run', '-i', 'bb', '-b', '0']
def executeBB():
    boxResults = []
    for key,value in dimensions.items():
        results = []
        string = 'docker run -i bb -b {bb}'.format(bb=key)
        bb = sp.Popen(string, shell=True,
                      stdin=PIPE, 
                      stdout=PIPE,
                      text=True)
        for x in range(1,10):
            vector = createInput(x,value)
            bb.stdin.write(vector)
            bb.stdin.flush()
            output = bb.stdout.readline()
            results.append((key,x,output))
        boxResults.append(results)
        bb.terminate()
    return boxResults
        #print(key, x, output)
#print(list(x for x in enumerate([1,2,3,4])))
    
def optimize(key,value):
    results = []
    minValue = sys.float_info.max
    string = 'docker run -i bb -b {bb}'.format(bb=key)
    bb = sp.Popen(string, shell=True,
                  stdin=PIPE, 
                  stdout=PIPE, 
                  text=True)
    for x in range(300):
        rd = random.randrange(-100,101)
        vector = createInput(rd,value)
        bb.stdin.write(vector)
        bb.stdin.flush()
        output = float(bb.stdout.readline())
        if output < minValue:
            minValue = output
        results.append(minValue)
    bb.terminate()
    return results

def avgValues(matrix):
    avg = np.array(matrix)
    avg = avg.mean(0)
    return avg

def writeCSV(matrix):
    print(matrix.shape)
    for index in range(matrix.shape[0]):
        fileName = './bb{id}.csv'.format(id=index)
        row = matrix[index,:]
        data = [(k,row[k]) for k in range(matrix.shape[1])]
        #np.savetxt(filename, row, delimiter=',')
        with open(fileName, "w") as file:
            writer = csv.writer(file, delimiter=',')
            writer.writerow(('iteration','value'))
            for entry in data:
                writer.writerow(entry)
                
def startExperiment():
    finalResult = []
    for key,value in dimensions.items():
        experiments = []
        for num in range(10):
            optResult = optimize(key,value)
            experiments.append(optResult)
        avg = avgValues(experiments)
        finalResult.append(avg)
    writeCSV(np.array(finalResult))       
   
                
def main():
    #result = executeBB()
    #print(result)
    startExperiment()
    
if __name__ == "__main__":
    main()
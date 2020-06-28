#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Thu Jun 25 12:20:59 2020

@author: giuliano
"""

import utility
import random
import math
import numpy as np

class GeneticAlgorithm():
    
    def __init__(self, population_size, i_max, tournament_size, parent_num, precision, encoder, stride=0):
        self.population_size = population_size
        self.tournament_size = tournament_size
        self.parent_num = parent_num
        self.i_max = i_max
        self.encoder = encoder
        self.stride = stride
        self.individuals = (100-(-100))*10**precision
        self.precision = precision
        self.children_num = 0
        self.population = []
        self.result = []
        self.matrix = []
        self.avg = []
        
    def initGA(self, bb, dim):
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
            self.startGA(bb, dim)
            self.matrix.append(self.result)
        self.avg = utility.avgValues(self.matrix)
        
    def startGA(self, bb, dim):
        self.result = []
        self.population = []
        for i in range(self.population_size):
            vector = utility.initVector(dim)
            self.population.append((vector,self.exe_bb(bb,vector)))
        for i in range(self.i_max):
            parents = self.selectParent(bb)
            if self.encoder == "ga_simple":
                encoded = self.simple_encode(parents, dim)
                children = self.crossover(encoded, dim)
                children_vector = self.simple_decode(bb,children,dim)
            elif self.encoder == "ga_gray":
                encoded = self.gray_encode(parents, dim)
                children = self.crossover(encoded, dim)
                children_vector = self.gray_decode(bb,children,dim)
            elif self.encoder == "ga_none":
                children_vector = self.direct_crossover(bb, parents, dim)
            self.population.extend(children_vector)
            self.selection(bb)
            
    def selectParent(self, bb):
        parents = []
        for i in range(self.parent_num):
            tournament = []
            for j in range(self.tournament_size):
                k = random.randrange(len(self.population))
                out = self.population[k][1]
                tournament.append((k,out))
            winner = self.tourn_winner(tournament)
            if self.population[winner] not in parents:
                parents.append(self.population[winner])
        return parents
        
    def exe_bb(self, bb, vector):
        bb.stdin.write(' '.join([str(x) for x in vector]) + '\n')
        result = float(bb.stdout.readline())
        return result
    
    def tourn_winner(self, member):
        min_value = (None, float('inf'))
        for x in member:
            if x[1] < min_value[1]:
                min_value = x
        return min_value[0]
    
    def crossover(self, parents, dim):
        index = 0
        bits = math.ceil(math.log(
            self.individuals,2))
        children = []
        while index < len(parents)-1:
            k = random.randrange(bits)
            parent_vector_1 = parents[index]
            parent_vector_2 = parents[index+1]
            child_vector_1 = []
            child_vector_2 = []
            for i in range(len(parent_vector_1)):
                entry1 = parent_vector_1[i][:k]
                entry1.extend(parent_vector_2[i][k:])
                child_vector_1.append(entry1)
                entry2 = parent_vector_2[i][:k]
                entry2.extend(parent_vector_1[i][k:])
                child_vector_2.append(entry2)
            children.append(child_vector_1)
            children.append(child_vector_2)
            index += 2
        self.children_num = len(children)
        return children
    
    def direct_crossover(self, bb, parents, dim):
        index = 0
        children = []
        while index < len(parents)-1:
            if dim-1 == 1:
                k = 1
            else:
                k = random.randrange(1,dim-1)
            parent_vector_1 = parents[index][0]
            parent_vector_2 = parents[index+1][0]
            child_vector_1 = parent_vector_1[:k]
            child_vector_1.extend(parent_vector_2[k:])
            bb_out = self.exe_bb(bb, child_vector_1)
            children.append((child_vector_1,bb_out))
            child_vector_2 = parent_vector_2[:k]
            child_vector_2.extend(parent_vector_1[k:])
            bb_out = self.exe_bb(bb, child_vector_2)
            children.append((child_vector_2,bb_out))
            index += 2
        self.children_num = len(children)
        return children
            
    def selection(self, bb):
        # for index in range(len(self.population)):
        #     vector = self.population[index]
        #     bb_out = self.exe_bb(bb, vector)
        #     self.population[index] = (vector, bb_out)
        self.population.sort(key = lambda x: x[1])
        array = [self.population[x][1] for x in range(len(self.population))]
        array1 = [self.population[x][0] for x in range(len(self.population))]
        if self.children_num != 0:
            self.population = self.population[:-self.children_num]
        self.result.append(self.population[0][1])
        
    def simple_encode(self, tuple_list, dim):
        bits = math.ceil(math.log(
            self.individuals,2))
        string = '0:0{bit}b'.format(bit=bits)
        string = '{' + string + '}'
        encoded = []
        for tuple_element in tuple_list:
            vector = tuple_element[0]
            enc_vector = []
            for entry in vector:
                binary = int((entry + 100) * (10**self.precision))
                binary = list(string.format(binary))
                enc_vector.append(binary)
            encoded.append(enc_vector)
        return encoded
    
    def simple_decode(self, bb, vector_list, dim):
        bits = math.ceil(math.log(
            self.individuals,2))
        dec_vector_list = []
        for vector in vector_list:
            dec_vector = []
            for entry in vector:
                decoded = 0
                for i in range(len(entry)):
                    decoded += int(entry[i])*2**(bits-i-1)
                decoded /= 10**self.precision
                decoded -= 100
                dec_vector.append(round(decoded,2))
            bb_out = self.exe_bb(bb,dec_vector)
            dec_vector_list.append((dec_vector,bb_out))
        return dec_vector_list
    
    def gray_encode(self, tuple_list, dim):
        bits = math.ceil(math.log(
            self.individuals,2))
        string = '0:0{bit}b'.format(bit=bits)
        string = '{' + string + '}'
        encoded = []
        for tuple_element in tuple_list:
            vector = tuple_element[0]
            enc_vector = []
            for entry in vector:
                binary = int((entry + 100) 
                             * (10**self.precision))
                binary = self.bin_to_gray(binary)
                binary = list(string.format(binary))
                enc_vector.append(binary)
            encoded.append(enc_vector)
        return encoded
    
    def bin_to_gray(self, n):
        return n ^ (n >> 1)
    
    def gray_decode(self, bb, vector_list, dim):
        bits = math.ceil(math.log(
            self.individuals,2))
        dec_vector_list = []
        for vector in vector_list:
            dec_vector = []
            for entry in vector:
                decoded = 0
                for i in range(len(entry)):
                    decoded += int(entry[i])*2**(bits-i-1)
                decoded = self.gray_to_bin(decoded)
                decoded /= 10**self.precision
                decoded -= 100
                dec_vector.append(round(decoded,2))
            bb_out = self.exe_bb(bb,dec_vector)
            dec_vector_list.append((dec_vector,bb_out))
        return dec_vector_list
    
    def gray_to_bin(self, n):
        """Convert Gray codeword to binary and return it."""
        #n = int(n, 2) # convert to int
     
        mask = n
        while mask != 0:
            mask >>= 1
            n ^= mask
     
        # bin(n) returns n's binary representation with a '0b' prefixed
        # the slice operation is to remove the prefix
        #return bin(n)[2:]
        return n
                    
    
# =============================================================================
# import subprocess as sp
# from subprocess import PIPE
# #import numpy as np
# 
# def runBlackBox(bb_id):
#     string = 'docker run -i bb -b {bb}'.format(bb=bb_id)
#     bb = sp.Popen(string,
#                   bufsize=0,
#                   shell=True,
#                   stdin=PIPE, 
#                   stdout=PIPE,
#                   text=True)
#     return bb
# 
# def Sort_Tuple(tup):  
#     # reverse = None (Sorts in Ascending order)  
#     # key is set to sort using second element of  
#     # sublist lambda has been used  
#     tup.sort(key = lambda x: x[1])  
#     return tup  
#     
# if __name__ == "__main__":
#     #bb = runBlackBox(0)
#     #vector = [0.1, 0.1, 0.1]
#     # vector = [1, 1, 1]
#     # string = '0:0{bits}b'.format(bits=8)
#     # string = '{'+string+'}'
#     # print(list(string.format(2)))
#     # Python program to sort a list of 
#     # Driver Code  
#     tup = [('rishav', 10), ('akash', 5), ('ram', 20), ('gaurav', 15)]   
#     # printing the sorted list of tuples 
#     print(Sort_Tuple(tup))
#     # bb.stdin.write(' '.join([str(x) for x in vector]) + '\n')
#     # result = float(bb.stdout.readline())
#     # print(result)
#     # bb.terminate()
# =============================================================================
    
#!/usr/bin/env python3

import numpy as np
import random
import math
import datetime

# generiert eine Abstandsmatrix mittels eines Seeds
def generator_tsp(count_cities, seed) :
    random.seed(seed)
    
    count_streets = (count_cities * (count_cities - 1))/2
    #distance_list = []
    
    #for x in range(0, count_streets):
    #    distance_list.append(random.randint(1, 1000))
    
    distance_matrix = np.zeros((count_cities, count_cities))
    
    for i in range(0, count_cities) :
        for j in range(i+1, count_cities) :
            temp_distance = random.randint(1, 1000)
            distance_matrix[i][j] = temp_distance
            distance_matrix[j][i] = temp_distance
    
    return distance_matrix
    
# löst das TSP-Problem
def solve_tsp(distance_matrix) :
    print(distance_matrix)
    
    n = len(distance_matrix) # Anzahl der Städte
    
    # Route startet immer bei Stadt 1, da Anfang egal ist
    # somit permutiere nur Städte 2 bis n
    permutation = np.arange(1, n) # Startpermutation; Länge: n-1
    
    # iterative Implementierung des Heap's Algorithmus für Permutationen
    stack_encoding = [0] * (n-1)
    
    current_shortest = 0
    optimal_route = []
    
    for i in range(0, n) :
        current_shortest = current_shortest + distance_matrix[i][(i+1) % n]
    
    test_counter = 0
    max_permut = math.factorial(n-1)
    
    print(datetime.datetime.now())
    
    i = 0
    while i < (n-1) :
        if (stack_encoding[i] < i) :
            if i % 2 == 0 :
                permutation[0], permutation[i] = permutation[i], permutation[0]
            else :
                permutation[stack_encoding[i]], permutation[i] = permutation[i], permutation[stack_encoding[i]]
                
            # neue Permutation -> Berechnen der Route und Vergleich mit aktueller optimalen Route
            new_route = distance_matrix[0][permutation[0]]
            for k in range(0, (n-1) - 1) :
                new_route += distance_matrix[permutation[k]][permutation[k+1]]
            new_route += distance_matrix[permutation[-1]][0]
            if(new_route < current_shortest) :
                current_shortest = new_route
                optimal_route = [1]
                optimal_route.extend([x+1 for x in permutation]) # Umwandeln der Indizes vor dem Hinzufügen
            
            # dient die Dauer und den Fortschritt herauszufinden
            test_counter += 1
            if(test_counter % 1000000 == 0) :
                print(str(test_counter * 100 / max_permut) + "%")
                print(datetime.datetime.now())
            
            stack_encoding[i] += 1
            i = 0
            
        else :
            stack_encoding[i] = 0
            i += 1
            
    print(datetime.datetime.now())
    print(optimal_route)
    print(current_shortest)
            
    return current_shortest

# Hilfsfunktion zum Testen, ob ein String einen Integer repräsentiert
def representsInt(s):
    try: 
        int(s)
        return True
    except ValueError:
        return False


print("Bitte geben Sie die Anzahl der Städte ein:")
boolean = False
city_count = 0
while(not boolean) :
    city_count = input()
    boolean = representsInt(city_count)

print("Bitte geben Sie einen Seed:")
boolean = False
seed = 0
while(not boolean) :
    seed = input()
    boolean = representsInt(seed)
    
solve_tsp(generator_tsp(int(city_count), int(seed)))


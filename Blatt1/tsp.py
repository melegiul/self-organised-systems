# Describe Problem with Matrix:
#   city 1   2   3   4   5
#     1  0   d   d   d   d
#     2  d   0   d   d   d
#     3  d   d   0   d   d
#     4  d   d   d   0   d
#     5  d   d   d   d   0

import numpy as np
from itertools import permutations
import time


def generate_tsp(v=3, seed=1425914):
    problem_matrix = np.zeros((v, v), dtype=np.int32)
    np.random.seed(seed)

    for x in range(v):
        for y in range(v):
            if x != y:
                problem_matrix[x][y] = np.random.randint(1, 1000, 1)
    return problem_matrix


def solve_tsp_brute_force(problem_matrix):
    routes = []
    v = len(problem_matrix)

    for path in permutations(range(v)):
        dist = calc_dist(problem_matrix, path)
        routes.append([dist, path])

    routes.sort()
    # print(routes)
    return routes[0]


def calc_dist(problem_matrix, path):
    dist = 0

    for i in range(len(path)):
        d = problem_matrix[path[i - 1]][path[i]]
        dist = dist + d
    return dist


def main(n=15):
    times = []
    for i in range(1, 11):
        start_time = time.time()

        problem_matrix = generate_tsp(n, i)
        print('Matrix: \n', problem_matrix)
        best_route = solve_tsp_brute_force(problem_matrix)
        print('best route: ', best_route)
        print('\n ------------------------------------------------ \n')

        end_time = time.time()
        times.append(end_time - start_time)

    print(' ------------------------------------------------ \n')
    print('mean time for', n, 'cities: ', np.mean(times), ' s')
    print('\n ------------------------------------------------ \n')


if __name__ == "__main__":
    main()

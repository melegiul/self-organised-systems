#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Created on Wed Jul  1 19:25:52 2020

@author: giuliano
"""

import gym
import random
import numpy as np
import matplotlib.pyplot as plt

class GreedyLearning:
    
    def __init__(self, alpha, gamma, epsilon, scenario, episode_num):
        self.alpha = alpha
        self.gamma = gamma
        self.epsilon = epsilon
        self.scenario = scenario
        self.episode_num = episode_num
        self.avg_values = []
        self.reward_avg = []
        self.reward_avg_2 = []
        
    def initGreedy(self):
        matrix = []
        for _ in range(10):
            result = self.greedyExe()
            matrix.append(result)
        self.avg_values = self.avgValues(matrix)
        self.reward_avg = self.reward_average()
        self.reward_avg_2 = self.reward_average_2()
        
    def greedyExe(self):
        result = []
        env = gym.make(self.scenario)
        q = self.q_init(env)
        for i_episode in range(self.episode_num):
            old_state = env.reset()
            if self.scenario == "CartPole-v1":
                old_state = self.discrete_state(old_state)
            reward_sum = 0
            for t in range(100):
                #env.render()
                print(old_state)
                action = self.choose_action(env, old_state, q)
                new_state, reward, done, info = env.step(action)
                if self.scenario == 'CartPole-v1':
                    reward_sum += reward
                    new_state = self.discrete_state(new_state)
                self.update_q(q,old_state,new_state,action,reward)
                old_state = new_state
                if done:
                    print("Episode finished after {} timesteps".format(t+1))
                    break
            if i_episode == 0:
                pre_gain = 0
            else:
                pre_gain = result[-1]
            if self.scenario == 'FrozenLake-v0':
                cur_reward = (self.gamma ** i_episode) * reward
                result.append(cur_reward+pre_gain)
            else:
                result.append(reward_sum)
        env.close()
        return result
        
    def q_init(self, env):
        if self.scenario == 'FrozenLake-v0':
            action_num = env.action_space.n
            state_num = env.observation_space.n
            return np.zeros((state_num,action_num))
        else:
            return self.cart_pole_init(env)
            
    def cart_pole_init(self, env):
        action_num = env.action_space.n
        state_num = 2**12
        return np.zeros((state_num,action_num))
    
    def discrete_state(self, state):
        i = 0
        max_border = 7
        border = [-0.6, -0.3, -0.06, 0, 0.06, 0.3, 0.6, 1.0]
        while state[0] > border[i]:
            i += 1
            if i == max_border:
                break
        j = 0
        while state[1] > border[j]:
            j += 1
            if j == max_border:
                break
        k = 0
        while state[2] > border[k]:
            k += 1
            if k == max_border:
                break
        l = 0
        while state[3] > border[l]:
            l += 1
            if l == max_border:
                break
        state_string = ''
        for x in [i,j,k,l]:
            state_string += "{0:03b}".format(x)
        return int(state_string, 2)
        
        
    def choose_action(self, env, state, q):
        rd = random.random()
        if rd < self.epsilon:
            action = env.action_space.sample()
            return action
        else:
            row = q[state,:]
            action = np.argmax(row)
            return action      
        
    def update_q(self, q, old_state, new_state, action, reward):
        row = q[new_state,:]
        max_a = np.max(row)
        x = reward + self.gamma * max_a - q[old_state][action]
        q[old_state][action] += self.alpha * x
        
    def avgValues(self, matrix):
        length = max(map(len, matrix))
        avg = np.array([x + [min(x)]*(length-len(x)) for x in matrix])
        avgValues = avg.mean(0)
        return avgValues
    
    def reward_average(self):
        r_avg = []
        for i in range(self.episode_num):
            reward_sum = 0
            for j in range(i):
                reward_sum += self.avg_values[j]
            r_avg.append(reward_sum/(i+1))
        return r_avg
    
    def reward_average_2(self):
        r_avg = [self.avg_values[0] / self.episode_num]
        for i in range(1,self.episode_num):
            x = self.avg_values[i] / self.episode_num
            r_avg.append(x + r_avg[i-1])
        return r_avg
            
        
def main():
    alpha = 0.9
    gamma = 0.9
    epsilon = 0.5
    scenario = 'FrozenLake-v0'
    episode_num = 2000
    q_learn = GreedyLearning(alpha, gamma, epsilon, scenario, episode_num)
    q_learn.initGreedy()
    plt.plot(q_learn.avg_values)
    plt.ylabel('reward')
    plt.xlabel('episode')
    plt.title(scenario + ' reward of single episode')
    plt.show()
    plt.plot(q_learn.reward_avg)
    plt.ylabel('reward_avg')
    plt.xlabel('episode')
    plt.title(scenario + ' average of predecessor episodes')
    plt.show()
    plt.plot(q_learn.reward_avg_2)
    plt.ylabel('reward_avg')
    plt.xlabel('episode')
    plt.title(scenario + ' average of all episodes')
    plt.show()

if __name__ == "__main__":
    main()
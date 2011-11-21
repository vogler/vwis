#!/usr/bin/python2
# -*- coding: utf-8 -*-

"""
    Optimize the problem from task 2c)

    .. moduleauthor::  Sebastian Wiesner  <lunaryorn@googlemail.com>
"""


from operator import itemgetter
from itertools import product, permutations

import numpy as np

Gp = np.array([10000] * 3)
U = np.array(
    [
        [0, 1, 1],
        [1, 0, 1],
        [1, 1, 0]
    ])
Hit = np.array(
    [
        [210, 10],
        [190, 150],
        [430, 30]
    ]
)
Rtp = np.array(
    [
        [10000] * 3,
        [9000] + [0]*2,
    ]
)
P = range(3)
T = range(2)
K = range(3)


def sum_transmission(V):
    return sum (
        Hit[i,t]*Rtp[t,p]*V[p,j]*U[i,j]
        for i in K
        for t in T
        for p in P
        for j in K
    )


def all_Vs():
    L = (0,1)
    for p1, p2, p3 in product(permutations(L), permutations(L), permutations(L)):
        yield np.array(
            [
                [0] + list(p1),
                [0] + list(p2),
                [0] + list(p3),
            ]
        )


def main():
    V, vsum = min(((V, sum_transmission(V)) for V in all_Vs()), key=itemgetter(1))
    print(vsum)
    print(V)



if __name__ == '__main__':
    main()


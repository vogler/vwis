#!/usr/bin/env python2
# Copyright (c) 2012 Sebastian Wiesner <lunaryorn@googlemail.com>

# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this software and associated documentation files (the "Software"),
# to deal in the Software without restriction, including without limitation
# the rights to use, copy, modify, merge, publish, distribute, sublicense,
# and/or sell copies of the Software, and to permit persons to whom the
# Software is furnished to do so, subject to the following conditions:

# The above copyright notice and this permission notice shall be included in
# all copies or substantial portions of the Software.

# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
# THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
# FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.


from __future__ import (print_function, division, unicode_literals,
                        absolute_import)

import sys
import os
from collections import namedtuple
from subprocess import Popen, PIPE
from shutil import copyfileobj
from csv import DictWriter
from argparse import ArgumentParser


NUMBER_OF_PROCESSES = range(4)


class BenchmarkResult(namedtuple('BenchmarkResult', 'TTrans TPS TFork TQuery')):

    @classmethod
    def parse(cls, output):
        results = {l: [] for l in cls._fields}
        for line in output.splitlines():
            parts = line.strip().split()
            label = parts[0].rstrip(':')
            value = float(parts[1])
            results[label].append(value)
        return cls(**{l: min(v) if v else 0 for l, v in results.iteritems()})

    def print(self):
        for label in self._fields:
            formatstr = '{0:<6} - {1}' if label == 'TPS' else '{0:<6} - {1:5.3f} ms'
            print(formatstr.format(label, getattr(self, label)))


def do_benchmark(executable, number_of_processes):
    with open(os.devnull, 'w') as sink:
        print('*** {0} OLAP process(es) ***'.format(number_of_processes))
        process = Popen([executable, str(number_of_processes)], stdout=sink, stderr=PIPE)
        stderr = process.communicate()[1]
        results = BenchmarkResult.parse(stderr)
        results.print()
        return results


def print_cpuinfo():
    with open('/proc/cpuinfo') as source:
        copyfileobj(source, sys.stdout)


def write_csv(filename, results):
    with open(filename, 'wb') as sink:
        fieldnames = ['no_processes']
        fieldnames.extend(BenchmarkResult._fields)
        writer = DictWriter(sink, fieldnames)
        writer.writeheader()
        for no_processes in sorted(results):
            row = {'no_processes': no_processes}
            row.update(results[no_processes]._asdict())
            writer.writerow(row)


def main():
    parser = ArgumentParser()
    parser.add_argument('executable')
    parser.add_argument('outfile', nargs='?')
    args = parser.parse_args()

    print_cpuinfo()

    results = {}
    for no_processes in NUMBER_OF_PROCESSES:
        results[no_processes] = do_benchmark(args.executable, no_processes)

    if args.outfile:
        write_csv(args.outfile, results)


if __name__ == '__main__':
    main()


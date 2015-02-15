#!/usr/bin/env python
"""
Command-line utility for S3DataStore operations. Works directly on S3, not via
REST API.

Usage:
"""

import argparse
import sys
import os
import json
import datetime

sys.path.append(os.path.join(os.path.dirname(__file__), "../src"))

from mydetic.s3_datastore import S3DataStore
from mydetic.memorydata import MemoryData
from mydetic.mydeticexceptions import MyDeticException

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Mydetic command-line')
    parser.add_argument('-c', '--config', help='mydetic S3 config JSON file', required=True)
    parser.add_argument('-u', '--userid', help='user ID', required=True)
    parser.add_argument('-d', '--date', help='date of memory (YYYYMMDD)')
    parser.add_argument('-m', '--memory', help='Memory text to add/update')
    parser.add_argument('-v', '--verbose', help='verbose memory listing', action='store_true')
    parser.add_argument('command', help="S3 operation: 'ls', 'add', 'update' , 'get', 'delete'")

    args = parser.parse_args()
    if not args.config:
        parser.print_help()
        sys.exit(1)

    try:
        with open(args.config, 'r') as fp:
            config = json.load(fp)
    except StandardError, e:
        sys.stderr.write("Failed to load %s: %s\n" % (args.config, str(e)))
        sys.exit(1)

    uid = args.userid
    mem_date = None
    if args.date:
        try:
            mem_date = datetime.datetime.strptime(args.date, "%Y%m%d").date()
        except ValueError, e:
            sys.stderr.write("%s\n" % str(e))
            parser.print_help()
            sys.exit(1)

    if args.command in ['get', 'add', 'update', 'delete']:
        if not mem_date:
            sys.stderr.write("Need to specify memory date\n\n")
            parser.print_help()
            sys.exit(1)

    if args.command in ['add', 'update']:
        if not args.memory:
            sys.stderr.write("Need to specify memory text\n\n")
            parser.print_help()
            sys.exit(1)

    try:
        ds = S3DataStore(s3_config=config)

        if args.command == 'add':
            # Add a memory
            ds.add_memory(user_id=args.userid, memory_date=mem_date,
                          memory=MemoryData(text=args.memory))
        elif args.command == 'ls':
            # List memories
            for mdate in ds.list_memories(user_id=args.userid):
                if args.verbose:
                    memory = ds.get_memory(user_id=uid, memory_date=mdate)
                    print "%s : %s" % (mdate.isoformat(), memory.memory_text)
                else:
                    print mdate.isoformat()
        elif args.command == 'update':
            # Update a memory
            ds.update_memory(user_id=args.userid, memory_date=mem_date,
                             memory=MemoryData(text=args.memory))
        elif args.command == 'get':
            # get memory details
            if not ds.has_memory(user_id=args.userid, memory_date=mem_date):
                sys.stderr.write("No memory for %s on %s\n" % (args.userid, mem_date.isoformat()))
            else:
                print ds.get_memory(user_id=args.userid, memory_date=mem_date).as_json_str()
        elif args.command == 'delete':
            ds.delete_memory(user_id=args.userid, memory_date=mem_date)

    except MyDeticException, e:
        sys.stderr.write("%s\n" % str(e))
        sys.exit(1)

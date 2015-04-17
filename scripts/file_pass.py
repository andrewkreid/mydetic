#!/usr/bin/env python
"""
Command-line utility for managing entries in a FilePasswordStore JSON file
"""

import argparse
import sys
import os

sys.path.append(os.path.join(os.path.dirname(__file__), "../src"))

from mydetic.passwordstore import FilePasswordStore

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Mydetic command-line')
    parser.add_argument('-f', '--file', help='password JSON file', required=True)
    parser.add_argument('-u', '--userid', help='user ID', required=True)
    parser.add_argument('-p', '--password', help='Password to add/update')
    parser.add_argument('command', help="password operation: 'set', 'remove ', 'verify'")

    args = parser.parse_args()
    if not args.file:
        parser.print_help()
        sys.exit(1)

    ds = FilePasswordStore(args.file)

    if args.command in ['set', 'verify']:
        if not args.password:
            sys.stderr.write("need password to set an entry")
            parser.print_help()
            sys.exit(1)

    if args.command == 'set':
        ds.set(args.userid, args.password)

    if args.command == 'verify':
        verified = ds.verify(args.userid, args.password)
        print "Verification: %s" % str(verified)
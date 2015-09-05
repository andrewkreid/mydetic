#!/usr/bin/env python
"""
Command-line utility to import the Little Memory backup format synced to Dropbox.
Writes to S3 directly, not via the REST API.

Usage: import_little_memory.py -c CONFIG -u USERID <directory>
"""

import argparse
import sys
import os
import re
import json
import datetime

sys.path.append(os.path.join(os.path.dirname(__file__), "../src"))

from mydetic.s3_datastore import S3DataStore
from mydetic.memorydata import MemoryData
from mydetic.mydeticexceptions import MyDeticException, MyDeticMemoryAlreadyExists

# Globals
ds = None
user_id = None
filename_re = re.compile('^.*(\d\d\d\d-\d\d-\d\d)\.txt$')


# import a single file into MyDetic
#
# A valid file has a name of the form YYYY-MM-DD.txt. The contents are the plain memory text.
def import_file(filename):
    global filename_re
    global ds
    global user_id
    m = filename_re.match(filename)
    if m:
        print "Matched %s to %s" % (filename, m.group(1))
        try:
            mem_date = datetime.datetime.strptime(m.group(1), "%Y-%m-%d").date()
            with open(filename) as memory_file:
                mem_text = memory_file.read()
            ds.add_memory(MemoryData(user_id=user_id, memory_date=mem_date, memory_text=mem_text))
        except ValueError, e:
            sys.stderr.write("%s for %s\n" % (str(e), filename))
            return
        except MyDeticMemoryAlreadyExists, me:
            sys.stderr.write("%s for %s\n" % (str(me), filename))
            # TODO: option to retry as an update.
            return
    else:
        print "UnMatched %s" % filename


def process_dir(path):
    print "Looking in %s..." % path
    for entry in os.listdir(path):
        fullpath = os.path.join(path, entry)
        # print "Entry: %s" % fullpath
        if os.path.isdir(fullpath):
            process_dir(fullpath)
        elif os.path.isfile(fullpath):
            import_file(fullpath)
        else:
            print "Eh? (%s)" % fullpath

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Mydetic command-line')
    parser.add_argument('-c', '--config', help='mydetic S3 config JSON file', required=True)
    parser.add_argument('-u', '--userid', help='user ID', required=True)
    parser.add_argument('-p', '--path', help='root path of Little Memory Dropbox backup', required=True)

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

    user_id = args.userid
    ds = S3DataStore(s3_config=config["s3_config"])

    # Recurse into the backup path
    process_dir(args.path)



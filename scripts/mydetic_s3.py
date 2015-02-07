#!/usr/bin/env python
"""
Command-line utility for S3DataStore operations. Works directly on S3, not via
REST API.

Usage:
"""

import argparse
import sys
import json

from mydetic.s3_datastore import S3DataStore

if __name__ == "__main__":

    parser = argparse.ArgumentParser(description='Mydetic command-line')
    parser.add_argument('-c', '--config', help='mydetic S3 config JSON file')

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

    ds = S3DataStore(s3_config=config)
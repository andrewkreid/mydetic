#!/usr/bin/env python

#
# MyDetic REST API
#

import sys
import argparse
import json
import logging
import logging.config

from retrying import retry
from flask import Flask, abort, make_response
from flask.ext.restful import Api, Resource, reqparse, fields, marshal

from mydetic.s3_datastore import S3DataStore
from mydetic.memorydata import MemoryData

# The mydetic.datastore.DataStore to use
ds = None

app = Flask(__name__, static_url_path="")
api = Api(app)
# TODO: Authentication


class MemoryListAPI(Resource):
    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument('uid', type=str, required=True,
                                   help='No user ID provided')
        # TODO: Date range params
        super(MemoryListAPI, self).__init__()

    def get(self):
        req_args = self.reqparse.parse_args()
        self.logger.debug("Requesting memories for %s", req_args.uid)
        memories = ds.list_memories(user_id=req_args.uid)
        retval = dict()
        retval['uid'] = req_args.uid
        mem_dates = map(lambda d: d.isoformat(), memories)
        retval['memories'] = mem_dates
        return retval


api.add_resource(MemoryListAPI, '/mydetic/api/v1.0/memories', endpoint='memories')
# api.add_resource(MemoryAPI, '/todo/api/v1.0/memories/<int:id>', endpoint='memory')


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='MyDetic command-line')
    parser.add_argument('-c', '--config', help='mydetic config JSON file', required=True)
    parser.add_argument('-l', '--logconfig', help='logging config file', required=False)

    args = parser.parse_args()
    if not args.config:
        parser.print_help()
        sys.exit(1)

    # set up logging
    if args.logconfig:
        logging.config.fileConfig(args.logconfig)
    else:
        logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s',
                            datefmt='%m/%d/%Y %I:%M:%S %p',
                            level=logging.DEBUG)

    try:
        logging.debug("Configuring with %s", args.config)
        with open(args.config, 'r') as fp:
            config = json.load(fp)
    except StandardError, e:
        logging.critical("Failed to load %s: %s\n", args.config, str(e))
        sys.exit(1)

    ds = S3DataStore(s3_config=config)

    # TODO: Add server config to config file (port, API version etc.)
    app.run(debug=True)
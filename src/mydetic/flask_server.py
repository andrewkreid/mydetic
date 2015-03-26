#!/usr/bin/env python

#
# MyDetic REST API
#

import sys
import argparse
import json
import logging
import logging.config
from datetime import datetime, date

from retrying import retry
from flask import Flask
from flask.ext.restful import Api, Resource, reqparse, fields

from mydetic.s3_datastore import S3DataStore
from mydetic.mydeticexceptions import MyDeticException, MyDeticNoMemoryFound
import errorcodes

# The mydetic.datastore.DataStore to use
ds = None

app = Flask(__name__, static_url_path="")
api = Api(app)
# TODO: Authentication


def parse_iso_date(datestr):
    """
    :param datestr: date string in YYYY-MM-DD format
    :return: datetime.date
    :raises: ValueError
    """
    return datetime.strptime(datestr, "%Y-%m-%d").date()


def generate_error_json(exception=None,
                        mydetic_error_code=None,
                        short_message=None, long_message=None ):
    """
    Generate a standard error JSON body for the API to return on errors.
    :param exception: Optional MyDeticException to get error info from
    :param mydetic_error_code: One of errorcodes
    :param short_message: The default short message will come from errorcodes.error_descs. You can override this
                          by passing in a string message
    :param long_message:
    :return: A JSON object
    """
    rval = {
        'error_code': errorcodes.UNEXPLAINED_FAILURE,
        'short_message': errorcodes.error_descs[errorcodes.UNEXPLAINED_FAILURE],
        'long_message': ''
    }
    if exception is not None:
        if not isinstance(exception, MyDeticException):
            raise ValueError("Exception is not of type MyDeticException")
        rval['error_code'] = exception.error_code
        rval['long_message'] = str(exception)

    if mydetic_error_code is not None:
        rval['error_code'] = mydetic_error_code
    if short_message is not None:
        rval['short_message'] = short_message
    else:
        rval['short_message'] = errorcodes.error_descs[rval['error_code']]
    if long_message is not None:
        rval['long_message'] = long_message

    return rval


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


class MemoryAPI(Resource):
    """ Handles CRUD operations on individual memory entries
    """
    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument('uid', type=str, required=True,
                                   help='No user ID provided')
        super(MemoryAPI, self).__init__()

    def get(self, date_str):
        try:
            req_args = self.reqparse.parse_args()
            mem_date = parse_iso_date(date_str)
            self.logger.debug("Requesting memory for %s on %s", req_args.uid, mem_date.isoformat())
            memory = ds.get_memory(req_args.uid, mem_date)
            return memory.to_dict()
        except MyDeticNoMemoryFound, mnfe:
            return generate_error_json(mnfe), 404
        except MyDeticException, mde:
            return generate_error_json(mde), 500
        except ValueError:
            # invalid date
            return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                       long_message='Expected YYYY-MM-DD'), 400


api.add_resource(MemoryListAPI, '/mydetic/api/v1.0/memories', endpoint='memories')
api.add_resource(MemoryAPI, '/mydetic/api/v1.0/memories/<string:date_str>', endpoint='memory')


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
#!/usr/bin/env python

#
# MyDetic REST API
#

import sys
import argparse
import json
import copy
import logging
import logging.config
from datetime import datetime

from flask import Flask, request
from flask.ext.restful import Api, Resource, reqparse, fields

from memorydata import MemoryData
from mydetic.s3_datastore import S3DataStore
from mydetic.mydeticexceptions import MyDeticException, MyDeticNoMemoryFound, MyDeticMemoryAlreadyExists, \
    MyDeticInvalidMemoryString
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
                        short_message=None, long_message=None):
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


def parse_memory_from_request():
    if not request.json:
        raise MyDeticInvalidMemoryString("POST request body was not JSON")

    for arg in ['user_id', 'memory_date', 'memory_text']:
        if arg not in request.json:
            raise MyDeticInvalidMemoryString("expected '%s' parameter" % arg)

    try:
        memory_date = parse_iso_date(request.json['memory_date'])
    except ValueError:
        raise MyDeticInvalidMemoryString('Expected YYYY-MM-DD')

    memory_text = request.json['memory_text']
    user_id = request.json['user_id']

    return MemoryData(user_id=user_id, memory_date=memory_date, memory_text=memory_text)


class MemoryListAPI(Resource):
    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.reqparse_get = reqparse.RequestParser()
        self.reqparse_get.add_argument('user_id', type=str, required=True,
                                       help='No user ID provided')
        self.reqparse_get.add_argument('start_date', type=str, required=False,
                                       help='start date of query range (inclusive) YYYY-MM-DD')
        self.reqparse_get.add_argument('end_date', type=str, required=False,
                                       help='end date of query range (inclusive) YYYY-MM-DD')

        super(MemoryListAPI, self).__init__()

    def get(self):
        """
        Get the list of memories for a user
        :return:
        """
        req_args = self.reqparse_get.parse_args()

        start_date = None
        if req_args.start_date:
            try:
                start_date = parse_iso_date(req_args.start_date)
            except ValueError:
                return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                           long_message='Expected YYYY-MM-DD for start_date'), 400
        end_date = None
        if req_args.end_date:
            try:
                end_date = parse_iso_date(req_args.end_date)
            except ValueError:
                return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                           long_message='Expected YYYY-MM-DD for end_date'), 400

        if start_date and end_date and (start_date > end_date):
            return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                       long_message='start_date after end_date'), 400

        self.logger.debug("Requesting memories for %s (%s -> %s)",
                          req_args.user_id,
                          start_date.isoformat() if start_date else 'NO_DATE',
                          end_date.isoformat() if end_date else 'NO_DATE')

        try:
            memories = ds.list_memories(user_id=req_args.user_id, start_date=start_date, end_date=end_date)
        except MyDeticException, mde:
            return generate_error_json(mde), 500
        retval = dict()
        retval['user_id'] = req_args.user_id
        mem_dates = map(lambda d: d.isoformat(), memories)
        retval['memories'] = mem_dates
        return retval

    def post(self):
        """
        Create a new memory for a user/date combination. Expects the request body to be
        a MemoryData JSON object
        :return:
        """
        try:
            memory = parse_memory_from_request()
            self.logger.info("Adding memory for %s on %s", memory.user_id, memory.memory_date)
            ds.add_memory(memory)

            # re-fetch the memory and return it in the response body
            added_memory = ds.get_memory(memory.user_id, memory.memory_date)
            return added_memory.to_dict(), 201
        except MyDeticMemoryAlreadyExists, mdmae:
            self.logger.info("Memory already exists on add (%s : %s)", memory.user_id, memory.memory_date)
            return generate_error_json(mdmae)
        except MyDeticInvalidMemoryString, mdims:
            self.logger.info("Invalid memory string on add (%s)" % mdims.msg)
            return generate_error_json(mdims)
        except MyDeticException, mde:
            return generate_error_json(mde)


class MemoryAPI(Resource):
    """ Handles CRUD operations on individual memory entries
    """

    def __init__(self):
        self.logger = logging.getLogger(__name__)
        self.reqparse = reqparse.RequestParser()
        self.reqparse.add_argument('user_id', type=str, required=True,
                                   help='No user ID provided')
        super(MemoryAPI, self).__init__()

    def get(self, date_str):
        try:
            req_args = self.reqparse.parse_args()
            mem_date = parse_iso_date(date_str)
            self.logger.info("Requesting memory for %s on %s", req_args.user_id, mem_date.isoformat())
            memory = ds.get_memory(req_args.user_id, mem_date)
            return memory.to_dict()
        except MyDeticNoMemoryFound, mnfe:
            return generate_error_json(mnfe), 404
        except MyDeticException, mde:
            return generate_error_json(mde), 500
        except ValueError:
            # invalid date
            return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                       long_message='Expected YYYY-MM-DD'), 400

    def put(self, date_str):
        """
        Modify an existing memory for a user/date combination. Expects the request body to be
        a MemoryData JSON object
        :return:
        """
        try:
            req_args = self.reqparse.parse_args()
            memory = parse_memory_from_request()
            req_date = parse_iso_date(date_str)
            if req_date != memory.memory_date:
                raise MyDeticException(error_code=errorcodes.INVALID_INPUT,
                                       msg="Parameter and body dates don't match")
            if req_args.user_id != memory.user_id:
                raise MyDeticException(error_code=errorcodes.INVALID_INPUT,
                                       msg="Parameter and body user_id don't match")

            self.logger.info("Updating memory for %s on %s", memory.user_id, memory.memory_date)
            ds.update_memory(memory)

            # re-fetch the memory and return it in the response body
            updated_memory = ds.get_memory(memory.user_id, memory.memory_date)
            return updated_memory.to_dict()
        except MyDeticNoMemoryFound, mdnmf:
            self.logger.info("Memory not found on update (%s)", date_str)
            return generate_error_json(mdnmf), 404
        except MyDeticInvalidMemoryString, mdims:
            self.logger.info("Invalid memory string on add (%s)" % mdims.msg), 400
            return generate_error_json(mdims)
        except MyDeticException, mde:
            return generate_error_json(mde), 500
        except ValueError:
            # invalid date
            return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                       long_message='Expected YYYY-MM-DD'), 400

    def delete(self, date_str):
        try:
            req_args = self.reqparse.parse_args()
            req_date = parse_iso_date(date_str)
            memory = ds.delete_memory(user_id=req_args.user_id, memory_date=req_date)
            return memory.to_dict()
        except MyDeticNoMemoryFound, mdnmf:
            self.logger.info("Memory not found on delete (%s)", date_str)
            return generate_error_json(mdnmf), 404
        except MyDeticInvalidMemoryString, mdims:
            self.logger.info("Invalid memory string on add (%s)" % mdims.msg)
            return generate_error_json(mdims), 400
        except MyDeticException, mde:
            return generate_error_json(mde), 500
        except ValueError:
            # invalid date
            return generate_error_json(mydetic_error_code=errorcodes.INVALID_INPUT,
                                       long_message='Expected YYYY-MM-DD'), 400


api.add_resource(MemoryListAPI, '/mydetic/api/v1.0/memories', endpoint='memories')
api.add_resource(MemoryAPI, '/mydetic/api/v1.0/memories/<string:date_str>', endpoint='memory')

# This is the default configuration for the server. It will be merged with
# entries from the config JSON file (which should be in the same format)
DEFAULT_CONFIG = {
    "s3_config": {
        "aws_access_key_id": "",
        "aws_secret_access_key": "",
        "bucket": "mydetic.yourdomain",
        "region": "ap-southeast-2"
    },
    "server_config": {
        "port": 5000,
        "debug": True
    }
}

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
            user_config = json.load(fp)
        config = copy.deepcopy(DEFAULT_CONFIG)
        config.update(user_config)

    except StandardError, e:
        logging.critical("Failed to load %s: %s\n", args.config, str(e))
        sys.exit(1)

    ds = S3DataStore(s3_config=config["s3_config"])

    server_config = config['server_config']
    app.run(debug=server_config['debug'], port=server_config['port'])
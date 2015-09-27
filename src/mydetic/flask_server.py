#!/usr/bin/env python

#
# MyDetic REST API
#

import os
import traceback
import sys
import argparse
import json
import copy
import logging
import logging.config
from datetime import datetime

from flask import Flask, request, jsonify
from flask.ext.restful import Api, Resource, reqparse, fields
from flask.ext.httpauth import HTTPBasicAuth

from mydetic.memorydata import MemoryData
from mydetic.s3_datastore import S3DataStore
from mydetic.mydeticexceptions import MyDeticException, MyDeticNoMemoryFound, MyDeticMemoryAlreadyExists, \
    MyDeticInvalidMemoryString
import errorcodes
from mydetic.passwordstore import FilePasswordStore

auth = HTTPBasicAuth()

@auth.verify_password
def verify_pw(username, password):
    """
    Called to verify HTTP Basic auth username/password combinations
    :param username:
    :param password:
    :return: True if credentials are valid, False otherwise
    """
    global password_store
    logger = logging.getLogger('verify_pw')
    if not password_store:
        logger.error("No password store specified")
        return False
    logger.debug("Verifying password for %s" % username)
    return password_store.verify(username, password)


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
    """
    Handles the REST API for listing and creating new memories.
    """

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
        self.logger.error("VV GET")
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

# This is the default configuration for the server. It will be merged with
# entries from the config JSON file (which should be in the same format)
DEFAULT_CONFIG = {
    "s3_config": {
        "aws_access_key_id": "UNSET",
        "aws_secret_access_key": "UNSET",
        "bucket": "mydetic.yourdomain",
        "region": "ap-southeast-2"
    },
    "server_config": {
        "port": 5000,
        "debug": True
    },
    "auth_config": {
        "method": "HTTP Basic",
        "store": "file",
        "store_file": "passwords.json"
    }
}

# Initialize the API configuration from the command-line and/or
# env vars
def init_config():
    config_filename = None
    logging_filename = None

    if __name__ == "__main__":
        # Use command-line args if we're running standalone

        parser = argparse.ArgumentParser(description='MyDetic command-line')
        parser.add_argument('-c', '--config', help='mydetic config JSON file', required=False)
        parser.add_argument('-l', '--logconfig', help='logging config file', required=False)
        # parser = OptionParser()
        # parser.add_option("-c", "--config", dest="config",
        #                           help="mydetic config JSON file", metavar="FILE")
        # parser.add_option("-l", "--logconfig", dest="logconfig",
        #                           help="logging config file", metavar="FILE")

        args = parser.parse_args()
        # (options, args) = parser.parse_args()

        config_filename = args.config
        logging_filename = args.logconfig
    else:
        # Look for environment variables
        if 'MYDETIC_LOGCONFIG' in os.environ:
            logging_filename = os.environ['MYDETIC_LOGCONFIG']
        if 'MYDETIC_CONFIG' in os.environ:
            config_filename = os.environ['MYDETIC_CONFIG']

    the_config = copy.deepcopy(DEFAULT_CONFIG)

    if config_filename is not None:
        logging.debug("Configuring with %s", config_filename)
        with open(config_filename, 'r') as fp:
            user_config = json.load(fp)
            the_config.update(user_config)

    if logging_filename is not None:
        logging.config.fileConfig(logging_filename)
    else:
        logging.basicConfig(format='%(asctime)s %(levelname)s %(message)s',
                            datefmt='%m/%d/%Y %I:%M:%S %p',
                            level=logging.DEBUG)
    return the_config

# ---------------------------------------------------------------------------

# The mydetic.datastore.DataStore to use
ds = None
config = None
password_store = None

def create_app():
    global ds
    global config
    global password_store

    try:
        app = Flask(__name__, static_url_path="")
        api = Api(app)
        password_store = None

        config = init_config()

        # A stand alone route for testing
        @app.route('/')
        def index():
            ### Some code here ###
            return jsonify({'status': 200, 'success':True})

        # Set up password store for HTTP Basic auth
        if config['auth_config']['method'] == "HTTP Basic":
            password_store = FilePasswordStore(config['auth_config']['store_file'])
            MemoryAPI.decorators = [auth.login_required]
            MemoryListAPI.decorators = [auth.login_required]

        api.add_resource(MemoryListAPI, '/mydetic/api/v1.0/memories', endpoint='memories')
        api.add_resource(MemoryAPI, '/mydetic/api/v1.0/memories/<string:date_str>', endpoint='memory')

        ds = S3DataStore(s3_config=config["s3_config"])

        server_config = config['server_config']

        return app
    except:
        traceback.print_exc(file=sys.stderr)
    return None


if __name__ == "__main__":
    app = create_app()
    server_config = config['server_config']
    app.run(debug=server_config['debug'], port=server_config['port'])

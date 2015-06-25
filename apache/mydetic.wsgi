#
# MyDetic sample WSGI file
#
import os
import sys, traceback
PROPAGATE_EXCEPTIONS = True

# replace this with the path to your virtualenv environment
activate_this = '/home/ec2-user/mydetic-env/bin/activate_this.py'
execfile(activate_this, dict(__file__=activate_this))

def application(req_environ, start_response):
    try:
        from mydetic.flask_server import create_app

        # Copy environment params from the Apache config to the system
        # environment
        for env_param in ['MYDETIC_CONFIG', 'MYDETIC_LOGGING_CONFIG']:
            if env_param in req_environ:
                os.environ[env_param] = req_environ[env_param]

        # TODO: Don't create a new app object each request.
        _application = create_app()
        return _application(req_environ, start_response)
    except:
        traceback.print_exc(file=sys.stderr)

# vi:syntax=python

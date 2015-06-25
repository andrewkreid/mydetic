#
# MyDetic sample WSGI file
#
import sys, traceback
PROPAGATE_EXCEPTIONS = True
print >>sys.stderr, "hello"

# replace this with the path to your virtualenv environment
activate_this = '/home/ec2-user/mydetic-env/bin/activate_this.py'
execfile(activate_this, dict(__file__=activate_this))
print >>sys.stderr, "hello2"

try:
   from mydetic.flask_server import app as _application
except:
    print >>sys.stderr, "hello2.5"
    traceback.print_exc(file=sys.stderr)
print >>sys.stderr, "hello3"

def application(req_environ, start_response):
    for env_param in ['MYDETIC_CONFIG', 'MYDETIC_LOGGING_CONFIG']:
        if env_param in req_environ:
            os.environ[param] = req_environ[param]
    return _application(req_environ, start_response)

# Copy environment params from the Apache config to the system
# environment
print >>sys.stderr, "hello4"


# vi:syntax=python

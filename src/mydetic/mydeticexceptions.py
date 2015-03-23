
"""
Exceptions for the MyDetic data store
"""

import datetime
import errorcodes


class MyDeticException(Exception):
    def __init__(self, error_code, msg=""):
        self._error_code = error_code
        self._msg = msg

    def __str__(self):
        return "MyDeticException: %d:%s" % (self.error_code, self._msg)

    @property
    def msg(self):
        return self._msg

    @property
    def error_code(self):
        return self._error_code


class MyDeticMemoryException(MyDeticException):
    def __init__(self, user_id, date, msg=""):
        MyDeticException.__init__(self, errorcodes.INVALID_DATA_OPERATION, msg)
        self._user_id = user_id
        if not isinstance(date, datetime.date):
            raise TypeError('MyDeticMemoryException: expected datetime.date, got %s' % str(type(date)))
        self._date = date
        self._msg = msg

    def __str__(self):
        return "MyDeticMemoryException for %s on %s" % (self._user_id, self._date.isoformat())


class MyDeticNoMemoryFound(MyDeticMemoryException):
    """
    Exception raised when we expected to find a memory for a user on a date but
    there wasn't one.
    """
    def __init__(self, user_id, date, msg=""):
        MyDeticMemoryException.__init__(self, user_id, date, msg)

    def __str__(self):
        return "No memory available for %s on %s" % (self._user_id, self._date.isoformat())


class MyDeticMemoryAlreadyExists(MyDeticMemoryException):
    """
    Exception raised when we expected no memory to exist but one did.
    """
    def __init__(self, user_id, date, msg=""):
        MyDeticMemoryException.__init__(self, user_id, date, msg)

    def __str__(self):
        return "A memory already exists for %s on %s" % (self._user_id, self._date.isoformat())


class MyDeticInvalidMemoryString(MyDeticException):
    """
    Exception raised when the string JSON format of a memory can't be parsed
    """
    def __init__(self, memory_text):
        MyDeticException.__init__(self, errorcodes.INVALID_DATA_FORMAT, memory_text)

    def __str__(self):
        return "MyDeticInvalidMemoryString: \"%s\"" % self.msg


class MyDeticDataStoreException(MyDeticException):
    """
    Wrapper exception for errors from the underlying data store. Use this
    exception so that the REST API doesn't need to be aware of exception classes
    peculiar to the data store implementation (eg Boto or SQLite).
    """
    def __init__(self, caused_by, error_code=errorcodes.DATASTORE_FAILURE, msg=None):
        self._caused_by = caused_by
        MyDeticException.__init__(self, error_code=error_code,
                                  msg=msg if msg is not None else str(caused_by))

    def caused_by(self):
        return self._caused_by

    def __str__(self):
        return "MyDeticDataStoreException: \"%s\"" & self.msg
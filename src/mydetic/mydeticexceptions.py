
"""
Exceptions for the MyDetic data store
"""

import datetime


class MyDeticException(Exception):
    def __init__(self, msg=""):
        self._msg = msg

    def __str__(self):
        return "MyDeticException: %s" % self._msg

    @property
    def msg(self):
        return self._msg


class MyDeticMemoryException(MyDeticException):
    def __init__(self, user_id, date, msg=""):
        MyDeticException.__init__(self, msg)
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
        MyDeticException.__init__(self, memory_text)

    def __str__(self):
        return "MyDeticInvalidMemoryString: \"%s\"" % self._msg

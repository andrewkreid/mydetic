"""
Exceptions for the MyDetic data store
"""

import datetime


class MyDeticMemoryException(Exception):
    def __init__(self, user_id, date):
        self._user_id = user_id
        if not isinstance(date, datetime.date):
            raise TypeError('MyDeticMemoryException: expected datetime.date, got %s' % str(type(date)))
        self._date = date

    def __str__(self):
        return "MyDeticMemoryException for %s on %s" % (self._user_id, self._date.isoformat())


class MyDeticNoMemoryFound(MyDeticMemoryException):
    """
    Exception raised when we expected to find a memory for a user on a date but
    there wasn't one.
    """
    def __init__(self, user_id, date):
        super.__init__(user_id, date)

    def __str__(self):
        return "No memory available for %s on %s" % (self._user_id, self._date.isoformat())


class MyDeticMemoryAlreadyExists(MyDeticMemoryException):
    """
    Exception raised when we expected no memory to exist but one did.
    """
    def __init__(self, user_id, date):
        super.__init__(user_id, date)

    def __str__(self):
        return "A memory already exists for %s on %s" % (self._user_id, self._date.isoformat())


"""
Definition of the DataStore interface to the stored memories.

S3DataStore implementation that stores memories in an S3 bucket.
"""

import datetime


class DataStore:
    """
    Abstract base class for memory data stores
    """

    def __init__(self):
        pass

    def list_memories(self, user_id, start_date=None, end_date=None):
        """
        Get a list of dates that contain memories for a user.
        :param user_id: the User ID
        :type user_id: str or unicode
        :param start_date: optional start date to clamp range to (inclusive)
        :type start_date: datetime.date
        :param end_date: datetime.date optional end date to clamp range to (inclusive)
        :type end_date: datetime.date
        :return: a list of datetime.date in ascending date order where memories exist.
        """
        raise NotImplemented()

    def get_memory(self, user_id, memory_date):
        """
        :param user_id: str the User ID
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: The memory
        :raises MyDeticNoMemoryFound if there isn't a memory on this day
        """
        raise NotImplemented()

    def has_memory(self, user_id, memory_date):
        """
        Return whether a memory exists for a user at a date.
        :param user_id:
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: True if a memory exists, false otherwise.
        """
        raise NotImplemented()

    def add_memory(self, user_id, memory_date, memory):
        """

        :param user_id:
        :type user_id: str or unicode
        :param memory_date:
        :param memory:
        :raises MyDeticMemoryAlreadyExists
        :return:
        """
        raise NotImplemented()

    def update_memory(self, user_id, memory_date, memory):
        raise NotImplemented()

    def delete_memory(self, user_id, memory_date, memory):
        raise NotImplemented()
# Definition of the DataStore interface to the stored memories.
# S3DataStore implementation that stores memories in an S3 bucket.

import mydeticexceptions


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
        raise NotImplementedError()

    def get_memory(self, user_id, memory_date):
        """
        :param user_id: str the User ID
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: The memory
        :raises MyDeticNoMemoryFound if there isn't a memory on this day
        """
        raise NotImplementedError()

    def has_memory(self, user_id, memory_date):
        """
        Return whether a memory exists for a user at a date.
        :param user_id:
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: True if a memory exists, false otherwise.
        """
        raise NotImplementedError()

    def add_memory(self, memory):
        """
        :param memory:
        :raises MyDeticMemoryAlreadyExists
        :return:
        """
        raise NotImplementedError()

    def update_memory(self, memory):
        """
        :param memory: updated MemoryData object. NOTE: only text is changed.
        :return: No return value
        :raises: MyDeticNoMemoryFound is memory doesn't already exist
        """
        raise NotImplementedError()

    def delete_memory(self, user_id, memory_date):
        """
        :param user_id: str the User ID
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: The memory
        :raises MyDeticNoMemoryFound if there isn't a memory on this day
        """
        raise NotImplementedError()


class ExceptionWrappedDataStore(DataStore):
    """
    A decorator for a DataStore that ensures that all exceptions thrown are
    descendants of MyDeticException.
    """

    def __init__(self, datastore_impl):
        self._ds_impl = datastore_impl
        DataStore.__init__(self)

    def wrap_call(self, func, *args, **kwargs):
        try:
            return func(self._ds_impl, *args, **kwargs)
        except Exception, e:
            if isinstance(e, mydeticexceptions.MyDeticException):
                raise
            else:
                raise mydeticexceptions.MyDeticDataStoreException(caused_by=e)

    def get_memory(self, user_id, memory_date):
        return self.wrap_call(ExceptionWrappedDataStore.get_memory, user_id, memory_date)

    def has_memory(self, user_id, memory_date):
        return self.wrap_call(ExceptionWrappedDataStore.has_memory, user_id, memory_date)

    def update_memory(self, memory):
        return self.wrap_call(ExceptionWrappedDataStore.update_memory, memory)

    def add_memory(self, memory):
        return self.wrap_call(ExceptionWrappedDataStore.add_memory, memory)

    def list_memories(self, user_id, start_date=None, end_date=None):
        return self.wrap_call(ExceptionWrappedDataStore.list_memories, user_id, start_date, end_date)

    def delete_memory(self, user_id, memory_date):
        return self.wrap_call(ExceptionWrappedDataStore.delete_memory, user_id, memory_date)



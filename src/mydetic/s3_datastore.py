"""
DataStore implementation that stores data in an S3 Bucket.
"""

import boto
from boto.s3.key import Key
from datastore import DataStore


class S3DataStore(DataStore):
    def __init__(self, s3_config=None):
        """
        Constructor
        :param s3_config: Dictionary containing S3 connection details.
        """
        DataStore.__init__(self)
        if s3_config is not None:
            self.validate_s3_params(s3_config)
        self._s3_config = s3_config
        self._connection = boto.s3.connect_to_region('ap-southeast-2')

    @staticmethod
    def validate_s3_params(s3_config):
        """
        Check that everything is tickety-boo with the s3 params. Just
        check that they're all there, not that they're right.
        :param s3_config: Dictionary containing S3 connection details.
        :raises: ValueError
        """
        missing_params = []
        for required_param in ['region', 'aws_access_key_id', 'aws_secret_access_key', 'bucket']:
            if required_param not in s3_config:
                missing_params.append(required_param)
        if len(missing_params) > 0:
            raise ValueError("s3 config is missing [%s]" % ','.join(missing_params))

    def get_memory(self, user_id, memory_date):
        return DataStore.get_memory(self, user_id, memory_date)

    def has_memory(self, user_id, memory_date):
        return DataStore.has_memory(self, user_id, memory_date)

    def update_memory(self, user_id, memory_date, memory):
        return DataStore.update_memory(self, user_id, memory_date, memory)

    def add_memory(self, user_id, memory_date, memory):
        return DataStore.add_memory(self, user_id, memory_date, memory)

    def list_memories(self, user_id, start_date=None, end_date=None):
        return DataStore.list_memories(self, user_id, start_date, end_date)

    def delete_memory(self, user_id, memory_date, memory):
        return DataStore.delete_memory(self, user_id, memory_date, memory)
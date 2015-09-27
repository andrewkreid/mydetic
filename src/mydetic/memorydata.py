# class MemoryData represents all the data associated with a memory.

import json
from datetime import datetime, date
import dateutil.parser
from mydeticexceptions import MyDeticInvalidMemoryString


class MemoryData(object):

    _user_id = ''
    _memory_text = ''
    _memory_date = None
    _created_at = None
    _modified_at = None
    _revision = 1

    def __init__(self, user_id, memory_date, memory_text='', created_at=None, modified_at=None):
        self._user_id = user_id
        self._memory_text = memory_text
        if not isinstance(memory_date, date):
            raise ValueError("memory_date must be a datetime.date")

        self._memory_date = memory_date
        if created_at is not None:
            if not isinstance(created_at, datetime):
                raise ValueError("created_at must be a datetime.datetime")
            self._created_at = created_at
        else:
            self._created_at = datetime.utcnow()
        if modified_at is not None:
            if not isinstance(modified_at, datetime):
                raise ValueError("modified_at must be a datetime.datetime")
            self._modified_at = modified_at
        else:
            self._modified_at = self.created_at

    @property
    def user_id(self):
        return self._user_id

    @user_id.setter
    def user_id(self, user_id):
        self._user_id = user_id

    @property
    def memory_text(self):
        return self._memory_text

    @memory_text.setter
    def memory_text(self, memory_text):
        self._memory_text = memory_text

    @property
    def memory_date(self):
        return self._memory_date

    @memory_date.setter
    def memory_date(self, memory_date):
        self._memory_text = memory_date

    @property
    def created_at(self):
        return self._created_at

    @property
    def modified_at(self):
        return self._modified_at

    @property
    def revision(self):
        """
        The revision is an integer that the API will increment each time it saves the memory. It is used to check
        whether an update would overwrite another change which has been saved to the datastore since the memory was
        first read. Only the API should modify this value.
        :return:
        """
        return self._revision

    @revision.setter
    def revision(self, new_rev):
        self._revision = new_rev

    def touch(self):
        """
        Update modified_at to current UTC time
        :return:
        """
        self._modified_at = datetime.utcnow()
        return self._modified_at

    def to_dict(self):
        return {
            'user_id': self._user_id,
            'memory_date': self._memory_date.isoformat(),
            'memory_text': self._memory_text,
            'created_at': self._created_at.isoformat(),
            'modified_at': self._modified_at.isoformat(),
            'revision': self._revision
        }

    @staticmethod
    def from_dict(memory_dict):
        if not MemoryData.validate_memory_dict(memory_dict):
            raise MyDeticInvalidMemoryString(json.dumps(memory_dict))
        retval = MemoryData(
            user_id=memory_dict['user_id'],
            memory_text=memory_dict['memory_text'],
            memory_date=dateutil.parser.parse(memory_dict['memory_date']).date(),
            created_at=dateutil.parser.parse(memory_dict['created_at']),
            modified_at=dateutil.parser.parse(memory_dict['modified_at']))
        # revision added later, so don't require it
        if 'revision' in memory_dict:
            retval.revision = memory_dict['revision']
        else:
            retval.revision = 1
        return retval

    @staticmethod
    def from_json_str(memory_json):
        return MemoryData.from_dict(json.loads(memory_json))

    @staticmethod
    def validate_memory_dict(memory_dict):
        """
        Do some sanity checking on the Dict form of a MemoryData object
        :param memory_dict:
        :return: True if contains all fields and fields are valid.
        """
        for param in ['user_id', 'memory_text', 'memory_date', 'created_at', 'modified_at']:
            if param not in memory_dict:
                return False
        try:
            dateutil.parser.parse(memory_dict['memory_date'])
            dateutil.parser.parse(memory_dict['created_at'])
            dateutil.parser.parse(memory_dict['modified_at'])
        except ValueError:
            # Couldn't parse date fields into datetime objects
            return False

        return True

    def as_json_str(self):
        """
        :return: a JSON string of the memory
        """
        return json.dumps(self.to_dict(), indent=4)

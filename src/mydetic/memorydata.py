"""
class MemoryData represents all the data associated with a memory.
"""
import json
from datetime import datetime, date
import dateutil.parser

from mydeticexceptions import MyDeticInvalidMemoryString


class MemoryData(object):

    # TODO: _user_id ??????
    _memory_text = ''
    _memory_date = None
    _created_at = None
    _modified_at = None

    def __init__(self, memory_text, memory_date, created_at=None, modified_at=None):
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
    def memory_text(self):
        return self._memory_text

    @memory_text.setter
    def memory_text(self, memory_text):
        self._memory_text = memory_text

    @property
    def created_at(self):
        return self._created_at

    @property
    def modified_at(self):
        return self._modified_at

    def to_dict(self):
        return {
            'memory_text': self._memory_text,
            'memory_date': self._memory_date.isoformat(),
            'created_at': self._created_at.isoformat(),
            'modified_at': self._modified_at.isoformat()
        }

    @staticmethod
    def from_dict(memory_dict):
        if not MemoryData.validate_memory_dict(memory_dict):
            raise MyDeticInvalidMemoryString(json.dumps(memory_dict))
        return MemoryData(
            memory_text=memory_dict['memory_text'],
            memory_date=dateutil.parser.parse(memory_dict['memory_date']).date(),
            created_at=dateutil.parser.parse(memory_dict['created_at']),
            modified_at=dateutil.parser.parse(memory_dict['modified_at']))

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
        for param in ['memory_text', 'memory_date', 'created_at', 'modified_at']:
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

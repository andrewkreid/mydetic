"""
class MemoryData represents all the data associated with a memory.
"""
import json
from exceptions import MyDeticInvalidMemoryString

class MemoryData:

    def __init__(self, memory_text):
        self._memory_text = memory_text

    @property
    def memory_text(self):
        return self._memory_text

    @memory_text.setter
    def memory_text(self, x):
        self._memory_text = x

    def to_dict(self):
        return {
            'memory_text': self._memory_text
        }

    @staticmethod
    def from_dict(memory_dict):
        if not MemoryData.validate_memory_dict(memory_dict):
            raise MyDeticInvalidMemoryString(json.dumps(memory_dict))
        return MemoryData(memory_text=memory_dict['memory_text'])

    @staticmethod
    def from_json_str(memory_json):
        return MemoryData.from_dict(json.loads(memory_json))

    @staticmethod
    def validate_memory_dict(memory_dict):
        if 'memory_text' not in memory_dict:
            return False
        return True

    def as_json_str(self):
        """
        :return: a JSON string of the memory
        """
        return json.dumps(self.to_dict())

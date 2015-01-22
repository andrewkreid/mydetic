from mydetic.memorydata import MemoryData
from mydetic.exceptions import MyDeticInvalidMemoryString
import json
import pytest


def test_create_memorydata():
    m = MemoryData('foo')
    assert m.memory_text == 'foo'


def test_from_json():
    m = MemoryData.from_json_str('{ "memory_text": "foo" }')
    assert m.memory_text == 'foo'

    try:
        MemoryData.from_json_str('{ "memory_text_wrong": "foo" }')
        assert False, "Should have thrown exception"
    except MyDeticInvalidMemoryString, e:
        assert "memory_text_wrong" in e.msg


import boto
from boto.s3.connection import Location
from moto import mock_s3

from mydetic.s3_datastore import S3DataStore
from datetime import date
from mydetic.memorydata import MemoryData
from mydetic.mydeticexceptions import MyDeticMemoryAlreadyExists, MyDeticNoMemoryFound
import pytest

DEF_CONFIG = {
    'aws_access_key_id': 'foo',
    'aws_secret_access_key': 'foo',
    'bucket': 'foo',

    # NB: Moto doesn't mock regional S3 endpoint URLs properly
    'region': Location.DEFAULT
}


def test_validate_config():
    s3_config = {}
    with pytest.raises(ValueError):
        S3DataStore.validate_s3_params(s3_config)

    # missing values
    s3_config = dict(region='foo', bucket='foo')
    try:
        S3DataStore.validate_s3_params(s3_config)
        assert False, "Should have thrown exception"
    except ValueError, e:
        assert 'aws_secret_access_key' in str(e)
        assert 'aws_access_key_id' in str(e)

    # should not raise exception
    s3_config = {
        'aws_access_key_id': 'foo',
        'aws_secret_access_key': 'foo',
        'bucket': 'foo',
        'region': Location.APSoutheast2
    }
    S3DataStore.validate_s3_params(s3_config)


def test_generate_key_name():
    d1 = date(2013, 11, 12)
    assert "foo/20131112.json" == S3DataStore.generate_memory_key_name('foo', d1)

    # Unicode should be saved as utf-8
    u = unichr(40960) + u'abcd' + unichr(1972) + "20131112.json"
    expected_str = u.encode("utf-8") + "/20131112.json"
    assert expected_str == S3DataStore.generate_memory_key_name(u, d1)


@mock_s3
def test_add_memory():
    s3store = S3DataStore(DEF_CONFIG)
    s3store.add_memory('foo', date(2013, 11, 12), MemoryData(text='foo'))

    try:
        s3store.add_memory('foo', date(2013, 11, 12), MemoryData(text='foo'))
        assert False, "Should have throws exception"
    except MyDeticMemoryAlreadyExists:
        pass


@mock_s3
def test_get_memory():
    s3store = S3DataStore(DEF_CONFIG)

    s3store.add_memory('bar', date(2014, 11, 12), MemoryData(text='bar memory'))
    memory = s3store.get_memory('bar', date(2014, 11, 12))
    assert memory is not None
    assert memory.memory_text == 'bar memory'


@mock_s3
def test_update_memory():
    s3store = S3DataStore(DEF_CONFIG)
    mem_date = date(2013, 11, 12)
    uid = 'foo'
    s3store.add_memory(uid, mem_date, MemoryData(text='foo'))

    memory = s3store.get_memory(uid, mem_date)
    assert memory.memory_text == 'foo'

    with pytest.raises(MyDeticNoMemoryFound):
        s3store.update_memory('nosuchuid', mem_date, memory)

    memory.memory_text = 'bar'
    s3store.update_memory(uid, mem_date, memory)

    updated_memory = s3store.get_memory(uid, mem_date)
    assert memory.memory_text == updated_memory.memory_text


@mock_s3
def test_list_memories():
    s3store = S3DataStore(DEF_CONFIG)
    uid = 'foo'

    memories = s3store.list_memories(uid)
    assert len(memories) == 0

    s3store.add_memory(uid, date(2014, 11, 14), MemoryData(text='yet another memory'))
    s3store.add_memory(uid, date(2014, 11, 12), MemoryData(text='memory'))
    s3store.add_memory(uid, date(2014, 11, 13), MemoryData(text='another memory'))
    s3store.add_memory('different uid', date(2014, 11, 13), MemoryData(text='another memory'))

    memories = s3store.list_memories(uid)
    assert len(memories) == 3

    # test contents are in date order
    assert memories[0].day == 12
    assert memories[1].day == 13
    assert memories[2].day == 14

    memories = s3store.list_memories(uid, start_date=date(2014, 11, 13))
    assert len(memories) == 2

    memories = s3store.list_memories(uid, start_date=date(2014, 11, 13), end_date=date(2014, 11, 13))
    assert len(memories) == 1


@mock_s3
def test_delete_memory():
    s3store = S3DataStore(DEF_CONFIG)
    uid = 'del'
    mem_date = date(2014, 11, 13)

    with pytest.raises(MyDeticNoMemoryFound):
        s3store.delete_memory(uid, mem_date)

    s3store.add_memory(uid, mem_date, MemoryData(text='foo'))
    assert s3store.has_memory(uid, mem_date)

    del_mem = s3store.delete_memory(uid, mem_date)
    assert not s3store.has_memory(uid, mem_date)
    assert del_mem.memory_text == 'foo'







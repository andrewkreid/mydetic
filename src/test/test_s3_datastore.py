import boto
from boto.s3.connection import Location
import boto.exception
from moto import mock_s3

from mydetic.datastore import ExceptionWrappedDataStore
from mydetic.s3_datastore import S3DataStore
from datetime import date
from mydetic.memorydata import MemoryData
from mydetic.mydeticexceptions import MyDeticMemoryAlreadyExists, \
    MyDeticNoMemoryFound, MyDeticDataStoreException, MyDeticMemoryRevisionMismatch
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
    s3store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))

    try:
        s3store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))
        assert False, "Should have thrown exception"
    except MyDeticMemoryAlreadyExists:
        pass


def test_only_mydetic_exceptions_thrown_for_add():
    # don't use moto here because we want things to fail at the S3 level
    try:
        s3store = ExceptionWrappedDataStore(S3DataStore(DEF_CONFIG))
        s3store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))
        assert False, "Should have thrown exception"
    except MyDeticDataStoreException:
        # boto errors should come back wrapped in one of these
        pass


@mock_s3
def test_get_memory():
    s3store = S3DataStore(DEF_CONFIG)

    s3store.add_memory(MemoryData(user_id='foo', memory_text='bar memory', memory_date=date(2014, 11, 12)))
    memory = s3store.get_memory('foo', date(2014, 11, 12))
    assert memory is not None
    assert memory.memory_text == 'bar memory'


def test_only_mydetic_exceptions_thrown_for_get():
    # don't use moto here because we want things to fail at the S3 level
    try:
        s3store = ExceptionWrappedDataStore(S3DataStore(DEF_CONFIG))
        s3store.get_memory('foo', date(2014, 11, 12))
        assert False, "Should have thrown exception"
    except MyDeticDataStoreException:
        # boto errors should come back wrapped in one of these
        pass


@mock_s3
def test_update_memory():
    s3store = S3DataStore(DEF_CONFIG)
    mem_date = date(2013, 11, 12)
    uid = 'foo'
    s3store.add_memory(MemoryData(user_id=uid, memory_date=mem_date, memory_text='bar'))

    memory = s3store.get_memory(uid, mem_date)
    assert memory.memory_text == 'bar'
    assert memory.revision == 1

    with pytest.raises(MyDeticNoMemoryFound):
        s3store.update_memory(MemoryData(user_id=uid, memory_date=date(2012, 1, 1)))

    memory.memory_text = 'changed'
    s3store.update_memory(memory)

    updated_memory = s3store.get_memory(uid, mem_date)
    assert memory.memory_text == updated_memory.memory_text

    # Each save should update the revision count.
    assert updated_memory.revision == 2

    # backdate the revision number. This should make the update fail.
    memory.revision = 1
    with pytest.raises(MyDeticMemoryRevisionMismatch):
        s3store.update_memory(memory)

    # TODO: test that only memory_text and modified_at get updated


def test_only_mydetic_exceptions_thrown_for_update():
    # don't use moto here because we want things to fail at the S3 level
    try:
        s3store = ExceptionWrappedDataStore(S3DataStore(DEF_CONFIG))
        s3store.update_memory(MemoryData(user_id='foo', memory_date=date(2012, 1, 1)))
        assert False, "Should have thrown exception"
    except MyDeticDataStoreException:
        # boto errors should come back wrapped in one of these
        pass


@mock_s3
def test_list_memories():
    s3store = S3DataStore(DEF_CONFIG)
    uid = 'foo'

    memories = s3store.list_memories(uid)
    assert len(memories) == 0

    s3store.add_memory(MemoryData(user_id=uid, memory_text='yet another memory', memory_date=date(2014, 11, 14)))
    s3store.add_memory(MemoryData(user_id=uid, memory_text='memory', memory_date=date(2014, 11, 12)))
    s3store.add_memory(MemoryData(user_id=uid, memory_text='another memory', memory_date=date(2014, 11, 13)))
    s3store.add_memory(MemoryData(user_id='someone else', memory_text='another memory', memory_date=date(2014, 11, 13)))

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


def test_only_mydetic_exceptions_thrown_for_list():
    # don't use moto here because we want things to fail at the S3 level
    try:
        s3store = ExceptionWrappedDataStore(S3DataStore(DEF_CONFIG))
        s3store.list_memories(user_id='foo')
        assert False, "Should have thrown exception"
    except MyDeticDataStoreException:
        # boto errors should come back wrapped in one of these
        pass


@mock_s3
def test_delete_memory():
    s3store = S3DataStore(DEF_CONFIG)
    uid = 'del'
    mem_date = date(2014, 11, 13)

    with pytest.raises(MyDeticNoMemoryFound):
        s3store.delete_memory(uid, mem_date)

    s3store.add_memory(MemoryData(user_id=uid, memory_text='foo', memory_date=mem_date))
    assert s3store.has_memory(uid, mem_date)

    del_mem = s3store.delete_memory(uid, mem_date)
    assert not s3store.has_memory(uid, mem_date)
    assert del_mem.memory_text == 'foo'


def test_only_mydetic_exceptions_thrown_for_delete():
    # don't use moto here because we want things to fail at the S3 level
    try:
        uid = 'del'
        mem_date = date(2014, 11, 13)
        s3store = ExceptionWrappedDataStore(S3DataStore(DEF_CONFIG))
        s3store.delete_memory(uid, mem_date)
        assert False, "Should have thrown exception"
    except MyDeticDataStoreException:
        # boto errors should come back wrapped in one of these
        pass






import boto
from boto.s3.connection import Location
from moto import mock_s3

from mydetic.s3_datastore import S3DataStore
from datetime import date
from mydetic.memorydata import MemoryData
from mydetic.exceptions import MyDeticMemoryAlreadyExists, MyDeticNoMemoryFound
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
        'region': 'foo',
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
    assert s3store.create_bucket_if_required() is not None
    s3store.add_memory('foo', date(2013, 11, 12), MemoryData(memory_text='foo'))

    try:
        s3store.add_memory('foo', date(2013, 11, 12), MemoryData(memory_text='foo'))
        assert False, "Should have throws exception"
    except MyDeticMemoryAlreadyExists:
        pass


@mock_s3
def test_get_memory():
    s3store = S3DataStore(DEF_CONFIG)
    assert s3store.create_bucket_if_required() is not None

    s3store.add_memory('bar', date(2014, 11, 12), MemoryData(memory_text='bar memory'))
    memory = s3store.get_memory('bar', date(2014, 11, 12))
    assert memory is not None
    assert memory.memory_text == 'bar memory'



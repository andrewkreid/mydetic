from mydetic.s3_datastore import S3DataStore
import pytest


def test_validate_config():
    s3_config = {}
    with pytest.raises(ValueError):
        S3DataStore.validate_s3_params(s3_config)

    # missing values
    s3_config = {
        'region': 'foo',
        #'aws_access_key_id': 'foo',
        # 'aws_secret_access_key': 'foo',
        'bucket': 'foo'
    }
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
        'bucket': 'foo'
    }
    S3DataStore.validate_s3_params(s3_config)

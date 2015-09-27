from mydetic.memorydata import MemoryData
from mydetic.mydeticexceptions import MyDeticInvalidMemoryString
from datetime import datetime, date


def test_create_memorydata():
    m = MemoryData(user_id="me", memory_text='foo', memory_date=date(2014, 11, 12))
    assert m.user_id == 'me'
    assert m.memory_text == 'foo'
    assert m.memory_date == date(2014, 11, 12)
    assert m.revision == 1
    assert isinstance(m.created_at, datetime)
    assert isinstance(m.modified_at, datetime)


def test_from_json():
    m = MemoryData.from_json_str("""
    {
        "user_id": "me@me.mail.com",
        "memory_text": "foo",
        "memory_date": "2014-11-12",
        "created_at": "2015-02-15T04:08:26.979808",
        "modified_at": "2015-02-15T04:09:26.979808",
        "revision": 3
    }
    """)
    assert m.user_id == 'me@me.mail.com'
    assert m.memory_text == 'foo'
    assert m.created_at == datetime(2015, 2, 15, 4, 8, 26, 979808)
    assert m.modified_at == datetime(2015, 2, 15, 4, 9, 26, 979808)
    assert m.revision == 3

    try:
        MemoryData.from_json_str('{ "memory_text_wrong": "foo" }')
        assert False, "Should have thrown exception"
    except MyDeticInvalidMemoryString, e:
        assert "memory_text_wrong" in e.msg


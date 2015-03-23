from mydetic.memorydata import MemoryData
from mydetic.datastore import ExceptionWrappedDataStore
from mydetic.sqlite_datastore import SqliteDataStore
from datetime import date
from mydetic.mydeticexceptions import MyDeticMemoryAlreadyExists, MyDeticNoMemoryFound, MyDeticDataStoreException
import unittest
import pytest


class SqliteDataStoreTestCase(unittest.TestCase):
    def test_add_memory(self):
        store = SqliteDataStore(":memory:")
        store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))

        try:
            store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))
            assert False, "Should have thrown exception"
        except MyDeticMemoryAlreadyExists:
            pass

    def test_only_mydetic_exceptions_thrown_for_add(self):
        try:
            store = SqliteDataStore(":memory:")
            store._conn.close()
            wrapped_store = ExceptionWrappedDataStore(store)
            wrapped_store.add_memory(MemoryData(user_id='foo', memory_date=date(2013, 11, 12)))
            assert False, "Should have thrown exception"
        except MyDeticDataStoreException:
            pass

    def test_get_memory(self):
        store = SqliteDataStore(":memory:")

        store.add_memory(MemoryData(user_id='foo', memory_text='bar memory', memory_date=date(2014, 11, 12)))
        memory = store.get_memory('foo', date(2014, 11, 12))
        assert memory is not None
        assert memory.memory_text == 'bar memory'

    def test_only_mydetic_exceptions_thrown_for_get(self):
        try:
            store = SqliteDataStore(":memory:")
            store._conn.close()
            wrapped_store = ExceptionWrappedDataStore(store)
            wrapped_store.get_memory('foo', date(2014, 11, 12))
            assert False, "Should have thrown exception"
        except MyDeticDataStoreException:
            pass

    def test_update_memory(self):
        store = SqliteDataStore(":memory:")
        mem_date = date(2013, 11, 12)
        uid = 'foo'
        store.add_memory(MemoryData(user_id=uid, memory_date=mem_date, memory_text='bar'))

        memory = store.get_memory(uid, mem_date)
        assert memory.memory_text == 'bar'

        with pytest.raises(MyDeticNoMemoryFound):
            store.update_memory(MemoryData(user_id=uid, memory_date=date(2012, 1, 1)))

        memory.memory_text = 'changed'
        store.update_memory(memory)

        updated_memory = store.get_memory(uid, mem_date)
        assert memory.memory_text == updated_memory.memory_text
        assert memory.memory_date == updated_memory.memory_date
        assert memory.created_at == updated_memory.created_at
        assert memory.user_id == updated_memory.user_id
        assert memory.modified_at != updated_memory.modified_at

    def test_only_mydetic_exceptions_thrown_for_update(self):
        try:
            uid = 'foo'
            store = SqliteDataStore(":memory:")
            store._conn.close()
            wrapped_store = ExceptionWrappedDataStore(store)
            wrapped_store.update_memory(MemoryData(user_id=uid, memory_date=date(2012, 1, 1)))
            assert False, "Should have thrown exception"
        except MyDeticDataStoreException:
            pass

    def test_list_memories(self):
        store = SqliteDataStore(":memory:")
        uid = 'foo'

        memories = store.list_memories(uid)
        assert len(memories) == 0

        store.add_memory(MemoryData(user_id=uid, memory_text='yet another memory', memory_date=date(2014, 11, 14)))
        store.add_memory(MemoryData(user_id=uid, memory_text='memory', memory_date=date(2014, 11, 12)))
        store.add_memory(MemoryData(user_id=uid, memory_text='another memory', memory_date=date(2014, 11, 13)))
        store.add_memory(MemoryData(user_id='someone else',
                         memory_text='another memory', memory_date=date(2014, 11, 13)))

        memories = store.list_memories(uid)
        assert len(memories) == 3

        # test contents are in date order
        assert memories[0].day == 12
        assert memories[1].day == 13
        assert memories[2].day == 14

        memories = store.list_memories(uid, start_date=date(2014, 11, 13))
        assert len(memories) == 2

        memories = store.list_memories(uid, start_date=date(2014, 11, 13), end_date=date(2014, 11, 13))
        assert len(memories) == 1

    def test_only_mydetic_exceptions_thrown_for_list(self):
        try:
            uid = 'foo'
            store = SqliteDataStore(":memory:")
            store._conn.close()
            wrapped_store = ExceptionWrappedDataStore(store)
            wrapped_store.list_memories(uid)
            assert False, "Should have thrown exception"
        except MyDeticDataStoreException:
            pass

    def test_delete_memory(self):
        store = SqliteDataStore(":memory:")
        uid = 'del'
        mem_date = date(2014, 11, 13)

        with pytest.raises(MyDeticNoMemoryFound):
            store.delete_memory(uid, mem_date)

        store.add_memory(MemoryData(user_id=uid, memory_text='foo', memory_date=mem_date))
        assert store.has_memory(uid, mem_date)

        del_mem = store.delete_memory(uid, mem_date)
        assert not store.has_memory(uid, mem_date)
        assert del_mem.memory_text == 'foo'

    def test_only_mydetic_exceptions_thrown_for_delete(self):
        try:
            uid = 'foo'
            mem_date = date(2014, 11, 13)
            store = SqliteDataStore(":memory:")
            wrapped_store = ExceptionWrappedDataStore(store)
            store._conn.close()
            wrapped_store.delete_memory(uid, mem_date)
            assert False, "Should have thrown exception"
        except MyDeticDataStoreException:
            pass


def suite():
    return unittest.TestLoader().loadTestsFromTestCase(SqliteDataStore)

if __name__ == "__main__":
    unittest.main()

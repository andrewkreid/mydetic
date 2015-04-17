
import os
import tempfile
import unittest
import json
from mydetic.passwordstore import FilePasswordStore


class FilePasswordStoreTestCase(unittest.TestCase):

    def setUp(self):
        # set up an empty data store with tmp file.
        (self._tmpfilename, self._ds) = self.make_empty_store()

    def tearDown(self):
        os.unlink(self._tmpfilename)
        self._ds = None
        self._tmpfilename = None

    def make_empty_store(self):
        (tmpfd, tmpfilename) = tempfile.mkstemp()
        os.close(tmpfd)
        return tmpfilename, FilePasswordStore(tmpfilename)

    def test_add_entry(self):
        self._ds.set('user', 'password')
        hashed_pwd = self._ds.get('user')
        assert hashed_pwd is not None

    def test_verify(self):
        self._ds.set('user', 'password')
        assert self._ds.verify('user', 'not the password') is False
        assert self._ds.verify('user', 'password') is True

    def test_non_existent_user(self):
        assert not self._ds.verify('mreynolds', 'serenity')

    def test_changed(self):
        self._ds.set('user', 'password')
        assert self._ds.verify('user', 'password') is True
        self._ds.set('user', 'different password')
        assert self._ds.verify('user', 'password') is False
        assert self._ds.verify('user', 'different password') is True

    def test_persisted(self):
        self._ds.set('user1', 'password1')
        self._ds.set('user2', 'password2')
        self._ds.set('user3', 'password3')

        # load the JSON file separately to make sure it's saved
        with open(self._tmpfilename, 'r') as fd:
            raw_data = json.load(fd)
            assert len(raw_data) == 3
            assert 'user3' in raw_data

    def test_deleted(self):
        self._ds.set('user', 'password')
        assert self._ds.verify('user', 'password') is True
        self._ds.remove('user')
        assert self._ds.verify('user', 'password') is False

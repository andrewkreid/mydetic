import json
import os
from passlib.apps import custom_app_context as pwd_context


class PasswordStore:
    """
    Abstract base class for password storage mechanisms
    """

    def __init__(self):
        pass

    def set(self, username, password):
        raise NotImplementedError

    def remove(self, username):
        raise NotImplementedError

    def verify(self, username, password):
        raise NotImplementedError

    def get(self, username):
        raise NotImplementedError

    @staticmethod
    def hash_password(password):
        return pwd_context.encrypt(password)

    @staticmethod
    def verify_password(password, hash):
        return pwd_context.verify(password, hash)


class FilePasswordStore(PasswordStore):
    """
    Super-simple password store that stores username -> hashed password pairs
    in a local JSON file.

    NB: Does not handle concurrent writes
    """

    def __init__(self, filename):
        PasswordStore.__init__(self)
        self._filename = filename
        self._userdata = dict()
        self.load()

    def set(self, username, password):
        self.load()
        self._userdata[username] = PasswordStore.hash_password(password)
        self.save()

    def remove(self, username):
        self.load()
        if username in self._userdata:
            del self._userdata[username]
        self.save()

    def verify(self, username, password):
        self.load()
        if username in self._userdata:
            return PasswordStore.verify_password(password, self._userdata[username])
        return False

    def get(self, username):
        self.load()
        if username in self._userdata:
            return self._userdata[username]
        return None

    def load(self):
        if not os.path.exists(self._filename):
            self._userdata = dict()
        else:
            with open(self._filename, 'r') as fd:
                data_str = fd.read()
                if len(data_str) > 0:
                    self._userdata = json.loads(data_str)

    def save(self):
        with open(self._filename, 'w') as fd:
            self._userdata = json.dump(self._userdata, fd, indent=4)

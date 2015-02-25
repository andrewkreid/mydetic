# Definition of the Sqlite DataStore to the stored memories.
import datetime
import dateutil.parser
import sqlite3
from memorydata import MemoryData
from mydeticexceptions import MyDeticMemoryAlreadyExists, MyDeticNoMemoryFound


# db model
# user_id : string
# memory_date: timestamp
# created_at : timestamp
# modified_at : timestamp
# memory_text : text
class SqliteDataStore:
    """
    Abstract base class for memory data stores
    """

    def __init__(self, db_name="datastore.db"):
        self._conn = sqlite3.connect(
            db_name
            # ,
            # detect_types=sqlite3.PARSE_DECLTYPES | sqlite3.PARSE_COLNAMES
        )
        self._conn.row_factory = sqlite3.Row
        with self._conn:
            self._conn.execute('''
                CREATE TABLE IF NOT EXISTS memories
                (
                    user_id text NOT NULL,
                    memory_date date NOT NULL,
                    memory_text text NOT NULL,
                    created_at date NOT NULL,
                    modified_at date NOT NULL,
                    PRIMARY KEY (user_id, memory_date)
                )''')

    def __del__(self):
        if self._conn:
            self._conn.close()

    def list_memories(self, user_id, start_date=None, end_date=None):
        """
        Get a list of dates that contain memories for a user.
        :param user_id: the User ID
        :type user_id: str or unicode
        :param start_date: optional start date to clamp range to (inclusive)
        :type start_date: datetime.date
        :param end_date: datetime.date optional end date to clamp range to (inclusive)
        :type end_date: datetime.date
        :return: a list of datetime.date in ascending date order where memories exist.
        """
        args = [user_id]
        query = 'SELECT memory_date FROM memories WHERE user_id=?'
        if start_date:
            query += " AND memory_date >=?"
            args.append(start_date)
        if end_date:
            query += " AND memory_date <=?"
            args.append(end_date)
        query += " ORDER BY memory_date ASC"
        with self._conn:
            sql = self._conn.execute(query, args).fetchall()
        dates = []
        for date in sql:
            dates.append(dateutil.parser.parse(date["memory_date"]).date())
        return dates

    def get_memory(self, user_id, memory_date):
        """
        :param user_id: str the User ID
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: The memory
        :raises MyDeticNoMemoryFound if there isn't a memory on this day
        """
        args = [user_id, memory_date]
        query = '''
            SELECT
                user_id,
                memory_date,
                modified_at,
                created_at ,
                memory_text
            FROM
                memories
            WHERE
                user_id=?
                AND memory_date=?
        '''
        with self._conn:
            sql = self._conn.execute(query, args).fetchone()
        if not sql:
            raise MyDeticNoMemoryFound(user_id, memory_date)
        else:
            return MemoryData.from_dict(dict(sql))
            # return self._sql_to_memory(sql)

    def has_memory(self, user_id, memory_date):
        """
        Return whether a memory exists for a user at a date.
        :param user_id:
        :type user_id: str or unicode
        :param memory_date:
        :type memory_date: datetime.date
        :return: True if a memory exists, false otherwise.
        """
        args = [user_id, memory_date]
        query = 'SELECT memory_date FROM memories WHERE user_id=? AND memory_date=?'
        with self._conn:
            sql = self._conn.execute(query, args).fetchone()
        if not sql:
            return False
        else:
            return True

    def add_memory(self, memory):
        """
        :param memory:
        :raises MyDeticMemoryAlreadyExists
        :return:
        """
        query = '''
            INSERT INTO
                memories
                (
                    user_id,
                    memory_date,
                    modified_at,
                    created_at,
                    memory_text
                )
                VALUES
                ( ?, ?, ?, ?, ?)
        '''
        if self.has_memory(memory.user_id, memory.memory_date):
            raise MyDeticMemoryAlreadyExists(memory.user_id, memory.memory_date)
        args = [
            memory.user_id,
            memory.memory_date,
            memory.modified_at,
            memory.created_at,
            memory.memory_text
        ]
        try:
            with self._conn:
                self._conn.execute(query, args)
        except sqlite3.IntegrityError:
            raise MyDeticMemoryAlreadyExists(memory.user_id, memory.memory_date)

    def update_memory(self, memory):
        """
        :param memory: updated MemoryData object. NOTE: only text is changed.
        :return: No return value
        :raises: MyDeticNoMemoryFound is memory doesn't already exist
        """

        if not self.has_memory(memory.user_id, memory.memory_date):
            raise MyDeticNoMemoryFound(memory.user_id, memory.memory_date)
        args = [memory.memory_text, datetime.datetime.now(), memory.user_id, memory.memory_date]
        query = '''
            UPDATE
                memories
            SET
                memory_text = ?,
                modified_at = ?
            WHERE
                user_id=?
                AND memory_date=?
        '''
        with self._conn:
            self._conn.execute(query, args)

    def delete_memory(self, user_id, memory_date):
        """
        :param user_id:
        :param memory_date:
        :return: the deleted memory
        :raises: MyDeticNoMemoryFound
        """
        memory = self.get_memory(user_id, memory_date)
        args = [user_id, memory_date]
        query = 'DELETE FROM memories WHERE user_id=? AND memory_date=?'
        with self._conn:
            self._conn.execute(query, args)
        return memory

    @staticmethod
    def _sql_to_memory(sql):
        return MemoryData(
            sql["user_id"],
            datetime.date(sql["memory_date"]).date(),
            sql["memory_text"],
            datetime.datetime(sql["created_at"]),
            datetime.datetime(sql["modified_at"])
        )

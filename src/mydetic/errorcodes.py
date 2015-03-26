# Public facing error codes for the REST API and a short description
#

# Any input validation failure that's the caller's fault.
INVALID_INPUT = 100

# Failure to read/write data from/to the data store
DATASTORE_FAILURE = 101

# Logically invalid data operation (eg deleting a memory that doesn't exist or adding one that's
# already there).
INVALID_DATA_OPERATION = 102

# Data read from the data store is corrupted/in the incorrect format.
INVALID_DATA_FORMAT = 103

# Fallback code. It would be implolite to use this if you actually know
# what went wrong
UNEXPLAINED_FAILURE = 999

error_descs = {
    INVALID_INPUT: "Invalid input",
    DATASTORE_FAILURE: "Datastore Error",
    INVALID_DATA_OPERATION: "Invalid Data Operation",
    INVALID_DATA_FORMAT: "Invalid Data Format",
    UNEXPLAINED_FAILURE: "Unexplained Failure"
}

